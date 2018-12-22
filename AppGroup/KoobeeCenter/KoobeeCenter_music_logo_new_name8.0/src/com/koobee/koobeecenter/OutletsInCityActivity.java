package com.koobee.koobeecenter;

import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

import org.w3c.dom.Text;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.koobee.koobeecenter.db.AsyncBase;
import com.koobee.koobeecenter.db.outlets.AsyncOutletsDatabase;
import com.koobee.koobeecenter.db.outlets.CustomerTable;
import com.koobee.koobeecenter02.R;

public class OutletsInCityActivity extends Activity implements
		AsyncBase.OnDataAvailable {
	public static final String PARAM_PROVINCE = "p_province";
	public static final String PARAM_AREA = "p_area";
	private ListView mContentLsw;
	private int expandPos = -1;
	private String telePhoneNum;

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Window window = getWindow();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			window = getWindow();
			window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
					| WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
			window.getDecorView().setSystemUiVisibility(
					View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
							| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
							| View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
			window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
			window.setStatusBarColor(getResources().getColor(
					R.color.status_color));
			// window.setNavigationBarColor(Color.TRANSPARENT);
		}

		// StateBarUtils.initStateBar(this);
		setContentView(R.layout.activity_outlets_in_city);
		initUI();

	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	private void initUI() {
		mContentLsw = (ListView) findViewById(R.id.content_lsw);
		mContentLsw
				.setOnItemClickListener(new AdapterView.OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						if (expandPos == position)
							expandPos = -1;
						else
							expandPos = position;
						int totalHeight = 0;
						BaseAdapter mAdapter = (BaseAdapter) parent
								.getAdapter();
						for (int i = 0; i < mAdapter.getCount(); i++) {
							View viewItem = mAdapter.getView(i, null, parent);
							viewItem.measure(0, 0);
							totalHeight += viewItem.getMeasuredHeight();
						}

						ViewGroup.LayoutParams params = mContentLsw
								.getLayoutParams();
						params.height = totalHeight
								+ (mContentLsw.getDividerHeight() * (mContentLsw
										.getCount() - 1));
						mContentLsw.setLayoutParams(params);
						mAdapter.notifyDataSetChanged();
					}
				});
		TextView titleText = (TextView) findViewById(R.id.title_text);
		Bundle bun = getIntent().getExtras();
		String province = bun.getString(PARAM_PROVINCE);
		String area = bun.getString(PARAM_AREA);
		titleText.setText(province + "\t" + area);
		AsyncOutletsDatabase.getInstance().queryByArea(
				AsyncOutletsDatabase.Q_BYAREA, area, this);
	}

	public void back_clk(View v) {
		finish();
	}

	@Override
	public void onDataBack(int id, Object object) {
		if (id == AsyncOutletsDatabase.Q_BYAREA) {
			List<CustomerTable.Info> list = (List<CustomerTable.Info>) object;
			if (list != null) {
				mContentLsw.setAdapter(new MAdapter(list));
			}
		}
	}

	private class MAdapter extends BaseAdapter {
		private List<CustomerTable.Info> mDatas;

		MAdapter(List<CustomerTable.Info> list) {
			mDatas = list;
		}

		@Override
		public int getCount() {
			if (mDatas == null)
				return 0;
			return mDatas.size();
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
		public View getView(final int position, View convertView,
				ViewGroup parent) {
			ViewHolder holder = null;
			if (convertView == null) {
				convertView = getLayoutInflater().inflate(
						R.layout.cityoutlets_item, null);
				holder = new ViewHolder();
				holder.companyText = (TextView) convertView
						.findViewById(R.id.company_text);
				holder.addressText = (TextView) convertView
						.findViewById(R.id.address_text);
				holder.phoneText = (TextView) convertView
						.findViewById(R.id.phone_text);
				holder.expandView = convertView
						.findViewById(R.id.expand_layout);
				holder.arrowImage = (ImageView) convertView
						.findViewById(R.id.expand_image);
				holder.companyView = convertView
						.findViewById(R.id.company_layout);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			CustomerTable.Info info = mDatas.get(position);
			holder.companyText.setText(info.company);
			holder.phoneText.setText(info.tel.trim());
			holder.phoneText.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG); // 下划线
			holder.phoneText.getPaint().setAntiAlias(true);// 抗锯齿
			holder.addressText.setText(info.address);

			if (expandPos == position) {
				holder.expandView.setVisibility(View.VISIBLE);
				holder.arrowImage.setImageResource(R.drawable.arrow_up);
				holder.companyView
						.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								mContentLsw.performItemClick(null, position, 0);
							}
						});
			} else {
				holder.expandView.setVisibility(View.GONE);
				holder.arrowImage.setImageResource(R.drawable.arrow_down);
			}

			holder.phoneText.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					String content = ((TextView) v).getText().toString();
					if (TextUtils.isEmpty(content)) {
						return;
					}
					String param = ";";
					final String[] items;
					if (content.contains(";")) {

						items = content.split(param);
						disPlayDialog(items);
						return;
					} else if (content.contains("；")) {
						param = "；";
						items = content.split(param);
						disPlayDialog(items);
						return;
					} else {
						dialTel(content);
					}
				}

			});

			return convertView;
		}

		private class ViewHolder {
			TextView companyText, addressText, phoneText;
			ImageView arrowImage;
			View expandView, companyView;
		}
	}

	@Override
	public void finish() {
		super.finish();
		overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
	}

	/**
	 * 启动打电话界面
	 * 
	 * @param number
	 * @return void
	 * @see
	 */
	private void dialTel(String number) {
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_DIAL);
		intent.setData(Uri.parse("tel:" + number));
		startActivity(intent);
	}

	private void disPlayDialog(final String[] items) {
		AlertDialog.Builder adb = new AlertDialog.Builder(this);
		adb.setTitle(getString(R.string.pl_select_number));
		adb.setCancelable(true);
		telePhoneNum = items[0];
		adb.setSingleChoiceItems(items, 0,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						telePhoneNum = items[which];
					}
				});
		adb.setPositiveButton(getString(R.string.sure),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialTel(telePhoneNum);
					}
				});
		adb.setNegativeButton(getString(R.string.cancel), null);
		adb.create().show();

	}
}
