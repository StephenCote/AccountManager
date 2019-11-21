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
package org.cote.rocket.test;

import static org.junit.Assert.assertTrue;

import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.factory.GroupFactory;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.propellant.objects.ArtifactParticipantType;
import org.cote.propellant.objects.ArtifactType;
import org.cote.propellant.objects.CaseParticipantType;
import org.cote.propellant.objects.CaseType;
import org.cote.propellant.objects.ModelType;
import org.cote.propellant.objects.RequirementParticipantType;
import org.cote.propellant.objects.RequirementType;
import org.cote.propellant.objects.types.ArtifactEnumType;
import org.cote.propellant.objects.types.CaseEnumType;
import org.cote.rocket.Factories;
import org.cote.rocket.factory.ArtifactFactory;
import org.cote.rocket.factory.CaseFactory;
import org.cote.rocket.factory.ModelFactory;
import org.cote.rocket.factory.ModelParticipationFactory;
import org.cote.rocket.factory.RequirementFactory;
import org.junit.Test;
public class TestModelParticipationFactory extends BaseAccelerantTest{
	public static final Logger logger = LogManager.getLogger(TestModelParticipationFactory.class);
	private static String sessionId = null;
	private static ModelType model1 = null;
	private static ModelType model2 = null;
	private static String modelName1 = "Model QA #1";
	private static String modelName2 = "Model QA #2";

	
	
	@Test
	public void testCreateParts(){
		boolean added = false;
		assertTrue("User is not populated", testUser.getPopulated());
		try{
			DirectoryGroupType dir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreateDirectory(testUser, "Rocket", testUser.getHomeDirectory(), testUser.getOrganizationId());
			ArtifactType art1 = ((ArtifactFactory)Factories.getFactory(FactoryEnumType.ARTIFACT)).newArtifact(testUser, dir.getId());
			art1.setArtifactType(ArtifactEnumType.TEST);
			art1.setName(UUID.randomUUID().toString());
			((ArtifactFactory)Factories.getFactory(FactoryEnumType.ARTIFACT)).add(art1);
			art1 = ((ArtifactFactory)Factories.getFactory(FactoryEnumType.ARTIFACT)).getByNameInGroup(art1.getName(), dir);
			ArtifactParticipantType artPart1 = ((ModelParticipationFactory)Factories.getFactory(FactoryEnumType.MODELPARTICIPATION)).newArtifactParticipation(model1,  art1);
			added = ((ModelParticipationFactory)Factories.getFactory(FactoryEnumType.MODELPARTICIPATION)).add(artPart1);

			ArtifactType art2 = ((ArtifactFactory)Factories.getFactory(FactoryEnumType.ARTIFACT)).newArtifact(testUser, dir.getId());
			art2.setArtifactType(ArtifactEnumType.DEPENDENCY);
			art2.setName(UUID.randomUUID().toString());
			((ArtifactFactory)Factories.getFactory(FactoryEnumType.ARTIFACT)).add(art2);
			art2 = ((ArtifactFactory)Factories.getFactory(FactoryEnumType.ARTIFACT)).getByNameInGroup(art2.getName(), dir);
			ArtifactParticipantType artPart2 = ((ModelParticipationFactory)Factories.getFactory(FactoryEnumType.MODELPARTICIPATION)).newDependencyParticipation(model1,  art2);
			added = ((ModelParticipationFactory)Factories.getFactory(FactoryEnumType.MODELPARTICIPATION)).add(artPart2);
			
			RequirementType req1 = ((RequirementFactory)Factories.getFactory(FactoryEnumType.REQUIREMENT)).newRequirement(testUser,  dir.getId());
			req1.setName(UUID.randomUUID().toString());
			req1.setDescription("QA Desc #");
			((RequirementFactory)Factories.getFactory(FactoryEnumType.REQUIREMENT)).add(req1);
			req1 = ((RequirementFactory)Factories.getFactory(FactoryEnumType.REQUIREMENT)).getByNameInGroup(req1.getName(), dir);
			RequirementParticipantType reqPart1 = ((ModelParticipationFactory)Factories.getFactory(FactoryEnumType.MODELPARTICIPATION)).newRequirementParticipation(model1, req1);
			added = ((ModelParticipationFactory)Factories.getFactory(FactoryEnumType.MODELPARTICIPATION)).add(reqPart1);
			
			CaseType case1 = ((CaseFactory)Factories.getFactory(FactoryEnumType.CASE)).newCase(testUser,  dir.getId());
			case1.setName(UUID.randomUUID().toString());
			case1.setDescription("Example use case");
			case1.setCaseType(CaseEnumType.USE);
			((CaseFactory)Factories.getFactory(FactoryEnumType.CASE)).add(case1);
			case1 = ((CaseFactory)Factories.getFactory(FactoryEnumType.CASE)).getByNameInGroup(case1.getName(), dir);
			CaseParticipantType casePart1 = ((ModelParticipationFactory)Factories.getFactory(FactoryEnumType.MODELPARTICIPATION)).newCaseParticipation(model1,  case1);
			added = ((ModelParticipationFactory)Factories.getFactory(FactoryEnumType.MODELPARTICIPATION)).add(casePart1);
			
			((ModelFactory)Factories.getFactory(FactoryEnumType.MODEL)).populate(model1);
			((ModelFactory)Factories.getFactory(FactoryEnumType.MODEL)).populate(model2);
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
		} catch (ArgumentException e) {
			
			logger.error(e.getMessage());
		} 
		logger.info("Model: " + model1.getName());
		logger.info("Artifacts: " + model1.getArtifacts().size());
		logger.info("Dependencies: " + model1.getDependencies().size());
		logger.info("Requirements: " + model1.getRequirements().size());
		logger.info("Cases: " + model1.getCases().size());
		logger.info("Models: " + model1.getModels().size());
		assertTrue("Parts not added", added);
	}
	
}