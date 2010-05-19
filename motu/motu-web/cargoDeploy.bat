:begin

echo off

set CURRENT_DIR=%cd%
REM echo %CURRENT_DIR%

REM set PROFILE=%1
set PROFILE=
REM set PROFILE=-P localhost,onlybinaries

REM set WHAT=%2
set WHAT=install


echo %WHAT%

if %WHAT% == deploy goto deploy

:install-motu-api-message
cd %CURRENT_DIR%\..\motu-api\motu-api-message
call mvn -Dmaven.test.skip=true %PROFILE% clean install 

:install-motu-api-rest
cd %CURRENT_DIR%\..\motu-api\motu-api-rest
call mvn -Dmaven.test.skip=true %PROFILE% clean install 

:install-motu-library-converter
cd %CURRENT_DIR%\..\motu-library\motu-library-converter
call mvn -Dmaven.test.skip=true %PROFILE% clean install 

:install-motu-library-inventory
cd %CURRENT_DIR%\..\motu-library\motu-library-inventory
call mvn -Dmaven.test.skip=true %PROFILE% clean install 

:install-motu-library-misc
cd %CURRENT_DIR%\..\motu-library\motu-library-misc
call mvn -Dmaven.test.skip=true %PROFILE% clean install 

:install-motu-processor-wps
cd %CURRENT_DIR%\..\motu-processor\motu-processor-wps
call mvn -Dmaven.test.skip=true %PROFILE% clean install 

:install-motu-api-client
cd %CURRENT_DIR%\..\motu-api\motu-api-client
REM call mvn -Dmaven.test.skip=true %PROFILE% clean install 
call mvn -Dmaven.test.skip=true %PROFILE% assembly:assembly

cd /D %CURRENT_DIR%
:install-motu-web
REM echo install atoll-motu-web
call mvn -Dmaven.test.skip=true %PROFILE% clean install 

if %WHAT% == install goto end

:deploy

echo undeploy motu-web with profile '%PROFILE%'
call mvn -Dmaven.test.skip=true %PROFILE% cargo:deployer-undeploy 
echo deploy motu-web with profile '%PROFILE%'
call mvn -Dmaven.test.skip=true %PROFILE% clean package  cargo:deployer-deploy 


:end