# Batch Uploading to a PURL Server v1.0-1.6.x #

Examples of code to perform batch uploading may be found in [PURLClients](PURLClients.md).

Batches of PURL are defined in XML, using the following syntax:

```
<?xml version="1.0" encoding="ISO-8859-1"?>
<purls>
	<!-- Type 301.  Moved Permanently to a target URL -->
	<purl id="/tld/subdomain/test301" type="301">
		<maintainers>
			<uid>uche</uid>
		</maintainers>
		<target url="http://example.com/test301target/"/>
	</purl>
	<!-- Type 302.  Simple redirection to a target URL -->
	<purl id="/tld/subdomain/test302" type="302">
		<maintainers>
			<uid>uche</uid>
		</maintainers>
		<target url="http://example.com/test302target/"/>
	</purl>
	<!-- Type 303.  See Also URL -->
	<purl id="/tld/subdomain/test303/" type="303">
		<maintainers>
			<uid>uche</uid>
		</maintainers>
		<seealso url="http://example.com/see/also/some/more.xml"/>
	</purl>
	<!-- Type 307.  Temporary redirect to a target URL -->
	<purl id="/tld/subdomain/test307" type="307">
		<maintainers>
			<uid>uche</uid>
		</maintainers>
		<target url="http://example.com/test307target/"/>
	</purl>
	<!-- Type 404.  Temporarily gone -->
	<purl id="/tld/subdomain/test404" type="404">
		<maintainers>
			<uid>uche</uid>
		</maintainers>
	</purl>
	<!-- Type 410.  Permanently gone -->
	<purl id="/tld/subdomain/test410" type="410">
		<maintainers>
			<uid>uche</uid>
		</maintainers>
	</purl>
	<!-- Type clone.  Clone an existing PURL -->
	<purl id="/tld/subdomain/testClone" type="clone">
		<basepurl path="/NET/an/old/PURL"/>
	</purl>
	<!-- Type chain.  Chain an existing PURL -->
	<purl id="/tld/subdomain/testChain" type="chain">
		<maintainers>
			<uid>uche</uid>
		</maintainers>
		<basepurl path="/NET/an/old/PURL"/>
	</purl>
	<!-- Type partial.  Partial-redirect PURL -->
	<purl id="/tld/subdomain/testPartial" type="partial">
		<maintainers>
			<uid>uche</uid>
		</maintainers>
		<target url="http://example.com/testPartialtarget/"/>
	</purl>
</purls>
```

The XML must comply with the following RELAX NG schema:

```
<grammar xmlns="http://relaxng.org/ns/structure/1.0" xmlns:sch="http://www.ascc.net/xml/schematron">
	<start>
		<element name="purls">
			<oneOrMore>
				<element name="purl">
					<attribute name="id"/>
					<attribute name="type"/>
					<optional>
						<element name="maintainers">
							<interleave>
								<zeroOrMore>
									<element name="uid">
										<text/>
									</element>
								</zeroOrMore>
								<zeroOrMore>
									<element name="gid">
										<text/>
									</element>
								</zeroOrMore>
							</interleave>
						</element>                    
					</optional>
					<optional>
						<choice>
							<element name="target">
								<attribute name="url"/>
							</element>
							<element name="seealso">
								<attribute name="url"/>
							</element>
							<element name="basepurl">
								<attribute name="path"/>
							</element>                    
						</choice>
					</optional>
				</element>
			</oneOrMore>
		</element>
	</start>
</grammar>
```