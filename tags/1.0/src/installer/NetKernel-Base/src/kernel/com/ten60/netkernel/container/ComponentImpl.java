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
 * File:          $RCSfile: ComponentImpl.java,v $
 * Version:       $Name:  $ $Revision: 1.4 $
 * Last Modified: $Date: 2004/09/28 12:08:58 $
 *****************************************************************************/
package com.ten60.netkernel.container;

//import com.ten60.netkernel.container.*;
import com.ten60.netkernel.urii.*;
import com.ten60.netkernel.urii.meta.MetaImpl;
import com.ten60.netkernel.urii.representation.SimpleRepresentationImpl;
import com.ten60.netkernel.util.*;

import java.io.*;

/**
 * Abstract base class of all NetKernel System Components
 * @author  tab
 */
public abstract class ComponentImpl extends SimpleRepresentationImpl implements IComponent
{
	/** our URI */
	private URIdentifier mURI;
	
	/** Constructs a new ComponentImpl
	 * @param aURI the URI we should have
	 */
	public ComponentImpl(URIdentifier aURI)
	{	this(aURI, false);
	}
	/** Constructs a new ComponentImpl
	 * @param aURI the URI we should have
	 * @param aStatic the Components state will never change during the containers lifetime
	 */
	public ComponentImpl(URIdentifier aURI, boolean aStatic)
	{	super(new MetaImpl("application/vnd.netkernel-component",aStatic?Long.MAX_VALUE:0,0)
		{		public boolean isContextSensitive()
				{	return false;
				}
		});
		mURI = aURI;
	}
	/** returns our URI
	 *@return our URI
	 */
	public URIdentifier getURI()
	{	return mURI;
	}
	
	/** start method implemented with null body
	 */
	public void start(Container aContainer) throws NetKernelException
	{	
	}
	
	/** stop method implemented with null body
	 */
	public void stop() throws NetKernelException
	{
	}
	
	/** doPeriodicHouseKeeping method implemented with null body
	 */
	public void doPeriodicHouseKeeping()
	{
	}
	
	/** write method implemented with null body
	 */
	public void write(OutputStream aStream) throws IOException
	{
	}
	
	/* Get encoding */
	public String getEncoding()
	{ return "UTF-8";
	}
}