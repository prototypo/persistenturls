/*
 * Copyright (c) Zepheira LLC, Some rights reserved.
 * 
 * Source code developed for this project is licensed under the Apache
 * License, Version 2.0. See the file LICENSE.txt for details.
 */
package name.persistent.behaviours;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.channels.ReadableByteChannel;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.xml.datatype.XMLGregorianCalendar;

import name.persistent.concepts.Domain;
import name.persistent.concepts.RemoteGraph;

import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.message.BasicHttpResponse;
import org.openrdf.http.object.annotations.operation;
import org.openrdf.http.object.annotations.transform;
import org.openrdf.http.object.annotations.type;
import org.openrdf.http.object.concepts.Transaction;
import org.openrdf.http.object.model.ReadableHttpEntityChannel;
import org.openrdf.http.object.threads.ManagedExecutors;
import org.openrdf.http.object.traits.VersionedObject;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.object.ObjectConnection;
import org.openrdf.repository.object.ObjectRepository;
import org.openrdf.repository.object.RDFObject;
import org.openrdf.repository.object.annotations.iri;
import org.openrdf.repository.object.annotations.sparql;
import org.openrdf.repository.object.annotations.triggeredBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class DomainSupport extends PartialSupport implements Domain,
		RDFObject, VersionedObject {
	private static final String NS = "http://persistent.name/rdf/2010/purl#";
	private static final String DEFINED_BY = NS + "definedBy";
	private static final String SERVICED_BY = NS + "servicedBy";
	private static final String MIRRORED_BY = NS + "mirroredBy";
	private static final String PREFIX = "PREFIX purl:<http://persistent.name/rdf/2010/purl#>\n"
			+ "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>\n";
	private static final ProtocolVersion HTTP11 = new ProtocolVersion("HTTP",
			1, 1);
	/** Date format pattern used to generate the header in RFC 1123 format. */
	public static final String PATTERN_RFC1123 = "EEE, dd MMM yyyy HH:mm:ss zzz";
	/** The time zone to use in the date header. */
	public static final TimeZone GMT = TimeZone.getTimeZone("GMT");
	private static final DateFormat dateformat;
	static {
		dateformat = new SimpleDateFormat(PATTERN_RFC1123, Locale.US);
		dateformat.setTimeZone(GMT);
	}
	private static final ScheduledExecutorService executor = ManagedExecutors
			.newSingleScheduler("Denfine Domain");

	@Override
	public void touchRevision() {
		super.touchRevision();
		for (Domain domain : getPurlDomainOf()) {
			if (domain instanceof VersionedObject) {
				((VersionedObject) domain).touchRevision();
			}
		}
	}

	@operation("mirror")
	@type("application/rdf+xml")
	@transform("http://persistent.name/rdf/2010/purl#entity-graph")
	@sparql(PREFIX
			+ "CONSTRUCT {\n"
			+ "?partial rdfs:label ?dlabel; purl:belongsTo ?domain; a ?ptype; purl:partOf ?parent;\n"
			+ "purl:pattern ?pattern; ?dpred ?dhref.\n"
			+ "?purl rdfs:label ?label; purl:partOf ?partial; a ?type; ?pred ?href .\n"
			+ "} WHERE { { ?partial a ?dtype FILTER (?partial = $this) }\n"
			+ "UNION { ?partial a ?ptype; purl:belongsTo ?domain FILTER (?domain = $this) }\n"
			+ "OPTIONAL { ?partial rdfs:label ?dlabel }\n"
			+ "OPTIONAL { ?partial purl:partOf ?parent }\n"
			+ "OPTIONAL { ?partial purl:pattern ?pattern }\n"
			+ "OPTIONAL { ?purl purl:partOf ?partial; a purl:PURL, ?type\n"
			+ "\t OPTIONAL { ?purl rdfs:label ?label }\n"
			+ "\t OPTIONAL { ?purl ?pred ?href . ?pred purl:rel ?rel }}\n"
			+ "OPTIONAL { ?partial ?dpred ?dhref . ?dpred purl:rel ?drel }}")
	public abstract GraphQueryResult mirror();

	@operation("services")
	@type("application/rdf+xml")
	@transform("http://persistent.name/rdf/2010/purl#entity-graph")
	@sparql(PREFIX
			+ "CONSTRUCT { $this rdfs:label ?label; purl:service ?srv .\n"
			+ "?srv a purl:Service; purl:server ?server; purl:priority ?p; purl:weight ?w\n"
			+ "} WHERE { $this purl:service ?srv . ?srv purl:server ?server\n"
			+ "OPTIONAL { $this rdfs:label ?label }\n"
			+ "OPTIONAL { ?srv purl:priority ?p}\n"
			+ "OPTIONAL { ?srv purl:weight ?w }}")
	public abstract GraphQueryResult services();

	@operation("remote-domains")
	@type("application/rdf+xml")
	@transform("http://persistent.name/rdf/2010/purl#add-operations")
	@sparql(PREFIX
			+ "CONSTRUCT {\n"
			+ "?domain a purl:RemoteDomain; purl:servicedBy ?server; purl:domainOf ?top .\n"
			+ "} WHERE { { ?domain a ?type FILTER(?domain = $this) }\n"
			+ "UNION { ?domain purl:domainOf $this }\n"
			+ "{ ?server a purl:Server; purl:serves $this }\n"
			+ "UNION { ?server a purl:Server; purl:serves ?top . $this purl:domainOf ?top }\n"
			+ "OPTIONAL { ?domain purl:domainOf ?top }}")
	public abstract GraphQueryResult remoteDomains();

	@operation("mirror-domains")
	@type("application/rdf+xml")
	@transform("http://persistent.name/rdf/2010/purl#add-operations")
	@sparql(PREFIX
			+ "CONSTRUCT {\n"
			+ "?domain a purl:MirroredDomain; purl:mirroredBy ?server; purl:domainOf ?top .\n"
			+ "} WHERE { { ?domain a ?type FILTER(?domain = $this) }\n"
			+ "UNION { ?domain purl:domainOf $this }\n"
			+ "{ ?server a purl:Server; purl:serves $this }\n"
			+ "UNION { ?server a purl:Server; purl:serves ?top . $this purl:domainOf ?top }\n"
			+ "OPTIONAL { ?domain purl:domainOf ?top }}")
	public abstract GraphQueryResult mirrorDomains();

	@type("application/rdf+xml")
	@iri("http://persistent.name/rdf/2010/purl#add-operations")
	@transform("http://persistent.name/rdf/2010/purl#entity-graph")
	public GraphQueryResult addOperations(final GraphQueryResult delegate) {
		final ValueFactory vf = getObjectConnection().getValueFactory();
		final URI mirroredBy = vf.createURI(MIRRORED_BY);
		final URI servicedBy = vf.createURI(SERVICED_BY);
		return new GraphQueryResult() {
			public Statement next() throws QueryEvaluationException {
				Statement st = delegate.next();
				try {
					Resource subj = st.getSubject();
					String uri = subj.stringValue();
					URI pred = st.getPredicate();
					Value obj = st.getObject();
					String server = obj.stringValue();
					if (mirroredBy.equals(pred)) {
						String enc = URLEncoder.encode(uri, "UTF-8");
						String url = server + "diverted;" + enc + "?mirror";
						URI o = vf.createURI(url);
						return new StatementImpl(subj, mirroredBy, o);
					} else if (servicedBy.equals(pred)) {
						String enc = URLEncoder.encode(uri, "UTF-8");
						String url = server + "diverted;" + enc + "?services";
						URI o = vf.createURI(url);
						return new StatementImpl(subj, servicedBy, o);
					}
				} catch (UnsupportedEncodingException e) {
					throw new AssertionError(e);
				}
				return st;
			}

			public boolean hasNext() throws QueryEvaluationException {
				return delegate.hasNext();
			}

			public void close() throws QueryEvaluationException {
				delegate.close();
			}

			public Map<String, String> getNamespaces() {
				return delegate.getNamespaces();
			}

			public void remove() throws QueryEvaluationException {
				delegate.remove();
			}
		};
	}

	@type("message/x-response")
	@iri("http://persistent.name/rdf/2010/purl#entity-graph")
	public HttpResponse entityGraph(@type("application/rdf+xml") ReadableByteChannel in) {
		HttpResponse resp = new BasicHttpResponse(HTTP11, 200, "OK");
		purlSetEntityHeaders(resp);
		String type = "application/rdf+xml";
		resp.setEntity(new ReadableHttpEntityChannel(type, -1, in));
		return resp;
	}

	@Override
	public void purlSetEntityHeaders(HttpResponse resp) {
		setHeader(resp, "Cache-Control", "max-age=3600");
		setHeader(resp, "ETag", revisionTag(0));
		Transaction revision = getRevision();
		if (revision != null) {
			setHeader(resp, "Last-Modified", revision.getCommittedOn());
		}
	}

	@triggeredBy( { DEFINED_BY, MIRRORED_BY, SERVICED_BY })
	public void definitionChanged() throws Exception {
		ObjectConnection active = getObjectConnection();
		final Logger logger = LoggerFactory.getLogger(DomainSupport.class);
		final ObjectRepository repository = active.getRepository();
		final Resource resource = getResource();
		executor.schedule(new Runnable() {
			public String toString() {
				return "refresh " + resource;
			}

			public void run() {
				try {
					logger.info("Refreshing {}", resource);
					ObjectConnection con = repository.getConnection();
					try {
						Domain domain = con.getObject(Domain.class, resource);
						domain.refreshGraphs();
					} finally {
						con.close();
					}
				} catch (Exception e) {
					logger.warn("Could not refresh " + resource, e);
					executor.schedule(this, 5, TimeUnit.MINUTES);
				}
			}
		}, 30, TimeUnit.SECONDS);
	}

	public void refreshGraphs() throws Exception {
		Object graph = getPurlDefinedBy();
		if (graph != null && !(graph instanceof RemoteGraph)) {
			ObjectConnection con = getObjectConnection();
			RemoteGraph rg = con.addDesignation(graph, RemoteGraph.class);
			rg.load(getResource().stringValue());
		}
	}

	protected void setHeader(HttpResponse resp, String name, String value) {
		if (value != null && !resp.containsHeader(name)) {
			resp.setHeader(name, value);
		}
	}

	protected void setHeader(HttpResponse resp, String name,
			XMLGregorianCalendar value) {
		if (value != null && !resp.containsHeader(name)) {
			Date time = value.toGregorianCalendar().getTime();
			resp.setHeader(name, dateformat.format(time));
		}
	}
}
