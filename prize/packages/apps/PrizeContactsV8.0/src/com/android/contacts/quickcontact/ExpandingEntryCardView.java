/*
* Copyright (C) 2014 MediaTek Inc.
* Modification based on code covered by the mentioned copyright
* and/or permission notice(s).
*/
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
 * limitations under the License.
 */
package com.android.contacts.quickcontact;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.Spannable;
import android.text.TextUtils;
import android.transition.ChangeBounds;
import android.transition.Fade;
import android.transition.Transition;
import android.transition.Transition.TransitionListener;
import android.transition.TransitionManager;
import android.transition.TransitionSet;
import android.util.AttributeSet;
import android.util.Property;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.contacts.R;
import com.android.contacts.common.dialog.CallSubjectDialog;
import com.android.contacts.prize.PrizeQuickContactDataManager;//prize-add for dido os8.0-hpf-2017-8-9
import com.mediatek.contacts.util.Log;

import java.util.ArrayList;
import java.util.List;
import com.mediatek.telecom.TelecomManagerEx;

import android.os.SystemProperties;    //prize adaptation big-font by zhaojian 20180601
/**
 * Display entries in a LinearLayout that can be expanded to show all entries.
 */
public class ExpandingEntryCardView extends CardView {

    private static final String TAG = "ExpandingEntryCardView";
    private static final int DURATION_EXPAND_ANIMATION_FADE_IN = 200;
    private static final int DURATION_COLLAPSE_ANIMATION_FADE_OUT = 75;
    private static final int DELAY_EXPAND_ANIMATION_FADE_IN = 100;

    public static final int DURATION_EXPAND_ANIMATION_CHANGE_BOUNDS = 300;
    public static final int DURATION_COLLAPSE_ANIMATION_CHANGE_BOUNDS = 300;
    
    /*prize-add-huangliemin-2016-7-16-start*/
    public /*static*/ boolean isContactEditable = false;//prize-remove static -huangpengfei-2016-9-26
    public /*static*/ boolean isContactCard = false;//prize-remove static -huangpengfei-2016-9-26
    public /*static*/ String FirstNumber;//prize-remove static -huangpengfei-2016-9-26
    public /*static*/ ArrayList<TextView> mHeaderList = new ArrayList<TextView>();//prize-remove static -huangpengfei-2016-9-26
    private TelephonyManager mTelephonyManager;
    private TelecomManager mTelecomManager;
    /*prize-add for dido os8.0 -hpf-2017-8-14-start*/
    private Context mContext;
    private int mContentMarginTop;
    /*prize-add for dido os8.0 -hpf-2017-8-14-end*/
    
    //prize-remove static -huangpengfei-2016-9-26
    public /*static*/ void setFirstNumber(String number, boolean editable, boolean isCall) {
    	isContactEditable = editable;
    	if(editable && number!=null && mHeaderList.size()>0) {
    		for(int i=0;i<mHeaderList.size();i++) {
    			Log.i("logtest", "mHeader: "+mHeaderList.get(i).getText() + " : "+number);
    			String phoneNumber = mHeaderList.get(i).getText().toString().trim();
    			phoneNumber = phoneNumber.replace(" ", "");
    			Log.i("logtest", "set2: "+(phoneNumber.equals(number.trim())));
    			if(phoneNumber.equals(number.trim())) {
    				mHeaderList.get(i).setTextColor(mContext.getResources().getColor(R.color.prize_theme_color));
    				break;
    			}
    		}
    		if(isCall) {
    			mHeaderList.clear();
    		}
    	}
    }
    
    //prize-remove static -huangpengfei-2016-9-26
    public /*static*/ void setIsContactCard(boolean iscontactcard) {
    	isContactCard = iscontactcard;
    }
    /*prize-add-huangliemin-2016-7-16-end*/

    private static final Property<View, Integer> VIEW_LAYOUT_HEIGHT_PROPERTY =
            new Property<View, Integer>(Integer.class, "height") {
                @Override
                public void set(View view, Integer height) {
                    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)
                            view.getLayoutParams();
                    params.height = height;
                    view.setLayoutParams(params);
                }

                @Override
                public Integer get(View view) {
                    return view.getLayoutParams().height;
                }
            };

    /**
     * Entry data.
     */
    public static final class Entry {
        // No action when clicking a button is specified.
        public static final int ACTION_NONE = 1;
        // Button action is an intent.
        public static final int ACTION_INTENT = 2;
        // Button action will open the call with subject dialog.
        public static final int ACTION_CALL_WITH_SUBJECT = 3;

        private final int mId;
        private final Drawable mIcon;
        private final String mHeader;
        private final String mSubHeader;
        private final Drawable mSubHeaderIcon;
        private final String mText;
        private final Drawable mTextIcon;
        /* M: add sim icon @ { */
        private final Drawable mSimIcon;
        private final String mSimName;
        /* @ } */
        private Spannable mPrimaryContentDescription;
        private final Intent mIntent;
        private final Drawable mAlternateIcon;
        private final Intent mAlternateIntent;
        private Spannable mAlternateContentDescription;
        private final boolean mShouldApplyColor;
        private final boolean mIsEditable;
        private final EntryContextMenuInfo mEntryContextMenuInfo;
        private final Drawable mThirdIcon;
        private final Intent mThirdIntent;
        private final String mThirdContentDescription;
        private final int mIconResourceId;
        private final int mThirdAction;
        private final Bundle mThirdExtras;
        /*PRIZE-add call duration -huangliemin-2016-5-27 -start*/
        private String mCallDuration = null;
        /*PRIZE-add call duration -huangliemin-2016-5-27 -end*/
        
    
        /*PRIZE-add-huangpengfei-2016-10-26 -start*/
        public Entry(int id, Drawable mainIcon, String header, String subHeader,
                Drawable subHeaderIcon, String text, Drawable textIcon,
                /* M: add sim icon */Drawable simIcon, /* M: add sim name */String simName,
                Spannable primaryContentDescription, Intent intent, Drawable alternateIcon,
                Intent alternateIntent, Spannable alternateContentDescription,
                boolean shouldApplyColor, boolean isEditable,
                EntryContextMenuInfo entryContextMenuInfo, Drawable thirdIcon, Intent thirdIntent,
                String thirdContentDescription, int thirdAction, Bundle thirdExtras,
                int iconResourceId,String callDuration) {
        		
        	this(id,mainIcon,header,subHeader,
        			subHeaderIcon,text,textIcon,
        			simIcon,simName,
        			primaryContentDescription,intent,alternateIcon,
        			alternateIntent,alternateContentDescription,
        			shouldApplyColor,isEditable,
        			entryContextMenuInfo,thirdIcon,thirdIntent,
        			thirdContentDescription,thirdAction,thirdExtras,
        			iconResourceId);
        	mCallDuration = callDuration;
        	
        }
        
        /*PRIZE-add-huangpengfei-2016-10-26 -end*/

        public Entry(int id, Drawable mainIcon, String header, String subHeader,
                Drawable subHeaderIcon, String text, Drawable textIcon,
                Spannable primaryContentDescription, Intent intent,
                Drawable alternateIcon, Intent alternateIntent,
                Spannable alternateContentDescription, boolean shouldApplyColor, boolean isEditable,
                EntryContextMenuInfo entryContextMenuInfo, Drawable thirdIcon, Intent thirdIntent,
                String thirdContentDescription, int thirdAction, Bundle thirdExtras,
                int iconResourceId) {
            this(id, mainIcon, header, subHeader, subHeaderIcon, text, textIcon, null, null,
                        primaryContentDescription, intent, alternateIcon,
                        alternateIntent, alternateContentDescription, shouldApplyColor, isEditable,
                        entryContextMenuInfo, thirdIcon, thirdIntent, thirdContentDescription,
                        thirdAction,  thirdExtras, iconResourceId);  
        }

        public Entry(int id, Drawable mainIcon, String header, String subHeader,
                Drawable subHeaderIcon, String text, Drawable textIcon,
                /* M: add sim icon */Drawable simIcon, /* M: add sim name */String simName,
                Spannable primaryContentDescription, Intent intent, Drawable alternateIcon,
                Intent alternateIntent, Spannable alternateContentDescription,
                boolean shouldApplyColor, boolean isEditable,
                EntryContextMenuInfo entryContextMenuInfo, Drawable thirdIcon, Intent thirdIntent,
                String thirdContentDescription, int thirdAction, Bundle thirdExtras,
                int iconResourceId) {
        	
        	Log.d(TAG, "[Entry]		header = "+ header + "subHeader = "+subHeader);
            mId = id;
            mIcon = mainIcon;
            mHeader = header;
            mSubHeader = subHeader;
            mSubHeaderIcon = subHeaderIcon;
            mText = text;
            mTextIcon = textIcon;
            /* M: add sim icon & sim name @ { */
            mSimIcon = simIcon;
            mSimName = simName;
            /* M: @ } */
            mPrimaryContentDescription = primaryContentDescription;
            mIntent = intent;
            mAlternateIcon = alternateIcon;
            mAlternateIntent = alternateIntent;
            mAlternateContentDescription = alternateContentDescription;
            mShouldApplyColor = shouldApplyColor;
            mIsEditable = isEditable;
            mEntryContextMenuInfo = entryContextMenuInfo;
            mThirdIcon = thirdIcon;
            mThirdIntent = thirdIntent;
            mThirdContentDescription = thirdContentDescription;
            mThirdAction = thirdAction;
            mThirdExtras = thirdExtras;
            mIconResourceId = iconResourceId;
        }

        Drawable getIcon() {
            return mIcon;
        }

        public String getHeader() {
            return mHeader;
        }

        String getSubHeader() {
            return mSubHeader;
        }

        Drawable getSubHeaderIcon() {
            return mSubHeaderIcon;
        }

        public String getText() {
            return mText;
        }

        Drawable getTextIcon() {
            return mTextIcon;
        }

        /* M: add sim icon @ { */
        Drawable getSimIcon() {
            return mSimIcon;
        }

        public String getSimName() {
            return mSimName;
        }
        /* M: @ } */

        Spannable getPrimaryContentDescription() {
            return mPrimaryContentDescription;
        }

        Intent getIntent() {
            return mIntent;
        }

        Drawable getAlternateIcon() {
            return mAlternateIcon;
        }

        Intent getAlternateIntent() {
            return mAlternateIntent;
        }

        Spannable getAlternateContentDescription() {
            return mAlternateContentDescription;
        }

        boolean shouldApplyColor() {
            return mShouldApplyColor;
        }

        boolean isEditable() {
            return mIsEditable;
        }

        int getId() {
            return mId;
        }

        EntryContextMenuInfo getEntryContextMenuInfo() {
            return mEntryContextMenuInfo;
        }

        Drawable getThirdIcon() {
            return mThirdIcon;
        }

        Intent getThirdIntent() {
            return mThirdIntent;
        }

        String getThirdContentDescription() {
            return mThirdContentDescription;
        }

        int getIconResourceId() {
            return mIconResourceId;
        }

        public int getThirdAction() {
            return mThirdAction;
        }

        public Bundle getThirdExtras() {
            return mThirdExtras;
        }
        public String getCallDuration(){
      			return mCallDuration;
        }
            
    }
        

    public interface ExpandingEntryCardViewListener {
        void onCollapse(int heightDelta);
        void onExpand();
        void onExpandDone();
    }

    private View mExpandCollapseButton;
    private TextView mExpandCollapseTextView;
    private TextView mTitleTextView;
    private CharSequence mExpandButtonText;
    private CharSequence mCollapseButtonText;
    private OnClickListener mOnClickListener;
    private OnCreateContextMenuListener mOnCreateContextMenuListener;
    private boolean mIsExpanded = false;
    /**
     * The max number of entries to show in a collapsed card. If there are less entries passed in,
     * then they are all shown.
     */
    private int mCollapsedEntriesCount;
    private ExpandingEntryCardViewListener mListener;
    private List<List<Entry>> mEntries;
    private int mNumEntries = 0;
    private boolean mAllEntriesInflated = false;
    private List<List<View>> mEntryViews;
    private LinearLayout mEntriesViewGroup;
    private final ImageView mExpandCollapseArrow;
    private int mThemeColor;
    private ColorFilter mThemeColorFilter;
    /**
     * Whether to prioritize the first entry type. If prioritized, we should show at least two
     * of this entry type.
     */
    private boolean mShowFirstEntryTypeTwice;
    private boolean mIsAlwaysExpanded;
    /** The ViewGroup to run the expand/collapse animation on */
    private ViewGroup mAnimationViewGroup;
    private LinearLayout mBadgeContainer;
    private final List<ImageView> mBadges;
    private final List<Integer> mBadgeIds;
    private final int mDividerLineHeightPixels;
    /**
     * List to hold the separators. This saves us from reconstructing every expand/collapse and
     * provides a smoother animation.
     */
    private List<View> mSeparators;
    private LinearLayout mContainer;

    private final OnClickListener mExpandCollapseButtonListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mIsExpanded) {
                collapse();
            } else {
                expand();
            }
        }
    };

    public ExpandingEntryCardView(Context context) {
        this(context, null);
    }

    public ExpandingEntryCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater inflater = LayoutInflater.from(context);
        View expandingEntryCardView = inflater.inflate(R.layout.expanding_entry_card_view, this);
        
        /*prize-add for dido os 8.0-hpf-2017-7-25-start*/
        this.mContext = context;
        mContentMarginTop = (int)context.getResources().getDimension(R.dimen.prize_card_layout_padding_top);
        /*prize-add for dido os 8.0-hpf-2017-7-25-end*/
        
        mEntriesViewGroup = (LinearLayout)
                expandingEntryCardView.findViewById(R.id.content_area_linear_layout);
        mTitleTextView = (TextView) expandingEntryCardView.findViewById(R.id.title);
        mContainer = (LinearLayout) expandingEntryCardView.findViewById(R.id.container);
        /*prize-add-huangliemin-2016-7-23-start*/
        mTelephonyManager = (TelephonyManager)getContext().getSystemService(Context.TELEPHONY_SERVICE);
		mTelecomManager = (TelecomManager)getContext().getSystemService(Context.TELECOM_SERVICE);
        /*prize-add-huangliemin-2016-7-23-end*/

        mExpandCollapseButton = inflater.inflate(
                R.layout.quickcontact_expanding_entry_card_button, this, false);
        mExpandCollapseTextView = (TextView) mExpandCollapseButton.findViewById(R.id.text);
        mExpandCollapseArrow = (ImageView) mExpandCollapseButton.findViewById(R.id.arrow);
        mExpandCollapseButton.setOnClickListener(mExpandCollapseButtonListener);
        mBadgeContainer = (LinearLayout) mExpandCollapseButton.findViewById(R.id.badge_container);
        mDividerLineHeightPixels = getResources()
                .getDimensionPixelSize(R.dimen.divider_line_height);

        mBadges = new ArrayList<ImageView>();
        mBadgeIds = new ArrayList<Integer>();
    }

    public void initialize(List<List<Entry>> entries, int numInitialVisibleEntries,
            boolean isExpanded, boolean isAlwaysExpanded, ExpandingEntryCardViewListener listener,
            ViewGroup animationViewGroup,boolean isRecentsCard) {
        initialize(entries, numInitialVisibleEntries, isExpanded, isAlwaysExpanded,
                listener, animationViewGroup, /* showFirstEntryTypeTwice = */ false,isRecentsCard);
    }

    /**
     * Sets the Entry list to display.
     *
     * @param entries The Entry list to display.
     */
    public void initialize(List<List<Entry>> entries, int numInitialVisibleEntries,
            boolean isExpanded, boolean isAlwaysExpanded,
            ExpandingEntryCardViewListener listener, ViewGroup animationViewGroup,
            boolean showFirstEntryTypeTwice,boolean isRecentsCard) {
        LayoutInflater layoutInflater = LayoutInflater.from(getContext());
        mIsExpanded = isExpanded;
        mIsAlwaysExpanded = isAlwaysExpanded;
        // If isAlwaysExpanded is true, mIsExpanded should be true
        mIsExpanded |= mIsAlwaysExpanded;
        mEntryViews = new ArrayList<List<View>>(entries.size());
        mEntries = entries;
        mNumEntries = 0;
        mAllEntriesInflated = false;
        mShowFirstEntryTypeTwice = showFirstEntryTypeTwice;
        for (List<Entry> entryList : mEntries) {
            mNumEntries += entryList.size();
            mEntryViews.add(new ArrayList<View>());
        }
        mCollapsedEntriesCount = Math.min(numInitialVisibleEntries, mNumEntries);
        // We need a separator between each list, but not after the last one
        if (entries.size() > 1) {
            mSeparators = new ArrayList<>(entries.size() - 1);
        }
        mListener = listener;
        mAnimationViewGroup = animationViewGroup;
        /*PRIZE-judge display page -huangliemin-2016-5-27 -start*/
        if (mIsExpanded) {
            updateExpandCollapseButton(getCollapseButtonText(), /* duration = */ 0);
            inflateAllEntries(layoutInflater,isRecentsCard);
        } else {
            updateExpandCollapseButton(getExpandButtonText(), /* duration = */ 0);
            inflateInitialEntries(layoutInflater,isRecentsCard);
        }
        /*PRIZE-judge display page -huangliemin-2016-5-27 -end*/
        /*prize-change-huangliemin-2016-7-22-start*/
        //insertEntriesIntoViewGroup();
        insertEntriesIntoViewGroup(isRecentsCard);
        /*prize-change-huangliemin-2016-7-22-end*/
        applyColor();
    }

    /**
     * Sets the text for the expand button.
     *
     * @param expandButtonText The expand button text.
     */
    public void setExpandButtonText(CharSequence expandButtonText) {
        mExpandButtonText = expandButtonText;
        if (mExpandCollapseTextView != null && !mIsExpanded) {
            mExpandCollapseTextView.setText(expandButtonText);
        }
    }

    /**
     * Sets the text for the expand button.
     *
     * @param expandButtonText The expand button text.
     */
    public void setCollapseButtonText(CharSequence expandButtonText) {
        mCollapseButtonText = expandButtonText;
        if (mExpandCollapseTextView != null && mIsExpanded) {
            mExpandCollapseTextView.setText(mCollapseButtonText);
        }
    }

    @Override
    public void setOnClickListener(OnClickListener listener) {
        mOnClickListener = listener;
    }

    @Override
    public void setOnCreateContextMenuListener (OnCreateContextMenuListener listener) {
        mOnCreateContextMenuListener = listener;
    }

    private List<View> calculateEntriesToRemoveDuringCollapse() {
        final List<View> viewsToRemove = getViewsToDisplay(true);
        final List<View> viewsCollapsed = getViewsToDisplay(false);
        viewsToRemove.removeAll(viewsCollapsed);
        return viewsToRemove;
    }

    private void insertEntriesIntoViewGroup() {
        mEntriesViewGroup.removeAllViews();

        for (View view : getViewsToDisplay(mIsExpanded)) {
            mEntriesViewGroup.addView(view);
        }

        removeView(mExpandCollapseButton);
        if (mCollapsedEntriesCount < mNumEntries
                && mExpandCollapseButton.getParent() == null && !mIsAlwaysExpanded) {
            mContainer.addView(mExpandCollapseButton, -1);
        }
    }
    
    /*prize-add-huangliemin-2016-7-22-start*/
    private void insertEntriesIntoViewGroup(boolean isRecentCard) {
        mEntriesViewGroup.removeAllViews();

        for (View view : getViewsToDisplay(mIsExpanded, isRecentCard)) {
            mEntriesViewGroup.addView(view);
        }

        removeView(mExpandCollapseButton);
        /*PRIZE-remove-yuandailin-2016-8-4-start*/        
//        if (mCollapsedEntriesCount < mNumEntries
//                && mExpandCollapseButton.getParent() == null && !mIsAlwaysExpanded) {
//            mContainer.addView(mExpandCollapseButton, -1);
//        }
        /*PRIZE-remove-yuandailin-2016-8-4-end*/        
    }
    /*prize-add-huangliemin-2016-7-22-end*/

    /**
     * Returns the list of views that should be displayed. This changes depending on whether
     * the card is expanded or collapsed.
     */
    private List<View> getViewsToDisplay(boolean isExpanded) {
        final List<View> viewsToDisplay = new ArrayList<View>();
        if (isExpanded) {
            for (int i = 0; i < mEntryViews.size(); i++) {
                List<View> viewList = mEntryViews.get(i);
                if (i > 0) {
                    View separator;
                    if (mSeparators.size() <= i - 1) {
                        separator = generateSeparator(viewList.get(0));
                        mSeparators.add(separator);
                    } else {
                        separator = mSeparators.get(i - 1);
                    }
                    viewsToDisplay.add(separator);
                }
                for (View view : viewList) {
                    viewsToDisplay.add(view);
                }
            }
        } else {
            // We want to insert mCollapsedEntriesCount entries into the group. extraEntries is the
            // number of entries that need to be added that are not the head element of a list
            // to reach mCollapsedEntriesCount.
            int numInViewGroup = 0;
            int extraEntries = mCollapsedEntriesCount - mEntryViews.size();
            for (int i = 0; i < mEntryViews.size() && numInViewGroup < mCollapsedEntriesCount;
                    i++) {
                List<View> entryViewList = mEntryViews.get(i);
                if (i > 0) {
                    View separator;
                    if (mSeparators.size() <= i - 1) {
                        separator = generateSeparator(entryViewList.get(0));
                        mSeparators.add(separator);
                    } else {
                        separator = mSeparators.get(i - 1);
                    }
                    viewsToDisplay.add(separator);
                }
                viewsToDisplay.add(entryViewList.get(0));
                numInViewGroup++;

                int indexInEntryViewList = 1;
                if (mShowFirstEntryTypeTwice && i == 0 && entryViewList.size() > 1) {
                    viewsToDisplay.add(entryViewList.get(1));
                    numInViewGroup++;
                    extraEntries--;
                    indexInEntryViewList++;
                }

                // Insert entries in this list to hit mCollapsedEntriesCount.
                for (int j = indexInEntryViewList;
                        j < entryViewList.size() && numInViewGroup < mCollapsedEntriesCount &&
                        extraEntries > 0;
                        j++) {
                    viewsToDisplay.add(entryViewList.get(j));
                    numInViewGroup++;
                    extraEntries--;
                }
            }
        }

        //TODO:M: Bug fix ALPS01771310, google issue, google's removeView method is not standard.
        formatEntryIfFirst(viewsToDisplay);
        return viewsToDisplay;
    }
    
    /*prize-add-delete-divider-for-rencent-huangliemin-2016-7-22-start*/
    private List<View> getViewsToDisplay(boolean isExpanded, boolean isRecentCard) {
        Log.d(TAG,"[getViewsToDisplay]  isExpanded = "+isExpanded+"   isRecentCard = "+isRecentCard);
        final List<View> viewsToDisplay = new ArrayList<View>();
        /*prize-add for dido os8.0-hpf-2017-8-11-start*/
        if(isRecentCard){
        	mEntriesViewGroup.setBackground(mContext.getResources().getDrawable(R.drawable.prize_card_layout_bg_normal));
        }
        final List<View> prizeKindViewsToDisplay = new ArrayList<View>();    
        LinearLayout prizeContactsCardContainer = null;
        /*prize-add for dido os8.0-hpf-2017-8-11-end*/
        if (isExpanded) {
            for (int i = 0; i < mEntryViews.size(); i++) {
                List<View> viewList = mEntryViews.get(i);
                /*prize-remove for dido os8.0-hpf-2017-8-11-start*/
                /*if (i > 0 && !isRecentCard) {
                    View separator;
                    if (mSeparators.size() <= i - 1) {
                        separator = generateSeparator(viewList.get(0));
                        mSeparators.add(separator);
                    } else {
                        separator = mSeparators.get(i - 1);
                    }
                    viewsToDisplay.add(separator);
                }*/
                /*prize-remove for dido os8.0-hpf-2017-8-11-end*/
                
                /*prize-change for dido os 8.0-hpf-2017-7-25-start*/
                prizeContactsCardContainer = new LinearLayout(mContext);
                prizeContactsCardContainer.setOrientation(LinearLayout.VERTICAL);
                prizeContactsCardContainer.setBackground(mContext.getResources().getDrawable(R.drawable.prize_card_layout_bg_normal));
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                lp.setMargins(0, mContentMarginTop, 0, 0);
                prizeContactsCardContainer.setLayoutParams(lp);
                prizeKindViewsToDisplay.add(prizeContactsCardContainer);
                /*prize-add for dido os 8.0-hpf-2017-7-25-end*/
                /*prize-change for dido os8.0-hpf-2017-8-11-start*/
                for (int j = 0; j < viewList.size(); j ++) {
                	prizeContactsCardContainer.addView(viewList.get(j));
                	if(viewList.size() != j+1){
                		prizeContactsCardContainer.addView(generateSeparator(null));
                	}
                    //viewsToDisplay.add(view);
                }
                /*prize-change for dido os8.0-hpf-2017-8-11-end*/
            }
        } else {
            // We want to insert mCollapsedEntriesCount entries into the group. extraEntries is the
            // number of entries that need to be added that are not the head element of a list
            // to reach mCollapsedEntriesCount.
            int numInViewGroup = 0;
            int extraEntries = mCollapsedEntriesCount - mEntryViews.size();
            for (int i = 0; i < mEntryViews.size() && numInViewGroup < mCollapsedEntriesCount;
                    i++) {
                List<View> entryViewList = mEntryViews.get(i);
                /*prize-remove for dido os8.0-hpf-2017-8-11-start*/
                /*if (i > 0 && !isRecentCard) {
                    View separator;
                    if (mSeparators.size() <= i - 1) {
                        separator = generateSeparator(entryViewList.get(0));
                        mSeparators.add(separator);
                    } else {
                        separator = mSeparators.get(i - 1);
                    }
                    viewsToDisplay.add(separator);
                }
                viewsToDisplay.add(entryViewList.get(0));*/
                /*prize-remove for dido os8.0-hpf-2017-8-11-end*/
                numInViewGroup++;

                int indexInEntryViewList = 1;
                if (mShowFirstEntryTypeTwice && i == 0 && entryViewList.size() > 1) {
                    viewsToDisplay.add(entryViewList.get(1));
                    numInViewGroup++;
                    extraEntries--;
                    indexInEntryViewList++;
                }
                
                /*prize-add for dido os 8.0-hpf-2017-7-25-start*/
                if(!isRecentCard){
                    prizeContactsCardContainer = new LinearLayout(mContext);
                    prizeContactsCardContainer.setOrientation(LinearLayout.VERTICAL);
                    prizeContactsCardContainer.setBackground(mContext.getResources().getDrawable(R.drawable.prize_card_layout_bg_normal));
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                    lp.setMargins(0, mContentMarginTop, 0, 0);
                    prizeContactsCardContainer.setLayoutParams(lp);
                    prizeContactsCardContainer.addView(entryViewList.get(0));
                    if(entryViewList.size() > 1){
                      prizeContactsCardContainer.addView(generateSeparator(null));
                    }
                    prizeKindViewsToDisplay.add(prizeContactsCardContainer);

                }else{
                    viewsToDisplay.add(entryViewList.get(0));
                    if(i < mEntryViews.size() - 1){
                        viewsToDisplay.add(generateSeparator(null));
                    }
                }
                /*prize-add for dido os 8.0-hpf-2017-7-25-end*/

                // Insert entries in this list to hit mCollapsedEntriesCount.
                for (int j = indexInEntryViewList;
                        j < entryViewList.size() && numInViewGroup < mCollapsedEntriesCount &&
                        extraEntries > 0;
                        j++) {
                	/*prize-change dido os 8.0-hpf-2017-7-25-start*/       
                  prizeContactsCardContainer.addView(entryViewList.get(j));
                  if(j != entryViewList.size()-1){
                      prizeContactsCardContainer.addView(generateSeparator(null));
                  }
                  //viewsToDisplay.add(entryViewList.get(j));
                	/*prize-change for dido os8.0-hpf-2017-8-11-end*/
                    numInViewGroup++;
                    extraEntries--;
                }
            }
        }

        //TODO:M: Bug fix ALPS01771310, google issue, google's removeView method is not standard.
        /*prize-change for dido os8.0-hpf-2017-8-11-start*/
        if(isRecentCard){
          formatEntryIfFirst(viewsToDisplay);
          return viewsToDisplay;
        }else{
          return prizeKindViewsToDisplay;
        }
        /*prize-change for dido os8.0-hpf-2017-8-11-end*/
    }
    /*prize-add-delete-divider-for-rencent-huangliemin-2016-7-22-end*/

    private void formatEntryIfFirst(List<View> entriesViewGroup) {
        // If no title and the first entry in the group, add extra padding
        if (TextUtils.isEmpty(mTitleTextView.getText()) &&
                entriesViewGroup.size() > 0) {
            final View entry = entriesViewGroup.get(0);
            entry.setPadding(entry.getPaddingLeft(),
                    getResources().getDimensionPixelSize(
                            R.dimen.expanding_entry_card_item_padding_top) +
                    getResources().getDimensionPixelSize(
                            R.dimen.expanding_entry_card_null_title_top_extra_padding),
                    entry.getPaddingRight(),
                    entry.getPaddingBottom());
        }
    }

    private View generateSeparator(View entry) {
        Log.d(TAG,"[generateSeparator]");
        View separator = new View(getContext());
        Resources res = getResources();

        separator.setBackgroundColor(res.getColor(
                R.color.divider_line_color_light));
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, mDividerLineHeightPixels);
        // The separator is aligned with the text in the entry. This is offset by a default
        // margin. If there is an icon present, the icon's width and margin are added
        int marginStart = res.getDimensionPixelSize(
                R.dimen.expanding_entry_card_item_padding_start);
/*PRIZE-remove imageview-huangliemin-2016-5-27 -start*/				
//        ImageView entryIcon = (ImageView) entry.findViewById(R.id.icon);
//        if (entryIcon.getVisibility() == View.VISIBLE) {
//            int imageWidthAndMargin =
//                    res.getDimensionPixelSize(R.dimen.expanding_entry_card_item_icon_width) +
//                    res.getDimensionPixelSize(R.dimen.expanding_entry_card_item_image_spacing);
//            marginStart += imageWidthAndMargin;
//        }
/*PRIZE-remove imageview-huangliemin-2016-5-27 -end*/
        layoutParams.setMarginStart(marginStart);
        layoutParams.setMarginEnd(marginStart);//prize-add for dido os8.0 -hpf-2017-7-25
        separator.setLayoutParams(layoutParams);
        return separator;
    }

    private CharSequence getExpandButtonText() {
        if (!TextUtils.isEmpty(mExpandButtonText)) {
            return mExpandButtonText;
        } else {
            // Default to "See more".
            return getResources().getText(R.string.expanding_entry_card_view_see_more);
        }
    }

    private CharSequence getCollapseButtonText() {
        if (!TextUtils.isEmpty(mCollapseButtonText)) {
            return mCollapseButtonText;
        } else {
            // Default to "See less".
            return getResources().getText(R.string.expanding_entry_card_view_see_less);
        }
    }
    /*PRIZE-judge display page -huangliemin-2016-5-27 -start*/
    /**
     * Inflates the initial entries to be shown.
     */
    private void inflateInitialEntries(LayoutInflater layoutInflater,boolean isRecentsCard) {
        // If the number of collapsed entries equals total entries, inflate all
        if (mCollapsedEntriesCount == mNumEntries) {
            inflateAllEntries(layoutInflater,isRecentsCard);
        } else {
            // Otherwise inflate the top entry from each list
            // extraEntries is used to add extra entries until mCollapsedEntriesCount is reached.
            int numInflated = 0;
            int extraEntries = mCollapsedEntriesCount - mEntries.size();
            for (int i = 0; i < mEntries.size() && numInflated < mCollapsedEntriesCount; i++) {
                List<Entry> entryList = mEntries.get(i);
                List<View> entryViewList = mEntryViews.get(i);

                entryViewList.add(createEntryView(layoutInflater, entryList.get(0),
                        /* showIcon = */ View.VISIBLE,isRecentsCard));
                numInflated++;

                int indexInEntryViewList = 1;
                if (mShowFirstEntryTypeTwice && i == 0 && entryList.size() > 1) {
                    entryViewList.add(createEntryView(layoutInflater, entryList.get(1),
                        /* showIcon = */ View.INVISIBLE,isRecentsCard));
                    numInflated++;
                    extraEntries--;
                    indexInEntryViewList++;
                }

                // Inflate entries in this list to hit mCollapsedEntriesCount.
                for (int j = indexInEntryViewList; j < entryList.size()
                        && numInflated < mCollapsedEntriesCount
                        && extraEntries > 0; j++) {
                    entryViewList.add(createEntryView(layoutInflater, entryList.get(j),
                            /* showIcon = */ View.INVISIBLE,isRecentsCard));
                    numInflated++;
                    extraEntries--;
                }
            }
        }
    }
    /*PRIZE-judge display page -huangliemin-2016-5-27 -end*/

    /**
     * Inflates all entries.
     */
    /*PRIZE-judge display page -huangliemin-2016-5-27 -start*/
    private void inflateAllEntries(LayoutInflater layoutInflater,boolean isRecentsCard) {
    /*PRIZE-judge display page -huangliemin-2016-5-27 -end*/
        if (mAllEntriesInflated) {
            return;
        }
        for (int i = 0; i < mEntries.size(); i++) {
            List<Entry> entryList = mEntries.get(i);
            List<View> viewList = mEntryViews.get(i);
            for (int j = viewList.size(); j < entryList.size(); j++) {
                final int iconVisibility;
                final Entry entry = entryList.get(j);
                // If the entry does not have an icon, mark gone. Else if it has an icon, show
                // for the first Entry in the list only
                if (entry.getIcon() == null) {
                    iconVisibility = View.GONE;
                } else if (j == 0) {
                    iconVisibility = View.VISIBLE;
                } else {
                    iconVisibility = View.INVISIBLE;
                }
                /*PRIZE-judge display page -huangliemin-2016-5-27 -start*/
                viewList.add(createEntryView(layoutInflater, entry, iconVisibility,isRecentsCard));
                /*PRIZE-judge display page -huangliemin-2016-5-27 -end*/
            }
        }
        mAllEntriesInflated = true;
    }

    public void setColorAndFilter(int color, ColorFilter colorFilter) {
        mThemeColor = color;
        mThemeColorFilter = colorFilter;
        applyColor();
    }

    public void setEntryHeaderColor(int color) {
        if (mEntries != null) {
            for (List<View> entryList : mEntryViews) {
                for (View entryView : entryList) {
                    TextView header = (TextView) entryView.findViewById(R.id.header);
                    if (header != null) {
                        header.setTextColor(color);
                    }
                }
            }
        }
    }

    /**
     * The ColorFilter is passed in along with the color so that a new one only needs to be created
     * once for the entire activity.
     * 1. Title
     * 2. Entry icons
     * 3. Expand/Collapse Text
     * 4. Expand/Collapse Button
     */
    public void applyColor() {
        if (mThemeColor != 0 && mThemeColorFilter != null) {
            // Title
            if (mTitleTextView != null) {
                mTitleTextView.setTextColor(mThemeColor);
            }

            // Entry icons
            if (mEntries != null) {
                for (List<Entry> entryList : mEntries) {
                    for (Entry entry : entryList) {
                        if (entry.shouldApplyColor()) {
                            Drawable icon = entry.getIcon();
                            if (icon != null) {
                                icon.mutate();
                                icon.setColorFilter(mThemeColorFilter);
                            }
                        }
                        Drawable alternateIcon = entry.getAlternateIcon();
                        if (alternateIcon != null) {
                            alternateIcon.mutate();
                            /* prize delete icon color huangliemin start */
                            //alternateIcon.setColorFilter(mThemeColorFilter);
                            /* prize delete icon color huangliemin end */
                        }
                        Drawable thirdIcon = entry.getThirdIcon();
                        if (thirdIcon != null) {
                            thirdIcon.mutate();
                            //thirdIcon.setColorFilter(mThemeColorFilter);//prize-delete-huangliemin-2016-7-7
                        }
                    }
                }
            }

            // Expand/Collapse
            mExpandCollapseTextView.setTextColor(mThemeColor);
            mExpandCollapseArrow.setColorFilter(mThemeColorFilter);
        }
    }
    /*PRIZE-judge display page -huangliemin-2016-5-27 -start*/
    private View createEntryView(LayoutInflater layoutInflater, final Entry entry,
            int iconVisibility,boolean isRecentCard) {
    	if(isRecentCard){
    		 final EntryView view = (EntryView) layoutInflater.inflate(
    	                R.layout.expanding_entry_recents_card_item, this, false);
    		 view.setContextMenuInfo(entry.getEntryContextMenuInfo());
    		 final TextView date = (TextView)view.findViewById(R.id.date);
    		 date.setText(entry.getText());
    		 final ImageView styleIcon = (ImageView)view.findViewById(R.id.style_icon);
    		 styleIcon.setImageDrawable(entry.getTextIcon());
    		 
    		 /*prize-add-huangliemin-2016-7-23-start*/
    		 final ImageView simIcon = (ImageView)view.findViewById(R.id.sim_icon);
    		 PhoneAccountHandle mPhoneAccountHandle = entry.getIntent().getParcelableExtra(TelecomManagerEx.EXTRA_SUGGESTED_PHONE_ACCOUNT_HANDLE);
 			List<SubscriptionInfo> mSubInfoList = SubscriptionManager.from(getContext()).getActiveSubscriptionInfoList();
 			int simcount = 0;
 			if(mSubInfoList!=null) {
 				simcount = mSubInfoList.size();
 			}
 			if(mPhoneAccountHandle!=null && mPhoneAccountHandle.getId()!=null && simcount > 1) {
 				int defaultSubId = mTelephonyManager.getSubIdForPhoneAccount(mTelecomManager.getPhoneAccount(mPhoneAccountHandle));
 				int slotId = SubscriptionManager.getSlotId(defaultSubId);
 				if(slotId == 0) {
 					simIcon.setImageDrawable(getContext().getResources().getDrawable(R.drawable.prize_sim_1));
 					simIcon.setVisibility(View.VISIBLE);
 				} else if (slotId == 1) {
 					simIcon.setImageDrawable(getContext().getResources().getDrawable(R.drawable.prize_sim_2));
 					simIcon.setVisibility(View.VISIBLE);
 				} else {
 					simIcon.setVisibility(View.GONE);
 				}
 			} else {
 				simIcon.setVisibility(View.GONE);
 			}
    		 /*prize-add-huangliemin-2016-7-23-end*/

        /*PRIZE-add call duration -huangliemin-2016-5-27 -start*/			 
    		 final TextView styleText = (TextView)view.findViewById(R.id.style_text);
			 if(entry.getCallDuration() != null){
			 styleText.setText(entry.getCallDuration());
			 	}else{
				styleText.setText(getResources().getString(R.string.prize_callnotice) + 0 + getResources().getString(R.string.prize_seconds));
			 		}
        /*PRIZE-add call duration -huangliemin-2016-5-27 -end*/

			 final ImageView alternateIcon = (ImageView) view.findViewById(R.id.icon_alternate);
             final ImageView thirdIcon = (ImageView) view.findViewById(R.id.third_icon);
             /*PRIZE-remove-yuandailin-2016-8-27-start*/
//			  if (entry.getIntent() != null) {
//                 view.setOnClickListener(mOnClickListener);
//                 view.setTag(new EntryTag(entry.getId(), entry.getIntent()));
//              }
             /*PRIZE-remove-yuandailin-2016-8-27-end*/
        if (entry.getIntent() == null && entry.getEntryContextMenuInfo() == null) {
            // Remove the click effect
            view.setBackground(null);
        }
             view.setOnCreateContextMenuListener(mOnCreateContextMenuListener);
    		return view;
    	}
		/*PRIZE-judge display page -zhangzhonghao-2015-4-20 -end*/
		/*prize adaptation big-font by zhaojian 20180601 start*/
        boolean isPrizeFontSize = SystemProperties.getBoolean("persist.sys.prize.fontsize",false);
        final EntryView view;
        if(isPrizeFontSize){
            view = (EntryView) layoutInflater.inflate(
                    R.layout.prize_expanding_entry_card_item_big_font, this, false);
        }else {
            view = (EntryView) layoutInflater.inflate(
                    R.layout.expanding_entry_card_item, this, false);
        }
        /*final EntryView view = (EntryView) layoutInflater.inflate(
                R.layout.expanding_entry_card_item, this, false);*/
        /*prize adaptation big-font by zhaojian 20180601 end*/

        view.setContextMenuInfo(entry.getEntryContextMenuInfo());
        if (!TextUtils.isEmpty(entry.getPrimaryContentDescription())) {
            view.setContentDescription(entry.getPrimaryContentDescription());
        }

        final ImageView icon = (ImageView) view.findViewById(R.id.icon);
           icon.setVisibility(View.GONE);
        /*PRIZE-remove-yuandailin-2015-5-9-start*/   
//        icon.setVisibility(iconVisibility);
        /*PRIZE-remove-yuandailin-2015-5-9-end*/
        if (entry.getIcon() != null) {
            icon.setImageDrawable(entry.getIcon());
        }
        final TextView header = (TextView) view.findViewById(R.id.header);
        if (!TextUtils.isEmpty(entry.getHeader())) {
        	/*prize-add-huangliemin-2016-7-16-start*/
        	//if(isContactEditable) {
        		mHeaderList.add(header);
        	//}
        	/*prize-add-huangliemin-2016-7-16-end*/
            header.setText(entry.getHeader());
        } else {
            header.setVisibility(View.GONE);
        }

        final TextView subHeader = (TextView) view.findViewById(R.id.sub_header);
        if (!TextUtils.isEmpty(entry.getSubHeader())) {
            /*prize modify for bug 51265 by zhaojian 20180411 start*/
        	/*prize-change for dido os8.0-hpf-2017-8-31-start*/
        	/*if(entry.getSubHeader().equals(mContext.getResources().getString(R.string.aas_phone_primary))
        			||entry.getSubHeader().equals(mContext.getResources().getString(R.string.aas_phone_additional))){
        		subHeader.setVisibility(View.GONE);
        	}else{
        		 subHeader.setText(entry.getSubHeader());
        	}*/
            subHeader.setText(entry.getSubHeader());
        	/*prize modify for bug 51265 by zhaojian 20180411 end*/

        	/*prize-change for dido os8.0-hpf-2017-8-31-end*/
        } else {
            subHeader.setVisibility(View.GONE);
        }

        final ImageView subHeaderIcon = (ImageView) view.findViewById(R.id.icon_sub_header);
        if (entry.getSubHeaderIcon() != null) {
            subHeaderIcon.setImageDrawable(entry.getSubHeaderIcon());
        } else {
        subHeaderIcon.setVisibility(View.GONE);
        /*PRIZE-remove-yuandailin-2015-5-9-start*/
//        if (entry.getSubHeaderIcon() != null) {
//            subHeaderIcon.setImageDrawable(entry.getSubHeaderIcon());
//        } else {
//            subHeaderIcon.setVisibility(View.GONE);
//        }
       /*PRIZE-remove-yuandailin-2015-5-9-end*/
	   }

        final TextView text = (TextView) view.findViewById(R.id.text);
        if (!TextUtils.isEmpty(entry.getText())) {
            text.setText(entry.getText());
        } else {
            text.setVisibility(View.GONE);
        }

        final ImageView textIcon = (ImageView) view.findViewById(R.id.icon_text);
        if (entry.getTextIcon() != null) {
            textIcon.setImageDrawable(entry.getTextIcon());
        } else {
        textIcon.setVisibility(View.GONE);
        /*PRIZE-remove-yuandailin-2015-5-9-start*/
//        if (entry.getTextIcon() != null) {
//            textIcon.setImageDrawable(entry.getTextIcon());
//        } else {
//            textIcon.setVisibility(View.GONE);
//        }
        /*PRIZE-remove-yuandailin-2015-5-9-end*/
		}
        /** M: add sim icon & sim name @ { */
        final ImageView simIcon = (ImageView) view.findViewById(R.id.icon_sim);
        if (entry.getSimIcon() != null) {
            simIcon.setImageDrawable(entry.getSimIcon());
        } else {
            simIcon.setVisibility(View.GONE);
        }

        final TextView simNameText = (TextView) view.findViewById(R.id.sim_name);
        if (!TextUtils.isEmpty(entry.getSimName())) {
            simNameText.setText(entry.getSimName());
        } else {
            simNameText.setVisibility(View.GONE);
        }


        /* @ } */

        if (entry.getIntent() != null) {
            view.setOnClickListener(mOnClickListener);
            view.setTag(new EntryTag(entry.getId(), entry.getIntent()));
        }

        if (entry.getIntent() == null && entry.getEntryContextMenuInfo() == null) {
            // Remove the click effect
            view.setBackground(null);
        }

        // If only the header is visible, add a top margin to match icon's top margin.
        // Also increase the space below the header for visual comfort.
        if (header.getVisibility() == View.VISIBLE && subHeader.getVisibility() == View.GONE &&
                text.getVisibility() == View.GONE) {
            RelativeLayout.LayoutParams headerLayoutParams =
                    (RelativeLayout.LayoutParams) header.getLayoutParams();
            headerLayoutParams.topMargin = (int) (getResources().getDimension(
                    R.dimen.expanding_entry_card_item_header_only_margin_top));
            headerLayoutParams.bottomMargin += (int) (getResources().getDimension(
                    R.dimen.expanding_entry_card_item_header_only_margin_bottom));
            header.setLayoutParams(headerLayoutParams);
        }

        // Adjust the top padding size for entries with an invisible icon. The padding depends on
        // if there is a sub header or text section
        if (iconVisibility == View.INVISIBLE &&
                (!TextUtils.isEmpty(entry.getSubHeader()) || !TextUtils.isEmpty(entry.getText()))) {
            view.setPaddingRelative(view.getPaddingStart(),
                    getResources().getDimensionPixelSize(
                            R.dimen.expanding_entry_card_item_no_icon_margin_top),
                    view.getPaddingEnd(),
                    view.getPaddingBottom());
        } else if (iconVisibility == View.INVISIBLE &&  TextUtils.isEmpty(entry.getSubHeader())
                && TextUtils.isEmpty(entry.getText())) {
            view.setPaddingRelative(view.getPaddingStart(), 0, view.getPaddingEnd(),
                    view.getPaddingBottom());
        }

        final ImageView alternateIcon = (ImageView) view.findViewById(R.id.icon_alternate);
        final ImageView thirdIcon = (ImageView) view.findViewById(R.id.third_icon);

        if (entry.getAlternateIcon() != null && entry.getAlternateIntent() != null) {
            alternateIcon.setImageDrawable(entry.getAlternateIcon());
            alternateIcon.setOnClickListener(mOnClickListener);
            alternateIcon.setTag(new EntryTag(entry.getId(), entry.getAlternateIntent()));
            alternateIcon.setVisibility(View.VISIBLE);
            alternateIcon.setContentDescription(entry.getAlternateContentDescription());
            
            /*prize-add fix bug[48148]-dialer-direct-hpf-2018-1-12-start*/
            if(entry.getIntent()!=null && entry.getIntent().getAction().equals(Intent.ACTION_CALL)) {
            	final ImageView PhoneIcon = (ImageView) view.findViewById(R.id.icon_one);
            	final View divider = view.findViewById(R.id.icon_one_divider);
            	divider.setVisibility(View.VISIBLE);
            	PhoneIcon.setOnClickListener(mOnClickListener);
            	PhoneIcon.setTag(new EntryTag(entry.getId(), entry.getIntent()));
            	PhoneIcon.setVisibility(View.VISIBLE);
                header.setTextColor(mContext.getResources().getColor(R.color.prize_button_text_default_color));
            }
            /*prize-add fix bug[48148]-dialer-direct-hpf-2018-1-12-end*/
        }

        if (entry.getThirdIcon() != null && entry.getThirdAction() != Entry.ACTION_NONE) {
        	
        	/*prize-change for dido os8.0-hpf-2017-8-9-start*/
        	
        	PrizeQuickContactDataManager.setEntry(entry);
        	
            /*thirdIcon.setImageDrawable(entry.getThirdIcon());
            if (entry.getThirdAction() == Entry.ACTION_INTENT) {
                thirdIcon.setOnClickListener(mOnClickListener);
                thirdIcon.setTag(new EntryTag(entry.getId(), entry.getThirdIntent()));
            } else if (entry.getThirdAction() == Entry.ACTION_CALL_WITH_SUBJECT) {
                thirdIcon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Object tag = v.getTag();
                        if (!(tag instanceof Bundle)) {
                            return;
                        }

                        Context context = getContext();
                        if (context instanceof Activity) {
                            CallSubjectDialog.start((Activity) context, entry.getThirdExtras());
                        }
                    }
                });
                thirdIcon.setTag(entry.getThirdExtras());
            }
            thirdIcon.setVisibility(View.VISIBLE);
            thirdIcon.setContentDescription(entry.getThirdContentDescription());*/
        	
        	/*prize-change for dido os8.0-hpf-2017-8-9-end*/
        }

        // Set a custom touch listener for expanding the extra icon touch areas
        view.setOnTouchListener(new EntryTouchListener(view, alternateIcon, thirdIcon));
        view.setOnCreateContextMenuListener(mOnCreateContextMenuListener);

        return view;
    }

    private void updateExpandCollapseButton(CharSequence buttonText, long duration) {
        if (mIsExpanded) {
            final ObjectAnimator animator = ObjectAnimator.ofFloat(mExpandCollapseArrow,
                    "rotation", 180);
            animator.setDuration(duration);
            animator.start();
        } else {
            final ObjectAnimator animator = ObjectAnimator.ofFloat(mExpandCollapseArrow,
                    "rotation", 0);
            animator.setDuration(duration);
            animator.start();
        }
        updateBadges();

        mExpandCollapseTextView.setText(buttonText);
    }

    private void updateBadges() {
        if (mIsExpanded) {
            mBadgeContainer.removeAllViews();
        } else {
            int numberOfMimeTypesShown = mCollapsedEntriesCount;
            if (mShowFirstEntryTypeTwice && mEntries.size() > 0
                    && mEntries.get(0).size() > 1) {
                numberOfMimeTypesShown--;
            }
            // Inflate badges if not yet created
            if (mBadges.size() < mEntries.size() - numberOfMimeTypesShown) {
                for (int i = numberOfMimeTypesShown; i < mEntries.size(); i++) {
                    Drawable badgeDrawable = mEntries.get(i).get(0).getIcon();
                    int badgeResourceId = mEntries.get(i).get(0).getIconResourceId();
                    // Do not add the same badge twice
                    if (badgeResourceId != 0 && mBadgeIds.contains(badgeResourceId)) {
                        continue;
                    }
                    if (badgeDrawable != null) {
                        ImageView badgeView = new ImageView(getContext());
                        LinearLayout.LayoutParams badgeViewParams = new LinearLayout.LayoutParams(
                                (int) getResources().getDimension(
                                        R.dimen.expanding_entry_card_item_icon_width),
                                (int) getResources().getDimension(
                                        R.dimen.expanding_entry_card_item_icon_height));
                        badgeViewParams.setMarginEnd((int) getResources().getDimension(
                                R.dimen.expanding_entry_card_badge_separator_margin));
                        badgeView.setLayoutParams(badgeViewParams);
                        badgeView.setImageDrawable(badgeDrawable);
                        mBadges.add(badgeView);
                        mBadgeIds.add(badgeResourceId);
                    }
                }
            }
            mBadgeContainer.removeAllViews();
            for (ImageView badge : mBadges) {
                mBadgeContainer.addView(badge);
            }
        }
    }

    private void expand() {
        ChangeBounds boundsTransition = new ChangeBounds();
        boundsTransition.setDuration(DURATION_EXPAND_ANIMATION_CHANGE_BOUNDS);

        Fade fadeIn = new Fade(Fade.IN);
        fadeIn.setDuration(DURATION_EXPAND_ANIMATION_FADE_IN);
        fadeIn.setStartDelay(DELAY_EXPAND_ANIMATION_FADE_IN);

        TransitionSet transitionSet = new TransitionSet();
        transitionSet.addTransition(boundsTransition);
        transitionSet.addTransition(fadeIn);

        transitionSet.excludeTarget(R.id.text, /* exclude = */ true);

        final ViewGroup transitionViewContainer = mAnimationViewGroup == null ?
                this : mAnimationViewGroup;

        transitionSet.addListener(new TransitionListener() {
            @Override
            public void onTransitionStart(Transition transition) {
                mListener.onExpand();
            }

            @Override
            public void onTransitionEnd(Transition transition) {
                mListener.onExpandDone();
            }

            @Override
            public void onTransitionCancel(Transition transition) {
            }

            @Override
            public void onTransitionPause(Transition transition) {
            }

            @Override
            public void onTransitionResume(Transition transition) {
            }
        });

        TransitionManager.beginDelayedTransition(transitionViewContainer, transitionSet);

        mIsExpanded = true;
        // In order to insert new entries, we may need to inflate them for the first time
        inflateAllEntries(LayoutInflater.from(getContext()),false);
        insertEntriesIntoViewGroup();
        updateExpandCollapseButton(getCollapseButtonText(),
                DURATION_EXPAND_ANIMATION_CHANGE_BOUNDS);
    }

    private void collapse() {
        final List<View> views = calculateEntriesToRemoveDuringCollapse();

        // This animation requires layout changes, unlike the expand() animation: the action bar
        // might get scrolled open in order to fill empty space. As a result, we can't use
        // ChangeBounds here. Instead manually animate view height and alpha. This isn't as
        // efficient as the bounds and translation changes performed by ChangeBounds. Nonetheless, a
        // reasonable frame-rate is achieved collapsing a dozen elements on a user Svelte N4. So the
        // performance hit doesn't justify writing a less maintainable animation.
        final AnimatorSet set = new AnimatorSet();
        final List<Animator> animators = new ArrayList<Animator>(views.size());
        int totalSizeChange = 0;
        for (View viewToRemove : views) {
            final ObjectAnimator animator = ObjectAnimator.ofObject(viewToRemove,
                    VIEW_LAYOUT_HEIGHT_PROPERTY, null, viewToRemove.getHeight(), 0);
            totalSizeChange += viewToRemove.getHeight();
            animator.setDuration(DURATION_COLLAPSE_ANIMATION_CHANGE_BOUNDS);
            animators.add(animator);
            viewToRemove.animate().alpha(0).setDuration(DURATION_COLLAPSE_ANIMATION_FADE_OUT);
        }
        set.playTogether(animators);
        set.start();
        set.addListener(new AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                // Now that the views have been animated away, actually remove them from the view
                // hierarchy. Reset their appearance so that they look appropriate when they
                // get added back later.
                insertEntriesIntoViewGroup();
                for (View view : views) {
                    if (view instanceof EntryView) {
                        VIEW_LAYOUT_HEIGHT_PROPERTY.set(view, LayoutParams.WRAP_CONTENT);
                    } else {
                        VIEW_LAYOUT_HEIGHT_PROPERTY.set(view, mDividerLineHeightPixels);
                    }
                    view.animate().cancel();
                    view.setAlpha(1);
            }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });

        mListener.onCollapse(totalSizeChange);
        mIsExpanded = false;
        updateExpandCollapseButton(getExpandButtonText(),
                DURATION_COLLAPSE_ANIMATION_CHANGE_BOUNDS);
    }

    /**
     * Returns whether the view is currently in its expanded state.
     */
    public boolean isExpanded() {
        return mIsExpanded;
    }

    /**
     * Sets the title text of this ExpandingEntryCardView.
     * @param title The title to set. A null title will result in the title being removed.
     */
    public void setTitle(String title) {
        if (mTitleTextView == null) {
            Log.e(TAG, "mTitleTextView is null");
            return;
        }
        mTitleTextView.setText(title);
        mTitleTextView.setVisibility(TextUtils.isEmpty(title) ? View.GONE : View.GONE);
//        findViewById(R.id.title_separator).setVisibility(TextUtils.isEmpty(title) ?
//                View.GONE : View.VISIBLE);
        // If the title is set after children have been added, reset the top entry's padding to
        // the default. Else if the title is cleared after children have been added, set
        // the extra top padding
        if (!TextUtils.isEmpty(title) && mEntriesViewGroup.getChildCount() > 0) {
            View firstEntry = mEntriesViewGroup.getChildAt(0);
            firstEntry.setPadding(firstEntry.getPaddingLeft(),
                    getResources().getDimensionPixelSize(
                            R.dimen.expanding_entry_card_item_padding_top),
                    firstEntry.getPaddingRight(),
                    firstEntry.getPaddingBottom());
        } else if (!TextUtils.isEmpty(title) && mEntriesViewGroup.getChildCount() > 0) {
            View firstEntry = mEntriesViewGroup.getChildAt(0);
            firstEntry.setPadding(firstEntry.getPaddingLeft(),
                    getResources().getDimensionPixelSize(
                            R.dimen.expanding_entry_card_item_padding_top) +
                            getResources().getDimensionPixelSize(
                                    R.dimen.expanding_entry_card_null_title_top_extra_padding),
                    firstEntry.getPaddingRight(),
                    firstEntry.getPaddingBottom());
        }
    }

    public boolean shouldShow() {
        return mEntries != null && mEntries.size() > 0;
    }

    public static final class EntryView extends RelativeLayout {
        private EntryContextMenuInfo mEntryContextMenuInfo;

        public EntryView(Context context) {
            super(context);
        }

        public EntryView(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public void setContextMenuInfo(EntryContextMenuInfo info) {
            mEntryContextMenuInfo = info;
        }

        @Override
        protected ContextMenuInfo getContextMenuInfo() {
            return mEntryContextMenuInfo;
        }
    }

    public static final class EntryContextMenuInfo implements ContextMenuInfo {
        private final String mCopyText;
        private final String mCopyLabel;
        private final String mMimeType;
        private final long mId;
        private final boolean mIsSuperPrimary;

        public EntryContextMenuInfo(String copyText, String copyLabel, String mimeType, long id,
                boolean isSuperPrimary) {
            mCopyText = copyText;
            mCopyLabel = copyLabel;
            mMimeType = mimeType;
            mId = id;
            mIsSuperPrimary = isSuperPrimary;
        }

        public String getCopyText() {
            return mCopyText;
        }

        public String getCopyLabel() {
            return mCopyLabel;
        }

        public String getMimeType() {
            return mMimeType;
        }

        public long getId() {
            return mId;
        }

        public boolean isSuperPrimary() {
            return mIsSuperPrimary;
        }
    }

    public static final class EntryTag {
        private final int mId;
        private final Intent mIntent;

        public EntryTag(int id, Intent intent) {
            mId = id;
            mIntent = intent;
        }

        public int getId() {
            return mId;
        }

        public Intent getIntent() {
            return mIntent;
        }
    }

    /**
     * This custom touch listener increases the touch area for the second and third icons, if
     * they are present. This is necessary to maintain other properties on an entry view, like
     * using a top padding on entry. Based off of {@link android.view.TouchDelegate}
     */
    private static final class EntryTouchListener implements View.OnTouchListener {
        private final View mEntry;
        private final ImageView mAlternateIcon;
        private final ImageView mThirdIcon;
        /** mTouchedView locks in a view on touch down */
        private View mTouchedView;
        /** mSlop adds some space to account for touches that are just outside the hit area */
        private int mSlop;

        public EntryTouchListener(View entry, ImageView alternateIcon, ImageView thirdIcon) {
            mEntry = entry;
            mAlternateIcon = alternateIcon;
            mThirdIcon = thirdIcon;
            mSlop = ViewConfiguration.get(entry.getContext()).getScaledTouchSlop();
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            View touchedView = mTouchedView;
            boolean sendToTouched = false;
            boolean hit = true;
            boolean handled = false;

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (hitThirdIcon(event)) {
                        mTouchedView = mThirdIcon;
                        sendToTouched = true;
                    } else if (hitAlternateIcon(event)) {
                        mTouchedView = mAlternateIcon;
                        sendToTouched = true;
                    } else {
                        mTouchedView = mEntry;
                        sendToTouched = false;
                    }
                    touchedView = mTouchedView;
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_MOVE:
                    sendToTouched = mTouchedView != null && mTouchedView != mEntry;
                    if (sendToTouched) {
                        final Rect slopBounds = new Rect();
                        touchedView.getHitRect(slopBounds);
                        slopBounds.inset(-mSlop, -mSlop);
                        if (!slopBounds.contains((int) event.getX(), (int) event.getY())) {
                            hit = false;
                        }
                    }
                    break;
                case MotionEvent.ACTION_CANCEL:
                    sendToTouched = mTouchedView != null && mTouchedView != mEntry;
                    mTouchedView = null;
                    break;
            }
            if (sendToTouched) {
                if (hit) {
                    event.setLocation(touchedView.getWidth() / 2, touchedView.getHeight() / 2);
                } else {
                    // Offset event coordinates to be outside the target view (in case it does
                    // something like tracking pressed state)
                    event.setLocation(-(mSlop * 2), -(mSlop * 2));
                }
                handled = touchedView.dispatchTouchEvent(event);
            }
            return handled;
        }

        private boolean hitThirdIcon(MotionEvent event) {
            if (mEntry.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
                return mThirdIcon.getVisibility() == View.VISIBLE &&
                        event.getX() < mThirdIcon.getRight();
            } else {
                return mThirdIcon.getVisibility() == View.VISIBLE &&
                        event.getX() > mThirdIcon.getLeft();
            }
        }

        /**
         * Should be used after checking if third icon was hit
         */
        private boolean hitAlternateIcon(MotionEvent event) {
            // LayoutParams used to add the start margin to the touch area
            final RelativeLayout.LayoutParams alternateIconParams =
                    (RelativeLayout.LayoutParams) mAlternateIcon.getLayoutParams();
            if (mEntry.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
                return mAlternateIcon.getVisibility() == View.VISIBLE &&
                        event.getX() < mAlternateIcon.getRight() + alternateIconParams.rightMargin;
            } else {
                return mAlternateIcon.getVisibility() == View.VISIBLE &&
                        event.getX() > mAlternateIcon.getLeft() - alternateIconParams.leftMargin;
            }
        }
    }
    
    /*prize-add-huangpengfei-2016-9-27-start*/
    public void removeAllView(){
    	
    	if(mHeaderList != null){
    		mHeaderList.clear();
    	}
    }
    /*prize-add-huangpengfei-2016-9-27-end*/
    
    /*prize-add-huangpengfei-2016-10-18-start*/
    public void removeAllListener(){
    	if(mOnClickListener != null){
    		mOnClickListener = null;
    	}
    	if(mOnCreateContextMenuListener != null){
    		mOnCreateContextMenuListener = null;
    	}
    }
    /*prize-add-huangpengfei-2016-10-18-end*/
    
    
}
