package com.android.notepad.note.debug;


import com.android.notepad.note.database.NotePadDataBaseDao;
import com.android.notepad.note.database.NotePadDataBaseDaoImpl;
import com.android.notepad.note.model.NoteEvent;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

public class NoteDebugActivity extends Activity {

	private long mCurrentTime;
	private NotePadDataBaseDao mNotePadDataBaseDao;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getNotePadCreateTime();
		insert1000();

		Toast.makeText(this, "insert 1000", Toast.LENGTH_SHORT).show();

		Intent intent = new Intent();
		intent.setClass(this, com.android.notepad.NotePadActivity.class);
		startActivity(intent);
		finish();
	}

	private void insert1000() {
		String content = "insert 1000";

		int font_size = 24;
		int id_bg_color = 2;

		NoteEvent event = new NoteEvent();
		event.setContents(content);
		event.setCreateDate(mCurrentTime);
		event.setBgColor(id_bg_color);
		event.setFontSize(font_size);

		mNotePadDataBaseDao = new NotePadDataBaseDaoImpl(this);
		mNotePadDataBaseDao.create(event);
	}

	private void getNotePadCreateTime() {

		mCurrentTime = System.currentTimeMillis();
	}

}
