package com.mediatek.gallery3d.video;

import android.app.ActionBar;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

/**
 * This Hooker is come form letv requirement
 * It used to set video title or landscape by intent bundles
 *
 */
public class LetvHooker extends MovieHooker {

    private static final String TAG = "Gallery2/VideoPlayer/LetvHooker";
    // / M: add for le @{
    public static final String SCREEN_ORIENTATION_LANDSCAPE = "SCREEN_ORIENTATION_LANDSCAPE";
    public static final String STREAMING_VIDEO_TITLE = "STREAMING_VIDEO_TITLE";
    private boolean mIsLandscape = false;
    private String mVideoTitle = null;

    // / @}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // / M: for le landscape playback and show title from intent@{
        Intent intent = getIntent();
        mIsLandscape = intent.getBooleanExtra(SCREEN_ORIENTATION_LANDSCAPE,
                false);
        if (mIsLandscape) {
            getContext().setRequestedOrientation(
                    ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        mVideoTitle = intent.getStringExtra(STREAMING_VIDEO_TITLE);
        // / @}
    }

    @Override
    public void onResume() {
        enhanceActionBar();
    }

    // / M: enhance the title feature @{
    private void enhanceActionBar() {
        final IMovieItem movieItem = getMovieItem();
        if (movieItem == null) {
            Log.e(TAG, "enhanceActionBar, movieItem == null");
            return;
        }
        final Uri uri = movieItem.getUri();
        final String scheme = movieItem.getUri().getScheme();
        final String authority = movieItem.getUri().getAuthority();
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String title = null;
                // / M: get title from le @{
                if (mVideoTitle != null) {
                    title = mVideoTitle;
                    Log.v(TAG, "enhanceActionBar() task return mVideoTitle "
                            + title);
                    return title;
                }
                // / @}
                // / M: Get title from uri if the title can not find in
                // database @{
                try {
                    if (ContentResolver.SCHEME_FILE.equals(scheme)) { // from
                                                                      // file
                                                                      // manager
                        title = MovieTitleHelper.getTitleFromMediaData(
                                getContext(), uri);
                        if (title == null) {
                            title = MovieTitleHelper.getTitleFromUri(uri);
                        }
                    } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
                        title = MovieTitleHelper.getTitleFromDisplayName(
                                getContext(), uri);
                        if (title == null) {
                            title = MovieTitleHelper.getTitleFromData(
                                    getContext(), uri);
                        }
                    } else {
                        title = MovieTitleHelper.getTitleFromUri(uri);
                    } // / @}
                } catch (final IllegalArgumentException ex) {
                    Log.v(TAG,
                            "enhanceActionBar() IllegalArgumentException happen ");
                    return null;
                }
                Log.v(TAG, "enhanceActionBar() task return " + title);
                return title;
            }

            @Override
            protected void onPostExecute(String result) {
                Log.v(TAG, "onPostExecute(" + result + ") movieItem="
                        + movieItem + ", mMovieItem=" + getMovieItem());
                movieItem.setTitle(result);
                if (movieItem == getMovieItem()) {
                    setActionBarTitle(result);
                }
            };
        }.execute();
        Log.v(TAG, "enhanceActionBar() " + movieItem);
    }

    private void setActionBarTitle(String title) {
        ActionBar actionBar = getContext().getActionBar();
        Log.v(TAG, "setActionBarTitle(" + title + ") actionBar = " + actionBar);
        if (actionBar != null && title != null) {
            actionBar.setTitle(title);
        }
    }
}
