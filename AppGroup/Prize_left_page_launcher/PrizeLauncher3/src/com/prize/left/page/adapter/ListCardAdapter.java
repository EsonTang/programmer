package com.prize.left.page.adapter;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.xutils.common.util.LogUtil;

import android.R.integer;
import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.launcher3.R;
import com.prize.left.page.ItemViewType;
import com.prize.left.page.bean.CardBean;
import com.prize.left.page.bean.table.CardType;
import com.prize.left.page.model.BDGroupModel;
import com.prize.left.page.model.BDMovieModel;
import com.prize.left.page.model.BaseModel;
import com.prize.left.page.model.FoodsModel;
import com.prize.left.page.model.HeadNewsModel;
import com.prize.left.page.model.HotWordModel;
import com.prize.left.page.model.ICardNotify;
import com.prize.left.page.model.InvnoNewsModel;
import com.prize.left.page.model.LeftModel;
import com.prize.left.page.model.NavisModel;
import com.prize.left.page.model.OneNewsModel;
import com.prize.left.page.model.PushModel;
import com.prize.left.page.model.UsedModel;
import com.prize.left.page.util.IConstants;
import com.prize.left.page.view.holder.BDGroupViewHolder;
import com.prize.left.page.view.holder.BDMovieViewHolder;
import com.prize.left.page.view.holder.FoodsViewHolder;
import com.prize.left.page.view.holder.FooterViewHolder;
import com.prize.left.page.view.holder.HeadViewHolder;
import com.prize.left.page.view.holder.HotWordViewHolder;
import com.prize.left.page.view.holder.InvnoNewsViewHolder;
import com.prize.left.page.view.holder.NavisViewHolder;
import com.prize.left.page.view.holder.NewsViewHolder;
import com.prize.left.page.view.holder.OneNewsViewHolder;
import com.prize.left.page.view.holder.PushViewHolder;
import com.prize.left.page.view.holder.UsedViewHolder;
/***
 * 主recycleView适配器
 * @author fanjunchen
 *
 */
public class ListCardAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

	private List<CardBean> cards;

	private Context mContext;
	
	private View.OnClickListener clickListener;
	
	private HashMap<String, BaseModel> map = new HashMap<String, BaseModel>();
	
	private HeadViewHolder mHeadHolder = null;
	
	private LeftModel mLeftModel;
	/**导航卡片对应的东东*/
	private final String NAVI = "navis";
	
	private NavisViewHolder mNavisHolder = null;
	
	private Activity mAct;
	
	private ICardNotify mNotify;
	
	public void setActivity(Activity m) {
		mAct = m;
	}
	
	public BaseModel getModel(String dataUrl) {
		return map.get(dataUrl);
	}
	
	
	public void onDestry() {
		mAct =null;
	}
	public void setICardNotify(ICardNotify n) {
		mNotify = n;
	}

	public ListCardAdapter(Context context, List<CardBean> cards) {
		this.mContext = context;
		this.cards = cards;
	}
	
	public void setLeftModel(LeftModel m) {
		mLeftModel = m;
	}
	
	/***
	 * 刷新
	 */
	public void doRefresh() {
		Set<String> set = map.keySet();
		Iterator<String> it = set.iterator();
		while (it.hasNext()) {
			String key = it.next();
			BaseModel m = map.get(key);
			if (m != null)
				m.doRefresh();
		}
	}
	/***
	 * 删除某个model, 使其不会经常做没有必要的刷新
	 * @param key
	 */
	public void removeModelByKey(String key) {
		map.remove(map.get(key));
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
	
	public void onPause() {
		Set<String> set = map.keySet();
		Iterator<String> it = set.iterator();
		while (it.hasNext()) {
			String key = it.next();
			BaseModel m = map.get(key);
			if (m != null)
				m.onPause();
		}
	}
	
	@Override
	public int getItemViewType(int pos) {
		if (null == cards || cards.size() <= pos)
			return -1;
		CardType c = cards.get(pos).cardType;
		if (c!=null) {
			if (IConstants.BD_GROUP_CARD_UITYPE.equals(c.uitype)||IConstants.BDMOVIE_CARD_UITYPE.equals(c.uitype)||IConstants.INVNO_NEWS_CARD_UITYPE.equals(c.uitype)) {
				return c.code+c.subCode;
			}
			return c.code;
		}		
		return -1;
		
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
			case ItemViewType.NEWS:
				v = LayoutInflater.from(mContext).inflate(
						R.layout.news_card_item, viewGroup, false);
				return new NewsViewHolder(v);
				
			case ItemViewType.HEADER:
				v = LayoutInflater.from(mContext).inflate(
						R.layout.left_header_card, viewGroup, false);
				return new HeadViewHolder(v);
				
			case ItemViewType.NAVI:
				v = LayoutInflater.from(mContext).inflate(
						R.layout.navis_card_item, viewGroup, false);
				return new NavisViewHolder(v);
/*
				
			case ItemViewType.PUSH_APP:
			case ItemViewType.PUSH_APP+1:
			case ItemViewType.PUSH_APP+2:
			case ItemViewType.PUSH_APP+3:
			case ItemViewType.PUSH_APP+4:
			case ItemViewType.PUSH_APP+5:
			case ItemViewType.PUSH_APP+6:
			case ItemViewType.PUSH_APP+7:
			case ItemViewType.PUSH_APP+8:
				v = LayoutInflater.from(mContext).inflate(
						R.layout.push_card_item, viewGroup, false);
				return new PushViewHolder(v);*/
				
			case ItemViewType.FOOD:
				v = LayoutInflater.from(mContext).inflate(
						R.layout.foods_card_item, viewGroup, false);
				return new FoodsViewHolder(v);
				
			case ItemViewType.RECENT_USE: //最近使用
				v = LayoutInflater.from(mContext).inflate(
						R.layout.left_recent_used_card, viewGroup, false);
				return new UsedViewHolder(v);
				
			case ItemViewType.ONE_NEWS: // 一点资讯
			case ItemViewType.ONE_NEWS + 1:
			case ItemViewType.ONE_NEWS + 2:
			case ItemViewType.ONE_NEWS + 3:
			case ItemViewType.ONE_NEWS + 4:
			case ItemViewType.ONE_NEWS + 5:
			case ItemViewType.ONE_NEWS + 6:
			case ItemViewType.ONE_NEWS + 7:
			case ItemViewType.ONE_NEWS + 8:
			case ItemViewType.ONE_NEWS + 9:
			case ItemViewType.ONE_NEWS + 10:
			case ItemViewType.ONE_NEWS + 11:
			case ItemViewType.ONE_NEWS + 12:
			case ItemViewType.ONE_NEWS + 13:
			case ItemViewType.ONE_NEWS + 14:
			case ItemViewType.ONE_NEWS + 15:
			case ItemViewType.ONE_NEWS + 16:
			case ItemViewType.ONE_NEWS + 17:
				
				v = LayoutInflater.from(mContext).inflate(
						R.layout.one_news_card_item, viewGroup, false);
				return new OneNewsViewHolder(v);
				
			case ItemViewType.BDMOVIE:
			case ItemViewType.BDMOVIE + 1:
			case ItemViewType.BDMOVIE + 2:
			case ItemViewType.BDMOVIE + 3:
			case ItemViewType.BDMOVIE + 4:
			case ItemViewType.BDMOVIE + 5:
			case ItemViewType.BDMOVIE + 6:
			case ItemViewType.BDMOVIE + 7:
			case ItemViewType.BDMOVIE + 8:
			case ItemViewType.BDMOVIE + 9:// 百度电影 // 百度电影
				v = LayoutInflater.from(mContext).inflate(
						R.layout.bd_movie_card_item, viewGroup, false);
				return new BDMovieViewHolder(v);
			case ItemViewType.BD_GROUP:// 百度团购
			case ItemViewType.BD_GROUP + 1:
			case ItemViewType.BD_GROUP + 2:
			case ItemViewType.BD_GROUP + 3:
			case ItemViewType.BD_GROUP + 4:
			case ItemViewType.BD_GROUP + 5:
			case ItemViewType.BD_GROUP + 6:
			case ItemViewType.BD_GROUP + 7:
			case ItemViewType.BD_GROUP + 8:
			case ItemViewType.BD_GROUP + 9:// 百度团购
				v = LayoutInflater.from(mContext).inflate(
						R.layout.bd_group_card_item, viewGroup, false);
				return new BDGroupViewHolder(v);
			case ItemViewType.INVNO_NEWS:
			case ItemViewType.INVNO_NEWS + 1:
			case ItemViewType.INVNO_NEWS + 2:
			case ItemViewType.INVNO_NEWS + 3:
			case ItemViewType.INVNO_NEWS + 4:
			case ItemViewType.INVNO_NEWS + 5:
			case ItemViewType.INVNO_NEWS + 6:
			case ItemViewType.INVNO_NEWS + 7:
			case ItemViewType.INVNO_NEWS + 8:
			case ItemViewType.INVNO_NEWS + 9:
				v = LayoutInflater.from(mContext).inflate(
						R.layout.left_invno_card, viewGroup, false);
				return new InvnoNewsViewHolder(v);
			case ItemViewType.BD_HOT_WD:
				v = LayoutInflater.from(mContext).inflate(
						R.layout.left_hot_word_card, viewGroup, false);
				return new HotWordViewHolder(v);
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
		CardBean p = cards.get(pos);
		int cctype = getItemViewType(pos);
		switch (cctype) {
			/*case ItemViewType.NEWS:
				NewsViewHolder nsHolder = (NewsViewHolder)viewHolder;
				nsHolder.titleTxt.setText(p.cardType.name);
				nsHolder.imgMenu.setOnClickListener(clickListener);
				nsHolder.imgMenu.setTag(p);
				nsHolder.txtMore.setTag(p);
				nsHolder.txtMore.setOnClickListener(clickListener);
				HeadNewsModel model = (HeadNewsModel)map.get(p.cardType.dataUrl);
				if (nsHolder.getDatas() == null) {
					if (null == model) {
						model = new HeadNewsModel(mContext);
						model.setCardType(cctype);
						model.setICardNotify(mNotify);
						map.put(p.cardType.dataUrl, model);
					}
					model.setViewHolder(nsHolder);
					model.setResIdentity(p.cardType.dataUrl);
					model.doPost();
				}
				else
					model.doBindImg();
				break;*/
				
			case ItemViewType.HEADER:
				HeadViewHolder headHolder = (HeadViewHolder)viewHolder;
				mHeadHolder = headHolder;
				headHolder.menuView.setOnClickListener(clickListener);
				//headHolder.txtCenter.setText(p.cardType.name);
				//headHolder.txtCenter.setOnClickListener(clickListener);
				headHolder.txtSearch.setOnClickListener(clickListener);
				/*if (mLeftModel != null)
					mLeftModel.setHeadSearchView(headHolder.txtSearch);*/
				break;
				
			case ItemViewType.NAVI:
				mNavisHolder = (NavisViewHolder)viewHolder;
				mNavisHolder.setAdapter(this, pos);
				mNavisHolder.setCardBean(p);
				if (mNavisHolder.titleTxt != null)
					mNavisHolder.titleTxt.setText(p.cardType.name);
				if (mNavisHolder.imgMenu != null) {
					/*mNavisHolder.imgMenu.setOnClickListener(clickListener);
					mNavisHolder.imgMenu.setTag(p);*/
					mNavisHolder.imgMenu.setVisibility(View.INVISIBLE);
				}
				if (mNavisHolder.imgRefresh != null)
					mNavisHolder.imgRefresh.setVisibility(View.INVISIBLE);
				
				NavisModel naviModel = (NavisModel)map.get(p.cardType.dataUrl);//p.cardType.dataUrl
				if (mNavisHolder.getDatas() == null) {
					if (null == naviModel) {
						naviModel = new NavisModel(mContext);
						naviModel.setCardType(cctype);
						naviModel.setICardNotify(mNotify);
						map.put(p.cardType.dataUrl, naviModel);//p.cardType.dataUrl
					}
					naviModel.setCardBean(p);
					naviModel.setViewHolder(mNavisHolder);
					naviModel.setResIdentity(p.cardType.dataUrl);
					naviModel.doPost();
				}
				else{
					if(naviModel!=null){
					naviModel.setCardBean(p);
					naviModel.doBindImg();
					}
				}
				
				
				break;
				
			case ItemViewType.RECENT_USE:
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
					uModel.setCardType(cctype);
					map.put(p.cardType.dataUrl, uModel);
					uModel.setViewHolder(h);
					uModel.setICardNotify(mNotify);
					h.setActivity(mAct);
					uModel.setResIdentity(p.cardType.dataUrl);
				}
				else {
					uModel.setCardType(cctype);
					uModel.setViewHolder(h);
					uModel.setICardNotify(mNotify);
					h.setActivity(mAct);
				}
				uModel.doPost();
				break;
				
			/*case ItemViewType.FOOD: // 美食(团队)
				FoodsViewHolder foodHolder = (FoodsViewHolder)viewHolder;
				foodHolder.setAdapter(this, pos);
				foodHolder.titleTxt.setText(p.cardType.name);
				foodHolder.imgMenu.setOnClickListener(clickListener);
				foodHolder.imgMenu.setTag(p);
				foodHolder.txtMore.setTag(p);
				foodHolder.txtMore.setOnClickListener(clickListener);
				
				FoodsModel foodModel = (FoodsModel)map.get(p.cardType.dataUrl);
				if (foodHolder.getDatas() == null) {
					if (null == foodModel) {
						foodModel = new FoodsModel(mContext);
						foodModel.setCardType(cctype);
						foodModel.setICardNotify(mNotify);
						map.put(p.cardType.dataUrl, foodModel);
					}
					foodModel.setViewHolder(foodHolder);
					foodModel.setResIdentity(p.cardType.dataUrl);
					foodModel.doPost();
				}
				else
					foodModel.doBindImg();
				break;*/
		/*	case ItemViewType.ONE_NEWS: // 一点资讯
			case ItemViewType.ONE_NEWS + 1:
			case ItemViewType.ONE_NEWS + 2:
			case ItemViewType.ONE_NEWS + 3:
			case ItemViewType.ONE_NEWS + 4:
			case ItemViewType.ONE_NEWS + 5:
			case ItemViewType.ONE_NEWS + 6:
			case ItemViewType.ONE_NEWS + 7:
			case ItemViewType.ONE_NEWS + 8:
			case ItemViewType.ONE_NEWS + 9:
			case ItemViewType.ONE_NEWS + 10:
			case ItemViewType.ONE_NEWS + 11:
			case ItemViewType.ONE_NEWS + 12:
			case ItemViewType.ONE_NEWS + 13:
			case ItemViewType.ONE_NEWS + 14:
			case ItemViewType.ONE_NEWS + 15:
			case ItemViewType.ONE_NEWS + 16:
			case ItemViewType.ONE_NEWS + 17:
				OneNewsViewHolder oneHolder = (OneNewsViewHolder)viewHolder;
				oneHolder.titleTxt.setText(p.cardType.name);
				oneHolder.imgMenu.setOnClickListener(clickListener);
				oneHolder.imgMenu.setTag(p);
				oneHolder.txtMore.setOnClickListener(clickListener);
				
				OneNewsModel nModel = (OneNewsModel)map.get(p.cardType.dataUrl + p.cardType.subCode);
				if (oneHolder.getDatas() == null || null == nModel) {
					
					if (null == nModel) {
						nModel = new OneNewsModel(mContext);
						nModel.setCardType(cctype);
						nModel.setICardNotify(mNotify);
						map.put(p.cardType.dataUrl + p.cardType.subCode, nModel);
						
						oneHolder.txtMore.setTag(p);
						oneHolder.setCardBean(p);
						
						nModel.setCardBean(p);
						nModel.setViewHolder(oneHolder);
						nModel.doPost();
					}
				}
				else{
					if(nModel!=null){
						nModel.setCardBean(p);
						nModel.doBindImg();
					}
				}
					
				break;*/
			case ItemViewType.BDMOVIE:
				bindBDMovie(viewHolder, p, cctype);
				break;
			case ItemViewType.BDMOVIE + 1:
				bindBDMovie(viewHolder, p, cctype);
				break;
			case ItemViewType.BDMOVIE + 2:
				bindBDMovie(viewHolder, p, cctype);
				break;
			case ItemViewType.BDMOVIE + 3:
				bindBDMovie(viewHolder, p, cctype);
				break;
			case ItemViewType.BDMOVIE + 4:
				bindBDMovie(viewHolder, p, cctype);
				break;
			case ItemViewType.BDMOVIE + 5:
				bindBDMovie(viewHolder, p, cctype);
				break;
			case ItemViewType.BDMOVIE + 6:
				bindBDMovie(viewHolder, p, cctype);
				break;
			case ItemViewType.BDMOVIE + 7:
				bindBDMovie(viewHolder, p, cctype);
				break;
			case ItemViewType.BDMOVIE + 8:
				bindBDMovie(viewHolder, p, cctype);
				break;
			case ItemViewType.BDMOVIE + 9:// 百度电影
				bindBDMovie(viewHolder, p, cctype);
				break;
		/*		
			case ItemViewType.PUSH_APP: // 推送
			case ItemViewType.PUSH_APP+1:
			case ItemViewType.PUSH_APP+2:
			case ItemViewType.PUSH_APP+3:
			case ItemViewType.PUSH_APP+4:
			case ItemViewType.PUSH_APP+5:
			case ItemViewType.PUSH_APP+6:
			case ItemViewType.PUSH_APP+7:
			case ItemViewType.PUSH_APP+8:
				PushViewHolder pushHolder = (PushViewHolder)viewHolder;
			pushHolder.titleTxt.setText(p.cardType.name);
			pushHolder.imgMenu.setOnClickListener(clickListener);
			pushHolder.imgMenu.setTag(p);
			pushHolder.setCardBean(p);
				
			pushHolder.txtMore.setTag(p);
				
				PushModel pushModel = (PushModel)map.get(p.cardType.dataUrl);
				if (pushHolder.getDatas() == null) {
					if (null == pushModel) {
						pushModel = new PushModel(mContext);
						pushModel.setCardType(cctype);
						pushModel.setICardNotify(mNotify);
						map.put(p.cardType.dataUrl, pushModel);
					}
					pushModel.setCardBean(p);
					pushModel.setViewHolder(pushHolder);
					pushModel.gone();
					pushModel.doPost();
				}
				else
					pushModel.doBindImg();
				break;*/
				
			case ItemViewType.BD_GROUP:// 百度团购 
				bindBDGroup(viewHolder, p, cctype);	
				break;
			case ItemViewType.BD_GROUP + 1:
				bindBDGroup(viewHolder, p, cctype);	
				break;
			case ItemViewType.BD_GROUP + 2:
				bindBDGroup(viewHolder, p, cctype);	
				break;
			case ItemViewType.BD_GROUP + 3:
				bindBDGroup(viewHolder, p, cctype);	
				break;
			case ItemViewType.BD_GROUP + 4:
				bindBDGroup(viewHolder, p, cctype);	
				break;
			case ItemViewType.BD_GROUP + 5:
				bindBDGroup(viewHolder, p, cctype);	
				break;
			case ItemViewType.BD_GROUP + 6:
				bindBDGroup(viewHolder, p, cctype);	
				break;
			case ItemViewType.BD_GROUP + 7:
				bindBDGroup(viewHolder, p, cctype);	
				break;
			case ItemViewType.BD_GROUP + 8:
				bindBDGroup(viewHolder, p, cctype);	
				break;
			case ItemViewType.BD_GROUP + 9:
				bindBDGroup(viewHolder, p, cctype);	
				break;
				
			case ItemViewType.INVNO_NEWS://百度新闻
				bindInvnoNews(viewHolder, p, cctype);
				break;
			case ItemViewType.INVNO_NEWS + 1:
				bindInvnoNews(viewHolder, p, cctype);
				break;
			case ItemViewType.INVNO_NEWS + 2:
				bindInvnoNews(viewHolder, p, cctype);
				break;
			case ItemViewType.INVNO_NEWS + 3:
				bindInvnoNews(viewHolder, p, cctype);
				break;
			case ItemViewType.INVNO_NEWS + 4:
				bindInvnoNews(viewHolder, p, cctype);
				break;
			case ItemViewType.INVNO_NEWS + 5:
				bindInvnoNews(viewHolder, p, cctype);
				break;
			case ItemViewType.INVNO_NEWS + 6:
				bindInvnoNews(viewHolder, p, cctype);
				break;
			case ItemViewType.INVNO_NEWS + 7:
				bindInvnoNews(viewHolder, p, cctype);
				break;
			case ItemViewType.INVNO_NEWS + 8:
				bindInvnoNews(viewHolder, p, cctype);
				break;
			case ItemViewType.INVNO_NEWS + 9:
				bindInvnoNews(viewHolder, p, cctype);
				break;
			case ItemViewType.BD_HOT_WD:
				HotWordViewHolder hotHolder = (HotWordViewHolder)viewHolder;
				hotHolder.titleTxt.setText(p.cardType.name);
				hotHolder.imgMenu.setOnClickListener(clickListener);
				hotHolder.imgMenu.setTag(p);
				hotHolder.setCardBean(p);
				if (hotHolder.txtMore != null) {
					hotHolder.txtMore.setTag(p);
				}
				
				HotWordModel hotModel = (HotWordModel)map.get(p.cardType.dataUrl);
				if (hotHolder.getDatas() == null) {
					//hotHolder.itemView.setVisibility(View.GONE);
					if (null == hotModel) {
						hotModel = new HotWordModel(mContext,null);
						hotModel.setCardType(cctype);
						hotModel.setICardNotify(mNotify);
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
				break;
			default:
				break;
		}
	}

	@Override
	public int getItemCount() {
		// 返回数据总数
		return cards == null ? 0 : cards.size();
	}
	/***
	 * 获取头card
	 * @return
	 */
	public HeadViewHolder getHeaderHolder() {
		return mHeadHolder;
	}
	/***
	 * 暂时显示此界面
	 */
	public void pauseOpt() {
		if (mNavisHolder != null)
			mNavisHolder.pause();
	}
	/*@Override
    public void onViewDetachedFromWindow(RecyclerView.ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        holder.itemView.clearAnimation();
    }*/
	
	public void bindInvnoNews(ViewHolder viewHolder,CardBean p,int cctype){
		
		InvnoNewsViewHolder invnoHolder = (InvnoNewsViewHolder)viewHolder;
		invnoHolder.titleTxt.setText(p.cardType.name);
		invnoHolder.imgMenu.setOnClickListener(clickListener);
		invnoHolder.imgMenu.setTag(p);
		invnoHolder.setCardBean(p);
		if (invnoHolder.txtMore != null) {
			invnoHolder.txtMore.setTag(p);
		}
		
		InvnoNewsModel invoModel = (InvnoNewsModel)map.get(p.cardType.dataUrl+p.cardType.subCode);
		if(invoModel==null) {
			invnoHolder.datas=null;
		}
		if (invnoHolder.getDatas() == null) {
			//invnoHolder.itemView.setVisibility(View.GONE);
			if (null == invoModel) {
				invoModel = new InvnoNewsModel(mContext);
				invoModel.setCardType(cctype);
				invoModel.setICardNotify(mNotify);
				map.put(p.cardType.dataUrl+p.cardType.subCode, invoModel);
			}
			invoModel.setCardBean(p);
			invoModel.setViewHolder(invnoHolder);
			invoModel.gone();
			invoModel.doPost();
		}
		else{
			if(invoModel!=null){
			invoModel.setCardBean(p);
			invoModel.setViewHolder(invnoHolder);
//			invoModel.doPost();
			invoModel.doBindImg();
			}
		}
			
	}
	
	public void bindBDGroup(ViewHolder viewHolder,CardBean p,int cctype){
		
		BDGroupViewHolder groupHolder = (BDGroupViewHolder)viewHolder;
		groupHolder.titleTxt.setText(p.cardType.name);
		groupHolder.imgMenu.setOnClickListener(clickListener);
		groupHolder.imgMenu.setTag(p);
		groupHolder.setCardBean(p);
		if (groupHolder.txtMore != null)
			groupHolder.txtMore.setTag(p);
		
		BDGroupModel groupModel = (BDGroupModel)map.get(p.cardType.dataUrl+p.cardType.subCode);
		if(groupModel==null) {
			groupHolder.datas=null;
		}
		if (groupHolder.getDatas() == null) {
			if (null == groupModel) {
				groupModel = new BDGroupModel(mContext);
				groupModel.setCardType(cctype);
				groupModel.setICardNotify(mNotify);
				map.put(p.cardType.dataUrl+p.cardType.subCode, groupModel);
			}
			groupModel.setCardBean(p);
			groupModel.setViewHolder(groupHolder);
			groupModel.gone();
			groupModel.doPost();
		}
		else{
			if(groupModel!=null){
			groupModel.setViewHolder(groupHolder);
			groupModel.setCardBean(p);
			groupModel.doBindImg();
			}
		}
	}
	
	public void bindBDMovie(ViewHolder viewHolder,CardBean p,int cctype){
		
		BDMovieViewHolder movieHolder = (BDMovieViewHolder)viewHolder;
		movieHolder.titleTxt.setText(p.cardType.name);
		movieHolder.imgMenu.setOnClickListener(clickListener);
		movieHolder.imgMenu.setTag(p);
		movieHolder.setCardBean(p);
		
		movieHolder.txtMore.setTag(p);
		
		BDMovieModel movieModel = (BDMovieModel)map.get(p.cardType.dataUrl+p.cardType.subCode);
		if(movieModel==null) {
			movieHolder.datas=null;
		}
		if (movieHolder.getDatas() == null) {
			if (null == movieModel) {
				movieModel = new BDMovieModel(mContext);
				movieModel.setCardType(cctype);
				movieModel.setICardNotify(mNotify);
				map.put(p.cardType.dataUrl+p.cardType.subCode, movieModel);
			}
			movieModel.setCardBean(p);
			movieModel.setViewHolder(movieHolder);
			movieModel.gone();
			movieModel.doPost();
		}
		else{
			if(movieModel!=null)
			{
				movieModel.setViewHolder(movieHolder);
				movieModel.setCardBean(p);
				movieModel.doBindImg();	
			}				
		}
	}
}
