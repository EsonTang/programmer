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

package com.android.contacts.editor;

import android.content.Context;
import android.provider.ContactsContract.CommonDataKinds.ImsCall;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Data;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;//prize-add-huangliemin-2016-5-31

import com.android.contacts.R;
import com.android.contacts.editor.Editor.EditorListener;
import com.android.contacts.editor.LabeledEditorView.OnAddItemRequestListener;//prize-add for dido os 8.0-hpf-2017-7-19
import com.android.contacts.common.model.RawContactModifier;
import com.android.contacts.common.model.RawContactDelta;
import com.android.contacts.common.model.ValuesDelta;
import com.android.contacts.common.model.dataitem.DataKind;

import java.util.ArrayList;
import java.util.List;

import com.mediatek.contacts.GlobalEnv;
import com.mediatek.contacts.aassne.SimAasEditor;
import com.mediatek.contacts.util.AccountTypeUtils;
import com.mediatek.contacts.util.Log;
import com.mediatek.contacts.simcontact.SimCardUtils;

/**
 * Custom view for an entire section of data as segmented by
 * {@link DataKind} around a {@link Data#MIMETYPE}. This view shows a
 * section header and a trigger for adding new {@link Data} rows.
 */
public class KindSectionView extends LinearLayout implements EditorListener {

    private static final String TAG = "KindSectionView";
    public interface Listener {

        /**
         * Invoked when any editor that is displayed in this section view is deleted by the user.
         */
        public void onDeleteRequested(Editor editor);
    }

    private ViewGroup mEditors;
    private ImageView mIcon;
    /*prize add-some-view huangliemin-2016-5-31 start*/
    private TextView mTitle;
    private View mAddFieldFooter;
    private String mTitleString;
    /*prize add-some-view huangliemin-2016-5-31 end*/
    
    private boolean mIsSaveForSIMCard = false;//prize add-huangpengfei-2016-8-26

    private DataKind mKind;
    private RawContactDelta mState;
    private boolean mReadOnly;

    private ViewIdGenerator mViewIdGenerator;

    private LayoutInflater mInflater;

    private Listener mListener;

    private final ArrayList<Runnable> mRunWhenWindowFocused = new ArrayList<Runnable>(1);//prize-add-huangliemin-2016-5-31
    private Context mContext;//prize-add-for dido os 8.0-hpf-2017-7-18
    
    public KindSectionView(Context context) {
        this(context, null);
    }

    public KindSectionView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;//prize-add-for dido os 8.0-hpf-2017-7-18
    }

    @Override
    public void setEnabled(boolean enabled) {
    	Log.d(TAG, "[setEnabled]");
        super.setEnabled(enabled);
        if (mEditors != null) {
            int childCount = mEditors.getChildCount();
            for (int i = 0; i < childCount; i++) {
                mEditors.getChildAt(i).setEnabled(enabled);
            }
        }

        updateEmptyEditors(/* shouldAnimate = */ true);
    }

    public boolean isReadOnly() {
    	Log.d(TAG, "[isReadOnly]");
        return mReadOnly;
    }

    /** {@inheritDoc} */
    @Override
    protected void onFinishInflate() {
    	Log.d(TAG, "[onFinishInflate]");
        setDrawingCacheEnabled(true);
        setAlwaysDrawnWithCacheEnabled(true);

        mInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        mEditors = (ViewGroup) findViewById(R.id.kind_editors);
        mIcon = (ImageView) findViewById(R.id.kind_icon);
        /*prize add-some-view huangliemin-2016-5-31 start*/
        mTitle = (TextView) findViewById(R.id.kind_title);
        mAddFieldFooter = findViewById(R.id.add_field_footer);
        mAddFieldFooter.setVisibility(View.GONE);//PRIZE-add-yuandailin-2016-8-8
        mAddFieldFooter.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // Setup click listener to add an empty field when the footer is clicked.
                mAddFieldFooter.setVisibility(View.GONE);
                addItem();
            }
        });
        /*prize add-some-view huangliemin-2016-5-31 end*/
        
       
    }

    @Override
    public void onDeleteRequested(Editor editor) {
       Log.d(TAG, "[onDeleteRequested]");
        /*prize add-huangliemin 2016-5-31 start*/
        final boolean animate;
        /*prize add-huangliemin 2016-5-31 end*/
        if ((getEditorCount() == 1 || GlobalEnv.getAasExtension().updateView(
                mState, null, null, SimAasEditor.VIEW_UPDATE_DELETE_EDITOR))) {
            // If there is only 1 editor in the section, then don't allow the user to delete it.
            // Just clear the fields in the editor.
            editor.clearAllFields();
            animate=true;
        } else {
            // If there is a listener, let it decide whether to delete the Editor or the entire
            // KindSectionView so that there is no jank from both animations happening in succession
            if (mListener != null) {
                editor.markDeleted();
                mListener.onDeleteRequested(editor);
            } else {
                editor.deleteEditor();
                /// M: change for ALPS02335993,
                // SIM or Exchange contact all have child count limit, ex. SIM contact can just
                // insert 2 phone numbers. Could not add the empty editor if child count reach the
                // max count, but it still not show the empty editor after user delete one item.
                // So add this code to add the empty editor after delete. @{
                if (!hasEmptyEditor()) {
                    updateEmptyEditors(false);
                }
                /// @}
            }
            animate=false;
         }
         /*prize-add-for dido os 8.0-hpf-2018-3-6 start*/
         upDateEditorAddOrDeleteBtn();
         /*prize-add-for dido os 8.0-hpf-2017-3-6 end*/
    }

    @Override
    public void onRequest(int request) {
    	Log.d(TAG,"[onRequest]");
        // If a field has become empty or non-empty, then check if another row
        // can be added dynamically.
        if (/*request == FIELD_TURNED_EMPTY ||*/ request == FIELD_TURNED_NON_EMPTY) {//prize-change-for dido os 8.0-hpf-2017-7-19
        	/*prize add huangpengfei-2016-8-26-start*/
        	Log.d(TAG, "[onRequest]     mIsSaveForSIMCard="+mIsSaveForSIMCard+"   request = "+request);
        	if(mIsSaveForSIMCard){
        		updateEmptyEditors(/* shouldAnimate = */ true);
        	}
    		if(mIsSaveForSIMCard & getEditorCount() >1){
    			return;
    		}
    		if(getEmptyEditors().size()<1){
    			addItem();
    		}
        	/*prize add huangpengfei-2016-8-26-end*/
            
            /*PRIZE-change -yuandailin-2016-8-8-start*/
//            updateAddFooterVisible(true);
        	//updateEmptyEditors(/* shouldAnimate = */ true);//prize remove huangpengfei-2016-8-26
            
            //addItem();//prize remove huangpengfei-2016-8-26
            /*PRIZE-change -yuandailin-2016-8-8-end*/
        }
    }

    public void setListener(Listener listener) {
    	Log.d(TAG, "[setListener]");
        mListener = listener;
    }

    public void setState(DataKind kind, RawContactDelta state, boolean readOnly,
            ViewIdGenerator vig) {
    	Log.d(TAG, "[setState]   kind.mimeType = "+kind.mimeType);
        mKind = kind;
        mState = state;
        mReadOnly = readOnly;
        mViewIdGenerator = vig;
        
        /*prize add-huangpengfei-2016-8-26-start*/
        String saveLocation = state.getAccountName();
        if(saveLocation != null){
	        String reg=".*SIM.*"; 
	        if(saveLocation.matches(reg)){
	        	//init tag
	        	mIsSaveForSIMCard = true;
	        }else{
	        	//reset this tag
	        	mIsSaveForSIMCard = false;
	        }
        }
        Log.d(TAG,"[setState]   saveLocation="+saveLocation);
        /*prize add-huangpengfei-2016-8-26-end*/
        
        setId(mViewIdGenerator.getId(state, kind, null, ViewIdGenerator.NO_VIEW_INDEX));

        // TODO: handle resources from remote packages
        /*prize change-huangliemin 2016-5-31 start*/
        /*
        final String titleString = (kind.titleRes == -1 || kind.titleRes == 0)
                ? ""
                : getResources().getString(kind.titleRes);
        mIcon.setContentDescription(titleString);
        */
        mTitleString = (kind.titleRes == -1 || kind.titleRes == 0)
                ? ""
                : getResources().getString(kind.titleRes);
        mTitle.setText(mTitleString);
        
        /*prize change-huangliemin 2016-5-31 end*/

        /** M: Bug Fix for ALPS00557517 @{ */
        String accountType = mState.getAccountType();
        if ((AccountTypeUtils.ACCOUNT_TYPE_USIM.equals(accountType)
                || AccountTypeUtils.ACCOUNT_TYPE_CSIM.equals(accountType))
                && mKind.mimeType.equals(Phone.CONTENT_ITEM_TYPE)) {
            int subId = AccountTypeUtils.getSubIdBySimAccountName(mContext, mState
                    .getAccountName());
            mKind.typeOverallMax = SimCardUtils.getAnrCount(subId) + 1;
            Log.d(TAG, "[setState] Usim max number = ANR + 1 = " + mKind.typeOverallMax);
        }
        Log.d(TAG, "[setState]   mTitleString="+mTitleString+"   [mKind.typeOverallMax]="+mKind.typeOverallMax);
        /** @ } */
        /*prize remove-kind-icon huangliemin 2016-5-31 start*/
        /*
        mIcon.setImageDrawable(getMimeTypeDrawable(kind.mimeType));
        if (mIcon.getDrawable() == null) {
            mIcon.setContentDescription(null);
        }
        */
        /*prize remove-kind-icon huangliemin 2016-5-31 end*/
        
        /*prize-add-for dido os 8.0-hpf-2017-7-19-start*/
        //Add default item 
        String miniType = kind.mimeType;
        if(TextFieldsEditorView.MINITYPE_ADRESS.equals(miniType)||
        		TextFieldsEditorView.MINITYPE_NOTE.equals(miniType)||
        		TextFieldsEditorView.MINITYPE_IM.equals(miniType)||
                /*prize add for bug 55485 by zhaojian 20180505 start*/
                TextFieldsEditorView.MINITYPE_EMAIL.equals(miniType)){
                /*prize add for bug 55485 by zhaojian 20180505 end*/
        	addItem();
        }
        /*prize-add-for dido os 8.0-hpf-2017-7-19-end*/
        
        rebuildFromState();
        updateEmptyEditors(/* shouldAnimate = */ false);
        /*prize add-huangliemin-2016-5-31 start*/
        updateAddFooterVisible(false);
        updateSectionVisible();
        /*prize add-huangliemin-2016-5-31 end*/
     
    }
    

    /**
     * Build editors for all current {@link #mState} rows.
     */
    private void rebuildFromState() {
    	Log.d(TAG, "[rebuildFromState]");
        // Remove any existing editors
        mEditors.removeAllViews();
        ///M:
        ArrayList<ValuesDelta> values = GlobalEnv.getAasExtension()
                .rebuildFromState(mState, mKind.mimeType);
        if (values != null) {
            for (ValuesDelta entry : values) {
                // Skip entries that aren't visible
                if (!entry.isVisible()) continue;
                if (isEmptyNoop(entry)) continue;             
                createEditorView(entry);
            }
        }
    }


    /**
     * Creates an EditorView for the given entry. This function must be used while constructing
     * the views corresponding to the the object-model. The resulting EditorView is also added
     * to the end of mEditors
     */
    private View createEditorView(ValuesDelta entry) {
    	Log.d(TAG, "[createEditorView]");
        final View view;
        final int layoutResId = EditorUiUtils.getLayoutResourceId(mKind.mimeType);
        try {
            view = mInflater.inflate(layoutResId, mEditors, false);
        } catch (Exception e) {
            throw new RuntimeException(
                    "Cannot allocate editor with layout resource ID " +
                    layoutResId + " for MIME type " + mKind.mimeType +
                    " with error " + e.toString());
        }
        view.setEnabled(isEnabled());
        if (view instanceof Editor) {
            Editor editor = (Editor) view;
            //editor.setDeletable(true);////prize remove-huangpengfei-2016-8-26
            editor.setValues(mKind, entry, mState, mReadOnly, mViewIdGenerator);
            editor.setEditorListener(this);
        }
        mEditors.addView(view);

		/* prize-add for dido os 8.0-hpf-2017-7-19-start */
		if (view instanceof LabeledEditorView) {
			upDateEditorAddOrDeleteBtn();
			((LabeledEditorView) view).setOnAddItemRequestListener(new OnAddItemRequestListener() {

				@Override
				public void onAddItemRequest() {
					Log.d(TAG, "[onAddItemRequest]");
					addItem();
				}
			});
		}
		/* prize-add for dido os 8.0-hpf-2017-7-19-end */
        return view;
    }

    /**
     * Tests whether the given item has no changes (so it exists in the database) but is empty
     */
    private boolean isEmptyNoop(ValuesDelta item) {
    	Log.d(TAG, "[isEmptyNoop]");
        if (!item.isNoop()) return false;
        final int fieldCount = mKind.fieldList.size();
        for (int i = 0; i < fieldCount; i++) {
            final String column = mKind.fieldList.get(i).column;
            final String value = item.getAsString(column);
            if (!TextUtils.isEmpty(value)) return false;
        }
        return true;
    }


    /*prize add-some-view huangliemin-2016-5-31 start*/
    private void updateSectionVisible() {
    	Log.d(TAG, "[updateSectionVisible]");
        setVisibility(getEditorCount() != 0 ? VISIBLE : GONE);
    }
    
    public String getTitle() {
    	Log.d(TAG, "[getTitle]");
        return mTitleString;
    }

    protected void updateAddFooterVisible(boolean animate) {
    	Log.d(TAG, "[updateAddFooterVisible]"+"animate: "+animate);
        if (!mReadOnly && (mKind.typeOverallMax != 1)) {
            // First determine whether there are any existing empty editors.
            updateEmptyEditors(animate);
            // If there are no existing empty editors and it's possible to add
            // another field, then make the "add footer" field visible.
            if (!hasEmptyEditor() && RawContactModifier.canInsert(mState, mKind)) {
//                if (ExtensionManager.getInstance().getAasExtension()
//                            .updateView(mState, mAddFieldFooter, null, IAasExtension.VIEW_UPDATE_VISIBILITY)) {
//                    return;
//                }
                /*PRIZE-remove -yuandailin-2016-8-8-start*/          
//                if (animate) {
//                    EditorAnimator.getInstance().showFieldFooter(mAddFieldFooter);
//                } else {
//                    mAddFieldFooter.setVisibility(View.VISIBLE);
//                }
                /*PRIZE-remove -yuandailin-2016-8-8-end*/
                return;
            }
        }
        if (animate) {
            EditorAnimator.getInstance().hideFieldFooter(mAddFieldFooter);
        } else {
            mAddFieldFooter.setVisibility(View.GONE);
        }
    }
    /*prize add-some-view huangliemin-2016-5-31 end*/

    /**
     * Updates the editors being displayed to the user removing extra empty
     * {@link Editor}s, so there is only max 1 empty {@link Editor} view at a time.
     */
    public void updateEmptyEditors(boolean shouldAnimate) {
    	Log.d(TAG, "[updateEmptyEditors]");
    	if(mIsSaveForSIMCard){
    		return;
    	}

        final List<View> emptyEditors = getEmptyEditors();

        // If there is more than 1 empty editor, then remove it from the list of editors.
        /** M:AAS max default value is 1 [COMMD_FOR_AAS]@ { */
        int max = 0;
        if (mKind != null) {
            max = GlobalEnv.getAasExtension().getMaxEmptyEditors(mState, mKind.mimeType);
        }
        Log.d(TAG, "[updateEmptyEditors] max =" + max + " emptyEditors.size()="
                + emptyEditors.size() + ", mEditors=" + mEditors.getChildCount());
        /** M: @ } */

        if (emptyEditors.size() > max) {
            for (final View emptyEditorView : emptyEditors) {
                // If no child {@link View}s are being focused on within this {@link View}, then
                // remove this empty editor. We can assume that at least one empty editor has focus.
                // The only way to get two empty editors is by deleting characters from a non-empty
                // editor, in which case this editor has focus.
                if (emptyEditorView.findFocus() == null) {
                    final Editor editor = (Editor) emptyEditorView;
                    if (shouldAnimate) {
                        editor.deleteEditor();
                    } else {
                        /// M: fix ALPS01127992
                        onChildViewRemoved(emptyEditorView);
                        mEditors.removeView(emptyEditorView);
                    }
                }
                ///M: Fix  ALPS02044867 we need to reserve max empty entry@{
                if (getEmptyEditors().size() <= max) {
                    break;
                }
                /// @}
            }
        } else if (mKind == null) {
            // There is nothing we can do.
            return;
        } else if (isReadOnly()) {
            // We don't show empty editors for read only data kinds.
            return;
        } else if (!RawContactModifier.canInsert(mState, mKind)) {
            // We have already reached the maximum number of editors. Lets not add any more.
            return;
        } else if (emptyEditors.size() == 1) {
            // We have already reached the maximum number of empty editors. Lets not add any more.
            return;
            /*prize delete huangliemin 2016-5-31 start*/
        } /*else if (mShowOneEmptyEditor) {
            final ValuesDelta values = RawContactModifier.insertChild(mState, mKind);
            final View newField = createEditorView(values);
            if (shouldAnimate) {
                newField.setVisibility(View.GONE);
                EditorAnimator.getInstance().showFieldFooter(newField);
            }
        }
        */
            /*prize delete huangliemin 2016-5-31 end*/
        
    }

    /** M:
     * Whether this section has any empty editors.
     */
    public boolean hasEmptyEditor() {
    	Log.d(TAG, "[hasEmptyEditor]");
        return !getEmptyEditors().isEmpty();
    }

    /**
     * Returns a list of empty editor views in this section.
     */
    private List<View> getEmptyEditors() {
    	Log.d(TAG, "[getEmptyEditors]");
        List<View> emptyEditorViews = new ArrayList<View>();
        for (int i = 0; i < mEditors.getChildCount(); i++) {
            View view = mEditors.getChildAt(i);
            if (((Editor) view).isEmpty()) {
                emptyEditorViews.add(view);
            }
        }
        return emptyEditorViews;
    }

    public boolean areAllEditorsEmpty() {
    	Log.d(TAG, "[areAllEditorsEmpty]");
        for (int i = 0; i < mEditors.getChildCount(); i++) {
            final View view = mEditors.getChildAt(i);
            if (!((Editor) view).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /*prize add-some-view huangliemin-2016-5-31 start*/
        /**
     * Extends superclass implementation to also run tasks
     * enqueued by {@link #runWhenWindowFocused}.
     */
    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        Log.d(TAG, "[onWindowFocusChanged]");
        if (hasWindowFocus) {
            for (Runnable r: mRunWhenWindowFocused) {
                r.run();
            }
            mRunWhenWindowFocused.clear();
        }
       
    }

    /**
     * Depending on whether we are in the currently-focused window, either run
     * the argument immediately, or stash it until our window becomes focused.
     */
    private void runWhenWindowFocused(Runnable r) {
    	Log.d(TAG, "[runWhenWindowFocused]");
        if (hasWindowFocus()) {
            r.run();
        } else {
            mRunWhenWindowFocused.add(r);
        }
    }

    /**
     * Simple wrapper around {@link #runWhenWindowFocused}
     * to ensure that it runs in the UI thread.
     */
    private void postWhenWindowFocused(final Runnable r) {
    	Log.d(TAG, "[postWhenWindowFocused]");
        post(new Runnable() {
            @Override
            public void run() {
                runWhenWindowFocused(r);
            }
        });
    }
 
    public void addItem() {
    	Log.d(TAG, "[addItem]");
        ValuesDelta values = null;
        // If this is a list, we can freely add. If not, only allow adding the first.
        if (mKind.typeOverallMax == 1) {
            if (getEditorCount() == 1) {
                return;
            }

            // If we already have an item, just make it visible
            ArrayList<ValuesDelta> entries = mState.getMimeEntries(mKind.mimeType);
            
            if (entries != null && entries.size() > 0) {
                values = entries.get(0);
            }
        }

        // Insert a new child, create its view and set its focus
        if (values == null) {
            values = RawContactModifier.insertChild(mState, mKind);
        }
       
        final View newField = createEditorView(values);

        /*prize-remove-hpf-2017-12-15-start*/
        /*if (newField instanceof Editor) {
            postWhenWindowFocused(new Runnable() {
                @Override
                public void run() {
                    newField.requestFocus();//PRIZE-remove-yuandailin-2016-8-10
                    ((Editor)newField).editNewlyAddedField();
                }
            });
        }*/
        /*prize-remove-hpf-2017-12-15-end*/

		/* prize-add for dido os 8.0-hpf-2017-7-19-start */
		if (newField instanceof LabeledEditorView) {
			upDateEditorAddOrDeleteBtn();
		}
		/* prize-add for dido os 8.0-hpf-2017-7-19-end */
        
        // Hide the "add field" footer because there is now a blank field.
        mAddFieldFooter.setVisibility(View.GONE);

        // Ensure we are visible
        updateSectionVisible();
    }
    /*prize add-some-view huangliemin-2016-5-31 end*/

    public int getEditorCount() {
    	Log.d(TAG, "[getEditorCount]");
        return mEditors.getChildCount();
    }

    public DataKind getKind() {
    	Log.d(TAG, "[getKind]");
        return mKind;
    }

    /**
     * M:fix ALPS01127992
     * remove the data from mState when the view will be removed
     * @param view The view to be removed
     */
    protected void onChildViewRemoved(View view) {
    	Log.d(TAG, "[onChildViewRemoved]");
        int viewIndex = mEditors.indexOfChild(view);
        if (mState.getMimeEntries(mKind.mimeType) != null) {
            Log.d(TAG, "[onViewRemoved] remove the data,viewIndex:" + viewIndex
                    + ",mimeType:" + mKind.mimeType);
            mState.getMimeEntries(mKind.mimeType).remove(viewIndex);
        }
    }
    
    /*prize-add-for dido os 8.0-hpf-2017-7-19-start*/
    private void upDateEditorAddOrDeleteBtn(){
    	final int viewCount = mEditors.getChildCount();
        for (int i = 0; i < viewCount;i++) {
        	final LabeledEditorView editor = (LabeledEditorView) mEditors.getChildAt(i);
        	if(i == viewCount -1){
        		editor.showAddFieldsBtn();
        	}else{
        		editor.showRemoveFieldsBtn();
        	}
        	if(i != 0 && viewCount > 1){
        		editor.setDividerVisibility(true);
        	}else{
        		editor.setDividerVisibility(false);
        	}
        }
    }
    
    public int getEmptyEditorCount(){
    	return getEmptyEditors().size();
    }
    /*prize-add-for dido os 8.0-hpf-2017-7-19-end*/
}
