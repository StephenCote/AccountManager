package org.cote.accountmanager.data.operation;

import org.cote.accountmanager.objects.FactType;
import org.cote.accountmanager.objects.OperationResponseEnumType;
import org.cote.accountmanager.objects.PatternType;

public interface IOperation {
	public <T> T read(FactType sourceFact,final FactType referenceFact);
	public OperationResponseEnumType operate(final PatternType pattern, FactType sourceFact,final FactType referenceFact);
}
