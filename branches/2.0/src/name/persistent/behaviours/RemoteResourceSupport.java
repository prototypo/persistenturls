/*
 * Copyright (c) Zepheira LLC, Some rights reserved.
 * 
 * Source code developed for this project is licensed under the Apache
 * License, Version 2.0. See the file LICENSE.txt for details.
 */
package name.persistent.behaviours;

import info.aduna.net.ParsedURI;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import name.persistent.concepts.RemoteGraph;
import name.persistent.concepts.RemoteResource;
import name.persistent.concepts.Unresolvable;

import org.openrdf.http.object.exceptions.BadGateway;
import org.openrdf.http.object.exceptions.GatewayTimeout;
import org.openrdf.http.object.traits.ProxyObject;
import org.openrdf.http.object.util.SharedExecutors;
import org.openrdf.model.Resource;
import org.openrdf.repository.object.ObjectConnection;
import org.openrdf.repository.object.ObjectFactory;
import org.openrdf.repository.object.RDFObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages and load remote resources.
 * 
 * @author James Leigh
 */
public abstract class RemoteResourceSupport implements RDFObject, RemoteResource {
	private static Map<InetSocketAddress, Boolean> blackList = new ConcurrentHashMap<InetSocketAddress, Boolean>();
	static {
		SharedExecutors.getTimeoutThreadPool().scheduleWithFixedDelay(new Runnable() {
			public void run() {
				blackList.clear();
			}
		}, 1, 4, TimeUnit.HOURS);
	}
	private Logger logger = LoggerFactory.getLogger(RemoteResourceSupport.class);

	@Override
	public boolean load() throws Exception {
		return load(true);
	}

	@Override
	public boolean reload() throws Exception {
		return reload(true);
	}

	private boolean load(boolean bl) throws Exception {
		List<InetSocketAddress> list = getBlackListing(bl);
		Exception gateway = null;
		ObjectConnection con = getObjectConnection();
		ObjectFactory of = con.getObjectFactory();
		String originURI = getOriginURI();
		boolean loaded = false;
		for (Object graph : getNonBlackListedGraphs()) {
			Resource target = ((RDFObject) graph).getResource();
			RemoteGraph rg = of.createObject(target, RemoteGraph.class);
			try {
				loaded |= rg.load(originURI);
			} catch (GatewayTimeout timeout) {
				gateway = timeout;
				blackList(rg, timeout);
			} catch (BadGateway bad) {
				gateway = bad;
				blackList(rg, bad);
			}
		}
		if (loaded)
			return true;
		if (list != null) {
			blackList.keySet().removeAll(list);
			return load(false);
		}
		if (gateway != null)
			throw gateway;
		return false;
	}

	private boolean reload(boolean bl) throws Exception {
		List<InetSocketAddress> list = getBlackListing(bl);
		Exception gateway = null;
		ObjectConnection con = getObjectConnection();
		ObjectFactory of = con.getObjectFactory();
		String originURI = getOriginURI();
		boolean loaded = false;
		for (Object graph : getNonBlackListedGraphs()) {
			Resource target = ((RDFObject) graph).getResource();
			RemoteGraph rg = of.createObject(target, RemoteGraph.class);
			try {
				if (rg instanceof Unresolvable) {
					loaded |= rg.reload(originURI);
				} else {
					loaded |= rg.validate(originURI);
				}
			} catch (GatewayTimeout timeout) {
				gateway = timeout;
				blackList(rg, timeout);
			} catch (BadGateway bad) {
				gateway = bad;
				blackList(rg, bad);
			}
		}
		if (loaded)
			return true;
		if (list != null) {
			blackList.keySet().removeAll(list);
			return reload(false);
		}
		if (gateway != null)
			throw gateway;
		return false;
	}

	private String getOriginURI() {
		ParsedURI parsed = new ParsedURI(getResource().stringValue());
		String scheme = parsed.getScheme();
		String auth = parsed.getAuthority();
		assert auth != null;
		String originURI = new ParsedURI(scheme, auth, "/", null, null).toString();
		return originURI;
	}

	private ArrayList<InetSocketAddress> getBlackListing(boolean bl) {
		if (bl && !blackList.isEmpty())
			return new ArrayList<InetSocketAddress>(blackList.keySet());
		return null;
	}

	private void blackList(RemoteGraph rg, Exception reason) {
		logger.warn(reason.toString());
		InetSocketAddress server = getInetSocketAddressOf(rg);
		blackList.put(server, Boolean.TRUE);
	}

	private ArrayList<Object> getNonBlackListedGraphs() {
		ArrayList<Object> list = new ArrayList<Object>(getPurlDefinedBy());
		Iterator<Object> iter = list.iterator();
		while (iter.hasNext()) {
			InetSocketAddress server = getInetSocketAddressOf(iter.next());
			if (blackList.containsKey(server))
				iter.remove();
		}
		return list;
	}

	private InetSocketAddress getInetSocketAddressOf(Object entity) {
		return ((ProxyObject) entity).getProxyObjectInetAddress();
	}

}
