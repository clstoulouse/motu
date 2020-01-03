#!/bin/sh

export MOTU_PRODUCTS_VERSION=${MOTU_VERSION:-3.10.0}

export APACHE_TOMCAT_VERSION=${MOTU_PRODUCTS_TOMCAT_VERSION:-9.0.8}
export LIBTCNATIVE_VERSION=${project.version.libtcnative}
export ORACLE_JDK_VERSION=8u181
export ORACLE_JDK_BUILD=13
export ORACLE_JDK_INSTALLED_VERSION=1.8.0_181

export ZLIB_VERSION=${project.version.zlib}
export HDF5_VERSION=${project.version.hdf5}
export NETCDF_VERSION=${project.version.netcdf}
export CDO_VERSION=${project.version.cdo}
export GLIBC_VERSION=${project.version.glibc}