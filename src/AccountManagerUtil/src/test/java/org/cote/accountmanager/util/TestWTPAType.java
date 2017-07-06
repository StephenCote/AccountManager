package org.cote.accountmanager.util;

import static org.junit.Assert.assertTrue;

import java.security.Security;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.beans.SecurityBean;
import org.cote.accountmanager.factory.SecurityFactory;
import org.cote.accountmanager.objects.WTPAType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

public class TestWTPAType {
	public static final Logger logger = LogManager.getLogger(TestWTPAType.class);
	@Before
	public void setUp() throws Exception {
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void testDecryptRemoteValue(){
		String key_source = "<?xml version=\"1.0\"?><SecurityManager><public><key><![CDATA[PFJTQUtleVZhbHVlPjxNb2R1bHVzPm5kM2xxMUszVWk1SHc5bnZ0YVd3MVp1eU1sUkJYZHkvVGNWZ0VQaUxzZThwMmRoN3VPUDg4VGVyUjc5NWFwTXJEYmFOQTJleW5ndk9seDUrYWtpWmdMTGtJYTVZVXlvMFZmVnZ3UnU4dm5lb2NSaUluUUIyTUY3THhhUjdRckkxclhIaGhiaHFtaW1wZnYxaE1vbmV3cmZ2WkJIZGt4N3BSLzNCK0FuVGRZaz08L01vZHVsdXM+PEV4cG9uZW50PkFRQUI8L0V4cG9uZW50PjwvUlNBS2V5VmFsdWU+]]></key></public><private><key><![CDATA[PFJTQUtleVZhbHVlPjxNb2R1bHVzPm5kM2xxMUszVWk1SHc5bnZ0YVd3MVp1eU1sUkJYZHkvVGNWZ0VQaUxzZThwMmRoN3VPUDg4VGVyUjc5NWFwTXJEYmFOQTJleW5ndk9seDUrYWtpWmdMTGtJYTVZVXlvMFZmVnZ3UnU4dm5lb2NSaUluUUIyTUY3THhhUjdRckkxclhIaGhiaHFtaW1wZnYxaE1vbmV3cmZ2WkJIZGt4N3BSLzNCK0FuVGRZaz08L01vZHVsdXM+PEV4cG9uZW50PkFRQUI8L0V4cG9uZW50PjxQPnlTaE1mRWd2Z21tRHgrYkxoSHdXUUtqQXRTQVdlWnJrU2xESUN0OU1hdTlBWmlJOXJ1Z2ZzcG5ueDZaNnRMWEFTWjNKc2xmUXVtNmZoWmFqOTRBbzhRPT08L1A+PFE+eU9nbGFIZjI2amN2dE9RVXlwQ0E2c3JXSVYzbUVEd3lwcjF0Uk5OUU83cGxxTEUvVmdGMnpVWnlxSm1GQnhYMnh2MHZiaTlVS0ZyM3BmOXlyWGJXR1E9PTwvUT48RFA+R0U4MmJ3NktMMGh4RklkZnNQTU4vV0puWjN3cE95anN6YzVWWG5yOTBTNTRxZDhaZFRtNEd1MWVoVklwSWcyVTMxQ2lQMXM5YmtwUUhPVEhpL0dCQVE9PTwvRFA+PERRPmRxeHFMR053ZnJsS2ZOZWRVR283UEhYRU5zRjRmRzZTbk51WUIrZXFwUjFkbjEvVHdjSHJveVhSNUxXS1ZyMHFvREErTEIvWTNsMmRtM2hoRFFYOVFRPT08L0RRPjxJbnZlcnNlUT51d1l1UkJ2bGlrK3h0dG1TL3V0WTZKOHV0Qnh6cWZpRGRvdXlpelBzSStuWCtMbDNiNUQ0VE13Z2VQV2I5UXVKMXVWUmhITXFCSk9FdXpmWElqdE5NQT09PC9JbnZlcnNlUT48RD5kUWNWQmU4NHRQUllBUWtqV1U0dUMvdnltcnE1Qm1McGNqYTZJM3FNM0dnR1oxYkRTT25DRGZPTnhvOWI2N1NUZXdQei95MDFUVkpGWU9PYkpTRVNvUnB0c3VmSUpvcVcwaCtMWXBaNmszYzd5dnlnTWhvZGtWVTM3TEhkV0xKQXFRMDJUeDc2cWdPSWQ4OGphQzA2RXk2Rk53NFhXM3E0cDNUMlVJMWJuWUU9PC9EPjwvUlNBS2V5VmFsdWU+]]></key></private><cipher><key><![CDATA[vbTSKZ7ss0AQkC6BFYR/vg==]]></key><iv><![CDATA[BvuxGsOWgFnRGBlbfhDbUQ==]]></iv></cipher></SecurityManager>";
		String test_enc = "eJIljc/cXTFj0dAvggZV5SG/ULb2gUfnicisOZDQG7245KLS+YSAdThBZGRzJZz4n0ms9cHnfA0xVuWstIsbZki8rC73WMbKdBmpj+q9BFIx9cU33lb+AxGaMQJpLqLOEzcWc4RE+CHoah5/MsiM9lOGZc51tDA8sCcGvRDbLSNAkZGy8Fy/KOCvNqJMUTQ2KsQSiJeSz7CXYSkZoiU2280dNcy/JlbAeB8xg1J114ccWfqcKDXtDzw0b+ydRb9QSbBk+66f48bHgvillUVQdVJwB2DUMD5l4Jv1pUbDgt3U8EItY3gjcwy8d4zRkRiNJsQrVF4XJSy4sRSXLVD2jFYnUdgY8ttO6/74cg+SA/kzHxA1BKKptxASAI3NQIi4dGSGuvCMpLWEuh3jdPrpRBZUZXxpRTYmRSlAoLcafm+Fdc3HfEWrDFGj4xYG8zlXKiTmvxirmdvaVlHNnrW7COOahYs0EtLh6ouEB6AAgTZrHKCCOwifsoROkQgaM69l8fmxFwA2VVt6q+bBXCJqbNYNlDTPUeWERTvySbbRvN1Q7okc45AEGi32YRwDL3UD"; 
			//"2EfD+suF+QoOYC5+eW4c9S19mpAa5LFU9Yv4acWVQwUJC8y3W2eUcZpqUNAian9bj0lZxVxUKkN9TUbWm+qN9mksRO+32bjYj50hIvRKgmtgO2MN3WIcG2eYwQTcHaIESlSSB5YwNElXXrFcsqJSrnEpEyQvHcKGQJ+K3V7gxAueRPAtqvKe4JahO/xXW1zxRwPnk/kPgDoUz2Vqa24zxj7vycAZG1Re3OKremFNj6g9UXwaN0hsoc5W+nciIsaYbQ9k6PIQM2qjqYUrUa5nUjtGRvNvdZLokbyFAmzzYUK291QTpeM227+4eX5mGv/dEet+sTZYdlq8gRhjVZXOgzKpknsezGyrVmDpN5BLHyfJofYEip7viPGeuSmo0GEHC2T4TO1NlzlXgX3jpxtqz4XXv2645O5iHRusMYaH2HqshFEgRRNyEOEGNVcDgBU1fN6fSyaaFhxzncTNkrLet6KlKZlv31l0nDQTP3RiKj+TUekWVmhFEl9/Wv//0Ed9tTBSdMiJFoJtG9hiBVo7TXRTF37+IYoaaw4G1GmXNC1Rx9rZrJgOtqywMYXOqOFhw8fR1dpUZFbAvOYwdHxTIg=="; 
			/// "hQn6f6C1gsq0wRqxNeaJdT1OK5167VqiA8X4p1S7miVoeFz3Bj6Od8EuHvFjDYdpLlzc/sT8WqXT0X79rseWKMavYox76D3ogIC66ss+ng5yGPfuzhrtMUw/1TageFqsWytYBsWn33iZYHiutsUllcGWJZgomVYtOza686ikGtTdrLSQp/Nwy1/R0SXJs5mFxzkxw//6KYQYDyqQWZWiL3/NLf+Wy57l+vzR5Ym/iceIjnSt2pL/QVWGVEtljro2z9VCPYVvMNVzrsSDxNY0yoABFl6JblsFZVnr3bNHtMTZovehSiE2CajRP8rzbIupAdmGEod1/j1KkwHvBug8+1HflBdqap0jeDt4vJvi3yPTEZ0p+6LosV3ogtQmNTulwIu/XnWXJZIz9bQv+2xKTNEzrLyQjozL8jRwE1oAg6GEZtB9PbX+9DT/vG7CUbdwDo3IKCMsWzRK/ii8FzNljg5YzQVyyWtmgtj5zyZ9ozMz0SCVwCkFVr8bPSfX31Dp+EqpEy5VjGvhKOR9h/ac6Q=="; 
		SecurityFactory sf = SecurityFactory.getSecurityFactory();
		SecurityBean bean = sf.createSecurityBean(key_source.getBytes(), false);
		//byte[] test_data = BinaryUtil.fromBase64(test_enc.getBytes());

		//byte[] test_dec = SecurityUtil.decipher(bean,  test_data);
		WTPAType wtpa = WTPAUtil.decipherWTPA(bean, test_enc);
		assertTrue("WTPAType is null", wtpa != null);
		///logger.info(WTPAUtil.serializeToken(wtpa));
		
		//logger.info("Deciphered data='" + (new String(test_dec)));
		/*
		assertTrue("Unable to decipher external data", test_dec.length != 0);
		VTPA vtpaToken = new VTPA();
		boolean parsed = vtpaToken.parseToken(new String(test_dec));		
		assertTrue("Unable to decode VTPA token", parsed);
		*/
	}
	
	@Test
	public void testDecryptRemoteValueWithDoubleSerial(){
		String key_source = "<?xml version=\"1.0\"?><SecurityManager><public><key><![CDATA[PFJTQUtleVZhbHVlPjxNb2R1bHVzPm5kM2xxMUszVWk1SHc5bnZ0YVd3MVp1eU1sUkJYZHkvVGNWZ0VQaUxzZThwMmRoN3VPUDg4VGVyUjc5NWFwTXJEYmFOQTJleW5ndk9seDUrYWtpWmdMTGtJYTVZVXlvMFZmVnZ3UnU4dm5lb2NSaUluUUIyTUY3THhhUjdRckkxclhIaGhiaHFtaW1wZnYxaE1vbmV3cmZ2WkJIZGt4N3BSLzNCK0FuVGRZaz08L01vZHVsdXM+PEV4cG9uZW50PkFRQUI8L0V4cG9uZW50PjwvUlNBS2V5VmFsdWU+]]></key></public><private><key><![CDATA[PFJTQUtleVZhbHVlPjxNb2R1bHVzPm5kM2xxMUszVWk1SHc5bnZ0YVd3MVp1eU1sUkJYZHkvVGNWZ0VQaUxzZThwMmRoN3VPUDg4VGVyUjc5NWFwTXJEYmFOQTJleW5ndk9seDUrYWtpWmdMTGtJYTVZVXlvMFZmVnZ3UnU4dm5lb2NSaUluUUIyTUY3THhhUjdRckkxclhIaGhiaHFtaW1wZnYxaE1vbmV3cmZ2WkJIZGt4N3BSLzNCK0FuVGRZaz08L01vZHVsdXM+PEV4cG9uZW50PkFRQUI8L0V4cG9uZW50PjxQPnlTaE1mRWd2Z21tRHgrYkxoSHdXUUtqQXRTQVdlWnJrU2xESUN0OU1hdTlBWmlJOXJ1Z2ZzcG5ueDZaNnRMWEFTWjNKc2xmUXVtNmZoWmFqOTRBbzhRPT08L1A+PFE+eU9nbGFIZjI2amN2dE9RVXlwQ0E2c3JXSVYzbUVEd3lwcjF0Uk5OUU83cGxxTEUvVmdGMnpVWnlxSm1GQnhYMnh2MHZiaTlVS0ZyM3BmOXlyWGJXR1E9PTwvUT48RFA+R0U4MmJ3NktMMGh4RklkZnNQTU4vV0puWjN3cE95anN6YzVWWG5yOTBTNTRxZDhaZFRtNEd1MWVoVklwSWcyVTMxQ2lQMXM5YmtwUUhPVEhpL0dCQVE9PTwvRFA+PERRPmRxeHFMR053ZnJsS2ZOZWRVR283UEhYRU5zRjRmRzZTbk51WUIrZXFwUjFkbjEvVHdjSHJveVhSNUxXS1ZyMHFvREErTEIvWTNsMmRtM2hoRFFYOVFRPT08L0RRPjxJbnZlcnNlUT51d1l1UkJ2bGlrK3h0dG1TL3V0WTZKOHV0Qnh6cWZpRGRvdXlpelBzSStuWCtMbDNiNUQ0VE13Z2VQV2I5UXVKMXVWUmhITXFCSk9FdXpmWElqdE5NQT09PC9JbnZlcnNlUT48RD5kUWNWQmU4NHRQUllBUWtqV1U0dUMvdnltcnE1Qm1McGNqYTZJM3FNM0dnR1oxYkRTT25DRGZPTnhvOWI2N1NUZXdQei95MDFUVkpGWU9PYkpTRVNvUnB0c3VmSUpvcVcwaCtMWXBaNmszYzd5dnlnTWhvZGtWVTM3TEhkV0xKQXFRMDJUeDc2cWdPSWQ4OGphQzA2RXk2Rk53NFhXM3E0cDNUMlVJMWJuWUU9PC9EPjwvUlNBS2V5VmFsdWU+]]></key></private><cipher><key><![CDATA[vbTSKZ7ss0AQkC6BFYR/vg==]]></key><iv><![CDATA[BvuxGsOWgFnRGBlbfhDbUQ==]]></iv></cipher></SecurityManager>";
		String test_enc = "eJIljc/cXTFj0dAvggZV5SG/ULb2gUfnicisOZDQG7245KLS+YSAdThBZGRzJZz4n0ms9cHnfA0xVuWstIsbZki8rC73WMbKdBmpj+q9BFIx9cU33lb+AxGaMQJpLqLOEzcWc4RE+CHoah5/MsiM9lOGZc51tDA8sCcGvRDbLSNAkZGy8Fy/KOCvNqJMUTQ2KsQSiJeSz7CXYSkZoiU2280dNcy/JlbAeB8xg1J114ccWfqcKDXtDzw0b+ydRb9QSbBk+66f48bHgvillUVQdVJwB2DUMD5l4Jv1pUbDgt3U8EItY3gjcwy8d4zRkRiNJsQrVF4XJSy4sRSXLVD2jFYnUdgY8ttO6/74cg+SA/kzHxA1BKKptxASAI3NQIi4dGSGuvCMpLWEuh3jdPrpRBZUZXxpRTYmRSlAoLcafm+Fdc3HfEWrDFGj4xYG8zlXKiTmvxirmdvaVlHNnrW7COOahYs0EtLh6ouEB6AAgTZrHKCCOwifsoROkQgaM69l8fmxFwA2VVt6q+bBXCJqbNYNlDTPUeWERTvySbbRvN1Q7okc45AEGi32YRwDL3UD"; 

		Document d = XmlUtil.GetDocumentFromBytes(key_source.getBytes());
		assertTrue("Serial document is null", d != null);
		String pubKey = XmlUtil.FindElementText(d.getDocumentElement(), "public", "key");
		assertTrue("Public key not found", pubKey != null);
		//logger.info("Public key = " + BinaryUtil.fromBase64Str(pubKey.getBytes()));
		String priKey = XmlUtil.FindElementText(d.getDocumentElement(), "private", "key");
		assertTrue("Private key not found", priKey != null);
		//logger.info("Private key = " + BinaryUtil.fromBase64Str(priKey.getBytes()));
		String cipKey = XmlUtil.FindElementText(d.getDocumentElement(), "cipher", "key");
		String cipIv = XmlUtil.FindElementText(d.getDocumentElement(), "cipher", "iv");
		assertTrue("Cipher IV not found", cipIv != null);
		assertTrue("Cipher Key not found", cipKey != null);
		
		
		SecurityFactory sf = SecurityFactory.getSecurityFactory();
		SecurityBean bean = sf.createSecurityBean(key_source.getBytes(), false);
		assertTrue("Public key is null or empty", (bean.getPublicKeyBytes() != null && bean.getPublicKeyBytes().length > 0));
		assertTrue("Private key is null or empty", (bean.getPrivateKeyBytes() != null && bean.getPrivateKeyBytes().length  > 0));
		assertTrue("Cipher key is null or empty", (bean.getCipherKey() != null && bean.getCipherKey().length > 0));
		assertTrue("Cipher IV is null or empty", (bean.getCipherIV() != null && bean.getCipherIV().length > 0));
		String serial = SecurityUtil.serializeToXml(bean, true, true, true);
		///logger.info("Java Key = " + serial);
		bean = sf.createSecurityBean(serial.getBytes(), false);
		
		byte[] test_data = BinaryUtil.fromBase64(test_enc.getBytes());
		byte[] test_dec = SecurityUtil.decipher(bean, test_data);
		assertTrue("Test data is null or empty",test_dec != null && test_dec.length > 0);

		WTPAType wtpa = WTPAUtil.decipherWTPA(bean, test_enc);
		assertTrue("WTPAType is null", wtpa != null);
		logger.info(WTPAUtil.serializeToken(wtpa));
		
		//logger.info("Deciphered data='" + (new String(test_dec)));
		/*
		assertTrue("Unable to decipher external data", test_dec.length != 0);
		VTPA vtpaToken = new VTPA();
		boolean parsed = vtpaToken.parseToken(new String(test_dec));		
		assertTrue("Unable to decode VTPA token", parsed);
		*/
	}
}
