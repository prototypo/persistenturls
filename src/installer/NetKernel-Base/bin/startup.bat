@echo off
rem ######################################################
rem #NetKernel Startup Script
rem #v1.1.0
rem #(C) 2005, 1060 Research Limited
rem ######################################################

rem #####################
rem #Get local path
rem #####################
set BASEPATH="$INSTALL_PATH"
set LIB=%BASEPATH%\lib
set EXT=%BASEPATH%\lib\ext

rem #####################
rem #system properties
rem #####################
set PROPS=-Djava.endorsed.dirs=%LIB%\endorsed
set PROPS=%PROPS% -Dbootloader.basepath=%BASEPATH%
set PROPS=%PROPS% -Dbootloader.jarDir=%EXT%
set PROPS=%PROPS% -Dbootloader.nativeDir=%LIB%\native
set PROPS=%PROPS% -Djava.protocol.handler.pkgs=com.ten60.netkernel
set PROPS=%PROPS% -Dsun.net.client.defaultConnectTimeout=20000
set PROPS=%PROPS% -Dsun.net.client.defaultReadTimeout=20000

rem #####################
rem #Kernel ClassPath
rem #####################
set CP=%LIB%/1060netkernel-bootloader-1.1.2.jar

rem #####################
rem #Start Kernel
rem #####################
java -Xmx64m -Xms62m %PROPS% -cp %CP% com.ten60.netkernel.bootloader.BootLoader
