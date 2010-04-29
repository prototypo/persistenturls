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
import java.util.ArrayDeque;
import java.util.Queue;

import name.persistent.concepts.Domain;
import name.persistent.concepts.MirroredResource;
import name.persistent.concepts.Origin;
import name.persistent.concepts.Server;

import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.message.BasicHttpResponse;
import org.openrdf.OpenRDFException;
import org.openrdf.http.object.annotations.operation;
import org.openrdf.http.object.annotations.parameter;
import org.openrdf.http.object.annotations.rel;
import org.openrdf.http.object.annotations.type;
import org.openrdf.http.object.exceptions.BadRequest;
import org.openrdf.http.object.exceptions.NotFound;
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
public abstract class ServerSupport extends MirrorSupport implements RDFObject, Server {
	private static final String PURL = "http://persistent.name/rdf/2010/purl#";
	private static final String PREFIX = "PREFIX purl:<http://persistent.name/rdf/2010/purl#>\n";
	private static final ProtocolVersion HTTP11 = new ProtocolVersion("HTTP",
			1, 1);
	private static final AggregateWriter writer = AggregateWriter.getInstance();

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

		private RemoteResult(GraphQueryResult result, ValueFactory vf, URI rel,
				String definedByGraph) {
			super(result);
			this.vf = vf;
			this.rel = rel;
			this.RemoteResource = vf.createURI(PURL, "RemoteResource");
			this.definedBy = vf.createURI(PURL, "definedBy");
			this.definedByGraph = definedByGraph;
		}

		public Statement next() throws QueryEvaluationException {
			Statement st = super.next();
			Value obj = st.getObject();
			if (st.getPredicate().equals(rel) && obj instanceof URI) {
				URI sobj = (URI) obj;
				add(sobj, RDF.TYPE, RemoteResource);
				try {
					String uri = obj.stringValue();
					String suffix = URLEncoder.encode(uri, "UTF-8");
					URI graph = vf.createURI(definedByGraph + suffix);
					add(sobj, definedBy, graph);
				} catch (UnsupportedEncodingException e) {
					throw new AssertionError(e);
				}
			}
			return st;
		}
	}

	/**
	 * List of origins on this domain service.
	 */
	@rel("alternate")
	@operation("listOrigins")
	@type("application/rdf+xml")
	public GraphQueryResult listRemoteOrigins() throws OpenRDFException {
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
	public HttpResponse listRemoteDomains(@parameter("origin") Origin origin)
			throws Exception {
		if (origin == null)
			throw new BadRequest("Missing origin");
		ObjectConnection con = getObjectConnection();
		Resource target = ((RDFObject) origin).getResource();
		ValueFactory vf = con.getValueFactory();
		URI part = vf.createURI(PURL, "part");
		String desc = getResource().stringValue() + "?listServices&domain=";
		RemoteResult r = new RemoteResult(listDomains(origin), vf, part,
				desc);
		if (origin instanceof MirroredResource
				|| !origin.getCalliMaintainers().isEmpty()) {
			URI content = vf.createURI(PURL, "mirroredBy");
			String domains = getResource().stringValue() + "?domainsOf&origin=";
			r.add(target, content, vf.createURI(domains + enc(target)));
		}
		return mirrorOf((RDFObject) origin, r);
	}

	/**
	 * List of services for the given domain.
	 */
	@operation("listServices")
	@type("message/x-response")
	public HttpResponse listRemoteServices(@parameter("domain") Domain domain)
			throws Exception {
		if (domain == null)
			throw new BadRequest("Missing domain");
		ObjectConnection con = getObjectConnection();
		Resource target = ((RDFObject) domain).getResource();
		ValueFactory vf = con.getValueFactory();
		URI part = vf.createURI(PURL, "part");
		String desc = getResource().stringValue() + "?listServices&domain=";
		RemoteResult r = new RemoteResult(listServices(domain), vf, part,
				desc);
		if (domain instanceof MirroredResource
				|| !domain.getCalliMaintainers().isEmpty()) {
			URI content = vf.createURI(PURL, "mirroredBy");
			String purls = getResource().stringValue() + "?purlsOf&domain=";
			r.add(target, content, vf.createURI(purls + enc(target)));
		}
		return mirrorOf((RDFObject) domain, r);
	}

	/**
	 * Description of all domains in the given origin.
	 */
	@operation("domainsOf")
	@type("message/x-response")
	public HttpResponse domainsOf(@parameter("origin") Origin origin)
			throws Exception {
		if (origin == null)
			throw new BadRequest("Missing origin");
		if (origin instanceof MirroredResource
				|| !origin.getCalliMaintainers().isEmpty())
			return mirrorOf((RDFObject) origin, describeAllDomains(origin));
		throw new NotFound("Mirror Not Available");
	}

	/**
	 * Description of all PURLs in the given domain.
	 */
	@operation("purlsOf")
	@type("message/x-response")
	public HttpResponse purlsOf(@parameter("domain") Domain domain)
			throws Exception {
		if (domain == null)
			throw new BadRequest("Missing domain");
		if (domain instanceof MirroredResource
				|| !domain.getCalliMaintainers().isEmpty())
			return mirrorOf((RDFObject) domain, describePURLs(domain));
		throw new NotFound("Mirror Not Available");
	}

	@sparql(PREFIX + "CONSTRUCT { $this a purl:Server; purl:serves ?origin }\n"
			+ "WHERE { $this a purl:Server; purl:serves ?origin }")
	protected abstract GraphQueryResult listOrigins();

	@sparql(PREFIX
			+ "CONSTRUCT { $origin a purl:Origin; purl:part ?domain }\n"
			+ "WHERE { $origin a purl:Origin; purl:part ?domain }")
	protected abstract GraphQueryResult listDomains(
			@name("origin") Origin origin);

	@sparql(PREFIX
			+ "CONSTRUCT { $domain a purl:Domain; purl:service ?service .\n"
			+ "?service a purl:Service; purl:server ?server .\n"
			+ "?service purl:priority ?priority; purl:weight ?weight }\n"
			+ "WHERE { $domain a purl:Domain; purl:service ?service .\n"
			+ "?service a purl:Service; purl:server ?server\n"
			+ "OPTIONAL { ?service purl:priority ?priority\n"
			+ "OPTIONAL { ?service purl:weight ?weight }}}")
	protected abstract GraphQueryResult listServices(
			@name("domain") Domain domain);

	@sparql(PREFIX
			+ "CONSTRUCT { $origin a purl:Origin, purl:MirroredResource; purl:part ?domain .\n"
			+ "?domain a purl:Domain; purl:service ?service .\n"
			+ "?service a purl:Service; purl:server ?server .\n"
			+ "?service purl:priority ?priority; purl:weight ?weight }\n"
			+ "WHERE { $origin a purl:Origin; purl:part ?domain .\n"
			+ "OPTIONAL { ?domain a purl:Domain; purl:service ?service .\n"
			+ "?service a purl:Service; purl:server ?server\n"
			+ "OPTIONAL { ?service purl:priority ?priority\n"
			+ "OPTIONAL { ?service purl:weight ?weight }}}}")
	protected abstract GraphQueryResult describeAllDomains(
			@name("origin") Origin origin);

	@sparql(PREFIX
			+ "CONSTRUCT { $domain a purl:Domain, purl:MirroredResource .\n"
			+ "?purl purl:partOf $domain; a ?type; purl:pattern ?pattern; ?pred ?href .\n"
			+ "?pred purl:rel ?rel}\n"
			+ "WHERE { ?purl purl:partOf $domain; a ?type\n"
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
