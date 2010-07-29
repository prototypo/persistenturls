package org.purl.accessor.domain;

public class Domain {
    private String id;
    boolean pub;
    String name;
    String [] maintainers;
    String [] writers;
    
    public Domain(String id, boolean pub, String name, String [] maintainers, String [] writers) {
        this.id = id;
        this.pub = pub;
        this.name = name;
        this.maintainers = maintainers;
        this.writers = writers;
    }
    
    public String toString() {
        StringBuffer sb = new StringBuffer("<domain>");
        sb.append("<public>");
        sb.append(Boolean.toString(pub));
        sb.append("</public>");
        sb.append("<id>");
        sb.append(id);
        sb.append("</id>");
        sb.append("<name>");
        sb.append(name);
        sb.append("</name>");
        sb.append("<maintainers>");
        
        for(String s : maintainers) {
            sb.append("<uid>");
            sb.append(s);
            sb.append("</uid>");
        }
        
        sb.append("</maintainers>");
        sb.append("<writers>");

        for(String s : writers) {
            sb.append("<uid>");
            sb.append(s);
            sb.append("</uid>");
        }
        sb.append("</writers>");
        sb.append("</domain>");
        
        return sb.toString();
    }
}
