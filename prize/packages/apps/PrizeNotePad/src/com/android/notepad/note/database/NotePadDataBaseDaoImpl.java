
/*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：数据库操作实现
 *当前版本：V1.0
 *作	者：朱道鹏
 *完成日期：2015-04-17
 *修改记录：
 *修改日期：
 *版 本 号：
 *修 改 人：
 *修改内容：
 ...
 *修改记录：
 *修改日期：
 *版 本 号：
 *修 改 人：
 *修改内容：
 *********************************************/

package com.android.notepad.note.database;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.android.notepad.note.model.NoteEvent;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 **
 * 类描述：数据库操作实现类
 * @author 朱道鹏
 * @version V1.0
 */
public class NotePadDataBaseDaoImpl implements NotePadDataBaseDao{

	private Context mContext;
	private NotePadDataBaseHelper mHelper;

	public NotePadDataBaseDaoImpl(Context context) {
		mContext = context;
		mHelper = NotePadDataBaseHelper.getInstance(mContext);
	}

	/**
	 * 方法描述：添加NoteEvent事件
	 * @param NoteEvent
	 * @return int
	 * @see NotePadDataBaseDaoImpl#create
	 */
	@Override
	public int create(NoteEvent event){
		int flag = -1;
		ContentValues values = new ContentValues();
		values.put(NoteEvent.CONTENTS, event.getContents());
		values.put(NoteEvent.CREATED_DATE, event.getCreateDate());
		values.put(NoteEvent.BG_COLOR, event.getBgColor());
		values.put(NoteEvent.FONT_SIZE, event.getFontSize());
		values.put(NoteEvent.UUID, event.getUuid());
		
		SQLiteDatabase mWriteDb = mHelper.getWritableDatabase();
		flag = (int)mWriteDb.insert(NoteEvent.TABLE_NAME, null, values);
		return flag;
	}


	/**
	 * 方法描述：查询数据库中所有事件并返回
	 * @param void
	 * @return List<NoteEvent>
	 * @see NotePadDataBaseDaoImpl#queryForAll
	 */
	@Override
	public List<NoteEvent> queryForAll(){
		List<NoteEvent> mNoteEventList = new ArrayList<NoteEvent>();
		SQLiteDatabase mReadDb = mHelper.getReadableDatabase();
		
		NoteEvent event = null;
		Cursor cursor = mReadDb.query(NoteEvent.TABLE_NAME, null, null, null, null, null, null);
		while(null != cursor && cursor.moveToNext()){
			event = new NoteEvent();
			event.setId(cursor.getInt(cursor.getColumnIndex(NoteEvent.ID)));
			event.setContents(cursor.getString(cursor.getColumnIndex(NoteEvent.CONTENTS)));
			event.setCreateDate(cursor.getLong(cursor.getColumnIndex(NoteEvent.CREATED_DATE)));
			event.setBgColor(cursor.getInt(cursor.getColumnIndex(NoteEvent.BG_COLOR)));
			event.setFontSize(cursor.getInt(cursor.getColumnIndex(NoteEvent.FONT_SIZE)));
			event.setUuid(cursor.getString(cursor.getColumnIndex(NoteEvent.UUID)));
			mNoteEventList.add(event);
		}
		cursor.close();
		
		Collections.sort(mNoteEventList, new Comparator<NoteEvent>() {
			public int compare(NoteEvent event1, NoteEvent event2) {
				return event1.compareTo(event2);
			}
		});
		return mNoteEventList;
	}

	/**
	 * 方法描述：删除_id=id行的数据
	 * @param int
	 * @return int
	 * @see NotePadDataBaseDaoImpl#deleteById
	 */
	@Override
	public int deleteById(int id){
		int flag = -1;
		SQLiteDatabase mWriteDb = mHelper.getWritableDatabase();
		String selection = NoteEvent.ID+" =?";
		String[] selectionArgs = new String[]{String.valueOf(id)};
		flag = mWriteDb.delete(NoteEvent.TABLE_NAME, selection, selectionArgs);
		return flag;
	}

	/**
	 * 方法描述：查询_id=id行的数据
	 * @param int
	 * @return NoteEvent
	 * @see NotePadDataBaseDaoImpl#queryForId
	 */
	@Override
	public NoteEvent queryForId(int id){
		NoteEvent event = new NoteEvent();
		SQLiteDatabase mReadDb = mHelper.getReadableDatabase();
		String selection = NoteEvent.ID+" =?";
		String[] selectionArgs = new String[]{String.valueOf(id)};
		
		Cursor cursor = mReadDb.query(NoteEvent.TABLE_NAME, null, selection, selectionArgs, null, null, null);
		if(null != cursor && cursor.moveToNext()){
			event.setId(cursor.getInt(cursor.getColumnIndex(NoteEvent.ID)));
			event.setContents(cursor.getString(cursor.getColumnIndex(NoteEvent.CONTENTS)));
			event.setCreateDate(cursor.getLong(cursor.getColumnIndex(NoteEvent.CREATED_DATE)));
			event.setBgColor(cursor.getInt(cursor.getColumnIndex(NoteEvent.BG_COLOR)));
			event.setFontSize(cursor.getInt(cursor.getColumnIndex(NoteEvent.FONT_SIZE)));
			event.setUuid(cursor.getString(cursor.getColumnIndex(NoteEvent.UUID)));
		}
		cursor.close();
		return event;
	}

	/**
	 * 方法描述：更新某NoteEvent对象数据
	 * @param NoteEvent
	 * @return int
	 * @see NotePadDataBaseDaoImpl#update
	 */
	@Override
	public int update(NoteEvent event){
		int flag = -1;
		SQLiteDatabase mWriteDb = mHelper.getWritableDatabase();
		
		ContentValues values = new ContentValues();
		values.put(NoteEvent.CONTENTS, event.getContents());
		values.put(NoteEvent.CREATED_DATE, event.getCreateDate());
		values.put(NoteEvent.BG_COLOR, event.getBgColor());
		values.put(NoteEvent.FONT_SIZE, event.getFontSize());
		values.put(NoteEvent.UUID, event.getUuid());
		
		String selection = NoteEvent.ID+" =?";
		String[] selectionArgs = new String[]{String.valueOf(event.getId())};
		
		flag = mWriteDb.update(NoteEvent.TABLE_NAME, values, selection, selectionArgs);
		return flag;
	}


	/**
	 * 方法描述：更新_id=id的NoteEvent对象数据
	 * @param NoteEvent int
	 * @return int
	 * @see NotePadDataBaseDaoImpl#updateId
	 */
	@Override
	public int updateId(NoteEvent event, int id){
		int flag = -1;
		SQLiteDatabase mWriteDb = mHelper.getWritableDatabase();
		
		ContentValues values = new ContentValues();
		values.put(NoteEvent.CONTENTS, event.getContents());
		values.put(NoteEvent.CREATED_DATE, event.getCreateDate());
		values.put(NoteEvent.BG_COLOR, event.getBgColor());
		values.put(NoteEvent.FONT_SIZE, event.getFontSize());
		values.put(NoteEvent.UUID, event.getUuid());
		
		String selection = NoteEvent.ID+" =?";
		String[] selectionArgs = new String[]{String.valueOf(id)};
		
		flag = mWriteDb.update(NoteEvent.TABLE_NAME, values, selection, selectionArgs);
		return flag;
	}


	/**
	 * 方法描述：根据关键字进行模糊查询，获取到对应的List
	 * @param String
	 * @return List<NoteEvent>
	 * @see NotePadDataBaseDaoImpl#queryForLike
	 */
	@Override
	public List<NoteEvent> queryForLike(String keyWord) {
		List<NoteEvent> mNoteEventList = new ArrayList<NoteEvent>();
		SQLiteDatabase mReadDb = mHelper.getReadableDatabase();
		String selection = NoteEvent.CONTENTS+" like ?";
		String[] selectionArgs = new String[]{"%"+keyWord+"%"};
		
		NoteEvent event = null;
		Cursor cursor = mReadDb.query(NoteEvent.TABLE_NAME, null, selection, selectionArgs, null, null, null);
		while(null != cursor && cursor.moveToNext()){
			event = new NoteEvent();
			event.setId(cursor.getInt(cursor.getColumnIndex(NoteEvent.ID)));
			event.setContents(cursor.getString(cursor.getColumnIndex(NoteEvent.CONTENTS)));
			event.setCreateDate(cursor.getLong(cursor.getColumnIndex(NoteEvent.CREATED_DATE)));
			event.setBgColor(cursor.getInt(cursor.getColumnIndex(NoteEvent.BG_COLOR)));
			event.setFontSize(cursor.getInt(cursor.getColumnIndex(NoteEvent.FONT_SIZE)));
			event.setUuid(cursor.getString(cursor.getColumnIndex(NoteEvent.UUID)));
			mNoteEventList.add(event);
		}
		cursor.close();
		
		Collections.sort(mNoteEventList, new Comparator<NoteEvent>() {
			public int compare(NoteEvent event1, NoteEvent event2) {
				return event1.compareTo(event2);
			}
		});
		return mNoteEventList;
	}
}

