/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.prize.music.app.widgets;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.RemoteViews;

import com.prize.music.activities.AudioPlayerActivity;
import com.prize.music.service.ApolloService;
import com.prize.music.R;

/**
 * Simple widget to show currently playing album art along with play/pause and
 * next track buttons.
 */
public class AppWidget42 extends AppWidgetProvider {
	ProgressBar progress_horizontal;
	public static final String CMDAPPWIDGETUPDATE = "appwidgetupdate4x2";

	private static AppWidget42 sInstance;
	// private Context mContext;
	private ApolloService mService;

	private int[] appWidgetIds;
	private RemoteViews views;

	public static synchronized AppWidget42 getInstance() {
		if (sInstance == null) {
			sInstance = new AppWidget42();
		}
		return sInstance;
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		defaultAppWidget(context, appWidgetIds);
		// Send broadcast intent to any running ApolloService so it can
		// wrap around with an immediate update.
		Intent updateIntent = new Intent(ApolloService.SERVICECMD);
		updateIntent.putExtra(ApolloService.CMDNAME,
				AppWidget42.CMDAPPWIDGETUPDATE);
		updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,
				appWidgetIds);
		updateIntent.addFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY);
		context.sendBroadcast(updateIntent);
	}

	/**
	 * Initialize given widgets to default state, where we launch Music on
	 * default click and hide actions if service not running.
	 */
	private void defaultAppWidget(Context context, int[] appWidgetIds) {
		final RemoteViews views = new RemoteViews(context.getPackageName(),
				R.layout.fourbytwo_app_widget);
		linkButtons(context, views, false /* not playing */);
		pushUpdate(context, appWidgetIds, views);
	}

	private void pushUpdate(Context context, int[] appWidgetIds,
			RemoteViews views) {
		// Update specific list of appWidgetIds if given, otherwise default to
		// // all
		// mContext = context;
		// mHandler.sendEmptyMessageDelayed(999, 300);
		final AppWidgetManager gm = AppWidgetManager.getInstance(context);
		
		try {
			if (appWidgetIds != null) {
				gm.updateAppWidget(appWidgetIds, views);
			} else {
				gm.updateAppWidget(new ComponentName(context, this.getClass()),
						views);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * Check against {@link AppWidgetManager} if there are any instances of this
	 * widget.
	 */
	private boolean hasInstances(Context context) {
		AppWidgetManager appWidgetManager = AppWidgetManager
				.getInstance(context);
		int[] appWidgetIds = appWidgetManager
				.getAppWidgetIds(new ComponentName(context, this.getClass()));
		return (appWidgetIds.length > 0);
	}

	/**
	 * Handle a change notification coming over from {@link ApolloService}
	 */
	public void notifyChange(ApolloService service, String what) {
		if (hasInstances(service)) {
			if (ApolloService.META_CHANGED.equals(what)
					|| ApolloService.PLAYSTATE_CHANGED.equals(what)
					|| ApolloService.REPEATMODE_CHANGED.equals(what)
					|| ApolloService.SHUFFLEMODE_CHANGED.equals(what)) {
				performUpdate(service, null);
			}
		}
	}

	/**
	 * Update all active widget instances by pushing changes
	 */
	@SuppressLint("NewApi")
	public void performUpdate(ApolloService service, int[] appWidgetIds) {
		Context mContext = service.getApplicationContext();
		views = new RemoteViews(mContext.getPackageName(),
					R.layout.fourbytwo_app_widget);
		views.setProgressBar(R.id.progress_horizontal, 1000, 0, false);
		this.mService = service;
		this.appWidgetIds = appWidgetIds;
		long next = refreshNow();
		mDuration = service.duration();
		queueNextRefresh(next);

		CharSequence trackName = service.getTrackName();
		views.setTextViewText(R.id.four_by_two_trackname, trackName);

		Bitmap bitmap = service.getAlbumBitmap();
		if (bitmap != null) {
			views.setImageViewBitmap(R.id.four_by_two_albumart, bitmap);
		} else {
			views.setImageViewResource(R.id.four_by_two_albumart,
					R.drawable.plug_default);
		}

		// Set correct drawable and contentDescription for pause state
		final boolean playing = service.isPlaying();
		if (playing) {
			views.setImageViewResource(R.id.four_by_two_control_play,
					R.drawable.plug_pause_selector);
			views.setContentDescription(R.id.four_by_two_albumart, service
					.getResources().getString(R.string.nowplaying));
		} else {
			views.setImageViewResource(R.id.four_by_two_control_play,
					R.drawable.plug_play_selector);
			views.setContentDescription(R.id.four_by_two_albumart, service
					.getResources().getString(R.string.app_name));
		}
		// ink actions buttons to intents
		linkButtons(service, views, playing);

		pushUpdate(service, appWidgetIds, views);

	}

	/**
	 * Link up various button actions using {@link PendingIntents}.
	 * 
	 * @param playerActive
	 *            True if player is active in background, which means widget
	 *            click will launch {@link MediaPlaybackActivity}, otherwise we
	 *            launch {@link MusicBrowserActivity}.
	 */
	private void linkButtons(Context context, RemoteViews views,
			boolean playerActive) {

		// Connect up various buttons and touch events
		Intent intent;
		PendingIntent pendingIntent;

		final ComponentName serviceName = new ComponentName(context,
				ApolloService.class);

		if (playerActive) {
			intent = new Intent(context, AudioPlayerActivity.class).putExtra(
					"started_from", "NOTIF_SERVICE");
			pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
			views.setOnClickPendingIntent(R.id.four_by_two_albumart,
					pendingIntent);
			// views.setOnClickPendingIntent(R.id.four_by_two_info,
			// pendingIntent);
		} else {
			intent = new Intent(context, AudioPlayerActivity.class);
			pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
			views.setOnClickPendingIntent(R.id.four_by_two_albumart,
					pendingIntent);
			// views.setOnClickPendingIntent(R.id.four_by_two_info,
			// pendingIntent);
		}

		intent = new Intent(ApolloService.TOGGLEPAUSE_ACTION);
		intent.setComponent(serviceName);
		pendingIntent = PendingIntent.getService(context, 0, intent, 0);
		views.setOnClickPendingIntent(R.id.four_by_two_control_play,
				pendingIntent);

		intent = new Intent(ApolloService.NEXT_ACTION);
		intent.setComponent(serviceName);
		pendingIntent = PendingIntent.getService(context, 0, intent, 0);
		views.setOnClickPendingIntent(R.id.four_by_two_control_next,
				pendingIntent);

		intent = new Intent(ApolloService.PREVIOUS_ACTION);
		intent.setComponent(serviceName);
		pendingIntent = PendingIntent.getService(context, 0, intent, 0);
		views.setOnClickPendingIntent(R.id.four_by_two_control_prev,
				pendingIntent);

	}

	private static final int REFRESH = 1;
	/**
	 * We need to refresh the time via a Handler
	 */
	private final Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case REFRESH:
				long next = refreshNow();
				queueNextRefresh(next);
				break;
			// case UPDATEINFO:
			// break;
			default:
				break;
			}
		}
	};
	private boolean paused = false;

	/**
	 * @param delay
	 */
	private void queueNextRefresh(long delay) {
		if (!paused && mService.isPlaying()) {
			Message msg = mHandler.obtainMessage(REFRESH);
			mHandler.removeMessages(REFRESH);
			mHandler.sendMessageDelayed(msg, delay);
		}
	}

	private long mDuration, mPosOverride = -1;

	/**
	 * @return current time
	 */
	private long refreshNow() {
		if (mService == null)
			return 500;
		long pos = mPosOverride < 0 ? mService.position() : mPosOverride;
		// long remaining = 1000 - (pos % 1000);
		if ((pos >= 0) && (mDuration > 0)) {
			int Tag = (int) (1000 * pos / mDuration);
			views.setProgressBar(R.id.progress_horizontal, 1000, Tag, false);
			pushUpdate(mService, appWidgetIds, views);
		}
		// return remaining;
		return 1000;
	}

	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
		paused = true;
		mHandler.removeMessages(REFRESH);
		super.onDeleted(context, appWidgetIds);
	}

}
