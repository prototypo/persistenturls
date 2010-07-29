<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:template match="/group">
        <batch>
            <sql>
                UPDATE groups SET 
                "name" = '<xsl:value-of select="name"/>',
                "comments" = '<xsl:value-of select="comments"/>',
                "lastmodified" = NOW,
                "indexed" = 0
                where "g_id" = '<xsl:value-of select="id"/>';
            </sql>
            <sql>
                DELETE FROM groupmaintainers WHERE "g_id" = '@@GID@@';
            </sql>
            <sql>
                DELETE FROM groupmembers WHERE "g_id" = '@@GID@@';
            </sql>            
            
            <xsl:for-each select="maintainers/uid">
                <sql>
                    INSERT INTO groupmaintainers VALUES(null, @@GID@@, @@USER-<xsl:value-of select="."/>@@, 0);                
                </sql>
            </xsl:for-each>
            
            <xsl:for-each select="maintainers/gid">
                <sql>
                    INSERT INTO groupmaintainers VALUES(null, @@GID@@, @@GROUP-<xsl:value-of select="."/>@@, 1);                
                </sql>
            </xsl:for-each>
            
            <xsl:for-each select="members/uid">
                <sql>
                    INSERT INTO groupmembers VALUES(null, @@GID@@, @@USER-<xsl:value-of select="."/>@@, 0);                
                </sql>
            </xsl:for-each>
            
            <xsl:for-each select="members/gid">
                <sql>
                    INSERT INTO groupmembers VALUES(null, @@GID@@, @@GROUP-<xsl:value-of select="."/>@@, 1);                
                </sql>
            </xsl:for-each>            
        </batch>
    </xsl:template>
</xsl:stylesheet>