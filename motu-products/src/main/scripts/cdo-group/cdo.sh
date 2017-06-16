#!/bin/sh
#
# Set the LD_LIBRARY_PATH before running CDO
#

cd "$(dirname "$0")"
CUR_DIR=$(pwd)

HDF_DIR=$CUR_DIR/hdf5-1.8.17-install
GLIBC_DIR=$CUR_DIR/glibc-2.14-install
ZLIB_DIR=$CUR_DIR/zlib-1.2.11-install
NETCDF_DIR=$CUR_DIR/netcdf-4.4.1-install

# Check if the SYSTEM GLIBC version if greater than 1.14
# In this case, use the sytem GLIBC version, otherwise use the embeded one
GLIBC_SYSTEM_VESION=`ldd --version | grep "2\." | cut -d. -f2`
if (( $GLIBC_SYSTEM_VESION >= 15 )); then
  GLIBC_DIR=
fi

export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:$HDF_DIR/share/:$HDF_DIR/lib:$GLIBC_DIR/lib:$NETCDF_DIR/lib:$ZLIB_DIR/lib

$CUR_DIR/cdo-1.8.1-install/bin/cdo -s --history $*
exit $?
