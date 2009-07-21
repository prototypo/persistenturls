package org.purl.accessor;


import com.ten60.netkernel.urii.IURRepresentation;
import com.ten60.netkernel.urii.aspect.StringAspect;
import org.ten60.netkernel.layer1.nkf.*;
import org.ten60.netkernel.layer1.nkf.impl.NKFAccessorImpl;
import org.ten60.netkernel.xml.representation.IAspectXDA;
import org.ten60.netkernel.xml.xda.IXDAReadOnlyIterator;

import java.util.Properties;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

public class IndexAccessor extends NKFAccessorImpl {


    public IndexAccessor() {
        super(SAFE_FOR_CONCURRENT_USE, INKFRequestReadOnly.RQT_SOURCE);
    }

    @Override
    public void processRequest(INKFConvenienceHelper context) throws Exception {

        if (context.getThisRequest().argumentExists("initialize")) {
            initializeIndex(context);
        } else {
            index(context);
        }

    }

    private void index(INKFConvenienceHelper context) throws Exception {
        SolrIndexManager manager = SolrIndexManager.getInstance(context);
        String type = context.getThisRequest().getArgument("importType");
        String response = "";

        if (!"delta-import".equals(type) && !"full-import".equals(type)) {
            throw new NKFException("Invalid import type");
        }

        if (manager.isIndexing()) {
            response = "<already-indexing/>";
        } else {
            updateTimestamp(context);
            manager.startIndexing(type);
            response = "<indexing/>";
        }


        INKFResponse resp = context.createResponseFrom(new StringAspect(response));
        resp.setMimeType("text/xml");
        context.setResponse(resp);
    }

    private void initializeIndex(INKFConvenienceHelper context) throws Exception {
        // Simply copy conf/* to ffcpl:/solr/conf
        IAspectXDA dbfiles = (IAspectXDA) context.sourceAspect("ffcpl:/solr/solrfiles.xml", IAspectXDA.class);
        IXDAReadOnlyIterator filesItor = dbfiles.getXDA().readOnlyIterator("/solrfiles/file");

        while (filesItor.hasNext()) {
            filesItor.next();
            String file = filesItor.getText(".", true);
            copyFiles(context, file);
        }


        IAspectXDA dbconfig = (IAspectXDA)context.sourceAspect("ffcpl:/etc/ConfigPURLSDB.xml", IAspectXDA.class);
	    String dbtype = dbconfig.getXDA().getText("/config/type", true);

        String username =  ((StringAspect)context.sourceAspect("this:param:username", StringAspect.class)).getString();
        String password =  ((StringAspect)context.sourceAspect("this:param:password", StringAspect.class)).getString();
        String jdbc =  ((StringAspect)context.sourceAspect("this:param:jdbc", StringAspect.class)).getString();

        copySolrDataConfig(context, dbtype, username, password, jdbc);
    }

    private void copySolrDataConfig(INKFConvenienceHelper context, String db, String username, String password, String jdbc) throws Exception  {
         IURRepresentation copyTemplate = context.source("ffcpl:/db/" + db + "/solr-data-config.xml");

        StringBuffer sb = new StringBuffer("<sed>");
        sb.append("<pattern><regex>@@JDBC-URL@@</regex><replace>");
                sb.append(jdbc);
                sb.append("</replace></pattern>");
        sb.append("<pattern><regex>@@USERNAME@@</regex><replace>");
                sb.append(username);
                sb.append("</replace></pattern>");
        sb.append("<pattern><regex>@@PASSWORD@@</regex><replace>");
                sb.append(password);
                sb.append("</replace></pattern>");

        sb.append("</sed>");
        INKFRequest req = context.createSubRequest("active:sed");
        req.addArgument("operand", copyTemplate);
        req.addArgument("operator", new StringAspect(sb.toString()));
        IURRepresentation res = context.issueSubRequest(req);

        context.sinkAspect("ffcpl:/solr/conf/data-config.xml", context.transrept(res, IAspectXDA.class));


    }

    private void updateTimestamp(INKFConvenienceHelper context) throws Exception {
        if (context.exists("ffcpl:/solr/conf/db-timestamp.properties")) {
            StringAspect ts = (StringAspect)context.sourceAspect("ffcpl:/solr/conf/db-timestamp.properties", StringAspect.class);
            context.sinkAspect("ffcpl:/solr/conf/dataimport.properties", ts);
        }

        StringAspect timestamp = (StringAspect)context.sourceAspect("active:purl-storage-db-timestamp", StringAspect.class);
        Properties p = new Properties();
        p.put("last_index_time", timestamp.getString());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        p.store(out, "last database timestamp");
        context.sinkAspect("ffcpl:/solr/conf/db-timestamp.properties", new StringAspect(out.toString()));

    }

    private void copyFiles(INKFConvenienceHelper context, String file) throws Exception {
        StringBuffer sb = new StringBuffer("<sed>");
        sb.append("<pattern><regex>src-file</regex><replace>");
        sb.append(file);
        sb.append("</replace></pattern></sed>");

        IURRepresentation copyTemplate = context.source("ffcpl:/solr/copyFileTemplate.idoc");
        INKFRequest req = context.createSubRequest("active:sed");
        req.addArgument("operand", copyTemplate);
        req.addArgument("operator", new StringAspect(sb.toString()));
        IURRepresentation res = context.issueSubRequest(req);

        req = context.createSubRequest("active:dpml");
        req.addArgument("operand", res);
        res = context.issueSubRequest(req);
    }


}
