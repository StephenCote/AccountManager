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
package org.cote.util;

import java.io.UnsupportedEncodingException;

import org.cote.accountmanager.beans.SecurityBean;
import org.cote.accountmanager.data.security.OrganizationSecurity;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.util.BinaryUtil;
import org.cote.accountmanager.util.SecurityUtil;

public class OrganizationSecurityUtil {
	public static String encipherString(String inStr, OrganizationType org){
		return BinaryUtil.toBase64Str(encipherBytes(inStr.getBytes(), org));
	}
	public static String decipherString(String inStr, OrganizationType org){
		String out_str = null;
		try{
			out_str = new String(decipherBytes(BinaryUtil.fromBase64(inStr.getBytes()), org),"UTF-8");
		}
		catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return out_str;
	}
	public static byte[] encipherBytes(byte[] inBytes, OrganizationType org){
		SecurityBean secBean = OrganizationSecurity.getSecurityBean(org);
		return SecurityUtil.encipher(secBean, inBytes);
	}
	public static byte[] decipherBytes(byte[] inBytes, OrganizationType org){
		SecurityBean secBean = OrganizationSecurity.getSecurityBean(org);
		return SecurityUtil.decipher(secBean, inBytes);
	}
}
