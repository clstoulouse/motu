#!/bin/sh
# Count in tomcat Motu logs
#$1=productdownload|getreqstatus|productdownloadhome|...
#$2=2018-09-18

cd "$(dirname "$0")"
MOTU_INSTALL_DIR=$(pwd)/../..

SEARCH_ACTION=$1
FILE_NAME_PREFIX=localhost_access_log..
FILE_NAME=$FILE_NAME_PREFIX$2.txt

TOMCAT_FILE_PATH="$MOTU_INSTALL_DIR/tomcat-motu/logs/$FILE_NAME"
if [ -f $TOMCAT_FILE_PATH ]; then
	nbCurAction=`grep "action=" $TOMCAT_FILE_PATH | sed -E 's/action/@/g' | cut -d'@' -f 2 | cut -d'&' -f 1 | cut -c 2- | sort | grep -i "$SEARCH_ACTION" | wc -l`
	if [ "$SEARCH_ACTION" == "root" ]; then
		nbCurAction=`grep "/Motu " $TOMCAT_FILE_PATH | wc -l`
	fi
    total=$((nbCurAction + nbDefault))
    echo $total
else
   echo "ERROR: File $TOMCAT_FILE_PATH does not exists."
   echo
   echo
   echo "Check: ls -al $MOTU_INSTALL_DIR/tomcat-motu/logs/ | grep $FILE_NAME_PREFIX"
   ls -al $MOTU_INSTALL_DIR/tomcat-motu/logs/ | grep $FILE_NAME_PREFIX
   echo
   exit 1
fi
  
