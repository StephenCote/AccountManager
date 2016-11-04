/*******************************************************************************
 * Copyright (C) 2002, 2015 Stephen Cote Enterprises, LLC. All rights reserved.
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
package org.cote.accountmanager.console;

import org.apache.log4j.Logger;

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
			
			logger.error(e.getStackTrace());
		} catch (ArgumentException e) {
			
			logger.error(e.getStackTrace());
		} catch (DataAccessException e) {
			
			logger.error(e.getStackTrace());
		}
		if(proj == null){
			logger.error("Null project for name '" + projectName + "'");
			return;
		}
		ProjectImportUtil.importProject(user, proj,projectFile, false);

	}
	*/
}
