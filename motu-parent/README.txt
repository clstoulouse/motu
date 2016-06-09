### Compile projects ### 
mvn clean install -Dmaven.test.skip=true


### Configure eclipse ### 
 > Add variable: Run/Debug > String subsitution
MOTU_HOME=J:\dev\cmems-cis-motu\motu-install-dir

 > From a file explorer, create folders:
$MOTU_HOME/log
$MOTU_HOME/config
$MOTU_HOME/data-deliveries

 > Copy configuration files from Eclipse to configuration folder:
cp $eclipse/motu-config/src/config/common/config $MOTU_HOME/config
cp $eclipse/motu-config/src/config/cls/dev-win7 $MOTU_HOME/config
 
 > Add a server in Eclipse: Window>Preferences>Servers>Runtime environment
Name=Apache Tomcat 7.0 
Tomcat installation directory=C:\dvlt\java\servers\tomcat\apache-tomcat-7.0.65

 > J2EE perspective > Under the Servers view > Right click > New > Server
Server Name: Tomcat v7.0 Server at localhost

 > Edit /Servers/Tomcat v7.0 Server at localhost/context.xml and add 
<Context docBase="J:/dev/cmems-cis-motu/motu-install-dir/data-deliveries" path="/mis-gateway/deliveries" />
                                                                    
                                                                     just under the line:
<Host appBase="webapps" autoDeploy="true" name="localhost" unpackWARs="true">

        
 
### Launch Tomcat7 server ### 
Click Debug configurations...> Under Apache Tomcat, debug "Motu Tomcat v7.0 Server at localhost"

Open a web browser and test:
http://localhost:8080/motu-web/Motu?action=ping

it displays "OK - response action=ping"

