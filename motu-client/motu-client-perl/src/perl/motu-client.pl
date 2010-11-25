#!/usr/bin/env perl
use strict;
use warnings;
use Archive::Zip qw(:ERROR_CODES);
use Carp;
use Date::Manip;
use English qw(-no_match_vars);
use File::Basename;
use File::stat;
use File::Temp qw(tempdir);
use Getopt::Long;
use HTTP::Cookies;
use HTML::TokeParser;
use LWP::UserAgent;
use Readonly;
use XML::Simple;

=pod

=head1 NAME

sltac-download-misgw.pl - Download SL-TAC products via the MIS-Gateway

=head1 SYNOPSIS

This program can be integrated into a processing chain in order to automate the
downloading of SL-TAC products via the MIS-Gateway.

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

"$HOME/.sltac-gateway" on Unix platforms.

=item *

"%USERPROFILE%\.sltac-gateway" on Windows platforms.

=back

This file must be read-only because it contains the password use to authenticate
to the CAS server.

The expected structure of the XML file is:

    <?xml version="1.0" encoding="UTF-8"?>
        <configuration>
        <mis_gateway>http://web-qt.cls.fr/sltac-gateway-servlet</mis_gateway>
        <service>SL-CLS-TOULOUSE-FR-MOTU-REST</service>
        <user>john</user>
        <password>secret</password>
    </configuration>

=head1 INSTALLATION

Copy the script in the directory of your choice. Create a configuration file
(see L</XML CONFIGURATION FILE>) to inform the user and password to use to
connect to the CAS server.

Installing Perl modules which are not provided in the standard installation of
Perl. The list of modules to be installed is described in section
L</REQUIRED MODULES>.

=head1 USAGE

    Usage: sltac-gateway.pl dataset dir
        --help      Print this message
        --date_min  Date of first file to download. A full date may include a
                    calendar date (year, month, day), a time of day (hour, minute,
                    second), and time zone information. All of this can be entered
                    in many different formats. If this option is not set, the
                    program takes into account the date 20 days ago.
        --date_max  Date of last file to download. The date format expected is
                    described in the previous option. If this option is not set, the
                program takes into account today's date.
        --verbose   Verbose mode.
        dataset     Dataset to download. The list of known dataset is:
                        * dataset-duacs-X-Y-Z-sla-l3
                    where X, Y, Z, W can take the following values:
                        * X: nrt, dt
                        * Y: global, medsea, blacksea
                        * Z: tp, tpn, j1, j1n, j2, e1, e2, en, g2
        dir         Directory where files will be deposited

=head1 REQUIRED MODULES

This program requires several other modules:

L<Archive::Zip>

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

Readonly my $MY_OCEAN => 'http://purl.org/myocean/ontology/individual/myocean';

#
# Returns the URL for the service name
#
sub _get_service
{
    my $service_name = shift;

    return sprintf '%s%%23%s', $MY_OCEAN, $service_name;
}

#
# Returns the URL for the product name
#
sub _get_product
{
    my $product_name = shift;

    return sprintf '%s%%23%s', $MY_OCEAN, $product_name;
}

#
# Returns the request parameters.
#
sub _get_parameter
{
    my ($p) = @_;

    my $parameters = {
        action  => 'productdownload',
        service => _get_service( $p->{service} ),
        product => _get_product( $p->{product} ),
        mode    => 'console'
    };

    $parameters->{t_lo} = $p->{date_min};
    $parameters->{t_hi} = $p->{date_max};

    my @argv;

    foreach ( keys %{$parameters} )
    {
        push @argv, sprintf '%s=%s', $_, $parameters->{$_};
    }
    return join q{&}, @argv;
}

#
# Returns the URL to download product
#
sub _get_url
{
    my ($p) = @_;

    return sprintf '%s/Motu?%s', $p->{mis_gateway}, _get_parameter($p);
}

#
# Authentification on CAS server. Returns the HTML code containing the granting
# ticket
#
sub _get_cas_url
{
    my ( $ua, $content, $username, $password ) = @_;

    my ($url) = $content =~ m{(https://.*/cas)}xms;

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
# Displays the syntax of the program
#
sub _usage
{
    my $progname = basename($PROGRAM_NAME);

    print <<"EOF";
Usage: $progname dataset dir
    --help      Print this message
    --date_min  Date of first file to download. A full date may include a
                calendar date (year, month, day), a time of day (hour, minute,
                second), and time zone information. All of this can be entered
                in many different formats. If this option is not set, the
                program takes into account the date 20 days ago.
    --date_max  Date of last file to download. The date format expected is
                described in the previous option. If this option is not set, the
                program takes into account today's date.
    --verbose   Verbose mode.
    dataset     Dataset to download. The list of known dataset is:
                    * dataset-duacs-X-Y-Z-sla-l3
                where X, Y, Z, W can take the following values:
                    * X: nrt, dt
                    * Y: global, medsea, blacksea
                    * Z: tp, tpn, j1, j1n, j2, e1, e2, en, g2
    dir         Directory where files will be deposited
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
      ( $OSNAME eq 'MSWin32' ? $ENV{USERPROFILE} : $ENV{HOME} ) . q{/.}
      . basename($PROGRAM_NAME);
    $path =~ s/\.\w+$//;

    # The configuration file must be read-only.
    my $st = stat $path or croak "cannot acces `$path' : $OS_ERROR";
    my $mode = $st->mode & 07777;

    if ( $OSNAME ne 'MSWin32' )
    {
        croak "`$path' must be in user read-only mode"
          unless $mode == 256 || $mode == 384;
    }
    my $config = XMLin($path);

    $config->{date_min} = $p{date_min};
    $config->{date_max} = $p{date_max};
    $config->{product}  = $p{product};

    # Check configuration file
    _defined( 'mis_gateway', $config->{mis_gateway} );
    _defined( 'service',     $config->{service} );
    _defined( 'user',        $config->{user} );
    _defined( 'password',    $config->{password} );

    return $config;
}

#
# Returns the name of the temporary ZIP created.
#
sub _tmp_zip
{
    my ($dir) = @_;

    my $tmp = File::Temp->new( UNLINK => 0, DIR => $dir, SUFFIX => '.zip' );
    $tmp->close;

    return $tmp->filename;
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
# On Archive::Zip error do nothing
#
sub _zip_error_null_handler
{
    return;
}

#
# On Archive::Zip error croak
#
sub _zip_error_handler
{
    my ($msg) = @_;

    croak $msg;
    return;
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

    my ($dataset) = $line =~ m{product.*#(.*)'\s+not found};

    if ( defined $dataset )
    {
        croak "Dataset `$dataset' not found.";
    }
    else
    {
        croak $line;
    }
}

#
# Main program
#
my %options;

# Check syntax
_usage()
  unless GetOptions( \%options, 'help', 'date_min=s', 'date_max=s', 'verbose' );

_usage()
  if $options{help} || @ARGV != 2;

# Setup default value for the last date
$options{date_max} = $options{date_max} ? $options{date_max} : 'today';

# Setup default value for the first date
my @date_min =
  $options{date_min}
  ? ( $options{date_min} )
  : ( $options{date_max}, '20 days ago' );

# Read configuration file
my $config = _configuration(
    date_min => _parse_date(@date_min),
    date_max => _parse_date( $options{date_max} ),
    product  => $ARGV[0],
);

# Go to the working directory
croak "Unable to change working directory to `$ARGV[1]`: $OS_ERROR"
  unless chdir $ARGV[1];

# Setup temporary file
my $dir = tempdir( CLEANUP => 1 );
my $tmp = _tmp_zip($dir);

# Create the URL for downloading the required products
my $url = _get_url($config);
my $ua  = LWP::UserAgent->new();

# Setup LWP::UserAgent
$ua->env_proxy();
$ua->show_progress('TRUE') if $options{verbose};

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
            $ua, $response->base(), $config->{user}, $config->{password}
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

    # Disables error handling for Archive::Zip.
    Archive::Zip::setErrorHandler( \&_zip_error_null_handler );
    my $zip = Archive::Zip->new($tmp);

    # If an error occurs while extracting the archive the program is stopped
    Archive::Zip::setErrorHandler( \&_zip_error_handler );

    # If the opening of the archive failed, the received file is a text file
    # containing the error message
    _server_error($tmp) if !defined $zip;

    # Unpacks the archive and displays the downloaded files
    foreach my $element ( $zip->members() )
    {
        print $element->fileName() . "\n";
        $zip->extractMember($element);
    }
}
else
{
    croak $response->status_line();
}
