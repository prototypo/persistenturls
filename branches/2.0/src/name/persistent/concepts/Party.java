/*
 * Copyright (c) Zepheira LLC, Some rights reserved.
 * 
 * Source code developed for this project is licensed under the Apache
 * License, Version 2.0. See the file LICENSE.txt for details.
 */
package name.persistent.concepts;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.callimachusproject.framework.Deletable;
import org.callimachusproject.framework.Editable;
import org.callimachusproject.framework.Viewable;
import org.openrdf.repository.object.annotations.iri;

/** An individual or group. */
@iri("http://persistent.name/rdf/2010/purl#Party")
public interface Party extends Viewable, Editable, Deletable {
	@iri("http://persistent.name/rdf/2010/purl#name")
	String getPurlName();

	@iri("http://persistent.name/rdf/2010/purl#name")
	void setPurlName(String purlName);

	String getPurlId();

	void writeTo(XMLStreamWriter xml) throws XMLStreamException;

}
