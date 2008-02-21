package org.purl.accessor.util;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.ten60.netkernel.layer1.nkf.INKFConvenienceHelper;
import org.ten60.netkernel.layer1.nkf.NKFException;
import org.ten60.netkernel.layer1.representation.IAspectNVP;
import org.ten60.netkernel.xml.representation.DOMXDAAspect;
import org.ten60.netkernel.xml.representation.IAspectXDA;
import org.ten60.netkernel.xml.xda.DOMXDA;
import org.ten60.netkernel.xml.xda.IXDA;
import org.ten60.netkernel.xml.xda.XDOIncompatibilityException;
import org.ten60.netkernel.xml.xda.XPathLocationException;

import com.ten60.netkernel.urii.IURAspect;
import com.ten60.netkernel.urii.aspect.StringAspect;

public class PURLCreator implements ResourceCreator {
    private URIResolver[] maintainerResolvers;
    private URIResolver purlResolver;
    private ResourceStorage purlStorage;

    public PURLCreator( URIResolver[] maintainerResolvers, URIResolver purlResolver, ResourceStorage purlStorage, ResourceStorage maintainerStorage) {
        this.maintainerResolvers = maintainerResolvers;
        this.purlResolver = purlResolver;
        this.purlStorage = purlStorage;
    }

    private IURAspect createChainedPURL(INKFConvenienceHelper context, IAspectNVP params) throws NKFException {
        IURAspect retValue = null;

        String purl = purlResolver.getURI(context);
        String existingPurl = params.getValue("basepurl");
        String oldURI = purlResolver.getURI(existingPurl);

        if(purlStorage.resourceExists(context, oldURI)) {
            StringBuffer sb = new StringBuffer("<purl>");
            sb.append("<id>");
            sb.append(purlResolver.getDisplayName(purl));
            sb.append("</id>");
            sb.append("<type>chain</type>");
            sb.append("<target><url>");
            sb.append(purlResolver.getDisplayName(oldURI));
            sb.append("</url></target>");

            String maintainers=params.getValue("maintainers");
            if(maintainers!=null) {
                sb.append("<maintainers>");
                StringTokenizer st = new StringTokenizer(maintainers, ",");
                while(st.hasMoreElements()) {
                    sb.append("<uid>");
                    sb.append(st.nextToken().trim());
                    sb.append("</uid>");
                }
                sb.append("</maintainers>");
            }

            sb.append("</purl>");

            retValue = new StringAspect(sb.toString());


        } else {
            throw new PURLException("Cannot chain non-existent PURL: "
                + existingPurl, 400);
        }

        return retValue;
    }

    private IURAspect createClonedPURL(INKFConvenienceHelper context, IAspectNVP params) throws NKFException {
        IURAspect retValue = null;
        String purl = NKHelper.getArgument(context, "path");
        if(purl.startsWith("ffcpl:/purl")) {
            purl = purl.substring(11);
        }
        String existingPurl = params.getValue("basepurl");
        String oldURI = purlResolver.getURI(existingPurl);

        if(purlStorage.resourceExists(context, oldURI)) {

            IURAspect iur = purlStorage.getResource(context, oldURI);
            IAspectXDA oldPurlXDAOrig = (IAspectXDA) context.transrept(iur, IAspectXDA.class);
            IXDA oldPurlXDA = oldPurlXDAOrig.getClonedXDA();
            try {
                oldPurlXDA.replaceByText("/purl/id/text()", purl);
                retValue = new DOMXDAAspect((DOMXDA)oldPurlXDA);
            } catch (XPathLocationException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (XDOIncompatibilityException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            throw new PURLException("Cannot clone non-existent PURL: "
                    + existingPurl, 400);
        }

        return retValue;
    }

    private IURAspect createPartialRedirectPURL(INKFConvenienceHelper context, IAspectNVP params) throws NKFException {
        IURAspect retValue = null;

        StringBuffer sb = new StringBuffer("<purl>");
        String target = params.getValue("target");
        String purlURI = purlResolver.getURI(context);
        String purl = purlResolver.getDisplayName(purlURI);
        
        sb.append("<id>");
        sb.append(purl);
        sb.append("</id>");
        sb.append("<type>partial</type>");

        sb.append("<target><url>");
        target = target.replaceAll("&", "&amp;");
        sb.append(target);
        sb.append("</url></target>");

        String maintainers=params.getValue("maintainers");
        if(maintainers!=null) {
            sb.append("<maintainers>");
            StringTokenizer st = new StringTokenizer(maintainers, ",");
            while(st.hasMoreElements()) {
                sb.append("<uid>");
                sb.append(st.nextToken().trim());
                sb.append("</uid>");
            }
            sb.append("</maintainers>");
        }

        sb.append("</purl>");

        retValue = new StringAspect(sb.toString());

        return retValue;
    }

    private IURAspect createNumericPURL(INKFConvenienceHelper context, IAspectNVP params, int type) throws NKFException, UnsupportedEncodingException {
        IURAspect retValue = null;

        StringBuffer sb = new StringBuffer("<purl>");
        String target = params.getValue("target");
        String purl = purlResolver.getDisplayName(NKHelper.getArgument(context, "path"));
        
        sb.append("<id>");
        sb.append(purl);
        sb.append("</id>");
        sb.append("<type>");
        sb.append(type);
        sb.append("</type>");

        switch(type) {
        case 301:
        case 302:
        case 307:
            sb.append("<target><url>");
            target = target.replaceAll("&", "&amp;");
            sb.append(target);
            sb.append("</url></target>");
            break;
        case 303:
            String seealsos=params.getValue("seealso");
            StringTokenizer st = new StringTokenizer(seealsos, " \n");
            while(st.hasMoreElements()) {
                sb.append("<seealso><url>");
                sb.append(st.nextToken());
                sb.append("</url></seealso>");
            }

            break;
        case 404:
        case 410:
            break;
        default :
            // TODO: Handle Error
            break;
        }

        String maintainers=params.getValue("maintainers");
        if(maintainers!=null) {
            sb.append("<maintainers>");
            StringTokenizer st = new StringTokenizer(maintainers, ",");
            while(st.hasMoreElements()) {
                sb.append("<uid>");
                sb.append(st.nextToken().trim());
                sb.append("</uid>");
            }
            sb.append("</maintainers>");
        }

        sb.append("</purl>");

        retValue = new StringAspect(sb.toString());


        return retValue;
    }

    public boolean checkMaintainersList(INKFConvenienceHelper context, IAspectNVP params) throws NKFException {
        String maintainers = params.getValue("maintainers");

        StringTokenizer st = new StringTokenizer(maintainers, ",\n");

        List<String> notFoundList = null;

        while(st.hasMoreTokens()) {
            String next = st.nextToken();
            boolean individualPermitted = false;

            for(URIResolver ur : maintainerResolvers) {
                String uri = ur.getURI(next);
                
                if(uri.startsWith("ffcpl:/user")) {
                    individualPermitted = UserHelper.isValidUser(context, ur.getURI(next));                    
                } else if(uri.startsWith("ffcpl:/group")) {
                    individualPermitted = GroupHelper.isValidGroup(context, ur.getURI(next));                    
                }
                
                if(individualPermitted) {
                    break;
                }
            }

            if(!individualPermitted) {
                if(notFoundList == null) {
                    notFoundList = new ArrayList<String>();
                }

                notFoundList.add(next);
            }
        }

        boolean permitted = notFoundList == null;
        if(!permitted) {
            String errorMessage = null;

            if(notFoundList.size() == 1) {
                errorMessage = "Maintainer not found: " + notFoundList.get(0);
            } else {
                StringBuffer sb = new StringBuffer("Maintainers not found: ");
                Iterator <String> nfItor = notFoundList.iterator();
                sb.append(nfItor.next());

                while(nfItor.hasNext()) {
                    sb.append(",");
                    sb.append(nfItor.next());
                }
            }
            throw new PURLException(errorMessage, 400);
        }

        return permitted;
    }
    
    public IURAspect createResource(INKFConvenienceHelper context, IAspectNVP params) throws NKFException {
        IURAspect retValue = null;

        // TODO: Validate the PURLs against the existing character restrictions

        String type = params.getValue("type");
        String target = params.getValue("target");

        if(type==null) {
            if(target!=null) {
                type = "302";
            }
        }

        if(!type.equals("clone")) {
            checkMaintainersList(context, params);
        }
        
        String purl = purlResolver.getDisplayName(NKHelper.getArgument(context, "path"));
        NKHelper.createNecessarySubdomains(context, purl);

        if(type != null) {
            if(!isNumber(type)) {
                if(type.equals("chain")) {
                    retValue = createChainedPURL(context, params);
                } else if(type.equals("clone")) {
                    retValue = createClonedPURL(context, params);
                } else if(type.equals("partial")) {
                    retValue = createPartialRedirectPURL(context, params);
                } else {
                    // TODO: Handle unexpected
                }
            } else {
                try {
                    int typeInt = Integer.valueOf(type).intValue();
                    retValue = createNumericPURL(context, params, typeInt);
                } catch(NumberFormatException nfe){
                    // TODO: Handle
                } catch (UnsupportedEncodingException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

         } else {
            throw new PURLException("Missing PURL type parameter", 400);
        }

        return retValue;
    }
    
    /**
     * Determine if the specified number is a number.
     * @param number String
     * @return boolean indicating whether it was true or not
     */
    private boolean isNumber(String number) {
        boolean retValue = true;
        int idx = 0;

        if(number != null) {
            int len = number.length();

            while(retValue && idx < len) {
                retValue = Character.isDigit(number.charAt(idx++));
            }
        }

        return retValue;
    }
}