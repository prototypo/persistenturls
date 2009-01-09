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
 * File:          $RCSfile: ModuleResourceRepresentation.java,v $
 * Version:       $Name:  $ $Revision: 1.12 $
 * Last Modified: $Date: 2007/12/10 13:48:52 $
 *****************************************************************************/
package com.ten60.netkernel.module.accessor;

import com.ten60.netkernel.urii.*;
import com.ten60.netkernel.urii.representation.*;
import com.ten60.netkernel.urii.aspect.*;
import com.ten60.netkernel.module.*;
import com.ten60.netkernel.container.*;
import com.ten60.netkernel.util.Utils;
import java.io.*;
import java.net.*;

/**
 * The representation returned by the module resource accessor. It is based on SimpleRepresentationImpl
 * which means the aspect and representation are rolled into one object.
 * @author  tab
 */
public final class ModuleResourceRepresentation extends SimpleRepresentationImpl implements IAspectReadableBinaryStream
{
	private String mPath;
	private URLConnection mConnection;
	private int mCost;
	private long mTimestamp;
	private String mMime;
	private long mLastChecked;
	private long mExpiryOffset;
	private String mEncoding;
	private URL mURL;
	private File mFile;
	
	/** Creates a new instance of ModuleResourceRepresentation */
	public ModuleResourceRepresentation(String aPath, ModuleDefinition aModule, Container aContainer, long aExpiryOffset) throws IOException
	{	super(null);
		mMeta=createMeta();
		mPath=aPath;
		mExpiryOffset = aExpiryOffset;
		
		mURL = aModule.getResource(mPath);
		if (mURL==null)
		{	throw new IOException();
		}
		else
		{	if (mURL.getProtocol().equals("file"))
			{	mFile = new File(URI.create(mURL.toString()));
				mTimestamp = mFile.lastModified();
			}
			else
			{	String url=mURL.toString();
				String unescaped = Utils.decode(url);
				mURL=new URL(unescaped);
			}
		}
		URLConnection c=ensureOpenConnection();
		
		mMime = c.getContentType();
		mCost = 4+(c.getContentLength()>>10); // number of Kb
		if(!mURL.getProtocol().equals("jar"))
		{	mEncoding = c.getContentEncoding();
		}
	}
	/** return the length of the resource */
	public int getContentLength()
	{	 try
		 {	return ensureOpenConnection().getContentLength();
		 } catch (IOException e)
		 {	return -1;
		 }
	}
	/** write the resource to the given output stream */
	public  void write(OutputStream aStream) throws IOException
	{	InputStream is;
		synchronized(this)
		{	is = ensureOpenConnection().getInputStream();
			mConnection=null;
		}
		Utils.pipe(is,aStream);
		
	}
	/** get an input stream for the  resource */
	public synchronized InputStream getInputStream() throws IOException
	{	InputStream result =ensureOpenConnection().getInputStream();
		mConnection=null;
		return result;
	}
	private URLConnection ensureOpenConnection() throws IOException
	{	
		if (mConnection==null)
		{	mConnection = mURL.openConnection();
		}
		mLastChecked = System.currentTimeMillis()+mExpiryOffset;
		return mConnection;
	}
	
	private IURMeta createMeta()
	{	IURMeta meta = new IURMeta()
		{	public boolean isExpired()
			{				
				boolean result;
				if (mLastChecked==0)
				{	result = true;
				}
				else if (mFile==null || mLastChecked>System.currentTimeMillis() )
				{	result = false;
				}
				else
				{	result = mFile.lastModified()>mTimestamp;
					if (result)
					{	mLastChecked=0;
					}
					else
					{	mLastChecked = System.currentTimeMillis()+mExpiryOffset;
					}
				}
				
				return result;
			}
			/** return the pessimistic period that this resource may expire */
			public long getPessimisticExpiryTime()
			{	return mLastChecked;
			}
			/** return (a guess based on filename)) at the mimetype for this resource */
			public String getMimeType()
			{	return mMime;
			}
			/** return the cost of getting this resource- fixed at 4 */
			public int getCreationCost()
			{	return 4;
			}
			/** return the usage cost of this resource- proportional to length */
			public int getUsageCost()
			{	return mCost;
			}
			/** module resources are never intermediate resources, always primary */
			public boolean isIntermediate()
			{	return false;
			}
			/** module resources are never context sensitive because they are always
			 * the same no matter who asks for them
			 */
			public int getContextSensitivity()
			{	return 0;
			}
			/** debug representation */
			public String toString()
			{	return "ffcpl:"+mPath;
			}
		};
		return meta;
	}
	/** returns a guess at the encoding if the resource is text
	 */
	public String getEncoding()
	{	return mEncoding;
	}
}