
/*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：便签列表界面Activity
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
package com.android.notepad;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.android.notepad.note.NoteListPage;
import com.android.notepad.R;

/**
 **
 * 类描述：便签列表界面Activity
 * @author 朱道鹏
 * @version V1.0
 */
public class NotePadActivity extends BaseActivity {
	private ViewSwitcher mViewSwitcher;
	private NoteListPage mNoteListPage;

	private TextView mImageViewNewNote;
	private int mCurrentPage;
	
	/**
	 * 是否需要加载便签数据
	 */
	private boolean isLoadData;
	public static RelativeLayout mNewNoteButtonRl;
	private static final int PAGE_NOTE = 0;
	
	/**
	 * 退出请求
	 */
	public static final int EXIT_APP = 2;
	/**
	 * 是否需要更新数据
	 */
	public static final int NO_NEED_LOAD_DATA = 3;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setSubContentView(R.layout.layout_notepad);

		setLeftClickAreaClickEnAble();
		hideBackButton();
		hideWeekAndTimeView();
		hideRightClickArea();
		hideMidView();
		hideMonthAndDayView();
		
		displayMidView();
		setMidViewTitle(getResources().getString(R.string.str_page_note)); 
		//setMonthAndDayViewTitle(getResources().getString(R.string.str_page_note));

		mCurrentPage = PAGE_NOTE;     

		mNewNoteButtonRl = (RelativeLayout) findViewById(R.id.create_new_note_rl);
		mImageViewNewNote = (TextView)findViewById(R.id.new_note);
		mImageViewNewNote.setOnClickListener(new OnClickListener(){
			public void onClick(View arg0) {
				// 生成文本新便签
				mCurrentPage = PAGE_NOTE;
				mNoteListPage.newNoteing();
			}
		});

		mViewSwitcher = (ViewSwitcher)findViewById(R.id.viewSwitcher);
		mNoteListPage = new NoteListPage(this);
		mNoteListPage.pageCreate();
		mViewSwitcher.addView(mNoteListPage);

		resetPage();
	}

	public void selectPage(int page) {
		if(page != mCurrentPage) {
			mCurrentPage = page;
			mViewSwitcher.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.move_right_in));
			mViewSwitcher.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.move_right_out));

			mViewSwitcher.setDisplayedChild(mCurrentPage);
		}
	}

	public void resetPage() {
		mViewSwitcher.setInAnimation(null);
		mViewSwitcher.setOutAnimation(null);
		mViewSwitcher.setDisplayedChild(mCurrentPage);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_notepad, menu);
		return true;
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		resetPage();
	}
	
	@Override
	protected void onDestroy() {
		mNoteListPage.unregisterReceiver();
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if(mCurrentPage == PAGE_NOTE){
			isLoadData = true;
			mNoteListPage.pageResume();
		}
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if(mCurrentPage == PAGE_NOTE){
			if(mNoteListPage.pageKeyUp(keyCode, event)){
				return true;
			}
		}
		return super.onKeyUp(keyCode, event);
	}

	@Override
	public void onBackPressed() {
		String searchContent = mNoteListPage.mSearchEditText.getText().toString();
		if(searchContent.length()>0){
			mNoteListPage.mSearchEditText.setText("");
			searchContent = mNoteListPage.mSearchEditText.getText().toString();
			mNoteListPage.initAdapterDate();
		}else{
			super.onBackPressed();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(mCurrentPage == PAGE_NOTE){			
			if(resultCode == EXIT_APP ){
				finish();
			}
		}
	}

	 /**
	 * 方法描述：当前界面焦点发生改变时（新建便签或者更改便签返回时，isLoadData=true触发有效），执行数据更新
	 * @param boolean
	 * @return void
	 * @see NotePadActivity#onWindowFocusChanged
	 */
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		if(isLoadData){
			Log.e("", "onWindowFocusChanged()");
			mNoteListPage.pageOnWindowFocusChanged();
			isLoadData = false;
		}
	}
}
