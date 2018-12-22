/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.android.systemui.qs;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorListenerAdapter;
import android.annotation.Nullable;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.Animatable;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.android.internal.logging.MetricsLogger;
import com.android.systemui.FontSizeUtils;
import com.android.systemui.R;
import com.android.systemui.qs.QSTile.DetailAdapter;
import com.android.systemui.statusbar.phone.BaseStatusBarHeader;
import com.android.systemui.statusbar.phone.QSTileHost;

/*PRIZE-import pkg-liufan-2016-11-16-start*/
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.WindowManager;
import android.view.Display;
import android.util.DisplayMetrics;
/*PRIZE-import pkg-liufan-2016-11-16-end*/

public class QSDetail extends LinearLayout {

    private static final String TAG = "QSDetail";
    private static final long FADE_DURATION = 300;

    private final SparseArray<View> mDetailViews = new SparseArray<>();

    private ViewGroup mDetailContent;
    private TextView mDetailSettingsButton;
    private TextView mDetailDoneButton;
    private QSDetailClipper mClipper;
    private DetailAdapter mDetailAdapter;
    private QSPanel mQsPanel;

    private View mQsDetailHeader;
    private TextView mQsDetailHeaderTitle;
    private Switch mQsDetailHeaderSwitch;
    private ImageView mQsDetailHeaderProgress;

    private QSTileHost mHost;

    private boolean mScanState;
    private boolean mClosingDetail;
    private boolean mFullyExpanded;
    private BaseStatusBarHeader mHeader;
    private boolean mTriggeredExpand;
    private int mOpenX;
    private int mOpenY;
    private boolean mAnimatingOpen;
    private boolean mSwitchState;

    public QSDetail(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        FontSizeUtils.updateFontSize(mDetailDoneButton, R.dimen.qs_detail_button_text_size);
        FontSizeUtils.updateFontSize(mDetailSettingsButton, R.dimen.qs_detail_button_text_size);

        for (int i = 0; i < mDetailViews.size(); i++) {
            mDetailViews.valueAt(i).dispatchConfigurationChanged(newConfig);
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mDetailContent = (ViewGroup) findViewById(android.R.id.content);
        mDetailSettingsButton = (TextView) findViewById(android.R.id.button2);
        mDetailDoneButton = (TextView) findViewById(android.R.id.button1);

        mQsDetailHeader = findViewById(R.id.qs_detail_header);
        mQsDetailHeaderTitle = (TextView) mQsDetailHeader.findViewById(android.R.id.title);
        mQsDetailHeaderSwitch = (Switch) mQsDetailHeader.findViewById(android.R.id.toggle);
        mQsDetailHeaderProgress = (ImageView) findViewById(R.id.qs_detail_header_progress);

        updateDetailText();

        mClipper = new QSDetailClipper(this);

        final OnClickListener doneListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                announceForAccessibility(
                        mContext.getString(R.string.accessibility_desc_quick_settings));
                mQsPanel.closeDetail();
            }
        };
        mDetailDoneButton.setOnClickListener(doneListener);
        /*PRIZE-init-liufan-2016-11-16-start*/
        init();
        /*PRIZE-init-liufan-2016-11-16-end*/
    }

    public void setQsPanel(QSPanel panel, BaseStatusBarHeader header) {
        mQsPanel = panel;
        mHeader = header;
        mHeader.setCallback(mQsPanelCallback);
        mQsPanel.setCallback(mQsPanelCallback);
    }

    public void setHost(QSTileHost host) {
        mHost = host;
    }
    public boolean isShowingDetail() {
        return mDetailAdapter != null;
    }

    public void setFullyExpanded(boolean fullyExpanded) {
        mFullyExpanded = fullyExpanded;
    }

    public void setExpanded(boolean qsExpanded) {
        if (!qsExpanded) {
            mTriggeredExpand = false;
        }
    }

    private void updateDetailText() {
        mDetailDoneButton.setText(R.string.quick_settings_done);
        mDetailSettingsButton.setText(R.string.quick_settings_more_settings);
    }

    public void updateResources() {
        updateDetailText();
    }

    public boolean isClosingDetail() {
        return mClosingDetail;
    }

    private void handleShowingDetail(final QSTile.DetailAdapter adapter, int x, int y) {
        final boolean showingDetail = adapter != null;
        setClickable(showingDetail);
        if (showingDetail) {
            mQsDetailHeaderTitle.setText(adapter.getTitle());
            final Boolean toggleState = adapter.getToggleState();
            if (toggleState == null) {
                mQsDetailHeaderSwitch.setVisibility(INVISIBLE);
                mQsDetailHeader.setClickable(false);
            } else {
                mQsDetailHeaderSwitch.setVisibility(VISIBLE);
                handleToggleStateChanged(toggleState, adapter.getToggleEnabled());
                mQsDetailHeader.setClickable(true);
                mQsDetailHeader.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        boolean checked = !mQsDetailHeaderSwitch.isChecked();
                        mQsDetailHeaderSwitch.setChecked(checked);
                        adapter.setToggleState(checked);
                    }
                });
            }
            if (!mFullyExpanded) {
                mTriggeredExpand = true;
                mHost.animateToggleQSExpansion();
            } else {
                mTriggeredExpand = false;
            }
            mOpenX = x;
            mOpenY = y;
        } else {
            // Ensure we collapse into the same point we opened from.
            x = mOpenX;
            y = mOpenY;
            if (mTriggeredExpand) {
                mHost.animateToggleQSExpansion();
                mTriggeredExpand = false;
            }
        }

        boolean visibleDiff = (mDetailAdapter != null) != (adapter != null);
        if (!visibleDiff && mDetailAdapter == adapter) return;  // already in right state
        AnimatorListener listener = null;
        if (adapter != null) {
            int viewCacheIndex = adapter.getMetricsCategory();
            View detailView = adapter.createDetailView(mContext, mDetailViews.get(viewCacheIndex),
                    mDetailContent);
            if (detailView == null) throw new IllegalStateException("Must return detail view");

            final Intent settingsIntent = adapter.getSettingsIntent();
            mDetailSettingsButton.setVisibility(settingsIntent != null ? VISIBLE : GONE);
            mDetailSettingsButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mHost.startActivityDismissingKeyguard(settingsIntent);
                }
            });

            mDetailContent.removeAllViews();
            mDetailContent.addView(detailView);
            mDetailViews.put(viewCacheIndex, detailView);
            MetricsLogger.visible(mContext, adapter.getMetricsCategory());
            announceForAccessibility(mContext.getString(
                    R.string.accessibility_quick_settings_detail,
                    adapter.getTitle()));
            mDetailAdapter = adapter;
            listener = mHideGridContentWhenDone;
            setVisibility(View.VISIBLE);
        } else {
            if (mDetailAdapter != null) {
                MetricsLogger.hidden(mContext, mDetailAdapter.getMetricsCategory());
            }
            mClosingDetail = true;
            mDetailAdapter = null;
            listener = mTeardownDetailWhenDone;
            mHeader.setVisibility(View.VISIBLE);
            mQsPanel.setGridContentVisibility(true);
            mQsPanelCallback.onScanStateChanged(false);
        }
        sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED);
        if (visibleDiff) {
            mAnimatingOpen = adapter != null;
            if (mFullyExpanded || mDetailAdapter != null) {
                setAlpha(1);
                mClipper.animateCircularClip(x, y, mDetailAdapter != null, listener);
            } else {
                animate().alpha(0)
                        .setDuration(FADE_DURATION)
                        .setListener(listener)
                        .start();
            }
        }
    }

    private void handleToggleStateChanged(boolean state, boolean toggleEnabled) {
        mSwitchState = state;
        if (mAnimatingOpen) {
            return;
        }
        mQsDetailHeaderSwitch.setChecked(state);
        mQsDetailHeader.setEnabled(toggleEnabled);
        mQsDetailHeaderSwitch.setEnabled(toggleEnabled);
    }

    private void handleScanStateChanged(boolean state) {
        if (mScanState == state) return;
        mScanState = state;
        final Animatable anim = (Animatable) mQsDetailHeaderProgress.getDrawable();
        if (state) {
            mQsDetailHeaderProgress.animate().alpha(1f);
            anim.start();
        } else {
            mQsDetailHeaderProgress.animate().alpha(0f);
            anim.stop();
        }
    }

    private void checkPendingAnimations() {
        handleToggleStateChanged(mSwitchState,
                            mDetailAdapter != null && mDetailAdapter.getToggleEnabled());
    }

    private final QSPanel.Callback mQsPanelCallback = new QSPanel.Callback() {
        @Override
        public void onToggleStateChanged(final boolean state) {
            post(new Runnable() {
                @Override
                public void run() {
                    handleToggleStateChanged(state,
                            mDetailAdapter != null && mDetailAdapter.getToggleEnabled());
                }
            });
        }

        @Override
        public void onShowingDetail(final DetailAdapter detail, final int x, final int y) {
            post(new Runnable() {
                @Override
                public void run() {
                    handleShowingDetail(detail, x, y);
                }
            });
        }

        @Override
        public void onScanStateChanged(final boolean state) {
            post(new Runnable() {
                @Override
                public void run() {
                    handleScanStateChanged(state);
                }
            });
        }
    };

    private final AnimatorListenerAdapter mHideGridContentWhenDone = new AnimatorListenerAdapter() {
        public void onAnimationCancel(Animator animation) {
            // If we have been cancelled, remove the listener so that onAnimationEnd doesn't get
            // called, this will avoid accidentally turning off the grid when we don't want to.
            animation.removeListener(this);
            mAnimatingOpen = false;
            checkPendingAnimations();
        };

        @Override
        public void onAnimationEnd(Animator animation) {
            // Only hide content if still in detail state.
            if (mDetailAdapter != null) {
                mQsPanel.setGridContentVisibility(false);
                mHeader.setVisibility(View.INVISIBLE);
            }
            mAnimatingOpen = false;
            checkPendingAnimations();
        }
    };

    private final AnimatorListenerAdapter mTeardownDetailWhenDone = new AnimatorListenerAdapter() {
        public void onAnimationEnd(Animator animation) {
            mDetailContent.removeAllViews();
            setVisibility(View.INVISIBLE);
            mClosingDetail = false;
        };
    };
	
    /*PRIZE-draw background-liufan-2016-11-16-start*/
    private Bitmap bitmap;
    private Paint paint;
    private Display mDisplay;
    private void init() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setFilterBitmap(true);
        
        WindowManager mWindowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        mDisplay = mWindowManager.getDefaultDisplay();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (this.bitmap != null && !bitmap.isRecycled()) {
            int w = getWidth();
            int h = getHeight();
            if(w == 0 || h == 0){
                invalidate();
                return;
            }
            canvas.save();
            Rect rect = new Rect(0,0,w,h);
            canvas.clipRect(rect);
            DisplayMetrics mDisplayMetrics = new DisplayMetrics();
            mDisplay.getRealMetrics(mDisplayMetrics);
            int screenWidth = mDisplayMetrics.widthPixels ;
            int screenHeight = mDisplayMetrics.heightPixels;
            Rect r = new Rect(0, 0, screenWidth, screenHeight);
            canvas.drawBitmap(bitmap, null, r, paint);
		    canvas.restore();
        }
    }

    public void setBg(Bitmap bitmap) {
        this.bitmap = bitmap;
        invalidate();
    }
    /*PRIZE-draw background-liufan-2016-11-16-end*/
}
