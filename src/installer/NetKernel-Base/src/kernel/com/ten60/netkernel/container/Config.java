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
 * File:          $RCSfile: Config.java,v $
 * Version:       $Name:  $ $Revision: 1.6 $
 * Last Modified: $Date: 2005/09/30 10:31:31 $
 *****************************************************************************/
package com.ten60.netkernel.container;
import com.ten60.netkernel.urii.*;
import com.ten60.netkernel.util.*;
import com.ten60.netkernel.urii.aspect.IAspectBinaryStream;

import java.net.*;
import org.w3c.dom.*;
import java.io.*;

/**
 * Configuration Component reads in system configuration information and holds it for other components
 * to access
 * @author  tab
 */
public class Config extends ComponentImpl
{	/** our URI */
	public static final URIdentifier URI = new URIdentifier("netkernel:config");
	/** the parsed XML document of configuration data */
	private Document mDocument;
	/** An XMLReadable interface over mDocument */
	private XMLReadable mReadable;
	/** the URL of our configuration data */
	private URL mURL;
	
	/** Constructs a Config
	 */
	public Config()
	{	super(URI);
	}
	
	/** Starts the Config component reading in the configuration ready for use. Configuration
	 * path is specified by the Container.getConfigPath() method.
	 *@ NetKernelException thrown if we fail to find or parse the configuration file
	 */
	public void start(Container aContainer) throws NetKernelException
	{	try
		{	mURL=new URL(aContainer.getConfigURI());
			URLConnection con = mURL.openConnection();
			InputStream is = con.getInputStream();
			mDocument = XMLUtils.parse(is);
			mReadable = new XMLReadable(mDocument);
			is.close();
			NetKernelException.setTraceDepth(mReadable.getInt("system/exceptionStackDepth",4));
			NetKernelError.setTraceDepth(mReadable.getInt("system/exceptionStackDepth",4));
			SysLogger.log1(SysLogger.CONTAINER, this, "  System Configuration read from [%1]", aContainer.getConfigURI());
		} catch (Exception e)
		{	NetKernelException e2 = new NetKernelException("Failed to read/parse configuration",null,aContainer.getConfigURI());
			e2.addCause(e);
			throw e2;
		}
		
		//Update HTTP proxy
		boolean proxyEnabled = !mReadable.getText("/system/proxy/type").equals("none");
		if (proxyEnabled)
		{	System.getProperties().put("proxySet", "true");
			System.getProperties().put("proxyHost", mReadable.getText("/system/proxy/host"));
			System.getProperties().put("proxyPort", mReadable.getText("/system/proxy/port"));		
		}
		else
		{	System.getProperties().put("proxySet", "false");
		}
		//update meta debug flag
		System.getProperties().put("netkernel.debugMeta", mReadable.getText("/system/debugMeta").trim());
	}

	/** Returns an XMLReadable interface over the configuration
	 */
	public XMLReadable getReadable()
	{	return mReadable;
	}
	/** Writes the stored configuration to the outputstream
	 * @param aStream the output stream
	 */
	public void write(OutputStream aStream) throws IOException
	{	URLConnection con = mURL.openConnection();
		InputStream is = con.getInputStream();
		Utils.pipe(is, aStream);
	}
	
	/** Writes the configuration from the given aspect to the underlying storage.
	 * A restart is necessary for changes to take effect
	 */
	public void sink(IAspectBinaryStream aData) throws IOException, NetKernelException
	{	if (mURL.getProtocol().equals("file"))
		{	URI uri = java.net.URI.create(mURL.toString());
			File f = new File(uri);
			FileOutputStream os = new FileOutputStream(f);
			try
			{	aData.write(os);
			}
			finally
			{	os.close();
			}
		}
		else
		{	throw new NetKernelException("Config not read from file","It cannot be updated",mURL.toString());
		}
	}
}