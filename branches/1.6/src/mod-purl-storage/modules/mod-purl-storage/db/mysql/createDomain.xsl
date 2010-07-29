<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:template match="/">
        <batch>
            <xsl:apply-templates/>
        </batch>
    </xsl:template>
    
    <xsl:template match="domain">       
        <sql>
            INSERT INTO domains VALUES( null, '<xsl:value-of select="name"/>', '<xsl:value-of select="id"/>', <xsl:value-of select="public"/>, NOW(), NOW(), 0, false);
        </sql>
        
        <sql>
            set @did = LAST_INSERT_ID();
        </sql>
        
        <xsl:for-each select="maintainers/uid">
            <sql>
                INSERT INTO domainmaintainers VALUES(null, @did, @@USER-<xsl:value-of select="."/>@@, 0);                
            </sql>
        </xsl:for-each>
        <xsl:for-each select="maintainers/gid">
            <sql>
                INSERT INTO domainmaintainers VALUES(null, @did, @@GROUP-<xsl:value-of select="."/>@@, 1);                
            </sql>
        </xsl:for-each>
        
        <xsl:for-each select="writers/uid">
            <sql>
                INSERT INTO domainwriters VALUES(null, @did, @@USER-<xsl:value-of select="."/>@@, 0);                
            </sql>
        </xsl:for-each>
        <xsl:for-each select="writers/gid">
            <sql>
                INSERT INTO domainwriters VALUES(null, @did, @@GROUP-<xsl:value-of select="."/>@@, 1);                
            </sql>
        </xsl:for-each>
    </xsl:template>  
</xsl:stylesheet>
