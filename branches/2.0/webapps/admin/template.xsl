<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:param name="xslt" />
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
			<style type="text/css">@import url("<xsl:value-of select="$xslt"/>/../../docs/style.css");</style>
			<script type="text/javascript" src="{$xslt}/../../callimachus/jquery-1.3.2.js">
			</script>
			<script type="text/javascript" src="{$xslt}/../../callimachus/jquery-ui-1.7.2.js">
			</script>
			<script type="text/javascript" src="{$xslt}/../../callimachus/jquery.qtip-1.0.0-rc3.js">
			</script>
			<script type="text/javascript" src="{$xslt}/../../callimachus/jquery.rdfquery.rdfa-1.0.js">
			</script>
			<script type="text/javascript" src="{$xslt}/../../callimachus/resource-log.js">
			</script>
			<script type="text/javascript" src="{$xslt}/../../callimachus/resource-elements.js">
			</script>
			<script type="text/javascript" src="{$xslt}/../../callimachus/diverted.js">
			</script>
			<script type="text/javascript" src="{$xslt}/../purl-ui.js">
			</script>
			<xsl:if test="contains($mode, 'copy')">
				<script type="text/javascript" src="{$xslt}/../../callimachus/resource-copy.js">
				</script>
			</xsl:if>
			<xsl:if test="contains($mode, 'edit')">
				<script type="text/javascript" src="{$xslt}/../../callimachus/resource-edit.js">
				</script>
			</xsl:if>
			<xsl:if test="contains($mode, 'delete')">
				<script type="text/javascript" src="{$xslt}/../../callimachus/resource-delete.js">
				</script>
			</xsl:if>
			<xsl:if test="(contains($mode, 'copy') or contains($mode, 'edit')) and $section='purl'">
				<script type="text/javascript" src="{$xslt}/../purl-form.js">
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
				<div id="header">
					<ul id="actions" class="{$mode}">
						<li class="view">
							<xsl:choose>
								<xsl:when test="contains($mode, 'view')">
									<span class="current">View</span>
								</xsl:when>
								<xsl:otherwise>
									<a class="diverted" href="?view">View</a>
								</xsl:otherwise>
							</xsl:choose>
						</li>
						<li class="edit">
							<xsl:choose>
								<xsl:when test="contains($mode, 'edit')">
									<span class="current">Edit</span>
								</xsl:when>
								<xsl:otherwise>
									<a class="diverted" href="?edit">Edit</a>
								</xsl:otherwise>
							</xsl:choose>
						</li>
						<li class="delete">
							<xsl:choose>
								<xsl:when test="contains($mode, 'delete')">
									<span class="current">Delete</span>
								</xsl:when>
								<xsl:otherwise>
									<a class="diverted" href="?delete">Delete</a>
								</xsl:otherwise>
							</xsl:choose>
						</li>
					</ul>
					<div class="clear">&#160;</div>
				</div>

				<ul id="menu">
					<li>
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
						<a href="/docs/purls.html">PURLs</a>
						<form action="/?purl" method="get" title="Search PURL maintainers and curators" class="search">
							<label></label> <input name="q" type="text" value="" />
						</form>
					</li>
					<li>
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
						<a href="/docs/user.html">Users</a>
						<form action="/?user" method="get" title="Search usernames" class="search">
							<label></label> <input name="q" type="text" />
						</form>
						<xsl:if test="$section='user'">
							<ul>
								<li><a class="diverted new" href="/admin/user/template?copy">Create New User</a></li>
							</ul>
						</xsl:if>
					</li>
					<li>
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
						<a href="/docs/group.html">Groups</a>
						<form action="/?group" method="get" title="Search group names" class="search">
							<label></label> <input name="q" type="text" />
						</form>
						<xsl:if test="$section='group'">
							<ul>
								<li><a class="diverted new" href="/admin/group/template?copy">Create New Group</a></li>
							</ul>
						</xsl:if>
					</li>
					<li>
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
						<a href="/docs/domain.html">Domains</a>
						<form action="/?domain" method="get" title="Search maintainers and curators" class="search">
							<label></label> <input name="q" type="text" />	
						</form>
					</li>
					<li class="inactive">
						<a href="/docs/pending/index.html">Admin</a>
					</li>
					<li class="inactive">
						<a href="/docs/help.html">Help</a>
					</li>
				</ul>
			</xsl:if>

			<div id="content">
				<ul id="breadcrumbs">
					<li class="home"><a href="/">Home</a></li>
					<xsl:choose>
						<xsl:when test="$section='user'">
							<li><a href="/">Users</a></li>
						</xsl:when>
						<xsl:when test="$section='group'">
							<li><a href="/">Groups</a></li>
						</xsl:when>
					</xsl:choose>
				</ul>
				<div class="clear">&#160;</div>
				<div id="message-container">
					<a id="message" />
				</div>
				<xsl:apply-templates select="*|comment()|text()" />
				<div class="clear">&#160;</div>
			</div>

			<xsl:if test="not(starts-with($mode, 'pre-'))">
				<div class="clear">&#160;</div>
				<div id="footer">
					<a href="http://www.oclc.org/" title="OCLC">
						<img src="{$xslt}/../../docs/images/oclclogo.png" alt="OCLC logo" />
					</a>
					<a href="http://zepheira.com/" title="Zepheira">
						<img src="{$xslt}/../../docs/images/zepheiralogo.png" alt="Zepheira logo" />
					</a>
				</div>
			</xsl:if>
		</xsl:copy>
	</xsl:template>
</xsl:stylesheet>
