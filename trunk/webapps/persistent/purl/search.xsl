<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:sparql="http://www.w3.org/2005/sparql-results#">
	<xsl:output method="xml" encoding="UTF-8"/>
	<xsl:import href="../search.xsl"/>
	<xsl:template match="/">
		<html>
			<head>
				<title>PURL Search Results</title>
				<link rel="help" title="Help" target="_blank" href="/persistent/docs/" type="text/html" />
			</head>
			<body class="search purl">
				<h1>PURL Search Results</h1>
				<form action="/" method="get" class="search" onsubmit="if(elements['q'].value.indexOf(':')>0)elements['purl'].name='target'">
					<input type="hidden" class="profile" name="purl" />
					<input type="text" id="q" name="q" value="" />
					<img class="submit" src="/persistent/images/search-button.png" alt="Search" title="Click to search" />
				</form>
				<ul id="results">
					<xsl:apply-templates />
					<xsl:if test="count(/sparql:sparql/sparql:results/sparql:result)=0">
						<p>No results found.</p>
					</xsl:if>
				</ul>
			</body>
		</html>
	</xsl:template>
	<xsl:template match="sparql:result">
		<li>
			<a href="{sparql:binding[@name='uri']/sparql:uri}">
				<xsl:attribute name="class">
					<xsl:text>diverted</xsl:text>
					<xsl:if test="sparql:binding[@name='redirection']">
						<xsl:text> redirection</xsl:text>
					</xsl:if>
					<xsl:if test="sparql:binding[@name='unresolvable']">
						<xsl:text> unresolvable</xsl:text>
					</xsl:if>
				</xsl:attribute>
				<xsl:apply-templates select="sparql:binding[@name='label']/*" />
			</a>
			<xsl:text> [</xsl:text>
			<a href="{sparql:binding[@name='uri']/sparql:uri}?edit" class="diverted">edit</a>
			<xsl:text>] </xsl:text>
			<xsl:text> </xsl:text>
			<xsl:value-of select="substring-after(sparql:binding[@name='type']/sparql:uri, 'http://persistent.name/rdf/2010/purl#')"/>
			<xsl:if test="sparql:binding[@name='parent']">
			<xsl:text> in </xsl:text>
			<a href="{sparql:binding[@name='parent']/sparql:uri}" class="diverted"><xsl:value-of select="sparql:binding[@name='parent']/sparql:uri"/></a>
			</xsl:if>
		</li>
	</xsl:template>
</xsl:stylesheet>
