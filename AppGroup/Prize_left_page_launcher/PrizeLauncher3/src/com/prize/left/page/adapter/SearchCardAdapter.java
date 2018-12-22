package com.prize.left.page.adapter;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.xutils.common.util.LogUtil;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.android.launcher3.R;
import com.prize.left.page.ItemViewType;
import com.prize.left.page.bean.CardBean;
import com.prize.left.page.model.AppModel;
import com.prize.left.page.model.BaseModel;
import com.prize.left.page.model.ContactModel;
import com.prize.left.page.model.HotTipsModel;
import com.prize.left.page.model.HotWordModel;
import com.prize.left.page.model.UsedModel;
import com.prize.left.page.view.holder.AppViewHolder;
import com.prize.left.page.view.holder.ContactsViewHolder;
import com.prize.left.page.view.holder.FooterViewHolder;
import com.prize.left.page.view.holder.HotTipsViewHolder;
import com.prize.left.page.view.holder.HotWordViewHolder;
import com.prize.left.page.view.holder.UsedViewHolder;
/***
 * 主recycleView适配器
 * @author fanjunchen
 *
 */
public class SearchCardAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

	private List<CardBean> cards;

	private Context mContext;
	
	private View.OnClickListener clickListener;
	
	private HashMap<String, BaseModel> map = new HashMap<String, BaseModel>();
	
	private Activity mAct = null;

	private EditText mEdit;
	
	public SearchCardAdapter(Context context, List<CardBean> cards,EditText edit) {
		this.mContext = context;
		this.cards = cards;
		mEdit = edit;
	}
	
	public void setActivity(Activity a) {
		mAct = a;
	}
	
	public void onResume() {
		Set<String> set = map.keySet();
		Iterator<String> it = set.iterator();
		while (it.hasNext()) {
			String key = it.next();
			BaseModel m = map.get(key);
			if (m != null)
				m.onResume();
		}
	}
	
	public void addModelTo(String key, BaseModel m) {
		BaseModel model = map.get(key);
		if (null == model) {
			map.put(key, m);
		}
	}
	/***
	 * 调用model去搜索
	 * @param str
	 */
	public void doSearch(String str) {
		Iterator<String> keys = map.keySet().iterator();
		while(keys.hasNext()) {
			String key = keys.next();
			BaseModel model = map.get(key);
			if (model instanceof ContactModel) {
				((ContactModel)model).setQuery(str);
				((ContactModel)model).doPost();
			}
			else if (model instanceof AppModel) {
				((AppModel)model).setQuery(str);
				((AppModel)model).doPost();
			}
		}
	}
	
	@Override
	public int getItemViewType(int pos) {
		if (null == cards || cards.size() <= pos)
			return -1;
		
		return cards.get(pos).cardType.code;
	}
	
	public void setClickListener(View.OnClickListener cls) {
		clickListener = cls;
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
		// 给ViewHolder设置布局文件
		View v = null;
		LogUtil.i("==onCreatViewHolder===i=" + viewType);
		switch (viewType) {
			case ItemViewType.SEARCH_APP:
				v = LayoutInflater.from(mContext).inflate(
						R.layout.app_card_item, viewGroup, false);
				return new AppViewHolder(v);
				
			case ItemViewType.SEARCH_CONTACT:
				v = LayoutInflater.from(mContext).inflate(
						R.layout.contact_card_item, viewGroup, false);
				return new ContactsViewHolder(v);
				

			case ItemViewType.BD_HOT_TIPS:
				v = LayoutInflater.from(mContext).inflate(
						R.layout.left_hot_word_card, viewGroup, false);

			    HotTipsViewHolder hot = new HotTipsViewHolder(v);
				hot.imgMenu.setVisibility(View.GONE);
				return  hot;
				
		/*	case ItemViewType.RECENT_USE: //最近使用
				v = LayoutInflater.from(mContext).inflate(
						R.layout.left_recent_used_card, viewGroup, false);
				return new UsedViewHolder(v);*/
				
			case ItemViewType.FOOTER:
				v = LayoutInflater.from(mContext).inflate(
						R.layout.left_card_footer, viewGroup, false);
				return new FooterViewHolder(v);
		}
		// 若没有对应的类型, 返回null会报错的.
		return null;
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int pos) {
		// 给ViewHolder设置元素
		LogUtil.i("==onBindViewHolder===pos=" + pos);
		CardBean p = cards.get(pos);
		switch (getItemViewType(pos)) {
			case ItemViewType.SEARCH_CONTACT:
				ContactsViewHolder nsHolder = (ContactsViewHolder)viewHolder;
				nsHolder.titleTxt.setText(p.cardType.name);
				nsHolder.txtExpand.setOnClickListener(clickListener);
				nsHolder.txtExpand.setTag(ItemViewType.SEARCH_CONTACT);
				nsHolder.setActivity(mAct);
				if (nsHolder.getDatas() == null) {
					ContactModel model = (ContactModel)map.get(p.cardType.dataUrl);
					if (null == model) {
						model = new ContactModel(mContext);
						map.put(p.cardType.dataUrl, model);
					}
					nsHolder.setAdapter(this);
					nsHolder.setPos(pos);
					model.setViewHolder(nsHolder);
					model.doPost();
				}
				break;
				
			case ItemViewType.SEARCH_APP:
				AppViewHolder appHolder = (AppViewHolder)viewHolder;
				appHolder.setAdapter(this, pos);
				if (appHolder.titleTxt != null)
					appHolder.titleTxt.setText(p.cardType.name);
				if (appHolder.imgMenu != null) {
					appHolder.imgMenu.setOnClickListener(clickListener);
					appHolder.imgMenu.setTag(ItemViewType.NAVI);
					appHolder.imgMenu.setVisibility(View.INVISIBLE);
				}
				if (appHolder.imgRefresh != null)
					appHolder.imgRefresh.setVisibility(View.INVISIBLE);
				
				if (appHolder.getDatas() == null) {
					AppModel model = (AppModel)map.get(p.cardType.dataUrl);
					if (null == model) {
						model = new AppModel(mContext);
						map.put(p.cardType.dataUrl, model);
					}
					model.setViewHolder(appHolder);
					model.doPost();
				}
				break;
				
			/*case ItemViewType.RECENT_USE:
				UsedViewHolder h = (UsedViewHolder)viewHolder;
				h.setAdapter(this);
				if (h.titleTxt != null)
					h.titleTxt.setText(p.cardType.name);
				
				if (h.imgMenu != null) {
					h.imgMenu.setVisibility(View.INVISIBLE);
				}
				
				if (h.imgRefresh != null)
					h.imgRefresh.setVisibility(View.INVISIBLE);
				
				UsedModel uModel = (UsedModel)map.get(p.cardType.dataUrl);
				if (null == uModel) {
					uModel = new UsedModel(mContext);
					map.put(p.cardType.dataUrl, uModel);
					uModel.setViewHolder(h);
					h.setActivity(mAct);
					uModel.setResIdentity(p.cardType.dataUrl);
				}
				uModel.doPost();
				break;*/
				
				

			case ItemViewType.BD_HOT_TIPS:
				HotTipsViewHolder hotHolder = (HotTipsViewHolder)viewHolder;
				hotHolder.titleTxt.setText(p.cardType.name);
				hotHolder.imgMenu.setOnClickListener(clickListener);
				hotHolder.imgMenu.setTag(p);
				hotHolder.setCardBean(p);
				if (hotHolder.txtMore != null) {
					hotHolder.txtMore.setTag(p);
				}
				
				HotTipsModel hotModel = (HotTipsModel)map.get(p.cardType.dataUrl);
				if (hotHolder.getDatas() == null) {
					//hotHolder.itemView.setVisibility(View.GONE);
					if (null == hotModel) {
						hotModel = new HotTipsModel(mContext,mEdit);
						map.put(p.cardType.dataUrl, hotModel);
					}
					hotModel.setCardBean(p);
					hotModel.setViewHolder(hotHolder);
					hotModel.doPost();
				}
				break;
				
			case ItemViewType.FOOTER:
				FooterViewHolder fHolder = (FooterViewHolder)viewHolder;
				fHolder.itemView.setOnClickListener(clickListener);
				TextView tv = (TextView)fHolder.itemView.findViewById(R.id.txt_add_card);
				if (tv != null)
					tv.setVisibility(View.VISIBLE);
					tv.setText(R.string.str_search_net);
				break;
		}
	}

	@Override
	public int getItemCount() {
		// 返回数据总数
		return cards == null ? 0 : cards.size();
	}
}
