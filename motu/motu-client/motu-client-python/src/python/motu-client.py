#! /usr/bin/env python
# -*- coding: utf-8 -*-
#

import urllib
import urllib2
try:
    from cls.utils import exceptions
except ImportError:
    pass
else:
    # for verbose exceptions handling
    exceptions.change_except_hook()

import HTMLParser
import os
import re
import tempfile
import datetime
import shutil
import zipfile
import cookielib

# the config file to load from 
CFG_FILE = '~/.py_sltac_gateway'
# the suffix to add at the gateway
END_REQUEST = '/Motu?'
# The product prefix 
MY_OCEAN = 'http://purl.org/myocean/ontology/individual/myocean'
# Verbose Mode
VERBOSE = False

_opener = None

#===============================================================================
# print_url
#===============================================================================
def print_url(url):
    urls = url.split('?')
    print urls[0]
    if len(urls) > 1:
        for a in sorted(urls[1].split('&')):
            print '\t', a.split('=')

#===============================================================================
# open_url
#===============================================================================
def open_url(*args, **kargs):
    global _opener, VERBOSE
    if _opener is None:
        _opener = urllib2.build_opener(
            urllib2.HTTPCookieProcessor(
                cookielib.CookieJar()))
        if VERBOSE:
            print '\thandlers:\n\t\t', '\n\t\t'.join(
                str(h) for h in _opener.handlers)

    if VERBOSE:
        print '\tOpening url:\n\t\targs:\n\t\t\t', '\n\t\t\t'.join(str(a) for a in args)
        print '\t\tkargs:\n\t\t\t', '\n\t\t\t'.join('%s\t%s' % (k, v) for k, v in kargs.iteritems())
        print '\tWith cookiers:\t\t', _opener.handlers[8].cookiejar
        print

    kargs['headers'] = {"Accept": "text/plain"}

    r = urllib2.Request(*args, **kargs)

    try:
        return _opener.open(r)
    except urllib2.HTTPError, he:
        if VERBOSE:
            print he.code
            print he.msg
            for l in he.fp:
                print l
            print he.filename
        raise he


#===============================================================================
# encode
#===============================================================================
def encode(**kargs):
    opts = []
    for k, v in kargs.iteritems():
        opts.append('%s=%s' % (str(k), str(v)))
    return '&'.join(opts)


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
    add_option(key = 'uncompress',
                       help = "If true, data will be uncompressed",
                       action = 'store_true',
                       default = False,
                       inline = ('--uncompress', '-z'))

    add_option(key = 'verbose',
                       help = "print information in stdout",
                       action = 'store_true',
                       default = False,
                       inline = ('--verbose',))

    add_option(key = 'user',
                       help = "the user name",
                       inline = ('--user', '-u'))

    add_option(key = 'pwd',
                       help = "the user password",
                       inline = ('--pwd', '-p'))

    add_option(key = 'gateway',
                       help = "the gateway to use",
                       inline = ('--gateway', '-g'))

    add_option(key = 'service',
                       help = "The service name",
                       inline = ('--service', '-s'))

    add_option(key = 'date_min',
                       help = "The min date (YYYY-MM-DD)",
                       inline = ('--date-min', '-t'))

    add_option(key = 'date_max',
                       help = "The max date (YYYY-MM-DD)",
                       inline = ('--date-max', '-T'),
                       default = datetime.date.today().isoformat())

    add_option(key = 'product',
                       help = "The product (data set) to download",
                       inline = ('--product', '-d'))

    add_option(key = 'out_dir',
                       help = "The output dir",
                       inline = ('--out', '-o'))



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
def get_product(product):
    """
    Return the product/service string
    """
    return '%s%%23%s' % (MY_OCEAN, product)


#===============================================================================
# build_url
#===============================================================================
def build_url(gateway, service, product, date_min, date_max):
    """
    Build the main url to connect too
    """
    opts = encode(action = 'productdownload',
                   mode = 'console',
                   service = get_product(service),
                   product = get_product(product),
                   t_lo = format_date(date_min),
                   t_hi = format_date(date_max),
                   )
    return gateway + END_REQUEST + opts


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

    if VERBOSE:
        print "url cas:\t", url_cas

    opts = encode(username = usr,
                  password = pwd)
    connexion = open_url(url_cas, opts)

    fp = FounderParser()
    for line in connexion:
        fp.feed(line)

    url_ticket = fp.action_
    if url_ticket is None:
        raise Exception('Unable to find the form to get the Ticket Granting Ticket (TGT)')

    if VERBOSE:
        print "url ticket:\t",
        print_url(url_ticket)
        print "url service:\t",
        print_url(url)
        print "main url:\t",
        print_url(main_url)

    opts = encode(service = main_url)
    return open_url(url_ticket, opts).readline()


#===============================================================================
# dl_2_file
#===============================================================================
def dl_2_file(url, ticket, fh):
    """
    Download the file with the main url,
    the ticket and the destination zip file
    """
    dl_url = url + '&ticket=' + ticket
    m = open_url(dl_url)
    for l in m:
        fh.write(l)
    fh.flush()


#===============================================================================
# main
#===============================================================================
loaded = False
def main():
    """
    the main function
    """
    global loaded, VERBOSE
    if not loaded:
        # we prepare options we want
        load_options()
        # we load the configuration
        load_config(os.path.expanduser(CFG_FILE))
        loaded = True

    VERBOSE = eval(unicode(get_option(key = 'verbose')))

    # we change date types
    date_max = get_option(key = 'date_max')
    if isinstance(date_max, basestring):
        date_max = datetime.date(*(int(x) for x in date_max.split('-')))

    date_min = get_option(key = 'date_min')
    if date_min is None or date_min == 'None':
        date_min = date_max - datetime.timedelta(20)
    elif isinstance(date_min, basestring):
        date_min = datetime.date(*(int(x) for x in date_min.split('-')))

    # we build the url
    main_url = build_url(get_option(key = 'gateway'),
                         get_option(key = 'service'),
                         get_option(key = 'product'),
                         date_min,
                         date_max)
    # now we connect
    # if a problem append, an exception is thrown
    connexion = open_url(main_url)

    ticket = get_ticket(connexion.url,
                        main_url,
                        get_option('user'),
                        get_option('pwd'))
    if VERBOSE:
        print 'ticket:\t', ticket

    with tempfile.NamedTemporaryFile(suffix = 'zip') as fh:
        namezip = fh.name
        dl_2_file(main_url, ticket, fh)

        out_dir = get_option(key = 'out_dir')
        if not os.path.exists(out_dir):
            raise exceptions.UncorrectValueError(
                'Directory : "%s" does not exist' % out_dir)

        uncompress = eval(unicode(get_option(key = 'uncompress')))
        if uncompress:
            zip = zipfile.ZipFile(namezip)
            zip.extractall(out_dir)
        else:
            shutil.copy(namezip, out_dir)



#===============================================================================
# options module... included
#===============================================================================

import ConfigParser
import optparse

_SECTION_GENERAL = 'Main'
_options = None
_arguments = None
_parser = optparse.OptionParser()
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
    return _all_opt[section][key].get('default')


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

#===============================================================================
# The Main function
#===============================================================================
if __name__ == '__main__':
    main()
