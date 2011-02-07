<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:sparql="http://www.w3.org/2005/sparql-results#">
	<xsl:output method="xml" encoding="UTF-8"/>
	<xsl:import href="../search.xsl"/>
	<xsl:template match="/">
		<html>
			<head>
				<title>Nested PURLs</title>
				<link rel="view" title="View" target="_self" href="?view" />
				<link rel="edit" title="Edit" target="_self" href="?edit" />
				<link rel="review" title="History" target="_self" href="?review" />
				<link rel="parts" title="Parts" target="_self" href="" />
				<link rel="help" title="Help" target="help" href="/persistent/docs/purl.html" />
			</head>
			<body class="search purl">
				<h1>Nested PURLs</h1>
				<xsl:if test="not(/sparql:sparql/sparql:results/sparql:result)">
					<p>No nested PURLs.</p>
				</xsl:if>
				<xsl:if test="/sparql:sparql/sparql:results/sparql:result">
					<ul id="results">
						<xsl:apply-templates />
					</ul>
				</xsl:if>
			</body>
		</html>
	</xsl:template>
	<xsl:template match="sparql:result">
		<li>
			<a href="{sparql:binding[@name='uri']/sparql:uri}" data-diverted="?view">
				<xsl:attribute name="class">
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
			<a href="{sparql:binding[@name='uri']/sparql:uri}" data-diverted="?edit">edit</a>
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
