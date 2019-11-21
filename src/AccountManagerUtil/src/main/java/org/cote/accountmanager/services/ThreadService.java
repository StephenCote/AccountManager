/*******************************************************************************
 * Copyright (C) 2002, 2020 Stephen Cote Enterprises, LLC. All rights reserved.
 * Redistribution without modification is permitted provided the following conditions are met:
 *
 *    1. Redistribution may not deviate from the original distribution,
 *        and must reproduce the above copyright notice, this list of conditions
 *        and the following disclaimer in the documentation and/or other materials
 *        provided with the distribution.
 *    2. Products may be derived from this software.
 *    3. Redistributions of any form whatsoever must retain the following acknowledgment:
 *        "This product includes software developed by Stephen Cote Enterprises, LLC"
 *
 * THIS SOFTWARE IS PROVIDED BY STEPHEN COTE ENTERPRISES, LLC ``AS IS''
 * AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THIS PROJECT OR ITS CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, 
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY 
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package org.cote.accountmanager.services;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.exceptions.FactoryException;

public abstract class ThreadService implements Runnable {
	public static final Logger logger = LogManager.getLogger(ThreadService.class);
	private int threadDelay = 1000;
	private boolean stopRequested=false;
	private Thread svcThread = null;
	
	public ThreadService(){
		svcThread = new Thread(this);
		svcThread.setPriority(Thread.MIN_PRIORITY);
		svcThread.start();
	}

	public int getThreadDelay() {
		return threadDelay;
	}

	public void setThreadDelay(int threadDelay) {
		this.threadDelay = threadDelay;
	}

	public void requestStop(){
		stopRequested=true;
		svcThread.interrupt();
		try{
			execute();
		}
		catch(Exception e){
			logger.error(e.getMessage());
		}
		
	}
	
	public void execute(){
		
	}
	
	@Override
	public void run(){
		while (!stopRequested){
			try{
				Thread.sleep(threadDelay);
			}
			catch (InterruptedException ex){
				/* ... */
			}
			try{
				execute();
			}
			catch(Exception e){
				logger.error(e.getMessage());
				logger.error(FactoryException.TRACE_EXCEPTION,e);
			}
		}
	}

}
