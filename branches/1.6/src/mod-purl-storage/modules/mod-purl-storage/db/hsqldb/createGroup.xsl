<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:template match="/group">
        <batch>
            <sql>
                INSERT INTO groups VALUES( null, '<xsl:value-of select="name"/>', '<xsl:value-of select="id"/>', '<xsl:value-of select="comments"/>', NOW, NOW, 1, false);
            </sql>

            <xsl:for-each select="maintainers/uid">
                <sql>
                    INSERT INTO groupmaintainers VALUES(null, 0, @@USER-<xsl:value-of select="."/>@@, 0);                
                </sql>
                <sql>
                    UPDATE groupmaintainers set "g_id" = (select "z_id" from groups where "g_id" = '<xsl:value-of select="../../id"/>') where "z_id" = IDENTITY();
                </sql>
            </xsl:for-each>
            
            <xsl:for-each select="maintainers/gid">
                <sql>
                    INSERT INTO groupmaintainers VALUES(null, 0, @@GROUP-<xsl:value-of select="."/>@@, 1);                
                </sql>
                <sql>
                    UPDATE groupmaintainers set "g_id" = (select "z_id" from groups where "g_id" = '<xsl:value-of select="../../id"/>') where "z_id" = IDENTITY();
                </sql>                
            </xsl:for-each>
            
            <xsl:for-each select="members/uid">
                <sql>
                    INSERT INTO groupmembers VALUES(null, 0, @@USER-<xsl:value-of select="."/>@@, 0);                
                </sql>
                <sql>
                    UPDATE groupmembers set "g_id" = (select "z_id" from groups where "g_id" = '<xsl:value-of select="../../id"/>') where "z_id" = IDENTITY();
                </sql>                 
            </xsl:for-each>
            
            <xsl:for-each select="members/gid">
                <sql>
                    INSERT INTO groupmembers VALUES(null, 0, @@GROUP-<xsl:value-of select="."/>@@, 1);                
                </sql>
                <sql>
                    UPDATE groupmembers set "g_id" = (select "z_id" from groups where "g_id" = '<xsl:value-of select="../../id"/>') where "z_id" = IDENTITY();
                </sql>                 
            </xsl:for-each> 
            
        </batch>
    </xsl:template>  
</xsl:stylesheet>
