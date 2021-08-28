package org.cote.accountmanager.data;

import static org.junit.Assert.assertNotNull;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import javax.crypto.KeyAgreement;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import org.cote.accountmanager.beans.SecurityBean;
import org.cote.accountmanager.data.security.KeyService;
import org.cote.accountmanager.exceptions.ArgumentException;
import org.cote.accountmanager.factory.SecurityFactory;
import org.cote.accountmanager.util.JSONUtil;
import org.cote.accountmanager.util.SecurityUtil;
import org.junit.Test;

public class TestCurveEncrypt extends BaseDataAccessTest {

	@Test
	public void TestCreateCurve() {
		SecurityBean bean = new SecurityBean();
		bean.setAsymmetricCipherKeySpec("ECIES");
		bean.setKeyAgreementSpec("ECDH");
		bean.setCipherKeySpec("AES/GCM/NoPadding");
		bean.setHashProvider("SHA256withECDSA");
		bean.setCurveName("secp256r1");
		SecurityBean key = null;
		try {
			key = KeyService.newPersonalAsymmetricKey(testUser, bean, false);
		} catch (ArgumentException e) {
			logger.error("Failed to create key");
		}
		assertNotNull("Key is null", key);
		logger.info("New key: " + key.getObjectId());
	}

}
