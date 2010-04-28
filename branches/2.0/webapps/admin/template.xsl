<?xml version="1.0" encoding="UTF-8" ?>
	<!-- Copyright (c) 2010 Zepheira LLC, Some Rights Reserved. -->
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:param name="mode" />
	<xsl:variable name="section" select="/html/body/@class" />
	<xsl:template match="*">
		<xsl:copy>
			<xsl:apply-templates select="@*|*|comment()|text()" />
		</xsl:copy>
	</xsl:template>
	<xsl:template match="@*">
		<xsl:attribute name="{name()}">
			<xsl:value-of select="." />
		</xsl:attribute>
	</xsl:template>
	<xsl:template match="comment()">
		<xsl:copy />
	</xsl:template>
	<xsl:template match="head">
		<xsl:copy>
			<xsl:apply-templates select="@*|*|text()|comment()" />
			<style type="text/css">@import url("/docs/style.css")</style>
			<script type="text/javascript" src="/callimachus/jquery-1.3.2.js">
			</script>
			<script type="text/javascript" src="/callimachus/jquery-ui-1.7.2.js">
			</script>
			<script type="text/javascript" src="/callimachus/jquery.qtip-1.0.0-rc3.js">
			</script>
			<script type="text/javascript" src="/callimachus/jquery.rdfquery.rdfa-1.0.js">
			</script>
			<script type="text/javascript" src="/callimachus/profile-message.js">
			</script>
			<script type="text/javascript" src="/callimachus/profile-elements.js">
			</script>
			<xsl:if test="contains($mode, 'copy')">
				<script type="text/javascript" src="/callimachus/profile-copy.js">
				</script>
			</xsl:if>
			<xsl:if test="(contains($mode, 'copy') or contains($mode, 'edit')) and $section='user'">
				<script type="text/javascript" src="/callimachus/md5.js">
				</script>
			</xsl:if>
			<xsl:if test="contains($mode, 'edit')">
				<script type="text/javascript" src="/callimachus/profile-edit.js">
				</script>
			</xsl:if>
			<xsl:if test="contains($mode, 'delete')">
				<script type="text/javascript" src="/callimachus/profile-delete.js">
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
		</xsl:copy>
	</xsl:template>
	<xsl:template match="body">
		<xsl:copy>
			<xsl:apply-templates select="@*" />
			<xsl:if test="not(starts-with($mode, 'pre-'))">
			<div id="loginstatus">
				<p>Login status not determined. Do you have Javascript and cookies
					enabled?</p>
			</div>
			<div id="header">
				<h1>PURL User Administration</h1>
				<ul id="tabmenu">
					<li>
						<a>
							<xsl:attribute name="class">
								<xsl:choose>
									<xsl:when test="$section='home'">
										active
									</xsl:when>
									<xsl:otherwise>
										inactive
									</xsl:otherwise>
								</xsl:choose>
							</xsl:attribute>
							<xsl:attribute name="href">/docs/index.html</xsl:attribute>
							<xsl:text>Home</xsl:text>
						</a>
					</li>
					<li>
						<a>
							<xsl:attribute name="class">
								<xsl:choose>
									<xsl:when test="$section='purl'">
										active
									</xsl:when>
									<xsl:otherwise>
										inactive
									</xsl:otherwise>
								</xsl:choose>
							</xsl:attribute>
							<xsl:attribute name="href">/docs/purls.html</xsl:attribute>
							<xsl:text>PURLs</xsl:text>
						</a>
					</li>
					<li>
						<a>
							<xsl:attribute name="class">
								<xsl:choose>
									<xsl:when test="$section='user'">
										active
									</xsl:when>
									<xsl:otherwise>
										inactive
									</xsl:otherwise>
								</xsl:choose>
							</xsl:attribute>
							<xsl:attribute name="href">/docs/user.html</xsl:attribute>
							<xsl:text>Users</xsl:text>
						</a>
					</li>
					<li>
						<a>
							<xsl:attribute name="class">
								<xsl:choose>
									<xsl:when test="$section='group'">
										active
									</xsl:when>
									<xsl:otherwise>
										inactive
									</xsl:otherwise>
								</xsl:choose>
							</xsl:attribute>
							<xsl:attribute name="href">/docs/group.html</xsl:attribute>
							<xsl:text>Groups</xsl:text>
						</a>
					</li>
					<li>
						<a>
							<xsl:attribute name="class">
								<xsl:choose>
									<xsl:when test="$section='domain'">
										active
									</xsl:when>
									<xsl:otherwise>
										inactive
									</xsl:otherwise>
								</xsl:choose>
							</xsl:attribute>
							<xsl:attribute name="href">/docs/domain.html</xsl:attribute>
							<xsl:text>Domains</xsl:text>
						</a>
					</li>
					<li>
						<a class="inactive" href="/docs/pending/index.html">Admin</a>
					</li>
					<li>
						<a class="inactive" href="/docs/help.html">Help</a>
					</li>
				</ul>
			</div>
			</xsl:if>
			<div id="content">
				<div id="">
					<a id="message" />
				</div>
				<xsl:apply-templates select="*|comment()|text()" />
			</div>

			<xsl:if test="not(starts-with($mode, 'pre-'))">
			<div id="footer">
				<table width="100%" summary="supporters">
					<tr>
						<td>
							<a href="http://www.oclc.org/">
								<img align="left" src="/docs/images/oclclogo.png" alt="oclc" />
							</a>
						</td>
						<td>
							<a href="http://zepheira.com/">
								<img align="right" src="/docs/images/zepheiralogo.png" alt="zepheira" />
							</a>
						</td>
					</tr>
				</table>
			</div>
			</xsl:if>
		</xsl:copy>
	</xsl:template>
</xsl:stylesheet>
