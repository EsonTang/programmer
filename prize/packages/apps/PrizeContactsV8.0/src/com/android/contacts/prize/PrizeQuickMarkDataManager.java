package com.android.contacts.prize;

import com.android.contacts.common.model.Contact;

import android.os.Parcelable;

/**
 * com.android.contacts.prize.QuickMarkDataManager
 * @author huangpengfei <br/>
 * create at 2016-8-16 
 */
public class PrizeQuickMarkDataManager {

	private static Contact mContact ;
	
	public static void setContact(Contact contact){
		mContact = contact;
	}
	
	public static Contact getContact(){
		return mContact;
	}
	
	public static void clearData(){
		mContact = null;
		
	}
	
}
