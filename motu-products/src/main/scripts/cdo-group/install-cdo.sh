#!/bin/sh
#
# Used to install CDO.
# CDO tools are used by Motu in order to concatenate depth
#   - cdo: https://code.zmaw.de/projects/cdo
#   - zlib: http://zlib.net/
#   - hdf5: https://www.hdfgroup.org/HDF5   
# $1 reference the MOTU_HOME, folder will be created from this folder

checkCompilation(){
   # $1 a return code
   # $2 an exit code if $1 != 0
   # $3 an error message
   if [ $1 -ne 0 ]; then
       echo "$3 error"
       exit $2
   fi
}

compileAndInstall(){
  # $1 = Lib name for message only
  # $2 = false: do not run check-install
  echo
  echo "======================================"
  echo "$1 install"
  echo

  make
  checkCompilation $? 4 "$1"

  make check
  checkCompilation $? 4 "$1"

  make install
  checkCompilation $? 4 "$1"

  if [ "$2" == "false" ]; then
    echo "Avoid check-install: $1"
  else
    make check-install
    checkCompilation $? 4 "$1"
  fi

  echo
  echo "======== END $1 INSTALL =============="
  echo
}

# Config folders
MOTU_HOME=/opt/motu
if [ ! -z "$1" ]; then
  MOTU_HOME=$1
fi
CDO_GROUP_FOLDER_ABSPATH=$MOTU_HOME/products/cdo-group
ZLIB_FOLDER_NAME=zlib-$ZLIB_VERSION
HDF_FOLDER_NAME=hdf5-$HDF5_VERSION
NETCDF_FOLDER_NAME=netcdf-$NETCDF_VERSION
CDO_FOLDER_NAME=cdo-$CDO_VERSION


# Zlib
ZLIB_HOME_PATH=$CDO_GROUP_FOLDER_ABSPATH/$ZLIB_FOLDER_NAME
ZLIB_INSTALL_PATH=$ZLIB_HOME_PATH-install
mkdir $ZLIB_INSTALL_PATH
cd $ZLIB_HOME_PATH
./configure --prefix=$ZLIB_HOME_PATH-install
compileAndInstall "Zlib" false


# Install HDF5
HDF_HOME_PATH=$CDO_GROUP_FOLDER_ABSPATH/$HDF_FOLDER_NAME
HDF_INSTALL_PATH=$HDF_HOME_PATH-install
mkdir $HDF_INSTALL_PATH
cd $HDF_HOME_PATH
./configure --prefix=$HDF_HOME_PATH-install --enable-cxx --with-zlib=$ZLIB_INSTALL_PATH CFLAGS=-fPIC
compileAndInstall "HDF" false


#install NetCDF
NETCDF_HOME_PATH=$CDO_GROUP_FOLDER_ABSPATH/$NETCDF_FOLDER_NAME
NETCDF_INSTALL_PATH=$NETCDF_HOME_PATH-install
LIBCURLDEVEL_INSTALL_PATH=$MOTU_HOME/products/cdo-group
mkdir $NETCDF_INSTALL_PATH
cd $NETCDF_HOME_PATH
export CPPFLAGS="-I$ZLIB_INSTALL_PATH/include -I$HDF_INSTALL_PATH/include " 
export  LDFLAGS="-L$ZLIB_INSTALL_PATH/lib     -L$HDF_INSTALL_PATH/lib     " 
echo
echo "CPPFLAGS=$CPPFLAGS" 
echo
./configure --prefix=$NETCDF_INSTALL_PATH CFLAGS=-fPIC --disable-dap --disable-cmdremote
export LD_LIBRARY_PATH="$LD_LIBRARY_PATH:$HDF_INSTALL_PATH/lib"
compileAndInstall "NETCDF" false


# Install CDO
cd $CDO_GROUP_FOLDER_ABSPATH
CDO_HOME_PATH=$CDO_GROUP_FOLDER_ABSPATH/$CDO_FOLDER_NAME-install
mkdir $CDO_HOME_PATH
cd $CDO_FOLDER_NAME
./configure --with-hdf5=$HDF_INSTALL_PATH --with-netcdf=$NETCDF_INSTALL_PATH --prefix=$CDO_HOME_PATH
make
make check
make install
make check-install        # verify installation.

# Set execution rights 
echo "Add execution rights to cdo.sh and merge.sh"
chmod a+x $CDO_GROUP_FOLDER_ABSPATH/cdo.sh
chmod a+x $CDO_GROUP_FOLDER_ABSPATH/merge.sh

echo 
echo "##########################################################"
echo " - ZLIB: $ZLIB_HOME_PATH"
echo " - HDF:  $HDF_HOME_PATH"
echo " - CDO:  $CDO_HOME_PATH"
echo "END OF CDO INSTALLATION"
echo "##########################################################"
$CDO_GROUP_FOLDER_ABSPATH/cdo.sh --version 2>&1
echo


exit 0
