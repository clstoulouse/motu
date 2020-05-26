#!/bin/bash

###########################################
###  COMMON 
###########################################

__addCatalinaOpt() {
   if [ -n "$CATALINA_OPTS" ]; then
      export CATALINA_OPTS="$CATALINA_OPTS $1"
   else 
      export CATALINA_OPTS="$1"
   fi
}

###########################################
###  MAIN 
###########################################

if [ "${JMX_ENABLED}" == "true" ]; then
    __addCatalinaOpt "-Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=$JVM_PORT_JMX -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false"
fi
if [ "${DEBUG_ENABLED}" == "true" ]; then
    __addCatalinaOpt "-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=$JVM_ADDRESS_DEBUG"
fi