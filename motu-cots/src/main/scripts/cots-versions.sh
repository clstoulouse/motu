#!/bin/sh

export MOTU_PRODUCTS_VERSION=${project.version}

export APACHE_TOMCAT_VERSION=${motu.products.tomcat.version}
export LIBTCNATIVE_VERSION=${project.version.libtcnative}
export OPEN_JDK_VERSION=${project.version.openjdk.version}
export OPEN_JDK_RELEASE=${project.version.openjdk.release}
export OPEN_JDK_BUILD=${project.version.openjdk.build}
export OPEN_JDK_INSTALLED_VERSION=jdk${project.version.openjdk.version}u${project.version.openjdk.release}-b${project.version.openjdk.build}

export ZLIB_VERSION=${project.version.zlib}
export HDF5_VERSION=${project.version.hdf5}
export NETCDF_VERSION=${project.version.netcdf}
export CDO_VERSION=${project.version.cdo}
export GLIBC_VERSION=${project.version.glibc}