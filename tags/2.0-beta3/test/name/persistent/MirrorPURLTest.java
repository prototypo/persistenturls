package name.persistent;

import info.aduna.io.FileUtil;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.Set;

import junit.framework.TestCase;
import name.persistent.behaviours.RemoteGraphSupport;
import name.persistent.concepts.Domain;
import name.persistent.concepts.PURL;
import name.persistent.concepts.Partial;
import name.persistent.concepts.Resolvable;
import name.persistent.concepts.Server;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.message.BasicHttpRequest;
import org.openrdf.http.object.HTTPObjectServer;
import org.openrdf.http.object.annotations.cacheControl;
import org.openrdf.http.object.annotations.header;
import org.openrdf.http.object.annotations.method;
import org.openrdf.http.object.annotations.parameter;
import org.openrdf.http.object.annotations.type;
import org.openrdf.http.object.client.HTTPObjectClient;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.Repository;
import org.openrdf.repository.object.ObjectConnection;
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
	public static abstract class PURLResolver implements Resolvable, RDFObject {
		@method("GET")
		@type("message/x-response")
		@cacheControl("max-age=60")
		public HttpResponse get(@parameter("*") String qs,
				@header("Via") Set<String> via) throws Exception {
			return resolvePURL(getResource().stringValue(), qs, "*/*", "*", via);
		}
	}

	private static final String ORIGIN = "http://test.persistent.name/";
	private static final String PARTIAL = "http://test.persistent.name/test/";
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
		mirror = repository2.getConnection();
		ValueFactory vf = con.getValueFactory();
		URI rel = vf.createURI(NS, "rel");
		con.add(vf.createURI(NS, "renamedTo"), rel, vf.createLiteral("canonical"));
		con.add(vf.createURI(NS, "alternative"), rel, vf.createLiteral("alternate"));
		con.add(vf.createURI(NS, "describedBy"), rel, vf.createLiteral("describedby"));
		con.add(vf.createURI(NS, "redirectsTo"), rel, vf.createLiteral("located"));
		mirror.add(vf.createURI(NS, "renamedTo"), rel, vf.createLiteral("canonical"));
		mirror.add(vf.createURI(NS, "alternative"), rel, vf.createLiteral("alternate"));
		mirror.add(vf.createURI(NS, "describedBy"), rel, vf.createLiteral("describedby"));
		mirror.add(vf.createURI(NS, "redirectsTo"), rel, vf.createLiteral("located"));
		root = con.addDesignation(con.getObject(uri), Server.class);
		Domain origin = con.addDesignation(con.getObject(ORIGIN), Domain.class);
		root.getPurlServes().add(origin);
		Partial partial = con.addDesignation(con.getObject(PARTIAL), Partial.class);
		PURL purl = con.addDesignation(con.getObject(PURL0), PURL.class);
		purl.setPurlRenamedTo(con.getObject(PURL1));
		purl.setPurlPartOf(partial);
		partial.setPurlBelongsTo(origin);
	}

	@Override
	public void tearDown() throws Exception {
		con.close();
		mirror.close();
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

	public void testDomainETag() throws Exception {
		String url = root.toString() + "diverted;" + ORIGIN + "?mirror-domains";
		Domain domain = mirror.addDesignation(mirror.getObject(ORIGIN), Domain.class);
		domain.setPurlDefinedBy(mirror.getObject(url));
		HTTPObjectClient client = HTTPObjectClient.getInstance();
		InetSocketAddress port = new InetSocketAddress("localhost", 3128);
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

}
