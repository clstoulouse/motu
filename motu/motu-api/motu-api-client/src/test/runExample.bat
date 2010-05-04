


REM --------------------------------
REM example with a non-CAsified Opendap
REM --------------------------------
java -jar J:\dev\motu\motu-api\motu-api-client\target\motu-api-client-1.0.0-SNAPSHOT-full.jar data="http://opendap.mercator-ocean.fr/thredds/dodsC/mercatorPsy2v3_med_mean_best_estimate" 

pause

REM --------------------------------
REM example with a CAsified Opendap
REM --------------------------------
set your_login=%1
set your_pwd=%2
 
java -Djavax.net.ssl.trustStore=%JAVA_HOME%\jre\lib\security\cacerts -jar J:\dev\motu\motu-api\motu-api-client\target\motu-api-client-1.0.0-SNAPSHOT-full.jar data="http://atoll-dev.cls.fr:43080/thredds/dodsC/mercator_modified" login=%your_login% pwd=%your_pwd% 
