package org.cote.accountmanager.data.operation;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.cote.accountmanager.data.fact.FactUtil;
import org.cote.accountmanager.objects.CredentialType;
import org.cote.accountmanager.objects.FactType;
import org.cote.accountmanager.objects.OperationResponseEnumType;
import org.cote.accountmanager.objects.PatternType;
import org.cote.accountmanager.objects.types.FactoryEnumType;

public class RegexOperation implements IOperation {
	public static final Logger logger = Logger.getLogger(RegexOperation.class.getName());
	Map<String,Pattern> patterns = new HashMap<String,Pattern>();
	public <T> T read(FactType sourceFact,final FactType referenceFact){
		return FactUtil.factoryRead(sourceFact, referenceFact);
	}
	public OperationResponseEnumType operate(final PatternType pattern, FactType sourceFact,final FactType referenceFact){
		if(sourceFact.getFactData() == null){
			logger.error("Invalid argument.  Expecting source string value");
			return OperationResponseEnumType.ERROR;
		}
		if(referenceFact.getFactData() == null){
			logger.error("Invalid argument.  Expecting reference string value");
			return OperationResponseEnumType.ERROR;
		}
		//logger.info("Comparing " + sourceFact.getFactData() + " with " + referenceFact.getFactData());
		
		if(patterns.containsKey(referenceFact.getFactData()) == false){
			patterns.put(referenceFact.getFactData(), Pattern.compile(referenceFact.getFactData()));
		}
		Pattern exp = patterns.get(referenceFact.getFactData());
		if(exp.matcher(sourceFact.getFactData()).find()){
			logger.info("Pattern successfully matched");
			return OperationResponseEnumType.SUCCEEDED;

		}
		logger.info("Pattern failed to match");
		return OperationResponseEnumType.FAILED;

	}
}
