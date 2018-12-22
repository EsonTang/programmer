/*****************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：主界面
 *当前版本：V1.0
 *作  者：longbaoxiu
 *完成日期：2015-7-21
 *修改记录：
 *修改日期：
 *版 本 号：
 *修 改 人：
 *修改内容：
 ********************************************/
package com.prize.music.activities;

import java.util.ArrayList;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.prize.app.constants.Constants;
import com.prize.app.database.dao.GameDAO;
import com.prize.app.download.AppManagerCenter;
import com.prize.app.download.DownloadHelper;
import com.prize.app.util.ToastUtils;
import com.prize.music.helpers.utils.StateBarUtils;
import com.prize.music.ui.adapters.BatchEditAdapter;
import com.prize.music.R;
import com.prize.onlinemusibean.SongDetailInfo;

/**
 * 类描述：批删除下载任务
 *
 * @author longbaoxiu
 * @version v1.0
 */
public class BatchDeleteTaskActivity extends BaseActivity implements
		OnClickListener, OnItemClickListener {
	private ListView batch_list;
	private TextView cancel_Tv;
	private TextView item_select_Tv;
	private TextView all_select_Tv;
	private TextView mEdit_delete;
	private ArrayList<SongDetailInfo> items = new ArrayList<SongDetailInfo>();
	private ArrayList<SongDetailInfo> itemSelected = new ArrayList<SongDetailInfo>();
	BatchEditAdapter mBatchEditAdapter;
	RelativeLayout nota_Rlyt;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		StateBarUtils.initStateBar(this,
				getResources().getColor(R.color.statusbar_color));
		setContentView(R.layout.batch_edittask_layout);
		StateBarUtils.changeStatus(getWindow());
		findViewById();
		init();
		setListener();

	}

	@Override
	public void onResume() {
		super.onResume();
	}

	private void setListener() {
		cancel_Tv.setOnClickListener(this);
		mEdit_delete.setOnClickListener(this);
		all_select_Tv.setOnClickListener(this);
		batch_list.setOnItemClickListener(this);

	}

	private void findViewById() {
		batch_list = (ListView) findViewById(R.id.batch_list);
		mEdit_delete = (TextView) findViewById(R.id.mEdit_delete);
		cancel_Tv = (TextView) findViewById(R.id.cancel_Tv);
		item_select_Tv = (TextView) findViewById(R.id.item_select_Tv);
		all_select_Tv = (TextView) findViewById(R.id.all_select_Tv);
		nota_Rlyt = (RelativeLayout) findViewById(R.id.nota_Rlyt);
	}

	private void init() {
		if (getIntent() != null
				&& getIntent().getParcelableArrayListExtra(
						Constants.INTENTTRANSBEAN) != null) {
			items = getIntent().getParcelableArrayListExtra(
					Constants.INTENTTRANSBEAN);
			if (items != null && items.size() > 0) {
				mBatchEditAdapter = new BatchEditAdapter(this);
				mBatchEditAdapter.setData(items);
				batch_list.setAdapter(mBatchEditAdapter);
				item_select_Tv.setText(getString(R.string.number_item_selected,
						0));
			} else {
				this.finish();
			}
		} else {
			this.finish();
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onStop() {
		super.onStop();

	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.cancel_Tv:
			this.finish();
			break;
		case R.id.all_select_Tv:
			if(mBatchEditAdapter!=null&&mBatchEditAdapter.getCount()<=0){
				return;
			}
			if (mBatchEditAdapter.isSelectAll()) {
				all_select_Tv.setText(R.string.all_select);
				mBatchEditAdapter.selectAllItem(false);
			} else {
				mBatchEditAdapter.selectAllItem(true);
				all_select_Tv.setText(R.string.no_select);
			}
			int selectCount = mBatchEditAdapter.getSelectedAudioIds().length;
			item_select_Tv.setText(getString(R.string.number_item_selected,
					selectCount));
			break;
		case R.id.mEdit_delete:
			itemSelected.clear();
			itemSelected=mBatchEditAdapter.getSelectedSongDetailInfo();
			int[] postions = mBatchEditAdapter.getSelectedItemPositions();
			if (itemSelected == null || itemSelected.size() <= 0) {
				ToastUtils.showToast(R.string.pl_select);
				return;
			}
			int len=itemSelected.size();
//			int len=postions.length;
			if(len==1){
				AppManagerCenter.cancelDownload(itemSelected.get(0));
				DownloadHelper
						.deleteTmpDownloadFile(itemSelected.get(0));
				if(DownloadHelper.isFileExists(itemSelected.get(0))){
					DownloadHelper.deleteDownloadedFile(itemSelected.get(0));
				}
				items.remove(postions[0]);
				responseDelete();
				mBatchEditAdapter.setData(items);
				return;
			}
			DownloadHelper.deleteBatchTmpFile(itemSelected);
			AppManagerCenter.cancelBatchDownload(itemSelected);
			if(len==items.size()){
				items.clear();
				responseDelete();
				nota_Rlyt.setVisibility(View.VISIBLE);
				mEdit_delete.setEnabled(false);
				mBatchEditAdapter.setData(items);
				return;
			}
			mBatchEditAdapter.notifyDataSetChanged();
			mHandler.postDelayed(new Runnable() {
				
				@Override
				public void run() {
					filterDeleteSong();
					responseDelete();
				}
			}, 1500);
			break;

		default:
			break;
		}

	}

	protected void responseDelete() {
		mBatchEditAdapter.selectAllItem(false);
		all_select_Tv.setText(R.string.all_select);
		item_select_Tv.setText(getString(R.string.number_item_selected, 0));
		if (mBatchEditAdapter.getCount() <= 0) {
			nota_Rlyt.setVisibility(View.VISIBLE);
			mEdit_delete.setEnabled(false);
		} else{
			mEdit_delete.setEnabled(true);
			
		}
		
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		mBatchEditAdapter.toggleCheckedState(position);
		int selectCount = mBatchEditAdapter.getSelectedAudioIds().length;
		item_select_Tv.setText(getString(R.string.number_item_selected,
				selectCount));
		if (selectCount == batch_list.getCount()) {
			mBatchEditAdapter.setIsSelectAll(true);
			all_select_Tv.setText(R.string.no_select);
		} else {
			all_select_Tv.setText(R.string.all_select);
			mBatchEditAdapter.setIsSelectAll(false);
		}
	}

	void filterDeleteSong() {
		items = GameDAO.getInstance().getDownAppList();
		if (items.size() <= 0) {
			nota_Rlyt.setVisibility(View.VISIBLE);
			mEdit_delete.setEnabled(false);
		} else{
			mEdit_delete.setEnabled(true);
			
		}
		mBatchEditAdapter.setData(items);
	}
	
	private Handler mHandler= new Handler();
}
