# PURLZ Frequently Asked Questions #

## PURLZ Server ##

### How is the PURLZ server installed? ###

A Java JAR installer is distributed for each release version and is linked from http://purlz.org/project/purl/downloads/ . The installer is run like this, using a modern version of Java (greater than 1.4.2):

```
  java -jar PURLZ-Server-<version>.jar
```

### How can the PURLZ server be installed on a headless machine? ###

The graphical installer requires a windowing system and a monitor.  However, an automated installation script may be generated on a machine with a windowing system and then subsequently used on a headless machine.  Run through the graphical installer to the end and select the option to generate an automated installation script.  Either make sure that the options selected during the installation are the same as you want to use on the headless target host or edit the automated installation file after you save it.

If you named the automated installation file 'purlz-config', you can run the following on the headless machine to install the software:

```
  java -jar PURLZ-Server-<version>.jar purlz-config
```

### My PURLZ server is running, but attempting to access any URL results in an error like "URI Resolution Failure".  What is happening? ###

Chances are that you are accessing your PULZ server with a host name that is not configured in its Web server software.  Host names need to be configured in the file '''<PURL server installation directory>/modules/mod-purl-virtualhost/module.xml'''

A portion of that XML file looks like the following and names the hosts for which the PURLZ server should answer:

```
	<export>
		<!--
		***********
		Export all of host address space - note could export multiple hosts here.
		(Note have added localhost so you can test it)
		***********
		-->
		<uri>
			<match>jetty://localhost.*</match>
			<match>jetty://127\.0\.0\.1.*</match>
			<match>jetty://purlws01\.prod\.oclc\.org.*</match>
			<match>jetty://purl\.oclc\.org.*</match>
			<match>jetty://www\.purl\.org.*</match>
			<match>jetty://www\.purl\.net.*</match>
			<match>jetty://purl\.org.*</match>
			<match>jetty://purl\.net.*</match>
			<!-- Add any other jetty://<servername> matches that you want
				 to match.  -->
			<match>ffcpl:/etc/HTTPBridgeConfig.xml</match>
		</uri>
	</export>
```

Change the host names to match the canonical and DNS CNAME host names for your PURLZ server.


### How can 404 or 410 Pages be Customized (or What's with 'Gone, Daddy, Gone'??) ###

The default "Not Found" and "Gone" pages are meant to be replaced by site-specific content.  Replace the sample HTML in the following files:

```
$ cd <installation directory>
$ grep -lr 'Gone, Daddy, Gone!' *
modules/mod-purl-admin/pub/404-gone.html
modules/mod-purl-admin/pub/410-gone.html
```

## PURLZ Community ##

### Where can bugs be submitted?  I don't see a 'new ticket' button on the issues tracker at http://code.google.com/p/persistenturls/issues/ ###

In order to submit bugs on the PURLs issue tracker, you will have to log in using a Google account.


## PURLs ##

### Why are PURLs "tombstoned" instead of being deleted?  What does tombstoning mean? Why can't I create a PURL with the same name as one I have deleted? ###

PURLs are not deleted - they are "tombstoned".   This is because they are **persistent** URLs and may therefore not be reused (although their properties can of course be updated over time).

A PURL is tombstoned by sending an HTTP DELETE, which is an idempotent operation.  Quoting from RFC 2616 (http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html):

```
Methods can also have the property of "idempotence" in that (aside from error or expiration issues)
the side-effects of N > 0 identical requests is the same as for a single request. The methods GET,
HEAD, PUT and DELETE share this property. Also, the methods OPTIONS and TRACE SHOULD NOT
have side effects, and so are inherently idempotent.
```

Subsequent DELETE operations do not send an error message; they result in the state you requested.
If you search for a "deleted" PURL with the "search tombstoned" checkbox selected, you will discover that the PURL is still there.  Just its status has been changed.