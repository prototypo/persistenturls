# README.txt file for the Current PURL server release #

```
PURL Server Installation Instructions
Version 1.6.3
March 9, 2010
purlz@zepheira.com
purl-interest@purlz.org
purl-dev@purlz.org

----------------
| Introduction |
----------------

Thank you for your interest in the Persistent Uniform Resource Locator
(PURL) server.  More information about PURLs and the PURL server
software may be found at http://purlz.org.  Please consider joining
the mailing lists and participating in the online community so we can
serve you better.

This software is licensed as defined in the accompanying files
LICENSE.txt and LEGAL.txt.  These files may be found in the
installation directory.


----------------
| Installation |
----------------

There are three ways to install a PURL server, each of which are
described below.  Please choose the option which matches your needs.

  ----------------------------------------------
  | Option A.  Installing from a JAR Installer |
  ----------------------------------------------

NOTE: This option is recommended for end users and those wishing to
run a PURL server on non-default ports.  It will install a fully
functional instance of 1060 NetKernel as well as the PURL service.

1)  Download the JAR installer:

  http://purlz.org/project/purl/downloads/PURLZ-Server-<version>.jar

2) Run the installer and follow its directions.  If you operating
system does not allow JAR files to be executed directly, run the
installer using this command:

  java -jar PURLZ-Server-<version>.jar


3) Hit http://localhost:8080/docs/index.html with a Web browser. Help
is available at http://localhost:8080/docs/help.html

4) Log in as user 'admin' with password 'password'.  You may change the
password by modifying the user record after you have logged in.

5) Have fun!

A few thoughts:

1) If you just want to play around with the server, the HSQLDB
installation should be fine and self-contained. If you want to install a
more realistic server, please select the MySQL option and make sure it
is installed and configured (it will ask for username/password/port info
during installation).

2) By default, users and top-level domains are "approved" automatically.
This is important for unit testing purposes and will be fine for many
installations. If you want to have a production environment where this
is not the case, this is most easily specified during the installation
process. If you run without the auto approval process selected, you will
have to approve these through the admin interface which is now linked
off of the main UI (you need admin privileges).

3) By default an admin account is created with username/password of
admin/password.

4) To hit the main documentation page, go to
http://localhost:8080/docs/index.html. We make a best guess as to the
name of the machine during the installation process. If you want to
override this behavior during the installation, you can. If you need to
add other hostnames after the fact, please edit the
$installation_dir/modules/mod-purl-virtualhost/module.xml and look for
the section that discusses this.


  ----------------------------------------------------------
  | Option B.  Installing using a NetKernel Module Package |
  ----------------------------------------------------------

NOTE: This option is recommended for users with existing NetKernel
services installed.  Please be careful to ensure that you install the
PURL module into your existing NetKernel or, if you want to install
another NetKernel instance, that you use different ports for the
NetKernel fulcrums (default ports are 8080 for the Front End Fulcrum
and 1060 for the Back End Fulcrum).  Note that Option A (Installing
from a JAR Installer) allows you to install a new NetKernel and
manually set the port numbers.

1) Install NetKernel 3.3.1, available from:

  http://www.1060.org/download/

You will want to choose the top option ("Executable JAR Graphical
Installer").

2) Run NetKernel

3) Access NetKernel's Module Management Wizard by going to:

  http://localhost:1060/ep+name@app_install_installer

4) Choose "Deploy a packaged set of modules" and select the button
labeled "next".

5) Enter the URL to the PURL server:

  http://purlz.org/project/purl/downloads/purlz-<version>.zip

6) Try the tests:

(a) For the server-side tests, hit http://localhost:1060/ep+name@app_xunit

(b) For the client-side tests, MAKE SURE that your PURL server is using
automatic user creation.  Then run:

 $ cd <repository>/src
 $ ant test

7) Hit http://localhost:8080/docs/index.html with a Web browser. Help
is available at http://localhost:8080/docs/help.html

8) Log in as user 'admin' with password 'password'.  You may change the
password by modifying the user record after you have logged in.

9) Have fun!


  ----------------------------------------------------
  | Option C.  Installing from a Subversion Checkout |
  ----------------------------------------------------

NOTE: This option is useful for developers and those wishing to
customize their installation.

To install a PURL server from a Subversion checkout, do the following:

1) Install NetKernel 3.3.1, available from:

  http://www.1060.org/download/

NB: You will probably want to choose the top option ("Executable JAR
Graphical Installer").  But proceed this way if you want to cause
yourself headaches.

2) Checkout the PURL source code:

 svn checkout http://purlz.zepheira.com/svn/purlz <local_repository_name>

3) Edit the file <NK_Install_dir>/etc/deployedModules.xml and add
lines like the following (editing the path as necessary):

 <module>file:/<path_to_repository>/src/mod-purl-gatekeeper/modules/mod-purl-gatekeeper</module>
 <module>file:/<path_to_repository>/src/mod-purl-storage/modules/mod-purl-storage</module>
 <module>file:/<path_to_repository>/src/mod-purl-storage/modules/test-mod-purl-storage</module>
 <module>file:/<path_to_repository>/src/mod-purl-admin/modules/mod-purl-admin</module>
 <module>file:/<path_to_repository>/src/mod-purl-admin/modules/test-mod-purl-admin</module>	
 <module>file:/<path_to_repository>/src/mod-purl-search/modules/mod-purl-search</module>
 <module>file:/<path_to_repository>/src/mod-purl-virtualhost/modules/mod-purl-virtualhost</module>
 <module>file:/<path_to_repository>/src/mod-session/modules/mod-session</module>
 <module>file:/<path_to_repository>/src/mod-template/modules/mod-template</module>
 <module>file:/<path_to_repository>/src/mod-template/modules/test-mod-template</module>
 <module>file:/<path_to_repository>/src/mod-purl-documentation/modules/mod-purl-documentation</module>  

While you are in that file, comment out the NetKernel demo applications:

  	<!--Demonstration Apps - remove these for production systems-->
	<!--
	<module>modules/app-address-book-1.1.4.jar</module>
	<module>modules/pingpong-1.2.0.jar</module>
	<module>modules/forum-web-1.1.6.jar</module>
	<module>modules/forum-services-lite-1.0.0.jar</module>
	<module>modules/forum-style-2.0.1.jar</module>
	-->

4) Edit the file <NK_Install_dir>/modules/mod-fulcrum-frontend/module.xml 
and do the following:

(a) Add these lines toward the bottom (where the comment directing you
to add your modules is):

       <import>
           <uri>urn:org:purl:virtual:host</uri>
       </import>

(b) Comment out the following lines:

       <!--Put every request into the FFCPL domain-->
       <!--
       <rewrite>
	<match>jetty://.*?/(.*)</match>
	<to>ffcpl:/$1</to>
       </rewrite>
       -->

(c) Search for "Demo and Test modules" and comment them out.

5) Edit the file
/<path_to_repository>/src/mod-purl-admin/modules/mod-purl-admin/etc/PURLConfig.xml
and comment out the following line (IFF you want to review and approve
new user registrations:

     <!--
     <allowUserAutoCreation/>
     -->

6) Edit the file
/<path_to_repository>/src/mod-purl-admin/modules/mod-purl-admin/module.xml
and comment out the following lines:

	<!--
	<import>
		<uri>urn:org:purl:static</uri>
	</import>
	-->

7) Build the source code:

 $ cd <repository>/src

 (edit the file build.xml and change the value of the property
 "netkernel.home.dir" to the path to the NetKernel installation
 directory.)

 $ ant all
 $ ant deploy

 To make the graphical installer:

 $ cd installer
 $ ./mkinstaller.sh

8) Start NetKernel by:

 $ cd <NK_Install_dir>/bin
 $ ./start.sh

9) Try the tests:

(a) For the server-side tests, hit http://localhost:1060/ep+name@app_xunit

(b) For the client-side tests, MAKE SURE that your PURL server is using
automatic user creation.  Then run:

 $ cd <repository>/src
 $ ant test

10) Hit http://localhost:8080/docs/index.html with a Web browser. Help
is available at http://localhost:8080/docs/help.html

11) Log in as user 'admin' with password 'password'.  You may change the
password by modifying the user record after you have logged in.

12) Have fun!
```