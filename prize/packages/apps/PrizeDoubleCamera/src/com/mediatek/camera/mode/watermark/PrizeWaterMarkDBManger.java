package com.mediatek.camera.mode.watermark;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.R.integer;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import com.android.camera.Log;

public class PrizeWaterMarkDBManger {
	
	private Context mContext;
	
	private static final String DB_PATH = "watermark.db";
	
	private static final String DBWaterMarkAlbumName = "watermark_album";
	
	private static final String DBWaterMarkDataName = "watermark_data";
	
	private static final String DBWaterMarkImageName = "image_resource_data";
	
	private static final String DBWaterMarkTextName = "text_resource_data";
	
	private static final String DBWaterMarkHistory ="watermark_history";
	
	private String dbSavePath = null;
	
	private SQLiteDatabase mWaterMarkDbSqlite =null;

	public PrizeWaterMarkDBManger(Context mContext){
		this.mContext = mContext;
		mWaterMarkDbSqlite = openDatabase();
	}
	
	private SQLiteDatabase openDatabase(){
		
		dbSavePath = mContext.getApplicationContext().getFilesDir().getAbsolutePath() + "/" + DB_PATH;
		
		File mDbFile = new File(dbSavePath);
		
		if(mDbFile.exists()){
			return SQLiteDatabase.openOrCreateDatabase(dbSavePath,null);
		}else{
			try {
				 AssetManager am= mContext.getAssets();  
				 
				 InputStream is=am.open(DB_PATH);
				 
				 FileOutputStream fos=new FileOutputStream(mDbFile); 
				 
				 byte[] buffer=new byte[1024];  
                 int count = 0;  
                 while((count = is.read(buffer))>0){  
                     fos.write(buffer,0,count);  
                 }  
                 fos.flush();  
                 fos.close();  
                 is.close();  
                 
			} catch (IOException e) {
				// TODO: handle exception
                    // TODO Auto-generated catch block  
				e.printStackTrace();  
				return null;  
			}
		}
		return null;
	}
	
	public ArrayList<PrizeWaterMarkAlbum> getSQLQueryAlbum(){
		
		ArrayList<PrizeWaterMarkAlbum> mPrizeWaterMarkAlbumList = new ArrayList<PrizeWaterMarkAlbum>();
		
		String[] mColumns = new String[2];
		
		mColumns[0] = PrizeWaterMarkAlbum.DBSQL_ALBUMIDNAME;
		
		mColumns[1] = PrizeWaterMarkAlbum.DBSQL_ALBUMNAME;
		
		if(mWaterMarkDbSqlite == null){
			
			mWaterMarkDbSqlite = openDatabase();
		}
		
		Cursor cursor =  mWaterMarkDbSqlite.query(DBWaterMarkAlbumName, mColumns, null, null, null, null, PrizeWaterMarkAlbum.DBSQL_ALBUMIDNAME);
		
		if(cursor!=null&&cursor.moveToFirst()){
			
			while (!cursor.isAfterLast()) {
				
				PrizeWaterMarkAlbum mPrizeWaterMarkAlbum = new PrizeWaterMarkAlbum();
				
				int _id = cursor.getInt(cursor.getColumnIndex(PrizeWaterMarkAlbum.DBSQL_ALBUMIDNAME));
				
				String _tname = cursor.getString(cursor.getColumnIndex(PrizeWaterMarkAlbum.DBSQL_ALBUMNAME));
				
				mPrizeWaterMarkAlbum.setAlbumid(_id);
				
				mPrizeWaterMarkAlbum.setAlbumName(_tname);
				
				mPrizeWaterMarkAlbumList.add(mPrizeWaterMarkAlbum);
				
				cursor.moveToNext();	
			}
		}
		cursor.close();
		
		return mPrizeWaterMarkAlbumList;
	}
	
	public ArrayList<PrizeWaterMarkThumbInfo> getSQLQueryHistoryData(){
		ArrayList<PrizeWaterMarkThumbInfo> mWaterMarkThumbList = new ArrayList<PrizeWaterMarkThumbInfo>();

		String string = "select * from " +  DBWaterMarkHistory +
				"  order by edit_time desc limit 8 " ;
		
		if(mWaterMarkDbSqlite == null){
			mWaterMarkDbSqlite = openDatabase();
		}
		
		Cursor cursor =  mWaterMarkDbSqlite.rawQuery(string, null);
		if(cursor!=null&&cursor.moveToFirst()){
			
			while (!cursor.isAfterLast()) {
				PrizeWaterMarkThumbInfo mPrizeWaterMarkThumbInfo = new PrizeWaterMarkThumbInfo();
				mPrizeWaterMarkThumbInfo.setAlbumId(cursor.getInt(cursor.getColumnIndex("ttype")));
				mPrizeWaterMarkThumbInfo.setThumbID(cursor.getInt(cursor.getColumnIndex("data_id")));
				mWaterMarkThumbList.add(mPrizeWaterMarkThumbInfo);
				
				cursor.moveToNext();
			}
		}
		cursor.close();
		
		return mWaterMarkThumbList;
	}
	
	public void InsertOrUpdateSQLHistory(int album_id, int data_id, int firstAlbumId, int firstDataId, int size){
		String[] mColumns = new String[2];
		mColumns[0] = "ttype";
		mColumns[1] = "data_id";
		if(mWaterMarkDbSqlite == null){
			mWaterMarkDbSqlite = openDatabase();
		}
		Cursor cursor = mWaterMarkDbSqlite.query(DBWaterMarkHistory, mColumns, "ttype=" + album_id +" and data_id="+data_id , null, null, null, null);
		if(cursor!=null&&cursor.moveToFirst()){
			ContentValues values=new ContentValues();
			values.put("ttype", album_id);
			values.put("data_id", data_id);
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd  HH:mm:ss");
			values.put("edit_time", df.format(new Date(System.currentTimeMillis())));
			mWaterMarkDbSqlite.update(DBWaterMarkHistory, values, "ttype ="+album_id+" and data_id="+data_id , null);
			cursor.close();
		} else if (size == 8) {
			ContentValues values=new ContentValues();
			values.put("ttype", album_id);
			values.put("data_id", data_id);
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd  HH:mm:ss");
			values.put("edit_time", df.format(new Date(System.currentTimeMillis())));
			mWaterMarkDbSqlite.update(DBWaterMarkHistory, values, "ttype ="+firstAlbumId+" and data_id="+firstDataId , null);
			cursor.close();
		} else{
			if(cursor!=null){
				cursor.close();
			}
			ContentValues values=new ContentValues();
			values.put("_id", album_id*1000+data_id);
			values.put("ttype", album_id);
			values.put("data_id", data_id);
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd  HH:mm:ss");
			values.put("edit_time", df.format(new Date(System.currentTimeMillis())));
			long rend = mWaterMarkDbSqlite.insert(DBWaterMarkHistory, null, values);
		}
	}
	
	
	/**select  data from table by liudong  2015-12-24*/
	public ArrayList<PrizeWaterMarkThumbInfo> getSQLQueryData(int malbumid){
		
		ArrayList<PrizeWaterMarkThumbInfo> mWaterMarkThumbList = new ArrayList<PrizeWaterMarkThumbInfo>();
		
		String[] mColumns = new String[9];
		mColumns[0] = PrizeWaterMarkThumbInfo.DBSQL_THUMBIDNAME;
		mColumns[1] = PrizeWaterMarkThumbInfo.DBSQL_THUMPATHNAME;
		mColumns[2] = PrizeWaterMarkThumbInfo.DBSQL_ISEDITNAME;
		mColumns[3] = PrizeWaterMarkThumbInfo.DBSQL_WIDTHNAME;
		mColumns[4] = PrizeWaterMarkThumbInfo.DBSQL_HEIGHTNAME;
		mColumns[5] = PrizeWaterMarkThumbInfo.DBSQL_COLORCHANGENAME;
		mColumns[6] = PrizeWaterMarkThumbInfo.DBSQL_THUMALBUMIDNAME;
		mColumns[7] = PrizeWaterMarkThumbInfo.DBSQL_XPOSTIONNAME;
		mColumns[8] = PrizeWaterMarkThumbInfo.DBSQL_YPOSTIONNAME;
		if(mWaterMarkDbSqlite == null){
			mWaterMarkDbSqlite = openDatabase();
		}
		
		Cursor cursor =  mWaterMarkDbSqlite.query(DBWaterMarkDataName, mColumns, PrizeWaterMarkThumbInfo.DBSQL_THUMALBUMIDNAME+"="+malbumid, null, null, null, PrizeWaterMarkThumbInfo.DBSQL_THUMBIDNAME);
		
		if(cursor!=null&&cursor.moveToFirst()){
			while (!cursor.isAfterLast()) {
		
				PrizeWaterMarkThumbInfo mPrizeWaterMarkThumbInfo = new PrizeWaterMarkThumbInfo();
				
				mPrizeWaterMarkThumbInfo.setThumbID(cursor.getInt(cursor.getColumnIndex(PrizeWaterMarkThumbInfo.DBSQL_THUMBIDNAME)));
				
				mPrizeWaterMarkThumbInfo.setAlbumId(cursor.getInt(cursor.getColumnIndex(PrizeWaterMarkThumbInfo.DBSQL_THUMALBUMIDNAME)));
				
				mPrizeWaterMarkThumbInfo.setThumbPathString(cursor.getString(cursor.getColumnIndex(PrizeWaterMarkThumbInfo.DBSQL_THUMPATHNAME)));
				
				mPrizeWaterMarkThumbInfo.setIsEdit(cursor.getInt(cursor.getColumnIndex(PrizeWaterMarkThumbInfo.DBSQL_ISEDITNAME))>0?false:true);
				float density = mContext.getResources().getDisplayMetrics().density;
				mPrizeWaterMarkThumbInfo.setMWidth(cursor.getFloat(cursor.getColumnIndex(PrizeWaterMarkThumbInfo.DBSQL_WIDTHNAME))*density);
				
				mPrizeWaterMarkThumbInfo.setMHeight(cursor.getFloat(cursor.getColumnIndex(PrizeWaterMarkThumbInfo.DBSQL_HEIGHTNAME))*density);
				
				mPrizeWaterMarkThumbInfo.setColorChange(cursor.getInt(cursor.getColumnIndex(PrizeWaterMarkThumbInfo.DBSQL_COLORCHANGENAME))>0?true:false);
				
				mPrizeWaterMarkThumbInfo.setXpostion(cursor.getFloat(cursor.getColumnIndex(PrizeWaterMarkThumbInfo.DBSQL_XPOSTIONNAME))*density);
				
				mPrizeWaterMarkThumbInfo.setYpostion(cursor.getFloat(cursor.getColumnIndex(PrizeWaterMarkThumbInfo.DBSQL_YPOSTIONNAME))*density);

				mWaterMarkThumbList.add(mPrizeWaterMarkThumbInfo);
				
				cursor.moveToNext();
			}
		}
		
		cursor.close();
		
		return mWaterMarkThumbList;
	}
	
	/** select image data from imagetable by liudong 2015-12-24*/
	public ArrayList<PrizeWaterMarkImageResourceInfo> getSQLQueryImageResourceData(int data_id){
		ArrayList<PrizeWaterMarkImageResourceInfo> PrizeWaterMarkImageViewList = new ArrayList<PrizeWaterMarkImageResourceInfo>();
		
		String[] mColumns = new String[6];
		mColumns[0] = PrizeWaterMarkImageResourceInfo.DBSQL_RIDNAME;
		mColumns[1] = PrizeWaterMarkImageResourceInfo.DBSQL_MIDNAME;
		mColumns[2] = PrizeWaterMarkImageResourceInfo.DBSQL_RPATHNAME;
		mColumns[3] = PrizeWaterMarkImageResourceInfo.DBSQL_XPOSTIONNAME;
		mColumns[4] = PrizeWaterMarkImageResourceInfo.DBSQL_YPOSTIONNAME;
		mColumns[5] = PrizeWaterMarkImageResourceInfo.DBSQL_IMAGETYPENAME;
		
		if(mWaterMarkDbSqlite == null){
			mWaterMarkDbSqlite = openDatabase();
		}
		
		Cursor cursor =  mWaterMarkDbSqlite.query(DBWaterMarkImageName, mColumns, PrizeWaterMarkImageResourceInfo.DBSQL_MIDNAME+"="+data_id, null, null, null, null);
		
		if(cursor!=null&&cursor.moveToFirst()){
			
			while (!cursor.isAfterLast()) {
		
				PrizeWaterMarkImageResourceInfo mPrizeWaterMarkImageResourceInfo = new PrizeWaterMarkImageResourceInfo();
				
				mPrizeWaterMarkImageResourceInfo.setDataAlbumId(cursor.getInt(cursor.getColumnIndex(PrizeWaterMarkImageResourceInfo.DBSQL_RIDNAME)));
				
				mPrizeWaterMarkImageResourceInfo.setDataId(cursor.getInt(cursor.getColumnIndex(PrizeWaterMarkImageResourceInfo.DBSQL_MIDNAME)));
				
				mPrizeWaterMarkImageResourceInfo.setImageDataPath(cursor.getString(cursor.getColumnIndex(PrizeWaterMarkImageResourceInfo.DBSQL_RPATHNAME)));
				float density = mContext.getResources().getDisplayMetrics().density;
				mPrizeWaterMarkImageResourceInfo.setImageXPostion(cursor.getFloat(cursor.getColumnIndex(PrizeWaterMarkImageResourceInfo.DBSQL_XPOSTIONNAME))*density);
				
				mPrizeWaterMarkImageResourceInfo.setImageYPostion(cursor.getFloat(cursor.getColumnIndex(PrizeWaterMarkImageResourceInfo.DBSQL_YPOSTIONNAME))*density);
				
				mPrizeWaterMarkImageResourceInfo.setImageType(cursor.getInt(cursor.getColumnIndex(PrizeWaterMarkImageResourceInfo.DBSQL_IMAGETYPENAME)));
				
				PrizeWaterMarkImageViewList.add(mPrizeWaterMarkImageResourceInfo);
				
				cursor.moveToNext();
			}
		}
		
		cursor.close();
		
		return PrizeWaterMarkImageViewList;
	}
	

	public ArrayList<PrizeWaterMarkTextResourceInfo> getSQLQueryTextResourceData(int data_id){
		ArrayList<PrizeWaterMarkTextResourceInfo> mPrizeWaterMarkTextList = new ArrayList<PrizeWaterMarkTextResourceInfo>();
		
		String[] mColumns = new String[12];
		mColumns[0] = PrizeWaterMarkTextResourceInfo.DBSQL_TEXTALBUMDATAID;
		mColumns[1] = PrizeWaterMarkTextResourceInfo.DBSQL_TEXTDATAID;
		mColumns[2] = PrizeWaterMarkTextResourceInfo.DBSQL_TEXTINITSTRING;
		mColumns[3] = PrizeWaterMarkTextResourceInfo.DBSQL_TEXTSIZE;
		mColumns[4] = PrizeWaterMarkTextResourceInfo.DBSQL_TEXT_V;
		mColumns[5] = PrizeWaterMarkTextResourceInfo.DBSQL_TEXTCOLOR;
		mColumns[6] = PrizeWaterMarkTextResourceInfo.DBSQL_TEXTTYPE;
		mColumns[7] = PrizeWaterMarkTextResourceInfo.DBSQL_TEXTXPOSTION;
		mColumns[8] = PrizeWaterMarkTextResourceInfo.DBSQL_TEXTYPOSTION;
		mColumns[9] = PrizeWaterMarkTextResourceInfo.DBSQL_TEXTLIMITS;
		mColumns[10] = PrizeWaterMarkTextResourceInfo.DBSQL_TEXTTIMETYPE;
		mColumns[11] = PrizeWaterMarkTextResourceInfo.DBSQL_TEXTVIEWALIGN;
		
		if(mWaterMarkDbSqlite == null){
			mWaterMarkDbSqlite = openDatabase();
		}
		
		Cursor cursor =  mWaterMarkDbSqlite.query(DBWaterMarkTextName,mColumns,PrizeWaterMarkTextResourceInfo.DBSQL_TEXTDATAID+"="+data_id,null, null, null, null);
		
		if(cursor!=null&&cursor.moveToFirst()){
			
			while (!cursor.isAfterLast()) {
				PrizeWaterMarkTextResourceInfo mPrizeWaterMarkTextResourceInfo = new PrizeWaterMarkTextResourceInfo();
				mPrizeWaterMarkTextResourceInfo.setAlbumDataId(cursor.getInt(cursor.getColumnIndex(PrizeWaterMarkTextResourceInfo.DBSQL_TEXTALBUMDATAID)));
				mPrizeWaterMarkTextResourceInfo.setTextDataId(cursor.getInt(cursor.getColumnIndex(PrizeWaterMarkTextResourceInfo.DBSQL_TEXTDATAID)));
				mPrizeWaterMarkTextResourceInfo.setInitString(cursor.getString(cursor.getColumnIndex(PrizeWaterMarkTextResourceInfo.DBSQL_TEXTINITSTRING)));
				mPrizeWaterMarkTextResourceInfo.setTextSize(cursor.getInt(cursor.getColumnIndex(PrizeWaterMarkTextResourceInfo.DBSQL_TEXTSIZE)));
				mPrizeWaterMarkTextResourceInfo.setTextIsVertical(cursor.getInt(cursor.getColumnIndex(PrizeWaterMarkTextResourceInfo.DBSQL_TEXT_V))>0?true:false);
				mPrizeWaterMarkTextResourceInfo.setTextColor(cursor.getString(cursor.getColumnIndex(PrizeWaterMarkTextResourceInfo.DBSQL_TEXTCOLOR)));
				mPrizeWaterMarkTextResourceInfo.setTextWtype(cursor.getInt(cursor.getColumnIndex(PrizeWaterMarkTextResourceInfo.DBSQL_TEXTTYPE)));
				float density = mContext.getResources().getDisplayMetrics().density;
				mPrizeWaterMarkTextResourceInfo.setTextXPostion((int)(cursor.getFloat(cursor.getColumnIndex(PrizeWaterMarkTextResourceInfo.DBSQL_TEXTXPOSTION))*density));
				mPrizeWaterMarkTextResourceInfo.setTextYPostion((int)(cursor.getFloat((cursor.getColumnIndex(PrizeWaterMarkTextResourceInfo.DBSQL_TEXTYPOSTION)))*density));
				mPrizeWaterMarkTextResourceInfo.setTextLimitPostion(cursor.getInt(cursor.getColumnIndex(PrizeWaterMarkTextResourceInfo.DBSQL_TEXTLIMITS)));
				mPrizeWaterMarkTextResourceInfo.setTimeTextType(cursor.getString(cursor.getColumnIndex(PrizeWaterMarkTextResourceInfo.DBSQL_TEXTTIMETYPE)));
				mPrizeWaterMarkTextResourceInfo.setTextViewAlign(cursor.getInt(cursor.getColumnIndex(PrizeWaterMarkTextResourceInfo.DBSQL_TEXTVIEWALIGN)));
				mPrizeWaterMarkTextList.add(mPrizeWaterMarkTextResourceInfo);
				cursor.moveToNext();
			}
		}
		
		cursor.close();
		
		return mPrizeWaterMarkTextList;
	}
}
