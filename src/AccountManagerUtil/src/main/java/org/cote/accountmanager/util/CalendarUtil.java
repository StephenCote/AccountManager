package org.cote.accountmanager.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

public class CalendarUtil {
	private static DatatypeFactory dataTypeFactory = null;
	private static String legacyDateFormat = "yyyy/MM/dd HH:mm:ss";
	private static String dateFormat = "yyyy-MM-dd:hh:mm:ss Z";

	public static Date importDateFromLegacyString(String s){
		return importDateFromString(s, legacyDateFormat);
	}
	public static Date importDateFromString(String s){
		return importDateFromString(s, dateFormat);
	}
	public static Date importDateFromString(String s, String format){
		SimpleDateFormat parserSDF=new SimpleDateFormat(format);
		Date d = null;
		try {
			d = parserSDF.parse(s);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return d;
		
	}
	public static String exportDateAsLegacyString(Date d){
		return exportDateAsString(d, legacyDateFormat);
	}

	public static String exportDateAsString(Date d){
		return exportDateAsString(d, dateFormat);
	}
	public static String exportDateAsString(Date d, String format){
		SimpleDateFormat parserSDF=new SimpleDateFormat(format);
		return parserSDF.format(d);
	}
	public static long getTimeSpanFromNow(XMLGregorianCalendar startCal){
		Calendar cal = Calendar.getInstance();
		return (startCal.toGregorianCalendar().getTimeInMillis() - cal.getTimeInMillis());
	}
	public static long getTimeSpan(XMLGregorianCalendar startCal, XMLGregorianCalendar endCal){
		return (endCal.toGregorianCalendar().getTimeInMillis() - startCal.toGregorianCalendar().getTimeInMillis());
	}
	public static Date getDate(XMLGregorianCalendar cal){
		return new Date((cal.toGregorianCalendar().getTimeInMillis()));
	}
	public static DatatypeFactory getDatatypeFactory(){
		if(dataTypeFactory != null) return dataTypeFactory;
		try {
			dataTypeFactory = DatatypeFactory.newInstance();
		} catch (DatatypeConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return dataTypeFactory;
	}
	public static XMLGregorianCalendar getXmlGregorianCalendar(Date date){
		GregorianCalendar c = new GregorianCalendar();
		c.setTime(date);
		return getDatatypeFactory().newXMLGregorianCalendar(c);
	}
}
