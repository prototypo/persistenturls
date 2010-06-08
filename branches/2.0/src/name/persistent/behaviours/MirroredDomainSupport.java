package name.persistent.behaviours;

import info.aduna.net.ParsedURI;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.xml.datatype.XMLGregorianCalendar;

import name.persistent.concepts.MirroredDomain;
import name.persistent.concepts.RemoteGraph;
import name.persistent.concepts.Unresolvable;

import org.apache.http.HttpResponse;
import org.openrdf.http.object.util.NamedThreadFactory;
import org.openrdf.model.Resource;
import org.openrdf.repository.object.ObjectConnection;
import org.openrdf.repository.object.ObjectRepository;
import org.openrdf.repository.object.RDFObject;
import org.openrdf.repository.object.annotations.sparql;
import org.openrdf.repository.object.traits.Refreshable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class MirroredDomainSupport extends DomainSupport implements
		MirroredDomain, RDFObject, Refreshable {
	private static final String PREFIX = "PREFIX purl:<http://persistent.name/rdf/2010/purl#>\n";
	private static final String PROTOCOL = "1.1";
	private static final String VIA;
	private static String localhost;
	static {
		try {
			localhost = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			localhost = "localhost";
		}
		VIA = PROTOCOL + " " + localhost;
	}
	private static String WARN_110 = "110 " + localhost
			+ " \"Response is stale\"";
	private static String WARN_111 = "111 " + localhost
			+ " \"Revalidation failed\"";
	private static String WARN_199 = "199 " + localhost
			+ " \"Mirrored response\"";

	private static final ScheduledExecutorService executor = Executors
			.newSingleThreadScheduledExecutor(new NamedThreadFactory(
					"RemoteGraph", true));
	private static final Map<Object, Refresher> alwaysFresh = new HashMap<Object, Refresher>();

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
			schedule = executor.schedule(this, freshness + 10, TimeUnit.SECONDS);
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

	@Override
	public void purlSetEntityHeaders(HttpResponse resp) {
		List<RemoteGraph> graphs = getRemoteGraphs();
		if (!graphs.isEmpty()) {
			RemoteGraph graph = graphs.get(0);
			XMLGregorianCalendar validated = graph.getPurlLastValidated();
			long now = System.currentTimeMillis();
			long date = validated.toGregorianCalendar().getTimeInMillis();
			int age = (int) ((now - date) / 1000);
			String via = graph.getPurlVia();
			setHeader(resp, "Via", via == null ? VIA : via + "," + VIA);
			resp.setHeader("Age", Integer.toString(age));
			setHeader(resp, "Date", validated);
			setHeader(resp, "Cache-Control", graph.getPurlCacheControl());
			setHeader(resp, "ETag", graph.getPurlEtag());
			setHeader(resp, "Last-Modified", graph.getPurlLastModified());
			if (!graph.isFresh()) {
				resp.addHeader("Warning", WARN_110);
			}
			if (graph instanceof Unresolvable) {
				resp.addHeader("Warning", WARN_111);
			}
			resp.addHeader("Warning", WARN_199);
		} else {
			super.purlSetEntityHeaders(resp);
		}
	}

	@Override
	public boolean reload() throws Exception {
		String origin = getOrigin();
		if (reload(getPurlDefinedBy(), origin)) {
			refresh();
		}
		return reload(getReloadGraph(), origin);
	}

	@Override
	public void refreshGraphs() throws Exception {
		ObjectConnection con = getObjectConnection();
		for (RemoteGraph graph : selectOrphanGraphs()) {
			goStale((RDFObject) graph);
			graph.removeRemoteGraph();
			con.removeDesignation(graph, RemoteGraph.class);
		}
		loadGraph(getPurlDefinedBy());
		loadGraph(getReloadGraph());
	}

	@Override
	public void purlRefreshGraphs() throws Exception {
		Object graph = getPurlDefinedBy();
		if (graph != null) {
			if (!(graph instanceof RemoteGraph)) {
				ObjectConnection con = getObjectConnection();
				graph = con.addDesignation(graph, RemoteGraph.class);
			}
			if (!isAlwaysFresh((RemoteGraph) graph)) {
				stayFresh((RemoteGraph) graph);
			}
		}
		graph = getReloadGraph();
		if (graph != null) {
			if (!(graph instanceof RemoteGraph)) {
				ObjectConnection con = getObjectConnection();
				graph = con.addDesignation(graph, RemoteGraph.class);
			}
			if (!isAlwaysFresh((RemoteGraph) graph)) {
				stayFresh((RemoteGraph) graph);
			}
		}
	}

	@Override
	public void purlStallGraphs() throws Exception {
		Object graph = getPurlDefinedBy();
		if (graph != null) {
			goStale((RDFObject) graph);
		}
		graph = getReloadGraph();
		if (graph != null) {
			goStale((RDFObject) graph);
		}
	}

	@sparql(PREFIX + "SELECT REDUCED ?graph\n"
			+ "WHERE { ?graph a purl:RemoteGraph .\n"
			+ "OPTIONAL { ?domain ?pred ?graph }\n"
			+ "FILTER (!bound(?domain)) }")
	protected abstract List<RemoteGraph> selectOrphanGraphs();

	protected Object getReloadGraph() {
		return getPurlMirroredBy();
	}

	@sparql(PREFIX + "SELECT REDUCED ?graph\n"
			+ "WHERE { GRAPH ?graph { $this ?p ?o }\n"
			+ "?graph a purl:RemoteGraph; purl:last-validated ?last }\n"
			+ "ORDER BY desc(?last)")
	protected abstract List<RemoteGraph> getRemoteGraphs();

	private String getOrigin() {
		ParsedURI parsed = new ParsedURI(getResource().stringValue());
		String scheme = parsed.getScheme();
		String auth = parsed.getAuthority();
		assert auth != null;
		return new ParsedURI(scheme, auth, "/", null, null).toString();
	}

	private boolean reload(Object obj, String origin) throws Exception {
		if (obj == null)
			return false;
		if (!(obj instanceof RemoteGraph)) {
			ObjectConnection con = getObjectConnection();
			obj = con.addDesignation(obj, RemoteGraph.class);
		}
		RemoteGraph graph = (RemoteGraph) obj;
		boolean reload;
		if (graph instanceof Unresolvable) {
			reload = graph.reload(origin);
		} else {
			reload = graph.validate(origin);
		}
		if (reload && !isAlwaysFresh(graph)) {
			stayFresh(graph);
		}
		return reload;
	}

	private void loadGraph(Object graph) throws Exception {
		if (graph != null) {
			if (!(graph instanceof RemoteGraph)) {
				ObjectConnection con = getObjectConnection();
				RemoteGraph rg = con.addDesignation(graph, RemoteGraph.class);
				rg.load(getResource().stringValue());
				graph = rg;
			}
			if (!isAlwaysFresh((RemoteGraph) graph)) {
				stayFresh((RemoteGraph) graph);
			}
		}
	}

	private boolean isAlwaysFresh(RemoteGraph graph) {
		Refresher refresher = new Refresher((RDFObject) graph);
		synchronized (alwaysFresh) {
			return alwaysFresh.containsKey(refresher.key);
		}
	}

	private void stayFresh(RemoteGraph graph) throws Exception {
		Refresher refresher = new Refresher((RDFObject) graph);
		int freshness = Math.max(graph.getFreshness(), 0);
		synchronized (alwaysFresh) {
			if (alwaysFresh.containsKey(refresher.key)) {
				refresher = alwaysFresh.get(refresher.key);
			}
		}
		refresher.schedule(freshness);
	}

	private void goStale(RDFObject graph) throws Exception {
		Refresher refresher = new Refresher(graph);
		synchronized (alwaysFresh) {
			refresher = alwaysFresh.remove(refresher.key);
		}
		if (refresher != null) {
			refresher.cancel(false);
			refresher.await();
		}
	}

}
