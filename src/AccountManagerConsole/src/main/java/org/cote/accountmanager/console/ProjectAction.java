package org.cote.accountmanager.console;

import org.apache.log4j.Logger;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.DataAccessException;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.objects.UserType;
/*
import org.cote.propellant.objects.LifecycleType;
import org.cote.propellant.objects.ProjectType;
import org.cote.rocket.Factories;
import org.cote.rocket.Rocket;
import org.cote.rocket.util.ProjectImportUtil;
*/

public class ProjectAction {
	public static final Logger logger = Logger.getLogger(ProjectAction.class.getName());
	/*
	public static void importProjectFile(UserType user, String lifecycleName, String projectName, String projectFile){
		ProjectType proj = null;
		LifecycleType lc = null;
		try {
			lc = Rocket.getLifecycle(lifecycleName, user.getOrganization());
			if(lc == null) lc = Rocket.createLifecycle(user, lifecycleName);
			else Factories.getLifecycleFactory().populate(lc);
			if(lc == null){
				logger.error("Null lifecycle for name '" + lifecycleName + "'");
				return;
			}
			proj = Rocket.getProject(projectName, lc, user.getOrganization());
			if(proj != null){
				logger.error("Project '" + projectName + "' already exists");
				return;
			}
			proj = Rocket.createProject(user, lc, projectName);


		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DataAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(proj == null){
			logger.error("Null project for name '" + projectName + "'");
			return;
		}
		ProjectImportUtil.importProject(user, proj,projectFile, false);

	}
	*/
}
