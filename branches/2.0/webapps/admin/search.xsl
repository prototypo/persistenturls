<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:sparql="http://www.w3.org/2005/sparql-results#">
	<xsl:output method="xml" encoding="UTF-8"/>
	<xsl:template match="/">
		<html>
			<head>
				<title>Search Results</title>
			</head>
			<body class="search">
				<h1>Search Results</h1>
				<form action="" method="get" class="search">
					<label for="q">Search again:</label>
					<input type="text" id="q" name="q" value="" />
				</form>
				<ul id="results" class="">
					<xsl:apply-templates />
					<xsl:if test="count(/sparql:sparql/sparql:results/sparql:result)=0">
						<p>No results found.</p>
					</xsl:if>
				</ul>
			</body>
		</html>
	</xsl:template>
	<xsl:template match="sparql:sparql">
		<xsl:apply-templates select="sparql:results" />
	</xsl:template>
	<xsl:template match="sparql:results">
		<xsl:apply-templates select="sparql:result" />
	</xsl:template>
	<xsl:template match="sparql:result">
		<li>
			<a href="{sparql:binding[@name='uri']/sparql:uri}?view" class="diverted">
				<xsl:choose>
					<xsl:when test="sparql:binding[@name='label']">
						<xsl:apply-templates select="sparql:binding[@name='label']" />
					</xsl:when>
					<xsl:otherwise>
						<xsl:apply-templates select="sparql:binding[@name='uri']" />
					</xsl:otherwise>
				</xsl:choose>
			</a>
			<dl>
				<xsl:apply-templates select="sparql:binding[@name!='uri' and @name!='label']" />
			</dl>
		</li>
	</xsl:template>
	<xsl:template match="sparql:binding[@name='uri' or @name='label']">
		<xsl:apply-templates select="*" />
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
