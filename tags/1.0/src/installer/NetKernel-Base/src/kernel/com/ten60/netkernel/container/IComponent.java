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
 * File:          $RCSfile: IComponent.java,v $
 * Version:       $Name:  $ $Revision: 1.2 $
 * Last Modified: $Date: 2005/10/03 13:07:33 $
 *****************************************************************************/
package com.ten60.netkernel.container;

import com.ten60.netkernel.urii.*;
import com.ten60.netkernel.urii.aspect.IAspectBinaryStream;
import com.ten60.netkernel.util.NetKernelException;

/**
 * Interface for all NetKernel System Components, they are all IURRepresentations
 * and implements IRepresentationBinaryStream.
 * @author  tab
 */
public interface IComponent extends IURRepresentation, IAspectBinaryStream
{
	/** return our URI
	 *@return our URI
	 */
	public abstract URIdentifier getURI();
	
	/** perform some housekeeping work every house keeping period.
	 */
	public abstract void doPeriodicHouseKeeping();
	
	/** starts the component
	 *@param aContainer the container we will execute inside
	 *@exception NetKernelException thrown if we encounter any error during startup
	 */
	void start(Container aContainer) throws NetKernelException;
	/** stops the component
	 *@exception NetKernelException thrown if we encounter any error during startup
	 */
	void stop() throws NetKernelException;		
	
}