package com.android.provider;

import android.provider.WhiteListColumns;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import com.android.provider.PreInstallApps.ReleateWakeupItem;
import com.android.provider.PreInstallApps.ProviderWakeupItem;

public class DbHelper extends SQLiteOpenHelper {

	private static final String DATABASE = "prizeblacklist.db";
	private static final int  DATABASE_VERSION = 11;
	private static final String TAG = "whitelist";
       private Context mContext;
	
	public DbHelper(Context context) {
		super(context, DATABASE, null, DATABASE_VERSION);
		// TODO Auto-generated constructor stub
		mContext =  context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		
		Log.i(TAG,"load create tables...");
		db.execSQL("create table " + WhiteListColumns.Purebackground.TABLE + " ( "
				+ WhiteListColumns.BaseColumns._ID + " integer primary key autoincrement,"
				+ WhiteListColumns.BaseColumns.PKGNAME + " text NOT NULL, " 
				+ WhiteListColumns.BaseColumns.ENABLE + " integer,"				
				+ WhiteListColumns.BaseColumns.ISSERVERCONFIG + " integer"				
				+ " );");
		db.execSQL("create table " + WhiteListColumns.Notification.TABLE + " ( "
				+ WhiteListColumns.BaseColumns._ID + " integer primary key autoincrement,"
				+ WhiteListColumns.BaseColumns.PKGNAME + " text NOT NULL, " 
				+ WhiteListColumns.BaseColumns.ENABLE + " integer,"				
				+ WhiteListColumns.BaseColumns.ISSERVERCONFIG + " integer"				
				+ " );");
		db.execSQL("create table " + WhiteListColumns.FloatWindow.TABLE + " ( "
				+ WhiteListColumns.BaseColumns._ID + " integer primary key autoincrement,"
				+ WhiteListColumns.BaseColumns.PKGNAME + " text NOT NULL, " 
				+ WhiteListColumns.BaseColumns.ENABLE + " integer,"				
				+ WhiteListColumns.BaseColumns.ISSERVERCONFIG + " integer"				
				+ " );");
		db.execSQL("create table " + WhiteListColumns.AutoLaunch.TABLE + " ( "
				+ WhiteListColumns.BaseColumns._ID + " integer primary key autoincrement,"
				+ WhiteListColumns.BaseColumns.PKGNAME + " text NOT NULL, " 
				+ WhiteListColumns.BaseColumns.ENABLE + " integer,"				
				+ WhiteListColumns.BaseColumns.ISSERVERCONFIG + " integer"				
				+ " );");
		db.execSQL("create table " + WhiteListColumns.NetForbade.TABLE + " ( "
				+ WhiteListColumns.BaseColumns._ID + " integer primary key autoincrement,"
				+ WhiteListColumns.BaseColumns.PKGNAME + " text NOT NULL, " 
				+ WhiteListColumns.BaseColumns.ENABLE + " integer,"				
				+ WhiteListColumns.BaseColumns.ISSERVERCONFIG + " integer"				
				+ " );");
		//relate wakeup
		db.execSQL("create table " + WhiteListColumns.RelateWakeup.TABLE + " ( "
				+ WhiteListColumns.BaseColumns._ID + " integer primary key autoincrement,"
				+ WhiteListColumns.BaseColumns.PKGNAME + " text, " 
				+ WhiteListColumns.RelateWakeup.CLASS + " text, " 
				+ WhiteListColumns.RelateWakeup.ACTION + " text, " 
				+ WhiteListColumns.RelateWakeup.CALLERPKG + " text, " 
				+ WhiteListColumns.BaseColumns.ENABLE + " integer,"				
				+ WhiteListColumns.BaseColumns.ISSERVERCONFIG + " integer"
				+ " );");
		//sleepnetwhite
		db.execSQL("create table " + WhiteListColumns.SleepNetWhite.TABLE + " ( "
				+ WhiteListColumns.BaseColumns._ID + " integer primary key autoincrement,"
				+ WhiteListColumns.BaseColumns.PKGNAME + " text NOT NULL, " 
				+ WhiteListColumns.BaseColumns.ENABLE + " integer,"				
				+ WhiteListColumns.BaseColumns.ISSERVERCONFIG + " integer"				
				+ " );");
		//blockactivity
		db.execSQL("create table " + WhiteListColumns.BlockActivity.TABLE + " ( "
				+ WhiteListColumns.BaseColumns._ID + " integer primary key autoincrement,"
				+ WhiteListColumns.BaseColumns.PKGNAME + " text, " 
				+ WhiteListColumns.BlockActivity.CLASS + " text NOT NULL, " 
				+ WhiteListColumns.BaseColumns.ENABLE + " integer,"				
				+ WhiteListColumns.BaseColumns.ISSERVERCONFIG + " integer"				
				+ " );");
		db.execSQL("create table " + WhiteListColumns.MsgWhite.TABLE + " ( "
				+ WhiteListColumns.BaseColumns._ID + " integer primary key autoincrement,"
				+ WhiteListColumns.BaseColumns.PKGNAME + " text NOT NULL, " 
				+ WhiteListColumns.BaseColumns.ENABLE + " integer,"				
				+ WhiteListColumns.BaseColumns.ISSERVERCONFIG + " integer"				
				+ " );");
		db.execSQL("create table " + WhiteListColumns.InstallWhite.TABLE + " ( "
				+ WhiteListColumns.BaseColumns._ID + " integer primary key autoincrement,"
				+ WhiteListColumns.BaseColumns.PKGNAME + " text NOT NULL, " 
				+ WhiteListColumns.BaseColumns.ENABLE + " integer,"				
				+ WhiteListColumns.BaseColumns.ISSERVERCONFIG + " integer"				
				+ " );");	

		db.execSQL("create table " + WhiteListColumns.DozeWhiteList.TABLE + " ( "
				+ WhiteListColumns.BaseColumns._ID + " integer primary key autoincrement,"
				+ WhiteListColumns.BaseColumns.PKGNAME + " text NOT NULL, " 
				+ WhiteListColumns.BaseColumns.ENABLE + " integer,"				
				+ WhiteListColumns.BaseColumns.ISSERVERCONFIG + " integer"				
				+ " );");

              db.execSQL("create table " + WhiteListColumns.ProviderWakeup.TABLE + " ( "
				+ WhiteListColumns.BaseColumns._ID + " integer primary key autoincrement,"
				+ WhiteListColumns.BaseColumns.PKGNAME + " text, " 
				+ WhiteListColumns.ProviderWakeup.CLASS + " text, " 
				+ WhiteListColumns.ProviderWakeup.CALLERPKG + " text, " 
				+ WhiteListColumns.BaseColumns.ENABLE + " integer,"				
				+ WhiteListColumns.BaseColumns.ISSERVERCONFIG + " integer"
				+ " );");
		
		Log.i(TAG,"load create tables end...");
		
		//purebackground
		Log.i(TAG,"load purebackground...");
		/*for(int i=0;i<PreInstallApps.purebackground_hidelist.length;i++)
		{
			db.execSQL("insert into " + WhiteListColumns.Purebackground.TABLE+" ("+WhiteListColumns.BaseColumns.PKGNAME+","+WhiteListColumns.BaseColumns.ENABLE+","+WhiteListColumns.BaseColumns.ISSERVERCONFIG+")"
					+ " values ('" + PreInstallApps.purebackground_hidelist[i] + "', " + PreInstallApps.HIDE + ","+WhiteListColumns.LOCAL_CONFIG+");");
		}		
		for(int i=0;i<PreInstallApps.purebackground_defenablelist.length;i++)
		{
			db.execSQL("insert into " + WhiteListColumns.Purebackground.TABLE+" ("+WhiteListColumns.BaseColumns.PKGNAME+","+WhiteListColumns.BaseColumns.ENABLE+","+WhiteListColumns.BaseColumns.ISSERVERCONFIG+")"
					+ " values ('" + PreInstallApps.purebackground_defenablelist[i] + "', " + PreInstallApps.DEFENABLE + ","+WhiteListColumns.LOCAL_CONFIG+ ");");
		}		
		for(int i=0;i<PreInstallApps.purebackground_notkilllist.length;i++)
		{
			db.execSQL("insert into " + WhiteListColumns.Purebackground.TABLE+" ("+WhiteListColumns.BaseColumns.PKGNAME+","+WhiteListColumns.BaseColumns.ENABLE+","+WhiteListColumns.BaseColumns.ISSERVERCONFIG+")"
					+ " values ('" + PreInstallApps.purebackground_notkilllist[i] + "', " + PreInstallApps.NOTKILL + ","+WhiteListColumns.LOCAL_CONFIG+ ");");
		}*/
		for(int i=0;i<PreInstallApps.prize_switchon_list.length;i++)
		{
			db.execSQL("insert into " + WhiteListColumns.Purebackground.TABLE+" ("+WhiteListColumns.BaseColumns.PKGNAME+","+WhiteListColumns.BaseColumns.ENABLE+","+WhiteListColumns.BaseColumns.ISSERVERCONFIG+")"
					+ " values ('" + PreInstallApps.prize_switchon_list[i] + "', " + PreInstallApps.DEFENABLE + ","+WhiteListColumns.SERVER_CONFIG+ ");");
		}
		
		//notification
		Log.i(TAG,"load notification...");
		for(int i=0;i<PreInstallApps.notification_hidelist.length;i++)
		{
			db.execSQL("insert into " + WhiteListColumns.Notification.TABLE+" ("+WhiteListColumns.BaseColumns.PKGNAME+","+WhiteListColumns.BaseColumns.ENABLE+","+WhiteListColumns.BaseColumns.ISSERVERCONFIG+")"
					+ " values ('" + PreInstallApps.notification_hidelist[i] + "', " + PreInstallApps.HIDE + ","+WhiteListColumns.LOCAL_CONFIG+ ");");
		}
		for(int i=0;i<PreInstallApps.notification_defenablelist.length;i++)
		{
			db.execSQL("insert into " + WhiteListColumns.Notification.TABLE+" ("+WhiteListColumns.BaseColumns.PKGNAME+","+WhiteListColumns.BaseColumns.ENABLE+","+WhiteListColumns.BaseColumns.ISSERVERCONFIG+")"
					+ " values ('" + PreInstallApps.notification_defenablelist[i] + "', " + PreInstallApps.DEFENABLE + ","+WhiteListColumns.LOCAL_CONFIG+ ");");
		}
		
		//floatwindow
		Log.i(TAG,"load floatwindow...");
		for(int i=0;i<PreInstallApps.floatwindow_defenablelist.length;i++)
		{
			db.execSQL("insert into " + WhiteListColumns.FloatWindow.TABLE+" ("+WhiteListColumns.BaseColumns.PKGNAME+","+WhiteListColumns.BaseColumns.ENABLE+","+WhiteListColumns.BaseColumns.ISSERVERCONFIG+")"
					+ " values ('" + PreInstallApps.floatwindow_defenablelist[i] + "', " + PreInstallApps.ENABLE + ","+WhiteListColumns.LOCAL_CONFIG+ ");");
		}
		
		//autolaunch
		Log.i(TAG,"load autolaunch...");
		for(int i=0;i<PreInstallApps.autolaunch_defenablelist.length;i++)
		{
			db.execSQL("insert into " + WhiteListColumns.AutoLaunch.TABLE+" ("+WhiteListColumns.BaseColumns.PKGNAME+","+WhiteListColumns.BaseColumns.ENABLE+","+WhiteListColumns.BaseColumns.ISSERVERCONFIG+")"
					+ " values ('" + PreInstallApps.autolaunch_defenablelist[i] + "', " + PreInstallApps.DEFENABLE + ","+WhiteListColumns.LOCAL_CONFIG+ ");");
		}
		
		//netforbade
		Log.i(TAG,"load autolaunch...");
		for(int i=0;i<PreInstallApps.netforbade_alwaysforbadelist.length;i++)
		{
			db.execSQL("insert into " + WhiteListColumns.NetForbade.TABLE+" ("+WhiteListColumns.BaseColumns.PKGNAME+","+WhiteListColumns.BaseColumns.ENABLE+","+WhiteListColumns.BaseColumns.ISSERVERCONFIG+")"
					+ " values ('" + PreInstallApps.netforbade_alwaysforbadelist[i] + "', " + PreInstallApps.DISABLE + ","+WhiteListColumns.LOCAL_CONFIG+ ");");
		}
		
		//releatewakeup
		Log.i(TAG,"load releatewakeup...");
		for(int i=0;i<PreInstallApps.releatewakeup_deflist.length;i++)
		{
			ReleateWakeupItem item = PreInstallApps.releatewakeup_deflist[i];
			db.execSQL("insert into " + WhiteListColumns.RelateWakeup.TABLE
					+" ("+WhiteListColumns.BaseColumns.PKGNAME+","
					+WhiteListColumns.RelateWakeup.CLASS+","
					+WhiteListColumns.RelateWakeup.ACTION+","
					+WhiteListColumns.RelateWakeup.CALLERPKG+","
					+WhiteListColumns.BaseColumns.ENABLE+","
					+WhiteListColumns.BaseColumns.ISSERVERCONFIG+")"
					+ " values ('" 
					+ item.targetpkg +"','"
					+item.classname	+ "', '"
					+item.action+"','"
					+item.callerpkg+"',"
					+ item.state + ","
					+WhiteListColumns.LOCAL_CONFIG+ ");");
		}
		
		//sleepnetwhite
		Log.i(TAG,"load sleepnetwhite...");
		for(int i=0;i<PreInstallApps.sleepnetwhite_deflist.length;i++)
		{
			db.execSQL("insert into " + WhiteListColumns.SleepNetWhite.TABLE+" ("+WhiteListColumns.BaseColumns.PKGNAME+","+WhiteListColumns.BaseColumns.ENABLE+","+WhiteListColumns.BaseColumns.ISSERVERCONFIG+")"
					+ " values ('" + PreInstallApps.sleepnetwhite_deflist[i] + "', " + PreInstallApps.ENABLE + ","+WhiteListColumns.LOCAL_CONFIG+ ");");
		}
		
		//blockactivity
		Log.i(TAG,"load blockactivity...");
		for(int i=0;i<PreInstallApps.blockactivity_deflist.length;i++)
		{
			db.execSQL("insert into " + WhiteListColumns.BlockActivity.TABLE
					+" ("+WhiteListColumns.BaseColumns.PKGNAME+","+WhiteListColumns.BlockActivity.CLASS+","+WhiteListColumns.BaseColumns.ENABLE+","+WhiteListColumns.BaseColumns.ISSERVERCONFIG+")"
					+ " values ('" +null+"','"+ PreInstallApps.blockactivity_deflist[i] + "', " + PreInstallApps.DISABLE + ","+WhiteListColumns.LOCAL_CONFIG+ ");");
		}
		
		//msgwhite
		Log.i(TAG,"load msgwhite...");
		for(int i=0;i<PreInstallApps.msgwhite_deflist.length;i++)
		{
			db.execSQL("insert into " + WhiteListColumns.MsgWhite.TABLE+" ("+WhiteListColumns.BaseColumns.PKGNAME+","+WhiteListColumns.BaseColumns.ENABLE+","+WhiteListColumns.BaseColumns.ISSERVERCONFIG+")"
					+ " values ('" + PreInstallApps.msgwhite_deflist[i] + "', " + PreInstallApps.ENABLE + ","+WhiteListColumns.LOCAL_CONFIG+ ");");
		}
		//installwhite
		Log.i(TAG,"load installwhite...");
		for(int i=0;i<PreInstallApps.installwhite_deflist.length;i++)
		{
			db.execSQL("insert into " + WhiteListColumns.InstallWhite.TABLE+" ("+WhiteListColumns.BaseColumns.PKGNAME+","+WhiteListColumns.BaseColumns.ENABLE+","+WhiteListColumns.BaseColumns.ISSERVERCONFIG+")"
					+ " values ('" + PreInstallApps.installwhite_deflist[i] + "', " + PreInstallApps.ENABLE + ","+WhiteListColumns.LOCAL_CONFIG+ ");");
		}

		//dozewhite
		Log.i(TAG,"load dozewhite...");
		for(int i=0;i<PreInstallApps.dozewhite_deflist.length;i++)
		{
			db.execSQL("insert into " + WhiteListColumns.DozeWhiteList.TABLE+" ("+WhiteListColumns.BaseColumns.PKGNAME+","+WhiteListColumns.BaseColumns.ENABLE+","+WhiteListColumns.BaseColumns.ISSERVERCONFIG+")"
					+ " values ('" + PreInstallApps.dozewhite_deflist[i] + "', " + PreInstallApps.ENABLE + ","+WhiteListColumns.LOCAL_CONFIG+ ");");
		}

              //releatewakeup
            Log.i(TAG,"load providerwakeup...");
            for(int i=0;i<PreInstallApps.providerwakeup_deflist.length;i++)
            {
                ProviderWakeupItem item = PreInstallApps.providerwakeup_deflist[i];
                db.execSQL("insert into " + WhiteListColumns.ProviderWakeup.TABLE
                		+" ("+WhiteListColumns.BaseColumns.PKGNAME+","
                		+WhiteListColumns.ProviderWakeup.CLASS+","
                		+WhiteListColumns.ProviderWakeup.CALLERPKG+","
                		+WhiteListColumns.BaseColumns.ENABLE+","
                		+WhiteListColumns.BaseColumns.ISSERVERCONFIG+")"
                		+ " values ('" 
                		+ item.targetpkg +"','"
                		+item.classname	+ "', '"
                		+item.callerpkg+"',"
                		+ item.state + ","
                		+WhiteListColumns.LOCAL_CONFIG+ ");");
            }                
		
		Log.i(TAG,"load end....");

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		Log.d(TAG,"onUpgrade oldVersion:"+oldVersion+",newVersion:"+newVersion);
//		String sql = "DROP TABLE IF EXISTS " + TABLE;
//		db.execSQL(sql);
		//onCreate(db);
		//for version 10		
              if(oldVersion == 10)
              {
                Log.i(TAG,"update load floatwindow...");
                String [] addfloatwindow_defenablelist = new String[]{"com.android.phone","com.mediatek.mtklogger",};
                oldVersion = 11;
                for(int i=0;i<addfloatwindow_defenablelist.length;i++)
                {
                    //delete first
                    int rows = db.delete(WhiteListColumns.FloatWindow.TABLE,
                        WhiteListColumns.BaseColumns.PKGNAME+"=? AND "
                        +WhiteListColumns.BaseColumns.ISSERVERCONFIG+"=?",
                        new String[]{addfloatwindow_defenablelist[i],""+WhiteListColumns.LOCAL_CONFIG});
                    Log.d(TAG,"delete "+WhiteListColumns.FloatWindow.TABLE+" "+WhiteListColumns.BaseColumns.PKGNAME +",rows:"+rows);
                    //insert
                    db.execSQL("insert into " + WhiteListColumns.FloatWindow.TABLE+" ("+WhiteListColumns.BaseColumns.PKGNAME+","+WhiteListColumns.BaseColumns.ENABLE+","+WhiteListColumns.BaseColumns.ISSERVERCONFIG+")"
                			+ " values ('" + addfloatwindow_defenablelist[i] + "', " + PreInstallApps.ENABLE + ","+WhiteListColumns.LOCAL_CONFIG+ ");");                    
                }
                //for reset floatwindow permissions
                android.provider.Settings.System.putInt(mContext.getContentResolver(),android.provider.Settings.System.PRIZE_NOTIFICATION_CUSTOM,1);
            }
	}

}
