package android.os;
import android.os.IWhiteListChangeListener;
import android.os.WakeupItem;

interface IWhiteListService
{
    void registerChangeListener(IWhiteListChangeListener changelistener,int whitelisttype);
    void unregisterChangeListener(IWhiteListChangeListener changelistener);
    
	String[] getPurebackgroundHideList();
	  boolean isPurebackgroundHide(String pkgname);
	  String[] getPurebackgroundDefList();
	  boolean isPurebackgroundDef(String pkgname);
	  String[] getPurebackgroundNotKillList();
	  String[] getPurbackgroundEnableList();
	  String[] getPuregackgroundDisableList();
	  
	
	  String[] getNotificationHideList();
	  boolean isNotificationHide(String pkgname);
	  String[] getNotificationDefList();
	  boolean isNotificationDef(String pkgname);
	
	  String[] getFloatDefList();
	  boolean isFloatEnable(String pkgname);

	String[] getAutoLaunchDefList(); 
	boolean isAutoLaunchDef(String pkgname);
	 
	String[] getNetForbadeList();
		
	WakeupItem[] getWakeupList();
	
	String[] getSleepNetList();
	
	String[] getBlockActivityList();
	
	String[] getMsgWhiteList();
	boolean isCanSendMsg(String pkg);
	
	String[] getInstallWhiteList();
	boolean isCanInstall(String pkgname); 

	String[] getDozeWhiteList();
}