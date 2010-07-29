<?xml version="1.0" encoding="UTF-8" ?>

<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:output encoding="UTF-8" indent="yes" method="html" />

	<xsl:template match="/">
		
		<html xmlns="http://www.w3.org/1999/xhtml" version="-//W3C//DTD XHTML 1.1//EN" xml:lang="en">
			
		<head>
			<meta http-equiv="Content-Type" content="application/xhtml+xml; charset=utf-8" />

		      <link href="http://zepheira.com/feeds/news.atom" type="application/atom+xml" rel="alternate" title="Zepheira News Feed"/>
		      <link href="http://zepheira.com/favicon.ico" rel="shortcut icon"/>
		      <!--[if gt IE 6]><!-->
		      <link media="screen, projection" href="http://zepheira.com/styles/import.css" type="text/css" rel="stylesheet"/>
		      <!--<![endif]-->
		      <!--[if IE 7]>
		      <link rel="stylesheet" type="text/css" href="http://zepheira.com/styles/ie7.css" media="screen, projection" />
		      <![endif]-->
		      <!--[if lt IE 7]>
		      <script defer type="text/javascript" src="http://zepheira.com/scripts/pngfix.js"></script>
		      <link rel="stylesheet" type="text/css" href="http://zepheira.com/styles/ie6.css" media="screen, projection" />
		      <![endif]-->        
		      <link rel="stylesheet" type="text/css" href="http://zepheira.com/styles/customers.css" media="screen, projection" />

			<meta http-equiv="author">
				<xsl:attribute name="content">
				  <xsl:value-of select="/document/definedby/name"/>
				</xsl:attribute>
			</meta>

			<title><xsl:value-of select="/document/name"/>: PURL 2 Project</title>

		</head>

		<body>

		 <a href="http://zepheira.com/"><img src="/images/zepheiralogo.png"
	align="right" border="0"/></a>

		<h1>PURL 2: <xsl:value-of select="/document/name"/></h1>

		<center>

		<xsl:if test="/document/depiction">

			<hr />
			<h2>Snapshot</h2>
			<p>
			<img>
				<xsl:attribute name="src">
				  <xsl:value-of select="/document/depiction/url"/>
				</xsl:attribute>
				<xsl:attribute name="alt">
				  <xsl:value-of select="/document/depiction/alt"/>
				</xsl:attribute>
				<xsl:attribute name="width">
				  <xsl:value-of select="/document/depiction/width"/>
				</xsl:attribute>
				<xsl:attribute name="height">
				  <xsl:value-of select="/document/depiction/height"/>
				</xsl:attribute>
			</img></p>

			<p> </p>
		</xsl:if>
			
			<hr />
			<h2>Requirement</h2>
			<p>
			<table border="1" width="80%">
				<tr>
					<td><b>Requirement name</b></td>
					<td><xsl:value-of select="/document/name"/></td>
				</tr>
				<tr>
					<td><b>Requirement reference</b></td>
					<td><xsl:value-of select="/document/reference"/></td>
				</tr>
			
			<xsl:if test="/document/url">
				<tr>
					<td><b>URL</b></td>
					<td>
						<a>
							<xsl:attribute name="href">
							  <xsl:value-of select="/document/url"/>
							</xsl:attribute>
							<xsl:value-of select="/document/url"/>
						</a>
					</td>
				</tr>
			</xsl:if>
			
			<xsl:if test="/document/formerurl">
				<tr>
					<td><b>Former URL</b></td>
					<td>
						<a>
							<xsl:attribute name="href">
							  <xsl:value-of select="/document/formerurl"/>
							</xsl:attribute>
							<xsl:value-of select="/document/formerurl"/>
						</a>
					</td>
				</tr>
			</xsl:if>
						
			<xsl:if test="/document/history">
				<tr>
					<td><b>History</b></td>
					<td><xsl:value-of select="/document/history"/></td>
				</tr>
			</xsl:if>
			
			<xsl:if test="/document/functionality">
				<tr>
					<td><b>Functionality</b></td>
					<td>
						<ul>
							<xsl:for-each select="/document/functionality/item">
								<li>
									<xsl:choose>
									  <xsl:when test="@rel='todo'">
									    <font color="red">TODO</font>: <xsl:value-of select="."/>
									  </xsl:when>    
									  <xsl:when test="@rel='new'">
									    <font color="green">NEW</font>: <xsl:value-of select="."/>
									  </xsl:when>
									  <xsl:otherwise>
									    <xsl:value-of select="."/>
									  </xsl:otherwise>
									</xsl:choose>
								</li>
							</xsl:for-each>
						</ul>
					</td>
				</tr>
			</xsl:if>
			
			<xsl:if test="/document/graphics">
				<tr>
					<td><b>Graphics</b></td>
					<td>
						<ul>
							<!-- TODO: Fix this! -->
							<xsl:for-each select="/document/graphics/item">
								<li>
									<a>
										<xsl:attribute name="href">
										  <xsl:value-of select="url"/>
										</xsl:attribute>
										<xsl:value-of select="label"/>
									</a>
									<xsl:if test="linksto">
										(links to 
										<a>
											<xsl:attribute name="href">
											  <xsl:value-of select="linksto"/>
											</xsl:attribute>
											<xsl:value-of select="linksto"/>
										</a>)
									</xsl:if>
								</li>
							</xsl:for-each>
						</ul>
					</td>
				</tr>
			</xsl:if>
			
			<xsl:if test="/document/assumptions">
				<tr>
					<td><b>Assumptions</b></td>
					<td><xsl:value-of select="/document/assumptions"/></td>
				</tr>
			</xsl:if>
			
			<xsl:if test="/document/preconditions">
				<tr>
					<td><b>Pre-conditions</b></td>
					<td><xsl:value-of select="/document/preconditions"/></td>
				</tr>
			</xsl:if>
			
			<xsl:if test="/document/postconditions">
				<tr>
					<td><b>Post-conditions</b></td>
					<td><xsl:value-of select="/document/postconditions"/></td>
				</tr>
			</xsl:if>
			
			<xsl:if test="/document/implementedby">
				<tr>
					<td><b>Implemented by</b></td>
					<td><ul>
						<xsl:for-each select="/document/implementedby/item">
							<li>
								<a>
									<xsl:attribute name="href">
									  <xsl:value-of select="."/>.html
									</xsl:attribute>
									<xsl:value-of select="."/>
								</a>
							</li>
						</xsl:for-each>
					</ul>
					</td>
				</tr>
			</xsl:if>
			
			<xsl:if test="/document/formerlyimplementedby">
				<tr>
					<td><b>Formerly Implemented by</b></td>
					<td><ul>
						<xsl:for-each select="/document/formerlyimplementedby/item">
							<li>
								<a>
									<xsl:attribute name="href">
									  <xsl:value-of select="."/>.html
									</xsl:attribute>
									<xsl:value-of select="."/>
								</a>
							</li>
						</xsl:for-each>
					</ul>
					</td>
				</tr>
			</xsl:if>
			
			<xsl:if test="/document/linksto">
				<tr>
					<td><b>Links to</b></td>
					<td>
						<xsl:if test="/document/linksto/parentof | /document/linksto/parentoflink">
								Parent of:<ul>
									<xsl:for-each select="/document/linksto/parentof">
										<li><xsl:value-of select="."/></li>
									</xsl:for-each>
									<xsl:for-each select="/document/linksto/parentoflink">
										<li>
											<a>
												<xsl:attribute name="href">
												  <xsl:value-of select="."/>.html
												</xsl:attribute>
												<xsl:value-of select="."/>
											</a>
										</li>
									</xsl:for-each>
								</ul>
						</xsl:if>
						<xsl:if test="/document/linksto/childof | /document/linksto/childoflink">
								Child of:<ul>
									<xsl:for-each select="/document/linksto/childof">
										<li><xsl:value-of select="."/></li>
									</xsl:for-each>
									<xsl:for-each select="/document/linksto/childoflink">
										<li>
											<a>
												<xsl:attribute name="href">
												  <xsl:value-of select="."/>.html
												</xsl:attribute>
												<xsl:value-of select="."/>
											</a>
										</li>
									</xsl:for-each>
								</ul>
						</xsl:if>
						<xsl:if test="/document/linksto/external | /document/linksto/externallink">
								External:<ul>
									<xsl:for-each select="/document/linksto/external">
										<li><xsl:value-of select="."/></li>
									</xsl:for-each>
									<xsl:for-each select="/document/linksto/externallink">
										<li>
											<a>
												<xsl:attribute name="href">
												  <xsl:value-of select="url"/>
												</xsl:attribute>
												<xsl:value-of select="description"/>
											</a>
										</li>
									</xsl:for-each>
								</ul>
						</xsl:if>
					</td>
				</tr>
			</xsl:if>
			
			<xsl:if test="/document/formerlylinkedto">
				<tr>
					<td><b>Formerly Linked to</b></td>
					<td>
						<xsl:if test="/document/formerlylinkedto/parentof | /document/formerlylinkedto/parentoflink">
								Parent of:<ul>
									<xsl:for-each select="/document/formerlylinkedto/parentof">
										<li><xsl:value-of select="."/></li>
									</xsl:for-each>
									<xsl:for-each select="/document/formerlylinkedto/parentoflink">
										<li>
											<a>
												<xsl:attribute name="href">
												  <xsl:value-of select="."/>.html
												</xsl:attribute>
												<xsl:value-of select="."/>
											</a>
										</li>
									</xsl:for-each>
								</ul>
						</xsl:if>
						<xsl:if test="/document/formerlylinkedto/childof | /document/formerlylinkedto/childoflink">
								Child of:<ul>
									<xsl:for-each select="/document/formerlylinkedto/childof">
										<li><xsl:value-of select="."/></li>
									</xsl:for-each>
									<xsl:for-each select="/document/formerlylinkedto/childoflink">
										<li>
											<a>
												<xsl:attribute name="href">
												  <xsl:value-of select="."/>.html
												</xsl:attribute>
												<xsl:value-of select="."/>
											</a>
										</li>
									</xsl:for-each>
								</ul>
						</xsl:if>
						<xsl:if test="/document/formerlylinkedto/external | /document/formerlylinkedto/externallink">
								External:<ul>
									<xsl:for-each select="/document/formerlylinkedto/external">
										<li><xsl:value-of select="."/></li>
									</xsl:for-each>
									<xsl:for-each select="/document/formerlylinkedto/externallink">
										<li>
											<a>
												<xsl:attribute name="href">
												  <xsl:value-of select="url"/>
												</xsl:attribute>
												<xsl:value-of select="description"/>
											</a>
										</li>
									</xsl:for-each>
								</ul>
						</xsl:if>
					</td>
				</tr>
			</xsl:if>
			
			<xsl:if test="/document/assignedto">
				<tr>
					<td><b>Assigned to</b></td>
					<td>
						<a>
							<xsl:attribute name="href">
						  	<xsl:value-of select="/document/assignedto/url"/>
							</xsl:attribute>
							<xsl:value-of select="/document/assignedto/name"/>
						</a>
					</td>
				</tr>
			</xsl:if>
			
			<xsl:if test="/document/status">
				<tr>
					<td><b>Status</b></td>
					<td><xsl:value-of select="/document/status"/></td>
				</tr>
			</xsl:if>
			
			</table>
			</p>

		</center>

		<hr />
		
		<p><a href="index.html">Site Index</a></p>

		<p><small>Author: 
			<a>
				<xsl:attribute name="href">
			  	<xsl:value-of select="/document/definedby/url"/>
				</xsl:attribute>
				<xsl:value-of select="/document/definedby/name"/>
			</a>, <a href="http://zepheira.com/">Zepheira</a></small><br /></p>

		<p><a rel="license" href="http://creativecommons.org/licenses/by-nd/3.0/">
			<img alt="Creative Commons Attribution-No Derivative Works 3.0 License" style="border-width:0" src="http://i.creativecommons.org/l/by-nd/3.0/88x31.png" />
			</a></p>

		</body>
		
	</html>
		
	</xsl:template>
	
</xsl:stylesheet>
