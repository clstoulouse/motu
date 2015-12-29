begin

echo off

set CURRENT_DIR=%cd%

:install-motu

cd ..

REM echo validate atoll-motu and  dependencies
REM call mvn -Dmaven.test.skip=true -P %1 validate 

REM echo install atoll-motu and  dependencies
call mvn -Dmaven.test.skip=true -P %1 clean install 

cd /D %CURRENT_DIR%
:install-motu-web

call mvn -Dmaven.test.skip=true install

:deploy

set CURRENT_DIR=%cd%

REM call mvn -Dmaven.test.skip=true -P localhost cargo:deployer-undeploy

cd /D C:\Java\apache-tomcat-6.0.9\bin
call shutdown.bat

REM wait
REM ping 127.0.0.1 -n 5 -w 1000  
echo "appuyer sur une touche..." 
pause

rmdir /S /Q C:\Java\apache-tomcat-6.0.9\webapps\atoll-motuservlet
del /Q C:\Java\apache-tomcat-6.0.9\webapps\atoll-motuservlet.war
call startup.bat
cd /D %CURRENT_DIR%
call mvn -Dmaven.test.skip=true -P localhost package  cargo:deployer-deploy


:end