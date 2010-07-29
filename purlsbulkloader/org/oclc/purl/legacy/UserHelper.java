package org.oclc.purl.legacy;

import java.util.StringTokenizer;

public class UserHelper {
	/**
	 * Generates a valid user list from the specified list. This will remove
	 * references to unknown users and substitute in the admin account if
	 * need be.
	 * 
	 * @param userlist
	 * @return String representing a valid user list if possible
	 */
	public static String generateValidUserList(String userlist, UserValidator uv) {
		String retValue = null;
		StringBuffer sb = new StringBuffer();
		
		StringTokenizer st = new StringTokenizer(userlist, ",");
		
		boolean lastWasComma = false;
		
		while(st.hasMoreTokens()) {
			// If we have more users and have already appended 
			// something, add a comma.
			
			if(sb.length()>0 && !lastWasComma) {
				sb.append(",");
				lastWasComma = true;
			}
			
			String user = st.nextToken();
			
			// Drop invalid user references, only keep
			// good ones.
			
			if(uv.valid(user)) {
				sb.append(user);
				lastWasComma = false;
			} else {
				System.out.println(user + " is not a valid user or group. Ignoring...");
			}
		}
		
		// If we have no valid users, use admin as a last
		// resort.
		
		if(sb.length()==0) {
			sb.append("admin");
			System.out.println("Substituting admin user for " + userlist);
		}
		
		retValue = sb.toString();
		if(retValue.endsWith(",")) {
			retValue = retValue.substring(0,retValue.length()-1);
		}
		return retValue;
	}
}
