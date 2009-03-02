#!/bin/bash
######################################################
#NetKernel Startup Script
#v1.1.0
#(C) 2005, 1060 Research Limited
######################################################

#####################
#Get local path
#####################
BASEPATH=$INSTALL_PATH
LIB=$BASEPATH/lib
EXT=$BASEPATH/lib/ext

#####################
#system properties
#####################
PROPS=-Djava.endorsed.dirs=$LIB/endorsed
PROPS="$PROPS -Dbootloader.basepath=$BASEPATH"
PROPS="$PROPS -Dbootloader.jarDir=$EXT"
PROPS="$PROPS -Dbootloader.nativeDir=$LIB/native"
PROPS="$PROPS -Djava.protocol.handler.pkgs=com.ten60.netkernel"
PROPS="$PROPS -Dsun.net.client.defaultConnectTimeout=20000"
PROPS="$PROPS -Dsun.net.client.defaultReadTimeout=20000"
PROPS="$PROPS -Djava.awt.headless=false"
PROPS="$PROPS -Dten60.pid=1"   #Process id must be set when using 'netkernel' daemon


#####################
#Kernel ClassPath
#####################
CP=$LIB/1060netkernel-bootloader-1.1.2.jar

#####################
#Start Server
#####################
java -Xmx64m -Xms62m $PROPS -cp $CP com.ten60.netkernel.bootloader.BootLoader
