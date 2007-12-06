Introduction
------------

This directory contains scripts and libraries to enable NetKernel to be
registered as an NT service on the Windows platform. It uses the open-source
'Java Service Wrapper' http://wrapper.tanukisoftware.org/ - our grateful thanks
to all who have contributed to this project.

Configuration
-------------

All configuration is specified in the 'wrapper.conf' file. This specifies JVM
options and command line arguments for invoking the NetKernel bootloader.  The
supplied wrapper.conf has the same settings as the [install]/bin/startup.bat
script.

Additional settings for managing the service can be found here:

http://wrapper.tanukisoftware.org/doc/english/properties.html

It is recommended that you stop and uninstall the '1060 NetKernel' service
before changing the wrapper.conf settings - see below.

********IMPORTANT*******

During installation of NetKernel, substitutions are performed to set the
basepath for a number of wrapper.conf options.

However, if your basepath contains spaces you must hand edit the wrapper.conf
to replace directory names containing spaces by their short non-spaced DOS
equivalents. For example:

C:\Program Files\NetKernel\

should be changed to...

C:\Progra~1\NetKernel\

To get the short names, open a command line and type 'dir /x' to get a directory
listing containing short names.

Testing
-------

You can test the settings you provided in wrapper.conf by running NetKernel.bat
This will boot NetKernel through the service wrapper.  When you are happy that
NetKernel boots and is configured correctly you can register a windows service
to start/stop and manage NetKernel as a native windows service.

Install/Uninstall Service
-------------------------

To install, run InstallNetKernel-NT.bat.  This will create a service called
'1060 NetKernel' in the service management console.  You can now use the console
to set start and stop options and to start NetKernel for the first time after
installing.

To uninstall stop the '1060 NetKernel' service and run UnInstallNetKernel-NT.bat
