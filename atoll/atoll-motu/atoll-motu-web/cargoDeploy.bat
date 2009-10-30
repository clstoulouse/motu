:begin

echo off

set CURRENT_DIR=%cd%

:install-motu

cd ..

REM echo validate atoll-motu and  dependencies
REM call mvn -Dmaven.test.skip=true -P %1 validate 

REM echo install atoll-motu and  dependencies
REM call mvn -Dmaven.test.skip=true -P %1 clean install 

cd /D %CURRENT_DIR%
:install-motu-web
REM echo install atoll-motu-web
REM call mvn -Dmaven.test.skip=true -P %1 clean install 

:deploy

echo undeploy atoll-motu-web with profile '%1'
call mvn -Dmaven.test.skip=true -P %1 cargo:deployer-undeploy 
echo deploy atoll-motu-web with profile '%1'
call mvn -Dmaven.test.skip=true -P %1 clean package  cargo:deployer-deploy 


:end