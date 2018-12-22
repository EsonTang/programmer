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

package com.prize.prizethemecenter.ui.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.prize.prizethemecenter.MainApplication;
import com.prize.prizethemecenter.R;
import com.prize.prizethemecenter.bean.HomeAdBean;
import com.prize.prizethemecenter.ui.utils.UILimageUtil;
import com.prize.prizethemecenter.ui.widget.CornerImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * 壁纸、字体头部适配器
 * @author pengy
 */
public class WallpaperFontHeadAdapter extends BaseAdapter {
	private List<HomeAdBean> items = new ArrayList<HomeAdBean>();;
	private Context context;

	public WallpaperFontHeadAdapter(Context activity) {
		this.context = activity;
	}

	public void setData(List<HomeAdBean> data) {
		if (data != null) {
			items = data;
		}
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return items == null ? 0 : items.size();
	}

	@Override
	public HomeAdBean getItem(int position) {
		if (items == null)
			return null;
		return items.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
    @Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;
		HomeAdBean item = items.get(position);
		if (null == convertView) {
			convertView = LayoutInflater.from(MainApplication.curContext)
					.inflate(R.layout.item_wallpaper_font_layout, null);
			holder = new ViewHolder();
			holder.wallpaper_font_logo = (CornerImageView) convertView
					.findViewById(R.id.wallpaper_font_logo);
			convertView.setTag(holder);
			convertView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		String url = !TextUtils.isEmpty(item.bigImageUrl) ? item.bigImageUrl
				: null;
		ImageLoader imageLoader = ImageLoader.getInstance();
		imageLoader.setRotate(false);
		imageLoader.init(ImageLoaderConfiguration.createDefault(context));
		String tag = (String) holder.wallpaper_font_logo.getTag();
		if(tag==null||!tag.equals(item.bigImageUrl)){
			imageLoader.displayImage(url, holder.wallpaper_font_logo,
					UILimageUtil.getWallpaperFontLoptions(), UILimageUtil.setTagHolder(holder.wallpaper_font_logo,item.bigImageUrl));
		}
		return convertView;
	}

	private class ViewHolder {
		CornerImageView wallpaper_font_logo;
	}
}
