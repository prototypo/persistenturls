26 February 2009

To generate Doxygen (http://www.stack.nl/~dimitri/doxygen/) documentation
and update it on the purlz.org Web site, do the following:

1) Run Doxygen and load the configuration file .../src/Doxyfile by selecting
the "Load" button in the GUI.

2) Start the Doxygen run by pressing the "Start" (or "Run doxygen",
depending on your version) button in the GUI.

3) Copy the purlz-doxygen stylesheet from the existing Doxygen content, because
it lives in a tree we are about to remove.

The purlz-skined doxygen stylesheet can be found at 

.../site/project/purl/doxygen-main.css

This will need to replace the one here

.../site/project/purl/documentation/doxygen/html/main.css

see step #5 for specifics

4) Remove the old Doxygen content from svn - the Doxygen application
doesn't replace the files otherwise...

  $ svn remove .../site/project/purl/documentation/doxygen/
  $ svn commit -m 'Removed old Doxygen documentation'

5) Put the new Doxygen output in place and add to svn:

  $ mv .../src/doxygen .../site/project/purl/documentation
  $ cp .../site/project/purl/doxygen-main.css .../site/project/purl/documentation/doxygen/html/main.css
  $ svn add .../site/project/purl/documentation/doxygen/
  $ svn commit -m 'Updated Doxygen for current version'

