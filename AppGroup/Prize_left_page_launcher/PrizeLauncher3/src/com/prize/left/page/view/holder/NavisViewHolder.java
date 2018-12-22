package com.prize.left.page.view.holder;

import java.util.List;
import java.util.Properties;

import org.json.JSONObject;
import org.xutils.x;
import org.xutils.image.ImageOptions;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.launcher3.LauncherApplication;
import com.android.launcher3.R;
import com.prize.left.page.activity.WebViewActivity;
import com.prize.left.page.bean.CardBean;
import com.prize.left.page.bean.table.NetNaviTable;
import com.prize.left.page.ui.NoScroolGridView;
import com.prize.left.page.util.ToastUtils;
import com.tencent.stat.StatService;

/**
 * 导航Card Holder
 */
public class NavisViewHolder extends RecyclerView.ViewHolder {
	/**卡片标题*/
	public TextView titleTxt;
	/**删除按钮*/
	public ImageView imgRefresh;
	/**菜单/更多 按钮*/
	public ImageView imgMenu;
	/**导航容器*/
	public NoScroolGridView contents;
	/**适配器*/
	private GridAdapter mAdapter;
	
	private LayoutInflater mInflater;
	
	private RecyclerView.Adapter<RecyclerView.ViewHolder> mRecycleAdapter;
	
	private int pos = -1;
	
	private Context mCtx;
	
	private CardBean cardBean = null;
	
	private ImageOptions imgOption = new ImageOptions.Builder()
    	//.setSize(60, 60)
    	//.setPlaceholderScaleType(ImageView.ScaleType.MATRIX)
    	.setImageScaleType(ImageView.ScaleType.CENTER_CROP)
    	.setFailureDrawableId(R.drawable.left_news_ico)
    	.setLoadingDrawableId(R.drawable.left_news_ico).setConfig(Bitmap.Config.ARGB_8888)
    	.build();
//	
//	private BDXLifeUtil mXLife;
//	
	private boolean canClick = true;
//	
//	private ILifeCallback xCallback = new ILifeCallback() {
//		@Override
//		public void onError(JSONObject json) {
//			// TODO Auto-generated method stub
//			//json.toString();
//			canClick = true;
//		}
//
//		@Override
//		public void onFinish(JSONObject json) {
//			// TODO Auto-generated method stub
//			//json.toString();
//			canClick = true;
//		}
//
//		public void onPreExecute() {
//			// TODO Auto-generated method stub
//			
//		}
//
//		@Override
//		public void onUpdate(JSONObject json) {
//			// TODO Auto-generated method stub
//			canClick = true;
//		}
//
//		@Override
//		public void onPreExecute(JSONObject arg0) {
//			// TODO Auto-generated method stub
//			
//		}
//		
//	};
	
	private int[] icons = new int[]{
			R.drawable.left_net_navi_ico
			, R.drawable.left_reader_ico
			, R.drawable.left_sina_ico
			, R.drawable.left_58_ico
			, R.drawable.ico_mobile_charge
			, R.drawable.left_movie_ico
			, R.drawable.left_groupon_ico
			, R.drawable.left_wm_ico
			, R.drawable.left_tb_ico
			, R.drawable.left_ticket_ico
			};
	
	private Properties mProp = new Properties();
	
	public NavisViewHolder(View v) {
		super(v);
		imgOption.setCompress(false);
		mCtx = v.getContext();
		titleTxt = (TextView) v.findViewById(R.id.txt_title);
		imgRefresh = (ImageView) v.findViewById(R.id.img_refresh);
		imgMenu = (ImageView) v.findViewById(R.id.img_more);
		contents = (NoScroolGridView) v.findViewById(R.id.content);
		mAdapter = new GridAdapter();
		contents.setAdapter(mAdapter);
		
		contents.setSelector(new ColorDrawable(Color.TRANSPARENT));
		
		contents.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int pos, long id) {
				// 点击事件处理
				if (!canClick)
					return;
				
				if (null == mAdapter.datas || pos < 0 ||
						pos >= mAdapter.datas.size())
					return;
				NetNaviTable bean = mAdapter.datas.get(pos);
				if (null == bean || TextUtils.isEmpty(bean.downUrl))
					return;
				canClick = false;
				mProp.clear();
				mProp.setProperty("position", String.valueOf(pos + 1));
				StatService.trackCustomKVEvent(mCtx, "CardNavis", mProp);
//				if (bean.downUrl.endsWith("_SDK")) {
//					if (bean.downUrl.equals("BDMOVIE_SDK")) { // 电影
//						mXLife.moreMovie(LauncherApplication.getInstance().getCityId(), xCallback);
//					}
//					else if (bean.downUrl.equals("BDTAKEAWAY_SDK")) { // 外卖
//						mXLife.moreTakeaway(LauncherApplication.getInstance().getCityId(), xCallback);
//					}
//					else if (bean.downUrl.equals("BDGROUP_SDK")) { // 团购暂时没有更多
//						mXLife.moreGroup(LauncherApplication.getInstance().getCityId(), xCallback);
//						/*ToastUtils.showToast(mCtx, R.string.str_navi_building);
//						canClick = true;*/
//					}
//					else if (bean.downUrl.equals("BDRECHARGE_SDK")) { // 手机充值首页
//						mXLife.toRechargePage(LauncherApplication.getInstance().getCityId(), xCallback);
//						/*ToastUtils.showToast(mCtx, R.string.str_navi_building);
//						canClick = true;*/
//					}
//					return;
//				} 
				/*else*/ if (bean.downUrl.startsWith("http")){
					Intent it = new Intent(mCtx, WebViewActivity.class);
					it.putExtra(WebViewActivity.P_URL, bean.downUrl);
					mCtx.startActivity(it);
					it = null;
					canClick = true;
				}
				else {
					ToastUtils.showToast(mCtx, R.string.str_navi_building);
					canClick = true;
				}
			}
			
		});
		
		mInflater = LayoutInflater.from(mCtx);
		
//		mXLife = BDXLifeUtil.getInstance();
//		mXLife.init(mCtx);
	}
	
	public void setAdapter(RecyclerView.Adapter<RecyclerView.ViewHolder> ad, int pos) {
		mRecycleAdapter = ad;
		this.pos = pos;
	}
	/***
	 * 设置导航数据
	 * @param ls
	 */
	public void setDatas(List<NetNaviTable> ls) {
		
		if (null == mAdapter)
			return;
		
		mAdapter.datas = ls;
		mAdapter.notifyDataSetChanged();
		
		/*if (mRecycleAdapter != null && pos != -1)
			mRecycleAdapter.notifyItemChanged(pos);*/
	}
	
	public List<NetNaviTable> getDatas() {
		if (mAdapter != null)
			return mAdapter.datas;
		
		return null;
	}
	
	/***
	 * GridAdapter
	 * @author fanjunchen
	 *
	 */
	class GridAdapter extends BaseAdapter {
		
		List<NetNaviTable> datas = null;
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return datas != null ? datas.size() : 0;
		}

		@Override
		public NetNaviTable getItem(int pos) {
			// TODO Auto-generated method stub
			return datas != null ? datas.get(pos) : null;
		}

		@Override
		public long getItemId(int pos) {
			// TODO Auto-generated method stub
			return pos;
		}

		@Override
		public View getView(int pos, View convertView, ViewGroup parent) {
			NaviHolder holder = null;
			if (null == convertView) {
				holder = new NaviHolder();
				convertView = mInflater.inflate(R.layout.navi_grid_item, null);
				holder.icImg = (ImageView)convertView.findViewById(R.id.img_ico);
				holder.txtName = (TextView)convertView.findViewById(R.id.txt_name);
				
				convertView.setTag(holder);
			}
			else {
				holder = (NaviHolder)convertView.getTag();
			}
			
			NetNaviTable item = datas.get(pos);
			
			holder.txtName.setText(item.name);
			if (TextUtils.isEmpty(item.iconUrl)) {
				holder.icImg.setImageResource(icons[pos]);
			}
			else
				x.image().bind(holder.icImg, item.iconUrl, imgOption);
			return convertView;
		}
	}
	/**暂停显示左一屏*/
	public void pause() {
		canClick = true;
	}
	
	static class NaviHolder {
		
		public ImageView icImg;
		
		public TextView txtName;
	}
	public void setCardBean(CardBean p) {
		// TODO Auto-generated method stub
		cardBean=p;
	}
}
