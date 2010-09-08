package name.persistent;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;
import name.persistent.concepts.Disabled;
import name.persistent.concepts.PURL;
import name.persistent.concepts.Tombstoned;
import name.persistent.concepts.Unresolvable;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.object.ObjectConnection;
import org.openrdf.repository.object.ObjectRepository;
import org.openrdf.repository.object.config.ObjectRepositoryFactory;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.memory.MemoryStore;


public class PURLTest extends TestCase {
	private static final String NS = "http://persistent.name/rdf/2010/purl#";
	private static final String PURL0 = "http://test.persistent.name/test/test0";
	private static final String PURL1 = "http://test.persistent.name/test/test1";
	private static final String PURL2 = "http://test.persistent.name/test/test2";
	private Set<String> via = Collections.emptySet();
	private ObjectRepository repo;
	private ObjectConnection con;

	public void setUp() throws Exception {
		ObjectRepositoryFactory orf = new ObjectRepositoryFactory();
		SailRepository sail = new SailRepository(new MemoryStore());
		sail.initialize();
		repo = orf.createRepository(sail);
		con = repo.getConnection();
		ValueFactory vf = con.getValueFactory();
		URI rel = vf.createURI(NS, "rel");
		con.add(vf.createURI(NS, "copyOf"), rel, vf.createURI(NS, "copyOf"));
		con.add(vf.createURI(NS, "renamedTo"), rel, vf.createLiteral("canonical"));
		con.add(vf.createURI(NS, "alternative"), rel, vf.createLiteral("alternate"));
		con.add(vf.createURI(NS, "describedBy"), rel, vf.createLiteral("describedby"));
		con.add(vf.createURI(NS, "redirectsTo"), rel, vf.createURI(NS, "redirectsTo"));
	}

	public void tearDown() throws Exception {
		con.close();
		repo.shutDown();
	}

	public void test203() throws Exception {
		PURL purl = con.addDesignation(con.getObject(PURL0), PURL.class);
		purl.setPurlCopyOf(con.getObject("http://example.com/"));
		HttpResponse resp = purl.resolvePURL(purl.toString(), null, "*/*", "*", via);
		HttpEntity entity = resp.getEntity();
		assertNotNull(entity);
		entity.consumeContent();
		assertEquals(203, resp.getStatusLine().getStatusCode());
		assertEquals(0, resp.getHeaders("Location").length);
		assertEquals(1, resp.getHeaders("Content-Location").length);
		assertEquals("http://example.com/", resp.getFirstHeader("Content-Location").getValue());
	}

	public void test301() throws Exception {
		PURL purl = con.addDesignation(con.getObject(PURL0), PURL.class);
		purl.setPurlRenamedTo(con.getObject(PURL1));
		HttpResponse resp = purl.resolvePURL(purl.toString(), null, "*/*", "*", via);
		assertEquals(301, resp.getStatusLine().getStatusCode());
		assertEquals(1, resp.getHeaders("Location").length);
		assertEquals(PURL1, resp.getFirstHeader("Location").getValue());
	}

	public void test302() throws Exception {
		PURL purl = con.addDesignation(con.getObject(PURL0), PURL.class);
		purl.getPurlAlternatives().add(con.getObject(PURL1));
		HttpResponse resp = purl.resolvePURL(purl.toString(), null, "*/*", "*", via);
		assertEquals(302, resp.getStatusLine().getStatusCode());
		assertEquals(1, resp.getHeaders("Location").length);
		assertEquals(PURL1, resp.getFirstHeader("Location").getValue());
	}

	public void test303() throws Exception {
		PURL purl = con.addDesignation(con.getObject(PURL0), PURL.class);
		purl.getPurlDescribedBy().add(con.getObject(PURL1));
		purl.getPurlDescribedBy().add(con.getObject(PURL2));
		HttpResponse resp = purl.resolvePURL(purl.toString(), null, "*/*", "*", via);
		assertEquals(303, resp.getStatusLine().getStatusCode());
		assertEquals(2, resp.getHeaders("Location").length);
		Set<String> values = new HashSet<String>();
		for (Header hd : resp.getHeaders("Location")) {
			values.add(hd.getValue());
		}
		assertEquals(new HashSet<String>(Arrays.asList(PURL1, PURL2)), values);
	}

	public void test307() throws Exception {
		PURL purl = con.addDesignation(con.getObject(PURL0), PURL.class);
		purl.setPurlRedirectsTo(con.getObject(PURL1));
		HttpResponse resp = purl.resolvePURL(purl.toString(), null, "*/*", "*", via);
		assertEquals(307, resp.getStatusLine().getStatusCode());
		assertEquals(1, resp.getHeaders("Location").length);
		assertEquals(PURL1, resp.getFirstHeader("Location").getValue());
	}

	public void testDisabled() throws Exception {
		PURL purl = con.addDesignation(con.getObject(PURL0), PURL.class);
		purl.getPurlAlternatives().add(con.getObject(PURL1));
		purl = (PURL) con.addDesignation(con.getObject(PURL0), Disabled.class);
		HttpResponse resp = purl.resolvePURL(purl.toString(), null, "*/*", "*", via);
		assertEquals(404, resp.getStatusLine().getStatusCode());
		assertEquals(0, resp.getHeaders("Location").length);
	}

	public void testTombstoned() throws Exception {
		PURL purl = con.addDesignation(con.getObject(PURL0), PURL.class);
		purl.getPurlAlternatives().add(con.getObject(PURL1));
		purl = (PURL) con.addDesignation(con.getObject(PURL0), Tombstoned.class);
		HttpResponse resp = purl.resolvePURL(purl.toString(), null, "*/*", "*", via);
		assertEquals(410, resp.getStatusLine().getStatusCode());
		assertEquals(0, resp.getHeaders("Location").length);
	}

	public void test302Chain() throws Exception {
		PURL purl0 = con.addDesignation(con.getObject(PURL0), PURL.class);
		purl0.getPurlAlternatives().add(con.getObject(PURL1));
		PURL purl1 = con.addDesignation(con.getObject(PURL1), PURL.class);
		purl1.getPurlAlternatives().add(con.getObject(PURL2));
		HttpResponse resp = purl0.resolvePURL(purl0.toString(), null, "*/*", "*", via);
		assertEquals(302, resp.getStatusLine().getStatusCode());
		assertEquals(1, resp.getHeaders("Location").length);
		assertEquals(PURL2, resp.getFirstHeader("Location").getValue());
	}

	public void testMixedChain() throws Exception {
		PURL purl0 = con.addDesignation(con.getObject(PURL0), PURL.class);
		purl0.getPurlAlternatives().add(con.getObject(PURL1));
		PURL purl1 = con.addDesignation(con.getObject(PURL1), PURL.class);
		purl1.getPurlDescribedBy().add(con.getObject(PURL2));
		HttpResponse resp = purl0.resolvePURL(purl0.toString(), null, "*/*", "*", via);
		assertEquals(302, resp.getStatusLine().getStatusCode());
		assertEquals(1, resp.getHeaders("Location").length);
		assertEquals(PURL1, resp.getFirstHeader("Location").getValue());
	}

	public void testAlternateBeforeSeeAlso() throws Exception {
		PURL purl = con.addDesignation(con.getObject(PURL0), PURL.class);
		purl.getPurlAlternatives().add(con.getObject(PURL1));
		purl.getPurlDescribedBy().add(con.getObject(PURL2));
		HttpResponse resp = purl.resolvePURL(purl.toString(), null, "*/*", "*", via);
		assertEquals(302, resp.getStatusLine().getStatusCode());
		assertEquals(1, resp.getHeaders("Location").length);
		assertEquals(PURL1, resp.getFirstHeader("Location").getValue());
	}

	public void testAcceptable() throws Exception {
		PURL purl = con.addDesignation(con.getObject(PURL0), PURL.class);
		purl.getPurlAlternatives().add(con.getObject(PURL1));
		purl.getPurlDescribedBy().add(con.getObject(PURL2));
		ValueFactory vf = con.getValueFactory();
		con.add(vf.createURI(PURL1), vf.createURI(NS, "type"), vf.createLiteral("text/html"));
		con.add(vf.createURI(PURL2), vf.createURI(NS, "type"), vf.createLiteral("application/rdf+xml"));
		HttpResponse resp = purl.resolvePURL(purl.toString(), null, "application/rdf+xml", "*", via);
		assertEquals(303, resp.getStatusLine().getStatusCode());
		assertEquals(1, resp.getHeaders("Location").length);
		assertEquals(PURL2, resp.getFirstHeader("Location").getValue());
	}

	public void testUnresolvable() throws Exception {
		con.addDesignation(con.getObject(PURL1), Unresolvable.class);
		PURL purl = con.addDesignation(con.getObject(PURL0), PURL.class);
		purl.getPurlAlternatives().add(con.getObject(PURL1));
		purl.getPurlAlternatives().add(con.getObject(PURL2));
		HttpResponse resp = purl.resolvePURL(purl.toString(), null, "*/*", "*", via);
		assertEquals(302, resp.getStatusLine().getStatusCode());
		assertEquals(2, resp.getHeaders("Location").length);
		assertEquals(PURL2, resp.getFirstHeader("Location").getValue());
	}

	public void testSpecificLanguage() throws Exception {
		PURL purl = con.addDesignation(con.getObject(PURL0), PURL.class);
		purl.getPurlAlternatives().add(con.getObject(PURL1));
		purl.getPurlAlternatives().add(con.getObject(PURL2));
		ValueFactory vf = con.getValueFactory();
		con.add(vf.createURI(PURL1), vf.createURI(NS, "lang"), vf.createLiteral("en-US"));
		con.add(vf.createURI(PURL2), vf.createURI(NS, "lang"), vf.createLiteral("en-CA"));
		HttpResponse resp = purl.resolvePURL(purl.toString(), null, "*/*", "en-CA", via);
		assertEquals(302, resp.getStatusLine().getStatusCode());
		assertEquals(1, resp.getHeaders("Location").length);
		assertEquals(PURL2, resp.getFirstHeader("Location").getValue());
	}

	public void testGeneralLanguage() throws Exception {
		PURL purl = con.addDesignation(con.getObject(PURL0), PURL.class);
		purl.getPurlAlternatives().add(con.getObject(PURL1));
		purl.getPurlAlternatives().add(con.getObject(PURL2));
		ValueFactory vf = con.getValueFactory();
		con.add(vf.createURI(PURL1), vf.createURI(NS, "lang"), vf.createLiteral("fr"));
		con.add(vf.createURI(PURL2), vf.createURI(NS, "lang"), vf.createLiteral("en"));
		HttpResponse resp = purl.resolvePURL(purl.toString(), null, "*/*", "en-CA", via);
		assertEquals(302, resp.getStatusLine().getStatusCode());
		assertEquals(1, resp.getHeaders("Location").length);
		assertEquals(PURL2, resp.getFirstHeader("Location").getValue());
	}

	public void testPreferredLanguage() throws Exception {
		PURL purl = con.addDesignation(con.getObject(PURL0), PURL.class);
		purl.getPurlAlternatives().add(con.getObject(PURL1));
		purl.getPurlAlternatives().add(con.getObject(PURL2));
		ValueFactory vf = con.getValueFactory();
		con.add(vf.createURI(PURL1), vf.createURI(NS, "lang"), vf.createLiteral("fr"));
		con.add(vf.createURI(PURL2), vf.createURI(NS, "lang"), vf.createLiteral("fr-CA"));
		HttpResponse resp = purl.resolvePURL(purl.toString(), null, "*/*", "fr-CA", via);
		assertEquals(302, resp.getStatusLine().getStatusCode());
		assertEquals(1, resp.getHeaders("Location").length);
		assertEquals(PURL2, resp.getFirstHeader("Location").getValue());
	}
}
