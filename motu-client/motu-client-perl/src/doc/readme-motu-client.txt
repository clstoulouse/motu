NAME
    download-misgw.pl - Download MOTU products via the MIS-Gateway

SYNOPSIS
    This program can be integrated into a processing chain in order to
    automate the downloading of MOTU products via the MIS-Gateway.

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

    * "$HOME/pl_motu_gateway" on Unix platforms.

    * "%USERPROFILE%\pl_motu_gateway" on Windows platforms.

    This file must be read-only because it contains the password use to
    authenticate to the CAS server.

    The expected structure of the XML file is:

        <?xml version="1.0" encoding="UTF-8"?>
            <configuration>
            <user>john</user>
            <password>secret</password>
			<verbose>0</verbose>
			<mis_gateway>http://web-qt.cls.fr/mis-gateway-servlet/Motu?</mis_gateway>
			<service_url>http://purl.org/myocean/ontology/service/database</service_url>
			<service_name>CLS-TOULOUSE-FR-MERCATOR-MOTU-REST</service_name>
			<dataset_url>http://purl.org/myocean/ontology/product/database</dataset_url>
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

INSTALLATION
    Copy the script in the directory of your choice. Create a configuration
    file (see "XML CONFIGURATION FILE") to inform the user and password to
    use to connect to the CAS server.

    Installing Perl modules which are not provided in the standard
    installation of Perl. The list of modules to be installed is described
    in section "REQUIRED MODULES".

USAGE
        Usage: download-misgw.pl 
		
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
		  -D=DATASET_URL, --dataset_url=DATASET_URL
		                        The dataset url
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

