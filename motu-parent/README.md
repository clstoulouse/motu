# Motu Project
>@author Project manager <rdedianous@cls.fr>
>@author Product owner <tjolibois@cls.fr>
>@author Scrum master, software architect <smarty@cls.fr>

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
* [Generation](#Generation)
* [Installation](#Installation)
* [Configuration](#Configuration)
* [START&STOP](#SS)

#<a name="Overview">Overview</a>
Motu project is a web server used to distribute data. [To be completed]


#<a name="DEV">Development environment</a>

## Configure eclipse development environment
- Add variable: Run/Debug > String substitution
MOTU_HOME=J:\dev\cmems-cis-motu\motu-install-dir

- From a file explorer, create folders:
$MOTU_HOME/log
$MOTU_HOME/config
$MOTU_HOME/data-deliveries

- Copy configuration files from Eclipse to configuration folder:
cp $eclipse/motu-config/src/config/common/config $MOTU_HOME/config
cp $eclipse/motu-config/src/config/cls/dev-win7 $MOTU_HOME/config
 
- Add a server in Eclipse: Window>Preferences>Server>Runtime environment
Name=Apache Tomcat 7.0 
Tomcat installation directory=C:\dvlt\java\servers\tomcat\apache-tomcat-7.0.65

- J2EE perspective > Under the Servers view > Right click > New > Server
Server Name: Tomcat v7.0 Server at localhost

- Edit /Servers/Tomcat v7.0 Server at localhost/server.xml and add 
&lt;Context docBase="J:/dev/cmems-cis-motu/motu-install-dir/data-deliveries" path="/mis-gateway/deliveries" />
<BR>
just under the line:
<BR>
&lt;Host appBase="webapps" autoDeploy="true" name="localhost" unpackWARs="true">

        
 
## Launch Tomcat7 server
Click Debug configurations...> Under Apache Tomcat, debug "Motu Tomcat v7.0 Server at localhost"

Open a web browser and test:
http://localhost:8080/motu-web/Motu?action=ping

it displays "OK - response action=ping"


#<a name="COMPILATION">Compilation</a>
cd /motu-parent
<BR>
mvn clean install -Dmaven.test.skip=true


#<a name="Generation">Generation</a>
cd /motu-distribution
<BR>
ant
<BR>
cd target-ant/delivery/YYYYMMDDhhmmssSSS
<BR>
- src: contains sources of motu application and the configuration files<BR>
- motu: contains the built application and the products (java, tomcat, cdo)<BR>
- config: contains the built configurations for each target platform<BR>


#<a name="Installation">Installation</a>
- Install from archives files
cd /opt/cmems-cis
<BR>
cp motu-products-A.B.tar.gz .
<BR>
cp motu-distribution-X.Y.Z.tar.gz .
<BR>
cp motu-config-X.Y.Z-$BUILDID-$TARGET-$PLATFORM.tar.gz .
<BR>
tar xzf motu-products-A.B.tar.gz
<BR>
tar xzf motu-distribution-X.Y.Z.tar.gz
<BR>
tar xzf motu-config-X.Y.Z-$BUILDID-$TARGET-$PLATFORM.tar.gz
<BR>
cd motu
<BR>
<BR>
Platform is installed and configured. It can be started.

- Installation folder structure
<BR>
Once archives have been extracted, a "motu" folder is created and contains several sub-folders and files.
  - config: Folder which contains the motu configuration files.
  - data: Folder used to save downloaded products.
  - log: Folder which contains all log files.
  - motu file: Script used to start, stop Motu application.
  - pid: Folder which contains pid files of the running Motu application.
  - products: Folder which contains Java, Tomcat ($CATALINE_BASE folder) and CDO products.
  - tomcat-motu: Tocat is deployed inside as a $CATALINE_HOME folder
  - version-distribution.txt file: Contains the version of the current Motu distribution.

- Setup a front Apache HTTPd server
TODO


#<a name="Configuration">Configuration</a>
cd /opt/cmems-cis/motu/config
<BR>
- System configuration
  - vi motu.properties : This file contains all network ports used by the motu application (Tomcat, JMX, Debug, ...) and CAS SSO server parameters.<BR>
- Motu data and product configuration
  - vi motu.xml : This file contains the proxy information, and folder configuration and dataset configuration parameters.<BR>

#<a name="SS">START&STOP</a>
- Start Motu
<BR>
cd /opt/cmems-cis/motu
<BR>
./ motu start


- Stop Motu
<BR>
cd /opt/cmems-cis/motu
<BR>
./ motu stop

