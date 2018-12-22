/*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：
 *当前版本：
 *作	者：
 *完成日期：
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

package com.prize.weather.city;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.prize.weather.R;
import com.prize.weather.WeatherHomeActivity;
import com.prize.weather.db.DBManager;
import com.prize.weather.framework.ISPCallBack;
import com.prize.weather.util.CityUtil;
import com.prize.weather.view.ClearEditText;

//import android.os.SystemProperties;

/**
 ** 
 * 类描述：
 * 
 * @author 作者
 * @version 版本
 */
public class CitySelectActivity extends Activity {

	DBManager db;

	TextView hotCityTv;
	TextView cityTv;
	TextView back;
	int backFlag = 0;
//	ClearEditText mClearEditText;
	EditText mClearEditText;
	private SortAdapter adapter;
	private ListView sortListView;
	/**
	 * 汉字转换成拼音的类
	 */
	private CharacterParser characterParser;
//	private List<SortModel> SourceDateList;
	/**
	 * 根据拼音来排列ListView里面的数据类
	 */
//	private PinyinComparator pinyinComparator;

	GridView hotCityGrid;
	GridAdapter hotCityAdapter;
	ArrayList<String> hotCityList = new ArrayList<String>();
	ArrayList<Integer> hotCitycCodeList = new ArrayList<Integer>();

	GridView provGrid = null;
	GridAdapter provGridAdapter = null;
	ArrayList<String> provList = new ArrayList<String>();
	ArrayList<Integer> provIdList = new ArrayList<Integer>();
	ArrayList<Integer> provTypeList = new ArrayList<Integer>();

	GridView cityGrid = null;
	GridAdapter cityGridAdapter = null;
	ArrayList<String> cityList = new ArrayList<String>();
	ArrayList<Integer> cityIdList = new ArrayList<Integer>();
	ArrayList<Integer> citycCodeList = new ArrayList<Integer>();

	GridView countyGrid = null;
	GridAdapter countyGridAdapter = null;
	ArrayList<String> countyList = new ArrayList<String>();
	ArrayList<Integer> countycCodeList = new ArrayList<Integer>();

	Thread mThread;
//	String names[],filterNames[];
//	Integer codes[],filterCodes[];
//	
//	private Thread thread;
	
	private LinearLayout city_content;
	private int mStatusBarHeight, mNavigationBarHeight;
	public int getStatusBarHeight() {
		if (mStatusBarHeight == 0) {
			try {
				Class<?> c = Class.forName("com.android.internal.R$dimen");
				Object o = c.newInstance();
				Field f = c.getField("status_bar_height");
				int x = (Integer) f.get(o);
				mStatusBarHeight = getResources().getDimensionPixelSize(x);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return mStatusBarHeight;
	}
	
	/**
	 * Get the height of the navigation bar.
	 * @return Returns the status bar height pixel values.
	 */
	public int getNavigationBarHeight() {
		/*if(SystemProperties.get("qemu.hw.mainkeys").equals("0")){
			if (mNavigationBarHeight == 0) {
				try {
					Class<?> c = Class.forName("com.android.internal.R$dimen");
					Object o = c.newInstance();
					Field f = c.getField("navigation_bar_height");
					int x = (Integer) f.get(o);
					mNavigationBarHeight = getResources().getDimensionPixelSize(x);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return mNavigationBarHeight;
		}else{
			return 0;
		}*/	
		return 0;
	}
	private void initStatusBar() {
		Window window = getWindow();
		window.requestFeature(Window.FEATURE_NO_TITLE);
		if(VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
			window = getWindow();
			window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS 
					| WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
			window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
					//| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
					| View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
			window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
			window.setStatusBarColor(Color.TRANSPARENT);
			//window.setNavigationBarColor(Color.TRANSPARENT);
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initStatusBar();
		Log.d("city","isUser = "+DBManager.isUser);
		setContentView(R.layout.province);
		db = new DBManager(this);
		initView();
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		initSearchView();
//		thread = new Thread(r);
//		thread.start();
		
		/**2015.7.20*/
		LinearLayout searchLayout = (LinearLayout)findViewById(R.id.search_layout);
		if(DBManager.isUser){
			provGrid.setVisibility(View.GONE);
			cityTv.setVisibility(View.GONE);
			searchLayout.setVisibility(View.GONE);	
		}else{
			provGrid.setVisibility(View.VISIBLE);
			cityTv.setVisibility(View.VISIBLE);
			searchLayout.setVisibility(View.VISIBLE);	
		}
	}

	private void initView() {
		hotCityGrid = (GridView) findViewById(R.id.hotcity_grid);
		hotCityAdapter = new GridAdapter(CitySelectActivity.this, R.layout.select_item_layout);
//		getHotCityData();
//		String[] hotCity_option = getResources().getStringArray(R.array.hot_city_option);
//		int[] hotCityCode = getResources().getIntArray(R.array.hot_city_code);
		/**2015.7.20*/
		String[] hotCity_option;
		int[] hotCityCode;
		if(DBManager.isUser){
			hotCity_option = getResources().getStringArray(R.array.hot_city_option_user);
			hotCityCode = getResources().getIntArray(R.array.hot_city_code_user);
		}else{
			hotCity_option = getResources().getStringArray(R.array.hot_city_option);
			hotCityCode = getResources().getIntArray(R.array.hot_city_code);
		}
		for (int i = 0; i < hotCity_option.length; i++) {
			hotCityList.add(hotCity_option[i]);
			hotCitycCodeList.add(hotCityCode[i]);
		}
		 
		hotCityAdapter.setListItems(hotCityList);
		hotCityGrid.setAdapter(hotCityAdapter);
		hotCityGrid.setOnItemClickListener(hotCityItemSelectListener);

		provGrid = (GridView) findViewById(R.id.provGrid);
		provGridAdapter = new GridAdapter(CitySelectActivity.this, R.layout.select_item_layout);
//		getProvincesData();
		String[] provice_option = getResources().getStringArray(R.array.provinces_option);
		int[] provId = getResources().getIntArray(R.array.provinces_dcode_option);
		for (int i = 0; i < provice_option.length; i++) {
        	provList.add(provice_option[i]);
        	provIdList.add(provId[i]);
		}
		
		provGridAdapter.setListItems(provList);
		provGrid.setAdapter(provGridAdapter);
		provGrid.setOnItemClickListener(provinceItemSelectListener);

		cityGrid = (GridView) findViewById(R.id.cityGrid);
		cityGridAdapter = new GridAdapter(CitySelectActivity.this, R.layout.select_item_layout);
		cityGridAdapter.setListItems(cityList);
		cityGrid.setAdapter(cityGridAdapter);
		cityGrid.setOnItemClickListener(cityItemSelectListener);

		countyGrid = (GridView) findViewById(R.id.countyGrid);
		countyGridAdapter = new GridAdapter(CitySelectActivity.this, R.layout.select_item_layout);
		countyGridAdapter.setListItems(countyList);
		countyGrid.setAdapter(countyGridAdapter);
		countyGrid.setOnItemClickListener(countyItemSelectListener);

		// mSearchView = (SearchView) findViewById(R.id.search_city);
		// mSearchView = (EditText)findViewById(R.id.search_city);
		// searchLayout = (LinearLayout)findViewById(R.id.linear_cancel_layer);
		hotCityTv = (TextView) findViewById(R.id.hot_city_tv);
		cityTv = (TextView) findViewById(R.id.city_tv);
		back = (TextView) findViewById(R.id.back);
		back.setOnClickListener(backOnClickListener);
		city_content = (LinearLayout) findViewById(R.id.city_content);
		//city_content.setPadding(0, getStatusBarHeight(), 0, getNavigationBarHeight());
		city_content.setPadding(0, getStatusBarHeight(), 0, 0);
		
		
	}

	/*public Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1000:
				names = (String[]) ((ArrayList) msg.obj).get(0);
				codes = (Integer[]) ((ArrayList) msg.obj).get(1);
				initSearchView();
			}
		}
	};
	
	private Runnable r = new Runnable() {
		@Override
		public void run() {
			ArrayList<CityEntity> citys = (ArrayList<CityEntity>) db.queryAllCitys();
			Log.d("hekeyi", "initSearchView  citys.length = " + citys.size());
			String[] names = new String[citys.size()];
			Integer[] codes = new Integer[citys.size()];
			for (int i = 0; i < citys.size(); i++) {
				names[i] = citys.get(i).getName();
				codes[i] = citys.get(i).getcCode();
			}
			Message msg = new Message();
			msg.what = 1000;
			ArrayList al = new ArrayList();
			al.add(names);
			al.add(codes);
			msg.obj = al;
			mHandler.sendMessage(msg);
		}
	};*/
	
	private void initSearchView() {
		if (DBManager.isUser) {
			return;
		}
//		ArrayList<CityEntity> citys = (ArrayList<CityEntity>) db.queryAllCitys();
//		names = new String[citys.size()];
//		codes = new Integer[citys.size()];
//		for (int i = 0; i < citys.size(); i++) {
//			names[i] = citys.get(i).getName();
//			codes[i] = citys.get(i).getcCode();
//		}

		// 实例化汉字转拼音类
		characterParser = CharacterParser.getInstance();

//		pinyinComparator = new PinyinComparator();
		// dialog = (TextView) findViewById(R.id.dialog);
//		SourceDateList = filledData(names,codes);

		sortListView = (ListView) findViewById(R.id.country_lvcountry);
		sortListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// 这里要利用adapter.getItem(position)来获取当前position所对应的对象
				Toast.makeText(getApplication(), ((SortModel) adapter.getItem(position)).getName(), Toast.LENGTH_SHORT).show();
				storeSelectedCity(Integer.valueOf(filterDateList.get(position).getCode()),filterDateList.get(position).getName());
//				afterSelectCity();
			}
		});

		// 根据a-z进行排序源数据
//		Collections.sort(CityUtil.SourceDateList, pinyinComparator);
		adapter = new SortAdapter(this, CityUtil.SourceDateList);
		sortListView.setAdapter(adapter);

		/********************/
		/*fs[0] = new SortModel[50];
		fs[1] = new SortModel[92];
		fs[2] = new SortModel[112];
		fs[3] = new SortModel[137];
		fs[4] = new SortModel[17];
		fs[5] = new SortModel[87];
		fs[6] = new SortModel[94];
		fs[7] = new SortModel[194];
		fs[8] = new SortModel[10];
		fs[9] = new SortModel[148];
		fs[10] = new SortModel[38];
		fs[11] = new SortModel[217];
		fs[12] = new SortModel[83];
		fs[13] = new SortModel[84];
		fs[14] = new SortModel[10];
		fs[15] = new SortModel[78];
		fs[16] = new SortModel[85];
		fs[17] = new SortModel[34];
		fs[18] = new SortModel[159];
		fs[19] = new SortModel[124];
		fs[20] = new SortModel[10];
		fs[21] = new SortModel[10];
		fs[22] = new SortModel[130];
		fs[23] = new SortModel[163];
		fs[24] = new SortModel[193];
		fs[25] = new SortModel[267];
		filterSource(SourceDateList);
		Log.d("search","fs[0][0]   = "+fs[0][0]);*/
		/********************/
		
//		mClearEditText = (ClearEditText) findViewById(R.id.filter_edit);		
		mClearEditText = (EditText) findViewById(R.id.filter_edit);
		
//		mClearEditText.clearFocus();
		mClearEditText.setCursorVisible(false);
		mClearEditText.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				mClearEditText.setCursorVisible(true);
				mClearEditText.requestFocus();			
				mClearEditText.setHint("");
//				mClearEditText.setPadding(30, 0, 0, 0);
			}
		});
		
		// 根据输入框输入值的改变来过滤搜索
		mClearEditText.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// 当输入框里面的值为空，更新为原来的列表，否则为过滤数据列表
//				filterData(s.toString());
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				sortListView.setVisibility(View.VISIBLE);
			}

			@Override
			public void afterTextChanged(Editable s) {
				filterData(s.toString());
			}
		});
	}

	private void getHotCityData() {
		ArrayList<CityEntity> citys = (ArrayList<CityEntity>) db.queryHotCity();
		hotCityList.clear();
		hotCitycCodeList.clear();
		for (int i = 0; i < citys.size(); i++) {
			hotCityList.add(citys.get(i).getName());
			hotCitycCodeList.add(citys.get(i).getcCode());
		}
		hotCityAdapter.notifyDataSetChanged();
	}

	private void getProvincesData() {
		ArrayList<CityEntity> citys = (ArrayList<CityEntity>) db.queryProvinces();
		provList.clear();
		provIdList.clear();
		for (int i = 0; i < citys.size(); i++) {
			provList.add(citys.get(i).getName());
			provIdList.add(citys.get(i).getId());
			// provTypeList.add(citys.get(i).getType());
		}
		provGridAdapter.notifyDataSetChanged();
	}

	private void getCitysData(int dCode) {
		ArrayList<CityEntity> citys = (ArrayList<CityEntity>) db.queryCity(dCode);
		cityList.clear();
		cityIdList.clear();
		citycCodeList.clear();
		// cityList = new ArrayList<String>();
		// cityIdList = new ArrayList<Integer>();
		// citycCodeList = new ArrayList<Integer>();
		for (int i = 0; i < citys.size(); i++) {
			cityList.add(citys.get(i).getName());
			cityIdList.add(citys.get(i).getId());
			citycCodeList.add(citys.get(i).getcCode());
		}
		cityGridAdapter.notifyDataSetChanged();
	}

	private void getCountysData(int cityId) {
		ArrayList<CityEntity> citys = (ArrayList<CityEntity>) db.queryCity(cityId);
		countyList.clear();
		countycCodeList.clear();
		for (int i = 0; i < citys.size(); i++) {
			countyList.add(citys.get(i).getName());
			countycCodeList.add(citys.get(i).getcCode());
		}
		countyGridAdapter.notifyDataSetChanged();
	}

	OnItemClickListener hotCityItemSelectListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			selectCityCode(hotCitycCodeList.get(position));
			storeSelectedCity(hotCitycCodeList.get(position), hotCityList.get(position));
			/*if (thread.isAlive()) {
				thread.interrupt();
				thread.destroy();
			}*/
//			if (!thread.isAlive()) {
//				afterSelectCity();
//			}
		}
	};

	OnItemClickListener provinceItemSelectListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			getCitysData(provIdList.get(position));
			cityGrid.setVisibility(View.VISIBLE);
			hotCityGrid.setVisibility(View.GONE);
			provGrid.setVisibility(View.GONE);
			// mSearchView.setVisibility(View.GONE);
			// searchLayout.setVisibility(View.GONE);
			hotCityTv.setVisibility(View.GONE);
			// cityTv.setVisibility(View.GONE);
			cityTv.setText("选择城市");
			backFlag = 1;
		}
	};

	OnItemClickListener cityItemSelectListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			backFlag = 2;
			if (citycCodeList.get(position) != 0) {
				selectCityCode(citycCodeList.get(position));
				storeSelectedCity(citycCodeList.get(position), cityList.get(position));
//				afterSelectCity();
			} else {
				getCountysData(cityIdList.get(position));
				cityGrid.setVisibility(View.GONE);
				countyGrid.setVisibility(View.VISIBLE);
			}
		}
	};

	private OnItemClickListener countyItemSelectListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			selectCityCode(countycCodeList.get(position));
			storeSelectedCity(countycCodeList.get(position), countyList.get(position));
//			afterSelectCity();
		}
	};

	public int selectCityCode(int cCode) {
		return cCode;
	}

//	int j;
	private void storeSelectedCity(int cCode, String cityName) {
		SharedPreferences citySharePreferences = getSharedPreferences(ISPCallBack.SHARED_PREFERENCES_FILE_NAME,
				Activity.MODE_PRIVATE);
		SharedPreferences.Editor editor = citySharePreferences.edit();
		int cityNum = citySharePreferences.getInt(ISPCallBack.SP_CITY_NUM, 1);
		int j = 0;
		boolean isSame = false;
		while (j < cityNum) {
			int cityPostal2 = citySharePreferences.getInt(ISPCallBack.SP_CITY_CODE + j, -1);
			if (cCode == cityPostal2) {
				isSame = true;
				Toast.makeText(getApplicationContext(), getResources().getString(R.string.repeate_select), Toast.LENGTH_SHORT).show();
				break;
			}
			j++;
		}
		if (!isSame) {
			j = j + 1;
			editor.putInt(ISPCallBack.SP_CITY_NUM, cityNum + 1);
			editor.putInt(ISPCallBack.SP_CITY_CODE + (j - 1), cCode);
			editor.putString(ISPCallBack.SP_CITY_NAME + (j - 1), cityName);
			editor.putInt(ISPCallBack.SP_CITY_FLAG + (j - 1), 0);
			editor.commit();
		}
		
		Intent intent = new Intent(this, WeatherHomeActivity.class);
		intent.putExtra("selectCity", j);
		startActivity(intent);
	}
	
	OnClickListener backOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			backKey();
		}
	};

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			// do something...
			backKey();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	private void backKey() {
		Log.d("back","backFlag = "+backFlag);
		if (backFlag == 0) {
			afterSelectCity();
		} else if (backFlag == 1) {
			cityGrid.setVisibility(View.GONE);
			countyGrid.setVisibility(View.GONE);
			hotCityGrid.setVisibility(View.VISIBLE);
			provGrid.setVisibility(View.VISIBLE);
			// mSearchView.setVisibility(View.VISIBLE);
			// searchLayout.setVisibility(View.VISIBLE);
			hotCityTv.setVisibility(View.VISIBLE);
			// cityTv.setVisibility(View.VISIBLE);
			backFlag = 0;
		} else if (backFlag == 2) {
			countyGrid.setVisibility(View.GONE);
			cityGrid.setVisibility(View.VISIBLE);
			backFlag = 1;
		}
	}
	
	private void afterSelectCity(){
		CitySelectActivity.this.finish();
//		Intent intent = new Intent(this, WeatherHomeActivity.class);
//		intent.putExtra("selectCity", j);
//		startActivity(intent);
	}
	
	List<SortModel> filterDateList;
	
	/**
	 * 根据输入框中的值来过滤数据并更新ListView
	 * @param filterStr
	 */
	private void filterData(String filterStr) {
		filterDateList = new ArrayList<SortModel>();
		if (TextUtils.isEmpty(filterStr)) {
			filterDateList = CityUtil.SourceDateList;
		} else {
			filterDateList.clear();
			/*for (SortModel sortModel : SourceDateList) {
				String name = sortModel.getName();
				if (name.indexOf(filterStr.toString()) != -1 || characterParser.getSelling(name).startsWith(filterStr.toString())) {
					filterDateList.add(sortModel);
				}
			}*/
			
			// 汉字转换成拼音
			String pinyin = characterParser.getSelling(filterStr);
			char c = pinyin.substring(0, 1).toUpperCase(Locale.getDefault()).charAt(0);
			switch (c) {
			case 'A':
				searchCityPerNum(filterStr, 0);
				break;
			case 'B':
				searchCityPerNum(filterStr, 1);
				break;
			case 'C':
				searchCityPerNum(filterStr, 2);
				break;
			case 'D':
				searchCityPerNum(filterStr, 3);
				break;
			case 'E':
				searchCityPerNum(filterStr, 4);
				break;
			case 'F':
				searchCityPerNum(filterStr, 5);
				break;
			case 'G':
				searchCityPerNum(filterStr, 6);
				break;
			case 'H':
				searchCityPerNum(filterStr, 7);
				break;
			case 'I':
				searchCityPerNum(filterStr, 8);
				break;
			case 'J':
				searchCityPerNum(filterStr, 9);
				break;
			case 'K':
				searchCityPerNum(filterStr, 10);
				break;
			case 'L':
				searchCityPerNum(filterStr, 11);
				break;
			case 'M':
				searchCityPerNum(filterStr, 12);
				break;
			case 'N':
				searchCityPerNum(filterStr, 13);
				break;
			case 'O':
				searchCityPerNum(filterStr, 14);
				break;
			case 'P':
				searchCityPerNum(filterStr, 15);
				break;
			case 'Q':
				searchCityPerNum(filterStr, 16);
				break;
			case 'R':
				searchCityPerNum(filterStr, 17);
				break;
			case 'S':
				searchCityPerNum(filterStr, 18);
				break;
			case 'T':
				searchCityPerNum(filterStr, 19);
				break;
			case 'U':
				searchCityPerNum(filterStr, 20);
				break;
			case 'V':
				searchCityPerNum(filterStr, 21);
				break;
			case 'W':
				searchCityPerNum(filterStr, 22);
				break;
			case 'X':
				searchCityPerNum(filterStr, 23);
				break;
			case 'Y':
				searchCityPerNum(filterStr, 24);
				break;
			case 'Z':
				searchCityPerNum(filterStr, 25);
				break;
			}				
		}
		// 根据a-z进行排序
//		Collections.sort(filterDateList, pinyinComparator);
		adapter.updateListView(filterDateList);
	}
	
	private void searchCityPerNum(String filterStr, int i) {
		if(CityUtil.fs[i] == null) return;   //2015.09.02
		for (SortModel sortModel : CityUtil.fs[i]) {
			if (null != sortModel) {
				String name = sortModel.getName();
				if (name.indexOf(filterStr.toString()) != -1 || characterParser.getSelling(name).startsWith(filterStr.toString())) {
					//System.out.println("1111111111111111111111111111111111111111111111111  " + name + " , PY : " + characterParser.getSelling(name));
					filterDateList.add(sortModel);
				}
			}
		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();	

	}
	
	
/*	private SortModel fs[][] = new SortModel[26][];
	private int k0 = 0, k1=0, k2=0, k3=0, k4=0, k5=0, k6=0, k7=0, k8=0, k9=0, k10=0,
			k11=0, k12=0, k13=0, k14=0, k15=0, k16=0, k17=0, k18=0, k19=0, k20=0,
			k21=0, k22=0, k23=0, k24=0, k25=0;
	private void filterSource(List<SortModel> SourceDateList){
		int i = 0;
		for(SortModel sortModel : SourceDateList){
			String name = sortModel.getName();
			// 汉字转换成拼音
			String pinyin = characterParser.getSelling(name);
			String sortString = pinyin.substring(0, 1).toUpperCase(Locale.getDefault());
			char c = sortString.charAt(0);
			switch (c){
			case 'A':
//				fs[0] = new SortModel[50];
				fs[0][k0] = sortModel;
				k0++;
				break;
			case 'B':
//				fs[1] = new SortModel[92];
				fs[1][k1] = sortModel;
				k1++;
				break;
			case 'C':
//				fs[2] = new SortModel[112];
				fs[2][k2] = sortModel;
				k2++;
				break;
			case 'D':
//				fs[3] = new SortModel[137];
				fs[3][k3] = sortModel;
				k3++;
				break;
			case 'E':
//				fs[4] = new SortModel[17];
				fs[4][k4] = sortModel;
				k4++;
			break;
				case 'F':
//				fs[5] = new SortModel[87];
				fs[5][k5] = sortModel;
				k5++;
				break;
			case 'G':
//				fs[6] = new SortModel[94];
				fs[6][k6] = sortModel;
				k6++;
				break;
			case 'H':
//				fs[7] = new SortModel[194];
				fs[7][k7] = sortModel;
				k7++;
				break;
			case 'I':
//				fs[8] = new SortModel[10];
				fs[8][k8] = sortModel;
				k8++;
				break;
			case 'J':
//				fs[9] = new SortModel[148];
				fs[9][k9] = sortModel;
				k9++;
				break;
			case 'K':
//				fs[10] = new SortModel[38];
				fs[10][k10] = sortModel;
				k10++;
				break;
			case 'L':
//				fs[11] = new SortModel[217];
				fs[11][k11] = sortModel;
				k11++;
				break;
			case 'M':
//				fs[12] = new SortModel[83];
				fs[12][k12] = sortModel;
				k12++;
				break;
			case 'N':
//				fs[13] = new SortModel[84];
				fs[13][k13] = sortModel;
				k13++;
				break;
			case 'O':
//				fs[14] = new SortModel[10];
				fs[14][k14] = sortModel;
				k14++;
				break;
			case 'P':
//				fs[15] = new SortModel[78];
				fs[15][k15] = sortModel;
				k15++;
				break;
			case 'Q':
//				fs[16] = new SortModel[85];
				fs[16][k16] = sortModel;
				k16++;
				break;
			case 'R':
//				fs[17] = new SortModel[34];
				fs[17][k17] = sortModel;
				k17++;
				break;
			case 'S':
//				fs[18] = new SortModel[159];
				fs[18][k18] = sortModel;
				k18++;
				break;
			case 'T':
//				fs[19] = new SortModel[124];
				fs[19][k19] = sortModel;
				k19++;
				break;
			case 'U':
//				fs[20] = new SortModel[10];
				fs[20][k20] = sortModel;
				k20++;
				break;
			case 'V':
//				fs[21] = new SortModel[10];
				fs[21][k21] = sortModel;
				k21++;
				break;
			case 'W':
//				fs[22] = new SortModel[130];
				fs[22][k22] = sortModel;
				k22++;
				break;
			case 'X':
//				fs[23] = new SortModel[163];
				fs[23][k23] = sortModel;
				k23++;
				break;
			case 'Y':
//				fs[24] = new SortModel[193];
				fs[24][k24] = sortModel;
				k24++;
				break;
			case 'Z':
//				fs[25] = new SortModel[267];
				fs[25][k25] = sortModel;
				k25++;
				break;
			}
		}
	}*/


	/**
	 * 为ListView填充数据
	 * @param date
	 * @return
	 */
	/*private List<SortModel> filledData(String[] date,Integer[] code) {
		List<SortModel> mSortList = new ArrayList<SortModel>();
		for (int i = 0; i < date.length; i++) {
			SortModel sortModel = new SortModel();
			sortModel.setName(date[i]);
			sortModel.setCode(code[i]);
			// 汉字转换成拼音
			String pinyin = characterParser.getSelling(date[i]);
			String sortString = pinyin.substring(0, 1).toUpperCase(Locale.getDefault());
			// 正则表达式，判断首字母是否是英文字母
			if (sortString.matches("[A-Z]")) {
				sortModel.setSortLetters(sortString.toUpperCase(Locale.getDefault()));
			} else {
				sortModel.setSortLetters("#");
			}
			mSortList.add(sortModel);
		}
		return mSortList;
	}*/
	
}
