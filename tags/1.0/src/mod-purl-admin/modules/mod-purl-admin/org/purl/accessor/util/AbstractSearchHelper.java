package org.purl.accessor.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ten60.netkernel.layer1.nkf.INKFConvenienceHelper;
import org.ten60.netkernel.layer1.nkf.NKFException;
import org.ten60.netkernel.xml.representation.IAspectXDA;
import org.ten60.netkernel.xml.xda.IXDAReadOnly;
import org.ten60.netkernel.xml.xda.IXDAReadOnlyIterator;
import org.ten60.netkernel.xml.xda.XPathLocationException;

import com.ten60.netkernel.urii.IURRepresentation;

abstract public class AbstractSearchHelper implements SearchHelper {
    private Map<String,String> keywordBasisMap;
    
    private AbstractSearchHelper() {
    }
    
    protected AbstractSearchHelper(Map<String,String> keywordBasisMap) {
        this.keywordBasisMap = keywordBasisMap;
    }
    
    private boolean keywordBasisMatches(String keyword, String basis) {
        boolean retValue = false;
        String registeredBasis = keywordBasisMap.get(keyword);
        retValue = (registeredBasis != null) && registeredBasis.equals(basis);
        return retValue;
    }
    
    public String[] processResults(INKFConvenienceHelper context, String key, IURRepresentation result) {
        String [] retValue = null;
        List<String> resultList = new ArrayList<String>();
        
        try {
            IAspectXDA searchXDA = (IAspectXDA) context.transrept(result, IAspectXDA.class);
            IXDAReadOnly roSearchXDA = searchXDA.getXDA();
            
            IXDAReadOnlyIterator roXDAItor = roSearchXDA.readOnlyIterator("//match");

            while(roXDAItor.hasNext()) {
                roXDAItor.next();
                String uri = roXDAItor.getText("docid", true);
                String basis = roXDAItor.getText("basis", true);
                
                if(keywordBasisMatches(key,basis)) {
                    resultList.add(uri);
                }
            }
            
            retValue = new String[resultList.size()];
            int idx=0;
            for(String s : resultList) {
                retValue[idx++] = s;
            }
            
        } catch (NKFException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (XPathLocationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        return retValue;
    }
    
    public String processKeyword(INKFConvenienceHelper context, String key, String value) {
        String retValue = null;
        
        // By default, no special handling
        String parts[] = value.split(" ");
        StringBuffer sb = new StringBuffer();
        
        if(parts.length > 1) {
            for(String s : parts) {
                // Skip over blanks and ignore terms that start with a '*'

                if(s.length() > 0) {
                    sb.append("+");
                    sb.append(s);
                    sb.append(" ");
                }
            }
        } else {
            
            int starIdx = value.indexOf("*");

            if(starIdx < 0 || starIdx == value.lastIndexOf("*")) {
              sb.append("+");
              sb.append(value);
              sb.append(" ");
            }
        }

        if(sb.length() > 0) {
            sb.append("and basis:");
            sb.append(key);
        }
        
        try {
            retValue = URLEncoder.encode(sb.toString(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        
        return retValue;
    }
}
