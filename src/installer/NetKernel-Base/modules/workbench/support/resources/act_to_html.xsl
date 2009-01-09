<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
   <xsl:output method="html" omit-xml-declaration="yes"/>

   <xsl:template match="/ACT">
   <html>
	  <title>King Lear</title>
	  <head>
	  <link href="/xlib/styles/css_ten60_lnf.css" rel="stylesheet" type="text/css"/>
	  </head>
	  <body>
         <xsl:apply-templates />
	  </body>
   </html>
   </xsl:template>

   <xsl:template match="SCENE">

      <xsl:apply-templates />
   </xsl:template>

   <xsl:template match="TITLE">
      <H1>
                  <xsl:apply-templates />

      </H1>
      <br/>
   </xsl:template>

   <xsl:template match="STAGEDIR">
      <H3>
		<i>
                  <xsl:apply-templates />
		</i>
      </H3>
      
   </xsl:template>

   <xsl:template match="SPEECH">
      <P>
         <xsl:apply-templates />
      </P>
	  <br />
   </xsl:template>

   <xsl:template match="SPEAKER">
      <h2>
         <xsl:apply-templates />
      </h2>
   </xsl:template>

   <xsl:template match="LINE">
      <xsl:apply-templates />
      <br/>
   </xsl:template>
</xsl:stylesheet>

