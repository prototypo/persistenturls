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
 * File:          $RCSfile: ModuleDefinition.java,v $
 * Version:       $Name:  $ $Revision: 1.27 $
 * Last Modified: $Date: 2006/11/01 13:43:13 $
 *****************************************************************************/
package com.ten60.netkernel.module;

import com.ten60.netkernel.util.*;
import com.ten60.netkernel.urrequest.*;
import com.ten60.netkernel.urii.*;
import com.ten60.netkernel.urii.accessor.*;
import com.ten60.netkernel.module.rewrite.*;
import com.ten60.netkernel.module.accessor.ModuleResourceAccessor;
import com.ten60.netkernel.urii.representation.ITransrepresentor;
import com.ten60.netkernel.urii.fragment.IFragmentor;
import com.ten60.netkernel.container.Container;
import com.ten60.netkernel.cache.*;

import java.net.*;
import java.util.*;
import java.util.regex.*;
import java.io.*;
import org.w3c.dom.*;
import org.xml.sax.SAXException;
import java.util.jar.*;
import java.util.zip.*;

/**
 * In memory representation of a deployed module 
 * @author  tab
 */
public final class ModuleDefinition implements IRequestorContext
{
	public final static String ELEMENT_IMPORT = "import";
	public final static String ELEMENT_ACCESSOR = "ura";
	public final static String ELEMENT_THIS = "this";
	public final static String ELEMENT_SUPER = "super";
	public final static String ELEMENT_REWRITE = "rewrite";
	public final static String ELEMENT_SKIP = "skip";
	public final static String ELEMENT_VALIDATION_ERROR="validation-error";
	public final static String MODULE_CONFIG="module.xml";
	
	private Document mDoc;
	private String mURIString;
	private Version mVersion;
	private XMLReadable mReadable;
	private ModuleClassLoader mClassLoader;
	private URIdentifier mURI;
	private URIMapping[] mURIMappingsArray;
	private URIMapping mExportedURIMatches;
	private List mExportedClasses;
	private Map mAccessors;
	private List mTransreptors;
	private List mFragmentors;
	private List mImportedModules;
	private RegexRewriterMaplet mRewriter;
	private ICachelet mCache;
	private PairList mResourceExpirys;
	private String mScratchDir;
	private String mScratchDirURI;
	private boolean mEditable;
	private long mTimestamp;
	private ModuleManager mManager;
	private boolean mDynamicallyInitialised;
	private List mJarURLs;
	
	/** Creates a new instance of ModuleDefinition */
	public ModuleDefinition(String aModuleURIString, long aTimestamp, Document aDoc, ModuleManager aModuleManager) throws IOException, SAXException, MalformedURLException, ClassNotFoundException, NetKernelException
	{	mURIString = aModuleURIString;
		mDoc = aDoc;
		mReadable = new XMLReadable(mDoc);
		mURI = new URIdentifier(mReadable.getText("module/identity/uri").trim());
		mVersion = new Version(mReadable.getText("module/identity/version").trim());
		mRewriter = new RegexRewriterMaplet();
		mRewriter.configure(mReadable.getNodes("module/rewrite/rule"));
		mImportedModules = new ArrayList();
		mResourceExpirys = new PairList(4);
		mClassLoader = createClassLoader(mURIString, aModuleManager);
		if (aModuleURIString.startsWith("file:"))
		{	String configURI = aModuleURIString+MODULE_CONFIG;
			File f = new File(URI.create(configURI));
			mEditable = f.canWrite();
		}
		mTimestamp=aTimestamp;
		mManager=aModuleManager;
		
	}
	
	public void cleanup()
	{	
		cleanupComponents();
		
		mCache=null;
		if (mClassLoader!=null)
		{	mClassLoader.cleanup();
			mClassLoader=null;
		}
		
		if (mManager.getDynamicClassProvider()!=null)
		{	try
			{	mManager.getDynamicClassProvider().unregisterModule(this);
			}
			catch (Throwable t)
			{	NetKernelException ex = new NetKernelException("IOException","Error unregistering module with Dynamic Class Provider",getURI().toString());
				ex.addCause(t);
				SysLogger.log(SysLogger.WARNING, this, ex.toString());
			}
		}
		
		mImportedModules=null;
		mDoc=null;
		mURIMappingsArray=null;
		mManager=null;
	}

	public void cleanupComponents()
	{	if (mAccessors!=null)
		{	cleanupComponents(mAccessors.values());
		}
		if (mTransreptors!=null)
		{	cleanupComponents(mTransreptors);
		}
		if (mFragmentors!=null)
		{	cleanupComponents(mFragmentors);
		}
		
		mAccessors=null;
		mTransreptors=null;
		mFragmentors=null;		
	}
	
	
	private void cleanupComponents(Collection aComponents)
	{	for (Iterator i=aComponents.iterator(); i.hasNext(); )
		{	IURComponent component=null;
			try
			{	component = (IURComponent)i.next();
				component.destroy();
			}
			catch (Throwable t)
			{	String ex;
				if (t instanceof NetKernelException)
				{	ex=t.toString();
				}
				else
				{	ex=t.getClass().getName()+": "+t.getMessage();
				}
				SysLogger.log2(SysLogger.WARNING, this, "Exception encountered destroying [%1]: %2", component.getClass().getName(), ex);
			}
		}
	}

	public String getSourceURI()
	{	return mURIString;
	}
	
	public ICachelet getCache()
	{	return mCache;
	}

	public String getScratchDir()
	{	return mScratchDir;
	}
	/** Guaranteed *nix/Win32 independent scratch dir URI*/
	public String getScratchDirURI()
	{	return mScratchDirURI;
	}

	public URL getResource(String aPath)
	{	return Utils.getResource(aPath, mScratchDir, mClassLoader);
	}
	
	public boolean isExpanded()
	{	return !mURIString.endsWith("!/");
	}
	
	public long getTimestamp()
	{	return mTimestamp;
	}
	
	private void buildScratchDir(Container aContainer) throws IOException
	{
		String uriString = mURIString;
		if (uriString.endsWith("/")) uriString=uriString.substring(0,uriString.length()-1);
		if (uriString.startsWith("jar:")) uriString=uriString.substring(4,uriString.length()-(1+ModuleManager.JAR_SUFFIX.length()));
		URI uri1 = URI.create(uriString);
		URI uri2 = new File(aContainer.getBasePath()).toURI();
		URI uri3 = uri2.relativize(uri1);
		StringBuffer sb=new StringBuffer(uri3.toString());
		for (int i=sb.length()-1; i>=0; i--)
		{	char c = sb.charAt(i);
			if (!Character.isLetterOrDigit(c))
			{	sb.setCharAt(i, '_');
			}
		}
		File scratchDir = new File(aContainer.getScratchPath(),sb.toString());
		scratchDir.mkdirs();
		mScratchDir = Utils.fixSlash(scratchDir.getCanonicalPath()+"/");
		mScratchDirURI = File.separatorChar=='/'? "file:"+mScratchDir : "file:///"+mScratchDir;
		mScratchDirURI=mScratchDirURI.replaceAll(" ", "%20");
		
	}
	
	void parseMappings(ModuleManager aModuleManager,Container aContainer) throws Exception
	{
		buildScratchDir(aContainer);
		List URIMappings = new ArrayList();
		List mappings = getReadable().getNodes("module/mapping/*");
		NetKernelException exception = null;
		mClassLoader.reset();
		mImportedModules.clear();
		for (Iterator j=mappings.iterator(); j.hasNext(); )
		{	try
			{	Node mapping = (Node)j.next();
				String nodeName = mapping.getNodeName();
				if (nodeName.equals(ELEMENT_IMPORT))
				{	XMLReadable r = new XMLReadable(mapping);
					URIdentifier uri = new URIdentifier(r.getText("uri").trim());
					String minVersionString = r.getText("version-min").trim();
					String maxVersionString = r.getText("version-max").trim();
					Version minVersion = (minVersionString.length()>0)?new Version(minVersionString):null;
					Version maxVersion = (maxVersionString.length()>0)?new Version(maxVersionString):null;
					ModuleDefinition imported;
					try
					{	imported  = aModuleManager.getModule(uri,minVersion,maxVersion);
					}
					catch (NetKernelException e)
					{	//XMLUtils.appendTextedElement(mapping, ELEMENT_VALIDATION_ERROR, XMLUtils.escape(e.getMessage()));
						SysLogger.log3(SysLogger.SEVERE, this, "Failed to locate import [%1] in module [%2] :%3",uri.toString(),mURI.toString(),e.getMessage());
						throw e;
					}
					URIMappings.add( imported.getExportedURIMatches() );
					List exported=imported.getExportedClassMatches();
					if (exported.size()>0)
					{	mClassLoader.addImportedModule(imported);
					}
					mImportedModules.add(imported);
				}
				else if (nodeName.equals(ELEMENT_ACCESSOR))
				{	XMLReadable r = new XMLReadable(mapping);
					String match = r.getText("match").trim();
					String classString = r.getText("class").trim();
					URIMapping uriMapping = new URIMapping(match,classString);
					URIMappings.add(uriMapping);
					
				}
				else if (nodeName.equals(ELEMENT_THIS))
				{	XMLReadable r = new XMLReadable(mapping);
					List matches = r.getTexts("match", true);
					if (matches.size()==0)
					{	URIMapping uriMapping = new URIMapping(ModuleManager.MODULE_SCHEME+":.*",URIMapping.TYPE_THIS);
						URIMappings.add(uriMapping);
					}
					else
					{	for (Iterator i=matches.iterator(); i.hasNext(); )
						{	String match = (String)i.next();
							URIMapping uriMapping = new URIMapping(match,URIMapping.TYPE_THIS);
							URIMappings.add(uriMapping);
						}
					}
					List expiries = r.getNodes("expiry");
					for (Iterator i=expiries.iterator(); i.hasNext(); )
					{	Node expiry = (Node)i.next();
						XMLReadable r2 = new XMLReadable(expiry);
						Matcher matcher = Pattern.compile(r2.getText("match").trim()).matcher("");
						Long offset = new Long(r2.getText("offset").trim());
						mResourceExpirys.put(matcher,offset);
					}
				}
				else if (nodeName.equals(ELEMENT_SUPER))
				{	URIMapping uriMapping = new URIMapping(".*",URIMapping.TYPE_SUPER);
					URIMappings.add(uriMapping);
				}
				else if (nodeName.equals(ELEMENT_REWRITE))
				{	XMLReadable r = new XMLReadable(mapping);
					String pattern = r.getText("match").trim();
					String to = r.getText("to").trim();
					RegexRewriterRule rrr = new RegexRewriterRule(pattern, to);
					URIMapping uriMapping = new URIMapping(rrr);
					URIMappings.add(uriMapping);
				}
				else if (nodeName.equals(ELEMENT_SKIP))
				{	XMLReadable r = new XMLReadable(mapping);
					String pattern = r.getText("match").trim();
					int skipCount = Integer.parseInt(r.getText("count").trim());
					URIMapping uriMapping = new URIMapping(skipCount,pattern);
					URIMappings.add(uriMapping);
				}
			}
			catch (Exception e)
			{	if (exception==null)
				{	exception = new NetKernelException("failed to parse module",null,getURI().toString());
				}
				exception.addCause(e);
				SysLogger.log2(SysLogger.SEVERE, this, "Failed to parse mappings in module [%1] : %2", mURI.toString(),e.toString());
			}
		}
		// rationalise uri mappings
		mURIMappingsArray = new URIMapping[URIMappings.size()];
		for (int i=URIMappings.size()-1; i>=0; i--)
		{	URIMapping mapping = (URIMapping)URIMappings.get(i);
			mURIMappingsArray[i]=mapping;
		}
		
			
		
		if (exception!=null)
		{	throw exception;
		}
	}
	
	URIMapping[] getMappings()
	{	return mURIMappingsArray;
	}
	
	void parseFinal(Container aContainer) throws NetKernelException
	{	NetKernelException exception = null;

		// do any dynamic compilation
		if (!mDynamicallyInitialised && mManager.getDynamicClassProvider()!=null)
		{	mDynamicallyInitialised=true;
			try
			{	mManager.getDynamicClassProvider().registerModule(this);
			}
			catch (Throwable t)
			{	NetKernelException ex = new NetKernelException("IOException","Error registering module with Dynamic Class Provider",getURI().toString());
				ex.addCause(t);
				SysLogger.log(SysLogger.WARNING, this, ex.toString());			
			}
		}
		
		// initialise cache
		String cacheClassString = mReadable.getText("module/cache").trim();
		if (cacheClassString.length()>0)
		{	try
			{	Class cacheClass = mClassLoader.loadClass(cacheClassString);
				mCache = (ICachelet)cacheClass.newInstance();
				mCache.init(aContainer,this);
				Cache cache = (Cache)aContainer.getComponent(Cache.URI);
				cache.registerCacheletModule(this);
			}
			catch (Throwable e)
			{	exception = new NetKernelException("failed to parse module",null,getURI().toString());
				NetKernelException nke = new NetKernelException("Failed to initialise cache");
				nke.addCause(e);
				SysLogger.log2(SysLogger.SEVERE, this, "Failed to initialise cache in module [%1] :%2",mURI.toString(),e.getMessage());
				exception.addCause(nke);
			}
		}
		
		//initialise accessor table
		mAccessors = new HashMap();
		
		// parse transreptors
		List transreptorClassNames = getReadable().getTexts("module/transreptors/transreptor",true);
		mTransreptors = new ArrayList(transreptorClassNames.size());
		for (Iterator i = transreptorClassNames.iterator(); i.hasNext(); )
		{	String transreptorClassName = (String)i.next();
			try
			{	Class transreptorClass = mClassLoader.loadClass(transreptorClassName);
				ITransrepresentor instance = (ITransrepresentor)transreptorClass.newInstance();
				instance.initialise(aContainer,this);
				mTransreptors.add(instance);
			}
			catch (Throwable e)
			{	if (exception==null)
				{	exception = new NetKernelException("problems installing Transreptors");
				}
				NetKernelException e2 = new NetKernelException("problem installing Transreptor",null, transreptorClassName);
				e2.addCause(e);
				SysLogger.log3(SysLogger.SEVERE, this, "Failed to install transreptor [%1] in module [%2] : %3",transreptorClassName, mURI.toString(),e2.toString());
				exception.addCause(e2);
			}
		}
		
		//parse fragmentors
		transreptorClassNames = getReadable().getTexts("module/fragmentors/fragmentor",true);
		mFragmentors = new ArrayList(transreptorClassNames.size());
		for (Iterator i = transreptorClassNames.iterator(); i.hasNext(); )
		{	String fragmentorClassName = (String)i.next();
			try
			{	Class fragmentorClass = mClassLoader.loadClass(fragmentorClassName);
				IFragmentor instance = (IFragmentor)fragmentorClass.newInstance();
				instance.initialise(aContainer,this);
				mFragmentors.add(instance);
			}
			catch (Throwable e)
			{	if (exception==null)
				{	exception = new NetKernelException("problems installing Fragmentors");
				}
				NetKernelException e2 = new NetKernelException("problem installing Fragmentor",null, fragmentorClassName);
				e2.addCause(e);
				SysLogger.log3(SysLogger.SEVERE, this, "Failed to install fragmentor [%1] in module [%2] : %3",fragmentorClassName, mURI.toString(),e2.toString());
				exception.addCause(e2);
			}
		}
		if (exception!=null)
		{	throw exception;
		}
		
	}
	
	public XMLReadable getReadable()
	{	return mReadable;
	}
	
	private ModuleClassLoader createClassLoader(String aModuleURIString, ModuleManager aManager) throws MalformedURLException, IOException
	{	mJarURLs = new ArrayList();
		mJarURLs.add( aModuleURIString );
		
		if (aModuleURIString.startsWith("jar:"))
		{	URL xarURL = new URL(aModuleURIString);
			JarURLConnection juc = (JarURLConnection)xarURL.openConnection();
			JarFile jf = juc.getJarFile();
			Matcher m = Pattern.compile("lib/[^/]*\\.jar").matcher(""); // any jar directly in the lib dir
			for (Enumeration e = jf.entries(); e.hasMoreElements(); )
			{	ZipEntry entry = (ZipEntry)e.nextElement();
				m.reset(entry.getName());
				if (m.matches())
				{	URL nestedJar = aManager.expandNestedJar(jf,entry,mURI,mVersion);
					mJarURLs.add("jar:"+nestedJar.toString()+"!/");
				}
			}
		}
		else
		{	File libDir = new File(URI.create(aModuleURIString));
			libDir=new File(libDir,"lib");
			if (libDir.exists())
			{
				FileFilter ff = new FileFilter()
				{	public boolean accept(File aFile)
					{	return aFile.getName().endsWith(".jar");
					}
				};
				File[] filenames = libDir.listFiles(ff);
				for (int i=0; i<filenames.length; i++)
				{	mJarURLs.add( "jar:"+filenames[i].toURI().toString()+"!/" );
				}
			}
		}
		ModuleClassLoader mcl = new ModuleClassLoader(getClass().getClassLoader(),mJarURLs,this);
		return mcl;
	}
	
	public URIMapping getExportedURIMatches()
	{	if (mExportedURIMatches==null)
		{	List exportedURIs = getReadable().getTexts("module/export/uri/match", true);
			mExportedURIMatches = new URIMapping(exportedURIs,this);
		}
		return mExportedURIMatches;
	}
	
	public List getExportedClassMatches()
	{	if (mExportedClasses==null)
		{	mExportedClasses = getReadable().getTexts("module/export/class/match", true);
		}
		return mExportedClasses;
	}
	
	/** Used for dynamically building any source. This method returns a list of URLs
	 */
	public List getRawClassPathElements()
	{	return mJarURLs;
	}
	
	public List getImportedModules()
	{	return mImportedModules;
	}
	
	public PairList getResourceExpiries()
	{	return mResourceExpirys;
	}
	
	public ModuleClassLoader getClassLoader()
	{	return mClassLoader;
	}
	
	public URIdentifier getURI()
	{	return mURI;
	}
	
	public Version getVersion()
	{	return mVersion;
	}
	
	public String toString()
	{	return mURI.toString();
	}
	
	public URIdentifier rewrite(URIdentifier aURI)
	{	return mRewriter.map(aURI);
	}
	
	public IURAccessor getAccessor(String aClassname, Container aContainer) throws NetKernelException
	{	synchronized(mAccessors)
		{	IURAccessor result = (IURAccessor)mAccessors.get(aClassname);
			if (result==null)
			{	// create it now
				result = createAccessor(aClassname, aContainer);
				mAccessors.put(aClassname,result);
			}
			return result;
		}
	}
	
	private IURAccessor createAccessor(String aClassname, Container aContainer) throws NetKernelException
	{	try
		{	Class accessorClass = getClassLoader().loadClass(aClassname);
			ClassLoader cl2 = accessorClass.getClassLoader();
			if (cl2!=getClassLoader() && accessorClass!=ModuleResourceAccessor.class)
			{	NetKernelException e = new NetKernelException("Accessor failed to inherit module classloader","Classpath misconfiguration, accessor must be found in module", aClassname);
				throw e;
			}
			Thread.currentThread().setContextClassLoader(cl2);
			IURAccessor accessor = (IURAccessor)accessorClass.newInstance();
			accessor.initialise(aContainer,this);
			if (accessorClass==ModuleResourceAccessor.class)
			{	ModuleResourceAccessor mra = (ModuleResourceAccessor)accessor;
				mra.setResourceExpiries(getResourceExpiries());
			}
			return accessor;
		}
		catch (Exception e)
		{	NetKernelException e2 = new NetKernelException("Failed to create accessor",null,aClassname);
			e2.addCause(e);
			throw e2;
		}
	}
	
	public ITransrepresentor getTransrepresentorFor(IURRepresentation aFrom, Class aTo, List aLookedAtModules)
	{	ITransrepresentor result = null;
		if (!aLookedAtModules.contains(this))
		{	aLookedAtModules.add(this);
			// search this
			for (Iterator i = mTransreptors.iterator(); i.hasNext(); )
			{	ITransrepresentor transreptor = (ITransrepresentor)i.next();
				if (transreptor.supports(aFrom, aTo))
				{	result = transreptor;
					break;
				}
			}
			if (result==null)
			{	// search imports
				for (Iterator i = mImportedModules.iterator(); i.hasNext(); )
				{	ModuleDefinition md = (ModuleDefinition)i.next();
					result = md.getTransrepresentorFor(aFrom, aTo, aLookedAtModules);
					if (result!=null) break;
				}
			}
		}
		return result;
	}

	public IFragmentor getFragmentorFor(URRequest aRequest, List aLookedAtModules)
	{	IFragmentor result = null;
		if (!aLookedAtModules.contains(this))
		{	aLookedAtModules.add(this);
			// search this
			for (Iterator i = mFragmentors.iterator(); i.hasNext(); )
			{	IFragmentor fragmentor = (IFragmentor)i.next();
				if (fragmentor.matches(aRequest))
				{	result = fragmentor;
					break;
				}
			}
			if (result==null)
			{	// search imports
				for (Iterator i = mImportedModules.iterator(); i.hasNext(); )
				{	ModuleDefinition md = (ModuleDefinition)i.next();
					result = md.getFragmentorFor(aRequest, aLookedAtModules);
					if (result!=null) break;
				}
			}
		}
		return result;
	}
	
	public void write(Writer aWriter) throws IOException
	{	aWriter.write("<module>");

		aWriter.write("<identity>");
		write(aWriter,"uri","module/identity/uri");
		XMLUtils.writeEscaped(aWriter,"version",getVersion().toString(3));
		aWriter.write("</identity>");
		
		aWriter.write("<info>");
		write(aWriter,"name","module/info/name");
		write(aWriter,"description","module/info/description");
		XMLUtils.writeEscaped(aWriter,"source",mURIString);
		XMLUtils.writeEscaped(aWriter,"scratch", getScratchDirURI());
		XMLUtils.writeEscaped(aWriter,"editable", Boolean.toString(mEditable));
		XMLUtils.writeEscaped(aWriter,"timestamp", Long.toString(mTimestamp));
		aWriter.write("</info>");
		

		aWriter.write("<publisher>");
		write(aWriter,"name","module/publisher/name");
		write(aWriter,"uri","module/publisher/uri");
		aWriter.write("</publisher>");

		aWriter.write("<licence>");
		write(aWriter,"name","module/licence/name");
		write(aWriter,"uri","module/licence/uri");
		aWriter.write("</licence>");

		aWriter.write("<dependencies>");
		List mappings = getReadable().getNodes("module/mapping/*");
		for (Iterator i = mappings.iterator(); i.hasNext(); )
		{	Node n = (Node)i.next();
			if (n.getNodeName().equals(ELEMENT_IMPORT))
			{	XMLReadable imp = new XMLReadable(n);
				aWriter.write("<module>");
				
				String uri = imp.getText("uri").trim();
				XMLUtils.writeEscaped(aWriter,"uri",uri);
				ModuleDefinition found=null;
				for (Iterator j=mImportedModules.iterator(); j.hasNext(); )
				{	found = (ModuleDefinition)j.next();
					if (found.getURI().toString().equals(uri))
					{	XMLUtils.writeEscaped(aWriter,"version",found.getVersion().toString(3));
						break;
					}
				}
				if (found==null)
				{	XMLUtils.writeEscaped(aWriter,ELEMENT_VALIDATION_ERROR,"");
					//write(aWriter,ELEMENT_VALIDATION_ERROR,imp,ELEMENT_VALIDATION_ERROR);
				}
				aWriter.write("</module>");
			}
		}

		aWriter.write("</dependencies>");

		aWriter.write("<raw>");
		XMLUtils.getInstance().toXML(aWriter, mDoc,false,true,"UTF-8",4);
		aWriter.write("</raw>");
		aWriter.write("</module>");
	}
	
	
	private void write(Writer aWriter,String aName, String aXPath) throws IOException
	{	write(aWriter,aName,mReadable,aXPath);
	}
	
	private void write(Writer aWriter,String aName, XMLReadable aReadable, String aXPath) throws IOException
	{	String result = aReadable.getText(aXPath).trim();
		XMLUtils.writeEscaped(aWriter,aName,result);
	}
	
	
}