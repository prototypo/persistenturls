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
 * File:          $RCSfile: AccessorImpl.java,v $
 * Version:       $Name:  $ $Revision: 1.2 $
 * Last Modified: $Date: 2005/10/03 16:37:56 $
 *****************************************************************************/
package com.ten60.netkernel.urii.accessor;

import com.ten60.netkernel.urii.*;
import com.ten60.netkernel.urii.representation.*;
import com.ten60.netkernel.urii.aspect.*;
import com.ten60.netkernel.util.*;
import com.ten60.netkernel.urrequest.*;
import com.ten60.netkernel.container.*;
import com.ten60.netkernel.scheduler.*;
import com.ten60.netkernel.module.*;

/**
 * Skeletal implementation of IURAccessor for an NetKernel inbuild accessors
 * @author  tab
 */
public abstract class AccessorImpl extends SimpleRepresentationImpl implements IURAccessor
{
	/** a reference to the scheduler in the container */
	private Scheduler mScheduler;
	/** the module we are in */
	private ModuleDefinition mModule;
	/** a reference to the container we are in */
	private Container mContainer;
	
	/** Construct a new AccessorImpl
	 * @param aMeta the meta we shoudl have
	 */
	public AccessorImpl(IURAccessorMeta aMeta)
	{	super(aMeta);
	}
	
	/** Initialise the accessor */
	public void initialise(Container aContainer, ModuleDefinition aModule)
	{	mModule = aModule;
		mContainer = aContainer;
		mScheduler = (Scheduler)aContainer.getComponent(Scheduler.URI);
	}
	
	public void destroy()
	{
	}
	
	public IURAccessorMeta getAccessorMeta()
	{	return (IURAccessorMeta)getMeta();
	}
	
	/** Return our module
	 */
	protected ModuleDefinition getModule()
	{	return mModule;
	}
	
	/** Return our container
	 */
	protected Container getContainer()
	{	return mContainer;
	}
	
	/** This callback must be overriden if we make asynchronous requests so at to
	 * receive the results
	 */
	public void receiveAsyncResult(URResult aResult)
	{
	}
	
	/** default exception handling is to add exception frame and re-throw */
	public void receiveAsyncException(URResult aResult)
	{	URRequest originalRequest = aResult.getRequest().getParent();
		NetKernelException originalException = ((NetKernelExceptionAspect)aResult.getResource().getAspect(IAspectNetKernelException.class)).getException();
		NetKernelException e = new NetKernelException("Received exception","in accessor ["+getClass().getName()+"]", originalRequest.getURI().toString());
		e.addCause(originalException);
		URResult result = new URResult(originalRequest,  NetKernelExceptionAspect.create(e));
		mScheduler.receiveAsyncException(result);
	}
	
	/** return result of our invocation
	 * @param aRequest the request we are replying to
	 * @param aResource the data we are providing
	 * @param aException true if result is exception, false if it is fine
	 */
	protected void issueResult(URRequest aRequest, IURRepresentation aResource, boolean aException)
	{	URResult result = new URResult(aRequest,aResource);
		if (aException)
		{	mScheduler.receiveAsyncException(result);
		}
		else
		{	mScheduler.receiveAsyncResult(result);
		}
	}
	
	/** issue an asychronous request
	 */
	protected void issueRequest(URRequest aRequest)
	{	aRequest.setRequestor(this);
		mScheduler.requestAsync(aRequest);
	}
	
	/** issue a synchronous request
	 */
	protected URResult issueSynchRequest(URRequest aRequest) throws NetKernelException, InterruptedException
	{	return mScheduler.requestSynch(aRequest);
	}

	/** issue a synchronous transrepresentation request
	 * @param aFrom The representation we want to transrepresent
	 * @param aToClass The class we want to transrepresent to
	 * @param aOriginalRequest The parent request that caused us to want to make this request
	 * @exception NetKernelException Thrown if the transrepresentation fails for any reason
	 * @return The transrepresented representation
	 */
	protected IURRepresentation transrepresent(IURRepresentation aFrom, URIdentifier aFromURI, Class aToClass, URRequest aOriginalRequest) throws NetKernelException
	{	URRequest request = new URRequest(aFromURI, this, aOriginalRequest.getSession(), mModule, URRequest.RQT_TRANSREPRESENT, null, aOriginalRequest, aToClass);
		request.addArg(URRequest.URI_SYSTEM,  aFrom);
		URResult result = mScheduler.requestSynch(request);
		return result.getResource();
	}
}