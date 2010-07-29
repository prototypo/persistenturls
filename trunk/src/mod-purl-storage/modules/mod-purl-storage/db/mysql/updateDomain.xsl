<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

    <xsl:template match="/">
        <batch>
            <xsl:apply-templates/>
        </batch>
    </xsl:template>
    
    <xsl:template match="domain">       
        <sql>
			UPDATE domains SET 
				name = '<xsl:value-of select="name"/>',
				public = <xsl:value-of select="public"/>,
				lastmodified = NOW(),
				indexed = 0  
			WHERE d_id = '<xsl:value-of select="id"/>';        	
        </sql>
        
        <sql>
        	DELETE FROM domainmaintainers WHERE d_id = '@@DID@@';
        </sql>
        
        <sql>
        	DELETE FROM domainwriters WHERE d_id = '@@DID@@';
        </sql>
        
        <xsl:for-each select="maintainers/uid">
            <sql>
                INSERT INTO domainmaintainers VALUES(null, @@DID@@, @@USER-<xsl:value-of select="."/>@@, 0);                
            </sql>
        </xsl:for-each>
        
        <xsl:for-each select="maintainers/gid">
            <sql>
                INSERT INTO domainmaintainers VALUES(null, @@DID@@, @@GROUP-<xsl:value-of select="."/>@@, 1);                
            </sql>
        </xsl:for-each>
        
        <xsl:for-each select="writers/uid">
            <sql>
                INSERT INTO domainwriters VALUES(null, @@DID@@, @@USER-<xsl:value-of select="."/>@@, 0);                
            </sql>
        </xsl:for-each>
        
        <xsl:for-each select="writers/gid">
            <sql>
                INSERT INTO domainwriters VALUES(null, @@DID@@, @@GROUP-<xsl:value-of select="."/>@@, 1);                
            </sql>
        </xsl:for-each>
    </xsl:template>  
</xsl:stylesheet>