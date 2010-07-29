PURL Bulk Loader Usage Instructions
Version 1.0
March 2, 2009
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

This file contains some basic information on running a loader
to convert legacy PURL server database dumps to the new PURLZ
server content.  The legacy PURL server by OCLC used a dbm database; the
new PURLZ server uses either HSQLDB or MySQL, but is accessed via a
RESTful interface.  This program reads from dbm dumps and writes to a
PURLZ server via its RESTful interface.

This software is licensed as defined in the accompanying files
LICENSE.txt.txt.  These files may be found in the
installation directory.

This is NOT production code.  There is an attempt to clean up and
log rejections.  Not all old-style PURLs are allowed to be new-style
PURLs (e.g. no '$' characters are allowed in URLs, partial PURLs no
longer end in '/').  You will have to fuss with this to get it to work
for you!


---------
| Usage |
---------

The input documents expected are table dumps from the old database.
The new server is loaded by creating XML files and using HTTP to
POST the data.

From purlsbulkloader, run:

java -cp
lib/org.restlet.jar:lib/com.noelios.restlet.jar:dist/legacyloader.jar
org.oclc.purl.legacy.DataLoader <dirname>

Where <dirname> is the directory where the input files are.

Then just let it run to completion.  It will take a while on a large
data set.  The log files will be generated in <dirname> alongside
the input data files.

If you want to specify the host and port, run:

java -cp
lib/org.restlet.jar:lib/com.noelios.restlet.jar:dist/legacyloader.jar -h
<host> -p <port> org.oclc.purl.legacy.DataLoader <dirname>

