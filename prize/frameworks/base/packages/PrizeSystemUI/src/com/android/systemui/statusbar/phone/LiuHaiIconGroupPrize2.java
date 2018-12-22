/*
 * Copyright (C) 2008 The Android Open Source Project
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
 * limitations under the License.
 */

package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import com.android.systemui.statusbar.phone.LiuHaiStatusBarIconController2.LiuHaiStatusBarIconData;
import java.util.ArrayList;

import com.android.systemui.R;

public class LiuHaiIconGroupPrize2 extends LinearLayout {
    private static final String TAG = "LiuHaiIconGroupPrize";

    public LiuHaiIconGroupPrize2(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    private boolean isLimitShow = false;
    public void setLimitShow(boolean limit){
        isLimitShow = limit;
        debugLiuHai("setLimitShow isLimitShow = " + isLimitShow);
    }

    private LiuHaiStatusBarIconController2 mLiuHaiStatusBarIconController;
    public void setLiuHaiStatusBarIconController2(LiuHaiStatusBarIconController2 controller){
        mLiuHaiStatusBarIconController = controller;
    }

    private void debugLiuHai(String msg){
        if(mLiuHaiStatusBarIconController != null){
            mLiuHaiStatusBarIconController.debugLiuHai(msg);
        }
    }

    private ArrayList<LiuHaiStatusBarIconData> viewList = new ArrayList<LiuHaiStatusBarIconData>();
    public void addView(LiuHaiStatusBarIconData data, int index){
        debugLiuHai("addView = " + data.slot + ", index = " + index);
        addView(data.view, index);
        viewList.add(data);
        requestLayout();
    }

    public void removeView(LiuHaiStatusBarIconData data){
        debugLiuHai("removeView = " + data.slot);
        removeView(data.view);
        viewList.remove(data);
        requestLayout();
    }

    private LiuHaiStatusBarIconData visibleData;
    public void setVisibleData(LiuHaiStatusBarIconData data){
        visibleData = data;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if(isLimitShow) checkOverflow(r - l);
    }

    private void checkOverflow(int width) {
        final int N = getChildCount();
        boolean isFull = false;
        int sumW = 0;
        debugLiuHai("checkOverflow width = " + width);
        if(visibleData != null){
            int viewW = visibleData.viewWidth;
            final View view = visibleData.view;
            if(width - sumW < viewW){
                if(view.getVisibility() != View.GONE){
                    post(new Runnable() {
                        @Override
                        public void run() {
                            view.setVisibility(View.GONE);
                            debugLiuHai("hide visibleData.slot = " + visibleData.slot
                                + " view, width = " + view.getWidth());
                        }
                    });
                }
            } else {
                if(view.getVisibility() != View.VISIBLE){
                    post(new Runnable() {
                        @Override
                        public void run() {
                            view.setVisibility(View.VISIBLE);
                        }
                    });
                }
                sumW += viewW;
            }
        }
        for (int i = N-1; i >= 0; i--) {
            final View view = getChildAt(i);
            final LiuHaiStatusBarIconData data = getViewData(view);
            if(visibleData == data){
                continue;
            }
            debugLiuHai("data.slot = " + data.slot + " view.width = " + view.getWidth());
            if(data != null){
                int vW = data.viewWidth;
                if(width - sumW < vW){
                    if(view.getVisibility() != View.GONE){
                        post(new Runnable() {
                            @Override
                            public void run() {
                                view.setVisibility(View.GONE);
                                debugLiuHai("hide data.slot = " + data.slot + " view, width = " + view.getWidth());
                            }
                        });
                    }
                } else {
                    if(view.getVisibility() != View.VISIBLE){
                        post(new Runnable() {
                            @Override
                            public void run() {
                                view.setVisibility(View.VISIBLE);
                                if(mLiuHaiStatusBarIconController != null){
                                    mLiuHaiStatusBarIconController.inverseView(data);
                                }
                            }
                        });
                    }
                    sumW += vW;
                }
            }
        }
    }

    private LiuHaiStatusBarIconData getViewData(View view){
        for(LiuHaiStatusBarIconData data : viewList){
            if(data.view == view){
                return data;
            }
        }
        return null;
    }
}
