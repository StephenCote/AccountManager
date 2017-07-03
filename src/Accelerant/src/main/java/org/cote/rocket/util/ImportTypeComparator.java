package org.cote.rocket.util;

import java.util.Comparator;

import org.cote.propellant.objects.IdentityDataImportType;

public class ImportTypeComparator  implements Comparator<IdentityDataImportType>{
	public static int classify(IdentityDataImportType idt){
		int out = 0;
		switch(idt.getType()){
		case PERSON: out = 1; break;
		case ACCOUNT: out = 2; break;
		case PERMISSION: out = 3; break;
		case GROUP: out = 4; break;
		case GROUPMAP: out = 5; break;
		case MAP: out = 6; break;
		case ENTITLEMENTMAP: out = 7; break;
		}
		return out;
	}
	public int compare(IdentityDataImportType id1, IdentityDataImportType id2){
		int id1i = classify(id1);
		int id2i = classify(id2);
		return (id1i > id2i ? 1 : id1i==id2i ? 0 : -1);

	}

}
