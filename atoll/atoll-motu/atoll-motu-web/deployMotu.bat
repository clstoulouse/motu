
rem call ant deploy-dev-atoll-motu

REM call ant deploy-localhost

REM call ant deploy-valid-aviso-motu
REM call ant deploy-valid-mercator-motu

REM call ant deploy-production-aviso-motu
REM call ant deploy-production-mercator-motu

REM call ant deploy-production-themismotu
set CURRENT_DIR=%cd%

call mvn -Dmaven.test.skip=true -P localhost cargo:deployer-undeploy

cd /D C:\Java\apache-tomcat-6.0.9\bin
call shutdown.bat
rmdir /S /Q C:\Java\apache-tomcat-6.0.9\webapps\atoll-motuservlet
call startup.bat
cd /D %CURRENT_DIR%
call mvn -Dmaven.test.skip=true -P localhost package  cargo:deployer-deploy

