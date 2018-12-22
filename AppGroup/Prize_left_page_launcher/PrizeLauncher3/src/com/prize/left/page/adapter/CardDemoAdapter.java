package com.prize.left.page.adapter;

import java.util.List;

import org.xutils.common.util.LogUtil;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.launcher3.R;
import com.prize.left.page.bean.DemoCardBean;
import com.prize.left.page.view.holder.AddedViewHolder;
import com.prize.left.page.view.holder.ImgViewHolder;
import com.prize.left.page.view.holder.UnSpcAddViewHolder;
import com.prize.left.page.view.holder.UnaddViewHolder;
/***
 * 示例recycleView适配器
 * @author fanjunchen
 *
 */
public class CardDemoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

	private List<DemoCardBean> cards;

	private Context mContext;
	
	private View.OnClickListener mClick;
	
	private Activity mAct;
	
	private String bigCode = null;
	
	public void setActivity(Activity m) {
		mAct = m;
	}
	
	public void setClickListener(View.OnClickListener c) {
		mClick = c;
	}

	public CardDemoAdapter(Context context, List<DemoCardBean> cards) {
		this.mContext = context;
		this.cards = cards;
	}
	
	public void onResume() {
	}
	
	public void onPause() {
	}
	/***
	 * 设置大类 编码, 用于决定展示哪张图片
	 * @param code
	 */
	public void setBigCode(String code) {
		bigCode = code;
	}
	
	private int getType() {
		if ("horizontal_message".equals(bigCode)) {
			return ImgViewHolder.TYPE_NEWS;
		}
		
		if ("vertical".equals(bigCode)) {
			return ImgViewHolder.TYPE_MOVIE;
		}
		
		if ("horizontal_goods".equals(bigCode)) {
			return ImgViewHolder.TYPE_GROUPON;
		}
		
		if ("navigation".equals(bigCode)) {
			return ImgViewHolder.TYPE_NAVI;
		}
		
		if ("common".equals(bigCode)) {
			return ImgViewHolder.TYPE_SUGG;
		}
		
		if ("hotsearch".equals(bigCode)) {
			return ImgViewHolder.TYPE_HOTW;
		}
		
		return 0;
	}
	@Override
	public int getItemViewType(int pos) {
		if (null == cards || cards.size() <= pos)
			return -1;
		return cards.get(pos).type;
	}
	
	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
		// 给ViewHolder设置布局文件
		View v = null;
		LogUtil.i("==onCreatViewHolder===i=" + viewType);
		switch (viewType) {
			case DemoCardBean.ADD:
				v = LayoutInflater.from(mContext).inflate(
						R.layout.left_demo_card_added, viewGroup, false);
				return new AddedViewHolder(v);
				
			case DemoCardBean.UNADD_SPE:
				v = LayoutInflater.from(mContext).inflate(
						R.layout.left_demo_card_spc_unadd, viewGroup, false);
				return new UnSpcAddViewHolder(v);
				
			case DemoCardBean.UNADD:
				v = LayoutInflater.from(mContext).inflate(
						R.layout.left_demo_card_unadd, viewGroup, false);
				return new UnaddViewHolder(v);
				
			case DemoCardBean.DEMO:
				v = LayoutInflater.from(mContext).inflate(
						R.layout.left_demo_card_img, viewGroup, false);
				return new ImgViewHolder(v);
		}
		// 若没有对应的类型, 返回null会报错的.
		return null;
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int pos) {
		// 给ViewHolder设置元素
		DemoCardBean p = cards.get(pos);
		switch (getItemViewType(pos)) {
			case DemoCardBean.ADD:
				AddedViewHolder addHolder = (AddedViewHolder)viewHolder;
				addHolder.setClickListener(mClick);
				addHolder.setDatas(p.items);
				break;
				
			case DemoCardBean.UNADD:
				UnaddViewHolder unaddHolder = (UnaddViewHolder)viewHolder;
				unaddHolder.setClickListener(mClick);
				unaddHolder.setDatas(p.items);
				break;
			case DemoCardBean.UNADD_SPE:
				UnSpcAddViewHolder unSpcAddHolder = (UnSpcAddViewHolder)viewHolder;
				unSpcAddHolder.setDatas(p.items);
				unSpcAddHolder.setClickListener(mClick);
				break;
				
			case DemoCardBean.DEMO:
				ImgViewHolder holder = (ImgViewHolder)viewHolder;
				holder.setType(getType());
				break;
		}
	}

	@Override
	public int getItemCount() {
		// 返回数据总数
		return cards == null ? 0 : cards.size();
	}
	/***
	 * 暂时显示此界面
	 */
	public void pauseOpt() {
	}
}
