package com.pr.scuritycenter.activity;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pr.scuritycenter.R;
import com.pr.scuritycenter.base.BaseActivity;
import com.pr.scuritycenter.optimize.OptimizingActivity;
import com.pr.scuritycenter.setting.SettingActivity;
import com.pr.scuritycenter.utils.SharedPreferencesUtil;

public class MainActivity extends BaseActivity implements OnClickListener,
		OnPageChangeListener {

	private int[] iconsOnepager = { R.drawable.pure_backgroud_selector,
			R.drawable.auto_boot_selector,
			R.drawable.permission_manage_selector,
			R.drawable.virus_scan_selector, R.drawable.rubbish_clean_selector,
			R.drawable.notification_manage_selector };

	private int[] iconsSecondpager = { R.drawable.data_update_selector,
			R.drawable.net_control_selector /*R.drawable.net_safe_selector*/ };

	private Intent intent;
	private Button bt_optimize;
	// private DialChartView abCircleProgressBar = null;
	private ImageButton ib_home_setting;

	private final static int ADDCYCLIE = 0x01;
	private final static int DELCYCLIE = 0x02;
	private int currentLabel = 0;
	private Grade grade;
	private String[] namesOnepager;
	private String[] namesSecondpager;
	// private DialChartView circle_view;
	private int optimizeresult;
	private long runtime;
	private ViewPager function_item;
	private ArrayList<GridView> lists;
	LinearLayout llPointGroup;
	private float density;
	LayoutParams params;
	private View mSelectPoinView;// 选中的点View的对象
	private int basicWidth;// 两个点之间的距离

	@Override
	protected void setContentView() {
		SharedPreferencesUtil.saveInt(getApplicationContext(),
				"OPTIMIZERESULT", 50);
		optimizeresult = SharedPreferencesUtil.getInt(getApplicationContext(),
				"OPTIMIZERESULT", 0);

		setContentView(R.layout.security_center_main);

		DisplayMetrics metric = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metric);
		density = metric.density;
	}

	@Override
	protected void findViewById() {
		bt_optimize = (Button) findViewById(R.id.bt_optimize);
		ib_home_setting = (ImageButton) findViewById(R.id.ib_home_setting);
		bt_optimize.setOnClickListener(this);
		ib_home_setting.setOnClickListener(this);
		function_item = (ViewPager) findViewById(R.id.function_item);
		function_item.setOnPageChangeListener(this);
		namesOnepager = this.getResources().getStringArray(
				R.array.function_items);
		namesSecondpager = this.getResources().getStringArray(
				R.array.function_items_expansion);
		llPointGroup = (LinearLayout) findViewById(R.id.ll_guide_point_group);
		mSelectPoinView = findViewById(R.id.select_point);

		// 添加2个viewPager
		lists = new ArrayList<GridView>();
		GridView gridView = new GridView(MainActivity.this);
		functionAdapter functionAdapter = new functionAdapter(namesOnepager,
				iconsOnepager);
		gridView.setAdapter(functionAdapter);
		gridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				switch (position) {

				case 0: // 纯净后台
					intent = new Intent();
					intent.setAction("android.intent.action.MAIN");
					intent.setAction("com.android.purebackground_BOOT");
					intent.addCategory("android.intent.category.DEFAULT");
					break;
				case 1: // 通知管理
					intent = new Intent();
					intent.setAction("android.intent.action.MAIN");
					intent.setAction("com.mediatek.security.AUTO_BOOT");
					intent.addCategory("android.intent.category.DEFAULT");
					break;
				case 2: // 应用权限
					intent = new Intent();
					intent.setAction("com.mediatek.security.PERMISSION_CONTROL");
					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					break;
				case 3: // 病毒扫描
					intent = new Intent();
					intent.setClass(getApplication(), ScannerActivity.class);
					break;
				case 4: // 垃圾清理
					runtime = SharedPreferencesUtil.getLong(
							getApplicationContext(), "RUNTIME", 0);
					intent = new Intent();
					if (System.currentTimeMillis() - runtime > 60 * 1000) {
						intent.setClass(getApplicationContext(),
								RubbishCleanActivity.class);
					} else {
						intent.setClass(getApplicationContext(),
								RubbishCleanJustActivity.class);
					}

					break;
				case 5: // 数据更新
					intent = new Intent();
					intent.setAction("android.intent.action.MAIN");
					intent.setAction("com.android.settings.NOTIFICATION_CENTRE");
					intent.addCategory("android.intent.category.DEFAULT");

					break;

				}
				if (intent != null) {
					startActivity(intent);
				}
			}
		});
		setStyle(gridView);
		lists.add(gridView);

		// 通知管理页面
		GridView gridView2 = new GridView(MainActivity.this);
		functionAdapter functionAdapter2 = new functionAdapter(
				namesSecondpager, iconsSecondpager);
		gridView2.setAdapter(functionAdapter2);
		gridView2.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				switch (position) {
				case 0:// 通知管理
					intent = new Intent();
					intent.setClass(getApplicationContext(),
							UpdateActivity.class);
					break;
				case 1:// 网络控制
					intent = new Intent();
					intent.setAction("android.intent.action.MAIN");
					intent.setAction("com.android.settings.APP_NET_CONTROL_SETTING");
					intent.addCategory("android.intent.category.DEFAULT");
					break;
				}
				if (intent != null) {
					startActivity(intent);
				}
			}
		});
		setStyle(gridView2);
		lists.add(gridView2);

		myViewPagerAdapter viewPagerAdapter = new myViewPagerAdapter();
		function_item.setAdapter(viewPagerAdapter);

	}

	private void setStyle(GridView gridView) {
		gridView.setNumColumns(3);
		gridView.setHorizontalSpacing((int) (25 * density));
		gridView.setVerticalSpacing((int) (30 * density));

	}

	@Override
	protected void controll() {
		initView();
	}

	@Override
	protected void onResume() {
		optimizeresult = SharedPreferencesUtil.getInt(getApplicationContext(),
				"OPTIMIZERESULT", 0);
		Log.v("bian", "optimizeresult2=" + optimizeresult);
		if (optimizeresult == 100) {
			grade = new Grade();
			grade.result = optimizeresult;
			Message message = Message.obtain();
			message.what = DELCYCLIE;
			message.obj = grade;
			mHandler.sendMessage(message);
		}
		super.onResume();
	}

	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case ADDCYCLIE:
				break;
			case DELCYCLIE:
				Grade mGrade = (Grade) msg.obj;
				optimizeresult = mGrade.result;
				// circle_view.setCurrentStatus(optimizeresult);
				break;
			default:
				break;
			}

		}

	};

	private void initView() {

		// 添加导航
		for (int i = 0; i < 2; i++) {
			View view = new View(MainActivity.this);
			view.setBackgroundResource(R.drawable.point_normal);
			params = new LayoutParams(10, 10);
			if (i != 0) {
				params.leftMargin = (int) (5 * density);
			}
			view.setLayoutParams(params);
			llPointGroup.addView(view);
		}

		mSelectPoinView.getViewTreeObserver().addOnGlobalLayoutListener(
				new OnGlobalLayoutListener() {
					@Override
					public void onGlobalLayout() {
						mSelectPoinView.getViewTreeObserver()
								.removeGlobalOnLayoutListener(this);
						basicWidth = llPointGroup.getChildAt(1).getLeft()
								- llPointGroup.getChildAt(0).getLeft();
					}
				});
	}

	public class myViewPagerAdapter extends PagerAdapter {

		@Override
		public int getCount() {
			return lists.size();
			// return 1;
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == object;
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			GridView view = lists.get(position);
			container.addView(view);
			return view;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((View) object);
		}

	}

	public class functionAdapter extends BaseAdapter {

		private String[] names;
		private int[] icons;

		public functionAdapter(String[] names, int[] icons) {
			this.names = names;
			this.icons = icons;
		}

		private TextView mTextView;

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = View.inflate(MainActivity.this,
					R.layout.item_main_gridview, null);
			mTextView = (TextView) view.findViewById(R.id.tv_name);
			mTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
			mTextView.setText(names[position]);
			ImageView mImageView = (ImageView) view.findViewById(R.id.iv_icon);
			mImageView.setImageResource(icons[position]);
			return view;
		}

		@Override
		public int getCount() {

			return names.length;
		}

		@Override
		public Object getItem(int position) {

			return names[position];
		}

		@Override
		public long getItemId(int position) {

			return position;
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// getMenuInflater().inflate(R.menu.main, menu);
		return false;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.bt_optimize:
			Intent iOptimizing = new Intent(this, OptimizingActivity.class);
			startActivity(iOptimizing);
			break;
		case R.id.ib_home_setting:
			Intent iSetting = new Intent(this, SettingActivity.class);
			startActivity(iSetting);
			break;
		default:
			break;
		}

	}

	class Grade {
		int result;
	}

	// 监听viewPager的滑动状态
	@Override
	public void onPageScrolled(int position, float positionOffset,
			int positionOffsetPixels) {

		int leftMargin = (int) (basicWidth * (position + positionOffset));
		RelativeLayout.LayoutParams params = (android.widget.RelativeLayout.LayoutParams) mSelectPoinView
				.getLayoutParams();

		params.leftMargin = leftMargin;
		mSelectPoinView.setLayoutParams(params);
	}

	@Override
	public void onPageSelected(int position) {

	}

	@Override
	public void onPageScrollStateChanged(int state) {

	}

}
