package org.cote.accountmanager.data.rule;

import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.cote.accountmanager.objects.types.ComparatorEnumType;

public class RuleUtil {
	private static final Pattern intPattern = Pattern.compile("^\\d+$");
	public static final Logger logger = Logger.getLogger(RuleUtil.class.getName());

	public static boolean compareValue(String chkData, ComparatorEnumType comparator, String compData){
		boolean out_bool = false;
		
		if(chkData != null && compData != null && intPattern.matcher(chkData).matches() && intPattern.matcher(compData).matches()){
			logger.info("Comparing as long " + chkData + " is " + comparator + " " + compData);
			long lChk = Long.parseLong(chkData);
			long lComp = Long.parseLong(compData);
			switch(comparator){
				case EQUALS:
					out_bool = (lChk == lComp);
					break;
				case GREATER_THAN:
					out_bool = (lChk > lComp);
					break;
				case GREATER_THAN_OR_EQUALS:
					out_bool = (lChk >= lComp);
					break;
				case LESS_THAN:
					out_bool = (lChk < lComp);
					break;
				case LESS_THAN_OR_EQUALS:
					out_bool = (lChk <= lComp);
					break;
				case NOT_EQUALS:
					out_bool = (lChk != lComp);
					break;
				default:
					logger.error("Unhandled comparator: " + comparator);
					break;

			}
		}
		else{
			logger.info("Comparing as string " + chkData + " is " + comparator + " " + compData);
			switch(comparator){
				case EQUALS:
					if(chkData != null && compData != null) out_bool = chkData.equals(compData);
					else if(chkData == null && compData == null){
						logger.info("Both values are null with an equals comparator.  Returning true");
						out_bool = true;
					}
					break;
				case IS_NULL:
					out_bool = (chkData == null);
					break;
				default:
					logger.error("Unhandled comparator: " + comparator);
					break;

			}
		}
		return out_bool;
	}
}
