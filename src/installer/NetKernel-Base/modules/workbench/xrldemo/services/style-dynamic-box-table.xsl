<?xml version="1.0" encoding="UTF-8" ?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xrl="http://1060.org/xrl">
    <xsl:output method="xml"/>
	<xsl:param name="param"/>
	
    <xsl:template match="/">
		<table>
			<xsl:call-template name="row">
				<xsl:with-param name="index" select="0"/>
			</xsl:call-template>
		</table>
    </xsl:template>
	
	<xsl:template name="row">
		<xsl:param name="index"/>
		<xsl:if test="$index &lt; $param/nvp/size">
			<tr>
				<xsl:call-template name="box">
					<xsl:with-param name="index" select="0"/>
				</xsl:call-template>
			</tr>
			<xsl:call-template name="row">
				<xsl:with-param name="index" select="$index+1"/>
			</xsl:call-template>
		</xsl:if>
	</xsl:template>

	<xsl:template name="box">
		<xsl:param name="index"/>
		<xsl:if test="$index &lt; $param/nvp/size">
			<td>
				<xrl:include href="xrl:service-random-box"/>
			</td>
			<xsl:call-template name="box">
				<xsl:with-param name="index" select="$index+1"/>
			</xsl:call-template>
		</xsl:if>
	</xsl:template>
</xsl:stylesheet>
