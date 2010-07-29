package name.persistent;

import info.aduna.io.FileUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import junit.framework.TestCase;
import name.persistent.behaviours.MirroredDomainSupport;
import name.persistent.behaviours.RemoteGraphSupport;
import name.persistent.concepts.Domain;
import name.persistent.concepts.PURL;
import name.persistent.concepts.Resolvable;
import name.persistent.concepts.Server;
import name.persistent.concepts.Service;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.openrdf.http.object.HTTPObjectServer;
import org.openrdf.http.object.annotations.cacheControl;
import org.openrdf.http.object.annotations.header;
import org.openrdf.http.object.annotations.method;
import org.openrdf.http.object.annotations.parameter;
import org.openrdf.http.object.annotations.type;
import org.openrdf.http.object.client.HTTPObjectClient;
import org.openrdf.http.object.exceptions.NotFound;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.Repository;
import org.openrdf.repository.object.ObjectConnection;
import org.openrdf.repository.object.ObjectFactory;
import org.openrdf.repository.object.ObjectRepository;
import org.openrdf.repository.object.RDFObject;
import org.openrdf.repository.object.annotations.matches;
import org.openrdf.repository.object.config.ObjectRepositoryConfig;
import org.openrdf.repository.object.config.ObjectRepositoryFactory;
import org.openrdf.rio.RDFFormat;
import org.openrdf.sail.Sail;
import org.openrdf.sail.auditing.AuditingSail;
import org.openrdf.sail.memory.MemoryStore;
import org.openrdf.sail.optimistic.OptimisticRepository;


public class ProxyPURLTest extends TestCase {
	private static final String NS = "http://persistent.name/rdf/2010/purl#";
	static {
		if (System.getProperty("ebug") != null) {
			final TreeSet<String> set = new TreeSet<String>(Arrays
					.asList(System.getProperty("ebug").split("\\s*,\\s*")));
			Logger logger = Logger.getLogger("");
			ConsoleHandler ch = new ConsoleHandler() {
				public void publish(LogRecord record) {
					String name = record.getLoggerName();
					String lower = set.lower(name);
					if (set.contains(name) || lower != null
							&& name.startsWith(lower)) {
						super.publish(record);
					}
				}
			};
			ch.setLevel(Level.ALL);
			logger.addHandler(ch);
			logger.setLevel(Level.FINE);
		}
	}

	@matches("http://test.persistent.name/*")
	public static abstract class PURLResolver implements RDFObject, Resolvable {
		@method("GET")
		@type("message/x-response")
		@cacheControl("max-age=60")
		public HttpResponse get(@parameter("*") String qs,
				@header("Accept") String accept,
				@header("Via") Set<String> via) throws Exception {
			return resolvePURL(getResource().stringValue(), qs, accept,
					"*", via);
		}
	}

	private static final String ORIGIN = "http://test.persistent.name/";
	private static final String DOMAIN = "http://test.persistent.name/test/";
	private static final String PURL0 = "http://test.persistent.name/test/test0";
	private static final String PURL1 = "http://test.persistent.name/test/test1";
	private static final String PURL2 = "http://test.persistent.name/test/test2";
	protected ObjectRepositoryConfig config = new ObjectRepositoryConfig();
	protected ObjectRepository repository1;
	protected ObjectRepository repository2;
	protected HTTPObjectServer server;
	protected File dataDir;
	protected ObjectConnection con;
	protected ObjectConnection proxy;
	private Server root;
	private ObjectFactory of;

	@Override
	public void setUp() throws Exception {
		RemoteGraphSupport.VIA = "1.1 test";
		HTTPObjectClient.getInstance().resetCache();
		config.addConcept(PURLResolver.class);
		repository1 = createRepository();
		repository2 = createRepository();
		dataDir = FileUtil.createTempDir("metadata");
		server = new HTTPObjectServer(repository1, new File(dataDir, "www"),
				new File(dataDir, "cache"), null);
		server.listen(3128);
		server.setEnvelopeType("message/x-response");
		String uri = "http://localhost:3128/";
		server.setIdentityPrefix(new String[] { uri + "diverted;" });
		HTTPObjectClient.getInstance().setEnvelopeType("message/x-response");
		server.start();
		HTTPObjectClient.getInstance().stop();
		con = repository1.getConnection();
		of = con.getObjectFactory();
		proxy = repository2.getConnection();
		ValueFactory vf = con.getValueFactory();
		URI rel = vf.createURI(NS, "rel");
		con.add(vf.createURI(NS, "renamedTo"), rel, vf.createLiteral("canonical"));
		con.add(vf.createURI(NS, "alternative"), rel, vf.createLiteral("alternate"));
		con.add(vf.createURI(NS, "describedBy"), rel, vf.createLiteral("describedby"));
		con.add(vf.createURI(NS, "redirectsTo"), rel, vf.createLiteral("located"));
		root = con.addDesignation(con.getObject(uri), Server.class);
	}

	@Override
	public void tearDown() throws Exception {
		MirroredDomainSupport.canacelAllValidation();
		con.close();
		proxy.close();
		server.stop();
		server.destroy();
		repository1.shutDown();
		repository2.shutDown();
		FileUtil.deltree(dataDir);
	}

	private ObjectRepository createRepository() throws Exception {
		Sail sail = new MemoryStore();
		sail = new AuditingSail(sail);
		Repository repo = new OptimisticRepository(sail);
		repo.initialize();
		ObjectRepositoryFactory factory = new ObjectRepositoryFactory();
		return factory.createRepository(config, repo);
	}

	public void testProxy() throws Exception {
		PURL purl = con.addDesignation(con.getObject(PURL0), PURL.class);
		purl.setPurlRenamedTo(con.getObject(PURL1));
		Service service = con.addDesignation(of.createObject(), Service.class);
		service.setPurlServer(root);
		Domain domain = con.addDesignation(con.getObject(DOMAIN), Domain.class);
		purl.setPurlPartOf(domain);
		domain.getPurlServices().add(service);
		Domain origin = con.addDesignation(con.getObject(ORIGIN), Domain.class);
		domain.getPurlDomainOf().add(origin);
		root.getPurlServes().add(origin);
		HttpResponse resp = resolvePURL(purl.toString());
		HttpEntity entity = resp.getEntity();
		if (entity != null) {
			entity.consumeContent();
		}
		assertEquals(301, resp.getStatusLine().getStatusCode());
		assertEquals(1, resp.getHeaders("Location").length);
		assertEquals(PURL1, resp.getFirstHeader("Location").getValue());
	}

	public void testInvalidSRV() throws Exception {
		try {
			HttpResponse resp = resolvePURL(PURL0);
			HttpEntity entity = resp.getEntity();
			if (entity != null) {
				entity.consumeContent();
			}
			assertEquals(404, resp.getStatusLine().getStatusCode());
			assertEquals(0, resp.getHeaders("Location").length);
		} catch (FileNotFoundException e) {
			// this is also okay
		} catch (NotFound e) {
			// this is also okay
		}
	}

	public void testProxyHTTPCache() throws Exception {
		Domain origin = con.addDesignation(con.getObject(ORIGIN), Domain.class);
		root.getPurlServes().add(origin);
		Domain domain = con.addDesignation(con.getObject(DOMAIN), Domain.class);
		Service service = con.addDesignation(of.createObject(), Service.class);
		PURL purl = con.addDesignation(con.getObject(PURL0), PURL.class);
		purl.getPurlAlternatives().add(con.getObject(PURL1));
		purl.setPurlPartOf(domain);
		domain.getPurlServices().add(service);
		service.setPurlServer(root);
		domain.getPurlDomainOf().add(origin);
		con.close(); // new revision
		HttpResponse resp = resolvePURL(purl.toString());
		HttpEntity entity = resp.getEntity();
		if (entity != null) {
			entity.consumeContent();
		}
		Thread.sleep(1000); // age
		resp = resolvePURL(purl.toString());
		entity = resp.getEntity();
		if (entity != null) {
			entity.consumeContent();
		}
		con = repository1.getConnection();
		purl = (PURL) con.getObject(PURL0);
		purl.getPurlAlternatives().add(con.getObject(PURL2));
		resp = resolvePURL(purl.toString());
		entity = resp.getEntity();
		if (entity != null) {
			entity.consumeContent();
		}
		assertEquals(302, resp.getStatusLine().getStatusCode());
		assertEquals(1, resp.getHeaders("Location").length);
		assertEquals(PURL1, resp.getFirstHeader("Location").getValue());
	}

	public void testProxyDomainCache() throws Exception {
		Domain origin = con.addDesignation(con.getObject(ORIGIN), Domain.class);
		root.getPurlServes().add(origin);
		Domain domain = con.addDesignation(con.getObject(DOMAIN), Domain.class);
		Service service = con.addDesignation(of.createObject(), Service.class);
		PURL purl1 = con.addDesignation(con.getObject(PURL1), PURL.class);
		PURL purl2 = con.addDesignation(con.getObject(PURL2), PURL.class);
		purl1.setPurlRenamedTo(con.getObject(PURL0));
		purl1.setPurlPartOf(domain);
		purl2.setPurlRenamedTo(con.getObject(PURL0));
		purl2.setPurlPartOf(domain);
		domain.getPurlServices().add(service);
		service.setPurlServer(root);
		domain.getPurlDomainOf().add(origin);
		HttpResponse resp = resolvePURL(purl1.toString());
		HttpEntity entity = resp.getEntity();
		if (entity != null) {
			entity.consumeContent();
		}
		assertEquals(301, resp.getStatusLine().getStatusCode());
		assertEquals(1, resp.getHeaders("Location").length);
		assertEquals(PURL0, resp.getFirstHeader("Location").getValue());
		Thread.sleep(2000);
		resp = resolvePURL(purl2.toString());
		entity = resp.getEntity();
		if (entity != null) {
			entity.consumeContent();
		}
		assertEquals(301, resp.getStatusLine().getStatusCode());
		assertEquals(1, resp.getHeaders("Location").length);
		assertEquals(PURL0, resp.getFirstHeader("Location").getValue());
	}

	private HttpResponse resolvePURL(String uri) throws Exception {
		String url = root.toString() + "diverted;" + DOMAIN + "?remote-domains";
		proxy.add(new java.net.URL(url), DOMAIN, RDFFormat.RDFXML);
		url = root.toString() + "diverted;" + DOMAIN + "?services";
		proxy.add(new java.net.URL(url), DOMAIN, RDFFormat.RDFXML);
		Resolvable target = (Resolvable) proxy.getObject(uri);
		Set<String> via = Collections.emptySet();
		return target.resolvePURL(uri, null, null, "*", via);
	}

}
