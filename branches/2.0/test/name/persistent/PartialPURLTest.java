package name.persistent;

import java.util.Collections;
import java.util.Set;

import junit.framework.TestCase;
import name.persistent.concepts.Domain;
import name.persistent.concepts.Resolvable;

import org.apache.http.HttpResponse;
import org.openrdf.http.object.exceptions.InternalServerError;
import org.openrdf.http.object.exceptions.NotFound;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.object.ObjectConnection;
import org.openrdf.repository.object.ObjectRepository;
import org.openrdf.repository.object.config.ObjectRepositoryFactory;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.memory.MemoryStore;


public class PartialPURLTest extends TestCase {
	private static final String NS = "http://persistent.name/rdf/2010/purl#";
	private static final String PURL0 = "http://test.persistent.name/test/test0/";
	private static final String PURL1 = "http://test.persistent.name/test/test1/";
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
		con.add(vf.createURI(NS, "alternative"), rel, vf.createLiteral("alternate"));
	}

	public void tearDown() throws Exception {
		con.close();
		repo.shutDown();
	}

	public void testRegex() throws Exception {
		Domain purl = con.addDesignation(con.getObject(PURL0), Domain.class);
		purl.getPurlAlternatives().add(con.getObject("http://docs.$1/pages/$2.html"));
		purl.setPurlPattern("http://test.([^/]*)/(.*)");
		HttpResponse resp = resolvePURL("http://test.persistent.name/test/test0/item");
		assertEquals(302, resp.getStatusLine().getStatusCode());
		assertEquals(1, resp.getHeaders("Location").length);
		assertEquals("http://docs.persistent.name/pages/test/test0/item.html",
				resp.getFirstHeader("Location").getValue());
	}

	public void testRegexWithBadTemplate() throws Exception {
		Domain purl = con.addDesignation(con.getObject(PURL0), Domain.class);
		purl.getPurlAlternatives().add(con.getObject("http://docs.$1/pages/$2/$3.html"));
		purl.setPurlPattern("http://test.([^/]*)/(.*)");
		HttpResponse resp = resolvePURL("http://test.persistent.name/test/test0/item");
		assertEquals(302, resp.getStatusLine().getStatusCode());
		assertEquals(1, resp.getHeaders("Location").length);
		assertEquals("http://docs.persistent.name/pages/test/test0/item/$3.html",
				resp.getFirstHeader("Location").getValue());
	}

	public void testInvalidRegex() throws Exception {
		Domain purl = con.addDesignation(con.getObject(PURL0), Domain.class);
		purl.getPurlAlternatives().add(con.getObject("http://docs.$1/pages/$2.html"));
		purl.setPurlPattern("http://test.([^/]*/(.*)");
		try {
			resolvePURL("http://test.persistent.name/test/test0/item");
			fail();
		} catch (InternalServerError e) {
			// invalid regex
		}
	}

	public void testBadRegex() throws Exception {
		Domain purl = con.addDesignation(con.getObject(PURL0), Domain.class);
		purl.getPurlAlternatives().add(con.getObject("http://docs.persistent.name/pages/test/test0/$1.html"));
		purl.setPurlPattern("(.*)\\.rdf");
		try {
			resolvePURL("http://test.persistent.name/test/test0/item");
			fail();
		} catch (NotFound e) {
		}
	}

	public void testPathFragment() throws Exception {
		Domain purl = con.addDesignation(con.getObject(PURL0), Domain.class);
		purl.getPurlAlternatives().add(con.getObject(PURL1));
		HttpResponse resp = resolvePURL("http://test.persistent.name/test/test0/item");
		assertEquals(302, resp.getStatusLine().getStatusCode());
		assertEquals(1, resp.getHeaders("Location").length);
		assertEquals(PURL1, resp.getFirstHeader("Location").getValue());
	}

	public void testZonedPURL() throws Exception {
		Domain purl = con.addDesignation(con.getObject(PURL0), Domain.class);
		purl.getPurlAlternatives().add(con.getObject(PURL1));
		HttpResponse resp = resolvePURL("http://my.test.persistent.name/test/test0/");
		assertEquals(302, resp.getStatusLine().getStatusCode());
		assertEquals(1, resp.getHeaders("Location").length);
		assertEquals(PURL1, resp.getFirstHeader("Location").getValue());
	}

	public void testZonedPartialPURL() throws Exception {
		Domain purl = con.addDesignation(con.getObject(PURL0), Domain.class);
		purl = con.addDesignation(purl, Domain.class);
		purl.getPurlAlternatives().add(con.getObject(PURL1));
		HttpResponse resp = resolvePURL("http://my.test.persistent.name/test/test0/item");
		assertEquals(302, resp.getStatusLine().getStatusCode());
		assertEquals(1, resp.getHeaders("Location").length);
		assertEquals(PURL1, resp.getFirstHeader("Location").getValue());
	}

	public void testZonedPartialPatternPURL() throws Exception {
		Domain purl = con.addDesignation(con.getObject(PURL0), Domain.class);
		purl.getPurlAlternatives().add(con.getObject("http://docs.$2/pages/$1/$3.html"));
		purl.setPurlPattern("http://([^.]*)\\.test\\.([^/]*)/.*/([^/]*)");
		HttpResponse resp = resolvePURL("http://my.test.persistent.name/test/test0/item");
		assertEquals(302, resp.getStatusLine().getStatusCode());
		assertEquals(1, resp.getHeaders("Location").length);
		assertEquals("http://docs.persistent.name/pages/my/item.html", resp.getFirstHeader("Location").getValue());
	}

	public void testUnknownPURL() throws Exception {
		Domain purl = con.addDesignation(con.getObject(PURL0), Domain.class);
		purl.getPurlAlternatives().add(con.getObject(PURL1));
		try {
			resolvePURL("http://my.persistent.name/test/test0/");
			fail();
		} catch (NotFound e) {
		}
	}

	private HttpResponse resolvePURL(String uri) throws Exception {
		Resolvable target = (Resolvable) con.getObject(uri);
		Set<String> via = Collections.emptySet();
		return target.resolvePURL(uri, null, null, "*", via);
	}
}
