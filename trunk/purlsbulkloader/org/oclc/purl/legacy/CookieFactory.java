package org.oclc.purl.legacy;

/**
 * Simple helper factory for PURLClients.
 * 
 * @author brian
 *
 */

// We don't need more than one Cookie instance, we just need a static 
// instance across the tests. If we need more than one instance
// we'll have to be smart about concurrency.

public class CookieFactory {
    private static String cookie;
    
    public synchronized static void setCookie(String cookie) {
        CookieFactory.cookie = cookie;
    }
    
    public synchronized static String getCookie() {
        return CookieFactory.cookie;
    }

}
