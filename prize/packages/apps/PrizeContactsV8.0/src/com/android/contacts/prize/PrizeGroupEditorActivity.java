/**
 * PrizeGroupEditorActivity.java [V 1.0.0]
 * classes : com.android.contacts.prize.PrizeGroupEditActivity
 * huangliemin Create at 2016-7-14 5:55:05
 */
package com.android.contacts.prize;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.provider.ContactsContract.Groups;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.contacts.GroupListLoader;
import com.android.contacts.R;
/*prize-add-hpf-2018-1-5-start*/
import android.os.Handler;
import android.view.inputmethod.InputMethodManager;
/*prize-add-hpf-2018-1-5-end*/

/**
 * com.android.contacts.prize.PrizeGroupEditorActivity
 * @author huangliemin <br/>
 * create at 2016-7-14 5:55:05
 */
public class PrizeGroupEditorActivity extends Activity implements View.OnClickListener{
	
	private String TAG = "PrizeGroupEditorActivity";
	private ListView mListView;
	private GroupListAdapter mAdapter;
	private static final int LOADER_GROUPS = -3;
	private static HashMap<Integer, Boolean> CheckedMap = new HashMap<Integer, Boolean>();
	private ImageButton RenameButton;
	private TextView RenameText;
	private ImageButton DeleteButton;
	private TextView DeleteText;
	private ContentResolver mResolver;
	private EditText mDialogEdit;
	private LinearLayout PrizeRenameBtnLayout;//prize add view huangpengfei 2016-8-13
	private LinearLayout PrizeDeleteLayout;//prize add view huangpengfei 2016-8-13
	private InputMethodManager imm;//prize add hpf 2018-1-4
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle, android.os.PersistableBundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.prize_group_edit_layout_huangliemin_2016_7_15);
		
		ActionBar actionBar = getActionBar();
		if(actionBar != null) {
			actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        	actionBar.setDisplayShowCustomEnabled(false);
        	actionBar.setDisplayHomeAsUpEnabled(true);
        	actionBar.setDisplayShowTitleEnabled(true);
        	actionBar.setDisplayUseLogoEnabled(false);
        	actionBar.setTitle(R.string.prize_group_manager_title); 
		}
		
		mListView = (ListView)findViewById(R.id.list);
		RenameButton = (ImageButton)findViewById(R.id.rename_btn);
		PrizeRenameBtnLayout = (LinearLayout)findViewById(R.id.prize_rename_btn_layout);
		PrizeDeleteLayout = (LinearLayout)findViewById(R.id.prize_delete_layout);
		DeleteButton = (ImageButton)findViewById(R.id.delete_btn);
		RenameText = (TextView)findViewById(R.id.rename_text);
		DeleteText = (TextView)findViewById(R.id.delete_text);
		/*prize change huangpengfei 2016-8-13 start*/
		//RenameText.setOnClickListener(this);
		//DeleteText.setOnClickListener(this);
		//RenameButton.setOnClickListener(this);
		//DeleteButton.setOnClickListener(this);
		PrizeRenameBtnLayout.setOnClickListener(this);
		PrizeDeleteLayout.setOnClickListener(this);
		/*prize change huangpengfei 2016-8-13 end*/
		
		mAdapter = new GroupListAdapter(this);
		mListView.setAdapter(mAdapter);
		CheckedMap.clear();
		mResolver = getContentResolver();
		getLoaderManager().initLoader(LOADER_GROUPS, null, mGroupLCallbacks);
		imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);//prize add hpf 2018-1-4
		
	}
	
	/*prize-add for dido os8.0-hpf-2017-8-2-start*/
	@Override  
    public boolean onOptionsItemSelected(MenuItem item) {  
        switch (item.getItemId()) {  
            case android.R.id.home:  
                onBackPressed();  
            break;  
              
            default:  
                  
            break;  
        }  
        return super.onOptionsItemSelected(item);  
    }  
	/*prize-add for dido os8.0-hpf-2017-8-2-end*/ 
	
	private final LoaderCallbacks<Cursor> mGroupLCallbacks = new LoaderCallbacks<Cursor>() {

		@Override
		public Loader<Cursor> onCreateLoader(int id, Bundle args) {
			Log.i("logtest", "groupListLoader");
			return new GroupListLoader(getBaseContext());
		}

		@Override
		public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
			Log.i("logtest", "groupListLoader2: "+(mAdapter==null) +" : "+(data==null));
			if(mAdapter!=null && data!=null) {
				mAdapter.setCursor(data);
				// prize add by zhaojian for bug 52843 20180315 start
				if(mAdapter.getCount() == 0){
					mListView.setBackgroundColor(PrizeGroupEditorActivity.this.getResources().getColor(R.color.prize_layout_bg_color));
				}
				// prize add by zhaojian for bug 52843 20180315 end
			}
		}

		@Override
		public void onLoaderReset(Loader<Cursor> loader) {
			
		}
	};
	
	
	class GroupListAdapter extends BaseAdapter {
		
		private Context mContext;
		private Cursor mCursor;
		
		public GroupListAdapter(Context context) {
			mContext = context;
		}
		
		public void setCursor(Cursor cursor) {
			mCursor = cursor;
			notifyDataSetChanged();
		}

		/* (non-Javadoc)
		 * @see android.widget.Adapter#getCount()
		 */
		@Override
		public int getCount() {
			if(mCursor!=null) {
				return mCursor.getCount();
			} else {
				return 0;
			}
		}

		/* (non-Javadoc)
		 * @see android.widget.Adapter#getItem(int)
		 */
		@Override
		public Object getItem(int position) {
			return null;
		}

		/* (non-Javadoc)
		 * @see android.widget.Adapter#getItemId(int)
		 */
		@Override
		public long getItemId(int position) {
			return position;
		}

		/* (non-Javadoc)
		 * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
		 */
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder mViewHolder;
			if(convertView == null) {
				mViewHolder = new ViewHolder();
				convertView = LayoutInflater.from(mContext).inflate(R.layout.prize_group_check_item_view_huangliemin_2016_7_15, null);
				mViewHolder.mCheckedTextView = (CheckedTextView)convertView.findViewById(R.id.group_check_item);
				convertView.setTag(mViewHolder);
				
			} else {
				mViewHolder = (ViewHolder)convertView.getTag();
			}
			
			if(mCursor.moveToPosition(position)) {
				mViewHolder.mCheckedTextView.setText(mCursor.getString(GroupListLoader.TITLE));
				mViewHolder.mCheckedTextView.setOnClickListener(new CheckTextClickListener((int)(mCursor.getLong(GroupListLoader.GROUP_ID))));
				//prize-add-huangliemin-2016-7-29-start
				if(CheckedMap.get((int)(mCursor.getLong(GroupListLoader.GROUP_ID)))!=null) {
					mViewHolder.mCheckedTextView.setChecked(CheckedMap.get((int)(mCursor.getLong(GroupListLoader.GROUP_ID))));
				} else {
					mViewHolder.mCheckedTextView.setChecked(false);
				}
				//prize-add-huangliemin-2016-7-29-end
				
			}
			return convertView;
		}
		
		class ViewHolder {
			public CheckedTextView mCheckedTextView;
		}
		
		class CheckTextClickListener implements View.OnClickListener {
			private int mPosition;

			public CheckTextClickListener (int position) {
				mPosition = position;
			}
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				CheckedTextView mCheckText = (CheckedTextView)v;
				mCheckText.toggle();
				Log.i("logtest", "onClick: "+mPosition+" : "+mCheckText.isChecked());
				CheckedMap.put(mPosition, mCheckText.isChecked());
				Iterator<Integer> list = CheckedMap.keySet().iterator();
				int count = 0;
				while(list.hasNext()) {
					if(CheckedMap.get(list.next())) {
						count ++;
					}
				}
				/*prize change huangpengfei 2016-8-13 start*/
				
				if(count == 1) {
					setRenameButtonState(true);
					
					
				} else {
					setRenameButtonState(false);
					
				}
				
				if(count == 0){
					setDeleteButtonState(false);
					
				}else{
					
					setDeleteButtonState(true);
				}
				/*prize change huangpengfei 2016-8-13 end*/
				
			}
			
		}
		
	}
	/*prize add huangpengfei 2016-11-14 start*/
	public void setRenameButtonState(boolean clickable){
		if(clickable){
			RenameButton.setImageResource(R.drawable.prize_rename_selector);
			RenameText.setTextColor(getResources().getColorStateList(R.drawable.prize_selector_text_color_btn));
			PrizeRenameBtnLayout.setClickable(true);
		}else{
			RenameButton.setImageResource(R.drawable.prize_rename_dark);
			RenameText.setTextColor(getResources().getColor(R.color.prize_button_text_dark_color));
			PrizeRenameBtnLayout.setClickable(false);
		}
		
	}
	
	public void setDeleteButtonState(boolean clickable){
		if(clickable){
			DeleteButton.setImageResource(R.drawable.prize_callrecords_delete);
			DeleteText.setTextColor(getResources().getColorStateList(R.drawable.prize_selector_text_color_btn));
			PrizeDeleteLayout.setClickable(true);
		}else{
			DeleteButton.setImageResource(R.drawable.callrecords_delete_dark);
			DeleteText.setTextColor(getResources().getColor(R.color.prize_button_text_dark_color));
			PrizeDeleteLayout.setClickable(false);
		}
	}
	/*prize add huangpengfei 2016-11-14 end*/

	/* (non-Javadoc)
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {
		switch(v.getId()) {
			case R.id.prize_rename_btn_layout:
				if(CheckedMap.size()<1) return;
				Dialog mDialog = createRenameGroupDialog();
				if(mDialog!=null) {
					mDialog.show();
					showInputMethod();
				}
				break;
			case R.id.prize_delete_layout:
				DeleteGroup();
				break;
		}
	}
	
	private void RenameGroup(String groupname) {
		if(CheckedMap.isEmpty()) {
			return;
		}
		
		Iterator<Integer> list = CheckedMap.keySet().iterator();
		while(list.hasNext()) {
			int position = list.next();
			if(CheckedMap.get(position)) {
				//CheckedMap.remove(position);
				boolean isnotlike = true; 
				ContentValues mValues = new ContentValues();
				ContentResolver resolver = this.getContentResolver();
				Cursor groupCursor = resolver.query(Groups.CONTENT_URI, new String[] {Groups.TITLE, Groups.DELETED}, null, null, null);
				if(groupCursor != null && groupCursor.moveToFirst()){
					do {
						String title =groupCursor.getString(groupCursor.getColumnIndex(Groups.TITLE));
						int deleted =groupCursor.getInt(groupCursor.getColumnIndex(Groups.DELETED));
						Log.e(TAG, "getColumnname = " + title);
						if (title.equals(groupname) && deleted == 0) {
							Toast.makeText(this, R.string.prize_repeat_group_name_toast, Toast.LENGTH_LONG).show();
							isnotlike = false;
							break;
						}
					}while(groupCursor.moveToNext());
				}
				if (isnotlike) {
					mValues.put(Groups.TITLE, groupname);
					mResolver.update(Groups.CONTENT_URI, mValues, Groups._ID+"="+position, null);
				}
				Log.e(TAG, "isnotlike = " + isnotlike);
			}
		}
		CheckedMap.clear();
		mAdapter.notifyDataSetChanged();
		/*prize-add-huangpengfei-2016-11-30-start*/
		setDeleteButtonState(false);
		setRenameButtonState(false);
		/*prize-add-huangpengfei-2016-11-30-end*/
		
	}
	
	private void DeleteGroup() {
		if(CheckedMap.isEmpty()) {
			return;
		}
		
		Iterator<Integer> list = CheckedMap.keySet().iterator();
		while(list.hasNext()) {
			int position = list.next();
			if(CheckedMap.get(position)) {
				//CheckedMap.remove(position);
				mResolver.delete(Groups.CONTENT_URI, Groups._ID+"="+position, null);
			}
		}
		CheckedMap.clear();
		mAdapter.notifyDataSetChanged();
		
		/*prize-add-huangpengfei-2016-11-14-start*/
		setRenameButtonState(false);
		setDeleteButtonState(false);
		/*prize-add-huangpengfei-2016-11-14-end*/
		
		
	}
	
	
	public Dialog createRenameGroupDialog() {
		AlertDialog.Builder mBuilder = new AlertDialog.Builder(this);
		mBuilder.setTitle(R.string.prize_rename_group_dialog_title);
		mBuilder.setPositiveButton(android.R.string.ok, new GroupEditDialogListener(this));
		mBuilder.setNegativeButton(android.R.string.cancel, null);
		View mDialogContent = LayoutInflater.from(this).inflate(R.layout.prize_group_dialog_content, null);
		mDialogEdit = (EditText)mDialogContent.findViewById(R.id.group_dialog_edit);
		mDialogEdit.setHint(R.string.prize_rename_group_hint_text);
		mBuilder.setView(mDialogContent);
		return mBuilder.create();
	}
	
	class GroupEditDialogListener implements DialogInterface.OnClickListener {
		private Context mDialogContext;
		
		public GroupEditDialogListener(Context context) {
			mDialogContext = context;
		}

		@Override
		public void onClick(DialogInterface dialog, int which) {
			if(mDialogEdit!=null) {
				String groupname = mDialogEdit.getText().toString().trim();
				if(!TextUtils.isEmpty(groupname)) {
					RenameGroup(groupname);
				}
			}
		}
	}
	
	/*prize add hpf 2018-1-4-start*/
	private void showInputMethod(){
		if (imm != null && mDialogEdit != null) {
			mDialogEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
				
				@Override
				public void onFocusChange(View v, boolean hasFocus) {
					if(hasFocus){
						new Handler().postDelayed(new Runnable() {
							
							@Override
							public void run() {
								if(imm.showSoftInput(v, 0)){
					            	Log.d(TAG, "[onFocusChange] showSoftInput true");
					            }else{
					            	Log.d(TAG, "[onFocusChange] showSoftInput false");
					            }
							}
						}, 200);
					}
				}
			});
			mDialogEdit.setFocusable(true);
			mDialogEdit.requestFocus();
		}
	}
	/*prize add hpf 2018-1-4-end*/

}
