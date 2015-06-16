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
