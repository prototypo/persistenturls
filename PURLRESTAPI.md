# PURL REST API for version 1.0-1.6.x servers #

This document describes Uniform Resource Locators (URLs) and HTTP verbs for each public-facing requirement.  See the list of requirements given in the subversion checkout of the code for more detailed information.

## Account Creation and Maintenance ##

| **Requirement** | **Public URL**| **HTTP Verb** | **Parameters** | **Auth required** |
|:----------------|:|:--------------|:---------------|:------------------|
| Log in an existing user | $PURL\_SERVER$/admin/login/login-submit.bsh | POST          | Username (id)<br>Password (passwd)<br>Optional referrer page (referrer, defaults to '/docs/index.html') <table><thead><th> no                </th></thead><tbody>
<tr><td> Register a new user </td><td> $PURL_SERVER$/admin/user/<code>&lt;id&gt;</code> </td><td> POST          </td><td> Full name (name)<br>Affiliation (affiliation)<br>E-mail address (email)<br>Password (passwd)<br>Hint (hint)<br>Justification (justification) </td><td> no                </td></tr>
<tr><td> Modify user information </td><td> $PURL_SERVER$/admin/user/<code>&lt;id&gt;</code> </td><td> PUT           </td><td> Full name (name)<br>Affiliation (affiliation)<br>E-mail address (email)<br>Password (passwd)<br>Hint (hint)<br>Justification (justification) </td><td> yes               </td></tr>
<tr><td> Search user information </td><td> $PURL_SERVER$/admin/user/ or $PURL_SERVER$/admin/user/<code>&lt;id&gt;</code> </td><td> GET           </td><td> Full name (name) or<br>Affiliation (affiliation)<br>E-mail address (email)<br>User ID (id)<br>Search tombstoned (tombstone) </td><td> no                </td></tr>
<tr><td> Delete a user   </td><td> $PURL_SERVER$/admin/user/<code>&lt;id&gt;</code> </td><td> DELETE        </td><td>                </td><td> yes               </td></tr></tbody></table>


<h2>PURL Creation and Maintenance</h2>

<table><thead><th> <b>Requirement</b> </th><th> <b>Public URL</b></th><th> <b>HTTP Verb</b> </th><th> <b>Parameters</b> </th><th> <b>Auth required</b> </th></thead><tbody>
<tr><td> Create a PURL<br>Create a partial redirect PURL<br>Chain a PURL<br>Clone a PURL </td><td> $PURL_SERVER$/admin/purl/<code>&lt;id&gt;</code> </td><td> POST             </td><td> Target URL (target)<br>User/Group IDs (maintainers)<br>Type (type)<br>See Also URLs (seealso) </td><td> yes                  </td></tr>
<tr><td> Populate the creation form for a simple (302) PURL from a Javascript bookmarklet </td><td> $PURL_SERVER$/docs/simplepurl.html </td><td> GET              </td><td> URL-encoded Target URL (referrer) </td><td> yes                  </td></tr>
<tr><td> Modify a PURL      </td><td> $PURL_SERVER$/admin/purl/<code>&lt;id&gt;</code> </td><td> PUT              </td><td> Target URL (target)<br>User/Group IDs (maintainers)<br>Type (type)<br>See Also URLs (seealso) </td><td> yes                  </td></tr>
<tr><td> Search PURL metadata </td><td> $PURL_SERVER$/admin/purl/ or $PURL_SERVER$/admin/purl/<code>&lt;id&gt;</code> </td><td> GET              </td><td> Target URL (target) or<br>See Also URLs (seealso) or<br>User/Group IDs (maintainers) or<br>Explicit User/Group ID (explicitmaintainers)<br>Search tombstoned (tombstone) </td><td> no                   </td></tr>
<tr><td> Delete PURLs       </td><td> $PURL_SERVER$/admin/purl/<code>&lt;id&gt;</code> </td><td> DELETE           </td><td>                   </td><td> yes                  </td></tr>
<tr><td> Batch add PURLs    </td><td> $PURL_SERVER$/admin/purls </td><td> POST             </td><td> XML (see <a href='PURLBatchUploadingVersionOne.md'>PURL batch documentation</a>) </td><td> yes                  </td></tr>
<tr><td> PURL Validation    </td><td> $PURL_SERVER$/admin/targeturl/<code>&lt;id&gt;</code> </td><td> GET              </td><td>                   </td><td> no                   </td></tr>
<tr><td> Batch PURL Validation </td><td> $PURL_SERVER$/admin/targeturls<br />NB: Not implemented in PURLz v1.x </td><td> POST             </td><td> XML (see <a href='PURLBatchUploadingVersionOne.md'>PURL batch documentation</a>) </td><td> no                   </td></tr></tbody></table>

<h2>Group Creation and Maintenance</h2>

<table><thead><th> <b>Requirement</b> </th><th> <b>Public URL</b></th><th> <b>HTTP Verb</b> </th><th> <b>Parameters</b> </th><th> <b>Auth required</b> </th></thead><tbody>
<tr><td> Create a new group </td><td> $PURL_SERVER$/admin/group/<code>&lt;id&gt;</code> </td><td> POST             </td><td> Group name (name)<br>Group maintainers (maintainers)<br>Group members (members)<br>Public comments (comments) </td><td> yes                  </td></tr>
<tr><td> Modify group information </td><td> $PURL_SERVER$/admin/group/<code>&lt;id&gt;</code> </td><td> PUT              </td><td> Group name (name)<br>Group maintainers (maintainers)<br>Group members (members)<br>Public comments (comments) </td><td> yes                  </td></tr>
<tr><td> Search group information </td><td> $PURL_SERVER$/admin/group/ or $PURL_SERVER$/admin/group/<code>&lt;id&gt;</code> </td><td> GET              </td><td> Group name (name) or<br>Group maintainers (maintainers) or<br>Group members (members)<br>Search tombstoned (tombstone) </td><td> no                   </td></tr>
<tr><td> Delete a group     </td><td> $PURL_SERVER$/admin/group/<code>&lt;id&gt;</code> </td><td> DELETE           </td><td>                   </td><td> yes                  </td></tr></tbody></table>

<h2>Domain Creation and Maintenance</h2>

<table><thead><th> <b>Requirement</b> </th><th> <b>Public URL</b></th><th> <b>HTTP Verb</b> </th><th> <b>Parameters</b> </th><th> <b>Auth required</b> </th></thead><tbody>
<tr><td> Request a top-level domain<br>Create a subdomain in an existing domain </td><td> $PURL_SERVER$/admin/domain/<code>&lt;id&gt;</code> </td><td> POST             </td><td> Domain name (name)<br>Domain maintainers (maintainers)<br>Domain writers (writers)<br>Public (public) </td><td> yes                  </td></tr>
<tr><td> Modify existing domain information </td><td> $PURL_SERVER$/admin/domain/<code>&lt;id&gt;</code> </td><td> PUT              </td><td> Domain name (name)<br>Domain maintainers (maintainers)<br>Domain writers (writers)<br>Public (public) </td><td> yes                  </td></tr>
<tr><td> Search domain information </td><td> $PURL_SERVER$/admin/domain/ or $PURL_SERVER$/admin/domain/<code>&lt;id&gt;</code> </td><td> GET              </td><td> Domain name (name) or<br>Domain maintainer (maintainers) or<br>Domain writer (writers)<br>Search tombstoned (tombstone) </td><td> no                   </td></tr>
<tr><td> Delete a domain    </td><td> $PURL_SERVER$/admin/domain/<code>&lt;id&gt;</code> </td><td> DELETE           </td><td>                   </td><td> yes                  </td></tr>