<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:sparql="http://www.w3.org/2005/sparql-results#">
	<xsl:output method="xml" encoding="UTF-8"/>
	<xsl:import href="../search.xsl"/>
	<xsl:template match="/">
		<html>
			<head>
				<title>User Domains</title>
				<link rel="view" title="View" target="_self" href="?view" />
				<link rel="edit" title="Edit" target="_self" href="?edit" />
				<link rel="review" title="History" target="_self" href="?review" />
				<link rel="domains" title="Domains" target="_self" href="" />
				<link rel="help" title="Help" target="help" href="/persistent/docs/user.html" />
			</head>
			<body class="search domain">
				<h1>User Domains</h1>
				<xsl:if test="not(/sparql:sparql/sparql:results/sparql:result)">
					<p>No domains found.</p>
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
			<a href="{sparql:binding[@name='uri']/sparql:uri}" class="diverted">
				<xsl:apply-templates select="sparql:binding[@name='label']/*" />
			</a>
			<xsl:text> [</xsl:text>
			<a href="{sparql:binding[@name='uri']/sparql:uri}?edit" class="diverted">edit</a>
			<xsl:text>] </xsl:text>
		</li>
	</xsl:template>
</xsl:stylesheet>
