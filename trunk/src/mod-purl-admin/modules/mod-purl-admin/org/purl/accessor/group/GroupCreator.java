package org.purl.accessor.group;

import com.ten60.netkernel.urii.IURAspect;
import com.ten60.netkernel.urii.aspect.StringAspect;
import org.ten60.netkernel.layer1.nkf.INKFConvenienceHelper;
import org.ten60.netkernel.layer1.nkf.NKFException;
import org.ten60.netkernel.layer1.representation.IAspectNVP;
import org.purl.accessor.util.*;
import org.purl.accessor.user.UserHelper;
import org.purl.accessor.util.PURLException;
import org.purl.accessor.ResourceCreator;

import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

public class GroupCreator implements ResourceCreator {

    private URIResolver userResolver;
    private URIResolver groupResolver;

    public GroupCreator(URIResolver userResolver, URIResolver groupResolver) {
        this.userResolver = userResolver;
        this.groupResolver = groupResolver;
    }


    public IURAspect createResource(INKFConvenienceHelper context, IAspectNVP params) throws NKFException {


        String maintainerXML = generateUserListXML(context, "maintainers", params);

        String memberXML = generateUserListXML(context, "members", params);

        String groupId = groupResolver.getURI(context);
        StringBuffer sb = new StringBuffer("<group>");
        sb.append("<id>");
        sb.append(groupResolver.getDisplayName(groupId));
        sb.append("</id>");
        sb.append("<name>");
        sb.append(DataHelper.cleanseInput(params.getValue("name")));
        sb.append("</name>");
        sb.append(maintainerXML);
        sb.append(memberXML);
        sb.append("<comments>");
        String comments = DataHelper.cleanseInput(params.getValue("comments"), 300);
        sb.append(comments);
        sb.append("</comments>");
        sb.append("</group>");

        return new StringAspect(sb.toString());
    }


    public String generateUserListXML(INKFConvenienceHelper context, String key, IAspectNVP params) throws NKFException {



        StringBuffer sb = new StringBuffer();
        String maintainers = params.getValue(key);
        sb.append("<" + key + ">");
        Set<String> maintainerList = new HashSet<String>();

        if (maintainers != null) {

            StringTokenizer st = new StringTokenizer(maintainers, "\n, ");
            while (st.hasMoreTokens()) {
                String next = st.nextToken();
                if (!UserHelper.isValidUser(context, userResolver.getURI(next)) &&
                        !GroupHelper.isValidGroup(context, groupResolver.getURI(next))) {
                    throw new PURLException(next + " does not exist or is not an approved user.", 400);
                }
            }
            st = new StringTokenizer(maintainers, ", ");
            while (st.hasMoreElements()) {
                String maintainer = st.nextToken().trim();

                // Avoid duplicates
                if (maintainerList.contains(maintainer)) {
                    continue;
                }

                if (UserHelper.isValidUser(context, userResolver.getURI(maintainer))) {
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


        }
        if ("maintainers".equals(key)) {
            String currentUser = params.getValue("currentuser");

            if (currentUser == null) {
                currentUser = NKHelper.getUser(context);
            }
            if (!maintainerList.contains(currentUser)) {
                sb.append("<uid>");
                sb.append(currentUser);
                sb.append("</uid>");
            }

        }
        sb.append("</" + key + ">");
        return sb.toString();
    }

}