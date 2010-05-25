<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:sparql="http://www.w3.org/2005/sparql-results#">
	<xsl:output method="xml" encoding="UTF-8"/>
	<xsl:import href="../search.xsl"/>
	<xsl:template match="/">
		<html>
			<head>
				<title>Group Search Results</title>
			</head>
			<body class="search group">
				<h1>Group Search Results</h1>
				<form action="/" method="get" class="search">
					<input type="hidden" class="profile" name="group" />
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
</xsl:stylesheet>
