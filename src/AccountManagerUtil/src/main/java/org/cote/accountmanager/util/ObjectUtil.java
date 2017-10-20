package org.cote.accountmanager.util;

import java.util.Random;

public class ObjectUtil {
	/// From stackoverflow: http://stackoverflow.com/questions/1972392/java-pick-a-random-value-from-an-enum
	///
    public static <T extends Enum<?>> T randomEnum(Class<T> cls){
        int x = (new Random()).nextInt(cls.getEnumConstants().length);
        return cls.getEnumConstants()[x];
    }
}
