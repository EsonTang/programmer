package com.android.launcher3;

public class AttributesInfo {
    public String className;
    public String packageName;
    public String screen;
    public String container;
    public String x;
    public String y;
    public int spanX;
    public int spanY;
    public int icon;
    public int title;
    public String name;
    public String uri;
    
    final static public  String CLASSNAME="className";
    
    final static public  String PACKAGENAME="packageName";
    
    final static public  String SCREEN="screen";
    
    final static public  String CONTAINER="container";
    
    final static public  String X="x";
    
    final static public  String Y="y";
    
    final static public  String SPANX="spanX";
    
    final static public  String SPANY="spanY";
    
    final static public  String ICON="icon";
    
    final static public  String TITLE="title";
    
    final static public  String NAME="name";
    
    final static public  String URI="uri";
    
    boolean hasValue(Object obj) {
    	if (obj != null) {
    		return true;
    	}
    	return false;
    }
    
}
