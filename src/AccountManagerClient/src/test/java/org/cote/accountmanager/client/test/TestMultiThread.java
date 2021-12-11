package org.cote.accountmanager.client.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.cote.accountmanager.client.ClientContext;
import org.cote.accountmanager.client.CommunityContext;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.BasePermissionType;
import org.cote.accountmanager.objects.BaseRoleType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.AuditEnumType;
import org.cote.accountmanager.services.ThreadService;
import org.cote.propellant.objects.LifecycleType;

import org.junit.Test;

public class TestMultiThread extends BaseClientTest {

	private String testOpPath = "~/BulkOp";
	private int jobPeriod = 250;
    private int threads = 10;
    private int jobs = 5;
    private long maxTime = 10000L;
    private String communityBase = "Bulk Op - ";
    private String projectBase = "Bulk Proj -";
   
	@Test
	public void TestBulkObjectQuery() {
		CommunityContext cc = prepareCommunityContext();
		/*
		List<TestThread> opThreads = new ArrayList<>();
	    for(int i = 0; i < threads; i++) {
	    	TestBulkObjectThread thread = new TestBulkObjectThread(testUserContext);
	    	for(int j = 0; j < jobs; j++) thread.queueImport(projectBase + UUID.randomUUID().toString());
	    	thread.setThreadDelay(jobPeriod);
	    	opThreads.add(thread);
	    }
	    boolean ran = runThreads(opThreads);
	    assertTrue("Failed to run the threads", ran);
		*/

		
	}
	private boolean runThreads(List<TestThread> threads) {
		boolean retVal = false;

	    long startTime = System.currentTimeMillis();
	    boolean running = true;
	    boolean hadError = false;
	    while(running) {
	    	long now = System.currentTimeMillis();
	    	boolean allDone = true;
	    	for(TestThread svc : threads) {
	    		if(svc.getErrors().size() > 0) {
	    			hadError = true;
	    			running = false;
	    			break;
	    		}
	    		if(svc.isProcessing()) {
	    			allDone = false;
	    			break;
	    		}
	    	}
	    	if(now > (startTime + maxTime)) {
	    		logger.info("Timeout!");
	    		break;
	    	}
	    	if(allDone) {
	    		logger.info("Completed " + threads.size() + " jobs");
	    		running = false;
	    		retVal = true;
	    		break;
	    	}
	    	
	    }
	    for(ThreadService svc : threads) {
	    	svc.requestStop();
	    }

		return retVal;
	}
	class TestThread extends ThreadService{
		protected List<String> errors = new ArrayList<>();
		protected boolean processing = false;
		protected ClientContext context = null;
		public TestThread(ClientContext context) {
			super();
			this.context = context;


		}
		public boolean isProcessing() {
			return processing;
		}
		public List<String> getErrors(){
			return errors;
		}
	}
	class TestBulkObjectThread extends TestThread{
		private List<String> queue = new ArrayList<>();
		public TestBulkObjectThread(ClientContext context) {
			super(context);
		}

		public void queueImport(String path){
			synchronized(queue){
				queue.add(path);
			}
		}

		public void execute(){
			if(processing){
				logger.info("Processes currently pending");
				return;
			}
			if(queue.isEmpty()){
				return;
			}
			processing = true;
			logger.info("Processing pending import activities");
			
			List<String> workingQueue = new ArrayList<>();

			synchronized(queue){
				for(int i = 0; i < queue.size();i++){
					workingQueue.add(queue.get(i));
				}
				logger.info("Flushed queue into work queue");
				queue.clear();
			}

			try{

			}
			catch(Exception e){
				logger.error(FactoryException.LOGICAL_EXCEPTION,e);
				errors.add(e.getMessage());
			}
			finally{
				logger.info("Completed processing");
				processing = false;
			}

		}
	}
}
