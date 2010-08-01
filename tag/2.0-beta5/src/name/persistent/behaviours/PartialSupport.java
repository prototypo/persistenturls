package name.persistent.behaviours;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import name.persistent.concepts.Domain;
import name.persistent.concepts.Partial;

import org.apache.http.HttpResponse;
import org.openrdf.http.object.exceptions.InternalServerError;
import org.openrdf.http.object.exceptions.NotFound;
import org.openrdf.http.object.traits.VersionedObject;
import org.openrdf.model.Value;

public abstract class PartialSupport extends PURLSupport implements Partial, VersionedObject {
	private static final Map<String, Pattern> patterns = new LinkedHashMap<String, Pattern>(
			1024, 0.75f, true) {
		private static final long serialVersionUID = 6748360669055961317L;

		protected boolean removeEldestEntry(Map.Entry<String, Pattern> eldest) {
			return size() > 1024;
		};
	};

	@Override
	public void touchRevision() {
		Domain domain = getPurlBelongsTo();
		if (domain instanceof VersionedObject) {
			((VersionedObject) domain).touchRevision();
		}
	}

	@Override
	public void purlSetEntityHeaders(HttpResponse resp) {
		Domain domain = getPurlBelongsTo();
		if (domain == null) {
			super.purlSetEntityHeaders(resp);
		} else {
			domain.purlSetEntityHeaders(resp);
		}
	}

	@Override
	protected Matcher compile(Value value, String source, String qs) {
		if (value == null)
			return null;
		String pattern = value.stringValue();
		String prefix = getResource().stringValue();
		boolean suffix = pattern.startsWith(prefix);
		if (suffix) {
			pattern = pattern.substring(prefix.length());
		}
		Pattern regex;
		synchronized (patterns) {
			regex = patterns.get(pattern);
		}
		if (regex == null) {
			try {
				regex = Pattern.compile(pattern);
			} catch (PatternSyntaxException e) {
				throw new InternalServerError(e);
			}
			synchronized (patterns) {
				patterns.put(pattern, regex);
			}
		}
		if (qs != null) {
			source = source + "?" + qs;
		}
		if (suffix) {
			assert source.startsWith(prefix);
			source = source.substring(prefix.length());
		}
		Matcher m = regex.matcher(source);
		if (m.matches())
			return m;
		throw new NotFound("No Matching PURL");
	}

	@Override
	protected String apply(Matcher m, String template) {
		if (m == null)
			return template;
		int start = template.indexOf('$');
		if (start < 0)
			return template;
		StringBuilder sb = new StringBuilder(255);
		sb.append(template, 0, start);
		for (int i = start, n = template.length(); i < n; i++) {
			char chr = template.charAt(i);
			if (chr == '$' && i + 1 < n) {
				char next = template.charAt(++i);
				if (next == '$') {
					sb.append(next);
				} else if (next >= '0' && next <= '9') {
					int idx = next - '0';
					try {
						sb.append(m.group(idx));
					} catch (IndexOutOfBoundsException e) {
						sb.append(chr).append(next);
					}
				} else {
					sb.append(chr).append(next);
				}
			} else {
				sb.append(chr);
			}
		}
		return sb.toString();
	}

}
