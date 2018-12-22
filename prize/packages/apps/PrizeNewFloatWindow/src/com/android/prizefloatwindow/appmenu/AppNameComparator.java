package com.android.prizefloatwindow.appmenu;

import java.text.CollationKey;
import java.text.Collator;
import java.text.RuleBasedCollator;
import java.util.Comparator;
import java.util.Locale;

public class AppNameComparator implements Comparator<MyAppInfo> {  
	private RuleBasedCollator collator = null;
	public AppNameComparator() {    
        collator = (RuleBasedCollator) Collator.getInstance(java.util.Locale.CHINA);    
    }  
	public AppNameComparator(Locale locale) {    
        collator = (RuleBasedCollator) Collator.getInstance(locale);    
    } 
    @Override  
    public int compare(MyAppInfo o1, MyAppInfo o2) {  
        // TODO Auto-generated method stub  
         CollationKey c1 = collator.getCollationKey(o1.getAppName());    
         CollationKey c2 = collator.getCollationKey(o2.getAppName());    
         return collator.compare(((CollationKey) c1).getSourceString(),  ((CollationKey) c2).getSourceString());    
    }  
}  