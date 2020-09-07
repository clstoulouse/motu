#!/bin/sh
#
# Set the LD_LIBRARY_PATH before running CDO
#

cd "$(dirname "$0")"
CUR_DIR=$(pwd)
source "$CUR_DIR/../cots-versions.sh"

HDF_DIR=$CUR_DIR/hdf5-$HDF5_VERSION-install
GLIBC_DIR=$CUR_DIR/glibc-$GLIBC_VERSION-install
ZLIB_DIR=$CUR_DIR/zlib-$ZLIB_VERSION-install
NETCDF_DIR=$CUR_DIR/netcdf-c-$NETCDF_VERSION-install

# Check if the SYSTEM GLIBC version if greater than 1.14
# In this case, use the sytem GLIBC version, otherwise use the embeded one
GLIBC_SYSTEM_VESION=`ldd --version | grep "2\." | cut -d. -f2`
if (( $GLIBC_SYSTEM_VESION >= 15 )); then
  GLIBC_DIR=
fi

export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:$HDF_DIR/share/:$HDF_DIR/lib:$GLIBC_DIR/lib:$NETCDF_DIR/lib:$ZLIB_DIR/lib

$CUR_DIR/cdo-$CDO_VERSION-install/bin/cdo -s --history $*
exit $?
