# Release Notes for the Current PURL server release #

```
PURL Server Release Notes
Version 1.6.3
March 9, 2010
http://purlz.org


This file contains release notes for the PURL server.

-------------------------
| Version 1.6.3 Changes |
-------------------------
This version changes PURL validation functionality to use the much more
efficient HTTP HEAD operation when validating PURL targets (versus HTTP
GET).


-------------------------
| Version 1.6.2 Changes |
-------------------------
Provides a single file (simplepurl.html) to facilitate the creation of 
simple (302) PURLs via a Javascript bookmarklet.  The bookmarklet itself
is accessible from:
  http://purlz.org/project/purl/downloads/

NB: To update a running v1.6.1 PURL server for this feature without
re-installing just follow these steps:
  $ cd {installation directory}/modules/
  $ mkdir mod-purl-documentation-1.6.1
  $ cd mod-purl-documentation-1.6.1
  $ unzip ../mod-purl-documentation-1.6.1.jar
  $ curl http://purlz.org/project/purl/downloads/1.6.2/simplepurl.html > resources/simplepurl.html
  $ zip -r ../mod-purl-documentation-1.6.1.jar .
  $ cd ..
  $ rm -r mod-purl-documentation-1.6.1
  <chown/chmod mod-purl-documentation-1.6.1.jar as necessary>
  <Restart your PURL server>

You can use the PURL bookmarklet with any PURL server that implements
this upgrade.  Get the PURL bookmarklet by going to:
  http://purlz.org/project/purl/downloads/purl_bookmarklet.html


------------------------- 
| Version 1.6.1 Changes |
-------------------------
Further fixes for resolution of URL's with invalid characters, fix for database timestamp 
sync issue, suppression of search indexer log messages

FIXED:
#1063 Items fail to index when the database clock is out of sync with the server's 
      -- Indexing now relies solely on the database server's timestamp
#1075 http code for syntax error in urls, should be 400 code
#1076 PURLs with percent-encoded or reserved sub-delims characters being http 500 code
      -- fix enhances that carried over from v1.6


----------------------- 
| Version 1.6 Changes |
-----------------------

Major changes:
* Feature request http://purlz.org/project/purl/development/ticket/1073
  partially addressed.  Three new variations on partial-redirect PURLs
  are introduced to support various use cases.  Directions for use and
  a full desription of functionality are available in the help file.

Minor:
* URLs with non-standard characters (e.g. '/dc/elements/1.1/\">') should
  now redirect instead of returning a 500-series error code.  URLs
  including the '$' character will still fail.
* Repaired a bug where a 500 status is being returned for all PURLs with
  a '/docs/' path segment.

Outstanding:
* This release (and previous releases) do not support input of Unicode
characters.

FIXED:
#1071
#1074
#1069
#1068
#1067
#1066
#1065
#1064
#32


------------------------- 
| Version 1.5 Bug Fixes |
-------------------------

Version 1.5 of the PURL server subtly changes the relationship between
PURLs and domains to better support legacy PURLs.  Domains may now be
created that contain PURLs of the same name (that is, a PURL with a name
cooresponding to an empty string or a '/' character).

PURLs may now be created with names that differ only by a trailing '/'
(e.g. /net/foo and /net/foo/).


These features were added in support legacy PURL migration.  Naming
new PURLs with these patterns is not encouraged.

No reported bugs were addressed in this release.  However, some
additional URL-encoding of form elements was added to the Web user
interface to better support special characters.

FIXED:
#1062

-------------------------
| Version 1.4 Bug Fixes |
-------------------------

Ticket numbers and descriptions refer to content on http://purlz.org/project/purl/development/

FIXED:
#1061 Max number of user sessions is too small
#1060 User ID's are case insensitive in the database, but not in search.
#1059 Newly created resources could be ignored by the search indexer
#1058 Target URL's in XML responses not being escaped
#1057 Target URL in PURL create and modify are not being escaped properly by the client app
#998 PURLs ending in common file extensions cause a java.lang.IllegalArgumentException
#44 Modify SQL search to be less exact?

WONTFIX:
#1001 Use of a '$' in a a purl, domain, group or user fails

-------------------------
| Version 1.3 Bug Fixes |
-------------------------

Ticket numbers and descriptions refer to content on http://purlz.org/project/purl/development/

#1053 The Modify PURL UI should track the create PURL interface
#1002 Lucene Index process blocks all requests with large numbers of purls
#999 Batch loader allows duplicate PURLs to be created
#998 PURLs ending in common file extensions cause a java.lang.IllegalArgumentException
#997 Rewrite batch handling to continue processing after single PURL error
#996 Fix broken URL resolution when cookie not set
#993 purl search by maintainer doesn't work
#992 error on 307 redirect
#989 Error during startup
#928 Port number to run service on is not offered in installer
#927 can't modify a partial redirect purl
#45 Can't create a PURL that matches the domain
#40 Delete (Tombstone) of PURLS, then search for PURLS with tombstone not working.
#39 Search purls with group IDs, not working
#38 Searching for USERs, return 2 instances for each record.
#37 Multiple Maintainer, Writers, Members Creation
#35 purl properties are not populated to purl modification form after purl validation
#28 auto-population of group fields when modifying groups causes bad characters to be passed to server
#27 clarify default maintainer assumption to the user
#24 groups treated as users
#22 include PURL prefix in interface where appropriate
#18 batch loading problem
#16 PURL User Login feedback
```