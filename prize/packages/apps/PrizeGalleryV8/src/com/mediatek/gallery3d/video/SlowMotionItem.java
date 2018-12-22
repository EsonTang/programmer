
package com.mediatek.gallery3d.video;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.MediaStore.Video;
import android.text.TextUtils;
import android.util.Log;

import com.android.gallery3d.R;

public class SlowMotionItem {

    private static final String TAG = "Gallery2/VideoPlayer/SlowMotionItem";

    public static final int NORMAL_VIDEO_FPS = 30;
    public static final int INVALID_FPS = -1;

    public static final int KEY_SLOW_MOTION_SPEED = 1800;
    public static final int KEY_SLOW_MOTION_SECTION = 1900;
    public static final int KEY_SLOW_MOTION_FPS = 2200;

    public static final int SLOW_MOTION_ONE_THIRTY_TWO_SPEED = 32; // 1/32x
    public static final int SLOW_MOTION_ONE_SIXTEENTH_SPEED = 16; // 1/16X
    public static final int SLOW_MOTION_ONE_EIGHT_SPEED = 8; // 1/8X
    public static final int SLOW_MOTION_QUARTER_SPEED = 4; // 1/4X
    public static final int SLOW_MOTION_HALF_SPEED = 2; // 1/2X
    public static final int SLOW_MOTION_NORMAL_SPEED = 1; // 1X
    public static final int NORMAL_VIDEO_SPEED = 0; // if normal video ,speed
                                                    // recorded in DB is 0.

    // default range, 1/32x -> 1/16x -> 1/8x -> 1/4x -> 1/2x -> 1x
    // use default range before mediaplayer started
    public static final int[] SPEED_RANGE = new int[] {
            SLOW_MOTION_ONE_THIRTY_TWO_SPEED, // 1/32x
            SLOW_MOTION_ONE_SIXTEENTH_SPEED, // 1/16x
            SLOW_MOTION_ONE_EIGHT_SPEED, // 1/8x
            SLOW_MOTION_QUARTER_SPEED, // 1/4x
            SLOW_MOTION_HALF_SPEED, // 1/2x
            SLOW_MOTION_NORMAL_SPEED
    // 1x
    };

    // 120fps video, without clear motion, 1/2x(default) -> 1/4x -> 1x
    public static final int[] SPEED_RANGE_120_WITHOUT_CLEARMOTION = new int[] {
            SLOW_MOTION_HALF_SPEED, // 1/2x
            SLOW_MOTION_NORMAL_SPEED, // 1x
            SLOW_MOTION_QUARTER_SPEED
    // 1/4x
    };

    // 240fps video, without clear motion, 1/4x(default) -> 1/8x -> 1x
    public static final int[] SPEED_RANGE_240_WITHOUT_CLEARMOTION = new int[] {
            SLOW_MOTION_QUARTER_SPEED, // 1/4x
            SLOW_MOTION_ONE_EIGHT_SPEED, // 1/8x
            SLOW_MOTION_NORMAL_SPEED
    // 1x
    };

    // 120fps video, with clear motion, 1/4x(default) -> 1/16x -> 1x
    public static final int[] SPEED_RANGE_120_WITH_CLEARMOTION = new int[] {
            SLOW_MOTION_QUARTER_SPEED, // 1/4x
            SLOW_MOTION_ONE_SIXTEENTH_SPEED, // 1/16x
            SLOW_MOTION_NORMAL_SPEED
    // 1x
    };

    // 240fps video, with clear motion, 1/8x(default) -> 1/32x -> 1x
    public static final int[] SPEED_RANGE_240_WITH_CLEARMOTION = new int[] {
            SLOW_MOTION_ONE_EIGHT_SPEED, // 1/8x
            SLOW_MOTION_ONE_THIRTY_TWO_SPEED, // 1/32x
            SLOW_MOTION_NORMAL_SPEED
    // 1x
    };

    // The mapping table of speed to icon resource
    private Map<Integer, Integer> mSpeedIconMapping = new HashMap<Integer, Integer>() {
        {
            put(SLOW_MOTION_NORMAL_SPEED, R.drawable.m_ic_slowmotion_1x_speed);
            put(SLOW_MOTION_HALF_SPEED, R.drawable.m_ic_slowmotion_2x_speed);
            put(SLOW_MOTION_QUARTER_SPEED, R.drawable.m_ic_slowmotion_4x_speed);
            put(SLOW_MOTION_ONE_EIGHT_SPEED, R.drawable.m_ic_slowmotion_8x_speed);
            put(SLOW_MOTION_ONE_SIXTEENTH_SPEED, R.drawable.m_ic_slowmotion_16x_speed);
            put(SLOW_MOTION_ONE_THIRTY_TWO_SPEED, R.drawable.m_ic_slowmotion_32x_speed);
        }
    };

    private Context mContext;
    private Uri mUri;
    private String mSlowMotionInfo;
    private int mStartTime;
    private int mEndTime;
    private int mSpeed;
    private int mDuration;

    public SlowMotionItem(Context context, Uri uri) {
        mContext = context;
        mUri = uri;
        updateItemUri(uri);
    }

    public int getSectionStartTime() {
        return mStartTime;
    }

    public int getSectionEndTime() {
        return mEndTime;
    }

    public int getSpeed() {
        return mSpeed;
    }

    public int getDuration() {
        return mDuration;
    }

    public String getSlowMotionInfo() {
        return mSlowMotionInfo;
    }

    public void setSectionStartTime(int startTime) {
        mStartTime = startTime;
    }

    public void setSectionEndTime(int endTime) {
        mEndTime = endTime;
    }

    public void setSpeed(int speed) {
        Log.v(TAG, "setSpeed speed = " + speed);
        mSpeed = speed;
    }

    public void updateItemToDB() {
        saveSlowMotionInfoToDB(mContext, mUri, mStartTime, mEndTime, mSpeed);
    }

    /**
     * Parse slowmotion information from string stored in
     * Video.Media.SLOW_MOTION_SPEED the format is (start, end)xSpeed
     *
     * @param uri
     */
    public void updateItemUri(Uri uri) {
        mUri = uri;
        updateItemUri();
    }

    public void updateItemUri() {
        getSlowMotionInfoFromDB(mContext, mUri);
        int[] time = getSlowMotionSectionFromString(mSlowMotionInfo);
        if (time != null) {
            mStartTime = time[0];
            mEndTime = time[1];
        }
        mSpeed = getSlowMotionSpeedFromString(mSlowMotionInfo);
        Log.v(TAG, "updateItemUri, " + this);
    }

    public boolean isSlowMotionVideo() {
        for (int i = 0; i < SPEED_RANGE.length; i++) {
            if (mSpeed == SPEED_RANGE[i]) {
                return true;
            }
        }
        if (MtkVideoFeature.isForceAllVideoAsSlowMotion()) {
            Log.i(TAG, "fore all video as slowmotion, set speed 1x");
            mSpeed = SLOW_MOTION_NORMAL_SPEED;
            return true;
        }
        return false;
    }

    private void getSlowMotionInfoFromDB(final Context context, final Uri uri) {
        Cursor cursor = null;
        try {
            String str = Uri.decode(uri.toString());
            str = str.replaceAll("'", "''");
            final String where = "_data LIKE '%"
                    + str.replaceFirst("file:///", "") + "'";
            String scheme = uri.getScheme();
            if (scheme == null) {
                Log.e(TAG, "scheme is null");
                return;
            }
            String[] projection = {
                    Video.Media.SLOW_MOTION_SPEED,
                    Video.Media.DURATION
            };
            if (scheme.equals("content")
                    && uri.getAuthority().equals(MediaStore.AUTHORITY)) {
                cursor = context.getContentResolver().query(uri, projection,
                        null, null, null);
            } else if (scheme.equals("file")) {
                cursor = context.getContentResolver().query(
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                        projection, where, null, null);
            }
            if (cursor != null && cursor.moveToFirst()) {
                mSlowMotionInfo = cursor.getString(0);
                mDuration = cursor.getInt(1);
            }
        } catch (final SQLiteException ex) {
            ex.printStackTrace();
        } catch (IllegalArgumentException ex) {
            // if this exception happen, return false.
            ex.printStackTrace();
            Log.v(TAG, "ContentResolver query IllegalArgumentException");
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private int getSlowMotionSpeedFromString(String str) {
        if (str == null || str.charAt(0) != '(') {
            Log.e(TAG, "Invalid string=" + str);
            return 0;
        }
        int pos = str.indexOf('x');
        if (pos != -1) {
            String speed = str.substring(pos + 1);
            try {
                return Integer.parseInt(speed);
            } catch (NumberFormatException ex) {
                ex.printStackTrace();
            }
        }
        return 0;
    }

    private int[] getSlowMotionSectionFromString(String str) {
        if (str == null || str.charAt(0) != '(') {
            Log.e(TAG, "Invalid string=" + str);
            return null;
        }
        int[] range = new int[2];
        int endIndex, fromIndex = 1;
        endIndex = str.indexOf(')', fromIndex);
        splitInt(str.substring(fromIndex, endIndex), range);
        return range;
    }

    private static void splitInt(String str, int[] output) {
        if (str == null)
            return;
        TextUtils.StringSplitter splitter = new TextUtils.SimpleStringSplitter(',');
        splitter.setString(str);
        int index = 0;
        for (String s : splitter) {
            output[index++] = Integer.parseInt(s);
        }
    }

    private void saveSlowMotionInfoToDB(final Context context, final Uri uri, final int startTime,
            final int endTime, final int speed) {
        Log.v(TAG, "saveSlowMotionInfoToDB uri " + uri);
        Log.v(TAG, "startTime " + startTime + " endTime " + endTime + " speed " + speed);
        ContentValues values = new ContentValues(1);
        Cursor cursor = null;
        try {
            values.put(Video.Media.SLOW_MOTION_SPEED, "(" + startTime + "," + endTime + ")" + "x"
                    + speed);
            if (uri.toString().toLowerCase(Locale.ENGLISH).contains("file:///")) {

                String data = Uri.decode(uri.toString());
                data = data.replaceAll("'", "''");
                String id = null;
                final String where = "_data LIKE '%" + data.replaceFirst("file:///", "") + "'";

                cursor = context.getContentResolver().query(
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                        new String[] {
                            MediaStore.Video.Media._ID
                        }, where, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    id = cursor.getString(0);
                }
                Log.v(TAG, "refreshSlowMotionSpeed id " + id);
                Uri tmp = Uri
                        .withAppendedPath(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, "" + id);
                context.getContentResolver().update(tmp, values, null, null);
            } else {
                context.getContentResolver().update(uri, values, null, null);
            }
        } catch (final SQLiteException ex) {
            ex.printStackTrace();
        } catch (IllegalArgumentException ex) {
            // if this exception happen, return false.
            Log.v(TAG, "ContentResolver query IllegalArgumentException");
            ex.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    // get currentSpeed index in a disorderly array,
    // if array is a sorted one, should use Arrays.binarySearch() instead.
    public int getCurrentSpeedIndex(final int[] speedRange,
            int currentSpeed) {
        for (int index = 0; index < speedRange.length; index++) {
            if (currentSpeed == speedRange[index]) {
                return index;
            }
        }
        return -1;
    }

    /**
     * Get supported speed range by fps
     *
     * @param supportedFps
     * @return speed range
     */
    public int[] getSupportedSpeedRange(int supportedFps) {
        int[] range;
        if (120 == supportedFps) {
            if (MtkVideoFeature.isClearMotionSupport()) {
                range = SPEED_RANGE_120_WITH_CLEARMOTION;
            } else {
                range = SPEED_RANGE_120_WITHOUT_CLEARMOTION;
            }
        } else if (240 == supportedFps) {
            if (MtkVideoFeature.isClearMotionSupport()) {
                range = SPEED_RANGE_240_WITH_CLEARMOTION;
            } else {
                range = SPEED_RANGE_240_WITHOUT_CLEARMOTION;
            }
        } else {
            range = SPEED_RANGE;
        }
        StringBuilder rangeInfo = new StringBuilder();
        rangeInfo.append("[");
        for (int i = 0; i < range.length; i++) {
            rangeInfo.append(range[i]);
            if (i < range.length - 1) {
                rangeInfo.append(", ");
            }
        }
        rangeInfo.append("]");
        Log.v(TAG, "supported speed range is " + rangeInfo.toString());
        return range;
    }

    /**
     * Get icon resource by speed
     *
     * @param speedIndex
     * @return icon resource id
     */
    public int getSpeedIconResource(int speedIndex) {
        return mSpeedIconMapping.get(speedIndex);
    }

    @Override
    public String toString() {
        StringBuffer info = new StringBuffer();
        info.append("SlowMotion Information[ uri: ");
        info.append(mUri);
        info.append(", info: ");
        info.append(mSlowMotionInfo);
        info.append(", start time: ");
        info.append(mStartTime);
        info.append(", end time: ");
        info.append(mEndTime);
        info.append(", speed: ");
        info.append(mSpeed);
        info.append(", duration: ");
        info.append(mDuration);
        info.append("]");
        return info.toString();
    }
}
