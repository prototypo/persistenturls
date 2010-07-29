package org.purl.accessor;

import org.ten60.netkernel.layer1.nkf.INKFConvenienceHelper;

import com.ten60.netkernel.urii.IURRepresentation;

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

/**
 * This interface is used to provide hints to the underlying search engine.
 */
public interface SearchHelper {
    public String processKeyword(INKFConvenienceHelper context, String key, String value);
    public String[] processResults(INKFConvenienceHelper context, String key, IURRepresentation result);
}
