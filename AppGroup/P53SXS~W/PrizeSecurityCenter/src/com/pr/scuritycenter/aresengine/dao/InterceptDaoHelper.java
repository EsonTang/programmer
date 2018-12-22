package com.pr.scuritycenter.aresengine.dao;

import java.util.List;

import android.util.Log;
import tmsdk.common.module.aresengine.ContactEntity;

public class InterceptDaoHelper {

	public static void populateStaticData(List<ContactEntity> contactList,
			int numEntities, int entityIds[], String phoneNums[], String names[]) {
		contactList.clear();
		for (int i = 0; i < numEntities; ++i) {
			ContactEntity entity = new ContactEntity();
			entity.id = entityIds[i];
			entity.phonenum = phoneNums[i];
			entity.name = names[i];
			contactList.add(entity);
		}
	}
	
	public static boolean contains(List<ContactEntity> contactList, String phonenum, int callfrom) {
		for (ContactEntity entity : contactList) {
			Log.v("JOHN", "entity.phonenum : " + entity.phonenum + ", phonenum : " + phonenum);
			String pattern = entity.phonenum;
			if (pattern.length() > 8)
				pattern = pattern.substring(pattern.length() - 8);
			if (phonenum.endsWith(pattern)) {
				return true;
			}
		}
		return false;
	}

}
