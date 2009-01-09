<?xml version="1.0" encoding="UTF-8" ?>

<!--
    Document   : myfirst_xslt.xsl
    Created on : 18 March 2003, 08:15
    Author     : pjr
    Description:
        Purpose of transformation follows.
-->

<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:template match="/data">
    <data>
      <xsl:apply-templates select="item"/>
    </data>
  </xsl:template>

  <xsl:template match="item">
    <xsl:if test="@name='red'">
      <xsl:copy-of select="."/>
    </xsl:if>
  </xsl:template>

</xsl:stylesheet> 
