package org.cote.accountmanager.util;

import org.apache.commons.codec.binary.Base64;
import org.cote.accountmanager.beans.SecurityBean;
import org.cote.accountmanager.factory.SecurityFactory;
import org.cote.accountmanager.objects.WTPAType;


public class WTPAUtil {
	public static String PRIVATE_KEY_SOURCE = "<?xml version=\"1.0\"?><SecurityManager><public><key><![CDATA[PFJTQUtleVZhbHVlPjxNb2R1bHVzPm5kM2xxMUszVWk1SHc5bnZ0YVd3MVp1eU1sUkJYZHkvVGNWZ0VQaUxzZThwMmRoN3VPUDg4VGVyUjc5NWFwTXJEYmFOQTJleW5ndk9seDUrYWtpWmdMTGtJYTVZVXlvMFZmVnZ3UnU4dm5lb2NSaUluUUIyTUY3THhhUjdRckkxclhIaGhiaHFtaW1wZnYxaE1vbmV3cmZ2WkJIZGt4N3BSLzNCK0FuVGRZaz08L01vZHVsdXM+PEV4cG9uZW50PkFRQUI8L0V4cG9uZW50PjwvUlNBS2V5VmFsdWU+]]></key></public><private><key><![CDATA[PFJTQUtleVZhbHVlPjxNb2R1bHVzPm5kM2xxMUszVWk1SHc5bnZ0YVd3MVp1eU1sUkJYZHkvVGNWZ0VQaUxzZThwMmRoN3VPUDg4VGVyUjc5NWFwTXJEYmFOQTJleW5ndk9seDUrYWtpWmdMTGtJYTVZVXlvMFZmVnZ3UnU4dm5lb2NSaUluUUIyTUY3THhhUjdRckkxclhIaGhiaHFtaW1wZnYxaE1vbmV3cmZ2WkJIZGt4N3BSLzNCK0FuVGRZaz08L01vZHVsdXM+PEV4cG9uZW50PkFRQUI8L0V4cG9uZW50PjxQPnlTaE1mRWd2Z21tRHgrYkxoSHdXUUtqQXRTQVdlWnJrU2xESUN0OU1hdTlBWmlJOXJ1Z2ZzcG5ueDZaNnRMWEFTWjNKc2xmUXVtNmZoWmFqOTRBbzhRPT08L1A+PFE+eU9nbGFIZjI2amN2dE9RVXlwQ0E2c3JXSVYzbUVEd3lwcjF0Uk5OUU83cGxxTEUvVmdGMnpVWnlxSm1GQnhYMnh2MHZiaTlVS0ZyM3BmOXlyWGJXR1E9PTwvUT48RFA+R0U4MmJ3NktMMGh4RklkZnNQTU4vV0puWjN3cE95anN6YzVWWG5yOTBTNTRxZDhaZFRtNEd1MWVoVklwSWcyVTMxQ2lQMXM5YmtwUUhPVEhpL0dCQVE9PTwvRFA+PERRPmRxeHFMR053ZnJsS2ZOZWRVR283UEhYRU5zRjRmRzZTbk51WUIrZXFwUjFkbjEvVHdjSHJveVhSNUxXS1ZyMHFvREErTEIvWTNsMmRtM2hoRFFYOVFRPT08L0RRPjxJbnZlcnNlUT51d1l1UkJ2bGlrK3h0dG1TL3V0WTZKOHV0Qnh6cWZpRGRvdXlpelBzSStuWCtMbDNiNUQ0VE13Z2VQV2I5UXVKMXVWUmhITXFCSk9FdXpmWElqdE5NQT09PC9JbnZlcnNlUT48RD5kUWNWQmU4NHRQUllBUWtqV1U0dUMvdnltcnE1Qm1McGNqYTZJM3FNM0dnR1oxYkRTT25DRGZPTnhvOWI2N1NUZXdQei95MDFUVkpGWU9PYkpTRVNvUnB0c3VmSUpvcVcwaCtMWXBaNmszYzd5dnlnTWhvZGtWVTM3TEhkV0xKQXFRMDJUeDc2cWdPSWQ4OGphQzA2RXk2Rk53NFhXM3E0cDNUMlVJMWJuWUU9PC9EPjwvUlNBS2V5VmFsdWU+]]></key></private><cipher><key><![CDATA[vbTSKZ7ss0AQkC6BFYR/vg==]]></key><iv><![CDATA[BvuxGsOWgFnRGBlbfhDbUQ==]]></iv></cipher></SecurityManager>";
	public static String PUBLIC_KEY_SOURCE = "";
	private static int KEY_LEN = 8;
	
	public static WTPAType decipherWTPA(String enciphered_token){
		SecurityFactory sf = SecurityFactory.getSecurityFactory();
		SecurityBean bean = sf.createSecurityBean(PRIVATE_KEY_SOURCE.getBytes(), false);
		return decipherWTPA(bean, enciphered_token);
	}
	public static WTPAType decipherWTPA(SecurityBean bean, String enciphered_token){
		byte[] token_data = BinaryUtil.fromBase64(enciphered_token.getBytes());
		byte[] token_dec = SecurityUtil.decipher(bean, token_data);
		if(token_dec.length == 0) return null;
		return parseToken(new String(token_dec));
	
	}
	public static String serializeToken(WTPAType wtpa){
		
		return BinaryUtil.toBase64Str(wtpa.getVersion())
				+ "-" + BinaryUtil.toBase64Str(wtpa.getGuid())
				+ "-" + BinaryUtil.toBase64Str(wtpa.getKeyId())
				+ "-" + BinaryUtil.toBase64Str(wtpa.getSource())
				+ "-" + BinaryUtil.toBase64Str(wtpa.getOrganizationId())
				+ "-" + BinaryUtil.toBase64Str(wtpa.getRecordId())
				+ "-" + BinaryUtil.toBase64Str(wtpa.getUid())
				+ "-" + BinaryUtil.toBase64Str(wtpa.getAclMask())
		;
	}
	public static WTPAType parseToken(String token){
		WTPAType wtpa = new WTPAType();
        String[] pairs = token.split("-");
        if (pairs.length != KEY_LEN) {
        	System.out.println("Specified pairs (" + pairs.length + ") do not match expected pairs (" + KEY_LEN + ")");
        	return null;
        }
        
        int i = 0;
        wtpa.setVersion(BinaryUtil.fromBase64Str(pairs[i++]));
        wtpa.setGuid(BinaryUtil.fromBase64Str(pairs[i++]));
        wtpa.setKeyId(BinaryUtil.fromBase64Str(pairs[i++]));
        wtpa.setSource(BinaryUtil.fromBase64Str(pairs[i++]));
        wtpa.setOrganizationId(BinaryUtil.fromBase64Str(pairs[i++]));
        wtpa.setRecordId(BinaryUtil.fromBase64Str(pairs[i++]));
        wtpa.setUid(BinaryUtil.fromBase64Str(pairs[i++]));
        wtpa.setAclMask(BinaryUtil.fromBase64Str(pairs[i++]));
        wtpa.setValid(true);
		
		return wtpa;
	}


}
