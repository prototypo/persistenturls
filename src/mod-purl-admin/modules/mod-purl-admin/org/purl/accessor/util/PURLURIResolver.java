package org.purl.accessor.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.ten60.netkernel.layer1.nkf.INKFConvenienceHelper;
import org.ten60.netkernel.layer1.nkf.NKFException;

public class PURLURIResolver extends URIResolver {

    @Override
    public String getURI(INKFConvenienceHelper context) {
        String retValue = null;

        try {
            String path = NKHelper.getArgument(context, "path");
            if(path.startsWith("ffcpl:")) {
                path=path.substring(6);
            }
            retValue =  getURI((!path.startsWith("/") ? ("/"+path) : path));
        } catch(NKFException nfe) {
            nfe.printStackTrace();
        }

        return retValue;
    }

    @Override
    public String getURI(String id) {
        String retValue = null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA");
            byte [] idBytes = id.getBytes("UTF-8");
            md.update(idBytes);
            String s = new sun.misc.BASE64Encoder().encode(md.digest());
            s = URLEncoder.encode(s, "8859_1");
            retValue = "ffcpl:/storedpurls/" + s;
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return retValue;
    }


}
