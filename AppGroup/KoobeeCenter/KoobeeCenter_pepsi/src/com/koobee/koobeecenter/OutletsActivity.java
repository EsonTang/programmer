package com.koobee.koobeecenter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.koobee.koobeecenter.db.AsyncBase;
import com.koobee.koobeecenter.db.outlets.AsyncOutletsDatabase;
import com.koobee.koobeecenter.db.outlets.OutletsDatabase;
import com.koobee.koobeecenter.widget.OutletsRowView;
import com.koobee.koobeecenter02.R;

public class OutletsActivity extends Activity implements
		AsyncBase.OnDataAvailable {
	private ListView mContentLsw;
	private Map<String, Set<String>> mCityMap;

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Window window = getWindow();
		window.requestFeature(Window.FEATURE_NO_TITLE);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			window = getWindow();
			window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
					| WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
			window.getDecorView().setSystemUiVisibility(
					View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
							| View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
			// | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
			window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
			window.setStatusBarColor(getResources().getColor(
					R.color.status_color));
			// window.setNavigationBarColor(Color.TRANSPARENT);
		}
		setContentView(R.layout.outlets_main);
		if (!OutletsDatabase.getInstance().isDatabaseExist()) {
			new CopydbTask().execute();
		} else {
			requestCitys();
		}

		initUI();
	}

	private void initUI() {
		TextView titleText = (TextView) findViewById(R.id.title_text);
		titleText.setText(R.string.alloutlets);
		mContentLsw = (ListView) findViewById(R.id.content_lsw);
	}

	private void requestCitys() {
		AsyncOutletsDatabase.getInstance().queryCityes(
				AsyncOutletsDatabase.Q_CITYS, this);
	}

	@Override
	public void onDataBack(int id, Object object) {
		if (id == AsyncOutletsDatabase.Q_CITYS) {
			mCityMap = (Map<String, Set<String>>) object;
			if (mCityMap != null) {
				mContentLsw.setAdapter(new OutletsAdapter(mCityMap.keySet()
						.toArray(new String[] {})));
			}
		}
	}

	private class OutletsAdapter extends BaseAdapter {
		private String[] mKeys;

		OutletsAdapter(String[] strs) {
			mKeys = strs;
		}

		@Override
		public int getCount() {
			if (mKeys == null)
				return 0;
			return mKeys.length;
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			if (convertView == null) {
				holder = new ViewHolder();
				OutletsRowView item = new OutletsRowView(OutletsActivity.this);
				convertView = item;
				holder.rowView = item;
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			final String key = mKeys[position];
			holder.rowView.setTitle(key);
			{
				Set<String> set = mCityMap.get(key);
				List<String> list = new LinkedList<String>();
				list.addAll(set);
				GridAdapter gridAdapter = (GridAdapter) holder.rowView
						.getAdapter();
				if (gridAdapter != null) {
					gridAdapter.setDatas(list);
					gridAdapter.notifyDataSetChanged();
				} else {
					gridAdapter = new GridAdapter();
					gridAdapter.setDatas(list);
					holder.rowView.setAdapter(gridAdapter);
				}
				holder.rowView
						.setOnItemClickListener(new AdapterView.OnItemClickListener() {
							@Override
							public void onItemClick(AdapterView<?> parent,
									View view, int position, long id) {
								Intent it = new Intent(OutletsActivity.this,
										OutletsInCityActivity.class);
								Bundle bun = new Bundle();
								bun.putString(
										OutletsInCityActivity.PARAM_PROVINCE,
										key);
								bun.putString(OutletsInCityActivity.PARAM_AREA,
										(String) parent
												.getItemAtPosition(position));
								it.putExtras(bun);
								startActivity(it);
								overridePendingTransition(R.anim.in_from_right,
										R.anim.out_to_left);

							}
						});
			}
			return convertView;
		}

		private class ViewHolder {
			OutletsRowView rowView;
		}

		private class GridAdapter extends BaseAdapter {
			private List<String> mList;

			void setDatas(List<String> list) {
				mList = list;
			}

			private class GridHolder {
				TextView textView;
			}

			@Override
			public int getCount() {
				if (mList == null)
					return 0;
				return mList.size();
			}

			@Override
			public Object getItem(int position) {
				return mList.get(position);
			}

			@Override
			public long getItemId(int position) {
				return 0;
			}

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				GridHolder holder = null;
				if (convertView == null) {
					convertView = getLayoutInflater().inflate(
							R.layout.outlets_text, null);
					holder = new GridHolder();
					holder.textView = (TextView) convertView;
					convertView.setTag(holder);
					AbsListView.LayoutParams param = new AbsListView.LayoutParams(
							ViewGroup.LayoutParams.MATCH_PARENT,
							(int) dipToPixels(convertView.getContext(), 31));
					convertView.setLayoutParams(param);
				} else {
					holder = (GridHolder) convertView.getTag();
				}
				String text = mList.get(position);
				holder.textView.setText(text);
				return convertView;
			}
		}
	}

	public static float dipToPixels(Context context, float dipValue) {
		DisplayMetrics metrics = context.getResources().getDisplayMetrics();
		return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue,
				metrics);
	}

	private class CopydbTask extends AsyncTask<Void, Void, Boolean> {

		@Override
		protected Boolean doInBackground(Void... params) {
			boolean copyOk = true;
			OutletsDatabase db = OutletsDatabase.getInstance();
			if (!db.isDatabaseExist())
				copyOk = copyDatabaseFile("user.db", db.getDatabaseFolder()
						+ db.getDatabaseFileName());
			return copyOk;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if (result) {
				requestCitys();
			} else {
				Toast.makeText(getApplicationContext(), "db bad.",
						Toast.LENGTH_SHORT).show();
			}
		}
	}

	/**
	 * 复制数assets中数据库到内存卡
	 * 
	 * @param fileName
	 *            assets文件夹中的数据库名字
	 * @param fullPath
	 *            该文件在内存中的全路径
	 * @return boolean true 复制成功；否则失败
	 */
	private boolean copyDatabaseFile(String fileName, String fullPath) {
		File file = new File(fullPath);
		if (file.exists()) {
			if (!file.delete()) {
				return false;
			}
		}

		try {
			InputStream inputStream = getAssets().open(fileName);
			OutputStream outputStream = new FileOutputStream(file);
			byte[] buffer = new byte[4096];
			int length = 0;
			while ((length = inputStream.read(buffer)) > 0) {
				outputStream.write(buffer, 0, length);
			}
			inputStream.close();
			outputStream.close();

		} catch (IOException e) {
			return false;
		}
		return true;
	}

	public void back_clk(View v) {
		finish();
	}

	@Override
	public void finish() {

		super.finish();
		overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
	}

}
