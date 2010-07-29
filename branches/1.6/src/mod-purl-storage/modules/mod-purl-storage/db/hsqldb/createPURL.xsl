<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:template match="/purl">
        <batch>
            <sql>
                INSERT INTO purls VALUES( null, '<xsl:value-of select="id"/>', '<xsl:value-of select="type"/>', '<xsl:value-of select="*/url"/>', NOW, NOW, 1, false);
            </sql>
            
            <sql>
                INSERT INTO purlhistory VALUES(null, IDENTITY(), @@CURRENTUSER@@, 0, '<xsl:value-of select="type"/>', '<xsl:value-of select="*/url"/>', NOW);
            </sql>
            
            <xsl:for-each select="maintainers/uid">
                <sql>
                    INSERT INTO purlmaintainers VALUES(null, 0, @@USER-<xsl:value-of select="."/>@@, 0);                
                </sql>
                <sql>
                    UPDATE purlmaintainers set "p_id" = (select "z_id" from purls where "p_id" = '<xsl:value-of select="../../id"/>') where "z_id" = IDENTITY();
                </sql>
            </xsl:for-each>
            
            <xsl:for-each select="maintainers/gid">
                <sql>
                    INSERT INTO purlmaintainers VALUES(null, 0, @@GROUP-<xsl:value-of select="."/>@@, 1);                
                </sql>
                <sql>
                    UPDATE purlmaintainers set "p_id" = (select "z_id" from purls where "p_id" = '<xsl:value-of select="../../id"/>') where "z_id" = IDENTITY();
                </sql>                
            </xsl:for-each>            
            
        </batch>
    </xsl:template>  
</xsl:stylesheet>
