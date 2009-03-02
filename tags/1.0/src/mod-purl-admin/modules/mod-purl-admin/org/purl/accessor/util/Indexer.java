package org.purl.accessor.util;

public class Indexer {
    private static boolean indexing;
    
    public synchronized static boolean alreadyIndexing() {
        return indexing;
    }
    
    public synchronized static boolean acquireIndexingLock() {
        boolean retValue = false;
        
        if(!indexing) {
            indexing = retValue = true;
        }
        
        return retValue;
    }
    
    public synchronized static void doneIndexing() {
        indexing = false;
    }
}
