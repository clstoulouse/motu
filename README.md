# Motu Project
@author <tjolibois@cls.fr>: Project manager & Product owner  
@author <smarty@cls.fr>: Scrum master, Software architect, Quality assurance, Continuous Integration manager   

>How to read this file? 
Use a markdown reader: 
plugins [chrome](https://chrome.google.com/webstore/detail/markdown-preview/jmchmkecamhbiokiopfpnfgbidieafmd?utm_source=chrome-app-launcher-info-dialog) exists (Once installed in Chrome, open URL chrome://extensions/, and check "Markdown Preview"/Authorise access to file URL.), 
or for [firefox](https://addons.mozilla.org/fr/firefox/addon/markdown-viewer/)  (anchor tags do not work)
and also plugin for [notepadd++](https://github.com/Edditoria/markdown_npp_zenburn).

>Be careful: Markdown format has issue while rendering underscore "\_" character which can lead to bad variable name or path.


# Summary
* [Overview](#Overview)
* [Architecture](#Architecture)
  * [Overall](#ArchitectureOverall)
       * [One instance](#ArchitectureOneInstance)  
	   * [Scalability](#ArchitectureScalability)  
  * [Interfaces](#ArchitectureInterfaces)
     * [Server interfaces](#ArchitectureInterfacesServer)  
     * [External interfaces with other systems or tools](#ArchitectureInterfacesExternal)  
  * [Design](#ArchitectureDesign)
  * [Design details](#ArchitectureDesignD)  
     * [Motu-web project](#ArchitectureDesignDMW)
     * [Other projects](#ArchitectureDesignDOthers)	 
  * [Algorithm details](#ArchiAlgo)  
     * [Downloading 1 point](#ArchiAlgoDownloading1Point)
* [Development](#Development)
  * [Source code](#DEVSRC)
  * [Development environment](#DEV)
  * [Compilation](#COMPILATION)
  * [Packaging](#Packaging)
* [Installation](#Installation)
  * [Prerequisites](#InstallPrerequisites)
     * [Hardware settings](#InstallPrerequisitesHard)
     * [Software settings](#InstallPrerequisitesSoft)
	 * [External interfaces](#InstallPrerequisitesExternalInterfaces)
     * [Several Motu instances on a same host](#InstallPrerequisitesSeveralsInstances)
  * [Upgrade from Motu v2.x](#UpgradeFromMotu2x)
  * [Install Motu from scratch](#InstallFromScratch)
  * [Check installation](#InstallCheck)
  * [CDO manual installation](#InstallCDO)
  * [Installation folder structure](#InstallFolders)
  * [Setup a frontal Apache HTTPd server](#InstallFrontal)
  * [Security](#InstallSecurity)
     * [Run Motu as an HTTPS Web server](#InstallSecurityRunHTTPs)
	 * [Motu and Single Sign On](#InstallSecuritySSO)
  * [Install a scalable Motu over several instances](#InstallationScalability)
* [Configuration](#Configuration)
  * [Configuration directory structure](#ConfigurationFolderStructure)
  * [Business settings](#ConfigurationBusiness)
  * [System settings](#ConfigurationSystem)
  * [Log settings](#LogSettings)
  * [Theme and Style](#ThemeStyle)
* [Operation](#Operation)
  * [Start, Stop and other Motu commands](#SS)
  * [Monitor performance](#ExpMonitorPerf)
  * [Logbooks](#Logbooks)
  * [Add a dataset](#AdminDataSetAdd)
  * [Tune the dataset metadata cache](#AdminMetadataCache)
  * [Debug view](#ExploitDebug)
  * [Clean files](#ExploitCleanDisk)
  * [Log Errors](#LogCodeErrors)
     * [Action codes](#LogCodeErrorsActionCode)  
     * [Error types](#LogCodeErrorsErrorType)  
* [Motu clients & REST API](#ClientsAPI)
  * [Python client](#ClientPython)
  * [OGC WCS API](#OGC_WCS_API)
  * [REST API](#ClientRESTAPI)  
  
  
# <a name="Overview">Overview</a>  
Motu is a robust web server allowing the distribution of met/ocean gridded data files through the web. 
Subsetter allows user to extract the data of a dataset, with geospatial, temporal and variable criterias. 
Thus, user download only the data of interest.  
A graphic web interface and machine to machine interfaces allow to access data and information on data (metadata).
The machine-to-machine interface can be used through a client written in python, freely available here https://github.com/clstoulouse/motu-client-python.
Output data files format can be netCDF3 or netCDF4.  
An important characteristic of Motu is its robustness: in order to be able to answer many users without crashing, Motu manages its incoming requests in a queue server.  
The aim is to obtain complete control over the requests processing by balancing the processing load according to criteria (volume of data to extract, number of requests to fulfill 
for a user at a given time, number of requests to process simultaneously).  
Moreover, Motu implements a request size threshold. Motu masters the amount of data to extract per request by computing, without any data processing, the result data size of the request.  
Beyond the allowed threshold, every request is rejected. The threshold is set in the configuration file.
Motu can be secured behind an authentication server and thus implements authorization. A CAS server can implement the authentication. 
Motu receives with authentication process user information, including a user profile associated with the account. 
Motu is configured to authorize or not the user to access the dataset or group of datasets which user is trying to access.  
For administrators, Motu allows to monitor the usage of the server: the logs produced by Motu allow to know who (login) requests what (dataset) and when, with extraction criterias.

# <a name="Architecture">Architecture</a>  
Motu is a Java Web Application running inside the Apache Tomcat application server. Motu can be run as a [single application](#ArchitectureSingleInstance) or can be scaled over [several instances](#ArchitectureScalability).


## <a name="ArchitectureOverall">Architecture overall</a>  

### <a name="ArchitectureSingleInstance">Architecture single instance</a>  
The clients ["motu-client-python"](#ClientPython) or an HTTP client like a [web browser](#ClientRESTAPI) are used to connect to Motu services.  
A frontal web, [Apache HTTPd](#InstallFrontal) for example, is used as a reverse proxy to redirect request to Motu server and also to serve the [downloaded](#motuConfig-downloadHttpUrl) data from Motu [download folder](#motuConfig-extractionPath).  
Motu server, runs on a Apache Tomcat server and can serve files either directly ["DGF"](#BSconfigServiceDatasetType) or by delegating extraction to Thredds server with NCSS or OpenDap [protocols](#BSconfigServiceDatasetType).  
A NFS server is used to share the netcdf files between Thredds and Motu DGF when they are not deployed on the same host.  
An (SSO CAS server)[#ConfigurationSystemCASSSO] is used for the authentication of users but Motu can also be deployed without any authentication system.  
The Apache HTTPd, on the top right corner is used to [serve the graphic chart](#InstallPublicFilesOnCentralServer) when several Motu Web server are deployed.

The schema below shows an example of Motu scalability architecture. The "i1, i2" are the Motu server deployed. They have to share the same [business configuration file](#ConfigurationBusiness) and the [download folder](#motuConfig-extractionPath).      

![Software architecture](./motu-parent/src/doc/softwareArchitecture.png "Motu software architecture, one instance")


### <a name="ArchitectureScalability">Architecture scalability</a>  
To run Motu over several instances, a [Redis server](#RedisServerConfig) has to be deployed in order to share to request id and status. The download folder of Motu has also to be shared between the different Motu instances.  
If can be on a NFS server or a GLusterFS server.  
The frontal web server "Apache HTTPd" must server the downloaded files and implements the load balencer between all Motu instances.   
All other server, CAS, NFS remains as on the single instance architecture.   
The same source code is used to run Motu with a single architecture or with several instances. It is just done by [configuration](#InstallationScalability).  
When Motu is scalable, one Motu server instance can run a download request, another distinct Motu server instance can respond to a get status request and a last one can respond the URL on the result file. 

![Software architecture](./motu-parent/src/doc/softwareArchitectureScalability.png "Motu software architecture, scalability")



	   
# <a name="ArchitectureInterfaces">Interfaces</a>  
## <a name="ArchitectureInterfacesServer">Server interfaces</a>  
All ports are defined in [motu.properties](#ConfigurationSystem) configuration file.

* __HTTP__: Apache tomcat manages incoming requests with HTTP protocol.
* __HTTPs__: Used to manage HTTPs incoming requests. This is delegated to Apache HTTPd frontal web server. Apache Tomcat is not intended to be used with HTTPs.
* __AJP__: Used to communicate with an Apache HTTPd frontal server
* __Socket for Shutdown__: Port opened by Tomcat to shutdown the server
* __JMX__: Used to monitor the application
* __Debug__: In development mode, used to remotely debug the application
  
## <a name="ArchitectureInterfacesExternal">External interfaces with other systems or tools</a>  
Motu has interfaces with other systems:  

* __DGF__: Direct Get File: Read dataset from the file system. (See how to [configure it](#AdminDataSetAdd).)
* __Unidata Thredds Data Server__: It connects with the NCSS or OpenDap HTTP REST API to run download request for example. (See how to [configure it](#AdminDataSetAdd).)
* __HTTP CAS Server__: Use for Single Sign On (SSO) in order to manager user authentication. (See how to [configure it](#ConfigurationSystem) "CAS SSO server" and check [profiles](#BSconfigService) attribute set on the dataset.)
* __CDO command line tool__: [CDO](#InstallCDO) is used to deal with 2 types of download requests, which are not covered by NCSS service of Thredds Data Server:  
  * a download request on a __range of depths__,  
  * a download request that come __across the boundary__ of the datasets (for global datasets)  
* __Redis__: Stores the request id and status into the Redis server when Motu is scaled over several instances.  
  
# <a name="ArchitectureDesign">Design</a>  
The Motu application has been designed by implementing the Three-Layered Services Application design. It takes many advantages in maintenance cost efficiency and in the ease of its future evolutivity.  
Three layers are set in the core "motu-web" project:  

* __USL__: User Service Layer: This layer manages all incoming actions through HTTP request
* __BLL__: Business Logic Layer: This layer manages all Motu business
* __DAL__: Data Access Layer: This layer manages all access to Motu external interfaces: DGF, Unidata server, CDO, ...

Each layer is an entry point of the application designed with a singleton. These three singletons gives access to high level managers which provides services by implementing a Java interface.
High level managers handle for example the configuration, the request, the catalog, the users.

A common package is also defined to provide utilities: log4j custom format, XML, String ...

# <a name="ArchitectureDesignD">Design details</a>  
The main project is "motu-web". This project is divided in three main layers detailed below:  

## <a name="ArchitectureDesignDMW">Motu-web project</a> 
### <a name="ArchitectureDesignDLayers">Layers</a> 
#### <a name="ArchitectureDesignDUSL">USL</a>  
* __usl/servlet/context/MotuWebEngineContextListener.java__: Apache tomcat ServletContextListener used to init and stop the application.  
* __usl/request__: All requests are managed with the "motu/web/servlet/MotuServlet.java" by following a command pattern. "action" HTTP parameter matches one of the "usl/request/actions" classes.  
* __usl/response__: XML and [Apache velocity](https://velocity.apache.org/) data model.  
* __usl/wcs__: All WCS requests and responses are managed in this package. The servlet entry point is defined with the class "motu/web/servlet/MotuWCSServlet.java".

#### <a name="ArchitectureDesignDBLL">BLL</a>  
* __bll/catalog__: Catalog and Product managers. Package "bll/catalog/product/cache" contains the product cache.
* __bll/config__: Configuration and version manager.  
* __bll/exception__: Business exceptions. MotuException is a generic one. MotuExceptionBase defines all other business exceptions.
* __bll/messageserror__: Message error manager
* __bll/request__: The queue server used to download requests
* __bll/users__: User manager

#### <a name="ArchitectureDesignDDAL">DAL</a>    
* __dal/catalog__: OpenDap, TDS or FILE catalog access.  
* __dal/config__: Configuration (/motu-web/src/main/resources/schema/MotuConfig.xsd) and version manager (configuration, distribution, products).  
* __dal/messageserror__: Manage messages for a specific error (/motu-web/src/main/resources/MessagesError.properties).
* __dal/request__: NCSS, OpenDAP and CDO
* __dal/tds__: NCSS and OpenDap datamodel
* __dal/users__: User manager

### <a name="ArchitectureDesignDDynamics">Daemon threads</a> 
"motu-web" project starts daemon threads for:  

* __bll/catalog/product/cache__: Keep a product cache and refresh it asynchronously to improve response time 
* __bll/request/cleaner__: Clean files (extracted files, java temp files) and request status (stored into java map or list objects)
* __bll/request/queueserver__: Contains a thread pool executor to treat download requests
* __dal/request/cdo__: A queue server used to manage requests using CDO tool to avoid using too much RAM. As CDO uses a lot of RAM memory, 
requests that require CDO to be processed are in sequence and only one is processed at a time




## <a name="ArchitectureDesignDOthers">Other projects</a>
* __motu-api-message__: JAXB API: All errors types, status mode, ... /motu-api-message/src/main/schema/XmlMessageModel.xsd  
* __motu-api-rest__: @Deprecated. Not used since Motu v3.
* __motu-distribution__: Deployment tools (ant script, install folder structure, ...)
* __motu-library-cas__: Used to manage JAVA HTTP client with CAS server.
* __motu-library-converter__: JodaTime, ...
* __motu-library-inventory__: Used for DGF access. JAXBmodels are /motu-library-inventory/src/main/resources/fr/cls/atoll/motu/library/inventory/Inventory.xsd and CatalogOLA.xsd
* __motu-parent__: Maven parent, eclipse launchers, documentation
* __motu-poducts__: Source code and scripts used to build archive motu-products.tar.gz (JDK, Apache tomcat, CDO tools)
* __motu-scripts__: ./motu bash script
* __motu-web__: Main Motu project. See [Motu-web project](#ArchitectureDesignDMW) for details.
  
  
## <a name="ArchiAlgo">Algorithm details</a>  

### <a name="ArchiAlgoDownloading1Point">Downloading 1 point</a>  
Schema below displays a subset of a dataset variable as an array of 2 longitudes and 2 latitudes. At each intersection, we have got 1 real value (10, 11, 12, 13) as defined in the gridded data.  
But which result value is returned by Motu when the request target a location between those longitudes and latitudes ?   
As we can see below, 4 areas are displayed and the nearest value from the requested location is returned.

![Downloading 1 point](./motu-parent/src/doc/downwloading1point.png "Motu algorithm: Downloading 1 point")
	 
# <a name="Development">Development</a>  

## <a name="DEVSRC">Source code</a>
Source code can be downloaded directly from Github.  

```  
mkdir motugithub  
cd motugithub  
git clone https://github.com/clstoulouse/motu.git  
    #In order to work on a specific version  
git tag -l  
git checkout motu-X.Y.Z  
git status  
cd motu  
cd motu-parent  
```

## <a name="DEV">Development environment</a>  

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


## <a name="COMPILATION">Compilation</a>  

Maven is used in order to compile Motu.  
You have to set maven settings in order to compile.  
Copy/paste content below in a new file settings.xml and adapt it to your information system by reading comments inside.

```  
    <settings>  
    <!-- localRepository: Path to the maven local repository used to store artifacts. (Default: ~/.m2/repository) -->  
    <localRepository>J:/dev/cmems-cis-motu/testGitHub/m2/repository&lt;/localRepository>  
    <!-- proxies: Optional. Set it if you need to connect to a proxy to access to Internet -->  
    <!--   
    <proxies>  
       <proxy>  
          <id>cls-proxy</id>  
          <active>true</active>  
          <protocol>http</protocol>  
          <host></host>  
          <port></port>  
          <username></username>  
          <password></password>  
          <nonProxyHosts></nonProxyHosts>  
        </proxy>  
      </proxies>  
    -->   
    <!-- Repositories used to download Maven artifacts in addition to https://repo.maven.apache.org   
         cls-to-ext-thirdparty : contains patched libraries and non public maven packaged libraries  
         geotoolkit: contains geographical tools libraries  
    -->  
    <profiles>    
       <profile>  
         <id>profile-cls-cmems-motu</id>  
         <repositories>  
            <repository>  
              <id>cls-to-ext-thirdparty</id>  
              <name>CLS maven central repository, used for CMEMS Motu project</name>  
              <url>http://mvnrepo.cls.fr:8081/nexus/content/repositories/cls-to-ext-thirdparty</url>  
            </repository>  
            <repository>  
              <id>geotoolkit</id>  
              <name>geotoolkit</name>  
              <url>http://maven.geotoolkit.org/</url>  
            </repository>  
        </repositories>  
       </profile>  
     </profiles>  
     </settings>
```

This step is used to generate JAR (Java ARchives) and WAR (Web application ARchive).   

```  
mkdir motu  
cd motu   
  #Copy paste the content above inside settings.xml  
vi settings.xml  
  #Get source code of the last Motu version  
git clone https://github.com/clstoulouse/motu.git  
cd motu/motu-parent  
  #  This remove the maven parent artifact from pom.xml, or remove lines below manually:  
  #  <parent>  
  #        <artifactId>cls-project-config&lt;/artifactId>  
  #        <groupId>cls.commons&lt;/groupId>  
  #        <version>1.2.00&lt;/version>  
  # </parent>  
sed -i '6,10d' pom.xml  
  #Compile the source code  
mvn -s ../../settings.xml -gs ../../settings.xml -Pprofile-cls-cmems-motu -Dmaven.test.skip=true clean install  
...  
[INFO] BUILD SUCCESS  
...    
``` 

All projects are built under target folder.  
The Motu war is built under "/motu-web/target/motu-web-X.Y.Z-classifier.war".  
It embeds all necessary jar libraries.  

## <a name="Packaging">Packaging</a>  
This packaging process can be run only on CLS development environment.
This is an helpful script used to packaged as tar.gz the different projects (products, distribution (server and client), configuration, themes).   
So if you try to run it outside of CLS development environment, you will have to tune and remove many things to run it successfully (in particular all which is related to motu-config and motu-products).
This step includes the compilation step. Once all projects are compiled, it groups all archives in a same folder in order to easy the final delivery.  
You have to set ANT script inputs parameter before running it.  
See /motu-distribution/build.xml header to get more details about inputs.  
```  
cd /motu-distribution  
ant  
cd target-ant/delivery/YYYYMMDDhhmmssSSS  
```  

4 folders are built containing archives:  

* src: contains sources of motu application and the configuration files
   * motu-$version-src.tar.gz  
   * motu-client-python-$version-src.tar.gz  
   * motu-config-$version-src.tar.gz  
   * motu-web-static-files-$version-src.tar.gz  
* motu: contains the built application archive and the products (java, tomcat, cdo) archive  
   * motu-distribution-$version.tar.gz
   * motu-products-$version.tar.gz
* config: contains two kind of archives:
  * motu-config-X.Y.Z-classifier-$timestamp-$target.tar.gz:  the built configurations for each target platform  
  * motu-web-static-files-X.Y.Z-classifier-$timestamp-$target.tar.gz: The public static files (css, js) for each target platform 
* motu-client  
  * motu-client-python-$version-bin.tar.gz

# <a name="Installation">Installation</a>  



## <a name="InstallPrerequisites">Prerequisites</a>  

In this chapter some paths are set. For example "/opt/cmems-cis" is often written to talk about the installation path.
You are free to install Motu in any other folder, so in the case, replace "/opt/cmems-cis" by your installation folder.  
This installation is used to install Motu on a [single instance](#ArchitectureSingleInstance). To scale Motu on [several instances](#ArchitectureScalability), refers to [Install a scalable Motu over several instances](#InstallationScalability).

### <a name="InstallPrerequisitesHard">Motu host, hardware settings</a>
OS target: Linux 64bits (Tested on centos-7.2.1511)

Minimal configuration for an __operational usage__:  

* __CPU__: 4 CPU, 2,4GHz
* __RAM__: 32 Gb RAM
* __Storage__: 
  * Motu installation folder 15Gb: can be install on the OS partition (default folder /opt/cmems-cis)
  * Motu download folder 200Gb: by default [/opt/cmems-cis/motu/data/public/download](#InstallFolders)  
    Has to be installed in a dedicated partition to avoid freezing Motu if disk is full.
Note that the available space of the download folder has to be tuned, depending on:  
     * The number of users which run requests at the same time on the server
     * The size of the data distributed
   
Once started, you can [check performance](#ExpMonitorPerf).
   
For __test usage__ we recommend:  

* __CPU__: 2 CPU, 2,4GHz
* __RAM__: 10 Gb RAM
* __Storage__: 
  * Motu installation folder 15Gb
  * Motu download folder 50Gb: by default [motu/data/public/download](#InstallFolders)  


### <a name="InstallPrerequisitesSoft">Motu host, software settings</a>
Motu embeds all its dependencies (Java , Tomcat, CDO). All versions of these dependencies will be visible in the folder name once the Motu product archive is extracted.  
For example:  
```
ls -1 /opt/cmems-cis/motu/products  
apache-tomcat-7.0.69  
cdo-group  
jdk1.7.0_79  
README  
version-products.txt  
```  

So bash shell is only required on the Linux host machine.  

### <a name="InstallPrerequisitesExternalInterfaces">External interfaces</a>
Motu is able to communicate with different external servers:  

* __Unidata | THREDDS Data Server (TDS)__: Motu has been only tested with TDS v4.6.10 2016-04-20. The links to this server are set in the [Business settings](#ConfigurationBusiness) and are used to run OpenDap or subsetter interfaces. If Motu runs only with DGF, this server is not required.  
Note that some specific characters have to be relaxed, e.g. when TDS is installed on Apache Tomcat, add attribute relaxedQueryChars="&lt;&gt;[\]{|}" in the connector node by editing conf/server.xml from your TDS tomcat installation folder:  
```  
<Connector relaxedQueryChars="&lt;&gt;[\]{|}" port="8080" ...  
```
as reported in this [forum topic](https://groups.google.com/a/opendap.org/d/msg/support/ixTqhDXoLZQ/IT0lvZQ7CAAJ).  
Without this configuration Motu server can raised exeptions visible in the Motu "errors.log", e.g.:  
```
ERROR fr.cls.atoll.motu.web.bll.catalog.product.cache.CacheUpdateService.updateConfigService Error during refresh of the describe product cache, config service=..., productId=...  
fr.cls.atoll.motu.web.bll.exception.MotuException: Error in NetCdfReader open - Unable to aquire dataset - location data:  
Caused by: java.io.IOException: http://.../thredds/dodsC/$dataset is not a valid URL, return status=400  
```  
  
  
 
* __Single Sign-On - CAS__: The link to this server is set in the [System settings](#ConfigurationSystem). If Motu does not use SSO, this server is not required.

The installation of these two servers is not detailed in this document. Refer to their official web site to know how to install them.

  
### <a name="InstallPrerequisitesSeveralsInstances">Several Motu instances on a same host</a>
If you need to instance several instances of Motu server on a same host, you have to:  

* __RAM__: set 32Go of RAM for each instance. For example, two Motu instances on a same host requires 64Go  
* __Storage__: allocate disk space, in relationship with the Motu usage. Download dedicated partition can be shared or dedicated.  
* __Folders__: Install each Motu instance in a dedicated folder:  
  * /opt/motu1/motu, 
  * /opt/motu2/motu, 
  * ..., 
  * /opt/motuN/motu  

  
## <a name="UpgradeFromMotu2x">Upgrade from Motu v2.x</a>    

Check this section only if you have installed Motu v2.x and you want to install Motu 3.x.
In this section we consider that your Motu installation folder of version 2.x is "/opt/atoll/misgw/".  

### Upgrade the software
First stop Motu v2.x: /opt/atoll/misgw/stop-motu.  
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
So you can use exactly the same file, but it is important to update some fields to improve performance and future compatibility. 
Copy your old motuConfiguration.xml file to the folder /opt/cmems-cis/motu/config, for example:  
```
cp  /opt/atoll/misgw/motu-configuration-sample-misgw/resources/motuConfiguration.xml /opt/cmems-cis/motu/config
```  

Then update attributes below:  

* use the TDS subsetter protocol to improve performance of product download  
   * __motuConfig/configService/catalog__
      * __ncss__: See [ncss protocol](#BSmotuConfigNCSS)
      * __urlSite__: Update URL to use the TDS subsetter URL. See [Add a dataset, TDS NCSS protocol](#AdminDataSetAdd)
* check the attributes used to serve downloaded datasets  
   * __motuConfig__
      * __extractionPath__ See [extractionPath](#motuConfig-extractionPath)
      * __downloadHttpUrl__ Set the URL used to serve files from extractionPath
* remove all @deprecated attributes listed below to ease future migrations. You can read the attribute description in [Business configuration](#ConfigurationBusiness).  
   * __XML header__ Remove header below  
```
< !DOCTYPE rdf:RDF [  
<!ENTITY myoceanUrn "http://purl.org/myocean/ontology/service/database#">  
]>  
```
   * __motuConfig__
      * __maxSizePerFile__ This attribute definition has been updated. See [maxSizePerFile parameter configuration](#motuConfig-defaultService)  
      * __maxSizePerFileTDS__ Rename this attribute to maxSizePerFileSub. See [maxSizePerFileSub parameter configuration](#motuConfig-defaultService)  
      * __runGCInterval__ Remove the attribute
      * __httpDocumentRoot__ Remove the attribute
      * __useAuthentication__ Remove the attribute
      * __defaultActionIsListServices__ Remove the attribute
      * __useProxy__, __proxyHost__, __proxyPort__, __proxyLogin__, __proxyPwd__ Remove the attributes
      * __defaultService__  Remove the attribute, See [defaultService](#motuConfig-defaultService) . It was previously used to declare a default config Service. Now this attribute sets a default action. If the action set is unknown, an error log will be written and user is redirected to the listServices action which is the default one.
   * __motuConfig/configService__
      * __defaultLanguage__ Remove the attribute
      * __defaultPriority__ Remove the attribute
      * __httpBaseRef__ Remove the attribute
      * __veloTemplatePrefix__ Remove the attribute
   * __motuConfig/queueServerConfig__
      * __defaultPriority__ Remove the attribute
   * __motuConfig/queues__
      * __batch__ Remove the attribute
      * __lowPriorityWaiting__ Remove the attribute
   * __motuConfig/configFileSystem__ Remove the node
* after starting Motu, if there is issues with the graphic chart, check the attributes below.
   * __motuConfig__
      * __httpBaseRef__ See [httpBaseRef](#motuConfig-httpBaseRef) attribute
   * __motuConfig/configService__
      * __httpBaseRef__ Remove the attribute

### Upgrade DGF
Resource URN attributes are not handled in the same way in Motu v3.  
In Motu __v2.x__, URN attributes had values set with an ontology URL:  
``` 
<resource urn="http://purl.org/myocean/ontology/product/database#dataset-bal-analysis-forecast-phys-V2-dailymeans">
```   

In Motu __v3.x__, you have to upgrade URN attributes with only the value found after the # character:  
``` 
<resource urn="dataset-bal-analysis-forecast-phys-V2-dailymeans">
```   
  



#### Log files
In CMEMS-CIS context the log file motuQSlog.xml is stored in a specific folder in order to be shared.  
You have so to check that with the new version this log file is well written in the shared folder.
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
In the CMEMS context, the installation on a dissemination unit is ended, static files are installed on a [central server](#InstallPublicFilesOnCentralServer).  

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
referencing static files only once on a unique machine. This is the case in the CMEMS context, where each dissemination unit host a Motu server, and 
a central server hosts static files.  
If you runs only one install of Motu, you can install static files directly on Motu Apache tomcat server.

#### <a name="InstallPublicFilesOnCentralServer">On a central server</a>    
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
Climate Data Operators version x.y.z (http://mpimet.mpg.de/cdo)  
[...]  
```  

If error appear like ones below, it certainly means that GLIC is not in the LD_LIBRARY_PATH.
```
$MotuInstallDir/motu/products/cdo-group/cdo-x.z.z-home/bin/cdo: error while loading shared libraries: libhdf5.so.10: cannot open shared object file: No such file or directory
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
     * __inventories__: This folder can be used to store the DGF files.  
     * __transaction__: This folder is used to serve the [transaction accounting logs](#LogbooksTransactions)
     * __static-files__: Used to store public static files. This folder can be served by a frontal Apache HTTPd Web server or Motu Apache Tomcat. In the CMEMS-CIS context, it is not used as static files are deployed on a central web server.      
* __log:__ Folder which contains all log files. Daily logging are suffixed by yyyy-MM-dd.
  * __logbook.log:__ Motu application logs (errors and warning are included)
  * __warnings.log:__ Motu application warnings
  * __errors.log:__ Motu application errors
  * __motuQSlog.xml,motuQSlog.csv:__ Motu queue server logs messages (transaction accounting logs), format is either xml or csv
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





## <a name="InstallFrontal">Setup a frontal Apache HTTPd server</a>  
Apache HTTPd is used as a frontal HTTP server in front of Motu Http server.
It has several aims:  

* Delegate HTTP requests to Motu HTTP server    
* Serve directly extracted dataset files written after a download action. The folder in which requests are written is [configurable](#motuConfig-extractionPath). URL used to download those files is "http://$ipMotuServer/mis-gateway/deliveries". This URL is [configurable](#motuConfig-downloadHttpUrl).  
* Serve the download transaction logbook files. The folder in which log files are written is [configurable](#LogSettings).  
* Manage errors like 403, 404. Motu server manages errors web page by displaying a custom message. Redirect to the URL "http://$ipMotuServer/motu-web/Motu?action=httperror&code=$errorCode" by replacing $errorCode with the HTTP error code.  
* Optionally acts as a load balancer when several instances of Motu.


See sample of the Apache HTTPd configuration in the Motu installation folder: __config/apache-httpd-conf-sample__  
The configuration is described for Apache2 contains files:

* __001_httpd-vhosts-motu.conf:__ Main apache configuration file used for Motu, replace __$serverHostName__ by the server host, and $webmasterEmail by the webmaster or administrator email.
* __apache2.conf:__ Use to show how timeout parameter shall be set
* __enableApacheModules.sh__ Describe the Apache modules to enable

When an SSO cas server is used, you have to st the property [cas-auth-serverName](#ConfigurationSystemCASSSO) to http://$serverHostName

Apache HTTPd can be used at different levels. The Apache HTTPd above is the one installed on the same machine as Motu.
An Apache HTTPd can be used as a frontal to manage Apache HTTPd load balancing. In the case, you can set up with the following example:  

```  
 # Use to authenticate users which want to download transaction files  
< Location /datastore-gateway/transactions/* >  
|--AuthType Basic  
|--AuthName "XXX"  
|--AuthUserFile /XXX/password.conf  
|--Require valid-user  
< / Location>   
  
 # Used to serve URL requested after a CAS authentication  
 # Because Motu SSO client set a redirection URL directly to its webapp name  
 # so we have to take into account the webapp name in Apache HTTPd  
ProxyPass /motu-web http://$motuTomcatIp:$motuTomcatPort/motu-web  
ProxyPassReverse /motu-web http://$motuTomcatIp:$motuTomcatPort/motu-web  
        
 # Used to serve Motu requests   
    # /mis-gateway-servlet These rules are used for retro compatibility between Motu v2.x and Motu v3.x  
ProxyPass /mis-gateway-servlet http://$motuTomcatIp:$motuTomcatPort/motu-web  
ProxyPassReverse /mis-gateway-servlet http://$motuTomcatIp:$motuTomcatPort/motu-web  
ProxyPreserveHost On  
    # /motu-web-servlet This URL is sometimes used.  
    # It can be customized depending of your current installation. If you have any doubt, keep this rule.  
ProxyPass /motu-web-servlet http://$motuTomcatIp:$motuTomcatPort/motu-web  
ProxyPassReverse /motu-web-servlet http://$motuTomcatIp:$motuTomcatPort/motu-web  
                
ProxyPass /datastore-gateway/transactions http://$apacheHTTPdOnMotuHost/datastore-gateway/transactions  
ProxyPassReverse /datastore-gateway/transactions http://$apacheHTTPdOnMotuHost/datastore-gateway/transactions  

ProxyPass /datastore-gateway/deliveries http://$apacheHTTPdOnMotuHost/datastore-gateway/deliveries  
ProxyPassReverse /datastore-gateway/deliveries http://$apacheHTTPdOnMotuHost/datastore-gateway/deliveries  

< Location /motu-web-servlet/supervision>  
|--Order allow,deny  
|--Allow from All  
< /Location>  
```  



## <a name="InstallSecurity">Security</a>  

### <a name="InstallSecurityRunHTTPs">Run Motu as an HTTPS Web server</a>  
Motu is a web server based on Apache Tomcat.  
In order to secure HTTP connections with a client, HTTPs protocol can be used.  
You have two choices:  

* __Motu as a standalone web server__  
  In this case only Motu is installed.  
  Refers to the Apache Tomcat official documentation to know how to set SSL certificates: [SSL/TLS Configuration HOW-TO](#https://tomcat.apache.org/tomcat-7.0-doc/ssl-howto.html)
* __Motu with an Apache HTTPd frontal web server__  
  In this case Motu is installed and also a frontal Apache HTTPd server.  
  Refers to the Apache HTTPd official documentation: [SSL/TLS Strong Encryption: How-To](#https://httpd.apache.org/docs/2.4/ssl/ssl_howto.html)  
  [Apache HTTPd](#InstallFrontal) communicates with Apache Tomcat with the [AJP protocol](#ConfigurationSystem).

### <a name="InstallSecuritySSO">Motu and Single Sign On</a>  
In order to manage SSO (Single Sign On) connections to Motu web server, Motu uses an HTTPs client.  
All documentation about how to setup is written in chapter [CAS SSO server](#ConfigurationSystemCASSSO).


## <a name="InstallationScalability">Install a scalable Motu over several instances</a>  
You have to install a [Redis server](https://redis.io/). (Motu has been tested with Redis version 4.0.8, 64 bit)
To use Redis in order to share the request ids and status between all Motu instances, you just have to set the Redis settings in the [business configuration file](#RedisServerConfig). If this parameter is not set, the request id and status are stored in RAM.     
You have to share the [download folder](#motuConfig-extractionPath) folder between all instances with a NFS mount, GlusterFS or any other file sharing system.   
You have to set a frontal web server to server the [downloaded](#motuConfig-downloadHttpUrl) files from the Motu server and to load balance the requests between all Motu servers. 

# <a name="Configuration">Configuration</a>  

This chapter describes the Motu configuration settings.  
All the configuration files are set in the $installDir/motu/config folder.  

* [Configuration directory structure](#ConfigurationFolderStructure)
* [Business settings](#ConfigurationBusiness)
* [System settings](#ConfigurationSystem)
* [Log settings](#LogSettings)

  
## <a name="ConfigurationFolderStructure">Configuration directory structure</a>  
cd $installDir/motu/config
  
* __config:__ Folder which contains the motu configuration files.
  * __[motu.properties](#ConfigurationSystem):__ JVM memory, network ports of JVM (JMX, Debug) and Tomcat (HTTP, HTTPS, AJP, SHUTDOWN). CAS SSO server settings.
  * __[motuConfiguration.xml](#ConfigurationBusiness):__ Motu settings (Service, Catalog via Thredds, Proxy, Queues, ....)
  * __[log4j.xml](#LogSettings):__ Log4j v2 configuration file
  * __[standardNames.xml](#ConfigStandardNames):__ Contains the standard names
  * __version-configuration.txt:__ Contains the version of the current Motu configuration.
  
## <a name="ConfigurationBusiness">Business settings</a>  
### motuConfiguration.xml: Motu business settings  
This file is watched and updated automatically. This means that when Motu is running, this file has to be written in a atomic way.  

You can configure 3 main categories:  

* [MotuConfig node : general settings](#BSmotuConfig)
* [ConfigService node : catalog settings](#BSconfigService)
* [QueueServerConfig node : request queue settings](#BSqueueServerConfig)
* [RedisConfig node : Redis server config](#RedisServerConfig)
  
  
If you have not this file, you can extract it (set the good version motu-web-X.Y.Z.jar):  
```
/opt/cmems-cis/motu/products/jdk1.7.0_79/bin/jar xf /opt/cmems-cis/motu/tomcat-motu/webapps/motu-web/WEB-INF/lib/motu-web-X.Y.Z.jar motuConfiguration.xml
```   
    
  
If you have this file from a version anterior to Motu v3.x, you can reuse it. In order to improve global performance, you have to upgrade some fields:  
* [ncss](#BSmotuConfigNCSS) Set it to "enabled" to use a faster protocol named subsetter rather than OpenDap to communicate with TDS server. ncss must be enabled only with regular grid. The datasets using curvilinear coordinates (like ORCA grid) can not be published with ncss. Thus, ncss option must be set to disable or empty.  
* [httpBaseRef](#motuConfig-httpBaseRef) shall be set to the ULR of the central repository to display the new theme  
* [ExtractionFilePatterns](#BSmotuConfigExtractionFilePatterns) to give a custom name to the downloaded dataset file  
  
  

#### <a name="BSmotuConfig">Attributes defined in motuConfig node</a>  

##### <a name="motuConfig-defaultService">defaultService</a>  
A string representing the default action in the URL /Motu?action=$defaultService  
The default one is "listservices".  
All values can be found in the method USLRequestManager#onNewRequest with the different ACTION_NAME.  

##### dataBlockSize
Number of data in Ko that can be read in the same time. Default is 2048Kb.

##### maxSizePerFile
This parameter is only used with a catalog type set to "FILE" meaning a DGF access.  
It allows download requests to be executed only if data extraction is lower than this parameter value.  
Unit of this value is MegaBytes.  
Default is 1024 MegaBytes.  
Example: maxSizePerFile="2048" to limit a request result file size to 2GB.  

##### maxSizePerFileSub
This parameter is only used with a catalog type used with Opendap or Ncss.  
It allows download requests to be executed only if data extraction is lower that this parameter value.  
Unit of this value is MegaBytes.  
Default is 1024 MegaBytes.  
Example: maxSizePerFileSub="2048" to limit request result file size to 2GB.

##### maxSizePerFileTDS
@Deprecated from v3 This parameter is not used and has been replaced by maxSizePerFile and maxSizePerFileSub.   
Number of data in Megabytes that can be written and download for a Netcdf file. Default is 1024Mb.

##### <a name="motuConfig-extractionPath">extractionPath</a>  
The absolute path where files downloaded from TDS are stored.  
For example: /opt/cmems-cis/motu/data/public/download
It is recommended to set this folder on an hard drive with very good performances in write mode.
It is recommended to have a dedicated partition disk to avoid freezing Motu if the hard drive is full.
By default value is $MOTU_HOME/data/public/download, this folder can be a symbolic link to another folder.  
String with format ${var} will be substituted with Java property variables. @See System.getProperty(var)  

##### <a name="motuConfig-downloadHttpUrl">downloadHttpUrl</a>
Http URL used to download files stored in the "extractionPath" described above. It is used to allow users to download the result data files.  
This URL is concatenated to the result data file name found in the folder "extractionPath".  
When a frontal HTTPd server is used, it is this URL that shall be configured to access to the folder "extractionPath".  
String with format ${var} will be substituted with Java property variables. @See System.getProperty(var)  

##### <a name="motuConfig-httpBaseRef">httpBaseRef</a>  
Http URL used to serve files from to the path where archive __motu-web-static-files-X.Y.Z-classifier-buildId.tar.gz__ has been extracted.  
For example: 

* When __httpBaseRef__ is set to an __URL__, for example __"http://resources.myocean.eu/motu"__, this URL serves a folder which contains ./css/motu/motu.css.  
For example, it enables to serve the file http://resources.myocean.eu/motu/css/motu/motu.css  
* When __httpBaseRef__ is set to __"."__, it serves static files which are included by default in Motu application
* When __httpBaseRef__ is __removed__ (not just empty but attribute is removed), it serves a path accessible from URL $motuIP/${motuContext}/motu

__IMPORTANT__: When Motu URL starts with "HTTPS", if you set an URL in __httpBaseRef__, this URL has also to start with "HTTPS". On the contrary, 
when Motu URL starts with "HTTP", if you set an URL in __httpBaseRef__, this URL can start with "HTTP" or "HTTPS".

        
##### cleanExtractionFileInterval
In minutes, oldest result files from extraction request are deleted. This check is done each "runCleanInterval" minutes.    
Default = 60min

##### <a name="BScleanRequestInterval">cleanRequestInterval</a>  
In minutes, oldest status (visible in [debug](#ExploitDebug) view) than this time are removed from Motu. This check is done each "runCleanInterval" minutes.  
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

Default is ".*\.nc$|.*\.zip$|.*\.tar$|.*\.gz$|.*\.extract$"  


##### extractionFileCacheSize
Size in Mbytes.  
A clean job runs each "runCleanInterval". All files with a size higher than this value are deleted by this job.
If value is zero, files are not deleted.  
Default value = 0.

##### <a name="describeProductCacheRefreshInMilliSec">describeProductCacheRefreshInMilliSec</a>
Provide the delay to wait to refresh the meta-data of products cache after the last refresh.  
Motu has a cache which is refreshed asynchronously. Cache is first refreshed as soon as Motu starts.   
Then Motu waits for this delay before refreshing again the cache.  
This delay is provided in millisecond.  
The default value is 60000 meaning 1 minute.  
  
Logbook file (motu/log/logbook.log) gives details about time taken to refresh cache, for example:   
```  
INFO  CatalogAndProductCacheRefreshThread.runProcess Product and catalog caches refreshed in 2min 19sec 75msec  
```
Logbook file gives details per config service ($configServiceId) about dedicated time taken to refresh cache, for example:   
```  
INFO  CatalogAndProductCacheRefreshThread.runProcess Refreshed statistics: $configServiceId@Index=0min 34sec 180msec, $configServiceId@Index=0min 31sec 46msec, ...   
```  
They are sorted by config service which has taken the most time first.  
@Index All config services are refreshed sequentially. This index is the sequence number for which this cached has been refreshed.
  
Example of archived data with several To of data. Cache is refreshed daily: describeProductCacheRefreshInMilliSec=86400000   
Example of real time data with several Go of data. Cache is refreshed each minute: describeProductCacheRefreshInMilliSec=60000    

##### runGCInterval
@Deprecated from v3 This parameter is not used. 

##### httpDocumentRoot
@Deprecated from v3 This parameter is not used. 
Document root of the servlet server.   

##### wcsDcpUrl 
Optional attribute. Used to set the tag value "DCP" in the response of the [WCS GetCapabilities](#GetCapabilities) request with a full URL.
The WCS DCP URL value is define using the following priority order:
	- The value of this parameter defines on the motuConfiguration.xml file. The value can be directly the URL to use or the name of a java property define between {} which contains the value of the URL.
	- The java property "wcs-dcp-url" value
	- The URL of the web server on which Motu webapps is deployed 
This attribute can be set when you use a frontal web server to serve the WCS requests, e.g. http://myFrontalWebServer/motu/wcs and your frontal is an HTTP proxy to http://motuWebServer/motu-web/wcs.  
        
##### useAuthentication
@Deprecated from v3 This parameter is not used. It is redundant with parameter config/motu.properties#cas-activated.


##### defaultActionIsListServices
@Deprecated from v3 This parameter is not used.  

##### Configure the Proxy settings  
@Deprecated from v3 This parameter is not used.
To use a proxy in order to access to a Threads, use the [JVM properties](#ConfigurationSystem), for example:  

```  
tomcat-motu-jvm-javaOpts=-server -Xmx4096M  ... -Dhttp.proxyHost=monProxy.host.fr -Dhttp.proxyPort=XXXX -Dhttp.nonProxyHosts='localhost|127.0.0.1'
```  


* __useProxy__  
* __proxyHost__  
* __proxyPort__  
* __proxyLogin__  
* __proxyPwd__ 


##### <a name="refreshCacheToken">refreshCacheToken</a>   

This token is a key value which is checked to authorize the execution of the cache refresh when it is request by the administrator .
If the token value provided by the administrator doesn't match the configured token value, the refresh is not executed and an error is returned.
A default value "a7de6d69afa2111e7fa7038a0e89f7e2" is configured but it's hardly recommended to change this value. If this token is not changed, it is a security breach and 
a log ERROR will be written while the configuration will be loaded.
The value can contains the characters [A-Za-z] and specials listed here ( -_@$*!:;.,?()[] )
It's recommended to configure a token with a length of 29 characters minimum.

##### downloadFileNameFormat  
Format of the file name result of a download request.  
2 dynamic parameters can be used to configure this attribute:  

* __@@requestId@@__: this pattern will be replaced in the final file name by the id of the request.  
* __@@productId@@__: this pattern will be replaced in the final file name by the id of the requested product.  

If this attribute is not present, default value is: "@@productId@@_@@requestId@@.nc"

##### motuConfigReload  
Configure how motu configuration is reloaded.  
Arguments are only 'inotify' or an 'integer in seconds'. 'inotify' is the default value.  
* __'inotify'__: reload as soon as the file is updated (works only on local file system, not for NFS file system).  
* __'integer in seconds'__: reload each X second the configuration in 'polling' mode. If this integer is equals or lower than 0, it disables the refresh of the configuration.  


#### <a name="BSconfigService">Attributes defined in configService node</a>  

##### <a name="BSconfigServiceName">name</a>  
String to set the config service name
If the value of this attribute contains some special caracters, those caracters have not to be encoded.
For example, if the value is an URL, the caracters ":" and "/" have not to be encoded like "%2E" or "%3A".

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

##### <a name="refreshCacheAutomaticallyEnabled">refreshCacheAutomaticallyEnabled</a>
Optional, boolean used to determine if the current config service have its cache updated automatically by Motu or not.
Default value is "true". 
"true" means that the config service cache update is executed automatically by Motu.

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
ncss must be enabled only with regular grid. The datasets using curvilinear coordinates (like ORCA grid) can not be published with ncss. Thus, ncss option must be set to disable or empty.
Without this attribute or when empty, Motu connects to TDS with Opendap protocol. If this attribute is set to "enabled" Motu connects to TDS with NCSS protocol in order to improve performance.   
We recommend to use "enabled" for regular grid datasets. 
Values are: "enabled", "disabled" or empty.

##### urlSite
* TDS URL  
For example: http://$ip:$port/thredds/  

* DGF URL  
For example: file:///opt/publication/inventories

#### <a name="BSqueueServerConfig">Attributes defined in queueServerConfig node</a>  

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
Size in Megabyte. A request has a size. The queue in which this request will be processed is defined by the request size.
All queues are sorted by size ascending. A request is put in the last queue which has a size lower than the request size.
If the request size if higher than the bigger queue dataThreshold, request is not treated and an error message is returned.  
This parameter is really useful when a Motu is used to server several kind of file size and you want to be sure that file with a specific size does no slow down request of small data size.  
In this case you can configure two queues and set a number of threads for each in order to match the number of processors. The JVM, even if requests for high volume are running, will be able to
process smallest requests by running the thread on the other processor core. Sp processing high volume requests will not block the smallest requests.  


##### Child node: lowPriorityWaiting
@Deprecated from v3 This parameter is not used.

#### <a name="RedisServerConfig">Attributes defined in redisConfig node</a>  
This optional node is used to run Motu in a [scalable architecture](#ArchitectureScalability). Do not add this node when you just run one single Motu instance.  
Once this node is added, Motu stores all its request ids and status in Redis.  

##### host
Define the host (ip or server name) where is deployed the Redis server od Redis cluster used by Motu to share the RequestId and RequestStatus data.
Default value is localhost

##### port
Define the port used by the Redis server or Redis cluster used by Motu to share the requestId and RequestStatus data.
Default value is 6379  

##### prefix
Define the prefix used to build the RequestId value of the shared RequestStatus data.
Default value is requestStatus

##### isRedisCluster 
Define if the redis server in in cluster mode.
This is a boolean value.
By default is set to false and the cluster mode is not activate.
To activate the cluster, the value have to be set on true.

## <a name="ConfigurationSystem">System settings</a>  

### motu.properties: Motu system settings  

System settings are configured in file config/motu.properties  
All parameters can be updated in the file.  

* [Java options](#ConfigurationSystemJavaOptions)
* [Tomcat network ports](#ConfigurationSystemTomcatNetworkPorts)
* [CAS SSO server](#ConfigurationSystemCASSSO)

#### <a name="ConfigurationSystemJavaOptions">Java options</a>
The three parameters below are used to tune the Java Virtual Machine:  
   &#35; -server: tells the Hostspot compiler to run the JVM in "server" mode (for performance)  
__tomcat-motu-jvm-javaOpts__=-server -Xmx4096M -Xms512M -XX:PermSize=128M -XX:MaxPermSize=512M  
__tomcat-motu-jvm-port-jmx__=9010  
__tomcat-motu-jvm-address-debug__=9090  
__tomcat-motu-jvm-umask__=tomcat|umask|0000 [(More details...)](#ConfigurationSystemTomcatUmask)

##### <a name="ConfigurationSystemTomcatUmask">Tomcat umask</a>
By default, if tomcat-motu-jvm-umask is not set, motu sets the umask with result of the command `umask`  
__tomcat-motu-jvm-umask__=umask|tomcat|0000  
* __umask__:  By default, if tomcat-motu-jvm-umask is not set, motu sets the umask with result of the command `umask`  
* __tomcat__: Apache Tomcat process forces umask to 0027 (https://tomcat.apache.org/tomcat-8.5-doc/security-howto.html)  
* __0000__:   Custom umask value  
Values 0002 or umask are recommended if Motu download results are served by a frontal web server

#### <a name="ConfigurationSystemTomcatNetworkPorts">Tomcat network ports</a>
The parameters below are used to set the different network ports used by Apache Tomcat.  
At startup, these ports are set in the file "$installdir/motu/tomcat-motu/conf/server.xml".    
But if this file already exist, it won't be replaced. So in order to apply these parameters, remove the file "$installdir/motu/tomcat-motu/conf/server.xml".  
  
__tomcat-motu-port-http__=9080  
  &#35; HTTPs is in a common way managed from a frontal Apache HTTPd server. If you really need to use it from Tomcat, you have to tune the SSL certificates and the protocols directly in the file "$installdir/motu/tomcat-motu/conf/server.xml".  
__tomcat-motu-port-https__=9443  
__tomcat-motu-port-ajp__=9009  
__tomcat-motu-port-shutdown__=9005  

#### <a name="ConfigurationSystemCASSSO">CAS SSO server</a>

   &#35;  true or false to enable the SSO connection to a CAS server  
__cas-activated__=false  
  
   &#35;  Cas server configuration to allow Motu to access it  
   &#35;  @see https://wiki.jasig.org/display/casc/configuring+the+jasig+cas+client+for+java+in+the+web.xml  
     
   &#35;  The  start of the CAS server URL, i.e. https://cas-cis.cls.fr/cas  
__cas-server-url__=https://cas-cis.cls.fr/cas   

   &#35;  The Motu HTTP server URL, for example: http://misgw-ddo-qt.cls.fr:9080 or http://motu.cls.fr   
   &#35;  If you use a frontal HTTPd server, you have to known if its URL will be called once the user will be login on CAS server.  
   &#35;  In this case, set the Apache HTTPd server. The value will be http://$apacheHTTPdServer/motu-web/Motu So, in Apache HTTPd, you have to redirect this URL to the Motu Web server  
__cas-auth-serverName__=http://$motuServerIp:$motuServerPort   

   &#35;  The proxy callback HTTPs URL of the Motu server ($motuServerIp is either the Motu host or the frontal Apache HTTPs host ip or name. $motuServerHttpsPort is optional if default HTTPs port 443 is used, otherwise it is the same value as defined above with the key "tomcat-motu-port-https", or it is the port defined for the HTTPs server on the frontal Apache HTTPd)  
__cas-validationFilter-proxyCallbackUrl__=https://$motuServerIp:$motuServerHttpsPort/motu-web/proxyCallback  
  
  
__IMPORTANT__: Motu uses a Java HTTPs client to communicate with the CAS server. When the CAS server has an untrusted SSL certificate, you have to add it to Java default certificates or to add the Java property named "javax.net.ssl.trustStore" to target a CA keystore which contains the CAS Server SSL CA public key.
For example, add this property by setting Java option [tomcat-motu-jvm-javaOpts](#ConfigurationSystem):  
```
tomcat-motu-jvm-javaOpts=-server -Xmx4096M -Xms512M -XX:PermSize=128M -XX:MaxPermSize=512M -Djavax.net.ssl.trustStore=/opt/cmems-cis/motu/config/security/cacerts-with-cas-qt-ca.jks
```

The following part is not relevant in the CMEMS context as the SSO CAS server has been signed by a known certification authority.  
If you need to run tests with your own SSO CAS server without any certificate signed by a known certification authority, you have to follow the following steps.  

How to build the file cacerts-with-cas-qt-ca.jks on Motu server?  

* Download the certificate file (for example "ca.crt") of the authority which has signed the CAS SSO certificate on the CAS server machine (/opt/atoll/ssl/ca.crt) and copy it to "${MOTU_HOME}/config/security/", then rename it "cas-qt-ca.crt"
* Copy the default Java cacerts "/opt/cmems-cis-validation/motu/products/jdk1.7.0_79/jre/lib/security/cacerts" file into ${MOTU_HOME}/config/security/
  and rename this file to "cacerts-with-cas-qt-ca.jks"  
  ```
  cp /opt/cmems-cis-validation/motu/products/jdk1.7.0_79/jre/lib/security/cacerts /opt/cmems-cis/motu/config/security/  
  mv /opt/cmems-cis/motu/config/security/cacerts /opt/cmems-cis/motu/config/security/cacerts-with-cas-qt-ca.jks  
  ```
* Then import "cas-qt-ca.crt" inside "cacerts-with-cas-qt-ca.jks", Trust the certificate=yes  
  ```
  /opt/cmems-cis-validation/motu/products/jdk1.7.0_79/bin/keytool -import -v -trustcacerts -alias $CAS_HOST_NAME -file cas-qt-ca.crt -keystore cacerts-with-cas-qt-ca.jks -keypass XXX  
  ```  

#### <a name="ConfigStandardNames">NetCdf standard names</a>  
When NetCdf variables are read in data files, either by Threads or directly by Motu, Motu wait for a standard name metadata sttribute to be found for each variable as requiered by the [CF convention](#http://cfconventions.org/Data/cf-standard-names/docs/guidelines.html).
Due to any production constraints, some netcdf files does not have any standard_name attribute.  
In the case, you can add directly in the configuration folder, a file named standardNames.xml in order to map a standard_name to a netcdf variable name.  
You can find an example in Motu source: /motu-web/src/main/resources/standardNames.xml  

#### Supervision
To enable the status supervision, set the parameter below:  
__tomcat-motu-urlrewrite-statusEnabledOnHosts__=localhost,*.cls.fr

This parameter is used to set the property below in the WEB.XML file:  
```
        <!-- Documentation from http://tuckey.org/urlrewrite/manual/3.0/
        you may want to allow more hosts to look at the status page
        statusEnabledOnHosts is a comma delimited list of hosts, * can
        be used as a wildcard (defaults to "localhost, local, 127.0.0.1") -->
        <init-param>  
            <param-name>statusEnabledOnHosts</param-name>  
            <param-value>${tomcat-motu-urlrewrite-statusEnabledOnHosts}</param-value>  
        </init-param>  
```  

For more detail read:  
org.tuckey UrlRewriteFilter FILTERS : see http://tuckey.org/urlrewrite/manual/3.0/  

  
## <a name="LogSettings">Log settings</a>  

Log are configured by using log4j2 in file config/log4j.xml  

### Motu queue server logs: motuQSlog.xml, motuQSlog.csv

This log files are used to compute statistics about Motu server usage.  
Two format are managed by this log, either XML or CSV.  
To configure it, edit config/log4j.xml  

##### Log format: XML or CSV  
Update the fileFormat attribute of the node "MotuCustomLayout": <MotuCustomLayout fileFormat="xml">
A string either "xml" or "csv" to select the format in which log message are written.  
Also update the log file name extension of the attributes "fileName" and "filePattern" in order to get a coherent content in relationship with value set for MotuCustomLayout file format.  
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
In the dissemination unit, Motu shares its log files with a central server.  
Log files have to be save on a public access folder.  
Set absolute path in "fileName" and "filePattern" attributes. This path shall be serve by the frontal Apache HTTPd or Apache Tomcat.
  
For example, if you want to share account transaction log files, you edit config/log4j.xml. 
Update content below:  
``` 
<RollingFile name="log-file-infos.queue" fileName="${sys:motu-log-dir}/motuQSlog.xml"
            filePattern="${sys:motu-log-dir}/motuQSlog.xml.%d{MM-yyyy}"
```   
 with:  
``` 
<RollingFile name="log-file-infos.queue" fileName="/opt/cmems-cis/motu/data/public/transaction/motuQSlog.xml"
            filePattern="/opt/cmems-cis/motu/data/public/transaction/motuQSlog.xml.%d{MM-yyyy}"
```   
Note that both attributes __fileName__ and __filePattern__ have been updated.  
Then the frontal [Apache HTTPd server](#InstallFrontal) has to serve this folder.



## <a name="ThemeStyle">Theme and Style</a>  
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





# <a name="Operation">Operation</a>    

## <a name="SS">Start, Stop and other Motu commands</a>    
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
At the shutdown of Motu, the server waits for none of the pending or in progress request to be in execution.  
If it's the case, the server waits the end of the request before shutdown.  
Note that after waiting 10 minutes, server will automatically shutdown without waiting any running requests.  
So command below can respond quickly if no requests are in the queue server or takes time to process them.  
``` 
./motu stop
``` 

If you needs to understand what Motu is waiting for, you can check the logbook:  
``` 
tail -f log/logbook.log  
Stop in progress...  
Stop: Pending=0; InProgress=2  
Stop: Pending=0; InProgress=2  
...  
Stop: Pending=0; InProgress=1  
Stop: Pending=0; InProgress=0  
...  
Stop done  
``` 

During the stop step, from a web browser, the user will be able to ends its download request if a front web server (Apache HTTPd) serves the statics files and the downloaded product.
In case where Motu is installed as a standalone web server, user will get a 500 HTTP error. For example in development or qualification environment, 
this could lead to block the download of the files if Motu is used to serve both static and requested product files.


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

## <a name="ExpMonitorPerf">Monitor performance</a> 

Once started, you can use the Linux command "top" to check performance:  

* __load average__ the three numbers shall be low and under the number of CPU (lscpu | grep Proc). For example if you have 4 processors this indicator can rise up to 4 but not above. If it is above, you have to add more CPU power.
* __%CpuX, parameter wa__ This indicator shall be near 0 to indicate that processes does not wait to access to the disks. When this number is above 0.5 you have to improve access disk performance.
* __KiB Mem__ Be sure that free memory is available. If it is less than 5000000, meaning less than 5GB, you have to add RAM memory in order to manage pic load. 

Example of top command:  

``` 
top - 11:07:01 up 19:46,  3 users,  ***load average: 0,05, 0,09, 0,25***  
Tasks: 395 total,   2 running, 393 sleeping,   0 stopped,   0 zombie  
%Cpu0  :  1,0 us,  1,0 sy,  0,0 ni, 98,1 id,   ***0,0 wa***,  0,0 hi,  0,0 si,  0,0 st  
%Cpu1  :  1,0 us,  0,0 sy,  0,0 ni, 99,0 id,  ***0,0 wa***,  0,0 hi,  0,0 si,  0,0 st  
KiB Mem : 10224968 total,  ***4034876 free***,  3334576 used,  2855516 buff/cache    
...
``` 

  
## <a name="Logbooks">Logbooks</a>    

Log messages are generated by Apache Log4j 2. The configuration file is "config/log4j.xml".  
By default, log files are created in the folder $MOTU_HOME/log. This folder contains Motu log messages.  
Tomcat log messages are generated in the tomcat-motu/logs folder.  

* __Motu log messages__
  * __logbook.log__: All Motu log messages including WARN and ERROR(without stacktrace) messages.
  * __warnings.log__: Only Motu log messages with a WARN level
  * <a name="LogbooksErrors">__errors.log__</a>: Only Motu log messages with an ERROR level. When this file is not empty, it means that at least an error has been generated by the Motu application.
  * <a name="LogbooksTransactions">__motuQSlog.xml__, __motuQSlog.csv__</a>: Either a "CSV" or "XML" format which logs all queue events.
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
         * inQueueTime: Timestamp with format "yyyy-MM-dd' 'HH:mm:ss.SSS" when the request has been put in the queue
         * startTime: Timestamp with format "yyyy-MM-dd' 'HH:mm:ss.SSS" when the request has been started to be processed
         * endTime: Timestamp with format "yyyy-MM-dd' 'HH:mm:ss.SSS" when the request has been ended to be processed
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

* __Tomcat log messages__: This folder contains all Apache Tomcat log files. The file below is important to check startup logs:  
  * __catalina.out__: Catalina output matching the environment variable CATALINA_OUT.
    

## <a name="AdminDataSetAdd">Add a dataset</a>    
In order to add a new Dataset, you have to add a new configService node in the [Motu business configuration](#ConfigurationBusiness).  
When Motu read data through TDS (Opendap or NCSS service) url, the data shall be configured in TDS before this configuration is saved in Motu. The [TDS configuration](https://www.unidata.ucar.edu/software/thredds/v4.6/tds/catalog/index.html) is not explained here.  

Within CMEMS, the datasets are organized in a tree structure, where the product granularity appears above the dataset granularity.  
To be noticed:  

* All gridded dataset shall be configured in TDS, to be served through the subsetter of Motu  
* A product is a coherent group of datasets. The product is the granularity used in the catalogue of CMEMS  
* In the XML tree structure of the TDS configuration, each product shall be configured through a unique node  
* This node shall correspond to one XML file in the TDS configuration (for example GLOBAL_ANALYSIS_PHYS_001_016.xml) and shall be further referenced in the motuConfiguration.xml file as one <catalog name> (for example <catalog name=" GLOBAL_ANALYSIS_PHYS_001_016.xml>)  
* The value of the "name" attribute of the element <dataset> shall be identical to the Product Name (from CMEMS Product Information Table). 
In the example below named “CMEMS DU xxx Thredds Catalog” there are three datasets. The following catalog tree presents a hierarchical organization for this catalog.
      
```       
<  CMEMS DU xxx Thredds Catalog >  
| ------ < GLOBAL_ANALYSIS_PHYS_001_016  >   
|------- < dataset-armor-3d-v5-myocean >  
|----------------- < GLOBAL_REP_PHYS_001_013  >   
|------- < dataset-armor-3d-rep-monthly-v3-1-myocean >                                                                     
|------- < dataset-armor-3d-rep-weekly-v3-1-myocean>   
``` 

The Motu configuration (motuConfiguration.xml) should reference the node corresponding to one XML file in the TDS configuration.

  
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
This protocol is used to access to local files. With this protocol user download the full data source file and can run only temporal extractions on the dataset (As a reminder, a dataset is temporal aggregation of several datasource files.    

```
<configService description="Free text to describe your dataSet" group="HR-Sample" profiles="internal, external, major" httpBaseRef="" name="HR_MOD-TDS" veloTemplatePrefix="">  
           <catalog name="catalogFILE_GLOBAL_ANALYSIS_PHYS_001_016.xml" type="file" urlSite="file:///opt/cmems-cis-data/data/public/inventories"/>  
</configService>  
```

An an example, the file __catalogFILE_GLOBAL_ANALYSIS_PHYS_001_016.xml__ contains:  

```
< ?xml version="1.0" encoding="UTF-8"?>  
<!DOCTYPE rdf:RDF [  
<!ENTITY atoll "http://purl.org/cls/atoll/ontology/individual/atoll#">  
]>  
<catalogOLA xmlns="http://purl.org/cls/atoll" name="catalog GLOBAL-ANALYSIS-PHYS-001-016">  
        <resourcesOLA>  
                <resourceOLA urn="dataset-armor-3d-v5-myocean" inventoryUrl="file:///opt/cmems-cis-data/data/public/inventories/dataset-armor-3d-v5-myocean-cls-toulouse-fr-armor-motu-rest-file.xml"/>  
        </resourcesOLA>  
</catalogOLA>    
```

File __dataset-armor-3d-v5-myocean-cls-toulouse-fr-armor-motu-rest-file.xml__:  

```
< ?xml version="1.0" encoding="UTF-8"?>  
<!DOCTYPE rdf:RDF [  
<!ENTITY atoll "http://purl.org/cls/atoll/ontology/individual/atoll#">  
<!ENTITY cf "http://purl.org/myocean/ontology/vocabulary/cf-standard-name#">  
<!ENTITY cu "http://purl.org/myocean/ontology/vocabulary/cf-unofficial-standard-name#">  
<!ENTITY ct "http://purl.org/myocean/ontology/vocabulary/forecasting#">  
<!ENTITY cp "http://purl.org/myocean/ontology/vocabulary/grid-projection#">  
]>  
<inventory lastModificationDate="2016-01-27T00:10:10+00:00" xmlns="http://purl.org/cls/atoll" updateFrequency="P1D">  
  <service urn="cls-toulouse-fr-armor-motu-rest-file"/>  
  <resource urn="dataset-armor-3d-v5-myocean">  
    <access urlPath="file:///data/atoll/armor/armor-3d-v3/"/>  
    <geospatialCoverage south="-82" north="90" west="0" east="359.75"/>  
    <depthCoverage min="0" max="5500" units="m"/>  
    <timePeriod start="2014-10-01T00:00:00+00:00" end="2016-01-26T23:59:59+00:00"/>  
    <theoricalTimePeriod start="2014-10-01T00:00:00+00:00" end="2016-01-26T23:59:59+00:00"/>  
    <variables>  
      <variable name="zvelocity" vocabularyName="http://mmisw.org/ont/cf/parameter/eastward_sea_water_velocity" units="m/s"/>  
      <variable name="height" vocabularyName="http://purl.org/myocean/ontology/vocabulary/cf-standard-name#height_above_geoid" units="m"/>  
      <variable name="mvelocity" vocabularyName="http://mmisw.org/ont/cf/parameter/northward_sea_water_velocity" units="m/s"/>  
      <variable name="salinity" vocabularyName="http://mmisw.org/ont/cf/parameter/sea_water_salinity" units="1e-3"/>  
      <variable name="temperature" vocabularyName="http://mmisw.org/ont/cf/parameter/sea_water_temperature" units="degC"/>  
    </variables>  
  </resource>  
  <files>  
    <file name="ARMOR3D_TSHUV_20141001.nc" weight="327424008" modelPrediction="http://www.myocean.eu.org/2009/resource/vocabulary/forecasting#" startCoverageDate="2014-10-01T00:00:00+00:00" endCoverageDate="2014-10-07T23:59:59+00:00" creationDate="2015-03-17T00:00:00+00:00" availabilitySIDate="2016-01-27T00:10:10+00:00" availabilityServiceDate="2016-01-27T00:10:10+00:00" theoreticalAvailabilityDate="2015-03-17T00:00:00+00:00"/>  
    <file name="ARMOR3D_TSHUV_20141008.nc" weight="327424008" modelPrediction="http://www.myocean.eu.org/2009/resource/vocabulary/forecasting#" startCoverageDate="2014-10-08T00:00:00+00:00" endCoverageDate="2014-10-14T23:59:59+00:00" creationDate="2015-03-17T00:00:00+00:00" availabilitySIDate="2016-01-27T00:10:10+00:00" availabilityServiceDate="2016-01-27T00:10:10+00:00" theoreticalAvailabilityDate="2015-03-17T00:00:00+00:00"/>  
    ...  
    <file name="ARMOR3D_TSHUV_20160120.nc" weight="327424008" modelPrediction="http://www.myocean.eu.org/2009/resource/vocabulary/forecasting#" startCoverageDate="2016-01-20T00:00:00+00:00" endCoverageDate="2016-01-26T23:59:59+00:00" creationDate="2016-01-26T11:11:00+00:00" availabilitySIDate="2016-01-27T00:10:10+00:00" availabilityServiceDate="2016-01-27T00:10:10+00:00" theoreticalAvailabilityDate="2016-01-26T11:11:00+00:00"/>  
  </files>  
</inventory>  
```  


## <a name="AdminMetadataCache">Tune the dataset metadata cache</a>  
In order to improve response time, Motu uses an in-memory cache which stores datasets metadata. This cache is indexed by config service.    
You can tune the cache behaviour in order to manage both real time and archived datasets effectively.      
At startup, Motu loads datasets metadata of each configService by turn. Once done, cache is refreshed either periodically in an automatic manner or either when asked by triggering a [specific action](#ClientAPI_RefreshCache).  
The cache is kept in memory and all Motu requests are based on it. When a cache refresh is asked, a second cache loads new metadata and when fully loaded, Motu main cache is replaced. So until the full loading, old cache is used in Motu responses.   

For config services which manages real time datasets, meaning datasets which are daily updated, you can set the following configuration in motuConfiguration.xml:
```  
<?xml version="1.0"?>
<motuConfig  ...
<configService ... refreshCacheAutomaticallyEnabled="true"
...
```  

For config services which manages archived datasets, meaning dataset which not updated frequently for example only once a week, you can set the following configuration in motuConfiguration.xml:
```  
<?xml version="1.0"?>
<motuConfig  ...
<configService ... refreshCacheAutomaticallyEnabled="false"
...
```  
In this case, when you want to refresh metadata cache of these datasets, you can use this dedicated [action](#ClientAPI_RefreshCache).


## <a name="ExploitDebug">Debug view</a>  
From a web browser access to the Motu web site with the URL:  
``` 
/Motu?action=debug  
``` 

You can see the different requests and their [status](#ClientAPI_Debug).  
You change the status order by entering 4 parameters in the URL:  
``` 
/Motu?action=debug&order=DONE,ERROR,PENDING,INPROGRESS
``` 


 
 
## <a name="ExploitCleanDisk">Clean files</a>  

## <a name="ExploitCleanDiskLogbook">Logbook files</a>   
Logbook files are written by Apache Tomcat server and Motu application.  
 
### <a name="ExploitCleanDiskLogbookTomcat">Apache Tomcat Logbook files</a>  
Tomcat writes log files in folder tomcat-motu/logs.  
You can customize this default configuration by editing tomcat-motu/conf/logging.properties  
This file is the default file provided by Apache Tomcat.
There is a daily rotation so you can clean those files to fullfill the harddrive.   
crontab -e   
0 * * * * find /opt/cmems-cis/motu/tomcat-motu/logs/*.log* -type f -mmin +14400 -delete >/dev/null 2>&1   
0 * * * * find /opt/cmems-cis/motu/tomcat-motu/logs/*.txt* -type f -mmin +14400 -delete >/dev/null 2>&1   

### <a name="ExploitCleanDiskLogbookMotu">Motu Logbook files</a>  
Logbook files are written in the folder(s) configured in the log4j.xml configuration file.  
All logs are generated daily except for motuQSLog (xml or csv) which are generated monthly.  
You can clean those files to avoid to fullfill the harddrive.   
crontab -e   
0 * * * * find /opt/cmems-cis/motu/log/*.log* -type f -mmin +14400 -delete >/dev/null 2>&1  
0 * * * * find /opt/cmems-cis/motu/log/*.out* -type f -mmin +14400 -delete >/dev/null 2>&1  
0 * * * * find /opt/cmems-cis/motu/log/*.xml* -type f -mmin +144000 -delete >/dev/null 2>&1  
0 * * * * find /opt/cmems-cis/motu/log/*.csv* -type f -mmin +144000 -delete >/dev/null 2>&1  
  
Note that Motu is often tuned to write the motuQSLog in a dedicated folder. So you have to clean log files in this folder too. For example:  
0 * * * * find /opt/cmems-cis/motu/data/public/transaction/*.xml* -type f -mmin +144000 -delete >/dev/null 2>&1  
0 * * * * find /opt/cmems-cis/motu/data/public/transaction/*.csv* -type f -mmin +144000 -delete >/dev/null 2>&1 

## <a name="LogCodeErrors">Log Errors</a>   

### The code pattern
The error codes of Motu as the following format "XXXX-Y":
  
* [XXXX](#LogCodeErrorsActionCode) code matching the action which is executed when the error is raised. This part is the "ActionCode".  The action is in general a HTTP request and matches the following HTTP parameter http://$server/motu-web/Motu?action=.
* [Y](#LogCodeErrorsErrorType) code which identifies the part of the program from which the error was raised. This part is the "ErrorType".
  
  
For example, the web browser can display:  
011-1 : A system error happened. Please contact the administrator of the site. 

Here, we have the error code in order to understand better what happens. But the end user has a generic message and no detail is given to him. These end user messages are described in the file "/motu-web/src/main/resources/MessagesError.properties". The file provided with the project is a default one and can be customized for specific purposes. Just put this file in the "config" folder, edit it and restart Motu to take it into account. So when a user has an error, it just have to tell you the error code and you can check the two numbers with the descriptions below.  


### <a name="LogCodeErrorsActionCode">Action codes</a>  

The Action Code        =>    A number matching the HTTP request with the action parameter.

001        =>    UNDETERMINED\_ACTION           
002        =>    PING\_ACTION                   
003        =>    DEBUG\_ACTION                  
004        =>    GET\_REQUEST\_STATUS\_ACTION     
005        =>    GET\_SIZE\_ACTION               
006        =>    DESCRIBE\_PRODUCT\_ACTION       
007        =>    TIME\_COVERAGE\_ACTION          
008        =>    LOGOUT\_ACTION                 
010        =>    DOWNLOAD\_PRODUCT\_ACTION       
011        =>    LIST\_CATALOG\_ACTION           
012        =>    PRODUCT\_METADATA\_ACTION       
013        =>    PRODUCT\_DOWNLOAD\_HOME\_ACTION  
014        =>    LIST\_SERVICES\_ACTION              
015        =>    DESCRIBE\_COVERAGE\_ACTION         
016        =>    ABOUT\_ACTION  
017        =>    WELCOME\_ACTION  

### <a name="LogCodeErrorsErrorType">Error types</a>  

The Error Type Code    =>    A number defining a specific error on the server.

0         =>    No error.  
1         =>    There is a system error. Please contact the Administrator.    
2         =>    There is an error with the parameters. There are inconsistent.         
3         =>    The date provided into the parameters is invalid.         
4         =>    The latitude provided into the parameters is invalid.  
5         =>    The longitude provided into the parameters is invalid.         
6         =>    The range defined by the provided dates is invalid.         
7         =>    The memory capacity of the motu server is exceeded.         
8         =>    The range defined by the provided latitude/longitude parameters is invalid.         
9         =>    The range defined by the provided depth parameters is invalid.         
10        =>    The functionality is not yet implemented.         
11        =>    There is an error with the provided NetCDF variables.         
12        =>    There is not variables into the variable parameter.         
13        =>    NetCDF parameter error. Example: Invalid date range, invalid depth range, ...         
14        =>    There is an error with the provided NetCDF variable. Have a look at the log file to have more information.         
15        =>    The number of maximum request in the queue server pool is reached. it's necessary to wait that some requests are finished.         
16        =>    The number of maximum request for the user is reached. It's necessary to wait that some requests are finished for the user.         
18        =>    The priority of the request is invalid in the queue server manager. Have a look at the log file to have more information.         
19        =>    The id of the request is not know by the server. Have a look at the log file to have more information.         
20        =>    The size of the request is greater than the maximum data managed by the available queue. It's impossible to select a queue for this request. It's necessary to narrow the request.         
21        =>    The application is shutting down. it's necessary to wait a while before the application is again available.         
22        =>    There is a problem with the loading of the motu configuration file. Have a look at the log file to have more information.         
23        =>    There is a problem with the loading of the catalog configuration file. Have a look at the log file to have more information.         
24        =>    There is a problem with the loading of the error message configuration file. Have a look at the log file to have more information.         
25        =>    There is a problem with the loading of the netcdf file. Have a look at the log file to have more information.         
26        =>    There is a problem with the provided parameters. Have a look at the log file to have more information.         
27        =>    There is a problem with the NetCDF generation engine. Have a look at the log file to have more information.         
28        =>    The required action is unknown. Have a look at the log file to have more information.
29        =>    The product is unknown.
30        =>    The service is unknown.
31        =>    The request cut the ante meridian. In this case, it's not possible to request more than one depth. It's necessary to change the depth selection and to select in the "from" and the "to" the values that have the same index into the depth list.
32        =>  	Due to a known bug in Thredds Data Server, a request cannot be satisfied wit netCDF4. User has to request a netCDF3 output file.
101		  =>	WCS specific error code : A WCS mandatory parameter is missing
102		  =>	WCS specific error code : A WCS parameter doesn't match the mandatory format
103		  =>	WCS specific error code : The WCS version parameter is not compatible with the Motu WCS server
104		  =>	WCS specific error code : A system error append.
105		  =>	WCS specific error code : The coverage ident doesn't exist
106		  =>	WCS specific error code : The list of coverage id is empty
107		  =>	WCS specific error code : The provided parameter used to define a subset is invalid
108		  =>	WCS specific error code : The provided axis label doesn't match any available label

  
# <a name="ClientsAPI">Motu clients & REST API</a>  

You can connect to Motu by using a web browser or a client.

## <a name="ClientPython">Python client</a>   
Motu offers an easy to use Python client. Very useful in machine to machine context, it enables to download data by running a python script.   
Project and all its documentation is available at [https://github.com/clstoulouse/motu-client-python](https://github.com/clstoulouse/motu-client-python).
  
## <a name="OGC_WCS_API">OGC WCS API</a>  
Motu offers a Web Service interface which implements the OGC WCS standard described, in particular, by the two following documents on the OGC web site:  

* [09-110r4_WCS_Core_2.0.1.pdf](https://portal.opengeospatial.org/files/09-110r4)
* [09-147_WCS_2.0_Extension_--_KVP_Protocol.pdf](http://portal.opengeospatial.org/files/?artifact_id=36263&format=pdf)

Available Web Services are:  

* [Get capabilities](#GetCapabilities)
* [Describe coverage](#DescribeCoverage)
* [Get coverage](#GetCoverage)

Parameters can be added to each request and they are described with their cardinality [x,y].  

* [0,1] is an optional parameter.   
* [1] is a mandatory parameter.  
* [0,n] is an optional parameter which can be set several times.  
* [1,n] is a mandatory parameter which can be set several times. 

### <a name="GetCapabilities">WCS: Get Capabilities</a>

The GetCapabilities request retrieves all available products defined on Motu server.


__URL__: http://localhost:8080/motu-web/wcs?service=WCS&version=2.0.1&request=GetCapabilities

__Parameters__:  

* __service [1]:__ Value is fixed to "WCS"
* __version [1]:__ Value is fixed to "2.0.1"
* __request [1]:__ Value is fixed to "GetCapabilties"

__Return__: 
A XML document as shown below:

<pre>
  <code>
&lt;?xml version="1.0" encoding="UTF-8" standalone="yes"?&gt;
&lt;ns3:Capabilities version="2.0.1" xmlns:ns6="http://www.opengis.net/swe/2.0" xmlns:ns5="http://www.opengis.net/gmlcov/1.0" xmlns:ns2="http://www.w3.org/1999/xlink" xmlns:ns1="http://www.opengis.net/ows/2.0" xmlns:ns4="http://www.opengis.net/gml/3.2" xmlns:ns3="http://www.opengis.net/wcs/2.0"&gt;
    &lt;ns1:ServiceIdentification&gt;
        &lt;ns1:Title&gt;Motu&lt;/ns1:Title&gt;
        &lt;ns1:Abstract&gt;Motu WCS service&lt;/ns1:Abstract&gt;
        &lt;ns1:ServiceType&gt;OGC WCS&lt;/ns1:ServiceType&gt;
        &lt;ns1:ServiceTypeVersion&gt;2.0.1&lt;/ns1:ServiceTypeVersion&gt;
        &lt;ns1:Profile&gt;http://www.opengis.net/spec/WCS/2.0/conf/core&lt;/ns1:Profile&gt;
        &lt;ns1:Profile&gt;http://www.opengis.net/spec/WCS_protocol-binding_get-kvp/1.0/conf/get-kvp&lt;/ns1:Profile&gt;
    &lt;/ns1:ServiceIdentification&gt;
    &lt;ns1:OperationsMetadata&gt;
        &lt;ns1:Operation name="GetCapabilities"&gt;
            &lt;ns1:DCP&gt;
                &lt;ns1:HTTP&gt;
                    &lt;ns1:Get ns2:href="http://localhost:8080/motu-web/wcs"/&gt;
                &lt;/ns1:HTTP&gt;
            &lt;/ns1:DCP&gt;
        &lt;/ns1:Operation&gt;
        &lt;ns1:Operation name="DescribeCoverage"&gt;
            &lt;ns1:DCP&gt;
                &lt;ns1:HTTP&gt;
                    &lt;ns1:Get ns2:href="http://localhost:8080/motu-web/wcs"/&gt;
                &lt;/ns1:HTTP&gt;
            &lt;/ns1:DCP&gt;
        &lt;/ns1:Operation&gt;
        &lt;ns1:Operation name="GetCoverage"&gt;
            &lt;ns1:DCP&gt;
                &lt;ns1:HTTP&gt;
                    &lt;ns1:Get ns2:href="http://localhost:8080/motu-web/wcs"/&gt;
                &lt;/ns1:HTTP&gt;
            &lt;/ns1:DCP&gt;
        &lt;/ns1:Operation&gt;
    &lt;/ns1:OperationsMetadata&gt;
    &lt;ns3:ServiceMetadata&gt;
        &lt;ns3:formatSupported&gt;application/netcdf&lt;/ns3:formatSupported&gt;
    &lt;/ns3:ServiceMetadata&gt;
    &lt;ns3:Contents&gt;
        &lt;ns3:CoverageSummary&gt;
            &lt;ns3:CoverageId&gt;HR_MOD_NCSS-TDS@HR_MOD&lt;/ns3:CoverageId&gt;
            &lt;ns3:CoverageSubtype&gt;ns3:GridCoverage&lt;/ns3:CoverageSubtype&gt;
        &lt;/ns3:CoverageSummary&gt;

            ...

		&lt;/ns3:Contents&gt;
&lt;/ns3:Capabilities&gt;
  </code>
</pre>



### <a name="DescribeCoverage">WCS: Describe Coverage</a>

The DescribeCoverage request retrieves the parameters description and the list of available  variables.
For the parameters description, low and high values are provided.

__URL__: http://localhost:8080/motu-web/wcs?service=WCS&version=2.0.1&request=DescribeCoverage&coverageId=$coverageId

__Parameters__:  

* __service [1]:__ Value is fixed to "WCS"
* __version [1]:__ Value is fixed to "2.0.1"
* __request [1]:__ Value is fixed to "DescribeCoverage"
* __coverageId [1]:__ list of identifiers of the required coverages. Each coverage identifiers are separated by a comma (,). CoverageId are returned by the [GetCapabilities](#GetCapabilities) service.

__Return__: 
A XML document as shown below:

<pre>
  <code>
  &lt;?xml version="1.0" encoding="UTF-8" standalone="yes"?&gt;
&lt;ns4:CoverageDescriptions xmlns:ns6="http://www.opengis.net/ows/2.0"
	xmlns:ns5="http://www.opengis.net/swe/2.0" xmlns:ns2="http://www.w3.org/1999/xlink"
	xmlns:ns1="http://www.opengis.net/gml/3.2" xmlns:ns4="http://www.opengis.net/wcs/2.0"
	xmlns:ns3="http://www.opengis.net/gmlcov/1.0"&gt;
	&lt;ns4:CoverageDescription ns1:id="$covergaeId"&gt;
		&lt;ns1:boundedBy&gt;
			&lt;ns1:Envelope
				uomLabels="latitude longitude depth date in seconds since 1970, 1 jan"
				axisLabels="Lat Lon Height Time"&gt;
				&lt;ns1:lowerCorner&gt;-80.0 -180.0 0.0 1.3565232E9&lt;/ns1:lowerCorner&gt;
				&lt;ns1:upperCorner&gt;90.0 180.0 5728.0 1.4657328E9&lt;/ns1:upperCorner&gt;
			&lt;/ns1:Envelope&gt;
		&lt;/ns1:boundedBy&gt;
		&lt;ns4:CoverageId&gt;$covergaeId&lt;/ns4:CoverageId&gt;
		&lt;ns1:domainSet&gt;
			&lt;ns1:Grid dimension="4"
				uomLabels="latitude longitude depth date in seconds since 1970, 1 jan"
				axisLabels="Lat Lon Height Time" ns1:id="Grid000"&gt;
				&lt;ns1:limits&gt;
					&lt;ns1:GridEnvelope&gt;
						&lt;ns1:low&gt;-80 -180 0 1356523200&lt;/ns1:low&gt;
						&lt;ns1:high&gt;90 180 5728 1465732800&lt;/ns1:high&gt;
					&lt;/ns1:GridEnvelope&gt;
				&lt;/ns1:limits&gt;
			&lt;/ns1:Grid&gt;
		&lt;/ns1:domainSet&gt;
		&lt;ns3:rangeType&gt;
			&lt;ns5:DataRecord&gt;
				&lt;ns5:field name="uice"&gt;
					&lt;ns5:Quantity&gt;
						&lt;ns5:uom code="m s-1" /&gt;
					&lt;/ns5:Quantity&gt;
				&lt;/ns5:field&gt;
				&lt;ns5:field name="salinity"&gt;
					&lt;ns5:Quantity&gt;
						&lt;ns5:uom code="1e-3" /&gt;
					&lt;/ns5:Quantity&gt;
				&lt;/ns5:field&gt;
				&lt;ns5:field name="vice"&gt;
					&lt;ns5:Quantity&gt;
						&lt;ns5:uom code="m s-1" /&gt;
					&lt;/ns5:Quantity&gt;
				&lt;/ns5:field&gt;
				&lt;ns5:field name="hice"&gt;
					&lt;ns5:Quantity&gt;
						&lt;ns5:uom code="m" /&gt;
					&lt;/ns5:Quantity&gt;
				&lt;/ns5:field&gt;
				&lt;ns5:field name="u"&gt;
					&lt;ns5:Quantity&gt;
						&lt;ns5:uom code="m s-1" /&gt;
					&lt;/ns5:Quantity&gt;
				&lt;/ns5:field&gt;
				&lt;ns5:field name="v"&gt;
					&lt;ns5:Quantity&gt;
						&lt;ns5:uom code="m s-1" /&gt;
					&lt;/ns5:Quantity&gt;
				&lt;/ns5:field&gt;
				&lt;ns5:field name="temperature"&gt;
					&lt;ns5:Quantity&gt;
						&lt;ns5:uom code="K" /&gt;
					&lt;/ns5:Quantity&gt;
				&lt;/ns5:field&gt;
				&lt;ns5:field name="ssh"&gt;
					&lt;ns5:Quantity&gt;
						&lt;ns5:uom code="m" /&gt;
					&lt;/ns5:Quantity&gt;
				&lt;/ns5:field&gt;
				&lt;ns5:field name="fice"&gt;
					&lt;ns5:Quantity&gt;
						&lt;ns5:uom code="1" /&gt;
					&lt;/ns5:Quantity&gt;
				&lt;/ns5:field&gt;
			&lt;/ns5:DataRecord&gt;
		&lt;/ns3:rangeType&gt;
	&lt;/ns4:CoverageDescription&gt;
&lt;/ns4:CoverageDescriptions&gt;
  </code>
</pre>

  
### <a name="GetCoverage">WCS: Get Coverage</a>

The GetCoverage request is used to run an extraction on a dataset using some filtering parameters and a list of required variables.

__URL__:  

* __DGF__: http://localhost:8080/motu-web/wcs?service=WCS&version=2.0.1&request=GetCoverage&coverageId=$coverageId&subset=Time(1412157600,1412244000)
* __Subetter__: http://localhost:8080/motu-web/wcs?service=WCS&version=2.0.1&request=GetCoverage&coverageId=$coverageId&subset=Time(1412157600,1412244000)&subset=Lat(50,70)&subset=Lon(0,10)&subset=Height(0,5728)&rangeSubset=temperature,salinity


__Parameters__:   

* __service [1]:__ Value is fixed to "WCS"
* __version [1]:__ Value is fixed to "2.0.1"
* __request [1]:__ Value is fixed to "GetCoverage"
* __coverageId [1]:__ the identifier of the required coverage. CoverageId are returned by the [GetCapabilities](#GetCapabilities) service.
* __subset [1,n]:__ the list of filtering parameters.  
	* To define one filtering parameter, the following format have to be respected:<br/>
	For the Time parameter:
	```
	SUBSET=Time(lowTimeValue,highTimeValue)
	```
	Unit is epoch since 1st January 1970, in UTC. E.g. Thu Dec 01 2016 00:00:00 is set to 1480550400000.  
	* To define multiple filtering parameters, the following format have to be respected:<br/>
	For the Latitude and the Longitue:
	```
	SUBSET=Lat(lowLatValue,highLatValue)&SUBSET=Lon(lowLonValue,highLonValue)
	```  
	In order to know which subset filters can be applied, you have to run a [DescribeCoverage](#DescribeCoverage) request.
* __rangesubset:__ the list of required variables for the coverage. Each variable have to be separated by a comma (,)

__Return__: 
A Netcdf file. When you request for one point, a specific algorithm is used, see [Downloading 1 point](#ArchiAlgoDownloading1Point).


## <a name="ClientRESTAPI">MOTU REST API</a>   
__MOTU REST API__ defines a set of services accessible from an HTTP URLs.  
All URLs have always the same pattern: http://motuServer/${context}/Motu?action=$actionName  
Other parameters can be added and they are described with their cardinality [x,y].  

* [0,1] is an optional parameter.   
* [1] is a mandatory parameter.  
* [0,n] is an optional parameter which can be set several times.  
* [1,n] is a mandatory parameter which can be set several times.  

__$actionName is an action, they are all listed below:__  
  
* XML API
   * [Describe coverage](#ClientAPI_DescribeCoverage)  
   * [Describe product](#ClientAPI_DescribeProduct)  
   * [Request status](#ClientAPI_RequestStatus)  
   * [Get size](#ClientAPI_GetSize)  
   * [Time coverage](#ClientAPI_GetTimeCov)  
   * [Download product](#ClientAPI_DownloadProduct)  
* HTML Web pages
   * [About](#ClientAPI_About)  
   * [Debug](#ClientAPI_Debug)  
   * [Download product](#ClientAPI_DownloadProduct)  
   * [List catalog](#ClientAPI_ListCatalog)  
   * [List services](#ClientAPI_ListServices)  
   * [Product download home](#ClientAPI_ProductDownloadHome)  
   * [Product medatata](#ClientAPI_ProductMetadata)    
   * [Welcome](#ClientAPI_welcome)  
* Plain Text 
   * [Ping](#ClientAPI_Ping)  
   * [Refresh config services metadata cache](#ClientAPI_RefreshCache) 
* JSON
   * [Supervision](#ClientAPI_supervision)  


 
### <a name="ClientAPI_About">About</a>    
Display version of the archives installed on Motu server  
__URL__: http://localhost:8080/motu-web/Motu?action=about  

__Parameters__: No parameter.  

__Return__: An HTML page. Motu-static-files (Graphic chart) is refreshed thanks to Ajax because its version file can be installed on a distinct server.   
Example:  
```
Motu-products: 3.0  
Motu-distribution: 2.6.00-SNAPSHOT  
Motu-configuration: 2.6.00-SNAPSHOT-20160623173246403  
Motu-static-files (Graphic chart): 3.0.00-RC1-20160914162955422  
```


### <a name="ClientAPI_Debug">Debug</a>    
Display all requests status managed by Motu server in the last [cleanRequestInterval](#BScleanRequestInterval) minutes.
Tables are sorted by time ascending.  
4 status are defined:

* __DONE__: Request has been processed successfully. Result file can be downloaded.  
* __ERROR__: Request has not been processed successfully. No result file is available.  
* __PENDING__: Request has been received by the server. Server computes its size and runs some checks, it can take a while.  
* __INPROGRESS__: Request has been delegated to the queue server which is currently processing it.  


__URL__: http://localhost:8080/motu-web/Motu?action=debug  

__Parameters__:  
* __order__ [0,1]: Change the order of items INPROGRESS,PENDING,ERROR,DONE. All items shall be set.  
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
Display the product meaning dataset description. Result contains notably: the datasetid, the time coverage, the geospatial coverage, the variable(s) (with the standard_name and unit), eventually the vertical coverage.  
There is 2 ways to call describe product, both returning a same response.  


#### Way 1   
  
__URL__: http://localhost:8080/motu-web/Motu?action=describeproduct&service=HR_MOD-TDS&product=HR_MOD  

__Parameters__:  

* __service__ [1]: The [service name](#BSconfigServiceName)  
* __product__ [1]: The product id  
  
  
#### Way 2  (Deprecated) 

__URL__: http://localhost:8080/motu-web/Motu?action=describeproduct&data=http://$tdsServer/thredds/dodsC/path_HR_MOD&xmlfile=http://$tdsServer/thredds/m_HR_MOD.xml  

__Parameters__:  

* __xmlfile__ [1]: The Thredds dataset, example: http://$tdsServer/thredds/m_HR_MOD.xml  
* __data__ [1]: The Thredds data, example http://$tdsServer/thredds/dodsC/path_HR_MOD  
    
__Return__: An XML document  

```
<productMetadataInfo code="OK" msg="OK" lastUpdate="Not Available" title="HR_MOD" id="HR_MOD">  
<timeCoverage code="OK" msg="OK"/>  
<availableTimes code="OK" msg="OK">  
1993-01-15T12:00:00ZP2D;2001-01-01T00:00:00ZPT12H;2012-03-01T00:00:00ZPT6H
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


#### availableTimes XML tag 
In the XML result file the tag "availableTimes" provides the list of date where data are available for the requested product.
The format of the date follows the convention ISO_8601 used to represent the dates and times. (https://en.wikipedia.org/wiki/ISO_8601)
Foreach available time period, the period definition format is "StartDatePeriod/EndDatePeriod/DurationBetweenEachAvailableData".
The "availableTimes" contains a list of time period separated by a ",".
* __StartDate__ : this the first date of the period where data are available.
* __EndDate__ : this the last date of the period where data are available.
* __DurationBetweenEachAvailableData__ : This the period duration between each available data in the interval defined by the the "StartDate" and "EndDate" date.

##### StartDate and EndDate format
The format of the StartDate and EndDate is YYYY-MM-DDThh:mm:ssZ where:
* __YYYY__ : is the year defined on 4 digits
* __MM__ : is the number of the month defined on 2 digits
* __DD__: is the number of the day in the month on 2 digits
* __hh__: is the hour of the day on 2 digits
* __mm__: is the minutes of the hour on 2 digits
* __ss__: is the seconds of the minutes on 2 digits

Examples:
* 1993-01-15T12:00:00Z
* 2016-07-25T06:35:45Z
* 2017-08-31T15:05:08Z

##### DurationBetweenEachAvailableData
The formation of the duration is P*nbyers*Y*nbmonths*M*nbdays*DT*nbhours*H*nbminutes*M*nbseconds*S.*nbmillisec* where:
* __nbyears__ : is the number of years. The ISO_8601 is ambiguous on the number of days in the year. For the Motu project, the number of days is fixed to 365 as in the most of projects.
* __nbmonths__ : is the number of month. The ISO_8601 is ambiguous on the number of days in the month. For the Motu project, the number of days is fixed to 30 as in the most of projects.
* __nbdays__ : is the number of day. One day is 24 hours.
* __nbhours__ : is the number of hours. One hour is 60 minutes.
* __nbseconds__: is the number of seconds. One seconds is 1000 milliseconds.
* __nbmillisec__ : is the number of milliseconds.

By convention, P1M defines a duration of 1 month and PT1M defines a duration of 1 minutes.

Examples:
* each minutes => PT1M
* each hours => PT1H
* each 12 hours => PT12H
* echo days => P1D
* each 15 days => P15D
* each 1 months => P1M
 
### <a name="ClientAPI_DownloadProduct">Download product</a>    
Request used to download a product  

__URL__: http://localhost:8080/motu-web/Motu?action=productdownload  
example:  
http://localhost:8080/motu-web/Motu?action=productdownload&service=HR_MOD-TDS&product=HR_MOD&x_lo=-2&x_hi=2&y_lo=-2&y_hi=2&output=netcdf&t_lo=2016-06-12+12%3A00%3A00&t_hi=2016-06-12+12%3A00%3A00&z_lo=0.49&z_hi=5727.92  

__Parameters__:  

* __service__ [1]: The [service name](#BSconfigServiceName)  
* __product__ [1]: The product id  
* __variable__ [0,n]: physical variables to be extracted from the product. When no variable is set, all the variables of the dataset are extracted.  
* __y_lo__ [0,1]: low latitude of a geographic extraction. Default value is -90.  
* __y_hi__ [0,1]: high latitude of a geographic extraction. Default value is 90.  
* __x_lo__ [0,1]: low longitude of a geographic extraction. Default value is -180.  
* __x_hi__ [0,1]: high longitude of a geographic extraction. Default value is 180.  
* __z_lo__ [0,1]: low vertical depth . Default value is 0.  
* __z_hi__ [0,1]: high vertical depth. Default value is 180.  
* __t_lo__ [0,1]: Start date of a temporal extraction. If not set, the default value is the first date/time available for the dataset. Format is  "yyy-MM-dd" or "yyyy-MM-dd HH:mm:ss" or "yyyy-MM-ddTHH:mm:ss" and depends on the requested dataset.  
* __t_hi__ [0,1]: End date of a temporal extraction. If not set, the default value is the last date/time available for the dataset. Format is "yyy-MM-dd" or "yyyy-MM-dd HH:mm:ss" or "yyyy-MM-ddTHH:mm:ss" and depends on the requested dataset.    
* __output__ [0,1]: netcdf. Due to a TDS issue, only netcdf is available. netcdf4 will be available as soon as TDS will have resolved its issue.
* __mode__ [0,1]: Specify the desired result mode. Enumeration value from [url, console, status] represented as a string. If no mode, "url" value is the default mode.  

   * mode=__url__: URL of the delivery file is directly returned in the HTTP response as an HTML web page. Then Javascript read this URL to download file. The request is processed in a synchronous mode.  
   * mode=__console__: the response is a 302 HTTP redirection to the delivery file to be returned as a binary stream. The request is processed in a synchronous mode.  
   * mode=__status__: request is submitted and [the status](#ClientAPI_RequestStatus) of the request processing is immediately returned as an XML. The request is processed in an asynchronous mode.  
   Web Portal submits the request to the Dissemination Unit Subsetter and gets an immediate response of the Subsetter. 
   This response contains the identifier and the status of the order (pending, in progress, done, error).
   So long as the order is not completed (done or error), Web Portal requests the status of the order at regular and fair intervals (> 5 seconds) 
   and gets an immediate response. When the status is “done”, Web Portal retrieves the url of the file to download, from the status response. 
   Then Web Portal redirects response to this url. 
   The Web Browser opens a binary stream of the file to download and shows a dialog box to allow the user saving it as a local file.  

__Return__: Several ways depending of the selected http parameter mode. When you request for one point, a specific algorithm is used, see [Downloading 1 point](#ArchiAlgoDownloading1Point).  



 
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
Get the size of a download request. Result contains the size of the potential result file, with a unit, and the maximum allowed size for this service.  

__URL__: http://localhost:8080/motu-web/Motu?action=getsize  

__Parameters__:  

Parameters below are exactly the same as for [Download product](#ClientAPI_DownloadProduct)   

* __service__ [1]: The [service name](#BSconfigServiceName)  
* __product__ [1]: The product id  
* __variable__ [0,n]: physical variables to be extracted from the product. When no variable is set, all the variables of the dataset are extracted.  
* __y_lo__ [0,1]: low latitude of a geographic extraction. Default value is -90.  
* __y_hi__ [0,1]: high latitude of a geographic extraction. Default value is 90.  
* __x_lo__ [0,1]: low longitude of a geographic extraction. Default value is -180.  
* __x_hi__ [0,1]: high longitude of a geographic extraction. Default value is 180.  
* __z_lo__ [0,1]: low vertical depth . Default value is 0.  
* __z_hi__ [0,1]: high vertical depth. Default value is 180.  
* __t_lo__ [0,1]: Start date of a temporal extraction. If not set, the default value is the first date/time available for the dataset. Format is yyy-mm-dd or yyyy-dd h:m:s or yyyy-ddTh:m:s.  
* __t_hi__ [0,1]: End date of a temporal extraction. If not set, the default value is the last date/time available for the dataset. Format is yyy-mm-dd or yyyy-dd h:m:s or yyyy-ddTh:m:s.  
  
__Return__: An XML document.    
The unit is "KB" means Kilobyte.
Validated by the schema /motu-api-message/src/main/schema/XmlMessageModel.xsd#RequestSize  
Example:  

```
<requestSize code="005-0" msg="OK" unit="kb" size="1.5104933E8" maxAllowedSize="9.961472E8"/>  
```  

### <a name="ClientAPI_ListCatalog">List catalog</a>    
Display information about a catalog (last update timestamp) and display link to access to download page and dataset metadata.

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
Used to be sure that server is up. You can also use the [supervision](#ClientAPI_supervision) URL.  

__URL__: http://localhost:8080/motu-web/Motu?action=ping  

__Parameters__: No parameter.  

__Return__: An plain text  

```  
OK - response action=ping    
```     

### <a name="ClientAPI_RefreshCache">Refresh config services metadata cache</a>    
Force the refresh of the cache of [config service](#BSconfigService) metadata instead of waiting the [automatic refresh](#describeproductcacherefreshinmillisec).   
This action is secured and is only triggered when a valid [token](#refreshCacheToken) is given.    
Moreover a list of config services needed to be refreshed is shared with the automatic update process.     
This add robustness because a job refreshes only cache of the config services which are is the list. So when this action is called several times, if a config service in already in this waiting list, it is not added a second time.
A soon as a cache for a config service is refreshed, config service is removed from this waiting list.   

__URL__: http://localhost:8080/motu-web/Motu?action=refreshcache&token=tokenValid&configServiceNames=all  

__Parameters__:

* __token__ [1] : Used to secure this action. The [token](#refreshCacheToken) configured in the motuConfiguration.xml file which allowed the execution of the refresh. See this section for [the token configured](#refreshCacheToken)
* __configServiceNames__ [1] : [all,onlyauto,$configServiceNames] 3 options to tune how the cache will be resfreshed.  
   * __all__ : Refresh immediately all the config service. 
   * __onlyauto__ : Refresh immediately only the config services which enable the [automatic refresh](#refreshCacheAutomaticallyEnabled).
   * __$configServiceNames__ : Refresh immediately all the config services listed. Value of this parameter is a list of all [config service](#BSconfigServiceName) name is separated by a comma character, e.g. configServiceNames=AAA,BBB,CCC

__Return__: A plain text which specify if the refresh is launched or if an error occurred, e.g. "OK: config service AAA cache refresh in progress" or "ERROR: Unknwon config service UnknownConfigService"

```  
OK cache refresh in progress   
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

__URL__: http://localhost:8080/motu-web/Motu?action=gettimecov&service=HR_MOD-TDS&product=HR_MOD  

__Parameters__:  

* __service__ [1]: The [service name](#BSconfigServiceName)  
* __product__ [1]: The product id  
  
__Return__: A XML document  

```  
<timeCoverage code="007-0" msg="OK" end="2016-09-17T00:00:00.000Z" start="2007-05-13T00:00:00.000Z"/>
```  

### <a name="ClientAPI_supervision">Supervision</a>  
Gives information about Motu server.  
For more details, see [https://jolokia.org/reference/html/agents.html].

__URL__: http://localhost:8080/motu-web/supervision

__Parameters__: No parameter
  
__Return__: A JSON document  

```  
{"timestamp":1474638852,"status":200,"request":{"type":"version"},"value":{"protocol":"7.2","config":{"agentId":"10.1.20.198-18043-2df3a4-servlet","agentType":"servlet"},"agent":"1.3.3","info":{"product":"tomcat","vendor":"Apache","version":"7.0.69"}}}
```  



### <a name="ClientAPI_welcome">Welcome</a>  
HTML page which gives access to several web pages, in particular the Motu listservices web page.

__URL__:  
* http://localhost:8080/motu-web/  
* http://localhost:8080/motu-web/Motu?action=welcome

__Parameters__: No parameter
  
__Return__: An HTML web page  

