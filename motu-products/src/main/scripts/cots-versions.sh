#!/bin/sh

export MOTU_PRODUCTS_VERSION=${MOTU_VERSION:-3.10.0}

export APACHE_TOMCAT_VERSION=${MOTU_PRODUCTS_TOMCAT_VERSION:-9.0.8}
export LIBTCNATIVE_VERSION=${project.version.libtcnative}
export OPEN_JDK_VERSION=${project.version.openjdk.version}
export OPEN_JDK_RELEASE=${project.version.openjdk.release}
export OPEN_JDK_BUILD=${project.version.openjdk.build}
export OPEN_JDK_INSTALLED_VERSION=openjdk-${OPEN_JDK_VERSION}u${OPEN_JDK_RELEASE}-b${OPEN_JDK_BUILD}

export ZLIB_VERSION=${project.version.zlib}
export HDF5_VERSION=${project.version.hdf5}
export NETCDF_VERSION=${project.version.netcdf}
export CDO_VERSION=${project.version.cdo}
export GLIBC_VERSION=${project.version.glibc}