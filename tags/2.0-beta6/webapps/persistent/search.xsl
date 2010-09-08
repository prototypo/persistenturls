<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:sparql="http://www.w3.org/2005/sparql-results#">
	<xsl:output method="xml" encoding="UTF-8"/>
	<xsl:template match="/">
		<html>
			<head>
				<title>Search Results</title>
				<link rel="help" href="/persistent/docs/" />
			</head>
			<body class="search">
				<xsl:apply-templates />
			</body>
		</html>
	</xsl:template>
	<xsl:template match="sparql:sparql">
		<xsl:apply-templates select="sparql:results" />
	</xsl:template>
	<xsl:template match="sparql:results">
		<ul>
			<xsl:apply-templates select="sparql:result" />
		</ul>
	</xsl:template>
	<xsl:template match="sparql:result">
		<li>
			<dl>
				<xsl:apply-templates select="sparql:binding[@name!='uri' and @name!='label' and @name!='unresolvable']" />
			</dl>
		</li>
	</xsl:template>
	<xsl:template match="sparql:binding">
		<dt>
			<xsl:value-of select="@name"/>
		</dt>
		<dd>
			<xsl:apply-templates select="*" />
		</dd>
	</xsl:template>
	<xsl:template match="sparql:uri">
		<span class="uri">
			<xsl:value-of select="text()" />
		</span>
	</xsl:template>
	<xsl:template match="sparql:bnode">
		<span class="bnode">
			<xsl:value-of select="text()" />
		</span>
	</xsl:template>
	<xsl:template match="sparql:literal">
		<span class="literal">
			<xsl:value-of select="text()" />
		</span>
	</xsl:template>
	<xsl:template
		match="sparql:literal[@datatype='http://www.w3.org/1999/02/22-rdf-syntax-ns#XMLLiteral']">
		<span class="literal" datatype="rdf:XMLLiteral">
			<xsl:value-of disable-output-escaping="yes" select="text()" />
		</span>
	</xsl:template>
</xsl:stylesheet>
