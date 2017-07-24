/*******************************************************************************
 * Copyright (C) 2002, 2017 Stephen Cote Enterprises, LLC. All rights reserved.
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
package org.cote.accountmanager.data.factory;

import org.cote.accountmanager.objects.UserSessionDataType;
import org.cote.accountmanager.objects.UserSessionType;

public class SessionDataFactory {
	public void setValue(UserSessionType session, String name, String data){
		UserSessionDataType newData = new UserSessionDataType();
		newData.setName(name);
		newData.setValue(data);
		session.getChangeSessionData().add(newData);
		int index = getIndex(session, name);
		if(index >= 0){
			session.getSessionData().remove(index);
		}
		if(data != null){
			//session.setDataSize(session.getDataSize() + 1);
			session.getSessionData().add(newData);
		}
		//else session.setDataSize(session.getDataSize() - 1);
		session.setDataSize(session.getSessionData().size());
	}
	public String getValue(UserSessionType session, String name){
		int index = getIndex(session, name);
		if(index <= -1) return null;
		return session.getSessionData().get(index).getValue();
	}
	
	private boolean hasKey(UserSessionType session, String name){
		return (getIndex(session, name) >= 0);
	}
	
	private int getIndex(UserSessionType session, String name){
		int out_index = -1;
		for(int i = session.getSessionData().size() - 1; i >= 0; i--){
			if(session.getSessionData().get(i).getName().equals(name)){
				out_index = i;
				break;
			}
		}
		return out_index;
	}
}
