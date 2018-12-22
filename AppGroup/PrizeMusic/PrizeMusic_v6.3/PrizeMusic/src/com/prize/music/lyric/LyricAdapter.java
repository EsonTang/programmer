/*****************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：歌词adapter
 *当前版本：V1.0
 *作  者：longbaoxiu
 *完成日期：2015-7-21
 *修改记录：
 *修改日期：
 *版 本 号：
 *修 改 人：
 *修改内容：
 ********************************************/
package com.prize.music.lyric;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.prize.music.R;

/**
 **
 * 歌词adapter
 * 
 * @author longbaoxiu
 * @version V1.0
 */
public class LyricAdapter extends BaseAdapter {

	/** 歌词句子集合 */
	List<LyricSentence> mLyricSentences = null;

	Context mContext = null;

	/** 当前的句子索引号 */
	int mIndexOfCurrentSentence = 0;

	// float mCurrentSize = 20;
	// float mNotCurrentSize = 17;

	public LyricAdapter(Context context) {
		mContext = context;
		mLyricSentences = new ArrayList<LyricSentence>();
		mIndexOfCurrentSentence = 0;
	}

	/** 设置歌词，由外部调用， */
	public void setLyric(List<LyricSentence> lyric) {
		mLyricSentences.clear();
		if (lyric != null) {
			mLyricSentences.addAll(lyric);
		}
		mIndexOfCurrentSentence = 0;
	}

	@Override
	public boolean isEmpty() {
		// 歌词为空时，让ListView显示EmptyView
		if (mLyricSentences == null) {
			return true;
		} else if (mLyricSentences.size() == 0) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean isEnabled(int position) {
		// 禁止在列表条目上点击
		return false;
	}

	@Override
	public int getCount() {
		return mLyricSentences.size();
	}

	@Override
	public Object getItem(int position) {
		return mLyricSentences.get(position).getContentText();
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if (convertView == null) {
			holder = new ViewHolder();
			LayoutInflater inflater = (LayoutInflater) mContext
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.lyric_line, null);
			holder.lyric_line = (TextView) convertView
					.findViewById(R.id.lyric_line_text);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		if (position >= 0 && position < mLyricSentences.size()) {
			holder.lyric_line.setText(mLyricSentences.get(position)
					.getContentText());
		}
		if (mIndexOfCurrentSentence == position) {
			// 当前播放到的句子设置为白色
			holder.lyric_line.setTextColor(Color.WHITE);
			// holder.lyric_line.setTextSize(mCurrentSize);
		} else {
			// 其他的句子设置为暗色
			holder.lyric_line.setTextColor(mContext.getResources().getColor(
					R.color.text_color_gray));
			// holder.lyric_line.setTextSize(mNotCurrentSize);
		}
		return convertView;
	}

	public void setCurrentSentenceIndex(int index) {
		mIndexOfCurrentSentence = index;
	}

	static class ViewHolder {
		TextView lyric_line;
	}
}
