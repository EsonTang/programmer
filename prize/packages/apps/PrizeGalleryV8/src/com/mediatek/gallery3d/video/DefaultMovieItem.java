package com.mediatek.gallery3d.video;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

public class DefaultMovieItem implements IMovieItem {
    private static final String TAG = "Gallery2/VideoPlayer/DefaultMovieItem";

    private Context mContext;
    private Uri mUri;
    private String mMimeType;
    private String mTitle;
    private boolean mError;
    private SlowMotionItem mSlowMotionItem;

    /**
     * Constructor.
     * Instantiate to record video information.
     * If platform support slow motion, need new SlowMotionItem
     * to check current video whether is slow motion video.
     * @param context
     * @param uri
     * @param mimeType
     * @param title
     */
    public DefaultMovieItem(Context context, Uri uri, String mimeType,
            String title) {
        mContext = context;
        mUri = uri;
        mMimeType = mimeType;
        mTitle = title;
        if (MtkVideoFeature.isSlowMotionSupport()) {
            mSlowMotionItem = new SlowMotionItem(context, mUri);
        } else {
            mSlowMotionItem = null;
        }
    }

    /**
     * Constructor.
     * Instantiate to record video information.
     * If platform support slow motion, need new SlowMotionItem
     * to check current video whether is slow motion video.
     * @param context
     * @param uri
     * @param mimeType
     * @param title
     */
    public DefaultMovieItem(Context context, String uri, String mimeType,
            String title) {
        this(context, Uri.parse(uri), mimeType, title);
    }

    @Override
    public Uri getUri() {
        return mUri;
    }

    @Override
    public String getMimeType() {
        return mMimeType;
    }

    @Override
    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    @Override
    public void setUri(Uri uri) {
        mUri = uri;
    }

    @Override
    public void setMimeType(String mimeType) {
        mMimeType = mimeType;
    }

    @Override
    public SlowMotionItem getSlowMotionItem() {
        return mSlowMotionItem;
    }

    @Override
    public void setSlowMotionItem(SlowMotionItem slowMotionItem) {
        mSlowMotionItem = slowMotionItem;
    }

    @Override
    public boolean isSlowMotion() {
        boolean isSlowMotion = MtkVideoFeature.isSlowMotionSupport() && mSlowMotionItem != null
                && mSlowMotionItem.isSlowMotionVideo();
        Log.v(TAG, "isSlowMotion = " + isSlowMotion);
        return isSlowMotion;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("MovieItem(uri=").append(mUri)
                .append(", mime=").append(mMimeType).append(", title=")
                .append(mTitle).append(", error=").append(mError)
                .append(", mSlowMotionItem=").append(mSlowMotionItem)
                .append(")").toString();
    }
}