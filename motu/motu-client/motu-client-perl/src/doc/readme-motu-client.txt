NAME
    sltac-gateway.pl - Download SL-TAC products via the MIS-Gateway

SYNOPSIS
    This program can be integrated into a processing chain in order to
    automate the downloading of SL-TAC products via the MIS-Gateway.

ALGORITHM
    The communication algorithm with the MIS-Gateway is the following:

    * The program builds a URL based on parameters provided by the user to
      download the requested product.

    * The program sends the URL built to the MIS-Gateway.

    * The MIS-Gateway sends a new URL to perform the authentication server
      via a "Central Authentication Service" (CAS).

    * The program posts a new URL to get a "Ticket Granting Ticket" (TGT) to
      download the requested product.

    * The CAS server sends the ticket.

    * The program sends the first joined with ticket obtained above and
      download the requested product.

XML CONFIGURATION FILE
    The program parameters are contained in an XML file. This file is
    located in the directory:

    * "$HOME/.sltac-gateway" on Unix platforms.

    * "%USERPROFILE%\.sltac-gateway" on Windows platforms.

    This file must be read-only because it contains the password use to
    authenticate to the CAS server.

    The expected structure of the XML file is:

        <?xml version="1.0" encoding="UTF-8"?>
            <configuration>
            <mis_gateway>http://web-qt.cls.fr/sltac-gateway-servlet</mis_gateway>
            <service>SL-CLS-TOULOUSE-FR-MOTU-REST</service>
            <user>john</user>
            <password>secret</password>
        </configuration>

INSTALLATION
    Copy the script in the directory of your choice. Create a configuration
    file (see "XML CONFIGURATION FILE") to inform the user and password to
    use to connect to the CAS server.

    Installing Perl modules which are not provided in the standard
    installation of Perl. The list of modules to be installed is described
    in section "REQUIRED MODULES".

USAGE
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
                            * dataset-duacs-X-Y-Z-W-l3
                        where X, Y, Z, W can take the following values:
                            * X: nrt, dt
                            * Y: global, medsea, blacksea
                            * Z: tp, tpn, j1, j1n, j2, e1, e2, en, g2
                            * W: sla, adt
            dir         Directory where files will be deposited

REQUIRED MODULES
    This program requires several other modules:

    Archive::Zip

    Date::Manip

    Readonly

    XML::Simple

BUGS AND QUESTIONS
    Please refer to the documentation for information on submitting bug
    reports or questions to the author.

LICENSE
    This script is free software; you can redistribute it and/or modify it
    under the same terms as Perl itself.

AUTHOR
    AVISO (aviso@cls.fr)

