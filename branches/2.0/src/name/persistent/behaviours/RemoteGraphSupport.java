/*
 * Copyright (c) Zepheira LLC, Some rights reserved.
 * 
 * Source code developed for this project is licensed under the Apache
 * License, Version 2.0. See the file LICENSE.txt for details.
 */
package name.persistent.behaviours;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import name.persistent.concepts.Origin;
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
import org.openrdf.http.object.traits.ProxyObject;
import org.openrdf.http.object.util.NamedThreadFactory;
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
import org.openrdf.repository.object.ObjectRepository;
import org.openrdf.repository.object.RDFObject;
import org.openrdf.repository.util.RDFInserter;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
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
	private static final String NS = "http://persistent.name/rdf/2010/purl#";
	private static final String REL = NS + "rel";
	private static final String REMOTE_RESOURCE = NS + "RemoteResource";
	private static final String DEFINED_BY = NS + "definedBy";

	public static void canacelAllValidation() throws InterruptedException {
		List<Refresher> list;
		synchronized (alwaysFresh) {
			list = new ArrayList<Refresher>(alwaysFresh.values());
			alwaysFresh.clear();
		}
		for (Refresher refresher : list) {
			refresher.cancel(false);
		}
		for (Refresher refresher : list) {
			refresher.await();
		}
	}

	private static final ScheduledExecutorService executor = Executors
			.newSingleThreadScheduledExecutor(new NamedThreadFactory(
					"RemoteGraph"));
	private static final Map<Object, Refresher> alwaysFresh = new HashMap<Object, Refresher>();

	private final static class Refresher implements Runnable {
		private Logger logger = LoggerFactory.getLogger(Refresher.class);
		private final ObjectRepository repository;
		private final Resource subj;
		private ScheduledFuture<?> schedule;
		private volatile boolean running;
		private volatile boolean cancelled;
		private Object key;

		private Refresher(RDFObject object) {
			this.repository = object.getObjectConnection().getRepository();
			this.subj = object.getResource();
			key = Arrays.asList(new Object[] { repository, subj });
		}

		public void schedule(int freshness) {
			Refresher pre;
			synchronized (alwaysFresh) {
				pre = alwaysFresh.remove(key);
				alwaysFresh.put(key, this);
			}
			if (pre != null) {
				pre.cancel(false);
			}
			cancelled = false;
			logger.info("Mirror {}", subj);
			schedule = executor.schedule(this, freshness + 1, TimeUnit.SECONDS);
		}

		public synchronized void await() throws InterruptedException {
			while (running) {
				wait();
			}
		}

		public boolean cancel(boolean mayInterruptIfRunning) {
			cancelled = true;
			if (schedule == null)
				return false;
			logger.info("Stale {}", subj);
			return schedule.cancel(mayInterruptIfRunning);
		}

		public synchronized void run() {
			if (cancelled)
				return;
			running = true;
			try {
				ObjectConnection con = repository.getConnection();
				try {
					RemoteGraph graph = con.getObject(RemoteGraph.class, subj);
					int freshness;
					if (graph.reload(null)) {
						freshness = Math.max(graph.getFreshness(), 0);
					} else {
						freshness = Math.max(graph.getFreshness(), 4 * 60 * 60);
					}
					synchronized (alwaysFresh) {
						if (alwaysFresh.get(key) == this) {
							schedule = executor.schedule(this, freshness + 1,
									TimeUnit.SECONDS);
						}
					}
				} finally {
					con.close();
				}
			} catch (Exception e) {
				logger.error(e.toString());
				synchronized (alwaysFresh) {
					if (alwaysFresh.get(key) == this) {
						schedule = executor.schedule(this, 4 * 60 * 60,
								TimeUnit.SECONDS);
					}
				}
			} finally {
				running = false;
				notifyAll();
			}
		}
	}

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
	public boolean load(String origin) throws Exception {
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
		if (isAlwaysFresh() || isFresh())
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

	@Override
	public void stayFresh() throws Exception {
		Refresher refresher = new Refresher(this);
		int freshness = Math.max(getFreshness(), 0);
		synchronized (alwaysFresh) {
			if (alwaysFresh.containsKey(refresher.key)) {
				refresher = alwaysFresh.get(refresher.key);
			}
		}
		refresher.schedule(freshness);
	}

	@Override
	public void goStale() throws Exception {
		Refresher refresher = new Refresher(this);
		synchronized (alwaysFresh) {
			refresher = alwaysFresh.remove(refresher.key);
		}
		if (refresher != null) {
			refresher.cancel(false);
			refresher.await();
		}
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

	private boolean isAlwaysFresh() {
		Refresher refresher = new Refresher(this);
		synchronized (alwaysFresh) {
			return alwaysFresh.containsKey(refresher.key);
		}
	}

	private HttpResponse requestRDF(HttpRequest req, int max) throws Exception {
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
				HttpRequest req1 = new BasicHttpRequest("GET", location
						.getValue());
				return requestRDF(req1, (max - 1));
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
	}

	private boolean importResponse(HttpResponse resp, String origin)
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
			if (code == 410) {
				removeRemoteGraph();
				return false;
			}
			if (autoCommit) {
				con.setAutoCommit(false); // begin
			}
			InputStream in = entity.getContent();
			try {
				String type = getHeader(resp, "Content-Type");
				con.removeDesignation(this, Unresolvable.class);
				con.clear(ctx);
				if (!parse(origin, type, in))
					return false;
				store(origin, type, resp);
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

	private void store(String origin, String type, HttpResponse resp)
			throws Exception {
		ObjectConnection con = getObjectConnection();
		con.addDesignation(this, RemoteGraph.class);
		setPurlContentType(type);
		setPurlEtag(getHeader(resp, "ETag"));
		setPurlCacheControl(getHeader(resp, "Cache-Control"));
		DatatypeFactory df = DatatypeFactory.newInstance();
		GregorianCalendar gc = new GregorianCalendar();
		setPurlLastValidated(df.newXMLGregorianCalendar(gc));
		gc.setTime(getDateHeader(resp, "Last-Modified"));
		setPurlLastModified(df.newXMLGregorianCalendar(gc));
		if (origin != null) {
			ObjectFactory of = con.getObjectFactory();
			Origin o = of.createObject(origin, Origin.class);
			getPurlAllowedOrigins().add(o);
		}
		for (Header hd : resp.getHeaders("Warning")) {
			if (hd.getValue().contains("111")) {
				// 111 "Revalidation failed"
				con.addDesignation(this, Unresolvable.class);
			}
		}
		con.commit();
		logger.info("Updated {}", getResource());
		stayFresh();
	}

	private void removeRemoteGraph() throws Exception {
		ObjectConnection con = getObjectConnection();
		boolean autoCommit = con.isAutoCommit();
		try {
			goStale();
			if (autoCommit) {
				con.setAutoCommit(false); // begin
			}
			con.clear(getResource());
			setPurlCacheControl(null);
			setPurlEtag(null);
			setPurlLastModified(null);
			setPurlLastValidated(null);
			setPurlContentType(null);
			con.removeDesignation(this, RemoteGraph.class);
			con.removeDesignation(this, Unresolvable.class);
			con.commit();
			logger.info("Removed {}", getResource());
			goStale();
		} finally {
			if (autoCommit && !con.isAutoCommit()) {
				con.rollback();
				con.setAutoCommit(true);
			}
		}
	}

	private boolean parse(String origin, String type, InputStream in)
			throws IOException, RDFParseException, RDFHandlerException {
		Resource ctx = getResource();
		String resource = ctx.stringValue();
		ObjectConnection con = getObjectConnection();
		ValueFactory vf = con.getValueFactory();
		RemoteResourceInserter handler;
		List<String> origins = new ArrayList<String>();
		if (origin != null) {
			origins.add(origin);
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
		RDFParser parser = reg.get(format).getParser();
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
		Header hd = resp.getFirstHeader(name);
		if (hd == null)
			return null;
		return hd.getValue();
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
