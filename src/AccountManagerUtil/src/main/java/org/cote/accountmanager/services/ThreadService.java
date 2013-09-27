package org.cote.accountmanager.services;

public abstract class ThreadService extends Thread {
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
	}
	
	public void execute(){
		
	}
	
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
				e.printStackTrace();
			}
		}
	}

}
