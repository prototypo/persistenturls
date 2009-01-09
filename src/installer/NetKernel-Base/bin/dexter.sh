#!/bin/bash
######################################################
#NetKernel Command Line Startup Script
#v1.0.0
#(C) 2003-2005, 1060 Research Limited
######################################################

#####################
#Get local path
#####################
DEXTER_HOME=$INSTALL_PATH
LIB=$DEXTER_HOME/lib
XAR=$DEXTER_HOME/modules
ETC=$DEXTER_HOME/etc

#####################
#Kernel ClassPath
#####################
CP=$ETC/:$LIB/1060netkernel-2.8.5.jar:$LIB/1060netkernel-embedded-1.0.1.jar:$LIB/1060netkernel-cli-1.0.1.jar
CP=$CP:$XAR/ext-layer1-1.3.5.jar
JVM_FLAGS="-Xmx64m -Djava.endorsed.dirs=$LIB/endorsed -Dsun.net.client.defaultConnectTimeout=20000 -Dsun.net.client.defaultReadTimeout=20000"

#####################
#Start CLI
#####################
java $JVM_FLAGS -cp $CP com.ten60.netkernel.cli.CommandLine -b $DEXTER_HOME/ $@