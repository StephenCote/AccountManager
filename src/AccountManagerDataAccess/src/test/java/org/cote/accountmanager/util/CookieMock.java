package org.cote.accountmanager.util;
import javax.servlet.http.Cookie;
public class CookieMock extends Cookie{
	private String name = null;
	private String val = null;
	public CookieMock(String arg0, String arg1) {
		super(arg0, arg1);
		name = arg0;
		val = arg1;
		
	}



	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return name;
	}



	@Override
	public String getValue() {
		// TODO Auto-generated method stub
		return val;
	}


}
