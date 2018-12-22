package com.prize.videoc.presenter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;

/**
 * Created by yiyi on 2015/6/10.
 */
public class BroadcastPresenter {
	private IBroadcastView iView;
	private NContentObserver mObserver;
	private BroadcastReceiver mReceiver;

	public BroadcastPresenter(IBroadcastView view) {
		iView = view;
	}

	public void registerUri(Context ctx, Uri uri) {
		if (mObserver != null) {
			ctx.getContentResolver().unregisterContentObserver(mObserver);
		}
		mObserver = new NContentObserver(new Handler()) {
		};
		ctx.getContentResolver().registerContentObserver(uri, true, mObserver);
	}

	public void register(Context ctx, String... actions) {
		if (mReceiver == null) {
			mReceiver = new BroadcastReceiver() {

				@Override
				public void onReceive(Context context, Intent intent) {
					iView.onReceive(intent);
				}
			};
		}
		IntentFilter filter = new IntentFilter();
		for (String action : actions) {
			filter.addAction(action);
		}
		ctx.registerReceiver(mReceiver, filter);
	}

	private class NContentObserver extends ContentObserver {

		public NContentObserver(Handler handler) {
			super(handler);
		}

		@Override
		public void onChange(boolean selfChange) {
			iView.onChange();
		}
	}

	public void unRegister(Context ctx) {
		if (mObserver != null)
			ctx.getContentResolver().unregisterContentObserver(mObserver);

		if (mReceiver != null)
			ctx.unregisterReceiver(mReceiver);

	}
}
