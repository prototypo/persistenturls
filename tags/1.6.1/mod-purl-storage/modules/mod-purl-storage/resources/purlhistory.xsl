<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:template match="results">
        <history>
            <id>@@PID@@</id>
            <xsl:apply-templates/>
        </history>
    </xsl:template>
    <xsl:template match="row">
        <entry>
            <xsl:attribute name="type">
                <xsl:choose>
                    <xsl:when test="status = '0'">create</xsl:when>
                    <xsl:when test="status = '1'">modified</xsl:when>
                    <xsl:when test="status = '2'">tombstoned</xsl:when>
                    <xsl:otherwise>default</xsl:otherwise>
                </xsl:choose>
            </xsl:attribute>
            <type><xsl:value-of select="type"/></type>
            <target><url><xsl:value-of select="target"/></url></target>
            <modifiedby><uid>@@MAINTAINER-<xsl:value-of select="u_id"/>@@</uid></modifiedby>
            <modifieddate><xsl:value-of select="modtime"/></modifieddate>
        </entry>
    </xsl:template>
</xsl:stylesheet>