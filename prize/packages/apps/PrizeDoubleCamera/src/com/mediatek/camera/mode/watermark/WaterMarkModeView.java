package com.mediatek.camera.mode.watermark;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import com.android.camera.CameraActivity;
import com.android.camera.CameraHolder;
import com.android.camera.Util;
import com.android.camera.adapter.PrizeWaterMarkShowApdater;
import com.android.camera.bridge.CameraAppUiImpl;
import com.android.camera.manager.CombinViewManager;
import com.android.camera.manager.ModePicker;
import com.android.camera.manager.ViewManager;
import com.android.camera.ui.PickerButton;
import com.android.camera.ui.RotateImageView;

import android.R.integer;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Resources;
import android.filterfw.core.FinalPort;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Bitmap.Config;
import android.graphics.Paint.FontMetrics;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils.TruncateAt;
import android.util.DisplayMetrics;
import android.util.FloatMath;
import com.android.camera.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.android.camera.R;
import com.mediatek.camera.mode.watermark.WaterMarkDialog.WaterMarkDialogBitmap;
import com.mediatek.camera.platform.ICameraView;
import com.mediatek.camera.platform.IModuleCtrl;
import com.mediatek.camera.platform.ICameraAppUi;
import com.mediatek.camera.platform.ICameraAppUi.ViewState;
import com.mediatek.camera.setting.SettingConstants;
import com.mediatek.camera.setting.SettingDataBase;
import com.prize.setting.LogTools;
import com.prize.setting.PrizeLunar;
import com.prize.ui.CenterHorizontalScroll;
import com.prize.ui.CenterHorizontalScroll.OnItemChangeListener;
/*prize-xuchunming-20180108-bugid:47105-start*/
import com.mediatek.camera.mode.watermark.WaterMarkRotateLayout.OnMeasureListener;
/*prize-xuchunming-20180108-bugid:47105-end*/

public class WaterMarkModeView  extends ViewManager implements OnTouchListener,WaterMarkDialogBitmap,ICameraView, OnMeasureListener {
	
    private static final String TAG = "WatermarkManager";
    private CameraActivity mCameraActivity;
    private LinearLayout layoutView ;
	private ViewPager mViewPager;
	private PrizeWaterMarkShowApdater mApdater;
	private int mCurrentAlbumId = -1;
	private int mCurrentItemPostion = 0;
	private ArrayList<View> mPagerChildViewList;
	private TextView mPageIndicationTv;
	private PrizeWaterMarkDBManger mDbManger;
	private ArrayList<PrizeWaterMarkAlbum> mAlbumList = new ArrayList<PrizeWaterMarkAlbum>();
	private float Xpostion = 0.0f;
	private float Ypostion = 0.0f;
	private int _xDelta;  
	private int _yDelta;  
	private int frist_xMargin;
	private int frist_yMargin;
	private boolean isMove;
	private int mDegrees = 0;
	private GestureDetector mGesture;  
	private ICameraAppUi mICameraAppUi;
    private IModuleCtrl mIModuleCtrl;
    private boolean mIsInCameraPreview = true;
	private boolean mIsShowSetting;
	private boolean mIsInPictureTakenProgress;
	private CenterHorizontalScroll mChsView;
	private final static int MAX_VIEW_SIZE = 5;
	private final static int PAGER_OFFSET = 10000;
	private TextView mCloseTv;
	private static final int MSG_SHOW_PAGER = 0;
	private static final int MSG_SHOW_PROGRESS = 1;
	private static final int MSG_DISMISS_PROGRESS = 2;
	private static final int MSG_REFRESH_VIEW = 3;
	private static final int MSG_STATE_VIEW = 4;
	
	public static final String ADDR_CITY_KEY = "addr_city_key";
	private int mMaxBottom;
	private int mMaxRight;
	private int mScreenHeight;
	private int mTopMargin;
	private Typeface mTypeFace;
	private ArrayList<WbPosition> mPostionList = new ArrayList<WbPosition>(MAX_VIEW_SIZE);
	private int mCurrentIndex;
	/*prize-xuchunming-20171218-bugid:44235-start*/
	private WaterMarkDialog editDialog;
	/*prize-xuchunming-20171218-bugid:44235-end*/
	
	/*prize-xuchunming-20180108-bugid:47105-start*/
	private int mWaterTopMarginToSurfaceView;
	/*prize-xuchunming-20180108-bugid:47105-end*/
	public WaterMarkModeView(CameraActivity mCameraActivity) {
		/*prize-xuchunming-adjust layout at 18:9 project-start*/
		super(mCameraActivity,VIEW_LAYER_TOP);
		/*prize-xuchunming-adjust layout at 18:9 project-end*/
		this.mCameraActivity = mCameraActivity;
		mDbManger = new PrizeWaterMarkDBManger(mCameraActivity.getBaseContext());
		DisplayMetrics displayMetrics = getDisplayMetrics();
		mScreenHeight = displayMetrics.heightPixels;
		mMaxRight = displayMetrics.widthPixels;
		mTopMargin = mCameraActivity.getResources().getDimensionPixelOffset(R.dimen.view_top_height);
		mMaxBottom = mScreenHeight - mCameraActivity.getResources().getDimensionPixelOffset(R.dimen.wb_margin_bottom) - mTopMargin;
	}
	
	private DisplayMetrics getDisplayMetrics(){
		WindowManager wm = (WindowManager) mCameraActivity.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		DisplayMetrics displayMetrics = new DisplayMetrics();
		display.getRealMetrics(displayMetrics);
		return displayMetrics;
	}
	
	/*prize-xuchunming-20180108-bugid:47105-start*/
	@Override
	public void onOrientationChanged(int orientation) {
		mDegrees = orientation;
		super.onOrientationChanged(orientation);
	}
	/*prize-xuchunming-20180108-bugid:47105-end*/

	@Override
	protected View getView() {
		View view = inflate(R.layout.watermarking_layout);
		findViews(view);
		initData();
		chsData();
		initPagerView();
		showWbPager();
		mGesture =  new GestureDetector(mCameraActivity.getBaseContext(), new GatureListener());
		return view;
	}


	@Override
	public boolean onTouch(View v, MotionEvent event) {

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			RelativeLayout.LayoutParams params = (android.widget.RelativeLayout.LayoutParams) v.getLayoutParams();
			_xDelta =(int)event.getRawX() - params.leftMargin;
			_yDelta = (int)event.getRawY() - params.topMargin;
			frist_xMargin = params.leftMargin;
			frist_yMargin = params.topMargin;
			
			if(v.getTag()==null){
				isMove = true;
			}else{
				isMove = false;
			}
			getPositionInfo().setXpostion(params.leftMargin);
			getPositionInfo().setYpostion(params.topMargin);
			break;
		case MotionEvent.ACTION_MOVE:
			RelativeLayout.LayoutParams params1 = (android.widget.RelativeLayout.LayoutParams) v.getLayoutParams();
			int checkLeftMargin = (int)event.getRawX() - _xDelta;
			int checkTopMargin = (int)event.getRawY() - _yDelta;

			if (checkLeftMargin + v.getWidth() > mMaxRight) {
				params1.leftMargin = mMaxRight - v.getWidth();
			} else if (checkLeftMargin < 0) {
				params1.leftMargin = 0;
			} else {
				params1.leftMargin = checkLeftMargin;
			}
			/*prize-xuchunming-20180108-bugid:47105-start*/
            mMaxBottom = mViewPager.getHeight();
			/*prize-xuchunming-20180108-bugid:47105-end*/
			if (checkTopMargin + v.getHeight() > mMaxBottom) {
				params1.topMargin = mMaxBottom - v.getHeight();
			} else if (checkTopMargin < 0) {
				params1.topMargin = 0;
			} else {
				params1.topMargin = checkTopMargin;
			}
			
			int moveMin = (int) (10*(480* mCameraActivity.getResources().getDisplayMetrics().density));
			if((Math.abs((params1.leftMargin-frist_xMargin))>moveMin|| (Math.abs((params1.topMargin-frist_yMargin))>moveMin))){
				isMove = true;
			}
			
			Xpostion = params1.leftMargin;
			Ypostion = params1.topMargin;
			
			getPositionInfo().setXpostion(params1.leftMargin);
			getPositionInfo().setYpostion(params1.topMargin);
			v.setLayoutParams(params1);
			/*prize-xuchunming-20180108-bugid:47105-start*/
			((WaterMarkRotateLayout)v.findViewById(R.id.bg_replace_message)).setOverLimmitTopMargin(-1);
			((WaterMarkRotateLayout)v.findViewById(R.id.bg_replace_message)).setOverLimmitLeftMargin(-1);
			/*prize-xuchunming-20180108-bugid:47105-end*/
			break;
		case MotionEvent.ACTION_UP:
			RelativeLayout.LayoutParams params2 = (android.widget.RelativeLayout.LayoutParams) v.getLayoutParams();
			break;
		default:
			break;
		}
		/*prize-xuchunming-20180516-bugid:57419-start*/
		mViewPager.requestDisallowInterceptTouchEvent(true);
		/*prize-xuchunming-20180516-bugid:57419-end*/
		if(isMove){
			return true;
		}else{
			return false;
		}

	}

	@Override
	public void show() {
		super.show();
	}
	
	@Override
	public void hide() {
		/*prize-xuchunming-20171218-bugid:44235-start*/
		if(editDialog != null) {
			editDialog.dismiss();
		}
		/*prize-xuchunming-20171218-bugid:44235-end*/
		super.hide();
	}
	
	
	@Override
	public boolean collapse(boolean force) {
		return false;
	}
	
	/**
	 * init Watermark db data
	 * */
	private void initData(){
		
		mAlbumList = new ArrayList<PrizeWaterMarkAlbum>();
		
		PrizeWaterMarkAlbum historyAlbum = new PrizeWaterMarkAlbum();
		historyAlbum.setAlbumid(0);
		historyAlbum.setAlbumName("最近");
		
		mCurrentAlbumId = 0;
		mCurrentItemPostion = 0;
		
		mAlbumList.add(historyAlbum);
		mAlbumList.addAll(mDbManger.getSQLQueryAlbum());
		
		ArrayList<PrizeWaterMarkThumbInfo> wbInfoList = mDbManger.getSQLQueryHistoryData();
		ArrayList<PrizeWaterMarkThumbInfo> historyList = new ArrayList<PrizeWaterMarkThumbInfo>(wbInfoList.size());
		for (PrizeWaterMarkThumbInfo wbInfo : wbInfoList) {
			int itemposition = 1;
			int albumId = wbInfo.getAlbumId();
			int index = wbInfo.getThumbID();
			LogTools.i(TAG, "initData albumId=" + albumId);
			ArrayList<PrizeWaterMarkThumbInfo> wbMarkThumbInfoList = getWbThumbInfoList(albumId);
			int first = wbMarkThumbInfoList.get(0).getThumbID();
			LogTools.i(TAG, "initData albumId=" + albumId + " index=" + index + " first=" + first+",wbMarkThumbInfoList.size(): "+wbMarkThumbInfoList.size());
			if (index > 40) {
				itemposition = adjustIndex(albumId, index);
			} else {
				itemposition = index - first;
			}
			PrizeWaterMarkThumbInfo waterMarkThumbInfo = wbMarkThumbInfoList.get(itemposition);
			historyList.add(waterMarkThumbInfo);
		}
		historyAlbum.setWaterMarkThumbList(historyList);
	}
	
	private void chsData() {
		List<String> albumNameList = new ArrayList<String>();
		String[] wbType = mCameraActivity.getResources().getStringArray(R.array.watermark_type);
		LogTools.i(TAG, "chsData size=" + mAlbumList.size());
		for (int i = 0, size = wbType.length; i < size; i++) {
			String name = wbType[i];
			LogTools.i(TAG, "chsData name=" + name);
			albumNameList.add(name);
		}
		mChsView.initAdapter(albumNameList, mCurrentAlbumId);
		mChsView.setOnItemChangeListener(new OnItemChangeListener() {
			
			@Override
			public void itemChange(int postion) {
				if (mCurrentAlbumId != postion) {
					mCurrentAlbumId = postion;
					mCurrentItemPostion = 0;
					mHandler.sendEmptyMessageDelayed(MSG_SHOW_PAGER, 200);
				}
			}
		});
	}
	
	private void findViews(View view) {
		mPageIndicationTv = (TextView) view.findViewById(R.id.mselect_paper);
		mViewPager = (ViewPager) view.findViewById(R.id.view_show_pager);	
		mChsView = (CenterHorizontalScroll) view.findViewById(R.id.chs_wb_type);
		mViewPager.setOnTouchListener(mOnTouchListener);
		mCloseTv = (TextView) view.findViewById(R.id.tv_close);
        mCloseTv.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mICameraAppUi.closeCombinView(CombinViewManager.COMBIN_WATERMARK);
			}
		});
	}
	
	private Handler mHandler = new Handler(){
		
		public void dispatchMessage(Message msg) {
			switch (msg.what) {
			case MSG_SHOW_PAGER:
				showWbPager();
				break;
				
			case MSG_SHOW_PROGRESS:
				mCameraActivity.getCameraAppUI().showProgress(
						mCameraActivity.getString(R.string.saving));
				mCameraActivity.getCameraAppUI().dismissInfo();
				break;
			case MSG_DISMISS_PROGRESS:
				mCameraActivity.getCameraAppUI().dismissProgress();
				break;
			case MSG_REFRESH_VIEW:
				refreshByHistory();
				break;
			case MSG_STATE_VIEW:
				boolean inProgress = msg.arg1 == 1;
				Log.i(TAG, "inProgress = " + inProgress);
	            if (inProgress) {
	            	mCloseTv.setEnabled(false);
	            } else {
	            	mCloseTv.setEnabled(true);
	            }
				break;
			default:
				break;
			}
		};
	};
	
	public void showProgress() {
		mHandler.sendEmptyMessage(MSG_SHOW_PROGRESS);
	}

	public void dismissProgress() {
		mHandler.sendEmptyMessage(MSG_DISMISS_PROGRESS);
	}
	
	private void pagerChildViews() {
		if(mPagerChildViewList == null){
			 mPagerChildViewList = new ArrayList<View>();
			 for(int i=0;i<MAX_VIEW_SIZE;i++){
				 View mViews = LayoutInflater.from(mCameraActivity.getBaseContext()).inflate(R.layout.show_watermark_view, null);
				 /*prize-xuchunming-20180108-bugid:47105-start*/
				 ((WaterMarkRotateLayout)mViews.findViewById(R.id.bg_replace_message)).setOnMeasureListener(this);
				 /*prize-xuchunming-20180108-bugid:47105-end*/
				 mPagerChildViewList.add(mViews);
				 WbPosition wbPosition = new WbPosition();
				 mPostionList.add(wbPosition);
			 }
		 }
	}
	
	private ArrayList<PrizeWaterMarkImageResourceInfo> getImageResourceList(PrizeWaterMarkThumbInfo wbThumbInfo) {
		ArrayList<PrizeWaterMarkImageResourceInfo> wbPhotoResInfoList = wbThumbInfo.getImageResourceList();
		 if (wbPhotoResInfoList == null) {
			 wbPhotoResInfoList = mDbManger.getSQLQueryImageResourceData(wbThumbInfo.getThumbID());
			 wbThumbInfo.setImageResourceList(wbPhotoResInfoList);
		 }
		 return wbPhotoResInfoList;
	}
	
	private ArrayList<PrizeWaterMarkTextResourceInfo> getTextResourceList(PrizeWaterMarkThumbInfo wbThumbInfo) {
		ArrayList<PrizeWaterMarkTextResourceInfo> wbTextResInfoList = wbThumbInfo.getTextResourceList();
		 if (wbTextResInfoList == null) {
			 wbTextResInfoList = mDbManger.getSQLQueryTextResourceData(wbThumbInfo.getThumbID());
			 wbThumbInfo.setTextResourceList(wbTextResInfoList);
		 }
		 return wbTextResInfoList;
	}
	
	private void bindViewData(PrizeWaterMarkThumbInfo wbThumbInfo, View pagerChildView) {
		ArrayList<PrizeWaterMarkImageResourceInfo> wbPhotoResInfoList = getImageResourceList(wbThumbInfo);
		 RelativeLayout mvRlLayout = (RelativeLayout) pagerChildView.findViewById(R.id.move_watermarkinglayout);
		 mvRlLayout.setOnClickListener(null);
		 RelativeLayout mChildRelativeLayout = (RelativeLayout) pagerChildView.findViewById(R.id.childwatermarkinglayout);
		 mChildRelativeLayout.removeAllViews();
		 RelativeLayout.LayoutParams  params ;
		 
		 /*prize-xuchunming-20180108-bugid:47105-start*/
		 WaterMarkRotateLayout waterMarkRotateLayout = (WaterMarkRotateLayout) pagerChildView.findViewById(R.id.bg_replace_message);
		 waterMarkRotateLayout.setPrizeWaterMarkThumbInfo(wbThumbInfo);
		 int waterWidth = 0;
		 int waterHeight = 0;
		 /*prize-xuchunming-20180108-bugid:47105-end*/
			
		 if(wbThumbInfo.getIsEdit()){
			 
			 ArrayList<PrizeWaterMarkTextResourceInfo> wbTextResInfoList = getTextResourceList(wbThumbInfo);
			 
			 float wbWidth = wbThumbInfo.getMWidth();
			 float wbHeight = wbThumbInfo.getMHeight();
			 if (wbWidth > 1 && wbHeight > 1 && wbWidth >= wbHeight) {
				 params =  new RelativeLayout.LayoutParams((int)wbWidth,(int)wbHeight);
			 } else {
				 params =  new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			 }
			 
			 params.leftMargin = (int) wbThumbInfo.getXpostion();
			 params.topMargin = (int) wbThumbInfo.getYpostion();
			 Context context = mCameraActivity.getBaseContext();
			 for(PrizeWaterMarkImageResourceInfo wbPhotoResInfo : wbPhotoResInfoList){
				
				 ImageView mRotateImageView = new ImageView(context);
				 RelativeLayout.LayoutParams mLayoutParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
				 if(wbPhotoResInfo.getImageXPostion() == 0.0f){
					 mLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL); 
				 }else{
					 mLayoutParams.leftMargin = (int) wbPhotoResInfo.getImageXPostion();
				 }

				 mLayoutParams.topMargin = (int) wbPhotoResInfo.getImageYPostion();
					String string = null;
					if(wbPhotoResInfo.getImageType() == 1){
						 Calendar c = Calendar.getInstance();  
						 c.setTimeZone(TimeZone.getTimeZone("GMT+8:00")); 
						 int w =c.get(Calendar.DAY_OF_WEEK) - 1;      
						 if (w < 0){        
							 w = 0;      
						 } 
						 if(w==0){
							 string ="prize_watermark_mode"+7+"_time1";
						 }else{
							 string ="prize_watermark_mode"+w+"_time1";
						 }
					}else{
						string = wbPhotoResInfo.getImageDataPath();
					}
				 /*prize-xuchunming-20180108-bugid:47105-start*/
				 int resID = mCameraActivity.getResources().getIdentifier(string, "drawable", mCameraActivity.getBaseContext().getPackageName());	
				 Bitmap bitmapss = BitmapFactory.decodeResource(mCameraActivity.getResources(), resID);
				 waterWidth = bitmapss.getWidth();
				 waterHeight = bitmapss.getHeight();
				 /*prize-xuchunming-20180108-bugid:47105-end*/
				 mRotateImageView.setImageResource(mCameraActivity.getResources().getIdentifier(string, "drawable", mCameraActivity.getBaseContext().getPackageName()));
				 mChildRelativeLayout.addView(mRotateImageView, mLayoutParams);
			 }
			 
			 for(PrizeWaterMarkTextResourceInfo wbTextResInfo:wbTextResInfoList){
				 Paint mPaint =  new Paint();
				 TextView mTextView = new TextView(context);
				 mTextView.setGravity(Gravity.CENTER);
				 mPaint.setTextSize(wbTextResInfo.getTextSize()*context.getResources().getDisplayMetrics().density);
				 Rect rect = new Rect();
				 /*prize-xuchunming-20171211-bugid:46832-start*/
				 if(wbTextResInfo.getShowString() != null && wbTextResInfo.getShowString().length() > 1) {
					mPaint.getTextBounds(wbTextResInfo.getShowString(), 0, 1, rect);
				 }
				 /*prize-xuchunming-20171211-bugid:46832-end*/
				 RelativeLayout.LayoutParams mLayoutParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);

				 if(wbTextResInfo.getTextWtype() == 1){
					 SimpleDateFormat formatter = new SimpleDateFormat(wbTextResInfo.getTimeTextType());   
					 Date curDate = new Date(System.currentTimeMillis());
					 wbTextResInfo.setShowString(formatter.format(curDate));
				 } else  if(wbTextResInfo.getTextWtype() == 6){
					 Calendar mCalendar = Calendar.getInstance();
					 mCalendar.setTimeInMillis(System.currentTimeMillis());
					 wbTextResInfo.setShowString(PrizeLunar.getLunar(mCalendar.get(Calendar.YEAR), mCalendar.get(Calendar.MONTH)+1, mCalendar.get(Calendar.DAY_OF_MONTH)));
				 } else if(wbTextResInfo.getTextWtype() == 2){
					 if(mCameraActivity.getLocationManager().getCurrentLocation()!=null){
						 wbTextResInfo.setShowString(mCameraActivity.getLocationManager().getCurrentLocation().getExtras().getString(ADDR_CITY_KEY));
					 }else{
						 wbTextResInfo.setShowString(wbTextResInfo.getInitString());
					 }
				 }
				
				 if(wbTextResInfo.getTextViewAlign() == 0){
					 if(wbTextResInfo.getTextXPostion()==0){
						 mLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL); 
					 }else{
						 mLayoutParams.leftMargin = (int) (wbTextResInfo.getTextXPostion() - mPaint.measureText(wbTextResInfo.getShowString())/2);
					 }
				 }else if(wbTextResInfo.getTextViewAlign() == 1){
					 mLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
				 }
				 if (mTypeFace == null) {
					 mTypeFace =Typeface.createFromAsset(mCameraActivity.getAssets(),"text_youyuan.ttf");
				 }
				 mTextView.setGravity(Gravity.CENTER_VERTICAL);
				 mTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, wbTextResInfo.getTextSize());
				 mTextView.setTypeface(mTypeFace);
				 mTextView.setTextColor(Color.parseColor(wbTextResInfo.getTextColor()));
				 mTextView.setText(wbTextResInfo.getShowString());
				 mLayoutParams.topMargin =  wbTextResInfo.getTextYPostion();
				
				 mChildRelativeLayout.addView(mTextView, mLayoutParams);
				 wbTextResInfo.setTextView(mTextView);
				 if(wbTextResInfo.getTextWtype()==0){
					 mvRlLayout.setTag(mTextView);
					 mTextView.setTag(wbTextResInfo);
					 mvRlLayout.setOnClickListener(new OnClickListener() {
						
						@Override
						public void onClick(View arg0) {
							// TODO Auto-generated method stub
							if(arg0.getTag()!=null){
								/*prize-xuchunming-20171218-bugid:44235-start*/
								editDialog = new WaterMarkDialog(mCameraActivity, (TextView)arg0.getTag(),WaterMarkModeView.this);
								/*prize-xuchunming-20171218-bugid:44235-end*/
								editDialog.show();
							}
						}
					});
				 }
			 }
		 }else{
			 params =  new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			 params.leftMargin = (int) wbThumbInfo.getXpostion();
			 params.topMargin = (int) wbThumbInfo.getYpostion();
			 ImageView mRotateImageView = new ImageView(mCameraActivity.getBaseContext());
			 RelativeLayout.LayoutParams mLayoutParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			 /*prize-xuchunming-20180108-bugid:47105-start*/
			 int resID = mCameraActivity.getResources().getIdentifier(wbPhotoResInfoList.get(0).getImageDataPath(), "drawable", mCameraActivity.getBaseContext().getPackageName());	
			 Bitmap bitmapss = BitmapFactory.decodeResource(mCameraActivity.getResources(), resID);
			 waterWidth = bitmapss.getWidth();
			 waterHeight = bitmapss.getHeight();
			 /*prize-xuchunming-20180108-bugid:47105-end*/
			 mRotateImageView.setImageResource(mCameraActivity.getResources().getIdentifier(wbPhotoResInfoList.get(0).getImageDataPath(), "drawable", mCameraActivity.getBaseContext().getPackageName()));
			 mChildRelativeLayout.addView(mRotateImageView, mLayoutParams);
		 }
		 /*prize-xuchunming-20180108-bugid:47105-start*/
		 if((mDegrees == 0 || mDegrees == 180) && waterHeight > waterWidth && mViewPager.getHeight() > 0 && params.topMargin+waterHeight>mViewPager.getHeight()){
			 waterMarkRotateLayout.setOverLimmitTopMargin(params.topMargin);
			 params.topMargin = mViewPager.getHeight() - waterHeight;
		 }else if((mDegrees == 270 || mDegrees == 90) &&  mViewPager.getHeight() > 0 && waterWidth > waterHeight && params.topMargin+waterWidth>mViewPager.getHeight()){
			 waterMarkRotateLayout.setOverLimmitTopMargin(params.topMargin);
			 params.topMargin = mViewPager.getHeight() - waterWidth;
		 }
		 
		 if((mDegrees == 0 || mDegrees == 180) && waterWidth > waterHeight && mViewPager.getWidth() > 0 && params.leftMargin+waterWidth>mViewPager.getWidth()){
			 waterMarkRotateLayout.setOverLimmitLeftMargin(params.leftMargin);
			 params.leftMargin = mViewPager.getWidth() - waterWidth;
		 }else if((mDegrees == 270 || mDegrees == 90) && waterHeight > waterWidth && mViewPager.getWidth() > 0 && params.leftMargin+waterHeight>mViewPager.getWidth()){
			 waterMarkRotateLayout.setOverLimmitLeftMargin(params.leftMargin);
			 params.leftMargin = mViewPager.getWidth() - waterHeight;
		 }
		 /*prize-xuchunming-20180108-bugid:47105-end*/
		 mvRlLayout.setLayoutParams(params);
		 mvRlLayout.setOnTouchListener(this);
		 Xpostion = mvRlLayout.getX();
		 Ypostion = mvRlLayout.getY();
	}
	
	private ArrayList<PrizeWaterMarkThumbInfo> getWbThumbInfoList(int albumId) {
		ArrayList<PrizeWaterMarkThumbInfo> wbInfoList = mAlbumList.get(albumId).getWaterMarkThumbList();
		 if (wbInfoList == null) {
			 wbInfoList = mDbManger.getSQLQueryData(mAlbumList.get(albumId).getAlbumId());
			 mAlbumList.get(albumId).setWaterMarkThumbList(wbInfoList);
		 }
		 return wbInfoList;
	}
	
	private int getPreAlbumId(int albumId, int size) {
		int preAlbumId = (albumId - 1 + size) % size;
		return preAlbumId;
	}
	
	private int getNextAlbumId(int albumId, int size) {
		int nextAlbumId = (albumId + 1) % size;
		return nextAlbumId;
	}
	
	private void initPagerView() {
		pagerChildViews();
		Context context = mCameraActivity.getBaseContext();
		mApdater = new PrizeWaterMarkShowApdater(context, mPagerChildViewList, MAX_VIEW_SIZE);
		mViewPager.setAdapter(mApdater);
		mLastItem = mCurrentItemPostion + MAX_VIEW_SIZE / 2 + PAGER_OFFSET;
		mViewPager.setCurrentItem(mLastItem);
		mViewPager.setOnPageChangeListener(mShowOnPageChangeListener);
	}
	
	public void showWbPager(){
		 
		 Context context = mCameraActivity.getBaseContext();
		 int albumSize = mAlbumList.size();
		 try {
			 int addView = 0;
			 int albumId = mCurrentAlbumId;
			 View pagerChildView;
			 int viewIndex = mLastItem;
			 mCurrentIndex = 2;
			 while (addView < MAX_VIEW_SIZE / 2) {
				 LogTools.i(TAG, "showWbPager addView=" + addView + " albumId=" + albumId);
				 albumId = getPreAlbumId(albumId, albumSize);
				 ArrayList<PrizeWaterMarkThumbInfo> preWbInfoList = getWbThumbInfoList(albumId);
				 int preSize = preWbInfoList.size();
				 for (int i = preSize - 1; i >= 0 && addView < MAX_VIEW_SIZE / 2; i--, addView++) {
					 PrizeWaterMarkThumbInfo preWbThumbInfo = preWbInfoList.get(i);
					 WbPosition wbPosition = mPostionList.get(1 - addView);
					 wbPosition.mAlbumId = albumId;
					 wbPosition.mIndex = i;
					 LogTools.i(TAG, "showWbPager i=" + i + " addView=" + addView + " wbPosition=" + wbPosition);
					 pagerChildView = mPagerChildViewList.get((mLastItem - 1 - addView + MAX_VIEW_SIZE) % MAX_VIEW_SIZE);
					 bindViewData(preWbThumbInfo, pagerChildView);
				 }
			 }
			 
			 albumId = mCurrentAlbumId;
			 LogTools.i(TAG, "showWbPager next addView=" + addView + " albumId=" + albumId);
			 while (addView < MAX_VIEW_SIZE) {
				 ArrayList<PrizeWaterMarkThumbInfo> nextWbInfoList = getWbThumbInfoList(albumId);
				 int nextSize = nextWbInfoList.size();
				 for (int i = 0; i < nextSize && addView < MAX_VIEW_SIZE; i++, addView++) {
					 PrizeWaterMarkThumbInfo nextWbThumbInfo = nextWbInfoList.get(i);
					 WbPosition wbPosition = mPostionList.get(addView);
					 wbPosition.mAlbumId = albumId;
					 wbPosition.mIndex = i;
					 LogTools.i(TAG, "showWbPager i=" + i + " addView=" + addView + " wbPosition=" + wbPosition);
					 pagerChildView = mPagerChildViewList.get((mLastItem - 2 + addView + MAX_VIEW_SIZE) % MAX_VIEW_SIZE);
					 bindViewData(nextWbThumbInfo, pagerChildView);
				 }
				 albumId = getNextAlbumId(albumId, albumSize);
			 }
		} catch (Exception e) {
			e.printStackTrace();
		}
		 setPageIndication(mCurrentItemPostion);
	}
	
	private boolean isScorall = false;
	private boolean isDoutap =  false;
	private int mCurrentPagePostion = -1;
	private boolean  isFrist = false;  
	private float oldDist;  
	
	private OnTouchListener mOnTouchListener = new OnTouchListener() {
		
		@Override
		public boolean onTouch(View arg0, MotionEvent event) {
			// TODO Auto-generated method stub
			//mGesture.onTouchEvent(event);
			/*prize-modify-inconsistent display of click position and focus area-xiaoping-20180626-start*/
			if (is16Radio9()) {
				int marginTop = getContext().getPreviewSurfaceView() != null ?  getContext().getPreviewSurfaceView().getTop() : 0;
				event.setLocation(event.getX(), (float)(event.getRawY() -marginTop));
			}
			/*prize-modify-inconsistent display of click position and focus area-xiaoping-20180626-end*/
			/*prize-xuchuming-20180427-bugid:55041-start*/
			mCameraActivity.getGestureRecognizer().onTouchEvent(event);
			/*prize-xuchuming-20180427-bugid:55041-end*/
			/*switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					isScorall = false;
					isDoutap = false;
					isFrist =  false;
					break;
				case MotionEvent.ACTION_UP:
					isScorall = false;
					isDoutap = false;
					isFrist =  false;
					break;
			  
			      case MotionEvent.ACTION_MOVE:
			    	  if (event.getPointerCount()>= 2) {
			    		  if(!isFrist){
			    			  isFrist =  true;
			    			  oldDist = spacing(event);  
			    		  }else{
				    		  float newDist = spacing(event);  
				    		  if (newDist > oldDist + 1) { 
				    			  prize-xuchuming-20180427-bugid:55041-start
				    			  //zoom(newDist / oldDist);  
				    			  prize-xuchuming-20180427-bugid:55041-end
				    		  }  
				    		  else if (newDist < oldDist - 1) { 
				    			  prize-xuchuming-20180427-bugid:55041-start
				    			  //zoom(newDist  / oldDist);  
				    			  prize-xuchuming-20180427-bugid:55041-end
				    		  } 
			    		  }
			    	  }  
			    	  break;  
				default:
					break;
			}
			if(event.getPointerCount()>1&&!isScorall){
				isDoutap = true;
			}
			
			if(isDoutap){
				return true;
			}
			return false;
		*/return false;
			}
	};
	
	 private void zoom(float f) {  
		 if(isDoutap){
			 mICameraAppUi.zoom(f);
		 }
	 }
	
    private float spacing(MotionEvent event) {  
        float x = event.getX(0) - event.getX(1);  
        float y = event.getY(0) - event.getY(1);  
        return FloatMath.sqrt(x * x + y * y);  
    }  
    
    private int mLastItem;
	
	private OnPageChangeListener mShowOnPageChangeListener = new OnPageChangeListener(){

		@Override
		public void onPageScrollStateChanged(int arg0) {
			// TODO Auto-generated method stub
			isScorall =  true;
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onPageSelected(int arg0) {
			int count = getWbThumbInfoList(mCurrentAlbumId).size();
			int albumId = mCurrentAlbumId;
			int size = mAlbumList.size();
			int diff = 0;
			int viewIndex = arg0 % MAX_VIEW_SIZE;
	        if (viewIndex < 0){
	        	viewIndex = MAX_VIEW_SIZE + viewIndex;
	        }
	        View view;
	        LogTools.i(TAG, "onPageSelected mLastItem=" + mLastItem + " arg0=" + arg0 + " mCurrentAlbumId=" + mCurrentAlbumId + " mCurrentItemPostion=" + mCurrentItemPostion);
			if (mLastItem > arg0) { // pre
				if (mCurrentItemPostion == 0) { // preAlbum
					albumId = getPreAlbumId(mCurrentAlbumId, size);
					// TODO zero
					mCurrentItemPostion = getWbThumbInfoList(albumId).size() - 1;
				} else {
					mCurrentItemPostion = mCurrentItemPostion - 1;
				}
				view = mPagerChildViewList.get((viewIndex + 3) % MAX_VIEW_SIZE);
				mCurrentIndex = (mCurrentIndex - 1 + MAX_VIEW_SIZE) % MAX_VIEW_SIZE;
				int preIndex = (mCurrentIndex - 1 + MAX_VIEW_SIZE) % MAX_VIEW_SIZE;
				WbPosition wbPosition = mPostionList.get(preIndex);
				int preTwoIndex = (preIndex - 1 + MAX_VIEW_SIZE) % MAX_VIEW_SIZE;
				WbPosition preWbPosition = mPostionList.get(preTwoIndex);
				bindViewData(getPreWbThumbInfo(wbPosition.mAlbumId, wbPosition.mIndex, preWbPosition), view);
				LogTools.i(TAG, "onPageSelected wbPosition=" + preWbPosition);
				diff = -1;
			} else if (mLastItem < arg0) { // next
				if (mCurrentItemPostion == count - 1) { // nextAlbum
					albumId = getNextAlbumId(mCurrentAlbumId, size);
					mCurrentItemPostion = 0;
				} else {
					mCurrentItemPostion = mCurrentItemPostion + 1;
				}
				view = mPagerChildViewList.get((viewIndex - 3 + MAX_VIEW_SIZE) % MAX_VIEW_SIZE);
				mCurrentIndex = (mCurrentIndex + 1) % MAX_VIEW_SIZE;
				int nextIndex = (mCurrentIndex + 1) % MAX_VIEW_SIZE;
				WbPosition wbPosition = mPostionList.get(nextIndex);
				int nextTwoIndex = (nextIndex + 1) % MAX_VIEW_SIZE;
				WbPosition nextWbPosition = mPostionList.get(nextTwoIndex);
				bindViewData(getNextWbThumbInfo(wbPosition.mAlbumId, wbPosition.mIndex, nextWbPosition), view);
				LogTools.i(TAG, "onPageSelected wbPosition=" + nextWbPosition);
				diff = 1;
			}
			if (albumId != mCurrentAlbumId) {
				mCurrentAlbumId = albumId;
				mChsView.scrollSelect(diff);
			}
			mLastItem = arg0;
			setPageIndication(mCurrentItemPostion);
			LogTools.i(TAG, "onPageSelected after mLastItem=" + mLastItem + " mCurrentAlbumId=" + mCurrentAlbumId + " mCurrentItemPostion=" + mCurrentItemPostion);
		}
	};
	
	private PrizeWaterMarkThumbInfo getPreWbThumbInfo(int albumId, WbPosition wbPosition) {
		int size = mAlbumList.size();
		int preAlbumId = getPreAlbumId(albumId, size);
		ArrayList<PrizeWaterMarkThumbInfo> wbThumbInfoList = getWbThumbInfoList(preAlbumId);
		if (wbThumbInfoList.size() > 0) {
			wbPosition.mAlbumId = preAlbumId;
			wbPosition.mIndex = wbThumbInfoList.size() - 1;
			return wbThumbInfoList.get(wbThumbInfoList.size() - 1);
		} else {
			return getPreWbThumbInfo(preAlbumId, wbPosition);
		}
	}
	
	private PrizeWaterMarkThumbInfo getPreWbThumbInfo(int albumId, int itemIndex, WbPosition wbPosition) {
		int preAlbumId = albumId;
		int preItemIndex = itemIndex;
		if (itemIndex > 0) {
			preItemIndex = itemIndex - 1;
			ArrayList<PrizeWaterMarkThumbInfo> wbThumbInfoList = getWbThumbInfoList(albumId);
			wbPosition.mAlbumId = preAlbumId;
			wbPosition.mIndex = preItemIndex;
			return wbThumbInfoList.get(preItemIndex);
		} else {
			return getPreWbThumbInfo(albumId, wbPosition);
		}
	}
	
	private PrizeWaterMarkThumbInfo getNextWbThumbInfo(int albumId, int itemIndex, WbPosition wbPosition) {
		ArrayList<PrizeWaterMarkThumbInfo> wbThumbInfoList = getWbThumbInfoList(albumId);
		int nextIndex = itemIndex + 1;
		int thumbInfoSize = wbThumbInfoList.size();
		if (nextIndex < thumbInfoSize) {
			wbPosition.mAlbumId = albumId;
			wbPosition.mIndex = nextIndex;
			return wbThumbInfoList.get(nextIndex);
		} else {
			return getNextWbThumbInfo(albumId, wbPosition);
		}
	}
	
	private PrizeWaterMarkThumbInfo getPreTwoWbThumbInfo(int albumId, int itemIndex, WbPosition wbPosition) {
		int preTwoAlbumId = albumId;
		int preTwoItemIndex = itemIndex;
		if (itemIndex > 1) { //
			preTwoItemIndex = itemIndex - 2;
			ArrayList<PrizeWaterMarkThumbInfo> wbThumbInfoList = getWbThumbInfoList(albumId);
			wbPosition.mAlbumId = preTwoAlbumId;
			wbPosition.mIndex = preTwoItemIndex;
			return wbThumbInfoList.get(preTwoItemIndex);
		} else if (itemIndex > 0) {
			return getPreWbThumbInfo(albumId, wbPosition);
		} else {
			int size = mAlbumList.size();
			int preAlbumId = getPreAlbumId(albumId, size);
			ArrayList<PrizeWaterMarkThumbInfo> wbThumbInfoList = getWbThumbInfoList(preAlbumId);
			if (wbThumbInfoList.size() > 1) {
				wbPosition.mAlbumId = preTwoAlbumId;
				wbPosition.mIndex = wbThumbInfoList.size() - 2;
				return wbThumbInfoList.get(wbThumbInfoList.size() - 2);
			} else if (wbThumbInfoList.size() > 0) {
				return getPreWbThumbInfo(preAlbumId, wbPosition);
			} else {
				return getPreTwoWbThumbInfo(getPreAlbumId(preAlbumId, size), 0, wbPosition);
			}
		}
	}
	
	private PrizeWaterMarkThumbInfo getNextWbThumbInfo(int albumId, WbPosition wbPosition) {
		int size = mAlbumList.size();
		int nextAlbumId = getNextAlbumId(albumId, size);
		ArrayList<PrizeWaterMarkThumbInfo> wbThumbInfoList = getWbThumbInfoList(nextAlbumId);
		if (wbThumbInfoList.size() > 0) {
			wbPosition.mAlbumId = nextAlbumId;
			wbPosition.mIndex = 0;
			return wbThumbInfoList.get(0);
		} else {
			return getNextWbThumbInfo(nextAlbumId, wbPosition);
		}
	}
	
	private PrizeWaterMarkThumbInfo getNextTwoWbThumbInfo(int albumId, int itemIndex, WbPosition wbPosition) {
		ArrayList<PrizeWaterMarkThumbInfo> wbThumbInfoList = getWbThumbInfoList(albumId);
		int nextIndex = itemIndex + 2;
		int thumbInfoSize = wbThumbInfoList.size();
		if (nextIndex < thumbInfoSize) {
			wbPosition.mAlbumId = albumId;
			wbPosition.mIndex = nextIndex;
			return wbThumbInfoList.get(nextIndex);
		} else if (nextIndex == thumbInfoSize) {
			return getNextWbThumbInfo(albumId, wbPosition);
		} else {
			int size = mAlbumList.size();
			int nextAlbumId = getNextAlbumId(albumId, size);
			wbThumbInfoList = getWbThumbInfoList(nextAlbumId);
			if (wbThumbInfoList.size() > 2) {
				wbPosition.mAlbumId = nextAlbumId;
				wbPosition.mIndex = 1;
				return wbThumbInfoList.get(1);
			} else if (wbThumbInfoList.size() > 1) {
				return getNextWbThumbInfo(nextAlbumId, wbPosition);
			} else {
				return getNextTwoWbThumbInfo(getNextAlbumId(nextAlbumId, size), -1, wbPosition);
			}
		}
	}
	
	private void setPageIndication(int index) {
		int count = getWbThumbInfoList(mCurrentAlbumId).size();
		mPageIndicationTv.setText(""+(index+1)+"/"+""+count);
	}
	
	private void setPageIndication(int index, int count) {
		mPageIndicationTv.setText(""+(index+1)+"/"+""+count);
	}
	
	private Bitmap getShowBitmap(final int albumPostion,final int itemPostion){
		Bitmap mBitmap = null;
		PrizeWaterMarkThumbInfo mPrizeWaterMarkThumbInfo = getPositionInfo(albumPostion, itemPostion);
		
		if(mPrizeWaterMarkThumbInfo.getIsEdit()){
			
			ArrayList<PrizeWaterMarkImageResourceInfo> imageResourceInfoList = getImageResourceList(mPrizeWaterMarkThumbInfo);
			if((mPrizeWaterMarkThumbInfo.getMWidth()!=0.0f||mPrizeWaterMarkThumbInfo.getMHeight()!=0.0f)){
				if(mPrizeWaterMarkThumbInfo.getMWidth()>=mPrizeWaterMarkThumbInfo.getMHeight()){
					mBitmap = Bitmap.createBitmap((int)mPrizeWaterMarkThumbInfo.getMWidth(), (int)mPrizeWaterMarkThumbInfo.getMHeight(), Config.ARGB_8888);
				}else{
					mBitmap = Bitmap.createBitmap((int)mPrizeWaterMarkThumbInfo.getMWidth(), (int)mPrizeWaterMarkThumbInfo.getMHeight(), Config.ARGB_8888);
				}
			}else {
				int resID = mCameraActivity.getResources().getIdentifier(imageResourceInfoList.get(0).getImageDataPath(), "drawable", mCameraActivity.getBaseContext().getPackageName());
				Bitmap bitmapss = BitmapFactory.decodeResource(mCameraActivity.getResources(), resID);
				if(bitmapss.getWidth()>=bitmapss.getHeight()){
					mBitmap = Bitmap.createBitmap(bitmapss.getWidth(), bitmapss.getHeight(), Config.ARGB_8888);
				}else{
					mBitmap = Bitmap.createBitmap(bitmapss.getWidth(), bitmapss.getHeight(), Config.ARGB_8888);
				}
				if(!bitmapss.isRecycled()){
					bitmapss.recycle();
					bitmapss = null;
				}
			}
			Canvas canvas = null;
			if (mTypeFace == null) {
				 mTypeFace =Typeface.createFromAsset(mCameraActivity.getAssets(),"text_youyuan.ttf");
			 }
			Paint mPaint = new Paint();
			mPaint.setAntiAlias(true);
			float mdisplaypx = mCameraActivity.getResources().getDisplayMetrics().density;
			/*prize-xuchunming-20180108-bugid:47105-start*/
			//canvas = new Canvas(mBitmap);
			/*prize-xuchunming-20180108-bugid:47105-end*/
			int mBitmapWidth = 0;
			int mBitmapHeight = 0;
			boolean isBitmapIsFull = true;

			for(PrizeWaterMarkImageResourceInfo mPrizeWaterMarkImageResourceInfo:imageResourceInfoList){
				/*prize-xuchunming-20180108-bugid:47105-start*/
				//canvas.save();
				/*prize-xuchunming-20180108-bugid:47105-end*/
				String string = null;
				if(mPrizeWaterMarkImageResourceInfo.getImageType() == 1){
					 Calendar c = Calendar.getInstance();  
					 c.setTimeZone(TimeZone.getTimeZone("GMT+8:00")); 
					 int w =c.get(Calendar.DAY_OF_WEEK) - 1;      
					 if (w < 0){        
						 w = 0;      
					 } 
					 if(w==0){
						 string ="prize_watermark_mode"+7+"_time1";
					 }else{
						 string ="prize_watermark_mode"+w+"_time1";
					 }
				}else{
					string = mPrizeWaterMarkImageResourceInfo.getImageDataPath();
				}
				int resID = mCameraActivity.getResources().getIdentifier(string, "drawable", mCameraActivity.getBaseContext().getPackageName());
				Bitmap bitmap = BitmapFactory.decodeResource(mCameraActivity.getResources(), resID);
				/*prize-xuchunming-20180108-bugid:47105-start*/
				if(bitmap.getWidth() > mBitmap.getWidth() || bitmap.getHeight() > mBitmap.getHeight()) {
					mBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888);
				}
				canvas = new Canvas(mBitmap);
				canvas.save();
				/*prize-xuchunming-20180108-bugid:47105-end*/
				Rect mSrcRect, mDestRect;  
				canvas.translate(mPrizeWaterMarkImageResourceInfo.getImageXPostion(), mPrizeWaterMarkImageResourceInfo.getImageYPostion());
				if(mPrizeWaterMarkImageResourceInfo.getImageXPostion()>0||mPrizeWaterMarkImageResourceInfo.getImageYPostion()>0){
					isBitmapIsFull = false;
				}
				if(isBitmapIsFull){
					mBitmapWidth = bitmap.getWidth();
					mBitmapHeight = bitmap.getHeight();
				}else{
					mBitmapWidth = (int) mPrizeWaterMarkThumbInfo.getMWidth();
					mBitmapHeight = (int) mPrizeWaterMarkThumbInfo.getMHeight();
				}
		
				mSrcRect = new Rect(0,0,(int)(bitmap.getWidth()),(int)(bitmap.getHeight()));
				mDestRect = new Rect(0,0,(int)(bitmap.getWidth()),(int)(bitmap.getHeight()));

				canvas.drawBitmap(bitmap, mSrcRect, mDestRect, mPaint);
				if(bitmap.isRecycled()){
					bitmap.recycle();
				}
				
				canvas.restore();
				
				ArrayList<PrizeWaterMarkTextResourceInfo> textResourceInfoList = getTextResourceList(mPrizeWaterMarkThumbInfo);
				mPaint.setTextAlign(Paint.Align.CENTER);
				for(PrizeWaterMarkTextResourceInfo mPrizeWaterMarkTextResourceInfo:textResourceInfoList){
					canvas.save();
					mPaint.setTextSize(mPrizeWaterMarkTextResourceInfo.getTextSize()*mdisplaypx);
					mPaint.setColor(Color.parseColor(mPrizeWaterMarkTextResourceInfo.getTextColor())); 
					mPaint.setTypeface(mTypeFace);
					FontMetrics mFontMetricsInt = mPaint.getFontMetrics();
					Rect bounds = new Rect();
					/*prize-xuchunming-20171211-bugid:44931-start*/
					if(mPrizeWaterMarkTextResourceInfo.getShowString() != null && mPrizeWaterMarkTextResourceInfo.getShowString().length() > 1) {
						mPaint.getTextBounds(mPrizeWaterMarkTextResourceInfo.getShowString(), 0, 1, bounds);
					}
					/*prize-xuchunming-20171211-bugid:44931-end*/
					if(mPrizeWaterMarkTextResourceInfo.getTextWtype() == 1){
						SimpleDateFormat formatter = new SimpleDateFormat(mPrizeWaterMarkTextResourceInfo.getTimeTextType());   
						Date curDate = new Date(System.currentTimeMillis());
						mPrizeWaterMarkTextResourceInfo.setShowString(formatter.format(curDate));
					}else  if(mPrizeWaterMarkTextResourceInfo.getTextWtype() == 6){
						Calendar mCalendar = Calendar.getInstance();
						mCalendar.setTimeInMillis(System.currentTimeMillis());
						mPrizeWaterMarkTextResourceInfo.setShowString(PrizeLunar.getLunar(mCalendar.get(Calendar.YEAR), mCalendar.get(Calendar.MONTH)+1, mCalendar.get(Calendar.DAY_OF_MONTH)));
					}
					if(mPrizeWaterMarkTextResourceInfo.getTextViewAlign() == 0){
						
						int translatey = mPrizeWaterMarkTextResourceInfo.getTextYPostion();
						if(mPrizeWaterMarkTextResourceInfo.getTextView()!=null){
							translatey += (mPrizeWaterMarkTextResourceInfo.getTextView().getHeight()-bounds.height())/2+bounds.height();
						}
						if(mPrizeWaterMarkTextResourceInfo.getTextXPostion()==0){
							canvas.translate(mPrizeWaterMarkTextResourceInfo.getTextXPostion()+mBitmapWidth/2, translatey);
						}else{
							canvas.translate(mPrizeWaterMarkTextResourceInfo.getTextXPostion(), translatey);
						}
						canvas.drawText(getDrawBitMap(mPrizeWaterMarkTextResourceInfo), 0, 0, mPaint);
						
					}else if(mPrizeWaterMarkTextResourceInfo.getTextViewAlign() == 1){
						int translatey = mPrizeWaterMarkTextResourceInfo.getTextYPostion();
						
						if(mPrizeWaterMarkTextResourceInfo.getTextView()!=null){
							translatey += (mPrizeWaterMarkTextResourceInfo.getTextView().getHeight()-bounds.height())/2+bounds.height();
						}
						if(mPrizeWaterMarkTextResourceInfo.getTextXPostion()==0){
							canvas.translate(mBitmapWidth-mPaint.measureText(mPrizeWaterMarkTextResourceInfo.getShowString())/2, translatey);
						}else{
							canvas.translate(mBitmapWidth - mPrizeWaterMarkTextResourceInfo.getTextXPostion()-mPaint.measureText(mPrizeWaterMarkTextResourceInfo.getShowString())/2, translatey);
						}
						canvas.drawText(getDrawBitMap(mPrizeWaterMarkTextResourceInfo), 0, 0, mPaint);
					}
					canvas.restore();
				}
			}
			
		} else{
			
			mBitmap = BitmapFactory.decodeResource (mCameraActivity.getResources(), mCameraActivity.getResources().getIdentifier(getImageResourceList(mPrizeWaterMarkThumbInfo).get(0).getImageDataPath(), "drawable", mCameraActivity.getBaseContext().getPackageName()));
		}
		
		return mBitmap;
	}
	
	private String getDrawBitMap(final PrizeWaterMarkTextResourceInfo mPrizeWaterMarkTextResourceInfo){
		String string = null;
		if(mPrizeWaterMarkTextResourceInfo.getShowString().length()<=mPrizeWaterMarkTextResourceInfo.getTextLimitPostion()){
			string = mPrizeWaterMarkTextResourceInfo.getShowString();
		}else{
			string = mPrizeWaterMarkTextResourceInfo.getShowString().substring(0, mPrizeWaterMarkTextResourceInfo.getTextLimitPostion()-1)+"..";
		}
		return string ;
	}
	
	private void refreshHistory(PrizeWaterMarkThumbInfo mPrizeWaterMarkThumbInfo) {
		ArrayList<PrizeWaterMarkThumbInfo> historyList = getWbThumbInfoList(0);
		int size = historyList.size();
		mDbManger.InsertOrUpdateSQLHistory(mPrizeWaterMarkThumbInfo.getAlbumId(), mPrizeWaterMarkThumbInfo.getThumbID(), historyList.get(size - 1).getAlbumId(), historyList.get(size - 1).getThumbID(), size);
		int findIndex = -1;
		for (int i = 0; i < historyList.size(); i++) {
			PrizeWaterMarkThumbInfo wbInfo = historyList.get(i);
			int albumId = wbInfo.getAlbumId();
			int index = wbInfo.getThumbID();
			if (albumId == mPrizeWaterMarkThumbInfo.getAlbumId() && mPrizeWaterMarkThumbInfo.getThumbID() == index) {
				findIndex = i;
				break;
			}
		}
		if (findIndex == -1) {
			if (size < 8) {
				historyList.add(0, mPrizeWaterMarkThumbInfo);
				mHandler.sendEmptyMessage(MSG_REFRESH_VIEW);
			} else {
				historyList.remove(size - 1);
				historyList.add(0, mPrizeWaterMarkThumbInfo);
				mHandler.sendEmptyMessage(MSG_REFRESH_VIEW);
			}
		} else if (findIndex > 0) {
			historyList.remove(findIndex);
			historyList.add(0, mPrizeWaterMarkThumbInfo);
			mHandler.sendEmptyMessage(MSG_REFRESH_VIEW);
		}
	}
	
	private void refreshByHistory() {
		WbPosition wbPosition = mPostionList.get(mCurrentIndex);
		Log.i(TAG, "refreshByHistory wbPosition=" + wbPosition);
		if (wbPosition.mAlbumId == 0) {
			if (wbPosition.mIndex != 0) {
				mCurrentItemPostion = 0;
				showWbPager();
			}
		} else if (wbPosition.mAlbumId == mAlbumList.size() - 1) {
			ArrayList<PrizeWaterMarkThumbInfo> wbThumbInfoList = getWbThumbInfoList(wbPosition.mAlbumId);
			int viewIndex = mViewPager.getCurrentItem() % MAX_VIEW_SIZE;
			if (wbThumbInfoList.size() == wbPosition.mIndex + 1) {
				View view = mPagerChildViewList.get((viewIndex + 1) % MAX_VIEW_SIZE);
				int nextPosition = (mCurrentIndex + 1) % MAX_VIEW_SIZE;
				WbPosition nextWbPosition = mPostionList.get(nextPosition);
				bindViewData(getNextWbThumbInfo(wbPosition.mAlbumId, wbPosition.mIndex, nextWbPosition), view);
				
				view = mPagerChildViewList.get((viewIndex + 2) % MAX_VIEW_SIZE);
				nextPosition = (mCurrentIndex + 2) % MAX_VIEW_SIZE;
				nextWbPosition = mPostionList.get(nextPosition);
				bindViewData(getNextTwoWbThumbInfo(wbPosition.mAlbumId, wbPosition.mIndex, nextWbPosition), view);
			} else if (wbThumbInfoList.size() == wbPosition.mIndex + 2) {
				
				View view = mPagerChildViewList.get((viewIndex + 2) % MAX_VIEW_SIZE);
				int nextPosition = (mCurrentIndex + 2) % MAX_VIEW_SIZE;
				WbPosition nextWbPosition = mPostionList.get(nextPosition);
				bindViewData(getNextWbThumbInfo(wbPosition.mAlbumId, wbPosition.mIndex + 1, nextWbPosition), view);
			}
		} else if (wbPosition.mAlbumId == 1) {
			int viewIndex = mViewPager.getCurrentItem() % MAX_VIEW_SIZE;
			if (wbPosition.mIndex == 0) {
				View view = mPagerChildViewList.get((viewIndex - 1 + MAX_VIEW_SIZE) % MAX_VIEW_SIZE);
				int prePosition = (mCurrentIndex - 1 + MAX_VIEW_SIZE) % MAX_VIEW_SIZE;
				WbPosition preWbPosition = mPostionList.get(prePosition);
				bindViewData(getPreWbThumbInfo(wbPosition.mAlbumId, wbPosition.mIndex, preWbPosition), view);
				
				view = mPagerChildViewList.get((viewIndex - 2 + MAX_VIEW_SIZE) % MAX_VIEW_SIZE);
				prePosition = (mCurrentIndex - 2 + MAX_VIEW_SIZE) % MAX_VIEW_SIZE;
				preWbPosition = mPostionList.get(prePosition);
				bindViewData(getPreTwoWbThumbInfo(wbPosition.mAlbumId, wbPosition.mIndex, preWbPosition), view);
			} else if (wbPosition.mIndex == 1) {
				View view = mPagerChildViewList.get((viewIndex - 2 + MAX_VIEW_SIZE) % MAX_VIEW_SIZE);
				int prePosition = (mCurrentIndex - 2 + MAX_VIEW_SIZE) % MAX_VIEW_SIZE;
				WbPosition preWbPosition = mPostionList.get(prePosition);
				bindViewData(getPreTwoWbThumbInfo(wbPosition.mAlbumId, wbPosition.mIndex - 1, preWbPosition), view);
			}
		}
	}
	
	private Bitmap convertViewToBitmap(View view){
		view.setDrawingCacheEnabled(true);
		view.buildDrawingCache();
	    Bitmap bitmap = view.getDrawingCache();
		return bitmap;
	}
	
	public Bitmap getMagicDrawingCache(View view, int x, int y) {
		if (view.getWidth() + view.getHeight() == 0) {
			view.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
			view.layout(x, y, view.getMeasuredWidth() + x, view.getMeasuredHeight() + y);
		}
		int viewWidth = view.getWidth();
		int viewHeight = view.getHeight();
		Bitmap bitmap = Bitmap.createBitmap(viewWidth, viewHeight, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		view.draw(canvas);
		return bitmap;
	}
	
	public Bitmap getScreenBitmap(int orientation, float x, float y) {
		Bitmap bitmap = getShowBitmap(mCurrentAlbumId, mCurrentItemPostion);
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		/*prize-xuchunming-20171218-bugid:45614-start*/
		Bitmap b ;
		if(getOrientation() != getUiOrientation()) {
			Matrix matrix = new Matrix();
			matrix.setRotate(getOrientation() - getUiOrientation(), width / 2, height / 2);
			b = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
		}else {
			b = Bitmap.createBitmap(bitmap, 0, 0, width, height, null, true);
		}
		/*prize-xuchunming-20171218-bugid:45614-end*/
		return b;
	}
	
	public void refreshHistory() {
		AsyncTask.execute(new Runnable() {
			
			@Override
			public void run() {
				PrizeWaterMarkThumbInfo mPrizeWaterMarkThumbInfo = getPositionInfo();
				if (mPrizeWaterMarkThumbInfo != null && mPrizeWaterMarkThumbInfo.getAlbumId() > 0) {
					refreshHistory(mPrizeWaterMarkThumbInfo);
				}
			}
		});
	}
	
	public PrizeWaterMarkThumbInfo getPositionInfo() {
		return getPositionInfo(mCurrentAlbumId, mCurrentItemPostion);
	}
	
	public PrizeWaterMarkThumbInfo getPositionInfo(int albumId, int itemPositon) {
		ArrayList<PrizeWaterMarkThumbInfo> infos = getWbThumbInfoList(albumId);
		int size = infos.size();
		if (size > 0 && itemPositon >= 0 && itemPositon < size) {
			return infos.get(itemPositon);
		} else {
			return new PrizeWaterMarkThumbInfo();
		}
	}
	
	@Override
	public void setChangeCurrentShow(final String string,final ImageView mRotateImageView) {
		// TODO Auto-generated method stub
		RelativeLayout mRelativeLayout = (RelativeLayout)mApdater.getListWaterViewShow().get(mCurrentItemPostion).findViewById(R.id.move_watermarkinglayout);
		mRotateImageView.setImageBitmap(getShowBitmap(mCurrentAlbumId, mCurrentItemPostion));
		mApdater.notifyDataSetChanged();
	}

	private class GatureListener extends SimpleOnGestureListener{
		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			int x = 0;
			int y = 0;
			/*prize-add-for 19:9 full screen xiaoping-20180504-start*/
			if(mCameraActivity.getISettingCtrl().getSettingValue(SettingConstants.KEY_PICTURE_RATIO) != null && mCameraActivity.getISettingCtrl().getSettingValue(SettingConstants.KEY_PICTURE_RATIO).equals(SettingDataBase.PICTURE_RATIO_16_9)) {
				x = (int)e.getX();
				y = (int)e.getRawY() - (int)mCameraActivity.getResources().getDimension(R.dimen.effect_layout_margintop);
			} else if (mCameraActivity.getISettingCtrl().getSettingValue(SettingConstants.KEY_PICTURE_RATIO).equals(SettingDataBase.PICTURE_RATIO_19_9))  {
				x = (int) e.getRawX();
				y = (int) e.getRawY();
			} else {
				x = (int)e.getX();
				y = (int)e.getY();
			}
			/*prize-add-for 19:9 full screen xiaoping-20180504-end*/
			Log.i(TAG,"getX: "+e.getX()+",getY: "+e.getY()+",getRawX: "+e.getRawX()+",getRawY: "+e.getRawY()+",x: "+x+",y: "+y);
			mCameraActivity.getCameraActor().getonSingleTapUpListener().onSingleTapUp(null, x, y);
			return super.onSingleTapUp(e);
		}
	}
	
	@Override
	public void init(Activity activity, ICameraAppUi cameraAppUi, IModuleCtrl moduleCtrl) {
		Log.i(TAG, "[init]...");
        mICameraAppUi = cameraAppUi;
        mIModuleCtrl = moduleCtrl;
	}

	@Override
	public void reset() {
		Log.i(TAG, "[reset]...");
	}

	@Override
	public boolean update(int type, Object... args) {
		switch (type) {
		case WaterMarkMode.ON_CAMERA_CLOSED:
			
			break;
		case WaterMarkMode.ON_FULL_SCREEN_CHANGED:
			mIsInCameraPreview = (Boolean) args[0];
			Log.i(TAG, "ON_FULL_SCREEN_CHANGED, mIsInCameraPreview = " + mIsInCameraPreview);
			if (mIsInCameraPreview) {
                show();
            } else {
                // because when effect is showing, we have hide the ALLViews,so
                // need show the views
                // otherwise back to Camera,you will found all the UI is hide
                if (isWaterMarkModeViewShowing()) {
                    mICameraAppUi.showAllViews();
                }
                hide();
            }
			break;
		case WaterMarkMode.ON_SETTING_BUTTON_CLICK:
			mIsShowSetting = (Boolean) args[0];
            Log.i(TAG, "ON_SETTING_BUTTON_CLICK,mIsShowSetting =  " + mIsShowSetting);
            if (mIsShowSetting) {
                hide();
            } else {
                show();
            }
			break;
		case WaterMarkMode.ON_LEAVE_WATERMARK_MODE:
            uninit();
            break;
		case WaterMarkMode.ON_SELFTIMER_CAPTUEING:
            Log.i(TAG, "[ON_SELFTIMER_CAPTUEING] args[0] = "
                    + (Boolean) args[0] + ", mIsInPictureTakenProgress = "
                    + mIsInPictureTakenProgress);
            if ((Boolean) args[0]) {
                hide();
            } else {
                if (!mIsInPictureTakenProgress) {
                    show();
                }
            }
            break;

        case WaterMarkMode.IN_PICTURE_TAKEN_PROGRESS:
            boolean inProgress = (Boolean) args[0];
            Message msg = mHandler.obtainMessage(MSG_STATE_VIEW, inProgress ? 1 : 0, 0);
            mHandler.sendMessage(msg);
            break;
		default:
			break;
		}
		return false;
	}
	
	private boolean isWaterMarkModeViewShowing() {
		boolean isWaterMarkModeViewShowing = (View.VISIBLE == getView().getVisibility());
        Log.d(TAG, "isWaterMarkModeViewShowing = " + isWaterMarkModeViewShowing);
        return isWaterMarkModeViewShowing;
	}
	
	private boolean is16Radio9() {
		String radio = mCameraActivity.getISettingCtrl().getSettingValue(SettingConstants.KEY_PICTURE_RATIO);
    	boolean is169 = radio.equals(SettingDataBase.PICTURE_RATIO_16_9);
    	Log.i(TAG, "is16Radio9 radio=" + radio + " is169=" + is169);
		return is169;
	}
	
	public int getTopMargin() {
		return is16Radio9() ? mTopMargin : 0;
	}

	@Override
	public void setListener(Object obj) {
	}
	
	public void showCloseTv(){
		if(mCloseTv != null){
			mCloseTv.setVisibility(View.VISIBLE);
		}
	}
	
	public void hideCloseTv(){
		if(mCloseTv != null){
			mCloseTv.setVisibility(View.INVISIBLE);
		}
	}

	/*prize-xuchunming-20180108-bugid:47105-start*/
	@Override
	public void onRotateMeasure(WaterMarkRotateLayout v) {
		// TODO Auto-generated method stub

		// TODO Auto-generated method stub

		RelativeLayout view = (RelativeLayout) v.getParent();
		RelativeLayout.LayoutParams params = (android.widget.RelativeLayout.LayoutParams) view.getLayoutParams();
		if(view.getWidth() < view.getHeight() && v.getOverLimmitTopMargin() != -1) {
			params.topMargin = v.getOverLimmitTopMargin();
			v.setOverLimmitTopMargin(-1);
			
		}
		
		if(view.getWidth() > view.getHeight() &&params.topMargin+view.getWidth()>mViewPager.getHeight()){
			v.setOverLimmitTopMargin(params.topMargin);
			params.topMargin = mViewPager.getHeight() - view.getWidth();
		}
		
		if(view.getWidth() > view.getHeight() && v.getOverLimmitLeftMargin() != -1) {
			params.leftMargin = v.getOverLimmitLeftMargin();
			v.setOverLimmitLeftMargin(-1);
			
		}
		
		if(view.getWidth() < view.getHeight()  &&params.leftMargin+view.getHeight()>mViewPager.getWidth()){
			v.setOverLimmitLeftMargin(params.leftMargin);
			params.leftMargin = mViewPager.getWidth() - view.getHeight();
		}
		
		Xpostion = params.leftMargin;
		Ypostion = params.topMargin;
		
		v.getPrizeWaterMarkThumbInfo().setXpostion(Xpostion);
		v.getPrizeWaterMarkThumbInfo().setYpostion(Ypostion);
		view.setLayoutParams(params);
	
	}
	
	public int getWaterX() {
		RelativeLayout mRelativeLayout = (RelativeLayout) mPagerChildViewList.get((mViewPager.getCurrentItem() + MAX_VIEW_SIZE) % MAX_VIEW_SIZE).findViewById(R.id.move_watermarkinglayout);
		RelativeLayout.LayoutParams params = (android.widget.RelativeLayout.LayoutParams) mRelativeLayout.getLayoutParams();
		return params.leftMargin;
	}
	
	public int getWaterY() {
		RelativeLayout mRelativeLayout = (RelativeLayout) mPagerChildViewList.get((mViewPager.getCurrentItem() + MAX_VIEW_SIZE) % MAX_VIEW_SIZE).findViewById(R.id.move_watermarkinglayout);
		RelativeLayout.LayoutParams params = (android.widget.RelativeLayout.LayoutParams) mRelativeLayout.getLayoutParams();
		return params.topMargin;
	}
	
	public int getWaterTopMarginToSurfaceView() {
		int[] viewPageLocation = new int[2] ;
		int[] surfaceViewLocation = new int[2] ;
		mViewPager.getLocationInWindow(viewPageLocation);
		getContext().getPreviewSurfaceView().getLocationInWindow(surfaceViewLocation);
		mWaterTopMarginToSurfaceView = viewPageLocation[1] - surfaceViewLocation[1];
		return mWaterTopMarginToSurfaceView;
	}
	/*prize-xuchunming-20180108-bugid:47105-end*/
	
		/*prize-add-first open the watermark flash back after the database is modified-xiaoping-20180316-start*/
	public int adjustIndex(int albumId, int index) {
		int itemposition = 1;
		switch (albumId) {
		case 1:
			itemposition = index -41 +6;
			break;
		case 2:
			itemposition = index - 44 + 7;
			break;
		case 3:
			itemposition = index - 47 + 7;
			break;
		case 4:
			itemposition = index - 50 + 6;
			break;
		case 5:
			itemposition = index - 53 + 6;
			break;
		case 6:
			itemposition = index - 56 + 8;
			break;
		default:
			break;
		}
		Log.d(TAG, "itemposition: "+itemposition);
		return itemposition;
	}
	/*prize-add-first open the watermark flash back after the database is modified-xiaoping-20180316-start*/
}
