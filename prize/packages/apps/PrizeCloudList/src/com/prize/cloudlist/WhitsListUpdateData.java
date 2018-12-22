package com.prize.cloudlist;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Environment;
import android.os.Process;
import android.os.RemoteException;
import android.os.UserHandle;
import android.provider.DatabaseUtil;
import android.provider.DatabaseUtil.DbRecord;
import android.provider.Settings;
import android.provider.WhiteListColumns;
import android.util.Log;
import android.util.Xml;
import android.view.WindowManager;

import com.prize.cloudlist.CloudUtils.ProviderWakeupItem;
import com.prize.cloudlist.CloudUtils.ReleateWakeupItem;

import com.android.internal.util.XmlUtils;
import android.content.pm.ParceledListSlice;
import android.content.pm.IPackageManager;
import android.app.INotificationManager;
import android.os.INetworkManagementService;
import android.os.ServiceManager;
import android.content.pm.ParceledListSlice;

public class WhitsListUpdateData
{
	public WhitsListUpdateData()
	{
		
	}
	public void init(Context context)
	{
		mContext = context;
	}
	private Context mContext ;
	public static final String TAG = CloudUtils.TAG;
	
	
	public static final String ACTION_UPDATE_DATA = "com.prize.whitelist.datachange";
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
	public static final int WHITELIST_TYPE_DOZEWHITE = 11;
	public static final int WHITELIST_TYPE_PROVIDERWAKEUP = 14;
	
	
	
	public void updateWhiteListSmsData(ArrayList<String> svrEnablelist)
	{
		String str = "";
		DatabaseUtil.deleteMsgWhiteDb(mContext, WhiteListColumns.ENABLE,WhiteListColumns.SERVER_CONFIG);
		for (int i = 0; i < svrEnablelist.size(); i++)
		{
			String pkgname = svrEnablelist.get(i);
			DatabaseUtil.insertMsgWhiteDb(mContext, pkgname, WhiteListColumns.ENABLE,WhiteListColumns.SERVER_CONFIG);
		}
		ArrayList<String> localenablelist = DatabaseUtil.getMsgWhiteDb(mContext, WhiteListColumns.ENABLE,WhiteListColumns.LOCAL_CONFIG);
		svrEnablelist.addAll(localenablelist);		
		//check repeat
		DatabaseUtil.checkRepeatItem(svrEnablelist);
		
		for (int i = 0; i < svrEnablelist.size(); i++)
		{			
			str += svrEnablelist.get(i) + ";";
		}
		Log.i(TAG, "updateWhiteListSmsData list:" + str);
		Settings.System.putString(mContext.getContentResolver(), "key_default_sendmsg", str);
	}

	// ////////////////////////////////////////////////////////////////////////////////////////
	public void updateWhiteListInstallData(ArrayList<String> svrEnablelist)
	{
		String str = "";
		DatabaseUtil.deleteInstallWhiteDb(mContext, WhiteListColumns.ENABLE,WhiteListColumns.SERVER_CONFIG);
		for (int i = 0; i < svrEnablelist.size(); i++)
		{
			String pkgname = svrEnablelist.get(i);
			DatabaseUtil.insertInstallWhiteDb(mContext, pkgname, WhiteListColumns.ENABLE,WhiteListColumns.SERVER_CONFIG);
		}
		ArrayList<String> localenablelist = DatabaseUtil.getInstallWhiteDb(mContext, WhiteListColumns.ENABLE,WhiteListColumns.LOCAL_CONFIG);
		svrEnablelist.addAll(localenablelist);
		//check repeat
		DatabaseUtil.checkRepeatItem(svrEnablelist);
		
		for (int i = 0; i < svrEnablelist.size(); i++)
		{
			str += svrEnablelist.get(i) + ";";
		}
		Log.i(TAG, "updateWhiteListInstallData list:" + str);
		Settings.System.putString(mContext.getContentResolver(), "key_default_installed", str);
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////
	public static void bootcompletedSetproperty(Context context)
	{
		ArrayList<DbRecord> olddefenablelist = DatabaseUtil.getPurebackgroundList(context, WhiteListColumns.DEFENABLE,WhiteListColumns.SERVER_CONFIG);
		Log.i(TAG,"bootcompletedSetproperty get olddefenablelist:"+olddefenablelist.size());		
		for (int i = 0; i < olddefenablelist.size(); i++)
		{
			DbRecord item = olddefenablelist.get(i);			
			try{
				Log.i(TAG,"bootcompletedSetproperty :"+item.pkgname);
			       android.os.SystemProperties.set(item.pkgname ,"1");
			   } catch (Exception e) {  
			       e.printStackTrace();				       
			   }
		}
	}
	public void updateWhiteListPureBkgroundData(ArrayList<String> svrhidelist, ArrayList<String> svrdefenablelist, ArrayList<String> svrnotkilllist)
	{
		Log.i(TAG,"updateWhiteListPureBkgroundData check svrhidelist & svrdefenablelist & svrnotkilllist repeat item");
		//check repeatitem
		DatabaseUtil.checkRepeatItem(svrhidelist);
		DatabaseUtil.checkRepeatItem(svrdefenablelist);
		DatabaseUtil.checkRepeatItem(svrnotkilllist);
				
		// check reapeat hidelist/deflist/notkilllist
		for (int i = 0; i < svrhidelist.size(); i++)
		{
			String pkgname = svrhidelist.get(i);
			for (int j = 0; j < svrdefenablelist.size(); j++)
			{
				if (svrdefenablelist.get(j).equals(pkgname))
				{
					Log.i(TAG, "updateWhiteListPureBkgroundData pkg:" + pkgname + " is in hidelist & defenable list,remove one");
					svrdefenablelist.remove(j);
					j--;
				}
			}

			for (int j = 0; j < svrnotkilllist.size(); j++)
			{
				if (svrnotkilllist.get(j).equals(pkgname))
				{
					Log.i(TAG, "updateWhiteListPureBkgroundData pkg:" + pkgname + " is in hidelist & notkill list,remove one");
					svrnotkilllist.remove(j);
					j--;
				}
			}
		}		

		// ///////////////////////////////////////////////////////////////
		// process hidelist
		// get old svr hidelist,remove the more and add new
		/*Log.i(TAG,"updateWhiteListPureBkgroundData check svrhidelist & oldsvrhidelist");
		ArrayList<DbRecord> oldhidelist = DatabaseUtil.getPurebackgroundList(mContext, WhiteListColumns.HIDE,WhiteListColumns.SERVER_CONFIG);
		ArrayList<DbRecord> localhidelist = DatabaseUtil.getPurebackgroundList(mContext, WhiteListColumns.HIDE,WhiteListColumns.LOCAL_CONFIG);
		Log.i(TAG,"updateWhiteListPureBkgroundData oldhidelist size:"+oldhidelist.size());
		ArrayList<String> addhidelist = new ArrayList<String>();
		PackageManager packageManager = mContext.getPackageManager();
		for (int i = 0; i < oldhidelist.size(); i++)
		{
			DbRecord item = oldhidelist.get(i);
			// delete old record not in svrhidelist
			if (!svrhidelist.contains(item.pkgname))
			{
				Log.i(TAG,"updateWhiteListPureBkgroundData delete oldhidelist item:"+item.pkgname);
				DatabaseUtil.deletePurebackgroundSvrRecord(mContext, item);
				
				//set the remove item of server, to disable when not in localhidelist
				boolean isinlocalhidelist = false;
				for(int j=0;j<localhidelist.size();j++)
				{
					if(localhidelist.get(j).pkgname.equals(item.pkgname))
					{
						isinlocalhidelist = true;
						break;
					}
				}
				if(!isinlocalhidelist)
				{
					try
					{
						ApplicationInfo appinfo = packageManager.getApplicationInfo(item.pkgname, PackageManager.GET_ACTIVITIES);
						 if(appinfo != null)
						 {
							 Log.i(TAG,"updateWhiteListPureBkgroundData set to disable: "+item.pkgname);
							 DatabaseUtil.insertPurebackgroundDb(mContext, item.pkgname,WhiteListColumns.DISABLE,WhiteListColumns.LOCAL_CONFIG);						 
						 }
					}
					catch(Exception e)
					{
						e.printStackTrace();
					}
				}
				oldhidelist.remove(i);
				i--;
			}
		}
		for (int i = 0; i < svrhidelist.size(); i++)
		{
			String pkgname = svrhidelist.get(i);
			boolean isadd = true;
			for (int j = 0; j < oldhidelist.size(); j++)
			{
				if (oldhidelist.get(j).pkgname.equals(pkgname))
				{
					isadd = false;
					break;
				}
			}
			// add new not in oldhielist & in svrhidelist
			if (isadd)
			{
				Log.i(TAG,"updateWhiteListPureBkgroundData add svrhidelist:"+pkgname);
				addhidelist.add(pkgname);
				DatabaseUtil.insertPurebackgroundDb(mContext, pkgname, WhiteListColumns.HIDE,WhiteListColumns.SERVER_CONFIG);
			}
		}

		// delete enable|disable when in svrhidelist
		Log.i(TAG,"updateWhiteListPureBkgroundData remove item enable|disable in addhidelist");
		ArrayList<DbRecord> enablelist = DatabaseUtil.getPurebackgroundList(mContext, WhiteListColumns.ENABLE,WhiteListColumns.LOCAL_CONFIG);
		ArrayList<DbRecord> disablelist = DatabaseUtil.getPurebackgroundList(mContext, WhiteListColumns.DISABLE,WhiteListColumns.LOCAL_CONFIG);
		for (int i = 0; i < enablelist.size(); i++)
		{
			DbRecord item = enablelist.get(i);
			if (addhidelist.contains(item.pkgname))
			{
				Log.i(TAG,"updateWhiteListPureBkgroundData delete enable in hidelist:"+item.pkgname);
				DatabaseUtil.deletePurebackgroundSvrRecord(mContext, item);
			}
		}
		for (int i = 0; i < disablelist.size(); i++)
		{
			DbRecord item = disablelist.get(i);
			if (addhidelist.contains(item.pkgname))
			{
				Log.i(TAG,"updateWhiteListPureBkgroundData delete disablelist in hidelist:"+item.pkgname);
				DatabaseUtil.deletePurebackgroundSvrRecord(mContext, item);
			}
		}
		*/
		// /////////////////////////////////////////////////////////////////////////////////
		// process defenablelist
		Log.i(TAG,"updateWhiteListPureBkgroundData check svrdefenablelist & olddefenablelist");
		ArrayList<DbRecord> olddefenablelist = DatabaseUtil.getPurebackgroundList(mContext, WhiteListColumns.DEFENABLE,WhiteListColumns.SERVER_CONFIG);
		Log.i(TAG,"updateWhiteListPureBkgroundData get olddefenablelist:"+olddefenablelist.size());
		ArrayList<String> adddefenablelist = new ArrayList<String>();
		for (int i = 0; i < olddefenablelist.size(); i++)
		{
			DbRecord item = olddefenablelist.get(i);
			// delete old record not in svrdefenablelist
			if (!svrdefenablelist.contains(item.pkgname))
			{
				Log.i(TAG,"updateWhiteListPureBkgroundData delete olddefenable item:"+item.pkgname);
				DatabaseUtil.deletePurebackgroundSvrRecord(mContext, item);
				olddefenablelist.remove(i);
				i--;
				//for set property 0
				try{
				       android.os.SystemProperties.set(item.pkgname ,"0");
				   } catch (Exception e) {  
				       e.printStackTrace();				       
				   }
			}

		}
		for (int i = 0; i < svrdefenablelist.size(); i++)
		{
			String pkgname = svrdefenablelist.get(i);
			boolean isadd = true;
			for (int j = 0; j < olddefenablelist.size(); j++)
			{
				if (olddefenablelist.get(j).pkgname.equals(pkgname))
				{
					isadd = false;
					break;
				}
			}
			// add new not in olddefenablelist & in svrdefenablelist
			if (isadd)
			{
				Log.i(TAG,"updateWhiteListPureBkgroundData add svrdefenablelist :"+pkgname);
				adddefenablelist.add(pkgname);
				DatabaseUtil.insertPurebackgroundDb(mContext, pkgname, WhiteListColumns.DEFENABLE,WhiteListColumns.SERVER_CONFIG);
				//for set property 0
				try{
				       android.os.SystemProperties.set(pkgname ,"1");
				   } catch (Exception e) {  
				       e.printStackTrace();				       
				   }
			}
		}
		/*Log.i(TAG,"updateWhiteListPureBkgroundData set new defenable item to enable");
		for (int i = 0; i < disablelist.size(); i++)
		{
			DbRecord item = disablelist.get(i);
			if (adddefenablelist.contains(item.pkgname))
			{
				item.status = WhiteListColumns.ENABLE;
				item.isserver = WhiteListColumns.LOCAL_CONFIG;
				Log.i(TAG,"updateWhiteListPureBkgroundData update disable in adddefenablelist to enable:"+item.pkgname);
				DatabaseUtil.updatePurebackgroundSvrRecord(mContext, item);
			}
		}*/

		// ///////////////////////////////////////////////////
		// process not kill list
		/*Log.i(TAG,"updateWhiteListPureBkgroundData delete notkill ad new ");
		DatabaseUtil.deletePurebackgroundWhiteDb(mContext, WhiteListColumns.NOTKILL,WhiteListColumns.SERVER_CONFIG);
		for (int i = 0; i < svrnotkilllist.size(); i++)
		{
			String pkgname = svrnotkilllist.get(i);
			DatabaseUtil.insertPurebackgroundDb(mContext, pkgname, WhiteListColumns.NOTKILL,WhiteListColumns.SERVER_CONFIG);
		}*/

		// notify update,not need
		Intent intent = new Intent(ACTION_UPDATE_DATA);
		intent.putExtra("type", WHITELIST_TYPE_PUREBKGROUND);
		mContext.sendBroadcast(intent);

	}

	// /////////////////////////////////////////////////////////////////////////////////////////
	public void updateWhiteListNotificationData(ArrayList<String> svrhidelist, ArrayList<String> svrdefenablelist)
	{
		//check repeatitem
		DatabaseUtil.checkRepeatItem(svrhidelist);
		DatabaseUtil.checkRepeatItem(svrdefenablelist);
		
		// check reapeat hidelist/deflist
		Log.i(TAG,"updateWhiteListNotificationData check svrhidelist & deflist repeat item");
		for (int i = 0; i < svrhidelist.size(); i++)
		{
			String pkgname = svrhidelist.get(i);
			for (int j = 0; j < svrdefenablelist.size(); j++)
			{
				if (svrdefenablelist.get(j).equals(pkgname))
				{
					Log.i(TAG, "updateWhiteListPureBkgroundData pkg:" + pkgname + " is in hidelist & defenable list,remove one");
					svrdefenablelist.remove(j);
					j--;
				}
			}

		}

		// ///////////////////////////////////////////////////////////////
		// process hidelist
		// get old svr hidelist,remove the more and add new
		Log.i(TAG,"updateWhiteListNotificationData check svrhidelist & oldsvrhidelist");
		ArrayList<DbRecord> oldhidelist = DatabaseUtil.getNotificationList(mContext, WhiteListColumns.HIDE,WhiteListColumns.SERVER_CONFIG);
		ArrayList<DbRecord> localhidelist = DatabaseUtil.getNotificationList(mContext, WhiteListColumns.HIDE,WhiteListColumns.LOCAL_CONFIG);
		Log.i(TAG,"updateWhiteListNotificationData oldhidelist size:"+oldhidelist.size());
		ArrayList<String> addhidelist = new ArrayList<String>();
		for (int i = 0; i < oldhidelist.size(); i++)
		{
			DbRecord item = oldhidelist.get(i);
			// delete old record not in svrhidelist
			if (!svrhidelist.contains(item.pkgname))
			{
				Log.i(TAG,"updateWhiteListNotificationData delete oldhidelist :"+item.pkgname);
				DatabaseUtil.deleteNotificationSvrRecord(mContext, item);
				
				//set the remove item of server, to disable when not in localhidelist
				boolean isinlocalhidelist = false;
				for(int j=0;j<localhidelist.size();j++)
				{
					if(localhidelist.get(j).pkgname.equals(item.pkgname))
					{
						isinlocalhidelist = true;
						break;
					}
				}
				if(!isinlocalhidelist)
				{
					setNotificationEanble(item.pkgname,false);
				}
				
				oldhidelist.remove(i);
				i--;
			}
		}
		for (int i = 0; i < svrhidelist.size(); i++)
		{
			String pkgname = svrhidelist.get(i);
			boolean isadd = true;
			for (int j = 0; j < oldhidelist.size(); j++)
			{
				if (oldhidelist.get(j).pkgname.equals(pkgname))
				{
					isadd = false;
					break;
				}
			}
			// add new not in oldhielist & in svrhidelist
			if (isadd)
			{
				Log.i(TAG,"updateWhiteListNotificationData add svrhidelist:"+pkgname);
				addhidelist.add(pkgname);
				DatabaseUtil.insertNotificationDb(mContext, pkgname, WhiteListColumns.HIDE,WhiteListColumns.SERVER_CONFIG);
			}
		}

		Log.i(TAG,"updateWhiteListNotificationData set svrhidelist new item enable notification");
		// set notification enable when in svrhidelist
		for (int i = 0; i < addhidelist.size(); i++)
		{
			setNotificationEanble(addhidelist.get(i),true);
		}

		// /////////////////////////////////////////////////////////////////////////////////
		// process defenablelist
		Log.i(TAG,"updateWhiteListNotificationData set svrdefenablelist  & oldsvrdefenablelist");
		ArrayList<DbRecord> olddefenablelist = DatabaseUtil.getNotificationList(mContext, WhiteListColumns.DEFENABLE,WhiteListColumns.SERVER_CONFIG);
		Log.i(TAG,"updateWhiteListNotificationData olddefenablelist size:"+olddefenablelist.size());
		ArrayList<String> adddefenablelist = new ArrayList<String>();
		for (int i = 0; i < olddefenablelist.size(); i++)
		{
			DbRecord item = olddefenablelist.get(i);
			// delete old record not in svrdefenablelist
			if (!svrdefenablelist.contains(item.pkgname))
			{
				Log.i(TAG,"updateWhiteListNotificationData delete olddefenablelist:"+item.pkgname);
				DatabaseUtil.deleteNotificationSvrRecord(mContext, item);
				olddefenablelist.remove(i);
				i--;
			}

		}
		for (int i = 0; i < svrdefenablelist.size(); i++)
		{
			String pkgname = svrdefenablelist.get(i);
			boolean isadd = true;
			for (int j = 0; j < olddefenablelist.size(); j++)
			{
				if (olddefenablelist.get(j).pkgname.equals(pkgname))
				{
					isadd = false;
					break;
				}
			}
			// add new not in olddefenablelist & in svrdefenablelist
			if (isadd)
			{
				adddefenablelist.add(pkgname);
				Log.i(TAG,"updateWhiteListNotificationData add adddefenablelist:"+pkgname);
				DatabaseUtil.insertNotificationDb(mContext, pkgname, WhiteListColumns.DEFENABLE,WhiteListColumns.SERVER_CONFIG);
			}
		}
		
		Log.i(TAG,"updateWhiteListNotificationData set svrdefenablelistlist new item enable notification");
		for (int i = 0; i < adddefenablelist.size(); i++)
		{
			setNotificationEanble(adddefenablelist.get(i),true);
		}

		// notify update,not need
		Intent intent = new Intent(ACTION_UPDATE_DATA);
		intent.putExtra("type", WHITELIST_TYPE_NOTIFICATION);
		mContext.sendBroadcast(intent);
	}
	
	public void setNotificationEanble(String pkgname,boolean isEnable)
	{
		try
		{
			PackageManager packageManager = mContext.getPackageManager();
			ApplicationInfo appinfo = packageManager.getApplicationInfo(pkgname, PackageManager.GET_ACTIVITIES);
			if (appinfo != null)
			{
				Log.i(TAG, "set notification enable :" + pkgname+",isEnable:"+isEnable);
				mBackend.setNotificationsBanned(pkgname, appinfo.uid, !isEnable);
			}
		}
		catch (Exception e)
		{
		}
	}

	private Backend mBackend = new Backend();

	public static class Backend
	{

		private INotificationManager sINM;

		public Backend()
		{
			sINM = INotificationManager.Stub.asInterface(ServiceManager.getService(Context.NOTIFICATION_SERVICE));
		}

		public boolean setNotificationsBanned(String pkg, int uid, boolean banned)
		{
			try
			{
				sINM.setNotificationsEnabledForPackage(pkg, uid, !banned);
				return true;
			}
			catch (Exception e)
			{
				return false;
			}
		}

		public boolean getNotificationsBanned(String pkg, int uid)
		{
			try
			{
				boolean enabled = sINM.areNotificationsEnabledForPackage(pkg, uid);
				return !enabled;
			}
			catch (Exception e)
			{
				return false;
			}
		}

	}

	// /////////////////////////////////////////////////////////////////////////////////////
	public void updateWhiteListFloatwindowData(ArrayList<String> svrenablelist)
	{		
		//check repeatitem
		DatabaseUtil.checkRepeatItem(svrenablelist);		
		
		//delete old svr config data
		DatabaseUtil.deleteFloatwindowWhiteDb(mContext, WhiteListColumns.ENABLE,WhiteListColumns.SERVER_CONFIG);
		//save new svr config data
		WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
		for (int i = 0; i < svrenablelist.size(); i++)
		{
			Log.i(TAG, "updateWhiteListFloatwindowData set floatenable :" + svrenablelist.get(i));
			DatabaseUtil.deleteFloatwindowWhiteDb(mContext,svrenablelist.get(i),WhiteListColumns.LOCAL_CONFIG);
			DatabaseUtil.insertFloatwindowDb(mContext, svrenablelist.get(i), WhiteListColumns.ENABLE,WhiteListColumns.SERVER_CONFIG);
			wm.setFloatEnable(svrenablelist.get(i), true);
		}

		Intent intent = new Intent(ACTION_UPDATE_DATA);
		intent.putExtra("type", WHITELIST_TYPE_FLOATWINDOW);
		// sendBroadcast(intent);
	}

	// ///////////////////////////////////////////////////////////////////////////////
	private static final String FILE_DIR = "data/com.mediatek.security";
	private static final String FILE_NAME = "bootreceiver.xml";
	private static final String PKG_TAG = "pkg";
	private int mUid;

	public ArrayList<String> loadAutoLaunchDenyListFromFile()
	{
		Log.d(TAG, "loadAutoLaunchDenyListFromFile()");
		File dataDir = Environment.getDataDirectory();
		File systemDir = new File(dataDir, FILE_DIR);
		File mFile = new File(systemDir, FILE_NAME);

		ArrayList<String> results = new ArrayList<String>();

		FileInputStream stream = null;
		boolean success = false;
		try
		{
			stream = new FileInputStream(mFile);
			XmlPullParser parser = Xml.newPullParser();
			parser.setInput(stream, null);
			int type;
			while ((type = parser.next()) != XmlPullParser.START_TAG && type != XmlPullParser.END_DOCUMENT)
			{
				;
			}

			if (type != XmlPullParser.START_TAG)
			{
				Log.e(TAG, "loadAutoLaunchDenyListFromFile can not find " + XmlPullParser.START_TAG);
				return results;
			}

			int outerDepth = parser.getDepth();
			while ((type = parser.next()) != XmlPullParser.END_DOCUMENT && (type != XmlPullParser.END_TAG || parser.getDepth() > outerDepth))
			{
				if (type == XmlPullParser.END_TAG || type == XmlPullParser.TEXT)
				{
					continue;
				}
				String tagName = parser.getName();
				if (tagName.equals(PKG_TAG))
				{
					String pkgName = parser.getAttributeValue(null, "n");
					int userId = Integer.parseInt(parser.getAttributeValue(null, "u"));
					boolean enabled = Boolean.parseBoolean(parser.getAttributeValue(null, "e"));
					if (enabled == true)
					{
						Log.e(TAG, "get autolaunch from file " + pkgName + " enabled:" + enabled);
					}
					Log.d(TAG, "Read package name: " + pkgName + " enabled: " + enabled + " at User(" + userId + ")");
					if (mUid == userId)
					{
						results.add(pkgName);
					}
				}
				else
				{
					Log.w(TAG, "Unknown element under <boot-receiver>: " + parser.getName());
					XmlUtils.skipCurrentTag(parser);
				}
			}
			success = true;
		}
		catch (IllegalStateException e)
		{
			Log.w(TAG, "Failed parsing " + e);
		}
		catch (NullPointerException e)
		{
			Log.w(TAG, "Failed parsing " + e);
		}
		catch (NumberFormatException e)
		{
			Log.w(TAG, "Failed parsing " + e);
		}
		catch (XmlPullParserException e)
		{
			Log.w(TAG, "Failed parsing " + e);
		}
		catch (FileNotFoundException e)
		{
			Log.i(TAG, "No existing " + mFile + "; starting empty");
		}
		catch (IOException e)
		{
			Log.w(TAG, "Failed parsing " + e);
		}
		catch (IndexOutOfBoundsException e)
		{
			Log.w(TAG, "Failed parsing " + e);			
		}
		finally
		{
			try
			{
				if (stream != null)
				{
					stream.close();
				}
			}
			catch (IOException e)
			{
				Log.e(TAG, "Fail to read receiver list");
			}
		}

		return results;
	}

	public void saveAutolaunchDenyToFile(ArrayList<String> denyList)
	{
		Log.d(TAG, "saveAutolaunchDenyToFile");
		File dataDir = Environment.getDataDirectory();
		File systemDir = new File(dataDir, FILE_DIR);
		File mFile = new File(systemDir, FILE_NAME);

		if (mFile.exists() && (!mFile.getAbsoluteFile().delete()))
		{
			Log.e(TAG, "saveAutolaunchDenyToFile, deleteFile failed");
		}

		if (denyList.size() == 0)
		{
			Log.d(TAG, "saveAutolaunchDenyToFile, size = 0");
			return;
		}

		FileOutputStream os = null;
		try
		{
			mFile.getAbsoluteFile().createNewFile();
			os = new FileOutputStream(mFile);
			XmlSerializer xml = Xml.newSerializer();
			xml.setOutput(os, "UTF-8");
			xml.startDocument("UTF-8", true);
			xml.startTag(null, "bootlist");
			for (String name : denyList)
			{
				Log.d(TAG, "saveToFile, tag = " + name);
				xml.startTag(null, PKG_TAG);
				xml.attribute(null, "n", name);
				xml.attribute(null, "u", String.valueOf(mUid));
				xml.attribute(null, "e", "false");
				xml.endTag(null, PKG_TAG);
			}
			xml.endTag(null, "bootlist");
			xml.endDocument();
		}
		catch (IOException | IllegalArgumentException | IllegalStateException e)
		{
			Log.e(TAG, "saveAutolaunchDenyToFile, exception + " + e);
			e.printStackTrace();
		}
		finally
		{
			if (os != null)
			{
				try
				{
					os.close();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
		// write to settings
		String denyAutoAppList = "";
		for (String name : denyList)
		{
			denyAutoAppList += name + ",";
		}
		Settings.System.putString(mContext.getContentResolver(), "denyAutoAppList", denyAutoAppList);
	}

	public static boolean isSystemApp(ApplicationInfo applicationInfo)
	{
		if (applicationInfo != null)
		{
			int appId = UserHandle.getAppId(applicationInfo.uid);
			return ((applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) || (appId == Process.SYSTEM_UID);
		}
		else
		{
			return false;
		}
	}
	private ArrayList<String> getBootReceivers(Context context)
	{
		ArrayList<String> bootReceivers = new ArrayList<String>();
		String[] policy = context.getResources().getStringArray(com.mediatek.internal.R.array.config_auto_boot_policy_intent_list);
		Log.d(TAG, "getBootReceivers, policy:" + policy);
		IPackageManager pm = IPackageManager.Stub.asInterface(ServiceManager.getService("package"));

		for (int i = 0; i < policy.length; i++)
		{
			Intent intent = new Intent(policy[i]);
			try
			{
				ParceledListSlice<ResolveInfo> parlist = pm.queryIntentReceivers(intent, intent.resolveTypeIfNeeded(context.getContentResolver()), PackageManager.GET_META_DATA, mUid);
				if (parlist != null)
				{
					List<ResolveInfo> receivers = parlist.getList();
					if (receivers != null)
					{
						for (int j = 0; j < receivers.size(); j++)
						{
							ResolveInfo info = receivers.get(j);
							String packageName = (info.activityInfo != null) ? info.activityInfo.packageName : null;
							ApplicationInfo appinfo = pm.getApplicationInfo(packageName, 0, mUid);
							if (isSystemApp(appinfo))
							{
								continue;
							}
							if (packageName != null && !bootReceivers.contains(packageName))
							{
								Log.d(TAG, "getBootReceivers, packageName:" + packageName);
								bootReceivers.add(packageName);
							}
						}
					}
				}
			}
			catch (RemoteException e)
			{
				// Should never happend
				continue;
			}
		}
		return bootReceivers;
	}
	public void updateWhiteListAutoLaunchData(ArrayList<String> svrdefenablelist)
	{
		//check repeatitem
		DatabaseUtil.checkRepeatItem(svrdefenablelist);
				
		//delete old svr config data
		DatabaseUtil.deleteAutolaunchWhiteDb(mContext, WhiteListColumns.DEFENABLE,WhiteListColumns.SERVER_CONFIG);
		ArrayList<DbRecord> localdefenablelist = DatabaseUtil.getAutoLaunchList(mContext,WhiteListColumns.DEFENABLE,WhiteListColumns.LOCAL_CONFIG);
		ArrayList<String> defenablelist = new ArrayList<String>();
		//save new svr config data
		for (int i = 0; i < svrdefenablelist.size(); i++)
		{
			Log.i(TAG,"updateWhiteListAutoLaunchData insert AutoLaunch:"+svrdefenablelist.get(i));
			defenablelist.add(svrdefenablelist.get(i));
			DatabaseUtil.insertAutolaunchDb(mContext, svrdefenablelist.get(i), WhiteListColumns.DEFENABLE,WhiteListColumns.SERVER_CONFIG);
		}
		for(int i=0;i<localdefenablelist.size();i++)
		{
			defenablelist.add(localdefenablelist.get(i).pkgname);
		}
		
		ArrayList<String> bootrecvlist = getBootReceivers(mContext);
		ArrayList<String> denylist = loadAutoLaunchDenyListFromFile();
		
		for(int i=0;i<bootrecvlist.size();i++)
		{
			String pkg = bootrecvlist.get(i);
			if(defenablelist.contains(pkg))//can auto launch
			{
				if(denylist.contains(pkg))
				{
					Log.i(TAG,"updateWhiteListAutoLaunchData  remove from denylist:"+pkg);
					denylist.remove(pkg);
				}
			}
			else
			{
				if(!denylist.contains(pkg))
				{
					Log.i(TAG,"updateWhiteListAutoLaunchData  add to denylist:"+pkg);
					denylist.add(pkg);
				}
			}
		}	

		saveAutolaunchDenyToFile(denylist);

		
		Intent intent = new Intent(ACTION_UPDATE_DATA);
		intent.putExtra("type", WHITELIST_TYPE_AUTOLAUNCH);
		mContext.sendBroadcast(intent);		
	}

	// //////////////////////////////////////////////////////////////////////////
	/**
	 * netType: wifi 1, 3g 0 true:forbidden network
	 */
	private void setNetwork(int uid, int netType)
	{
		try
		{
			INetworkManagementService netMgr = INetworkManagementService.Stub.asInterface(ServiceManager.getService(Context.NETWORKMANAGEMENT_SERVICE));
			netMgr.setFirewallUidChainRule(uid, netType, true);
		}
		catch (Exception e)
		{
			// e.printStackTrace();
		}
	}

	public void updateWhiteListNetForbadeData(ArrayList<String> disablelist)
	{
		//check repeatitem
		DatabaseUtil.checkRepeatItem(disablelist);
				
		//delete old svr config data
		DatabaseUtil.deleteNetForbadeWhiteDb(mContext, WhiteListColumns.DISABLE,WhiteListColumns.SERVER_CONFIG);
		//save new svr config data
		PackageManager packageManager = mContext.getPackageManager();
		for (int i = 0; i < disablelist.size(); i++)
		{
			String pkgname = disablelist.get(i);
			Log.i(TAG,"insert NetForbade:"+pkgname);
			
			DatabaseUtil.insertNetForbadeDb(mContext, pkgname, WhiteListColumns.DISABLE,WhiteListColumns.SERVER_CONFIG);
			
			try
			{
				ApplicationInfo appinfo = packageManager.getApplicationInfo(pkgname, PackageManager.GET_ACTIVITIES);
				 if(appinfo != null)
				 {
					 int userid = appinfo.uid;
					 setNetwork(userid,0);
					 setNetwork(userid,1);
					 Log.i(TAG,"setNetworkForbided "+pkgname+" userid:"+userid);
				 }
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}

		Intent intent = new Intent(ACTION_UPDATE_DATA);
		intent.putExtra("type", WHITELIST_TYPE_NETFORBADE);
		// sendBroadcast(intent);
	}
	////////////////////////////////////////////////////////////////////////////////////////////////
	public void updateWhiteListReleateWakeupData(ArrayList<ReleateWakeupItem>whitelistdata)
	{
		//delete old svr config data
		DatabaseUtil.deleteReleateWakeupSvrWhiteDb(mContext,WhiteListColumns.SERVER_CONFIG);
		//save new svr config data
		for (int i = 0; i < whitelistdata.size(); i++)
		{
			ReleateWakeupItem wakeupitem = whitelistdata.get(i);
			Log.i(TAG,"insert ReleateWakeup:"+wakeupitem.toString());
			DatabaseUtil.insertReleateWakeupDb(mContext, wakeupitem.targetpkg, wakeupitem.action, wakeupitem.classname, wakeupitem.callerpkg, wakeupitem.state,WhiteListColumns.SERVER_CONFIG);
		}
		
		Intent intent = new Intent(ACTION_UPDATE_DATA);
		intent.putExtra("type", WHITELIST_TYPE_RELEATEWAKEUP);
		mContext.sendBroadcast(intent);
	}
	////////////////////////////////////////////////////////////////////////////////////////
	public void updateWhiteListSleepNetData(ArrayList<String>whitelistdata)
	{
		//check repeatitem
		DatabaseUtil.checkRepeatItem(whitelistdata);
				
		//delete old svr config data
		DatabaseUtil.deleteSleepNetWhiteDb(mContext, WhiteListColumns.ENABLE,WhiteListColumns.SERVER_CONFIG);
		//save new svr config data
		for (int i = 0; i < whitelistdata.size(); i++)
		{
			String pkgname = whitelistdata.get(i);	
			Log.i(TAG,"insert SleepNet:"+pkgname);
			DatabaseUtil.insertSleepNetDb(mContext, pkgname, WhiteListColumns.ENABLE,WhiteListColumns.SERVER_CONFIG);
		}
		Intent intent = new Intent(ACTION_UPDATE_DATA);
		intent.putExtra("type", WHITELIST_TYPE_SLEEPNET);
		mContext.sendBroadcast(intent);
	}
	//////////////////////////////////////////////////////////////////////////////////////////
	public void updateWhiteListBlockActivity(ArrayList<String>whitelistdata)
	{
		//check repeatitem
		DatabaseUtil.checkRepeatItem(whitelistdata);
				
		//delete old svr config data
		DatabaseUtil.deleteBlockActivityWhiteDb(mContext, WhiteListColumns.DISABLE,WhiteListColumns.SERVER_CONFIG);
		//save new svr config data
		for (int i = 0; i < whitelistdata.size(); i++)
		{
			String pkgname = whitelistdata.get(i);
			Log.i(TAG,"insert blockactivity:"+pkgname);
			DatabaseUtil.insertBlockActivityDb(mContext, pkgname, WhiteListColumns.DISABLE,WhiteListColumns.SERVER_CONFIG);
		}
		
		Intent intent = new Intent(ACTION_UPDATE_DATA);
		intent.putExtra("type", WHITELIST_TYPE_BLOCKACTIVITY);
		mContext.sendBroadcast(intent);		
	}
	public void updateWhiteListDozeWhiteData(ArrayList<String>whitelistdata)
	{
		//check repeatitem
		DatabaseUtil.checkRepeatItem(whitelistdata);
				
		//delete old svr config data
		DatabaseUtil.deleteDozeWhiteDb(mContext, WhiteListColumns.ENABLE,WhiteListColumns.SERVER_CONFIG);
		//save new svr config data
		for (int i = 0; i < whitelistdata.size(); i++)
		{
			String pkgname = whitelistdata.get(i);	
			Log.i(TAG,"insert DozeWhite:"+pkgname);
			DatabaseUtil.insertDozeWhiteDb(mContext, pkgname, WhiteListColumns.ENABLE,WhiteListColumns.SERVER_CONFIG);
		}
		Intent intent = new Intent(ACTION_UPDATE_DATA);
		intent.putExtra("type", WHITELIST_TYPE_DOZEWHITE);
		mContext.sendBroadcast(intent);
	}
	/////////////////////////////////////////////////////////////////////////////////////
	public void updateWhiteListProviderWakeupData(ArrayList<ProviderWakeupItem>whitelistdata)
	{
		//delete old svr config data
		DatabaseUtil.deleteProviderWakeupSvrWhiteDb(mContext,WhiteListColumns.SERVER_CONFIG);
		//save new svr config data
		for (int i = 0; i < whitelistdata.size(); i++)
		{
			ProviderWakeupItem wakeupitem = whitelistdata.get(i);
			Log.i(TAG,"insert ProviderWakeupItem:"+wakeupitem.toString());
			DatabaseUtil.insertProviderWakeupDb(mContext, wakeupitem.targetpkg,  wakeupitem.classname, wakeupitem.callerpkg, wakeupitem.state,WhiteListColumns.SERVER_CONFIG);
		}
		
		Intent intent = new Intent(ACTION_UPDATE_DATA);
		intent.putExtra("type", WHITELIST_TYPE_PROVIDERWAKEUP);
		mContext.sendBroadcast(intent);
	}
	/////////////////////////////////////////////////////////////////////////////
	public boolean updateScreenOffKillWhiteData(ArrayList<String> allkillwhitelist,ArrayList<String> mornkillwhitelist,
			ArrayList<String> screenoffkillwhitelist,ArrayList<String> calledkilwhitellist,ArrayList<String> protectprocesswhitelist,
			ArrayList<String> protectservicewhitelist,ArrayList<String> forcestopwhitelist,ArrayList<String> dozemodewhitelist,
			ArrayList<String> musicwhitelist,ArrayList<String> mapwhitelist)
	{
		//check repeatitem
		DatabaseUtil.checkRepeatItem(allkillwhitelist);
		DatabaseUtil.checkRepeatItem(mornkillwhitelist);
		DatabaseUtil.checkRepeatItem(screenoffkillwhitelist);
		DatabaseUtil.checkRepeatItem(calledkilwhitellist);
		DatabaseUtil.checkRepeatItem(protectprocesswhitelist);
		DatabaseUtil.checkRepeatItem(protectservicewhitelist);
		DatabaseUtil.checkRepeatItem(forcestopwhitelist);
		DatabaseUtil.checkRepeatItem(dozemodewhitelist);
		DatabaseUtil.checkRepeatItem(musicwhitelist);
		DatabaseUtil.checkRepeatItem(mapwhitelist);
		
		Log.d(TAG,"updateScreenOffKillWhiteData musiclist:"+musicwhitelist.size());
		Log.d(TAG,"updateScreenOffKillWhiteData mapwhitelist:"+mapwhitelist.size());
		//save to xml file
		String filepath = CloudUtils.getScreenOffKillDataFilePath(mContext);
		String filepathbk = filepath + ".bk";
		
		File filebk = new File(filepathbk);
		if(filebk.exists())
		{
			filebk.delete();
		}
		boolean ret = false;		
		FileOutputStream fstr = null;
		BufferedOutputStream str = null;
		try
		{
			fstr = new FileOutputStream(filebk);
			str = new BufferedOutputStream(fstr);

			final XmlSerializer serializer = new FastXmlSerializer();
			serializer.setOutput(str, "utf-8");
			serializer.startDocument(null, true);
			
			serializer.startTag(null, "PrizeSysClearFilter");
			
			for(int i=0;i<allkillwhitelist.size();i++)
			{
				serializer.startTag(null, "PackageFilterList");		
				serializer.text(allkillwhitelist.get(i));
				serializer.endTag(null, "PackageFilterList");
			}
			
			for(int i=0;i<mornkillwhitelist.size();i++)
			{
				serializer.startTag(null, "MorningKillFilterList");		
				serializer.text(mornkillwhitelist.get(i));
				serializer.endTag(null, "MorningKillFilterList");
			}
			
			for(int i=0;i<screenoffkillwhitelist.size();i++)
			{
				serializer.startTag(null, "ScreenOffKillFilterList");		
				serializer.text(screenoffkillwhitelist.get(i));
				serializer.endTag(null, "ScreenOffKillFilterList");
			}
			
			for(int i=0;i<calledkilwhitellist.size();i++)
			{
				serializer.startTag(null, "CalledKillFilterList");		
				serializer.text(calledkilwhitellist.get(i));
				serializer.endTag(null, "CalledKillFilterList");
			}
			
			for(int i=0;i<protectprocesswhitelist.size();i++)
			{
				serializer.startTag(null, "ProtectProcessList");		
				serializer.text(protectprocesswhitelist.get(i));
				serializer.endTag(null, "ProtectProcessList");
			}
			
			for(int i=0;i<protectservicewhitelist.size();i++)
			{
				serializer.startTag(null, "ProtectServiceInfo");		
				serializer.text(protectservicewhitelist.get(i));
				serializer.endTag(null, "ProtectServiceInfo");
			}
			
			for(int i=0;i<forcestopwhitelist.size();i++)
			{
				serializer.startTag(null, "ForceStopFilterList");		
				serializer.text(forcestopwhitelist.get(i));
				serializer.endTag(null, "ForceStopFilterList");
			}
			
			for(int i=0;i<dozemodewhitelist.size();i++)
			{
				serializer.startTag(null, "DozeModeFilterList");		
				serializer.text(dozemodewhitelist.get(i));
				serializer.endTag(null, "DozeModeFilterList");
			}
			
			for(int i=0;i<musicwhitelist.size();i++)
			{
				serializer.startTag(null, "MusicFilterList");		
				serializer.text(musicwhitelist.get(i));
				serializer.endTag(null, "MusicFilterList");
			}
			
			for(int i=0;i<mapwhitelist.size();i++)
			{
				serializer.startTag(null, "MapFilterList");		
				serializer.text(mapwhitelist.get(i));
				serializer.endTag(null, "MapFilterList");
			}
			
			serializer.endTag(null, "PrizeSysClearFilter");
			serializer.endDocument();

			str.flush();
			CloudUtils.sync(fstr);
			fstr.close();
			fstr = null;
			str.close();
			str = null;
			ret = true;
		}
		catch (Exception e)
		{
			ret = false;
		}
		finally
		{
			try
			{
				if (str != null)
				{
					str.close();
				}
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			try
			{
				if (fstr != null)
				{
					fstr.close();
				}
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		
		if(ret)
		{
			//ret = filebk.renameTo(new File(filepath));
			ret = CloudUtils.copyFile(filebk, new File(filepath));
			Log.d(TAG,"updateScreenOffKillWhiteData save to "+filepath+",ret:"+ret);
		}
		
		return ret;
	}
	
	public boolean updateSmartKillWhiteData(ArrayList<String> protectprocesslist,ArrayList<String> protectpackagelist)
	{
		//check repeatitem
		DatabaseUtil.checkRepeatItem(protectprocesslist);
		DatabaseUtil.checkRepeatItem(protectpackagelist);		
		
		//save to xml file
		String filepath = CloudUtils.getSmartKillDataFilePath(mContext);
		String filepathbk = filepath + ".bk";
		
		File filebk = new File(filepathbk);
		if(filebk.exists())
		{
			filebk.delete();
		}
		boolean ret = false;		
		FileOutputStream fstr = null;
		BufferedOutputStream str = null;
		try
		{
			fstr = new FileOutputStream(filebk);
			str = new BufferedOutputStream(fstr);

			final XmlSerializer serializer = new FastXmlSerializer();
			serializer.setOutput(str, "utf-8");
			serializer.startDocument(null, true);
			
			serializer.startTag(null, "PrizeSmartKillFilter");
			
			for(int i=0;i<protectpackagelist.size();i++)
			{
				serializer.startTag(null, "ProtectPackage");		
				serializer.text(protectpackagelist.get(i));
				serializer.endTag(null, "ProtectPackage");
			}
			
			for(int i=0;i<protectprocesslist.size();i++)
			{
				serializer.startTag(null, "ProtectProcess");		
				serializer.text(protectprocesslist.get(i));
				serializer.endTag(null, "ProtectProcess");
			}			

			serializer.endTag(null, "PrizeSmartKillFilter");
			serializer.endDocument();

			str.flush();
			CloudUtils.sync(fstr);
			fstr.close();
			fstr = null;
			str.close();
			str = null;
			ret = true;
		}
		catch (Exception e)
		{
			ret = false;
		}
		finally
		{
			try
			{
				if (str != null)
				{
					str.close();
				}
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			try
			{
				if (fstr != null)
				{
					fstr.close();
				}
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		
		if(ret)
		{
			//ret = filebk.renameTo(new File(filepath));
			ret = CloudUtils.copyFile(filebk, new File(filepath));
			Log.d(TAG,"updateSmartKillWhiteData save to "+filepath+",ret:"+ret);
		}
		
		return ret;
	}
	//////////////////////////////////////////////////////////////////
}
