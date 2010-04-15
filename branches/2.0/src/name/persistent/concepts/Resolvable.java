/*
 * Copyright (c) Zepheira LLC, Some rights reserved.
 * 
 * Source code developed for this project is licensed under the Apache
 * License, Version 2.0. See the file LICENSE.txt for details.
 */
package name.persistent.concepts;

import org.apache.http.HttpResponse;
import org.openrdf.repository.object.annotations.matches;

@matches( { "http://*", "https://*" })
public interface Resolvable {

	HttpResponse resolvePURL(String source, String qs, String accept, String language, int max)
			throws Exception;

}
