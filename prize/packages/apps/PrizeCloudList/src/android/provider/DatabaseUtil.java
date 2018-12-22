package android.provider;

import java.util.ArrayList;

import com.prize.cloudlist.CloudUtils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public class DatabaseUtil
{
	private static final String TAG = CloudUtils.TAG;

	public static class DbRecord
	{
		public int recordId;
		public String pkgname;
		public int status;//
		public int isserver;
	}

	public static void deletePurebackgroundWhiteDb(Context context, int status, int isserver)
	{
		try
		{
			int rows = context.getContentResolver().delete(WhiteListColumns.Purebackground.CONTENT_URI, 
					WhiteListColumns.BaseColumns.ENABLE + "=? AND "
					+WhiteListColumns.BaseColumns.ISSERVERCONFIG+"=?", 
					new String[]{"" + status,""+isserver});
			Log.i(TAG, "deletePurebackgroundWhiteDb rows:" + rows + "/" + status+"/"+isserver);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static void deletePurebackgroundSvrRecord(Context context, DbRecord item)
	{
		try
		{
			int rows = context.getContentResolver().delete(WhiteListColumns.Purebackground.CONTENT_URI, 
					WhiteListColumns.BaseColumns._ID + "=? ", 
					new String[]{"" + item.recordId	});
			Log.i(TAG, "deletePurebackgroundSvrRecord rows:" + rows +",recordid:"+item.recordId+ ",item:" + item.pkgname + "/" + item.status+"/"+item.isserver);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static void updatePurebackgroundSvrRecord(Context context, DbRecord item)
	{
		try
		{
			ContentValues values = new ContentValues();
			values.put(WhiteListColumns.BaseColumns.ENABLE, item.status);
			values.put(WhiteListColumns.BaseColumns.ISSERVERCONFIG, item.isserver);
			int ret = context.getContentResolver().update(WhiteListColumns.Purebackground.CONTENT_URI, values, 
					WhiteListColumns.BaseColumns._ID + "=?",
					new String[]{"" + item.recordId});
			Log.i(TAG, "update ret:" + ret + ",pkg:" + item.pkgname + ",status:" + item.status);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static ArrayList<DbRecord> getPurebackgroundList(Context context, int status,int isserver)
	{
		ArrayList<DbRecord> itemlist = new ArrayList<DbRecord>();
		Cursor cursor = null;
		try
		{
			cursor = context.getContentResolver().query(WhiteListColumns.Purebackground.CONTENT_URI, 
					new String[]
					{
					WhiteListColumns.BaseColumns._ID, 
					WhiteListColumns.BaseColumns.PKGNAME
					}, 
					WhiteListColumns.BaseColumns.ENABLE + "=?  AND "
					+WhiteListColumns.BaseColumns.ISSERVERCONFIG+"=?",					
					new String[]
					{"" + status,""+isserver}, null);
			if (cursor != null)
			{
				while (cursor.moveToNext())
				{
					DbRecord record = new DbRecord();
					record.recordId = cursor.getInt(0);
					record.pkgname = cursor.getString(1);
					record.status = status;
					record.isserver = isserver;
					itemlist.add(record);
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (cursor != null)
			{
				cursor.close();
				cursor = null;
			}
		}
		return itemlist;
	}

	/**
	 * update hidelist/defenablelist/notkilllist
	 * 
	 * @param context
	 * @param pkgname
	 * @param isEnable
	 */
	public static void insertPurebackgroundDb(Context context, String pkgname, int isEnable,int isserver)
	{
		ContentValues values = new ContentValues();
		Cursor cursor = null;
		int retId = -1;
		try
		{
			cursor = context.getContentResolver().query(WhiteListColumns.Purebackground.CONTENT_URI, 
			    new String[]
				{
					WhiteListColumns.BaseColumns._ID
				}, 
				WhiteListColumns.BaseColumns.PKGNAME + "=?" + " AND " 
				+ WhiteListColumns.BaseColumns.ENABLE + "=?  AND "
				+WhiteListColumns.BaseColumns.ISSERVERCONFIG+"=?", 
				new String[]
				{
						pkgname, "" + isEnable,""+isserver
				}, null);
			if (cursor != null)
			{
				int count = cursor.getCount();
				if (count > 0)
				{
					cursor.moveToNext();
					retId = cursor.getInt(0);
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (cursor != null)
			{
				cursor.close();
				cursor = null;
			}
		}
		try
		{
			if (retId > 0)// update
			{
				// values.put(WhiteListColumns.BaseColumns.ENABLE,isEnable); 
				//int ret = context.getContentResolver().update(WhiteListColumns. Purebackground.CONTENT_URI, values,
				// WhiteListColumns.BaseColumns._ID+"=?",new
				// String[]{(""+retId)}); 
				//Log.i(TAG,"update ret:"+ret);
				 
				Log.i(TAG, "update already id:" + retId);
				return;
			}
			else
			// insert
			{
				values.put(WhiteListColumns.BaseColumns.PKGNAME, pkgname);
				values.put(WhiteListColumns.BaseColumns.ENABLE, isEnable);
				values.put(WhiteListColumns.BaseColumns.ISSERVERCONFIG, isserver);
				Uri uri = context.getContentResolver().insert(WhiteListColumns.Purebackground.CONTENT_URI, values);
				if (uri != null)
				{
					Log.i(TAG, "insert uri:" + uri);
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	// ///////////////////////////////////////////////////////////////////////////////////////////////////////
	public static void deleteNotificationSvrRecord(Context context, DbRecord item)
	{
		try
		{
			int rows = context.getContentResolver().delete(WhiteListColumns.Notification.CONTENT_URI, 
					WhiteListColumns.BaseColumns._ID + "=? ", 
					new String[]{"" + item.recordId	});
			Log.i(TAG, "deleteNotificationSvrRecord rows:" + rows + ",item:" + item.pkgname + "/" + item.status+"/"+item.isserver);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static ArrayList<DbRecord> getNotificationList(Context context, int status,int isserver)
	{
		ArrayList<DbRecord> itemlist = new ArrayList<DbRecord>();
		Cursor cursor = null;
		try
		{
			cursor = context.getContentResolver().query(WhiteListColumns.Notification.CONTENT_URI, 
					new String[]
			{
					WhiteListColumns.BaseColumns._ID, WhiteListColumns.BaseColumns.PKGNAME
			}, 
			WhiteListColumns.BaseColumns.ENABLE + "=?  AND "
			+WhiteListColumns.BaseColumns.ISSERVERCONFIG+"=?",
			new String[]{"" + status,""+isserver}, null);
			if (cursor != null)
			{
				while (cursor.moveToNext())
				{
					DbRecord record = new DbRecord();
					record.recordId = cursor.getInt(0);
					record.pkgname = cursor.getString(1);
					record.status = status;
					record.isserver = isserver;
					
					itemlist.add(record);
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (cursor != null)
			{
				cursor.close();
				cursor = null;
			}
		}
		return itemlist;
	}

	/**
	 * update hidelist/defenablelist
	 * 
	 * @param context
	 * @param pkgname
	 * @param isEnable
	 */
	public static void insertNotificationDb(Context context, String pkgname, int isEnable,int isserver)
	{
		ContentValues values = new ContentValues();
		Cursor cursor = null;
		int retId = -1;
		try
		{
			cursor = context.getContentResolver().query(WhiteListColumns.Notification.CONTENT_URI, 
			new String[]
			{
				WhiteListColumns.BaseColumns._ID
			}, 
			WhiteListColumns.BaseColumns.PKGNAME + "=? AND " 
			+ WhiteListColumns.BaseColumns.ENABLE + "=? AND "
			+WhiteListColumns.BaseColumns.ISSERVERCONFIG+"=?",
			new String[]{pkgname, "" + isEnable,""+isserver	}, null);
			if (cursor != null)
			{
				int count = cursor.getCount();
				if (count > 0)
				{
					cursor.moveToNext();
					retId = cursor.getInt(0);
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (cursor != null)
			{
				cursor.close();
				cursor = null;
			}
		}
		try
		{
			if (retId > 0)// update
			{
				// values.put(WhiteListColumns.BaseColumns.ENABLE,isEnable); 
				//int ret = context.getContentResolver().update(WhiteListColumns.Notification.CONTENT_URI, values,
				// WhiteListColumns.BaseColumns._ID+"=?",new String[]{(""+retId)}); 
				//Log.i(TAG,"update ret:"+ret);
				 
				Log.i(TAG, "update already id:" + retId);
				return;
			}
			else
			// insert
			{
				values.put(WhiteListColumns.BaseColumns.PKGNAME, pkgname);
				values.put(WhiteListColumns.BaseColumns.ENABLE, isEnable);
				values.put(WhiteListColumns.BaseColumns.ISSERVERCONFIG, isserver);
				Uri uri = context.getContentResolver().insert(WhiteListColumns.Notification.CONTENT_URI, values);
				if (uri != null)
				{
					Log.i(TAG, "insert uri:" + uri);
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	// //////////////////////////////////////////////////////////////////////
	public static void deleteFloatwindowWhiteDb(Context context, int status, int isserver)
	{
		try
		{
			int rows = context.getContentResolver().delete(WhiteListColumns.FloatWindow.CONTENT_URI, 
					WhiteListColumns.BaseColumns.ENABLE + "=? AND "
					+WhiteListColumns.BaseColumns.ISSERVERCONFIG+"=?", 
					new String[]{"" + status, "" + isserver});
			Log.i(TAG, "deleteFloatwindowWhiteDb rows:" + rows + "/" + status+"/"+isserver);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	public static void deleteFloatwindowWhiteDb(Context context, String pkgname, int isserver)
	{
		try
		{
			int rows = context.getContentResolver().delete(WhiteListColumns.FloatWindow.CONTENT_URI, 
					WhiteListColumns.BaseColumns.PKGNAME + "=? AND "
					+WhiteListColumns.BaseColumns.ISSERVERCONFIG+"=?", 
					new String[]{pkgname, "" + isserver});
			Log.i(TAG, "deleteFloatwindowWhiteDb rows:" + rows + "/" + pkgname+"/"+isserver);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	/**
	 * update enablelist
	 * 
	 * @param context
	 * @param pkgname
	 * @param isEnable
	 */
	public static void insertFloatwindowDb(Context context, String pkgname, int isEnable,int isserver)
	{
		ContentValues values = new ContentValues();
		Cursor cursor = null;
		int retId = -1;
		try
		{
			cursor = context.getContentResolver().query(WhiteListColumns.FloatWindow.CONTENT_URI, 
			new String[]
			{
				WhiteListColumns.BaseColumns._ID
			}, 
			WhiteListColumns.BaseColumns.PKGNAME + "=?" + " AND " 
			+ WhiteListColumns.BaseColumns.ENABLE + "=?  "+" AND "
			+WhiteListColumns.BaseColumns.ISSERVERCONFIG + "=?",
			new String[]
			{
					pkgname, "" + isEnable,""+isserver
			}, null);
			if (cursor != null)
			{
				int count = cursor.getCount();
				if (count > 0)
				{
					cursor.moveToNext();
					retId = cursor.getInt(0);
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (cursor != null)
			{
				cursor.close();
				cursor = null;
			}
		}
		try
		{
			if (retId > 0)// update
			{
				//values.put(WhiteListColumns.BaseColumns.ENABLE,isEnable); 
				//int ret = context.getContentResolver().update(WhiteListColumns.Notification.CONTENT_URI, values,
				//WhiteListColumns.BaseColumns._ID+"=?",new String[]{(""+retId)}); 
				//Log.i(TAG,"update ret:"+ret);				 
				Log.i(TAG, "update already id:" + retId);
				return;
			}
			else
			// insert
			{
				values.put(WhiteListColumns.BaseColumns.PKGNAME, pkgname);
				values.put(WhiteListColumns.BaseColumns.ENABLE, isEnable);
				values.put(WhiteListColumns.BaseColumns.ISSERVERCONFIG, isserver);
				Uri uri = context.getContentResolver().insert(WhiteListColumns.FloatWindow.CONTENT_URI, values);
				if (uri != null)
				{
					Log.i(TAG, "insert uri:" + uri);
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	public static void deleteAutolaunchWhiteDb(Context context, int status, int isserver)
	{
		try
		{
			int rows = context.getContentResolver().delete(WhiteListColumns.AutoLaunch.CONTENT_URI, 
					WhiteListColumns.BaseColumns.ENABLE + "=? AND "
			        +WhiteListColumns.BaseColumns.ISSERVERCONFIG+"=?",
					new String[]	{"" + status, "" + isserver});
			Log.i(TAG, "deleteAutolaunchWhiteDb rows:" + rows + "/" + status+"/"+isserver);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	public static ArrayList<DbRecord> getAutoLaunchList(Context context, int status,int isserver)
	{
		ArrayList<DbRecord> itemlist = new ArrayList<DbRecord>();
		Cursor cursor = null;
		try
		{
			cursor = context.getContentResolver().query(WhiteListColumns.AutoLaunch.CONTENT_URI, 
					new String[]
					{
					WhiteListColumns.BaseColumns._ID, 
					WhiteListColumns.BaseColumns.PKGNAME
					}, 
					WhiteListColumns.BaseColumns.ENABLE + "=?  AND "
					+WhiteListColumns.BaseColumns.ISSERVERCONFIG+"=?",					
					new String[]
					{"" + status,""+isserver}, null);
			if (cursor != null)
			{
				while (cursor.moveToNext())
				{
					DbRecord record = new DbRecord();
					record.recordId = cursor.getInt(0);
					record.pkgname = cursor.getString(1);
					record.status = status;
					record.isserver = isserver;
					itemlist.add(record);
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (cursor != null)
			{
				cursor.close();
				cursor = null;
			}
		}
		return itemlist;
	}
	/**
	 * update defenablelist
	 * 
	 * @param context
	 * @param pkgname
	 * @param isEnable
	 */
	public static void insertAutolaunchDb(Context context, String pkgname, int isEnable,int isserver)
	{
		ContentValues values = new ContentValues();
		Cursor cursor = null;
		int retId = -1;
		try
		{
			cursor = context.getContentResolver().query(WhiteListColumns.AutoLaunch.CONTENT_URI, new String[]
			{
				WhiteListColumns.BaseColumns._ID
			}, 
			WhiteListColumns.BaseColumns.PKGNAME + "=?" + " AND " 
			+ WhiteListColumns.BaseColumns.ENABLE + "=?" + " AND "
			+WhiteListColumns.BaseColumns.ISSERVERCONFIG + "=?",
			new String[]{pkgname, "" + isEnable,""+isserver	}, null);
			if (cursor != null)
			{
				int count = cursor.getCount();
				if (count > 0)
				{
					cursor.moveToNext();
					retId = cursor.getInt(0);
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (cursor != null)
			{
				cursor.close();
				cursor = null;
			}
		}
		try
		{
			if (retId > 0)// update
			{
				//values.put(WhiteListColumns.BaseColumns.ENABLE,isEnable); 
				//int ret = context.getContentResolver().update(WhiteListColumns.Notification.CONTENT_URI, values,
				//WhiteListColumns.BaseColumns._ID+"=?",new String[]{(""+retId)}); 
				//Log.i(TAG,"update ret:"+ret);				 
				Log.i(TAG, "update already id:" + retId);
				return;
			}
			else
			// insert
			{
				values.put(WhiteListColumns.BaseColumns.PKGNAME, pkgname);
				values.put(WhiteListColumns.BaseColumns.ENABLE, isEnable);
				values.put(WhiteListColumns.BaseColumns.ISSERVERCONFIG, isserver);
				Uri uri = context.getContentResolver().insert(WhiteListColumns.AutoLaunch.CONTENT_URI, values);
				if (uri != null)
				{
					Log.i(TAG, "insert uri:" + uri);
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////

	public static void deleteNetForbadeWhiteDb(Context context, int status, int isserver)
	{
		try
		{
			int rows = context.getContentResolver().delete(WhiteListColumns.NetForbade.CONTENT_URI, 
					WhiteListColumns.BaseColumns.ENABLE + "=? "+ " AND "
					+WhiteListColumns.BaseColumns.ISSERVERCONFIG + "=?", 
					new String[]{"" + status, "" + isserver});
			Log.i(TAG, "deleteNetForbadeWhiteDb rows:" + rows + "/" + status+"/"+isserver);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * update disablelist
	 * 
	 * @param context
	 * @param pkgname
	 * @param isEnable
	 */
	public static void insertNetForbadeDb(Context context, String pkgname, int isEnable,int isserver)
	{
		ContentValues values = new ContentValues();
		Cursor cursor = null;
		int retId = -1;
		try
		{
			cursor = context.getContentResolver().query(WhiteListColumns.NetForbade.CONTENT_URI, 
			new String[]
			{
				WhiteListColumns.BaseColumns._ID
			}, 
			WhiteListColumns.BaseColumns.PKGNAME + "=?" + " AND " 
			+ WhiteListColumns.BaseColumns.ENABLE + "=?  AND "
			+WhiteListColumns.BaseColumns.ISSERVERCONFIG + "=?", 
			new String[]
			{
				pkgname, "" + isEnable,""+isserver
			}, null);
			if (cursor != null)
			{
				int count = cursor.getCount();
				if (count > 0)
				{
					cursor.moveToNext();
					retId = cursor.getInt(0);
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (cursor != null)
			{
				cursor.close();
				cursor = null;
			}
		}
		try
		{
			if (retId > 0)// update
			{
				// values.put(WhiteListColumns.BaseColumns.ENABLE,isEnable); 
				//int ret = context.getContentResolver().update(WhiteListColumns.Notification.CONTENT_URI, values,
				// WhiteListColumns.BaseColumns._ID+"=?",new String[]{(""+retId)}); 
				//Log.i(TAG,"update ret:"+ret);				 
				Log.i(TAG, "update already id:" + retId);
				return;
			}
			else
			// insert
			{
				values.put(WhiteListColumns.BaseColumns.PKGNAME, pkgname);
				values.put(WhiteListColumns.BaseColumns.ENABLE, isEnable);
				values.put(WhiteListColumns.BaseColumns.ISSERVERCONFIG, isserver);
				Uri uri = context.getContentResolver().insert(WhiteListColumns.NetForbade.CONTENT_URI, values);
				if (uri != null)
				{
					Log.i(TAG, "insert uri:" + uri);
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	// //////////////////////////////////////////////////////////////////////////////////////////////
	public static void deleteReleateWakeupSvrWhiteDb(Context context,  int isserver)
	{
		try
		{
			int rows = context.getContentResolver().delete(WhiteListColumns.RelateWakeup.CONTENT_URI, 
					WhiteListColumns.BaseColumns.ISSERVERCONFIG + "=? ", 
					new String[]{"" + isserver});
			Log.i(TAG, "deleteReleateWakeupSvrWhiteDb rows:" + rows + "/" + isserver);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * update enablelist
	 * 
	 * @param context
	 * @param pkgname
	 * @param isEnable
	 */
	public static void insertReleateWakeupDb(Context context, String pkgname, String action, String classname, String callerpkg, int state,int isserver)
	{
		ContentValues values = new ContentValues();
		Cursor cursor = null;
		int retId = -1;
		try
		{
			if (pkgname == null || pkgname.equals("")) pkgname = "null";
			if (action == null || action.equals("")) action = "null";
			if (classname == null || classname.equals("")) classname = "null";
			if (callerpkg == null || callerpkg.equals("")) callerpkg = "null";

			cursor = context.getContentResolver().query(
					WhiteListColumns.RelateWakeup.CONTENT_URI,
					new String[]
					{
						WhiteListColumns.BaseColumns._ID
					},
					WhiteListColumns.BaseColumns.PKGNAME + "=?" + " AND " 
					+ WhiteListColumns.RelateWakeup.ACTION + "=?" + " AND " 
					+ WhiteListColumns.RelateWakeup.CLASS + "=?" + " AND "
					+ WhiteListColumns.RelateWakeup.CALLERPKG + "=?"+" AND "
					+WhiteListColumns.BaseColumns.ISSERVERCONFIG+"=?", 
					new String[]
					{
							"" + pkgname, "" + action, "" + classname, "" + callerpkg,""+isserver
					}, null);
			if (cursor != null)
			{
				int count = cursor.getCount();
				if (count > 0)
				{
					cursor.moveToNext();
					retId = cursor.getInt(0);
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (cursor != null)
			{
				cursor.close();
				cursor = null;
			}
		}
		try
		{
			if (retId > 0)// update
			{
				values.put(WhiteListColumns.BaseColumns.ENABLE, state);
				int ret = context.getContentResolver().update(WhiteListColumns.RelateWakeup.CONTENT_URI, values, 
						WhiteListColumns.BaseColumns._ID + "=?", 
						new String[]{("" + retId)});
				Log.i(TAG, "update ret:" + ret);
				return;
			}
			else
			// insert
			{
				values.put(WhiteListColumns.BaseColumns.PKGNAME, pkgname);
				values.put(WhiteListColumns.RelateWakeup.ACTION, action);
				values.put(WhiteListColumns.RelateWakeup.CLASS, classname);
				values.put(WhiteListColumns.RelateWakeup.CALLERPKG, callerpkg);
				values.put(WhiteListColumns.BaseColumns.ENABLE, state);
				values.put(WhiteListColumns.BaseColumns.ISSERVERCONFIG, isserver);
				Uri uri = context.getContentResolver().insert(WhiteListColumns.RelateWakeup.CONTENT_URI, values);
				if (uri != null)
				{
					Log.i(TAG, "insert uri:" + uri);
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	////////////////////////////////////////////////////////////////////////////
	public static void deleteProviderWakeupSvrWhiteDb(Context context,  int isserver)
	{
		try
		{
			int rows = context.getContentResolver().delete(WhiteListColumns.ProviderWakeup.CONTENT_URI, 
					WhiteListColumns.BaseColumns.ISSERVERCONFIG + "=? ", 
					new String[]{"" + isserver});
			Log.i(TAG, "deleteProviderWakeupSvrWhiteDb rows:" + rows + "/" + isserver);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * update enablelist
	 * 
	 * @param context
	 * @param pkgname
	 * @param isEnable
	 */
	public static void insertProviderWakeupDb(Context context, String pkgname, String classname, String callerpkg, int state,int isserver)
	{
		ContentValues values = new ContentValues();
		Cursor cursor = null;
		int retId = -1;
		try
		{
			if (pkgname == null || pkgname.equals("")) pkgname = "null";			
			if (classname == null || classname.equals("")) classname = "null";
			if (callerpkg == null || callerpkg.equals("")) callerpkg = "null";

			cursor = context.getContentResolver().query(
					WhiteListColumns.ProviderWakeup.CONTENT_URI,
					new String[]
					{
						WhiteListColumns.BaseColumns._ID
					},
					WhiteListColumns.BaseColumns.PKGNAME + "=?" + " AND "					
					+ WhiteListColumns.RelateWakeup.CLASS + "=?" + " AND "
					+ WhiteListColumns.RelateWakeup.CALLERPKG + "=?"+" AND "
					+WhiteListColumns.BaseColumns.ISSERVERCONFIG+"=?", 
					new String[]
					{
							"" + pkgname,  "" + classname, "" + callerpkg,""+isserver
					}, null);
			if (cursor != null)
			{
				int count = cursor.getCount();
				if (count > 0)
				{
					cursor.moveToNext();
					retId = cursor.getInt(0);
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (cursor != null)
			{
				cursor.close();
				cursor = null;
			}
		}
		try
		{
			if (retId > 0)// update
			{
				values.put(WhiteListColumns.BaseColumns.ENABLE, state);
				int ret = context.getContentResolver().update(WhiteListColumns.ProviderWakeup.CONTENT_URI, values, 
						WhiteListColumns.BaseColumns._ID + "=?", 
						new String[]{("" + retId)});
				Log.i(TAG, "update ret:" + ret);
				return;
			}
			else
			// insert
			{
				values.put(WhiteListColumns.BaseColumns.PKGNAME, pkgname);				
				values.put(WhiteListColumns.ProviderWakeup.CLASS, classname);
				values.put(WhiteListColumns.ProviderWakeup.CALLERPKG, callerpkg);
				values.put(WhiteListColumns.BaseColumns.ENABLE, state);
				values.put(WhiteListColumns.BaseColumns.ISSERVERCONFIG, isserver);
				Uri uri = context.getContentResolver().insert(WhiteListColumns.ProviderWakeup.CONTENT_URI, values);
				if (uri != null)
				{
					Log.i(TAG, "insert uri:" + uri);
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	// ////////////////////////////////////////////////////////////////////////
	public static void deleteSleepNetWhiteDb(Context context, int status,int isserver)
	{
		try
		{
			int rows = context.getContentResolver().delete(WhiteListColumns.SleepNetWhite.CONTENT_URI, 
					WhiteListColumns.BaseColumns.ENABLE + "=? AND "
					+WhiteListColumns.BaseColumns.ISSERVERCONFIG+"=?", 
					new String[]{"" + status, "" + isserver});
			Log.i(TAG, "deleteSleepNetWhiteDb rows:" + rows + "/" + status+"/"+isserver);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * update enablelist
	 * 
	 * @param context
	 * @param pkgname
	 * @param isEnable
	 */
	public static void insertSleepNetDb(Context context, String pkgname, int isEnable,int isserver)
	{
		ContentValues values = new ContentValues();
		Cursor cursor = null;
		int retId = -1;
		try
		{
			cursor = context.getContentResolver().query(WhiteListColumns.SleepNetWhite.CONTENT_URI, 
			new String[]
			{
				WhiteListColumns.BaseColumns._ID
			}, 
			WhiteListColumns.BaseColumns.PKGNAME + "=?" + " AND " 
			+ WhiteListColumns.BaseColumns.ENABLE + "=?  "	+" AND "
			+WhiteListColumns.BaseColumns.ISSERVERCONFIG+"=?", 
			new String[]
			{
					pkgname, "" + isEnable,""+isserver
			}, null);
			if (cursor != null)
			{
				int count = cursor.getCount();
				if (count > 0)
				{
					cursor.moveToNext();
					retId = cursor.getInt(0);
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (cursor != null)
			{
				cursor.close();
				cursor = null;
			}
		}
		try
		{
			if (retId > 0)// update
			{
				//values.put(WhiteListColumns.BaseColumns.ENABLE,isEnable); 
				//int ret = context.getContentResolver().update(WhiteListColumns.Notification.CONTENT_URI, values,
				//WhiteListColumns.BaseColumns._ID+"=?",new String[]{(""+retId)}); 
				//Log.i(TAG,"update ret:"+ret);
				 
				Log.i(TAG, "update already id:" + retId);
				return;
			}
			else
			// insert
			{
				values.put(WhiteListColumns.BaseColumns.PKGNAME, pkgname);
				values.put(WhiteListColumns.BaseColumns.ENABLE, isEnable);
				values.put(WhiteListColumns.BaseColumns.ISSERVERCONFIG, isserver);
				Uri uri = context.getContentResolver().insert(WhiteListColumns.SleepNetWhite.CONTENT_URI, values);
				if (uri != null)
				{
					Log.i(TAG, "insert uri:" + uri);
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	// //////////////////////////////////////////////////////////////////////////////////////////
	public static void deleteBlockActivityWhiteDb(Context context, int status,int isserver)
	{
		try
		{
			int rows = context.getContentResolver().delete(WhiteListColumns.BlockActivity.CONTENT_URI,
					WhiteListColumns.BaseColumns.ENABLE + "=? AND "
					+WhiteListColumns.BaseColumns.ISSERVERCONFIG+"=?",
					new String[]{"" + status, "" + isserver});
			Log.i(TAG, "deleteSleepNetWhiteDb rows:" + rows + "/" + status+"/"+isserver);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * update disablelist
	 * 
	 * @param context
	 * @param pkgname
	 * @param isEnable
	 */
	public static void insertBlockActivityDb(Context context, String classname, int isEnable,int isserver)
	{
		ContentValues values = new ContentValues();
		Cursor cursor = null;
		int retId = -1;
		try
		{
			cursor = context.getContentResolver().query(WhiteListColumns.BlockActivity.CONTENT_URI, new String[]
			{
				WhiteListColumns.BaseColumns._ID
			}, 
			WhiteListColumns.BlockActivity.CLASS + "=?"+" AND "
			+WhiteListColumns.BaseColumns.ISSERVERCONFIG+"=?", 
			new String[]
			{
				classname,""+isserver
			}, null);
			if (cursor != null)
			{
				int count = cursor.getCount();
				if (count > 0)
				{
					cursor.moveToNext();
					retId = cursor.getInt(0);
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (cursor != null)
			{
				cursor.close();
				cursor = null;
			}
		}
		try
		{
			if (retId > 0)// update
			{
				//values.put(WhiteListColumns.BaseColumns.ENABLE,isEnable); 
				//int ret = context.getContentResolver().update(WhiteListColumns.Notification.CONTENT_URI, values,
				// WhiteListColumns.BaseColumns._ID+"=?",new String[]{(""+retId)}); 
				//Log.i(TAG,"update ret:"+ret);
				 
				Log.i(TAG, "update already id:" + retId);
				return;
			}
			else
			// insert
			{
				values.put(WhiteListColumns.BaseColumns.PKGNAME, "" + null);
				values.put(WhiteListColumns.BlockActivity.CLASS, classname);
				values.put(WhiteListColumns.BaseColumns.ENABLE, isEnable);
				values.put(WhiteListColumns.BaseColumns.ISSERVERCONFIG, isserver);
				Uri uri = context.getContentResolver().insert(WhiteListColumns.BlockActivity.CONTENT_URI, values);
				if (uri != null)
				{
					Log.i(TAG, "insert uri:" + uri);
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	// /////////////////////////////////////////////////////////////////////
	public static void deleteMsgWhiteDb(Context context, int status, int isserver)
	{
		try
		{
			int rows = context.getContentResolver().delete(WhiteListColumns.MsgWhite.CONTENT_URI,
					WhiteListColumns.BaseColumns.ENABLE + "=? AND " 
					+ WhiteListColumns.BaseColumns.ISSERVERCONFIG + "=? ", 
					new String[]{"" + status, "" + isserver	});
			Log.i(TAG, "deleteMsgWhiteDb rows:" + rows + "/" + status+"/"+isserver);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static ArrayList<String> getMsgWhiteDb(Context context, int status, int isserver)
	{
		ArrayList<String> itemlist = new ArrayList<String>();
		Cursor cursor = null;
		try
		{
			cursor = context.getContentResolver().query(WhiteListColumns.MsgWhite.CONTENT_URI, new String[]
			{
					WhiteListColumns.BaseColumns._ID, WhiteListColumns.BaseColumns.PKGNAME
			}, 
			WhiteListColumns.BaseColumns.ENABLE + "=?  AND " 
			+ WhiteListColumns.BaseColumns.ISSERVERCONFIG + "=? ",
			new String[]
			{
					"" + status, "" + isserver
			}, null);
			if (cursor != null)
			{
				while (cursor.moveToNext())
				{
					String pkgname = cursor.getString(1);
					itemlist.add(pkgname);
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (cursor != null)
			{
				cursor.close();
				cursor = null;
			}
		}
		return itemlist;
	}

	/**
	 * update enablelist
	 * 
	 * @param context
	 * @param pkgname
	 * @param isEnable
	 */
	public static void insertMsgWhiteDb(Context context, String pkgname, int isEnable, int isserver)
	{
		ContentValues values = new ContentValues();
		Cursor cursor = null;
		int retId = -1;
		try
		{
			cursor = context.getContentResolver().query(WhiteListColumns.MsgWhite.CONTENT_URI, 
					new String[]
					{
						WhiteListColumns.BaseColumns._ID
					}, 
					WhiteListColumns.BaseColumns.PKGNAME + "=? AND " 
					+ WhiteListColumns.BaseColumns.ENABLE + "=?  AND " 
					+ WhiteListColumns.BaseColumns.ISSERVERCONFIG + "=? ", 
					new String[]
					{
							pkgname, "" + isEnable, "" + isserver
					}, 
					null);
			if (cursor != null)
			{
				int count = cursor.getCount();
				if (count > 0)
				{
					cursor.moveToNext();
					retId = cursor.getInt(0);
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (cursor != null)
			{
				cursor.close();
				cursor = null;
			}
		}
		try
		{
			if (retId > 0)// update
			{
				// values.put(WhiteListColumns.BaseColumns.ENABLE,isEnable); int
				// ret = context.getContentResolver().update(WhiteListColumns.Notification.CONTENT_URI, values,
				// WhiteListColumns.BaseColumns._ID+"=?",new
				// String[]{(""+retId)}); Log.i(TAG,"update ret:"+ret);
				 
				Log.i(TAG, "update already id:" + retId);
				return;
			}
			else
			// insert
			{
				values.put(WhiteListColumns.BaseColumns.PKGNAME, pkgname);
				values.put(WhiteListColumns.BaseColumns.ENABLE, isEnable);
				values.put(WhiteListColumns.BaseColumns.ISSERVERCONFIG, isserver);
				Uri uri = context.getContentResolver().insert(WhiteListColumns.MsgWhite.CONTENT_URI, values);
				if (uri != null)
				{
					Log.i(TAG, "insert uri:" + uri);
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	public static void deleteInstallWhiteDb(Context context, int status, int isserver)
	{
		try
		{
			int rows = context.getContentResolver().delete(WhiteListColumns.InstallWhite.CONTENT_URI, 
					WhiteListColumns.BaseColumns.ENABLE + "=?"+" AND "
					+WhiteListColumns.BaseColumns.ISSERVERCONFIG+"=?",
					new String[]{"" + status,""+isserver	});
			Log.i(TAG, "deleteInstallWhiteDb rows:" + rows + "/" + status+"/"+isserver);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static ArrayList<String> getInstallWhiteDb(Context context, int status,int isserver)
	{
		ArrayList<String> itemlist = new ArrayList<String>();
		Cursor cursor = null;
		try
		{
			cursor = context.getContentResolver().query(WhiteListColumns.InstallWhite.CONTENT_URI, new String[]
			{
					WhiteListColumns.BaseColumns._ID, WhiteListColumns.BaseColumns.PKGNAME
			}, 
			WhiteListColumns.BaseColumns.ENABLE + "=?  "+" AND "
			+WhiteListColumns.BaseColumns.ISSERVERCONFIG+"=?", new String[]
			{
				"" + status,""+isserver
			}, null);
			if (cursor != null)
			{
				while (cursor.moveToNext())
				{
					String pkgname = cursor.getString(1);
					itemlist.add(pkgname);
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (cursor != null)
			{
				cursor.close();
				cursor = null;
			}
		}
		return itemlist;
	}

	/**
	 * update enablelist
	 * 
	 * @param context
	 * @param pkgname
	 * @param isEnable
	 */
	public static void insertInstallWhiteDb(Context context, String pkgname, int isEnable,int isserver)
	{
		ContentValues values = new ContentValues();
		Cursor cursor = null;
		int retId = -1;
		try
		{
			cursor = context.getContentResolver().query(WhiteListColumns.InstallWhite.CONTENT_URI, 
			new String[]
			{
				WhiteListColumns.BaseColumns._ID
			}, 
			WhiteListColumns.BaseColumns.PKGNAME + "=?" + " AND " 
			+ WhiteListColumns.BaseColumns.ENABLE + "=?  "+" AND "
			+WhiteListColumns.BaseColumns.ISSERVERCONFIG+"=?", 
			new String[]
			{
					pkgname, "" + isEnable,""+isserver
			}, null);
			if (cursor != null)
			{
				int count = cursor.getCount();
				if (count > 0)
				{
					cursor.moveToNext();
					retId = cursor.getInt(0);
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (cursor != null)
			{
				cursor.close();
				cursor = null;
			}
		}
		try
		{
			if (retId > 0)// update
			{
				//values.put(WhiteListColumns.BaseColumns.ENABLE,isEnable); 
				//int ret = context.getContentResolver().update(WhiteListColumns.Notification.CONTENT_URI, values,
				//WhiteListColumns.BaseColumns._ID+"=?",new String[]{(""+retId)}); 
				//Log.i(TAG,"update ret:"+ret);
				 
				Log.i(TAG, "update already id:" + retId);
				return;
			}
			else
			// insert
			{
				values.put(WhiteListColumns.BaseColumns.PKGNAME, pkgname);
				values.put(WhiteListColumns.BaseColumns.ENABLE, isEnable);
				values.put(WhiteListColumns.BaseColumns.ISSERVERCONFIG, isserver);
				Uri uri = context.getContentResolver().insert(WhiteListColumns.InstallWhite.CONTENT_URI, values);
				if (uri != null)
				{
					Log.i(TAG, "insert uri:" + uri);
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	/////////////////////////////////////////////////////////////////////////////////
	public static void deleteDozeWhiteDb(Context context, int status, int isserver)
	{
		try
		{
			int rows = context.getContentResolver().delete(WhiteListColumns.DozeWhiteList.CONTENT_URI, 
					WhiteListColumns.BaseColumns.ENABLE + "=?"+" AND "
					+WhiteListColumns.BaseColumns.ISSERVERCONFIG+"=?",
					new String[]{"" + status,""+isserver	});
			Log.i(TAG, "deleteInstallWhiteDb rows:" + rows + "/" + status+"/"+isserver);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static ArrayList<String> getDozeWhiteDb(Context context, int status,int isserver)
	{
		ArrayList<String> itemlist = new ArrayList<String>();
		Cursor cursor = null;
		try
		{
			cursor = context.getContentResolver().query(WhiteListColumns.DozeWhiteList.CONTENT_URI, new String[]
			{
					WhiteListColumns.BaseColumns._ID, WhiteListColumns.BaseColumns.PKGNAME
			}, 
			WhiteListColumns.BaseColumns.ENABLE + "=?  "+" AND "
			+WhiteListColumns.BaseColumns.ISSERVERCONFIG+"=?", new String[]
			{
				"" + status,""+isserver
			}, null);
			if (cursor != null)
			{
				while (cursor.moveToNext())
				{
					String pkgname = cursor.getString(1);
					itemlist.add(pkgname);
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (cursor != null)
			{
				cursor.close();
				cursor = null;
			}
		}
		return itemlist;
	}

	/**
	 * update enablelist
	 * 
	 * @param context
	 * @param pkgname
	 * @param isEnable
	 */
	public static void insertDozeWhiteDb(Context context, String pkgname, int isEnable,int isserver)
	{
		ContentValues values = new ContentValues();
		Cursor cursor = null;
		int retId = -1;
		try
		{
			cursor = context.getContentResolver().query(WhiteListColumns.DozeWhiteList.CONTENT_URI, 
			new String[]
			{
				WhiteListColumns.BaseColumns._ID
			}, 
			WhiteListColumns.BaseColumns.PKGNAME + "=?" + " AND " 
			+ WhiteListColumns.BaseColumns.ENABLE + "=?  "+" AND "
			+WhiteListColumns.BaseColumns.ISSERVERCONFIG+"=?", 
			new String[]
			{
					pkgname, "" + isEnable,""+isserver
			}, null);
			if (cursor != null)
			{
				int count = cursor.getCount();
				if (count > 0)
				{
					cursor.moveToNext();
					retId = cursor.getInt(0);
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (cursor != null)
			{
				cursor.close();
				cursor = null;
			}
		}
		try
		{
			if (retId > 0)// update
			{
				//values.put(WhiteListColumns.BaseColumns.ENABLE,isEnable); 
				//int ret = context.getContentResolver().update(WhiteListColumns.DozeWhiteList.CONTENT_URI, values,
				//WhiteListColumns.BaseColumns._ID+"=?",new String[]{(""+retId)}); 
				//Log.i(TAG,"update ret:"+ret);
				 
				Log.i(TAG, "update already id:" + retId);
				return;
			}
			else
			// insert
			{
				values.put(WhiteListColumns.BaseColumns.PKGNAME, pkgname);
				values.put(WhiteListColumns.BaseColumns.ENABLE, isEnable);
				values.put(WhiteListColumns.BaseColumns.ISSERVERCONFIG, isserver);
				Uri uri = context.getContentResolver().insert(WhiteListColumns.DozeWhiteList.CONTENT_URI, values);
				if (uri != null)
				{
					Log.i(TAG, "insert uri:" + uri);
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	
	///////////////////////////////////////////////////////////////////////////////////	
	public static void checkRepeatItem(ArrayList<String> itemlist)
	{		
		for(int i=0;i<itemlist.size();i++)
		{
			String pkgname = itemlist.get(i);			
			for(int j=i+1;j<itemlist.size();j++)
			{
				if(pkgname.equals(itemlist.get(j)))
				{
					Log.i(TAG,"checkRepeatItem rmeove repeat:"+itemlist.get(j));
					itemlist.remove(j);
					j--;
				}
			}			
		}
	}
}
