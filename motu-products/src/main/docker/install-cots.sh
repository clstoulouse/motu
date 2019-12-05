#!/bin/sh


source "$MOTU_PRODUCTS_DIR/cots-versions.sh"
# --- How to rebuild this archive from scratch?
#     gcc, gcc-c++ and m4 libraries have to be installed on the system.
#
#     First set the version variable:
#     Then run this file as a shell script to build this archive: 
#     chmod a+x ./README ./cots-versions.sh ./cdo-group/*
#     ./README
#     WARN: You need an Internet Access to download products. So optionally, set your proxy parameters:
#         vi ~/.wgetrc
#         --
#         http_proxy=http://$ip:$port/
#         https_proxy=http://$ip:$port/
#         use_proxy=on
#         wait=15
#         --

cd $MOTU_PRODUCTS_DIR

wget --no-cookies https://archive.apache.org/dist/tomcat/tomcat-${APACHE_TOMCAT_VERSION:0:1}/v$APACHE_TOMCAT_VERSION/bin/apache-tomcat-$APACHE_TOMCAT_VERSION.tar.gz
tar xzf apache-tomcat-$APACHE_TOMCAT_VERSION.tar.gz
rm apache-tomcat-$APACHE_TOMCAT_VERSION.tar.gz
result=$?
if [ $result -ne 0 ]; then
   echo "Impossible to retrieve Apache Tomcat"
   exit 3
fi

#echo "### Download Apache Tomcat libtcnative"
wget http://dl.fedoraproject.org/pub/epel/7/x86_64/Packages/t/tomcat-native-$LIBTCNATIVE_VERSION.rpm
rpm2cpio tomcat-native-$LIBTCNATIVE_VERSION.rpm | cpio -idmv
cp ./usr/lib64/libtcnative-1.so* apache-tomcat-$APACHE_TOMCAT_VERSION/lib/
rm -rf ./usr/

cd $MOTU_PRODUCTS_DIR/cdo-group

echo "- download zlib"
wget http://zlib.net/zlib-$ZLIB_VERSION.tar.gz
tar xvf zlib-$ZLIB_VERSION.tar.gz
echo

echo "- download hdf5"
wget https://support.hdfgroup.org/ftp/HDF5/releases/hdf5-${HDF5_VERSION:0:4}/hdf5-$HDF5_VERSION/src/hdf5-$HDF5_VERSION.tar.gz
tar xvzf hdf5-$HDF5_VERSION.tar.gz
echo

echo "- download netcdf"
wget https://www.unidata.ucar.edu/downloads/netcdf/ftp/netcdf-$NETCDF_VERSION.tar.gz
tar xvzf netcdf-$NETCDF_VERSION.tar.gz
echo

echo "- download cdo"
wget https://code.mpimet.mpg.de/attachments/download/18264/cdo-$CDO_VERSION.tar.gz
tar xvzf cdo-$CDO_VERSION.tar.gz
echo

echo "-- Now compile and build cdo tools from sources"
./install-cdo.sh $INSTALL_DIR
result=$?
if [ $result -ne 0 ]; then
   echo "Impossible to install cdo tools"
   exit 3
fi


echo
echo "### Create version file: $MOTU_PRODUCTS_VERSION"
cd $MOTU_PRODUCTS_DIR
echo $MOTU_PRODUCTS_VERSION > version-products.txt 
