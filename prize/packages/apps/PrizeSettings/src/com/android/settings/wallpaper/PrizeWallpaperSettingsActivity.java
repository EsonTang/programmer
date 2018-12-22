package com.android.settings.wallpaper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.app.WallpaperManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.settings.R;
/*prize-add-v8.0_wallpaper-yangming-2017_7_20-start*/
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ScrollView;
/*prize-add-v8.0_wallpaper-yangming-2017_7_20-end*/
public class PrizeWallpaperSettingsActivity extends Activity{
	private static final String TAG = "PrizeWallpaperSettingsActivity";
	private GridViewAdapter mGridViewAdapter;
	private WallpaperManager mWallpaperManager;
	private DisplayMetrics mDisplayMetrics;
	private ActionBar mActionBar;
	/*prize-add-v8.0_wallpaper-yangming-2017_7_20-start*/
	private TextView mTextView;
	private TextView mText;
	private ScrollView mScrollView;
	/*prize-add-v8.0_wallpaper-yangming-2017_7_20-end*/
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	/*prize-add-v8.0_wallpaper-yangming-2017_7_20-start*/
		//setContentView(R.layout.prize_wallpaper_settings);
		setContentView(R.layout.prize_wallpaper_settings_scrollview);
	/*prize-add-v8.0_wallpaper-yangming-2017_7_20-end*/
		mWallpaperManager = WallpaperManager.getInstance(this);
		mDisplayMetrics = getDisplayMetrics();
		mActionBar = getActionBar();
        if (mActionBar != null) {
            mActionBar.setDisplayHomeAsUpEnabled(true);
            mActionBar.setHomeButtonEnabled(true);
        }
		if(new File("/system/local-wallpapers").exists())
			initView();
		/*prize-add-v8.0_wallpaper-yangming-2017_7_20-start*/
		setToLauncher();
		/*prize-add-v8.0_wallpaper-yangming-2017_7_20-end*/
	} 
	
	private void initView() {
		// TODO Auto-generated method stub
		GridView grid = (GridView) findViewById(R.id.wallpaper_gridview);
		grid.setFocusable(false);
		mGridViewAdapter = new GridViewAdapter(this);
		grid.setAdapter(mGridViewAdapter);
		/*prize-add-v8.0_wallpaper-yangming-2017_7_20-start*/
		mScrollView = (ScrollView) findViewById(R.id.scrollView);
		mText = (TextView) findViewById(R.id.wallpapaerstore);
		mText.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				/*prize-bugid:43569-change-yangming-2017_11_29-start*/
				/*intent.setClassName("com.prize.prizethemecenter", "com.prize.prizethemecenter.activity.MainActivity");
				startActivity(intent);*/
				ComponentName p = new ComponentName("com.prize.prizethemecenter","com.prize.prizethemecenter.activity.MainActivity");
			    Intent i = new Intent();
			    i.setComponent(p);
			    i.putExtra("page", 1);
			    i.putExtra("from_more", true);
			    try {
			      startActivity(i);
			    } catch (Exception e) {
			      e.printStackTrace();
			    }
				/*prize-bugid:43569-change-yangming-2017_11_29-end*/
			}
		});
		/*prize-add-v8.0_wallpaper-yangming-2017_7_20-end*/
	}
	
	
	
	@Override
	public boolean onNavigateUp() {
		// TODO Auto-generated method stub
		finish();
		return super.onNavigateUp();
	}

	private DisplayMetrics getDisplayMetrics(){
		WindowManager mWindowManager = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
		Display mDisplay = mWindowManager.getDefaultDisplay();
		DisplayMetrics displayMetrics = new DisplayMetrics();
		mDisplay.getRealMetrics(displayMetrics);
		return displayMetrics;
	}
	
    private Bitmap convertToBitmap(String path) {
        long time1 = System.currentTimeMillis();
		DisplayMetrics mDisplayMetrics = getDisplayMetrics();
        BitmapFactory.Options opts = new BitmapFactory.Options();
        // get the picture size when inJustDecodeBounds is true
        opts.inJustDecodeBounds = true;
        //opts.inPreferredConfig = Bitmap.Config.RGB_565;
        opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
        // BitmapFactory.decodeFile return null
        BitmapFactory.decodeFile(path, opts);
        int width = opts.outWidth;
        int height = opts.outHeight;
        int newWidth = mDisplayMetrics.widthPixels; 
        int newHeight = mDisplayMetrics.heightPixels;
		if(getBaseContext().getResources().getConfiguration().orientation==2 && newWidth > newHeight){
			newHeight = mDisplayMetrics.widthPixels; 
			newWidth = mDisplayMetrics.heightPixels;
		}
        float scaleWidth = 1.f, scaleHeight = 1.f;
        if (width > newWidth || height > newHeight) {
            // scale
            scaleWidth = ((float) width) / newWidth;
            scaleHeight = ((float) height) / newHeight;
            
            if (scaleWidth < 1)
                scaleWidth = 1;
            if (scaleHeight < 1)
                scaleHeight = 1;
        }
        float scale = Math.min(scaleWidth, scaleHeight);
        opts.inJustDecodeBounds = false;
        //opts.inSampleSize = 1;//(int)scale;
        opts.inSampleSize = (int)scale;

        Bitmap bitmap = null;
        try{
            bitmap = BitmapFactory.decodeFile(path, opts);
            width = bitmap.getWidth();
            height = bitmap.getHeight();
            newWidth = mDisplayMetrics.widthPixels;
            newHeight = mDisplayMetrics.heightPixels;			
			/*prize-public-bug:Changed lock screen-liuweiquan-20160309-start*/
			if(getBaseContext().getResources().getConfiguration().orientation==2 && newWidth > newHeight){
				newHeight = mDisplayMetrics.widthPixels; 
				newWidth = mDisplayMetrics.heightPixels;
			}	
			/*prize-public-bug:Changed lock screen-liuweiquan-20160309-end*/
            scale = 0;
            Bitmap bmp = Bitmap.createBitmap(newWidth,newHeight,Config.ARGB_8888);
            Canvas canvas = new Canvas(bmp);
            Rect src = new Rect();
            if (newWidth * height > width * newHeight) {
                scale = newWidth / (float)width;
                src.left = 0;
                src.right = width;
                src.top = (int)((height - newHeight / scale) / 2);
                src.bottom = (int)((height + newHeight / scale) / 2);
            }else{
                scale = newHeight / (float)height;
                src.left = (int)((width - newWidth / scale) / 2);
                src.right = (int)((width + newWidth / scale) / 2);
                src.top = 0;
                src.bottom = height;
            }
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setDither(true);
            paint.setFilterBitmap(true);
            paint.setAntiAlias(true);
            canvas.drawBitmap(bitmap, src, new Rect(0,0,newWidth,newHeight), paint);
            bitmap.recycle();
            bitmap = bmp;
        }catch(Exception e){
        }
        long time2 = System.currentTimeMillis();
        Log.d(TAG,"time time--------->"+(time2-time1));
        return bitmap;
    }
	
	private class GridViewAdapter extends BaseAdapter {
		private int w;
		private int h;
		public class Item{
	        public String text;
	        public String srcPath;
	        public String iconPath;
	    }
		
		private List<Item> mItems = new ArrayList<Item>();
	    private Context mContext;
	    public GridViewAdapter(Context context) {
	    	mContext = context;
	    	w = (mDisplayMetrics.widthPixels -mContext.getResources().getDimensionPixelSize(R.dimen.padding_default_prize)*2
	        		-mContext.getResources().getDimensionPixelSize(R.dimen.gridview_Spacing_prize)*2)/3;
	    	h = 1920*w/1080;
	    	setData();
	    }
	    /*prize-change-queryLocalWallPaper-yangming-2018_1_2-start*/
	    private void setData(){
	    	/*String[] text = getResources().getStringArray(R.array.wallpaper_text);
	    	for(int i=0;i<text.length;i++){
	    		Item object = new Item();
	    		object.text = text[i];
	            object.srcPath = "/system/local-wallpapers/wallpaper"+String.format("%02d",i)+".jpg";
	            object.iconPath = "/system/local-wallpapers/wallpaper"+String.format("%02d",i)+"_icon.jpg";;
	            Log.d(TAG,"setData srcPath = "+object.srcPath+",iconPath="+object.iconPath);
	            mItems.add(object);
	    	}*/
	    	List<LocalWallPaperBean> wallpapersList = queryLocalWallPaper(PrizeWallpaperSettingsActivity.this);
	    	if(wallpapersList != null && wallpapersList.size() > 0){
	    		for(int i = 0; i < wallpapersList.size(); i++){
		    		Item object = new Item();
		    		object.text = wallpapersList.get(i).getName();
		    		object.srcPath = wallpapersList.get(i).getWallpaperPath();
		    		object.iconPath = wallpapersList.get(i).getIconPath();
		    		Log.d(TAG,"setData srcPath = "+object.srcPath+",iconPath="+object.iconPath);
		            mItems.add(object);
		    	}
	    	}else{
	    		String[] text = getResources().getStringArray(R.array.wallpaper_text);
		    	for(int i=0;i<text.length;i++){
		    		Item object = new Item();
		    		object.text = text[i];
		            object.srcPath = "/system/local-wallpapers/wallpaper"+String.format("%02d",i)+".jpg";
		            object.iconPath = "/system/local-wallpapers/wallpaper"+String.format("%02d",i)+"_icon.jpg";
		            Log.d(TAG,"setData srcPath = "+object.srcPath+",iconPath="+object.iconPath);
		            mItems.add(object);
		    	}
	    	}
	    }
	    /*prize-change-queryLocalWallPaper-yangming-2018_1_2-end*/

		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = LayoutInflater.from(mContext).inflate(R.layout.prize_wallper_gridview_item, null);
			}
			PrizeImageView image = (PrizeImageView) convertView.findViewById(R.id.item_img);
	        TextView text = (TextView) convertView.findViewById(R.id.item_text);
	        final Item item = (Item) getItem(position);
	        //Drawable wallPaper = mWallpaperManager.getDrawable();
	        image.setLayoutParams(new RelativeLayout.LayoutParams(w, h));
	        //image.setImageDrawable(wallPaper);
	        //image.setImageBitmap(convertToBitmap(item.srcPath));
	        image.setImageURI(Uri.fromFile(new File(item.iconPath)));
	        image.setImagePath(item.srcPath);
	        Log.d(TAG,"getView "+item.srcPath+","+item.iconPath);
	        text.setText(item.text);
	        /*prize-add-v8.0_wallpaper-yangming-2017_7_20-start*/
	        mTextView =  (TextView) convertView.findViewById(R.id.item_textview);
	        mTextView.setOnClickListener(new OnClickListener() {
				
                @Override
                public void onClick(View v) {
					/*File f = new File(item.srcPath);
					Uri mPickedItem  = PrizeImageView.getImageContentUri(mContext,f);
					try {
						WallpaperManager wpm = WallpaperManager.getInstance(mContext);
			            Intent cropAndSetWallpaperIntent = wpm.getCropAndSetWallpaperIntent(mPickedItem);
			            mContext.startActivity(cropAndSetWallpaperIntent);
			        } catch (ActivityNotFoundException anfe) {
			            Log.e("setting", "<onClick> ActivityNotFoundException", anfe);
			        } catch (IllegalArgumentException iae) {
			            Log.e("setting", "<onClick> IllegalArgumentException", iae);
			        }*/
                    Intent intent = new Intent(mContext, PreviewActivity.class);
                    //intent.putExtra("wallID", bean.getWallpaperId());
                    intent.putExtra("localWallPath", item.srcPath);
                    intent.putExtra("wallType", "1");
                    //intent.putExtra("activityType", 1);
                    intent.putExtra("activityType", 4);
                    mContext.startActivity(intent);
                }
	        });
	        /*prize-add-v8.0_wallpaper-yangming-2017_7_20-end*/
			return convertView;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return mItems.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return mItems.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}
	}
	
	/*prize-add-v8.0_wallpaper-yangming-2017_7_20-start*/
    private void setToLauncher() {
        sendBroadcast(new Intent("com.android.launcher3.action.PRELOAD_WORKSPACE"));
        Log.i("setting", "sendBroadcast launcher3 PRELOAD_WORKSPACE");
    }
   /*prize-add-v8.0_wallpaper-yangming-2017_7_20-start*/
    /*prize-add-queryLocalWallPaper-yangming-2018_1_2-start*/
    private static String LOCAL_WALLPAGE_PATH = "content://com.android.launcher3.provider.theme/"
			+ "t_wallpaper_table";
    public static  List<LocalWallPaperBean> queryLocalWallPaper(Context context) {
		if (context == null) {
			return null;
		}
		ContentResolver resolver = context.getContentResolver();
		Uri uri = null;
		uri = Uri.parse(LOCAL_WALLPAGE_PATH);
		List<LocalWallPaperBean> list = null;
		Cursor cursor = null;
		try {
			if (uri != null) {
				cursor = resolver.query(uri, null, null, null, null);
			}
			list = new ArrayList<LocalWallPaperBean>();
			while (cursor!=null&&cursor.moveToNext()) {
				LocalWallPaperBean bean = new LocalWallPaperBean();
				int wallpaperId =cursor.getColumnIndex("wallpaperId");
				int iconPath =cursor.getColumnIndex("iconPath");
				int name =cursor.getColumnIndex("name");
				int isSelected = cursor.getColumnIndex("isSelected");
				int wallpaperPath = cursor.getColumnIndex("wallpaperPath");
				String names = cursor.getString(name);
				String path = cursor.getString(iconPath);
				String isSelecteds = cursor.getString(isSelected);
				String wallId = cursor.getString(wallpaperId);
				String wallPath = cursor.getString(wallpaperPath);
				bean.setWallpaperId(wallId);
				bean.setIconPath(path);
				bean.setName(names);
				bean.setIsSelected(isSelecteds);
				bean.setWallpaperPath(wallPath);
				list.add(bean);
				//Log.d("settings",wallpaperId+":"+path+":"+name+":");
			}
			return list;

		} catch (Exception e) {
			Log.d("settings", "PrizeWallpaperSettingsActivity  failed");
		} finally {
			if(cursor!= null)
			cursor.close();
		}
		return null;
	}
    /*prize-add-queryLocalWallPaper-yangming-2018_1_2-end*/
}
