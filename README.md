# Motu Project
@author Project manager <rdedianous@cls.fr>  
@author Product owner <tjolibois@cls.fr>  
@author Scrum master, software architect <smarty@cls.fr>  
@author Quality assurance, continuous integration manager <bpirrotta@cls.fr> 

>How to read this file? 
Use a markdown reader: 
plugins [chrome](https://chrome.google.com/webstore/detail/markdown-preview/jmchmkecamhbiokiopfpnfgbidieafmd?utm_source=chrome-app-launcher-info-dialog) exists (Once installed in Chrome, open URL chrome://extensions/, and check "Markdown Preview"/Authorise access to file URL.), 
or for [firefox](https://addons.mozilla.org/fr/firefox/addon/markdown-viewer/)  (anchor tags do not work)
and also plugin for [notepadd++](https://github.com/Edditoria/markdown_npp_zenburn).

>Be careful: Markdown format has issue while rendering underscore "\_" character which can lead to bad variable name or path.


#Summary
* [Overview](#Overview)
* [Architecture](#Architecture)
  * [Interfaces](#ArchitectureInterfaces)
     * [Server interfaces](#ArchitectureInterfacesServer)  
     * [External interfaces with other systems or tools](#ArchitectureInterfacesExternal)  
  * [Design](#ArchitectureDesign)
* [Development](#Development)
  * [Development environment](#DEV)
  * [Compilation](#COMPILATION)
  * [Packaging](#Packaging)
* [Installation](#Installation)
  * [Prerequisites](#InstallPrerequisites)
  * [Upgrade from Motu v2.x](#UpgradeFromMotu2x)
  * [Install Motu from scratch](#InstallFromScratch)
  * [Check installation](#InstallCheck)
  * [CDO manual installation](#InstallCDO)
  * [Installation folder structure](#InstallFolders)
  * [Setup a front Apache HTTPd server](#InstallFrontal)
* [Configuration](#Configuration)
  * [Configuration directory structure](#ConfigurationFolderStructure)
  * [Business settings](#ConfigurationBusiness)
  * [System settings](#ConfigurationSystem)
  * [Log settings](#LogSettings)
  * [Theme and Style](#ThemeStyle)
* [Exploitation](#Exploitation)
  * [Start, Stop and other Motu commands](#SS)
  * [Logbooks](#Logbooks)
  * [Add a dataset](#AdminDataSetAdd)
  * [Debug view](#ExploitDebug)
  * [Clean files](#ExploitCleanDisk)
  * [Log Errors](#LogCodeErrors)
     * [Action codes](#LogCodeErrorsActionCode)  
     * [Error types](#LogCodeErrorsErrorType)  
* [Motu clients & API](#ClientsAPI)
  
#<a name="Overview">Overview</a>
Motu is a robust web server allowing the distribution of met/ocean gridded data files through the web. 
Subsetter allows user to extract the data of a dataset, with geospatial, temporal and variable criterias. 
Thus, user download only the data of interest.  
A graphic web interface and machine to machine interfaces allow to access data and information on data (metadata).
The machine-to-machine interface can be used through a client written in python, freely available here https://sourceforge.net/projects/cls-motu/files/client/motu-client-python/.
Output data files format can be netCDF3 or netCDF4.  
An important characteristic of Motu is its robustness: in order to be able to answer many users without crashing, Motu manages its incoming requests in a queue server.  
The aim is to obtain complete control over the requests processing by balancing the processing load according to criteria (volume of data to extract, number of requests to fulfill 
for a user at a given time, number of requests to process simultaneously).  
Moreover, Motu implements a request size threshold. Motu masters the amount of data to extract per request by computing, without any data processing, the result data size of the request.  
Beyond the allowed threshold, every request is rejected. The threshold is set in the configuration file.
Motu can be secured behing an authentication server and thus implements authorization. A CAS server can implement the authentication. 
Motu receives with authentication process user information, including a user profile associated with the account. 
Motu is configured to authorize or not the user to access the dataset or group of datasets which user is trying to access.  
For administrators, Motu allows to monitor the usage of the server: the logs produced by Motu allow to know who (login) requests what (dataset) and when, with extraction criterias.




#<a name="Architecture">Architecture</a>
Motu is a web application running inside the HTTPd Apache Tomcat application server.

#<a name="ArchitectureInterfaces">Interfaces</a>
##<a name="ArchitectureInterfacesServer">Server interfaces</a>
All ports are defined in [motu.properties](#ConfigurationSystem) configuration file.

* __HTTP__: Used to manage HTTP incoming requests
* __HTTPs__: Used to manage HTTPs incoming requests
* __AJP__: Used to communicate with an Apache HTTPd frontal server
* __JMX__: Used to monitor the application
* __Debug__: In development mode, used to remotely debug the application
* __Socket for Shutdown__: Port opened by Tomcat to shutdown the server
  
##<a name="ArchitectureInterfacesExternal">External interfaces with other systems or tools</a>
Motu has interfaces with other systems:  

* __DGF__: Direct Get File: Read dataset from the file system. (See how to [configure it](#AdminDataSetAdd).)
* __Unidata Thredds Data Server__: It connects with the NCSS or OpenDap HTTP REST API to run download request for example. (See how to [configure it](#AdminDataSetAdd).)
* __HTTP CAS Server__: Use for Single Sign On (SSO) in order to manager user authentication. (See how to [configure it](#ConfigurationSystem) "CAS SSO server" and check [profiles](#BSconfigService) attribute set on the dataset.)
* __CDO command line tool__: [CDO](#InstallCDO) is used to deal with 2 types of download requests, which are not covered by NCSS service of Thredds Data Server:  
  * a download request on a __range of depths__,  
  * a download request that come __across the boundary__ of the datasets (for global datasets)  

#<a name="ArchitectureDesign">Design</a>
The Motu application has been designed by implementing the Three-Layered Services Application design. It takes many advantages
in maintenance cost efficiency et in the ease of its future evolutivity.  
Three layers are set in the core "motu-web" project:  

* __USL__: User Service Layer: This layer manages all incoming actions throught HTTP request
* __BLL__: Business Logic Layer: This layer manages all Motu business
* __DAL__: Data Access Layer: This layer manages all access to Motu external interfaces: DGF, Unidata server, CDO, ...

Each layer is an entry point of the application designed with a singleton. These three singletons gives access to high level managers which provides services by implementing a Java interface.
High level managers handle for example the configuration, the request, the catalog, the users.

A common package is also defined to provide utilities: log4j custom format, XML, String ...

#<a name="Development">Development</a>

##<a name="DEV">Development environment</a>

### Configure Eclipse development environment
* Add variable in order to run/debug Motu on your localhost:  
From Eclipse menu bar: Run/Debug > String substitution  
MOTU_HOME=J:\dev\cmems-cis-motu\motu-install-dir  
This variable represent the folder where Motu is installed.  

* From a file explorer, create folders:  
$MOTU_HOME/log  
$MOTU_HOME/config  
$MOTU_HOME/data/public/download  

* Copy configuration files from Eclipse to configuration folder:  
Note: If you do not have any motu-config folder available, default configuration files are folders are available in the "/motu-web/src/main/resources" folder  
If "motu-config" exists, copy:  
cp $eclipse/motu-config/src/config/common/config $MOTU_HOME/config  
cp $eclipse/motu-config/src/config/cls/dev-win7 $MOTU_HOME/config  

 
* Add an application server in Eclipse: Window>Preferences>Server>Runtime environment  
Name=Apache Tomcat 7.0  
Tomcat installation directory=C:\dvlt\java\servers\tomcat\apache-tomcat-7.0.65  

* J2EE perspective > Under the Servers view > Right click > New > Server  
Server Name: Tomcat v7.0 Server at localhost  

* Edit /Servers/Tomcat v7.0 Server at localhost/server.xml and add  
```
<Context docBase="J:/dev/cmems-cis-motu/motu-install-dir/data-deliveries" path="/mis-gateway/deliveries" />
```  
just under the line:  
```
<Host appBase="webapps" autoDeploy="true" name="localhost" unpackWARs="true">
```  
Now Tomcat can serve downloaded files directly   
 
### Run/Debug Motu

Click Debug configurations...> Under Apache Tomcat, debug "Motu Tomcat v7.0 Server at localhost"

Open a web browser and test:  
http://localhost:8080/motu-web/Motu?action=ping  

it displays "OK - response action=ping"  


For more details about Eclipse launchers, refers to /motu-parent/README-eclipseLaunchers.md.


##<a name="COMPILATION">Compilation</a>
This step is used to generate JAR (Java ARchives) and WAR (Web application ARchive).  
```
cd /motu-parent  
mvn clean install -Dmaven.test.skip=true  
```  

All projects are built under target folder.  
The Motu war is built under "/motu-web/target/motu-web-X.Y.Z-classifier.war".  
It embeds all necessary jar libraries.  

##<a name="Packaging">Packaging</a>
This step includes the compilation step. Once all projects are compiled, it groups all archive in a same folder in order to easy the client delivery.  
You have to set ANT script inputs parameter before running it. See /motu-distribution/build.xml header to get more details about inputs.  
```
cd /motu-distribution  
ant  
cd target-ant/delivery/YYYYMMDDhhmmssSSS  
```

Three folders are built containing archives :

* src: contains sources of motu application and the configuration files
* motu: contains the built application and the products (java, tomcat, cdo)
* config: contains two kind of archives:
  * motu-config-X.Y.Z-classifier-$timestamp-$target.tar.gz:  the built configurations for each target platform  
  * motu-web-static-files-X.Y.Z-classifier-$timestamp-$target.tar.gz: The public static files (css, js) for each target platform 


#<a name="Installation">Installation</a>



## <a name="InstallPrerequisites">Prerequisites</a>

In this chapter some paths are set. For example "/opt/cmems-cis" is often written to talk about the installation path.
You are free to install Motu in any other folder, so in the case, replace "/opt/cmems-cis" by your installation folder.

### Motu host
OS target: Linux 64bits (Tested on centos-7.2.1511)

Minimal configuration for an __operational usage__:  

* __CPU__: 4 CPU, 2,4GHz
* __RAM__: 16 Gb RAM
* __Storage__: 
  * Motu installation folder 15Gb
  * Motu download folder 200Gb: by default [motu/data/public/download](#InstallFolders)  
Note that the available space of the download folder has to be tuned, depending on:
  * The number of user which runs requests at the same time on the server
  * The size of the data distributed
   
   
For __test usage__ we recommend:  

* __CPU__: 2 CPU, 2,4GHz
* __RAM__: 10 Gb RAM
* __Storage__: 
  * Motu installation folder 15Gb
  * Motu download folder 50Gb: by default [motu/data/public/download](#InstallFolders)  





  
### External interfaces
Motu is able to communicate with different external servers:  

* __Unidata | THREDDS Data Server (TDS)__: Motu has been only tested with TDS v4.6.6 2016-06-13. The links to this server are set in the [Business settings](#ConfigurationBusiness) and are used to run OpenDap or subsetter interfaces. If Motu runs only with DGF, this server is not required.
* __Single Sign-On - CAS__: The link to this server is set in the [System settings](#ConfigurationSystem). If Motu does not use SSO, this server is not required.

The installation of these two servers is not detailed in this document. Refer to their official web site to know how to install them.


## <a name="UpgradeFromMotu2x">Upgrade from Motu v2.x</a>  

Check this section only if you have installed Motu v2.x and you want to install Motu 3.x.
In this section we consider that your Motu installation folder of version 2.x is "/opt/atoll/misgw/".  

### Upgrade the software
First stop your Motu v2.x: /opt/atoll/misgw/stop-motu.  
Then install the version 3.x of [Motu from scratch](#InstallFromScratch). Before starting the new Motu version, upgrade its configuration by ready section below.  
Once the version 3.x of Motu runs well, you can fully remove the folder of version 2.x is "/opt/atoll/misgw/".  
To avoid any issue, perhaps backup the folder of Motu v2.x before removing it definitively.  
``` 
motu2xInstallFolder=/opt/atoll/misgw  
rm -rf $motu2xInstallFolder/deliveries  
rm -rf $motu2xInstallFolder/motu-configuration-common-2.1.16  
rm -rf $motu2xInstallFolder/motu-configuration-sample-misgw-1.0.5  
rm -rf $motu2xInstallFolder/motu-web  
rm -rf $motu2xInstallFolder/start-motu  
rm -rf $motu2xInstallFolder/stop-motu  
rm -rf $motu2xInstallFolder/tomcat7-motu  
rm -rf $motu2xInstallFolder/tomcat-motu-cas  
```  



### Upgrade the configuration

#### Business configuration, Product & dataset: motuConfiguration.xml  
The new version of Motu is compatible with the motuConfiguration.xml file configured in Motu v3.x.  
So you can use exactly the same file. 
Copy your old motuConfiguration.xml file to the folder /opt/cmems-cis/motu/config, for example:  
```
cp  /opt/atoll/misgw/motu-configuration-sample-misgw/resources/motuConfiguration.xml /opt/cmems-cis/motu/config
```  

It is important to update this file in order to:  

* use the new [ncss protocol](#BSmotuConfigNCSS) to improve performance of product download 
* remove @deprecated attributes to ease future migrations. You can check them [Business configuration](#ConfigurationBusiness).
* Check the attribute [extractionPath](#motuConfig-extractionPath) to continue to serve downloaded dataset from a fontral Apache HTTPd server.


#### Log files
In CMEMS-CIS context the log file motuQSlog.xml is stored in a specific place in order to be shared.  
You have so to check that with the new version this log file is well written in the share folder.
Here is where this log files were written in Motu v2.x:  
``` 
grep -i "motuQSlog.xml" /opt/atoll/misgw/motu-configuration-sample-misgw-1.0.5/resources/log4j.xml
<param name="file" value="/opt/atoll/misgw/tomcat-motu-cas/logs/motuQSlog.xml" />
```  
The folder set in the value attribute shall be the same as the one defined in new the Motu configuration file. Replace $path below by the folder path:  
``` 
grep -i "motuQSlog.xml" /opt/cmems-cis/motu/config/log4j.xml
fileName="$path/motuQSlog.xml"
filePattern="$path/motuQSlog.xml.%d{MM-yyyy}"
``` 






  
## <a name="InstallFromScratch">Install Motu from scratch</a>

Motu installation needs two main step: the software installation and optionally the theme installation.  
The software installation brings by default the CLS theme. The theme installation is there to customize or change this default theme.  

### Install Motu software, for example on a <a name="IntallDU">Dissemination Unit</a>  

Copy the installation archives and extract them.  
```
cd /opt/cmems-cis  
cp motu-products-A.B.tar.gz .  
cp motu-distribution-X.Y.Z.tar.gz .  
cp motu-config-X.Y.Z-$BUILDID-$TARGET-$PLATFORM.tar.gz .  
tar xzf motu-products-A.B.tar.gz  
tar xzf motu-distribution-X.Y.Z.tar.gz  
tar xzf motu-config-X.Y.Z-$BUILDID-$TARGET-$PLATFORM.tar.gz  
cd motu
```

At this step, Motu is able to start. But static files used for customizing the web theme can be installed.  
In the CMEMS context, the installation on a dissemination unit is ended, static files are installed on a [central server](#IntallPublicFilesOnCentralServer).  

Now you can configure the server:  
* Set the [system properties](#ConfigurationSystem): http port, ...
* Configure [dataset](#ConfigurationBusiness)
* Configure the [logs](#LogSettings)
  
Refer to [configuration](#Configuration) in order to check your configuration settings.  

Motu is installed and configured. You can [start Motu server](#SS).  
Then you can [check installation](#InstallCheck).


### Install Motu theme (public static files)

As a dissemination unit administrator, in CMEMS context, this section is not applicable.  

Public static files are used to customized Motu theme. When several Motu are installed, a central server eases the installation and the update by 
referencing static files only once on a unique machine. This is the case in the CMEMS contact, where each dissemination unit host a Motu server, and 
a central server hosts static files.  
If you runs only one install of Motu, you can install static files directly on Motu Apache tomcat server.

#### <a name="IntallPublicFilesOnCentralServer">On a central server</a>  
Extract this archive on a server.
```
tar xvzf motu-web-static-files-X.Y.Z-classifier-$timestamp-$target.tar.gz  
```
Then use a server to make these extracted folders and files accessible from an HTTP address.
 
Example: The archive contains a motu folder at its root. Then a particular file is "motu/css/motu/motu.css" and this file is served by the URL   http://resources.myocean.eu/motu/css/motu/motu.css in the CMEMS CIS context.   



#### <a name="IntallPublicFilesOnMotuTomcat">Directly on Motu Apache tomcat server</a>  
 
If you do not use a central entity to serve public static files, you can optionally extract the archive 
and serve files directly by configuring Motu.  
First extract the archive:   
```
tar xzf motu-web-static-files-X.Y.Z-classifier-$timestamp-$target.tar.gz -C /opt/cmems-cis/motu/data/public/static-files   
```

Then edit "motu/tomcat-motu/conf/server.xml" in order to serve files from Motu.  
Add then "Context" node as shown below. Note that severals "Context" nodes can be declared under the Host node.  
```
[...]  
<Host appBase="webapps" [...]  
        <!-- Used to serve public static files -->  
        <Context docBase="/opt/cmems-cis/motu/data/public/static-files/motu" path="/motu"/>    
 [...]  
```  
  
Finally in motuConfiguration.xml, remove all occurrences of the attribute named: [httpBaseRef](#motuConfig-httpBaseRef) in motuConfig and configService nodes. (Do not set it empty, remove it).


If you want to set another path instead of "/motu", you have to set also the business configuration parameter named [httpBaseRef](#motuConfig-httpBaseRef).  

 
## <a name="InstallCheck">Check installation</a>

### Start motu
```
./motu start 
```

### Check messages on the server console

When you start Motu, the only message shall be:  
```
tomcat-motu - start
```

Optionaly, when this is your first installation or when a software update is done, an INFO message is displayed:  
```
INFO: War updated: tomcat-motu/webapps/motu-web.war [$version]  
```  
  
  
If any other messages appear, you have to treat them.

As Motu relies on binary software like CDO, error could be raised meaning that CDO does not runs well.  
```
ERROR: cdo tool does not run well: $cdo --version  
[...]
```  

In this case, you have to install CDO manually.  

### Check Motu web site available

Open a Web browser, and enter:
http://$motuUrl/motu-web/Motu?action=ping  
Where $motuUrl is: ip adress of the server:tomcat port
Refer to [configuration](#Configuration) regarding the tomcat port

Response has to be:   
```  
OK - response action=ping
```  

Open a Web browser, and enter:
http://$motuUrl/motu-web/Motu  
If nothing appears, it is because you have to [add dataset](#AdminDataSetAdd).  


### Check Motu logs
Check that no error appears in Motu [errors](#LogbooksErrors) log files.

## <a name="InstallCDO">CDO manual installation</a>
This section has to be read only if Motu does not start successfully.  
Select one option below to install "cdo". If you have no idea about cdo installation, choose the Default option.

* [Default option: Install cdo](#InstallCDOHelp)
* [cdo is already installed on this machine](#InstallCDOAlreadyInstalled)
* [Try MOTU without cdo installation](#InstallCDONoInstall)
* [How cdo is built?](#InstallCDOUnderstand)  
  
### <a name="InstallCDOHelp">Install cdo</a>
"cdo" (Climate Data operators) are commands which has to be available in the PATH when Motu starts.   
By default, Motu provides a built of CDO and add the "cdo" command to the PATH, but with some Linux distribution it is necessary to install it.  
Motu provides some help in order to install CDO.  

First check your GLibC version:  
```
ldd --version  
ldd (GNU libc) 2.12  
[...]  
```  

If your GlibC lower than 2.14, you have to install GLIBC 2.14, but to highly recommend to upgrade your Linux operating system to get an up to date GLIBC version:  
__INSTALL GLIBC 2.14__  
```
export MotuInstallDir=/opt/cmems-cis  
cd $MotuInstallDir/motu/products/cdo-group  
wget http://ftp.gnu.org/gnu/glibc/glibc-2.14.tar.gz  
tar zxvf glibc-2.14.tar.gz  
cd glibc-2.14  
mkdir build  
cd build  
mkdir $MotuInstallDir/motu/products/cdo-group/glibc-2.14-home  
../configure --prefix=$MotuInstallDir/motu/products/cdo-group/glibc-2.14-home  
make -j4  
make install  
cd $MotuInstallDir/motu  
```  


__Now check if "cdo" runs well__:  
```
export MotuInstallDir=/opt/cmems-cis  
$MotuInstallDir/motu/products/cdo-group/cdo.sh --version  
Climate Data Operators version 1.7.1 (http://mpimet.mpg.de/cdo)  
[...]  
```  

If error appear like ones below, it certainly means that GLIC is not in the LD_LIBRARY_PATH.
```
$MotuInstallDir/motu/products/cdo-group/cdo-1.7.1-home/bin/cdo: error while loading shared libraries: libhdf5.so.10: cannot open shared object file: No such file or directory
```  
or  
```
cdo: /lib64/libc.so.6: version `GLIBC_2.14' not found (required by cdo)
cdo: /lib64/libc.so.6: version `GLIBC_2.14' not found (required by /opt/cmems-cis-validation/motu/products/cdo-group/hdf5-1.8.17-home/lib/libhdf5.so.10)
```

In this case, edit $MotuInstallDir/products/cdo-group/cdo.sh and add "$GLIBC-home/lib" to LD\_LIBRARY\_PATH.   

Now check again if "cdo" runs well.

If it runs well, you can now start Motu.  


### <a name="InstallCDOAlreadyInstalled">cdo is already installed on this machine</a>
If "cdo" is installed in another folder on the machine, you can add its path in "$MotuInstallDir/motu/motu" script:  

```  
__setPathWithCdoTools() {  
  PATH=$MOTU_PRODUCTS_CDO_HOME_DIR/bin:$PATH  
}  
```  

Optionnaly set LD_LIBRAY_PATH in $MotuInstallDir/products/cdo-group/setLDLIBRARYPATH.sh  



### <a name="InstallCDONoInstall">Try MOTU without cdo installation</a>
Note that without CDO, some functionalities on depth requests or on download product won't work successfully.
If any case, you can disable the CDO check by commented the check call:  

* Disable check:  
```
cd /opt/cmems-cis/motu/  
sed -i 's/  __checkCDOToolAvailable/#  __checkCDOToolAvailable/g' motu
```  
* Enable check:  
```
cd /opt/cmems-cis/motu/  
sed -i 's/#  __checkCDOToolAvailable/  __checkCDOToolAvailable/g' motu
```  
  
  
  
  
### <a name="InstallCDOUnderstand">How cdo is built?</a>
CDO is automaticcly build from the script $MotuInstallDir/motu/products/cdo-group/install-cdo.sh
Also in order to get full details about CDO installation, you can get details in /opt/cmems-cis/motu/products/README and
search for 'Download CDO tools'.  



## <a name="InstallFolders">Installation folder structure</a>
  
Once archives have been extracted, a "motu" folder is created and contains several sub-folders and files:  
__motu/__  

* __config:__ Folder which contains the motu configuration files. Refers to [Configuration](#Configuration) for more details.
* __data:__ Folder used to managed Motu data.
  * __public__: Folders which contain files exposed to public. It can be published through a frontal Apache HTTPd Web server, through Motu Apache Tomcat or any other access.
     * __download__: Folder used to save the products downloaded. This folder is sometimes elsewhere, for example in Motu v2: /datalocal/atoll/mis-gateway/deliveries/. A best practice is to create a symbolic link to a dedicated partition to avoid to freeze Motu when there is no space left.   
     * __inventories__: [AD]  
     * __transaction__: [AD]   
     * __static-files__: Used to store public static files. This folder can be served by a frontal Apache HTTPd Web server or Motu Apache Tomcat. In the CMEMS-CIS context, it is not used as static files are deployed on a central web server.      
* __log:__ Folder which contains all log files. Daily logging are suffixed by yyyy-MM-dd.
  * __errors.log:__ Motu application errors
  * __warnings.log:__ Motu application warnings
  * __logbook.log:__ Motu application logs (errors and warning are included)
  * __tomcat-motu.log:__ Apache tomcat console output and errors
  * __tomcat-motu-catalina.out:__ CATALINA_OUT environment variable used by catalina engine to write its logs.
  * __queue.log:__ Motu queue server logs messages (transaction accounting logs)
* __motu file:__ Script used to start, stop Motu application.  Refers to [Start & Stop Motu](#SS) for more details.
* __pid:__ Folder which contains pid files of the running Motu application.
  * __tomcat-motu.pid:__ Contains the UNIX PID of the Motu process.
* __products:__ Folder which contains Java, Tomcat ($CATALINE_BASE folder) and CDO products.
  * __apache-tomcat-X.Y.Z:__ Apache tomcat default installation folder from http://tomcat.apache.org/
  * __cdo-group:__ CDO tools from https://code.zmaw.de/projects/cdo
  * __jdkX.Y.Z:__ Oracle JDK from http://www.oracle.com/technetwork/java
  * __version-products.txt:__ Contains the version of the current Motu products.
* __tomcat-motu:__ Tomcat is deployed inside this folder. This folder is built by setting the $CATALINE_HOME environment variable.
* __version-distribution.txt file:__ Contains the version of the current Motu distribution.





## <a name="InstallFrontal">Setup a front Apache HTTPd server</a>  
See sample of the Apache HTTPd configuration in the folder: config/apache-httpd-conf-sample  
The configuration is described for Apache2  

* __apache2.conf:__ Use to show how timeout parameter shall be set
* __httpd.conf:__ Main apache configuration file used for Motu
* __enableApacheModules.sh__ Describe the Apache modules to enable


#<a name="Configuration">Configuration</a>

This chapter describes the Motu configuration settings.  
All the configuration files are set in the $installDir/motu/config folder.  

* [Configuration directory structure](#ConfigurationFolderStructure)
* [Business settings](#ConfigurationBusiness)
* [System settings](#ConfigurationSystem)
* [Log settings](#LogSettings)

  
##<a name="ConfigurationFolderStructure">Configuration directory structure</a>
cd $installDir/motu/config
  
* __config:__ Folder which contains the motu configuration files.
  * __motu.properties:__ JVM memory, network ports of JVM (JMX, Debug) and Tomcat (HTTP, HTTPS, AJP, SHUTDOWN). CAS SSO server settings.
  * __motuConfiguration.xml:__ Motu settings (Service, Catalog via Thredds, Proxy, Queues, ....)
  * __log4j.xml:__ Log4j v2 configuration file
  * __standardNames.xml:__ Contains the standard names [TBD]
  * __version-configuration.txt:__ Contains the version of the current Motu configuration.
  
##<a name="ConfigurationBusiness">Business settings</a>
### motuConfiguration.xml: Motu business settings  

You can configure 3 main categories:  

* [MotuConfig node : general settings](#BSmotuConfig)
* [ConfigService node : catalog settings](#BSconfigService)
* [QueueServerConfig node : request queue settings](#BSqueueServerConfig)
  
  
If you have not this file, you can extract it (set the good version motu-web-X.Y.Z.jar):  
```
/opt/cmems-cis/motu/products/jdk1.7.0_79/bin/jar xf /opt/cmems-cis/motu/tomcat-motu/webapps/motu-web/WEB-INF/lib/motu-web-X.Y.Z.jar motuConfiguration.xml
```   
    
  
If you have this file from a version anterior to Motu v3.x, you can reuse it. In order to improve global performance, you have to upgrade some fields:  
* [ncss](#BSmotuConfigNCSS) Set it to "enabled" to use a faster protocol named subsetter rather than OpenDap to communicate with TDS server.  
* [httpBaseRef](#motuConfig-httpBaseRef) shall be set to the ULR of the central repository to display the new theme  
* [ExtractionFilePatterns](#BSmotuConfigExtractionFilePatterns) to give a custom name to the downloaded dataset file  
  
  

####<a name="BSmotuConfig">Attributes defined in motuConfig node</a>

##### defaultService  
A string representing the default action in the URL /Motu?action=$defaultService  
The default one is "listservices".  
All values can be found in the method USLRequestManager#onNewRequest with the different ACTION_NAME.  

##### dataBlockSize
Number of data in Ko that can be read in the same time. Default is 2048Kb.

##### maxSizePerFile
@Deprecated from v3 This parameter is not used. 
Number of data in Megabytes that can be written and download for a Netcdf file. Default is 1024Mb. 

##### maxSizePerFileTDS
Number of data in Megabytes that can be written and download for a Netcdf file. Default is 1024Mb. 

##### <a name="motuConfig-extractionPath">extractionPath</a>
The absolute path where files downloaded from TDS are stored.  
For example: /opt/cmems-cis/motu/data/public/download
It is recommended to set this folder on an hard drive with very good performances in write mode.
It is recommended to have a dedicated partition disk to avoid freezing Motu if the hard drive is full.
By default value is $MOTU_HOME/data/public/download, this folder can be a symbolic link to another folder.  

##### downloadHttpUrl
Http URL used to download files stored in the "extractionPath" described above. It is used to allow users to download the result data files.  
This URL is concatenated to the result data file name found in the folder "extractionPath".  
When a frontal HTTPd server is used, it is this URL that shall be configured to access to the folder "extractionPath".  
String with format ${var} will be substituted with Java property variables. @See System.getProperty(var)  

##### <a name="motuConfig-httpBaseRef">httpBaseRef</a>
Http URL used to serve files from to the path where archive __motu-web-static-files-X.Y.Z-classifier-buildId.tar.gz__ has been extracted.  
For example: 

* Value http://resources.myocean.eu/motu serve a folder which contains ./css/motu/motu.css.  
It so enable to server http://resources.myocean.eu/motu/css/motu/motu.css  
* Value . is used to server statics files included by default in Motu application
* Remove this value to serve a path accessible from $motuServer/motu

        
##### cleanExtractionFileInterval
In minutes, oldest result files from extraction request are deleted. This check is done each "runCleanInterval" minutes.    
Default = 60min

##### <a name="BScleanRequestInterval">cleanRequestInterval</a>
In minutes, oldest status than this time are removed from Motu. This check is done each "runCleanInterval" minutes.  
Default = 60min

##### runCleanInterval
In minutes, the waiting time between each clean process.   
A clean process does:  

* delete files inside java.io.tmpdir
* delete all files found in extractionFolder bigger than extractionFileCacheSize is Mb
* delete all files found in extractionFolder oldest than cleanExtractionFileInterval minutes
* remove all status oldest than [cleanRequestInterval](#BScleanRequestInterval) minutes

Default = 1min

##### <a name="BSmotuConfigExtractionFilePatterns">extractionFilePatterns</a>
Patterns (as regular expression) that match extraction file name to delete in folders:

* java.io.tmpdir
* extractionPath

Default is ".*\\.nc$|.*\\.zip$|.*\\.tar$|.*\\.gz$|wps_output_.*$|wps_response_.*$"  


##### extractionFileCacheSize
Size in Mbytes.  
A clean job runs each <runCleanInterval>. File with a size higher than this value are deleted by this job.
If value is zero: no delete.  
Default value = 0.

##### describeProductCacheRefreshInMilliSec
Provide the delay between two refresh of the Describe product cache.
This delay is provided in millisecond.
The default value is 60000 meaning 1 minute.


##### runGCInterval
@Deprecated from v3 This parameter is not used. 


##### httpDocumentRoot
@Deprecated from v3 This parameter is not used. 
Document root of the servlet server.       
        
##### useAuthentication
@Deprecated from v3 This parameter is not used. It is redundant with parameter config/motu.properties#cas-activated.


##### defaultActionIsListServices
@Deprecated from v3 This parameter is not used.  

##### Configure the Proxy settings  
@Deprecated from v3 This parameter is not used.
Proxy settings are not used on Motu:  

* __useProxy__
* __proxyHost__
* __proxyPort__
* __proxyLogin__
* __proxyPwd__


#### <a name="BSconfigService">Attributes defined in configService node</a>

##### <a name="BSconfigServiceName">name</a>
String to set the config service name

##### group
String which describes the group

##### description
String which describes the service

##### profiles
Optional string containing one value, several values separated by a comma or empty (meaning everybody can access).  
Used to manage access right from a SSO cas server.  
In the frame of CMEMS, three profiles exist:  

* internal: internal users of the CMEMS project  
* major: major accounts  
* external: external users  

Otherwise, it's possible to configure as many profiles as needed.  
Profiles are configured in LDAP within the attribute "memberUid" of each user. This attribute is read by CAS and is sent to Motu 
once a user is logged in, in order to check if it matches profiles configured in Motu to allow a user accessing the data.  
In LDAP, "memberUid" attribute can be empty, contains one value or several values separated by a comma.  


##### veloTemplatePrefix
Optional, string used to target the default velocity template. It is used to set a specific theme.  
Value is the velocity template file name without the extension.  
Default value is "index".

##### httpBaseRef
Optional, used to override [motuConfig httpBaseRef](#motuConfig-httpBaseRef) attribute for this specific service.

##### defaultLanguage
@Deprecated from v3 This parameter is not used.


#### Attributes defined in catalog node

##### <a name="BSconfigServiceDatasetName">name</a>
This catalog name refers a TDS catalog name available from the URL: http://$ip:$port/thredds/m_HR_MOD.xml
Example: m_HR_OBS.xml 

##### <a name="BSconfigServiceDatasetType">type</a>  
* tds: Dataset is downloaded from TDS server. In this case, you can use [Opendap or NCSS protocol](#BSmotuConfigNCSS).
* file: Dataset is downloaded from DGF

Example: tds

##### <a name="BSmotuConfigNCSS">ncss</a>
Optional parameter used to enable or disable the use of NetCDF Subset Service (NCSS) in order to request the TDS server.
Without this attribute or when empty, Motu connects to TDS with Opendap protocol. If this attribute is set to "enabled" Motu connects to TDS with NCSS protocol in order to improve performance.   
We recommend to use "enabled".   
Values are: "enabled", "disable" or empty.

##### urlSite
* TDS URL  
For example: http://$ip:$port/thredds/  

* DGF URL  
For example: file:///opt/publication/inventories

####<a name="BSqueueServerConfig">Attributes defined in queueServerConfig node</a>

##### maxPoolAnonymous
Maximum number of request that an anonymous user can send to Motu before throwing an error message.  
Value of -1 means no check is done so an unlimited number of user can request the server.  
Default value is 10  
In case where an SSO server is used for authentication, this parameter is not used. In this you you will be able to fix a limit by setting "maxPoolAuth" parameter value.  

##### maxPoolAuth
Maximum number of request that an authenticated user can send to Motu before throwing an error message.  
Value of -1 means no check is done so an unlimited number of user can request the server.  
Default value is 1
In case where no SSO server is used for authentication, this parameter is not used. In this you you will be able to fix a limit by setting "maxPoolAnonymous" parameter value.  

##### defaultPriority
@Deprecated from v3 This parameter is not used.


#### Attributes defined in queues
##### id
An id to identify the queue.

##### description
Description of the queue.

##### batch
@Deprecated from v3 This parameter is not used.

##### Child node: maxThreads
Use to build a java.util.concurrent.ThreadPoolExecutor an to set "corePoolSize" and "maximumPoolSize" values.  
Default value is 1  
The total number of threads should not be up to the total number of core of the processor on which Motu is running.  

##### Child node: maxPoolSize
Request are put in a queue before being executed by the ThreadPoolExecutor. Before being put in the queue, the queue size
is checked. If it is upper than this value maxPoolSize, an error message is returned.
Value of -1 means no check is done.  
Default value is -1


##### Child node: dataThreshold
Size in Mbytes. A request has a size. The queue in which this request will be processed is defined by the request size.
All queues are sorted by size ascending. A request is put in the last queue which has a size lower than the request size.
If the request size if higher than the bigger queue dataThreshold, request is not treated and an error message is returned.  
This parameter is really useful when a Motu is used to server several kind of file size and you want to be sure that file with a specific size does no slow down request of small data size.  
In this case you can configure two queues and set a number of threads for each in order to match the number of processors. The JVM, even if requests for high volume are running, will be able to
process smallest requests by running the thread on the other processor core. Sp processing high volume requests will not block the smallest requests.  


##### Child node: lowPriorityWaiting
@Deprecated from v3 This parameter is not used.

##<a name="ConfigurationSystem">System settings</a>

### motu.properties: Motu system settings  

#### Java options
The three parameters below are used to tune the Java Virtual Machine:  
   # -server: tells the Hostspot compiler to run the JVM in "server" mode (for performance)  
__tomcat-motu-jvm-javaOpts__=-server -Xmx4096M -Xms512M -XX:PermSize=128M -XX:MaxPermSize=512M  
__tomcat-motu-jvm-port-jmx__=9010  
__tomcat-motu-jvm-address-debug__=9090  


#### Tomcat network ports
The parameters below are used to set the different network ports used by Apache Tomcat.  
At startup, these ports are set in the file "$installdir/motu/tomcat-motu/conf/server.xml".    
But if this file already exist, it won't be replaced. So in order to apply these parameters, remove the file "$installdir/motu/tomcat-motu/conf/server.xml".  
  
__tomcat-motu-port-http__=9080  
  # HTTPs is in a common way managed from a frontal Apache HTTPd server. If you really need to use it from Tomcat, you have to tune the SSL certificates and the protocols directly in the file "$installdir/motu/tomcat-motu/conf/server.xml".  
__tomcat-motu-port-https__=9443  
__tomcat-motu-port-ajp__=9009  
__tomcat-motu-port-shutdown__=9005  



#### CAS SSO server

   # true or false to enable the SSO connection to a CAS server  
__cas-activated__=false  
  
   # Cas server configuration to allow Motu to access it  
   # @see https://wiki.jasig.org/display/casc/configuring+the+jasig+cas+client+for+java+in+the+web.xml  
     
   # The Cas server URL  
__cas-server-url__=https://cas-cis.cls.fr/cas  
   # The Motu HTTP server url: example: http://misgw-ddo-qt.cls.fr:9080 or http://motu.cls.fr   
__cas-auth-serverName__=http://$motuServerIp:$motuServerPort  
   # The proxy callback HTTP URL on the Motu server (this URL can be defined on the frontal Apache HTTPs server)  
__cas-validationFilter-proxyCallbackUrl__=http://$motuServerIp:$motuServerPort/motu-web/proxyCallback  

#### Supervision
To enable the status supervision, set the parameter below:  
__tomcat-motu-urlrewrite-statusEnabledOnHosts__=localhost,*.cls.fr

This parameter is used to set the property below in the WEB.XML file:  
```
        <init-param>  
            <param-name>statusEnabledOnHosts</param-name>  
            <param-value>${tomcat-motu-urlrewrite-statusEnabledOnHosts}</param-value>  
        </init-param>  
```  

For more detail read:  
org.tuckey UrlRewriteFilter FILTERS : see http://urlrewritefilter.googlecode.com/svn/trunk/src/doc/manual/3.1/index.html  

  
## <a name="LogSettings">Log settings</a>

Log are configured by using log4j2 in file config/log4j2.xml  

### Motu queue server logs: motuQSlog.xml, motuQSlog.csv

This log files are used to compute statistics about Motu server usage.  
Two format are managed by this log, either XML or CSV.  
To configure it, edit config/log4j.xml  

##### Log format: XML or CSV  
Update the fileFormat attribute of the node "MotuCustomLayout": <MotuCustomLayout fileFormat="xml">
A string either "xml" or "csv" to select the format in which log message are written.  
If this attribute is not set, the default format is "xml".  
``` 
		<RollingFile name="log-file-infos.queue"   
		    fileName="${sys:motu-log-dir}/motuQSlog.xml"   
			filePattern="${sys:motu-log-dir}/motuQSlog.xml.%d{MM-yyyy}"    
			append="true">   
			<!-- fileFormat=xml or csv -->  
			<MotuCustomLayout fileFormat="xml" />  
			<Policies>  
				<TimeBasedTriggeringPolicy interval="1" modulate="true"/>  
			</Policies>  
		</RollingFile>  
``` 

##### Log path
In the dissemination unit, Motu share its log files with a central server.  
Log files have to be save on a public access folder.  
Set the absolute path in the "fileName" and "filePattern" attributes. This path shall be serve by the frontal Apache HTTPd or Apache Tomcat.





##<a name="ThemeStyle">Theme and Style</a>
In Motu you can update the theme of the website. There is 2 mains things in order to understand how it works?  

* [Template] velocity: The velocity templates are used to generated HTML pages from Java objects.  
* [Style] CSS, Images and JS: These files are used to control style and behaviour of the web UI.

By default, the template and style are integrated in the "war". But the Motu design enable to customize it easily.

* [Template] velocity: You can change all templates defined in:
motu/tomcat-motu/webapps/motu-web/WEB-INF/lib/motu-web-2.6.00-SNAPSHOT.jar/velocityTemplates/*.vm
by defining them in motu/config/velocityTemplates.

The main HTML web page structure is defined by the index.vm velocity template. For example, in you create a file motu/config/velocityTemplates/index.vm containing an empty html page, website will render empty web pages.  
"index.vm" is the default theme. The name can be updated for each motuConfig#configService by setting veloTemplatePrefix="".
By default veloTemplatePrefix="index".


* [Style] CSS, Images and JS: Those files are integrated with the default theme motu-web-2.6.00-SNAPSHOT.war/css/*, motu-web-2.6.00-SNAPSHOT.war/js/*. These files can be downloaded from an external server which enable to benefit to several mMotu server at he same time. The external server name can be updated for each motuConfig#configService by setting httpBaseRef="".  
By default httpBaseRef search static files from the Motu web server, for example:  
``` 
service.getHttpBaseRef()/css/motu/screen/images/favicon.ico"
``` 





#<a name="Exploitation">Exploitation</a>  

##<a name="SS">Start, Stop and other Motu commands</a>  
All operations are done from the Motu installation folder.  
For example:  
``` 
cd /opt/cmems-cis/motu 
```

### Start Motu
Start the Motu process.  
``` 
./motu start  
```

### Stop Motu  
At the shutdown of Motu, the server waits that none of the pending or in progress request are in execution.  
If it's the case, the server waits the end of the request before shutdown.  
Note that after waiting 10 minutes server will automatically shutdown without waiting any running requests.  
``` 
./motu stop
``` 


### Advanced commands
#### Restart Motu
``` 
./motu restart
``` 

#### Status of the Motu process
``` 
./motu status
``` 

Status are the following: 
 
* __tomcat-motu started__ A pid file exists
* __tomcat-motu stopped__ No pid file exists

#### Help about Motu parameters
``` 
./motu ?
``` 

##<a name="Logbooks">Logbooks</a>  

Log messages are generated by Apache Log4j 2. The configuration file is "config/log4j.xml".  
By default, log files are created in the folder $MOTU_HOME/log. This folder contains:  

* __Motu log messages__
  * __logbook.log__: All Motu log messages including WARN and ERROR(without stacktrace) messages.
  * __warnings.log__: Only Motu log messages with a WARN level
  * <a name="LogbooksErrors">__errors.log__</a>: Only Motu log messages with an ERROR level. When this file is not empty, it means that at least an error has been generated by the Motu application.
  * __motuQSlog.xml__, __motuQSlog.csv__: Either a "CSV" or "XML" format which logs all queue events.
     * CSV: On one unique line, writes:  
    [OK | ERR;ErrCode;ErrMsg;ErrDate];  
    queueId;queueDesc;
    requestId;  
    elapsedWaitQueueTime;elapsedRunTime;elapsedTotalTime;totalIOTime;preparingTime;readingTime;  
    inQueueTime;startTime;endTime;  
    amountDataSize;  
    downloadUrlPath;extractLocationData;  
    serviceName;TemporalCoverageInDays;ProductId;UserId;UserHost;isAnonymousUser;  
    variable1;variable2;...;variableN;  
    temporalMin,temporalMax;  
    LatitudeMin;LongitudeMin;LatitudeMax;LongitudeMax:
    DepthMin;DepthMax;
     * XML: XStream is used to serialized a Java Object to XML from fr.cls.atoll.motu.web.bll.request.queueserver.queue.log.QueueLogInfo  
     Same data are represented.
     * Field details
         * queueId, queueDesc: Queue used to process the request. Id and description found in config/motuConfiguration.xml
         * requestId: A timestamp representing the request id.
         * inQueueTime: Timestamp with format "yyyy-mm-dd' 'hh:mm:ss.SSS" when the request has been put in the queue
         * startTime: Timestamp with format "yyyy-mm-dd' 'hh:mm:ss.SSS" when the request has been started to be processed
         * endTime: Timestamp with format "yyyy-mm-dd' 'hh:mm:ss.SSS" when the request has been ended to be processed
         * elapsedWaitQueueTime: Duration in milliseconds, [startTime - inQueueTime]
         * elapsedRunTime: Duration in milliseconds, [endTime - startTime]
         * elapsedTotalTime: Duration in milliseconds, [endTime - inQueueTime]
         * totalIOTime: Duration in nanoseconds: reading + writing + copying + compressing times.
         * readingTime: Duration in nanoseconds.
         * writingTime: Duration in nanoseconds.
         * preparingTime: Duration in nanoseconds, same value as reading time.
         * copyingTime: Duration in nanoseconds, only set in DGF mode.
         * compressingTime: Duration in nanoseconds, only set in DGF mode.
         * amountDataSize: Size in MegaBytes
         * downloadUrlPath: URL to download the product
         * extractLocationData: Absolute path on the server
         * serviceName: The service name found in the configuration file motuConfiguration.xml
         * TemporalCoverageInDays: duration in days
         * ProductId: Product id
         * UserId: User login if user is not anonymous, otherwise its host or IP address from which he is connected
         * UserHost: Host or ip address from which user is connected
         * isAnonymousUser: true or false                
         * variable1;variable2;...;variableN; Extracted variable names
         * temporalMin,temporalMax: Temporal coverage
         * LatitudeMin;LongitudeMin;LatitudeMax;LongitudeMax: Geographical coverage (latitude:-90;+90; longitude:180;+180)
         * DepthMin;DepthMax;: Depth coverage   
  * __velocity.log__: Logs generated by the http://velocity.apache.org/ technology to render HTML web pages.

* __Tomcat log messages__
  * __tomcat-motu-catalina.out__: Catalina output matching the environment variable CATALINA_OUT.
  * __tomcat-motu.log__: $CATALINA_HOME/bin/startup.sh and $CATALINA_HOME/bin/shutdown.sh outputs are redirected to this file.  

##<a name="AdminDataSetAdd">Add a dataset</a>  
In order to add a new Dataset, you have to add a new configService node in the [Motu business configuration](#ConfigurationBusiness).  
When Motu read data through TDS (Opendap or NCSS service), the data shall be configured in TDS before this configuration in Motu. The TDS configuration is not explained here.
  
Examples:  

* __TDS NCSS protocol__:  
This is the fastest protocol implemented by Motu. Motu select this protocol because type is set to "tds" and ncss is set to "enabled".  

``` 
<configService description="Free text to describe your dataSet" group="HR-Sample" httpBaseRef="" name="HR_MOD-TDS" veloTemplatePrefix="" profiles="external">  
        <catalog name="m_HR_MOD.xml" type="tds" ncss="enabled" urlSite="http://$tdsUrl/thredds/"/>  
</configService>  
```  
  
* __TDS Opendap protocol__:  
Here OpenDap is used because it is the default protocol when tds type is set and ncss is not set or is disable.  

``` 
<configService description="Free text to describe your dataSet" group="HR-Sample" httpBaseRef="" name="HR_MOD-TDS" veloTemplatePrefix="" profiles="external">  
        <catalog name="m_HR_MOD.xml" type="tds" ncss="" urlSite="http://$tdsUrl/thredds/"/>  
</configService>  
```  

* __DGF protocol__:   
This protocol is used to access to local files on the current machine of done with a NFS mount. With this protocol user download the full file and can run any specific extraction.  
``` 
<configService description="Free text to describe your dataSet" group="HR-Sample" profiles="internal, external, major" httpBaseRef="" name="HR_MOD-TDS" veloTemplatePrefix="">  
           <catalog name="catalogFILE_GLOBAL_ANALYSIS_PHYS_001_016.xml" type="file" urlSite="file:///opt/atoll/hoa-armor/publication/inventories"/>  
</configService>  
```



##<a name="ExploitDebug">Debug view</a>  
From a web browser access to the Motu web site with the URL:  
``` 
/Motu?action=debug  
``` 

You can see the different requests and their status.  
You change the status order by entering 4 parameters in the URL:  
``` 
/Motu?action=debug&order=DONE,ERROR,PENDING,INPROGRESS
``` 
 
##<a name="ExploitCleanDisk">Clean files</a>  

##<a name="ExploitCleanDiskLogbook">Logbook files</a>  
Logbook files are written in the folder configured in the log4j.xml configuration file.  
All logs are generated daily except for motuQSLog (xml or csv) which are generated monthly.
You can clean those files to avoid to fullfill the harddrive. 
crontab -e
0 * * * * find /opt/cmems-cis/motu/log/*.log* -type f -mmin +14400 -delete >/dev/null 2>&1  
0 * * * * find /opt/cmems-cis/motu/log/*.out* -type f -mmin +14400 -delete >/dev/null 2>&1  
0 * * * * find /opt/cmems-cis/motu/log/*.xml* -type f -mmin +144000 -delete >/dev/null 2>&1  
0 * * * * find /opt/cmems-cis/motu/log/*.csv* -type f -mmin +144000 -delete >/dev/null 2>&1  
  

##<a name="LogCodeErrors">Log Errors</a> 

### The code pattern
The error codes of Motu as the following format "XXXX-Y":
  
* [XXXX](#LogCodeErrorsActionCode) code matching the action which is executed when the error is raised. This part is the "ActionCode".  The action is in general a HTTP request and matches the following HTTP parameter http://$server/motu-web/Motu?action=.
* [Y](#LogCodeErrorsErrorType) code which identifies the part of the program from which the error was raised. This part is the "ErrorType".
  
  
For example, the web browser can display:  
011-1 : A system error happened. Please contact the administrator of the site. 

Here, we have the error code in order to understand better what happens. But the end user has a generic message and no detail is given to him. These end user messages are described in the file "/motu-web/src/main/resources/MessagesError.properties". The file provided with the project is a default one and can be customized for specific purposes. Just put this file in the "config" folder, edit it and restart Motu to take it into account. So when a user has an error, it just have to tell you the error code and you can check the two numbers with the descriptions below.  


### <a name="LogCodeErrorsActionCode">Action codes</a>

The Action Code		=>	A number matching the HTTP request with the action parameter.

001		=>	UNDETERMINED\_ACTION           
002		=>	PING\_ACTION                   
003		=>	DEBUG\_ACTION                  
004		=>	GET\_REQUEST\_STATUS\_ACTION     
005		=>	GET\_SIZE\_ACTION               
006		=>	DESCRIBE\_PRODUCT\_ACTION       
007		=>	TIME\_COVERAGE\_ACTION          
008		=>	LOGOUT\_ACTION                 
010		=>	DOWNLOAD\_PRODUCT\_ACTION       
011		=>	LIST\_CATALOG\_ACTION           
012		=>	PRODUCT\_METADATA\_ACTION       
013		=>	PRODUCT\_DOWNLOAD\_HOME\_ACTION  
014		=>	LIST\_SERVICES\_ACTION              
015		=>	DESCRIBE\_COVERAGE\_ACTION         
016		=>	ABOUT\_ACTION  

### <a name="LogCodeErrorsErrorType">Error types</a>

The Error Type Code	=>	A number defining a specific error on the server.

0       =>  No error.  
1		=>	There is a system error. Please contact the Administrator.    
2		=>	There is an error with the parameters. There are inconsistent.         
3		=>	The date provided into the parameters is invalid.         
4		=>	The latitude provided into the parameters is invalid.  
5		=>	The longitude provided into the parameters is invalid.         
6		=>	The range defined by the provided dates is invalid.         
7		=>	The memory capacity of the motu server is exceeded.         
8		=>	The range defined by the provided latitude/longitude parameters is invalid.         
9		=>	The range defined by the provided depth parameters is invalid.         
10		=>	The functionality is not yet implemented.         
11		=>	There is an error with the provided NetCDF variables.         
12		=>	There is not variables into the variable parameter.         
13		=>	NetCDF parameter error. Example: Invalid date range, invalid depth range, ...         
14		=>	There is an error with the provided NetCDF variable. Have a look at the log file to have more information.         
15		=>	The number of maximum request in the queue server pool is reached. it's necessary to wait that some requests are finished.         
16		=>	The number of maximum request for the user is reached. It's necessary to wait that some requests are finished for the user.         
18		=>	The priority of the request is invalid in the queue server manager. Have a look at the log file to have more information.         
19		=>	The id of the request is not know by the server. Have a look at the log file to have more information.         
20		=>	The size of the request is greater than the maximum data managed by the available queue. It's impossible to select a queue for this request. It's necessary to narrow the request.         
21		=>	The application is shutting down. it's necessary to wait a while before the application is again available.         
22		=>	There is a problem with the loading of the motu configuration file. Have a look at the log file to have more information.         
23		=>	There is a problem with the loading of the catalog configuration file. Have a look at the log file to have more information.         
24		=>	There is a problem with the loading of the error message configuration file. Have a look at the log file to have more information.         
25		=>	There is a problem with the loading of the netcdf file. Have a look at the log file to have more information.         
26		=>	There is a problem with the provided parameters. Have a look at the log file to have more information.         
27		=>	There is a problem with the NetCDF generation engine. Have a look at the log file to have more information.         
28		=>	The required action is unknown. Have a look at the log file to have more information.
29		=>	The product is unknown.
30		=>	The service is unknown.
31		=>	The request cut the ante meridian. In this case, it's not possible to request more than one depth. It's necessary to change the depth selection and to select in the "from" and the "to" the values that have the same index into the depth list.
32      =>  Due to a known bug in Thredds Data Server, a request cannot be satisfied wit netCDF4. User has to request a netCDF3 output file.
  
#<a name="ClientsAPI">Motu clients & API</a>  

You can connect to Motu by using a web browser or a client.

## <a name="ClientPython">Python client</a> 
Motu offers an easy to use [Python client](https://github.com/clstoulouse/motu-client-python).
  
  
## <a name="ClientAPI">MOTU REST API</a> 
__MOTU REST API__ lets you use Motu server services.  
All URLs have always the same pattern: http://motuServer/${context}/Motu?action=$actionName  
__$actionName__ is an action in the list below:
Other parameters are used. They are described with their cardinality [x,y].  

* [0,1] is an optional parameter.   
* [1] is a mandatory parameter.  
* [0,n] is an optional parameter which can be set several times.  
* [1,n] is a mandatory parameter which can be set several times.  


  
__Summary of all actions:__   
  
* XML API
   * [Describe coverage](#ClientAPI_DescribeCoverage>)  
   * [Describe product](#ClientAPI_DescribeProduct>)  
   * [Request status](#ClientAPI_RequestStatus>)  
   * [Get size](#ClientAPI_GetSize>)  
   * [Time coverage](#ClientAPI_GetTimeCov>)  
* HTML Web pages
   * [About](#ClientAPI_About>)  
   * [Debug](#ClientAPI_Debug>)  
   * [Download product](#ClientAPI_DownloadProduct>)  
   * [List catalog](#ClientAPI_ListCatalog>)  
   * [List services](#ClientAPI_ListServices>)  
   * [Product download home](#ClientAPI_ProductDownloadHome>)  
   * [Product medatata](#ClientAPI_ProductMetadata>)  
* Plain Text 
   * [Ping](#ClientAPI_Ping>)  


 
### <a name="ClientAPI_About">About</a>  
Display version of the archives installed on Motu server  
__URL__: http://localhost:8080/motu-web/Motu?action=about  
__Parameters__: No parameter.  
__Return__: An HTML page. Motu-static-files (Graphic chart) is refresh thanks to Ajax.   
Example:  
```
Motu-products: 3.0  
Motu-distribution: 2.6.00-SNAPSHOT  
Motu-configuration: 2.6.00-SNAPSHOT-20160623173246403  
Motu-static-files (Graphic chart): 3.0.00-RC1-20160914162955422  
```

### <a name="ClientAPI_Debug">Debug</a>  
Display all requests status managed by Motu server in the last [cleanRequestInterval](#BScleanRequestInterval] minutes.
Tables are sorted by time ascending.
__URL__: http://localhost:8080/motu-web/Motu?action=debug  
__Parameters__:  
* __order__ [0-1]: Change the order of items INPROGRESS,PENDING,ERROR,DONE. All items shall be set.  
example: http://localhost:8080/motu-web/Motu?action=Debug&order=DONE,ERROR,PENDING,INPROGRESS  
Without this parameter, default order is: INPROGRESS,PENDING,ERROR,DONE  
__Return__: An HTML page  
  
  
### <a name="ClientAPI_DescribeCoverage">Describe coverage</a>  
Get coverage data in relationship with a dataset.  
__URL__: http://localhost:8080/motu-web/Motu?action=describecoverage&service=HR_MOD-TDS&datasetID=HR_MOD  
__Parameters__:  
* __service__ [1]: The [service name](#BSconfigServiceName)  
* __datesetID__ [1]: The [dataset ID](#BSconfigServiceDatasetName)  
__Return__: A XML document  
```   
<dataset xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"  xsi:noNamespaceSchemaLocation="describeDataset.xsd" name="HR_MOD" id="HR_MOD">  
<boundingBox>  
  <lon min="-180.0" max="179.91668701171875" units="degrees_east"/>
  <lat min="-80.0" max="-80.0" units="degrees_north"/>
</boundingBox>
<dimension name="time" start="2012-12-26T12:00:00.000+00:00" end="2016-06-12T12:00:00.000+00:00" units="ISO8601"/>  
<dimension name="z" start="" end="" units="m"/>  
<variables>  
<variable id="northward_sea_water_velocity" name="v" description="Northward velocity" standardName="northward_sea_water_velocity" units="m s-1">  
<dimensions></dimensions>  
</variable>  
...  
</variables>  
</dataset>  

```  
  
  
### <a name="ClientAPI_DescribeProduct">Describe product</a>  
Display the product meaning dataset description.  
There is 2 ways to call describe product, both returning a same response.


#### Way 1   
  
__URL__:   
* http://localhost:8080/motu-web/Motu?action=describeproduct&service=serviceId&product=datasetId  
__Parameters__:  
* __service__ [1]: The [service name](#BSconfigServiceName)  
* __product__ [1]: The product id  
  
  
#### Way 2   

__URL__:   
* http://localhost:8080/motu-web/Motu?action=describeproduct&data=http://$tdsServer/thredds/dodsC/path_HR_MOD&xmlfile=http://$tdsServer/thredds/m_HR_MOD.xml 
__Parameters__:  
* __xmlfile__ [1]: The Thredds dataset, example: http://$tdsServer/thredds/m_HR_MOD.xml  
* __data__ [1]: The Thredds data, example http://$tdsServer/thredds/dodsC/path_HR_MOD  
  
  
__Return__: An XML document  7

```   
<productMetadataInfo code="OK" msg="OK" lastUpdate="Not Available" title="HR_MOD" id="HR_MOD">  
<timeCoverage code="OK" msg="OK"/>  
<availableTimes code="OK" msg="OK">  
2012-12-26 12:00:00;2012-12-27 12:00:00;   
...  
</availableTimes>  
<availableDepths code="OK" msg="OK">  
0.49402;1.54138;2.64567;...  
</availableDepths>  
<geospatialCoverage code="OK" msg="OK"/>  
<variablesVocabulary code="OK" msg="OK"/>  
<variables code="OK" msg="OK">  
<variable description="Northward velocity" units="m s-1" longName="Northward velocity" standardName="northward_sea_water_velocity" name="v" code="OK" msg="OK"/>  
<variable description="Eastward velocity" units="m s-1" longName="Eastward velocity" standardName="eastward_sea_water_velocity" name="u" code="OK" msg="OK"/>  
...  
</variables>  
<dataGeospatialCoverage code="OK" msg="OK">  
<axis code="OK" msg="OK" description="Time (hours since 1950-01-01)" units="hours since 1950-01-01 00:00:00" name="time_counter" upper="582468" lower="552132" axisType="Time"/>  
<axis code="OK" msg="OK" description="Longitude" units="degrees_east" name="longitude" upper="179.91668701171875" lower="-180" axisType="Lon"/>  
<axis code="OK" msg="OK" description="Latitude" units="degrees_north" name="latitude" upper="90" lower="-80" axisType="Lat"/>  
<axis code="OK" msg="OK" description="Depth" units="m" name="depth" upper="5727.9169921875" lower="0.4940249919891357421875" axisType="Height"/>  
</dataGeospatialCoverage>  
</productMetadataInfo>  
```  


 
### <a name="ClientAPI_DownloadProduct">Download product</a>  
Request used to download a product  
__URL__: http://localhost:8080/motu-web/Motu?action=productdownload 
__Parameters__: TBD.  
__Return__: Severals ways  
Example:  
```

```  


 
### <a name="ClientAPI_RequestStatus">Request status</a>  
Get a request status to get more details about a download state.  
__URL__: http://localhost:8080/motu-web/Motu?action=getreqstatus&requestid=123456789
__Parameters__:  
* __requestid__ [1]: A request id.
__Return__: An XML document or an HTML page if requestId does not exists.    
Validated by the schema /motu-api-message/src/main/schema/XmlMessageModel.xsd#StatusModeResponse  
Example:  
```
<statusModeResponse code="004-0" msg="" scriptVersion="" userHost="" userId="" dateSubmit="2016-09-19T16:56:22.184Z" localUri="/$pathTo/HR_MOD_1474304182183.nc" remoteUri="http://localhost:8080/motu/deliveries/HR_MOD_1474304182183.nc" size="1152.0"dateProc="2016-09-19T16:56:22.566Z" requestId="1474304182183" status="1"/>
```  
Size is in MegaBits.



 
### <a name="ClientAPI_GetSize">Get size</a>  
Get the size of a download request.  
__URL__: http://localhost:8080/motu-web/Motu?action=getsize
__Parameters__:  
Parameters are exactly the same as for [Download product](#ClientAPI_DownloadProduct)  
__Return__: An XML document.    
Validated by the schema /motu-api-message/src/main/schema/XmlMessageModel.xsd#RequestSize  
Example:  
```
<requestSize code="005-0" msg="OK" unit="kb" size="1.5104933E8" maxAllowedSize="9.961472E8"/>  
```  

### <a name="ClientAPI_ListCatalog">List catalog</a>  
Get the size of a download request.  
__URL__: http://localhost:8080/motu-web/Motu?action=listcatalog&service=HR_MOD-TDS
__Parameters__:  
* __service__ [1]: The [service name](#BSconfigServiceName)  
__Return__: An HTML page   


### <a name="ClientAPI_ListServices">List services</a>  
Display the service web page 
__URL__: http://localhost:8080/motu-web/Motu?action=listcatalog&service=HR_MOD-TDS
__Parameters__:  
* __catalogtype__ [0,1]: The [catalog type](#BSconfigServiceDatasetType) used to filter by type.  
__Return__: An HTML page   


### <a name="ClientAPI_Ping">Ping</a>  
Used to be sure that server is up. 
__URL__: http://localhost:8080/motu-web/Motu?action=ping
__Parameters__: No parameter.
__Return__: An plain text
```  
OK - response action=ping    
```  

### <a name="ClientAPI_ProductDownloadHome">Product download home</a>  
Display an HTML page in order to set the download parameters. 
__URL__: http://localhost:8080/motu-web/Motu?action=productdownloadhome&service=HR_OBS-TDS&product=HR_OBS
__Parameters__:  

* __service__ [1]: The [service name](#BSconfigServiceName)  
* __product__ [1]: The product id  
  
__Return__: An HTML page



### <a name="ClientAPI_ProductMetadata">Product metadata Home</a>  
Display an HTML page with the geographical and temporal coverage, the last dataset update and the variables metadata. 
__URL__: http://localhost:8080/motu-web/Motu?action=listproductmetadata&service=HR_OBS-TDS&product=HR_OBS
__Parameters__:  

* __service__ [1]: The [service name](#BSconfigServiceName)  
* __product__ [1]: The product id  
  
__Return__: An HTML page



### <a name="ClientAPI_GetTimeCov">Time coverage</a>  
Display an HTML page with the geographical and temporal coverage, the last dataset update and the variables metadata. 
__URL__: http://localhost:8080/motu-web/Motu?action=listproductmetadata&service=HR_OBS-TDS&product=HR_OBS
__Parameters__:  

* __service__ [1]: The [service name](#BSconfigServiceName)  
* __product__ [1]: The product id  
  
__Return__: A XML document
```  
<timeCoverage code="007-0" msg="OK" end="2016-09-17T00:00:00.000Z" start="2007-05-13T00:00:00.000Z"/>
```  
