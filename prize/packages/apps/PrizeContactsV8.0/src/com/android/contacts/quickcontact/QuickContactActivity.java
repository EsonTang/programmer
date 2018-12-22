/*
* Copyright (C) 2014 MediaTek Inc.
* Modification based on code covered by the mentioned copyright
* and/or permission notice(s).
*/
/*
 * Copyright (C) 2009 The Android Open Source Project
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

import com.android.dialer.service.ILocationService;
import android.accounts.Account;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager.LoaderCallbacks;
import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Trace;
import android.provider.CalendarContract;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Event;
import android.provider.ContactsContract.CommonDataKinds.GroupMembership;
import android.provider.ContactsContract.CommonDataKinds.Identity;
import android.provider.ContactsContract.CommonDataKinds.Im;
import android.provider.ContactsContract.CommonDataKinds.Nickname;
import android.provider.ContactsContract.CommonDataKinds.Note;
import android.provider.ContactsContract.CommonDataKinds.Organization;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.Relation;
import android.provider.ContactsContract.CommonDataKinds.SipAddress;
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal;
import android.provider.ContactsContract.CommonDataKinds.Website;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.Directory;
import android.provider.ContactsContract.DisplayNameSources;
import android.provider.ContactsContract.DataUsageFeedback;
import android.provider.ContactsContract.Intents;
import android.provider.ContactsContract.QuickContact;
import android.provider.ContactsContract.RawContacts;
import android.support.v4.content.ContextCompat;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.CardView;
import android.telecom.PhoneAccount;
import android.telecom.TelecomManager;
import android.text.BidiFormatter;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextDirectionHeuristics;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;
import android.widget.LinearLayout.LayoutParams;

import com.android.contacts.ContactSaveService;
import com.android.contacts.ContactsActivity;
import com.android.contacts.ContactsApplication;
import com.android.contacts.NfcHandler;
import com.android.contacts.QuickMarkActivity;
import com.android.contacts.R;
import com.android.contacts.activities.ContactEditorActivity;
import com.android.contacts.activities.ContactEditorBaseActivity;
import com.android.contacts.activities.PeopleActivity;
import com.android.contacts.common.CallUtil;
import com.android.contacts.common.ClipboardUtils;
import com.android.contacts.common.Collapser;
import com.android.contacts.common.ContactPhotoManager;
import com.android.contacts.common.ContactsUtils;
import com.android.contacts.common.activity.RequestDesiredPermissionsActivity;
import com.android.contacts.common.activity.RequestPermissionsActivity;
import com.android.contacts.common.compat.CompatUtils;
import com.android.contacts.common.compat.EventCompat;
import com.android.contacts.common.compat.MultiWindowCompat;
import com.android.contacts.common.dialog.CallSubjectDialog;
import com.android.contacts.common.editor.SelectAccountDialogFragment;
import com.android.contacts.common.interactions.TouchPointManager;
import com.android.contacts.common.lettertiles.LetterTileDrawable;
import com.android.contacts.common.list.ShortcutIntentBuilder;
import com.android.contacts.common.list.ShortcutIntentBuilder.OnShortcutIntentCreatedListener;
import com.android.contacts.common.logging.Logger;
import com.android.contacts.common.logging.ScreenEvent.ScreenType;
import com.android.contacts.common.model.AccountTypeManager;
import com.android.contacts.common.model.Contact;
import com.android.contacts.common.model.ContactLoader;
import com.android.contacts.common.model.RawContact;
import com.android.contacts.common.model.account.AccountType;
import com.android.contacts.common.model.account.AccountWithDataSet;
import com.android.contacts.common.model.dataitem.DataItem;
import com.android.contacts.common.model.dataitem.DataKind;
import com.android.contacts.common.model.dataitem.EmailDataItem;
import com.android.contacts.common.model.dataitem.EventDataItem;
import com.android.contacts.common.model.dataitem.GroupMembershipDataItem;
import com.android.contacts.common.model.dataitem.ImDataItem;
import com.android.contacts.common.model.dataitem.NicknameDataItem;
import com.android.contacts.common.model.dataitem.NoteDataItem;
import com.android.contacts.common.model.dataitem.OrganizationDataItem;
import com.android.contacts.common.model.dataitem.PhoneDataItem;
import com.android.contacts.common.model.dataitem.RelationDataItem;
import com.android.contacts.common.model.dataitem.SipAddressDataItem;
import com.android.contacts.common.model.dataitem.StructuredNameDataItem;
import com.android.contacts.common.model.dataitem.StructuredPostalDataItem;
import com.android.contacts.common.model.dataitem.WebsiteDataItem;
import com.android.contacts.common.model.ValuesDelta;
import com.android.contacts.common.util.Constants;
import com.android.contacts.common.util.ImplicitIntentsUtil;
import com.android.contacts.common.util.DateUtils;
import com.android.contacts.common.util.MaterialColorMapUtils;
import com.android.contacts.common.util.MaterialColorMapUtils.MaterialPalette;
import com.android.contacts.common.util.UriUtils;
import com.android.contacts.common.util.ViewUtil;
import com.android.contacts.common.vcard.VCardCommonArguments;
import com.android.contacts.detail.ContactDisplayUtils;
import com.android.contacts.editor.AggregationSuggestionEngine;
import com.android.contacts.editor.AggregationSuggestionEngine.Suggestion;
import com.android.contacts.editor.ContactEditorFragment;
import com.android.contacts.editor.EditorIntents;
import com.android.contacts.interactions.CalendarInteractionsLoader;
import com.android.contacts.interactions.CallLogInteractionsLoader;
import com.android.contacts.interactions.ContactDeletionInteraction;
import com.android.contacts.interactions.ContactInteraction;
import com.android.contacts.interactions.JoinContactsDialogFragment;
import com.android.contacts.interactions.JoinContactsDialogFragment.JoinContactsListener;
import com.android.contacts.interactions.CallLogInteraction;
import com.android.contacts.interactions.SmsInteractionsLoader;
import com.android.contacts.prize.PrizeQuickMarkDataManager;
import com.android.contacts.quickcontact.ExpandingEntryCardView.Entry;
import com.android.contacts.quickcontact.ExpandingEntryCardView.EntryContextMenuInfo;
import com.android.contacts.quickcontact.ExpandingEntryCardView.EntryTag;
import com.android.contacts.quickcontact.ExpandingEntryCardView.ExpandingEntryCardViewListener;
import com.android.contacts.quickcontact.WebAddress.ParseException;
import com.android.contacts.util.ImageViewDrawableSetter;
import com.android.contacts.util.PhoneCapabilityTester;
import com.android.contacts.util.SchedulingUtils;
import com.android.contacts.util.StructuredPostalUtils;
import com.android.contacts.widget.DialogBottomMenu;
import com.android.contacts.widget.DialogItemOnClickListener;
import com.android.contacts.widget.MultiShrinkScroller;
import com.android.contacts.widget.MultiShrinkScroller.MultiShrinkScrollerListener;
import com.android.contacts.widget.QuickContactImageView;
import com.android.contactsbind.HelpUtils;

import com.google.common.collect.Lists;
import com.mediatek.contacts.ExtensionManager;
import com.mediatek.contacts.GlobalEnv;
import com.mediatek.contacts.quickcontact.QuickContactUtils;
import com.mediatek.contacts.ContactsSystemProperties;
import com.mediatek.contacts.model.dataitem.ImsCallDataItem;
import com.mediatek.contacts.simcontact.SimCardUtils;
import com.mediatek.contacts.util.AccountTypeUtils;
import com.mediatek.hotknot.HotKnotAdapter;
import com.mediatek.contacts.util.ContactsSettingsUtils;
import com.mediatek.contacts.util.Log;

import java.lang.SecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
/*PRIZE-add-huangliemin-2016-5-27 -start*/
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.provider.CallLog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
/*PRIZE-add-huangliemin-2016-5-27 -end*/
import com.prize.contacts.common.util.PrizeBlockNumberHelper;//PRIZE-add-yuandailin-2016-1-29
/*prize-add for dido os8.0 -hpf-2017-7-26-start*/
import android.util.TypedValue;
import android.app.ActionBar;
import com.android.contacts.prize.PrizeQuickContactDataManager;
import java.util.Random;
import android.content.ContentResolver;
import android.provider.Settings;
/*prize-add for dido os8.0 -hpf-2017-7-26-end*/
/*prize-add for BatteryBroadcastReciver huangpengfei-2017-6-26-start*/
import com.android.contacts.common.prize.PrizeBatteryBroadcastReciver;
import android.content.IntentFilter;
/*prize-add for BatteryBroadcastReciver huangpengfei-2017-6-26-end*/
/*prize add for bug 52649 zhaojian 20180313 start*/
import android.view.inputmethod.InputMethodManager;
/*prize add for bug 52649 zhaojian 20180313 start*/
import android.os.SystemProperties;    //prize adaptation big-font by zhaojian 20180601

/**
 * Mostly translucent {@link Activity} that shows QuickContact dialog. It loads
 * data asynchronously, and then shows a popup with details centered around
 * {@link Intent#getSourceBounds()}.
 */
public class QuickContactActivity extends ContactsActivity implements OnClickListener,DialogItemOnClickListener,
AggregationSuggestionEngine.Listener,JoinContactsDialogFragment.JoinContactsListener{

    /*prize add for bug 55239 by zhaojian 20180502 start*/
    private static final String PRIZE_CONTACTS_PACKAGE_NAME = "com.android.contacts";
    private static final String CONTACTS_SELECTION_ACTIVITY_NAME = "com.android.contacts.activities.ContactSelectionActivity";
    private static final String PRIZE_CONTACTS_EDITOR_ACTIVITY_NAME = "com.android.contacts.activities.ContactEditorActivity";
    /*prize add for bug 55239 by zhaojian 20180502 end*/
    /* prize add some view zhangzhonghao 20160314 start */
    private ImageButton prizeContactEditor;
    private ImageButton prizeContactDelete;
    private ImageButton prizeContactMoreMenu;//prize-add-huangliemin-2016-7-16
    /*prize-add-huangliemin-2016-7-18-start*/
    private LinearLayout PrizeContactsDeleteLayout;
    /*prize-add-huangliemin-2016-7-18-end*/
    private LinearLayout prizeQuickBottomButtonLinearLayout;
    private RelativeLayout prizeQuickDefaultMusiceLinearLayout;
    private TextView prizeQuickDefaultMusicTitle;
    private TextView prizeQuickDefaultMusicName;
    private LinearLayout prizeQuickTwoDeminsionLinearLayout;
    private TextView prizeQuickNoRecentsNotice;
    private String mCustomRingtone;
    private RelativeLayout prizeContactPhotoRelativeLayout;
    private static StringBuilder twoDeminsionCodeNumber = new StringBuilder();
    /* prize add some view zhangzhonghao 20160314 end */
    
    /*prize-add some view huangpengfei-2016-8-10-start*/
    private TextView PrizeContactsEditorText;
    private TextView prizeContactsMoreMenuText;
    private TextView PrizeContactsDeleteTextView;
    /*prize-add some view huangpengfei-2016-8-10-end*/
    
    private TextView mPrizeGroupSubTitles;//prize-add view huangpengfei-2016-8-11
    private TextView mPrizeGroupTitle;//prize-add view huangpengfei-2016-8-12
    private LinearLayout mPrizeGroupContent;//prize-add view huangpengfei-2016-8-12
    private StringBuilder mPrizeGroupSubTitlesStringBuilder = new StringBuilder();
    
    /*prize-add some view huangliemin-2016-5-26-start*/
    private int mDeleteSize;
    private List<String> mDeleteList;
    private View mDivider2;
    private String FirstNumber;
    /*prize-add some view huangliemin-2016-5-26-end*/
    /*prize-add-huangliemin-2016-7-16-start*/
    private DialogBottomMenu mBottomMenuDialog;
    private PopupWindow mPopupWindow;
    private View mPopuWindowView;
    /*prize-add-huangliemin-2016-7-16-start*/
    
    /**
     * QuickContacts immediately takes up the full screen. All possible information is shown.
     * This value for {@link android.provider.ContactsContract.QuickContact#EXTRA_MODE}
     * should only be used by the Contacts app.
     */
    public static final int MODE_FULLY_EXPANDED = 4;

    /** Used to pass the screen where the user came before launching this Activity. */
    public static final String EXTRA_PREVIOUS_SCREEN_TYPE = "previous_screen_type";

    private static final String TAG = "QuickContactActivity";

    private static final String KEY_THEME_COLOR = "theme_color";
    private static final String KEY_IS_SUGGESTION_LIST_COLLAPSED = "is_suggestion_list_collapsed";
    private static final String KEY_SELECTED_SUGGESTION_CONTACTS = "selected_suggestion_contacts";
    private static final String KEY_PREVIOUS_CONTACT_ID = "previous_contact_id";
    private static final String KEY_SUGGESTIONS_AUTO_SELECTED = "suggestions_auto_seleted";

    private static final int ANIMATION_STATUS_BAR_COLOR_CHANGE_DURATION = 150;
    private static final int REQUEST_CODE_CONTACT_EDITOR_ACTIVITY = 1;
    private static final int SCRIM_COLOR = Color.argb(0xC8, 0, 0, 0);
    private static final int REQUEST_CODE_CONTACT_SELECTION_ACTIVITY = 2;
    private static final String MIMETYPE_SMS = "vnd.android-dir/mms-sms";

    /** This is the Intent action to install a shortcut in the launcher. */
    private static final String ACTION_INSTALL_SHORTCUT =
            "com.android.launcher.action.INSTALL_SHORTCUT";

    @SuppressWarnings("deprecation")
    private static final String LEGACY_AUTHORITY = android.provider.Contacts.AUTHORITY;

    private static final String MIMETYPE_GPLUS_PROFILE =
            "vnd.android.cursor.item/vnd.googleplus.profile";
    private static final String GPLUS_PROFILE_DATA_5_ADD_TO_CIRCLE = "addtocircle";
    private static final String GPLUS_PROFILE_DATA_5_VIEW_PROFILE = "view";
    private static final String MIMETYPE_HANGOUTS =
            "vnd.android.cursor.item/vnd.googleplus.profile.comm";
    private static final String HANGOUTS_DATA_5_VIDEO = "hangout";
    private static final String HANGOUTS_DATA_5_MESSAGE = "conversation";
    private static final String CALL_ORIGIN_QUICK_CONTACTS_ACTIVITY =
            "com.android.contacts.quickcontact.QuickContactActivity";

	/*PRIZE-black number list-yuandailin-2016-1-29-start*/
	private static String prizeTwoDeminsionCodeNumber;
	private static ArrayList<String> numberList = new ArrayList<String>();
    /*PRIZE-black number list-yuandailin-2016-1-29-end*/
	
	//prize add the data for contact group hunangpengfei 2016-8-11
	private static List<String> listContactGroup = new ArrayList<String>();
    /**
     * The URI used to load the the Contact. Once the contact is loaded, use Contact#getLookupUri()
     * instead of referencing this URI.
     */
    private Uri mLookupUri;
    private String[] mExcludeMimes;
    private int mExtraMode;
    private String mExtraPrioritizedMimeType;
    private int mStatusBarColor;
    private boolean mHasAlreadyBeenOpened;
    private boolean mOnlyOnePhoneNumber;
    private boolean mOnlyOneEmail;

    private QuickContactImageView mPhotoView;
    private ExpandingEntryCardView mContactCard;
    /// M:[for RCS-e] show Joyn Card(rcs-e plugin) under ContactCard.
    private ExpandingEntryCardView mJoynCard;
    //private ExpandingEntryCardView mNoContactDetailsCard;//prize-remove huangpengfei-2016-8-20
    private ExpandingEntryCardView mRecentCard;
    private ExpandingEntryCardView mAboutCard;

    // Suggestion card.
    private CardView mCollapsedSuggestionCardView;
    private CardView mExpandSuggestionCardView;
    private View mCollapasedSuggestionHeader;
    private TextView mCollapsedSuggestionCardTitle;
    private TextView mExpandSuggestionCardTitle;
    private ImageView mSuggestionSummaryPhoto;
    private TextView mSuggestionForName;
    private TextView mSuggestionContactsNumber;
    private LinearLayout mSuggestionList;
    /*prize-change TextView-hpf-2017-12-12-start*/
    private TextView mSuggestionsCancelButton;
    private TextView mSuggestionsLinkButton;
    /*prize-change TextView-hpf-2017-12-12-end*/
    private boolean mIsSuggestionListCollapsed;
    private boolean mSuggestionsShouldAutoSelected = true;
    private long mPreviousContactId = 0;
    
    /*prize-add huangpengfei-2016-8-20 start*/
    private LinearLayout mNoContactDetailsLayout;
    private TextView mNoContactDataTextAddPhoneNum;
    private TextView mNoContactDataTextAddEmail;
    /*prize-add huangpengfei-2016-8-20 end*/
    /*prize-add for dido os8.0 -hpf-2017-8-7-start*/
    private View mPhotoContaienr;
    private TextView mPrizeTitle;
    private RelativeLayout mPrizeVideoCall;
    private ImageView mPrizeVideoCallIcon;
    private static boolean mPrizeIsVideoEnable = false;
    private PrizeBatteryBroadcastReciver mPrizeBatteryBroadcastReciver; 
    /*prize-add for dido os8.0 -hpf-2017-8-7-end*/

    private RelativeLayout mScroller; //zhangzhonghao change MultiShrinkScroller to RelativeLayout 20160322
    private SelectAccountDialogFragmentListener mSelectAccountFragmentListener;
    private AsyncTask<Void, Void, Cp2DataCardModel> mEntriesAndActionsTask;
    private AsyncTask<Void, Void, Void> mRecentDataTask;

    private AggregationSuggestionEngine mAggregationSuggestionEngine;
    private List<Suggestion> mSuggestions;

    private TreeSet<Long> mSelectedAggregationIds = new TreeSet<>();
    /**
     * The last copy of Cp2DataCardModel that was passed to {@link #populateContactAndAboutCard}.
     */
    private Cp2DataCardModel mCachedCp2DataCardModel;
    /**
     *  This scrim's opacity is controlled in two different ways. 1) Before the initial entrance
     *  animation finishes, the opacity is animated by a value animator. This is designed to
     *  distract the user from the length of the initial loading time. 2) After the initial
     *  entrance animation, the opacity is directly related to scroll position.
     */
    private ColorDrawable mWindowScrim;
    private boolean mIsEntranceAnimationFinished;
    private MaterialColorMapUtils mMaterialColorMapUtils;
    private boolean mIsExitAnimationInProgress;
    private boolean mHasComputedThemeColor;

    /**
     * Used to stop the ExpandingEntry cards from adjusting between an entry click and the intent
     * being launched.
     */
    private boolean mHasIntentLaunched;

    private Contact mContactData;
    private ContactLoader mContactLoader;
    private PorterDuffColorFilter mColorFilter;
    private int mColorFilterColor;

    private final ImageViewDrawableSetter mPhotoSetter = new ImageViewDrawableSetter();

    /**
     * {@link #LEADING_MIMETYPES} is used to sort MIME-types.
     *
     * <p>The MIME-types in {@link #LEADING_MIMETYPES} appear in the front of the dialog,
     * in the order specified here.</p>
     */
    private static final List<String> LEADING_MIMETYPES = Lists.newArrayList(
            Phone.CONTENT_ITEM_TYPE, SipAddress.CONTENT_ITEM_TYPE, Email.CONTENT_ITEM_TYPE,
            StructuredPostal.CONTENT_ITEM_TYPE);

    private static final List<String> SORTED_ABOUT_CARD_MIMETYPES = Lists.newArrayList(
            Nickname.CONTENT_ITEM_TYPE,
            // Phonetic name is inserted after nickname if it is available.
            // No mimetype for phonetic name exists.
            Website.CONTENT_ITEM_TYPE,
            Organization.CONTENT_ITEM_TYPE,
            Event.CONTENT_ITEM_TYPE,
            Relation.CONTENT_ITEM_TYPE,
            Im.CONTENT_ITEM_TYPE,
            GroupMembership.CONTENT_ITEM_TYPE,
            Identity.CONTENT_ITEM_TYPE,
            Note.CONTENT_ITEM_TYPE);

    private static final BidiFormatter sBidiFormatter = BidiFormatter.getInstance();

    /** Id for the background contact loader */
    private static final int LOADER_CONTACT_ID = 0;

    private static final String KEY_LOADER_EXTRA_PHONES =
            QuickContactActivity.class.getCanonicalName() + ".KEY_LOADER_EXTRA_PHONES";

    /** Id for the background Sms Loader */
    private static final int LOADER_SMS_ID = 1;
    private static final int MAX_SMS_RETRIEVE = 3;

    /** Id for the back Calendar Loader */
    private static final int LOADER_CALENDAR_ID = 2;
    private static final String KEY_LOADER_EXTRA_EMAILS =
            QuickContactActivity.class.getCanonicalName() + ".KEY_LOADER_EXTRA_EMAILS";
    private static final int MAX_PAST_CALENDAR_RETRIEVE = 3;
    private static final int MAX_FUTURE_CALENDAR_RETRIEVE = 3;
    private static final long PAST_MILLISECOND_TO_SEARCH_LOCAL_CALENDAR =
            1L * 24L * 60L * 60L * 1000L /* 1 day */;
    private static final long FUTURE_MILLISECOND_TO_SEARCH_LOCAL_CALENDAR =
            7L * 24L * 60L * 60L * 1000L /* 7 days */;

    /** Id for the background Call Log Loader */
    private static final int LOADER_CALL_LOG_ID = 3;
    private static final int MAX_CALL_LOG_RETRIEVE = 5;/*3;*///prize-chaneg-huangliemin-2016-7-22
    private static final int MIN_NUM_CONTACT_ENTRIES_SHOWN = 100;/*3;*///prize-chaneg-huangpengfei-2016-8-17
    private static final int MIN_NUM_COLLAPSED_RECENT_ENTRIES_SHOWN = 5;/*3;*///prize-change-huangliemin-2016-7-22
    private static final int CARD_ENTRY_ID_EDIT_CONTACT = -2;


    private static final int[] mRecentLoaderIds = new int[]{
        LOADER_SMS_ID,
        LOADER_CALENDAR_ID,
        LOADER_CALL_LOG_ID};
    /**
     * ConcurrentHashMap constructor params: 4 is initial table size, 0.9f is
     * load factor before resizing, 1 means we only expect a single thread to
     * write to the map so make only a single shard
     */
    private Map<Integer, List<ContactInteraction>> mRecentLoaderResults =
        new ConcurrentHashMap<>(4, 0.9f, 1);

    private static final String FRAGMENT_TAG_SELECT_ACCOUNT = "select_account_fragment";
    
    /*prize-add-huangliemin-2016-7-20-start*/
    private String[] mCallLogNumber = null;
    /*prize-add-huangliemin-2016-7-20-end*/
    /*prize-add-hpf-2018-1-6-start*/
    private boolean mIsPersonalInfo = false;
    private boolean mIsPersonalInfoFromEditor = false;
    /*prize-add-hpf-2018-1-6-end*/
    
    final OnClickListener mEntryClickHandler = new OnClickListener() {
        @Override
        public void onClick(View v) {
        	/*prize-add for BatteryBroadcastReciver huangpengfei-2017-6-26-start*/
        	if(R.id.prize_video_call == v.getId()){
        		if(PrizeBatteryBroadcastReciver.isBatteryLow){
             		android.widget.Toast.makeText(QuickContactActivity.this, R.string.prize_video_call_attention, 
	                            android.widget.Toast.LENGTH_SHORT).show();
             	}
        	}
        	/*prize-add for BatteryBroadcastReciver huangpengfei-2017-6-26-end*/
            final Object entryTagObject = v.getTag();
            if (entryTagObject == null || !(entryTagObject instanceof EntryTag)) {
                Log.w(TAG, "EntryTag was not used correctly");
                return;
            }
            final EntryTag entryTag = (EntryTag) entryTagObject;
            final Intent intent = entryTag.getIntent();
            final int dataId = entryTag.getId();
            Log.d(TAG, "[onClick]intent = " + intent + ",dataId = " + dataId);
            if (dataId == CARD_ENTRY_ID_EDIT_CONTACT) {
                editContact();
                return;
            }

            // Pass the touch point through the intent for use in the InCallUI
            if (Intent.ACTION_CALL.equals(intent.getAction())) {
                if (TouchPointManager.getInstance().hasValidPoint()) {
                    Bundle extras = new Bundle();
                    extras.putParcelable(TouchPointManager.TOUCH_POINT,
                            TouchPointManager.getInstance().getPoint());
                    intent.putExtra(TelecomManager.EXTRA_OUTGOING_CALL_EXTRAS, extras);
                }
            }

            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            mHasIntentLaunched = true;
            try {
                ImplicitIntentsUtil.startActivityInAppIfPossible(QuickContactActivity.this, intent);
            } catch (SecurityException ex) {
                Toast.makeText(QuickContactActivity.this, R.string.missing_app,
                        Toast.LENGTH_SHORT).show();
                Log.e(TAG, "QuickContacts does not have permission to launch "
                        + intent);
            } catch (ActivityNotFoundException ex) {
                Toast.makeText(QuickContactActivity.this, R.string.missing_app,
                        Toast.LENGTH_SHORT).show();
            }

            // Default to USAGE_TYPE_CALL. Usage is summed among all types for sorting each data id
            // so the exact usage type is not necessary in all cases
            String usageType = DataUsageFeedback.USAGE_TYPE_CALL;

            final Uri intentUri = intent.getData();
            if ((intentUri != null && intentUri.getScheme() != null &&
                    intentUri.getScheme().equals(ContactsUtils.SCHEME_SMSTO)) ||
                    (intent.getType() != null && intent.getType().equals(MIMETYPE_SMS))) {
                usageType = DataUsageFeedback.USAGE_TYPE_SHORT_TEXT;
            }

            // Data IDs start at 1 so anything less is invalid
            if (dataId > 0) {
                final Uri dataUsageUri = DataUsageFeedback.FEEDBACK_URI.buildUpon()
                        .appendPath(String.valueOf(dataId))
                        .appendQueryParameter(DataUsageFeedback.USAGE_TYPE, usageType)
                        .build();
                try {
                final boolean successful = getContentResolver().update(
                        dataUsageUri, new ContentValues(), null, null) > 0;
                if (!successful) {
                    Log.w(TAG, "DataUsageFeedback increment failed");
                }
                } catch (SecurityException ex) {
                    Log.w(TAG, "DataUsageFeedback increment failed", ex);
                }
            } else {
                Log.w(TAG, "Invalid Data ID");
            }
        }
    };

    final ExpandingEntryCardViewListener mExpandingEntryCardViewListener
            = new ExpandingEntryCardViewListener() {
        @Override
        public void onCollapse(int heightDelta) {
//            mScroller.prepareForShrinkingScrollChild(heightDelta); zhangzhonghao remove animation 20160322
        }

        @Override
        public void onExpand() {
//            mScroller.setDisableTouchesForSuppressLayout(/* areTouchesDisabled = */ true); zhangzhonghao remove animation 20160322
        }

        @Override
        public void onExpandDone() {
//            mScroller.setDisableTouchesForSuppressLayout(/* areTouchesDisabled = */ false); zhangzhonghao remove animation 20160322
        }
    };

    @Override
    public void onAggregationSuggestionChange() {
        Log.d(TAG, "[onAggregationSuggestionChange]");
        if (mAggregationSuggestionEngine == null) {
            return;
        }
        mSuggestions = mAggregationSuggestionEngine.getSuggestions();
        mCollapsedSuggestionCardView.setVisibility(View.GONE);
        mExpandSuggestionCardView.setVisibility(View.GONE);
        mSuggestionList.removeAllViews();

        if (mContactData == null) {
            return;
        }

        final String suggestionForName = mContactData.getDisplayName();
        final int suggestionNumber = mSuggestions.size();
        Log.d(TAG, "[onAggregationSuggestionChange] suggestionNumber=" + suggestionNumber
                + ", mSelectedAggregationIds=" + mSelectedAggregationIds);
        if (suggestionNumber <= 0) {
            mSelectedAggregationIds.clear();
            return;
        }

        ContactPhotoManager.DefaultImageRequest
                request = new ContactPhotoManager.DefaultImageRequest(
                suggestionForName, mContactData.getLookupKey(), ContactPhotoManager.TYPE_DEFAULT,
                /* isCircular */ true );
        final long photoId = mContactData.getPhotoId();
        final byte[] photoBytes = mContactData.getThumbnailPhotoBinaryData();
        if (photoBytes != null) {
            ContactPhotoManager.getInstance(this).loadThumbnail(mSuggestionSummaryPhoto, photoId,
                /* darkTheme */ false , /* isCircular */ true , request);
        } else {
            ContactPhotoManager.DEFAULT_AVATAR.applyDefaultImage(mSuggestionSummaryPhoto,
                    -1, false, request);
        }

        final String suggestionTitle = getResources().getQuantityString(
                R.plurals.quickcontact_suggestion_card_title, suggestionNumber, suggestionNumber);
        mCollapsedSuggestionCardTitle.setText(suggestionTitle);
        mExpandSuggestionCardTitle.setText(suggestionTitle);

        mSuggestionForName.setText(suggestionForName);
        final int linkedContactsNumber = mContactData.getRawContacts().size();
        final String contactsInfo;
        final String accountName = mContactData.getRawContacts().get(0).getAccountName();
        if (linkedContactsNumber == 1 && accountName == null) {
            mSuggestionContactsNumber.setVisibility(View.INVISIBLE);
        }
        if (linkedContactsNumber == 1 && accountName != null) {
            contactsInfo = getResources().getString(R.string.contact_from_account_name,
                    accountName);
        } else {
            contactsInfo = getResources().getString(
                    R.string.quickcontact_contacts_number, linkedContactsNumber);
        }
        mSuggestionContactsNumber.setText(contactsInfo);

        final Set<Long> suggestionContactIds = new HashSet<>();
        for (Suggestion suggestion : mSuggestions) {
            mSuggestionList.addView(inflateSuggestionListView(suggestion));
            suggestionContactIds.add(suggestion.contactId);
        }
        Log.d(TAG, "[onAggregationSuggestionChange]suggestionContactIds=" + suggestionContactIds);

        if (mIsSuggestionListCollapsed) {
            collapseSuggestionList();
        } else {
            expandSuggestionList();
        }

        // Remove contact Ids that are not suggestions.
        final Set<Long> selectedSuggestionIds = com.google.common.collect.Sets.intersection(
                mSelectedAggregationIds, suggestionContactIds);
        mSelectedAggregationIds = new TreeSet<>(selectedSuggestionIds);
        Log.d(TAG, "[onAggregationSuggestionChange]"
                + "mSelectedAggregationIds = " + mSelectedAggregationIds);
        if (!mSelectedAggregationIds.isEmpty()) {
            enableLinkButton();
        }

        /// M: ALPS02783465. not show duplicate contacts for sim contact.@{
        if (mContactData != null && mContactData.getIndicate() > 0) {
            Log.d(TAG, "[onAggregationSuggestionChange] sim disable duplicate card View");
            mCollapasedSuggestionHeader.setVisibility(View.GONE);
            mCollapsedSuggestionCardView.setVisibility(View.GONE);
        }
        /// @}
    }

    private void collapseSuggestionList() {
        mCollapsedSuggestionCardView.setVisibility(View.VISIBLE);
        mExpandSuggestionCardView.setVisibility(View.GONE);
        mIsSuggestionListCollapsed = true;
    }

    private void expandSuggestionList() {
        mCollapsedSuggestionCardView.setVisibility(View.GONE);
        mExpandSuggestionCardView.setVisibility(View.VISIBLE);
        mIsSuggestionListCollapsed = false;
    }

    private View inflateSuggestionListView(final Suggestion suggestion) {
        final LayoutInflater layoutInflater = LayoutInflater.from(this);
        final View suggestionView = layoutInflater.inflate(
                R.layout.quickcontact_suggestion_contact_item, null);

        ContactPhotoManager.DefaultImageRequest
                request = new ContactPhotoManager.DefaultImageRequest(
                suggestion.name, suggestion.lookupKey, ContactPhotoManager.TYPE_DEFAULT, /*
                isCircular */ true);
        final ImageView photo = (ImageView) suggestionView.findViewById(
                R.id.aggregation_suggestion_photo);
        if (suggestion.photo != null) {
            ContactPhotoManager.getInstance(this).loadThumbnail(photo, suggestion.photoId,
                   /* darkTheme */ false, /* isCircular */ true, request);
        } else {
            ContactPhotoManager.DEFAULT_AVATAR.applyDefaultImage(photo, -1, false, request);
        }

        final TextView name = (TextView) suggestionView.findViewById(R.id.aggregation_suggestion_name);
        name.setText(suggestion.name);

        final TextView accountNameView = (TextView) suggestionView.findViewById(
                R.id.aggregation_suggestion_account_name);
        final String accountName = suggestion.rawContacts.get(0).accountName;
        if (!TextUtils.isEmpty(accountName)) {
            accountNameView.setText(
                    getResources().getString(R.string.contact_from_account_name, accountName));
        } else {
            accountNameView.setVisibility(View.INVISIBLE);
        }

        final CheckBox checkbox = (CheckBox) suggestionView.findViewById(R.id.suggestion_checkbox);
        final int[][] stateSet = new int[][] {
                new int[] { android.R.attr.state_checked },
                new int[] { -android.R.attr.state_checked }
        };
        final int[] colors = new int[] { mColorFilterColor, mColorFilterColor };
        if (suggestion != null && suggestion.name != null) {
            checkbox.setContentDescription(suggestion.name + " " +
                    getResources().getString(R.string.contact_from_account_name, accountName));
        }
        //checkbox.setButtonTintList(new ColorStateList(stateSet, colors));//prize-remove-hpf-2017-12-12
        checkbox.setChecked(mSuggestionsShouldAutoSelected ||
                mSelectedAggregationIds.contains(suggestion.contactId));
        if (checkbox.isChecked()) {
            mSelectedAggregationIds.add(suggestion.contactId);
        }
        checkbox.setTag(suggestion.contactId);
        checkbox.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                final CheckBox checkBox = (CheckBox) v;
                final Long contactId = (Long) checkBox.getTag();
                if (mSelectedAggregationIds.contains(mContactData.getId())) {
                    mSelectedAggregationIds.remove(mContactData.getId());
                }
                if (checkBox.isChecked()) {
                    mSelectedAggregationIds.add(contactId);
                    if (mSelectedAggregationIds.size() >= 1) {
                        enableLinkButton();
                    }
                } else {
                    mSelectedAggregationIds.remove(contactId);
                    mSuggestionsShouldAutoSelected = false;
                    if (mSelectedAggregationIds.isEmpty()) {
                        disableLinkButton();
                    }
                }
            }
        });

        return suggestionView;
    }

    private void enableLinkButton() {
        mSuggestionsLinkButton.setClickable(true);
        /*prize-change-hpf-2017-12-12-start*/
        //mSuggestionsLinkButton.getBackground().setColorFilter(mColorFilter);
        mSuggestionsLinkButton.setTextColor(
                ContextCompat.getColor(this, /*android.R.color.white*/R.color.prize_button_text_default_color));
        /*prize-change-hpf-2017-12-12-end*/
        mSuggestionsLinkButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "click link button, mContactData.getId()=" + mContactData.getId()
                        + ", mSelectedAggregationIds=" + mSelectedAggregationIds);
                // Join selected contacts.
                if (!mSelectedAggregationIds.contains(mContactData.getId())) {
                    mSelectedAggregationIds.add(mContactData.getId());
                }
                JoinContactsDialogFragment.start(
                        QuickContactActivity.this, mSelectedAggregationIds);
            }
        });
    }

    @Override
    public void onContactsJoined() {
        disableLinkButton();
    }

    private void disableLinkButton() {
        mSuggestionsLinkButton.setClickable(false);
        /*prize-change-hpf-2017-12-12-start*/
        /*mSuggestionsLinkButton.getBackground().setColorFilter(
                ContextCompat.getColor(this, R.color.disabled_button_background),
                PorterDuff.Mode.SRC_ATOP);*/
        mSuggestionsLinkButton.setTextColor(
                ContextCompat.getColor(this, /*R.color.disabled_button_text*/R.color.prize_button_text_dark_color));
        /*prize-change-hpf-2017-12-12-end*/
    }

    private interface ContextMenuIds {
        static final int COPY_TEXT = 0;
        static final int CLEAR_DEFAULT = 1;
        static final int SET_DEFAULT = 2;
        /// M: add ip call
        static final int IP_CALL = 3;
    }

    private final OnCreateContextMenuListener mEntryContextMenuListener =
            new OnCreateContextMenuListener() {
        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
            if (menuInfo == null) {
                return;
            }
            final EntryContextMenuInfo info = (EntryContextMenuInfo) menuInfo;
            menu.setHeaderTitle(info.getCopyText());
            /// M: add ip call
            if (Phone.CONTENT_ITEM_TYPE.equals(info.getMimeType()) &&
                    PhoneCapabilityTester.isPhone(ContactsApplication.getInstance())) {
                menu.add(ContextMenu.NONE, ContextMenuIds.IP_CALL,
                    ContextMenu.NONE, getString(R.string.contact_detail_ip_call));
            }
            menu.add(ContextMenu.NONE, ContextMenuIds.COPY_TEXT,
                    ContextMenu.NONE, getString(R.string.copy_text));

            // Don't allow setting or clearing of defaults for non-editable contacts
            if (!isContactEditable()) {
                return;
            }

            final String selectedMimeType = info.getMimeType();

            // Defaults to true will only enable the detail to be copied to the clipboard.
            boolean onlyOneOfMimeType = true;

            // Only allow primary support for Phone and Email content types
            if (Phone.CONTENT_ITEM_TYPE.equals(selectedMimeType)) {
                onlyOneOfMimeType = mOnlyOnePhoneNumber;
            } else if (Email.CONTENT_ITEM_TYPE.equals(selectedMimeType)) {
                onlyOneOfMimeType = mOnlyOneEmail;
            }

            // Checking for previously set default
            if (info.isSuperPrimary()) {
                menu.add(ContextMenu.NONE, ContextMenuIds.CLEAR_DEFAULT,
                        ContextMenu.NONE, getString(R.string.clear_default));
            } else if (!onlyOneOfMimeType) {
                menu.add(ContextMenu.NONE, ContextMenuIds.SET_DEFAULT,
                        ContextMenu.NONE, getString(R.string.set_default));
            }
        }
    };

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        EntryContextMenuInfo menuInfo;
        try {
            menuInfo = (EntryContextMenuInfo) item.getMenuInfo();
        } catch (ClassCastException e) {
            Log.e(TAG, "bad menuInfo", e);
            return false;
        }

        switch (item.getItemId()) {
            /// M: add ip call
            case ContextMenuIds.IP_CALL:
                QuickContactUtils.dialIpCall(this, menuInfo.getCopyText());
                return true;
            case ContextMenuIds.COPY_TEXT:
                ClipboardUtils.copyText(this, menuInfo.getCopyLabel(), menuInfo.getCopyText(),
                        true);
                return true;
            case ContextMenuIds.SET_DEFAULT:
                final Intent setIntent = ContactSaveService.createSetSuperPrimaryIntent(this,
                        menuInfo.getId());
                this.startService(setIntent);
                return true;
            case ContextMenuIds.CLEAR_DEFAULT:
                final Intent clearIntent = ContactSaveService.createClearPrimaryIntent(this,
                        menuInfo.getId());
                this.startService(clearIntent);
                return true;
            default:
                throw new IllegalArgumentException("Unknown menu option " + item.getItemId());
        }
    }

    /**
     * Headless fragment used to handle account selection callbacks invoked from
     * {@link DirectoryContactUtil}.
     */
    public static class SelectAccountDialogFragmentListener extends Fragment
            implements SelectAccountDialogFragment.Listener {

        private QuickContactActivity mQuickContactActivity;

        public SelectAccountDialogFragmentListener() {}

        @Override
        public void onAccountChosen(AccountWithDataSet account, Bundle extraArgs) {
            DirectoryContactUtil.createCopy(mQuickContactActivity.mContactData.getContentValues(),
                    account, mQuickContactActivity);
        }

        @Override
        public void onAccountSelectorCancelled() {}

        /**
         * Set the parent activity. Since rotation can cause this fragment to be used across
         * more than one activity instance, we need to explicitly set this value instead
         * of making this class non-static.
         */
        public void setQuickContactActivity(QuickContactActivity quickContactActivity) {
            mQuickContactActivity = quickContactActivity;
        }
    }

    /* prize remove animation zhangzhonghao 20160325 start */
//    final MultiShrinkScrollerListener mMultiShrinkScrollerListener
//            = new MultiShrinkScrollerListener() {
//        @Override
//        public void onScrolledOffBottom() {
//            finish();
//        }
//
//        @Override
//        public void onEnterFullscreen() {
//            updateStatusBarColor();
//        }
//
//        @Override
//        public void onExitFullscreen() {
//            updateStatusBarColor();
//        }
//
//        @Override
//        public void onStartScrollOffBottom() {
//            mIsExitAnimationInProgress = true;
//        }
//
//        @Override
//        public void onEntranceAnimationDone() {
//            mIsEntranceAnimationFinished = true;
//        }
//
//        @Override
//        public void onTransparentViewHeightChange(float ratio) {
//            if (mIsEntranceAnimationFinished) {
//                mWindowScrim.setAlpha((int) (0xFF * ratio));
//            }
//        }
//    };
    /* prize remove animation zhangzhonghao 20160325 end */


    /**
     * Data items are compared to the same mimetype based off of three qualities:
     * 1. Super primary
     * 2. Primary
     * 3. Times used
     */
    private final Comparator<DataItem> mWithinMimeTypeDataItemComparator =
            new Comparator<DataItem>() {
        @Override
        public int compare(DataItem lhs, DataItem rhs) {
            if (!lhs.getMimeType().equals(rhs.getMimeType())) {
                Log.wtf(TAG, "Comparing DataItems with different mimetypes lhs.getMimeType(): " +
                        lhs.getMimeType() + " rhs.getMimeType(): " + rhs.getMimeType());
                return 0;
            }

            if (lhs.isSuperPrimary()) {
                return -1;
            } else if (rhs.isSuperPrimary()) {
                return 1;
            } else if (lhs.isPrimary() && !rhs.isPrimary()) {
                return -1;
            } else if (!lhs.isPrimary() && rhs.isPrimary()) {
                return 1;
            } else {
                final int lhsTimesUsed =
                        lhs.getTimesUsed() == null ? 0 : lhs.getTimesUsed();
                final int rhsTimesUsed =
                        rhs.getTimesUsed() == null ? 0 : rhs.getTimesUsed();

                return rhsTimesUsed - lhsTimesUsed;
            }
        }
    };

    /**
     * Sorts among different mimetypes based off:
     * 1. Whether one of the mimetypes is the prioritized mimetype
     * 2. Number of times used
     * 3. Last time used
     * 4. Statically defined
     */
    private final Comparator<List<DataItem>> mAmongstMimeTypeDataItemComparator =
            new Comparator<List<DataItem>> () {
        @Override
        public int compare(List<DataItem> lhsList, List<DataItem> rhsList) {
            final DataItem lhs = lhsList.get(0);
            final DataItem rhs = rhsList.get(0);
            final String lhsMimeType = lhs.getMimeType();
            final String rhsMimeType = rhs.getMimeType();

            // 1. Whether one of the mimetypes is the prioritized mimetype
            if (!TextUtils.isEmpty(mExtraPrioritizedMimeType) && !lhsMimeType.equals(rhsMimeType)) {
                if (rhsMimeType.equals(mExtraPrioritizedMimeType)) {
                    return 1;
                }
                if (lhsMimeType.equals(mExtraPrioritizedMimeType)) {
                    return -1;
                }
            }

            // 2. Number of times used
            final int lhsTimesUsed = lhs.getTimesUsed() == null ? 0 : lhs.getTimesUsed();
            final int rhsTimesUsed = rhs.getTimesUsed() == null ? 0 : rhs.getTimesUsed();
            final int timesUsedDifference = rhsTimesUsed - lhsTimesUsed;
            if (timesUsedDifference != 0) {
                return timesUsedDifference;
            }

            // 3. Last time used
            final long lhsLastTimeUsed =
                    lhs.getLastTimeUsed() == null ? 0 : lhs.getLastTimeUsed();
            final long rhsLastTimeUsed =
                    rhs.getLastTimeUsed() == null ? 0 : rhs.getLastTimeUsed();
            final long lastTimeUsedDifference = rhsLastTimeUsed - lhsLastTimeUsed;
            if (lastTimeUsedDifference > 0) {
                return 1;
            } else if (lastTimeUsedDifference < 0) {
                return -1;
            }

            // 4. Resort to a statically defined mimetype order.
            if (!lhsMimeType.equals(rhsMimeType)) {
            for (String mimeType : LEADING_MIMETYPES) {
                if (lhsMimeType.equals(mimeType)) {
                    return -1;
                } else if (rhsMimeType.equals(mimeType)) {
                    return 1;
                }
            }
            }
            return 0;
        }
    };

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            TouchPointManager.getInstance().setPoint((int) ev.getRawX(), (int) ev.getRawY());
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Trace.beginSection("onCreate()");
        super.onCreate(savedInstanceState);

        if (RequestPermissionsActivity.startPermissionActivity(this) ||
                RequestDesiredPermissionsActivity.startPermissionActivity(this)) {
            return;
        }
        
        /*PRIZE-add aidl for get location by service -qiaohu-2018-6-11 -start*/
        if("1".equals(android.os.SystemProperties.get("ro.prize_cootek_enable", "1"))) {
        	Intent intent = new Intent("com.android.dialer.service.ILocationService");
            intent.setComponent(new ComponentName("com.android.dialer", "com.android.dialer.service.LocationService"));
            boolean b = bindService(intent,
    				serConn, Context.BIND_AUTO_CREATE);
        }
        /*PRIZE-add aidl for get location by service -qiaohu-2018-6-11 -end*/
        
        listContactGroup.clear();//prize clear data huangpengfei 2016-9-6
        
        final int previousScreenType = getIntent().getIntExtra
                (EXTRA_PREVIOUS_SCREEN_TYPE, ScreenType.UNKNOWN);
        Logger.logScreenView(this, ScreenType.QUICK_CONTACT, previousScreenType);

        if (CompatUtils.isLollipopCompatible()) {
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        initStatusBar();
        
        processIntent(getIntent());

        // Show QuickContact in front of soft input
         getWindow().setFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM,
                WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);

         /*prize adaptation big-font by zhaojian 20180601 start*/
        boolean isPrizeFontSize = SystemProperties.getBoolean("persist.sys.prize.fontsize",false);
        if(isPrizeFontSize){
            setContentView(R.layout.prize_quickcontact_activity_big_font);
        }else{
            setContentView(R.layout.quickcontact_activity);
        }
        //setContentView(R.layout.quickcontact_activity);
        /*prize adaptation big-font by zhaojian 20180601 end*/

        mMaterialColorMapUtils = new MaterialColorMapUtils(getResources());

        mScroller = (RelativeLayout) findViewById(R.id.multiscroller); //zhangzhonghao change MultiShrinkScroller to RelativeLayout 20160322

        mContactCard = (ExpandingEntryCardView) findViewById(R.id.communication_card);
        /// M: [for rcs-e]
        mJoynCard = (ExpandingEntryCardView) ExtensionManager.getInstance()
            .getViewCustomExtension().getQuickContactCardViewCustom().createCardView(
                    (LinearLayout)findViewById(R.id.card_container),
                    (View) mContactCard, mLookupUri, this);
        /* prize-add huangpengfei-2016-8-12 start*/
        mPrizeGroupSubTitles = (TextView)findViewById(R.id.prize_group_subTitles);
        mPrizeGroupTitle = (TextView)findViewById(R.id.prize_group_title);
        mPrizeGroupContent = (LinearLayout)findViewById(R.id.prize_group_content);
        /* prize-add huangpengfei-2016-8-12 end*/
        
        //mNoContactDetailsCard = (ExpandingEntryCardView) findViewById(R.id.no_contact_data_card);//prize-remove huangpengfei-2016-8-20
        
        /* prize-add huangpengfei-2016-8-20 start*/
        mNoContactDetailsLayout = (LinearLayout)findViewById(R.id.no_contact_data_layout);
        mNoContactDataTextAddPhoneNum = (TextView)findViewById(R.id.no_contact_data_text_add_phone_num);
        mNoContactDataTextAddEmail = (TextView)findViewById(R.id.no_contact_data_add_email_address);
        mNoContactDataTextAddPhoneNum.setOnClickListener(this);
        mNoContactDataTextAddEmail.setOnClickListener(this);
        /* prize-add huangpengfei-2016-8-20 end*/
        
        mRecentCard = (ExpandingEntryCardView) findViewById(R.id.recent_card);
        mAboutCard = (ExpandingEntryCardView) findViewById(R.id.about_card);

        mCollapsedSuggestionCardView = (CardView) findViewById(R.id.collapsed_suggestion_card);
        mExpandSuggestionCardView = (CardView) findViewById(R.id.expand_suggestion_card);
        mCollapasedSuggestionHeader = findViewById(R.id.collapsed_suggestion_header);
        mCollapsedSuggestionCardTitle = (TextView) findViewById(
                R.id.collapsed_suggestion_card_title);
        mExpandSuggestionCardTitle = (TextView) findViewById(R.id.expand_suggestion_card_title);
        mSuggestionSummaryPhoto = (ImageView) findViewById(R.id.suggestion_icon);
        mSuggestionForName = (TextView) findViewById(R.id.suggestion_for_name);
        mSuggestionContactsNumber = (TextView) findViewById(R.id.suggestion_for_contacts_number);
        mSuggestionList = (LinearLayout) findViewById(R.id.suggestion_list);
        /*prize-change TextView-hpf-2017-12-12-start*/
        mSuggestionsCancelButton= (TextView) findViewById(R.id.cancel_button);
        mSuggestionsLinkButton = (TextView) findViewById(R.id.link_button);
        /*prize-change TextView-hpf-2017-12-12-end*/
        if (savedInstanceState != null) {
            mIsSuggestionListCollapsed = savedInstanceState.getBoolean(
                    KEY_IS_SUGGESTION_LIST_COLLAPSED, true);
            mPreviousContactId = savedInstanceState.getLong(KEY_PREVIOUS_CONTACT_ID);
            mSuggestionsShouldAutoSelected = savedInstanceState.getBoolean(
                    KEY_SUGGESTIONS_AUTO_SELECTED, true);
            mSelectedAggregationIds = (TreeSet<Long>)
                    savedInstanceState.getSerializable(KEY_SELECTED_SUGGESTION_CONTACTS);
        } else {
            mIsSuggestionListCollapsed = true;
            mSelectedAggregationIds.clear();
        }
        if (mSelectedAggregationIds.isEmpty()) {
            disableLinkButton();
        } else {
            enableLinkButton();
        }
        mCollapasedSuggestionHeader.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mCollapsedSuggestionCardView.setVisibility(View.GONE);
                mExpandSuggestionCardView.setVisibility(View.VISIBLE);
                mIsSuggestionListCollapsed = false;
                mExpandSuggestionCardTitle.requestFocus();
                mExpandSuggestionCardTitle.sendAccessibilityEvent(
                        AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED);
            }
        });

        mSuggestionsCancelButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mCollapsedSuggestionCardView.setVisibility(View.VISIBLE);
                mExpandSuggestionCardView.setVisibility(View.GONE);
                mIsSuggestionListCollapsed = true;
            }
        });

        //mNoContactDetailsCard.setOnClickListener(mEntryClickHandler);
        mContactCard.setOnClickListener(mEntryClickHandler);
        mContactCard.setExpandButtonText(
        getResources().getString(R.string.expanding_entry_card_view_see_all));
        mContactCard.setOnCreateContextMenuListener(mEntryContextMenuListener);

        mRecentCard.setOnClickListener(mEntryClickHandler);
        mRecentCard.setTitle(getResources().getString(R.string.recent_card_title));

        mAboutCard.setOnClickListener(mEntryClickHandler);
        mAboutCard.setOnCreateContextMenuListener(mEntryContextMenuListener);

        mPhotoView = (QuickContactImageView) findViewById(R.id.photo);

        //M:OP01 RCS will go to contact detail activity, update photo from rcs server.@{
        Log.d(TAG,"[updateContactPhotoFromRcsServer]"+mLookupUri.toString());
        ExtensionManager.getInstance().getRcsExtension()
                .updateContactPhotoFromRcsServer(mLookupUri, mPhotoView, this);
        /** @} */

        final View transparentView = findViewById(R.id.transparent_view);
        
        /* prize zhangzhonghao remove animation 20160322 start */
//        if (mScroller != null) {
//            transparentView.setOnClickListener(new OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    mScroller.scrollOffBottom();
//                }
//            });
//        }
        /* prize zhangzhonghao remove animation 20160322 end */

        // Allow a shadow to be shown under the toolbar.
        ViewUtil.addRectangularOutlineProvider(findViewById(R.id.toolbar_parent), getResources());

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setActionBar(toolbar);
        
        //getActionBar().setTitle(null);
        /*prize-remove for dido os8.0 -hpf-2017-8-23-start*/
        //toolbar.setTitleTextColor(getResources().getColor(R.color.contacts_accent_color));
        //((TextView)toolbar.getChildAt(0)).setTextSize(16);
        /*prize-remove for dido os8.0 -hpf-2017-8-23-end*/
        getActionBar().setDisplayHomeAsUpEnabled(true);
       
        // Put a TextView with a known resource id into the ActionBar. This allows us to easily
        // find the correct TextView location & size later.
        toolbar.addView(getLayoutInflater().inflate(R.layout.quickcontact_title_placeholder,null));
        /*prize-add for dido os8.0 -hpf-2017-8-8-start*/
        mPhotoContaienr = findViewById(R.id.toolbar_parent);
        mPrizeTitle = (TextView)findViewById(R.id.prize_title);
        mPrizeVideoCall = (RelativeLayout)findViewById(R.id.prize_video_call);
        mPrizeVideoCallIcon = (ImageView)findViewById(R.id.prize_video_call_icon);
        initRandomContactsBg();
        /*prize-add- for LiuHai screen-hpf-2018-4-20-start*/
        boolean hasSystemFeature = getPackageManager().hasSystemFeature("com.prize.notch.screen");
        RelativeLayout.LayoutParams rp = (RelativeLayout.LayoutParams)mPrizeTitle.getLayoutParams();
        if(!hasSystemFeature){
        	rp.addRule(RelativeLayout.CENTER_HORIZONTAL);
        }
        /*prize-add- for LiuHai screen-hpf-2018-4-20-end*/
        /*prize-add for dido os8.0 -hpf-2017-8-8-end*/
        
        /*prize-add for BatteryBroadcastReciver huangpengfei-2017-6-26-start*/
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        mPrizeBatteryBroadcastReciver = new PrizeBatteryBroadcastReciver();
        registerReceiver(mPrizeBatteryBroadcastReciver, intentFilter);
        /*prize-add for BatteryBroadcastReciver huangpengfei-2017-6-26-end*/
        
        mHasAlreadyBeenOpened = savedInstanceState != null;
        mIsEntranceAnimationFinished = mHasAlreadyBeenOpened;
        mWindowScrim = new ColorDrawable(SCRIM_COLOR);
        mWindowScrim.setAlpha(0);
        getWindow().setBackgroundDrawable(mWindowScrim);

//        mScroller.initialize(mMultiShrinkScrollerListener, mExtraMode == MODE_FULLY_EXPANDED); zhangzhonghao remove animation 20160322
        // mScroller needs to perform asynchronous measurements after initalize(), therefore
        // we can't mark this as GONE.
        mScroller.setVisibility(View.INVISIBLE);
        
        /* prize remove card elevation zhangzhonghao 20160325 start */
        mContactCard.setCardElevation(0);     
        mRecentCard.setCardElevation(0);
        mAboutCard.setCardElevation(0);
        //mNoContactDetailsCard.setCardElevation(0);//prize-remove huangpengfei-2016-8-20
        /* prize remove card elevation zhangzhonghao 20160325 end */
        
        /* prize instance our view object zhangzhonghao 20160314 start */
        prizeContactEditor = (ImageButton) findViewById(R.id.prize_contacts_editor);
        prizeContactDelete = (ImageButton) findViewById(R.id.prize_contacts_delete);
        prizeContactMoreMenu = (ImageButton) findViewById(R.id.prize_contacts_more_menu);//prize-add-huangliemin-2016-7-16
        prizeQuickBottomButtonLinearLayout = (LinearLayout) findViewById(R.id.prize_bottom_button);
        prizeQuickDefaultMusiceLinearLayout = (RelativeLayout) findViewById(R.id.mDefaultMusic);
        prizeQuickDefaultMusicTitle = (TextView) findViewById(R.id.mDefaultMusicTitle);
        prizeQuickDefaultMusicName = (TextView) findViewById(R.id.mDefaultMusicName);
        prizeQuickTwoDeminsionLinearLayout = (LinearLayout) findViewById(R.id.mTwoDeminsionCode);
        prizeQuickNoRecentsNotice = (TextView) findViewById(R.id.prizeQuickContactRecentNotice);
        /*prize-add-huangliemin-2016-7-20-start*/
        prizeQuickNoRecentsNotice.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(mCallLogNumber!=null && mCallLogNumber.length>0) {
					Intent intent = new Intent();
					intent.setAction("com.android.prize.CallLogActivity");
					intent.putExtra("KEY_LOADER_EXTRA_PHONES", mCallLogNumber);
					startActivity(intent);
				}
			}
		});
        /*prize-add-huangliemin-2016-7-20-end*/
        prizeContactPhotoRelativeLayout = (RelativeLayout) findViewById(R.id.mPhotoRelative);
        /* prize instance our view object zhangzhonghao 20160314 end */
        
        /*prize-add-huangliemin-2016-7-18-start*/
        PrizeContactsEditorText = (TextView)findViewById(R.id.prize_contacts_editor_text);
        PrizeContactsDeleteLayout = (LinearLayout)findViewById(R.id.prize_contacts_delete_layout);
        PrizeContactsDeleteLayout.setOnClickListener(this);
        PrizeContactsDeleteTextView = (TextView)findViewById(R.id.prize_contacts_delete_text);
        /*prize-add-huangliemin-2016-7-18-end*/
        
        /* prize add adapter zhangzhonghao 20160314 start */
        prizeQuickDefaultMusiceLinearLayout.setOnClickListener(this);
        prizeQuickTwoDeminsionLinearLayout.setOnClickListener(this);
        /* prize add adapter zhangzhonghao 20160314 end */
        /*prize-add huangliemin-2016-5-26-start*/
        mDivider2 = (View)findViewById(R.id.mDivider2);
        /*prize-add huangliemin-2016-5-26-end*/
        
        /*prize-add huangpengfei-2016-8-9-start*/
        LinearLayout prizeContactsEditorLayout = (LinearLayout)findViewById(R.id.prize_contacts_editor_layout);
        LinearLayout prizeContactsMoreLayout = (LinearLayout)findViewById(R.id.prize_contacts_more_menu_layout);
        prizeContactsMoreMenuText = (TextView)findViewById(R.id.prize_contacts_more_menu_text);
        prizeContactsMoreLayout.setOnClickListener(this);
        prizeContactsEditorLayout.setOnClickListener(this);
        

        /*prize-add huangpengfei-2016-8-9-end*/
        
        /* prize change recentcard visibilities zhangzhonghao 20160323 start */
        if(mExtraMode != 31){
            hideRecentCard();
        }else{
            hideDetailCard();
        }
        /* prize change recentcard visibilities zhangzhonghao 20160323 end */

        setHeaderNameText(R.string.missing_name);

        mSelectAccountFragmentListener= (SelectAccountDialogFragmentListener) getFragmentManager()
                .findFragmentByTag(FRAGMENT_TAG_SELECT_ACCOUNT);
        if (mSelectAccountFragmentListener == null) {
            mSelectAccountFragmentListener = new SelectAccountDialogFragmentListener();
            getFragmentManager().beginTransaction().add(0, mSelectAccountFragmentListener,
                    FRAGMENT_TAG_SELECT_ACCOUNT).commit();
            mSelectAccountFragmentListener.setRetainInstance(true);
        }
        mSelectAccountFragmentListener.setQuickContactActivity(this);

        /* prize zhangzhonghao remove animation 20160322 start */
//        SchedulingUtils.doOnPreDraw(mScroller, /* drawNextFrame = */ true,
//                new Runnable() {
//                    @Override
//                    public void run() {
//                        if (!mHasAlreadyBeenOpened) {
//                            // The initial scrim opacity must match the scrim opacity that would be
//                            // achieved by scrolling to the starting position.
//                            final float alphaRatio = mExtraMode == MODE_FULLY_EXPANDED ?
//                                    1 : mScroller.getStartingTransparentHeightRatio();
//                            final int duration = getResources().getInteger(
//                                    android.R.integer.config_shortAnimTime);
//                            final int desiredAlpha = (int) (0xFF * alphaRatio);
//                            ObjectAnimator o = ObjectAnimator.ofInt(mWindowScrim, "alpha", 0,
//                                    desiredAlpha).setDuration(duration);
//
//                            o.start();
//                        }
//                    }
//                });
        /* prize zhangzhonghao remove animation 20160322 end */

        if (savedInstanceState != null) {
            final int color = savedInstanceState.getInt(KEY_THEME_COLOR, 0);
            SchedulingUtils.doOnPreDraw(mScroller, /* drawNextFrame = */ false,
                    new Runnable() {
                        @Override
                        public void run() {
                            // Need to wait for the pre draw before setting the initial scroll
                            // value. Prior to pre draw all scroll values are invalid.
                            if (mHasAlreadyBeenOpened) {
                                mScroller.setVisibility(View.VISIBLE);
//                                mScroller.setScroll(mScroller.getScrollNeededToBeFullScreen()); zhangzhonghao remove animation 20160322
                            }
                            // Need to wait for pre draw for setting the theme color. Setting the
                            // header tint before the MultiShrinkScroller has been measured will
                            // cause incorrect tinting calculations.
                            if (color != 0) {
                                setThemeColor(mMaterialColorMapUtils
                                        .calculatePrimaryAndSecondaryColor(color));
                            }
                        }
                    });
        }
        
        Trace.endSection();
    }
    /*PRIZE-add aidl for get location by service -qiaohu-2018-6-11 -start*/
    private boolean mIsContact = true;
    
    private ILocationService locationService = null;
    
    private ServiceConnection serConn = new ServiceConnection() {
		
		@Override
		public void onServiceDisconnected(ComponentName name) {
			Log.v(TAG, "onServiceDisconnected() called");
			locationService = null;
		}
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			Log.v(TAG, "onServiceConnected() called");
			locationService = ILocationService.Stub.asInterface(service);
		}
	};
	
	private String getLocation(String num) {
		String geo = "";
		try {
			geo = locationService.getLocationInfo(num);
		} catch (Exception e) {
			Log.v(TAG, "getLocation exception");
			e.printStackTrace();
		}
		Log.v(TAG, "getLocation geo = " + geo);
		return geo;
	}
	/*PRIZE-add aidl for get location by service -qiaohu-2018-6-11 -end*/
	
    
  /*PRIZE-add dialog for delete callrencents -huangliemin-2016-5-27 -start*/
    private void prizeAddToContacts(){
    	/*prize-change-huangliemin-2016-7-28-start*/
    	if(numberList!=null && numberList.size()>0) {
    		String phoneNumber = numberList.get(0);
    		Intent intent = new Intent(Intent.ACTION_INSERT_OR_EDIT);
            /*prize add for bug 55239 by zhaojian 20180502 start*/
            intent.setClassName(PRIZE_CONTACTS_PACKAGE_NAME,CONTACTS_SELECTION_ACTIVITY_NAME);
            /*prize add for bug 55239 by zhaojian 20180502 end*/
    		intent.setType(ContactsContract.Contacts.CONTENT_ITEM_TYPE);
    		intent.putExtra(ContactsContract.Intents.Insert.PHONE, phoneNumber);
    		intent.putExtra(ContactEditorActivity.INTENT_KEY_FINISH_ACTIVITY_ON_SAVE_COMPLETED, true);
    		this.startActivityForResult(intent, REQUEST_CODE_CONTACT_SELECTION_ACTIVITY);
    	}
		/*prize-change-huangliemin-2016-7-28-end*/
    }
    
    private void prizeCallRecentsDelete(){
        AlertDialog.Builder builder = new Builder(QuickContactActivity.this);
        builder.setMessage(getResources().getString(R.string.prize_recents_delete_confirm))
			.setTitle(getResources().getString(R.string.prize_recents_delete_prompt))
			.setPositiveButton(getResources().getString(R.string.prize_recents_delete_ok), new DialogInterface.OnClickListener() {
            
            @Override
            public void onClick(DialogInterface dialog, int which) {
               if(mDeleteList !=null && mDeleteList.size()>0){//PRIZE-add-yuandailin-2016-5-20
                for(int i=0;i<mDeleteList.size();i++){
					getContentResolver().delete(CallLog.Calls.CONTENT_URI,"number=?",new String[]{mDeleteList.get(i)});
				 }
               	}
				mRecentCard.setVisibility(View.GONE);
				/*prize-add-huangliemin-2016-7-22-start*/
				prizeQuickNoRecentsNotice.setVisibility(View.GONE);
				/*prize-add-huangliemin-2016-7-22-end*/
				
            }
        }).setNegativeButton(getResources().getString(R.string.prize_recents_delete_cancel), new DialogInterface.OnClickListener() {
            
            @Override
            public void onClick(DialogInterface dialog, int which) {
                
            }
        }).create().show();
    }
    /*PRIZE-add dialog for delete callrencents -huangliemin-2016-5-27 -end*/

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "[onActivityResult] requestCode is " + requestCode
                + ", resultCode is " + resultCode);
        final boolean deletedOrSplit = requestCode == REQUEST_CODE_CONTACT_EDITOR_ACTIVITY &&
                (resultCode == ContactDeletionInteraction.RESULT_CODE_DELETED ||
                resultCode == ContactEditorBaseActivity.RESULT_CODE_SPLIT);
        if (deletedOrSplit) {
            finish();
        } else if (requestCode == REQUEST_CODE_CONTACT_SELECTION_ACTIVITY &&
                resultCode != RESULT_CANCELED) {
            /* M: [Google Issue]ALPS03375904
             * fix photo view display issue (tint and image blend together),
             * needs to re-compute theme color @{ */
            mHasComputedThemeColor = false;
            /* @} */
            processIntent(data);
        }
        
        /* prize set default music zhangzhonghao 20160328 start */
        else if (requestCode == 8) {
            if (data != null) {
                final Uri pickedUri = data.getParcelableExtra(
                        RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                onRingtonePicked(pickedUri);
            }
        }
        /* prize set default music zhangzhonghao 20160328 end */
        
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d(TAG,"[onNewIntent]");
        mHasAlreadyBeenOpened = true;
        mIsEntranceAnimationFinished = true;
        mHasComputedThemeColor = false;
        listContactGroup.clear();//prize clear group data huangpengfei 2016-8-12
        processIntent(intent);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        if (mColorFilter != null) {
            savedInstanceState.putInt(KEY_THEME_COLOR, mColorFilterColor);
        }
        savedInstanceState.putBoolean(KEY_IS_SUGGESTION_LIST_COLLAPSED, mIsSuggestionListCollapsed);
        savedInstanceState.putLong(KEY_PREVIOUS_CONTACT_ID, mPreviousContactId);
        savedInstanceState.putBoolean(
                KEY_SUGGESTIONS_AUTO_SELECTED, mSuggestionsShouldAutoSelected);
        savedInstanceState.putSerializable(
                KEY_SELECTED_SUGGESTION_CONTACTS, mSelectedAggregationIds);
    }

    private void processIntent(Intent intent) {
        if (intent == null) {
            Log.w(TAG, "[processIntent]intent is null,return!");
            finish();
            return;
        }
        /*prize-add-hpf-2018-1-6-start*/
        mIsPersonalInfo = intent.getBooleanExtra("personal", false);
        mIsPersonalInfoFromEditor = intent.getBooleanExtra("isPersonalInformation", false);
        /*prize-add-hpf-2018-1-6-end*/
        
        Log.d(TAG, "[processIntent]: intent = " + intent);
        Uri lookupUri = intent.getData();
        Log.d(TAG, "The original uri from intent: " + lookupUri);

        // Check to see whether it comes from the old version.
        if (lookupUri != null && LEGACY_AUTHORITY.equals(lookupUri.getAuthority())) {
            final long rawContactId = ContentUris.parseId(lookupUri);
            lookupUri = RawContacts.getContactLookupUri(getContentResolver(),
                    ContentUris.withAppendedId(RawContacts.CONTENT_URI, rawContactId));
            Log.d(TAG, "The uri from old version: " + lookupUri);
        }
        mExtraMode = getIntent().getIntExtra(QuickContact.EXTRA_MODE, QuickContact.MODE_LARGE);
        if (isMultiWindowOnPhone()) {
            mExtraMode = QuickContact.MODE_LARGE;
        }
        mExtraPrioritizedMimeType =
                getIntent().getStringExtra(QuickContact.EXTRA_PRIORITIZED_MIMETYPE);
        final Uri oldLookupUri = mLookupUri;
        if (lookupUri == null) {
            Log.w(TAG, "[processIntent]lookupUri is null,return!");
            finish();
            return;
        }
        mLookupUri = lookupUri;
        Log.d(TAG, "[processIntent] original uri is oldLookupUri = " + oldLookupUri
                + ", new uri from intent is mLookupUri = " + mLookupUri);
        mExcludeMimes = intent.getStringArrayExtra(QuickContact.EXTRA_EXCLUDE_MIMES);
        if (oldLookupUri == null) {
            mContactLoader = (ContactLoader) getLoaderManager().initLoader(
                    LOADER_CONTACT_ID, null, mLoaderContactCallbacks);
        } else if (oldLookupUri != mLookupUri) {
            // After copying a directory contact, the contact URI changes. Therefore,
            // we need to reload the new contact.
            destroyInteractionLoaders();
            mContactLoader = (ContactLoader) (Loader<?>) getLoaderManager().getLoader(
                    LOADER_CONTACT_ID);
            mContactLoader.setLookupUri(mLookupUri);
            mCachedCp2DataCardModel = null;
        }
      
        mContactLoader.forceLoad();

        NfcHandler.register(this, mLookupUri);

        // M: Add for presence @{
        ExtensionManager.getInstance().getContactsCommonPresenceExtension().processIntent(intent);
    }

    private void destroyInteractionLoaders() {
        for (int interactionLoaderId : mRecentLoaderIds) {
            getLoaderManager().destroyLoader(interactionLoaderId);
        }
    }

    private void runEntranceAnimation() {
        if (mHasAlreadyBeenOpened) {
            return;
        }
        mHasAlreadyBeenOpened = true;
//        mScroller.scrollUpForEntranceAnimation(mExtraMode != MODE_FULLY_EXPANDED); zhangzhonghao remove animation 20160322
    }

    private boolean isMultiWindowOnPhone() {
        return MultiWindowCompat.isInMultiWindowMode(this) && PhoneCapabilityTester.isPhone(this);
    }

    /** Assign this string to the view if it is not empty. */
    private void setHeaderNameText(int resId) {
        if (mScroller != null) {
            /*mScroller.*/setTitle(getText(resId) == null ? null : getText(resId).toString()); //zhangzhonghao remove animation 20160322
        }
    }

    /** Assign this string to the view if it is not empty. */
    private void setHeaderNameText(String value, boolean isPhoneNumber) {
        if (!TextUtils.isEmpty(value)) {
            if (mScroller != null) {
                /*mScroller.*/setTitle(value); //zhangzhonghao remove animation 20160322

            }
        }
    }

    /**
     * Check if the given MIME-type appears in the list of excluded MIME-types
     * that the most-recent caller requested.
     */
    private boolean isMimeExcluded(String mimeType) {
        if (mExcludeMimes == null) return false;
        for (String excludedMime : mExcludeMimes) {
            if (TextUtils.equals(excludedMime, mimeType)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Handle the result from the ContactLoader
     */
    private void bindContactData(final Contact data) {
    	Log.d(TAG, "[bindContactData]");
        Trace.beginSection("bindContactData");
        mContactData = data;
        /*prize-add huangliemin-2016-5-26-start*/
        if(isContactEditable()) {
          prizeContactEditor.setImageDrawable(getResources().getDrawable(R.drawable.prize_editmenu));
          PrizeContactsEditorText.setText(getResources().getString(R.string.prize_contacts_editor_text_string));//prize-add-huangliemin-2016-7-18-start
          prizeContactDelete.setImageDrawable(getResources().getDrawable(R.drawable.prize_callrecords_delete));
          PrizeContactsDeleteTextView.setText(getResources().getString(R.string.prize_contacts_delete_text_string));//prize-add-huangliemin-2016-7-18
        } else {
          prizeContactEditor.setImageDrawable(getResources().getDrawable(R.drawable.prize_ic_person_add));
          PrizeContactsEditorText.setText(getResources().getString(R.string.prize_contacts_new_text_string));//prize-add-huangliemin-2016-7-18-start
          prizeContactDelete.setImageDrawable(getResources().getDrawable(R.drawable.prize_add_to_contacts_selector));//prize-add-huangliemin-2016-7-15
          PrizeContactsDeleteTextView.setText(getResources().getString(R.string.prize_contacts_add_to_contacts_string));//prize-add-huangliemin-2016-7-18
        }
        /*prize-add huangliemin-2016-5-26-end*/
        
        /* prize-add-set default music-hpf-2018-1-12-start */
        if(mContactData!= null){
            mCustomRingtone = mContactData.getCustomRingtone();
            Log.d(TAG,"[bindContactData]   mCustomRingtone = " + mCustomRingtone);
            if(mCustomRingtone != null){
            	String contactRingTone = RingtoneManager.getRingtone(this, Uri.parse(mCustomRingtone)).getTitle(this);
            	Log.d(TAG,"[bindContactData]   contactRingTone = "+contactRingTone);
            	if (!"".equals(contactRingTone)) {
 					prizeQuickDefaultMusicName.setText(contactRingTone);
 				} else {
 					String systemRingTone = Settings.System.getString(getContentResolver(),Settings.System.PRIZE_RINGTONE_NAME);
 					Log.d(TAG,"[bindContactData]   systemRingTone = "+systemRingTone);
 					if("".equals(systemRingTone)){
 						prizeQuickDefaultMusicName.setText("");
 					}else{
 						prizeQuickDefaultMusicName.setText(systemRingTone);
 					}
 				}
	        }else{
	        	String systemRingTone = Settings.System.getString(getContentResolver(),Settings.System.PRIZE_RINGTONE_NAME);
	        	Log.d(TAG,"[bindContactData]   systemRingTone = "+systemRingTone);
				if("".equals(systemRingTone)){
					prizeQuickDefaultMusicName.setText("");
				}else{
					prizeQuickDefaultMusicName.setText(systemRingTone);
				}
	        }
        }
        /* prize-add-set default music-hpf-2018-1-12-end */
        
        /*PRIZE-remove defaultmusi when is user profile -huangliemin-2016-5-27 -start*/
        if(mExtraMode !=31){
            if(data.isUserProfile()){
              prizeQuickDefaultMusiceLinearLayout.setVisibility(View.GONE);
            }else{
              prizeQuickDefaultMusiceLinearLayout.setVisibility(View.VISIBLE);
            }
        }else{
            prizeQuickDefaultMusiceLinearLayout.setVisibility(View.GONE);
        }
        /*PRIZE-remove defaultmusi when is user profile -huangliemin-2016-5-27 -end*/
       /*PRIZE-hide the default music set-yuandailin-2016-6-3-start*/   
       if (mContactData != null && mContactData.getIndicate() >= 0) {
          prizeQuickDefaultMusiceLinearLayout.setVisibility(View.GONE);
        }
 	if (DirectoryContactUtil.isDirectoryContact(mContactData) || InvisibleContactUtil
                            .isInvisibleAndAddable(mContactData, this)){
        	prizeQuickDefaultMusiceLinearLayout.setVisibility(View.GONE);
	}
	/*PRIZE-hide the default music set-yuandailin-2016-6-3-end*/
        invalidateOptionsMenu();

        Trace.endSection();
        Trace.beginSection("Set display photo & name");

        mPhotoView.setIsBusiness(mContactData.isDisplayNameFromOrganization());
        mPhotoSetter.setupContactPhoto(data, mPhotoView);
        extractAndApplyTintFromPhotoViewAsynchronously();
        
        /*prize change by bxh start*/
        displayName = ContactDisplayUtils.getDisplayName(this, data).toString();
        android.util.Log.d("contact", "displayName---"+displayName);
        /*prize change by bxh end*/
        
        setHeaderNameText(
                displayName, mContactData.getDisplayNameSource() == DisplayNameSources.PHONE);
        final String phoneticName = ContactDisplayUtils.getPhoneticName(this, data);
        if (mScroller != null) {
            // Show phonetic name only when it doesn't equal the display name.
            if (!TextUtils.isEmpty(phoneticName) && !phoneticName.equals(displayName)) {
                //mScroller.setPhoneticName(phoneticName);//prize-remove-huangpengfei-2016-11-1
            } else {
                //mScroller.setPhoneticNameGone();//prize-remove-huangpengfei-2016-11-1
            }
        }

        Trace.endSection();

        mEntriesAndActionsTask = new AsyncTask<Void, Void, Cp2DataCardModel>() {

            @Override
            protected Cp2DataCardModel doInBackground(
                    Void... params) {
                Log.d(TAG, "[Cp2DataCardModel] doInBackground");
                return generateDataModelFromContact(data);
            }

            @Override
            protected void onPostExecute(Cp2DataCardModel cardDataModel) {
                super.onPostExecute(cardDataModel);
                // Check that original AsyncTask parameters are still valid and the activity
                // is still running before binding to UI. A new intent could invalidate
                // the results, for example.
                Log.d(TAG, "[Cp2DataCardModel] onPostExecute");
                if (data == mContactData && !isCancelled()) {
                    bindDataToCards(cardDataModel);
                    showActivity();
                ///M:[Google Issue][ALPS03391875] save DataModel event activity stopped @{
                } else {
                    Log.e(TAG, "[Cp2DataCardModel] Async task cancelled !!! isCancelled():" +
                            isCancelled() + ", data:" + data + ", mContactData:" + mContactData);
                    mCachedCp2DataCardModel = cardDataModel;
                /// @}
                }
            }
        };
        mEntriesAndActionsTask.execute();
        Log.d(TAG, "[bindContactData] mEntriesAndActionsTask.execute()");
    }

    private void bindDataToCards(Cp2DataCardModel cp2DataCardModel) {
        startInteractionLoaders(cp2DataCardModel);
        populateContactAndAboutCard(cp2DataCardModel, /* shouldAddPhoneticName */ true);
        populateSuggestionCard();
    }

    private void startInteractionLoaders(Cp2DataCardModel cp2DataCardModel) {
        final Map<String, List<DataItem>> dataItemsMap = cp2DataCardModel.dataItemsMap;
        final List<DataItem> phoneDataItems = dataItemsMap.get(Phone.CONTENT_ITEM_TYPE);
        if (phoneDataItems != null) {
            /// M: Reset the value as the size may change,
            //  otherwise when size > 1, the value will always be true.
            mOnlyOnePhoneNumber = phoneDataItems.size() == 1 ? true : false;
        }
        String[] phoneNumbers = null;
        if (phoneDataItems != null) {
            phoneNumbers = new String[phoneDataItems.size()];
            for (int i = 0; i < phoneDataItems.size(); ++i) {
                phoneNumbers[i] = ((PhoneDataItem) phoneDataItems.get(i)).getNumber();
            }
        }
        final Bundle phonesExtraBundle = new Bundle();
        phonesExtraBundle.putStringArray(KEY_LOADER_EXTRA_PHONES, phoneNumbers);
        /*prize-add-huangliemin-2016-7-20-start*/
        mCallLogNumber = phoneNumbers;
        /*prize-add-huangliemin-2016-7-20-end*/

        Trace.beginSection("start sms loader");
        getLoaderManager().initLoader(
                LOADER_SMS_ID,
                phonesExtraBundle,
                mLoaderInteractionsCallbacks);
        Trace.endSection();

        Trace.beginSection("start call log loader");
        getLoaderManager().initLoader(
                LOADER_CALL_LOG_ID,
                phonesExtraBundle,
                mLoaderInteractionsCallbacks);
        Trace.endSection();


        Trace.beginSection("start calendar loader");
        final List<DataItem> emailDataItems = dataItemsMap.get(Email.CONTENT_ITEM_TYPE);
        if (emailDataItems != null) {
            /// M: Reset the value as the size may change,
            //  otherwise when size > 1, the value will always be true.
            mOnlyOneEmail = emailDataItems.size() == 1 ? true : false;
        }
        String[] emailAddresses = null;
        if (emailDataItems != null) {
            emailAddresses = new String[emailDataItems.size()];
            for (int i = 0; i < emailDataItems.size(); ++i) {
                emailAddresses[i] = ((EmailDataItem) emailDataItems.get(i)).getAddress();
            }
        }
        final Bundle emailsExtraBundle = new Bundle();
        emailsExtraBundle.putStringArray(KEY_LOADER_EXTRA_EMAILS, emailAddresses);
        getLoaderManager().initLoader(
                LOADER_CALENDAR_ID,
                emailsExtraBundle,
                mLoaderInteractionsCallbacks);
        Trace.endSection();
    }

    private void showActivity() {
        Log.d(TAG, "[showActivity]");
        if (mScroller != null) {
            mScroller.setVisibility(View.VISIBLE);
            SchedulingUtils.doOnPreDraw(mScroller, /* drawNextFrame = */ false,
                    new Runnable() {
                        @Override
                        public void run() {
                            runEntranceAnimation();
                        }
                    });
        }
    }

    private List<List<Entry>> buildAboutCardEntries(Map<String, List<DataItem>> dataItemsMap) {
        Log.d(TAG,"[buildAboutCardEntries]");
        final List<List<Entry>> aboutCardEntries = new ArrayList<>();
        for (String mimetype : SORTED_ABOUT_CARD_MIMETYPES) {
            final List<DataItem> mimeTypeItems = dataItemsMap.get(mimetype);
            if (mimeTypeItems == null) {
                continue;
            }
            // Set aboutCardTitleOut = null, since SORTED_ABOUT_CARD_MIMETYPES doesn't contain
            // the name mimetype.
            final List<Entry> aboutEntries = dataItemsToEntries(mimeTypeItems,
                    /* aboutCardTitleOut = */ null);
            if (aboutEntries.size() > 0) {
                aboutCardEntries.add(aboutEntries);
            }
        }
        return aboutCardEntries;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // If returning from a launched activity, repopulate the contact and about card
        if (mHasIntentLaunched) {
            mHasIntentLaunched = false;
            populateContactAndAboutCard(mCachedCp2DataCardModel, /* shouldAddPhoneticName */ false);
        }

        // When exiting the activity and resuming, we want to force a full reload of all the
        // interaction data in case something changed in the background. On screen rotation,
        // we don't need to do this. And, mCachedCp2DataCardModel will be null, so we won't.
        if (mCachedCp2DataCardModel != null) {
            destroyInteractionLoaders();
            startInteractionLoaders(mCachedCp2DataCardModel);
        }
        ///M:[for rcs] update Rcs contact on the top left@{
        ExtensionManager.getInstance().getRcsExtension()
                .getQuickContactRcsScroller()
                .updateRcsContact(mContactLoader.getLookupUri(), false);
        ///@}

        //prize add zhaojian for bug 52649 20180313 start
        InputMethodManager inputmgr = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        inputmgr.prizeHideSoftInput(InputMethodManager.HIDE_NOT_ALWAYS, null);
        //prize add zhaojian for bug 52649 20180313 end
    }

    private void populateSuggestionCard() {
        Log.d(TAG, "[populateSuggestionCard] mPreviousContactId = " + mPreviousContactId
                + ", mContactData.getId()" + mContactData.getId());

        // Initialize suggestion related view and data.
        if (mPreviousContactId != mContactData.getId()) {
            mCollapsedSuggestionCardView.setVisibility(View.GONE);
            mExpandSuggestionCardView.setVisibility(View.GONE);
            mIsSuggestionListCollapsed = true;
            mSuggestionsShouldAutoSelected = true;
            mSuggestionList.removeAllViews();
        }

        // Do not show the card when it's directory contact or invisible.
        if (DirectoryContactUtil.isDirectoryContact(mContactData)
                || InvisibleContactUtil.isInvisibleAndAddable(mContactData, this)) {
            return;
        }

        if (mAggregationSuggestionEngine == null) {
            mAggregationSuggestionEngine = new AggregationSuggestionEngine(this);
            mAggregationSuggestionEngine.setListener(this);
            mAggregationSuggestionEngine.setSuggestionsLimit(getResources().getInteger(
                    R.integer.quickcontact_suggestions_limit));
            mAggregationSuggestionEngine.start();
        }

        mAggregationSuggestionEngine.setContactId(mContactData.getId());
        if (mPreviousContactId != 0
                && mPreviousContactId != mContactData.getId()) {
            // Clear selected Ids when listing suggestions for new contact Id.
            mSelectedAggregationIds.clear();
        }
        mPreviousContactId = mContactData.getId();

        // Trigger suggestion engine to compute suggestions.
        if (mContactData.getId() <= 0) {
            return;
        }
        final ContentValues values = new ContentValues();
        values.put(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
                mContactData.getDisplayName());
        values.put(ContactsContract.CommonDataKinds.StructuredName.PHONETIC_FAMILY_NAME,
                mContactData.getPhoneticName());
        mAggregationSuggestionEngine.onNameChange(ValuesDelta.fromBefore(values));
    }

    private void populateContactAndAboutCard(Cp2DataCardModel cp2DataCardModel,
            boolean shouldAddPhoneticName) {
            Log.d(TAG, "[populateContactAndAboutCard]");
        mCachedCp2DataCardModel = cp2DataCardModel;
        if (mHasIntentLaunched || cp2DataCardModel == null) {
            return;
        }
        Trace.beginSection("bind contact card");

        final List<List<Entry>> contactCardEntries = cp2DataCardModel.contactCardEntries;
        final List<List<Entry>> aboutCardEntries = cp2DataCardModel.aboutCardEntries;
        final String customAboutCardName = cp2DataCardModel.customAboutCardName;


        if (contactCardEntries.size() > 0) {
            final boolean firstEntriesArePrioritizedMimeType =
                    !TextUtils.isEmpty(mExtraPrioritizedMimeType) &&
                    mCachedCp2DataCardModel.dataItemsMap.containsKey(mExtraPrioritizedMimeType) &&
                    mCachedCp2DataCardModel.dataItemsMap.get(mExtraPrioritizedMimeType).size() != 0;
            mContactCard.setIsContactCard(true);//prize-add-huangliemin-2016-7-16
            PrizeQuickContactDataManager.clearData();//prize-add-for dido os8.0-hpf-2017-8-21
            //numInitialVisibleEntries can control how many item view to show, 
            mContactCard.initialize(contactCardEntries,
                    /* numInitialVisibleEntries = */ MIN_NUM_CONTACT_ENTRIES_SHOWN,
                    /* isExpanded = */ mContactCard.isExpanded(),
                    /* isAlwaysExpanded = */ false,
                    mExpandingEntryCardViewListener,
                    mScroller,
                    firstEntriesArePrioritizedMimeType,false);
            mContactCard.setIsContactCard(false);//prize-add-huangliemin-2016-7-16
            mContactCard.setVisibility(View.VISIBLE);
            
            /*prize-add for dido os8.0-hpf-2017-8-9-start*/
            List<Entry> entry = PrizeQuickContactDataManager.getEntryList();
      		if (entry != null && entry.size() > 0) {
      			Log.d(TAG,"[populateContactAndAboutCard]  entry.size() = " + entry.size());
      			mPrizeVideoCallIcon.setImageDrawable(entry.get(0).getThirdIcon());
      			mPrizeVideoCall.setVisibility(View.VISIBLE);
      			if(entry.size() > 1){
      				mPrizeVideoCall.setOnClickListener(new OnClickListener() {
						
      					@Override
      					public void onClick(View v) {
      						if(PrizeBatteryBroadcastReciver.isBatteryLow){
      	                 		android.widget.Toast.makeText(QuickContactActivity.this, R.string.prize_video_call_attention, 
      	 	                            android.widget.Toast.LENGTH_SHORT).show();
      						}
      						showVideoCallChoiceDialog(entry);
      					}
      				});
      			}else{
      				mPrizeVideoCall.setTag(new EntryTag(entry.get(0).getId(), entry.get(0).getThirdIntent()));
      				mPrizeVideoCall.setOnClickListener(mEntryClickHandler);
      			}
      		}
            /*prize-add for dido os8.0-hpf-2017-8-9-end*/
            
        } else {
            mContactCard.setVisibility(View.GONE);
        }
        /*prize-add huangliemin-2016-5-26-start*/
        changeTDCandDM();
        /*prize-add huangliemin-2016-5-26-end*/
        Trace.endSection();
        Trace.beginSection("bind about card");

        // Phonetic name is not a data item, so the entry needs to be created separately
        // But if mCachedCp2DataCardModel is passed to this method (e.g. returning from editor
        // without saving any changes), then it should include phoneticName and the phoneticName
        // shouldn't be changed. If this is the case, we shouldn't add it again. b/27459294
        final String phoneticName = mContactData.getPhoneticName();
        if (shouldAddPhoneticName && !TextUtils.isEmpty(phoneticName)) {
            Entry phoneticEntry = new Entry(/* viewId = */ -1,
                    /* icon = */ null,
                    getResources().getString(R.string.name_phonetic),
                    phoneticName,
                    /* subHeaderIcon = */ null,
                    /* text = */ null,
                    /* textIcon = */ null,
                    /* primaryContentDescription = */ null,
                    /* intent = */ null,
                    /* alternateIcon = */ null,
                    /* alternateIntent = */ null,
                    /* alternateContentDescription = */ null,
                    /* shouldApplyColor = */ false,
                    /* isEditable = */ false,
                    /* EntryContextMenuInfo = */ new EntryContextMenuInfo(phoneticName,
                            getResources().getString(R.string.name_phonetic),
                            /* mimeType = */ null, /* id = */ -1, /* isPrimary = */ false),
                    /* thirdIcon = */ null,
                    /* thirdIntent = */ null,
                    /* thirdContentDescription = */ null,
                    /* thirdAction = */ Entry.ACTION_NONE,
                    /* thirdExtras = */ null,
                    /* iconResourceId = */  0);
            List<Entry> phoneticList = new ArrayList<>();
            phoneticList.add(phoneticEntry);
            // Phonetic name comes after nickname. Check to see if the first entry type is nickname
            if (aboutCardEntries.size() > 0 && aboutCardEntries.get(0).get(0).getHeader().equals(
                    getResources().getString(R.string.header_nickname_entry))) {
                aboutCardEntries.add(1, phoneticList);
            } else {
                aboutCardEntries.add(0, phoneticList);
            }
        }


        /// M: Bug fix ALPS01775443, after deleted name in editor, need refresh about card.
        mAboutCard.setTitle(customAboutCardName);

 /* prize add contact group view huangpengfei 2016-8-11 start */
			String groupTile = getString(R.string.contact_detail_group_list_title);
			mPrizeGroupTitle.setText(groupTile);// set title
			mPrizeGroupSubTitlesStringBuilder.replace(0, mPrizeGroupSubTitlesStringBuilder.length(), "");

			if (listContactGroup.size() > 0) {

				mPrizeGroupContent.setVisibility(View.VISIBLE);
				for (int i = 0; i < listContactGroup.size(); i++) {

					String subTitle = listContactGroup.get(i);

					LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
							ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

					Log.d("mPrizeGroupSubTitles", "header=" + groupTile + "--" + "subHeader" + subTitle);
					
					if (i + 1 < listContactGroup.size()) {
						mPrizeGroupSubTitlesStringBuilder.append(subTitle + ",");
					} else {
						mPrizeGroupSubTitlesStringBuilder.append(subTitle);
					}

					mPrizeGroupSubTitles.setText(mPrizeGroupSubTitlesStringBuilder.toString());
				}

			} else {
				
				mPrizeGroupContent.setVisibility(View.GONE);// prize set view huangpengfei 2016-8-12
			}
        
        /* prize add contact group view huangpengfei 2016-8-11 end */
        if (aboutCardEntries.size() > 0) {
			mAboutCard.initialize(aboutCardEntries,
                     /*numInitialVisibleEntries = */ 1,
                     /*isExpanded =  */true,
                     /*isAlwaysExpanded =  */true,
                    mExpandingEntryCardViewListener,
                    mScroller,false);
			
			
			
        } else {
            /// M: Bug fix ALPS01763309, after deleted all about card informations
            //  in editor, need refresh about card. @{
            mAboutCard.initialize(aboutCardEntries, 1, true, true,
                    mExpandingEntryCardViewListener, mScroller, false);
            mAboutCard.setVisibility(View.GONE);
            /// @}
           
          
        }
        if (contactCardEntries.size() == 0 && aboutCardEntries.size() == 0) {
            //initializeNoContactDetailCard();
            initializeNoContactDetailLayout();
        } else {
            //mNoContactDetailsCard.setVisibility(View.GONE);//prize-remove huangpengfei-2016-8-20
            mNoContactDetailsLayout.setVisibility(View.GONE);//prize-add huangpengfei-2016-8-20
        }

        // If the Recent card is already initialized (all recent data is loaded), show the About
        // card if it has entries. Otherwise About card visibility will be set in bindRecentData()
        if (isAllRecentDataLoaded() && aboutCardEntries.size() > 0) {
            mAboutCard.setVisibility(View.VISIBLE);
        }
        Trace.endSection();
    }
    
    
    private void initializeNoContactDetailLayout(){
    	String strAddPhoneNum = getString(R.string.quickcontact_add_phone_number);
    	String strAddEmail = getString(R.string.quickcontact_add_email);
    	mNoContactDataTextAddPhoneNum.setText(strAddPhoneNum);
    	mNoContactDataTextAddEmail.setText(strAddEmail);
    	
    }
    
  /*prize-remove huangpengfei-2016-8-20 start*/
	
    /**
     * Create a card that shows "Add email" and "Add phone number" entries in grey.
     */
  /*  private void initializeNoContactDetailCard() {
        final Drawable phoneIcon = getResources().getDrawable(
                R.drawable.ic_phone_24dp).mutate();
        final Entry phonePromptEntry = new Entry(CARD_ENTRY_ID_EDIT_CONTACT,
                phoneIcon, getString(R.string.quickcontact_add_phone_number),
                 subHeader =  null,  subHeaderIcon =  null,  text =  null,
                 textIcon =  null,  primaryContentDescription =  null,
                getEditContactIntent(),
                 alternateIcon =  null,  alternateIntent =  null,
                 alternateContentDescription =  null,  shouldApplyColor =  true,
                 isEditable =  false,  EntryContextMenuInfo =  null,
                 thirdIcon =  null,  thirdIntent =  null,
                 thirdContentDescription =  null, R.drawable.ic_phone_24dp);

        final Drawable emailIcon = getResources().getDrawable(
                R.drawable.ic_email_24dp).mutate();
        final Entry emailPromptEntry = new Entry(CARD_ENTRY_ID_EDIT_CONTACT,
                emailIcon, getString(R.string.quickcontact_add_email),  subHeader =  null,
                 subHeaderIcon =  null,
                 text =  null,  textIcon =  null,  primaryContentDescription =  null,
                getEditContactIntent(),  alternateIcon =  null,
                 alternateIntent =  null,  alternateContentDescription =  null,
                 shouldApplyColor =  true,  isEditable =  false,
                 EntryContextMenuInfo =  null,  thirdIcon =  null,
                 thirdIntent =  null,  thirdContentDescription =  null,
                R.drawable.ic_email_24dp);

        final List<List<Entry>> promptEntries = new ArrayList<>();
        promptEntries.add(new ArrayList<Entry>(1));
        promptEntries.add(new ArrayList<Entry>(1));
        promptEntries.get(0).add(phonePromptEntry);
        promptEntries.get(1).add(emailPromptEntry);

        final int subHeaderTextColor = getResources().getColor(
                R.color.quickcontact_entry_sub_header_text_color);
        final PorterDuffColorFilter greyColorFilter =
                new PorterDuffColorFilter(subHeaderTextColor, PorterDuff.Mode.SRC_ATOP);
        mNoContactDetailsCard.initialize(promptEntries, 2,  isExpanded =  true,
                 isAlwaysExpanded =  true, mExpandingEntryCardViewListener, mScroller,false);
        mNoContactDetailsCard.setVisibility(View.VISIBLE);
        mNoContactDetailsCard.setEntryHeaderColor(subHeaderTextColor);
        mNoContactDetailsCard.setColorAndFilter(subHeaderTextColor, greyColorFilter);
    }*/
    
    
  /*prize-remove huangpengfei-2016-8-20 end*/

    /**
     * Builds the {@link DataItem}s Map out of the Contact.
     * @param data The contact to build the data from.
     * @return A pair containing a list of data items sorted within mimetype and sorted
     *  amongst mimetype. The map goes from mimetype string to the sorted list of data items within
     *  mimetype
     */
    private Cp2DataCardModel generateDataModelFromContact(
            Contact data) {
        Trace.beginSection("Build data items map");
        Log.d(TAG,"[generateDataModelFromContact]");
        final Map<String, List<DataItem>> dataItemsMap = new HashMap<>();

        final ResolveCache cache = ResolveCache.getInstance(this);
        for (RawContact rawContact : data.getRawContacts()) {
            for (DataItem dataItem : rawContact.getDataItems()) {
                dataItem.setRawContactId(rawContact.getId());

                final String mimeType = dataItem.getMimeType();
                if (mimeType == null) continue;

                final AccountType accountType = rawContact.getAccountType(this);
                final DataKind dataKind = AccountTypeManager.getInstance(this)
                        .getKindOrFallback(accountType, mimeType);
                if (dataKind == null) continue;

                dataItem.setDataKind(dataKind);

                final boolean hasData = !TextUtils.isEmpty(dataItem.buildDataString(this,
                        dataKind));

                if (isMimeExcluded(mimeType) || !hasData) continue;

                List<DataItem> dataItemListByType = dataItemsMap.get(mimeType);
                if (dataItemListByType == null) {
                    dataItemListByType = new ArrayList<>();
                    dataItemsMap.put(mimeType, dataItemListByType);
                }
                dataItemListByType.add(dataItem);
            }
        }
        Trace.endSection();

        Trace.beginSection("sort within mimetypes");
        /*
         * Sorting is a multi part step. The end result is to a have a sorted list of the most
         * used data items, one per mimetype. Then, within each mimetype, the list of data items
         * for that type is also sorted, based off of {super primary, primary, times used} in that
         * order.
         */
        final List<List<DataItem>> dataItemsList = new ArrayList<>();
        for (List<DataItem> mimeTypeDataItems : dataItemsMap.values()) {
            // Remove duplicate data items
            Collapser.collapseList(mimeTypeDataItems, this);
            // Sort within mimetype
            Collections.sort(mimeTypeDataItems, mWithinMimeTypeDataItemComparator);
            // Add to the list of data item lists
            dataItemsList.add(mimeTypeDataItems);
        }
        Trace.endSection();

        Trace.beginSection("sort amongst mimetypes");
        // Sort amongst mimetypes to bubble up the top data items for the contact card
        Collections.sort(dataItemsList, mAmongstMimeTypeDataItemComparator);
        Trace.endSection();

        Trace.beginSection("cp2 data items to entries");

        final List<List<Entry>> contactCardEntries = new ArrayList<>();
        final List<List<Entry>> aboutCardEntries = buildAboutCardEntries(dataItemsMap);
        final MutableString aboutCardName = new MutableString();
        /*prize-add for dido os8.0 -hpf-2017-8-10-start*/
        numberList.clear();
        twoDeminsionCodeNumber.delete(0, twoDeminsionCodeNumber.length());
        /*prize-add for dido os8.0 -hpf-2017-8-10-end*/
        for (int i = 0; i < dataItemsList.size(); ++i) {
            final List<DataItem> dataItemsByMimeType = dataItemsList.get(i);
            final DataItem topDataItem = dataItemsByMimeType.get(0);
            if (SORTED_ABOUT_CARD_MIMETYPES.contains(topDataItem.getMimeType())) {
                // About card mimetypes are built in buildAboutCardEntries, skip here
                continue;
            } else {
                List<Entry> contactEntries = dataItemsToEntries(dataItemsList.get(i),
                        aboutCardName);
                if (contactEntries.size() > 0) {
                    contactCardEntries.add(contactEntries);
                }
            }
        }

        Trace.endSection();

        final Cp2DataCardModel dataModel = new Cp2DataCardModel();
        dataModel.customAboutCardName = aboutCardName.value;
        dataModel.aboutCardEntries = aboutCardEntries;
        dataModel.contactCardEntries = contactCardEntries;
        dataModel.dataItemsMap = dataItemsMap;
        Log.d(TAG, "[generateDataModelFromContact] end contact: " + data);
        return dataModel;
    }

    /**
     * Class used to hold the About card and Contact cards' data model that gets generated
     * on a background thread. All data is from CP2.
     */
    private static class Cp2DataCardModel {
        /**
         * A map between a mimetype string and the corresponding list of data items. The data items
         * are in sorted order using mWithinMimeTypeDataItemComparator.
         */
        public Map<String, List<DataItem>> dataItemsMap;
        public List<List<Entry>> aboutCardEntries;
        public List<List<Entry>> contactCardEntries;
        public String customAboutCardName;
    }

    private static class MutableString {
        public String value;
    }

    /**
     * Converts a {@link DataItem} into an {@link ExpandingEntryCardView.Entry} for display.
     * If the {@link ExpandingEntryCardView.Entry} has no visual elements, null is returned.
     *
     * This runs on a background thread. This is set as static to avoid accidentally adding
     * additional dependencies on unsafe things (like the Activity).
     *
     * @param dataItem The {@link DataItem} to convert.
     * @param secondDataItem A second {@link DataItem} to help build a full entry for some
     *  mimetypes
     * @return The {@link ExpandingEntryCardView.Entry}, or null if no visual elements are present.
     */
    private static Entry dataItemToEntry(DataItem dataItem, DataItem secondDataItem,
            Context context, Contact contactData,
            final MutableString aboutCardName) {
        Log.d(TAG, "[dataItemToEntry] contact:" + contactData + " dataItem:" + dataItem.getClass());
        Drawable icon = null;
        String header = null;
        String subHeader = null;
        Drawable subHeaderIcon = null;
        String text = null;
        Drawable textIcon = null;
        StringBuilder primaryContentDescription = new StringBuilder();
        Spannable phoneContentDescription = null;
        Spannable smsContentDescription = null;
        Intent intent = null;
        boolean shouldApplyColor = true;
        Drawable alternateIcon = null;
        Intent alternateIntent = null;
        StringBuilder alternateContentDescription = new StringBuilder();
        final boolean isEditable = false;
        EntryContextMenuInfo entryContextMenuInfo = null;
        Drawable thirdIcon = null;
        Intent thirdIntent = null;
        int thirdAction = Entry.ACTION_NONE;
        String thirdContentDescription = null;
        Bundle thirdExtras = null;
        int iconResourceId = 0;

        context = context.getApplicationContext();
        final Resources res = context.getResources();
        DataKind kind = dataItem.getDataKind();

        QuickContactUtils.resetSipAddress();
        /// M: Fix ALPS01995031
        if (contactData == null) {
            Log.w(TAG, "[dataItemToEntry] contact data is null.");
            return null;
        }
        if (dataItem instanceof ImDataItem) {
            final ImDataItem im = (ImDataItem) dataItem;
            intent = ContactsUtils.buildImIntent(context, im).first;
            final boolean isEmail = im.isCreatedFromEmail();
            final int protocol;
            if (!im.isProtocolValid()) {
                protocol = Im.PROTOCOL_CUSTOM;
            } else {
                protocol = isEmail ? Im.PROTOCOL_GOOGLE_TALK : im.getProtocol();
            }
            if (protocol == Im.PROTOCOL_CUSTOM) {
                // If the protocol is custom, display the "IM" entry header as well to distinguish
                // this entry from other ones
                header = res.getString(R.string.header_im_entry);
                subHeader = Im.getProtocolLabel(res, protocol,
                        im.getCustomProtocol()).toString();
                text = im.getData();
            } else {
                header = Im.getProtocolLabel(res, protocol,
                        im.getCustomProtocol()).toString();
                subHeader = im.getData();
            }
            entryContextMenuInfo = new EntryContextMenuInfo(im.getData(), header,
                    dataItem.getMimeType(), dataItem.getId(), dataItem.isSuperPrimary());
        } else if (dataItem instanceof OrganizationDataItem) {         
            final OrganizationDataItem organization = (OrganizationDataItem) dataItem;
            header = res.getString(R.string.header_organization_entry);
            subHeader = organization.getCompany();
            entryContextMenuInfo = new EntryContextMenuInfo(subHeader, header,
                    dataItem.getMimeType(), dataItem.getId(), dataItem.isSuperPrimary());
            text = organization.getTitle();
        } else if (dataItem instanceof NicknameDataItem) {
            final NicknameDataItem nickname = (NicknameDataItem) dataItem;
            // Build nickname entries
            final boolean isNameRawContact =
                (contactData.getNameRawContactId() == dataItem.getRawContactId());

            final boolean duplicatesTitle =
                isNameRawContact
                && contactData.getDisplayNameSource() == DisplayNameSources.NICKNAME;

            if (!duplicatesTitle) {
                header = res.getString(R.string.header_nickname_entry);
                subHeader = nickname.getName();
                entryContextMenuInfo = new EntryContextMenuInfo(subHeader, header,
                        dataItem.getMimeType(), dataItem.getId(), dataItem.isSuperPrimary());
            }
        } else if (dataItem instanceof NoteDataItem) {
            final NoteDataItem note = (NoteDataItem) dataItem;
            header = res.getString(R.string.header_note_entry);
            subHeader = note.getNote();
            entryContextMenuInfo = new EntryContextMenuInfo(subHeader, header,
                    dataItem.getMimeType(), dataItem.getId(), dataItem.isSuperPrimary());
        } else if (dataItem instanceof WebsiteDataItem) {
            final WebsiteDataItem website = (WebsiteDataItem) dataItem;
            header = res.getString(R.string.header_website_entry);
            subHeader = website.getUrl();
            entryContextMenuInfo = new EntryContextMenuInfo(subHeader, header,
                    dataItem.getMimeType(), dataItem.getId(), dataItem.isSuperPrimary());
            try {
                final WebAddress webAddress = new WebAddress(website.buildDataStringForDisplay
                        (context, kind));
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse(webAddress.toString()));
            } catch (final ParseException e) {
                Log.e(TAG, "Couldn't parse website: " + website.buildDataStringForDisplay(
                        context, kind));
            }
        } else if (dataItem instanceof EventDataItem) {
            final EventDataItem event = (EventDataItem) dataItem;
            final String dataString = event.buildDataStringForDisplay(context, kind);
            final Calendar cal = DateUtils.parseDate(dataString, false);
            if (cal != null) {
                final Date nextAnniversary =
                        DateUtils.getNextAnnualDate(cal);
                final Uri.Builder builder = CalendarContract.CONTENT_URI.buildUpon();
                builder.appendPath("time");
                ContentUris.appendId(builder, nextAnniversary.getTime());
                intent = new Intent(Intent.ACTION_VIEW).setData(builder.build());
            }
            header = res.getString(R.string.header_event_entry);
            if (event.hasKindTypeColumn(kind)) {
                subHeader = EventCompat.getTypeLabel(res, event.getKindTypeColumn(kind),
                        event.getLabel()).toString();
            }
            text = DateUtils.formatDate(context, dataString);
            entryContextMenuInfo = new EntryContextMenuInfo(text, header,
                    dataItem.getMimeType(), dataItem.getId(), dataItem.isSuperPrimary());
        } else if (dataItem instanceof RelationDataItem) {
            final RelationDataItem relation = (RelationDataItem) dataItem;
            final String dataString = relation.buildDataStringForDisplay(context, kind);
            if (!TextUtils.isEmpty(dataString)) {
                intent = new Intent(Intent.ACTION_SEARCH);
                intent.putExtra(SearchManager.QUERY, dataString);
                intent.setType(Contacts.CONTENT_TYPE);
            }
            header = res.getString(R.string.header_relation_entry);
            subHeader = relation.getName();
            entryContextMenuInfo = new EntryContextMenuInfo(subHeader, header,
                    dataItem.getMimeType(), dataItem.getId(), dataItem.isSuperPrimary());
            if (relation.hasKindTypeColumn(kind)) {
                text = Relation.getTypeLabel(res,
                        relation.getKindTypeColumn(kind),
                        relation.getLabel()).toString();
            }
        } else if (dataItem instanceof PhoneDataItem) {
            final PhoneDataItem phone = (PhoneDataItem) dataItem;
            String phoneLabel = null;
            if (!TextUtils.isEmpty(phone.getNumber())) {
                primaryContentDescription.append(res.getString(R.string.call_other)).append(" ");
                header = sBidiFormatter.unicodeWrap(phone.buildDataStringForDisplay(context, kind),
                        TextDirectionHeuristics.LTR);
                Log.d(TAG,"[dataItemToEntry] header = "+header);
                
                /* prize add for two deminssions hpf 2017-8-10 start */
                if(header != null){
                	twoDeminsionCodeNumber.append(header.replaceAll(" ",""));
                	twoDeminsionCodeNumber.append("\n");
                	/*PRIZE-black number list-yuandailin-2016-1-29-start*/
                	prizeTwoDeminsionCodeNumber = header.replaceAll(" ","");
                	numberList.add(prizeTwoDeminsionCodeNumber);
                	/*PRIZE-black number list-yuandailin-2016-1-29-end*/
                }
                /* prize add for two deminssions hpf 2017-8-10 end */
                
                entryContextMenuInfo = new EntryContextMenuInfo(header,
                        res.getString(R.string.phoneLabelsGroup), dataItem.getMimeType(),
                        dataItem.getId(), dataItem.isSuperPrimary());
                if (phone.hasKindTypeColumn(kind)) {
                    final int kindTypeColumn = phone.getKindTypeColumn(kind);
                    final String label = phone.getLabel();
                    phoneLabel = label;
                    if (kindTypeColumn == Phone.TYPE_CUSTOM && TextUtils.isEmpty(label)) {
                        text = "";
                    } else {
                        text = Phone.getTypeLabel(res, kindTypeColumn, label).toString();
                        ///M:[for AAS]show Primary Number/Additional Number@{
                        int subId = contactData.getIndicate();
                        subHeader = GlobalEnv.getAasExtension().getSubheaderString(
                                subId, dataItem.getContentValues().getAsInteger(Data.DATA2));
                        text = (String) GlobalEnv.getAasExtension().getTypeLabel(
                                dataItem.getContentValues().getAsInteger(Data.DATA2),
                                (CharSequence) dataItem.getContentValues().getAsString(Data.DATA3),
                                (String) text, subId);
                        ///@}
                        phoneLabel= text;
                        primaryContentDescription.append(text).append(" ");
                    }

                }
                primaryContentDescription.append(header);
                phoneContentDescription = com.android.contacts.common.util.ContactDisplayUtils
                        .getTelephoneTtsSpannable(primaryContentDescription.toString(), header);
                icon = res.getDrawable(R.drawable.ic_phone_24dp);
                iconResourceId = R.drawable.ic_phone_24dp;
                if (PhoneCapabilityTester.isPhone(context)) {
                    intent = CallUtil.getCallIntent(phone.getNumber());
                }
                /// M: mtk add isSupportSms() method to enable Sms dynamically.@{
                if (PhoneCapabilityTester.isSupportSms(context)) {
                    alternateIntent = new Intent(Intent.ACTION_SENDTO,
                            Uri.fromParts(ContactsUtils.SCHEME_SMSTO, phone.getNumber(), null));

                    alternateIcon = res.getDrawable(R.drawable.prize_selector_ic_message_btn);
                    alternateContentDescription.append(res.getString(R.string.sms_custom, header));
                }
                /// @}
                smsContentDescription = com.android.contacts.common.util.ContactDisplayUtils
                        .getTelephoneTtsSpannable(alternateContentDescription.toString(), header);
                int videoCapability = CallUtil.getVideoCallingAvailability(context);
                boolean isPresenceEnabled =
                        (videoCapability & CallUtil.VIDEO_CALLING_PRESENCE) != 0;
                boolean isVideoEnabled = (videoCapability & CallUtil.VIDEO_CALLING_ENABLED) != 0;
                ///M: Video Entry @{
                isVideoEnabled = ExtensionManager.getInstance().getOp01Extension()
                        .isVideoButtonEnabled(isVideoEnabled, contactData.getLookupUri(), context);
                ///@}
                /* prize add for dido os8.0 hpf 2017-8-10-start */
                if(isVideoEnabled){
                	mPrizeIsVideoEnable = true;
                }else{
                	mPrizeIsVideoEnable = false;
                }
                /* prize add for dido os8.0 hpf 2017-8-10-end */

                if (CallUtil.isCallWithSubjectSupported(context)) {
                    thirdIcon = res.getDrawable(R.drawable.ic_call_note_white_24dp);
                    thirdAction = Entry.ACTION_CALL_WITH_SUBJECT;
                    thirdContentDescription =
                            res.getString(R.string.call_with_a_note);

                    // Create a bundle containing the data the call subject dialog requires.
                    thirdExtras = new Bundle();
                    thirdExtras.putLong(CallSubjectDialog.ARG_PHOTO_ID,
                            contactData.getPhotoId());
                    thirdExtras.putParcelable(CallSubjectDialog.ARG_PHOTO_URI,
                            UriUtils.parseUriOrNull(contactData.getPhotoUri()));
                    thirdExtras.putParcelable(CallSubjectDialog.ARG_CONTACT_URI,
                            contactData.getLookupUri());
                    thirdExtras.putString(CallSubjectDialog.ARG_NAME_OR_NUMBER,
                            contactData.getDisplayName());
                    thirdExtras.putBoolean(CallSubjectDialog.ARG_IS_BUSINESS, false);
                    thirdExtras.putString(CallSubjectDialog.ARG_NUMBER,
                            phone.getNumber());
                    thirdExtras.putString(CallSubjectDialog.ARG_DISPLAY_NUMBER,
                            phone.getFormattedPhoneNumber());
                    thirdExtras.putString(CallSubjectDialog.ARG_NUMBER_LABEL,
                            phoneLabel);
                } else if (isVideoEnabled|| ExtensionManager.getInstance()
                        .getContactsCommonPresenceExtension().isShowVideoIcon()) {
                    // Check to ensure carrier presence indicates the number supports video calling.
                    int carrierPresence = dataItem.getCarrierPresence();
                    boolean isPresent = (carrierPresence & Phone.CARRIER_PRESENCE_VT_CAPABLE) != 0;
                    
                    if ((isPresenceEnabled && isPresent) || !isPresenceEnabled) {
                    	/*prize-change for dido os8.0-hpf-2017-8-9-start*/
                        thirdIcon = res.getDrawable(R.drawable./*ic_videocam*/prize_selector_ic_video_call_btn);
                        /*prize-change for dido os8.0-hpf-2017-8-9-end*/
                        thirdAction = Entry.ACTION_INTENT;
                        thirdIntent = CallUtil.getVideoCallIntent(phone.getNumber(),
                                CALL_ORIGIN_QUICK_CONTACTS_ACTIVITY);
                        thirdContentDescription =
                                res.getString(R.string.description_video_call);
                    }
                }
                ExtensionManager.getInstance().getContactsCommonPresenceExtension().
                    setVideoIconAlpha(phone.getNumber(), thirdIcon);
            }
        } else if (dataItem instanceof EmailDataItem) {
            final EmailDataItem email = (EmailDataItem) dataItem;
            final String address = email.getData();
            if (!TextUtils.isEmpty(address)) {
                primaryContentDescription.append(res.getString(R.string.email_other)).append(" ");
                final Uri mailUri = Uri.fromParts(ContactsUtils.SCHEME_MAILTO, address, null);
                intent = new Intent(Intent.ACTION_SENDTO, mailUri);
                header = email.getAddress();
                entryContextMenuInfo = new EntryContextMenuInfo(header,
                        res.getString(R.string.emailLabelsGroup), dataItem.getMimeType(),
                        dataItem.getId(), dataItem.isSuperPrimary());
                if (email.hasKindTypeColumn(kind)) {
                    text = Email.getTypeLabel(res, email.getKindTypeColumn(kind),
                            email.getLabel()).toString();
                    primaryContentDescription.append(text).append(" ");
                }
                primaryContentDescription.append(header);
                text = res.getString(R.string.prize_contacts_email);
                icon = res.getDrawable(R.drawable.ic_email_24dp);
                iconResourceId = R.drawable.ic_email_24dp;
            }
        } else if (dataItem instanceof StructuredPostalDataItem) {
            StructuredPostalDataItem postal = (StructuredPostalDataItem) dataItem;
            final String postalAddress = postal.getFormattedAddress();
            if (!TextUtils.isEmpty(postalAddress)) {
                primaryContentDescription.append(res.getString(R.string.map_other)).append(" ");
                intent = StructuredPostalUtils.getViewPostalAddressIntent(postalAddress);
                header = postal.getFormattedAddress();
                entryContextMenuInfo = new EntryContextMenuInfo(header,
                        res.getString(R.string.postalLabelsGroup), dataItem.getMimeType(),
                        dataItem.getId(), dataItem.isSuperPrimary());
                if (postal.hasKindTypeColumn(kind)) {
                    text = StructuredPostal.getTypeLabel(res,
                            postal.getKindTypeColumn(kind), postal.getLabel()).toString();
                    primaryContentDescription.append(text).append(" ");
                }
                primaryContentDescription.append(header);
                alternateIntent =
                        StructuredPostalUtils.getViewPostalAddressDirectionsIntent(postalAddress);
//                alternateIcon = res.getDrawable(R.drawable.ic_directions_24dp); zhangzhonghao remove map menu 20160411
                alternateContentDescription.append(res.getString(
                        R.string.content_description_directions)).append(" ").append(header);
                icon = res.getDrawable(R.drawable.ic_place_24dp);
                iconResourceId = R.drawable.ic_place_24dp;
            }
        } else if (dataItem instanceof SipAddressDataItem) {
            final SipAddressDataItem sip = (SipAddressDataItem) dataItem;
            final String address = sip.getSipAddress();
            if (!TextUtils.isEmpty(address)) {
                QuickContactUtils.setSipAddress(address);
                primaryContentDescription.append(res.getString(R.string.call_other)).append(
                        " ");
                if (PhoneCapabilityTester.isSipPhone(context)) {
                    final Uri callUri = Uri.fromParts(PhoneAccount.SCHEME_SIP, address, null);
                    intent = CallUtil.getCallIntent(callUri);
                }
                header = address;
                entryContextMenuInfo = new EntryContextMenuInfo(header,
                        res.getString(R.string.phoneLabelsGroup), dataItem.getMimeType(),
                        dataItem.getId(), dataItem.isSuperPrimary());
                if (sip.hasKindTypeColumn(kind)) {
                    text = SipAddress.getTypeLabel(res,
                            sip.getKindTypeColumn(kind), sip.getLabel()).toString();
                    primaryContentDescription.append(text).append(" ");
                }
                primaryContentDescription.append(header);
                icon = res.getDrawable(R.drawable.ic_dialer_sip_black_24dp);
                iconResourceId = R.drawable.ic_dialer_sip_black_24dp;
            }
        } else if (dataItem instanceof StructuredNameDataItem) {
            // If the name is already set and this is not the super primary value then leave the
            // current value. This way we show the super primary value when we are able to.
            if (dataItem.isSuperPrimary() || aboutCardName.value == null
                    || aboutCardName.value.isEmpty()) {
                final String givenName = ((StructuredNameDataItem) dataItem).getGivenName();
                if (!TextUtils.isEmpty(givenName)) {
                    aboutCardName.value = res.getString(R.string.about_card_title) +
                            " " + givenName;
                } else {
                    aboutCardName.value = res.getString(R.string.about_card_title);
                }
            }
        } else if (dataItem instanceof ImsCallDataItem) { // M: add IMS Call
            if (ContactsSystemProperties.MTK_VOLTE_SUPPORT &&
                    ContactsSystemProperties.MTK_IMS_SUPPORT) {
                final ImsCallDataItem ims = (ImsCallDataItem) dataItem;
                String imsUri = ims.getUrl();
                if (!TextUtils.isEmpty(imsUri)) {
                    String imsLabel = ims.getLabel();
                    Log.d(TAG, "imsUri: " + imsUri + ", imsLabel: " + imsLabel);
                    intent = CallUtil.getCallIntent(Uri.fromParts(PhoneAccount.SCHEME_TEL,
                            imsUri, null), null, Constants.DIAL_NUMBER_INTENT_IMS);
                    icon = res.getDrawable(R.drawable.ic_dialer_ims_black);
                    text = res.getString(R.string.imsCallLabelsGroup);
                    header = imsUri;
                }
            }
        /// M: Group member ship.
        } else if (dataItem instanceof GroupMembershipDataItem) {
            final GroupMembershipDataItem groupDataItem = (GroupMembershipDataItem) dataItem;
            String groupTitle = QuickContactUtils.getGroupTitle(contactData.getGroupMetaData(),
                    groupDataItem.getGroupRowId());
            if (!TextUtils.isEmpty(groupTitle)) {
                header = res.getString(R.string.contact_detail_group_list_title);
                subHeader = groupTitle;
            }
        } else {
            // Custom DataItem
            header = dataItem.buildDataStringForDisplay(context, kind);
            text = kind.typeColumn;
            intent = new Intent(Intent.ACTION_VIEW);
            final Uri uri = ContentUris.withAppendedId(Data.CONTENT_URI, dataItem.getId());
            intent.setDataAndType(uri, dataItem.getMimeType());

            if (intent != null) {
                final String mimetype = intent.getType();

                // Build advanced entry for known 3p types. Otherwise default to ResolveCache icon.
                switch (mimetype) {
                    case MIMETYPE_GPLUS_PROFILE:
                        // If a secondDataItem is available, use it to build an entry with
                        // alternate actions
                        if (secondDataItem != null) {
                            icon = res.getDrawable(R.drawable.ic_google_plus_24dp);
                            alternateIcon = res.getDrawable(R.drawable.ic_add_to_circles_black_24);
                            final GPlusOrHangoutsDataItemModel itemModel =
                                    new GPlusOrHangoutsDataItemModel(intent, alternateIntent,
                                            dataItem, secondDataItem, alternateContentDescription,
                                            header, text, context);

                            populateGPlusOrHangoutsDataItemModel(itemModel);
                            intent = itemModel.intent;
                            alternateIntent = itemModel.alternateIntent;
                            alternateContentDescription = itemModel.alternateContentDescription;
                            header = itemModel.header;
                            text = itemModel.text;
                        } else {
                            if (GPLUS_PROFILE_DATA_5_ADD_TO_CIRCLE.equals(
                                    intent.getDataString())) {
                                icon = res.getDrawable(R.drawable.ic_add_to_circles_black_24);
                            } else {
                                icon = res.getDrawable(R.drawable.ic_google_plus_24dp);
                            }
                        }
                        break;
                    case MIMETYPE_HANGOUTS:
                        // If a secondDataItem is available, use it to build an entry with
                        // alternate actions
                        if (secondDataItem != null) {
                            icon = res.getDrawable(R.drawable.ic_hangout_24dp);
                            alternateIcon = res.getDrawable(R.drawable.ic_hangout_video_24dp);
                            final GPlusOrHangoutsDataItemModel itemModel =
                                    new GPlusOrHangoutsDataItemModel(intent, alternateIntent,
                                            dataItem, secondDataItem, alternateContentDescription,
                                            header, text, context);

                            populateGPlusOrHangoutsDataItemModel(itemModel);
                            intent = itemModel.intent;
                            alternateIntent = itemModel.alternateIntent;
                            alternateContentDescription = itemModel.alternateContentDescription;
                            header = itemModel.header;
                            text = itemModel.text;
                        } else {
                            if (HANGOUTS_DATA_5_VIDEO.equals(intent.getDataString())) {
                                icon = res.getDrawable(R.drawable.ic_hangout_video_24dp);
                            } else {
                                icon = res.getDrawable(R.drawable.ic_hangout_24dp);
                            }
                        }
                        break;
                    default:
                        entryContextMenuInfo = new EntryContextMenuInfo(header, mimetype,
                                dataItem.getMimeType(), dataItem.getId(),
                                dataItem.isSuperPrimary());
                        icon = ResolveCache.getInstance(context).getIcon(
                                dataItem.getMimeType(), intent);
                        // Call mutate to create a new Drawable.ConstantState for color filtering
                        if (icon != null) {
                            icon.mutate();
                        }
                        shouldApplyColor = false;
                }
            }
        }

        if (intent != null) {
            // Do not set the intent is there are no resolves
            if (!PhoneCapabilityTester.isIntentRegistered(context, intent)) {
                intent = null;
            }
        }

        if (alternateIntent != null) {
            // Do not set the alternate intent is there are no resolves
            if (!PhoneCapabilityTester.isIntentRegistered(context, alternateIntent)) {
                alternateIntent = null;
            } else if (TextUtils.isEmpty(alternateContentDescription)) {
                // Attempt to use package manager to find a suitable content description if needed
                alternateContentDescription.append(getIntentResolveLabel(alternateIntent, context));
            }
        }

        // If the Entry has no visual elements, return null
        if (icon == null && TextUtils.isEmpty(header) && TextUtils.isEmpty(subHeader) &&
                subHeaderIcon == null && TextUtils.isEmpty(text) && textIcon == null) {
            Log.d(TAG, "[dataItemToEntry] has no visual elements");
            return null;
        }

        // Ignore dataIds from the Me profile.
        final int dataId = dataItem.getId() > Integer.MAX_VALUE ?
                -1 : (int) dataItem.getId(); 
        Log.d(TAG, "[dataItemToEntry] end ");
        /*prize add hunangpengfei 2016-8-11 start */
        
        if(res.getString(R.string.contact_detail_group_list_title).equals(header)){
        	if(listContactGroup != null){
        		if(!isContain(listContactGroup,subHeader)){
        			listContactGroup.add(subHeader);
        			Log.d(TAG,"listContactGroup = "+listContactGroup.toString());
        		}
        	}
        	return null;
        }
        /*prize add hunangpengfei 2016-8-11 end */
        
        
        /* M: add sim icon & sim name */
        return new Entry(dataId, icon, header, subHeader, subHeaderIcon, text, textIcon,
                phoneContentDescription == null
                        ? new SpannableString(primaryContentDescription.toString())
                        : phoneContentDescription,
                intent, alternateIcon, alternateIntent,
                smsContentDescription == null
                        ? new SpannableString(alternateContentDescription.toString())
                        : smsContentDescription,
                shouldApplyColor, isEditable,
                entryContextMenuInfo, thirdIcon, thirdIntent, thirdContentDescription, thirdAction,
                thirdExtras, iconResourceId);

        /* M: add sim icon & sim name @{
        return new Entry(dataId, icon, header, subHeader, subHeaderIcon, text, textIcon, null, null,
                new SpannableString(primaryContentDescription.toString()),
                intent, alternateIcon, alternateIntent,
                smsContentDescription == null
                        ? new SpannableString(alternateContentDescription.toString())
                        : smsContentDescription,
                shouldApplyColor, isEditable,
                entryContextMenuInfo, thirdIcon, thirdIntent, thirdContentDescription, thirdAction,
                thirdExtras, iconResourceId);
         @} */
    }
    
    /*prize-add-huangpengfei-2016-11-14*/
    private static boolean isContain(List<String> list,String name){
    	if(list == null){
    		return false;
    	}
    	
    	int size = list.size();
    	for (int i = 0; i < size; i++){
    		String groupName = list.get(i);
    		Log.d(TAG,"[isContain]   groupName = "+groupName+"   name = "+name);
    		if(groupName.equals(name)){
    			return true;
    		}
    	}
    	return false;
    }
    /*prize-add-huangpengfei-2016-11-14*/
    
    private List<Entry> dataItemsToEntries(List<DataItem> dataItems,
            MutableString aboutCardTitleOut) {
        Log.d(TAG, "[dataItemsToEntries]");
        // Hangouts and G+ use two data items to create one entry.
        if (dataItems.get(0).getMimeType().equals(MIMETYPE_GPLUS_PROFILE) ||
                dataItems.get(0).getMimeType().equals(MIMETYPE_HANGOUTS)) {
            return gPlusOrHangoutsDataItemsToEntries(dataItems);
        } else {
            final List<Entry> entries = new ArrayList<>();
            for (DataItem dataItem : dataItems) {
                final Entry entry = dataItemToEntry(dataItem, /* secondDataItem = */ null,
                        this, mContactData, aboutCardTitleOut);
                if (entry != null) {
                    entries.add(entry);
                }
            }
            return entries;
        }
    }

    /**
     * G+ and Hangout entries are unique in that a single ExpandingEntryCardView.Entry consists
     * of two data items. This method attempts to build each entry using the two data items if
     * they are available. If there are more or less than two data items, a fall back is used
     * and each data item gets its own entry.
     */
    private List<Entry> gPlusOrHangoutsDataItemsToEntries(List<DataItem> dataItems) {
        Log.d(TAG, "[gPlusOrHangoutsDataItemsToEntries] start");
        final List<Entry> entries = new ArrayList<>();
        final Map<Long, List<DataItem>> buckets = new HashMap<>();
        // Put the data items into buckets based on the raw contact id
        for (DataItem dataItem : dataItems) {
            List<DataItem> bucket = buckets.get(dataItem.getRawContactId());
            if (bucket == null) {
                bucket = new ArrayList<>();
                buckets.put(dataItem.getRawContactId(), bucket);
            }
            bucket.add(dataItem);
        }

        // Use the buckets to build entries. If a bucket contains two data items, build the special
        // entry, otherwise fall back to the normal entry.
        for (List<DataItem> bucket : buckets.values()) {
            if (bucket.size() == 2) {
                // Use the pair to build an entry
                final Entry entry = dataItemToEntry(bucket.get(0),
                        /* secondDataItem = */ bucket.get(1), this, mContactData,
                        /* aboutCardName = */ null);
                if (entry != null) {
                    entries.add(entry);
                }
            } else {
                for (DataItem dataItem : bucket) {
                    final Entry entry = dataItemToEntry(dataItem, /* secondDataItem = */ null,
                            this, mContactData, /* aboutCardName = */ null);
                    if (entry != null) {
                        entries.add(entry);
                    }
                }
            }
        }
        Log.d(TAG, "[gPlusOrHangoutsDataItemsToEntries] end");
        return entries;
    }

    /**
     * Used for statically passing around G+ or Hangouts data items and entry fields to
     * populateGPlusOrHangoutsDataItemModel.
     */
    private static final class GPlusOrHangoutsDataItemModel {
        public Intent intent;
        public Intent alternateIntent;
        public DataItem dataItem;
        public DataItem secondDataItem;
        public StringBuilder alternateContentDescription;
        public String header;
        public String text;
        public Context context;

        public GPlusOrHangoutsDataItemModel(Intent intent, Intent alternateIntent,
                DataItem dataItem,
                DataItem secondDataItem, StringBuilder alternateContentDescription, String header,
                String text, Context context) {
            this.intent = intent;
            this.alternateIntent = alternateIntent;
            this.dataItem = dataItem;
            this.secondDataItem = secondDataItem;
            this.alternateContentDescription = alternateContentDescription;
            this.header = header;
            this.text = text;
            this.context = context;
        }
    }

    private static void populateGPlusOrHangoutsDataItemModel(
            GPlusOrHangoutsDataItemModel dataModel) {
        final Intent secondIntent = new Intent(Intent.ACTION_VIEW);
        secondIntent.setDataAndType(ContentUris.withAppendedId(Data.CONTENT_URI,
                dataModel.secondDataItem.getId()), dataModel.secondDataItem.getMimeType());
        // There is no guarantee the order the data items come in. Second
        // data item does not necessarily mean it's the alternate.
        // Hangouts video and Add to circles should be alternate. Swap if needed
        if (HANGOUTS_DATA_5_VIDEO.equals(
                dataModel.dataItem.getContentValues().getAsString(Data.DATA5)) ||
                GPLUS_PROFILE_DATA_5_ADD_TO_CIRCLE.equals(
                        dataModel.dataItem.getContentValues().getAsString(Data.DATA5))) {
            dataModel.alternateIntent = dataModel.intent;
            dataModel.alternateContentDescription = new StringBuilder(dataModel.header);

            dataModel.intent = secondIntent;
            dataModel.header = dataModel.secondDataItem.buildDataStringForDisplay(dataModel.context,
                    dataModel.secondDataItem.getDataKind());
            dataModel.text = dataModel.secondDataItem.getDataKind().typeColumn;
        } else if (HANGOUTS_DATA_5_MESSAGE.equals(
                dataModel.dataItem.getContentValues().getAsString(Data.DATA5)) ||
                GPLUS_PROFILE_DATA_5_VIEW_PROFILE.equals(
                        dataModel.dataItem.getContentValues().getAsString(Data.DATA5))) {
            dataModel.alternateIntent = secondIntent;
            dataModel.alternateContentDescription = new StringBuilder(
                    dataModel.secondDataItem.buildDataStringForDisplay(dataModel.context,
                            dataModel.secondDataItem.getDataKind()));
        }
    }

    private static String getIntentResolveLabel(Intent intent, Context context) {
        final List<ResolveInfo> matches = context.getPackageManager().queryIntentActivities(intent,
                PackageManager.MATCH_DEFAULT_ONLY);

        // Pick first match, otherwise best found
        ResolveInfo bestResolve = null;
        final int size = matches.size();
        if (size == 1) {
            bestResolve = matches.get(0);
        } else if (size > 1) {
            bestResolve = ResolveCache.getInstance(context).getBestResolve(intent, matches);
        }

        if (bestResolve == null) {
            return null;
        }

        return String.valueOf(bestResolve.loadLabel(context.getPackageManager()));
    }

    /**
     * Asynchronously extract the most vibrant color from the PhotoView. Once extracted,
     * apply this tint to {@link MultiShrinkScroller}. This operation takes about 20-30ms
     * on a Nexus 5.
     */
    private void extractAndApplyTintFromPhotoViewAsynchronously() {
        if (mScroller == null) {
            Log.d(TAG, "[extractAndApplyTintFromPhotoViewAsynchronously] mScroller=null");
            return;
        }
        final Drawable imageViewDrawable = mPhotoView.getDrawable();
        new AsyncTask<Void, Void, MaterialPalette>() {
            @Override
            protected MaterialPalette doInBackground(Void... params) {
                Log.d(TAG, "[extractAndApplyTintFromPhotoViewAsynchronously] doInBackground start");
                if (mContactData == null) {
                    Log.w(TAG, "[doInBackground] mContactData is null...");
                }

                if (imageViewDrawable instanceof BitmapDrawable && mContactData != null
                        && mContactData.getThumbnailPhotoBinaryData() != null
                        && mContactData.getThumbnailPhotoBinaryData().length > 0) {
                    // Perform the color analysis on the thumbnail instead of the full sized
                    // image, so that our results will be as similar as possible to the Bugle
                    // app.
                    final Bitmap bitmap = BitmapFactory.decodeByteArray(
                            mContactData.getThumbnailPhotoBinaryData(), 0,
                            mContactData.getThumbnailPhotoBinaryData().length);
                    try {
                        final int primaryColor = colorFromBitmap(bitmap);
                        if (primaryColor != 0) {
                            return mMaterialColorMapUtils.calculatePrimaryAndSecondaryColor(
                                    primaryColor);
                        }
                    } finally {
                        bitmap.recycle();
                    }
                }
                if (imageViewDrawable instanceof LetterTileDrawable) {
                    final int primaryColor = ((LetterTileDrawable) imageViewDrawable).getColor();
                    return mMaterialColorMapUtils.calculatePrimaryAndSecondaryColor(primaryColor);
                }
                Log.d(TAG, "[extractAndApplyTintFromPhotoViewAsynchronously] doInBackground end");
                return MaterialColorMapUtils.getDefaultPrimaryAndSecondaryColors(getResources());
            }

            @Override
            protected void onPostExecute(MaterialPalette palette) {
                super.onPostExecute(palette);
                Log.d(TAG, "extractAndApplyTintFromPhotoViewAsynchronously [onPostExecute]");
                //M:OP01 RCS will get photo from rcs server, and refresh thumbnail Photo.@{
                mHasComputedThemeColor = ExtensionManager.getInstance().getRcsExtension().
                        needUpdateContactPhoto(imageViewDrawable instanceof LetterTileDrawable,
                        mHasComputedThemeColor);
                /** @} */
                if (mHasComputedThemeColor) {
                    // If we had previously computed a theme color from the contact photo,
                    // then do not update the theme color. Changing the theme color several
                    // seconds after QC has started, as a result of an updated/upgraded photo,
                    // is a jarring experience. On the other hand, changing the theme color after
                    // a rotation or onNewIntent() is perfectly fine.
                    return;
                }
                // Check that the Photo has not changed. If it has changed, the new tint
                // color needs to be extracted
                if (imageViewDrawable == mPhotoView.getDrawable()) {
                    Log.d(TAG, "[extractAndApplyTintFromPhotoViewAsynchronously] onPostExecute"
                            + "to update color and photo in suggestion card");
                    mHasComputedThemeColor = true;
                    setThemeColor(palette);
                    // update color and photo in suggestion card
                    onAggregationSuggestionChange();
                }
            }
        }.execute();
        Log.d(TAG, "[extractAndApplyTintFromPhotoViewAsynchronously] execute()");
    }

    private void setThemeColor(MaterialPalette palette) {
        // If the color is invalid, use the predefined default
        mColorFilterColor = palette.mPrimaryColor;
//        mScroller.setHeaderTintColor(mColorFilterColor); zhangzhonghao remove animation 20160322
        mStatusBarColor = palette.mSecondaryColor;
//        updateStatusBarColor(); zhangzhonghao remove animation 20160322

        mColorFilter =
                new PorterDuffColorFilter(mColorFilterColor, PorterDuff.Mode.SRC_ATOP);
        mContactCard.setColorAndFilter(mColorFilterColor, mColorFilter);
        mRecentCard.setColorAndFilter(mColorFilterColor, mColorFilter);
        mAboutCard.setColorAndFilter(mColorFilterColor, mColorFilter);
        //mSuggestionsCancelButton.setTextColor(mColorFilterColor);//prize-remove-hpf-2017-12-12
        //prize-delete-huangliemin-2016-7-18
        //prizeContactPhotoRelativeLayout.setBackgroundColor(mColorFilter.getColor()); //zhangzhonghao set photo backgroundcolor
        /// M: [for RCS-e]
        if (mJoynCard != null) {
            mJoynCard.setColorAndFilter(mColorFilterColor, mColorFilter);
        }
    }

    /* prize zhangzhonghao remove animation 20160322 start */
//    private void updateStatusBarColor() {
//        if (mScroller == null) {
//            return;
//        }
//        final int desiredStatusBarColor;
//        // Only use a custom status bar color if QuickContacts touches the top of the viewport.
//        if (mScroller.getScrollNeededToBeFullScreen() <= 0) {
//            desiredStatusBarColor = mStatusBarColor;
//        } else {
//            desiredStatusBarColor = Color.TRANSPARENT;
//        }
//        // Animate to the new color.
//        final ObjectAnimator animation = ObjectAnimator.ofInt(getWindow(), "statusBarColor",
//                getWindow().getStatusBarColor(), desiredStatusBarColor);
//        animation.setDuration(ANIMATION_STATUS_BAR_COLOR_CHANGE_DURATION);
//        animation.setEvaluator(new ArgbEvaluator());
//        animation.start();
//    }
    /* prize zhangzhonghao remove animation 20160322 end */
    
    private int colorFromBitmap(Bitmap bitmap) {
        // Author of Palette recommends using 24 colors when analyzing profile photos.
        final int NUMBER_OF_PALETTE_COLORS = 24;
        final Palette palette = Palette.generate(bitmap, NUMBER_OF_PALETTE_COLORS);
        if (palette != null && palette.getVibrantSwatch() != null) {
            return palette.getVibrantSwatch().getRgb();
        }
        return 0;
    }

    private List<Entry> contactInteractionsToEntries(List<ContactInteraction> interactions) {
    	//prize-add-huangliemin-2016-7-16-start
    	boolean isFirtst;
    	isFirtst = false;
    	//prize-add-huangliemin-2016-7-16-end
        final List<Entry> entries = new ArrayList<>();
        for (ContactInteraction interaction : interactions) {
            if (interaction == null) {
                continue;
            }

      /*PRIZE-add call duration -huangliemin-2016-5-27 -start*/			
			if(interaction instanceof CallLogInteraction){
				CallLogInteraction pinteraction = (CallLogInteraction)interaction;
				/*prize-add-huangliemin-2016-7-16-start*/
				if(!isFirtst) {
					FirstNumber = pinteraction.getNumber();
					isFirtst = true;
				}
				/*prize-add-huangliemin-2016-7-16-end*/
				StringBuffer s = new StringBuffer();
				if(pinteraction.getType() == 1){
				    s.append(getResources().getString(R.string.prize_incall));
				}else if(pinteraction.getType() == 2){
				    s.append(getResources().getString(R.string.prize_outcall));
				}else{
				    s.append(getResources().getString(R.string.prize_callnotice));
				}
				int i = Integer.valueOf(pinteraction.getDuration().toString());
                int i1 = i / 3600;
                int i2 = (i % 3600) / 60;
                int i3 = i % 60;
                if (i1 != 0) {
                    s.append(i1 + getResources().getString(R.string.prize_hours));
                }
                if (i2 != 0) {
                    s.append(i2 + getResources().getString(R.string.prize_minutes));
                }
                s.append(i3 + getResources().getString(R.string.prize_seconds));
               
                entries.add(new Entry(/* id = */ -1,
                	/*mainIcon = */interaction.getIcon(this),
                	/*header = */interaction.getViewHeader(this),
                	/*subHeader = */interaction.getViewBody(this),
                	/*subHeaderIcon = */interaction.getBodyIcon(this),
                	/*text = */interaction.getViewFooter(this),
                	/*textIcon = */interaction.getFooterIcon(this),
                    /* M: add sim icon @ { */
                	/*simIcon = */interaction.getSimIcon(this),
                	/*simName = */interaction.getSimName(this),
                    /* @ } */
                	/*primaryContentDescription = */interaction.getContentDescription(this),
                	/* intent = */interaction.getIntent(),
                    /* alternateIcon = */ null,
                    /* alternateIntent = */ null,
                    /* alternateContentDescription = */ null,
                    /* shouldApplyColor = */ true,
                    /* isEditable = */ false,
                    /* EntryContextMenuInfo = */ null,
                    /* thirdIcon = */ null,
                    /* thirdIntent = */ null,
                    /* thirdContentDescription = */ null,
                    /* thirdAction = */0,
                    /* thirdExtras*/null,
                    interaction.getIconResourceId(),
					s.toString()));
				}else{
					entries.add(new Entry(/* id = */ -1,
                    interaction.getIcon(this),
                    interaction.getViewHeader(this),
                    interaction.getViewBody(this),
                    interaction.getBodyIcon(this),
                    interaction.getViewFooter(this),
                    interaction.getFooterIcon(this),
                    /* M: add sim icon @ { */
                    interaction.getSimIcon(this),
                    interaction.getSimName(this),
                    /* @ } */
                    interaction.getContentDescription(this),
                    interaction.getIntent(),
                    /* alternateIcon = */ null,
                    /* alternateIntent = */ null,
                    /* alternateContentDescription = */ null,
                    /* shouldApplyColor = */ true,
                    /* isEditable = */ false,
                    /* EntryContextMenuInfo = */ null,
                    /* thirdIcon = */ null,
                    /* thirdIntent = */ null,
                    /* thirdContentDescription = */ null,
                    /* thirdAction = */ Entry.ACTION_NONE,
                    /* thirdActionExtras = */ null,
                    interaction.getIconResourceId()));
      /*PRIZE-add call duration -huangliemin-2016-5-27 -end*/

				}
			
        }
			return entries;
        
    }

    private final LoaderCallbacks<Contact> mLoaderContactCallbacks =
            new LoaderCallbacks<Contact>() {
        @Override
        public void onLoaderReset(Loader<Contact> loader) {
            Log.d(TAG, "[onLoaderReset], mContactData been set null");
            mContactData = null;
        }

        @Override
        public void onLoadFinished(Loader<Contact> loader, Contact data) {
        	Log.d(TAG, "[onLoadFinished]");
            Trace.beginSection("onLoadFinished()");
            try {

                if (isFinishing()) {
                    return;
                }
                if (data.isError()) {
                    // This means either the contact is invalid or we had an
                    // internal error such as an acore crash.
                    Log.i(TAG, "Failed to load contact: " + ((ContactLoader)loader).getLookupUri());
                    Toast.makeText(QuickContactActivity.this, R.string.invalidContactMessage,
                            Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }
                if (data.isNotFound()) {
                    Log.i(TAG, "No contact found: " + ((ContactLoader)loader).getLookupUri());
                            //prize delete for bug 38022 zhaojian 20171016 start
//                    Toast.makeText(QuickContactActivity.this, R.string.invalidContactMessage,
//                            Toast.LENGTH_LONG).show();
                            //prize delete zhaojian 20171016 end
                            finish();
                            return;
                        }
                        Log.d(TAG, "onLoadFinished " + " | data.getContactId() : "
                                + data.getContactId() + " | data.getUri() : " + data.getUri());
                /*PRIZE-add aidl for get location by service -qiaohu-2018-6-11 -start*/
                mIsContact = data.getContactId() > 0;
                /*PRIZE-add aidl for get location by service -qiaohu-2018-6-11 -end*/
                bindContactData(data);

                ///M:[for rcs] update Rcs contact on the top left@{
                ExtensionManager.getInstance().getRcsExtension()
                        .getQuickContactRcsScroller()
                        .updateRcsContact(mContactLoader.getLookupUri(), true);
                Log.d(TAG, "onLoadFinished end");
                ///@}
            } finally {
                Trace.endSection();
            }
        }

        @Override
        public Loader<Contact> onCreateLoader(int id, Bundle args) {
            if (mLookupUri == null) {
                Log.wtf(TAG, "Lookup uri wasn't initialized. Loader was started too early");
            }
            // Load all contact data. We need loadGroupMetaData=true to determine whether the
            // contact is invisible. If it is, we need to display an "Add to Contacts" MenuItem.
            return new ContactLoader(getApplicationContext(), mLookupUri,
                    true /*loadGroupMetaData*/, false /*loadInvitableAccountTypes*/,
                    true /*postViewNotification*/, true /*computeFormattedPhoneNumber*/);
        }
    };

    /* prize zhangzhonghao remove animation 20160322 start */
    @Override
    public void onBackPressed() {
//        if (mScroller != null) {
//            if (!mIsExitAnimationInProgress) {
//                mScroller.scrollOffBottom();
//            }
//        } else {
            super.onBackPressed();
//        }
    }
    /* prize zhangzhonghao remove animation 20160322 end */

    /* prize-remove for dido os8.0-hpf-2017-9-5-start*/
    /*@Override
    public void finish() {
        super.finish();

        // override transitions to skip the standard window animations
        overridePendingTransition(0, 0);
    }*/
    /* prize-remove for dido os8.0-hpf-2017-9-5-end */

    private final LoaderCallbacks<List<ContactInteraction>> mLoaderInteractionsCallbacks =
            new LoaderCallbacks<List<ContactInteraction>>() {

        @Override
        public Loader<List<ContactInteraction>> onCreateLoader(int id, Bundle args) {
            Loader<List<ContactInteraction>> loader = null;
            switch (id) {
                case LOADER_SMS_ID:
                	/*prize-remove sms record -huangpengfei-2016-11-28-start*/
                   /* loader = new SmsInteractionsLoader(
                            QuickContactActivity.this,
                            args.getStringArray(KEY_LOADER_EXTRA_PHONES),
                            MAX_SMS_RETRIEVE);*/
                	loader = new SmsInteractionsLoader(
                            QuickContactActivity.this,
                            null,
                            0);
                	/*prize-remove sms record-huangpengfei-2016-11-28-end*/
                    break;
                case LOADER_CALENDAR_ID:
                    final String[] emailsArray = args.getStringArray(KEY_LOADER_EXTRA_EMAILS);
                    List<String> emailsList = null;
                    if (emailsArray != null) {
                        emailsList = Arrays.asList(args.getStringArray(KEY_LOADER_EXTRA_EMAILS));
                    }
                    loader = new CalendarInteractionsLoader(
                            QuickContactActivity.this,
                            emailsList,
                            MAX_FUTURE_CALENDAR_RETRIEVE,
                            MAX_PAST_CALENDAR_RETRIEVE,
                            FUTURE_MILLISECOND_TO_SEARCH_LOCAL_CALENDAR,
                            PAST_MILLISECOND_TO_SEARCH_LOCAL_CALENDAR);
                    break;
                case LOADER_CALL_LOG_ID:
                    loader = new CallLogInteractionsLoader(
                            QuickContactActivity.this,
                            args.getStringArray(KEY_LOADER_EXTRA_PHONES),
                            MAX_CALL_LOG_RETRIEVE);
            }
            return loader;
        }

        @Override
        public void onLoadFinished(Loader<List<ContactInteraction>> loader,
                List<ContactInteraction> data) {
        	/*prize-add-huangliemin-2016-7-22-start*/
        	if(loader.getId()==LOADER_CALL_LOG_ID && mExtraMode == 31) {
        		if(data!=null && data.size()>0 && mRecentCard.getVisibility() == View.GONE) {
        				mRecentCard.setVisibility(View.VISIBLE);
        		}
        		if(data!=null && data.size() < MAX_CALL_LOG_RETRIEVE) {
        			prizeQuickNoRecentsNotice.setVisibility(View.GONE);
        		} else {
        			prizeQuickNoRecentsNotice.setVisibility(View.VISIBLE);
        		}
        	}
        	/*prize-add-huangliemin-2016-7-22-end*/
            mRecentLoaderResults.put(loader.getId(), data);

            if (isAllRecentDataLoaded()) {
                bindRecentData();
            }
        }

        @Override
        public void onLoaderReset(Loader<List<ContactInteraction>> loader) {
            mRecentLoaderResults.remove(loader.getId());
        }
    };

    private boolean isAllRecentDataLoaded() {
        return mRecentLoaderResults.size() == mRecentLoaderIds.length;
    }
    
    /*prize-add-huangliemin-2016-7-16-start*/
    public Handler mHandler = new Handler() {

		/* (non-Javadoc)
		 * @see android.os.Handler#handleMessage(android.os.Message)
		 */
		@Override
		public void handleMessage(Message msg) {
			/*prize-add-huangliemin-2016-7-16-start*/
            if(mContactCard!=null) {
	            mContactCard.setFirstNumber(FirstNumber,isContactEditable()&&(mExtraMode==31), (mExtraMode==31));
			}
            /*prize-add-huangliemin-2016-7-16-end*/
		}
    	
    };
    /*prize-add-huangliemin-2016-7-16-end*/
    
    /*prize add by bxh  start*/
    private static String displayName;
	public static String getDisplayName() {
		return displayName;
	}
	/*prize add by bxh  end*/

    private void bindRecentData() {
    	Log.d(TAG, "[bindRecentData]");
        final List<ContactInteraction> allInteractions = new ArrayList<>();
        final List<List<Entry>> interactionsWrapper = new ArrayList<>();

        // Serialize mRecentLoaderResults into a single list. This should be done on the main
        // thread to avoid races against mRecentLoaderResults edits.
        for (List<ContactInteraction> loaderInteractions : mRecentLoaderResults.values()) {
            allInteractions.addAll(loaderInteractions);
        }

        mRecentDataTask = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                Trace.beginSection("sort recent loader results");

                // Sort the interactions by most recent
                Collections.sort(allInteractions, new Comparator<ContactInteraction>() {
                    @Override
                    public int compare(ContactInteraction a, ContactInteraction b) {
                        if (a == null && b == null) {
                            return 0;
                        }
                        if (a == null) {
                            return 1;
                        }
                        if (b == null) {
                            return -1;
                        }
                        if (a.getInteractionDate() > b.getInteractionDate()) {
                            return -1;
                        }
                        if (a.getInteractionDate() == b.getInteractionDate()) {
                            return 0;
                        }
                        return 1;
                    }
                });

                Trace.endSection();
                Trace.beginSection("contactInteractionsToEntries");

                // Wrap each interaction in its own list so that an icon is displayed for each entry
                for (Entry contactInteraction : contactInteractionsToEntries(allInteractions)) {
                    List<Entry> entryListWrapper = new ArrayList<>(1);
                    entryListWrapper.add(contactInteraction);
                    interactionsWrapper.add(entryListWrapper);
                }

                Trace.endSection();
                Log.d(TAG, "[bindRecentData] doInBackground()");
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                Trace.beginSection("initialize recents card");

                if (allInteractions.size() > 0) {
                    mRecentCard.initialize(interactionsWrapper,
                    /* numInitialVisibleEntries = */ MIN_NUM_COLLAPSED_RECENT_ENTRIES_SHOWN,
                    /* isExpanded = */ mRecentCard.isExpanded(), /* isAlwaysExpanded = */ false,
//                            mExpandingEntryCardViewListener, mScroller);
                            mExpandingEntryCardViewListener, mScroller,true);
                    //mRecentCard.setVisibility(View.VISIBLE);
        /*PRIZE-delete records-huangliemin-2016-5-27 -start*/	
					mDeleteList = new ArrayList<String>();
					for(int i=0;i<interactionsWrapper.size();i++){
						mDeleteList.add(interactionsWrapper.get(i).get(0).getHeader());
					}
        /*PRIZE-delete records-huangliemin-2016-5-27 -end*/
                } else {
                    /// M: Fix ALPS01763309
                    mRecentCard.setVisibility(View.GONE);
                }
                
                mHandler.postDelayed(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						Log.d(TAG, "[sendEmptyMessage]");
						mHandler.sendEmptyMessage(0);
					}
				}, 100);//prize-add-huangliemin-2016-7-16

                Trace.endSection();

                // About card is initialized along with the contact card, but since it appears after
                // the recent card in the UI, we hold off until making it visible until the recent
                // card is also ready to avoid stuttering.
                if (mAboutCard.shouldShow()) {
                    mAboutCard.setVisibility(View.VISIBLE);
                } else {
                    mAboutCard.setVisibility(View.GONE);
                }
                mRecentDataTask = null;
                Log.d(TAG, "[bindRecentData] onPostExecute(). size()=" + allInteractions.size());
            }
        };
        mRecentDataTask.execute();
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (mEntriesAndActionsTask != null) {
            // Once the activity is stopped, we will no longer want to bind mEntriesAndActionsTask's
            // results on the UI thread. In some circumstances Activities are killed without
            // onStop() being called. This is not a problem, because in these circumstances
            // the entire process will be killed.
            mEntriesAndActionsTask.cancel(/* mayInterruptIfRunning = */ false);
        }
        if (mRecentDataTask != null) {
            mRecentDataTask.cancel(/* mayInterruptIfRunning = */ false);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mAggregationSuggestionEngine != null) {
            mAggregationSuggestionEngine.quit();
        }
        
		///M: Video Entry @{
        ExtensionManager.getInstance().getOp01Extension().resetVideoState();
        ///@}
		
        /*prize-add for BatteryBroadcastReciver huangpengfei-2017-6-26-start*/
        if(mPrizeBatteryBroadcastReciver != null){
        	this.unregisterReceiver(mPrizeBatteryBroadcastReciver);
        }
        /*prize-add for BatteryBroadcastReciver huangpengfei-2017-6-26-end*/
        
    	/*prize-add-huangpengfei-2016-9-26-start*/
        Log.d(TAG, "[onDestory]");
    	 if (mEntriesAndActionsTask != null) {
             // Once the activity is stopped, we will no longer want to bind mEntriesAndActionsTask's
             // results on the UI thread. In some circumstances Activities are killed without
             // onStop() being called. This is not a problem, because in these circumstances
             // the entire process will be killed.
             mEntriesAndActionsTask.cancel(/* mayInterruptIfRunning = */ true);
             mEntriesAndActionsTask = null;
         }
         if (mRecentDataTask != null) { 	 
             mRecentDataTask.cancel(/* mayInterruptIfRunning = */ true);
             mRecentDataTask = null;
         }
       
         if(mContactCard != null ){
        	 mContactCard.removeAllView();
        	 mContactCard.removeAllListener();
         }
         if(mJoynCard != null ){
        	 mJoynCard.removeAllView();
        	 mJoynCard.removeAllListener();
         }
         if(mRecentCard != null ){
        	 mRecentCard.removeAllView();
        	 mRecentCard.removeAllListener();
         }
         if(mAboutCard != null ){
        	 mAboutCard.removeAllView();
        	 mAboutCard.removeAllListener();
         }
         mHandler.removeCallbacksAndMessages(null);
         PrizeQuickContactDataManager.clearData();//prize-add-for dido os8.0-hpf-2017-8-21
         /*prize-add-huangpengfei-2016-9-26-end*/
         
         /*PRIZE-add aidl for get location by service -qiaohu-2018-6-11 -start*/
         try {
        	 unbindService(serConn);
		 } catch (Exception e) {
			 Log.d(TAG, "unbindService");
		 }
         /*PRIZE-add aidl for get location by service -qiaohu-2018-6-11 -end*/
        
    }

    /**
     * M: sdn contact isn't possible to edit.
     * Returns true if it is possible to edit the current contact.
     */
    private boolean isContactEditable() {
        return mContactData != null && !mContactData.isDirectoryEntry() &&
            !mContactData.isSdnContacts();
    }

    /**
     * Returns true if it is possible to share the current contact.
     */
    private boolean isContactShareable() {
        return mContactData != null && !mContactData.isDirectoryEntry();
    }

    /// M: add isEditingUserProfile flag for user profile feature
    private Intent getEditContactIntent() {
        return EditorIntents.createCompactEditContactIntent(
                mContactData.getLookupUri(),
                mHasComputedThemeColor
                        ? new MaterialPalette(mColorFilterColor, mStatusBarColor) : null,
                mContactData.getPhotoId());
    }

    private void editContact() {
        mHasIntentLaunched = true;
        mContactLoader.cacheResult();
        startActivityForResult(getEditContactIntent(), REQUEST_CODE_CONTACT_EDITOR_ACTIVITY);
    }

    private void deleteContact() {
        final Uri contactUri = mContactData.getLookupUri();
        ContactDeletionInteraction.start(this, contactUri, /* finishActivityWhenDone =*/ true);
    }

    private void toggleStar(MenuItem starredMenuItem) {
        // Make sure there is a contact
        if (mContactData != null) {
            // Read the current starred value from the UI instead of using the last
            // loaded state. This allows rapid tapping without writing the same
            // value several times
            final boolean isStarred = starredMenuItem.isChecked();

            // To improve responsiveness, swap out the picture (and tag) in the UI already
            ContactDisplayUtils.configureStarredMenuItem(starredMenuItem,
                    mContactData.isDirectoryEntry(), mContactData.isUserProfile(),
                    !isStarred);

            // Now perform the real save
            final Intent intent = ContactSaveService.createSetStarredIntent(
                    QuickContactActivity.this, mContactData.getLookupUri(), !isStarred);
            startService(intent);

            final CharSequence accessibilityText = !isStarred
                    ? getResources().getText(R.string.description_action_menu_add_star)
                    : getResources().getText(R.string.description_action_menu_remove_star);
            // Accessibility actions need to have an associated view. We can't access the MenuItem's
            // underlying view, so put this accessibility action on the root view.
//            mScroller.announceForAccessibility(accessibilityText); zhangzhonghao remove animation 20160322
        }
    }
    ///M:
     /**
     * Calls into the contacts provider to get a pre-authorized version of the given URI.
     */
    private Uri getPreAuthorizedUri(Uri uri) {
        final Bundle uriBundle = new Bundle();
        uriBundle.putParcelable(ContactsContract.Authorization.KEY_URI_TO_AUTHORIZE, uri);
        final Bundle authResponse = getContentResolver().call(
                ContactsContract.AUTHORITY_URI,
                ContactsContract.Authorization.AUTHORIZATION_METHOD,
                null,
                uriBundle);
        if (authResponse != null) {
            return (Uri) authResponse.getParcelable(
                    ContactsContract.Authorization.KEY_AUTHORIZED_URI);
        } else {
            return uri;
        }
    }

    private void shareContact() {
        Log.d(TAG, "[shareContact]");
        final String lookupKey = mContactData.getLookupKey();
        Uri shareUri = Uri.withAppendedPath(Contacts.CONTENT_VCARD_URI, lookupKey);
        final Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType(Contacts.CONTENT_VCARD_TYPE);
        if (mContactData.isUserProfile()) {
            // User is sharing the profile.  We don't want to force the receiver to have
            // the highly-privileged READ_PROFILE permission, so we need to request a
            // pre-authorized URI from the provider.
            shareUri = getPreAuthorizedUri(shareUri);
            /** M for ALPS01752410 @{*/
            intent.putExtra("userProfile", "true");
        } else {
            intent.putExtra("contactId", String.valueOf(mContactData.getContactId()));
            /** @} */
        }
        intent.putExtra(Intent.EXTRA_STREAM, shareUri);
        /// M: Bug fix ALPS01749969, google default bug, need add the extra ARG_CALLING_ACTIVITY.
        intent.putExtra(VCardCommonArguments.ARG_CALLING_ACTIVITY,
                PeopleActivity.class.getName());

        // Launch chooser to share contact via
        final CharSequence chooseTitle = getText(R.string.share_via);
        final Intent chooseIntent = Intent.createChooser(intent, chooseTitle);

        try {
            mHasIntentLaunched = true;
            ImplicitIntentsUtil.startActivityOutsideApp(this, chooseIntent);
        } catch (final ActivityNotFoundException ex) {
            Toast.makeText(this, R.string.share_error, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Creates a launcher shortcut with the current contact.
     */
    private void createLauncherShortcutWithContact() {
        final ShortcutIntentBuilder builder = new ShortcutIntentBuilder(this,
                new OnShortcutIntentCreatedListener() {

                    @Override
                    public void onShortcutIntentCreated(Uri uri, Intent shortcutIntent) {
                        // Broadcast the shortcutIntent to the launcher to create a
                        // shortcut to this contact
                        shortcutIntent.setAction(ACTION_INSTALL_SHORTCUT);
                        QuickContactActivity.this.sendBroadcast(shortcutIntent);

                        // Send a toast to give feedback to the user that a shortcut to this
                        // contact was added to the launcher.
                        final String displayName = shortcutIntent
                                .getStringExtra(Intent.EXTRA_SHORTCUT_NAME);
                        final String toastMessage = TextUtils.isEmpty(displayName)
                                ? getString(R.string.createContactShortcutSuccessful_NoName)
                                : getString(R.string.createContactShortcutSuccessful, displayName);
                        Toast.makeText(QuickContactActivity.this, toastMessage,
                                Toast.LENGTH_SHORT).show();
                    }

                });
        builder.createContactShortcutIntent(mContactData.getLookupUri());
    }

    private boolean isShortcutCreatable() {
        if (mContactData == null || mContactData.isUserProfile() ||
                mContactData.isDirectoryEntry()) {
            return false;
        }
        final Intent createShortcutIntent = new Intent();
        createShortcutIntent.setAction(ACTION_INSTALL_SHORTCUT);
        final List<ResolveInfo> receivers = getPackageManager()
                .queryBroadcastReceivers(createShortcutIntent, 0);
        return receivers != null && receivers.size() > 0;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        final MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.quickcontact, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (mContactData != null) {
            final MenuItem starredMenuItem = menu.findItem(R.id.menu_star);
            ContactDisplayUtils.configureStarredMenuItem(starredMenuItem,
                    mContactData.isDirectoryEntry(), mContactData.isUserProfile(),
                    mContactData.getStarred());

            /// M: Disable sim contact star menu.
            if (mContactData.getIndicate() > 0) {
                starredMenuItem.setVisible(false);
            }

            final MenuItem editMenuItem = menu.findItem(R.id.menu_edit);

            // Configure edit MenuItem
            /// M: hide edit nenu if it is a sdn contact.
            Log.d(TAG, "[onPrepareOptionsMenu] is sdn contact: " + mContactData.isSdnContacts());
            if (mContactData.isSdnContacts()) {
                editMenuItem.setVisible(false);
            } else {
                editMenuItem.setVisible(true);
                editMenuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
                if (DirectoryContactUtil.isDirectoryContact(mContactData) ||
                        InvisibleContactUtil.isInvisibleAndAddable(mContactData, this)) {
                    editMenuItem.setIcon(R.drawable.ic_person_add_tinted_24dp);
                    editMenuItem.setTitle(R.string.menu_add_contact);
                } else if (isContactEditable()) {
                    editMenuItem.setIcon(R.drawable.ic_create_24dp);
                    editMenuItem.setTitle(R.string.menu_editContact);
                } else {
                    editMenuItem.setVisible(false);
                }
            }

            final MenuItem deleteMenuItem = menu.findItem(R.id.menu_delete);
            deleteMenuItem.setVisible(isContactEditable() && !mContactData.isUserProfile());

            final MenuItem shareMenuItem = menu.findItem(R.id.menu_share);
            shareMenuItem.setVisible(isContactShareable());

            final MenuItem shortcutMenuItem = menu.findItem(R.id.menu_create_contact_shortcut);
            /// M: hide the shortcut menu when it is sim contact.
            if (mContactData != null && mContactData.getIndicate() >= 0) {
                shortcutMenuItem.setVisible(false);
                Log.d(TAG, "[[onPrepareOptionsMenu]] contact indicator: " +
                        mContactData.getIndicate());
            } else {
                shortcutMenuItem.setVisible(/*isShortcutCreatable()*/false);//prize-change-huangliemin-2016-7-16
            }
            final MenuItem helpMenu = menu.findItem(R.id.menu_help);
            helpMenu.setVisible(HelpUtils.isHelpAndFeedbackAvailable());
            //M:OP01 RCS will add quick contact menu item @{
            ExtensionManager.getInstance().getRcsExtension().
                    addQuickContactMenuOptions(menu, mLookupUri, this);
            /** @} */
            Log.d(TAG, "[onPrepareOptionsMenu] return true");
            /* prize hide some menu zhangzhonghao 20160314 start */
            editMenuItem.setVisible(false);
            deleteMenuItem.setVisible(false);
            shareMenuItem.setVisible(false);
            starredMenuItem.setVisible(false);
            /* prize hide some menu zhangzhonghao 20160314 end */
			/*PRIZE-black number list-yuandailin-2016-1-29-start*/
			final MenuItem addBlackNumberMenuItem = menu.findItem(R.id.menu_contact_add_to_blacklist);
			final MenuItem deleteBlackNumberMenuItem = menu.findItem(R.id.menu_contact_delete_from_blacklist);
			boolean isNumberInBlackList = PrizeBlockNumberHelper.getInstance(getApplicationContext()).isNumberInBlacklist(prizeTwoDeminsionCodeNumber);//PRIZE-change-yuandailin-2016-8-15
			if(!TextUtils.isEmpty(prizeTwoDeminsionCodeNumber)){
			   addBlackNumberMenuItem.setVisible(!isNumberInBlackList);
			   deleteBlackNumberMenuItem.setVisible(isNumberInBlackList);
			}else{
			   addBlackNumberMenuItem.setVisible(false);
			   deleteBlackNumberMenuItem.setVisible(false);
			}
			/*prize-add-huangliemin-2016-7-16-start*/
			addBlackNumberMenuItem.setVisible(false);
			deleteBlackNumberMenuItem.setVisible(false);
			/*prize-add-huangliemin-2016-7-16-end*/
			/*PRIZE-black number list-yuandailin-2016-1-29-end*/
            
            return true;
        }
        Log.d(TAG, "[onPrepareOptionsMenu] return false");
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "[onOptionsItemSelected] item = " + item.getTitle());
        switch (item.getItemId()) {
            /* prize add home click listener zhangzhonghao 20160315 start */
            case android.R.id.home:
                this.finish();
                return true;
            /* prize add home click listener zhangzhonghao 20160315 start */
                
            case R.id.menu_star:
                toggleStar(item);
                return true;
            case R.id.menu_edit:
                if (DirectoryContactUtil.isDirectoryContact(mContactData)) {
                    // This action is used to launch the contact selector, with the option of
                    // creating a new contact. Creating a new contact is an INSERT, while selecting
                    // an exisiting one is an edit. The fields in the edit screen will be
                    // prepopulated with data.

                    final Intent intent = new Intent(Intent.ACTION_INSERT_OR_EDIT);
                    intent.setType(Contacts.CONTENT_ITEM_TYPE);

                    ArrayList<ContentValues> values = mContactData.getContentValues();

                    // Only pre-fill the name field if the provided display name is an nickname
                    // or better (e.g. structured name, nickname)
                    if (mContactData.getDisplayNameSource() >= DisplayNameSources.NICKNAME) {
                        intent.putExtra(Intents.Insert.NAME, mContactData.getDisplayName());
                    } else if (mContactData.getDisplayNameSource()
                            == DisplayNameSources.ORGANIZATION) {
                        // This is probably an organization. Instead of copying the organization
                        // name into a name entry, copy it into the organization entry. This
                        // way we will still consider the contact an organization.
                        final ContentValues organization = new ContentValues();
                        organization.put(Organization.COMPANY, mContactData.getDisplayName());
                        organization.put(Data.MIMETYPE, Organization.CONTENT_ITEM_TYPE);
                        values.add(organization);
                    }

                    // Last time used and times used are aggregated values from the usage stat
                    // table. They need to be removed from data values so the SQL table can insert
                    // properly
                    for (ContentValues value : values) {
                        value.remove(Data.LAST_TIME_USED);
                        value.remove(Data.TIMES_USED);
                    }
                    intent.putExtra(Intents.Insert.DATA, values);

                    // If the contact can only export to the same account, add it to the intent.
                    // Otherwise the ContactEditorFragment will show a dialog for selecting an
                    // account.
                    if (mContactData.getDirectoryExportSupport() ==
                            Directory.EXPORT_SUPPORT_SAME_ACCOUNT_ONLY) {
                        intent.putExtra(Intents.Insert.EXTRA_ACCOUNT,
                                new Account(mContactData.getDirectoryAccountName(),
                                        mContactData.getDirectoryAccountType()));
                        intent.putExtra(Intents.Insert.EXTRA_DATA_SET,
                                mContactData.getRawContacts().get(0).getDataSet());
                    }

                    // Add this flag to disable the delete menu option on directory contact joins
                    // with local contacts. The delete option is ambiguous when joining contacts.
                    intent.putExtra(ContactEditorFragment.INTENT_EXTRA_DISABLE_DELETE_MENU_OPTION,
                            true);

                    QuickContactUtils.addSipExtra(intent);
                    startActivityForResult(intent, REQUEST_CODE_CONTACT_SELECTION_ACTIVITY);
                } else if (InvisibleContactUtil.isInvisibleAndAddable(mContactData, this)) {
                    InvisibleContactUtil.addToDefaultGroup(mContactData, this);
                } else if (isContactEditable()) {
                    editContact();
                }
                return true;
            case R.id.menu_delete:
                if (isContactEditable()) {
                deleteContact();
                }
                return true;
            case R.id.menu_share:		
                if (isContactShareable()) {
                    shareContact();
                }
                return true;
            case R.id.menu_create_contact_shortcut:				
                if (isShortcutCreatable()) {
                createLauncherShortcutWithContact();
                }
                return true;
            case R.id.menu_help:
                HelpUtils.launchHelpAndFeedbackForContactScreen(this);
                return true;
			/*PRIZE-black number list-yuandailin-2016-1-29-start*/	
			case R.id.menu_contact_add_to_blacklist:
				PrizeBlockNumberHelper.getInstance(getApplicationContext()).insertNumberList(numberList);//PRIZE-change-yuandailin-2016-8-15
				return true;
			case R.id.menu_contact_delete_from_blacklist:
				PrizeBlockNumberHelper.getInstance(getApplicationContext()).deleteNumberList(numberList);   //PRIZE-change-yuandailin-2016-8-15         
				return true;
			/*PRIZE-black number list-yuandailin-2016-1-29-end*/	
			/*prize-add-huangliemin-2016-7-16-start*/
			case R.id.menu_twodeminsioncode:
				getTwoDeminsion();
				return true;
			/*prize-add-huangliemin-2016-7-16-end*/
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /// M: ALPS02782438.not support to show email for sim and Ruim type.@{
    private boolean isSupportShowEmailData(Contact contactsData) {
        Log.d(TAG, "[isSupportShowEmailData] data : " + contactsData);
        if (contactsData == null) {
            return false;
        }
        String accoutType = contactsData.getRawContacts().get(0).getAccountTypeString();
        Log.d(TAG, "[isSupportShowEmailData] accoutType : " + accoutType);
        if (AccountTypeUtils.ACCOUNT_TYPE_SIM.equals(accoutType) ||
                AccountTypeUtils.ACCOUNT_TYPE_RUIM.equals(accoutType)) {
            Log.i(TAG, "[isSupportShowEmailData] Ruim or sim not support email! ");
            return false;
        }
		if (AccountTypeUtils.ACCOUNT_TYPE_USIM.equals(accoutType)) {
            String accountName = contactsData.getRawContacts().get(0).getAccountName();
            int subId = AccountTypeUtils.getSubIdBySimAccountName(getApplicationContext(),
                    accountName);
            int emailCount = SimCardUtils.getIccCardEmailCount(subId);
            Log.d(TAG, "[isSupportShowEmailData] Usim type, accountName: " + accountName +
                    ",subId: " + subId + ",emailCount: " + emailCount);
            if (emailCount <= 0) {
                Log.i(TAG, "[isSupportShowEmailData] Usim not support email field,remove it!!");
                return false;
            }
        }
        return true;
    }
    /// @}
    /* prize zhangzhonghao 20160314 start */
    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch(v.getId()){
        	case R.id.no_contact_data_text_add_phone_num: 
        	case R.id.no_contact_data_add_email_address:
            case R.id.prize_contacts_editor_layout:
            	/*prize-change-huangliemin-2016-7-15-start*/
            	/*
                if (DirectoryContactUtil.isDirectoryContact(mContactData)) {
                    // This action is used to launch the contact selector, with the option of
                    // creating a new contact. Creating a new contact is an INSERT, while selecting
                    // an exisiting one is an edit. The fields in the edit screen will be
                    // prepopulated with data.

                    final Intent intent = new Intent(Intent.ACTION_INSERT_OR_EDIT);
                    intent.setType(Contacts.CONTENT_ITEM_TYPE);

                    ArrayList<ContentValues> values = mContactData.getContentValues();

                    // Only pre-fill the name field if the provided display name is an nickname
                    // or better (e.g. structured name, nickname)
                    if (mContactData.getDisplayNameSource() >= DisplayNameSources.NICKNAME) {
                        intent.putExtra(Intents.Insert.NAME, mContactData.getDisplayName());
                    } else if (mContactData.getDisplayNameSource()
                            == DisplayNameSources.ORGANIZATION) {
                        // This is probably an organization. Instead of copying the organization
                        // name into a name entry, copy it into the organization entry. This
                        // way we will still consider the contact an organization.
                        final ContentValues organization = new ContentValues();
                        organization.put(Organization.COMPANY, mContactData.getDisplayName());
                        organization.put(Data.MIMETYPE, Organization.CONTENT_ITEM_TYPE);
                        values.add(organization);
                    }

                    // Last time used and times used are aggregated values from the usage stat
                    // table. They need to be removed from data values so the SQL table can insert
                    // properly
                    for (ContentValues value : values) {
                        value.remove(Data.LAST_TIME_USED);
                        value.remove(Data.TIMES_USED);
                    }
                    intent.putExtra(Intents.Insert.DATA, values);

                    // If the contact can only export to the same account, add it to the intent.
                    // Otherwise the ContactEditorFragment will show a dialog for selecting an
                    // account.
                    if (mContactData.getDirectoryExportSupport() ==
                            Directory.EXPORT_SUPPORT_SAME_ACCOUNT_ONLY) {
                        intent.putExtra(Intents.Insert.EXTRA_ACCOUNT,
                                new Account(mContactData.getDirectoryAccountName(),
                                        mContactData.getDirectoryAccountType()));
                        intent.putExtra(Intents.Insert.EXTRA_DATA_SET,
                                mContactData.getRawContacts().get(0).getDataSet());
                    }

                    // Add this flag to disable the delete menu option on directory contact joins
                    // with local contacts. The delete option is ambiguous when joining contacts.
                    intent.putExtra(ContactEditorFragment.INTENT_EXTRA_DISABLE_DELETE_MENU_OPTION,
                            true);

                    QuickContactUtils.addSipExtra(intent);
                    startActivityForResult(intent, REQUEST_CODE_CONTACT_SELECTION_ACTIVITY);
                } else if (InvisibleContactUtil.isInvisibleAndAddable(mContactData, this)) {
                    InvisibleContactUtil.addToDefaultGroup(mContactData, this);
                } else if (isContactEditable()) {
                    editContact();
                }
                */
            	if(!isContactEditable()) {
            	    /*prize-change fix bug[44244]-hpf-2017-12-4-start*/
                    String phoneNumber = "";
            	    if (numberList.size() > 0){
                        phoneNumber = numberList.get(0);
                    }
                    /*prize-change fix bug[44244]-hpf-2017-12-4-end*/
            		Intent intent = new Intent(Intent.ACTION_INSERT, Contacts.CONTENT_URI);
            		intent.putExtra(ContactEditorActivity.INTENT_KEY_FINISH_ACTIVITY_ON_SAVE_COMPLETED, true);
            		intent.putExtra(ContactsSettingsUtils.ACCOUNT_TYPE, ContactsSettingsUtils.ALL_TYPE_ACCOUNT);
            		intent.putExtra(ContactsContract.Intents.Insert.PHONE, phoneNumber);
            		/*prize add for bug 59885 zhaojian 20180521 start*/
                    intent.setClassName(PRIZE_CONTACTS_PACKAGE_NAME,PRIZE_CONTACTS_EDITOR_ACTIVITY_NAME);
                    /*prize add for bug 59885 zhaojian 20180521 end*/
            		this.startActivityForResult(intent, REQUEST_CODE_CONTACT_SELECTION_ACTIVITY);
            	} else {
            		editContact();
            	}
                
                /*prize-change-huangliemin-2016-7-15-end*/
                break;
                
            /*prize-add-huangliemin-2016-7-15-start*/
                /*
            case R.id.prize_contacts_add_contacts:
            	if(!isContactEditable()) {
            		Log.i("logtest", "numberlist: "+numberList.size());
            		String phoneNumber = numberList.get(0);
            		Intent intent = new Intent(Intent.ACTION_INSERT_OR_EDIT);
            		intent.setType(ContactsContract.Contacts.CONTENT_ITEM_TYPE);
            		intent.putExtra(ContactsContract.Intents.Insert.PHONE, phoneNumber);
            		this.startActivityForResult(intent, REQUEST_CODE_CONTACT_SELECTION_ACTIVITY);
            	}
            	break;
            	*/
            /*prize-add-huangliemin-2016-7-15-end*/
                
            case R.id.prize_contacts_delete_layout:
            	/*prize-change-huangliemin-2016-7-21-start*/
                if (isContactEditable()) {
                    	deleteContact();
                    } else {
                		prizeAddToContacts();
                	}
                /*prize-change-huangliemin-2016-7-21-end*/
                break;
            
            /*prize-change-huangliemin-2016-7-16-start*/
            case R.id.prize_contacts_more_menu_layout:
            	showMoreMenuWindow();
            	break;
                /*prize-change-huangliemin-2016-7-16-start*/
                
            case R.id.mDefaultMusic:
                doPickRingtone();
                break;
            
            case R.id.mTwoDeminsionCode:
                getTwoDeminsion();
                break;
        }
    }
    
    /*PRIZE-change -yuandailin-2016-9-8-start*/
    public void showMoreMenuWindow() {
    	Resources mRes = getResources();
    	ArrayList<String> mMenuItemIds = new ArrayList<String>();
    	if(!TextUtils.isEmpty(prizeTwoDeminsionCodeNumber) && numberList!=null && numberList.size()>0) {
        	boolean isNumberInBlackList = PrizeBlockNumberHelper.getInstance(
        			getApplicationContext()).isNumberInBlacklist(prizeTwoDeminsionCodeNumber);
        	if(isNumberInBlackList) {
        		mMenuItemIds.add(mRes.getString(R.string.contact_delete_from_blacklist));
        	}else{
    		    mMenuItemIds.add(mRes.getString(R.string.prize_add_to_black_number));
        	}
    		if(isContactShareable())
    		mMenuItemIds.add(mRes.getString(R.string.menu_share));
        	/*prize-remvoe-2017-12-6-start*/
    		//if(isShortcutCreatable())
    		//mMenuItemIds.add(mRes.getString(R.string.prize_show_in_Launcher));
    		/*prize-remvoe-2017-12-6-start*/
    	} else {
            if (isContactShareable()) mMenuItemIds.add(mRes.getString(R.string.menu_share));
            /*prize-remvoe-2017-12-6-start*/
            //if(isShortcutCreatable())
            //mMenuItemIds.add(mRes.getString(R.string.prize_show_in_Launcher));
            /*prize-remvoe-2017-12-6-end*/
    	}
    /*PRIZE-change -yuandailin-2016-9-8-end*/	
    	
    	//Prize-change-zhudaopeng-2016-08-04 Start
//    	if(mPopuWindowView == null) {
//    		mPopuWindowView = View.inflate(this, R.layout.prize_popupwindow, null);
//    	}
    	mBottomMenuDialog = new DialogBottomMenu(this,mRes.getString(R.string.prize_contacts_more_menu_text_string),
    			R.layout.dialog_bottom_menu_item);
    	mBottomMenuDialog.setMenuItem(mMenuItemIds);
    	mBottomMenuDialog.setMenuItemOnClickListener(this);
    	mBottomMenuDialog.show();
    	
//    	final ViewGroup mMenuContent = (ViewGroup)mPopuWindowView.findViewById(R.id.prize_popup_content);
//    	
//    	if(mPopupWindow == null) {
//    		mPopupWindow = new PopupWindow(mPopuWindowView, 
//    				WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
//    		//mPopupWindow.setAnimationStyle(R.style.PrizePopupWindowStyle);
//    		mPopupWindow.setFocusable(true);
//    		mPopupWindow.setOutsideTouchable(false);
//    		//mPopupWindow.update();
//    	}
    	
//    	TextView TitleItem = (TextView)mPopuWindowView.findViewById(R.id.content_main_title);
//    	View TitleDivider = mPopuWindowView.findViewById(R.id.title_divider);//prize-add-huangliemin-2016-7-29
//    	TitleItem.setText(getResources().getString(R.string.prize_more));
//    	TitleDivider.setVisibility(View.VISIBLE);//prize-add-huangliemin-2016-7-29
//    	TitleItem.setVisibility(View.VISIBLE);
//    	
//    	mMenuContent.removeAllViews();
//    	for(int i=0;i<MenuItemIds.length;i++) {
//    		
//    		if(i==MenuItemIds.length-1) {
//    			if (mContactData != null && mContactData.getIndicate() >= 0) {
//                    continue;
//                } else {
//                    if(!isShortcutCreatable()){
//                    	continue;
//                    }
//                }
//    		} else if(MenuItemIds[i] == R.string.menu_share) {
//    			if(!isContactEditable()) {
//    				continue;
//    			}
//    		}
//    		TextView menuItem = (TextView)View.inflate(this, R.layout.prize_popmenu_text_huangliemin_2016_7_8, null);
//    		menuItem.setText(getResources().getString(MenuItemIds[i]));
//    		mMenuContent.addView(menuItem);
//    		menuItem.setOnClickListener(new PopuItemClickListener(MenuItemIds[i], this));
//    		View Divider = new View(this);
//        	Divider.setLayoutParams(new LayoutParams(WindowManager.LayoutParams.MATCH_PARENT,
//        			1));
//        	Divider.setBackgroundColor(getResources().getColor(R.color.divider_line_color_light));
//        	mMenuContent.addView(Divider);
//    	}
//    	mPopupWindow.showAtLocation(mPhotoView, Gravity.BOTTOM, 0, 0);
    	//Prize-change-zhudaopeng-2016-08-04 End
    }
    
    //Prize-add-zhudaopeng-2016-08-04 Start
    @Override
	public void onClickMenuItem(View v, int item_index, String item) {

		if(item.equals(getResources().getString(R.string.prize_add_to_black_number))){
				PrizeBlockNumberHelper.getInstance(getApplicationContext()).insertNumberList(numberList);	//PRIZE-change-yuandailin-2016-8-15
		} else if(item.equals(getResources().getString(R.string.contact_delete_from_blacklist))){
				PrizeBlockNumberHelper.getInstance(getApplicationContext()).deleteNumberList(numberList);//PRIZE-change-yuandailin-2016-8-15
		}else if(item.equals(getResources().getString(R.string.menu_share))){
			
			if (isContactShareable()) {
                shareContact();
            }
		}else if(item.equals(getResources().getString(R.string.prize_show_in_Launcher))){
			
            if (isShortcutCreatable()) {
            createLauncherShortcutWithContact();
            }
		} 
	}
    /*PRIZE-change -yuandailin-2016-8-10-end*/
    
    class PopuItemClickListener implements View.OnClickListener {
    	private int Id;
    	private Context mContext;
    	
    	public PopuItemClickListener(int id, Context context) {
    		Id = id;
    		mContext = context;
    	}
    	
    	@Override
    	public void onClick(View v) {
    		
//    		if(mPopupWindow!=null && mPopupWindow.isShowing()) {
//    			mPopupWindow.dismiss();
//    		}
    		
    		switch(Id) {
    		case R.string.prize_add_to_black_number:
    			PrizeBlockNumberHelper.getInstance(getApplicationContext()).insertNumberList(numberList);//PRIZE-change-yuandailin-2016-8-15
    			break;
    		case R.string.menu_share:
			
    			if (isContactShareable()) {
                    shareContact();
                }
    			break;
    		case R.string.prize_show_in_Launcher:
    			
                if (isShortcutCreatable()) {
                createLauncherShortcutWithContact();
                }
    			break;
    		case R.string.contact_delete_from_blacklist:
    			PrizeBlockNumberHelper.getInstance(getApplicationContext()).deleteNumberList(numberList);//PRIZE-change-yuandailin-2016-8-15
    			break;
    		}
    	}
    }
    /*prize-add-huangliemin-2016-7-16-end*/
    
    /*
     * set title with contact name
     */
    public void setTitle(String title) {
        ((TextView) findViewById(R.id.large_title)).setText(title);
        /*PRIZE-add aidl for get location by service -qiaohu-2018-6-11 -start*/
        if("1".equals(android.os.SystemProperties.get("ro.prize_cootek_enable", "1"))) {
        	if (!mIsContact) {
            	((TextView) findViewById(R.id.geo)).setText(getLocation(title));
            } else {
            	((TextView) findViewById(R.id.geo)).setText("");
            }
        }
        /*PRIZE-add aidl for get location by service -qiaohu-2018-6-11 -end*/
    }
    
    /*
     * change statusbar color
     */
    private void initStatusBar() {
        Window window = getWindow();
        window.requestFeature(Window.FEATURE_NO_TITLE);
        if(VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
	        window = getWindow();
	        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS 
	            | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
	        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
	            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
	        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
	        window.setStatusBarColor(Color.TRANSPARENT);
        }
    }
    
    /*
     * hide recentcard and show details card
     */
    private void hideRecentCard(){
        mContactCard.setVisibility(View.VISIBLE);
        //mNoContactDetailsCard.setVisibility(View.VISIBLE);//prize-remove huangpengfei-2016-8-20
        mNoContactDetailsLayout.setVisibility(View.VISIBLE);//prize-add huangpengfei-2016-8-20
        mRecentCard.setVisibility(View.GONE);
        mAboutCard.setVisibility(View.VISIBLE);
        prizeQuickBottomButtonLinearLayout.setVisibility(View.VISIBLE);
        prizeQuickDefaultMusiceLinearLayout.setVisibility(View.VISIBLE);
        prizeQuickNoRecentsNotice.setVisibility(View.GONE);
        prizeQuickTwoDeminsionLinearLayout.setVisibility(/*View.VISIBLE*/View.GONE);//prize-change-huangliemin-2016-7-16
        getActionBar().setTitle(""/*getResources().getString(R.string.prize_quickcontact_title)*/);
        /*prize-add for dido os8.0 -hpf-2017-8-8-start*/
        if(mPrizeTitle != null && !mIsPersonalInfo && !mIsPersonalInfoFromEditor){
        	mPrizeTitle.setText(getResources().getString(R.string.prize_quickcontact_title));
        }
        /*prize-add for dido os8.0 -hpf-2017-8-8-end*/
    }
    
    /*
     * hide detailcard and show recentcard
     */
    private void hideDetailCard(){
        mContactCard.setVisibility(View.GONE);
        //mNoContactDetailsCard.setVisibility(View.GONE);//prize-remove huangpengfei-2016-8-20
        mNoContactDetailsLayout.setVisibility(View.GONE);//prize-add huangpengfei-2016-8-20
        mRecentCard.setVisibility(View.VISIBLE);
        mAboutCard.setVisibility(View.GONE);
        /*prize-delete-huangliemin-2016-7-15-end*/
        prizeQuickBottomButtonLinearLayout.setVisibility(View.VISIBLE);
        prizeQuickDefaultMusiceLinearLayout.setVisibility(View.GONE);
        prizeQuickNoRecentsNotice.setVisibility(View.GONE);//prize-change-huangliemin-2016-7-22
        prizeQuickTwoDeminsionLinearLayout.setVisibility(/*View.VISIBLE*/View.GONE);//prize-change-huangliemin-2016-7-16
        getActionBar().setTitle(""/*getResources().getString(R.string.prize_recordcard_title)*/);
        /*prize-add for dido os8.0 -hpf-2017-8-8-start*/
        if(mPrizeTitle != null){
        	mPrizeTitle.setText(getResources().getString(R.string.prize_recordcard_title));
        }
        /*prize-add for dido os8.0 -hpf-2017-8-8-end*/
    }
    
    /*
     * set default music for this contact
     */
    private void doPickRingtone() {
        Log.d(TAG, "[doPickRingtone]");
        final Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
        
        /*prize-add-fix bug:[55741]-hpf-2018-4-19-start*/
        intent.setComponent(new android.content.ComponentName(
        		new String("com.android.providers.media"), 
        		new String("com.android.providers.media.RingtonePickerActivity")));
        /*prize-add-fix bug:[55741]-hpf-2018-4-19-end*/
        
        // Allow user to pick 'Default'
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, /*true*/false);//prize-change hint default ringtone-huangpengfei-2017-2-10
        // Show only ringtones
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_RINGTONE);
        // Allow the user to pick a silent ringtone
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, /*true*/false);  //prize modify for bug 59262 zhaojian 20180522

        final Uri ringtoneUri;
        if (mCustomRingtone != null) {
            ringtoneUri = Uri.parse(mCustomRingtone);
        } else {
            // Otherwise pick default ringtone Uri so that something is selected.
            /*prize-change fix bug:[44796][44988] -hpf-2017-12-11-start*/
            //ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            ringtoneUri = RingtoneManager.getActualDefaultRingtoneUri(this,RingtoneManager.TYPE_RINGTONE);
            /*prize-change fix bug:[44796][44988] -hpf-2017-12-11-end*/
        }

        // Put checkmark next to the current ringtone for this contact
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, ringtoneUri);

        // Launch!
        try {
            startActivityForResult(intent, 8);
        } catch (ActivityNotFoundException ex) {
            Toast.makeText(QuickContactActivity.this, R.string.missing_app, Toast.LENGTH_SHORT).show();
        }
    }
    
    /*
     * set default music
     */
    private void onRingtonePicked(Uri pickedUri) {
        if (pickedUri == null || RingtoneManager.isDefault(pickedUri)) {
            mCustomRingtone = null;
            prizeQuickDefaultMusicName.setText("");
        } else {
            mCustomRingtone = pickedUri.toString();
            prizeQuickDefaultMusicName.setText(RingtoneManager.getRingtone(this, Uri.parse(mCustomRingtone)).getTitle(this));
        }
        Intent intent = ContactSaveService.createSetRingtone(
                QuickContactActivity.this, mLookupUri, mCustomRingtone);
        Log.d(TAG, "[onRingtonePicked]start ContactSaveService,intent = " + intent);
        startService(intent);
    }
    
    /*
     * get twodeminsion code for number
     */
	private void getTwoDeminsion() {
		Intent intent = new Intent(QuickContactActivity.this, QuickMarkActivity.class);
		intent.putExtra("number",
				getResources().getString(R.string.header_phone_entry) + ":" + twoDeminsionCodeNumber.toString());
		if (mContactData != null) {
			intent.putExtra("name",
					getResources().getString(R.string.header_name_entry) + ":" + mContactData.getDisplayName() + "\n");
			intent.putExtra("display_name", mContactData.getDisplayName() + "\n");
		} else {
			intent.putExtra("name", "null");
		}

		/* prize-add huangpengfei-2016-8-12-start */
		String photoUri = mLookupUri.toString();
		Log.d(TAG, "mLookupUri=============" + photoUri);
		if (mLookupUri != null) {
			PrizeQuickMarkDataManager.setContact(mContactData);
			intent.putExtra("photoUri", photoUri);
		} else {
			PrizeQuickMarkDataManager.setContact(null);
			intent.putExtra("photoUri", "null");

		}

		/* prize-add huangpengfei-2016-8-12-end */
		startActivity(intent);
	}
    
    /* prize zhangzhonghao 20160314 end */
    
    /*prize-add huangliemin-2016-5-26-start*/
    private void changeTDCandDM(){
        if(mExtraMode == 31){
            prizeQuickTwoDeminsionLinearLayout.setVisibility(View.GONE);
            mDivider2.setVisibility(View.GONE);
            
        }else{
            prizeQuickTwoDeminsionLinearLayout.setVisibility(View.GONE);//prize-add-huangliemin-2016-7-18
            mDivider2.setVisibility(View.GONE);//prize-change-huangliemin-2016-7-21
        }
    }
    /*prize-add huangliemin-2016-5-26-end*/
    
    /*prize-add for dido os-hpf-2017-8-9-start*/
    private void showVideoCallChoiceDialog(List<Entry> entryList){
    	Resources mRes = getResources();
    	ArrayList<String> mMenuItemIds = new ArrayList<String>();
    	Log.d(TAG,"[showVideoCallChoiceDialog]   entryList.size = "+entryList.size());
    	for (Entry entry : entryList) {
    		String number = entry.getHeader();
    		mMenuItemIds.add(number);
    		Log.d(TAG,"[showVideoCallChoiceDialog]   number = "+number);
		}
    	
    	mBottomMenuDialog = new DialogBottomMenu(this,mRes.getString(R.string.prize_video_call),
    			R.layout.dialog_bottom_menu_item);
    	mBottomMenuDialog.setMenuItem(mMenuItemIds);
    	mBottomMenuDialog.configView(new DialogBottomMenu.ConfigViewCallBack() {
    			
    			@Override
    			public void callBack(View v,int position){
    				Log.d(TAG,"[callBack]");
    				Entry entry = entryList.get(position);
    				v.setTag(new EntryTag(entry.getId(), entry.getThirdIntent()));
    				
    			}
    	});
    	mBottomMenuDialog.setPrizeMenuItemOnClickListener(mEntryClickHandler);
    	mBottomMenuDialog.show();
    	
    }
    
    private void initRandomContactsBg(){
    	int bgRes[] = {R.drawable.prize_contacts_bg1,R.drawable.prize_contacts_bg2,R.drawable.prize_contacts_bg3,
    			R.drawable.prize_contacts_bg4,R.drawable.prize_contacts_bg5,R.drawable.prize_contacts_bg6,
    			R.drawable.prize_contacts_bg7,R.drawable.prize_contacts_bg8,R.drawable.prize_contacts_bg9,
    			R.drawable.prize_contacts_bg10,R.drawable.prize_contacts_bg11,R.drawable.prize_contacts_bg12,
    			R.drawable.prize_contacts_bg13,R.drawable.prize_contacts_bg14,R.drawable.prize_contacts_bg15,
    			R.drawable.prize_contacts_bg16,R.drawable.prize_contacts_bg17,R.drawable.prize_contacts_bg18,
    			R.drawable.prize_contacts_bg19,R.drawable.prize_contacts_bg20,R.drawable.prize_contacts_bg21,
    			R.drawable.prize_contacts_bg22,R.drawable.prize_contacts_bg23,R.drawable.prize_contacts_bg24,
    			R.drawable.prize_contacts_bg25,R.drawable.prize_contacts_bg26};
    	Random random = new Random();
    	int nextInt = random.nextInt(26);
    	mPhotoContaienr.setBackground(getResources().getDrawable(bgRes[nextInt]));
    }
    
    /*prize-add for dido os-hpf-2017-8-9-end*/

    
}
