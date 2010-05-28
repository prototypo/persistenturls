/*
 * Copyright (c) Zepheira LLC, Some rights reserved.
 * 
 * Source code developed for this project is licensed under the Apache
 * License, Version 2.0. See the file LICENSE.txt for details.
 */
package name.persistent.behaviours;

import info.aduna.net.ParsedURI;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import name.persistent.concepts.Domain;
import name.persistent.concepts.Redirection;
import name.persistent.concepts.Service;
import name.persistent.concepts.Unresolvable;

import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.message.BasicHttpRequest;
import org.apache.http.message.BasicStatusLine;
import org.openrdf.OpenRDFException;
import org.openrdf.http.object.client.HTTPObjectClient;
import org.openrdf.http.object.exceptions.BadGateway;
import org.openrdf.http.object.exceptions.GatewayTimeout;
import org.openrdf.http.object.exceptions.InternalServerError;
import org.openrdf.http.object.util.NamedThreadFactory;
import org.openrdf.http.object.util.SharedExecutors;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.object.ObjectConnection;
import org.openrdf.repository.object.ObjectFactory;
import org.openrdf.repository.object.ObjectRepository;
import org.openrdf.repository.object.RDFObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Validates local PURLs and resolves remote PURLs.
 * 
 * @author James Leigh
 */
public abstract class DomainSupport implements Domain, RDFObject {
	private static final String PROTOCOL = "1.1";
	private static final String NS = "http://persistent.name/rdf/2010/purl#";
	private static final String PREFIX = "PREFIX purl:<http://persistent.name/rdf/2010/purl#>\n";
	private static final String TARGET_BY_DATE = PREFIX
			+ "SELECT REDUCED ?target\n"
			+ "WHERE { { ?purl purl:partOf $this }\n"
			+ "UNION { ?purl a purl:Domain FILTER (?purl = $this) }\n"
			+ "?purl ?pred ?target . ?pred purl:rel ?rel .\n"
			+ "OPTIONAL { ?purl purl:pattern ?pattern } FILTER (!bound(?pattern))\n"
			+ "?target purl:last-resolved $date }";
	private static final String TARGET_WITHOUT_DATE = PREFIX
			+ "SELECT REDUCED ?target\n"
			+ "WHERE { { ?purl purl:partOf $this }\n"
			+ "UNION { ?purl a purl:Domain FILTER (?purl = $this) }\n"
			+ "?purl ?pred ?target . ?pred purl:rel ?rel .\n"
			+ "OPTIONAL { ?purl purl:pattern ?pattern } FILTER (!bound(?pattern))\n"
			+ "OPTIONAL { ?target purl:last-resolved ?last }\n"
			+ "FILTER (!bound(?last))}";
	private static final String TARGET_BEFORE_DATE = PREFIX
			+ "SELECT REDUCED ?target\n"
			+ "WHERE { { ?purl purl:partOf $this }\n"
			+ "UNION { ?purl a purl:Domain FILTER (?purl = $this) }\n"
			+ "?purl ?pred ?target . ?pred purl:rel ?rel .\n"
			+ "OPTIONAL { ?purl purl:pattern ?pattern } FILTER (!bound(?pattern))\n"
			+ "?target purl:last-resolved ?last\n" + "FILTER (?last < $date) }";
	private static final String VIA;
	static {
		String host = "localhost";
		try {
			host = InetAddress.getLocalHost().getCanonicalHostName();
		} catch (UnknownHostException e) {
			// ignore
		}
		VIA = PROTOCOL + " " + host;
	}
	private static Map<InetSocketAddress, Boolean> blackList = new ConcurrentHashMap<InetSocketAddress, Boolean>();
	static {
		SharedExecutors.getTimeoutThreadPool().scheduleWithFixedDelay(
				new Runnable() {
					public void run() {
						blackList.clear();
					}
				}, 1, 4, TimeUnit.HOURS);
	}

	private static ThreadLocal<Random> random = new ThreadLocal<Random>() {
		protected Random initialValue() {
			return new Random(System.nanoTime());
		}
	};

	private static final ScheduledExecutorService executor = Executors
			.newSingleThreadScheduledExecutor(new NamedThreadFactory(
					"URL Resolver"));
	private static final Map<Object, Resolver> resolvers = new HashMap<Object, Resolver>();

	private final static class Resolver implements Runnable {
		private Logger logger = LoggerFactory.getLogger(Resolver.class);
		private final ObjectRepository repository;
		private final Resource subj;
		private ScheduledFuture<?> schedule;
		private volatile boolean running;
		private volatile boolean cancelled;
		private Object key;
		private int interval;

		private Resolver(RDFObject object) {
			this.repository = object.getObjectConnection().getRepository();
			this.subj = object.getResource();
			key = Arrays.asList(new Object[] { repository, subj });
		}

		public void schedule(int period, TimeUnit unit) {
			assert period > 0;
			Resolver pre;
			synchronized (resolvers) {
				pre = resolvers.remove(key);
				resolvers.put(key, this);
			}
			if (pre != null) {
				pre.cancel(false);
			}
			cancelled = false;
			interval = (int) unit.toSeconds(period);
			int delay = random.get().nextInt(interval);
			schedule = executor.scheduleAtFixedRate(this, delay, interval,
					TimeUnit.SECONDS);
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
			return schedule.cancel(mayInterruptIfRunning);
		}

		@Override
		public synchronized void run() {
			if (cancelled)
				return;
			running = true;
			try {
				ObjectConnection con = repository.getConnection();
				try {
					Domain domain = con.getObject(Domain.class, subj);
					Integer count = domain.getPurlTargetCount();
					Integer days = domain.getPurlMaxUnresolvedDays();
					if (count == null || days == null || days < 1 || count < 1) {
						cancel(false);
					} else {
						GregorianCalendar cal;
						XMLGregorianCalendar xgc, now;
						int n = DatatypeConstants.FIELD_UNDEFINED;
						DatatypeFactory f = DatatypeFactory.newInstance();
						cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
						now = f.newXMLGregorianCalendar(cal);
						now.setTime(n, n, n, n);
						xgc = f.newXMLGregorianCalendar(cal);
						xgc.setTime(n, n, n, n);
						xgc.add(f.newDurationDayTime(false, days, 0, 0, 0));
						int periods = Math.max(1, (int) TimeUnit.DAYS
								.toSeconds(days)
								/ interval);
						int min = Math.max(1, count / periods);
						int max = (count + periods - 1) / periods;
						domain.validatePURLs(xgc, min, max, now);
					}
				} finally {
					con.close();
				}
			} catch (Exception e) {
				logger.error(e.toString(), e);
			} finally {
				running = false;
				notifyAll();
			}
		}
	}

	private Logger logger = LoggerFactory.getLogger(DomainSupport.class);

	@Override
	public boolean startResolving() {
		Integer count = getPurlTargetCount();
		Integer days = getPurlMaxUnresolvedDays();
		if (count == null || days == null || days < 1 || count < 1)
			return false;
		Resolver resolver = new Resolver(this);
		synchronized (resolvers) {
			if (resolvers.containsKey(resolver.key)) {
				resolver = resolvers.get(resolver.key);
			} else {
				resolvers.put(resolver.key, resolver);
			}
		}
		int periods = (count + 99) / 100; // check 100 targets at a time
		long maxHours = TimeUnit.DAYS.toHours(days);
		int interval = Math.max(1, (int) maxHours / periods);
		resolver.schedule(interval, TimeUnit.HOURS);
		return true;
	}

	@Override
	public boolean isResolving() {
		Resolver resolver = new Resolver(this);
		synchronized (resolvers) {
			return resolvers.containsKey(resolver.key);
		}
	}

	@Override
	public boolean stopResolving() throws InterruptedException {
		Resolver resolver = new Resolver(this);
		synchronized (resolvers) {
			resolver = resolvers.remove(resolver.key);
		}
		if (resolver == null)
			return false;
		resolver.cancel(false);
		resolver.await();
		return true;
	}

	@Override
	public HttpResponse resolveRemotePURL(String source, String qs,
			String accept, String language, Set<String> via)
			throws IOException, InterruptedException {
		return resolveRemotePURL(source, qs, accept, language, via, true, null);
	}

	@Override
	public void validatePURLs(XMLGregorianCalendar xgc, int min, int max,
			XMLGregorianCalendar today) throws OpenRDFException, IOException {
		List<Value> targets = findTargetResolvedBefore(xgc, min, max);
		markResolvability(resolveTargets(targets), today);
	}

	private List<Value> findTargetResolvedBefore(XMLGregorianCalendar xgc,
			int min, int max) throws OpenRDFException {
		assert max >= min;
		List<Value> targets = new ArrayList<Value>(max);
		addTo(findTargetByLastResolved(xgc, max), targets);
		if (targets.size() < min) {
			addTo(findUnresolvedTarget(max - targets.size()), targets);
			if (targets.size() < min) {
				addTo(findResolvedTargetBefore(xgc, max - targets.size()),
						targets);
			}
		}
		return targets;
	}

	private TupleQueryResult findTargetByLastResolved(XMLGregorianCalendar xgc,
			int limit) throws OpenRDFException {
		String qry = TARGET_BY_DATE + "\nLIMIT " + limit;
		ObjectConnection con = getObjectConnection();
		ValueFactory vf = con.getValueFactory();
		TupleQuery query = con.prepareTupleQuery(qry);
		query.setBinding("this", getResource());
		query.setBinding("date", vf.createLiteral(xgc));
		return query.evaluate();
	}

	private TupleQueryResult findUnresolvedTarget(int limit)
			throws OpenRDFException {
		String qry = TARGET_WITHOUT_DATE + "\nLIMIT " + limit;
		ObjectConnection con = getObjectConnection();
		TupleQuery query = con.prepareTupleQuery(qry);
		query.setBinding("this", getResource());
		return query.evaluate();
	}

	private TupleQueryResult findResolvedTargetBefore(XMLGregorianCalendar xgc,
			int limit) throws OpenRDFException {
		String qry = TARGET_BEFORE_DATE + "\nLIMIT " + limit;
		ObjectConnection con = getObjectConnection();
		ValueFactory vf = con.getValueFactory();
		TupleQuery query = con.prepareTupleQuery(qry);
		query.setBinding("this", getResource());
		query.setBinding("date", vf.createLiteral(xgc));
		return query.evaluate();
	}

	private void addTo(TupleQueryResult result, Collection<Value> set)
			throws QueryEvaluationException {
		try {
			String name = result.getBindingNames().get(0);
			while (result.hasNext()) {
				set.add(result.next().getValue(name));
			}
		} finally {
			result.close();
		}
	}

	private Map<URI, Integer> resolveTargets(List<Value> targets)
			throws IOException {
		Map<URI, Future<HttpResponse>> responses;
		responses = new LinkedHashMap<URI, Future<HttpResponse>>(targets.size());
		HTTPObjectClient client = HTTPObjectClient.getInstance();
		for (Value target : targets) {
			String url = target.stringValue();
			if (!responses.containsKey(target) && target instanceof URI) {
				HttpRequest request = new BasicHttpRequest("HEAD", url);
				responses.put((URI) target, client.submitRequest(request));
			}
		}
		Map<URI, Integer> codes = new LinkedHashMap<URI, Integer>(responses
				.size());
		for (Entry<URI, Future<HttpResponse>> e : responses.entrySet()) {
			try {
				HttpResponse resp = e.getValue().get();
				try {
					int code = resp.getStatusLine().getStatusCode();
					codes.put(e.getKey(), code);
					continue;
				} finally {
					HttpEntity entity = resp.getEntity();
					if (entity != null) {
						entity.consumeContent();
					}
				}
			} catch (InterruptedException ex) {
				logger.info(ex.toString());
			} catch (ExecutionException ex) {
				logger.warn(ex.toString(), ex);
			}
			codes.put(e.getKey(), 504);
		}
		return codes;
	}

	private void markResolvability(Map<URI, Integer> codes,
			XMLGregorianCalendar today) throws RepositoryException {
		ObjectConnection con = getObjectConnection();
		ValueFactory vf = con.getValueFactory();
		ObjectFactory of = con.getObjectFactory();
		URI lastResolved = vf.createURI(NS, "last-resolved");
		Literal now = vf.createLiteral(today);
		boolean autoCommit = con.isAutoCommit();
		con.setAutoCommit(false); // begin
		try {
			for (Entry<URI, Integer> e : codes.entrySet()) {
				con.remove(e.getKey(), lastResolved, null);
				if (e.getValue() < 400) {
					con.removeDesignation(of.createObject(e.getKey()),
							Unresolvable.class);
				}
				if (e.getValue() < 300) {
					con.removeDesignation(of.createObject(e.getKey()),
							Redirection.class);
				}
			}
			for (Entry<URI, Integer> e : codes.entrySet()) {
				con.add(e.getKey(), lastResolved, now);
				if (e.getValue() >= 400) {
					con.addDesignation(of.createObject(e.getKey()),
							Unresolvable.class);
				} else if (e.getValue() >= 300) {
					con.addDesignation(of.createObject(e.getKey()),
							Redirection.class);
				}
			}
			if (autoCommit) {
				con.setAutoCommit(true); // commit
			}
		} finally {
			if (autoCommit && !con.isAutoCommit()) {
				con.rollback();
				con.setAutoCommit(false);
			}
		}
	}

	private HttpResponse resolveRemotePURL(String source, String qs,
			String accept, String language, Set<String> via,
			boolean useBlackList, HttpResponse bad) throws IOException, InterruptedException {
		List<InetSocketAddress> blacklisted = getBlackListing(useBlackList);
		Collection<List<Service>> records = getAllPURLServices();
		for (List<Service> services : records) {
			InetSocketAddress addr = pickService(services);
			if (addr != null) {
				HttpResponse resp = resolveRemotePURL(addr, source, qs, accept,
						language, via);
				if (resp == null)
					continue;
				StatusLine status = resp.getStatusLine();
				if (status.getStatusCode() >= 500 && bad == null) {
					bad = resp;
				} else if (status.getStatusCode() >= 500 && bad != null) {
					HttpEntity entity = resp.getEntity();
					if (entity != null) {
						entity.consumeContent();
					}
				} else {
					if (bad != null) {
						HttpEntity entity = bad.getEntity();
						if (entity != null) {
							entity.consumeContent();
						}
					}
					return resp;
				}
			}
		}
		if (useBlackList && (blacklisted != null || !blackList.isEmpty())
				&& !records.isEmpty()) {
			if (blacklisted != null) {
				blackList.keySet().removeAll(blacklisted);
			}
			return resolveRemotePURL(source, qs, accept, language, via, false, bad);
		}
		if (bad != null)
			return bad;
		throw new BadGateway("Couldn't Find Server");
	}

	private HttpResponse resolveRemotePURL(InetSocketAddress addr,
			String source, String qs, String accept, String language,
			Set<String> via) throws IOException, InterruptedException {
		HTTPObjectClient client = HTTPObjectClient.getInstance();
		String url = qs == null ? source : source + "?" + qs;
		BasicHttpRequest req = new BasicHttpRequest("GET", url);
		if (accept != null) {
			req.setHeader("Accept", accept);
		}
		if (language != null) {
			req.setHeader("Accept-Language", language);
		}
		StringBuilder sb = new StringBuilder();
		for (String v : via) {
			if (v.contains(VIA) && (v.endsWith(VIA) || v.contains(VIA + ",")))
				throw new InternalServerError("Request Loop Detected\n" + via
						+ "\n" + VIA);
			if (sb.length() > 0) {
				sb.append(",");
			}
			sb.append(v);
		}
		sb.append(VIA);
		req.setHeader("Via", sb.toString());
		try {
			HttpResponse resp = client.service(addr, req);
			if (!resp.containsHeader("Via")) {
				String original = "1.1 " + addr.getHostName();
				if (addr.getPort() != 80 && addr.getPort() != 443) {
					original += ":" + addr.getPort();
				}
				resp.addHeader("Via", original);
			}
			StatusLine status = resp.getStatusLine();
			if (status.getStatusCode() >= 500) {
				ProtocolVersion ver = status.getProtocolVersion();
				String phrase = status.getReasonPhrase();
				resp.setStatusLine(new BasicStatusLine(ver, 502, phrase));
				blackList.put(addr, Boolean.TRUE);
				return resp;
			} else {
				return resp;
			}
		} catch (GatewayTimeout e) {
			blackList.put(addr, Boolean.TRUE);
			return null;
		}
	}

	private List<InetSocketAddress> getBlackListing(boolean useBlackList) {
		if (useBlackList && !blackList.isEmpty())
			return new ArrayList<InetSocketAddress>(blackList.keySet());
		return null;
	}

	private Collection<List<Service>> getAllPURLServices() {
		Map<Number, List<Service>> map = new TreeMap<Number, List<Service>>();
		for (Service srv : getPurlServices()) {
			Number priority = srv.getPurlPriority();
			if (priority == null) {
				priority = 0;
			}
			List<Service> list = map.get(priority);
			if (list == null) {
				map.put(priority, list = new ArrayList<Service>());
			}
			list.add(srv);
		}
		return map.values();
	}

	private InetSocketAddress pickService(List<Service> services) {
		int total = 0;
		List<InetSocketAddress> addresses = new ArrayList<InetSocketAddress>();
		for (int i = 0, n = services.size(); i < n; i++) {
			addresses.add(null);
			Service srv = services.get(i);
			Object url = srv.getPurlServer();
			if (url == null)
				continue;
			ParsedURI parsed = new ParsedURI(((RDFObject) url).getResource()
					.stringValue());
			int port = "https".equalsIgnoreCase(parsed.getScheme()) ? 443 : 80;
			InetSocketAddress server = resolve(parsed.getAuthority(), port);
			if (isBlackListed(server) || server.isUnresolved())
				continue;
			addresses.set(i, server);
			Number weight = srv.getPurlWeight();
			total += weight == null ? 1 : weight.intValue();
		}
		total = random(total);
		for (int i = 0, n = services.size(); i < n; i++) {
			Service srv = services.get(i);
			if (addresses.get(i) == null)
				continue;
			Number weight = srv.getPurlWeight();
			total -= weight == null ? 1 : weight.intValue();
			if (total < 0) {
				return addresses.get(i);
			}
		}
		return null;
	}

	private InetSocketAddress resolve(String authority, int port) {
		if (authority.contains("@")) {
			authority = authority.substring(authority.indexOf('@') + 1);
		}
		String hostname = authority;
		if (hostname.contains(":")) {
			hostname = hostname.substring(0, hostname.indexOf(':'));
		}
		if (authority.contains(":")) {
			int idx = authority.indexOf(':') + 1;
			port = Integer.parseInt(authority.substring(idx));
		}
		return new InetSocketAddress(hostname, port);
	}

	private boolean isBlackListed(InetSocketAddress server) {
		return blackList.containsKey(server);
	}

	private int random(int total) {
		if (total <= 0)
			return total;
		return random.get().nextInt(total);
	}

}
