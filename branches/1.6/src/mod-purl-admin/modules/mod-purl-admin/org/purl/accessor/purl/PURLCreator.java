package org.purl.accessor.purl;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
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
import org.purl.accessor.group.GroupHelper;
import org.purl.accessor.util.*;
import org.purl.accessor.user.UserHelper;
import org.purl.accessor.ResourceCreator;
import org.purl.accessor.ResourceStorage;
import org.apache.commons.lang.StringEscapeUtils;

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
        String target = purlResolver.getDisplayName(oldURI);
        if((target == null) || target.length() == 0) {
                throw new PURLException("Chain PURLs must have a target URL", 400);
            }
        if(purlStorage.resourceExists(context, oldURI)) {
            StringBuffer sb = new StringBuffer("<purl>");
            sb.append("<id>");
            sb.append(purlResolver.getDisplayName(purl));
            sb.append("</id>");
            sb.append("<type>chain</type>");
            sb.append("<target><url>");
            sb.append(StringEscapeUtils.escapeXml(purlResolver.getDisplayName(oldURI)));
            sb.append("</url></target>");

            addMaintainerList(context, sb, params.getValue("maintainers"));

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

    private IURAspect createPartialRedirectPURL(INKFConvenienceHelper context, IAspectNVP params, String type) throws NKFException, PURLException {
        IURAspect retValue = null;

        StringBuffer sb = new StringBuffer("<purl>");
        String target = params.getValue("target");
        String purlURI = purlResolver.getURI(context);
        String purl = purlResolver.getDisplayName(purlURI);

        if((target == null) || target.length() == 0) {
            throw new PURLException("Partial PURLs must have a target URL", 400);
        }

        sb.append("<id>");
        sb.append(purl);
        sb.append("</id>");
        sb.append("<type>" + type + "</type>");

        sb.append("<target><url>");
        sb.append(StringEscapeUtils.escapeXml(target));
        sb.append("</url></target>");
        
        addMaintainerList(context, sb, params.getValue("maintainers"));

        sb.append("</purl>");

        retValue = new StringAspect(sb.toString());

        return retValue;
    }

    private IURAspect createNumericPURL(INKFConvenienceHelper context, IAspectNVP params, int type) throws PURLException, NKFException, UnsupportedEncodingException {
        IURAspect retValue = null;

        StringBuffer sb = new StringBuffer("<purl>");
        String target = params.getValue("target");
        String purl = purlResolver.getDisplayName(NKHelper.getArgument(context, "path"));
        
        sb.append("<id>");
        sb.append(StringEscapeUtils.escapeXml(purl));
        sb.append("</id>");
        sb.append("<type>");
        sb.append(type);
        sb.append("</type>");

        switch(type) {
        case 301:
        case 302:
        case 307:
            if((target == null) || target.length() == 0) {
                throw new PURLException(type + " PURLs must have a target URL", 400);
            }
            sb.append("<target><url>");
            sb.append(StringEscapeUtils.escapeXml(target));
            sb.append("</url></target>");
            break;
        case 303:
            String seealsos=params.getValue("seealso");
            
            if((seealsos == null) || seealsos.length() == 0) {
                throw new PURLException(type + " PURLs must have a seealso URL", 400);
            }            

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

        addMaintainerList(context, sb, params.getValue("maintainers"));

        sb.append("</purl>");

        retValue = new StringAspect(sb.toString());


        return retValue;
    }

    public boolean checkMaintainersList(INKFConvenienceHelper context, IAspectNVP params) throws NKFException {
        String maintainers = params.getValue("maintainers");

        StringTokenizer st = new StringTokenizer(maintainers, ",\n");

        List<String> notFoundList = null;

        while(st.hasMoreTokens()) {
            String next = st.nextToken().trim();
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
    
    public IURAspect createResource(INKFConvenienceHelper context, IAspectNVP params) throws PURLException, NKFException {
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

        if(type != null) {
            if(!isNumber(type)) {
                if(type.equals("chain")) {
                    retValue = createChainedPURL(context, params);
                } else if(type.equals("clone")) {
                    retValue = createClonedPURL(context, params);
                } else if(type.startsWith("partial")) {
                    retValue = createPartialRedirectPURL(context, params, type);                
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
                } catch( PURLException pe ) {
                    throw pe;
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
    
    /**
     * Build up the maintainer list in the StringBuffer adding the currentUser if they
     * have not been explicitly stated as a maintainer.
     *
     * @param context
     * @param sb
     * @param maintainersList
     */
    private void addMaintainerList(INKFConvenienceHelper context, StringBuffer sb, String maintainersList) {
        
        if(maintainersList!=null) {
            String currentUser = NKHelper.getUser(context);
            Set<String> processedSet = new HashSet<String>();
            
            sb.append("<maintainers>");
            StringTokenizer st = new StringTokenizer(maintainersList, ",\n");
            while(st.hasMoreElements()) {
                String maintainer = st.nextToken().trim().toLowerCase();
                
                // Avoid duplicates
                if(processedSet.contains(maintainer)) {
                    continue;
                }
                
                if(UserHelper.isValidUser(context, maintainerResolvers[0].getURI(maintainer))) {
                    sb.append("<uid>");
                    sb.append(maintainer);
                    sb.append("</uid>");
                } else {
                    sb.append("<gid>");
                    sb.append(maintainer);
                    sb.append("</gid>");                    
                }
                
                processedSet.add(maintainer);
            }
            
            // Add the currentUser if he/she has not yet been specified
            // to avoid orphaned PURLs.
            
            if(!processedSet.contains(currentUser)) {
                sb.append("<uid>");
                sb.append(currentUser);
                sb.append("</uid>");
            }
            
            sb.append("</maintainers>");
        }       
    }
}