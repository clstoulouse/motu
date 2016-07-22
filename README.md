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
* [Development environment](#DEV)
* [Compilation](#COMPILATION)
* [Packaging](#Packaging)
* [Installation](#Installation)
  * [Prerequisites](#InstallPrerequisites)
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
  * [Debug view](#ExploitDebug)
  
  
#<a name="Overview">Overview</a>
Motu project is a robust web server used to distribute data. [To be completed]


#<a name="DEV">Development environment</a>

## Configure eclipse development environment
* Add variable in order to run/debug Motu on your localhost:  
From Eclipse menu bar: Run/Debug > String substitution  
MOTU_HOME=J:\dev\cmems-cis-motu\motu-install-dir  
This variable represent the folder where Motu is installed.  

* From a file explorer, create folders:  
$MOTU_HOME/log  
$MOTU_HOME/config  
$MOTU_HOME/data-deliveries  

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
 
## Run/Debug Motu

Click Debug configurations...> Under Apache Tomcat, debug "Motu Tomcat v7.0 Server at localhost"

Open a web browser and test:  
http://localhost:8080/motu-web/Motu?action=ping  

it displays "OK - response action=ping"  


For more details about Eclipse launchers, refers to /motu-parent/README-eclipseLaunchers.md.


#<a name="COMPILATION">Compilation</a>
This step is used to generate Java ARchives (or war).
```
cd /motu-parent  
mvn clean install -Dmaven.test.skip=true  
```  

All projects are built under target folder.  
The Motu war is built under "/motu-web/target/motu-web-X.Y.Z-classifier.war"  
It embeds all necessary jar libraries.  

#<a name="Packaging">Packaging</a>
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
### Motu host
Motu OS target is Linux 64bits.

### External interfaces
Motu is able to communicate with different external servers:  

* __Unidata | THREDDS Data Server (TDS)__: The links to this server are set in the [Business settings](#ConfigurationBusiness) and are used to run OpenDap or subsetter interfaces. If Motu runs only with DGF, this server is not required.
* __Single Sign-On - CAS__: The link to this server is set in the [System settings](#ConfigurationSystem). If Motu does not use SSO, this server is not required.

The installation of these two servers is not detailed in this document. Refer to their official web site to know how to install them.

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

Motu is installed and configured. Refers to [configuration](#Configuration) for check configuration before [starting Motu](#SS).


### Install Motu theme (public static files)

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

Then edit "motu/tomcat-motu/conf/server.xml" in order to server files from Motu:  
```
[...]  
<Host appBase="webapps" [...]  
        <!-- Used to serve public static files -->  
        <Context docBase="/opt/cmems-cis/motu/data/public/static-files/motu" path="/motu"/>  
 [...]  
```
Finally in motuConfiguration.xml remove the attribute named: [httpBaseRef](#motuConfig-httpBaseRef). (Do not set it empty, remove it).


If you want to set another path instead of "/motu", you have to set also the business configuration parameter named [httpBaseRef](#motuConfig-httpBaseRef).  

 
## <a name="InstallCheck">Check installation</a>

When you start Motu, the only message shall be:  
```
tomcat-motu - start
```

If any other messages appear, you have to treat them.

As Motu relies on binary software like CDO, error could be raised meaning that CDO does not runs well.  
```
ERROR: cdo tool does not run well: $cdo --version  
[...]
```  

In this case, you have to install CDO manually.  


## <a name="InstallCDO">CDO manual installation</a>
This section has to be read only if Motu does not start successfully.  
  
"cdo" command has to be available in the PATH when Motu starts.   
By default, Motu provides a built of CDO and add the command to the PATH, but with some Linux distribution it is necessary to install it.  
Motu provides some help in order to install CDO:  

```
cd /opt/cmems-cis/motu/products/cdo-group  
vi install-cdo.sh  
./install-cdo $MOTU_HOME
```  

Also in order to get full details about CDO installation, you can get details in:  

```
cd /opt/cmems-cis/motu/products/  
vi README  
  
Search for 'Download CDO tools'  

mkdir cdo-group  
cd cdo-group  
cp $PRODUCT_INSTALL_DIR/cdo-group/install-cdo.sh ./  
[...]  
```

Note that without CDO some functionalities on depth requests or on download product won't work successfully.
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
  
  
__ERROR GLIBC_2.14 is missing__
In this case you have to install GLIBC 2.14:  
```
cd /opt/cmems-cis/motu/products/cdo-group
wget http://ftp.gnu.org/gnu/glibc/glibc-2.14.tar.gz
tar zxvf glibc-2.14.tar.gz
cd glibc-2.14
mkdir build
cd build
mkdir /opt/cmems-cis/motu/products/cdo-group/glibc-2.14-home
../configure --prefix=/opt/cmems-cis/motu/products/cdo-group/glibc-2.14-home
make -j4
make install
sed -i '15 a export LD_LIBRARY_PATH=/opt/cmems-cis/motu/products/cdo-group/glibc-2.14-home/lib:$LD_LIBRARY_PATH' /opt/cmems-cis/motu/motu
```  

## <a name="InstallFolders">Installation folder structure</a>
  
Once archives have been extracted, a "motu" folder is created and contains several sub-folders and files:

* __config:__ Folder which contains the motu configuration files. Refers to [Configuration](#Configuration) for more details.
* __data:__ Folder used to managed Motu data.
  * __public__: Folders which contain files exposed to public. It can be from a frontal Apache HTTPd Web server, from Motu Apache Tomcat or any other access.
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
  * __queue.log:__ Motu queue server logs messages
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

##### extractionPath
The absolute path where files downloaded from TDS are stored.  
For example: /opt/cmems-cis/motu/data/download
It is recommended to have an hard drive with very good performances in write mode.
It is recommended to have a dedicated partition disk to avoid freezing Motu if the hard drive is full.

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

##### cleanRequestInterval
In minutes, oldest status than this time are removed from Motu. This check is done each "runCleanInterval" minutes.  
Default = 60min

##### runCleanInterval
In minutes, the waiting time between each clean process.   
A clean process does:  

* delete files inside java.io.tmpdir
* delete all files found in extractionFolder bigger than extractionFileCacheSize is Mb
* delete all files found in extractionFolder oldest than cleanExtractionFileInterval minutes
* remove all status oldest than cleanRequestInterval minutes

Default = 1min

##### extractionFilePatterns
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

##### name
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

##### name
This catalog name refers a TDS catalog name available from the URL: http://$ip:$port/thredds/m_HR_MOD.xml
Example: m_HR_OBS.xml 

##### type
Example: tds

##### ncss
Without this attribute or when empty, Motu connects to TDS with Opendap protocol. If this attribute is set to "enabled" connects to TDS with ncss protocol in order to improve performance.   
We recommend to use "enabled".   
Values are: "enabled", "disable" or empty.

##### urlSite
TDS URL  
For example: http://$ip:$port/thredds/


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
tomcat-motu-jvm-javaOpts=-server -Xmx4096M -Xms512M -XX:PermSize=128M -XX:MaxPermSize=512M  
tomcat-motu-jvm-port-jmx=9010  
tomcat-motu-jvm-address-debug=9090  


#### Tomcat network ports
The parameters below are used to set the different network ports used by Apache Tomcat.  
At startup, these ports are set in the file "$installdir/motu/tomcat-motu/conf/server.xml".  
But if this file already exist, it won't be replaced. So in order to apply these parameters, remove the file "$installdir/motu/tomcat-motu/conf/server.xml".
tomcat-motu-port-http=9080  
  # HTTPs is in a common way managed from a frontal Apache HTTPd server. If you really need to use it from Tomcat, you have to tune the SSL certificates and the protocols directly in the file "$installdir/motu/tomcat-motu/conf/server.xml".
tomcat-motu-port-https=9443  
tomcat-motu-port-ajp=9009  
tomcat-motu-port-shutdown=9005  



#### CAS SSO server

   # true or false to enable the SSO connection to a CAS server
cas-activated=false
  
   # Cas server configuration to allow Motu to access to it  
   # @see https://wiki.jasig.org/display/casc/configuring+the+jasig+cas+client+for+java+in+the+web.xml  
     
   # The Cas server URL
cas-server-url=https://cas-cis.cls.fr/cas  
   # The Motu HTTP server url: example: http://misgw-ddo-qt.cls.fr:9080 or http://motu.cls.fr   
cas-auth-serverName=http://$motuServerIp:$motuServerPort 
   # The proxy callback HTTP URL on the Motu server (this URL can be defined on the frontal Apache HTTPs server)
cas-validationFilter-proxyCallbackUrl=http://$motuServerIp:$motuServerPort/motu-web/proxyCallback

#### Supervision
To enable the status supervision, set the parameter below:
tomcat-motu-urlrewrite-statusEnabledOnHosts=localhost,*.cls.fr

This parameter is used to set the property below in the WEB.XML file:
        <init-param>
            <param-name>statusEnabledOnHosts</param-name>
            <param-value>${tomcat-motu-urlrewrite-statusEnabledOnHosts}</param-value>
        </init-param>
        
For more detail read:
org.tuckey UrlRewriteFilter FILTERS : see http://urlrewritefilter.googlecode.com/svn/trunk/src/doc/manual/3.1/index.html  

  
## <a name="LogSettings">Log settings</a>

Log are configured by using log4j2 in file config/log4j2.xml


This log files are used to compute statistics about Motu server usage.  
Two format are managed by this log, either XML or CSV.
To configure it, edit config/log4j.xml

### Motu queue server logs: motuQSlog.xml, motuQSlog.csv

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
By default, log files are created in the folder $motu-install-dir/log. This folder contains:  

* __Motu log messages__
  * __logbook.log__: All Motu log messages including WARN and ERROR(without stacktrace) messages.
  * __warnings.log__: Only Motu log messages with a WARN level
  * __errors.log__: Only Motu log messages with an ERROR level. When this file is not empty, it means that at least an error has been generated by the Motu application.
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



