package org.cote.accountmanager.data.services;

import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.exceptions.DataException;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.AuditEnumType;

public interface ITypeSanitizer {
	public <T> boolean useAlternateDelete(AuditEnumType type, T object);
	public <T> boolean useAlternateUpdate(AuditEnumType type, T object);
	public <T> T sanitizeNewObject(AuditEnumType type, UserType user, T in_obj) throws ArgumentException, FactoryException, DataException;
	public <T> boolean useAlternateAdd(AuditEnumType type, T object);
	public <T> boolean add(AuditEnumType type, T object) throws FactoryException, ArgumentException;
	public <T> boolean delete(AuditEnumType type, T object) throws FactoryException, ArgumentException;

}
