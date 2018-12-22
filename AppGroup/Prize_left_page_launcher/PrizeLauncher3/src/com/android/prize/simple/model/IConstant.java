package com.android.prize.simple.model;

public interface IConstant {
	/**锁定key, boolean*/
	String KEY_LOCK = "isLock";
	/**模式key, 是否为编辑*/
	String KEY_MODE = "isEdit";
	/**是否已经初始化过*/
	String KEY_INIT = "isInit";
	/**城市代号key*/
	String KEY_POSTAL = "postal";

	final int TYPE_APP = 0;
	
	final int TYPE_WIDGET = 1;
	
	final int TYPE_CONTACT = 2;
	
	final int TYPE_ADD = 3;
}
