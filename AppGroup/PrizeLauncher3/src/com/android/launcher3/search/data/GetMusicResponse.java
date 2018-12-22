package com.android.launcher3.search.data;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.android.launcher3.R;
import com.android.launcher3.search.GroupMemberBean;
import com.android.launcher3.search.MyExpandableAdapter;

public class GetMusicResponse extends RSTResponse {

	public GetMusicResponse(Context mContext,
			List<GroupMemberBean> mGroupBeanList,
			HashMap<Integer, List<GroupMemberBean>> mGroupChildList,
			String groupTitle, MyExpandableAdapter adpter, Runnable r,HashMap<String, AsyncTaskCallback> groupClass,List<String> groups) {
		super(mContext, mGroupBeanList, mGroupChildList, groupTitle, adpter, r,groupClass,groups);
		// TODO Auto-generated constructor stub
	}
	
	private void loadFileData() {
		ContentResolver resolver = mContext.getContentResolver();
		Cursor cursor = resolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null, null);
		cursor.moveToFirst();
		List<MusicBean> musicList = new ArrayList<MusicBean>();
		if (cursor.moveToFirst()) {
			do {
				String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)); // 鏍囬
				long size = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.SIZE)); // 澶у皬
				String url = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
				 long duration = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)); // 鏃堕暱
				 String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));

					GroupMemberBean childBean = null;
				 if (duration >= 1000 && duration <= 900000) {
					 MusicBean music = new MusicBean();
					 music.setName(title);
					 music.setSize(size);
					 music.setUrl(url);
					 music.setDuration(duration);
					 musicList.add(music);
					 childBean = fillData(title);
					childBean.setMusic(music);

					Drawable icon = mContext
							.getResources()
							.getDrawable(
									R.drawable.a_target_selector);
					childBean.setIcon(icon);
					mTask.doPublishProgress(childBean);
				 }
				
			} while(cursor.moveToNext());
		}

	}
	
	
	@Override
	public List<GroupMemberBean> run() {
		try {
			loadFileData();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void onClick(GroupMemberBean item) {
		String path = item.getMusic().getUrl();
	      
	      
	      Intent i=new Intent(Intent.ACTION_VIEW);
	      File filemusic=new File(path);
	      Uri uri=Uri.parse("file://"+path);
	      //Uri uri=Uri.fromFile(filemusic);
	      i.setDataAndType(uri, "audio/*");
	      mContext.startActivity(i);
	      
	      
	}

}
