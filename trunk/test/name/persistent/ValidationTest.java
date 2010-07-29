package name.persistent;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import junit.framework.TestCase;
import name.persistent.concepts.Domain;
import name.persistent.concepts.PURL;
import name.persistent.concepts.Server;
import name.persistent.concepts.Service;
import name.persistent.concepts.Unresolvable;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.message.BasicHttpResponse;
import org.openrdf.http.object.client.HTTPObjectClient;
import org.openrdf.http.object.client.HTTPService;
import org.openrdf.model.Literal;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.object.ObjectConnection;
import org.openrdf.repository.object.ObjectFactory;
import org.openrdf.repository.object.ObjectRepository;
import org.openrdf.repository.object.config.ObjectRepositoryConfig;
import org.openrdf.repository.object.config.ObjectRepositoryFactory;
import org.openrdf.sail.Sail;
import org.openrdf.sail.auditing.AuditingSail;
import org.openrdf.sail.memory.MemoryStore;
import org.openrdf.sail.optimistic.OptimisticRepository;


public class ValidationTest extends TestCase {
	private static final String NS = "http://persistent.name/rdf/2010/purl#";
	private static final String ORIGIN = "http://example.com/";
	private static final String DOMAIN = "http://example.com/test/";
	private static final String PURL0 = "http://example.com/test/test0";
	private static final String PURL1 = "http://example.com/test/test1";
	private ObjectRepositoryConfig config = new ObjectRepositoryConfig();
	private ObjectRepository repository;
	private ObjectConnection con;
	private ObjectFactory of;
	private XMLGregorianCalendar xgc;
	private Domain domain;
	private PURL purl0;
	private ValueFactory vf;
	private URI lastResolved;

	@Override
	public void setUp() throws Exception {
		HTTPObjectClient.getInstance().resetCache();
		repository = createRepository();
		con = repository.getConnection();
		of = con.getObjectFactory();
		vf = con.getValueFactory();
		lastResolved = vf.createURI(NS, "last-resolved");
		URI rel = vf.createURI(NS, "rel");
		con.add(vf.createURI(NS, "renamedTo"), rel, vf.createLiteral("canonical"));
		con.add(vf.createURI(NS, "alternative"), rel, vf.createLiteral("alternate"));
		con.add(vf.createURI(NS, "describedBy"), rel, vf.createLiteral("describedby"));
		con.add(vf.createURI(NS, "redirectsTo"), rel, vf.createLiteral("located"));
		String uri = "http://localhost:3128/";
		Server root = con.addDesignation(con.getObject(uri), Server.class);
		Domain origin = con.addDesignation(con.getObject(ORIGIN), Domain.class);
		root.getPurlServes().add(origin);
		domain = con.addDesignation(con.getObject(DOMAIN), Domain.class);
		domain.getPurlDomainOf().add(origin);
		Service service = con.addDesignation(of.createObject(), Service.class);
		service.setPurlServer(root);
		domain.getPurlServices().add(service);
		purl0 = con.addDesignation(con.getObject(PURL0), PURL.class);
		purl0.setPurlRenamedTo(con.getObject(PURL1));
		purl0.setPurlPartOf(domain);
		GregorianCalendar cal;
		int n = DatatypeConstants.FIELD_UNDEFINED;
		DatatypeFactory f = DatatypeFactory.newInstance();
		cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
		xgc = f.newXMLGregorianCalendar(cal);
		xgc.setTime(n, n, n, n);
		HTTPObjectClient.getInstance().setProxy(new InetSocketAddress("example.com", 80), new HTTPService(){
			public HttpResponse service(HttpRequest request) throws IOException {
				return new BasicHttpResponse(new ProtocolVersion("HTTP", 1, 1), 500, "Error");
			}});
	}

	@Override
	public void tearDown() throws Exception {
		con.close();
		repository.shutDown();
	}

	private ObjectRepository createRepository() throws Exception {
		Sail sail = new MemoryStore();
		sail = new AuditingSail(sail);
		Repository repo = new OptimisticRepository(sail);
		repo.initialize();
		ObjectRepositoryFactory factory = new ObjectRepositoryFactory();
		return factory.createRepository(config, repo);
	}

	public void testWith() throws Exception {
		con.add(vf.createURI(PURL1), lastResolved, vf.createLiteral(xgc));
		domain.setPurlMaxUnresolvedDays(1);
		domain.validatePURLs(xgc, 1, 1, xgc);
		assertTrue(con.getObject(PURL1) instanceof Unresolvable);
		RepositoryResult<Statement> stmts = con.getStatements(vf.createURI(PURL1), lastResolved, null);
		assertTrue(stmts.hasNext());
		Literal lit = (Literal) stmts.next().getObject();
		assertEquals(xgc, lit.calendarValue());
	}

	public void testWithout() throws Exception {
		domain.setPurlMaxUnresolvedDays(1);
		domain.validatePURLs(xgc, 1, 1, xgc);
		assertTrue(con.getObject(PURL1) instanceof Unresolvable);
		RepositoryResult<Statement> stmts = con.getStatements(vf.createURI(PURL1), lastResolved, null);
		assertTrue(stmts.hasNext());
		Literal lit = (Literal) stmts.next().getObject();
		assertEquals(xgc, lit.calendarValue());
	}

	public void testBefore() throws Exception {
		GregorianCalendar cal;
		XMLGregorianCalendar yesterday;
		int n = DatatypeConstants.FIELD_UNDEFINED;
		DatatypeFactory f = DatatypeFactory.newInstance();
		cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
		yesterday = f.newXMLGregorianCalendar(cal);
		yesterday.setTime(n, n, n, n);
		yesterday.add(f.newDurationDayTime("-P1D"));
		con.add(vf.createURI(PURL1), lastResolved, vf.createLiteral(yesterday));
		domain.setPurlMaxUnresolvedDays(1);
		domain.validatePURLs(xgc, 1, 1, xgc);
		assertTrue(con.getObject(PURL1) instanceof Unresolvable);
		RepositoryResult<Statement> stmts = con.getStatements(vf.createURI(PURL1), lastResolved, null);
		assertTrue(stmts.hasNext());
		Literal lit = (Literal) stmts.next().getObject();
		assertEquals(xgc, lit.calendarValue());
	}
}
