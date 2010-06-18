package name.persistent.behaviours;

import java.io.IOException;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import name.persistent.concepts.PURL;
import name.persistent.concepts.Redirection;
import name.persistent.concepts.Unresolvable;

import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.message.BasicHttpRequest;
import org.apache.http.message.BasicHttpResponse;
import org.openrdf.http.object.client.HTTPObjectClient;
import org.openrdf.http.object.exceptions.GatewayTimeout;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.object.ObjectConnection;
import org.openrdf.repository.object.ObjectRepository;
import org.openrdf.repository.object.RDFObject;

public abstract class PURLValidationSupport implements RDFObject, PURL {
	private static final ProtocolVersion HTTP11 = new ProtocolVersion("HTTP",
			1, 1);
	private static final String NS = "http://persistent.name/rdf/2010/purl#";

	public Set<HttpResponse> purlValidate(Set<RDFObject> targets)
			throws IOException, DatatypeConfigurationException,
			RepositoryException {
		XMLGregorianCalendar today = today();
		Map<RDFObject, HttpResponse> map = new LinkedHashMap<RDFObject, HttpResponse>();
		for (RDFObject subj : targets) {
			map.put(subj, resolve(subj.getResource().stringValue()));
		}
		markResolved(map, today);
		return new LinkedHashSet<HttpResponse>(map.values());
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

	private HttpResponse resolve(String url) throws IOException {
		HTTPObjectClient client = HTTPObjectClient.getInstance();
		try {
			return client.service(new BasicHttpRequest("HEAD", url));
		} catch (GatewayTimeout e) {
			int code = e.getStatusCode();
			String msg = e.getMessage();
			return new BasicHttpResponse(HTTP11, code, msg);
		}
	}

	private void markResolved(Map<RDFObject, HttpResponse> map, XMLGregorianCalendar today)
			throws RepositoryException {
		ObjectRepository repository = getObjectConnection().getRepository();
		ObjectConnection con = repository.getConnection();
		try {
			con.setAutoCommit(false); // begin
			ValueFactory vf = con.getValueFactory();
			URI lastResolved = vf.createURI(NS, "last-resolved");
			Literal now = vf.createLiteral(today);
			for (Map.Entry<RDFObject, HttpResponse> e : map.entrySet()) {
				RDFObject target = e.getKey();
				int code = e.getValue().getStatusLine().getStatusCode();
				con.remove(target.getResource(), lastResolved, null);
				if (code < 400) {
					con.removeDesignation(target, Unresolvable.class);
				}
				if (code < 300) {
					con.removeDesignation(target, Redirection.class);
				}
			}
			for (Map.Entry<RDFObject, HttpResponse> e : map.entrySet()) {
				RDFObject target = e.getKey();
				int code = e.getValue().getStatusLine().getStatusCode();
				if (code >= 400) {
					con.addDesignation(target, Unresolvable.class);
				} else if (code >= 300) {
					con.addDesignation(target, Redirection.class);
				}
				con.add(target.getResource(), lastResolved, now);
			}
			con.setAutoCommit(true); // commit
		} finally {
			con.rollback();
			con.close();
		}
	}
}
