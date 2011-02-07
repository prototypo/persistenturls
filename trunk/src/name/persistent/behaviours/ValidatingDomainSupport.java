/*
 * Copyright (c) Zepheira LLC, Some rights reserved.
 * 
 * Source code developed for this project is licensed under the Apache
 * License, Version 2.0. See the file LICENSE.txt for details.
 */
package name.persistent.behaviours;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TimeZone;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import name.persistent.concepts.Domain;
import name.persistent.concepts.Redirection;
import name.persistent.concepts.Unresolvable;

import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.message.BasicHttpRequest;
import org.openrdf.OpenRDFException;
import org.openrdf.http.object.client.HTTPObjectClient;
import org.openrdf.http.object.threads.ManagedExecutors;
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
import org.openrdf.repository.object.annotations.sparql;
import org.openrdf.repository.object.annotations.triggeredBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Validates local PURLs and resolves remote PURLs.
 * 
 * @author James Leigh
 */
public abstract class ValidatingDomainSupport implements Domain, RDFObject {
	private static final String NS = "http://persistent.name/rdf/2010/purl#";
	private static final String PREFIX = "PREFIX purl:<http://persistent.name/rdf/2010/purl#>\n";
	private static final String ALL_TARGETS = PREFIX
			+ "SELECT REDUCED ?target\n"
			+ "WHERE { { ?purl purl:partOf $this }\n"
			+ "UNION { ?purl purl:partOf [purl:belongsTo $this] }\n"
			+ "UNION { ?purl a ?type FILTER (?purl = $this) }\n"
			+ "?purl ?pred ?target . ?pred purl:rel ?rel .\n"
			+ "OPTIONAL { ?purl purl:pattern ?pattern } FILTER (!bound(?pattern))\n"
			+ "}";
	private static final String TARGET_BY_DATE = PREFIX
			+ "SELECT REDUCED ?target\n"
			+ "WHERE { { ?purl purl:partOf $this }\n"
			+ "UNION { ?purl purl:partOf [purl:belongsTo $this] }\n"
			+ "UNION { ?purl a ?type FILTER (?purl = $this) }\n"
			+ "?purl ?pred ?target . ?pred purl:rel ?rel .\n"
			+ "OPTIONAL { ?purl purl:pattern ?pattern } FILTER (!bound(?pattern))\n"
			+ "?target purl:last-resolved $date }";
	private static final String TARGET_WITHOUT_DATE = PREFIX
			+ "SELECT REDUCED ?target\n"
			+ "WHERE { { ?purl purl:partOf $this }\n"
			+ "UNION { ?purl purl:partOf [purl:belongsTo $this] }\n"
			+ "UNION { ?purl a ?type FILTER (?purl = $this) }\n"
			+ "?purl ?pred ?target . ?pred purl:rel ?rel .\n"
			+ "OPTIONAL { ?purl purl:pattern ?pattern } FILTER (!bound(?pattern))\n"
			+ "OPTIONAL { ?target purl:last-resolved ?last }\n"
			+ "FILTER (!bound(?last))}";
	private static final String TARGET_BEFORE_DATE = PREFIX
			+ "SELECT REDUCED ?target\n"
			+ "WHERE { { ?purl purl:partOf $this }\n"
			+ "UNION { ?purl purl:partOf [purl:belongsTo $this] }\n"
			+ "UNION { ?purl a ?type FILTER (?purl = $this) }\n"
			+ "?purl ?pred ?target . ?pred purl:rel ?rel .\n"
			+ "OPTIONAL { ?purl purl:pattern ?pattern } FILTER (!bound(?pattern))\n"
			+ "?target purl:last-resolved ?last\n" + "FILTER (?last < $date) }";
	private static ThreadLocal<Random> random = new ThreadLocal<Random>() {
		protected Random initialValue() {
			return new Random(System.nanoTime());
		}
	};

	private static final ScheduledExecutorService executor = ManagedExecutors
			.newSingleScheduler("URL Resolver");
	private static final Map<Object, Resolver> resolvers = new HashMap<Object, Resolver>();

	private final static class Resolver implements Runnable {
		private Logger logger = LoggerFactory.getLogger(Resolver.class);
		private final ObjectRepository repository;
		private final Resource subj;
		private ScheduledFuture<?> schedule;
		private volatile boolean cancelled;
		private Object key;
		private int interval;
		private int maxCount;

		private Resolver(RDFObject object) {
			this.repository = object.getObjectConnection().getRepository();
			this.subj = object.getResource();
			key = Arrays.asList(new Object[] { repository, subj });
		}

		@Override
		public String toString() {
			return "resolve " + subj + " targets";
		}

		public void schedule(int period, TimeUnit unit, int maxCount) {
			assert period > 0;
			assert maxCount > 0;
			Resolver pre;
			synchronized (resolvers) {
				pre = resolvers.remove(key);
				resolvers.put(key, this);
			}
			if (pre != null) {
				pre.cancel(false);
			}
			cancelled = false;
			this.maxCount = maxCount;
			interval = (int) unit.toSeconds(period);
			int delay = random.get().nextInt(interval) + 60;
			TimeUnit sec = TimeUnit.SECONDS;
			schedule = executor.scheduleAtFixedRate(this, delay, interval, sec);
			logger.info("Validating {} in {} hours and every {} hours",
					new Object[] { subj, sec.toHours(delay),
							sec.toHours(interval) });
		}

		public boolean cancel(boolean mayInterruptIfRunning) {
			logger.info("Stopped validating {}", subj);
			cancelled = true;
			synchronized (resolvers) {
				Resolver pre = resolvers.get(key);
				if (pre.cancelled) {
					resolvers.remove(key);
				}
			}
			if (schedule == null)
				return false;
			return schedule.cancel(mayInterruptIfRunning);
		}

		@Override
		public synchronized void run() {
			if (cancelled)
				return;
			try {
				ObjectConnection con = repository.getConnection();
				try {
					Domain domain = con.getObject(Domain.class, subj);
					int count;
					Integer days = domain.getPurlMaxUnresolvedDays();
					if (days == null || days < 1) {
						cancel(false);
					} else if ((count = domain.countTargets()) > 0) {
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
						int size = Math.min(maxCount, count);
						int min = Math.max(1, size / periods);
						int max = (size + periods - 1) / periods;
						domain.validatePURLs(xgc, min, max, now);
						if (!cancelled && maxCount < count) {
							domain.stopResolving();
							domain.startResolving();
						}
					}
				} finally {
					con.close();
				}
			} catch (Exception e) {
				logger.error(e.toString(), e);
			}
		}
	}

	@triggeredBy("http://persistent.name/rdf/2010/purl#max-unresolved-days")
	public void changePurlMaxUnresolvedDays(Integer days)
			throws QueryEvaluationException {
		if (days != null && days > 0) {
			if (!isResolving()) {
				startResolving(days);
			}
		}
	}

	@Override
	public boolean startResolving() throws QueryEvaluationException {
		return startResolving(getPurlMaxUnresolvedDays());
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
		return true;
	}

	@Override
	public int countTargets() throws QueryEvaluationException {
		TupleQueryResult result = findTargets();
		try {
			int count;
			for (count = 0; result.hasNext(); count++) {
				result.next();
			}
			return count;
		} finally {
			result.close();
		}
	}

	@Override
	public void validatePURLs(XMLGregorianCalendar xgc, int min, int max,
			XMLGregorianCalendar today) throws OpenRDFException, IOException {
		List<Value> targets = findTargetResolvedBefore(xgc, min, max);
		markResolvability(resolveTargets(targets), today);
	}

	@sparql(ALL_TARGETS)
	protected abstract TupleQueryResult findTargets();

	private boolean startResolving(Integer days)
			throws QueryEvaluationException {
		if (days == null || days < 1)
			return false;
		int count = Math.max(1, countTargets());
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
		resolver.schedule(interval, TimeUnit.HOURS, Math.max(100, count * 2));
		return true;
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
		Map<URI, Integer> codes = new LinkedHashMap<URI, Integer>();
		HTTPObjectClient client = HTTPObjectClient.getInstance();
		for (Value target : targets) {
			String url = target.stringValue();
			if (!codes.containsKey(target) && target instanceof URI) {
				HttpRequest request = new BasicHttpRequest("HEAD", url);
				HttpResponse resp = client.service(request);
				try {
					int code = resp.getStatusLine().getStatusCode();
					codes.put((URI) target, code);
				} finally {
					HttpEntity entity = resp.getEntity();
					if (entity != null) {
						entity.consumeContent();
					}
				}
			}
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

}
