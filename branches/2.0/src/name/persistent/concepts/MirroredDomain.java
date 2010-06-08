package name.persistent.concepts;

import org.openrdf.repository.object.annotations.iri;

/** Domain that should be updated regularly from an authority. */
@iri("http://persistent.name/rdf/2010/purl#MirroredDomain")
public interface MirroredDomain extends Domain {

	boolean reload() throws Exception;

	void purlRefreshGraphs() throws Exception;

	void purlStallGraphs() throws Exception;
}
