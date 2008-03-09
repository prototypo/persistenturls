/******************************************************************************
 * (c) Copyright 2002,2003, 1060 Research Ltd
 *
 * This Software is licensed to You, the licensee, for use under the terms of
 * the 1060 Public License v1.0. Please read and agree to the 1060 Public
 * License v1.0 [www.1060research.com/license] before using or redistributing
 * this software.
 *
 * In summary the 1060 Public license has the following conditions.
 * A. You may use the Software free of charge provided you agree to the terms
 * laid out in the 1060 Public License v1.0
 * B. You are only permitted to use the Software with components or applications
 * that provide you with OSI Certified Open Source Code [www.opensource.org], or
 * for which licensing has been approved by 1060 Research Limited.
 * You may write your own software for execution by this Software provided any
 * distribution of your software with this Software complies with terms set out
 * in section 2 of the 1060 Public License v1.0
 * C. You may redistribute the Software provided you comply with the terms of
 * the 1060 Public License v1.0 and that no warranty is implied or given.
 * D. If you find you are unable to comply with this license you may seek to
 * obtain an alternative license from 1060 Research Limited by contacting
 * license@1060research.com or by visiting www.1060research.com
 *
 * NO WARRANTY:  THIS SOFTWARE IS NOT COVERED BY ANY WARRANTY. SEE 1060 PUBLIC
 * LICENSE V1.0 FOR DETAILS
 *
 * THIS COPYRIGHT NOTICE IS *NOT* THE 1060 PUBLIC LICENSE v1.0. PLEASE READ
 * THE DISTRIBUTED 1060_Public_License.txt OR www.1060research.com/license
 *
 * File:          $RCSfile: IURRepresentation.java,v $
 * Version:       $Name:  $ $Revision: 1.1 $
 * Last Modified: $Date: 2004/06/18 10:15:23 $
 *****************************************************************************/
package com.ten60.netkernel.urii;
import java.util.Collection;
/**
 * An IURRepresentationInterface is an object that acts as a representation for data pertaining to a URI. A representation is fundamentally
 * immutable from its API. It exhibits a IURMeta which gives meta data such as typing and validity. It also
 * exhibits a set (usually with one value) of other application interfaces. This interfaces are dependent upon
 * what the representation is a representation for.
 * @author  tab
 */
public interface IURRepresentation
{
	/** Return the meta for this representation
	 * @return the meta for this representation
	 */	
	IURMeta getMeta();
	
	/** Return true if this representation can return an aspect which implements the given interface
	 * @param aAspectClass a IURAspect class object for an aspect to test for
	 * @return true if any of the aspects which this representation holds implement the given interface
	 */
	boolean hasAspect(Class aAspectClass);
	
	/** Return the first aspect which implements the given interface class
	 * @return the aspect, null if none is found
	 */
	IURAspect getAspect(Class aAspectClass);
	
	/** Return a collection of all unique aspects that this representation exhibits */
	Collection getAspects();
}