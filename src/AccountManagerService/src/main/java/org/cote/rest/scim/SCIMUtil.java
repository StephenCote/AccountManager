package org.cote.rest.scim;

import java.util.ArrayList;
import java.util.List;

import org.cote.accountmanager.objects.AccountType;
import org.cote.accountmanager.objects.AddressType;
import org.cote.accountmanager.objects.BaseGroupType;
import org.cote.accountmanager.objects.BaseRoleType;
import org.cote.accountmanager.objects.ContactInformationType;
import org.cote.accountmanager.objects.ContactType;
import org.cote.accountmanager.objects.EntitlementType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.PersonType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.AuditEnumType;
import org.cote.accountmanager.objects.types.ContactEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.UserStatusEnumType;
import org.cote.accountmanager.service.rest.BaseService;

import com.unboundid.scim2.common.types.Address;
import com.unboundid.scim2.common.types.Email;
import com.unboundid.scim2.common.types.Entitlement;
import com.unboundid.scim2.common.types.Group;
import com.unboundid.scim2.common.types.PhoneNumber;
import com.unboundid.scim2.common.types.Role;
import com.unboundid.scim2.common.types.UserResource;

public class SCIMUtil {
	private static <T> T convert(ContactType contact) {
		T outC = null;
		switch(contact.getContactType()) {
			case EMAIL:
				Email emO = new Email();
				emO.setPrimary(contact.getPreferred());
				emO.setDisplay(contact.getName());
				emO.setValue(contact.getContactValue());
				outC = (T)emO;
				break;
			case PHONE:
				PhoneNumber phO = new PhoneNumber();
				phO.setDisplay(contact.getName());
				phO.setPrimary(contact.getPreferred());
				phO.setValue(contact.getContactValue());
				break;
		}
		return outC;
	}
	private static Address convert(AddressType addr) {
		Address outA = new Address();
		outA.setCountry(addr.getCountry());
		outA.setPostalCode(addr.getPostalCode());
		outA.setPrimary(addr.getPreferred());
		outA.setRegion(addr.getRegion());
		outA.setStreetAddress(addr.getAddressLine1());
		outA.setType(addr.getLocationType().toString());
		outA.setLocality(addr.getCity());
		return outA;
	}
	private static Entitlement convert(EntitlementType ent) {
		Entitlement outE = new Entitlement();
		outE.setDisplay(ent.getEntitlementName());
		outE.setValue(ent.getEntitlementGuid());
		outE.setType(ent.getEntitlementType().toString());
		return outE;
	}
	private static Role convert(BaseRoleType role) {
		Role outR = new Role();
		outR.setType(role.getRoleType().toString());
		outR.setDisplay(role.getName());
		outR.setValue(role.getUrn());
		return outR;
	}
	private static Group convert(BaseGroupType group) {
		Group outG = new Group();
		outG.setDisplay(group.getName());
		outG.setValue(group.getUrn());
		outG.setType(group.getGroupType().toString());
		return outG;
	}
	private static void applyContactInformation(UserResource user, ContactInformationType cinfo) {
		if(cinfo == null || user == null) return;

		List<Address> addrList = new ArrayList<>();
		for(AddressType addr : cinfo.getAddresses()) addrList.add(convert(addr));
		user.setAddresses(addrList);
		List<Email> emList = new ArrayList<>();
		List<PhoneNumber> phList = new ArrayList<>();
		for(ContactType contact : cinfo.getContacts()) {
			if(contact.getContactType().equals(ContactEnumType.EMAIL)) emList.add(convert(contact));
			else if(contact.getContactType().equals(ContactEnumType.PHONE)) phList.add(convert(contact));
			
		}
		user.setEmails(emList);
		user.setPhoneNumbers(phList);
		
	}
	private static void applyNameId(UserResource user, NameIdType obj) {
		user.setDisplayName(obj.getName());
		user.setId(obj.getObjectId());
		user.setExternalId(obj.getUrn());
		user.setUserName(obj.getName());
		user.setUserType(obj.getNameType().toString());
	}
	private static void applyEntitlements(UserType contextUser, UserResource user, NameIdType obj) {
		List<BaseRoleType> roles = BaseService.listForMember(AuditEnumType.ROLE, contextUser, obj, FactoryEnumType.fromValue(obj.getNameType().toString()));
		List<Role> rolList = new ArrayList<>();
		for(BaseRoleType role : roles) rolList.add(convert(role));
		user.setRoles(rolList);
		
		List<BaseGroupType> groups = BaseService.listForMember(AuditEnumType.GROUP, contextUser, obj, FactoryEnumType.fromValue(obj.getNameType().toString()));
		List<Group> grpList = new ArrayList<>();
		for(BaseGroupType group : groups) grpList.add(convert(group));
		user.setGroups(grpList);

		List<EntitlementType> ents = BaseService.aggregateEntitlementsForMember(contextUser, obj);
		List<Entitlement> entList = new ArrayList<>();
		for(EntitlementType ent : ents) entList.add(convert(ent));
		user.setEntitlements(entList);

	}
	public static UserResource convert(UserType contextUser, NameIdType obj) {
		UserResource outU = new UserResource();
		BaseService.populate(AuditEnumType.fromValue(obj.getNameType().toString()), obj);
		applyNameId(outU, obj);
		ContactInformationType cinfo = null;
		switch(obj.getNameType()) {
			case USER:
				UserType u1 = (UserType)obj;
				cinfo = u1.getContactInformation();
				outU.setActive(!u1.getUserStatus().equals(UserStatusEnumType.DISABLED));
				break;
			case PERSON:
				cinfo = ((PersonType)obj).getContactInformation();
				outU.setActive(true);
				break;
			case ACCOUNT:
				cinfo = ((AccountType)obj).getContactInformation();
				outU.setActive(true);
				break;
			default:
				break;
		}
		applyContactInformation(outU, cinfo);
		applyEntitlements(contextUser, outU, obj);
		return outU;
	}
}
