package com.prize.music;

import java.util.List;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.prize.app.util.ToastUtils;
import com.prize.music.database.ListInfo;
import com.prize.music.database.SQLUtils;
import com.prize.music.helpers.utils.MusicUtils;
import com.prize.music.helpers.utils.MusicUtils.AddCollectCallBack;
import com.prize.music.helpers.utils.StateBarUtils;
import com.prize.music.ui.adapters.MeFragmentSongSheetAdapter;
import com.prize.music.R;
/**
 * 
 **
 * 编辑歌单
 * @author longbaoxiu
 * @version V1.0
 */
public class EditSheetActivity extends FragmentActivity implements OnClickListener {

	GridView edit_grideView;
	TextView edit_fg_all;
	Button edit_sure;
	Button edit_neg;
	ImageView edit_fg_back;

	private MeFragmentSongSheetAdapter mSongSheetAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		StateBarUtils.initStateBar(this);
		setContentView(R.layout.activity_edit_sheet);
		StateBarUtils.changeStatus(getWindow());

		findViewById();
		init();
	}

	private void init() {
		// TODO Auto-generated method stub
		mSongSheetAdapter = new MeFragmentSongSheetAdapter(this,
				R.layout.edit_activity_item);
		edit_grideView.setAdapter(mSongSheetAdapter);
		edit_grideView.setSelector(new ColorDrawable(Color.TRANSPARENT));
		mSongSheetAdapter.setSelectMode(true);
		int selectCount = mSongSheetAdapter.getSelectedAudioIds().size();
		countNum(selectCount);
		setData();
	}

	private void setData() {
		// Cursor mPlayListCursor =
		// SQLUtils.getInstance(getBaseContext()).query(
		// DatabaseConstant.TABLENAME_LIST);
		List<ListInfo> arraylist = SQLUtils.getInstance(this).queryMenu();
		if(arraylist ==null||arraylist.isEmpty()){
			finish();
		}
		mSongSheetAdapter.addList(arraylist, false);

	}

	private void findViewById() {
		// TODO Auto-generated method stub
		edit_grideView = (GridView) findViewById(R.id.edit_grideView);
		edit_fg_all = (TextView) findViewById(R.id.edit_fg_all);
		edit_sure = (Button) findViewById(R.id.edit_sure);
		edit_neg = (Button) findViewById(R.id.edit_neg);
		edit_fg_back = (ImageView) findViewById(R.id.edit_fg_back);

		edit_fg_all.setOnClickListener(this);
		edit_sure.setOnClickListener(this);
		edit_neg.setOnClickListener(this);
		edit_fg_back.setOnClickListener(this);

		edit_grideView.setOnLongClickListener(new OnLongClickListener() {
			public boolean onLongClick(View v) {
				return false;
			}
		});

		edit_grideView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				mSongSheetAdapter.toggleCheckedState(position);		
				
				
				CheckBox  checkBox  = (CheckBox) view.findViewById(R.id.edit_check);
				if(checkBox.isChecked()){
					checkBox.setChecked(false);
				}else{
					checkBox.setChecked(true);
				}
				int selectCount = mSongSheetAdapter.getSelectedAudioIds().size();
				countNum(selectCount);
				if (selectCount == edit_grideView.getCount()) {
					mSongSheetAdapter.setIsSelectAll(true);
					edit_fg_all.setText(R.string.no_select);
				} else {
					mSongSheetAdapter.setIsSelectAll(false);
					edit_fg_all.setText(R.string.all_select);
				}

			}
		});
	}
	
	private void countNum(int num){
		if(num == 0){
			edit_sure.setEnabled(false);
		} else {
			edit_sure.setEnabled(true);
		}
	}
	

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		switch(arg0.getId()){
		case R.id.edit_fg_all:
			if (mSongSheetAdapter == null) {
				return;
			}
			if (mSongSheetAdapter.isSelectAll()) {
				mSongSheetAdapter.selectAllItem(false);
				edit_fg_all.setText(R.string.all_select);
			} else {
				mSongSheetAdapter.selectAllItem(true);
				edit_fg_all.setText(R.string.no_select);
			}
			int selectCount = mSongSheetAdapter.getSelectedAudioIds().size();
			countNum(selectCount);
			break;
		case R.id.edit_sure:
			List<ListInfo> list = mSongSheetAdapter.getSelectedAudioIds();
			MusicUtils.removeMultiOnLineAndLocalPlayList(getBaseContext(), list, removeCallback);
			break;
		case R.id.edit_neg:
			finish();
			break;
		case R.id.edit_fg_back:
			finish();
			break;
		}
	}

	AddCollectCallBack removeCallback = new AddCollectCallBack(){
		public void addCollectResult(boolean result,String tableName) {
			ToastUtils.showToast(R.string.deleteSuccessful);
			mSongSheetAdapter.selectAllItem(false);
			//删除完成置灰
			countNum(0);
			setData();
		}

		@Override
		public void isCollected() {
			
			
		}
	};
	
}
