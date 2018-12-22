package com.pr.scuritycenter.optimize;

import java.util.ArrayList;
import java.util.List;
import com.pr.scuritycenter.R;
import com.pr.scuritycenter.framework.BaseActivity;
import com.pr.scuritycenter.utils.DeviceUtils;
import com.pr.scuritycenter.utils.SharedPreferencesUtil;

import tmsdk.common.creator.ManagerCreatorC;
import tmsdk.common.module.optimize.IMemoryHelper;
import tmsdk.common.module.optimize.OptimizeManager;
import tmsdk.common.module.optimize.RunningProcessEntity;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * 
 * @author wangzhong
 *
 */
public class OptimizingActivity extends BaseActivity {
	
	private final static String TAG = "OptimizingActivity";

	private LinearLayout ll_optimizing_home;
	
	private ImageView iv_optimizing_percent_status;
//	private ImageView iv_optimizing_percent_dot;
	private TextView tv_optimizing_percent;
	private TextView tv_optimizing_status;
	private TextView tv_optimizing_num;
	private Button bt_optimizing_status;
	
	
	
	private ExpandableListView elv_optimizing_detail;
	private List<OptimizingBean> mListData;
	private OptimizingExpandableListAdapter mOptimizingExpandableListAdapter;
	private int[] mParentPic = new int[]{
			R.drawable.optimizing_item_safe,
			R.drawable.optimizing_item_clear,
			R.drawable.optimizing_item_speed,
			R.drawable.optimizing_item_finishpre
	};

	
	private OptimizeManager mOptimizeManager;
	private IMemoryHelper mMemoryHelper;
	
	@Override
	public void initInfo() {
		
	}

	@Override
	public void initView() {
		setContentView(R.layout.optimizing_activity);
		
		ll_optimizing_home = (LinearLayout) findViewById(R.id.ll_optimizing_home);
		ll_optimizing_home.setPadding(0, DeviceUtils.getStatusBarHeight(this), 0, 0);
		
		
		// Optimizing status.
		iv_optimizing_percent_status = (ImageView) findViewById(R.id.iv_optimizing_percent_status);
		Animation animation = AnimationUtils.loadAnimation(mLayoutInflater.getContext(), R.anim.animation_item_status);
		iv_optimizing_percent_status.startAnimation(animation);
		tv_optimizing_percent = (TextView) findViewById(R.id.tv_optimizing_percent);
		tv_optimizing_status = (TextView) findViewById(R.id.tv_optimizing_status);
		tv_optimizing_num = (TextView) findViewById(R.id.tv_optimizing_num);
		bt_optimizing_status = (Button) findViewById(R.id.bt_optimizing_status);
		bt_optimizing_status.setTag(false);
		bt_optimizing_status.setOnClickListener(this);
		
		
		
		
		// Detailed
		String[] arrayParent = getResources().getStringArray(R.array.array_optimizing_parent);
		mListData = new ArrayList<OptimizingBean>();
		for (int i = 0; i < arrayParent.length; i++) {
			OptimizingBean optimizingBean = new OptimizingBean();
			optimizingBean.setName(arrayParent[i]);
			optimizingBean.setPic(mParentPic[i]);
			mListData.add(optimizingBean);
		}
		mOptimizingExpandableListAdapter = new OptimizingExpandableListAdapter(this.getLayoutInflater(), mListData, mListData);
		elv_optimizing_detail = (ExpandableListView) findViewById(R.id.elv_optimizing_detail);
		elv_optimizing_detail.setAdapter(mOptimizingExpandableListAdapter);
		elv_optimizing_detail.setGroupIndicator(null);
		elv_optimizing_detail.setOnGroupClickListener(new OnGroupClickListener() {
			
			@Override
			public boolean onGroupClick(ExpandableListView arg0, View arg1, int arg2,
					long arg3) {
				return true;
			}
		});
		
		
		mOptimizeManager = ManagerCreatorC.getManager(OptimizeManager.class);
		// 1. Safe.
		//reinforceSecurity();
		
		// 2. Clear cache.
		// 选择需要结束的单个或者多个软件进程并释放缓存
		//clearAllCache();
		
		// 3.Accelerate.
		// 开启手机加速，显示所有正在运行的软件和对应软件的内存
		//speedUp();
		
		// 4. Close process.
		//closeAllProcess();
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				delayTwoSeconds();
				myHandler.sendEmptyMessage(MSG_START);
			}
		}).start();
	}

	protected void delayTwoSeconds() {
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	// 1. Safe.
	//////////////////////////////////////////////////////////////////////////////////////////
	private void reinforceSecurity() {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				
				delayTwoSeconds();
				myHandler.sendEmptyMessage(MSG_SECURITY);
			}
		}).start();
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	// 2. Close process.
	//////////////////////////////////////////////////////////////////////////////////////////
	private void clearAllCache() {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				mMemoryHelper = mOptimizeManager.getMemoryHelper();
				boolean isSuccess = mMemoryHelper.clearAllCacheData();
				Log.v(TAG, "isSuccess:" + isSuccess);
				
				
				delayTwoSeconds();
				//if (isSuccess) {
					myHandler.sendEmptyMessage(MSG_CLEARCACHE);
				//}
			}
		}).start();

	}

	//////////////////////////////////////////////////////////////////////////////////////////
	// 3.Accelerate.
	//////////////////////////////////////////////////////////////////////////////////////////
	private void speedUp() {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				
				
				delayTwoSeconds();
				myHandler.sendEmptyMessage(MSG_ACCELERATE);
			}
		}).start();
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	// 4. Close process.
	//////////////////////////////////////////////////////////////////////////////////////////
	private void closeAllProcess() {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				ArrayList<String> Pkglist = getAllKillableRunningProcessList();
				if (Pkglist != null && Pkglist.size() > 0) {
					try {
						mOptimizeManager.closeProcess(Pkglist, null);
					} catch (NameNotFoundException e) {
						e.printStackTrace();
					}
					mOptimizeManager.killTasks(Pkglist, true, null);
					
				}
				
				delayTwoSeconds();
				myHandler.sendEmptyMessage(MSG_CLOSEPROCESS);
			}
		}).start();
	}

	/**
	 * 
	 * @return
	 */
	protected ArrayList<String> getAllKillableRunningProcessList() {
		ArrayList<String> Pkglist = new ArrayList<String>();
		List<RunningProcessEntity> list = mOptimizeManager.getRunningProcessList(true, true);
		if (list != null && list.size() > 0) {
			for (RunningProcessEntity entity : list) {
				Log.v(TAG, "RunningappPkg:" + entity.mProcessEntity.mPackageName);
				if (!OptimizingActivity.this.getApplication().getPackageName().
						equals(entity.mProcessEntity.mPackageName)) {
					Pkglist.add(entity.mProcessEntity.mPackageName);
				}
			}
			removeWhiteListAPP(Pkglist);
		}
		return Pkglist;
	}

	/**
	 * The application of removing users are not allowed to kill.
	 * @param pkglist
	 */
	private void removeWhiteListAPP(ArrayList<String> pkglist) {
		//pkglist.remove(object);
		if(pkglist.contains("com.cooee.unilauncher")){
			pkglist.remove("com.cooee.unilauncher");
		}
		if(pkglist.contains("com.prize.weather")){
			pkglist.remove("com.prize.weather");
		}
		if(pkglist.contains("com.prize.music")){
			pkglist.remove("com.prize.music");
		}
	}

	private final static int MSG_START = 0;
	private final static int MSG_SECURITY = 1;
	private final static int MSG_CLEARCACHE = 2;
	private final static int MSG_ACCELERATE = 3;
	private final static int MSG_CLOSEPROCESS = 4;

	@SuppressLint("HandlerLeak")
	private Handler myHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			int parentPosition = 0;
			switch (msg.what) {
			case MSG_SECURITY:
				parentPosition = 0;
				break;
			case MSG_CLEARCACHE:
				parentPosition = 1;
				break;
			case MSG_ACCELERATE:
				parentPosition = 2;
				break;
			case MSG_CLOSEPROCESS:
				parentPosition = 3;
				break;

			default:
				break;
			}
			if (msg.what != MSG_START) {
				mListData.get(parentPosition).setStatus(OptimizingBean.STATUS_OPTIMIZATION_AFTER);
				mOptimizingExpandableListAdapter.notifyDataSetChanged();
			}
			toNextStep(msg.what);
		}
		
	};

	protected void toNextStep(int what) {
		switch (what) {
		case MSG_START:
//			elv_optimizing_detail.setSelectionFromTop(0, 0);
			reinforceSecurity();
			break;
		case MSG_SECURITY:
			tv_optimizing_percent.setText("25%");
			tv_optimizing_num.setText("已优化1个选项");
//			elv_optimizing_detail.setSelectionFromTop(1, 0);
			clearAllCache();
			break;
		case MSG_CLEARCACHE:
			tv_optimizing_percent.setText("50%");
			tv_optimizing_num.setText("已优化2个选项");
//			elv_optimizing_detail.setSelectionFromTop(2, 0);
			speedUp();
			break;
		case MSG_ACCELERATE:
			tv_optimizing_percent.setText("75%");
			tv_optimizing_num.setText("已优化3个选项");
			elv_optimizing_detail.setSelectionFromTop(3, 0);
			closeAllProcess();
			break;
		case MSG_CLOSEPROCESS:
			tv_optimizing_percent.setText("100%");
			tv_optimizing_num.setText("已优化4个选项");
			tv_optimizing_status.setText("优化完成");
			
			iv_optimizing_percent_status.clearAnimation();
			bt_optimizing_status.setBackgroundResource(R.drawable.optimizing_status_finish_selector);
			bt_optimizing_status.setText("完成");
			bt_optimizing_status.setTextColor(Color.WHITE);
			bt_optimizing_status.setTag(true);
			SharedPreferencesUtil.saveInt(getApplicationContext(), "OPTIMIZERESULT", 100);
			break;

		default:
			break;
		}
	}

	@Override
	protected void initTopbar() {
		//super.initTopbar();
		//setTitle(getResources().getString(R.string.optimizing_title));
		//setAssistBG(R.drawable.optimizing_warning);
		//bt_topbar_assist.setVisibility(View.GONE);
	}

	@Override
	public void initData() {
		
	}

	@Override
	public void onClick(View v) {
		super.onClick(v);
		switch (v.getId()) {
		case R.id.bt_optimizing_status:
			/*if ((Boolean) bt_optimizing_status.getTag()) {
				SharedPreferencesUtil.saveInt(OptimizingActivity.this, "OPTIMIZERESULT", 100);
				finish();
			} else {
				
			}*/
			finish();
			break;

		default:
			break;
		}
	}

}
