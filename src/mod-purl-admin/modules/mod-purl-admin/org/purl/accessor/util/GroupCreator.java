package org.purl.accessor.util;

import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import org.ten60.netkernel.layer1.nkf.INKFConvenienceHelper;
import org.ten60.netkernel.layer1.nkf.NKFException;
import org.ten60.netkernel.layer1.representation.IAspectNVP;

import com.ten60.netkernel.urii.IURAspect;
import com.ten60.netkernel.urii.aspect.StringAspect;

public class GroupCreator implements ResourceCreator {

    private URIResolver userResolver;
    private URIResolver groupResolver;

    public GroupCreator(URIResolver userResolver, URIResolver groupResolver) {
        this.userResolver = userResolver;
        this.groupResolver = groupResolver;
    }

    public IURAspect createResource(INKFConvenienceHelper context, IAspectNVP params) throws NKFException {

        String currentUser = params.getValue("currentuser");

        if(currentUser == null) {
        	currentUser = NKHelper.getUser(context);
        }
        
        String maintainers = params.getValue("maintainers");
        String members = params.getValue("members");

        StringTokenizer st = new StringTokenizer(maintainers, "\n, ");
        while(st.hasMoreTokens()) {
            String next = st.nextToken();
            if(!UserHelper.isValidUser(context, userResolver.getURI(next)) &&
               !GroupHelper.isValidGroup(context, groupResolver.getURI(next))) 
            {
                throw new PURLException(next + " does not exist or is not an approved user.", 400);
            }
        }

        st = new StringTokenizer(members, "\n, ");
        while(st.hasMoreTokens()) {
            String next = st.nextToken();
            if(!UserHelper.isValidUser(context, userResolver.getURI(next)) &&
               !GroupHelper.isValidGroup(context, groupResolver.getURI(next))) 
            {                
                throw new PURLException(next + " does not exist or is not an approved user.", 400);
            }
        }

        String groupId = groupResolver.getURI(context);
        StringBuffer sb = new StringBuffer("<group>");
        sb.append("<id>");
        sb.append(groupResolver.getDisplayName(groupId));
        sb.append("</id>");
        sb.append("<name>");
        sb.append(DataHelper.cleanseInput(params.getValue("name")));
        sb.append("</name>");
        sb.append("<maintainers>");
        
        Set<String> maintainerList = new HashSet<String>();
        
        st = new StringTokenizer(maintainers, ", ");
        while(st.hasMoreElements()) {
            String maintainer = st.nextToken().trim();
            
            // Avoid duplicates                
            if(maintainerList.contains(maintainer)) {
                continue; 
            }
            
            if(UserHelper.isValidUser(context, userResolver.getURI(maintainer))) {
                sb.append("<uid>");
                sb.append(maintainer);
                sb.append("</uid>");
            } else {
                sb.append("<gid>");
                sb.append(maintainer);
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
        
        sb.append("<members>");
        st = new StringTokenizer(members, "\n, ");
        while(st.hasMoreElements()) {
            String member = st.nextToken().trim();
            
            // Avoid duplicates                
            if(maintainerList.contains(member)) {
                continue; 
            }
            
            if(UserHelper.isValidUser(context, userResolver.getURI(member))) {
                sb.append("<uid>");
                sb.append(member);
                sb.append("</uid>");
            } else {
                sb.append("<gid>");
                sb.append(member);
                sb.append("</gid>");                    
            }
            
            maintainerList.add(member);
        }
        
        sb.append("</members>");
        sb.append("<comments>");
        sb.append(DataHelper.cleanseInput(params.getValue("comments")));
        sb.append("</comments>");
        sb.append("</group>");
        
        return new StringAspect(sb.toString());
    }
}