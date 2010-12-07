#!/usr/bin/env perl

#
# Perl motu client v.${project.version} 
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

use strict;
use warnings;
use Carp;
use Date::Manip;
use English qw(-no_match_vars);
use File::Basename;
use File::stat;
use File::Temp qw(tempdir);
use Getopt::Long qw(:config no_ignore_case bundling);
use HTTP::Cookies;
use HTML::TokeParser;
use LWP::UserAgent;
use Readonly;
use XML::Simple;
use File::Spec;

=pod

=head1 NAME

motu-client.pl - Download MOTU products via the MIS-Gateway

=head1 SYNOPSIS

This program can be integrated into a processing chain in order to automate the
downloading of MOTU products via the MIS-Gateway.

=head1 ALGORITHM

The communication algorithm with the MIS-Gateway is the following:

=over 2

=item *

The program builds a URL based on parameters provided by the user to download
the requested product.

=item *

The program sends the URL built to the MIS-Gateway.

=item *

The MIS-Gateway sends a new URL to perform the authentication server via a 
"Central Authentication Service" (CAS).

=item *

The program posts a new URL to get a "Ticket Granting Ticket" (TGT) to download
the requested product.

=item *

The CAS server sends the ticket.

=item *

The program sends the query joined with ticket obtained above and download
the requested product.

=back

=head1 XML CONFIGURATION FILE

The program parameters are contained in an XML file. This file is located in the
directory:

=over 2

=item *

"$HOME/motu-client/motu-client-perl.xml" on Unix platforms.

=item *

"%USERPROFILE%\motu-client/motu-client-perl.xml" on Windows platforms.

=back


The expected structure of the XML file is:

    <?xml version="1.0" encoding="UTF-8"?>
            <configuration>
            <user>john</user>
            <password>secret</password>
			<verbose>0</verbose>
			<mis_gateway>http://web-qt.cls.fr/mis-gateway-servlet/Motu?</mis_gateway>
			<service_url>http://purl.org/myocean/ontology/service/database</service_url>
			<service_name>CLS-TOULOUSE-FR-MERCATOR-MOTU-REST</service_name>
			<dataset_name>dataset-psy2v3-pgs-med-myocean-bestestimate</dataset_name>
			<out_dir>C:/MIS-152/out_dir/</out_dir>
			<out_name>perlTest.nc</out_name>
			<date_min>2010-11-08</date_min>
			<date_max>2010-11-10</date_max>
			<latitude_min>-75</latitude_min>
			<latitude_max>30</latitude_max>
			<longitude_min>20</longitude_min>
			<longitude_max>120</longitude_max>
			<depth_min>0</depth_min>
			<depth_max>15</depth_max>
			<variable>sea_water_x_velocity</variable>
        </configuration>

=head1 INSTALLATION

Copy the script in the directory of your choice. Create a configuration file
(see L</XML CONFIGURATION FILE>) to inform the user and password to use to
connect to the CAS server.

Installing Perl modules which are not provided in the standard installation of
Perl. The list of modules to be installed is described in section
L</REQUIRED MODULES>.

=head1 USAGE

    Usage: motu-client.pl 
		
        Options:
		  --help            
		  				show this help message and exit
		  --verbose
		  				print information in stdout
		  -u USER, --user=USER
								The user name
		  -p PWD, --password=PWD
								The user password
		  -g GATEWAY, --mis_gateway=GATEWAY
		                        The gateway to use
		  -S SERVICE_URL, --service_url=SERVICE_URL
		                        The service url
		  -s SERVICE_NAME, --service_name=SERVICE_NAME
		                        The service name
		  -d DATASET_NAME, --dataset_name=DATASET_NAME
		                        The dataset to download
		  		  
		  -o OUT_DIR, --out_dir=OUT_DIR
		                        The output dir
		  -f OUT_NAME, --out_name=OUT_NAME
		                        The output file name
		                        
		  -t DATE_MIN, --date_min=DATE_MIN
		                        The min date (YYYY-MM-DD)
		  -T DATE_MAX, --date_max=DATE_MAX
		                        The max date (YYYY-MM-DD)
		                        
		  -y LATITUDE_MIN, --latitude_min=LATITUDE_MIN
		                        The min latitude [-90 ; 90]
		  -Y LATITUDE_MAX, --latitude_min=LATITUDE_MAX
		                        The max latitude [-90 ; 90]                      
		  -x LONGITUDE_MIN, --longitude_min=LONGITUDE_MIN
		                        The min longitude [-180 ; 180]
		  -X LONGITUDE_MAX, --longitude_max=LONGITUDE_MAX
		                        The max longitude [-180 ; 180]
		                        
		  -z DEPTH_MIN, --depth_min=DEPTH_MIN
		                        The min depth [0 ; 2e31]
		  -Z DEPTH_MAX, --depth_min=DEPTH_MAX
		                        The max depth [0 ; 2e31]
		                        
		  -v VARIABLE, --variable=VARIABLE
		  						The physical variables to be extracted

=head1 REQUIRED MODULES

This program requires several other modules:

L<Date::Manip>

L<Readonly>

L<XML::Simple>

=head1 BUGS AND QUESTIONS

Please refer to the documentation for information on submitting bug reports or
questions to the author.

=head1 LICENSE

This script is free software; you can redistribute it and/or
modify it under the same terms as Perl itself.

=head1 AUTHOR

AVISO (aviso@cls.fr)

=cut

use constant false => 0;
use constant true  => 1;

my $_GEOGRAPHIC = false;
my $_VERTICAL = false;
my $_TEMPORAL = false;
my $config;
my %options;


#
# Returns the URL for the service name
#
sub _get_service
{

    return sprintf '%s%%23%s', _get_param('service_url'), _get_param('service_name');
}


#
# Returns the URL for the service name
#
sub _get_product
{

    return sprintf '%s%%23%s', _get_param('dataset_url'), _get_param('dataset_name');
}


#
# Returns the request parameters.
#
sub _get_parameter
{

    my $parameters = {
        action  => 'productdownload',
        service => _get_service(),
        product => _get_product(),
        mode    => 'console'
    };

	if($_TEMPORAL){
		
		# Setup default value for the last date
		#my $max_date = $options{date_max} ? ( $options{date_max} ) : $config->{date_max};
		#$max_date = $max_date ? $max_date : 'today';
		
		
		#my $min_date = $options{date_min} ? ( $options{date_min} ) : $config->{date_min};
		#$min_date = $min_date ? $min_date : ($max_date, '20 days ago' );
		
		
	    $parameters->{t_lo} = _get_param('date_min');
	    $parameters->{t_hi} = _get_param('date_max');
	}
	
	if($_GEOGRAPHIC){
	    $parameters->{y_lo} = _get_param('latitude_min');
	    $parameters->{y_hi} = _get_param('latitude_max');
	    $parameters->{x_lo} = _get_param('longitude_min');
	    $parameters->{x_hi} = _get_param('longitude_max');
	}
    
    if($_VERTICAL){
	    $parameters->{z_lo} = _get_param('depth_min');
	    $parameters->{z_hi} = _get_param('depth_max');
    }
    
    

    my @argv;

    foreach ( keys %{$parameters} )
    {
        push @argv, sprintf '%s=%s', $_, $parameters->{$_};
    }
    
    my $key;
    my $_variable = _get_param('variable');
    
    foreach $key (@$_variable)
    {
		push @argv, sprintf '%s=%s', 'variable', $key;
    }
    
    
    return join q{&}, @argv;
}

#
# Returns the URL to download product
#
sub _get_url
{

    return sprintf '%s%s', _get_param('mis_gateway'), _get_parameter();
}

#
# Authentification on CAS server. Returns the HTML code containing the granting
# ticket
#
sub _get_cas_url
{
    my ( $ua, $content, $username, $password ) = @_;

    my ($url) = $content =~ m{(http(s|)://.*/cas)}xms;

    if ( !defined $url )
    {
        croak 'Unable to find the URL of the CAS server';
    }

    $url = sprintf( '%s/v1/tickets', $url );

    my $response = $ua->post(
        $url,
        {
            username => $username,
            password => $password
        }
    );

    if ( !$response->is_success() )
    {
        croak $response->code() == 400
          ? 'Login failed: authentication failure'
          : $response->status_line();
    }
    return $response;
}

#
# Returns the " Ticket Granting Ticket" contained in the HTML response
#
sub _get_ticket
{
    my ( $ua, $url, $response ) = @_;

    my $stream = HTML::TokeParser->new( $response->content_ref() );
    my $result;

    while ( my $token = $stream->get_token )
    {
        if (   $token->[0] eq 'S'
            && $token->[1] eq 'form'
            && exists $token->[2]{action} )
        {
            $result = $token->[2]{action};
            last;
        }
    }

    if ( !defined $result )
    {
        croak 'Unable to find the form to get the Ticket Granting Ticket (TGT)';
    }

    $response = $ua->post( $result, { service => $url } );

    if ( !$response->is_success() )
    {
        croak $response->status_line();
    }

    return $response->content();
}

#
# Displays the version of the program
#
sub _version
{
	print '${project.artifactId} v${project.version}';
	print "\n";
    exit 1;
}

#
# Displays the syntax of the program
#
sub _usage
{
    my $progname = basename($PROGRAM_NAME);

    print <<"EOF";
Usage: $progname 1.2 dataset dir
    Options:
		  --help            
		  				show this help message and exit
		  --version
		  				show program's version number and exit
		  --verbose
		  				print information in stdout
		  -u=USER, --user=USER
		  				the user name
		  -p=PWD, --password=PWD
		  			    the user password
		  -g=GATEWAY, --mis_gateway=GATEWAY
		                        the gateway to use
		  -S=SERVICE_URL, --service_url=SERVICE_URL
		                        The service url
		  -s=SERVICE_NAME, --service_name=SERVICE_NAME
		                        The service name
		  -D=DATASET_URL, --dataset_url=DATASET_URL
		                        The dataset url
		  -d=DATASET_NAME, --dataset_name=DATASET_NAME
		                        The dataset to download
		  		  
		  -o=OUT_DIR, --out_dir=OUT_DIR
		                        The output dir
		  -f=OUT_NAME, --out_name=OUT_NAME
		                        The output file name
		                        
		  -t=DATE_MIN, --date_min=DATE_MIN
		                        The min date (YYYY-MM-DD)
		  -T=DATE_MAX, --date_max=DATE_MAX
		                        The max date (YYYY-MM-DD)
		                        
		  -y=LATITUDE_MIN, --latitude_min=LATITUDE_MIN
		                        The min latitude [-90 ; 90]
		  -Y=LATITUDE_MAX, --latitude_min=LATITUDE_MAX
		                        The max latitude [-90 ; 90]                      
		  -x=LONGITUDE_MIN, --longitude_min=LONGITUDE_MIN
		                        The min longitude [-180 ; 180]
		  -X=LONGITUDE_MAX, --longitude_max=LONGITUDE_MAX
		                        The max longitude [-180 ; 180]
		                        
		  -z=DEPTH_MIN, --depth_min=DEPTH_MIN
		                        The min depth [0 ; 2e31]
		  -Z=DEPTH_MAX, --depth_min=DEPTH_MAX
		                        The max depth [0 ; 2e31]
		                        
		  -v=VARIABLE, --variable=VARIABLE
		  						The physical variables to be extracted

EOF
    exit 1;
}

#
# Throw an exception if a parameter is missing in the XML configuration
#
sub _defined
{
    my ( $tag, $value ) = @_;

    if ( !defined $value )
    {
        croak "<$tag> missing in xml configuration file";
    }
    return;
}

#
# Reading XML configuration file
#
sub _configuration
{
    my (%p) = @_;

    my $path =
      ( $OSNAME eq 'MSWin32' ? $ENV{USERPROFILE} : $ENV{HOME} )
        . '/motu-client/motu-client-perl.xml';	
	$path = File::Spec->canonpath( $path );	
	
	my $config;
	
	if (-e $path ) {
		$config = XMLin($path);	
	} else
	{
	  $config = XMLin('<?xml version="1.0" encoding="UTF-8"?><configuration></configuration>');
	}

    return $config;
}

#
# Return param value
#
sub _check_param
{
	my $val;
	
	$val = _get_param('user');
	if ( !defined $val )
    {
        croak "<user> is missing";
    }
    
    $val = _get_param('password');
	if ( !defined $val )
    {
        croak "<password> is missing";
    }
    
    $val = _get_param('mis_gateway');
	if ( !defined $val )
    {
        croak "<mis_gateway> is missing";
    }
    
    $val = _get_param('service_url');
	if ( !defined $val )
    {
        croak "<service_url> is missing";
    }
    
    $val = _get_param('service_name');
	if ( !defined $val )
    {
        croak "<service_name> is missing";
    }
    
    $val = _get_param('dataset_url');
	if ( !defined $val )
    {
        croak "<dataset_url> is missing";
    }
    
    $val = _get_param('dataset_name');
	if ( !defined $val )
    {
        croak "<dataset_name> is missing";
    }
    
    $val = _get_param('out_name');
	if ( !defined $val )
    {
        croak "<out_name> is missing";
    }
    
    $val = _get_param('out_dir');
	if ( !defined $val )
    {
        croak "<out_dir> is missing";
    }
    
    
	#
	# Check VERTICAL Options
	#
	if( defined _get_param('depth_min') and defined _get_param('depth_max'))
	{
		$_VERTICAL = true;
		if(_get_param('depth_min') < 0 )
		{
			croak "<depth_min> is out of range";
		}
		if(_get_param('depth_max') < 0 )
		{
			croak "<depth_max> is out of range";
		}
	}elsif(defined _get_param('depth_min') or defined _get_param('depth_max')){
		croak "Missing one vertical parameter";
	}
	
	#
	# Check TEMPORAL Options
	#
	if( defined _get_param('date_min') and defined _get_param('date_max'))
	{
		$_TEMPORAL = true;

	}elsif(defined _get_param('date_min') or defined _get_param('date_max')){
		croak "Missing one temporal parameter";
	}
	
	#
	# Check GEOGRAPHIC Options
	#
	if( defined _get_param('latitude_min') and defined _get_param('latitude_max') and defined _get_param('longitude_min') and defined _get_param('longitude_max'))
	{
		$_GEOGRAPHIC = true;
		if(_get_param('latitude_min') < -90 or _get_param('latitude_min') > 90 )
		{
			croak "<latitude_min> is out of range";
		}
		if(_get_param('latitude_max') < -90 or _get_param('latitude_max') > 90 )
		{
			croak "<latitude_max> is out of range";
		}
		if(_get_param('longitude_min') < -180 or _get_param('longitude_min') > 180 )
		{
			croak "<longitude_min> is out of range";
		}
		if(_get_param('longitude_max') < -180 or _get_param('longitude_max') > 180 )
		{
			croak "<longitude_max> is out of range";
		}

	}elsif(defined _get_param('latitude_min') or defined _get_param('latitude_max') or defined _get_param('longitude_min') or defined _get_param('longitude_max')){
		croak "Missing one or more Geogrphic parameter";
	}
	
}


#
# Return param value
#
sub _get_param
{
	my ($p) = @_;
	my $value;
	
	$value = $options{$p} ? ( $options{$p} ) : $config->{$p};
	
	return $value;
}

#
# Returns the name of the file to created.
#
sub _result_file
{
	my $myfile = _get_param('out_dir')._get_param('out_name');
	
    return $myfile;
}

#
# Parse the date contained in a string
#
sub _parse_date
{
    my ( $datestr, $deltastr ) = @_;

    my $date =
      defined $deltastr
      ? DateCalc( $datestr, $deltastr )
      : ParseDate($datestr);

    if ( $date eq q{} )
    {
        croak "Unable to parse date `$datestr'.";
    }
    return UnixDate( $date, '%Y-%m-%d' );
}



#
# If the request sent to the server contains an error, the resulting file is not
# an ZIP archive. In this case, this function displays a diagnostic message.
#
sub _server_error
{
    my $filename = shift;

    my $fh = IO::File->new( $filename, 'r' );
    if ( !defined $fh )
    {
        croak "Unable to open `$filename' for reading.";
    }

    my $line = <$fh>;
    chomp $line;
    $fh->close();

    my ($dataset) = $line =~ m/ERROR/;

    if ( defined $dataset )
    {
    	unlink($filename) or die ("Erreur suppression \n");
    	
        croak $line;
    }
    
}

#
# Main program
#



# Check syntax
_usage()
  unless GetOptions( \%options, 'help', 'version', 'verbose', 'user|u=s', 'password|p=s', 'mis_gateway|g=s', 'service_url|S=s', 'service_name|s=s',
  'dataset_url|D=s', 'dataset_name|d=s', 'out_dir|o=s', 'out_name|f=s', 'date_min|t=s', 'date_max|T=s', 'latitude_min|y=s', 'latitude_max|Y=s',
  'longitude_min|x=s', 'longitude_max|X=s', 'depth_min|z=s', 'depth_max|Z=s', 'variable|v=s@');

_usage()
  if $options{help};

_version()
  if $options{version};
  
# Setup default value for the last date
#$options{date_max} = $options{date_max} ? $options{date_max} : 'today';

# Setup default value for the first date
#my @date_min =$options{date_min} ? ( $options{date_min} ) : ( $options{date_max}, '20 days ago' );

# Read configuration file
$config = _configuration();

# Check parameters
_check_param();

# Go to the working directory
#croak "Unable to change working directory to `$ARGV[1]`: $OS_ERROR"
#  unless chdir $ARGV[1];

# Setup temporary file
my $tmp = _result_file();

# Create the URL for downloading the required products

my $url = _get_url();

my $ua  = LWP::UserAgent->new();

# Setup LWP::UserAgent
$ua->env_proxy();
my $verb = $options{verbose} ? ( $options{verbose} ) : $config->{verbose};
if ($verb){
	$ua->show_progress('TRUE');
}


my $cookie_jar = HTTP::Cookies->new();
$ua->cookie_jar($cookie_jar);

# Sends request to the web server
my $response = $ua->get($url);

# If the answer is OK
if ( $response->is_success() )
{

    # Performs authentication on the CAS server to get a TGT.
    my $ticket = _get_ticket(
        $ua, $url,
        _get_cas_url(
            $ua, $response->base(), _get_param('user'), _get_param('password')
        )
    );


    # Download the product
    $response = $ua->get( "$url&ticket=$ticket", ':content_file' => $tmp );

    # Check the HTTP response
    if ( !$response->is_success() )
    {
        croak $response->code() == 404
          ? 'No files found for the requested period.'
          : $response->status_line();
    }

	_server_error($tmp)
    
}
else
{
    croak $response->status_line();
}
