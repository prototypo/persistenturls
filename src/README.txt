Installing and Running a PURL Server
------------------------------------

0) This write up assumes that you have NetKernel 3.3 installed somewhere.  We will refer to the NetKernel installation directory as $NETKERNEL_INSTALLATION_DIRECTORY.  At the time of this writing, we are using http://www.1060.org/upload/beta/1060-NetKernel-SE-DK3.3-Beta-1.jar


1) Next do an svn checkout if you haven't done one already:

	svn co http://purlz.zepheira.com/svn/purlz


2) You will need to customize one property in the top level build.xml file. In the "init" target, please point netkernel.home.dir to wherever you have NetKernel installed.


3) Issue the following commands:

  $ ant test
  $ ant deploy


4) We need to install the PURL NetKernel modules into NetKernel.  Please edit the file $NETKERNEL_INSTALLATION_DIRECTORY/etc/deployedModules.xml to comment out the NetKernel demonstration applications and include the following lines (replacing $SVN_CO_DIRECTORY with wherever you checked it the PURL code):

------------------------------%<-------------------------------------
  <!--Demonstration Apps - remove these for production systems-->
  <!--
  <module>modules/app-address-book-1.1.4.jar</module>
  <module>modules/pingpong-1.2.0.jar</module>
  <module>modules/forum-web-1.1.6.jar</module>
  <module>modules/forum-services-lite-1.0.0.jar</module>
  <module>modules/forum-style-2.0.1.jar</module>
  -->

  <!-- PURL modules -->
  <module>file:$SVN_CO_DIRECTORY/src/mod-purl-admin/modules/mod-purl-admin</module>
  <module>file:$SVN_CO_DIRECTORY/src/mod-purl-admin/modules/test-mod-purl-admin</module>	
  <module>file:$SVN_CO_DIRECTORY/src/mod-purl-search/modules/mod-purl-search</module>
  <module>file:$SVN_CO_DIRECTORY/src/mod-purl-virtualhost/modules/mod-purl-virtualhost</module>
  <module>file:$SVN_CO_DIRECTORY/src/mod-session/modules/mod-session</module>
  <module>file:$SVN_CO_DIRECTORY/src/mod-template/modules/mod-template</module>
  <module>file:$SVN_CO_DIRECTORY/src/mod-template/modules/test-mod-template</module>
  <module>file:$SVN_CO_DIRECTORY/src/mod-purl-documentation/modules/mod-purl-documentation</module>
------------------------------%<-------------------------------------


5) Edit $NETKERNEL_INSTALLATION_DIRECTORY/modules/mod-fulcrum-frontend/module.xml as follows:

(a) Add the following lines (look for the section that says "Add your modules below here..."):

<import>
    <uri>urn:org:purl:virtual:host</uri>
</import>

(b) Comment out the following lines:

------------------------------%<-------------------------------------
<!--Put every request into the FFCPL domain-->
<!--
<rewrite>
	<match>jetty://.*?/(.*)</match>
	<to>ffcpl:/$1</to>
</rewrite>
-->
------------------------------%<-------------------------------------

(c) Search for "Demo and Test modules" and comment them out.

6) You should be able to start up NetKernel by running the following:

  $ $NETKERNEL_INSTALLATION_DIRECTORY/bin/start.sh

NetKernel should start without error.


7) Run the server-side XUnit tests. Hit the following URL:

http://localhost:1060/ep+name@app_xunit

You should see "PURLs Template Library Tests". Select the Run button and make sure that you only see green test results. There should be three PURLs tests run.


8)  Run the client-side JUnit tests:

  $ cd $SVN_CO_DIRECTORY
  $ ant test

You should see an output that looks similar to this:

------------------------------%<-------------------------------------
Buildfile: build.xml

init:

build-test:

test:
    [junit] Testsuite: org.purl.test.purlClientTestRunner
    [junit] Tests run: 61, Failures: 0, Errors: 0, Time elapsed: 5.082 sec
    [junit] 

BUILD SUCCESSFUL
Total time: 5 seconds
------------------------------%<-------------------------------------


9) You should now be able to hit the Front End Fulcrum to interact with the PURL server:

http://localhost:8080/docs/index.html


10)  Help on using the server is available from:

http://localhost:8080/docs/help.html


11)  We are working to make this process easier.  In the meantime, have fun!