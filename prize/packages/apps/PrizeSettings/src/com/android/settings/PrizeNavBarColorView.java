/**
 * Nav bar color customized feature. prize-linkh-2017.07.11
 */
package com.android.settings;

import android.widget.RelativeLayout;
import android.util.AttributeSet;
import android.content.Context;
import android.content.Intent;
import android.widget.ImageView;
import android.view.View;
import java.util.ArrayList;
import android.util.Log;
import android.provider.Settings;
import android.content.Intent;
import android.database.ContentObserver;
import android.os.Handler;

public class PrizeNavBarColorView extends RelativeLayout implements View.OnClickListener {    
    private static final String TAG = "PrizeNavBarColorView";
    private static final boolean DBG = true;

    private ImageView[] mCurImageViews;
    private Context mContext;
    private int mCurSelectedColorViewId;

    private ContentObserver mColorIdxObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            if (DBG) {
                Log.d(TAG, "onChange()...");
            }

            handleColorIndexChanged();
        }

    };

    private void handleColorIndexChanged() {
        if (DBG) {
            Log.d(TAG, "handleColorIndexChanged()...");
        }

        init();
        View v = (ImageView)findViewById(mCurSelectedColorViewId);
        if (v != null) {
            changeColorImagesState(v);
        }
    }

    public PrizeNavBarColorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    private void init() {
        final int colorIndex = PrizeNavBarColorUtil.readColorIndexFromSettings(mContext);
        mCurSelectedColorViewId = PrizeNavBarColorUtil.getColorViewId(colorIndex);
        if (DBG) {
            Log.d(TAG, "Read color idx from sharedPref. colorIndex=" + colorIndex
                    + ", viewId=" + Integer.toHexString(mCurSelectedColorViewId));
        }
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        mContext.getContentResolver().registerContentObserver(
            Settings.System.getUriFor("prize_nav_bar_color_index"),
            false, mColorIdxObserver);
    }    

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mContext.getContentResolver().unregisterContentObserver(mColorIdxObserver);        
    }


    @Override
    public void onFinishInflate() {
        final ArrayList<ImageView> viewList = new ArrayList<ImageView>();
        ImageView iv = (ImageView)findViewById(R.id.default_white_color);
        if (iv != null) {
            viewList.add(iv);
        }
        iv = (ImageView)findViewById(R.id.black_color);
        if (iv != null) {
            viewList.add(iv);
        }
        iv = (ImageView)findViewById(R.id.gray_blue_color);
        if (iv != null) {
            viewList.add(iv);
        }
        iv = (ImageView)findViewById(R.id.pink_color);
        if (iv != null) {
            viewList.add(iv);
        }
        iv = (ImageView)findViewById(R.id.brown_color);
        if (iv != null) {
            viewList.add(iv);
        }
        iv = (ImageView)findViewById(R.id.gray_color);
        if (iv != null) {
            viewList.add(iv);
        }
        iv = (ImageView)findViewById(R.id.custom_color);
        if (iv != null) {
            viewList.add(iv);
        }        

        mCurImageViews = new ImageView[viewList.size()];
        viewList.toArray(mCurImageViews);
        
        setOnClickListeners(mCurImageViews);

        if (mCurSelectedColorViewId > 0) {
            iv = (ImageView)findViewById(mCurSelectedColorViewId);
            if (DBG) {
                Log.d(TAG, "onFinishInflate() selectedViewId=" + Integer.toHexString(mCurSelectedColorViewId)
                        + ", v=" + iv);
            }
            if (iv != null) {
                iv.setBackgroundResource(
                    PrizeNavBarColorUtil.getColorImageResource(mCurSelectedColorViewId, true));

            }
        }
    }

    private void setOnClickListeners(ImageView[] views) {
        if (views != null) {
            for (int i = 0; i < views.length; ++i) {
                ImageView v = views[i];
                v.setOnClickListener(this);
            }
        }
    }
    
    @Override
    public void onClick(View v) {
        handleClicked(v);
    }

    private void changeColorImagesState(View clickedView) {
        if (mCurImageViews != null) {
            for (int i = 0; i < mCurImageViews.length; ++i) {
                ImageView iv = mCurImageViews[i];
                iv.setBackgroundResource(
                    PrizeNavBarColorUtil.getColorImageResource(iv.getId(), false));
            }
        }

        clickedView.setBackgroundResource(
            PrizeNavBarColorUtil.getColorImageResource(clickedView.getId(), true));

    }

    private void handleClicked(View v) {
        if (DBG) {
            Log.d(TAG, "handleClicked() v=" + v);
        }

        mCurSelectedColorViewId = v.getId();

        changeColorImagesState(v);

        if (v.getId() == R.id.custom_color) {
            // we will save color index in onActivityResult();
            Intent intent = new Intent(mContext, PrizeNavBarColorPickerActivity.class);
            startActivityForResult(intent, PrizeNavBarColorUtil.REQUEST_CODE);
            if (DBG) {
                Log.d(TAG, "Start nav bar color picker activity...");
            }
        } else {
            final int color = PrizeNavBarColorUtil.getColor(v.getId());
            setNavBarColor(color);
            final int colorIndex = PrizeNavBarColorUtil.getColorIndex(mCurSelectedColorViewId);
            PrizeNavBarColorUtil.writeColorIndexToSettings(mContext, colorIndex);
            if (DBG) {
                Log.d(TAG, "Write color idx to sharedPref. colorIndex=" + colorIndex
                        + ", viewId=" + Integer.toHexString(mCurSelectedColorViewId));
            }
        }       
    }
    
    private void setNavBarColor(int color) {
        int oldColor = Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.PRIZE_NAV_BAR_BG_COLOR, -1);
        if (DBG) {
            Log.d(TAG, "setNavBarColor() color=0x" + Integer.toHexString(color)
                    + ", oldColor=0x" + Integer.toHexString(oldColor));
        }        
        if (oldColor != color) {
            Settings.System.putInt(mContext.
                getContentResolver(), Settings.System.PRIZE_NAV_BAR_BG_COLOR, color);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (DBG) {
            Log.d(TAG, "onActivityResult(). requestCode=" + requestCode + ", resultCode=" + resultCode
                        + ", data=" + data);
        }

        if (requestCode == PrizeNavBarColorUtil.REQUEST_CODE) {
            if (resultCode == 1) {
                // Set color successfully
                PrizeNavBarColorUtil.writeColorIndexToSettings(
                    mContext, PrizeNavBarColorUtil.CUSTOM_COLOR_IDX);
            } else {
                handleColorIndexChanged();
            }
        }
    }    
}

