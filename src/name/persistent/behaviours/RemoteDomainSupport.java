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
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import name.persistent.concepts.RemoteDomain;
import name.persistent.concepts.Service;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.message.BasicHttpRequest;
import org.apache.http.message.BasicStatusLine;
import org.openrdf.http.object.client.HTTPObjectClient;
import org.openrdf.http.object.exceptions.BadGateway;
import org.openrdf.http.object.exceptions.GatewayTimeout;
import org.openrdf.http.object.exceptions.InternalServerError;
import org.openrdf.http.object.threads.ManagedExecutors;
import org.openrdf.repository.object.RDFObject;

/**
 * Validates local PURLs and resolves remote PURLs.
 * 
 * @author James Leigh
 */
public abstract class RemoteDomainSupport extends MirroredDomainSupport implements RemoteDomain, RDFObject {
	private static final String PROTOCOL = "1.1";
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
		ManagedExecutors.getTimeoutThreadPool().scheduleWithFixedDelay(
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

	@Override
	protected Object getReloadGraph() {
		return getPurlServicedBy();
	}

	@Override
	public HttpResponse resolvePURL(String source, String qs,
			String accept, String language, Set<String> via)
			throws IOException, InterruptedException {
		stayFresh();
		return resolveRemotePURL(source, qs, accept, language, via, true, null);
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
