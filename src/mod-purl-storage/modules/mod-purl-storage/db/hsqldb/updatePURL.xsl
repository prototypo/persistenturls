<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:template match="/purl">
        <batch>
            <sql>
                UPDATE purls SET 
                "type" = '<xsl:value-of select="type"/>',
                <xsl:if test="//url">
                    "target"='<xsl:value-of select="//url[1]"/>',
                </xsl:if>
                "lastmodified" = NOW(),
                "indexed" = 0
                where "p_id" = '<xsl:value-of select="id"/>';
            </sql>
            <sql>
                DELETE FROM purlmaintainers WHERE "p_id" = '@@PID@@';
            </sql>
            
            <xsl:for-each select="maintainers/uid">
                <sql>
                    INSERT INTO purlmaintainers VALUES(null, @@PID@@, @@USER-<xsl:value-of select="."/>@@, 0);                
                </sql>
            </xsl:for-each>
            
            <xsl:for-each select="maintainers/gid">
                <sql>
                    INSERT INTO purlmaintainers VALUES(null, @@PID@@, @@GROUP-<xsl:value-of select="."/>@@, 1);                
                </sql>
            </xsl:for-each>
            
            <sql>
                INSERT INTO purlhistory VALUES(null, @@PID@@, @@CURRENTUSER@@, 1, '<xsl:value-of select="type"/>', '<xsl:value-of select="*/url"/>', NOW);
            </sql>
        </batch>
    </xsl:template>
</xsl:stylesheet>