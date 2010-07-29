package org.purl.accessor.command;

/**
 *=========================================================================
 *
 *  Copyright (C) 2007 OCLC (http://oclc.org)
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *=========================================================================
 *
 */

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import org.purl.accessor.util.NKHelper;
import org.purl.accessor.SearchHelper;
import org.purl.accessor.util.URIResolver;
import org.purl.accessor.AccessController;
import org.purl.accessor.ResourceFilter;
import org.purl.accessor.*;
import org.purl.accessor.command.PURLCommand;
import org.ten60.netkernel.layer1.nkf.INKFAsyncRequestHandle;
import org.ten60.netkernel.layer1.nkf.INKFConvenienceHelper;
import org.ten60.netkernel.layer1.nkf.INKFRequest;
import org.ten60.netkernel.layer1.nkf.INKFResponse;
import org.ten60.netkernel.layer1.nkf.NKFException;
import org.ten60.netkernel.layer1.representation.IAspectNVP;

import com.ten60.netkernel.urii.IURAspect;
import com.ten60.netkernel.urii.IURRepresentation;
import com.ten60.netkernel.urii.aspect.IAspectString;
import com.ten60.netkernel.urii.aspect.StringAspect;

public class GetResourceCommand extends PURLCommand {

    private SearchHelper search;
    private ResourceFilter filter;

    public GetResourceCommand(String type, URIResolver uriResolver, AccessController accessController, ResourceStorage resStorage, SearchHelper search) {
        this(type, uriResolver, accessController, resStorage, search, null);
    }

    public GetResourceCommand(String type, URIResolver uriResolver, AccessController accessController, ResourceStorage resStorage, SearchHelper search, ResourceFilter filter) {
        super(type, uriResolver, accessController, resStorage);
        this.search = search;
        this.filter = filter;
    }

    @Override
    public INKFResponse execute(INKFConvenienceHelper context) {
        INKFResponse retValue = null;

        try {
            String path = context.getThisRequest().getArgument("path");
            IAspectNVP params = null;

            if(!isSearch(path)) {
                if(resStorage.resourceExists(context, uriResolver)) {
                    IURAspect asp = resStorage.getResource(context, uriResolver);

                    // Filter the response if we have a filter
                    if(filter!=null) {
                        asp = filter.filter(context, asp);
                    }

                    // Default response code of 200 is fine
                    IURRepresentation rep = NKHelper.setResponseCode(context, asp, 200);
                    rep = NKHelper.attachGoldenThread(context, "gt:" + path , rep);
                    retValue = context.createResponseFrom(rep);
                    retValue.setCacheable();
                    retValue.setMimeType(NKHelper.MIME_XML);
                } else {
                    IURRepresentation rep = NKHelper.setResponseCode(context, new StringAspect("No such resource: " + uriResolver.getDisplayName(path)), 404);
                    retValue = context.createResponseFrom(rep);
                    retValue.setMimeType(NKHelper.MIME_TEXT);
                }
            } else {
                params = (IAspectNVP) context.sourceAspect( "this:param:param", IAspectNVP.class);
                List<String> searchList = new ArrayList<String>();
                Iterator namesItor = params.getNames().iterator();
                INKFAsyncRequestHandle handles[] = null;
                IURRepresentation results[] = null;
                String keys[] = null;

                boolean includeTombstoned = false;

                int idx = 0;

                while(namesItor.hasNext()) {
                    // TODO: Make this more efficient
                    String key = (String) namesItor.next();

                    if(key.equals("tombstone")) {
                        includeTombstoned = Boolean.valueOf(params.getValue(key));
                        continue;
                    }

                    String value = params.getValue(key);

                    if(value.length() == 0) {
                        continue;
                    }

                    searchList.add(key);
                }

                handles = new INKFAsyncRequestHandle[searchList.size()];
                results = new IURRepresentation[searchList.size()];
                keys = new String[searchList.size()];

                Iterator<String> searchCriteriaItor = searchList.iterator();

                while(searchCriteriaItor.hasNext()) {
                    String key = searchCriteriaItor.next();
                    String value = params.getValue(key);
                    INKFRequest req = context.createSubRequest("active:purl-search");

                    String query = "(";

                    // See if the keyword values need any processing or filtering

                    StringTokenizer st = new StringTokenizer(value, ",");
                    int kwidx = 0;

                    // We build up arguments this way to encourage caching of the search results

                    while(st.hasMoreTokens()) {
                        if (kwidx > 0) {
                            query += " OR ";
                        }
                        query += search.processKeyword(context, key, st.nextToken());
                        kwidx++;


                    }
                    query += ") AND entity:" + type;
                    
                    req.addArgument("query", new StringAspect(query));
                    handles[idx] = context.issueAsyncSubRequest(req);
                    keys[idx] = key;
                    idx++;
                }

                for(int i = 0; i < idx; i++ ) {
                    try {
                        results[i] = handles[i].join();
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }

                Set<String> alreadyDoneSet = new HashSet<String>();
                StringBuffer sb = new StringBuffer("<results>");

                for(int i = 0; i < idx; i++ ) {
                    if(results[i] != null) {
                        // This assumes XML search results, if that isn't always going to be the case
                        // this might break.

                        String uris[] = search.processResults(context, keys[i], results[i]);

                        for(String uri: uris) {
                        	// Make sure we have a URI
                        	uri = this.getURIResolver().getURI(uri);

                            if(!alreadyDoneSet.contains(uri)) {
                                if(resStorage.resourceExists(context, uri)) {
                                    if(!resStorage.resourceIsTombstoned(context, uri) || includeTombstoned) {
                                        IURAspect iur = resStorage.getResource(context, uri);
                                        if(iur != null) {
                                            // Filter the response if we have a filter
                                            if(filter!=null) {
                                                iur = filter.filter(context, iur);
                                            }

                                            StringAspect sa = (StringAspect) context.transrept(iur, IAspectString.class);
                                            sb.append(sa.getString());
                                        }
                                    }
                                }
                                alreadyDoneSet.add(uri);
                            }
                        }
                    }
                }

                sb.append("</results>");

                retValue = context.createResponseFrom(new StringAspect(sb.toString()));
                retValue.setMimeType(NKHelper.MIME_XML);
            }

        } catch (NKFException e) {
            // TODO Handle
            e.printStackTrace();
        }

        return retValue;
    }

    private boolean isSearch(String path) {
        // TODO: Use regexs
        return  path.endsWith("/user/") ||
                path.endsWith("/domain/") ||
                path.endsWith("/purl/") ||
                path.endsWith("/group/");
    }
}
