/*
 * Copyright (c) Zepheira LLC, Some rights reserved.
 * 
 * Source code developed for this project is licensed under the Apache
 * License, Version 2.0. See the file LICENSE.txt for details.
 */
package name.persistent.concepts;

import java.lang.Object;
import java.lang.String;
import org.openrdf.repository.object.annotations.iri;

/** Possible credential for administrating PURLs. */
@iri("http://persistent.name/rdf/2010/purl#RequestedUser")
public interface RequestedUser extends Party {
	@iri("http://persistent.name/rdf/2010/purl#affiliation")
	String getPurlAffiliation();
	@iri("http://persistent.name/rdf/2010/purl#affiliation")
	void setPurlAffiliation(String purlAffiliation);

	@iri("http://persistent.name/rdf/2010/purl#algorithm")
	String getPurlAlgorithm();
	@iri("http://persistent.name/rdf/2010/purl#algorithm")
	void setPurlAlgorithm(String purlAlgorithm);

	@iri("http://persistent.name/rdf/2010/purl#encoded")
	byte[] getPurlEncoded();
	@iri("http://persistent.name/rdf/2010/purl#encoded")
	void setPurlEncoded(byte[] purlEncoded);

	@iri("http://persistent.name/rdf/2010/purl#hint")
	String getPurlHint();
	@iri("http://persistent.name/rdf/2010/purl#hint")
	void setPurlHint(String purlHint);

	@iri("http://persistent.name/rdf/2010/purl#justification")
	String getPurlJustification();
	@iri("http://persistent.name/rdf/2010/purl#justification")
	void setPurlJustification(String purlJustification);

	@iri("http://persistent.name/rdf/2010/purl#mbox")
	Object getPurlMbox();
	@iri("http://persistent.name/rdf/2010/purl#mbox")
	void setPurlMbox(Object purlMbox);

}
