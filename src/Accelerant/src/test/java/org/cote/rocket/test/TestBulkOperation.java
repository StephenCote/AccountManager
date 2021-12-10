package org.cote.rocket.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.BasePermissionType;
import org.cote.accountmanager.objects.BaseRoleType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.AuditEnumType;
import org.cote.accountmanager.service.rest.BaseService;
import org.cote.accountmanager.services.ThreadService;
import org.cote.propellant.objects.LifecycleType;
import org.cote.rocket.Factories;
import org.junit.Test;



public class TestBulkOperation extends BaseAccelerantTest {

	private String testOpPath = "~/BulkOp";
	private int jobPeriod = 250;
    private int threads = 10;
    private int jobs = 5;
    private long maxTime = 10000L;
    private String communityBase = "Bulk Op - ";
    private String projectBase = "Bulk Proj -";
    private void cleanupLifecycles() {
    	UserType admin = this.getAdminUser(testUser.getOrganizationId());
    	DirectoryGroupType dir = BaseService.find(AuditEnumType.GROUP, "DATA", "/Rocket/Lifecycles", admin);
    	assertNotNull("Directory is null", dir);
    	List<DirectoryGroupType> alc = BaseService.listByParentObjectId(AuditEnumType.GROUP, "UNKNOWN", dir.getObjectId(), 0L, 0, admin);
    	int deleted = 0;
    	int failed = 0;
    	int missed = 0;
    	for(DirectoryGroupType ld : alc) {
    		if(ld.getName().startsWith(communityBase)) {
    			logger.info("Cleanup " + ld.getName());
    			LifecycleType l = getProvider().getCommunity(admin, ld.getName());
    			if(l == null) {
    				logger.warn("Orphan group parent: " + ld.getName());
    				
    				BaseRoleType role = BaseService.find(AuditEnumType.ROLE, "USER", "/Rocket/Lifecycles/" + ld.getName(), admin);
    				BasePermissionType per = BaseService.find(AuditEnumType.PERMISSION, "USER", "/Rocket/Lifecycles/" + ld.getName(), admin);
    				if(role != null) BaseService.delete(AuditEnumType.ROLE, role, admin);
    				if(per != null) BaseService.delete(AuditEnumType.PERMISSION, per, admin);
    				BaseService.delete(AuditEnumType.GROUP, ld, admin);
    				missed++;
    				continue;
    			}
    			boolean del = getProvider().deleteCommunity(admin, l.getObjectId());
    			if(del) deleted++;
    			else failed++;
    		}
    	}
    	logger.info("Cleaned up " + deleted + ", failed to cleanup " + failed + ", missed orphaned directories "  + missed);
    	Factories.cleanupOrphans();
    }
	@Test
	public void TestBulkProjectSetup() {

	    cleanupLifecycles();

		List<TestThread> opThreads = new ArrayList<>();
	    for(int i = 0; i < threads; i++) {
	    	TestBulkProjectSetup thread = new TestBulkProjectSetup(this, testUser, communityBase +  UUID.randomUUID().toString());
	    	for(int j = 0; j < jobs; j++) thread.queueImport(projectBase + UUID.randomUUID().toString());
	    	thread.setThreadDelay(jobPeriod);
	    	opThreads.add(thread);
	    }
	    boolean ran = runThreads(opThreads);
	    assertTrue("Failed to run the threads", ran);


		
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
		protected UserType user = null;
		protected BaseAccelerantTest test = null;
		public TestThread(BaseAccelerantTest test, UserType user) {
			super();
			this.user = user;
			this.test = test;

		}
		public boolean isProcessing() {
			return processing;
		}
		public List<String> getErrors(){
			return errors;
		}
	}
	class TestBulkProjectSetup extends TestThread{
		private List<String> queue = new ArrayList<>();
		
		private String communityName = null;
		
		public TestBulkProjectSetup(BaseAccelerantTest test, UserType user, String communityName){
			super(test, user);
			this.communityName = communityName;
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
				///LifecycleType community = test.getProviderCommunity(test.getAdminUser(user.getOrganizationId()), communityName, false);
				UserType admin = test.getAdminUser(user.getOrganizationId());
				logger.info("Create community: " + communityName);
				boolean created = test.getProvider().createCommunity(admin, communityName);
				if(created == false) {
					throw new FactoryException("Failed to created community " + communityName);
				}
				LifecycleType community = getProvider().getCommunity(admin, communityName);
				
				if(community == null) throw new FactoryException("Failed to retrieve community " + communityName);
				for(String projectName : workingQueue) {
					logger.info("Create project: " + projectName);
					boolean createdProj = getProvider().createCommunityProject(admin, community.getObjectId(), projectName);
					if(createdProj == false) {
						throw new FactoryException("Failed to create community project " + projectName);
					}
					/*
					DirectoryGroupType dir = BaseService.makeFind(AuditEnumType.GROUP, "DATA", path, testUser);
					if(dir == null) throw new FactoryException("Failed to create " + path);
					boolean addDefaults = RocketModel.addDefaults(user, dir.getId());
					if(!addDefaults) throw new FactoryException("Failed to add defaults to path " + path + " (#" + dir.getId() + ")");
					*/
					/*
					try{

					}
					catch(FactoryException | ArgumentException e) {
						logger.error(FactoryException.LOGICAL_EXCEPTION,e);
					}
					*/
				}
			}
			catch(Exception e){
				logger.error(FactoryException.LOGICAL_EXCEPTION,e);
				errors.add(e.getMessage());
				/*
				Map<Long, String> map = BulkFactories.getBulkFactory().getSessionIdMap();
				Iterator<Long> keys = map.keySet().iterator();
				
				logger.warn("Bulk map dump");
				while(keys.hasNext()){
					long val = keys.next();
					logger.warn(map.get(val) + " - " + val);
				}
				*/
			}
			finally{
				logger.info("Completed processing");
				processing = false;
			}

		}
	}
	
}
