package org.purl.accessor;

import com.ten60.netkernel.urii.aspect.IAspectReadableBinaryStream;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.core.*;
import org.apache.solr.schema.IndexSchema;
import org.ten60.netkernel.layer1.nkf.INKFConvenienceHelper;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class SolrIndexManager {

    private static SolrIndexManager instance;

    public static SolrIndexManager getInstance(INKFConvenienceHelper context) throws Exception {
        if (instance == null) {
            instance = new SolrIndexManager(context);

        }
        return instance;
    }

    private SolrIndexManager(INKFConvenienceHelper context) throws Exception {
        Logger.getLogger("org.apache.solr").setLevel(Level.WARNING);
        String coreName = "";

        String dataDirectory = context.getKernelHelper().getOwningModule().getScratchDir() + "solr/";

        SolrConfig solrConfig = new SolrConfig(dataDirectory, "purl",
                ((IAspectReadableBinaryStream) context.source("ffcpl:/solr/conf/solrconfig.xml",
                        IAspectReadableBinaryStream.class)).getInputStream());
        IndexSchema indexSchema = new IndexSchema(solrConfig, "schema.xml", null);

        CoreContainer container = new CoreContainer(new SolrResourceLoader(dataDirectory));
        CoreDescriptor dcore = new CoreDescriptor(container, coreName, dataDirectory);
        dcore.setConfigName(solrConfig.getResourceName());
        dcore.setSchemaName(indexSchema.getResourceName());
        SolrCore core = new SolrCore(null, dataDirectory, solrConfig, indexSchema, dcore);
        container.register(coreName, core, false);
        server = new EmbeddedSolrServer(container, "");
    }

    private SolrServer server;


    public SolrServer getServer() throws Exception {
        return server;
    }

    public boolean isIndexing() throws Exception {
        ModifiableSolrParams p = new ModifiableSolrParams();
        p.set("qt", "/dataimport");
        p.set("command", "status");
        QueryResponse response = server.query(p);

        return "busy".equals(response.getResponse().get("status"));
    }

    public boolean startIndexing(String type) throws Exception {
        ModifiableSolrParams params = new ModifiableSolrParams();
        params.set("qt", "/dataimport");
        params.set("command", type);
        params.set("commit", "true");
        QueryResponse response = server.query(params);
        return true;
    }


}
