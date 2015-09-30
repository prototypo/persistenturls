# PURL Legacy Loader instructions #

A legacy loader is available to take old OCLC version 1 PURL database dumps and upload PURLs into this project’s RESTful API. This is not production code, but is provided in the hope that it may be useful to operators of old PURL servers wishing to migrate to a more modern PURL server.  At least three PURL server installations have used this code to date.

> svn checkout https://persistenturls.googlecode.com/svn/trunk/purlsbulkloader purlsbulkloader --username `<Google username>`

Check out the code and follow the directions in the file README.txt.