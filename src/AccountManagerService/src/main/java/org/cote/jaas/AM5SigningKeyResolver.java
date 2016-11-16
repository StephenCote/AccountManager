package org.cote.jaas;

import java.security.Key;

import org.cote.accountmanager.beans.SecurityBean;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.factory.INameIdFactory;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.FactoryEnumType;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.SigningKeyResolver;

public class AM5SigningKeyResolver implements SigningKeyResolver{

	@Override
	public Key resolveSigningKey(JwsHeader arg0, Claims arg1) {
		// TODO Auto-generated method stub
		String urn = arg1.getId();
		Key key = null;
		if(urn != null){
			INameIdFactory iFact = Factories.getFactory(FactoryEnumType.USER);
			UserType user = iFact.getByUrn(urn);
			if(user != null){
				SecurityBean bean = TokenUtil.getJWTSecurityBean(user);
				if(bean != null && bean.getSecretKey() != null){
					key = bean.getSecretKey();
				}
			}
		}
		return key;
	}

	@Override
	public Key resolveSigningKey(JwsHeader arg0, String arg1) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
