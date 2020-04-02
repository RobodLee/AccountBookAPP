package com.robod.accountbook.util;

import java.util.UUID;


/**
 * @author Robod Lee
 */
public final class UuidUtil {

	public static String getUuid(){
		return UUID.randomUUID().toString().replace("-","");
	}
}
