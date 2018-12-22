/*
* Copyright (C) 2014 MediaTek Inc.
* Modification based on code covered by the mentioned copyright
* and/or permission notice(s).
*/
/*
 * Copyright (C) 2010 The Android Open Source Project
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

package com.android.contacts.editor;

import android.content.Context;
import android.graphics.Rect;
import android.os.Parcel;
import android.os.Parcelable;

import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.ImsCall;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spannable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.TtsSpan;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.android.contacts.R;
import com.android.contacts.common.model.RawContactDelta;
import com.android.contacts.common.compat.PhoneNumberUtilsCompat;
import com.android.contacts.common.ContactsUtils;
import com.android.contacts.common.model.ValuesDelta;
import com.android.contacts.common.model.account.AccountType.EditField;
import com.android.contacts.common.model.dataitem.DataKind;
import com.android.contacts.common.util.PhoneNumberFormatter;


import com.mediatek.contacts.aassne.SimAasEditor;
import com.mediatek.contacts.editor.ContactEditorUtilsEx;
import com.mediatek.contacts.ExtensionManager;
import com.mediatek.contacts.GlobalEnv;
import com.mediatek.contacts.util.Log;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;//prize-add-for dido os 8.0-hpf-2018-2-24
import android.os.Handler;
/**
 * Simple editor that handles labels and any {@link EditField} defined for the
 * entry. Uses {@link ValuesDelta} to read any existing {@link RawContact} values,
 * and to correctly write any changes values.
 */
public class TextFieldsEditorView extends LabeledEditorView {
    private static final String TAG = "TextFieldsEditorView";

    private EditText[] mFieldEditTexts = null;
    private ViewGroup mFields = null;
    /*prize-remove for dido os 8.0-hpf-2017-7-19-start*/
    //private View mExpansionViewContainer;
    //private ImageView mExpansionView;
    /*prize-remove for dido os 8.0-hpf-2017-7-19-end*/
    private boolean mHideOptional = true;
    private boolean mHasShortAndLongForms;
    private int mMinFieldHeight;
    /*prize add-huangliemin 2016-6-1 start*/
    private int mEditTextTopPadding;
    private int mEditTextBottomPadding;
    /*prize add-huangliemin 2016-6-1 end*/
    private int mPreviousViewHeight;
    private int mHintTextColorUnfocused;
    /*prize-add for dido os 8.0-hpf-2017-7-19-start*/
    public static String MINITYPE_ADRESS = "vnd.android.cursor.item/postal-address_v2";
    public static String MINITYPE_NICK_NAME = "vnd.android.cursor.item/nickname";
    public static String MINITYPE_NOTE = "vnd.android.cursor.item/note";
    public static String MINITYPE_WEBSITE = "vnd.android.cursor.item/website";
    public static String MINITYPE_IMS = "vnd.android.cursor.item/ims";
    public static String MINITYPE_IM = "vnd.android.cursor.item/im";
    public static String MINITYPE_EMAIL = "vnd.android.cursor.item/email_v2";
    public static String MINITYPE_COMPANY = "vnd.android.cursor.item/organization";
    public static String MINITYPE_DISPLAY_NAME = "#displayName";
    public static String MINITYPE_PHONETIC_NAME = "#phoneticName";
    private Context mContext;
    private int mPrizeLengthFilter = /*25*/20;                // prize modify for bug 52011 and 52010 by zhaojian 20180308
    public static boolean mShowFinish = false;
    /*prize-add for dido os 8.0-hpf-2017-7-19-end*/
    public TextFieldsEditorView(Context context) {
        super(context);
    }

    public TextFieldsEditorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;//prize-add for dido os 8.0-hpf-2017-7-19
    }

    public TextFieldsEditorView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /** {@inheritDoc} */
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        setDrawingCacheEnabled(true);
        setAlwaysDrawnWithCacheEnabled(true);

        mMinFieldHeight = getContext().getResources().getDimensionPixelSize(
                R.dimen.editor_min_line_item_height);
        /*prize-add-huangliemin 2016-6-1 start*/
        mEditTextBottomPadding = getContext().getResources().getDimensionPixelSize(
                R.dimen.editor_text_field_bottom_padding);
        mEditTextTopPadding = getContext().getResources().getDimensionPixelSize(
                R.dimen.editor_text_field_top_padding);
        /*prize-add-huangliemin 2016-6-1 end*/
        mFields = (ViewGroup) findViewById(R.id.editors);
        mHintTextColorUnfocused = getResources().getColor(R.color.editor_disabled_text_color);
        /*prize-remove for dido os 8.0-hpf-2017-7-19-start*/
        /*mExpansionView = (ImageView) findViewById(R.id.expansion_view);
        mExpansionViewContainer = findViewById(R.id.expansion_view_container);
        if (mExpansionViewContainer != null) {
            mExpansionViewContainer.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mPreviousViewHeight = mFields.getHeight();

                    // Save focus
                    final View focusedChild = getFocusedChild();
                    final int focusedViewId = focusedChild == null ? -1 : focusedChild.getId();

                    /// M : Fix CR ALPS00809436 @{
                    InputMethodManager imm = (InputMethodManager) getContext().
                            getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null && v != null) {
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    }
                    /// @}

                    // Reconfigure GUI
                    mHideOptional = !mHideOptional;
                    onOptionalFieldVisibilityChange();
                    rebuildValues();

                    // Restore focus
                    View newFocusView = findViewById(focusedViewId);
                    if (newFocusView == null || newFocusView.getVisibility() == GONE) {
                        // find first visible child
                        newFocusView = TextFieldsEditorView.this;
                    }
                    newFocusView.requestFocus();

                    EditorAnimator.getInstance().slideAndFadeIn(mFields, mPreviousViewHeight);
                }
            });
        }*/
        /*prize-remove for dido os 8.0-hpf-2017-7-19-end*/
    }

    @Override
    public void editNewlyAddedField() {
        // Some editors may have multiple fields (eg: first-name/last-name), but since the user
        // has not selected a particular one, it is reasonable to simply pick the first.
        final View editor = mFields.getChildAt(0);
        editor.requestFocus();//prize-add-fix bug[46068] -hpf-2017-12-19
        // Show the soft-keyboard.
        InputMethodManager imm =
                (InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            if (!imm.showSoftInput(editor, InputMethodManager.SHOW_IMPLICIT)) {
                Log.w(TAG, "Failed to show soft input method.");
            }
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);

        if (mFieldEditTexts != null) {
            for (int index = 0; index < mFieldEditTexts.length; index++) {
                mFieldEditTexts[index].setEnabled(!isReadOnly() && enabled);
            }
        }
        /*prize-remove for dido os 8.0-hpf-2017-7-19-start*/
        /*if (mExpansionView != null) {
            mExpansionView.setEnabled(!isReadOnly() && enabled);
        }*/
        /*prize-remove for dido os 8.0-hpf-2017-7-19-end*/
    }

    private OnFocusChangeListener mTextFocusChangeListener = new OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
        	Log.d(TAG, "[onFocusChange]");
            if (getEditorListener() != null) {
                getEditorListener().onRequest(EditorListener.EDITOR_FOCUS_CHANGED);
            }
            // Rebuild the label spinner using the new colors.
            rebuildLabel();
        }
    };

    /**
     * Creates or removes the type/label button. Doesn't do anything if already correctly configured
     */
    /*prize-remove for dido os 8.0-hpf-2017-7-19-start*/
    /*private void setupExpansionView(boolean shouldExist, boolean collapsed) {
            mExpansionView.setImageResource(collapsed
                    ? R.drawable.ic_menu_expander_minimized_holo_light
                    : R.drawable.ic_menu_expander_maximized_holo_light);
        mExpansionViewContainer.setVisibility(shouldExist ? View.VISIBLE : View.INVISIBLE);
    }*/
    /*prize-remove for dido os 8.0-hpf-2017-7-19-end*/

    @Override
    protected void requestFocusForFirstEditField() {
        if (mFieldEditTexts != null && mFieldEditTexts.length != 0) {
            EditText firstField = null;
            boolean anyFieldHasFocus = false;
            for (EditText editText : mFieldEditTexts) {
                if (firstField == null && editText.getVisibility() == View.VISIBLE) {
                    firstField = editText;
                }
                if (editText.hasFocus()) {
                    anyFieldHasFocus = true;
                    break;
                }
            }
            if (!anyFieldHasFocus && firstField != null) {
                firstField.requestFocus();
            }
        }
    }

    public void setValue(int field, String value) {
        mFieldEditTexts[field].setText(value);
    }

    @Override
    public void setValues(DataKind kind, ValuesDelta entry, final RawContactDelta state,
            boolean readOnly, ViewIdGenerator vig) {
        /// M: For Icc card, it's nickName is null, so the kind is empty.
        if (kind == null || kind.fieldList == null) {
            return;
        }

        super.setValues(kind, entry, state, readOnly, vig);
        // Remove edit texts that we currently have
        if (mFieldEditTexts != null) {
            for (EditText fieldEditText : mFieldEditTexts) {
                mFields.removeView(fieldEditText);
            }
        }
        boolean hidePossible = false;

        int fieldCount = kind.fieldList == null ? 0 : kind.fieldList.size();
        /*prize-add for dido os 8.0-hpf-2017-7-19-start*/
        int paddingStart = mContext.getResources().getInteger(R.integer.contacts_text_fields_editor_padding_start);
        if (MINITYPE_ADRESS.equals(kind.mimeType)||MINITYPE_PHONETIC_NAME.equals(kind.mimeType)||MINITYPE_DISPLAY_NAME.equals(kind.mimeType)) {
        	fieldCount = 1;
        }
        if(MINITYPE_COMPANY.equals(kind.mimeType) && fieldCount >= 1){
        	setEditorDividerVisibility(true);
        }else{
        	setEditorDividerVisibility(false);
        }
        /*prize-add for dido os 8.0-hpf-2017-7-19-end*/
        mFieldEditTexts = new EditText[fieldCount];
        /* M: [Google Issue]ALPS03260782, 1/3 @{*/
        boolean showDeleteButton = false;
        /* @} */
        for (int index = 0; index < fieldCount; index++) {
            final EditField field = kind.fieldList.get(index);
            final EditText fieldView = new EditText(getContext());
            /*prize-add-for dido os 8.0-hpf-2017-7-18-start*/
            fieldView.setBackground(null);
            fieldView.setTextColor(mContext.getResources().getColor(R.color.prize_content_title_color));
            if(MINITYPE_DISPLAY_NAME.equals(kind.mimeType)){
            	boolean isLayoutFinish = false;
            	fieldView.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
					
					@Override
					public void onGlobalLayout() {
						Log.d(TAG,"[onGlobalLayout]  mShowFinish = "+mShowFinish);
						if(!mShowFinish){
							mShowFinish = true;
							new Handler().postDelayed(new Runnable() {
								
								@Override
								public void run() {
									editNewlyAddedField();
								}
							}, 300);
						}
					}
				});
            }
            /*prize-add-for dido os 8.0-hpf-2017-7-18-end*/
            int height = (int)getResources().getDimension(R.dimen.prize_single_content_height);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
            lp.height = height;
            fieldView.setLayoutParams(lp);
            /*prize-change for dido os 8.0-hpf-2017-7-18 start*/
            
            fieldView.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                    getResources().getDimension(R.dimen.prize_single_content_size));
            fieldView.setHintTextColor(mContext.getResources().getColor(R.color.prize_hint_edit_text_color));
            
            //fieldView.setTextAppearance(getContext(), android.R.style.TextAppearance_Small);
            //fieldView.setPadding(fieldView.getPaddingLeft(), mEditTextTopPadding,
            //        fieldView.getPaddingRight(), mEditTextBottomPadding);
            fieldView.setGravity(Gravity.CENTER_VERTICAL);
            fieldView.setFilters(new InputFilter[]{new InputFilter.LengthFilter(mPrizeLengthFilter)});
            /*prize-change for dido os 8.0-hpf-2017-7-18-end*/
            
            mFieldEditTexts[index] = fieldView;
            fieldView.setId(vig.getId(state, kind, entry, index));
            if (field.titleRes > 0) {
                fieldView.setHint(field.titleRes);
            }
            /*prize-add for dido os 8.0-hpf-2017-7-19-start*/
            if (MINITYPE_ADRESS.equals(kind.mimeType)) {
                fieldView.setHint(com.android.contacts.common.R.string.postal_address);
                // prize add by zhaojian 20171108 start
                fieldView.setHorizontallyScrolling(true);
                // prize add by zhaojian 20171108 end
            }
            if(MINITYPE_NOTE.equals(kind.mimeType)
            		||MINITYPE_NICK_NAME.equals(kind.mimeType)
            		||MINITYPE_WEBSITE.equals(kind.mimeType)
            		||MINITYPE_IMS.equals(kind.mimeType)){
            	setPrizeFieldsEditorBtnEnable(false);
            	fieldView.setPadding(paddingStart, 0,
                        fieldView.getPaddingRight(), 0);
            	
            }else{
            	setDeletable(false);
            }
            /*prize-add for dido os 8.0-hpf-2017-7-19-end*/
            /// M: For AAS.
            if (Phone.CONTENT_ITEM_TYPE.equals(kind.mimeType)) {
                GlobalEnv.getAasExtension().updateView(state, fieldView, entry,
                        SimAasEditor.VIEW_UPDATE_HINT);
            }

            /** M: VOLTE IMS Call feature. @{ */
            if (ImsCall.CONTENT_ITEM_TYPE.equals(kind.mimeType)) {
                fieldView.setHint(com.android.contacts.common.R.string.imsCallLabelsGroup);
            }
            /** @} */

            /// M: New Feature xxx @{
            final int inputType = field.inputType;
            fieldView.setInputType(inputType);
            if (inputType == InputType.TYPE_CLASS_PHONE) {
                /// M:op01 will add its own listener to filter phone number.
                ExtensionManager.getInstance().getOp01Extension().setViewKeyListener(fieldView);

                PhoneNumberFormatter.setPhoneNumberFormattingTextWatcher(
                        getContext(), fieldView, /* formatAfterWatcherSet =*/ false);
                fieldView.setTextDirection(View.TEXT_DIRECTION_LTR);
            }
            fieldView.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);

            // Set either a minimum line requirement or a minimum height (because {@link TextView}
            // only takes one or the other at a single time).
            if (field.minLines > 1) {
                fieldView.setMinLines(field.minLines);
            } else {
                // This needs to be called after setInputType. Otherwise, calling setInputType
                // will unset this value.
                fieldView.setMinHeight(mMinFieldHeight);
            }

            // Show the "next" button in IME to navigate between text fields
            // TODO: Still need to properly navigate to/from sections without text fields,
            // See Bug: 5713510
            fieldView.setImeOptions(EditorInfo.IME_ACTION_NEXT | EditorInfo.IME_FLAG_NO_FULLSCREEN);

            // Read current value from state
            final String column = field.column;
            final String value = entry.getAsString(column);
            if (ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE.equals(kind.mimeType)) {
                fieldView.setText(PhoneNumberUtilsCompat.createTtsSpannable(value));
            } else {
                /// M: Bug fix ALPS00244669. @{
            	/*prize-remove-hpf-2017-11-2-start*/
                /*Log.d(TAG, "setValues setFilter");
                fieldView.setFilters(new InputFilter[] { new InputFilter.LengthFilter(
                        ContactEditorUtilsEx.getFieldEditorLengthLimit(inputType)) });*/
            	/*prize-remove-hpf-2017-11-2-end*/
                /// @}
                fieldView.setText(value);
            }

            // Show the delete button if we have a non-empty value
            /* M: [Google Issue]ALPS03260782, 2/3, show the delete button
             * if there is at least one non-empty value on all fieldView.
             * Original code: @{
            setDeleteButtonVisible(!TextUtils.isEmpty(value));
             * @}
             * New code: @{ */
            if (!TextUtils.isEmpty(value) && !showDeleteButton) {
                showDeleteButton = true;
            }
            /* @} */

            // Prepare listener for writing changes
            fieldView.addTextChangedListener(new TextWatcher() {
                @Override
                public void afterTextChanged(Editable s) {
                    // Trigger event for newly changed value
                    onFieldChanged(column, s.toString());
                    //M:OP01 RCS will listen phone number text change.@{
                    ExtensionManager.getInstance().getRcsExtension().
                            setTextChangedListener(state, fieldView, inputType, s.toString());
                    /** @} */
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    ((com.android.contacts.activities.ContactEditorActivity)mContext).onChange();//prize-add-hpf-2017-12-4
                    if (!ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE.equals(
                            getKind().mimeType) || !(s instanceof Spannable)) {
                        return;
                    }
                    final Spannable spannable = (Spannable) s;
                    final TtsSpan[] spans = spannable.getSpans(0, s.length(), TtsSpan.class);
                    for (int i = 0; i < spans.length; i++) {
                        spannable.removeSpan(spans[i]);
                    }
                    PhoneNumberUtilsCompat.addTtsSpan(spannable, 0, s.length());
                }
            });

            fieldView.setEnabled(isEnabled() && !readOnly);
            //prize-delete-huangliemin 2016-6-1 start
            //fieldView.setOnFocusChangeListener(mTextFocusChangeListener);
            //prize-delete-huangliemin 2016-6-1 end

            if (field.shortForm) {
                hidePossible = true;
                mHasShortAndLongForms = true;
                fieldView.setVisibility(mHideOptional ? View.VISIBLE : View.GONE);
            } else if (field.longForm) {
                hidePossible = true;
                mHasShortAndLongForms = true;
                fieldView.setVisibility(mHideOptional ? View.GONE : View.VISIBLE);
            } else {
                // Hide field when empty and optional value
                final boolean couldHide = (!ContactsUtils.isGraphic(value) && field.optional);
                final boolean willHide = (mHideOptional && couldHide);
                fieldView.setVisibility(willHide ? View.GONE : View.VISIBLE);
                hidePossible = hidePossible || couldHide;
            }
            mFields.addView(fieldView);
            ((com.android.contacts.activities.ContactEditorActivity)mContext).onChange();//prize-add-hpf-2017-12-4
        }
        /* M: [Google Issue]ALPS03260782, 3/3 @{ */
        setDeleteButtonVisible(showDeleteButton);
        /* @} */

        /*prize-remove for dido os 8.0-hpf-2017-7-19-start*/
        /*if (mExpansionView != null) {
            // When hiding fields, place expandable
            setupExpansionView(hidePossible, mHideOptional);
            mExpansionView.setEnabled(!readOnly && isEnabled());
        }*/
        /*prize-remove for dido os 8.0-hpf-2017-7-19-end*/
        updateEmptiness();
    }

    @Override
    public boolean isEmpty() {
        for (int i = 0; i < mFields.getChildCount(); i++) {
            EditText editText = (EditText) mFields.getChildAt(i);
            /*prize-change-hpf-2017-10-31-start*/
            String text = editText.getText().toString();
            if (!TextUtils.isEmpty(text.trim())) {
            /*prize-change-hpf-2017-10-31-end*/	
                return false;
            }
        }
        return true;
    }

    /**
     * Returns true if the editor is currently configured to show optional fields.
     */
    public boolean areOptionalFieldsVisible() {
        return !mHideOptional;
    }

    public boolean hasShortAndLongForms() {
        return mHasShortAndLongForms;
    }

    /**
     * Populates the bound rectangle with the bounds of the last editor field inside this view.
     */
    public void acquireEditorBounds(Rect bounds) {
        if (mFieldEditTexts != null) {
            for (int i = mFieldEditTexts.length; --i >= 0;) {
                EditText editText = mFieldEditTexts[i];
                if (editText.getVisibility() == View.VISIBLE) {
                    bounds.set(editText.getLeft(), editText.getTop(), editText.getRight(),
                            editText.getBottom());
                    return;
                }
            }
        }
    }

    /**
     * Saves the visibility of the child EditTexts, and mHideOptional.
     */
    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState ss = new SavedState(superState);

        ss.mHideOptional = mHideOptional;

        final int numChildren = mFieldEditTexts == null ? 0 : mFieldEditTexts.length;
        ss.mVisibilities = new int[numChildren];
        for (int i = 0; i < numChildren; i++) {
            ss.mVisibilities[i] = mFieldEditTexts[i].getVisibility();
        }

        return ss;
    }

    /**
     * Restores the visibility of the child EditTexts, and mHideOptional.
     */
    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());

        mHideOptional = ss.mHideOptional;

        /// M: Bug fix, add null pointer check.
        if (null != mFieldEditTexts) {
            int numChildren = Math.min(mFieldEditTexts == null ? 0 : mFieldEditTexts.length,
                ss.mVisibilities == null ? 0 : ss.mVisibilities.length);
            for (int i = 0; i < numChildren; i++) {
                mFieldEditTexts[i].setVisibility(ss.mVisibilities[i]);
            }
        }
    }

    private static class SavedState extends BaseSavedState {
        public boolean mHideOptional;
        public int[] mVisibilities;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            mVisibilities = new int[in.readInt()];
            in.readIntArray(mVisibilities);
            /// M: Bug fix ALPS00564820.
            mHideOptional = in.readInt() == 1 ? true : false;
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(mVisibilities.length);
            out.writeIntArray(mVisibilities);
            /// M: Bug fix ALPS00564820.
            out.writeInt(mHideOptional ? 1 : 0);
        }

        @SuppressWarnings({"unused", "hiding" })
        public static final Parcelable.Creator<SavedState> CREATOR
                = new Parcelable.Creator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

    @Override
    public void clearAllFields() {
        if (mFieldEditTexts != null) {
            for (EditText fieldEditText : mFieldEditTexts) {
                // Update UI (which will trigger a state change through the {@link TextWatcher})
                fieldEditText.setText("");
            }
        }
    }
    
    /*prize-add-hpf-2017-11-2-start*/
    public void prizeSetEditTextLengthFilter(int length){
    	this.mPrizeLengthFilter = length;
    }
    /*prize-add-hpf-2017-11-2-start*/
    
    
}
