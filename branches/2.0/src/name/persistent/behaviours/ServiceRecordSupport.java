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
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import name.persistent.concepts.Resolvable;

import org.openrdf.http.object.exceptions.NotFound;
import org.openrdf.http.object.util.SharedExecutors;
import org.openrdf.repository.object.RDFObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xbill.DNS.Address;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.SRVRecord;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;

public abstract class ServiceRecordSupport implements Resolvable, RDFObject {
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
	private Logger logger = LoggerFactory.getLogger(ServiceRecordSupport.class);

	public void blackList(InetSocketAddress server, Exception reason) {
		logger.warn(reason.toString());
		blackList.put(server, Boolean.TRUE);
	}

	public List<InetSocketAddress> getOriginServices(boolean useBlackList)
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
}
