/*
 * Copyright (C) 2014 MediaTek Inc.
 * Modification based on code covered by the mentioned copyright
 * and/or permission notice(s).
 */
/*
 * Copyright (C) 2007 The Android Open Source Project
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

package com.android.gallery3d.app;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContentUris;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ActivityChooserModel.OnChooseActivityListener;
import android.widget.ActivityChooserModel;
import android.widget.ActivityChooserView;
import android.widget.ShareActionProvider;
import android.content.ComponentName;

import com.android.gallery3d.R;
import com.android.gallery3d.common.ApiHelper;
import com.mediatek.gallery3d.ext.IActivityHooker;
import com.mediatek.gallery3d.video.IMovieItem;
import com.mediatek.gallery3d.video.DefaultMovieItem;
import com.mediatek.gallery3d.video.ExtensionHelper;
import com.mediatek.gallery3d.video.LetvHooker;
import com.mediatek.gallery3d.video.MovieUtils;
import com.mediatek.gallery3d.video.MtkVideoFeature;
import com.mediatek.gallery3d.video.RequestPermissionActivity;
// Nav bar color customized feature. prize-linkh-2017.08.31 @{
import com.mediatek.common.prizeoption.PrizeOption;
import android.graphics.Color;
// @}

/**
 * This activity plays a video from a specified URI. The client of this activity
 * can pass a logo bitmap in the intent (KEY_LOGO_BITMAP) to set the action bar
 * logo so the playback process looks more seamlessly integrated with the
 * original activity.
 */
public class MovieActivity extends Activity {
    @SuppressWarnings("unused")
    private static final String TAG = "Gallery2/VideoPlayer/MovieActivity";

    public static final String KEY_LOGO_BITMAP = "logo-bitmap";
    public static final String KEY_TREAT_UP_AS_BACK = "treat-up-as-back";
    private static final String SCHEME_FILE = "file";
    private static final String SCHEME_CONTENT_FILE = "content://media/external/file";
    public static final String KEY_LAUNCH_FROM_VIDEO = "isVideo";
    public static final String KEY_LAUNCH_FROM_SECURE_VIDEO = "isSecureVideo";

    private IActivityHooker mMovieHooker;
    private MoviePlayer mPlayer;
    private boolean mFinishOnCompletion;
    private boolean mTreatUpAsBack;
    // /M: add for streaming cookie
    public static final String COOKIE = "Cookie";
    private IMovieItem mMovieItem;
    // / M: add for share menu {@
    private MenuItem mShareMenu;
    private ShareActionProvider mShareProvider;
    private ActivityChooserModel mDataModel;
    private boolean isVideo = false;
    // @}

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void setSystemUiVisibility(View rootView) {
        if (ApiHelper.HAS_VIEW_SYSTEM_UI_FLAG_LAYOUT_STABLE) {
            rootView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.v(TAG, "onCreate()");
        super.onCreate(savedInstanceState);
        // / M: [FEATURE.MODIFY] [Runtime permission] {@
        if (RequestPermissionActivity.startPermissionActivity(this)) {
            Log.v(TAG, "onCreate(), need start permission activity, return");
            return;
        }
        // @}

        requestWindowFeature(Window.FEATURE_ACTION_BAR);
        requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        setContentView(R.layout.movie_view);
        View rootView = findViewById(R.id.movie_view_root);
        setSystemUiVisibility(rootView);
        Intent intent = getIntent();
        if (intent.hasExtra(KEY_LAUNCH_FROM_VIDEO)) {
            isVideo = intent.getBooleanExtra(KEY_LAUNCH_FROM_VIDEO, false);
        }
        Log.v(TAG, "onCreate() intent = " + intent + " isVideo = " + isVideo);
        if (!initMovieInfo(intent)) {
            Log.e(TAG, "finish activity");
            finish();
            return;
        }
        initializeActionBar(intent);
        mMovieHooker = ExtensionHelper.getHooker(this);
        mPlayer = new MoviePlayer(rootView, this, mMovieItem,
                savedInstanceState, !mFinishOnCompletion,
                intent.getStringExtra(COOKIE)) {
            @Override
            public void onCompletion() {
                Log.i(TAG, "onCompletion() mFinishOnCompletion="
                        + mFinishOnCompletion);
                if (mFinishOnCompletion) {
                    finish();
                }
				if(isVideo){
					Log.i(TAG,"onCompletion startactivity VideoActivity");
                    Intent intent = new Intent();  
                    intent.setComponent(new ComponentName("com.prize.videoc",  
                            "com.prize.videoc.LocalActivity"));  
					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                    startActivity(intent); 
				}
            }
        };

        if (intent.hasExtra(MediaStore.EXTRA_SCREEN_ORIENTATION)) {
            int orientation = intent.getIntExtra(
                    MediaStore.EXTRA_SCREEN_ORIENTATION,
                    ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
            if (orientation != getRequestedOrientation()) {
                setRequestedOrientation(orientation);
            }
        }
        // set window parameters
        Window win = getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        winParams.buttonBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_OFF;
        /// M: [FEATURE.ADD] Mutil-window. Do not show full screen in
        // mutil-window mode to avoid overlay with notification bar @{
        if (!isMultiWindowMode()) {
			/// M: [FEATURE.ADD] liangchangwei modify for notch screen feature
            Log.i(TAG, "onCreate(), add FLAG_FULLSCREEN");
			if(PrizeOption.PRIZE_NOTCH_SCREEN){
				win.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
			}else{
			    winParams.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
			}
        }
        // @}
        if (intent.hasExtra(KEY_LAUNCH_FROM_SECURE_VIDEO)&&intent.getBooleanExtra(KEY_LAUNCH_FROM_SECURE_VIDEO, false)) {
			Log.w(TAG,"set Flag FLAG_SHOW_WHEN_LOCKED");
			winParams.flags |= WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
        }else{
			//winParams.flags |= ~WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
			if((winParams.flags&WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED) != 0){
				Log.w(TAG,"need clear Flag FLAG_SHOW_WHEN_LOCKED");
				winParams.flags &= ~WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
			}
			Log.w(TAG,"Flag FLAG_SHOW_WHEN_LOCKED has clear");
        }
        win.setAttributes(winParams);
        win.setFormat(PixelFormat.TRANSLUCENT);
        // We set the background in the theme to have the launching animation.
        // But for the performance (and battery), we remove the background here.
        win.setBackgroundDrawable(null);

        if (mMovieHooker != null) {
            mMovieHooker.init(this, intent);
            mMovieHooker.setParameter(null, mMovieItem);
            mMovieHooker.setParameter(null, mPlayer);
            mMovieHooker.setParameter(null, mPlayer.getVideoSurface());
            mMovieHooker.setParameter(null, mPlayer.getPlayerWrapper());
            mMovieHooker.onCreate(savedInstanceState);
        }
        // Nav bar color customized feature. prize-linkh-2017.08.31 @{
        if(PrizeOption.PRIZE_NAVBAR_COLOR_CUST) {
            getWindow().setDisableCustNavBarColor(true);
        } // @} 
    }

    @Override
    public void onStart() {
        Log.v(TAG, "onStart()");
        super.onStart();
        if (mMovieHooker != null) {
            mMovieHooker.onStart();
        }
    }
    
    @Override
    public void onBackPressed() {
       Log.d(TAG, "onBackPressed");
       if (!mPlayer.onBack()) {
		   if(isVideo){
			   Log.i(TAG,"onBackPressed isVideo = " + isVideo);
			   Intent intent = new Intent();  
			   intent.setComponent(new ComponentName("com.prize.videoc",  
					   "com.prize.videoc.LocalActivity"));	
			   intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
			   startActivity(intent); 
			   finish();
		   }else{
               super.onBackPressed();
		   }
       }
    }

    @Override
    public void onResume() {
        Log.v(TAG, "onResume()");
        super.onResume();
        /// M: [FEATURE.ADD] Mutil-window. Refresh shareProvider when resume
        // in mutil-window to avoid share with incorrect video uri @{
        if (isMultiWindowMode()) {
            refreshShareProvider(mMovieItem);
        }
        // @}
        if (mPlayer != null) {
            mPlayer.onResume();
        }
        if (mMovieHooker != null) {
            mMovieHooker.onResume();
        }
    }

    @Override
    public void onPause() {
        Log.v(TAG, "onPause()");
        super.onPause();
        if (mPlayer != null) {
            mPlayer.onPause();
        }
        collapseShareMenu();
        if (mMovieHooker != null) {
            mMovieHooker.onPause();
        }
    }

    @Override
    protected void onStop() {
        Log.v(TAG, "onStop()");
        super.onStop();
        if (mPlayer != null) {
            mPlayer.onStop();
        }
        if (mMovieHooker != null) {
            mMovieHooker.onStop();
        }
    }

    @Override
    public void onDestroy() {
        Log.v(TAG, "onDestroy()");
        if (mPlayer != null) {
            mPlayer.onDestroy();
        }
        if (mMovieHooker != null) {
            mMovieHooker.onDestroy();
        }
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        Log.v(TAG, "onCreateOptionsMenu");
        if (mMovieItem == null) {
            Log.w(TAG, "onCreateOptionsMenu, mMovieItem is null, return");
            return false;
        }
        boolean local = MovieUtils.isLocalFile(mMovieItem.getUri(),
                mMovieItem.getMimeType());
        // / M: don't show share if intent from letv
        if (!MovieUtils.canShare(getIntent().getExtras())
                || (local && !ExtensionHelper.getMovieDrmExtension(this)
                        .canShare(this, mMovieItem))
                || getIntent().getBooleanExtra(
                        LetvHooker.SCREEN_ORIENTATION_LANDSCAPE, false)) {
            Log.w(TAG, "do not show share");
        } else {
            getMenuInflater().inflate(R.menu.movie, menu);
            mShareMenu = menu.findItem(R.id.action_share);
            ShareActionProvider provider = (ShareActionProvider) mShareMenu
                    .getActionProvider();
            mShareProvider = provider;
            // / M:useing ActivityChooserModel API to register a listener,
            // and activity chooser will not handle this intent when the value
            // of listener returns false.@{
            mDataModel = ActivityChooserModel.get(this,
                    ShareActionProvider.DEFAULT_SHARE_HISTORY_FILE_NAME);
            // }@

            refreshShareProvider(mMovieItem);
        }
        if (mMovieHooker != null) {
            return mMovieHooker.onCreateOptionsMenu(menu);
        }
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        Log.v(TAG, "onPrepareOptionsMenu");
        if (mMovieHooker != null) {
            return mMovieHooker.onPrepareOptionsMenu(menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            /*if (mTreatUpAsBack) {
                finish();
            } else {
                startActivity(new Intent(this, GalleryActivity.class));
                finish();
            }*/
        	finish();
            return true;
        } else if (id == R.id.action_share) {
            return true;
        }
        if (mMovieHooker != null) {
            return mMovieHooker.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.v(TAG, "onSaveInstanceState()");
        super.onSaveInstanceState(outState);
        if (mPlayer != null) {
            mPlayer.onSaveInstanceState(outState);
        }
    }

    private boolean initMovieInfo(Intent intent) {
        Uri original = intent.getData();
        Log.i(TAG, "initMovieInfo, original uri is " + original);
        Uri newUri = checkVideoUri(original);
        if (newUri == null) {
            Log.e(TAG, "initMovieInfo, acquired uri is null");
            return false;
        }
        String mimeType = intent.getType();
        mMovieItem = new DefaultMovieItem(getApplicationContext(), newUri,
                mimeType, null);
        mFinishOnCompletion = intent.getBooleanExtra(
                MediaStore.EXTRA_FINISH_ON_COMPLETION, true);
        mTreatUpAsBack = intent.getBooleanExtra(KEY_TREAT_UP_AS_BACK, false);

        Log.v(TAG, "initMovieInfo(" + newUri + ") mMovieInfo = " + mMovieItem
                + ", mFinishOnCompletion = " + mFinishOnCompletion
                + ", mTreatUpAsBack = " + mTreatUpAsBack);
        return true;
    }

    /// M: check the Video uri to ensure
    // 1, whether uri is null
    // 2, if uri's scheme is "file" type, transform it
    // 3, if uri is content file type, transform it @{
    private Uri checkVideoUri(Uri uri) {
        Uri newUri = null;
        if (uri == null) {
            return null;
        } else if (uri.getScheme() != null && uri.getScheme().equals(SCHEME_FILE)) {
            newUri = transcodeSchemeFileUri(uri);
        } else if (String.valueOf(uri).toLowerCase().startsWith(SCHEME_CONTENT_FILE)) {
            newUri = transcodeSchemeContentFileUri(uri);
        } else {
            newUri = uri;
        }
        Log.v(TAG, "checkVideoUri return new uri = " + newUri);
        return newUri;
    }

    // transform the uri from "file://" type to "content://" type
    private Uri transcodeSchemeFileUri(Uri uri) {
        Uri newUri = null;
        Cursor cursor = null;
        String videoPath = uri.getPath();
        Log.v(TAG, "transcodeSchemeFileUri, videoPath " + videoPath);
        try {
            if (videoPath != null) {
                videoPath = videoPath.replaceAll("'", "''");
            }
            cursor = getContentResolver()
                  .query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                  new String[] { MediaStore.Video.Media._ID },
                  MediaStore.Video.Media.DATA + " = '"
                  + videoPath + "'", null, null);
            if (cursor != null && cursor.moveToFirst()
                    && cursor.getCount() > 0) {
                int id = cursor.getInt(0);
                newUri = ContentUris.withAppendedId(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id);
                Log.v(TAG, "transcodeSchemeFileUri, newUri = " + newUri);
            } else {
                newUri = uri;
                Log.e(TAG, "transcodeSchemeFileUri, The uri not existed in video table");
            }
            return newUri;
        } catch (final SQLiteException ex) {
            ex.printStackTrace();
        } catch (final IllegalArgumentException ex) {
            ex.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        return null;
    }

    // transform the uri from "content://media/external/file" type
    // to "content://media/external/video/media" type
    private Uri transcodeSchemeContentFileUri(Uri uri) {
        Uri newUri = null;
        Cursor cursor = null;
        try {
            // get file id
            long id = ContentUris.parseId(uri);
            // check file whether has existed in video table
            cursor = getContentResolver()
                  .query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, null,
                  MediaStore.Video.Media._ID + " = '" + id + "'", null, null);
            if (cursor != null && cursor.moveToFirst()
                    && cursor.getCount() > 0) {
                newUri = ContentUris.withAppendedId(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id);
                Log.v(TAG, "transcodeSchemeContentFileUri, newUri " + newUri);
            } else {
                newUri = uri;
                Log.e(TAG, "transcodeSchemeContentFileUri, The uri not existed in video table");
            }
            return newUri;
        } catch (final SQLiteException ex) {
            ex.printStackTrace();
        } catch (final IllegalArgumentException ex) {
            ex.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        return null;
    }
    /// @}

    private void setActionBarLogoFromIntent(Intent intent) {
        Bitmap logo = intent.getParcelableExtra(KEY_LOGO_BITMAP);
        if (logo != null) {
            getActionBar().setLogo(
                    new BitmapDrawable(getResources(), logo));
        }
    }

    private void initializeActionBar(Intent intent) {
        final ActionBar actionBar = getActionBar();
        if (actionBar == null) {
            return;
        }
        setActionBarLogoFromIntent(intent);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP,
                ActionBar.DISPLAY_HOME_AS_UP);
        // / M: show title for video playback
        actionBar.setDisplayOptions(actionBar.getDisplayOptions()
                | ActionBar.DISPLAY_SHOW_TITLE);
    }

    private void setActionBarTitle(String title) {
        ActionBar actionBar = getActionBar();
        Log.v(TAG, "setActionBarTitle(" + title + ") actionBar = " + actionBar);
        if (actionBar != null && title != null) {
            actionBar.setTitle(title);
        }
    }

    public boolean isMultiWindowMode() {
        boolean isMultiWindow = false;
        if (MtkVideoFeature.isMultiWindowSupport() && isInMultiWindowMode()) {
            isMultiWindow = true;
        }
        Log.d(TAG, "isInMultiWindowMode = " + isMultiWindow
                + ", sdk version = " + Build.VERSION.SDK_INT);
        return isMultiWindow;
    }

    public void refreshMovieInfo(IMovieItem info) {
        Log.v(TAG, "refreshMovieInfo(" + info + ")");
        mMovieItem = info;
        setActionBarTitle(info.getTitle());
        refreshShareProvider(info);
        mMovieHooker.setParameter(null, mMovieItem);
    }

    private final OnChooseActivityListener mChooseActivityListener
        = new OnChooseActivityListener() {
        @Override
        public boolean onChooseActivity(ActivityChooserModel host, Intent intent) {
            startActivity(intent);
            Log.v(TAG, "onChooseActivity," + intent);
            // Return true meanings framework not start activity, APP will
            // handle it.
            return true;
        }
    };

    private void refreshShareProvider(IMovieItem info) {
        // / M:useing ActivityChooserModel API to register a listener,
        // and activity chooser will not handle this intent when the value of
        // listener returns false.@{
        if (mDataModel != null) {
            mDataModel.setOnChooseActivityListener(mChooseActivityListener);
        }
        // }@

        // Document says EXTRA_STREAM should be a content: Uri
        // So, we only share the video if it's "content:".
        // / M: the upper is JellyBean's comment, here we enhance the share
        // action.
        if (mShareProvider != null) {
            Intent intent = new Intent(Intent.ACTION_SEND);
            if (MovieUtils.isLocalFile(info.getUri(), info.getMimeType())) {
                intent.setType("video/*");
                intent.putExtra(Intent.EXTRA_STREAM, info.getUri());
            } else {
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT,
                        String.valueOf(info.getUri()));
                Log.v(TAG,
                        "share as text/plain, info.getUri() = " + info.getUri());
            }
            mShareProvider.setShareIntent(intent);
        }
        Log.v(TAG, "refreshShareProvider() mShareProvider=" + mShareProvider);
    }

    /*
     * M: ActivityChooseView's popup window will not dismiss when user press
     * power key off and on quickly. Here dismiss the popup window if need.
     * Note: dismissPopup() will check isShowingPopup().
     * @{
     */
    private void collapseShareMenu() {
        if (mShareMenu != null
                && mShareMenu.getActionView() instanceof ActivityChooserView) {
            ActivityChooserView chooserView = (ActivityChooserView) mShareMenu
                    .getActionView();
            Log.v(TAG, "collapseShareMenu() chooserView.isShowingPopup()="
                    + chooserView.isShowingPopup());
            chooserView.dismissPopup();
        }
    }

    /*--modify by liangchangwei fix bug 53962  2018-4-12---*/
    public boolean isShowShareMenu(){
        if (mShareMenu != null
                && mShareMenu.getActionView() instanceof ActivityChooserView) {
			ActivityChooserView chooserView = (ActivityChooserView) mShareMenu
					.getActionView();
			Log.v(TAG, "isShowShareMenu() chooserView.isShowingPopup() = "
					+ chooserView.isShowingPopup());
			return chooserView.isShowingPopup();
        }else{
			return false;
        }
    }
    /*--modify by liangchangwei fix bug 53962  2018-4-12---*/
    /* @} */

    /**
     * M: MoviePlayer call this function to set IMoviePlayer
     */
    public void setMovieHookerParameter(String key, Object value) {
        Log.v(TAG, "setMovieHookerParameter key = " + key + " value = " + value);
        if (mMovieHooker != null) {
            mMovieHooker.setParameter(key, value);
        }
    }

    public IMovieItem getMovieItem() {
        return mMovieItem;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (mPlayer != null) {
            return mPlayer.onKeyDown(keyCode, event)
                    || super.onKeyDown(keyCode, event);
        }
        return false;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (mPlayer != null) {
            return mPlayer.onKeyUp(keyCode, event) || super.onKeyUp(keyCode, event);
        }
        return false;
    }

    @Override
    @TargetApi(Build.VERSION_CODES.N)
    public void onMultiWindowModeChanged(boolean isInMultiWIndowMode) {
        Log.d(TAG, "[onMultiWindowModeChanged] isInMultiWIndowMode = " + isInMultiWIndowMode);
        if (isInMultiWIndowMode) {
            Log.d(TAG, "[onMultiWindowModeChanged] clear FLAG_FULLSCREEN");
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            Log.d(TAG, "[onMultiWindowModeChanged] add FLAG_FULLSCREEN");
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }

	public boolean isFromVideo(){
        Log.w(TAG,"isFromVideo isVideo = " + isVideo);
        return isVideo;
	}
}
