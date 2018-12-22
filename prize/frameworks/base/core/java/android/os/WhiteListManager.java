package android.os;

import android.content.Context;

public class WhiteListManager {
	private IWhiteListService mIWhiteList;
	Context mContext;
	public static final int WHITELIST_TYPE_PUREBKGROUND = 1; 
	public static final int WHITELIST_TYPE_NOTIFICATION = 2;
	public static final int WHITELIST_TYPE_FLOATWINDOW = 3;
	public static final int WHITELIST_TYPE_AUTOLAUNCH = 4;
	public static final int WHITELIST_TYPE_NETFORBADE = 5;
	public static final int WHITELIST_TYPE_RELEATEWAKEUP = 6;
	public static final int WHITELIST_TYPE_SLEEPNET = 7;
	public static final int WHITELIST_TYPE_BLOCKACTIVITY = 8;
	public static final int WHITELIST_TYPE_MSGWHITE = 9;
	public static final int WHITELIST_TYPE_INSTALLWHITE = 10;
	/*
	*@hide
	*/
	public static final int WHITELIST_TYPE_DOZEWHITE = 11;

       /*
	*@hide
	*/
	public static final int WHITELIST_TYPE_PROVIDERWAKEUP = 14;
	
	public WhiteListManager(Context context) {
		mIWhiteList = IWhiteListService.Stub.asInterface(ServiceManager.getService("whitelist"));
		mContext = context;
	}

	public void registerChangeListener(IWhiteListChangeListener changelistener,int whitelistType)
	{
		try
		{
			mIWhiteList.registerChangeListener(changelistener,whitelistType);
		}
		catch(RemoteException e)
		{
			
		}
	}
	public void unregisterChangeListener(IWhiteListChangeListener changelistener)
	{
		try
		{
			mIWhiteList.unregisterChangeListener(changelistener);
		}
		catch(RemoteException e)
		{
			
		}
	}

	public String[] getPurebackgroundHideList()
	{
		try
		{
			return mIWhiteList.getPurebackgroundHideList();
		}
		catch(RemoteException e)
		{
			
		}
		return null;
	}

	public boolean isPurebackgroundHide(String pkgname)
	{
		try
		{
			return mIWhiteList.isPurebackgroundHide(pkgname);
		}
		catch(RemoteException e)
		{
			
		}
		return false;
	}

	public String[] getPurebackgroundDefList()
	{
		try
		{
			return mIWhiteList.getPurebackgroundDefList();
		}
		catch(RemoteException e)
		{
			
		}
		return null;
	}

	public boolean isPurebackgroundDef(String pkgname)
	{
		try
		{
			return mIWhiteList.isPurebackgroundDef(pkgname);
		}
		catch(RemoteException e)
		{
			
		}
		return false;
	}

	public String[] getPurebackgroundNotKillList()
	{
		try
		{
			return mIWhiteList.getPurebackgroundNotKillList();
		}
		catch(RemoteException e)
		{
			
		}
		return null;
	}

	public String[] getPurbackgroundEnableList()
	{
		try
		{
			return mIWhiteList.getPurbackgroundEnableList();
		}
		catch(RemoteException e)
		{
			
		}
		return null;
	}

	public String[] getPuregackgroundDisableList()
	{
		try
		{
			return mIWhiteList.getPuregackgroundDisableList();
		}
		catch(RemoteException e)
		{
			
		}
		return null;
	}

	public String[] getNotificationHideList()
	{
		try
		{
			return mIWhiteList.getNotificationHideList();
		}
		catch(RemoteException e)
		{
			
		}
		return null;
	}

	public boolean isNotificationHide(String pkgname)
	{
		try
		{
			return mIWhiteList.isNotificationHide(pkgname);
		}
		catch(RemoteException e)
		{
			
		}
		return false;
	}

	public String[] getNotificationDefList()
	{
		try
		{
			return mIWhiteList.getNotificationDefList();
		}
		catch(RemoteException e)
		{
			
		}
		return null;
	}

	public boolean isNotificationDef(String pkgname)
	{
		try
		{
			return mIWhiteList.isNotificationDef(pkgname);
		}
		catch(RemoteException e)
		{
			
		}
		return false;
	}

	public String[] getFloatDefList()
	{
		try
		{
			return mIWhiteList.getFloatDefList();
		}
		catch(RemoteException e)
		{
			
		}
		return null;
	}
	

	public boolean isFloatEnable(String pkgname)
	{
		try
		{
			return mIWhiteList.isFloatEnable(pkgname);
		}
		catch(RemoteException e)
		{
			
		}
		return false;
	}

	public String[] getAutoLaunchDefList()
	{
		try
		{
			return mIWhiteList.getAutoLaunchDefList();
		}
		catch(RemoteException e)
		{
			
		}
		return null;
	}

	public boolean isAutoLaunchDef(String pkgname)
	{
		try
		{
			return mIWhiteList.isAutoLaunchDef(pkgname);
		}
		catch(RemoteException e)
		{
			
		}
		return false;
	}

	public String[] getNetForbadeList()
	{
		try
		{
			return mIWhiteList.getNetForbadeList();
		}
		catch(RemoteException e)
		{
			
		}
		return null;
	}

	public WakeupItem[] getWakeupList()
	{
		try
		{
			return mIWhiteList.getWakeupList();
		}
		catch(RemoteException e)
		{
			
		}
		return null;
	}

	public String[] getSleepNetList()
	{
		try
		{
			return mIWhiteList.getSleepNetList();
		}
		catch(RemoteException e)
		{
			
		}
		return null;
	}

	public String[] getBlockActivityList()
	{
		try
		{
			return mIWhiteList.getBlockActivityList();
		}
		catch(RemoteException e)
		{
			
		}
		return null;
	}

	public String[] getMsgWhiteList()
	{
		try
		{
			return mIWhiteList.getMsgWhiteList();
		}
		catch(RemoteException e)
		{
			
		}
		return null;
	}

	public boolean isCanSendMsg(String pkg)
	{
		try
		{
			return mIWhiteList.isCanSendMsg(pkg);
		}
		catch(RemoteException e)
		{
			
		}
		return false;
	}

	public String[] getInstallWhiteList()
	{
		try
		{
			return mIWhiteList.getInstallWhiteList();
		}
		catch(RemoteException e)
		{
			
		}
		return null;
	}

	public boolean isCanInstall(String pkgname)
	{
		try
		{
			return mIWhiteList.isCanInstall(pkgname);
		}
		catch(RemoteException e)
		{
			
		}
		return false;
	}
	/*
	*@hide
	*/
	public String[] getDozeWhiteList()
	{
		try
		{
			return mIWhiteList.getDozeWhiteList();
		}
		catch(RemoteException e)
		{
			
		}
		return null;
	}
}
