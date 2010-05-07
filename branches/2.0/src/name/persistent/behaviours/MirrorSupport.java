package name.persistent.behaviours;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import javax.xml.datatype.XMLGregorianCalendar;

import name.persistent.concepts.RemoteGraph;
import name.persistent.concepts.Unresolvable;

import org.apache.http.HttpResponse;
import org.openrdf.http.object.concepts.Transaction;
import org.openrdf.http.object.traits.VersionedObject;
import org.openrdf.repository.object.annotations.name;
import org.openrdf.repository.object.annotations.sparql;

public abstract class MirrorSupport {
	private static final String PROTOCOL = "1.1";
	private static final String PREFIX = "PREFIX purl:<http://persistent.name/rdf/2010/purl#>\n";
	/** Date format pattern used to generate the header in RFC 1123 format. */
	public static final String PATTERN_RFC1123 = "EEE, dd MMM yyyy HH:mm:ss zzz";
	/** The time zone to use in the date header. */
	public static final TimeZone GMT = TimeZone.getTimeZone("GMT");
	private static final DateFormat dateformat;
	private static String hostname;
	static {
		try {
			hostname = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			hostname = "AliBaba";
		}
	}
	private static final String VIA = PROTOCOL + " " + hostname;
	private static String WARN_110 = "110 " + hostname
			+ " \"Response is stale\"";
	private static String WARN_111 = "111 " + hostname
			+ " \"Revalidation failed\"";
	private static String WARN_199 = "199 " + hostname
			+ " \"Mirrored response\"";
	static {
		dateformat = new SimpleDateFormat(PATTERN_RFC1123, Locale.US);
		dateformat.setTimeZone(GMT);
	}

	@sparql(PREFIX + "SELECT ?graph\n"
			+ "WHERE { GRAPH ?graph { $target a ?type FILTER (?type != purl:RemoteResource) }\n"
			+ "?graph a purl:RemoteGraph; purl:last-validated ?last }\n"
			+ "ORDER BY desc(?last) LIMIT 1")
	protected abstract RemoteGraph getRemoteGraphsOf(
			@name("target") Object target);

	protected void mirrorEntityHeaders(Object target, HttpResponse resp) {
		RemoteGraph graph = getRemoteGraphsOf(target);
		if (graph != null) {
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
		} else if (target instanceof VersionedObject) {
			VersionedObject ver = (VersionedObject) target;
			setHeader(resp, "ETag", ver.revisionTag(0));
			Transaction revision = ver.getRevision();
			if (revision != null) {
				setHeader(resp, "Last-Modified", revision.getCommittedOn());
			}
		}
	}

	private void setHeader(HttpResponse resp, String name, String value) {
		if (value != null && !resp.containsHeader(name)) {
			resp.setHeader(name, value);
		}
	}

	private void setHeader(HttpResponse resp, String name,
			XMLGregorianCalendar value) {
		if (value != null && !resp.containsHeader(name)) {
			Date time = value.toGregorianCalendar().getTime();
			resp.setHeader(name, dateformat.format(time));
		}
	}
}
