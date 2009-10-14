:begin

echo off

set CURRENT_DIR=%cd%

:install-motu

cd ..

REM echo validate atoll-motu and  dependencies
REM call mvn -Dmaven.test.skip=true validate -P %1

echo install atoll-motu and  dependencies
call mvn -Dmaven.test.skip=true install

cd /D %CURRENT_DIR%
:install-motu-web
echo install atoll-motu-web
call mvn -Dmaven.test.skip=true install

:deploy

echo undeploy atoll-motu-web with profile '%1'
call mvn -Dmaven.test.skip=true -P %1 package  cargo:deployer-undeploy
echo deploy atoll-motu-web with profile '%1'
call mvn -Dmaven.test.skip=true -P %1 package  cargo:deployer-deploy


:end