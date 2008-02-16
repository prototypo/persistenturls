package org.purl.accessor.util;

import java.util.Iterator;

public class PURLDomainIterator implements Iterator<String> {
    
    private StringBuffer sb = new StringBuffer();
    private String [] components;
    private int index = 0;
    
    private static URIResolver domainResolver = new DomainResolver();
    
    public PURLDomainIterator(String purl) {
        if(purl.startsWith("ffcpl:/purl")) {
            purl = purl.substring(11);
        }
        
        components = purl.split("/");
    }

    public boolean hasNext() {
        return index < components.length - 2;
    }

    public String next() {
        String retValue = null;
        
        if(hasNext()) {
            sb.append("/");
            sb.append(components[++index]);
            retValue = domainResolver.getURI(sb.toString());
        }
        
        return retValue;
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }
    
    public static void main(String [] args) {
        PURLDomainIterator pdi = new PURLDomainIterator("ffcpl:/purl/net/foo/bar");
        while(pdi.hasNext()) {
            System.out.println(pdi.next());
        }
    }
}
