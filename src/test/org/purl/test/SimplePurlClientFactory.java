package org.purl.test;

/**
 * Simple helper factory for simplePurlClients.
 * 
 * @author brian
 *
 */

// We don't need more than one instance, we just need a static 
// instance across the tests. If we need more than one instance
// we'll have to be smart about concurrency.

public class SimplePurlClientFactory {
    private static simplePurlClient instance = new simplePurlClient();
    
    public static simplePurlClient getInstance() {
        return instance;
    }

}
