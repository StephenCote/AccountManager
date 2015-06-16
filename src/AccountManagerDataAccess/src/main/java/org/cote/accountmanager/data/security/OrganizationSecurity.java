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
package org.cote.accountmanager.data.security;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cote.accountmanager.beans.SecurityBean;
import org.cote.accountmanager.data.ConnectionFactory;
import org.cote.accountmanager.data.DBFactory;
import org.cote.accountmanager.data.DBFactory.CONNECTION_TYPE;
import org.cote.accountmanager.factory.SecurityFactory;
import org.cote.accountmanager.objects.OrganizationType;

public class OrganizationSecurity {
	
	//// TODO: I don't like the general setup of organization-level key persistence since it pretty much makes it only good for a single set of organization-level ciphers and keys.
	/// The general strategy for user-owned keys is to use DataType objects that are enciphered with the organization level keys
	/// This is captured in the original .NET Vault implementation
	/// The SessionSecurity implementation uses the spool to cache by-session cipher sets, but the implementation is still somewhat dirty on deciphering/validating
	///
	private static Map<Integer,SecurityBean> securityBeanMap = Collections.synchronizedMap(new HashMap<Integer,SecurityBean>());
	public static void clearCache(){
		securityBeanMap.clear();
	}
	
	/// 2013/09/14 - TODO: The first id lookup is unnecessary and should be a single query
	///
	public static SecurityBean getSecurityBean(OrganizationType organization)
	{
		SecurityBean out_bean = new SecurityBean();
		if (securityBeanMap.containsKey(organization.getId())) return securityBeanMap.get(organization.getId());

		
		Connection connection = ConnectionFactory.getInstance().getConnection();
		CONNECTION_TYPE connectionType = DBFactory.getConnectionType(connection);
		
		String nolock_hint = DBFactory.getNoLockHint(connectionType);

		List<Long> asym_ids = getAsymmetricKeyIdsForOrganization(connection, organization);
		List<Long> sym_ids = getSymmetricKeyIdsForOrganization(connection, organization);

		if (asym_ids.size() == 0 || sym_ids.size() == 0)
		{
			try {
				connection.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
		long dsa_id = asym_ids.get(0).intValue();
		long des_id = sym_ids.get(0).intValue();

		//System.out.println(dsa_id + ":" + des_id);
		
		byte[] des_key = new byte[0];
		byte[] des_iv = new byte[0];
		byte[] private_key = new byte[0];
		byte[] public_key = new byte[0];

		String sql_des = "SELECT cipherkey, cipheriv, asymmetrickeyid FROM symmetrickeys " + nolock_hint + " WHERE id = " + des_id;
		try
		{
			Statement statement = connection.createStatement();
			ResultSet rset = statement.executeQuery(sql_des);
			if (rset.next())
			{
				des_key = rset.getBytes(1);
				des_iv = rset.getBytes(2);
				dsa_id = rset.getLong(3);
			}
			rset.close();
		}
		catch (SQLException sqe)
		{
			sqe.printStackTrace();
			System.out.println(sqe.getMessage());
		}

		String sql_dsa = "SELECT publickey, privatekey FROM asymmetrickeys " + nolock_hint + " WHERE id = " + dsa_id;
		try
		{
			Statement statement = connection.createStatement();
			ResultSet rset = statement.executeQuery(sql_dsa);

			if (rset.next())
			{
				public_key = rset.getBytes(1);
				private_key = rset.getBytes(2);
				
			}
			rset.close();
		}
		catch (SQLException sqe)
		{
			sqe.printStackTrace();
			System.out.println(sqe.getMessage());
		}
		finally{
			try {
				connection.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		///System.out.println(new String(public_key));
		SecurityFactory sf = SecurityFactory.getSecurityFactory();
		sf.setPublicKey(out_bean,  public_key);
		sf.setPrivateKey(out_bean, private_key);
		sf.setSecretKey(out_bean, des_key, des_iv, true);

		return out_bean;
	}
	
	private static List<Long> getSymmetricKeyIdsForOrganization(Connection connection, OrganizationType organization){
		return getKeyIdsForOrganization(connection, organization, "symmetrickeys");
	}
	private static List<Long> getAsymmetricKeyIdsForOrganization(Connection connection, OrganizationType organization){
		return getKeyIdsForOrganization(connection, organization, "asymmetrickeys");
	}

	private static List<Long> getKeyIdsForOrganization(Connection connection, OrganizationType organization, String tableName){
		
		List<Long> keys = new ArrayList<Long>();

		String nolock_hint = DBFactory.getNoLockHint(DBFactory.getConnectionType(connection));

		try
		{
			String sql = "SELECT id FROM " + tableName + " " + nolock_hint + " WHERE organizationid = ?";
			PreparedStatement statement = connection.prepareStatement(sql);
			statement.setLong(1, organization.getId());
			ResultSet rset = statement.executeQuery();

			while (rset.next())
			{
				keys.add(rset.getLong(1));
			}
			rset.close();

		}
		catch (SQLException sqe)
		{
			sqe.printStackTrace();
			System.out.println(sqe.getMessage());
		}

		return keys;
	}
	public static boolean deleteSecurityKeys(OrganizationType organization)
	{
		boolean ret = false;
		Connection connection = ConnectionFactory.getInstance().getConnection();
		try
		{
			int deleted = 0;
			String token = DBFactory.getParamToken(DBFactory.getConnectionType(connection));
			PreparedStatement statement_1 = connection.prepareStatement("DELETE FROM asymmetrickeys WHERE organizationid = " + token);
			PreparedStatement statement_2 = connection.prepareStatement("DELETE FROM symmetrickeys WHERE organizationid = " + token);
			statement_1.setLong(1, organization.getId());
			statement_2.setLong(1, organization.getId());
			deleted = statement_1.executeUpdate();
			deleted += statement_2.executeUpdate();
			if (deleted > 0 && securityBeanMap.containsKey(organization.getId()))
			{
				securityBeanMap.remove(organization.getId());
			}
			ret = (deleted > 0);

		}
		catch (SQLException e)
		{
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
		finally{
			try {
				connection.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return ret;

	}
	public static boolean generateSecurityKeys(OrganizationType organization)
	{
		boolean ret = false;
		if (organization == null || organization.getId() < 0) return ret;

		Connection connection = ConnectionFactory.getInstance().getConnection();


		List<Long> key_ids = getSymmetricKeyIdsForOrganization(connection, organization);
		if (key_ids.size() == 0)
		{
			ret = addDefaultSecurityKeys(connection, organization);
		}
		else
		{
			System.out.println("Primary keys already generated for organization " + organization.getName());
			///Core.ApplicationContext.GetInstance().LogWarning("Primary keys already generated for organization " + organization + ". Use OrganizationSecurity.addSecurityKey");
		}

		try {
			connection.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return ret;
	}

	private static boolean addDefaultSecurityKeys(Connection connection, OrganizationType organization)
	{
		boolean ret = false;
		SecurityFactory sf = SecurityFactory.getSecurityFactory();
		SecurityBean securityBean = new SecurityBean();
		securityBean.setEncryptCipherKey(true);
		sf.generateKeyPair(securityBean);
		sf.generateSecretKey(securityBean);

		
		/* get DES Key and IV encrypted with RSA key */
		byte[] encrypted_des_key = securityBean.getEncryptedCipherKey();
		byte[] encrypted_des_iv = securityBean.getEncryptedCipherIV();

		byte[] private_key = securityBean.getPrivateKeyBytes();
		byte[] public_key = securityBean.getPublicKeyBytes();

		String param_token = DBFactory.getParamToken(DBFactory.getConnectionType(connection));

		///DataTable symTable = DBFactory.getDataTable(connection, "symmetrickeys");

		/* encipher org private DSA key with product des key */
		//byte[] enc_private_key = Default.SecurityManager.Encipher(private_key);

		/* since the private key is encrypted with the product des key ,the des_key field is left out as it should be 0; alternately, it could be set to the record id of the des key used to encrypt the private key */
		String sql = "INSERT INTO asymmetrickeys ("
			+ "organizationid"
			+ ",publickey"
			+ ",privatekey"
			+ ") VALUES ("
			+ organization.getId()
			+ "," + param_token + "," + param_token
			+ ")"
		;

		try
		{

			PreparedStatement statement = connection.prepareStatement(sql);
			statement.setBytes(1, public_key);
			statement.setBytes(2, private_key);
			statement.executeUpdate();
		}
		catch (SQLException sqe)
		{
			sqe.printStackTrace();
			System.out.println(sqe.getMessage());
		}

		List<Long> key_ids = getAsymmetricKeyIdsForOrganization(connection, organization);
		if (key_ids.size() == 0)
		{
			///Core.ApplicationContext.GetInstance().LogError("OrganizationSecurity:: addDefaultSecurityKey: Failed to retrieve record id for DSA keys");
			return false;
		}

		int dsa_key = key_ids.get(0).intValue();

		sql = "INSERT INTO symmetrickeys ("
			+ "organizationid"
			+ ",cipherkey,cipheriv"
			+ ",asymmetrickeyid"
			+ ") VALUES ("
			+ organization.getId()
			+ "," + param_token + "," + param_token
			+ "," + dsa_key
			+ ")"
		;
		try
		{
			PreparedStatement statement = connection.prepareStatement(sql);
			statement.setBytes(1, encrypted_des_key);
			statement.setBytes(2, encrypted_des_iv);
			statement.executeUpdate();
			ret = true;
		}
		catch (Exception sqe)
		{
			sqe.printStackTrace();
			System.out.println(sqe.getMessage());
		}




		return ret;

	}

}
