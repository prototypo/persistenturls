/*
 * Copyright (c) Zepheira LLC, Some rights reserved.
 * 
 * Source code developed for this project is licensed under the Apache
 * License, Version 2.0. See the file LICENSE.txt for details.
 */
package name.persistent.behaviours;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.Set;

import javax.tools.FileObject;

import name.persistent.concepts.Domain;
import name.persistent.concepts.Origin;
import name.persistent.concepts.Server;

import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.message.BasicHttpResponse;
import org.openrdf.OpenRDFException;
import org.openrdf.http.object.annotations.header;
import org.openrdf.http.object.annotations.method;
import org.openrdf.http.object.annotations.operation;
import org.openrdf.http.object.annotations.parameter;
import org.openrdf.http.object.annotations.type;
import org.openrdf.http.object.exceptions.BadRequest;
import org.openrdf.http.object.model.ReadableHttpEntityChannel;
import org.openrdf.http.object.writers.AggregateWriter;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.impl.GraphQueryResultImpl;
import org.openrdf.repository.object.ObjectConnection;
import org.openrdf.repository.object.ObjectFactory;
import org.openrdf.repository.object.RDFObject;
import org.openrdf.repository.object.annotations.name;
import org.openrdf.repository.object.annotations.sparql;

/**
 * Provides information about the domains and PURLs managed in this server.
 * 
 * @author James Leigh
 */
public abstract class ServerSupport extends MirrorSupport implements RDFObject,
		FileObject, Server {
	private static final String PROTOCOL = "1.1";
	private static final String PURL = "http://persistent.name/rdf/2010/purl#";
	private static final String PREFIX = "PREFIX purl:<http://persistent.name/rdf/2010/purl#>\n";
	private static final ProtocolVersion HTTP11 = new ProtocolVersion("HTTP",
			1, 1);
	private static final AggregateWriter writer = AggregateWriter.getInstance();
	private static String hostname;
	static {
		try {
			hostname = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			hostname = "localhost";
		}
	}
	private static final String VIA = PROTOCOL + " " + hostname;

	private static class QueuedResult extends GraphQueryResultImpl {
		private Queue<Statement> queue = new ArrayDeque<Statement>();

		private QueuedResult(GraphQueryResult result) {
			super(result.getNamespaces(), result);
		}

		public void add(Resource subj, URI pred, Value obj) {
			queue.add(new StatementImpl(subj, pred, obj));
		}

		@Override
		public boolean hasNext() throws QueryEvaluationException {
			return !queue.isEmpty() || super.hasNext();
		}

		public Statement next() throws QueryEvaluationException {
			if (!queue.isEmpty()) {
				return queue.remove();
			}
			return super.next();
		}
	}

	private static class RemoteResult extends QueuedResult {
		private ValueFactory vf;
		private URI rel;
		private URI RemoteResource;
		private URI definedBy;
		private String definedByGraph;

		private RemoteResult(GraphQueryResult result, ValueFactory vf) {
			this(result, vf, null, null);
		}

		private RemoteResult(GraphQueryResult result, ValueFactory vf, URI rel,
				String definedByGraph) {
			super(result);
			assert vf != null;
			this.vf = vf;
			this.rel = rel;
			this.definedByGraph = definedByGraph;
			this.RemoteResource = vf.createURI(PURL, "RemoteResource");
			this.definedBy = vf.createURI(PURL, "definedBy");
		}

		public Statement next() throws QueryEvaluationException {
			Statement st = super.next();
			Value obj = st.getObject();
			if (st.getPredicate().equals(rel) && obj instanceof Resource) {
				addRemoteResource((Resource) obj);
			}
			return st;
		}

		public void addRemoteResource(Resource subj) {
			add(subj, RDF.TYPE, RemoteResource);
			try {
				if (definedByGraph != null && vf != null) {
					String uri = subj.stringValue();
					String suffix = URLEncoder.encode(uri, "UTF-8");
					URI graph = vf.createURI(definedByGraph + suffix);
					add(subj, definedBy, graph);
				}
			} catch (UnsupportedEncodingException e) {
				throw new AssertionError(e);
			}
		}
	}

	@operation("")
	@method("GET")
	@type("message/x-response")
	public HttpResponse get(@header("Accept") String accept) {
		ProtocolVersion ver = new ProtocolVersion("HTTP", 1, 1);
		HttpResponse resp = new BasicHttpResponse(ver, 307,
				"Temporary Redirect");
		if (accept.contains("application/rdf+xml")) {
			resp.setHeader("Location", getResource().stringValue()
					+ "?listOrigins");
		} else {
			resp.setHeader("Location", getResource().stringValue() + "?view");
		}
		return resp;
	}

	/**
	 * List of origins on this domain service.
	 */
	@operation("listOrigins")
	@type("application/rdf+xml")
	public GraphQueryResult listRemoteOrigins(@header("Via") Set<String> via)
			throws OpenRDFException {
		if (via != null && via.toString().contains(VIA))
			throw new BadRequest("Request Loop Detected");
		ObjectConnection con = getObjectConnection();
		ValueFactory vf = con.getValueFactory();
		URI serves = vf.createURI(PURL, "serves");
		String desc = getResource().stringValue() + "?listDomains&origin=";
		return new RemoteResult(listOrigins(), vf, serves, desc);
	}

	/**
	 * List of domains for the given origin.
	 */
	@operation("listDomains")
	@type("message/x-response")
	public HttpResponse listRemoteDomains(@parameter("origin") Origin origin,
			@header("Via") Set<String> via) throws Exception {
		if (via != null && via.toString().contains(VIA))
			throw new BadRequest("Request Loop Detected");
		if (origin == null)
			throw new BadRequest("Missing origin");
		ObjectConnection con = getObjectConnection();
		Resource target = ((RDFObject) origin).getResource();
		ValueFactory vf = con.getValueFactory();
		URI part = vf.createURI(PURL, "part");
		String desc = getResource().stringValue() + "?listServices&domain=";
		RemoteResult r = new RemoteResult(listDomains(origin), vf, part, desc);
		r.addRemoteResource(target); // origin is also a domain
		URI mirroredBy = vf.createURI(PURL, "mirroredBy");
		String domains = getResource().stringValue() + "?domainsOf&origin=";
		r.add(target, mirroredBy, vf.createURI(domains + enc(target)));
		return mirrorOf((RDFObject) origin, r);
	}

	/**
	 * List of services for the given domain.
	 */
	@operation("listServices")
	@type("message/x-response")
	public HttpResponse listRemoteServices(@parameter("domain") Domain domain,
			@header("Via") Set<String> via) throws Exception {
		if (via != null && via.toString().contains(VIA))
			throw new BadRequest("Request Loop Detected");
		if (domain == null)
			throw new BadRequest("Missing domain");
		ObjectConnection con = getObjectConnection();
		Resource target = ((RDFObject) domain).getResource();
		ValueFactory vf = con.getValueFactory();
		RemoteResult r = new RemoteResult(listServices(domain), vf);
		URI mirroredBy = vf.createURI(PURL, "mirroredBy");
		String purls = getResource().stringValue() + "?purlsOf&domain=";
		r.add(target, mirroredBy, vf.createURI(purls + enc(target)));
		return mirrorOf((RDFObject) domain, r);
	}

	/**
	 * Description of all domains in the given origin.
	 */
	@operation("domainsOf")
	@type("message/x-response")
	public HttpResponse domainsOf(@parameter("origin") Origin origin,
			@header("Via") Set<String> via) throws Exception {
		if (via != null && via.toString().contains(VIA))
			throw new BadRequest("Request Loop Detected");
		if (origin == null)
			throw new BadRequest("Missing origin");
		return mirrorOf((RDFObject) origin, describeAllDomains(origin));
	}

	/**
	 * Description of all PURLs in the given domain.
	 */
	@operation("purlsOf")
	@type("message/x-response")
	public HttpResponse purlsOf(@parameter("domain") Domain domain,
			@header("Via") Set<String> via) throws Exception {
		if (via != null && via.toString().contains(VIA))
			throw new BadRequest("Request Loop Detected");
		if (domain == null)
			throw new BadRequest("Missing domain");
		return mirrorOf((RDFObject) domain, describePURLs(domain));
	}

	@sparql(PREFIX + "CONSTRUCT { $this a purl:Server; purl:serves ?origin }\n"
			+ "WHERE { $this purl:serves ?origin }")
	protected abstract GraphQueryResult listOrigins();

	@sparql(PREFIX + "CONSTRUCT { $origin a purl:Origin; purl:part ?domain }\n"
			+ "WHERE { $origin purl:part ?domain }")
	protected abstract GraphQueryResult listDomains(
			@name("origin") Origin origin);

	@sparql(PREFIX
			+ "CONSTRUCT { $domain a ?type; purl:service ?service .\n"
			+ "?service a purl:Service; purl:server ?server; purl:priority ?priority; purl:weight ?weight }\n"
			+ "WHERE { $domain a ?type; purl:service ?service .\n"
			+ "?service purl:server ?server\n"
			+ "FILTER (?type = purl:Domain || ?type = purl:Origin)\n"
			+ "OPTIONAL { ?service purl:priority ?priority}\n"
			+ "OPTIONAL { ?service purl:weight ?weight }}")
	protected abstract GraphQueryResult listServices(
			@name("domain") Domain domain);

	@sparql(PREFIX
			+ "CONSTRUCT {\n"
			+ "$origin a purl:Origin; purl:service ?osrv; purl:part ?domain .\n"
			+ "?domain a purl:Domain; purl:service ?srv .\n"
			+ "?osrv a purl:Service; purl:server ?oserver; purl:priority ?op; purl:weight ?ow .\n"
			+ "?srv a purl:Service; purl:server ?server; purl:priority ?p; purl:weight ?w }\n"
			+ "WHERE { $origin purl:service ?osrv .\n"
			+ "?osrv purl:server ?oserver\n"
			+ "OPTIONAL { ?osrv purl:priority ?op}\n"
			+ "OPTIONAL { ?osrv purl:weight ?ow }\n"
			+ "OPTIONAL { $origin purl:part ?domain .\n"
			+ "?domain purl:service ?srv .\n" + "?srv purl:server ?server\n"
			+ "OPTIONAL { ?srv purl:priority ?p}\n"
			+ "OPTIONAL { ?srv purl:weight ?w }}}")
	protected abstract GraphQueryResult describeAllDomains(
			@name("origin") Origin origin);

	@sparql(PREFIX
			+ "CONSTRUCT { $domain a ?dtype; purl:pattern ?dpattern; ?dpred ?dhref .\n"
			+ "?purl purl:partOf $domain; a ?type; purl:pattern ?pattern; ?pred ?href .\n"
			+ "?dpred purl:rel ?drel . ?pred purl:rel ?rel}\n"
			+ "WHERE { $domain a ?dtype .\n"
			+ "?purl purl:partOf $domain; a ?type\n"
			+ "OPTIONAL { ?purl purl:pattern ?pattern }\n"
			+ "OPTIONAL { ?purl ?pred ?href . ?pred purl:rel ?rel }}")
	protected abstract GraphQueryResult describePURLs(
			@name("domain") Domain domain);

	private HttpResponse mirrorOf(Object target, GraphQueryResult result)
			throws Exception {
		HttpResponse resp = new BasicHttpResponse(HTTP11, 200, "OK");
		mirrorEntityHeaders(target, resp);
		String type = "application/rdf+xml";
		Class<GraphQueryResult> t = GraphQueryResult.class;
		ObjectConnection con = getObjectConnection();
		ObjectFactory of = con.getObjectFactory();
		String b = getResource().stringValue();
		ReadableByteChannel in = writer.write(type, t, t, of, result, b, null);
		resp.setEntity(new ReadableHttpEntityChannel(type, -1, in));
		return resp;
	}

	private String enc(Resource target) throws UnsupportedEncodingException {
		return URLEncoder.encode(target.stringValue(), "UTF-8");
	}
}
