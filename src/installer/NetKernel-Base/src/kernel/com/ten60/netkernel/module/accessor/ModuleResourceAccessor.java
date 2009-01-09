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
 * File:          $RCSfile: ModuleResourceAccessor.java,v $
 * Version:       $Name:  $ $Revision: 1.8 $
 * Last Modified: $Date: 2005/10/03 16:38:56 $
 *****************************************************************************/
package com.ten60.netkernel.module.accessor;
import com.ten60.netkernel.urii.*;
import com.ten60.netkernel.urii.accessor.*;
import com.ten60.netkernel.urii.aspect.*;
import com.ten60.netkernel.urii.meta.*;
import com.ten60.netkernel.urii.representation.MonoRepresentationImpl;
import com.ten60.netkernel.urrequest.*;
import com.ten60.netkernel.util.*;
import java.net.*;
import java.io.*;
import java.util.*;
import java.util.regex.*;
/**
 * Accessor used for accessing module resources on the ffcpl: scheme
 * @author  tab
 */
public class ModuleResourceAccessor extends AccessorImpl
{	/** meta for all module resource accessors */
	private static IURAccessorMeta sMeta = new ModuleResourceAccessorMeta();
	/** list of regex and expiry periods */
	private PairList mExpiries;
	/** default resource expiry period as set from system configuration */
	private static long sDefaultResourceExpiry = 0;
	/** unexpanded modules are assumed to unchanging and their expiry period is set to this value */
	private static final long STATIC_OFFSET = 1000*60*60*24;
	
	/** constructs a new ModuleResourceAccessor
	 */
	public ModuleResourceAccessor()
	{	super(sMeta);
	}
	/** handle all request types
	 */
	public void requestAsync(URRequest aRequest)
	{	switch (aRequest.getType())
		{	case URRequest.RQT_SOURCE:
				source(aRequest);
				break;
			case URRequest.RQT_SINK:
				sink(aRequest);
				break;
			case URRequest.RQT_EXISTS:
				exists(aRequest);
				break;
			case URRequest.RQT_DELETE:
				delete(aRequest);
				break;
		}
	}
	
	/** updates the default resource expiry period */
	public static void setDefaultResourceExpiry(long aPeriod)
	{	sDefaultResourceExpiry = aPeriod;
	}
	/** sets the resource expiry map for this accessor */
	public void setResourceExpiries(PairList aResourceExpiries)
	{	mExpiries = aResourceExpiries;
	}
	
	private void source(URRequest aRequest)
	{	try
		{	String path = URI.create(aRequest.getURI().toString()).getSchemeSpecificPart();
			long expiryOffset;
			if (getModule().isExpanded())
			{	expiryOffset = getExpiryOffset(aRequest);
			}
			else
			{	expiryOffset = STATIC_OFFSET;
			}
			IURRepresentation representation = new ModuleResourceRepresentation(path, getModule(),getContainer(),expiryOffset);
			issueResult(aRequest,representation,false);
		}
		catch (IOException e)
		{	String resourceName = aRequest.getURI().toString();
			IURRepresentation representation = NetKernelExceptionAspect.create(new NetKernelException("Error sourcing resource","resource "+resourceName+" not found in module "+getModule().getURI().toString(),resourceName));
			issueResult(aRequest,representation,true);
		}
	}
	
	private void sink(URRequest aRequest)
	{
		try
		{	// validate that our caller has been invoked via us
			boolean auth=false;
			URRequest parent = aRequest.getParent();
			if (parent!=null)
			{	if (parent.getContext()==getModule())
				{	auth=true;
				}
				else
				{	for (Iterator i = parent.getSuperStack().iterator(); i.hasNext(); )
					{	IRequestorContext md = (IRequestorContext)i.next();
						if (md==getModule())
						{	auth=true;
							break;
						}
					}
				}
			}
			if (!auth) throw new Exception("External sink denied");
			
			IURRepresentation representation = aRequest.getArg(URRequest.URI_SYSTEM);
			IAspectBinaryStream data;
			if (representation.hasAspect(IAspectBinaryStream.class))
			{	data = (IAspectBinaryStream)representation.getAspect(IAspectBinaryStream.class);
			}
			else
			{	data = (IAspectBinaryStream)transrepresent(representation,URIdentifier.getUnique("literal:sink"),IAspectBinaryStream.class,aRequest).getAspect((IAspectBinaryStream.class));
			}
			
			String path = URI.create(aRequest.getURI().toString()).getSchemeSpecificPart();
			File f=new File(getModule().getScratchDir(),path);
			f.getParentFile().mkdirs();
			FileOutputStream fos = new FileOutputStream(f);
			try
			{	data.write(fos);
				fos.flush();
			}
			finally
			{	fos.close();
			}
			issueResult(aRequest,VoidAspect.create(),false);
		}
		catch (Exception e)
		{	NetKernelException e2 = new NetKernelException("Error sinking resource", null,aRequest.getURI().toString());
			e2.addCause(e);
			IURRepresentation representation = NetKernelExceptionAspect.create(e2);
			issueResult(aRequest,representation,true);
		}
	}
	
	private void exists(URRequest aRequest)
	{	String path = URI.create(aRequest.getURI().toString()).getSchemeSpecificPart();
		IURRepresentation rep = new ModuleResourceExpiryRepresentation(path,getModule(),getExpiryOffset(aRequest));
		issueResult(aRequest,rep,false);
	}
	
	private void delete(URRequest aRequest)
	{	String path = URI.create(aRequest.getURI().toString()).getSchemeSpecificPart();
		URL url = getModule().getResource(path);
		boolean deleted = false;
		if (url!=null && url.getProtocol().equals("file") && url.toString() .startsWith(getModule().getScratchDirURI()))
		{	try
			{	File f = new File(new URI(url.toString()));
				deleted = f.delete();
			}
			catch (Exception e)
			{
			}
		}
		IURMeta meta = new MetaImpl(BooleanAspect.MIME_TYPE, Long.MAX_VALUE, 2);
		IURAspect aspect = new BooleanAspect(url!=null);
		IURRepresentation rep = new MonoRepresentationImpl(meta,aspect);
		issueResult(aRequest,rep,false);
	}
	
	private long getExpiryOffset(URRequest aRequest)
	{	long result = sDefaultResourceExpiry;
		String uriString = aRequest.getURI().toString();
		int size = mExpiries.size();
		for (int i=0; i<size; i++)
		{	Matcher m = (Matcher)mExpiries.getValue1(i);
			boolean matches;
			synchronized(m)
			{	m.reset(uriString);
				matches = m.matches();
			}
			if (matches)
			{	result = ((Long)mExpiries.getValue2(i)).longValue();
				break;
			}
		}
		return result;
	}
	
}
