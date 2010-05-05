
set MOTU-API-CLIENT-VERSION=1.0.0-SNAPSHOT
set MOTU-API-CLIENT-JAR-PATH=J:\dev\motu\motu-api\motu-api-client\target
set MOTU-API-CLIENT-JAR=%MOTU-API-CLIENT-JAR-PATH%\motu-api-client-%MOTU-API-CLIENT-VERSION%-full.jar

REM --------------------------------
REM example to list the usage of motu-api-client
REM --------------------------------
java -jar %MOTU-API-CLIENT-JAR%

pause

REM --------------------------------
REM example with a non-CAsified Opendap
REM --------------------------------
java -jar %MOTU-API-CLIENT-JAR% data="http://opendap.mercator-ocean.fr/thredds/dodsC/mercatorPsy2v3_med_mean_best_estimate" login="sqd" pwd="df"

pause

REM --------------------------------
REM example with a CAsified Opendap
REM if CAS server has auto-signed certificate :
REM    - import certificate into your JVM trusted certificates file (default file is \jre\lib\security\cacerts file under java home directory
REM    - add '-Djavax.net.ssl.trustStore=your_JVM_certs_file' to the java command line as above
REM --------------------------------
set your_login=%1
set your_pwd=%2
 
REM java -Djavax.net.ssl.trustStore=%JAVA_HOME%\jre\lib\security\cacerts -jar %MOTU-API-CLIENT-JAR% data="http://atoll-dev.cls.fr:43080/thredds/dodsC/mercator_modified" login=%your_login% pwd=%your_pwd% 

pause

REM --------------------------------
REM example with all explicit parameters (except login and pwd).
REM --------------------------------
REM java -jar %MOTU-API-CLIENT-JAR% data=http://opendap.mercator-ocean.fr/thredds/dodsC/mercatorPsy2v3_med_mean_best_estimate xmlFile=catalog.xml authmode=CAS output=./test.xml
