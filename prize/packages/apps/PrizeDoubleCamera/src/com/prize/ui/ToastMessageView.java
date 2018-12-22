package com.prize.ui;

import android.app.Activity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.camera.CameraActivity;
import com.android.camera.R;
import com.android.camera.manager.ModePicker;
import com.mediatek.camera.ui.CameraView;
import com.mediatek.camera.util.Log;


/**
 * Created by prize on 2017/10/10.
 */

public class ToastMessageView extends CameraView {
    private static final String TAG = "ToastMessageView";
    private String mText;
    private TextView mToastMessageView;
    private int margin = 0;
    private int lastMarginBottom = 170;
    private static final int ROTATIONANGLE_90 = 90;
    private static final int ROTATIONANGLE_270 = 270;
    private int mPreviewheight;

    public ToastMessageView(Activity activity) {
        super(activity);
        mPreviewheight = (int) mActivity.getResources().getDimension(R.dimen.info_bottom);
        Log.i(TAG, "[CsView]constructor...");
    }

/*    @Override
    public boolean update(int type, Object... args) {
        Log.d(TAG, "[update]text = " + mText);
        margin = mPreviewheight;
        mText = (String) args[0];
        show();
        return true;
    }*/

    public boolean updateText(int type, int margin, Object... args) {
        this.margin = margin;
        mText = (String) args[0];
        Log.d(TAG, "[updateText]text = " + mText);
        show();
        return true;
    }

    @Override
    public void refresh() {
        Log.i(TAG, "[refresh]...margin: " + margin +",mPreviewheight: "+mPreviewheight+",lastMarginBottom: "+lastMarginBottom+",Orientation: "+getOrientation());
        if (mToastMessageView != null) {
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) mToastMessageView.getLayoutParams();
            Log.d(TAG, "left:" + mToastMessageView.getLeft() + "top: " + mToastMessageView.getTop() + "right: " + mToastMessageView.getRight() + ",bottom: " + mToastMessageView.getBottom());
            layoutParams.bottomMargin = mPreviewheight - margin;
            if (((CameraActivity) mActivity).getCurrentMode() == ModePicker.MODE_FACE_BEAUTY || ((CameraActivity) mActivity).getCurrentMode() == ModePicker.MODE_PORTRAIT) {
                if (getOrientation() == ROTATIONANGLE_90 || getOrientation() == ROTATIONANGLE_270) {
                    lastMarginBottom = layoutParams.bottomMargin;
                    layoutParams.bottomMargin = 0;
                    
                } else {
                    layoutParams.bottomMargin = lastMarginBottom;
                }
            }
            mToastMessageView.setLayoutParams(layoutParams);
            mToastMessageView.setText(mText);
            mToastMessageView.setVisibility(View.VISIBLE);

        }

    }


    @Override
    protected View getView() {
        View view = inflate(R.layout.toast_message);
        mToastMessageView = (TextView) view.findViewById(R.id.toast_message_view);
        return view;
    }

}
