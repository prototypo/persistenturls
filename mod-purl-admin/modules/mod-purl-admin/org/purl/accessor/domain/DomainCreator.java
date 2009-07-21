package org.purl.accessor.domain;

import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import org.ten60.netkernel.layer1.nkf.INKFConvenienceHelper;
import org.ten60.netkernel.layer1.nkf.NKFException;
import org.ten60.netkernel.layer1.representation.IAspectNVP;
import org.purl.accessor.util.*;
import org.purl.accessor.user.UserHelper;
import org.purl.accessor.util.PURLException;
import org.purl.accessor.ResourceCreator;
import org.purl.accessor.ResourceStorage;

import com.ten60.netkernel.urii.IURAspect;
import com.ten60.netkernel.urii.aspect.StringAspect;

public class DomainCreator implements ResourceCreator {

    private URIResolver domainResolver;
    private URIResolver userResolver;
    private URIResolver groupResolver;

    public DomainCreator(URIResolver domainResolver, URIResolver userResolver, URIResolver groupResolver, ResourceStorage userStorage) {
        this.domainResolver = domainResolver;
        this.userResolver = userResolver;
        this.groupResolver = groupResolver;
    }

    public IURAspect createResource(INKFConvenienceHelper context, IAspectNVP params) throws NKFException {

        String currentUser = NKHelper.getUser(context);
        String maintainers = params.getValue("maintainers");
        String writers = params.getValue("writers");

        StringTokenizer st = new StringTokenizer(maintainers, ", ");
        while(st.hasMoreTokens()) {
            String next = st.nextToken();
            if(!UserHelper.isValidUser(context, userResolver.getURI(next)) &&
               !UserHelper.isValidGroup(context, groupResolver.getURI(next)))
            {                
                throw new PURLException("User or group " + next + " does not exist", 400);
            }
        }

        st = new StringTokenizer(writers, ", ");
        while(st.hasMoreTokens()) {
            String next = st.nextToken();
            if(!UserHelper.isValidUser(context, userResolver.getURI(next))&&
               !UserHelper.isValidGroup(context, groupResolver.getURI(next))) 
            {                                
                throw new PURLException("User or group " + next + " does not exist", 400);
            }
        }
        
        //TODO: Migrate to use Domain Aspect
        
        String domain = domainResolver.getDisplayName(domainResolver.getURI(context));

        StringBuffer sb = new StringBuffer("<domain>");
        sb.append("<public>");
        sb.append(params.getValue("public"));
        sb.append("</public>");
        sb.append("<id>");
        sb.append(domain);
        sb.append("</id>");
        sb.append("<name>");
        sb.append(DataHelper.cleanseInput(params.getValue("name")));
        sb.append("</name>");
        sb.append("<maintainers>");
        
        st = new StringTokenizer(maintainers, ",");

        Set<String> maintainerList = new HashSet<String>();
        
        while(st.hasMoreElements()) {
            String maintainer = st.nextToken().trim();
            
            // Avoid duplicates                
            if(maintainerList.contains(maintainer)) {
                continue; 
            }

            if(UserHelper.isValidUser(context, userResolver.getURI(maintainer))) {
                sb.append("<uid>");
                sb.append(maintainer.trim());
                sb.append("</uid>");
            } else {
                sb.append("<gid>");
                sb.append(maintainer.trim());
                sb.append("</gid>");                    
            }
            
            maintainerList.add(maintainer);
        }
        
        if(!maintainerList.contains(currentUser)) {
            sb.append("<uid>");
            sb.append(currentUser);
            sb.append("</uid>");
        }
        
        sb.append("</maintainers>");
        
        maintainerList.clear();
        
        sb.append("<writers>");
        st = new StringTokenizer(writers, ",");
        
        while(st.hasMoreElements()) {
            String maintainer = st.nextToken().trim();
            
            // Avoid duplicates                
            if(maintainerList.contains(maintainer)) {
                continue; 
            }
            
            if(UserHelper.isValidUser(context, userResolver.getURI(maintainer))) {
                sb.append("<uid>");
                sb.append(maintainer.trim());
                sb.append("</uid>");
            } else {
                sb.append("<gid>");
                sb.append(maintainer.trim());
                sb.append("</gid>");                    
            }
            
            maintainerList.add(maintainer);
        }
        
        sb.append("</writers>");
        sb.append("</domain>");
        return new StringAspect(sb.toString());
    }
}