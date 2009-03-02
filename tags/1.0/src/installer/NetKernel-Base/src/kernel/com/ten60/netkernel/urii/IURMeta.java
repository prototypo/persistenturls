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
 * File:          $RCSfile: IURMeta.java,v $
 * Version:       $Name:  $ $Revision: 1.2 $
 * Last Modified: $Date: 2005/11/03 12:54:24 $
 *****************************************************************************/
package com.ten60.netkernel.urii;

/**
 * Metadata about an IURRepresentation
 * @author  tab
 */
public interface IURMeta
{
	/** Return the MIME (Multipurpose Internet Mail Extensions) type of the resource this representation represents */
	String getMimeType();
	
	/** Return a positive integer number of millseconds from the 1970 datum at which point the representation will
	 * need to be checked to see if it is expired- if it is before that time it can be assumed the representation _is_
	 * valid without need to call isExpired()
	 */
	long getPessimisticExpiryTime();
	
	/** Return true if the resource is considered expired and an IURAccessor should source a new representation */
	boolean isExpired();
	
	/** Return an abitrary integer cost quantifier for regenerating this representation- see typical accessors for costs
	 * they assign. This value helps caches decide the worth of data. The value accumulates in highly derived
	 * resources
	 */
	int getCreationCost();
	
	/** Return an abitrary integer cost quantifier for using this representation- this value is only significant
	 * for streaming resources
	 */
	int getUsageCost();
	
	/** Return true if the representation is considered to be of transient use within part of a larger operation, inputs to
	 * calculations and final results would not be intermediate. */
	boolean isIntermediate();
	/** Return the depth of sensitivity on the calling context (super stack). 0 means no sensitivity
	 */
	int getContextSensitivity();
}