package name.persistent.behaviours;

import static org.openrdf.query.QueryLanguage.SPARQL;
import info.aduna.net.ParsedURI;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.xml.datatype.XMLGregorianCalendar;

import name.persistent.concepts.MirroredDomain;
import name.persistent.concepts.RemoteGraph;
import name.persistent.concepts.Unresolvable;

import org.apache.http.HttpResponse;
import org.openrdf.http.object.threads.ManagedExecutors;
import org.openrdf.model.Resource;
import org.openrdf.query.BooleanQuery;
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

	private static final ScheduledExecutorService executor = ManagedExecutors
			.newSingleScheduler("RemoteGraph");
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
					RemoteGraph graph;
					try {
						graph = con.getObject(RemoteGraph.class, subj);
					} catch (ClassCastException e) {
						BooleanQuery qry = con.prepareBooleanQuery(SPARQL, PREFIX
								+ "ASK {{ ?domain purl:definedBy $subj }\n"
								+ "UNION { ?domain purl:mirroredBy $subj }\n"
								+ "UNION { ?domain purl:servicedBy $subj }}");
						qry.setBinding("subj", subj);
						if (qry.evaluate()) {
							graph = con.addDesignation(con.getObject(subj), RemoteGraph.class);
						} else {
							synchronized (alwaysFresh) {
								alwaysFresh.remove(key);
								return;
							}
						}
					}
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
		Object by = getPurlMirroredBy();
		if (by instanceof RemoteGraph) {
			RemoteGraph graph = (RemoteGraph) by;
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
	public HttpResponse resolvePURL(String source, String qs, String accept,
			String language, Set<String> via) throws Exception {
		stayFresh();
		return super.resolvePURL(source, qs, accept, language, via);
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
			+ "OPTIONAL {{ ?domain purl:definedBy ?graph }\n"
			+ "UNION { ?domain purl:mirroredBy ?graph }\n"
			+ "UNION { ?domain purl:servicedBy ?graph }}\n"
			+ "FILTER (!bound(?domain)) }")
	protected abstract List<RemoteGraph> selectOrphanGraphs();

	protected void stayFresh() {
		String origin = getOrigin();
		stayFresh(getPurlDefinedBy(), origin);
		stayFresh(getReloadGraph(), origin);
	}

	protected Object getReloadGraph() {
		return getPurlMirroredBy();
	}

	private String getOrigin() {
		ParsedURI parsed = new ParsedURI(getResource().stringValue());
		String scheme = parsed.getScheme();
		String auth = parsed.getAuthority();
		assert auth != null;
		return new ParsedURI(scheme, auth, "/", null, null).toString();
	}

	private void stayFresh(Object obj, String origin) {
		if (obj == null)
			return;
		if (!(obj instanceof RemoteGraph) && obj instanceof RDFObject) {
			ObjectConnection con = getObjectConnection();
			Resource subj = ((RDFObject) obj).getResource();
			obj = con.getObjectFactory().createObject(subj, RemoteGraph.class);
		}
		RemoteGraph graph = (RemoteGraph) obj;
		if (!isAlwaysFresh(graph)) {
			stayFresh(graph);
		}
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

	private void stayFresh(RemoteGraph graph) {
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
