package name.persistent.concepts;

import org.openrdf.repository.object.annotations.iri;

/** Domain that should use the configured services to resolve PURLs and update those services regularly from an authority. */
@iri("http://persistent.name/rdf/2010/purl#RemoteDomain")
public interface RemoteDomain extends MirroredDomain {
}
