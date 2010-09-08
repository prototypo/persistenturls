Persistent Uniform Resource Locator by Zepheira (PURLZ) Installation Instructions

http://purlz.org/

21 July 2010

----------------
| Introduction |
----------------

Thank you for your interest in the PURLZ server.

Please consider joining the mailing lists and participating in the
online community so we can serve you better.

Version-specific information is contained in the file RELEASENOTES.txt.
There are no release notes for the first release.

This software is licensed as defined in the accompanying files
LICENSE.txt and LEGAL.txt.  These files may be found in the
installation directory.

----------------
| Installation |
----------------

Download a release archive from http://purlz.org/ and extract it into a new directory.

Edit the file "etc/mail.properties" and include your local SMTP credentials. A typical file might contain the following:
mail.transport.protocol = smtp
mail.from = system@example.com
mail.smtp.host = example.com
mail.smtp.port = 25
mail.smtp.auth = true
mail.user = system
mail.password = secret

Stop any other servers listening on port 80. To change the port the PURL server listens on edit the etc/persistent.conf file.

Execute a start script to start the server. Open your browser to http://localhost/ listed in console output. Click the register link and fill in the form using the ID "admin". Follow the instructions to register and login.

Click the home link at the top and then click the edit link. In the edit screen click on the chevrons to create a new domain origin. Fill in the form to create a PURL authority that will be served by this server. Once a domain has been created assign it to the server, save the changes. This process may take a few moments, please be patient.

Click on the top level domain to continue to create the PURL hierarchy.

Domains are logical folders that assign access control lists and PURL authorities.

Partials are folders in the hierarchy that may use regular expressions to auto complete targets.

PURLs are persistent URLs within domains or partials that resolve to an explicit target.

Share your experience or ask questions on the mailing list.

