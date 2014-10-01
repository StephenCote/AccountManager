package org.cote.accountmanager.data.rule;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.cote.accountmanager.objects.types.ComparatorEnumType;

public class RuleUtil {
	private static final Pattern intPattern = Pattern.compile("^\\d+$");
	public static final Logger logger = Logger.getLogger(RuleUtil.class.getName());
	private static final Map<String,Pattern> patterns = new HashMap<String,Pattern>();
	private static Pattern getPattern(String pattern){
		if(patterns.containsKey(pattern)) return patterns.get(pattern);
		Pattern p = Pattern.compile(pattern);
		patterns.put(pattern, p);
		return p;
	}
	public static boolean compareValue(String chkData, ComparatorEnumType comparator, String compData){
		boolean out_bool = false;
		
		if(chkData != null && compData != null && intPattern.matcher(chkData).matches() && intPattern.matcher(compData).matches()){
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
			logger.info("Comparing as long " + chkData + " is " + comparator + " " + compData + " == " + out_bool);
		}
		else{

			switch(comparator){
				case LIKE:
					String likePat = "^" + compData.replaceAll("%", ".*") + "$";
					Pattern pat = getPattern(likePat);
					out_bool = pat.matcher(chkData).find();
					break;
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
			logger.info("Comparing as string " + chkData + " is " + comparator + " " + compData + " == " + out_bool);
		}
		return out_bool;
	}
}
