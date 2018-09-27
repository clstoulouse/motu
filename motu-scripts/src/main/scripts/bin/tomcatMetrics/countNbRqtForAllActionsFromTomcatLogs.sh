#!/bin/bash
# Count how many requests have been done
# $1=empty(i.e. Today)|DD|MM-DD|YYYY-MM-DD
#SEARCH_DATE=2018-09-18

TODAY=`date --iso-8601`
YYYY=${TODAY:0:4}
MM=${TODAY:5:2}
DD=${TODAY:8:2}
    
if [ "$#" -ge 1 ]; then 
	param1Length=${#1}
	if [[ "$1" =~ .*h.* ]]; then
		echo "Usage: $0 [empty(i.e. Today)|DD|MM-DD|YYYY-MM-DD]"
		exit 0
	elif [ $param1Length = 2 ]; then
		DD=$1
	elif [ $param1Length = 5 ]; then
	    MM=${1:0:2}
		DD=${1:3:2}
	elif [ $param1Length = 10 ]; then
		SEARCH_DATE=$1
	else
		echo "Usage: $0 [empty(i.e. Today)|DD|MM-DD|YYYY-MM-DD]"
		exit 1
	fi
fi

if [ -z "$SEARCH_DATE" ]; then 
	SEARCH_DATE=$YYYY-$MM-$DD
fi

DT=`date --iso-8601=s`
echo "--- $SEARCH_DATE, generated the $DT" 
for actionName in debug describecoverage describeproduct getreqstatus getsize gettimecov httperror listcatalog listservices root productdownload productdownloadhome  welcome; do
  countAction=`./countNbRqtFor1ActionFromTomcatLogs.sh $actionName $SEARCH_DATE`
  if [ $? != 0 ]; then
  	./countNbRqtFor1ActionFromTomcatLogs.sh $actionName $SEARCH_DATE
  	exit $?
  fi
 
  if [ "$actionName" == "root" ]; then
    printf "%25s%s%4d\n" " ( /Motu ) listservices" "=" "$countAction"
  else
  	sufix=""
  	if [ "$actionName" == "productdownload" ]; then
  		sufix="**"
  	fi
    printf "%25s%s%4d %s\n" "$prefix$actionName" "=" "$countAction" "$sufix"
  fi
done
