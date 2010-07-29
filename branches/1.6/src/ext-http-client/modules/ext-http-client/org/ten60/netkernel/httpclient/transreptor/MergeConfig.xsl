<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="xml"/>
	<xsl:param name="default"/>
    <xsl:template match="/config">
        <config>
			<xsl:choose>
				<xsl:when test="followRedirects">
					<xsl:copy-of select="followRedirects"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:copy-of select="$default/config/followRedirects"/>
				</xsl:otherwise>
			</xsl:choose>
			<xsl:choose>
				<xsl:when test="retryAttempts">
					<xsl:copy-of select="retryAttempts"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:copy-of select="$default/config/retryAttempts"/>
				</xsl:otherwise>
			</xsl:choose>
			<xsl:choose>
				<xsl:when test="maxConnectionsPerHost">
					<xsl:copy-of select="maxConnectionsPerHost"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:copy-of select="$default/config/maxConnectionsPerHost"/>
				</xsl:otherwise>
			</xsl:choose>
			<xsl:choose>
				<xsl:when test="maxTotalConnections">
					<xsl:copy-of select="maxTotalConnections"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:copy-of select="$default/config/maxTotalConnections"/>
				</xsl:otherwise>
			</xsl:choose>
			<xsl:choose>
				<xsl:when test="maxAcceptableContentLength">
					<xsl:copy-of select="maxAcceptableContentLength"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:copy-of select="$default/config/maxAcceptableContentLength"/>
				</xsl:otherwise>
			</xsl:choose>
			<xsl:choose>
				<xsl:when test="stateExpirationTime">
					<xsl:copy-of select="stateExpirationTime"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:copy-of select="$default/config/stateExpirationTime"/>
				</xsl:otherwise>
			</xsl:choose>
			<xsl:choose>
				<xsl:when test="connectTimeout">
					<xsl:copy-of select="connectTimeout"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:copy-of select="$default/config/connectTimeout"/>
				</xsl:otherwise>
			</xsl:choose>
			<xsl:choose>
				<xsl:when test="timeout">
					<xsl:copy-of select="timeout"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:copy-of select="$default/config/timeout"/>
				</xsl:otherwise>
			</xsl:choose>
			<xsl:choose>
				<xsl:when test="proxyPort">
					<xsl:copy-of select="proxyPort"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:copy-of select="$default/config/proxyPort"/>
				</xsl:otherwise>
			</xsl:choose>
			<xsl:choose>
				<xsl:when test="proxyHost">
					<xsl:copy-of select="proxyHost"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:copy-of select="$default/config/proxyHost"/>
				</xsl:otherwise>
			</xsl:choose>
		</config>
    </xsl:template>
</xsl:stylesheet>
