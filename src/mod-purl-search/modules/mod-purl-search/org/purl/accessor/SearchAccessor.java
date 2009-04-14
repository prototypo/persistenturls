package org.purl.accessor;

import com.ten60.netkernel.urii.aspect.StringAspect;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.ten60.netkernel.layer1.nkf.INKFConvenienceHelper;
import org.ten60.netkernel.layer1.nkf.INKFRequestReadOnly;
import org.ten60.netkernel.layer1.nkf.impl.NKFAccessorImpl;

public class SearchAccessor extends NKFAccessorImpl {
    
    public SearchAccessor() {
        super(SAFE_FOR_CONCURRENT_USE, INKFRequestReadOnly.RQT_SOURCE);
    }
    
    @Override
    public void processRequest(INKFConvenienceHelper context) throws Exception {

        String query = ((StringAspect)context.sourceAspect("this:param:query", StringAspect.class)).getString();
        SolrIndexManager manager = SolrIndexManager.getInstance(context);

        SolrQuery squery = new SolrQuery(query);

        // TODO: We will want to be able to parameterize the number of results coming back from a query.  Return the first 100 records for now.
        squery.setRows(100);

        QueryResponse qr = manager.getServer().query(squery);

        SolrDocumentList results = qr.getResults();

        StringBuffer sb = new StringBuffer();
        sb.append("<matches>");
        for (SolrDocument doc : results) {
            sb.append("<match>");
            String docid = "";
            for (String field : doc.getFieldNames()) {
                String value = doc.getFieldValue(field).toString();
                if ("id".equals(field)) {
                    docid = value.substring(value.indexOf(":") +1, value.length());
                }
                sb.append("<" + field + ">");
                sb.append(StringEscapeUtils.escapeXml(value));
                sb.append("</" + field + ">");
            }
            sb.append("<docid>" + docid + "</docid>");
            sb.append("</match>");
        }


        sb.append("</matches>");

        
        
        context.setResponse(context.createResponseFrom(new StringAspect(sb.toString())));
    }


}
