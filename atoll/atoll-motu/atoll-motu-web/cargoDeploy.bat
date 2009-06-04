:begin

:install
echo off
echo install atoll-motu-web
REM call mvn -Dmaven.test.skip=true install

:deploy

echo undeploy atoll-motu-web with profile '%1'
call mvn -Dmaven.test.skip=true -P %1 package  cargo:deployer-undeploy
echo deploy atoll-motu-web with profile '%1'
call mvn -Dmaven.test.skip=true -P %1 package  cargo:deployer-deploy


:end