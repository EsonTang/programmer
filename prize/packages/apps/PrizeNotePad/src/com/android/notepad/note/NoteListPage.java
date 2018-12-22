
/*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：列表界面外布局控件
 *当前版本：V1.0
 *作	者：朱道鹏
 *完成日期：2015-04-17
 *修改记录：
 *修改日期：
 *版 本 号：
 *修 改 人：
 *修改内容：
 ...
 *修改记录：
 *修改日期：
 *版 本 号：
 *修 改 人：
 *修改内容：
 *********************************************/

package com.android.notepad.note;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.android.notepad.note.NotePadAdapter.ViewHolder;
import com.android.notepad.note.database.NotePadDataBaseDao;
import com.android.notepad.note.database.NotePadDataBaseDaoImpl;
import com.android.notepad.note.model.NoteEvent;
import com.android.notepad.note.util.FileUtil;
import com.android.notepad.note.view.NotePadListView;
import com.android.notepad.NotePadActivity;
import com.android.notepad.R;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.TextView;
import android.text.TextUtils;
import android.view.Gravity;

/**
 **
 * 类描述：列表界面外布局控件
 * @author 朱道鹏
 * @version V1.0
 */
public class NoteListPage extends FrameLayout implements OnItemClickListener,
OnItemLongClickListener, OnClickListener {

	public static final int GO_TO_NotePadEditActivity = 1;
	private static final int DO_SOMETHING_FOR_DELETE_OR_BACKUP = 12;
	public static final String BROAD_CAST_ACTION = "AdapterDateChangeBroadcastReceiver";
	public static boolean isNewCreate = false;
	private static boolean mIsNoNeedLoadData;
	private boolean isBackupOrDelect = false;
	private boolean isMainListOrDelectList = true;

	private ArrayList<Integer> db_rowidList = new ArrayList<Integer>();
	private List<Integer> checkList = new ArrayList<Integer>();

	private NotePadActivity mContext ;
	private NotePadAdapter adapter;
	private NotePadListView listView;
	private NotePadDataBaseDao mNotePadDataBaseDao;
	private long mCurrentTime;

	private RelativeLayout mBottomRelativeLayout;
	private TextView deleteButton;
	private TextView selectButton;
	private TextView cancelSelectButton;
	
	private ImageView searchDelete;
	private boolean deleteBySearch = false;

	private Timer timer = new Timer();

	/**
	 * 列表界面删除操作时，动态刷新界面
	 */
	@SuppressLint("HandlerLeak")
	public Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case DO_SOMETHING_FOR_DELETE_OR_BACKUP:
				clearWhenExitMultiDeleteListView();
				initDate();

				if (isBackupOrDelect) {
					setNoNeedLoadData(true);
				} else { 
					pageOnWindowFocusChanged();
				}
				isBackupOrDelect = false;
				break;
			}
		}
	};

	/**
	 * 搜索时文本监听，文本变化时动态更新Adapter数据
	 */
	private TextWatcher editWatcher = new TextWatcher() {
		public void afterTextChanged(Editable s) {
			initAdapterDate();
		}

		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
		}

		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			if(null != s){
				String content = s.toString();
				if (TextUtils.isEmpty(content)){
					searchDelete.setVisibility(View.GONE);
				} else {
					searchDelete.setVisibility(View.VISIBLE);
				}
			}
		}
	};

	/**
	 * 搜索输入框焦点监听
	 */
	private OnFocusChangeListener focusChangeListener = new OnFocusChangeListener(){
		private InputMethodManager inputManager;
		@Override
		public void onFocusChange(View view, boolean flag) {
			if(flag){
				mSearchImageView.setVisibility(View.GONE);
				if (TextUtils.isEmpty(mSearchEditText.getText())) {
                    mSearchEditText.setHint(mContext.getString(R.string.quick_search_keywords));
				}else{
					mSearchEditText.setHint("");
				}
				mSearchEditText.setGravity(Gravity.LEFT);
				timer.schedule(new TimerTask(){
					public void run(){
						if(null == inputManager){
							inputManager = (InputMethodManager)mSearchEditText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);	
						}
						inputManager.showSoftInput(mSearchEditText, 0);
					}
				},400);
			}else{
				if (TextUtils.isEmpty(mSearchEditText.getText())) {
                    mSearchEditText.setGravity(Gravity.CENTER);
				}else{
					mSearchEditText.setGravity(Gravity.LEFT);
				}
				if(null == inputManager){
					inputManager = (InputMethodManager)mSearchEditText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);	
				}
				inputManager.hideSoftInputFromWindow(mSearchEditText.getWindowToken(), 0);

				mSearchImageView.setVisibility(View.VISIBLE);
				mSearchEditText.getText().clear();
				mSearchEditText.clearFocus();
				mSearchEditText.setHint(mContext.getString(R.string.quick_search_keywords));
			}
		}
	};

	/**
	 * 查询数据库得到所有的NoteEvent事件集合
	 */
	private List<NoteEvent> mNoteEventList;
	public EditText mSearchEditText;
	private ImageView mSearchImageView;
	private ArrayList<String> mUuidList;
	private AdapterDateChangeBroadcastReceiver mReceiver;
	private RelativeLayout mSearchRl;

	public NoteListPage(NotePadActivity context) {
		super(context);
		mContext = context;
	}

	/**
	 * 方法描述：建立列表界面
	 * @param void
	 * @return void
	 * @see NoteListPage#pageCreate
	 */
	public void pageCreate() {
		initLayout();
	}

	/**
	 * 方法描述：Resume列表界面
	 * @param void
	 * @return void
	 * @see NoteListPage#pageResume
	 */
	public void pageResume() {
		if(isMainListOrDelectList == false){
		  setNoNeedLoadData(true) ;
		  initForDelectListView(isBackupOrDelect);
		}else{ 
			if (View.VISIBLE == mBottomRelativeLayout.getVisibility()) {
				clearWhenExitMultiDeleteListView();
			}
		}
	}

	/**
	 * 方法描述：NoteListPage界面焦点切换时，界面更新
	 * @param void
	 * @return void
	 * @see NoteListPage#pageOnWindowFocusChanged
	 */
	public void pageOnWindowFocusChanged() {
		if (mIsNoNeedLoadData) {
			setNoNeedLoadData(false);
			return;
		}
		mNotePadDataBaseDao = new NotePadDataBaseDaoImpl(mContext );
		mNoteEventList = mNotePadDataBaseDao.queryForAll();
		adapter.setNoteEventList(mNoteEventList);
		adapter.notifyDataSetChanged();
	}

	/**
	 * 方法描述：列表界面初始化
	 * @param void
	 * @return void
	 * @see NoteListPage#initLayout
	 */
	private void initLayout() {
		LayoutInflater inflater = (LayoutInflater) mContext 
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.layout_note_list, this);

		listView = (NotePadListView) findViewById(R.id.id_notepads_listview);
		//		listView.setOverScrollMode(View.OVER_SCROLL_NEVER);
		listView.setOnItemClickListener(this); 
		listView.setOnItemLongClickListener(this); 

		deleteButton = (TextView) findViewById(R.id.delect_button);
		deleteButton.setOnClickListener(this);

		mSearchRl = (RelativeLayout)  findViewById(R.id.search);
		mSearchRl.setOnClickListener(this);
		mSearchImageView = (ImageView) findViewById(R.id.search_image_view);
		mSearchImageView.clearFocus();
		mSearchEditText = (EditText) findViewById(R.id.search_edit_text);
		mSearchEditText.setHint(mContext.getString(R.string.quick_search_keywords));

		mSearchEditText.addTextChangedListener(editWatcher);
		mSearchEditText.setOnFocusChangeListener(focusChangeListener);
		
		searchDelete = (ImageView) findViewById(R.id.search_delete);
		searchDelete.setVisibility(View.GONE);
		searchDelete.setOnClickListener(this);

		selectButton = (TextView) findViewById(R.id.select_all_button);
		selectButton.setOnClickListener(this);

		cancelSelectButton = (TextView) findViewById(R.id.cancel_select_all_button);
		cancelSelectButton.setOnClickListener(this);


			
		mBottomRelativeLayout = (RelativeLayout) findViewById(R.id.myBottomButton);

		mNotePadDataBaseDao = new NotePadDataBaseDaoImpl(mContext);
		mNoteEventList = mNotePadDataBaseDao.queryForAll();
		adapter = new NotePadAdapter(mContext,mNoteEventList,NotePadListView.MIN_HEIGHT);
		listView.setAdapter(adapter);
		mReceiver = new AdapterDateChangeBroadcastReceiver();
		IntentFilter filter = new IntentFilter(BROAD_CAST_ACTION);
		mContext.registerReceiver(mReceiver, filter);
	
	}

	/**
	 * 方法描述：跳转至文件便签编辑界面
	 * @param void
	 * @return void
	 * @see NoteListPage#setNewNotePadData
	 */
	protected void setNewNotePadData() {
		isNewCreate = false;
		getNotePadCreateTime();
		Intent intent = new Intent();
		intent.putExtra("notepads_create_time", mCurrentTime);
		//跳转至新文本便签
		intent.setClass(mContext, NotePadEditActivity.class);
		mContext.startActivityForResult(intent, GO_TO_NotePadEditActivity);
	}

	private void getNotePadCreateTime() {
		mCurrentTime = System.currentTimeMillis();
	}

	/**
	 * 方法描述：Item长按事件界面隐藏，清楚数据
	 * @param 参数名 说明
	 * @return 返回类型 说明
	 * @see NoteListPage#clearWhenExitMultiDeleteListView
	 */
	private void clearWhenExitMultiDeleteListView() {
		mBottomRelativeLayout.setVisibility(View.GONE);
		NotePadActivity.mNewNoteButtonRl.setVisibility(View.VISIBLE);
		adapter.isVisibleFlag = false;
		adapter.notifyDataSetChanged();
		isMainListOrDelectList = true;
		if (db_rowidList != null) {
			db_rowidList.clear();
			checkList.clear();
		}
	}

	/**
	 * 方法描述：显示长按操作界面
	 * @param boolean   是否为备份或观察
	 * @return void
	 * @see NoteListPage#initForDelectListView
	 */
	private void initForDelectListView(boolean isBackupOrDelect) {
		if (isBackupOrDelect) {
			deleteButton.setText(getResources().getString(R.string.backup_action));
		} else {
			deleteButton.setText(getResources().getString(R.string.delect_action));
		}
		isMainListOrDelectList = false;
		NotePadActivity.mNewNoteButtonRl.setVisibility(View.GONE);
		mBottomRelativeLayout.setVisibility(View.VISIBLE);
		
		if(checkList.size() == adapter.getCount()){				
			cancelSelectButton.setVisibility(View.VISIBLE);
			selectButton.setVisibility(View.GONE);
		}else{				
			cancelSelectButton.setVisibility(View.GONE);
			selectButton.setVisibility(View.VISIBLE);
		}
		adapter.isVisibleFlag = true;
		adapter.notifyDataSetChanged();
	}

	/**
	 * 方法描述：初始化Adapter中isSelect的Values为false
	 * @param void
	 * @return void
	 * @see NoteListPage#initDate
	 */
	private void initDate() {
		for (int i = 0; i < adapter.getIsSelect().size(); i++) {
			adapter.getIsSelect().put(i, false);
		}
	}

	public boolean pageKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (View.VISIBLE == mBottomRelativeLayout.getVisibility()) {
				clearWhenExitMultiDeleteListView();
				initDate();
				return true;
			}else if(View.GONE == mSearchImageView.getVisibility()){
				mSearchImageView.setVisibility(View.VISIBLE);
				mSearchEditText.getText().clear();
				mSearchEditText.clearFocus();
				mSearchEditText.setHint(mContext.getString(R.string.quick_search_keywords));
				return true;
			}
		}
		return false;
	}

	public void newNoteing() {
		setNewNotePadData();
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.select_all_button:
			int count = adapter.getIsSelect().size();
			for (int i = 0; i < count; i++) {
				adapter.getIsSelect().put(i, true);
			}
			checkList.clear();
			for(int i=0;i<count;i++){
				checkList.add(i);
			}
			dataChanged();
			cancelSelectButton.setVisibility(View.VISIBLE);
			selectButton.setVisibility(View.INVISIBLE);
			break;
		case R.id.cancel_select_all_button:
			int count2 = adapter.getIsSelect().size();
			for (int i = 0; i < count2; i++) {
				adapter.getIsSelect().put(i, false);
			}
			checkList.clear();
			dataChanged();
			selectButton.setVisibility(View.VISIBLE);
			cancelSelectButton.setVisibility(View.INVISIBLE);
			break;
		case R.id.delect_button:
			mNoteEventList = mNotePadDataBaseDao.queryForAll();
			int id = 1;
			NoteEvent event = null;
			mUuidList = new ArrayList<String>();
			db_rowidList.clear();
			for (int i = 0; i < adapter.getIsSelect().size(); i++) {
				if (adapter.getIsSelect().get(i)) {
					event = mNoteEventList.get(i);
					id = event.getId();
					db_rowidList.add(id);
					mUuidList.add(event.getUuid());
				}
			}
			if (db_rowidList.size() > 0) {
				new Thread(new Runnable() {
					public void run() {
						int id = -1;
						for(int i=0;i<db_rowidList.size();i++){
							id = db_rowidList.get(i);
							FileUtil.deleteNoteImageFile(mUuidList.get(i));
							mNotePadDataBaseDao.deleteById(id);
						}
						mHandler.sendEmptyMessage(DO_SOMETHING_FOR_DELETE_OR_BACKUP);

					}
				}).start();

			} else {
				setNoNeedLoadData(true);
				mHandler.sendEmptyMessage(DO_SOMETHING_FOR_DELETE_OR_BACKUP);
			}
			break;
		case R.id.search:
			if (TextUtils.isEmpty(mSearchEditText.getText())) {
				mSearchEditText.setHint(mContext.getString(R.string.quick_search_keywords));
			}else{
				mSearchEditText.setHint("");
			}
			mSearchEditText.requestFocus();
			break;
		case R.id.search_delete:
			deleteBySearch = true;
			mSearchEditText.setText("");
			break;
		default:
			break;
		}
	}

	private void dataChanged() {
		adapter.notifyDataSetChanged();
	}

	public boolean onItemLongClick(AdapterView<?> parent, View view,
			int position, long id) {
		adapter.getIsSelect().put(position, true);
		checkList.add(position);
		db_rowidList.add(position);
		if (isBackupOrDelect || !isMainListOrDelectList) {
			return false;
		} else {
			initForDelectListView(isBackupOrDelect);
			return true;
		}
	}

	public void unregisterReceiver(){
		mContext.unregisterReceiver(mReceiver);
	}

	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		if (isMainListOrDelectList) {
			NoteEvent event = mNoteEventList.get(arg2);

			Intent intent = new Intent();
			intent.putExtra("row_id", event.getId());
			intent.putExtra("notepads_create_time", event.getCreateDate());
			intent.putExtra("notepads_content", event.getContents());
			intent.putExtra("bg_color", event.getBgColor());
			intent.putExtra("font_size", event.getFontSize());
			intent.putExtra("uuid", event.getUuid());
			intent.setClass(mContext, NotePadEditActivity.class);
			mContext.startActivityForResult(intent, GO_TO_NotePadEditActivity);
		} else {
			ViewHolder holder = (ViewHolder) arg1.getTag();
			holder.check_box.toggle();
			adapter.getIsSelect().put(arg2, holder.check_box.isChecked());

			if (holder.check_box.isChecked() == true) {
				if(!checkList.contains(arg2)){
					checkList.add(arg2);
				}
			} else {
				if(checkList.contains(arg2)){
					checkList.remove(Integer.valueOf(arg2));
				}
			}
			
			if(checkList.size() == 0){
				//deleteButton.setTextColor(getResources().getColor(R.color.note_edit_text_color_unable));
				deleteButton.setClickable(false);
				deleteButton.setEnabled(false);				
			}else{
				//deleteButton.setTextColor(getResources().getColor(R.color.button_text_color));
				deleteButton.setEnabled(true);	
				deleteButton.setClickable(true);	
			}

			if(checkList.size() == adapter.getCount()){				
				cancelSelectButton.setVisibility(View.VISIBLE);
				selectButton.setVisibility(View.GONE);
			}else{				
				cancelSelectButton.setVisibility(View.GONE);
				selectButton.setVisibility(View.VISIBLE);
			}
			dataChanged();
		}
	}

	public static void setNoNeedLoadData(boolean isNoNeedLoadData) {
		mIsNoNeedLoadData = isNoNeedLoadData;
	}

	/**
	 * 方法描述：根据关键字在数据库中查找，并更新Adapter数据及界面显示
	 * @param void
	 * @return void
	 * @see NoteListPage#initAdapterDate
	 */
	public void initAdapterDate(){
		String keyWord = mSearchEditText.getText().toString();
		if(null == keyWord|| keyWord.length() == 0 && !deleteBySearch){			
			mSearchEditText.clearFocus();
			mSearchEditText.setHint(mContext.getString(R.string.quick_search_keywords));
			mSearchEditText.setGravity(Gravity.CENTER);
		}
		if(deleteBySearch){
			deleteBySearch = false;
		}
		mNoteEventList = mNotePadDataBaseDao.queryForLike(keyWord);
		adapter.setNoteEventList(mNoteEventList);
		adapter.notifyDataSetChanged();
	}

	private class AdapterDateChangeBroadcastReceiver extends BroadcastReceiver{
		@Override
		public void onReceive(Context context, Intent intent) {
			mNoteEventList = mNotePadDataBaseDao.queryForAll();
			adapter.setNoteEventList(mNoteEventList);
			adapter.notifyDataSetChanged();
		}		
	}

}
