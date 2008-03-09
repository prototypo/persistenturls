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
 * File:          $RCSfile: CommandLine.java,v $
 * Version:       $Name:  $ $Revision: 1.2 $
 * Last Modified: $Date: 2005/05/09 12:48:39 $
 *****************************************************************************/
package com.ten60.netkernel.cli;


import java.io.*;
import org.w3c.dom.*;
import java.net.URI;
import java.util.*;

import com.ten60.netkernel.embedded.*;
import com.ten60.netkernel.util.Utils;
import com.ten60.netkernel.urii.aspect.*;
import com.ten60.netkernel.urii.meta.*;
import org.ten60.netkernel.layer1.representation.*;  //NB Layer1 must be in classpath !


/**
 * Entrypoint to executing NetKernel on the commandline. Input stream is param doc,
 * output stream is response doc. Stderr is any console output.
 * Params are basepath, idoc-uri
 * @author tab
 */
public class CommandLine
{
	private String mBasePath;
	private String mURI;
	private boolean mIdocURI=true;
	private byte[] mParam;
	private boolean mParamURI=false;
	private URI mCWU;
	private Boolean mVerbose;
	
	/**
	 * Exec NetKernel to process request
	 */
	private void exec()
	{	// redirect stdout to stderr
		PrintStream oldOut = System.out;
		System.setOut(System.err);
		IEmbeddedAPI netKernel=null;
		try
		{	netKernel=EmbeddedAPIFactory.create(mBasePath);
			netKernel.start();
			
			URI request=null;
			
			RequestArgs args=new RequestArgs();
			request=URI.create(mURI);
			if (mParamURI)
			{	request=URI.create(request.toString()+"+param@"+mParamURI);
			}
			else
			{	if (mParam!=null)
				{	request=URI.create(request.toString()+"+param@literal:param");
					args.put("literal:param", ByteArrayAspect.create( new MetaImpl("text/xml", 0, 0), mParam));
				}
			}
			EmbeddedToolkit.writeResource(netKernel, request, args, oldOut);
		}
		catch (EmbeddedException e)
		{	e.getReason().printStackTrace();
			System.exit(2);
		}
		finally
		{	try
			{	netKernel.stop();
			}
			catch(EmbeddedException e)
			{	e.printStackTrace();
			}
		}
	}
	
	private void parseCL(String[] args)
	{	CmdLineParser clp=new CmdLineParser();
		CmdLineParser.Option basepath=clp.addStringOption('b', "basepath");
		//CmdLineParser.Option idoc=clp.addStringOption('i', "idoc");
		CmdLineParser.Option URI=clp.addStringOption('u', "URI");
		CmdLineParser.Option verbose=clp.addBooleanOption('v', "verbose");
		CmdLineParser.Option param=clp.addStringOption('P', "param");
		//CmdLineParser.Option paramURI=clp.addStringOption('p', "paramURI");
		CmdLineParser.Option options=clp.addStringOption('o', "options");
		CmdLineParser.Option stdin=clp.addBooleanOption('s', "stdin");
		CmdLineParser.Option xcwu=clp.addBooleanOption('x', "xcwu");
		CmdLineParser.Option help=clp.addBooleanOption('h', "help");
		
		try
		{	clp.parse(args);
		}
		catch ( CmdLineParser.OptionException e )
		{	System.err.println(e.getMessage());
			printUsage();
			System.exit(2);
		}
		
		try
		{	Boolean test=new Boolean(false);
			mVerbose=(Boolean)clp.getOptionValue(verbose);
			mBasePath=(String)clp.getOptionValue(basepath);
			if(mVerbose!=null && mVerbose.booleanValue()) System.err.println("BasePath="+mBasePath);
			if(mBasePath==null) throw new OptionMissingException("basepath");
			
			mURI=(String)clp.getOptionValue(URI);
			if(mURI==null)
			{
				throw new OptionMissingException("u");
				/*
				mURI=(String)clp.getOptionValue(idoc);
				mIdocURI=false;
				 */
			}
			if(mURI==null) throw new OptionMissingException("URI or idoc");
			
			test=(Boolean)clp.getOptionValue(stdin);
			if(test!=null && test.booleanValue())
			{	mParam=captureStdIn();
			}
			else
			{	String s=(String)clp.getOptionValue(param);
				if(s!=null)	mParam=s.getBytes();
				/*
				if(mParam==null)
				{
					s=(String)clp.getOptionValue(paramURI);
					mParam=s.getBytes();
					if(mParam!=null) mParamURI=true;
				}
				 */
			}
			
			test=(Boolean)clp.getOptionValue(xcwu);
			if(test==null)
			{	getCurrentWorkingURI();
			}
			
			test=(Boolean)clp.getOptionValue(help);
			if(test!=null && test.booleanValue())
			{	printUsage();
				System.exit(0);
			}
			
		}
		catch ( OptionMissingException e )
		{
			System.err.println(e.getMessage());
			printUsage();
			System.exit(2);
		}
	}
	
	private byte[] captureStdIn()
	{	ByteArrayOutputStream baos=new ByteArrayOutputStream(20000);
		//StringWriter sw=new StringWriter(20000);
		BufferedInputStream bis=new BufferedInputStream(System.in);
		try
		{	byte[] in=new byte[256];
			int read=0;
			while((read=bis.read(in))>0)
			{	baos.write(in,0, read);
			}
			return baos.toByteArray();
		}
		catch(IOException e)
		{	e.printStackTrace();
			return null;
		}
	}
	
	private void getCurrentWorkingURI()
	{	File f=new File(".");
		mCWU=f.toURI();
		if(mVerbose!=null && mVerbose.booleanValue())
		{	System.err.println("CWU="+mCWU);
		}
	}
	
	private void printUsage()
	{	System.err.println("1060 NetKernel v2.x.x");
		System.err.println("(C) 2002-2005 1060 Research Limited");
		System.err.println("Licensed under 1060 Public License v1.0\n");
		System.err.println("Usage: com.ten60.netkernel.cli.CommandLine [options]");
		System.err.println("-b <basepath>	: NetKernel installation basepath");
		System.err.println("-u <uri>	:  URI to execute, uri is either absolute or relative resolved to current working URI");
		//System.err.println("-p <paramuri>	: Parameter uri, uri is either absolute wrt xapp directory or relative resolved to current working URI");
		//System.err.println("-i <idoc>	: Execute literal idoc document");
		System.err.println("-P <param>	: Parameter literal, may be binary");
		System.err.println("-x		: Do NOT Inherit Path as Current Working URI");
		System.err.println("-v		: Verbose output");
		System.err.println("-s		: Get Parameter from Stdin, overrides -p -P, allows for piped execution");
		//[-o <name,type,value:..>]");
	}
	
	/**
	 * Commandline main method
	 * @param args the command line arguments
	 */
	public static void main(String[] args)
	{   CommandLine cl= new CommandLine();
		cl.parseCL(args);
		cl.exec();
	}
	
	public static class OptionMissingException extends Exception
	{	OptionMissingException( String optionName )
		{	super("option '" + optionName + "' is required");
		}
	}
}