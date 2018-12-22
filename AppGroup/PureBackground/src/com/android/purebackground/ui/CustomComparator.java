package com.android.purebackground.ui;

import java.util.Comparator;

import com.android.purebackground.unit.PackagesInfo;

public class CustomComparator implements Comparator<Object>{

	@Override
	public int compare(Object obj1, Object obj2) {
		if(obj1 instanceof PackagesInfo && obj2 instanceof PackagesInfo){
			PackagesInfo pkInfo1 = (PackagesInfo)obj1;
			PackagesInfo pkInfo2 = (PackagesInfo)obj2;
			int compareName = pkInfo1.getAppName().compareTo(pkInfo2.getAppName()); 
			return compareName;
		}
		return -1;
	}

}
