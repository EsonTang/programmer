/*
 * Copyright (C) 2014 The Android Open Source Project
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
 * limitations under the License
 */

package com.android.systemui.statusbar;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.view.View;
/*PRIZE-import pkg- liufan-2015-05-14-start*/
import com.android.systemui.statusbar.phone.FeatureOption;
import android.widget.FrameLayout;
import android.view.Gravity;
import android.content.res.Resources;
/*PRIZE-import pkg- liufan-2015-05-14-end*/

import com.android.systemui.R;

public class DismissView extends StackScrollerDecorView {
    private DismissViewButton mDismissButton;

    public DismissView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected View findContentView() {
        return findViewById(R.id.dismiss_text);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mDismissButton = (DismissViewButton) findContentView();
        /*PRIZE-set dismiss view background- liufan-2015-05-14-start*/
        if(FeatureOption.PRIZE_QS_SORT){
            FrameLayout.LayoutParams params =(FrameLayout.LayoutParams) mDismissButton.getLayoutParams();
            params.gravity = Gravity.CENTER;
            mDismissButton.setLayoutParams(params);
            mDismissButton.setBackgroundResource(R.drawable.qs_clear_notification_btn);
        }
        /*PRIZE-set dismiss view background- liufan-2015-05-14-end*/
    }

    public void setOnButtonClickListener(OnClickListener listener) {
        mContent.setOnClickListener(listener);
    }

    public boolean isOnEmptySpace(float touchX, float touchY) {
        return touchX < mContent.getX()
                || touchX > mContent.getX() + mContent.getWidth()
                || touchY < mContent.getY()
                || touchY > mContent.getY() + mContent.getHeight();
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        /*PRIZE-don't setText- liufan-2016-11-07-start*/
        //mDismissButton.setText(R.string.clear_all_notifications_text);
        /*PRIZE-don't setText- liufan-2016-11-07-end*/
        mDismissButton.setContentDescription(
                mContext.getString(R.string.accessibility_clear_all));
    }

    public boolean isButtonVisible() {
        return mDismissButton.getAlpha() != 0.0f;
    }
}
