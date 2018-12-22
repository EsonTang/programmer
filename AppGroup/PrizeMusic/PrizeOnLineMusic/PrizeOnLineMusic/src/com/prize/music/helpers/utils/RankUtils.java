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

package com.prize.music.helpers.utils;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.prize.app.BaseApplication;
import com.prize.music.R;

import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * 
 **
 * 处理替换排行榜的字和背景图
 * 
 * @author longbaoxiu
 * @version V1.0
 */
public class RankUtils {
/**
 * 
 * 处理替换排行榜item的字和背景图
 * @param imag
 * @param textView
 * @param param
 * @return boolean   返回true，表示已经处理了
 * @see
 */
	public static boolean setImagAndText(ImageView imag, TextView textView,
			String param) {
		if (TextUtils.isEmpty(param))
			return true;
		if (param.equals(BaseApplication.curContext
				.getString(R.string.xiami_new_songs_rank))) {
			textView.setText(R.string.new_songs_rank);
			 imag.setImageResource(R.drawable.icon_xiami_new_songs_rank);
			return true;
		}
		if (param.equals(BaseApplication.curContext
				.getString(R.string.xiami_songs_rank))) {
			textView.setText(R.string.hot_songs_rank);
			 imag.setImageResource(R.drawable.icon_xiami_songs_rank);
			return true;
		}
		if (param.equals(BaseApplication.curContext
				.getString(R.string.xiami_original_songs_rank))) {
			textView.setText(R.string.original_songs_rank);
			 imag.setImageResource(R.drawable.icon_xiami_original_songs_rank);
			return true;
		}
		if (param.equals(BaseApplication.curContext
				.getString(R.string.musicer_demo_rank))||param.equals(BaseApplication.curContext
						.getString(R.string.xiami_musicer_demo_rank))) {
			textView.setText(R.string.musicer_demo_rank);
			 imag.setImageResource(R.drawable.icon_xiami_demo_rank);
			return true;
		}
		if (param.equals(BaseApplication.curContext
				.getString(R.string.daxia_listner_rank))) {
			textView.setText(param);
			 imag.setImageResource(R.drawable.icon_daxia_listner_rank);
			return true;
		}
		if (param.equals(BaseApplication.curContext
				.getString(R.string.collect_sort_rank))) {
			textView.setText(param);
			 imag.setImageResource(R.drawable.icon_collect_sort_rank);
			return true;
		}
		if (param.equals(BaseApplication.curContext
				.getString(R.string.KTV_H_rank))) {
			textView.setText(param);
			 imag.setImageResource(R.drawable.icon_xiami_ktv_h_rank);
			return true;
		}
		
		return false;
	}

	/**
	 * 
	 * 处理替换排行榜详情的字和背景图
	 * 
	 * @param imag
	 * @param textView
	 * @param param
	 *            标题
	 * *@return boolean   返回true，表示已经处理了
	 */
	public static boolean setRankDeatailImagAndText(ImageView imag,
			TextView textView, String param) {
		if (TextUtils.isEmpty(param))
			return true;
		if (param.equals(BaseApplication.curContext
				.getString(R.string.xiami_new_songs_rank))) {
			textView.setText(R.string.new_songs_rank);
			 imag.setImageResource(R.drawable.icon_xiami_new_songs_rank_detail);
			return true;
		}
		if (param.equals(BaseApplication.curContext
				.getString(R.string.xiami_songs_rank))) {
			textView.setText(R.string.hot_songs_rank);
			 imag.setImageResource(R.drawable.icon_xiami_songs_rank_detail);
			return true;
		}
		if (param.equals(BaseApplication.curContext
				.getString(R.string.xiami_original_songs_rank))) {
			textView.setText(R.string.original_songs_rank);
			 imag.setImageResource(R.drawable.icon_xiami_original_songs_rank_detail);
			return true;
		}
		if (param.equalsIgnoreCase(BaseApplication.curContext
				.getString(R.string.musicer_demo_rank))||param.equalsIgnoreCase(BaseApplication.curContext
						.getString(R.string.xiami_musicer_demo_rank))) {
			textView.setText(R.string.musicer_demo_rank);
			 imag.setImageResource(R.drawable.icon_xiami_demo_rank_detail);
			return true;
		}
		if (param.equals(BaseApplication.curContext
				.getString(R.string.daxia_listner_rank))) {
			textView.setText(param);
			 imag.setImageResource(R.drawable.icon_daxia_listner_rank_detail);
			return true;
		}
		if (param.equals(BaseApplication.curContext
				.getString(R.string.collect_sort_rank))) {
			textView.setText(param);
			 imag.setImageResource(R.drawable.icon_collect_sort_rank_detail);
			return true;
		}
		if (param.equals(BaseApplication.curContext
				.getString(R.string.KTV_H_rank))) {
			textView.setText(param);
			 imag.setImageResource(R.drawable.icon_xiami_ktv_h_rank_detail);
			return true;
		}
		
		return false;
	}
}
