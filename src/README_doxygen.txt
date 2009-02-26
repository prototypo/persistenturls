26 February 2009

To generate Doxygen (http://www.stack.nl/~dimitri/doxygen/) documentation
and update it on the purlz.org Web site, do the following:

1) Run Doxygen and load the configuration file .../src/Doxyfile by selecting
the "Load" button in the GUI.

2) Start the Doxygen run by pressing the "Start" button in the GUI.

3) Remove the old Doxygen content from svn - the Doxygen application
doesn't replace the files otherwise...

  $svn remove .../site/project/purl/documentation/doxygen/

4) Put the new Doxygen output in place and add to svn:

  $ mv .../src/doxygen .../site/project/purl/documentation
  $ svn add .../site/project/purl/documentation/doxygen/
  $ svn commit -m 'Updated Doxygen for current version'

