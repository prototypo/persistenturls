<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

   <xsl:template match="/PLAY">
	     <xsl:copy-of select="ACT[1]"/>
    </xsl:template>

</xsl:stylesheet>

