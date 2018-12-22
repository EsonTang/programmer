/*
 * Copyright (C) 2011 The Android Open Source Project
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

package com.android.dialer.calllog;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.provider.CallLog.Calls;
import android.util.AttributeSet;
import android.view.View;

import com.android.contacts.common.testing.NeededForTesting;
import com.android.contacts.common.util.BitmapUtil;
import com.android.dialer.R;
import com.android.dialer.util.AppCompatConstants;
import com.google.common.collect.Lists;
import com.mediatek.dialer.ext.ExtensionManager;

import java.util.List;
/*PRIZE-add picture with simcard id -yuandailin-2015-10-30-start*/
import android.telecom.PhoneAccountHandle;
import android.telephony.SubscriptionManager;
import android.text.TextUtils;
import android.util.Log;
import android.telecom.TelecomManager;
import android.telephony.TelephonyManager;
import android.telecom.PhoneAccount;
/*PRIZE-add picture with simcard id -yuandailin-2015-10-30-start*/

/**
 * View that draws one or more symbols for different types of calls (missed calls, outgoing etc).
 * The symbols are set up horizontally. As this view doesn't create subviews, it is better suited
 * for ListView-recycling that a regular LinearLayout using ImageViews.
 */
/// M: Add mShowvowifi, mShowvolte,mShowViWifi to check enable/disable status of different call
 // types and make width and height public so that it can be updated in plugin for newly added
 // icons
public class CallTypeIconsView extends View {
    private List<Integer> mCallTypes = Lists.newArrayListWithCapacity(1);/*PRIZE-change from 3 to 1-yuandailin-2015-9-9*/
    private boolean mShowVideo = false;
    public int mWidth;
    public int mHeight;
    public boolean mShowvowifi = false;
    public boolean mShowvolte = false;
    public boolean mShowViWifi = false;

    private static Resources sResources;

    /*PRIZE-change-yuandailin-2016-9-21-start*/
    private int mSlotId;
    private int simCount;
    /*PRIZE-change-yuandailin-2016-9-21-start*/

    /*PRIZE-Add-DialerV8-wangzhong-2017_7_19-start*/
    private boolean isPrimaryCallTypeIcon = true;

    public boolean isPrimaryCallTypeIcon() {
        return isPrimaryCallTypeIcon;
    }

    public void setPrimaryCallTypeIcon(boolean primaryCallTypeIcon) {
        isPrimaryCallTypeIcon = primaryCallTypeIcon;
    }
    /*PRIZE-Add-DialerV8-wangzhong-2017_7_19-end*/

    public CallTypeIconsView(Context context) {
        this(context, null);
    }

    public CallTypeIconsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (sResources == null ||
                /** M: need reload resource too when system density changed @{ */
                sResources.density != context.getResources().getDisplayMetrics().density) {
                /** @}*/
            sResources = new Resources(context);
        }
    }

    public void clear() {
        mCallTypes.clear();
        mWidth = 0;
        mHeight = 0;
        invalidate();
    }

    public void add(int callType) {
        mCallTypes.add(callType);

        final Drawable drawable = getCallTypeDrawable(callType);
        //PRIZE-add-yuandailin-2016-3-17
        /*mWidth += drawable.getIntrinsicWidth() + sResources.iconMargin;
        mHeight = Math.max(mHeight, drawable.getIntrinsicHeight());*/
        if (drawable!=null) {
            mWidth += drawable.getIntrinsicWidth() + sResources.iconMargin;
            mHeight = Math.max(mHeight, drawable.getIntrinsicHeight());
        }
        //PRIZE-add-yuandailin-2016-3-17
        invalidate();
    }

    /**
     * Determines whether the video call icon will be shown.
     *
     * @param showVideo True where the video icon should be shown.
     */
    public void setShowVideo(boolean showVideo) {
        mShowVideo = showVideo;
        if (showVideo) {
            mWidth += sResources.videoCall.getIntrinsicWidth();
            mHeight = Math.max(mHeight, sResources.videoCall.getIntrinsicHeight());
            invalidate();
        }
    }

    /*PRIZE-change-yuandailin-2016-9-21-start*/
    public void setSlotIdAndSimCount(int slotId, int mSimCount) {
        mSlotId = slotId;
        simCount = mSimCount;
    }
    /*PRIZE-change-yuandailin-2016-9-5-end*/

    /**
     * Determines if the video icon should be shown.
     *
     * @return True if the video icon should be shown.
     */
    public boolean isVideoShown() {
        return mShowVideo;
    }

    @NeededForTesting
    public int getCount() {
        return mCallTypes.size();
    }

    @NeededForTesting
    public int getCallType(int index) {
        return mCallTypes.get(index);
    }

    private Drawable getCallTypeDrawable(int callType) {
        /*PRIZE-Add-DialerV8-wangzhong-2017_7_19-start*/
        if (isPrimaryCallTypeIcon()) {
            switch (callType) {
                case AppCompatConstants.CALLS_INCOMING_TYPE:
                    return sResources.incoming;
                case AppCompatConstants.CALLS_OUTGOING_TYPE:
                    return sResources.outgoing;
                case AppCompatConstants.CALLS_MISSED_TYPE:
                    return sResources.missed;
                case AppCompatConstants.CALLS_VOICEMAIL_TYPE:
                    return sResources.voicemail;
                case Calls.AUTO_REJECT_TYPE:
                    return sResources.autorejected;
                case AppCompatConstants.CALLS_BLOCKED_TYPE:
                    return sResources.blocked;
                default:
                    return sResources.missed;
            }
        } else {
            if (simCount > 1) {
                if (0 == mSlotId) {
                    return sResources.prize_simcard_one;
                } else if (1 == mSlotId) {
                    return sResources.prize_simcard_two;
                }
            } else {
                return null;
            }
        }
        return null;
        /*PRIZE-Add-DialerV8-wangzhong-2017_7_19-end*/

        /*PRIZE-change-yuandailin-2016-9-21-start*/
        /*if (simCount > 1) {
            if (0 == mSlotId) {
                switch (callType) {
                case AppCompatConstants.CALLS_INCOMING_TYPE:
                    return sResources.incoming1;
                case AppCompatConstants.CALLS_OUTGOING_TYPE:
                    return sResources.outgoing1;
                case AppCompatConstants.CALLS_MISSED_TYPE:
                    return sResources.missed1;
                case AppCompatConstants.CALLS_VOICEMAIL_TYPE:
                    return sResources.voicemail;
                *//*case AppCompatConstants.CALLS_AUTO_REJECT_TYPE:
                    return sResources.autorejected;*//*
                default:
                    return sResources.missed1;
                }
            } else if (1 == mSlotId) {
                switch (callType) {
                case AppCompatConstants.CALLS_INCOMING_TYPE:
                    return sResources.incoming2;
                case AppCompatConstants.CALLS_OUTGOING_TYPE:
                    return sResources.outgoing2;
                case AppCompatConstants.CALLS_MISSED_TYPE:
                    return sResources.missed2;
                case AppCompatConstants.CALLS_VOICEMAIL_TYPE:
                    return sResources.voicemail;
                *//*case AppCompatConstants.CALLS_AUTO_REJECT_TYPE:
                    return sResources.autorejected;*//*
                default:
                    return sResources.missed2;
                }
            }
        } else {
        switch (callType) {
            case AppCompatConstants.CALLS_INCOMING_TYPE:
                return sResources.incoming;
            case AppCompatConstants.CALLS_OUTGOING_TYPE:
                return sResources.outgoing;
            case AppCompatConstants.CALLS_MISSED_TYPE:
                return sResources.missed;
            case AppCompatConstants.CALLS_VOICEMAIL_TYPE:
                return sResources.voicemail;
            /// M: for OP01 AutoRejct @{
            case Calls.AUTO_REJECT_TYPE:
                *//*PRIZE-change-yuandailin-2016-3-17-start*//*
                *//*return ExtensionManager.getInstance().getCallLogExtension().
                    getAutoRejectDrawable();*//*
                return sResources.autorejected;
                *//*PRIZE-change-yuandailin-2016-3-17-end*//*
            /// @}
            case AppCompatConstants.CALLS_BLOCKED_TYPE:
                return sResources.blocked;
            default:
                // It is possible for users to end up with calls with unknown call types in their
                // call history, possibly due to 3rd party call log implementations (e.g. to
                // distinguish between rejected and missed calls). Instead of crashing, just
                // assume that all unknown call types are missed calls.
                return sResources.missed;
        }
        }
        return sResources.missed;*/
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(mWidth, mHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int left = 0;
        /*PRIZE-change for video call-yuandailin-2016-7-6-start*/
        /*for (Integer callType : mCallTypes) {
            final Drawable drawable = getCallTypeDrawable(callType);
            final int right = left + drawable.getIntrinsicWidth();
            drawable.setBounds(left, 0, right, drawable.getIntrinsicHeight());
            drawable.draw(canvas);
            left = right + sResources.iconMargin;
        }*/
        /*PRIZE-add picture with simcard id -yuandailin-2015-10-30-start*/
        Integer callType = mCallTypes.get(0);
        final Drawable drawable1 = getCallTypeDrawable(callType);
        int rightForVideoIco =0;
        if(drawable1!=null){
            final int right1 = left + drawable1.getIntrinsicWidth();
            rightForVideoIco = right1;
            drawable1.setBounds(left, 0, right1, drawable1.getIntrinsicHeight());
            drawable1.draw(canvas);
        }
        /*PRIZE-add picture with simcard id -yuandailin-2015-10-30-start*/

        // If showing the video call icon, draw it scaled appropriately.
        if (mShowVideo) {
            final Drawable drawable = sResources.videoCall;
            /*final int right = left + sResources.videoCall.getIntrinsicWidth();
            drawable.setBounds(left, 0, right, sResources.videoCall.getIntrinsicHeight());*/
            final int right = rightForVideoIco + sResources.videoCall.getIntrinsicWidth();
            drawable.setBounds(rightForVideoIco, 0, right, sResources.videoCall.getIntrinsicHeight());
            /*PRIZE-change for video call-yuandailin-2016-7-6-end*/
            drawable.draw(canvas);
        }
        ///M: Plug-in call to draw Canvas for different icons ViWifi, VoLTE, VoWifi
        ExtensionManager.getInstance().getCallLogExtension().drawWifiVolteCanvas(
                        left, canvas, this);
    }

    /// M: [CallLog Incoming and Outgoing Filter]
    public static class Resources {
        /*PRIZE-add picture with simcard id -yuandailin-2015-10-30-start*/
        // Drawable representing an incoming answered call.
        public final Drawable incoming;
        public final Drawable incoming1;
        public final Drawable incoming2;

        // Drawable respresenting an outgoing call.
        public final Drawable outgoing;
        public final Drawable outgoing1;
        public final Drawable outgoing2;

        // Drawable representing an incoming missed call.
        public final Drawable missed;
        public final Drawable missed1;
        public final Drawable missed2;
        /*PRIZE-add picture with simcard id -yuandailin-2015-10-30-end*/
        // Drawable representing a voicemail.
        public final Drawable voicemail;
        public final Drawable autorejected;
        // Drawable representing a blocked call.
        public final Drawable blocked;

        //  Drawable repesenting a video call.
        public final Drawable videoCall;

        /*PRIZE-Add-DialerV8-wangzhong-2017_7_19-start*/
        public final Drawable prize_simcard_one;
        public final Drawable prize_simcard_two;
        /*PRIZE-Add-DialerV8-wangzhong-2017_7_19-end*/

        /**
         * The margin to use for icons.
         */
        public final int iconMargin;

        /**
         * M: The density of current resource.
         * Need reload Resource when density changed.
         */
        public float density = 0;

        /**
         * Configures the call icon drawables.
         * A single white call arrow which points down and left is used as a basis for all of the
         * call arrow icons, applying rotation and colors as needed.
         *
         * @param context The current context.
         */
        public Resources(Context context) {
            final android.content.res.Resources r = context.getResources();

            /*PRIZE-add picture with simcard id -yuandailin-2015-10-30-start*/
            //incoming = r.getDrawable(R.drawable.ic_call_arrow);
            //incoming.setColorFilter(r.getColor(R.color.answered_call), PorterDuff.Mode.MULTIPLY);
            incoming = r.getDrawable(R.drawable.prize_calllog_item_incoming);
            incoming1 = r.getDrawable(R.drawable.prize_incoming_call_sim1_pic);
            incoming2 = r.getDrawable(R.drawable.prize_incoming_call_sim2_pic);


            // Create a rotated instance of the call arrow for outgoing calls.
            //outgoing = BitmapUtil.getRotatedDrawable(r, R.drawable.ic_call_arrow, 180f);
            //outgoing.setColorFilter(r.getColor(R.color.answered_call), PorterDuff.Mode.MULTIPLY);
            outgoing = r.getDrawable(R.drawable.prize_calllog_item_outgoing);
            outgoing1 = r.getDrawable(R.drawable.prize_outgoing_call_sim1_pic);
            outgoing2 = r.getDrawable(R.drawable.prize_outgoing_call_sim2_pic);

            // Need to make a copy of the arrow drawable, otherwise the same instance colored
            // above will be recolored here.
            //missed = r.getDrawable(R.drawable.ic_call_arrow).mutate();
            //missed.setColorFilter(r.getColor(R.color.missed_call), PorterDuff.Mode.MULTIPLY);
            missed = r.getDrawable(R.drawable.prize_calllog_item_missed).mutate();
            missed1 = r.getDrawable(R.drawable.prize_missing_call_sim1_pic).mutate();
            missed2 = r.getDrawable(R.drawable.prize_missing_call_sim2_pic).mutate();

            voicemail = r.getDrawable(R.drawable.ic_call_voicemail_holo_dark);

            autorejected = r.getDrawable(R.drawable.prize_calllog_item_autorejected);
            /*PRIZE-add picture with simcard id -yuandailin-2015-10-30-end*/

            blocked = getScaledBitmap(context, R.drawable.ic_block_24dp);
            blocked.setColorFilter(r.getColor(R.color.blocked_call), PorterDuff.Mode.MULTIPLY);

            videoCall = getScaledBitmap(context, R.drawable.prize_calllog_item_videocall);
            /*prize-delete-huangliemin-2016-7-7-start*/
            /*videoCall.setColorFilter(r.getColor(R.color.dialtacts_secondary_text_color),
                    PorterDuff.Mode.MULTIPLY);*/
            /*prize-delete-huangliemin-2016-7-7-end*/

            /*PRIZE-Add-DialerV8-wangzhong-2017_7_19-start*/
            prize_simcard_one = r.getDrawable(R.drawable.prize_simcard_one);
            prize_simcard_two = r.getDrawable(R.drawable.prize_simcard_two);
            /*PRIZE-Add-DialerV8-wangzhong-2017_7_19-end*/

            ///M: Plug-in call to draw different icons ViWifi, VoLTE, VoWifi
            ExtensionManager.getInstance().getCallLogExtension().drawWifiVolteCallIcon(
                        context.getResources().getDimensionPixelSize(R.dimen.call_type_icon_size));

            iconMargin = r.getDimensionPixelSize(R.dimen.call_log_icon_margin);
            /// M: Tag used to reload resource when density changed.
            density = context.getResources().getDisplayMetrics().density;
        }

        // Gets the icon, scaled to the height of the call type icons. This helps display all the
        // icons to be the same height, while preserving their width aspect ratio.
        private Drawable getScaledBitmap(Context context, int resourceId) {
            Bitmap icon = BitmapFactory.decodeResource(context.getResources(), resourceId);
            int scaledHeight =
                    context.getResources().getDimensionPixelSize(R.dimen.call_type_icon_size);
            int scaledWidth = (int) ((float) icon.getWidth()
                    * ((float) scaledHeight / (float) icon.getHeight()));
            Bitmap scaledIcon = Bitmap.createScaledBitmap(icon, scaledWidth, scaledHeight, false);
            return new BitmapDrawable(context.getResources(), scaledIcon);
        }
    }
}
