/******************************************************************************
  (c) Copyright 2002,2003, 1060 Research Ltd                                   

  This Software is licensed to You, the licensee, for use under the terms of   
  the 1060 Public License v1.0. Please read and agree to the 1060 Public       
  License v1.0 [www.1060research.com/license] before using or redistributing   
  this software.                                                               

  In summary the 1060 Public license has the following conditions.             
  A. You may use the Software free of charge provided you agree to the terms   
  laid out in the 1060 Public License v1.0                                     
  B. You are only permitted to use the Software with components or applications
  that provide you with OSI Certified Open Source Code [www.opensource.org], or
  for which licensing has been approved by 1060 Research Limited.              
  You may write your own software for execution by this Software provided any  
  distribution of your software with this Software complies with terms set out 
  in section 2 of the 1060 Public License v1.0                                 
  C. You may redistribute the Software provided you comply with the terms of   
  the 1060 Public License v1.0 and that no warranty is implied or given.       
  D. If you find you are unable to comply with this license you may seek to    
  obtain an alternative license from 1060 Research Limited by contacting       
  license@1060research.com or by visiting www.1060research.com                 

  NO WARRANTY:  THIS SOFTWARE IS NOT COVERED BY ANY WARRANTY. SEE 1060 PUBLIC  
  LICENSE V1.0 FOR DETAILS                                                     

  THIS COPYRIGHT NOTICE IS *NOT* THE 1060 PUBLIC LICENSE v1.0. PLEASE READ     
  THE DISTRIBUTED 1060_Public_License.txt OR www.1060research.com/license      

  File:          $RCSfile: WorkerThread.java,v $
  Version:       $Name:  $ $Revision: 1.3 $
  Last Modified: $Date: 2004/08/24 11:32:46 $
 *****************************************************************************/
package com.ten60.netkernel.scheduler;
import com.ten60.netkernel.util.*;

/** WorkerThread is a child of a WorkerThreadPool, it exists to provide
  execution threads to call its parents process() method
 */
public class WorkerThread extends Thread
{
    private WorkerThreadPool mParent;
    /** When true the thread is either signalled to stop ASAP or has already stopped.
     */
    private boolean mStopped;
	
    /** Construct a new QProcessorThread.
     * @param aParent The owning QProcessorThreadGroup.
     */    
    public WorkerThread(WorkerThreadPool aParent, ThreadGroup aThreadGroup)
    {	super(aThreadGroup,"WorkerThread"+getThreadNumber());
		mParent = aParent;
    }
	
	private static int mThreadNumber;
	
	private static String getThreadNumber()
	{	return Integer.toString(mThreadNumber++);
	}
    
    /** The processing loop to do the threads work. It also checks the enabled and stop flags
     * to control the threads operations in a safe manner.
     */
    public void run()
    {  setContextClassLoader(getClass().getClassLoader());
	   while(!mStopped)
	   {	try	// keep catch block outside loop
			{	while (!mStopped)
				{	mParent.process();
				}
			}
			catch (InterruptedException e)
			{   // natural termination
				mStopped=true;
			}
			catch (Throwable e)
			{   // don't allow it to kill throws the thread just carry on
				SysLogger.log1(SysLogger.SEVERE,this,"Caught runtime exception %1",e);
				e.printStackTrace();
			}
	   }
        XMLUtils.destroyInstance();
		setContextClassLoader(null);
    }
    
    /** Signals for the thread to stop once it has finished any work it is part way
     * through.
     */
    public void requestStop()
    {   if (mStopped==false)
        {
            mStopped=true;
            synchronized(this)
			{   notify();
			}
        }
    }	
}