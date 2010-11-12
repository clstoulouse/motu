#! /usr/bin/env python
# -*- coding: utf-8 -*-

#
# Motu, a high efficient, robust and Standard compliant Web Server for Geographic
#  Data Dissemination.
# 
#  http://cls-motu.sourceforge.net/
# 
#  (C) Copyright 2009-2010, by CLS (Collecte Localisation Satellites) -
#  http://www.cls.fr - and Contributors
# 
# 
#  This library is free software; you can redistribute it and/or modify it
#  under the terms of the GNU Lesser General Public License as published by
#  the Free Software Foundation; either version 2.1 of the License, or
#  (at your option) any later version.
# 
#  This library is distributed in the hope that it will be useful, but
#  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
#  or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
#  License for more details.
# 
#  You should have received a copy of the GNU Lesser General Public License
#  along with this library; if not, write to the Free Software Foundation,
#  Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307, USA.

import urllib
import urllib2
import traceback
import platform
import sys
import httplib
import HTMLParser
import os
import re
import tempfile
import datetime
import shutil
import zipfile
import cookielib
import logging
import logging.config


# the config file to load from 
CFG_FILE = '~/py_motu_gateway'
MESSAGES_FILE = './etc/messages.properties'
LOG_CFG_FILE = './etc/log.ini'

_GEOGRAPHIC = False
_VERTICAL   = False
_TEMPORAL   = False
_PROXY      = False

_opener = None
_messages = None

log = None

#===============================================================================
# Version
#===============================================================================

def get_client_version():
    return '${project.version}'

#===============================================================================
# print_url
#===============================================================================
def print_url(url):
    urls = url.split('?')
    log.debug( urls[0] )
    if len(urls) > 1:
        for a in sorted(urls[1].split('&')):
            log.debug( '\t' + a.split('=') )

class HTTPDebugProcessor(urllib2.BaseHandler):
    """ Track HTTP requests and responses with this custom handler.
    """
    def __init__(self, httpout=sys.stdout):
        self.httpout = httpout

    def http_request(self, request):
        host, full_url = request.get_host(), request.get_full_url()
        url_path = full_url[full_url.find(host) + len(host):]
        log.log( logging.NOTSET, "zob %s\n", request.get_full_url())
        self.httpout.write('\n')
        self.httpout.write("%s %s\n" % (request.get_method(), url_path))

        for header in request.header_items():
            self.httpout.write("%s: %s\n" % header[:])

        self.httpout.write('\n')

        return request

    def http_response(self, request, response):
        code, msg, hdrs = response.code, response.msg, response.info()
        self.httpout.write("HTTP/1.x %s %s\n" % (code, msg))
        self.httpout.write(str(hdrs))

        return response


#===============================================================================
# open_url
#===============================================================================
def open_url(*args, **kargs):
    global _opener, _PROXY
    if _opener is None:    
        # common handlers
        handlers = [urllib2.HTTPCookieProcessor(cookielib.CookieJar()),
                    urllib2.HTTPHandler(),
                    urllib2.HTTPSHandler(),
                    HTTPErrorProcessor(),
                    HTTPDebugProcessor()
                   ]

        # add handlers for managing proxy credentials if necessary        
        if _PROXY:
            # extract protocol
            url = get_option(key = 'proxy-server').partition(':')
            handlers.append( urllib2.ProxyHandler({url[0]:url[2]}) )
            if (get_option(key = 'proxy-user') != 'None'):
                proxy_auth_handler = urllib2.HTTPBasicAuthHandler()
                proxy_auth_handler.add_password('realm', get_option(key = 'proxy-user'), 'username', get_option(key = 'proxy-pwd'))
                handlers.append(proxy_auth_handler)
        _opener = urllib2.build_opener(*handlers)
        log.debug( 'handlers:\n\t\t' + '\n\t\t'.join(str(h) for h in _opener.handlers))

    kargs['headers'] = {"Accept": "text/plain", "X-Client-Id" : "motu-client-python", "X-Client-Version" : "1.0.0"}

    r = urllib2.Request(*args, **kargs)
  
    # open the url, but let the exception propagates to the caller  
    return _opener.open(r)

#===============================================================================
# encode
#===============================================================================
def encode(**kargs):
    opts = []
    for k, v in kargs.iteritems():
        opts.append('%s=%s' % (str(k), str(v)))
    return '&'.join(opts)



#===============================================================================
# HTTPErrorProcessor
#===============================================================================
class HTTPErrorProcessor(urllib2.HTTPErrorProcessor):
    def https_response(self, request, response):
        # Consider error codes that are not 2xx (201 is an acceptable response)
        code, msg, hdrs = response.code, response.msg, response.info()
        if code >= 300: 
            response = self.parent.error('http', request, response, code, msg, hdrs)
        return response

#===============================================================================
# FounderParser
#===============================================================================
class FounderParser(HTMLParser.HTMLParser):
    """
    Parser witch found the form/action section an return it
    """
    def __init__(self, *args, **kargs):
        HTMLParser.HTMLParser.__init__(self, *args, **kargs)
        self.action_ = None

    def handle_starttag(self, tag, attrs):
        d = dict(attrs)
        if tag == 'form' and 'action' in d:
            self.action_ = d['action']


#===============================================================================
# load_options
#===============================================================================
def load_options():
    '''
    load options to handle
    '''

    add_option(key = 'quiet',
                       help = "prevent any output in stdout",
                       action = 'store_const',
                       const = logging.WARN,
                       dest='level',
                       inline = ('--quiet','-q'))

    add_option(key = 'verbose',
                       help = "print information in stdout",
                       action='store_const',
                       const = logging.DEBUG,
                       dest='level',
                       inline = ('--verbose',))
 
    add_option(key = 'noisy',
                       help = "print more information (traces) in stdout",
                       action='store_const',
                       const = logging.NOTSET,
                       dest='level',
                       inline = ('--noisy',)) 
                       
    add_option(key = 'user',
                       help = "the user name",
                       inline = ('--user', '-u'))

    add_option(key = 'pwd',
                       help = "the user password",
                       inline = ('--pwd', '-p'))

    add_option(key = 'proxy-server',
                       help = "the proxy server",
                       inline = ('--proxy-server',))                       

    add_option(key = 'proxy-user',
                       help = "the proxy user",
                       inline = ('--proxy-user',))                       

    add_option(key = 'proxy-pwd',
                       help = "the proxy password",
                       inline = ('--proxy-pwd',))                       
                       
    add_option(key = 'motu',
                       help = "the motu server to use",
                       inline = ('--motu', '-m'))

    add_option(key = 'service-id',
                       help = "The service identifier",
                       inline = ('--service-id', '-s'))
                              
    add_option(key = 'product-id',
                       help = "The product (data set) to download",
                       inline = ('--product-id', '-d'))
               

    add_option(key = 'date-min',
                       help = "The min date (YYYY-MM-DD)",
                       inline = ('--date-min', '-t'))

    add_option(key = 'date-max',
                       help = "The max date (YYYY-MM-DD)",
                       inline = ('--date-max', '-T'),
               default = datetime.date.today().isoformat())
               
    add_option(key = 'latitude-min',
                       help = "The min latitude [-90 ; 90]",
                       inline = ('--latitude-min', '-y'))

    add_option(key = 'latitude-max',
                       help = "The max latitude [-90 ; 90]",
                       inline = ('--latitude-max', '-Y'))
               
    add_option(key = 'longitude-min',
                       help = "The min longitude [-180 ; 180]",
                       inline = ('--longitude-min', '-x'))

    add_option(key = 'longitude-max',
                       help = "The max longitude [-180 ; 180]",
                       inline = ('--longitude-max', '-X'))
               
    add_option(key = 'depth-min',
                       help = "The min depth [0 ; 2e31]",
                       inline = ('--depth-min', '-z'))

    add_option(key = 'depth-max',
                       help = "The max depth [0 ; 2e31]",
                       inline = ('--depth-max', '-Z'))

    add_option(key = 'variable',
                       help = "The variable",
                       action="append",
                       inline = ('--variable', '-v'))
                       
    add_option(key = 'out-dir',
                       help = "The output dir",
                       inline = ('--out-dir', '-o'),
                       default=".")
               
    add_option(key = 'out-name',
                       help = "The output file name",
                       inline = ('--out-name', '-f'),
                       default="data.nc")



#===============================================================================
# format_date
#===============================================================================
def format_date(date):
    """
    Format JulianDay date in unix time
    """
    return date.isoformat()


#===============================================================================
# get_product
#===============================================================================
def get_product():
    """
    Return the product string
    """
    # '#' is a special character in url, so we have to encode it
    return get_option(key = 'product-id').replace('#', '%23' )
    
    
#===============================================================================
# get_product
#===============================================================================
def get_service():
    """
    Return the service string
    """
    # '#' is a special character in url, so we have to encode it
    return get_option(key = 'service-id').replace('#', '%23' )


#===============================================================================
# build_url
#===============================================================================
def build_url():
    global _GEOGRAPHIC, _VERTICAL, _TEMPORAL
    temporal = ''
    geographic = ''
    vertical = ''
    other_opt = ''
    

    """
    Build the main url to connect too
    """
    opts = encode(action = 'productdownload',
                   mode = 'console',
                   service = get_service(),
                   product = get_option(key = 'product-id'),
                   )
    

    if _GEOGRAPHIC:
        geographic = '&' + encode(x_lo = get_option(key = 'latitude-min'),
                x_hi = get_option(key = 'latitude-max'),
                y_lo = get_option(key = 'longitude-min'),
                y_hi = get_option(key = 'longitude-max'),
                )
    
    if _VERTICAL:
        vertical = '&' + encode(z_lo = get_option(key = 'depth-min'),
                z_hi = get_option(key = 'depth-max'),
                )
    
    if _TEMPORAL:
        # we change date types
        date_max = get_option(key = 'date-max')
        if isinstance(date_max, basestring):
            date_max = datetime.date(*(int(x) for x in date_max.split('-')))
        
        date_min = get_option(key = 'date-min')
        if date_min is None or date_min == 'None':
            date_min = date_max - datetime.timedelta(20)
        elif isinstance(date_min, basestring):
            date_min = datetime.date(*(int(x) for x in date_min.split('-')))
        
        temporal = '&' + encode(t_lo = format_date(date_min),
                t_hi = format_date(date_max),
                )

    variable = get_attr_option(key = 'variable')
    if variable is not None:
        for i, opt in enumerate(variable):
            other_opt = other_opt + '&variable='+opt
    
    return opts + temporal + geographic + vertical + other_opt

#===============================================================================
# check_options
#===============================================================================
def check_options():
    global _GEOGRAPHIC, _VERTICAL, _TEMPORAL, _PROXY
    """
    Check Mandatory Options
    """
    if get_option(key = 'quiet') == True and get_option(key = 'verbose' ) == True:
        raise Exception('"quiet" option and "verbose option" can not both be set')
        
    if get_option(key = 'user') == 'None' :
        raise Exception(getExternalMessages()['motu-client.exception.missing-user'])

    if get_option(key = 'pwd') == 'None' :
        raise Exception(getExternalMessages()['motu-client.exception.missing-pwd'])
    
    if get_option(key = 'motu') == 'None' :
        raise Exception(getExternalMessages()['motu-client.exception.missing-motu'])
    
    if get_option(key = 'service-id') == 'None' :
        raise Exception(getExternalMessages()['motu-client.exception.missing-serviceid'])
    
    if get_option(key = 'product-id') == 'None' :
        raise Exception(getExternalMessages()['motu-client.exception.missing-productid'])
    
    if get_option(key = 'out-dir') == 'None' :
        raise Exception(getExternalMessages()['motu-client.exception.missing-outdir'])
    
    out_dir = get_option(key = 'out-dir')
    
    # check directory existance
    if not os.path.exists(out_dir):
        raise Exception(getExternalMessages()['motu-client.exception.outdir-notexist'] % out_dir)
    # check whether directory is writable or not
    if not os.access(out_dir, os.W_OK):
        raise Exception(getExternalMessages()['motu-client.exception.outdir-notwritable'] % out_dir)
    
    if get_option(key = 'out-name') == 'None' :
        raise Exception('Missing out-name')

    """
    Check PROXY Options
    """
    if get_option(key='proxy-server' ) != 'None':
        _PROXY = True
        # check that proxy server is a valid url
        url = get_option(key='proxy-server' )
        p = re.compile('^(ftp|http|https):\/\/(\w+:{0,1}\w*@)?(\S+)(:[0-9]+)?(\/|\/([\w#!:.?+=&%@!\-\/]))?')
        m = p.match(url)
        if not m :
            raise Exception( 'Bad url scheme for proxy server: "%s"' % url )
        # check that if proxy-user is defined then proxy-pwd shall be also, and reciprocally.
        if (get_option(key = 'proxy-user') != 'None') != ( get_option(key = 'proxy-pwd') != 'None' ) :
            raise Exception( 'both proxy user and proxy password must be defined' )
    
        
    """
    Check VERTICAL Options
    """
    if get_option(key = 'depth-min') != 'None' and get_option(key = 'depth-max') != 'None' :
        _VERTICAL = True
        tempvalue = float(get_option(key = 'depth-min'))
        if tempvalue < 0 :
            raise Exception('Vertical parameter depth-min is out of range for "%s"' % tempvalue)
        tempvalue = float(get_option(key = 'depth-max'))
        if tempvalue < 0 :
            raise Exception('Vertical parameter depth-max is out of range for "%s"' % tempvalue)
        
    
    """
    Check TEMPORAL Options
    """
    if get_option(key = 'date-min') != 'None' and get_option(key = 'date-max') != 'None' :
        _TEMPORAL = True
    
    
    """
    Check GEOGRAPHIC Options
    """
    if get_option(key = 'latitude-min') != 'None' and get_option(key = 'latitude-max') != 'None' and get_option(key = 'longitude-min') != 'None' and get_option(key = 'longitude-max') != 'None' :
        _GEOGRAPHIC = True
        tempvalue = float(get_option(key = 'latitude-min'))
        if tempvalue < -90 or tempvalue > 90 :
            raise Exception('Geographic parameter latitude-min is out of range for "%s"' % tempvalue)
        tempvalue = float(get_option(key = 'latitude-max'))
        if tempvalue < -90 or tempvalue > 90 :
            raise Exception('Geographic parameter latitude-max is out of range for "%s"' % tempvalue)
        tempvalue = float(get_option(key = 'longitude-min'))
        if tempvalue < -180 or tempvalue > 180 :
            raise Exception('Geographic parameter longitude-min is out of range for "%s"' % tempvalue)
        tempvalue = float(get_option(key = 'longitude-max'))
        if tempvalue < -180 or tempvalue > 180 :
            raise Exception('Geographic parameter longitude-max is out of range for "%s"' % tempvalue)
        
        
    elif get_option(key = 'latitude-min') != 'None' or get_option(key = 'latitude-max')!= 'None' or get_option(key = 'longitude-min') != 'None' or get_option(key = 'longitude-max') != 'None' :
        #raise exception missing one parameter
        raise Exception('Missing one or more Geographic parameter')
    
    
#===============================================================================
# get_ticket
#===============================================================================
def get_ticket(url, main_url, usr, pwd):
    """
    Return the ticket for the download
    """
    url = urllib.unquote(url)

    m = re.search('(https://.+/cas)', url)
    if m is None:
        raise Exception('Unable to find the URL of the CAS server')
    url_cas = m.group(1) + '/v1/tickets'

    log.debug( "url cas:\t%s" %  url_cas )

    opts = encode(username = usr,
                  password = pwd)
    connexion = open_url(url_cas, opts)

    fp = FounderParser()
    for line in connexion:
        fp.feed(line)

    url_ticket = fp.action_
    if url_ticket is None:
        raise Exception('Unable to find the form to get the Ticket Granting Ticket (TGT)')

    log.debug( "url ticket:" )
    print_url(url_ticket)
    log.debug( "url service:" )
    print_url(url)
    log.debug( "main url:" )
    print_url(main_url)

    opts = encode(service = main_url)
    return open_url(url_ticket, opts).readline()


#===============================================================================
# dl_2_file
#===============================================================================
def dl_2_file(url, ticket, fh):
    """
    Download the file with the main url (of Motu), the ticket and the destination
    file.
    Motu can return an error message in the response stream without setting an
    appropriate http error code. So, in that case, the content-type response is
    checked, and if it is text/plain, we consider this as an error.    
    """
    global VERBOSE, QUIET
    
    size = -1
    bs = 2048*8
    read = 0
    blocknum = 0
    
    log.info( "Begining to write file: %s " % os.path.abspath(fh) )

    dl_url = url + '&ticket=' + ticket
    
    try:
      temp = open(fh, 'w+b')       
      try:
        m = open_url(dl_url)
        # check that content type is not text/plain
        headers = m.info()
        if "Content-Type" in headers:
          if headers['Content-Type'] == 'text/plain':
             raise Exception('Motu server failed to process the request. Response returned is the following:\n%s' % m.read() )
          
          log.info( 'File type: %s' % headers['Content-Type'] )
        
        if "Content-Length" in headers:
          log.info( 'File size: %s' % headers['Content-Length'] )
          size = int(headers["Content-Length"])     
        else:
          log.info( 'File size: %s' % 'unknown' )
        
        log.info( 'Downloding file...')
        
        while 1:
           block = m.read(bs)
           if block == "":
               break;
           read += len(block)
           blocknum += 1;
           if True:
               percent = int(blocknum*bs*100/size)
               log.info( str(blocknum*bs ) + '/' + str(size) + '  (' + str(percent) + '%)' )
           
      finally:
        m.close()
    finally:
      temp.flush()
      temp.close()

    # raise exception if actual size does not match content-length header
    if size >= 0 and read < size:
        raise ContentTooShortError("Dataset retrival incomplete. Got only %i out "
                                    "of %i bytes" % (read, size), result)

#===============================================================================
# main
#===============================================================================
loaded = False
def main():
    """
    the main function
    """
    global loaded, log
    
    # first initialize the logger
    logging.config.fileConfig(  os.path.join(os.path.dirname(__file__),LOG_CFG_FILE) )
    log = logging.getLogger("motu-client-python")

    
    if not loaded:
        # we prepare options we want
        load_options()
        # we load the configuration
        load_config(os.path.expanduser(CFG_FILE))
        loaded = True
        
    # we put the verbose option before check parameters
    if eval(unicode(get_option(key = 'noisy'))):       
       log.setLevel(logging.NOSET)
    elif eval(unicode(get_option(key = 'verbose'))):
       log.setLevel(logging.DEBUG)
    elif eval(unicode(get_option(key = 'quiet'))):
       log.setLevel(logging.WARN)
    else: 
       log.setLevel(logging.INFO)
       
    print 'ici :', get_attr_option(key='lel')
    
    # then we check given options are ok
    check_options()

    # we build the url
    main_cas_url = get_option(key = 'motu') + urllib.quote_plus(build_url())

    main_url = get_option(key = 'motu') + build_url()
    
    
    log.info( 'Main URL:%s' % main_url )
    
    # now we connect
    # if a problem append, an exception is thrown
    log.info("Contacting server")
    
    connexion = open_url(main_url)

    log.info( 'Authenticating user %s' % get_option('user') )
    ticket = get_ticket(connexion.url,
                        main_cas_url,
                        get_option('user'),
                        get_option('pwd'))
    log.debug( 'ticket: %s' % ticket )
    
    fh = os.path.join(get_option(key = 'out-dir'),get_option(key = 'out-name'))
    dl_2_file(main_url, ticket, fh)

#===============================================================================
# external messages
#===============================================================================
def getExternalMessages():
    global _messages
    if _messages is None:
        propFile= file( os.path.join(os.path.dirname(__file__),MESSAGES_FILE), "rU" )
        propDict= dict()
        for propLine in propFile:
            propDef= propLine.strip()
            if len(propDef) == 0:
                continue
            if propDef[0] in ( '!', '#' ):
                continue
            punctuation= [ propDef.find(c) for c in ':= ' ] + [ len(propDef) ]
            found= min( [ pos for pos in punctuation if pos != -1 ] )
            name= propDef[:found].rstrip()
            value= propDef[found:].lstrip(":= ").rstrip()
            propDict[name]= value
        propFile.close()
        _messages = propDict
    return _messages

#===============================================================================
# options module... included
#===============================================================================

import ConfigParser
import optparse

_SECTION_GENERAL = 'Main'
_options = None
_arguments = None
_parser = optparse.OptionParser(version=get_client_version())
_conf_parser = ConfigParser.SafeConfigParser()
_all_opt = {}


def load_config(path):
    global _options, _arguments
    if _options is None and _arguments is None:
        _options, _arguments = _parser.parse_args()

    if os.path.exists(path) and os.path.isfile(path):
        _conf_parser.read(path)


def add_option(key = None, section = None, **kargs):
    """
    Ajoute les options à traiter par plog.
    
    ces options doivent être de la forme:
    section: la section a utiliser (Main par defaut)
    nom_option: le nom de l'option (avec lequel on la récupère)
    val: arguments

    Les arguments sont ceux de optparse et d'autres:
        inline: la liste des options en ligne de commande:
            (-a, --all)
        help: le message d'aide
        type: le type de la variable
        default: la valeur par default
        action: l'action de stockage (cf optparse)
        
    """
    section = section or _SECTION_GENERAL
    _all_opt.setdefault(section, {}).setdefault(key, {}).update(kargs)

    if kargs.get('inline') and len(kargs.get('inline')) > 0:
        d = dict()
        d.update(kargs)
        d.update(dest = key)
        d.pop('inline')
        _parser.add_option(*(kargs.get('inline')), **d)

    if not _conf_parser.has_section(section):
        _conf_parser.add_section(section)
    _conf_parser.set(section, key, str(kargs.get('default')))



def add_section_option(dic = None, **kargs):
    """
    Ajoute les options à traiter par plog.
    
    ces options doivent être de la forme:
    {section -> {nom_option -> {arguments}}}

    Les arguments sont ceux de optparse et d'autres:
        inline: la liste des options en ligne de commande:
            (-a, --all)
        help: le message d'aide
        type: le type de la variable
        default: la valeur par default
        action: l'action de stockage (cf optparse)
        
    """
    def _add_section(d):
        for s, d in dic.iteritems():
            for k, v in d.iteritems():
                add_option(section = s, key = k, **v)

    if dic is not None:
        _add_section(dic)
    if len(kargs) > 1:
        _add_section(kargs)



def get_default(key, section = None):
    """
    Return the default value
    """
    section = section or _SECTION_GENERAL
    if key in _all_opt[section]:
       return _all_opt[section][key].get('default')
    else:
       return None


def get_option(key, section = None):
    """
    Return the option value
    """
    section = section or _SECTION_GENERAL
    default = get_default(key, section)

    opt = None
    if hasattr(_options, key):
        opt = getattr(_options, key)

    if opt is None or opt == default:
        opt = _conf_parser.get(section, key)

    return opt

def get_attr_option(key, section = None):
    """
    Return the attribut option value
    """
    section = section or _SECTION_GENERAL
    default = get_default(key, section)

    opt = None
    if hasattr(_options, key):
        opt = getattr(_options, key)

    return opt


#===============================================================================
# The Main function
#===============================================================================
if __name__ == '__main__':
    try:
        main()
    except Exception, e:
        print "Execution failed:\n %s" % e
        if hasattr(e, 'reason'):
          print '\t- reason: ', e.reason
        if hasattr(e, 'code'):
          print '\t- code  : ', e.code  
        if hasattr(e, 'read'):
          print '\t- detail:\n', e.read()
       
        print
        print "Stack trace exception is detailed herafter:"
        print '-'*60
        print traceback.print_exc(e)
        print '-'*60
        print 'System info is provided hereafter:'
        system, node, release, version, machine, processor = platform.uname()
        print 'system   : %s' % system
        print 'node     : %s' % node
        print 'release  : %s' % release
        print 'version  : %s' % version
        print 'machine  : %s' % machine
        print 'processor: %s' % processor
        print 'python   : %s' % sys.version
        print 'client   : %s' % get_client_version()
        print '-'*60


