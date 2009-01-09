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
  <xsl:output method="html"/>

  <xsl:template match="/data">
    <html>
      <body bgcolor="white">
        <table>
          <tr>
            <xsl:apply-templates select="item"/>
          </tr>
        </table>
      </body>
    </html>
  </xsl:template>

  <xsl:template match="item">
    <td width="100" height="100">
      <xsl:attribute name="bgcolor">
        <xsl:value-of select="colour"/>
      </xsl:attribute>
    </td>
  </xsl:template>

</xsl:stylesheet> 
