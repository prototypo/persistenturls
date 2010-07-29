/*
 * Copyright (c) Zepheira LLC, Some rights reserved.
 * 
 * Source code developed for this project is licensed under the Apache
 * License, Version 2.0. See the file LICENSE.txt for details.
 */
package name.persistent.behaviours;

import info.aduna.net.ParsedURI;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import name.persistent.concepts.Domain;
import name.persistent.concepts.RemoteGraph;
import name.persistent.concepts.Unresolvable;

import org.apache.commons.httpclient.util.DateParseException;
import org.apache.commons.httpclient.util.DateUtil;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.message.BasicHttpRequest;
import org.openrdf.http.object.client.HTTPObjectClient;
import org.openrdf.http.object.exceptions.BadGateway;
import org.openrdf.http.object.exceptions.BadRequest;
import org.openrdf.http.object.exceptions.GatewayTimeout;
import org.openrdf.http.object.traits.ProxyObject;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ContextStatementImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.object.ObjectConnection;
import org.openrdf.repository.object.ObjectFactory;
import org.openrdf.repository.object.RDFObject;
import org.openrdf.repository.util.RDFInserter;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.RDFParserFactory;
import org.openrdf.rio.RDFParserRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages and loads remote RDF graphs.
 * 
 * @author James Leigh
 */
public abstract class RemoteGraphSupport implements RDFObject, RemoteGraph,
		ProxyObject {
	private static final String PROTOCOL = "1.1";
	private static final String NS = "http://persistent.name/rdf/2010/purl#";
	private static final String REL = NS + "rel";
	private static final String REMOTE_RESOURCE = NS + "RemoteResource";
	private static final String DEFINED_BY = NS + "definedBy";
	private static String hostname;
	static {
		try {
			hostname = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			hostname = "localhost";
		}
	}
	public static String VIA = PROTOCOL + " " + hostname;

	private final static class RemoteResourceInserter extends RDFInserter {
		private final List<String> origins;
		private final Resource ctx;
		private boolean nil = true;

		private RemoteResourceInserter(RepositoryConnection con,
				List<String> origins, Resource ctx) {
			super(con);
			this.origins = origins;
			this.ctx = ctx;
		}

		public boolean isNil() {
			return nil;
		}

		public void handleStatement(Statement st) throws RDFHandlerException {
			Resource subj = st.getSubject();
			URI pred = st.getPredicate();
			Value obj = st.getObject();
			if (!subj.equals(ctx) && subj instanceof URI) {
				boolean found = false;
				for (String origin : origins) {
					if (subj.stringValue().startsWith(origin)) {
						found = true;
						break;
					}
				}
				if (!found && !REL.equals(pred.stringValue())) {
					if (DEFINED_BY.equals(pred.stringValue()))
						return;
					if (RDF.TYPE.equals(pred) && REMOTE_RESOURCE.equals(obj.stringValue()))
						return;
					throw new BadGateway("Origin Not Allowed: "
							+ subj.stringValue() + " from " + ctx.stringValue());
				}
			}
			st = new ContextStatementImpl(subj, pred, obj, ctx);
			nil = false;
			super.handleStatement(st);
		}
	}
	private Logger logger = LoggerFactory.getLogger(RemoteGraphSupport.class);

	@Override
	public boolean load(String... origin) throws Exception {
		String url = getResource().stringValue();
		BasicHttpRequest req = new BasicHttpRequest("GET", url);
		String type = "application/rdf+xml";
		if (getPurlContentType() != null) {
			type = getPurlContentType();
		}
		req.setHeader("Accept", type);
		HttpResponse resp = requestRDF(req, 20);
		return importResponse(resp, origin);
	}

	@Override
	public boolean validate(String origin) throws Exception {
		if (isFresh())
			return true;
		return reload(origin);
	}

	@Override
	public boolean isFresh() {
		return getFreshness() >= 0;
	}

	@Override
	public boolean reload(String origin) throws Exception {
		String url = getResource().stringValue();
		BasicHttpRequest req = new BasicHttpRequest("GET", url);
		String type = getPurlContentType();
		if (type == null) {
			req.setHeader("Accept", "application/rdf+xml");
		} else {
			req.setHeader("Accept", type);
		}
		String etag = getPurlEtag();
		if (etag != null) {
			req.setHeader("If-None-Match", etag);
		}
		XMLGregorianCalendar modified = getPurlLastModified();
		if (modified != null) {
			Date date = modified.toGregorianCalendar().getTime();
			req.setHeader("If-Modified-Since", DateUtil.formatDate(date));
		}
		HttpResponse resp = requestRDF(req, 20);
		int code = resp.getStatusLine().getStatusCode();
		if (code == 304) {
			DatatypeFactory df = DatatypeFactory.newInstance();
			GregorianCalendar gc = new GregorianCalendar();
			setPurlLastValidated(df.newXMLGregorianCalendar(gc));
			setPurlCacheControl(getHeader(resp, "Cache-Control"));
		}
		if (code == 304 || code == 404) {
			HttpEntity entity = resp.getEntity();
			if (entity != null) {
				entity.consumeContent();
			}
			return true;
		}
		return importResponse(resp, origin);
	}

	public int getFreshness() {
		XMLGregorianCalendar modified = getPurlLastModified();
		XMLGregorianCalendar validated = getPurlLastValidated();
		String control = getPurlCacheControl();
		if (validated == null || control != null
				&& control.contains("no-cache"))
			return -1;
		long now = System.currentTimeMillis();
		long date = validated.toGregorianCalendar().getTimeInMillis();
		int age = (int) ((now - date) / 1000);
		long last = modified.toGregorianCalendar().getTimeInMillis();
		int lifeTime = Math.min(24 * 60 * 60, (int) ((now - last) / 10000));
		if (control != null && control.contains("age")) {
			Map<String, String> map = new LinkedHashMap<String, String>();
			for (String v : control.split(",")) {
				int idx = v.indexOf('=');
				if (idx < 0) {
					map.put(v, null);
				} else {
					map.put(v.substring(0, idx), v.substring(idx + 1));
				}
			}
			if (map.containsKey("s-maxage")) {
				lifeTime = Integer.parseInt(map.get("s-maxage"));
			} else if (map.containsKey("max-age")) {
				lifeTime = Integer.parseInt(map.get("max-age"));
			}
		}
		return lifeTime - age;
	}

	public void removeRemoteGraph() throws Exception {
		ObjectConnection con = getObjectConnection();
		con.clear(getResource());
		setPurlVia(null);
		setPurlCacheControl(null);
		setPurlEtag(null);
		setPurlLastModified(null);
		setPurlLastValidated(null);
		setPurlContentType(null);
		con.removeDesignation(this, Unresolvable.class);
		logger.info("Removing {}", getResource());
	}

	private HttpResponse requestRDF(HttpRequest req, int max) throws Exception {
		req.setHeader("Via", VIA);
		try {
			HTTPObjectClient client = HTTPObjectClient.getInstance();
			HttpResponse resp = client.service(req);
			HttpEntity entity = resp.getEntity();
			int code = resp.getStatusLine().getStatusCode();
			if (code >= 300 && code < 400 && max > 0) {
				Header location = resp.getFirstHeader("Location");
				if (location != null) {
					if (entity != null) {
						entity.consumeContent();
					}
					String href = location.getValue();
					HttpRequest req1 = new BasicHttpRequest("GET", href);
					return requestRDF(req1, max - 1);
				}
			}
			if (code == 200 || code == 304 || code == 404 || code == 410)
				return resp;
			if (entity != null) {
				entity.consumeContent();
			}
			if (code >= 300 && code < 400)
				throw new BadGateway("Too many redirects");
			throw new BadGateway(resp.getStatusLine().getReasonPhrase());
		} catch (BadGateway e) {
			logger.info("Unresolvable {}", getResource());
			ObjectConnection con = getObjectConnection();
			con.addDesignation(this, Unresolvable.class);
			throw e;
		} catch (GatewayTimeout e) {
			logger.info("Unresolvable {}", getResource());
			ObjectConnection con = getObjectConnection();
			con.addDesignation(this, Unresolvable.class);
			throw e;
		}
	}

	private boolean importResponse(HttpResponse resp, String... origin)
			throws Exception {
		ObjectConnection con = getObjectConnection();
		boolean autoCommit = con.isAutoCommit();
		HttpEntity entity = resp.getEntity();
		try {
			int code = resp.getStatusLine().getStatusCode();
			if (code == 404 || code == 504) {
				logger.info("Unresolvable {}", getResource());
				con.addDesignation(this, Unresolvable.class);
				return false;
			}
			if (code != 200 && code != 410)
				throw new BadGateway(resp.getStatusLine().getReasonPhrase());
			String resource = getResource().stringValue();
			ValueFactory vf = con.getValueFactory();
			URI ctx = vf.createURI(resource);
			if (autoCommit) {
				con.setAutoCommit(false); // begin
			}
			if (code == 410) {
				removeRemoteGraph();
				con.commit();
				return false;
			}
			InputStream in = entity.getContent();
			try {
				String type = getHeader(resp, "Content-Type");
				con.removeDesignation(this, Unresolvable.class);
				con.clear(ctx);
				logger.info("Loading {}", this);
				if (!parse(type, in, origin))
					return false;
				store(type, resp, origin);
				logger.info("Updating {}", getResource());
				con.commit();
				logger.info("Loaded {}", this);
				return true;
			} catch (RDFHandlerException e) {
				throw cause(e);
			} finally {
				in.close();
			}
		} finally {
			if (entity != null) {
				entity.consumeContent();
			}
			if (autoCommit && !con.isAutoCommit()) {
				con.rollback();
				con.setAutoCommit(true);
			}
		}
	}

	private void store(String type, HttpResponse resp, String... origins)
			throws Exception {
		ObjectConnection con = getObjectConnection();
		con.addDesignation(this, RemoteGraph.class);
		String via = getHeader(resp, "Via");
		if (via == null) {
			String url = getResource().stringValue();
			String authority = new ParsedURI(url).getAuthority();
			setPurlVia(PROTOCOL + " " + authority);
		} else {
			setPurlVia(via);
		}
		setPurlContentType(type);
		setPurlEtag(getHeader(resp, "ETag"));
		setPurlCacheControl(getHeader(resp, "Cache-Control"));
		DatatypeFactory df = DatatypeFactory.newInstance();
		GregorianCalendar gc = new GregorianCalendar();
		setPurlLastValidated(df.newXMLGregorianCalendar(gc));
		gc.setTime(getDateHeader(resp, "Last-Modified"));
		setPurlLastModified(df.newXMLGregorianCalendar(gc));
		if (origins != null) {
			ObjectFactory of = con.getObjectFactory();
			for (String origin : origins) {
				if (origin != null) {
					Domain o = of.createObject(origin, Domain.class);
					getPurlAllowedOrigins().add(o);
				}
			}
		}
		for (Header hd : resp.getHeaders("Warning")) {
			if (hd.getValue().contains("111")) {
				// 111 "Revalidation failed"
				con.addDesignation(this, Unresolvable.class);
			}
		}
	}

	private boolean parse(String type, InputStream in, String... origin)
			throws IOException, RDFParseException, RDFHandlerException {
		Resource ctx = getResource();
		String resource = ctx.stringValue();
		ObjectConnection con = getObjectConnection();
		ValueFactory vf = con.getValueFactory();
		RemoteResourceInserter handler;
		List<String> origins = new ArrayList<String>();
		if (origin != null) {
			for (String o : origin) {
				if (o != null) {
					origins.add(o);
				}
			}
		}
		for (Object o : getPurlAllowedOrigins()) {
			origins.add(((RDFObject) o).getResource().stringValue());
		}
		handler = new RemoteResourceInserter(con, origins, ctx);
		String mimeType = type;
		if (mimeType.contains(";")) {
			mimeType = mimeType.substring(0, mimeType.indexOf(';'));
		}
		RDFParserRegistry reg = RDFParserRegistry.getInstance();
		RDFFormat format = reg.getFileFormatForMIMEType(mimeType);
		if (format == null)
			throw new BadRequest("Unknown graph type: " + mimeType);
		RDFParserFactory factory = reg.get(format);
		if (factory == null)
			throw new BadRequest("Unsupported graph type: " + mimeType);
		RDFParser parser = factory.getParser();
		parser.setValueFactory(vf);
		parser.setRDFHandler(handler);
		parser.parse(in, resource);
		return !handler.isNil();
	}

	private Date getDateHeader(HttpResponse resp, String name)
			throws DateParseException {
		Header hd = resp.getFirstHeader(name);
		if (hd == null)
			return new Date();
		return DateUtil.parseDate(hd.getValue());
	}

	private String getHeader(HttpResponse resp, String name) {
		Header[] hd = resp.getHeaders(name);
		if (hd == null || hd.length == 0)
			return null;
		if (hd.length == 1)
			return hd[0].getValue();
		StringBuilder sb = new StringBuilder();
		for (Header h : hd) {
			sb.append(h.getValue()).append(",");
		}
		return sb.substring(0, sb.length() - 1);
	}

	private Exception cause(Exception e) throws Exception {
		try {
			throw e.getCause();
		} catch (Exception cause) {
			return cause;
		} catch (Error cause) {
			throw cause;
		} catch (Throwable cause) {
			throw e;
		}
	}

}
