#!/bin/bash
######################################################
#Tarball Installer Script v1.0.1
#Script to perform post tarball installation - must be
#run from base installation directory.  See readme.txt
#for details
#(C) 2005, 1060 Research Limited
######################################################

echo ------------------------------------------
echo Performing Substitutions...
#create sed script
cmd=s,\$INSTALL_PATH,$PWD,
echo $cmd > sub.sed

#Modify files in bin/
echo 'Directory bin/'
echo 'Processing start.sh'
sed -f sub.sed bin/start.sh  > bin/start.sh.ed
cp bin/start.sh.ed bin/start.sh
echo 'Processing netkernel'
sed -f sub.sed bin/netkernel  > bin/netkernel.ed
cp bin/netkernel.ed bin/netkernel
echo 'Processing startup.bat'
sed $cmd bin/startup.bat  > bin/startup.bat.ed
cp bin/startup.bat.ed bin/startup.bat
rm bin/*.ed
chmod u+x bin/start.sh
chmod u+x bin/netkernel
#win32 wrapper service
sed $cmd bin/win32-service/wrapper.conf  > bin/win32-service/wrapper.conf.ed
cp bin/win32-service/wrapper.conf.ed bin/win32-service/wrapper.conf
rm bin/win32-service/*.ed

#Modify Files in etc/
echo 'Directory etc/'
echo 'Processing system.xml'
sed $cmd etc/system.xml  > etc/system.xml.ed
cp etc/system.xml.ed etc/system.xml
echo 'Processing bootloader.cfg'
sed $cmd etc/bootloader.cfg  > etc/bootloader.cfg.ed
cp etc/bootloader.cfg.ed etc/bootloader.cfg
rm etc/*.ed

#Modify Files in extra/
echo 'Directory extra/servlet/WEB-INF/'
echo 'Processing web.xml'
sed $cmd extra/servlet/WEB-INF/web.xml  > extra/servlet/WEB-INF/web.xml.ed
cp extra/servlet/WEB-INF/web.xml.ed extra/servlet/WEB-INF/web.xml
rm extra/servlet/WEB-INF/web.xml.ed

#Clean up
rm sub.sed

echo ...Installation Completed.
echo ------------------------------------------
echo 'bin/start.sh' to boot NetKernel
echo ------------------------------------------
echo WARNING:  The installation has assumed that
echo your JVM is not headless, ie has access to
echo native graphical libraries etc.  If this
echo is a headless server please edit start.sh
echo so that java.awt.headless=true
echo ------------------------------------------
echo Solaris Notes:  Detailed info on how to
echo modify the bin/netkernel init.d script for use on
echo Solaris are available at http://1060.org/forum/
echo in the 'installation forum'.