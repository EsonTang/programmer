package com.android.contacts.prize;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.android.contacts.quickcontact.ExpandingEntryCardView.Entry;

import android.util.Log;


/**
 * com.android.contacts.prize.PrizeQuickContactDataManager
 * @author huangpengfei <br/>
 * create at 2016-2-9 
 */
public class PrizeQuickContactDataManager {

	private static final String TAG = "PrizeQuickContactDataManager";
	private static List<Entry> mEntryList = new ArrayList();
	
	public static void setEntry(Entry newEntry){
		if(mEntryList.size() == 0){
			mEntryList.add(newEntry);
		}else{
			if(!isExist(newEntry)){
				mEntryList.add(newEntry);
			}
		}
	}
	
	private static boolean isExist(Entry entry){
		boolean isExist = false;
		for (int i = 0;i < mEntryList.size();i++) {
			if(mEntryList.get(i).getHeader().equals(entry.getHeader())){
				isExist = true;
			}
		}
		return isExist;
	}
	
	public static List<Entry> getEntryList(){
		return mEntryList;
	}
	
	public static void clearData(){
		mEntryList.clear();
		
	}
	
}
