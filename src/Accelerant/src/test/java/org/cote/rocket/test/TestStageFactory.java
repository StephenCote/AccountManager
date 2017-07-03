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
package org.cote.rocket.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.propellant.objects.StageType;
import org.junit.Test;

public class TestStageFactory extends BaseAccelerantTest{
	public static final Logger logger = LogManager.getLogger(TestStageFactory.class);
	public static long StageId = 0;
	@Test
	public void TestNewStage(){
		StageType Stage = newStage(UUID.randomUUID().toString());
		assertNotNull("Stage is null", Stage);
		StageId = Stage.getId();
		logger.info("Stage '" + Stage.getName() + "' id is " + StageId);
	}
	@Test
	public void TestGetStage(){
		StageType Stage = getStage(StageId);
		assertNotNull("Stage is null", Stage);
	}
	@Test
	public void TestUpdateStage(){
		StageType Stage = getStage(StageId);
		Stage.setBudget(newBudget(Stage.getName()));
		Stage.setMethodology(newMethodology(Stage.getName()));
		Stage.setWork(newWork(Stage.getName()));
		Stage.setDescription("New desc");
		boolean update = updateStage(Stage);
		assertTrue("Stage was not updated", update);
	}
}