package com.prize.videoc.presenter;

import com.prize.videoc.LocalActivity;
import com.prize.videoc.bean.PVideo;
import com.prize.videoc.db.DbManager;
import com.prize.videoc.db.RecordDao;
import com.prize.videoc.db.VideoProvider;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class HistoryUpdateBroadcast extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		PVideo mPVideo=VideoProvider.findVideoByName(context, (String) intent.getExtras().get("videoName"));
		Log.i("pengcc", "video:"+mPVideo);
		if (mPVideo!=null) {
			RecordDao.saveLastPlay(DbManager.getInstance().getDb(), mPVideo);
			LocalActivity.getInstance().updateHistory(mPVideo);
		}else {
			LocalActivity.getInstance().updateHistory(null);
		}
	}

}
