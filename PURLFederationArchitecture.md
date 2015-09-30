# PURL FEDERATION ARCHITECTURE #

Version 10 February 2012
Original Version 12 March 2010

## LICENSE ##

<a href='http://creativecommons.org/licenses/by-nc-nd/3.0/us/'><img src='http://i.creativecommons.org/l/by-nc-nd/3.0/us/88x31.png' alt='Creative Commons License' /></a><br />PURL Federation Architecture by <a href='http://3roundstones.com'>3 Round Stones</a> and <a href='http://zepheira.com'>Zepheira</a> is licensed under a <a href='http://creativecommons.org/licenses/by-nc-nd/3.0/us/'>Creative Commons Attribution-Noncommercial-No Derivative Works 3.0 United States License</a> and also under under a <a href='http://creativecommons.org/licenses/by-nc-nd/2.5/au/'>Creative Commons Attribution-Noncommercial-No Derivative Works 2.5 Australia License</a>.


## OVERVIEW ##

Architecture for the development of a PURL federation is proposed in order to meet the goals outlined in the section GOALS.  Presumptions made in the architectural scenarios are provided in the section PRESUMPTIONS.

An architectural scenario is presented in which dynamic DNS resolution is used to allow PURLs to be resolved by proxies to a federation.  A third party service is required to both operate the dynamic DNS resolution service and to monitor (in near real time) the availability of PURL members.  The functions of and requirements for that service are discussed along with the architectural scenarios.

Changes to the PURL server software and anticipated costs are discussed following the architecture.


## GOALS ##

1.  PURLs are both URIs and URLs and must be treated as such.  Each PURL must be addressable solely by its URL and yet be resolvable by any proxy.  We recognize that some PURL service operators have spent significant amounts of time and money getting users to "buy into" their namespaces.

2.  Any member of the federation must be able to leave at any time, and new members must be able to join at any time, without impacting the ability of the federation to resolve the total set of PURLs from members past and present.

3.  PURL service operators will remain in control of their DNS registrations and operation.

4.  PURL service operators will remain in control of their user accounts and internal policies relating to the creation and modification of PURLs.  The federation serves solely to resolve existing PURLs.

5.  If any third party service is used to operate any component of a PURL federation, members of the federation must be free to switch out that provider without disruption to the operations of the federation.


## PRESUMPTIONS ##

1.  Some PURL service operators (e.g. grant-funded research organizations) may wish to join a PURL federation to enable ongoing resolution of their persistent identifiers in the case of a funding loss.

2.  Some PURL service operators (e.g. those providing government or non-profit library services) may wish to join a PURL federation to amortize the costs associated with operating a fully redundant Web service in multiple geographically-separated locations with hot backup and failure capabilities or to gain those capabilities in the absence of sufficient institutional support.

3.  It is important to minimize the cost (in terms of equipment, operations, systems administration complexity and relationship management time) of developing and operating a PURL federation for all participants.

4.  Speed of response (i.e. network efficiency) is not a design criterion, but may be treated as an available optimization in certain deployment scenarios.



## DRAFT ARCHITECTURE ##

A PURL Federation is proposed which will consist of multiple independently-operated PURL servers, each of which have their own DNS hostnames, name their PURLs using their own authority (different from the hostname) and mirror other PURLs in the federation.  The authorities will be "outsourced" to a dynamic DNS service that will resolve to proxies for all authorities of the PURLs in the federation.  The attached image illustrates and summarizes the proposed architecture.

Caching proxies are inserted between the client and federation members. The dynamic DNS service responds to any request with an IP address of a proxy.  The proxy attempts to contact the primary PURL member via its alternative DNS name to fulfill the request and caches the response for future requests.  In the case where the primary PURL member is not responsive, the proxy attempts to contact another host in the federation until it succeeds.  Thus, most traffic for a given PURL authority continues to flow to the primary PURL member for that authority and not other members of the federation.

It is envisioned that proxy servers may be virtual machines located in a cloud computing environment.  The proxy implementation need not require on-disk storage of any PURLs or related material.  Proxies are expected to acquire their configuration upon startup from the dynamic DNS service for a federation (the location of which need be their only configuration option).

So, let's say a client wants to resolve the PURL http://purl.zepheira.com/team/dave/.  The dynamic DNS resolver resolves the authority purl.zepheira.com to the IP address of a caching proxy (either by random selection, geographic closeness or other criteria). The client makes an HTTP GET request for the PURL path to the IP address of the caching proxy using a HOST header of purl.zepheira.com (or an HTTP GET on the fully-qualified PURL).  The proxy looks at the HOST header or fully-qualified PURL to determine the entire PURL URI and searches its cache to determine whether it can respond immediately.  If it cannot, it attempts to contact the primary PURL member for the PURL in question with the intent of proxying its response.  If the canonical PURL server cannot be reached, other PURL servers in the federation are tried until a response is found.  If a PURL member that is mirroring the PURL is used to satisfy the request, an HTTP "Warning" header will be included in the response.

In this way, each PURL proxy would be able to respond to requests for any PURL held by members of the federation.  Because each PURL is only ever resolved using its full and complete URI, a PURL can only be identified by its full and complete URI, regardless of the authority responding for it at any given time.

The act of "being in the federation" would be as simple as (a) operating a PURL server and (b) participating in the dynamic DNS operation.  Participating in the dynamic DNS operation is as simple as adding a DNS CNAME entry to the organization's DNS servers for the name of their PURL authorities, pointing to a dynamic DNS service.  Such a dynamic DNS service is intended to be operated by the federation as a consortium or by a vendor or partner agreed to by the members.  Primary/mirror purl servers will be listed in SRV DNS records (see http://en.wikipedia.org/wiki/SRV_record).

Individual PURL members may be directly contacted by clients making requests directly to the server's IP address or non-authority host name and including the appropriate authority in the HOST header.  This use case, while allowed, should not be encouraged outside of directly testing the responsiveness of a given host because it by-passes the automated failover service provided by the federation.

All changes (writes) to PURLs would be made on their primary PURL members.  Recognizing that DNS resolution as described above would resolve to a proxy, administrative actions (HTTP PUT, POST or DELETE operations) could be redirected from a receiving proxy to the primary PURL member as required.

Writes (additions and changes) will be made to the primary PURL member and promulgated to its mirrors.  PURLs are never deleted (but may be "tombstoned"), so changes resulting in the tombstoning of a PURL entail the addition of metadata to a PURLs existing record.  In other words, the number of PURL records are designed to only increase in time and never decrease.  Such an environment facilitates data mirroring because, although state still needs to propagate through a federation, complex issues of data integrity are kept to a minimum.

Non-administrative actions (HTTP GET) requests would be handled directly by a receiving proxy or PURL server.  We also envision the assignment of DNS A records to all hosts in a federation. In other words, it knows itself by the service name (its hostname).

A simpler architecture without the use of caching proxies was considered and rejected.  Dynamic DNS resolution directly to a non-canonical PURL server allows for a poorly-behaved host to make all PURLs served by a federation to appear to be inaccessible for a period of time for some clients.  This can occur when a server is unstable or overly busy and cannot respond to requests for PURL resolution.  Periodic monitoring of server availability by the dynamic DNS service is envisioned to reduce the likelihood of this scenario by removing a bad server from the list of hosts used for DNS resolution. However, if such an unstable host fails for short periods of time and thus appears available as of the next time a monitoring request is made, and DNS caching is used by clients or their network providers such that the unstable host continues to receive requests, the entire federation can appear to be unresponsive to any clients attempting to resolve PURLs via that host.  The use of caching proxies to receive dynamic DNS resolution and to direct most traffic to the authoritative PURL server reduces such risks considerably and ensures that a poorly-behanved host effects the resolution of its own PURLs more than others.

Even if service monitoring catches a poorly-behaved or failing host, DNS caching can continue to make the entire federation appear unresponsive to some clients until the DNS caches expire.  The dynamic DNS service is thus motivated to set low or null times-to-live on DNS responses. However, the use of proxies eliminates this risk.

Advantages:

  * Meets all goals and complies with all presumptions.

  * Request loads are spread fairly throughout the federation.  A dominant PURL service with many requests will generally handle their own requests.   Significant loads on other servers in the federation will only be encountered in the case of a systems failure.

  * Allows for a one-time, low-administration method for joining or leaving a federation; operate a PURL server and make a single DNS CNAME entry for that server.

  * Protects against poorly-behaved or unstable hosts that could make the entire federation appear inaccessible for a period of time for some clients.

  * Caching proxies may enable better network efficiency for repeat requests for PURLs in their cache.

Disadvantages:

  * Greater cost than a more simplistic architecture (without the caching proxies).

  * The use of proxies may cause difficulty with certificate validation for any PURL servers using the HTTPS protocol, depending on the detailed implementation.  This is not considered an important scenario for this project, but should be kept in mind if the goals or presumptions change.

![http://persistenturls.googlecode.com/svn/graphics/PURL_Federation_Architecture.png](http://persistenturls.googlecode.com/svn/graphics/PURL_Federation_Architecture.png)


## REQUIRED CHANGES TO THE PURL SERVER API ##

The PURL server API (currently described at http://purlz.org/project/purl/documentation/requirements/URLs.html) will need to be augmented to support operations for data mirroring including both full and partial updates, and the resolution of PURLs based on the HOST header of a request.  Some amount of configuration information will need to be provided at a PURL server to ensure that its operators can control whether or not to participate in a federation.

Members of a federation undergo some cost when other members join because the new member's data must be mirrored, stored and maintained. Therefore, some social and technical mechanisms should be put in place to prohibit unwanted members and denial-of-service attacks.  Ideally, this mechanism should be a simple administrative approval and not require a software change or server restart.


## DATA MIRRORING ##

### Overview ###

Data mirroring between servers in the Federation will be accomplished
via a "pull" strategy.  Each PURL server will publish two sources of data:
The first of these sources will, upon invocation by an HTTP GET request
message, return representations of each PURL domain (or collection).

The second data source published by each server will provide the list
of PURL domains (or collections) that the server is contains and is configured to poll, as well as the last
datetime and entity tag a successful poll response was received from each server. This data source is used to drive the configuration of the server and contains all vhost patterns and PURL authorities use by the federation. Any changes to this data source are reflected in the server operations automatically.
This will permit debugging of the state of the federation as a whole.

### Data Format ###

The existing format used to represent PURLs is a custom XML vocabulary
that was not designed for use outside of the scope of a single PURL
server.  Also, the existing format includes maintainer information which
is neither necessary nor desired for passing to PURL mirror hosts.
Therefore,  a new data format is required.  We suggest using
Turtle, a compact, non-XML based serialization of RDF.  Regardless
of a PURL server's data persistence implementation, an RDF serialization
can be parsed and transformed appropriately.  A suitable RDF
vocabulary for representing a complete PURL wil be developed.

### Partial PURLs ###

Partial PURLs can be defined for a domain and path fragment and will use regex pattern and template to map between the PURL and destination URL. Partial PURLs will be able to use any response codes (PURL types) that a full PURL can use. Partial PURLs can optionally match a wild card host (and the domain itself) with a predetermined domain name. Partial PURLs can also match path fragments ending with a '/'. For example a partial PURL can be defined for the pattern "!http://*.purl.sn.org/*" and use the regex !http://(.*).purl.sn.org/([^/]*)/(.*) with the template !http://$1.sn.org/$2/pages/$3.html to map !http://term.purl.sn.org/dom/item to !http://term.sn.org/dom/pages/item.html

In this way PURLs can be defined for the domain and any host within that domain.
This will allow !http://www.foo.org/purl, !http://ftp.foo.org/purl, and !http://foo.org/purl to
be the same partial purl, but !http://www.bar.foo.org/purl would have to be defined separately.

### Data Format Migration ###

A script will be developed for existing PURL servers that will allow
them to export their PURLs in the new Turtle based format.

### Batch Loading of PURLs on a Canonical PURL Server ###

A third data source exposed by a server - but not advertised to, nor
intended to be used by other servers in the federation - will provide
a batch loading capability for complete PURLs, including "private"
maintainer data.  An additional RDF vocabulary will be developed to
describe the maintainer information, and data using this format will
be included inline.  As a result, this batch format will appear as an
extension of the federation format.

Maintainer information is not required for data mirroring because (a)
PURL member mirroring PURLs will serve those PURLs read-only
and (b) user and group accounts associated with mirrored PURLs
will not be created on mirroring hosts in any event.

### Initial Configuration ###

In order to join a federation, a PURL server will need to be provided
the following information via a user interface;

  * the canonical URIs for each PURL server it will mirror;
  * its own authority and any aliases.

It is envisioned that each PURL server will advertise URIs for its PURL
data sources, batch upload endpoint and management interface at
its canonical URI (`http://<authority><port>/`) for the purposes of bootstrapping
initial configurations into operations and enabling Linked Data client
operations.