package org.purl.accessor.util;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.ten60.netkernel.layer1.nkf.INKFConvenienceHelper;
import org.ten60.netkernel.layer1.nkf.NKFException;
import org.ten60.netkernel.xml.representation.IAspectXDA;
import org.ten60.netkernel.xml.xda.IXDAReadOnly;
import org.ten60.netkernel.xml.xda.IXDAReadOnlyIterator;
import org.ten60.netkernel.xml.xda.XPathLocationException;

public class PurlSearchHelper extends AbstractSearchHelper {
    private static Map<String,String> keywordBasisMap = new HashMap<String,String>();
    
    private URIResolver groupResolver;
    private ResourceStorage groupStorage;
    
    static {
        // TODO: Turn this into a configuration file
        keywordBasisMap.put("id", "/purl/id");
        keywordBasisMap.put("maintainers", "/purl/maintainers/uid");
        keywordBasisMap.put("explicitmaintainers", "/purl/maintainers/uid");        
        keywordBasisMap.put("target", "/purl/target/url");
        keywordBasisMap.put("seealso", "/purl/seealso/url");
    }

    public PurlSearchHelper(URIResolver groupResolver, ResourceStorage groupStorage) {
        super(keywordBasisMap);
        this.groupResolver = groupResolver;
        this.groupStorage = groupStorage;
    }
    
    public String processKeyword(INKFConvenienceHelper context, String key, String value) {
        String retValue = value;
        boolean callSuper = true;
        
        if(key.equals("maintainers")) {
            StringTokenizer st = new StringTokenizer(value, ", ");
            StringBuffer sb = new StringBuffer();
            while(st.hasMoreTokens()) {
                try {
                    String maintainer = st.nextToken();
                    String groupURI = groupResolver.getURI(maintainer);
                    if(groupStorage.resourceExists(context, groupURI)) {
                        if(sb.length() != 0) {
                            sb.append(" ");
                        }
                        
                        sb.append(maintainer);
                    } else {
                        String uri = "active:purl-groups-for-user+user@user:" + maintainer;
                        if(context.exists(uri)) {
                            if(sb.length() != 0) {
                                sb.append(" ");
                            }
                            
                            sb.append(maintainer);
                            
                            IAspectXDA groupsForUserXDA = (IAspectXDA) context.sourceAspect(uri, IAspectXDA.class);
                            IXDAReadOnly groupsForUserXDARO = groupsForUserXDA.getXDA();
                            IXDAReadOnlyIterator rItor = groupsForUserXDARO.readOnlyIterator("/groups/group");
                            while(rItor.hasNext()) {
                                rItor.next();
                                if(sb.length() != 0) {
                                    sb.append(" ");
                                }
                                
                                sb.append(rItor.getText("@id", true));
                            }
                        } else {
                            if(sb.length() != 0) {
                                sb.append(" ");
                            }
                            
                            sb.append(maintainer);
                        }
                    }
                    
                } catch(NKFException nfe) {
                    nfe.printStackTrace();
                } catch (XPathLocationException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                
                retValue = sb.toString();
            }
        } else if(key.equals("target") || key.equals("seealso")) {
            if(retValue.startsWith("http://")) {
                retValue = retValue.substring(7);
            }
            retValue = retValue.replaceAll("/", " ");
        } else if(key.equals("id")) {
            if(retValue.startsWith("/")) {
                if(retValue.endsWith("*")) {
                    retValue = retValue.substring(0, retValue.length() - 1) + " " + retValue.substring(1); 
                } else {
                    retValue = retValue.substring(1);
                }
            }
            
            callSuper = false;
        }
        
        return callSuper ? super.processKeyword(context, key, retValue) : retValue;
    }
}
