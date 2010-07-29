<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:template match="/">
        <batch>
            <xsl:apply-templates/>
        </batch>
    </xsl:template>
    
    <xsl:template match="domain">       
        <sql>
            INSERT INTO domains VALUES( null, '<xsl:value-of select="name"/>', '<xsl:value-of select="id"/>', <xsl:value-of select="public"/>, NOW, NOW, 0, false);
        </sql>
        
        <xsl:for-each select="maintainers/uid">
            <sql>
                INSERT INTO domainmaintainers VALUES(null, 0, @@USER-<xsl:value-of select="."/>@@, 0);                
            </sql>
            <sql>
            	UPDATE domainmaintainers set "d_id" = (select "z_id" from domains where "d_id" = '<xsl:value-of select="../../id"/>') where "z_id" = IDENTITY();
            </sql>
        </xsl:for-each>
        <xsl:for-each select="maintainers/gid">
            <sql>
                INSERT INTO domainmaintainers VALUES(null, 0, @@GROUP-<xsl:value-of select="."/>@@, 1);                
            </sql>
            <sql>
            	UPDATE domainmaintainers set "d_id" = (select "z_id" from domains where "d_id" = '<xsl:value-of select="../../id"/>') where "z_id" = IDENTITY();
            </sql>
        </xsl:for-each>
        
        <xsl:for-each select="writers/uid">
            <sql>
                INSERT INTO domainwriters VALUES(null, 0, @@USER-<xsl:value-of select="."/>@@, 0);                
            </sql>
            <sql>
            	UPDATE domainwriters set "d_id" = (select "z_id" from domains where "d_id" = '<xsl:value-of select="../../id"/>') where "z_id" = IDENTITY();
            </sql>
        </xsl:for-each>
        <xsl:for-each select="writers/gid">
            <sql>
                INSERT INTO domainwriters VALUES(null, 0, @@GROUP-<xsl:value-of select="."/>@@, 1);                
            </sql>
            <sql>
            	UPDATE domainwriters set "d_id" = (select "z_id" from domains where "d_id" = '<xsl:value-of select="../../id"/>') where "z_id" = IDENTITY();
            </sql>            
        </xsl:for-each>
    </xsl:template>  
</xsl:stylesheet>
