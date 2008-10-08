package org.purl.accessor.util;

import java.util.Iterator;
import java.util.List;

public class DataHelper {
    public static String cleanseInput(String input) {
        // For now, fix the apostrophe problem
    	String retValue = input.replaceAll("'", "''");
    	retValue = retValue.replaceAll("&", "&amp;");
    	return retValue;
    }
    
    public static String generateListAsString(List<String> list) {
    	Iterator<String> itor = list.iterator();
    	StringBuffer sb = new StringBuffer();
    	
    	while(itor.hasNext()) {
    		sb.append(itor.next());
    		
    		if(itor.hasNext()) {
    			sb.append(",");
    		}
    	}
    	
    	return sb.toString();
    }
}
