@echo off
rem ######################################################
rem #Dexter CommandLine Startup Script
rem #v1.0.0
rem #(C) 2003, 1060 Research Limited
rem ######################################################

rem #####################
rem #Get local path
rem #####################
set DEXTER_HOME="$INSTALL_PATH"
set LIB=%DEXTER_HOME%\lib
set XAR=%DEXTER_HOME%\modules
set ETC=%DEXTER_HOME%\etc
set JVM_FLAGS=-Xmx64m -Djava.endorsed.dirs=%LIB%/endorsed -Dsun.net.client.defaultConnectTimeout=20000 -Dsun.net.client.defaultReadTimeout=20000
rem #####################
rem #Kernel ClassPath
rem #####################
set CP=%ETC%\;%LIB%\1060netkernel-2.8.5.jar;%LIB%\1060netkernel-embedded-1.0.1.jar;%LIB%\1060netkernel-cli-1.0.1.jar
set CP=%CP%;%XAR%\ext-layer1-1.3.5.jar

rem #####################
rem #Start CLI
rem #####################
java %JVM_FLAGS% -cp %CP% com.ten60.netkernel.cli.CommandLine -b %DEXTER_HOME%/ %1 %2 %3 %4 %5 %6 %7 %8 %9
