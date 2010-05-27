package name.persistent;

import info.aduna.io.FileUtil;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;
import name.persistent.behaviours.RemoteGraphSupport;
import name.persistent.behaviours.ServiceRecordSupport;
import name.persistent.concepts.Domain;
import name.persistent.concepts.Origin;
import name.persistent.concepts.PURL;
import name.persistent.concepts.RemoteGraph;
import name.persistent.concepts.Resolvable;
import name.persistent.concepts.Server;
import name.persistent.concepts.Service;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.message.BasicHttpRequest;
import org.openrdf.OpenRDFException;
import org.openrdf.http.object.HTTPObjectServer;
import org.openrdf.http.object.annotations.cacheControl;
import org.openrdf.http.object.annotations.header;
import org.openrdf.http.object.annotations.method;
import org.openrdf.http.object.annotations.operation;
import org.openrdf.http.object.annotations.parameter;
import org.openrdf.http.object.annotations.type;
import org.openrdf.http.object.client.HTTPObjectClient;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.object.ObjectConnection;
import org.openrdf.repository.object.ObjectFactory;
import org.openrdf.repository.object.ObjectRepository;
import org.openrdf.repository.object.RDFObject;
import org.openrdf.repository.object.annotations.matches;
import org.openrdf.repository.object.config.ObjectRepositoryConfig;
import org.openrdf.repository.object.config.ObjectRepositoryFactory;
import org.openrdf.sail.Sail;
import org.openrdf.sail.auditing.AuditingSail;
import org.openrdf.sail.memory.MemoryStore;
import org.openrdf.sail.optimistic.OptimisticRepository;


public class MirrorPURLTest extends TestCase {
	private static final String NS = "http://persistent.name/rdf/2010/purl#";

	@matches("http://test.persistent.name/*")
	public static abstract class PURLServer implements Resolvable, RDFObject {
		@method("GET")
		@type("message/x-response")
		@cacheControl("max-age=60")
		public HttpResponse get(@parameter("*") String qs,
				@header("Via") Set<String> via) throws Exception {
			return resolvePURL(getResource().stringValue(), qs, "*/*", "*", via);
		}
	}

	public static abstract class SRVRecordTester extends ServiceRecordSupport
			implements Resolvable {
		public static int port = 8080;

		@Override
		public List<InetSocketAddress> getOriginServices(boolean useBlackList)
				throws Exception {
			return Collections.singletonList(new InetSocketAddress("localhost",
					port));
		}

	}

	@matches("http://localhost:3128/")
	public interface MyServer {

		/**
		 * List of origins on this domain service.
		 */
		@operation("listOrigins")
		@type("application/rdf+xml")
		public GraphQueryResult myRemoteOrigins() throws OpenRDFException;

		/**
		 * List of domains for the given origin.
		 */
		@operation("listDomains")
		public GraphQueryResult myRemoteDomains(
				@parameter("origin") Object origin) throws Exception;

		/**
		 * List of services for the given domain.
		 */
		@operation("listServices")
		public GraphQueryResult myRemoteServices(
				@parameter("domain") Object domain) throws Exception;

		/**
		 * Description of all domains in the given origin.
		 */
		@operation("domainsOf")
		public GraphQueryResult myDomainsOf(@parameter("origin") Object origin)
				throws Exception;

		/**
		 * Description of all PURLs in the given domain.
		 */
		@operation("purlsOf")
		public GraphQueryResult myPurlsOf(@parameter("domain") Object domain)
				throws Exception;
	}

	private static final String ORIGIN = "http://test.persistent.name/";
	private static final String DOMAIN = "http://test.persistent.name/test/";
	private static final String PURL0 = "http://test.persistent.name/test/test0";
	private static final String PURL1 = "http://test.persistent.name/test/test1";
	protected ObjectRepositoryConfig config = new ObjectRepositoryConfig();
	protected ObjectRepository repository1;
	protected ObjectRepository repository2;
	protected HTTPObjectServer server;
	protected File dataDir;
	protected ObjectConnection con;
	protected ObjectConnection mirror;
	private Server root;
	private ObjectFactory of;

	@Override
	public void setUp() throws Exception {
		RemoteGraphSupport.VIA = "1.1 test";
		HTTPObjectClient.getInstance().resetCache();
		config.addConcept(PURLServer.class);
		config.addBehaviour(SRVRecordTester.class);
		repository1 = createRepository();
		config.addConcept(MyServer.class);
		repository2 = createRepository();
		dataDir = FileUtil.createTempDir("metadata");
		server = new HTTPObjectServer(repository1, new File(dataDir, "www"),
				new File(dataDir, "cache"), null);
		server.setPort(3128);
		server.setEnvelopeType("message/x-response");
		HTTPObjectClient.getInstance().setEnvelopeType("message/x-response");
		SRVRecordTester.port = server.getPort();
		server.start();
		HTTPObjectClient.getInstance().stop();
		con = repository1.getConnection();
		of = con.getObjectFactory();
		mirror = repository2.getConnection();
		ValueFactory vf = con.getValueFactory();
		URI rel = vf.createURI(NS, "rel");
		con.add(vf.createURI(NS, "renamedTo"), rel, vf.createLiteral("canonical"));
		con.add(vf.createURI(NS, "alternative"), rel, vf.createLiteral("alternate"));
		con.add(vf.createURI(NS, "describedBy"), rel, vf.createLiteral("describedby"));
		con.add(vf.createURI(NS, "redirectsTo"), rel, vf.createLiteral("located"));
		String uri = "http://localhost:3128/";
		root = con.addDesignation(con.getObject(uri), Server.class);
		Origin origin = con.addDesignation(con.getObject(ORIGIN), Origin.class);
		root.getPurlServes().add(origin);
		Domain domain = con.addDesignation(con.getObject(DOMAIN), Domain.class);
		Service service = con.addDesignation(of.createObject(), Service.class);
		PURL purl = con.addDesignation(con.getObject(PURL0), PURL.class);
		purl.setPurlRenamedTo(con.getObject(PURL1));
		purl.setPurlPartOf(domain);
		domain.getPurlServices().add(service);
		service.setPurlServer(root);
		origin.getPurlParts().add(domain);
	}

	@Override
	public void tearDown() throws Exception {
		con.close();
		mirror.close();
		server.stop();
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

	public void testETag() throws Exception {
		String url = root.toString() + "?purlsOf&domain=" + DOMAIN;
		RemoteGraph graph = mirror.addDesignation(mirror.getObject(url), RemoteGraph.class);
		assertTrue(graph.load(ORIGIN));
		HTTPObjectClient client = HTTPObjectClient.getInstance();
		InetSocketAddress port = new InetSocketAddress("localhost", server.getPort());
		HttpResponse resp = client.service(port, new BasicHttpRequest("GET", PURL0));
		HttpEntity entity = resp.getEntity();
		if (entity != null) {
			entity.consumeContent();
		}
		String etag = resp.getFirstHeader("ETag").getValue();
		assertEquals(etag, resolveTag(mirror.getObject(PURL0)));
	}

	private String resolveTag(Object target) throws Exception, IOException {
		Set<String> via = Collections.emptySet();
		HttpResponse resp = ((Resolvable) target).resolvePURL(PURL0, null, null, "*", via);
		HttpEntity entity = resp.getEntity();
		if (entity != null) {
			entity.consumeContent();
		}
		return resp.getFirstHeader("ETag").getValue();
	}

	public void testListOrigins() throws Exception {
		MyServer server = (MyServer) mirror.getObject(root.toString());
		GraphQueryResult origins = server.myRemoteOrigins();
		boolean found = false;
		boolean remote = false;
		boolean defined = false;
		Set<Statement> set = new HashSet<Statement>();
		while (origins.hasNext()) {
			Statement st = origins.next();
			if (st.getPredicate().getLocalName().equals("serves")
					&& st.getObject().stringValue().equals(ORIGIN)) {
				found = true;
				set.add(st);
			} else if (st.getSubject().stringValue().equals(ORIGIN)
					&& st.getPredicate().equals(RDF.TYPE)
					&& st.getObject().stringValue().endsWith("RemoteResource")) {
				remote = true;
				set.add(st);
			} else if (st.getSubject().stringValue().equals(ORIGIN)
					&& st.getPredicate().getLocalName().equals("definedBy")) {
				defined = true;
				set.add(st);
			} else if (st.getSubject().stringValue().equals(root.toString())
					&& st.getPredicate().equals(RDF.TYPE)
					&& st.getObject().stringValue().endsWith("Server")) {
				set.add(st);
			} else {
				assertEquals(null, st);
			}
		}
		assertFalse(set.isEmpty());
		assertTrue(found);
		assertTrue(remote);
		assertTrue(defined);
		assertEquals(4, set.size());
	}

	public void testListDomains() throws Exception {
		MyServer server = (MyServer) mirror.getObject(root.toString());
		GraphQueryResult origins = server.myRemoteDomains(mirror.getObject(ORIGIN));
		boolean found = false;
		boolean remote = false;
		boolean defined = false;
		Set<Statement> set = new HashSet<Statement>();
		while (origins.hasNext()) {
			Statement st = origins.next();
			if (st.getSubject().stringValue().equals(ORIGIN)
					&& st.getPredicate().equals(RDF.TYPE)
					&& st.getObject().stringValue().endsWith("Origin")) {
				set.add(st);
			} else if (st.getSubject().stringValue().equals(ORIGIN)
					&& st.getPredicate().equals(RDF.TYPE)
					&& st.getObject().stringValue().endsWith("RemoteResource")) {
				remote = true;
				set.add(st);
			} else if (st.getSubject().stringValue().equals(ORIGIN)
					&& st.getPredicate().getLocalName().equals("definedBy")) {
				defined = true;
				set.add(st);
			} else if (st.getSubject().stringValue().equals(ORIGIN)
					&& st.getPredicate().getLocalName().equals("mirroredBy")) {
				set.add(st);
			} else if (st.getSubject().stringValue().equals(ORIGIN)
					&& st.getPredicate().getLocalName().equals("part")
					&& st.getObject().stringValue().equals(DOMAIN)) {
				found = true;
				set.add(st);
			} else if (st.getSubject().stringValue().equals(DOMAIN)
					&& st.getPredicate().equals(RDF.TYPE)
					&& st.getObject().stringValue().endsWith("RemoteResource")) {
				remote = true;
				set.add(st);
			} else if (st.getSubject().stringValue().equals(DOMAIN)
					&& st.getPredicate().getLocalName().equals("definedBy")) {
				defined = true;
				set.add(st);
			} else {
				assertEquals(null, st);
			}
		}
		assertFalse(set.isEmpty());
		assertTrue(found);
		assertTrue(remote);
		assertTrue(defined);
		assertEquals(7, set.size());
	}

}
