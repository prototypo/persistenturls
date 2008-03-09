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

  File:          $RCSfile: WorkerThreadPool.java,v $
  Version:       $Name:  $ $Revision: 1.6 $
  Last Modified: $Date: 2007/10/05 12:25:41 $
 *****************************************************************************/
package com.ten60.netkernel.scheduler;

import java.util.*;

/** A ProcessofThreadGroup is a homogeneous collection of ProcessorThreads which can
 * be managed as a group. This includes start/stop/suspend/resume. But also the
 * groups size can be change dynamically.
 */
public abstract class WorkerThreadPool
{
    /** The real java ThreadGroup that threads in this ProcessorThreadGroup belong.
     */
    private ThreadGroup mThreadGroup;
    /** The set of active threads in this group
     */
    protected final ArrayList mProcessorThreads = new ArrayList();
    /** The set of dying or dead threads that this group has finished with
     */
    private final ArrayList mDyingThreads = new ArrayList();
    /** Set to true after start() is called.
     */
    private boolean mStarted;
    /** Set to true after stop() is called.
     */
    private boolean mStopped;
	
	private int mSetThreadCount;
    
    /** Creates a new ProcessorThreadGroup.
     * @param aName The name that the real java ThreadGroup will be given
     * @param aInitialThreads The initial number of threads that this group will have.
	 * @param aParentGroup parent thread group
     */
    public WorkerThreadPool(String aName, int aInitialThreads, ThreadGroup aParentGroup)
    {	ThreadGroup[]groups = new ThreadGroup[aParentGroup.activeGroupCount()];
		int c=aParentGroup.enumerate(groups,false);
		for (int i=0; i<c; i++)
		{	ThreadGroup g=groups[i];
			if (g.getName().equals(aName))
			{	mThreadGroup=g;
				break;
			}
		}
		if (mThreadGroup==null)
		{	mThreadGroup = new ThreadGroup(aParentGroup, aName);
		}
        setCount(aInitialThreads);
    }
    
    /** Returns the real java ThreadGroup object for this group.
     * @return The java.lang.ThreadGroup.
     */
    public ThreadGroup getThreadGroup()
    {   return mThreadGroup;
    }
    
    /** Dynamically changes the number of active threads within this group. It is
     * valid to call this method at any time before the group is stopped.
     * @param aThreadCount The desired number of active threads for the group.
     */
    public synchronized void setCount(int aThreadCount)
    {   int actualCount = getActualThreadCount();
		mSetThreadCount=aThreadCount;
        if (actualCount<aThreadCount)
        {   int threadsToCreate = aThreadCount - actualCount;
            for (int i=0; i<threadsToCreate; i++)
            {   WorkerThread pt = createNewThread(mThreadGroup);
                mProcessorThreads.add(pt);
                if (mStarted && !mStopped)
                {   pt.start();
                }
            }
        }
        else if (actualCount>aThreadCount)
        {   int threadsToStop = actualCount - aThreadCount;
            Iterator iterator = mProcessorThreads.iterator();
            // stopped threads are moved onto mDyingThreads so
            // that they can be tracked until dead.
            for (int i=0; i<threadsToStop; i++)
            {   WorkerThread pt = (WorkerThread)iterator.next();
                pt.requestStop();
                iterator.remove();
                mDyingThreads.add(pt);
            }
        }
    }
    
    /** The count of the number of actual threads in this group.
     * @return The count of the number of active threads in this group.
     */
    public int getActualThreadCount()
    {   purgeDyingThreads();
        return mProcessorThreads.size();
    }
	
	public int getSetThreadCount()
	{	return mSetThreadCount;
	}
    
    
    /** Internal housekeeping method to forget about threads which have now been
     * confirmed dead.
     */
    private synchronized void purgeDyingThreads()
    {   for (Iterator i = mDyingThreads.iterator(); i.hasNext(); )
        {   WorkerThread pt = (WorkerThread)i.next();
            if (!pt.isAlive())
            {   i.remove();
            }
        }
    }
    
    /** Abstract method to instantiate a new instance of a subclass of ProcessorThread
     * to be added to this group.
     * @param aThisGroup The ThreadGroup that the new thread should belong to.
     * @return The newly created ProcessorThread.
     */
    protected WorkerThread createNewThread(ThreadGroup aThisGroup)
	{	return new WorkerThread(this,aThisGroup);
	}
    
    /** Starts all the threads in the group.
     */
    public void start()
    {   if (!mStarted && !mStopped)
        {   mStarted=true;
            for (Iterator i = mProcessorThreads.iterator(); i.hasNext(); )
            {   WorkerThread pt = (WorkerThread)i.next();
                pt.start();
            }
        }
    }
    
    /** Requests that all the threads in the group are stopped.
     */
    public void stop()
    {   if (mStarted && !mStopped)
        {   setCount(0);
            mStopped=true;
		}
    }

    /**
     * Wait for all threads in group to stop.
	 * @param aTimeout time to wait for threads before killing them
     * @throws InterruptedException Thrown if we are interrupted whilst waiting for the threads to die.
     */
    public void join(long aTimeout)
    throws InterruptedException
    {   if (mStopped)
        {   // join on all dying threads
            for (Iterator i = mDyingThreads.iterator(); i.hasNext(); )
            {   WorkerThread pt = (WorkerThread)i.next();
                pt.join(aTimeout);
				if (pt.isAlive())
				{	pt.stop();
				}
				else
				{	i.remove();
				}
            }
        }
    }
	
	/** The given thread will be removed from the thread pool and replaced with a fresh one
	 * @param aThread the thread to replace
	 */
	public synchronized void replace(Thread aThread)
	{
		if (mProcessorThreads.remove(aThread))
		{	//stop existing thread
			((WorkerThread)aThread).requestStop();
			mDyingThreads.add(aThread);
			
			//start new one
			WorkerThread pt = createNewThread(mThreadGroup);
			mProcessorThreads.add(pt);
			if (mStarted && !mStopped)
			{   pt.start();
			}			
		}
	}
	
	
	public abstract void process() throws InterruptedException;
}
