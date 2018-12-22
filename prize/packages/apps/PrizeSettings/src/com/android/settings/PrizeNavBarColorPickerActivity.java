/*
* Nav bar color customized feature.
* created. prize-zhaojian
*/

package com.android.settings;

import android.app.Activity;
import android.app.StatusBarManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.PointF;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;

public class PrizeNavBarColorPickerActivity extends Activity implements View.OnClickListener {
    private static String TAG = "PrizeNavBarColorPickerActivity";

    private List<Integer> mColorValueList;
    private SharedPreferences.Editor mColorPositionEdit;
    private PrizeNavBarColorPickerView mColorPickerView;
    private float mTopXOne,mTopYOne,mBottomXOne,mBottomYOne;
    private float mTopXTwo,mTopYTwo,mBottomXTwo,mBottomYTwo;
    private float mTopXThree,mTopYThree,mBottomXThree,mBottomYThree;
    private float mTopXFour,mTopYFour,mBottomXFour,mBottomYFour;
    private float mTopXFive,mTopYFive,mBottomXFive,mBottomYFive;
    private float mTopXSix,mTopYSix,mBottomXSix,mBottomYSix;
    private List<float[]> mPositionList;
//    private ExecutorService mExecutorService = Executors.newFixedThreadPool(5);
    private static int mColor;
    private static PointF mTopSelectPoint;
    private static PointF mBottomSelectPoint;
    private boolean mIsTouchMoved = false;

    private TextView mNewColorView;
    private boolean mColorChanged;
    private int mNavigationBarColor;
    private float mCurrentTopX;
    private float mCurrentTopY;
    private float mCurrentBottomX;
    private float mCurrentBottomY;
    private int mRecentClickedColor;
    private boolean mFinishedOrCanceledOrPaused = false;
    private int mRecentColor;
    private boolean mHasClickedRecent;
    private SharedPreferences mColorPositionSp;
    private List<ImageView> mViewList;

    //prize modify bug 40394 zj 20171014 start
    private BroadcastReceiver homePressReceiver = new BroadcastReceiver() {
        final String SYSTEM_DIALOG_REASON_KEY = "reason";
        final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";
        final String SYSTEM_DIALOG_REASON_RECENT_APPS = "recentapps";
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)){
                String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
                if(SYSTEM_DIALOG_REASON_HOME_KEY.equals(reason)){
                    Log.d(TAG,"click the home key");

                    finish();
                }

                if(SYSTEM_DIALOG_REASON_RECENT_APPS.equals(reason)) {
                    Log.d(TAG,"click the recent apps key");

                    mIsClickedRecentApps = true;
                }
            }
        }
    };
    private boolean mIsClickedRecentApps;
    //prize modify bug 40394 zj 20171014 end

    private static class ColorPositionHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            ColorAndPositionData colorAndPositionData = (ColorAndPositionData) msg.obj;
            mColor = colorAndPositionData.getColor();
            mTopSelectPoint = colorAndPositionData.getTopSelectPoint();
            mBottomSelectPoint = colorAndPositionData.getBottomSelectPoint();
        }
    }
    ColorPositionHandler mColorPositionHandler = new ColorPositionHandler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_color_picker);

        initData();

        initView();

        setDialogActivity();
        // Enable nav bar color. prize-linkh-20170901
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        //prize modify bug 40394 zj 20171014 start
        IntentFilter homeFilter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        registerReceiver(homePressReceiver, homeFilter);
        //prize modify bug 40394 zj 20171014 end
    }

    private void initData() {
        mColorValueList = new ArrayList<>(6);
        mPositionList = new ArrayList<>();

        mColorPositionSp = getSharedPreferences("COLOR_POSITION_SP", Context.MODE_PRIVATE);
        mColorPositionEdit = mColorPositionSp.edit();
        int colorOne = mColorPositionSp.getInt("color_value_one", 0);
        mColorValueList.add(colorOne);
        mTopXOne = mColorPositionSp.getFloat("top_x_one", 0);
        mTopYOne = mColorPositionSp.getFloat("top_y_one", 0);
        mBottomXOne = mColorPositionSp.getFloat("bottom_x_one", 0);
        mBottomYOne = mColorPositionSp.getFloat("bottom_y_one", 0);
        mPositionList.add(new float[]{mTopXOne, mTopYOne, mBottomXOne, mBottomYOne});

        int colorTwo = mColorPositionSp.getInt("color_value_two", 0);
        mColorValueList.add(colorTwo);
        mTopXTwo = mColorPositionSp.getFloat("top_x_two", 0);
        mTopYTwo = mColorPositionSp.getFloat("top_y_two", 0);
        mBottomXTwo = mColorPositionSp.getFloat("bottom_x_two", 0);
        mBottomYTwo = mColorPositionSp.getFloat("bottom_y_two", 0);
        mPositionList.add(new float[]{mTopXTwo, mTopYTwo, mBottomXTwo, mBottomYTwo});

        int colorThree = mColorPositionSp.getInt("color_value_three", 0);
        mColorValueList.add(colorThree);
        mTopXThree = mColorPositionSp.getFloat("top_x_three", 0);
        mTopYThree = mColorPositionSp.getFloat("top_y_three", 0);
        mBottomXThree = mColorPositionSp.getFloat("bottom_x_three", 0);
        mBottomYThree = mColorPositionSp.getFloat("bottom_y_three", 0);
        mPositionList.add(new float[]{mTopXThree, mTopYThree, mBottomXThree, mBottomYThree});

        int colorFour = mColorPositionSp.getInt("color_value_four", 0);
        mColorValueList.add(colorFour);
        mTopXFour = mColorPositionSp.getFloat("top_x_four", 0);
        mTopYFour = mColorPositionSp.getFloat("top_y_four", 0);
        mBottomXFour = mColorPositionSp.getFloat("bottom_x_four", 0);
        mBottomYFour = mColorPositionSp.getFloat("bottom_y_four", 0);
        mPositionList.add(new float[]{mTopXFour, mTopYFour, mBottomXFour, mBottomYFour});

        int colorFive = mColorPositionSp.getInt("color_value_five", 0);
        mColorValueList.add(colorFive);
        mTopXFive = mColorPositionSp.getFloat("top_x_five", 0);
        mTopYFive = mColorPositionSp.getFloat("top_y_five", 0);
        mBottomXFive = mColorPositionSp.getFloat("bottom_x_five", 0);
        mBottomYFive = mColorPositionSp.getFloat("bottom_y_five", 0);
        mPositionList.add(new float[]{mTopXFive, mTopYFive, mBottomXFive, mBottomYFive});

        int colorSix = mColorPositionSp.getInt("color_value_six", 0);
        mColorValueList.add(colorSix);
        mTopXSix = mColorPositionSp.getFloat("top_x_six", 0);
        mTopYSix = mColorPositionSp.getFloat("top_y_six", 0);
        mBottomXSix = mColorPositionSp.getFloat("bottom_x_six", 0);
        mBottomYSix = mColorPositionSp.getFloat("bottom_y_six", 0);
        mPositionList.add(new float[]{mTopXSix, mTopYSix, mBottomXSix, mBottomYSix});

        mNavigationBarColor = Settings.System.getInt(getContentResolver(), Settings.System.PRIZE_NAV_BAR_BG_COLOR, StatusBarManager.DEFAULT_NAV_BAR_COLOR);

        mRecentClickedColor = mColorPositionSp.getInt("recent_clicked_color",0);
        mRecentColor = mRecentClickedColor;

    }

    private void initView() {
        ImageView recentImageOne = (ImageView) findViewById(R.id.recent_image_one);
        ImageView recentImageTwo = (ImageView) findViewById(R.id.recent_image_two);
        ImageView recentImageThree = (ImageView) findViewById(R.id.recent_image_three);
        ImageView recentImageFour = (ImageView) findViewById(R.id.recent_image_four);
        ImageView recentImageFive = (ImageView) findViewById(R.id.recent_image_five);
        ImageView recentImageSix = (ImageView) findViewById(R.id.recent_image_six);
        recentImageOne.setOnClickListener(this);
        recentImageTwo.setOnClickListener(this);
        recentImageThree.setOnClickListener(this);
        recentImageFour.setOnClickListener(this);
        recentImageFive.setOnClickListener(this);
        recentImageSix.setOnClickListener(this);
        mViewList = new ArrayList<>();
        mViewList.add(recentImageOne);
        mViewList.add(recentImageTwo);
        mViewList.add(recentImageThree);
        mViewList.add(recentImageFour);
        mViewList.add(recentImageFive);
        mViewList.add(recentImageSix);

        for (int i = 0; i < 6; i++) {
            GradientDrawable background = (GradientDrawable) mViewList.get(i).getBackground();
            background.setColor(mColorValueList.get(i));
        }

        TextView finishView = (TextView) findViewById(R.id.finish_tv);
        TextView cancelView = (TextView) findViewById(R.id.cancel_tv);
        finishView.setOnClickListener(this);
        cancelView.setOnClickListener(this);

        mNewColorView = (TextView) findViewById(R.id.new_color_tv);
        TextView currentColorView = (TextView) findViewById(R.id.current_color_tv);
        currentColorView.setBackgroundColor(
                Settings.System.getInt(getContentResolver(), Settings.System.PRIZE_NAV_BAR_BG_COLOR, mColor));
        mColorPickerView = (PrizeNavBarColorPickerView) findViewById(R.id.colorPickerView);

        float currentTopX = mColorPositionSp.getFloat("recent_top_x", 0);
        float currentTopY = mColorPositionSp.getFloat("recent_top_y", 0);
        float currentBottomX = mColorPositionSp.getFloat("recent_bottom_x", 0);
        float currentBottomY = mColorPositionSp.getFloat("recent_bottom_y", 0);
        mCurrentTopX = currentTopX;
        mCurrentTopY = currentTopY;
        mCurrentBottomX = currentBottomX;
        mCurrentBottomY = currentBottomY;
        if (currentTopX != 0 && currentTopY != 0 && currentBottomX != 0 && currentBottomY != 0) {
            mColorPickerView.recentColorPosition(currentTopX, currentTopY, currentBottomX, currentBottomY);
        }

        final ColorAndPositionData colorAndPositionData = new ColorAndPositionData();
        mColorPickerView.setOnColorChangedListener(new PrizeNavBarColorPickerView.OnColorChangedListener() {
            @Override
            public void onColorChanged(int color,PointF topSelectPoint,PointF bottomSelectPoint) {
                mIsTouchMoved = true;

                Message message = Message.obtain();
                colorAndPositionData.setColor(color);
                colorAndPositionData.setTopSelectPoint(topSelectPoint);
                colorAndPositionData.setBottomSelectPoint(bottomSelectPoint);
                message.obj = colorAndPositionData;
                mColorPositionHandler.sendMessage(message);

                mNewColorView.setBackgroundColor(color);
                setNavBarColor(color);          

                mColorPositionEdit.putInt("recent_clicked_color",color);
                mColorPositionEdit.commit();
                mHasClickedRecent = false;
            }
        });
    }

    
    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG,"onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG,"onStop");

		//prize modify bug 40394 zj 20171014 start
        if(mIsClickedRecentApps){
            setNavBarColor(mNavigationBarColor);
        }
		 //prize modify bug 40394 zj 20171014 end
    }

	//prize modify bug 40394 zj 20171014 start
    @Override
    protected void onRestart() {
        Log.d(TAG,"onRestart");

        if(mIsClickedRecentApps){
            mIsClickedRecentApps = false;
            int recentClickedColor = mColorPositionSp.getInt("recent_clicked_color",0);
            setNavBarColor(recentClickedColor);
        }
        super.onRestart();
    }
	//prize modify bug 40394 zj 20171014 end

    @Override
    protected void onDestroy() {
		//prize modify bug 40394 zj 20171014 start
        unregisterReceiver(homePressReceiver);
		//prize modify bug 40394 zj 20171014 end

        super.onDestroy();
        //prize add for bug 39912 zhaojian 2017929 start
        Log.d(TAG,"onDestroy   mFinishedOrCanceledOrPaused = " + mFinishedOrCanceledOrPaused);
        if(!mFinishedOrCanceledOrPaused){
            canceledOrPaused();

            return;
        }

        Log.d(TAG,"onDestroy   mHasClickedRecent = " + mHasClickedRecent);
        if(mHasClickedRecent) {
            mFinishedOrCanceledOrPaused = true;
            clickFinishNoTouch();
        }
        //prize add for bug 39912 zhaojian 2017929 end
    }
   

    private void setDialogActivity() {
        Window win = getWindow();
        WindowManager m = getWindowManager();
        Display d = m.getDefaultDisplay();
        win.getDecorView().setPadding(0, 0, 0, 0);
        WindowManager.LayoutParams lp = win.getAttributes();
        
		// prize modify for different resolution, so definit a constant height and width  by zj 2017927 start
		//lp.width = (int) (d.getWidth() * 0.87);
        //lp.height = (int) (d.getHeight() * 0.84);
		lp.width = getResources().getInteger(R.integer.prize_activity_width);                 
		lp.height = getResources().getInteger(R.integer.prize_activity_height); 
		// prize modify for different resolution, so definit a constant height and width  by zj 2017927 end
        lp.gravity = Gravity.CENTER;
        win.setAttributes(lp);
        // Added by prize-linkh-201710251455 @{
        win.setIgnoreDimAmountAdjustment(true);
        win.setEnableSystemUINavBarColorBackground(true);
        // @}        
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.recent_image_one:
				if(mColorValueList.get(0) == 0){
					return;
				}
				mHasClickedRecent = true;
			  
                if (mTopXOne == 0 && mTopYOne == 0 && mBottomXOne == 0 && mBottomYOne == 0) {
                    return;
                }
                mColorPickerView.recentColorPosition(mTopXOne,mTopYOne,mBottomXOne,mBottomYOne);

                mColorPickerView.moveRecentPosition(mTopXOne,mTopYOne,mBottomXOne,mBottomYOne/*,mExecutorService*/);

                mColorPositionEdit.putFloat("recent_top_x",mTopXOne);
                mColorPositionEdit.putFloat("recent_top_y",mTopYOne);
                mColorPositionEdit.putFloat("recent_bottom_x",mBottomXOne);
                mColorPositionEdit.putFloat("recent_bottom_y",mBottomYOne);

                int colorOne = mColorPositionSp.getInt("color_value_one",mColorValueList.get(0));
                setNavBarColor(colorOne);

                mNewColorView.setBackgroundColor(colorOne);

                mColorPositionEdit.putInt("recent_clicked_color", colorOne);
                mColorPositionEdit.commit();
                break;
            case R.id.recent_image_two:
				if(mColorValueList.get(1) == 0){
					return;
				}
				mHasClickedRecent = true;

                if(mTopXTwo == 0 && mTopYTwo == 0 && mBottomXTwo == 0 && mBottomYTwo == 0){
                    return;
                }
                mColorPickerView.recentColorPosition(mTopXTwo,mTopYTwo,mBottomXTwo,mBottomYTwo);

                mColorPickerView.moveRecentPosition(mTopXTwo,mTopYTwo,mBottomXTwo,mBottomYTwo/*,mExecutorService*/);

                mColorPositionEdit.putFloat("recent_top_x",mTopXTwo);
                mColorPositionEdit.putFloat("recent_top_y",mTopYTwo);
                mColorPositionEdit.putFloat("recent_bottom_x",mBottomXTwo);
                mColorPositionEdit.putFloat("recent_bottom_y",mBottomYTwo);

                int colorTwo= mColorPositionSp.getInt("color_value_two",mColorValueList.get(1));
                setNavBarColor(colorTwo);

                mNewColorView.setBackgroundColor(colorTwo);

                mColorPositionEdit.putInt("recent_clicked_color",colorTwo);
                mColorPositionEdit.commit();
                break;
            case R.id.recent_image_three:
				if(mColorValueList.get(2) == 0){
					return;
				}
				mHasClickedRecent = true;
                if(mTopXThree == 0 && mTopYThree == 0 && mBottomXThree == 0 && mBottomYThree == 0){
                    return;
                }
                mColorPickerView.recentColorPosition(mTopXThree,mTopYThree,mBottomXThree,mBottomYThree);

                mColorPickerView.moveRecentPosition(mTopXThree,mTopYThree,mBottomXThree,mBottomYThree/*,mExecutorService*/);

                mColorPositionEdit.putFloat("recent_top_x",mTopXThree);
                mColorPositionEdit.putFloat("recent_top_y",mTopYThree);
                mColorPositionEdit.putFloat("recent_bottom_x",mBottomXThree);
                mColorPositionEdit.putFloat("recent_bottom_y",mBottomYThree);

                int colorThree= mColorPositionSp.getInt("color_value_three",mColorValueList.get(2));
                setNavBarColor(colorThree);

                mNewColorView.setBackgroundColor(colorThree);

                mColorPositionEdit.putInt("recent_clicked_color",colorThree);
                mColorPositionEdit.commit();
                break;
            case R.id.recent_image_four:
				if(mColorValueList.get(3) == 0){
					return;
				}
				mHasClickedRecent = true;
                if(mTopXFour == 0 && mTopYFour == 0 && mBottomXFour == 0 && mBottomYFour == 0){
                    return;
                }
                mColorPickerView.recentColorPosition(mTopXFour,mTopYFour,mBottomXFour,mBottomYFour);

                mColorPickerView.moveRecentPosition(mTopXFour,mTopYFour,mBottomXFour,mBottomYFour/*,mExecutorService*/);

                mColorPositionEdit.putFloat("recent_top_x",mTopXFour);
                mColorPositionEdit.putFloat("recent_top_y",mTopYFour);
                mColorPositionEdit.putFloat("recent_bottom_x",mBottomXFour);
                mColorPositionEdit.putFloat("recent_bottom_y",mBottomYFour);

                int colorFour= mColorPositionSp.getInt("color_value_four",mColorValueList.get(3));
                setNavBarColor(colorFour);

                mNewColorView.setBackgroundColor(colorFour);

                mColorPositionEdit.putInt("recent_clicked_color",colorFour);
                mColorPositionEdit.commit();
                break;
            case R.id.recent_image_five:
				if(mColorValueList.get(4) == 0){
					return;
				}
				mHasClickedRecent = true;
                if(mTopXFive == 0 && mTopYFive == 0 && mBottomXFive == 0 && mBottomYFive == 0){
                    return;
                }
                mColorPickerView.recentColorPosition(mTopXFive,mTopYFive,mBottomXFive,mBottomYFive);

                mColorPickerView.moveRecentPosition(mTopXFive,mTopYFive,mBottomXFive,mBottomYFive/*,mExecutorService*/);

                mColorPositionEdit.putFloat("recent_top_x",mTopXFive);
                mColorPositionEdit.putFloat("recent_top_y",mTopYFive);
                mColorPositionEdit.putFloat("recent_bottom_x",mBottomXFive);
                mColorPositionEdit.putFloat("recent_bottom_y",mBottomYFive);

                int colorFive= mColorPositionSp.getInt("color_value_five",mColorValueList.get(4));
                setNavBarColor(colorFive);

                mNewColorView.setBackgroundColor(colorFive);

                mColorPositionEdit.putInt("recent_clicked_color",colorFive);
                mColorPositionEdit.commit();
                break;
            case R.id.recent_image_six:
				if(mColorValueList.get(5) == 0){
					return;
				}
				mHasClickedRecent = true;
                if(mTopXSix == 0 && mTopYSix == 0 && mBottomXSix == 0 && mBottomYSix == 0){
                    return;
                }
                mColorPickerView.recentColorPosition(mTopXSix,mTopYSix,mBottomXSix,mBottomYSix);

                mColorPickerView.moveRecentPosition(mTopXSix,mTopYSix,mBottomXSix,mBottomYSix/*,mExecutorService*/);

                mColorPositionEdit.putFloat("recent_top_x",mTopXSix);
                mColorPositionEdit.putFloat("recent_top_y",mTopYSix);
                mColorPositionEdit.putFloat("recent_bottom_x",mBottomXSix);
                mColorPositionEdit.putFloat("recent_bottom_y",mBottomYSix);

                int colorSix= mColorPositionSp.getInt("color_value_six",mColorValueList.get(5));
                setNavBarColor(colorSix);

                mNewColorView.setBackgroundColor(colorSix);

                mColorPositionEdit.putInt("recent_clicked_color",colorSix);
                mColorPositionEdit.commit();
                break;
            case R.id.finish_tv:
                Log.d(TAG,"finish_tv");
                if(mIsTouchMoved){
                    mIsTouchMoved = false;

                    mFinishedOrCanceledOrPaused = true;
                    if(!mHasClickedRecent){

                        if (mColorValueList.size() == 6) {
                            mColorValueList.remove(5);
                        }

                        mColorValueList.add(0,mColor);
                        float[] positionArr = new float[]{mTopSelectPoint.x, mTopSelectPoint.y, mBottomSelectPoint.x, mBottomSelectPoint.y};
                        mPositionList.add(0, positionArr);

                        setNavBarColor(mColor);
                        PrizeNavBarColorPickerActivity.this.setResult(1);

                        // when in landscape mode, the pir can't receive the activity result.
                        // So we need to save color index here.
                        PrizeNavBarColorUtil.writeColorIndexToSettings(PrizeNavBarColorPickerActivity.this,
                                PrizeNavBarColorUtil.CUSTOM_COLOR_IDX);
                        for (int i = 0; i < 6; i++) {
                            GradientDrawable background = (GradientDrawable) mViewList.get(i).getBackground();
                            background.setColor(mColorValueList.get(i));
                        }

                        mColorPositionEdit.putInt("color_value_one",mColorValueList.get(0));
                        mColorPositionEdit.putFloat("top_x_one",mPositionList.get(0)[0]);
                        mColorPositionEdit.putFloat("top_y_one",mPositionList.get(0)[1]);
                        mColorPositionEdit.putFloat("bottom_x_one",mPositionList.get(0)[2]);
                        mColorPositionEdit.putFloat("bottom_y_one",mPositionList.get(0)[3]);

                        mColorPositionEdit.putInt("color_value_two",mColorValueList.get(1));
                        mColorPositionEdit.putFloat("top_x_two",mPositionList.get(1)[0]);
                        mColorPositionEdit.putFloat("top_y_two",mPositionList.get(1)[1]);
                        mColorPositionEdit.putFloat("bottom_x_two",mPositionList.get(1)[2]);
                        mColorPositionEdit.putFloat("bottom_y_two",mPositionList.get(1)[3]);

                        mColorPositionEdit.putInt("color_value_three",mColorValueList.get(2));
                        mColorPositionEdit.putFloat("top_x_three",mPositionList.get(2)[0]);
                        mColorPositionEdit.putFloat("top_y_three",mPositionList.get(2)[1]);
                        mColorPositionEdit.putFloat("bottom_x_three",mPositionList.get(2)[2]);
                        mColorPositionEdit.putFloat("bottom_y_three",mPositionList.get(2)[3]);

                        mColorPositionEdit.putInt("color_value_four",mColorValueList.get(3));
                        mColorPositionEdit.putFloat("top_x_four",mPositionList.get(3)[0]);
                        mColorPositionEdit.putFloat("top_y_four",mPositionList.get(3)[1]);
                        mColorPositionEdit.putFloat("bottom_x_four",mPositionList.get(3)[2]);
                        mColorPositionEdit.putFloat("bottom_y_four",mPositionList.get(3)[3]);

                        mColorPositionEdit.putInt("color_value_five",mColorValueList.get(4));
                        mColorPositionEdit.putFloat("top_x_five",mPositionList.get(4)[0]);
                        mColorPositionEdit.putFloat("top_y_five",mPositionList.get(4)[1]);
                        mColorPositionEdit.putFloat("bottom_x_five",mPositionList.get(4)[2]);
                        mColorPositionEdit.putFloat("bottom_y_five",mPositionList.get(4)[3]);

                        mColorPositionEdit.putInt("color_value_six",mColorValueList.get(5));
                        mColorPositionEdit.putFloat("top_x_six",mPositionList.get(5)[0]);
                        mColorPositionEdit.putFloat("top_y_six",mPositionList.get(5)[1]);
                        mColorPositionEdit.putFloat("bottom_x_six",mPositionList.get(5)[2]);
                        mColorPositionEdit.putFloat("bottom_y_six",mPositionList.get(5)[3]);

                        mColorPositionEdit.putFloat("recent_top_x",mPositionList.get(0)[0]);
                        mColorPositionEdit.putFloat("recent_top_y",mPositionList.get(0)[1]);
                        mColorPositionEdit.putFloat("recent_bottom_x",mPositionList.get(0)[2]);
                        mColorPositionEdit.putFloat("recent_bottom_y",mPositionList.get(0)[3]);
                        mColorPositionEdit.commit();
                    }
                    PrizeNavBarColorPickerActivity.this.finish();
                }else {
                    mFinishedOrCanceledOrPaused = true;
                    clickFinishNoTouch();
                }

                break;
            case R.id.cancel_tv:
                Log.d(TAG,"cancel_tv");
                if(mIsTouchMoved){     
                    mIsTouchMoved = false;
                    mFinishedOrCanceledOrPaused = true;
                    mHasClickedRecent = false;

                    saveRecentColorAndPosition();

                    setNavBarColor(mNavigationBarColor);
                    PrizeNavBarColorPickerActivity.this.setResult(0);
                    finish();
                }else {
                    mFinishedOrCanceledOrPaused = true;
                    mHasClickedRecent = false;

                    canceledOrPaused();
                    finish();
                }
                break;
        }
    }

    private void canceledOrPaused(){
        saveRecentColorAndPosition();
        if (mNavigationBarColor == 0) {
            setNavBarColor(StatusBarManager.DEFAULT_NAV_BAR_COLOR);
        } else {
            setNavBarColor(mNavigationBarColor);
        }
        PrizeNavBarColorPickerActivity.this.setResult(0);
    }

    private void clickFinishNoTouch(){
        mRecentClickedColor = mColorPositionSp.getInt("recent_clicked_color",0);
        if(mRecentClickedColor != 0) {
            PrizeNavBarColorUtil.writeColorIndexToSettings(PrizeNavBarColorPickerActivity.this, PrizeNavBarColorUtil.CUSTOM_COLOR_IDX);
            setNavBarColor(mRecentClickedColor);
        }
        PrizeNavBarColorPickerActivity.this.setResult(mColorChanged ? 1 : 0);
        finish();
    }

    private void saveRecentColorAndPosition(){
        mColorPositionEdit.putFloat("recent_top_x", mCurrentTopX);
        mColorPositionEdit.putFloat("recent_top_y", mCurrentTopY);
        mColorPositionEdit.putFloat("recent_bottom_x", mCurrentBottomX);
        mColorPositionEdit.putFloat("recent_bottom_y", mCurrentBottomY);
        mColorPositionEdit.putInt("recent_clicked_color",mRecentColor);
        mColorPositionEdit.commit();
    }

    private void setNavBarColor(int color) {
        // Added by prize-linkh-20170927 @{
        if (color == 0) {
            Log.d(TAG, "setNavBarColor() invalid color 0! Ignore!", new Throwable());
            return;
        } // @}

        int oldColor = Settings.System.getInt(getContentResolver(),
                Settings.System.PRIZE_NAV_BAR_BG_COLOR, StatusBarManager.DEFAULT_NAV_BAR_COLOR);


        if (oldColor != color) {
            mColorChanged = true;
            Settings.System.putInt(getContentResolver(), Settings.System.PRIZE_NAV_BAR_BG_COLOR, color);
        }
    }

    class ColorAndPositionData{
        private int color;
        private PointF topSelectPoint;
        private PointF bottomSelectPoint;

        public int getColor() {
            return color;
        }

        public void setColor(int color) {
            this.color = color;
        }

        PointF getTopSelectPoint() {
            return topSelectPoint;
        }

        void setTopSelectPoint(PointF topSelectPoint) {
            this.topSelectPoint = topSelectPoint;
        }

        PointF getBottomSelectPoint() {
            return bottomSelectPoint;
        }

        void setBottomSelectPoint(PointF bottomSelectPoint) {
            this.bottomSelectPoint = bottomSelectPoint;
        }
    }
}


