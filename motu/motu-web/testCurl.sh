#!/bin/sh


# URL encode a stream or a string   ### / wird nicht encoded!
function url_encode() {
    # usage exit for too many parameters
    [ $# -gt 1 ] && { echo >&2 "usage: url_encode [string]"; return 1; }
    
    # self call when an argument is given, else handle stdin
    [ $# -eq 1 ] && { echo -n "$1" | url_encode; return $?; }
        
    # first create alternating lines of hex code and ascii characters
    # then remember the hex value,
    # convert spaces to +,
    # keep some selected characters unchanged
    # and use the hexvalue prefixed with a % for the rest
    ##od -t x1c -w1 -v -An |
    ##LANG=C awk '
    ##  NR % 2                  { hex=$1;               next    }
    ##  /^ *$/                  { printf("%s", "+");    next    }
    ##  /^ *[a-zA-Z0-9.*()-]$/  { printf("%s", $1);     next    }
    ##  /^ *\//                 { printf("%s", $1);     next    }
    ##                          { printf("%%%s", hex)           }
    ##'
    hexdump -v -e '1/1 "%02x\t"' -e '1/1 "%_c\n"' |
    LANG=C awk '
        $1 == "20"                      { printf("%s",      "+");   next    }
        $2 ~  /^[a-zA-Z0-9.*()\/-]$/    { printf("%s",      $2);    next    }
                                        { printf("%%%s",    $1)             }
    '
}



urlencode() {
arg="$1"
i="0"
while [ "$i" -lt ${#arg} ]; do
c=${arg:$i:1}
if echo "$c" | grep -q '[a-zA-Z/:_\.\-]'; then
echo -n "$c"
else
echo -n "%"
printf "%X" "'$c'"
fi
i=$((i+1))
done
}

urldecode() {
arg="$1"
i="0"
while [ "$i" -lt ${#arg} ]; do
c0=${arg:$i:1}
if [ "x$c0" = "x%" ]; then
c1=${arg:$((i+1)):1}
c2=${arg:$((i+2)):1}
printf "\x$c1$c2"
i=$((i+3))
else
echo -n "$c0"
i=$((i+1))
fi
done
}

SERVICE_URL="http://atoll-dev.cls.fr:43080/thredds/catalog.xml"
#SERVICE_URL="http://cls-earith.pc.cls.fr:8080/atoll-motuservlet/OpendapAuth"
#SERVICE_URL="http://mercator-data1.cls.fr:43080/thredds/catalog.xml"

./curlCasRest.sh dearith bienvenue $SERVICE_URL  test.xml


#SERVICE_URL="http://atoll-dev.cls.fr:30080/atoll-motuservlet/OpendapAuth?action=productdownload&data=http://atoll-dev.cls.fr:43080/thredds/dodsC/mercator_modified&x_lo=0&x_hi=2&y_lo=1&y_hi=0&t_lo=2002-05-03&t_hi=2002-05-03&z_lo=Surface&z_hi=Surface&variable=kz&mode=console"
#SERVICE_URL='http://atoll-dev.cls.fr:30080/atoll-motu-servlet/Catsat?action=productdownload&data=http://catsat-data1.cls.fr:43080/thredds/dodsC/nrt_glo_st_chlorophyll&x_lo=2&x_hi=3&y_lo=1&y_hi=4&t_lo=2010-03-02&t_hi=2010-03-02&variable=Grid_0001&mode=console'
#SERVICE_URL='http://cls-earith.pc.cls.fr:8080/atoll-motuservlet/OpendapAuth?action=productdownload&data=http://atoll-dev.cls.fr:43080/thredds/dodsC/nrt_glo_hr_infrared_sst&x_lo=2&x_hi=3&y_lo=1&y_hi=4&t_lo=2009-12-01&t_hi=2009-12-01&variable=Grid_0001&mode=console'
SERVICE_URL='http://atoll-dev.cls.fr:30080/atoll-motuservlet/Catsat?action=productdownload&data=http://atoll-dev.cls.fr:43080/thredds/dodsC/nrt_glo_hr_infrared_sst&x_lo=2&x_hi=3&y_lo=1&y_hi=4&t_lo=2009-12-01&t_hi=2009-12-01&variable=Grid_0001&mode=console'
#SERVICE_URL='http://atoll-dev.cls.fr:30080/atoll-motuservlet/OpendapAuth?action=productdownload&data=http://atoll-dev.cls.fr:43080/thredds/dodsC/nrt_glo_hr_infrared_sst&x_lo=2&x_hi=3&y_lo=1&y_hi=4&t_lo=2009-12-01&t_hi=2009-12-01&variable=Grid_0001&mode=console'
#SERVICE_URL='http://catsat-data1.cls.fr:30080/atoll-motu-servlet/Motu?action=listServices'
#SERVICE_URL="http://cls-earith.pc.cls.fr:8080/atoll-motuservlet/OpendapAuth?action=listServices"


#SERVICE_URL=`urlencode $SERVICE_URL`

#./curlCasRest.sh dearith bienvenue $SERVICE_URL test.nc

#curl -X POST -k -v -L -d "action=productdownload&data=http://atoll-dev.cls.fr:43080/thredds/dodsC/nrt_glo_hr_infrared_sst&x_lo=2&x_hi=3&y_lo=1&y_hi=4&t_lo=2009-12-01&t_hi=2009-12-01&variable=Grid_0001&mode=console&ticket=ST-138-7FDjJOJvYKmtvRrD0pDN-atoll-dev.cls.fr" "http://atoll-dev.cls.fr:30080/atoll-motuservlet/Catsat"

#PRODUCT='http://purl.org/cls/atoll/ontology/individual/atoll%23dataset-duacs-global-nrt-madt-merged-h'
#echo `url_encode 'http://purl.org/cls/atoll/ontology/individual/atoll#dataset-duacs-global-nrt-madt-merged-h'`

#iconv -f ISO-8859-1 -t UTF-8 < http://purl.org/cls/atoll/ontology/individual/atoll#dataset-duacs-global-nrt-madt-merged-h

#wget -O testFTP.nc --no-check-certificate "http://atoll-qt1.cls.fr:33080/atoll-motu-servlet/Motu?action=productdownload&service=http://purl.org/cls/atoll/ontology/individual/atoll%23motu-ftp-aviso&product=$PRODUCT&t_lo=2010-03-02&t_hi=2010-03-02&variable=Grid_0001&mode=console"
#wget -v -S -d  --no-check-certificate "http://atoll-qt1.cls.fr:33080/atoll-motu-servlet/Motu?action=productdownload&service=http://purl.org/cls/atoll/ontology/individual/atoll%23motu-ftp-aviso&product=$PRODUCT&t_lo=2010-02-04&t_hi=2010-02-05&mode=console"
#wget -v -S -d -O testFTP.nc --no-check-certificate "http://cls-earith.pc.cls.fr:8080/atoll-motuservlet/Motu?action=productdownload&service=http://purl.org/cls/atoll/ontology/individual/atoll%23motu-ftp-aviso&product=$PRODUCT&t_lo=2010-02-04&t_hi=2010-02-04&variable=Grid_0001&mode=console"
#curl "http://cls-earith.pc.cls.fr:8080/atoll-motuservlet/Motu?action=productdownload&service=http://purl.org/cls/atoll/ontology/individual/atoll%23motu-ftp-aviso&product=$PRODUCT&t_lo=2010-02-04&t_hi=2010-02-04&variable=Grid_0001&mode=console"


