package org.purl.accessor.purl;

import org.ten60.netkernel.layer1.nkf.INKFConvenienceHelper;
import org.ten60.netkernel.layer1.nkf.NKFException;
import org.ten60.netkernel.xml.representation.IAspectXDA;
import org.ten60.netkernel.xml.xda.IXDAReadOnly;
import org.ten60.netkernel.xml.xda.IXDAReadOnlyIterator;
import org.ten60.netkernel.xml.xda.XPathLocationException;
import org.purl.accessor.util.AbstractSearchHelper;
import org.purl.accessor.util.URIResolver;
import org.purl.accessor.ResourceStorage;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

public class PurlSearchHelper extends AbstractSearchHelper {
    private static Map<String, String> keywordBasisMap = new HashMap<String, String>();

    private URIResolver groupResolver;
    private ResourceStorage groupStorage;

    static {
        // TODO: Turn this into a configuration file
        keywordBasisMap.put("p_id", "/purl/id");
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

        if (key.endsWith("maintainers")) {
            StringTokenizer st = new StringTokenizer(value, ", ");
            StringBuffer sb = new StringBuffer();
            while (st.hasMoreTokens()) {
                try {
                    String maintainer = st.nextToken();
                    String groupURI = groupResolver.getURI(maintainer);
                    if (groupStorage.resourceExists(context, groupURI) || "explicitmaintainers".equals(key)) {

                        if (sb.length() != 0) {
                            sb.append(" ");
                        }

                        sb.append(maintainer);
                    } else {

                        String uri="active:purl-storage-groups-for-user+uri@ffcpl:/user/" + maintainer;
                        IAspectXDA groupsForUserXDA = (IAspectXDA) context.sourceAspect(uri, IAspectXDA.class);
                        if (sb.length() != 0) {
                            sb.append(" ");
                        }

                        sb.append(maintainer);


                        IXDAReadOnly groupsForUserXDARO = groupsForUserXDA.getXDA();
                        IXDAReadOnlyIterator rItor = groupsForUserXDARO.readOnlyIterator("/groups/group");
                        while (rItor.hasNext()) {
                            rItor.next();
                            if (sb.length() != 0) {
                                sb.append(" ");
                            }

                            sb.append(rItor.getText("@id", true));
                        }

                    }

                } catch (NKFException nfe) {
                    nfe.printStackTrace();
                } catch (XPathLocationException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }


            }
            return "maintainers:(" + sb.toString() + ")";
        } else if (key.equals("target") || key.equals("seealso")) {
            String type;
            if (key.equals("seealso")) {
                type = "type:303";
            } else {
                type = "-type:303";
            }
            if (retValue.startsWith("http://")) {
                retValue = retValue.substring(7);

            }
            retValue = retValue.replaceAll("/", " ");
            retValue = " (target:(" + retValue + ") AND " + type + ") ";
            return retValue;
        }

        return super.processKeyword(context, key, retValue);
    }
}
