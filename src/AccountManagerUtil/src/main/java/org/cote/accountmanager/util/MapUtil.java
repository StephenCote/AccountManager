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
package org.cote.accountmanager.util;

import org.cote.accountmanager.objects.BaseAuthorizationType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.types.NameEnumType;
public class MapUtil {

	public static void shallowCloneAznType(BaseAuthorizationType src, BaseAuthorizationType targ){
		//targ.setUrn(src.getUrn());
		if(src.getScore() != null) targ.setScore(src.getScore());
		if(src.getDescription() != null) targ.setDescription(src.getDescription());
		if(src.getLogicalOrder() != null) targ.setLogicalOrder(src.getLogicalOrder());
		shallowCloneNameIdDirectoryType(src, targ);
	}
	public static void shallowCloneNameIdDirectoryType(NameIdType src, NameIdType targ){
		if(src.getName() != null) targ.setName(src.getName());
		if(src.getParentId() != null) targ.setParentId(src.getParentId());
		if(src.getNameType() != null && src.getNameType() != NameEnumType.UNKNOWN) targ.setNameType(src.getNameType());
		if(src.getOrganizationId() != null) targ.setOrganizationId(src.getOrganizationId());
		if(src.getOrganizationPath() != null) targ.setOrganizationPath(src.getOrganizationPath());
		targ.getAttributes().addAll(src.getAttributes());
	}
}
