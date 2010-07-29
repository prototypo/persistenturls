<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:template match="/group">
        <batch>
            <sql>
                INSERT INTO groups VALUES( null, '<xsl:value-of select="name"/>', '<xsl:value-of select="id"/>', '<xsl:value-of select="comments"/>', NOW(), NOW(), 1, false);
            </sql>
            
            <sql>
                set @gid = LAST_INSERT_ID();
            </sql>
            
            <xsl:for-each select="maintainers/uid">
                <sql>
                    INSERT INTO groupmaintainers VALUES(null, @gid, @@USER-<xsl:value-of select="."/>@@, 0);                
                </sql>
            </xsl:for-each>
            
            <xsl:for-each select="maintainers/gid">
                <sql>
                    INSERT INTO groupmaintainers VALUES(null, @gid, @@GROUP-<xsl:value-of select="."/>@@, 1);                
                </sql>
            </xsl:for-each>
            
            <xsl:for-each select="members/uid">
                <sql>
                    INSERT INTO groupmembers VALUES(null, @gid, @@USER-<xsl:value-of select="."/>@@, 0);                
                </sql>
            </xsl:for-each>
            
            <xsl:for-each select="members/gid">
                <sql>
                    INSERT INTO groupmembers VALUES(null, @gid, @@GROUP-<xsl:value-of select="."/>@@, 1);                
                </sql>
            </xsl:for-each>             
            
        </batch>
    </xsl:template>  
</xsl:stylesheet>
