package org.purl.accessor.domain;

import org.purl.accessor.util.URIResolver;

import java.util.Iterator;

public class DomainIterator implements Iterator<String> {
    
    private StringBuffer sb = new StringBuffer();
    private String [] components;
    private int index = 0;
    
    private static URIResolver domainResolver = new DomainResolver();
    
    public DomainIterator(String purl) {
        if(purl.startsWith("ffcpl:/purl")) {
            purl = purl.substring(11);
        } else if(purl.startsWith("ffcpl:/domain")) {
            purl = purl.substring(13);
        }
        if (purl.endsWith("/")) {
            purl = purl.substring(0,purl.length()-1);
        }
        components = purl.split("/");
    }

    public boolean hasNext() {
        return index < components.length - 1;
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
        DomainIterator pdi = new DomainIterator("ffcpl:/purl/net/foo/bar");
        while(pdi.hasNext()) {
            System.out.println(pdi.next());
        }
        
        pdi = new DomainIterator("ffcpl:/domain/net/foo/bar");
        
        while(pdi.hasNext()) {
            System.out.println(pdi.next());
        }        
    }
}
