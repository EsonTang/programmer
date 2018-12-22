package com.android.settings.wallpaper;

import java.io.File;

import android.app.WallpaperManager;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

public class PrizeImageView extends ImageView implements View.OnClickListener{
	private static final String TAG = "PrizeWallpaperSettingsActivity";
	private Uri uri;
	private String srcPath;
	public PrizeImageView(Context context) {
		super(context);
		Log.d(TAG, "PrizeImageView1");
	}

	public PrizeImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		Log.d(TAG, "PrizeImageView2");
	}

	public PrizeImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		Log.d(TAG, "PrizeImageView3");
		setOnClickListener(this);
	}

	@Override
	public void onClick(View arg0) {
		Log.d(TAG, "onClick"+srcPath);
		if(srcPath!=null){
			setWallpaper(srcPath);
		}
	}
	private void setWallpaper(String path){
		/*prize-change-v8.0_wallpaper-yangming-2017_7_20-start*/
		/*WallpaperManager wpm = WallpaperManager.getInstance(getContext());
		File f = new File(path);
		//File f = new File(android.os.Environment.getExternalStorageDirectory().getAbsolutePath()+"/pictures/Screenshots","Screenshot_20150105-111313.png");
		//Log.d("lwq","setWallpaper "+f.getPath());
		Uri mPickedItem  = getImageContentUri(getContext(),f);
		Log.d("lwq","setWallpaper URI"+MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		
		//Uri mPickedItem  = Uri.fromFile(f);
		Log.d("lwq","setWallpaper uri "+mPickedItem);
		try {
            Intent cropAndSetWallpaperIntent = wpm.getCropAndSetWallpaperIntent(mPickedItem);
            /// M: [DEBUG.ADD] @{
            Log.d(TAG, "<onResume> start WallpaperCropActivity, intent "
                       + cropAndSetWallpaperIntent);
            /// @}
            getContext().startActivity(cropAndSetWallpaperIntent);
            return;
        } catch (ActivityNotFoundException anfe) {
            // ignored; fallthru to existing crop activity
            /// M: [DEBUG.ADD] @{
            Log.e(TAG, "<onResume> ActivityNotFoundException", anfe);
            /// @}
        } catch (IllegalArgumentException iae) {
            // ignored; fallthru to existing crop activity
            /// M: [DEBUG.ADD] @{
            Log.e(TAG, "<onResume> IllegalArgumentException", iae);
            /// @}
        }*/
        Intent intent = new Intent(getContext(), PreviewActivity.class);
        //intent.putExtra("wallID", bean.getWallpaperId());
        intent.putExtra("localWallPath", path);
        intent.putExtra("wallType", "1");
        intent.putExtra("activityType", 4);
        getContext().startActivity(intent);
        /*prize-change-v8.0_wallpaper-yangming-2017_7_20-end*/
	}
	
	public static Uri getImageContentUri(Context context, java.io.File imageFile) {
        String filePath = imageFile.getAbsolutePath();
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[] { MediaStore.Images.Media._ID },
                MediaStore.Images.Media.DATA + "=? ",
                new String[] { filePath }, null);
        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor
                    .getColumnIndex(MediaStore.MediaColumns._ID));
            Uri baseUri = Uri.parse("content://media/external/images/media");
            //Uri baseUri = Uri.parse(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            return Uri.withAppendedPath(baseUri, "" + id);
        } else {
            if (imageFile.exists()) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA, filePath);
                return context.getContentResolver().insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            } else {
                return null;
            }
        }
    }
	public void setImagePath(String path){
		srcPath = path;
	}
}
