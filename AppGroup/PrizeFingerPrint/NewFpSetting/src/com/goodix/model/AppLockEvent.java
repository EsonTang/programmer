package com.goodix.model;

/**
 **
 * 类描述：AppLock事件类
 * @author 朱道鹏
 * @version V1.0
 */
public class AppLockEvent {
	public static final String  ID= "_id";
	public static final String  PKG_NAME= "pkgName";
	public static final String  CLASS_NAME= "className";
	public static final String  NEED_LOCK= "needLock";
	public static final String  ALREADY_UNLOCKER= "alreadyUnlocked";
	
	/**
	 * id，无需主动设置，数据库已设为自增长
	 */
    private int _id;  
  
	/**
	 * 已锁应用名字段
	 */
    private String pkgName;  
    
    /**
	 * 已锁应用名字段
	 */
    private String className;  
  
    /**
	 * 已锁应用名单次解锁字段
	 */
    private int lockStatus;

	public int getId() {
		return _id;
	}

	public void setId(int id) {
		this._id = id;
	}

	public String getLockAppPkgName() {
		return pkgName;
	}

	public void setLockAppPkgName(String pkgName) {
		this.pkgName = pkgName;
	}
	
	public String getLockAppClassName() {
		return className;
	}

	public void setLockAppClassName(String className) {
		this.className = className;
	}

	public int getLockStatus() {
		return lockStatus;
	}

	public void setLockStatus(int lockStatus) {
		this.lockStatus = lockStatus;
	}
}

