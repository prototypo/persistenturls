Graphical Installer:
--------------------
After graphical install you are ready to go.

To start netkernel execute bin/start.sh [or bin\startup.bat Win32].  Point web-browser at http://localhost:1060/

TarBall Installation:
---------------------

After installation from the tarball the following files must be edited to replace $INSTALL_PATH with the
absolute path to your chosen installation directory.

Files To Edit:

bin/start.sh
bin/netkernel
bin/startup.bat
etc/bootloader.cfg
etc/system.xml

For Unix platforms a shell script is provided to do this automatically.  To use it, open a shell and change directory to
your newly created installation directory then run:

./complete-tarball-install.sh

[It is *important* that this is executed only from the NetKernel installation directory!]

Starting NetKernel
------------------
To start netkernel execute bin/start.sh [or bin/startup.bat Win32].  Point web-browser at http://localhost:1060/