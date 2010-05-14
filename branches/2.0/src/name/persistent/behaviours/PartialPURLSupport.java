/*
 * Copyright (c) Zepheira LLC, Some rights reserved.
 * 
 * Source code developed for this project is licensed under the Apache
 * License, Version 2.0. See the file LICENSE.txt for details.
 */
package name.persistent.behaviours;

import info.aduna.net.ParsedURI;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import name.persistent.concepts.Domain;
import name.persistent.concepts.MirroredResource;
import name.persistent.concepts.PURL;
import name.persistent.concepts.RemoteGraph;
import name.persistent.concepts.RemoteResource;
import name.persistent.concepts.Resolvable;

import org.apache.http.HttpResponse;
import org.openrdf.http.object.exceptions.BadGateway;
import org.openrdf.http.object.exceptions.GatewayTimeout;
import org.openrdf.http.object.exceptions.NotFound;
import org.openrdf.http.object.util.SharedExecutors;
import org.openrdf.repository.object.ObjectConnection;
import org.openrdf.repository.object.ObjectFactory;
import org.openrdf.repository.object.ObjectQuery;
import org.openrdf.repository.object.RDFObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xbill.DNS.Address;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.SRVRecord;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;

/**
 * Finds a PURL (possibly remote) for a request.
 * 
 * @author James Leigh
 */
public abstract class PartialPURLSupport extends MirrorSupport implements
		RDFObject, Resolvable {
	private static final String PREFIX = "PREFIX purl:<http://persistent.name/rdf/2010/purl#>\n"
			+ "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n";
	private static final ScheduledExecutorService executor = SharedExecutors
			.getTimeoutThreadPool();
	private static final Map<InetSocketAddress, Boolean> blackList = new ConcurrentHashMap<InetSocketAddress, Boolean>();
	private static ThreadLocal<Random> random = new ThreadLocal<Random>() {
		protected Random initialValue() {
			return new Random(System.nanoTime());
		}
	};
	static {
		executor.scheduleWithFixedDelay(new Runnable() {
			public void run() {
				blackList.clear();
			}
		}, 1, 4, TimeUnit.HOURS);
	}
	private Logger logger = LoggerFactory.getLogger(PartialPURLSupport.class);

	public HttpResponse resolvePURL(String source, String qs, String accept,
			String language, Set<String> via) throws Exception {
		return resolvePURL(source, qs, accept, language, via, 4);
	}

	protected List<InetSocketAddress> getOriginServices(boolean useBlackList)
			throws Exception {
		List<InetSocketAddress> blacklisted = getBlackListing(useBlackList);
		Collection<List<SRVRecord>> records = getServiceRecords();
		List<InetSocketAddress> result = new ArrayList<InetSocketAddress>();
		for (List<SRVRecord> servers : records) {
			InetSocketAddress server = pickService(servers);
			if (server != null) {
				result.add(server);
			}
		}
		if (blacklisted != null && result.isEmpty() && !records.isEmpty()) {
			blackList.keySet().removeAll(blacklisted);
			return getOriginServices(false);
		}
		if (result.isEmpty())
			throw new NotFound("Missing SRV records");
		return result;
	}

	private List<InetSocketAddress> getBlackListing(boolean useBlackList) {
		if (useBlackList && !blackList.isEmpty())
			return new ArrayList<InetSocketAddress>(blackList.keySet());
		return null;
	}

	private HttpResponse resolvePURL(String source, String qs, String accept,
			String language, Set<String> via, int maxload) throws Exception {
		if (maxload < 0)
			throw new BadGateway("Unknown PURL");
		ObjectConnection con = getObjectConnection();
		String sparql = createSPARQL(source);
		ObjectQuery query = con.prepareObjectQuery(sparql);
		List<?> result = query.evaluate().asList();
		if (result.isEmpty()) {
			loadOrigin();
			return resolvePURL(source, qs, accept, language, via, maxload - 1);
		} else {
			Object purl = result.get(0);
			if (purl instanceof PURL) {
				return ((PURL) purl).resolvePURL(source, qs, accept, language,
						via);
			} else if (purl instanceof Domain) {
				Domain domain = (Domain) purl;
				if (domain instanceof RemoteResource) {
					if (!((RemoteResource) domain).reload()) {
						loadOrigin();
						return resolvePURL(source, qs, accept, language, via,
								maxload - 1);
					}
				}
				if (domain instanceof MirroredResource
						|| !domain.getCalliMaintainers().isEmpty())
					throw new NotFound("Unknown Persistent URL");
				return domain.resolveRemotePURL(source, qs, accept, language,
						via);
			} else if (purl instanceof RemoteResource) {
				if (!((RemoteResource) purl).load()) {
					loadOrigin();
				}
				return resolvePURL(source, qs, accept, language, via,
						maxload - 1);
			} else {
				throw new AssertionError("Invalid resource type: " + purl);
			}
		}
	}

	private void loadOrigin() throws Exception {
		ParsedURI parsed = new ParsedURI(getResource().stringValue());
		String scheme = parsed.getScheme();
		String auth = parsed.getAuthority();
		assert auth != null;
		String originURI = new ParsedURI(scheme, auth, "/", null, null)
				.toString();
		ObjectConnection con = getObjectConnection();
		ObjectFactory of = con.getObjectFactory();
		Exception gateway = null;
		for (InetSocketAddress addr : getOriginServices(true)) {
			String host = addr.getHostName();
			if ("http".equalsIgnoreCase(scheme) && addr.getPort() != 80) {
				host += ":" + addr.getPort();
			} else if ("https".equalsIgnoreCase(scheme)
					&& addr.getPort() != 443) {
				host += ":" + addr.getPort();
			}
			String uri = new ParsedURI(scheme, host, "/", null, null)
					.toString();
			try {
				RemoteGraph origins = of.createObject(uri, RemoteGraph.class);
				if (!origins.load(originURI))
					continue;
				RemoteResource origin = of.createObject(originURI,
						RemoteResource.class);
				if (origin.load())
					return;
			} catch (GatewayTimeout timeout) {
				gateway = timeout;
				blackList(addr, timeout);
			} catch (BadGateway bad) {
				gateway = bad;
				blackList(addr, bad);
			}
		}
		if (gateway != null)
			throw gateway;
		throw new NotFound("No PURL Server Available");
	}

	private void blackList(InetSocketAddress server, Exception reason) {
		logger.warn(reason.toString());
		blackList.put(server, Boolean.TRUE);
	}

	private Collection<List<SRVRecord>> getServiceRecords()
			throws TextParseException {
		ParsedURI parsed = new ParsedURI(getResource().stringValue());
		String authority = parsed.getAuthority();
		int start = authority.indexOf('@');
		int end = authority.lastIndexOf(':');
		if (end < 0) {
			end = authority.length();
		}
		String hostname = authority.substring(start + 1, end);
		String service = "_purl._http." + hostname;
		Record[] records = new Lookup(service, Type.SRV).run();
		if (records == null && hostname.contains(".")) {
			service = "_purl._http."
					+ hostname.substring(hostname.indexOf('.') + 1);
			records = new Lookup(service, Type.SRV).run();
		}
		if (records == null) {
			return Collections.emptySet();
		}
		Map<Integer, List<SRVRecord>> map = new TreeMap<Integer, List<SRVRecord>>();
		for (int i = 0; records != null && i < records.length; i++) {
			SRVRecord srv = (SRVRecord) records[i];
			int priority = srv.getPriority();
			List<SRVRecord> list = map.get(priority);
			if (list == null) {
				map.put(priority, list = new ArrayList<SRVRecord>());
			}
			list.add(srv);
		}
		return map.values();
	}

	private InetSocketAddress pickService(List<SRVRecord> servers) {
		int total = 0;
		List<InetSocketAddress> addresses = new ArrayList<InetSocketAddress>();
		for (int i = 0, n = servers.size(); i < n; i++) {
			addresses.add(null);
			SRVRecord srv = servers.get(i);
			int port = srv.getPort();
			String name = srv.getTarget().toString();
			if (name.endsWith(".")) {
				name = name.substring(0, name.length() - 1);
			}
			try {
				InetAddress addr = Address.getByName(name);
				InetSocketAddress server = new InetSocketAddress(addr, port);
				if (isBlackListed(server) || server.isUnresolved())
					continue;
				addresses.set(i, server);
				total += srv.getWeight();
			} catch (UnknownHostException e) {
				logger.warn("{}: {}", e.toString(), name);
			}
		}
		total = random(total);
		for (int i = 0, n = servers.size(); i < n; i++) {
			SRVRecord srv = servers.get(i);
			if (addresses.get(i) == null)
				continue;
			total -= srv.getWeight();
			if (total < 0) {
				return addresses.get(i);
			}
		}
		return null;
	}

	private boolean isBlackListed(InetSocketAddress server) {
		return blackList.containsKey(server);
	}

	private int random(int total) {
		if (total <= 0)
			return total;
		return random.get().nextInt(total);
	}

	private String createSPARQL(String source) {
		StringBuilder sb = new StringBuilder();
		sb.append(PREFIX);
		sb.append("SELECT REDUCED ?purl");
		sb.append("\nWHERE {");
		sb.append("\n\t{ ?purl a purl:PartialPURL }");
		sb
				.append("\n\tUNION {?purl ?zoned purl:ZonedPURL FILTER(?zoned = rdf:type)}");
		sb.append("\n\tUNION {?origin purl:part ?purl }");
		sb.append("\n\tUNION {?purl a purl:RemoteResource }");
		sb.append("\nFILTER (?purl = <").append(source).append(">");
		ParsedURI uri = new ParsedURI(source);
		if (uri.isHierarchical()) {
			for (String match : pathFragments(uri, new ArrayList<String>())) {
				sb.append("\n\t|| ?purl = <").append(match).append(">");
			}
			String s = uri.getScheme();
			String a = uri.getAuthority();
			int at = a.indexOf('@');
			int idx = a.indexOf('.', at + 1);
			if (at > 0 && idx > 0) {
				a = a.substring(0, at + 1) + a.substring(idx + 1);
			} else if (idx > 0) {
				a = a.substring(idx + 1);
			}
			String path = uri.getPath();
			ParsedURI domain = new ParsedURI(s, a, path, null, null);
			String duri = domain.toString();
			sb.append("\n\t|| ?purl = <").append(duri).append(">");
			for (String match : pathFragments(domain, new ArrayList<String>())) {
				sb.append("\n\t|| ?purl = <").append(match).append(">");
			}
		}
		sb.append(")\n}\nORDER BY ?zoned desc(?purl)");
		return sb.toString();
	}

	private List<String> pathFragments(ParsedURI source, List<String> matches) {
		String s = source.getScheme();
		String a = source.getAuthority();
		String p = source.getPath();
		if (p.length() > 1 && p.startsWith("/")) {
			int idx = p.lastIndexOf('/', p.length() - 2);
			ParsedURI uri = new ParsedURI(s, a, p.substring(0, idx + 1), null,
					null);
			matches.add(uri.toString());
			return pathFragments(uri, matches);
		}
		return matches;
	}

}
