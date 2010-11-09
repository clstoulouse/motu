NAME
    ./sltac_down.py - Download SL-TAC products via the MIS-Gateway

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

CONFIGURATION FILE
    The program parameters are contained in an ini file. This file is
    located in the directory:

    * "$HOME/.py_sltac_gateway" on Unix platforms.

    * "%USERPROFILE%\.py_sltac_gateway" on Windows platforms.

    This file should be read-only because it might contains the password use to
    authenticate to the CAS server.

    The expected structure of file is:
    
		[Main]
		user=john
		pwd=secret
		verbose=False
		gateway=http://web-qt.cls.fr/sltac-gateway-servlet
		service=SL-CLS-TOULOUSE-FR-MOTU-REST
		product=dataset-duacs-nrt-blacksea-en-sla-l3
		out_dir=./out_dir
		date_min=2010-09-01
		date_max=2010-09-28
		

INSTALLATION
    Copy the script in the directory of your choice. Create a configuration
    file (see "CONFIGURATION FILE") to inform the user and password to
    use to connect to the CAS server.

    Installing Python modules which are not provided in the standard
    installation of Python. The list of modules to be installed is described
    in section "REQUIRED MODULES".

USAGE
        Usage: 
        ./sltac_down.py -h
		Usage: ./sltac_down.py [options]
		
		Options:
		  -h, --help            show this help message and exit
		  -z, --uncompress      If true, data will be uncompressed
		  --verbose             print information in stdout
		  -u USER, --user=USER  the user name
		  -p PWD, --pwd=PWD     the user password
		  -g GATEWAY, --gateway=GATEWAY
		                        the gateway to use
		  -s SERVICE, --service=SERVICE
		                        The service name
		  -t DATE_MIN, --date-min=DATE_MIN
		                        The min date (YYYY-MM-DD)
		  -T DATE_MAX, --date-max=DATE_MAX
		                        The max date (YYYY-MM-DD)
		  -d PRODUCT, --product=PRODUCT
		                        The product (data set) to download
		  -o OUT_DIR, --out=OUT_DIR
		                        The output dir


REQUIRED MODULES
    No module required.

BUGS AND QUESTIONS
    Please refer to the documentation for information on submitting bug
    reports or questions to the author.

LICENSE
    This script is free software; you can redistribute it and/or modify it
    under the same terms as Perl itself.

AUTHOR
    AVISO (aviso@cls.fr)

