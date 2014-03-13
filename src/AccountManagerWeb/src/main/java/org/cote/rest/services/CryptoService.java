package org.cote.rest.services;


import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;
import org.cote.accountmanager.beans.SecurityBean;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.data.services.SessionSecurity;
import org.cote.accountmanager.factory.SecurityFactory;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.SecuritySpoolType;
import org.cote.accountmanager.objects.UserSessionType;
import org.cote.accountmanager.util.BinaryUtil;
import org.cote.accountmanager.util.ServiceUtil;
import org.cote.beans.CryptoBean;
import org.cote.beans.SchemaBean;
import org.cote.rest.schema.ServiceSchemaBuilder;

import java.util.ArrayList;
import java.util.List;


@Path("/crypto")
public class CryptoService{

	public static final Logger logger = Logger.getLogger(CryptoService.class.getName());
	private static SchemaBean schemaBean = null;
	
	public CryptoService(){
		//JSONConfiguration.mapped().rootUnwrapping(false).build();
	}
	
	private CryptoBean getCryptoBean(SecurityBean bean,String guid){
		CryptoBean cBean = new CryptoBean();
		cBean.setSpoolId(guid);
		cBean.setCipherIV(bean.getCipherIV());
		cBean.setCipherKey(bean.getCipherKey());
		cBean.setCipherKeySpec(bean.getCipherKeySpec());
		cBean.setCipherProvider(bean.getCipherProvider());
		cBean.setSymetricCipherKeySpec(bean.getSymetricCipherKeySpec());
		return cBean;
	}

	@GET @Path("/getKeyRing") @Produces(MediaType.APPLICATION_JSON)
	public CryptoBean[] getKeyRing(@Context HttpServletRequest request){

		String sessionId = request.getSession(true).getId();
		UserSessionType session = null;
		SecuritySpoolType[] tokens = new SecuritySpoolType[0];
		List<CryptoBean> secs = new ArrayList<CryptoBean>();
		OrganizationType org = ServiceUtil.getOrganizationFromRequest(request);
		try {
			session = SessionSecurity.getUserSession(sessionId, org);
			tokens = Factories.getSecurityTokenFactory().getSecurityTokens(sessionId, org);
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage()); 
			e.printStackTrace();
			
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage()); 
			e.printStackTrace();
		}
		
		if(tokens.length == 0){
			
			for(int i = 0; i < tokens.length; i++){
				String[] pairs = tokens[i].getData().split("&&");
				SecurityBean bean = new SecurityBean();
				SecurityFactory.getSecurityFactory().setSecretKey(bean, BinaryUtil.fromBase64(pairs[0].getBytes()), BinaryUtil.fromBase64(pairs[1].getBytes()), false);
				secs.add(getCryptoBean(bean,tokens[i].getGuid()));
			}
		}
		else{
			try{
				List<SecuritySpoolType> tokenTypes = new ArrayList<SecuritySpoolType>();
				for(int i = 0; i < 10;i++){
					SecurityBean bean = new SecurityBean();
					SecurityFactory.getSecurityFactory().generateSecretKey(bean);
					SecuritySpoolType tokenType = Factories.getSecurityTokenFactory().newSecurityToken(sessionId, org);
					secs.add(getCryptoBean(bean,tokenType.getGuid()));
					
					tokenType.setOwnerId(session.getUserId());
					tokenType.setData(BinaryUtil.toBase64Str(bean.getCipherKey()) + "&&" + BinaryUtil.toBase64Str(bean.getCipherIV()));
					tokenTypes.add(tokenType);
				}
				if(Factories.getSecurityTokenFactory().addSecurityTokens(tokenTypes.toArray(new SecuritySpoolType[0])) == false){
					logger.error("Failed to persist tokens");
					secs.clear();
				}
			}
			catch(FactoryException fe){
				fe.printStackTrace();
				logger.error(fe.getMessage()); 
				secs.clear();
			}
		}
		
		///System.out.println(request.getSession(true).getId());

		return secs.toArray(new CryptoBean[0]);
	}
	
	 @GET @Path("/smd") @Produces(MediaType.APPLICATION_JSON)
	 public SchemaBean getSmdSchema(@Context UriInfo uri){
		 if(schemaBean != null) return schemaBean;
		 schemaBean = ServiceSchemaBuilder.modelRESTService(this.getClass(),uri.getAbsolutePath().getRawPath().replaceAll("/smd$", ""));
		 return schemaBean;
	 }
}