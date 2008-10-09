package org.purl.storage.util;

public class StorageHelper {
	/**
	 * This method cleans up URIs associated with PURL data
	 * prior to storage in the database.
	 * 
	 * @param url
	 * @return cleaned up version of the url
	 */
	public static String convertForStorage(String url) {
		String retValue = url;
		
		// Replace single quotations with double single
		// quotations to avoid SQL problems
		if(retValue.contains("'")) {
			retValue = retValue.replaceAll("'", "''");
		}
		
		// We need to escape + signs in URI values to avoid
		// confusing NetKernel active URI handling
		if(retValue.contains("%2B")) {
			retValue = retValue.replaceAll("%2B", "+");
		}
		
		return retValue;
	}
}
