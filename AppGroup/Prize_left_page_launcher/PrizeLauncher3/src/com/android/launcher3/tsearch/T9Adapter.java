package com.android.launcher3.tsearch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.android.launcher3.BubbleTextView;
import com.android.launcher3.IconCache;
import com.android.launcher3.R;
import com.android.launcher3.ShortcutInfo;
import com.android.launcher3.Utilities;

public class T9Adapter extends BaseAdapter {

	private LayoutInflater mInflater;
	private List<ShortcutInfo> mList;
	private String filterNum;

	public String getFilterNum() {
		return filterNum;
	}

	public void setFilterNum(String filterNum) {
		this.filterNum = filterNum;
	}

	private Context mContext;
	private int PAGE_SIZE = 12;

	public T9Adapter(Context context, List<ShortcutInfo> apps, int page) {
		mInflater = LayoutInflater.from(context);
		this.mList = new ArrayList<ShortcutInfo>();
		this.mContext = context;
		int i = page * PAGE_SIZE;
		int end = i + PAGE_SIZE;
		while ((i < apps.size()) && (i < end)) {
			mList.add(apps.get(i));
			i++;
		}

	}

	private SearchView mSearchView;

	public SearchView getSearchView() {
		return mSearchView;
	}

	public void setSearchView(SearchView mSearchView) {
		this.mSearchView = mSearchView;
	}

	public void setApps(List<ShortcutInfo> list) {
	}

	public void add(ShortcutInfo bean) {
		mList.add(bean);
	}

	public void remove(int position) {
		mList.remove(position);
	}

	public int getCount() {
		return mList.size();
	}

	IconCache mIconcache;

	public IconCache getIconcache() {
		return mIconcache;
	}

	public void setIconcache(IconCache mIconcache) {
		this.mIconcache = mIconcache;
	}

	public ShortcutInfo getItem(int position) {
		return mList.get(position);
	}

	public long getItemId(int position) {
		return 0;
	}

	public View getView(int position, View convertView, ViewGroup parent) {

		ViewHolder holder = null;
		final ShortcutInfo item = mList.get(position);
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.dialcontactitem, parent,
					false);
			convertView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
			holder = new ViewHolder();
			holder.icon = (BubbleTextView) convertView
					.findViewById(R.id.search_icon);
			holder.icon.setTag(item);
			holder.icon.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					mSearchView.onClick(v);
					
				}
			});
			holder.icon
					.setCompoundDrawablePadding((int) parent.getContext()
							.getResources()
							.getDimension(R.dimen.icon_drawable_padding));
			holder.pinyin = (TextView) convertView
					.findViewById(R.id.TvPinyinAddr);
			holder.number = (TextView) convertView.findViewById(R.id.TvNumAddr);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		String formattedNumber = item.getPinyin();
		String title = (String) item.title;
		HashMap<String, Character> hash = item.mNames;
		if (null != holder.icon) {
			holder.icon.applyFromShortcutInfo(item, mIconcache);
		}
		if (null == filterNum || "".equals(filterNum)) {
		} else {
			if (!TextUtils.isEmpty(filterNum)) {
				for (int i = 0; i < filterNum.length(); i++) {
					char c = filterNum.charAt(i);
					if (TextUtils.isDigitsOnly(String.valueOf(c))) {
						char[] zms = digit2Char(Integer.parseInt(c + ""));
						if (zms != null) {
							for (char c1 : zms) {

								for (String string : hash.keySet()) {
									for (int j = 0; j < string.length(); j++) {
										string = string.toUpperCase();
										String src = String.valueOf(c1)
												.toUpperCase();
										String dest = String.valueOf(
												string.charAt(j)).toUpperCase();
										if (src.equals(dest)) {
											String firstName = String.valueOf(
													hash.get(string))
													.toUpperCase();
											title = title.replaceAll(firstName,
													"%%" + firstName + "@@");
										}
									}
								}

							}
							title = title.replaceAll("%%",
									"<font color='#ec6104'>");
							title = title.replaceAll("@@", "</font>");
						}
					}
				}
				// holder.pinyin.setText(Html.fromHtml(formattedNumber));
				holder.icon.setText(Html.fromHtml(title));
			}
		}

		convertView.setTag(holder);
		/*ValueAnimator value = (ValueAnimator) ObjectAnimator.ofFloat(0f,1f);
		value.setDuration(2000);
		value.addUpdateListener(new AlphaChange(convertView));
		value.start();*/
		return convertView;
	}
	
	/*class AlphaChange  implements AnimatorUpdateListener  {
		View convertView;
		public AlphaChange(View convertView) {
			super();
			this.convertView = convertView;
		}
		@Override
		public void onAnimationUpdate(ValueAnimator arg0) {
			float parcent = (float) arg0.getAnimatedValue();
			convertView.setAlpha(parcent*255);
		}
		
	}*/

	public Drawable getIcon(ShortcutInfo info, IconCache iconCache) {
		Bitmap b = info.getIcon(iconCache);
		return Utilities.createIconDrawable(b);

	}

	public final class ViewHolder {
		public BubbleTextView icon;
		public TextView pinyin;
		public TextView number;
	}

	public char[] digit2Char(int digit) {
		char[] cs = null;
		switch (digit) {
		case 0:
			cs = new char[] {};
			break;
		case 1:
			break;
		case 2:
			cs = new char[] { 'a', 'b', 'c' };
			break;
		case 3:
			cs = new char[] { 'd', 'e', 'f' };
			break;
		case 4:
			cs = new char[] { 'g', 'h', 'i' };
			break;
		case 5:
			cs = new char[] { 'j', 'k', 'l' };
			break;
		case 6:
			cs = new char[] { 'm', 'n', 'o' };
			break;
		case 7:
			cs = new char[] { 'p', 'q', 'r', 's' };
			break;
		case 8:
			cs = new char[] { 't', 'u', 'v' };
			break;
		case 9:
			cs = new char[] { 'w', 'x', 'y', 'z' };
			break;
		}
		return cs;
	}

}
