<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:param name="xslt" select="'/persistent/template.xsl'" />
	<xsl:param name="mode" />
	<xsl:variable name="persistent" select="substring-before($xslt, '/template.xsl')" />
	<xsl:variable name="origin" select="substring-before($persistent, '/persistent')" />
	<xsl:variable name="callimachus" select="concat($origin, '/callimachus')" />
	<xsl:variable name="server" select="concat($persistent, '/server')" />
	<xsl:variable name="user" select="concat($persistent, '/user')" />
	<xsl:variable name="images" select="concat($persistent, '/images')" />
	<xsl:variable name="section" select="/html/body/@class" />
	<xsl:template match="*">
		<xsl:copy>
			<xsl:apply-templates select="@*|*|comment()|text()" />
		</xsl:copy>
	</xsl:template>
	<xsl:template match="@*">
		<xsl:attribute name="{name()}">
			<xsl:choose>
				<xsl:when test="starts-with(., '/persistent/')">
					<xsl:value-of select="$persistent"/>
					<xsl:value-of select="substring-after(., '/persistent')" />
				</xsl:when>
				<xsl:when test="starts-with(., '/')">
					<xsl:value-of select="$origin"/>
					<xsl:value-of select="." />
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="." />
				</xsl:otherwise>
			</xsl:choose>
		</xsl:attribute>
	</xsl:template>
	<xsl:template match="comment()">
		<xsl:copy />
	</xsl:template>
	<xsl:template match="head">
		<xsl:copy>
			<xsl:apply-templates select="@*" />
			<meta http-equiv="X-UA-Compatible" content="IE=8" />
			<link rel="stylesheet" href="{$persistent}/style.css" />
			<script type="text/javascript" src="{$callimachus}/jquery.js">
			</script>
			<script type="text/javascript" src="{$callimachus}/jquery-ui.js">
			</script>
			<script type="text/javascript" src="{$callimachus}/jquery.qtip.js">
			</script>
			<script type="text/javascript" src="{$callimachus}/jquery.rdfquery.rdfa.js">
			</script>
			<script type="text/javascript" src="{$callimachus}/status.js">
			</script>
			<script type="text/javascript" src="{$callimachus}/elements.js">
			</script>
			<script type="text/javascript" src="{$callimachus}/diverted.js">
			</script>
			<script type="text/javascript" src="{$persistent}/base.js">
			</script>
			<script type="text/javascript" src="{$persistent}/ui.js">
			</script>
			<xsl:if test="contains($mode, 'copy')">
				<script type="text/javascript" src="{$callimachus}/copy.js">
				</script>
			</xsl:if>
			<xsl:if test="contains($mode, 'edit')">
				<script type="text/javascript" src="{$callimachus}/edit.js">
				</script>
			</xsl:if>
			<xsl:if test="contains($mode, 'delete')">
				<script type="text/javascript" src="{$callimachus}/delete.js">
				</script>
				<script type="text/javascript" src="{$persistent}/delete.js">
				</script>
			</xsl:if>
			<xsl:if test="starts-with($mode, 'pre-')">
				<script>
					// <![CDATA[
					onload = function() {
						$("a[href]").each(function() {
							var href = this.href
							var q = href.indexOf('?')
							if (q >= 0 && href.substring(q) == "?edit") {
								this.href = href.substring(0, q) + "?pre-edit"
							} else if (q >= 0 && href.substring(q) == "?view") {
								this.href = href.substring(0, q) + "?pre-view"
							} else if (q < 0) {
								this.href = href + "?pre-view"
							} else {
								$(this).attr("target", "_blank")
							}
						})
					}
					// ]]>
				</script>
			</xsl:if>
			<xsl:apply-templates select="*|text()|comment()" />
		</xsl:copy>
	</xsl:template>
	<xsl:template match="body">
		<xsl:copy>
			<xsl:apply-templates select="@*" />
			<xsl:if test="not(starts-with($mode, 'pre-'))">
				<div id="home">
					<a href="{$origin}/">
						<img src="{$images}/web-color-sm.png" alt="PURLz logo"/>
					</a>
				</div>
				<div id="header">
					<ul id="actions" class="{$mode}">
						<xsl:for-each select="/html/head/link[@target='_self']">
							<xsl:choose>
								<xsl:when test="@href=''">
									<li>
										<span class="current"><xsl:value-of select="@title" /></span>
									</li>
								</xsl:when>
								<xsl:when test="@href">
									<li>
										<a class="diverted" target="_self">
											<xsl:apply-templates select="@href"/>
											<xsl:value-of select="@title" />
										</a>
									</li>
								</xsl:when>
							</xsl:choose>
						</xsl:for-each>
					</ul>
					<div id="account">
						<strong><a id="username" href="{$persistent}/authority?credential"></a></strong>
						<a id="login" href="{$persistent}/authority?login">Login</a>
						<span class="logout">&#160;&#183;&#160;</span>
						<a class="logout" href="{$persistent}/authority?logout">Logout</a>
					</div>
					<div class="clear">&#160;</div>
				</div>

				<ul id="menu">
					<li>
						<xsl:attribute name="class">
							<xsl:choose>
								<xsl:when test="contains($section, 'purl')">
									<xsl:text>active</xsl:text>
								</xsl:when>
								<xsl:otherwise>
									<xsl:text>inactive</xsl:text>
								</xsl:otherwise>
							</xsl:choose>
						</xsl:attribute>
						<a href="{$origin}/?purl">PURLs</a>
						<form action="{$origin}/" method="get" title="Search PURL maintainers and curators" class="search" onsubmit="if(elements['q'].value.indexOf(':')>0)elements['purl'].name='target'">
							<input name="purl" type="hidden" class="profile" />
							<input id="purl_menu_q" name="q" type="text" value="" />
							<img class="submit" src="{$persistent}/images/search-button.png" alt="Search" title="Click to search" />
						</form>
					</li>
					<li>
						<xsl:attribute name="class">
							<xsl:choose>
								<xsl:when test="contains($section, 'domain')">
									<xsl:text>active</xsl:text>
								</xsl:when>
								<xsl:otherwise>
									<xsl:text>inactive</xsl:text>
								</xsl:otherwise>
							</xsl:choose>
						</xsl:attribute>
						<a href="{$origin}/?domain">Domains</a>
						<form action="{$origin}/" method="get" title="Search maintainers and curators" class="search">
							<input name="domain" type="hidden" class="profile" />
							<input id="domain_form_q" name="q" type="text" />
							<img class="submit" src="{$persistent}/images/search-button.png" alt="Search" title="Click to search" />
						</form>
					</li>
					<li>
						<xsl:attribute name="class">
							<xsl:choose>
								<xsl:when test="contains($section, 'user')">
									<xsl:text>active</xsl:text>
								</xsl:when>
								<xsl:otherwise>
									<xsl:text>inactive</xsl:text>
								</xsl:otherwise>
							</xsl:choose>
						</xsl:attribute>
						<a href="{$origin}/?user">Users</a>
						<form action="{$origin}/" method="get" title="Search usernames" class="search">
							<input name="user" type="hidden" class="profile" />
							<input id="user_menu_q" name="q" type="text" />
							<img class="submit" src="{$persistent}/images/search-button.png" alt="Search" title="Click to search" />
						</form>
					</li>
					<xsl:for-each select="/html/head/link[@target!='_self']">
						<li class="inactive">
							<a target="{@target}" onclick="var win=open(this.href, '{@target}', 'scrollbars=yes, resizable=yes, width=550, height=550, dialog=yes, alwaysRaised=yes, dependent=yes');win.focus()" >
								<xsl:apply-templates select="@href"/>
								<xsl:value-of select="@title" />
								</a>
						</li>
					</xsl:for-each>
				</ul>
			</xsl:if>

			<div id="content">
				<div id="breadcrumbs">
					<a href="{$origin}/">Server</a>
					<xsl:choose>
						<xsl:when test="contains($section, 'user')">
						<span><span class="crumb">Users</span></span>
						</xsl:when>
						<xsl:when test="contains($section, 'search')">
						<span><span class="crumb">Search</span></span>
						</xsl:when>
						<xsl:when test="contains($section, 'purl') or contains($section, 'domain')">
						<span>
						<span rel="purl:partOf" resource="?bc_partial">
						<span rel="purl:partOf" resource="?bc_subsubdomain">
						<span rel="purl:partOf" resource="?bc_subdomain">
						<span rel="purl:partOf" resource="?bc_domain">
						<span rel="purl:partOf" resource="?bc_origin">
						<a href="?bc_origin" class="crumb diverted" property="rdfs:label"/>
						</span>
						<a href="?bc_domain" class="crumb diverted" property="rdfs:label"/>
						</span>
						<a href="?bc_subdomain" class="crumb diverted" property="rdfs:label"/>
						</span>
						<a href="?bc_subsubdomain" class="crumb diverted" property="rdfs:label"/>
						</span>
						<a href="?bc_partial" class="crumb diverted" property="rdfs:label"/>
						</span>
						<span class="crumb" property="rdfs:label"/>
						</span>
						</xsl:when>
					</xsl:choose>
				</div>
				<div id="message-container">
					<p id="message" />
				</div>
				<xsl:apply-templates select="*|comment()|text()" />
				<div class="clear">&#160;</div>
			</div>

			<xsl:if test="not(starts-with($mode, 'pre-'))">
				<div class="clear">&#160;</div>
				<div id="footer">
					<a href="http://zepheira.com/" title="Zepheira">
						<img src="{$images}/zepheiralogo.png" alt="Zepheira logo" />
					</a>
				</div>
			</xsl:if>
		</xsl:copy>
	</xsl:template>
</xsl:stylesheet>
