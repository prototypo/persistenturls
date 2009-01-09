<?xml version="1.0" encoding="UTF-8" ?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xrl="http://1060.org/xrl">
    <xsl:output method="xml"/>

    <xsl:template match="/data">
		<table>
			<xsl:apply-templates/>
		</table>
    </xsl:template>
	
	<xsl:template match="set">
		<tr>
			<xsl:apply-templates/>
		</tr>
	</xsl:template>

	<xsl:template match="box">
		<td>
			<xrl:include href="xrl:{.}-box"/>
		</td>
	</xsl:template>
</xsl:stylesheet>
