package name.persistent.behaviours;

import java.io.IOException;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import name.persistent.concepts.Resolvable;
import name.persistent.concepts.Unresolvable;

import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.message.BasicHttpRequest;
import org.apache.http.message.BasicHttpResponse;
import org.openrdf.http.object.annotations.cacheControl;
import org.openrdf.http.object.annotations.operation;
import org.openrdf.http.object.annotations.type;
import org.openrdf.http.object.client.HTTPObjectClient;
import org.openrdf.http.object.exceptions.GatewayTimeout;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.object.ObjectConnection;
import org.openrdf.repository.object.ObjectFactory;
import org.openrdf.repository.object.RDFObject;

public abstract class HeadSupport implements RDFObject, Resolvable {
	private static final ProtocolVersion HTTP11 = new ProtocolVersion("HTTP", 1, 1);
	private static final String NS = "http://persistent.name/rdf/2010/purl#";

	@operation("head")
	@type("message/http")
	@cacheControl("no-store")
	public HttpResponse headResponse() throws IOException,
			DatatypeConfigurationException, RepositoryException {
		Resource subj = getResource();
		XMLGregorianCalendar today = today();
		HttpResponse resp = resolve(subj);
		ObjectConnection con = getObjectConnection();
		boolean autoCommit = con.isAutoCommit();
		con.setAutoCommit(false); // begin
		try {
			markResolved(con, subj, resp.getStatusLine().getStatusCode(), today);
			if (autoCommit) {
				con.setAutoCommit(true); // commit
			}
		} finally {
			if (autoCommit && !con.isAutoCommit()) {
				con.rollback();
				con.setAutoCommit(false);
			}
		}
		return resp;
	}

	private XMLGregorianCalendar today() throws DatatypeConfigurationException {
		GregorianCalendar cal;
		XMLGregorianCalendar today;
		int n = DatatypeConstants.FIELD_UNDEFINED;
		DatatypeFactory f = DatatypeFactory.newInstance();
		cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
		today = f.newXMLGregorianCalendar(cal);
		today.setTime(n, n, n, n);
		return today;
	}

	private HttpResponse resolve(Resource subj) throws IOException {
		String url = subj.stringValue();
		HTTPObjectClient client = HTTPObjectClient.getInstance();
		try {
			return client.service(new BasicHttpRequest("HEAD", url));
		} catch (GatewayTimeout e) {
			int code = e.getStatusCode();
			String msg = e.getMessage();
			return new BasicHttpResponse(HTTP11, code, msg);
		}
	}

	private void markResolved(ObjectConnection con, Resource subj, int code,
			XMLGregorianCalendar today) throws RepositoryException {
		ObjectFactory of = con.getObjectFactory();
		ValueFactory vf = con.getValueFactory();
		URI lastResolved = vf.createURI(NS, "last-resolved");
		Literal now = vf.createLiteral(today);
		con.remove(subj, lastResolved, null);
		if (code < 400) {
			con.removeDesignation(of.createObject(subj), Unresolvable.class);
		}
		con.add(subj, lastResolved, now);
		if (code >= 400) {
			con.addDesignation(of.createObject(subj), Unresolvable.class);
		}
	}
}
