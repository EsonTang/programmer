package com.mediatek.contacts.aas;

import android.app.ActionBar;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.contacts.R;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.TelephonyIntents;
import com.mediatek.contacts.aas.AlertDialogFragment.EditTextDialogFragment;
import com.mediatek.contacts.aas.AlertDialogFragment.EditTextDialogFragment.EditTextDoneListener;
import com.mediatek.contacts.aas.MessageAlertDialogFragment.DoneListener;
import com.mediatek.contacts.aassne.SimAasSneUtils;
import com.mediatek.contacts.util.Log;
import com.mediatek.internal.telephony.uicc.AlphaTag;
/*prize-add for dido os8.0-hpf-2017-8-23-start*/
import android.view.View.OnClickListener;
import android.widget.ImageButton;
/*prize-add for dido os8.0-hpf-2017-8-23-end*/

/*prize add for bug 55199 by zhaojian 20180504 start*/
import android.content.res.ColorStateList;
/*prize add for bug 55199 by zhaojian 20180504 end*/

public class AasTagActivity extends Activity {
    private static final String TAG = "AasTagActivity";

    private static final String CREATE_AAS_TAG_DIALOG = "create_aas_tag_dialog";
    private static final String EDIT_AAS_NAME = "edit_aas_name";
    private static final String DELETE_TAG_DIALOG = "delet_tag_dialog";
    private static final String EDIT_TAG_DIALOG = "edit_tag_dialog";

    private boolean isModifying = false;
    private AasTagInfoAdapter mAasAdapter = null;
    //private int mSlotId = -1;
    private int mSubId = -1;
    private AlphaTag mAlphaTag = null;
    private View mActionBarEdit = null;
    private TextView mSelectedView = null;
    private ToastHelper mToastHelper = null;
    /*prize-add for dido os8.0-hpf-2017-8-23-start*/
    private TextView mSelectAllText;
    private View mDeleteContainer;
    private View mSelectAllContainer;
    private View mDeleteView;
    /*prize-add for dido os8.0-hpf-2017-8-23-end*/


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "[onCreate]");
        setContentView(R.layout.custom_aas);
        
        /*prize-add for dido os8.0-hpf-2017-8-23-start*/
        mDeleteContainer = findViewById(R.id.prize_bottom_delete_layout);
        mDeleteView = findViewById(R.id.delete_contacts_layout);
        mDeleteView.setEnabled(false);
    	mDeleteView.setClickable(false);
        mDeleteView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (mAasAdapter.getCheckedItemCount() == 0) {
                    mToastHelper.showToast(R.string.multichoice_no_select_alert);
                } else {
                    showDeleteAlertDialog();
                }
			}
		});
        /*prize-add for dido os8.0-hpf-2017-8-23-end*/

        Intent intent = getIntent();
        if (intent != null) {
            mSubId = intent.getIntExtra(SimAasSneUtils.KEY_SUB_ID, -1);
        }

        if (mSubId == -1) {
            Log.e(TAG, "[onCreate] Eorror slotId=-1, finish the AasTagActivity");
            finish();
        }

        final ListView listView = (ListView) findViewById(R.id.custom_aas);
        mAasAdapter = new AasTagInfoAdapter(this, mSubId);

        /*prize add for bug 55199 by zhaojian 20180504 start*/
        initActionBar();
        /*prize add for bug 55199 by zhaojian 20180504 endt*/


        /*prize-add-fix bug[52008]-hpf-2018-3-5-start*/
        mAasAdapter.setOnDataChangeListener(new AasTagInfoAdapter.OnDataChangeListener() {
			
			@Override
			public void onChange(int size) {
				if(size > 0){
					listView.setVisibility(View.VISIBLE);
                    /*prize add for bug 55199 by zhaojian 20180504 start*/
                    mSelectAllText.setEnabled(true);
                    mSelectAllText.setTextColor(getResources().getColorStateList(R.drawable.prize_selector_actionbar_text_btn));
                    /*prize add for bug 55199 by zhaojian 20180504 end*/
				}else{
					listView.setVisibility(View.INVISIBLE);
                    /*prize add for bug 55199 by zhaojian 20180504 start*/
                    mSelectAllText.setEnabled(false);
                    mSelectAllText.setTextColor(getResources().getColor(R.color.prize_button_text_dark_color));
                    mSelectAllText.setText(R.string.prize_select_all_contacts);
                    /*prize add for bug 55199 by zhaojian 20180504 end*/
				}
			}
		});
        /*prize-add-fix bug[52008]-hpf-2018-3-5-end*/
        mAasAdapter.updateAlphaTags();
        listView.setAdapter(mAasAdapter);
        mToastHelper = new ToastHelper(this);
        listView.setOnItemClickListener(new ListItemClickListener());
        /*prize delete for bug 55199 by zhaojian 20180504 start*/
        //initActionBar();
        /*prize delete for bug 55199 by zhaojian 20180504 end*/
        registerReceiver(mPhbStateListener, new IntentFilter(
                           TelephonyIntents.ACTION_PHB_STATE_CHANGED));
        
    }

    public void initActionBar() {
        ActionBar actionBar = getActionBar();
        LayoutInflater inflate = getLayoutInflater();
        View customView = inflate.inflate(R.layout.prize_custom_aas_action_bar, null);//prize-change for dido os8.0-hpf-2017-8-23
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM, ActionBar.DISPLAY_SHOW_CUSTOM
                | ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_HOME_AS_UP);

        mActionBarEdit = customView.findViewById(R.id.action_bar_edit);
        mSelectedView = (TextView) customView.findViewById(R.id.selected);
        View cancelContainer = customView.findViewById(R.id.cancel_container);//prize-change for dido os8.0-hpf-2017-8-23
        cancelContainer.setOnClickListener(new View.OnClickListener() {//prize-change for dido os8.0-hpf-2017-8-23
            @Override
            public void onClick(View v) {
                setMode(AasTagInfoAdapter.MODE_NORMAL);
                updateActionBar();
            }
        });
        
        /*prize-add for dido os8.0-hpf-2017-8-23-start*/
        mSelectAllContainer = customView.findViewById(R.id.select_all_container);
        mSelectAllText = (TextView)customView.findViewById(R.id.select_all);
        mSelectAllContainer.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
                if(mAasAdapter.isAllChecked()){
                	mAasAdapter.setAllChecked(false);
                    updateActionBar();
                    mSelectAllText.setText(R.string.prize_select_all_contacts);
                }else{
                	mAasAdapter.setAllChecked(true);
                    updateActionBar();
                    mSelectAllText.setText(R.string.prize_unselect_all);
                }
                if(mAasAdapter.isHaveChecked()){
                	mDeleteView.setEnabled(true);
                	mDeleteView.setClickable(true);
                }else{
                	mDeleteView.setEnabled(false);
                	mDeleteView.setClickable(false);
                }
			}
		});
        /*prize-add for dido os8.0-hpf-2017-8-23-end*/
        actionBar.setCustomView(customView);

        updateActionBar();
        
    }

    public void updateActionBar() {
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            if (mAasAdapter.isMode(AasTagInfoAdapter.MODE_NORMAL)) {
                actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP,
                        ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_HOME_AS_UP);

                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setDisplayShowTitleEnabled(true);
                actionBar.setTitle(R.string.aas_custom_title);
                mActionBarEdit.setVisibility(View.GONE);
                mDeleteContainer.setVisibility(View.GONE);//prize-add for dido os8.0-hpf-2017-8-23
            } else {
                actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM,
                        ActionBar.DISPLAY_SHOW_CUSTOM);

                actionBar.setDisplayHomeAsUpEnabled(false);
                actionBar.setDisplayShowTitleEnabled(false);
                mActionBarEdit.setVisibility(View.VISIBLE);
                mDeleteContainer.setVisibility(View.VISIBLE);//prize-add for dido os8.0-hpf-2017-8-23
                String select = getResources().getString(R.string.selected_item_count,
                        mAasAdapter.getCheckedItemCount());
                mSelectedView.setText(select);
            }
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        Log.d(TAG, "[onPrepareOptionsMenu]");
        MenuInflater inflater = getMenuInflater();
        menu.clear();
        if (mAasAdapter.isMode(AasTagInfoAdapter.MODE_NORMAL)) {
            inflater.inflate(R.menu.prize_custom_normal_menu, menu);
        } else {
            //inflater.inflate(R.menu.custom_edit_menu, menu);//prize-remove for dido os8.0-hpf-2017-8-23
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        Log.d(TAG, "[onOptionsItemSelected]");
        if (mAasAdapter.isMode(AasTagInfoAdapter.MODE_NORMAL)) {
            switch (item.getItemId()) {
            case R.id.menu_add_new:
                if (!mAasAdapter.isFull()) {
                    showNewAasDialog();
                } else {
                    mToastHelper.showToast(R.string.aas_usim_full);
                }
                break;
            case R.id.menu_deletion:
                setMode(AasTagInfoAdapter.MODE_EDIT);
                break;
            case android.R.id.home:
                finish();
                break;
            default:
            }
        } else {
            switch (item.getItemId()) {
            case R.id.menu_select_all:
                mAasAdapter.setAllChecked(true);
                updateActionBar();
                break;
            case R.id.menu_disselect_all:
                mAasAdapter.setAllChecked(false);
                updateActionBar();
                break;
            case R.id.menu_delete:
                // mAasAdapter.deleteCheckedAasTag();
                if (mAasAdapter.getCheckedItemCount() == 0) {
                    mToastHelper.showToast(R.string.multichoice_no_select_alert);
                } else {
                    showDeleteAlertDialog();
                }
                break;
            default:
                break;
            }
        }
        return true;
    }

    public void setMode(int mode) {
        mAasAdapter.setMode(mode);
        updateActionBar();
        invalidateOptionsMenu();
    }

    @Override
    public void onBackPressed() {
        if (mAasAdapter.isMode(AasTagInfoAdapter.MODE_EDIT)) {
            setMode(AasTagInfoAdapter.MODE_NORMAL);
        } else {
            super.onBackPressed();
        }
    }

    protected void showNewAasDialog() {
        EditTextDialogFragment createItemDialogFragment = EditTextDialogFragment.newInstance(
                R.string.aas_new_dialog_title, android.R.string.cancel, android.R.string.ok, "");
        createItemDialogFragment.setOnEditTextDoneListener(new NewAlpahTagListener());
        createItemDialogFragment.show(getFragmentManager(), CREATE_AAS_TAG_DIALOG);
    }

    final private class NewAlpahTagListener implements EditTextDoneListener {

        @Override
        public void onClick(String text) {
            if (mAasAdapter.isExist(text)) {
                mToastHelper.showToast(R.string.aas_name_exist);
            } else if (!SimAasSneUtils.isAasTextValid(text, mSubId)) {
                mToastHelper.showToast(R.string.aas_name_invalid);
            } else {
                int aasIndex = SimAasSneUtils.insertUSIMAAS(mSubId, text);
                Log.d(TAG, "[onClick] NewAlpahTagListener insertAasTag() aasIndex = " + aasIndex);
                if (aasIndex > 0) {
                    mAasAdapter.updateAlphaTags();
                } else {
                    mToastHelper.showToast(R.string.aas_new_fail);
                }
            }
        }
    }

    protected void showEditAasDialog(AlphaTag alphaTag) {
        if (alphaTag == null) {
            Log.e(TAG, "[showEditAasDialog] alphaTag is null,");
            return;
        }
        final String text = alphaTag.getAlphaTag();
        EditTextDialogFragment editDialogFragment = EditTextDialogFragment.newInstance(
                R.string.ass_rename_dialog_title, android.R.string.cancel, android.R.string.ok,
                text);
        editDialogFragment.setOnEditTextDoneListener(new EditAlpahTagListener(alphaTag));
        editDialogFragment.show(getFragmentManager(), EDIT_AAS_NAME);
    }

    final private class EditAlpahTagListener implements EditTextDoneListener {
        private AlphaTag mAlphaTag;

        public EditAlpahTagListener(AlphaTag alphaTag) {
            mAlphaTag = alphaTag;
        }

        @Override
        public void onClick(String text) {
            if (mAlphaTag.getAlphaTag().equals(text)) {
                Log.d(TAG, "[onClick] mAlphaTag.getAlphaTag()==text : " + text);
                return;
            }
            if (mAasAdapter.isExist(text)) {
                mToastHelper.showToast(R.string.aas_name_exist);
            } else if (!SimAasSneUtils.isAasTextValid(text, mSubId)) {
                mToastHelper.showToast(R.string.aas_name_invalid);
            } else {
                showEditAssertDialog(mAlphaTag, text);
            }
        }
    }

    private void showEditAssertDialog(AlphaTag alphaTag, String targetName) {
        MessageAlertDialogFragment editAssertDialogFragment = MessageAlertDialogFragment
                .newInstance(android.R.string.dialog_alert_title, R.string.ass_edit_assert_message,
                        true, targetName);
        editAssertDialogFragment.setDeleteDoneListener(new EditAssertListener(alphaTag));
        editAssertDialogFragment.show(getFragmentManager(), EDIT_TAG_DIALOG);
    }

    final private class EditAssertListener implements DoneListener {
        private AlphaTag mAlphaTag = null;

        public EditAssertListener(AlphaTag alphaTag) {
            mAlphaTag = alphaTag;
        }

        @Override
        public void onClick(String text) {
            boolean flag = SimAasSneUtils.updateUSIMAAS(mSubId, mAlphaTag.getRecordIndex(),
                    mAlphaTag.getPbrIndex(), text);
            if (flag) {
                mAasAdapter.updateAlphaTags();
            } else {
                String msg = getResources().getString(R.string.aas_edit_fail,
                        mAlphaTag.getAlphaTag());
                mToastHelper.showToast(msg);
            }
        }
    }

    protected void showDeleteAlertDialog() {
        MessageAlertDialogFragment deleteDialogFragment = MessageAlertDialogFragment.newInstance(
                android.R.string.dialog_alert_title, R.string.aas_delele_dialog_message, true, "");
        deleteDialogFragment.setDeleteDoneListener(new DeletionListener());
        deleteDialogFragment.show(getFragmentManager(), DELETE_TAG_DIALOG);
    }

    final private class DeletionListener implements DoneListener {
        @Override
        public void onClick(String text) {
            Log.d(TAG, "[onClick] DeletionListener");
            mAasAdapter.deleteCheckedAasTag();
            setMode(AasTagInfoAdapter.MODE_NORMAL);
        }
    }

    public class ListItemClickListener implements OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> view, View v, int pos, long arg3) {
            if (mAasAdapter.isMode(AasTagInfoAdapter.MODE_NORMAL)) {
                showEditAasDialog(mAasAdapter.getItem(pos).mAlphaTag);
            } else {
                mAasAdapter.updateChecked(pos);
                invalidateOptionsMenu();
                updateActionBar();
                /*prize-add-for dido os8.0-hpf-2017-8-23-start*/
                if(mAasAdapter.isAllChecked()){
                	mSelectAllText.setText(R.string.prize_unselect_all);
                }else{
                	mSelectAllText.setText(R.string.prize_select_all_contacts);
                }
                if(mAasAdapter.isHaveChecked()){
                	mDeleteView.setEnabled(true);
                	mDeleteView.setClickable(true);
                }else{
                	mDeleteView.setEnabled(false);
                	mDeleteView.setClickable(false);
                }
                /*prize-add-for dido os8.0-hpf-2017-8-23-end*/
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mPhbStateListener);
    }

    private BroadcastReceiver mPhbStateListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            int subId = intent.getIntExtra(PhoneConstants.SUBSCRIPTION_KEY, -1);
            boolean phbReady = intent.getBooleanExtra("ready", true);
            Log.d(TAG, "[onReceive] mPhbStateListener subId:" + subId + ",phbReady:" + phbReady);
            // for phb state change
            if (subId == mSubId && !phbReady) {
                Log.d(TAG, "[onReceive] subId: " + subId);
                finish();
            }
        }
    };

    /**
     * Add for ALPS02613851, do not destroy when configuration change.
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
}
