package com.prize.left.page.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.xutils.x;
import org.xutils.image.ImageOptions;

import com.android.launcher3.ImageViewWrapAware;
import com.android.launcher3.R;
import com.android.launcher3.TextViewWrapAware;
import com.android.launcher3.Utilities;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.prize.left.page.activity.WebViewActivity;
import com.prize.left.page.bean.table.PushTable;
import com.prize.left.page.model.PushModel;
import com.tencent.stat.StatService;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class PushViewLinearLayout extends LinearLayout implements
		View.OnClickListener {

	/** 图片配置器 */
	private ImageOptions leftImgOption = new ImageOptions.Builder()
			// .setSize(240, 360)
			.setImageScaleType(ImageView.ScaleType.FIT_XY)
			.setLoadingDrawableId(R.color.white_64)
			.setFailureDrawableId(R.color.white_64).build();

	private PushModel pushModel;
	private Context mCtx;
	private LayoutInflater mInflater = null;
	private LinearLayout.LayoutParams params;
	private List<PushTable> datas = null;

	private View divider_line;

	private View mUsedView;
	
	private Properties mProp = new Properties();
	private int indexOfChild;
//	private TextView txtName;

	public PushViewLinearLayout(Context context, AttributeSet attrs,
			int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		mCtx = context;
		// TODO Auto-generated constructor stub
	}

	public PushViewLinearLayout(Context context, AttributeSet attrs,
			int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		mCtx = context;
		// TODO Auto-generated constructor stub
	}
	
	public void onDestory() {
		pushModel = null;
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
	}

	/***
	 * 设置美食(团购)数据
	 * 
	 * @param ls
	 */
	public void setDatas(List<PushTable> ls) {
		if (null == ls || ls.size() < 1)
			return;
		datas = ls;
		dealTypeOthers();
	}
	


	public void doRefresh() {

		if (pushModel != null) {
			pushModel.doPost();
		}
	}
	
	public void setUsedView(View v) {
		mUsedView = v;
	}
	
	public void visible() {
		divider_line = mUsedView.findViewById(R.id.divider_line1);
		divider_line.setVisibility(View.VISIBLE);
		this.setVisibility(View.VISIBLE);
		
	}
	
	public void gone() {
		divider_line = mUsedView.findViewById(R.id.divider_line1);
		divider_line.setVisibility(View.GONE);
		this.setVisibility(View.GONE);
	}
	
	public void doPost() {

		if (pushModel == null) {
			pushModel = new PushModel(mContext);
			pushModel.setContent(this);
			pushModel.doPost();
		}
		else {
			pushModel.doBindImg();
		}
	}
	
	int page=0;

	/***
	 * 处理其他 频道
	 */
	private void dealTypeOthers() {
		int childSz = this.getChildCount();
		

		int row=(int) getWeightSum();
		int start = page*row;

		 List<PushTable> temp = new ArrayList<>();
		 int count = Math.min(row+start, datas.size());
		for(int i=start;i<count;i++) {
			temp.add(datas.get(i));
		}


		int dataSize = temp.size();
		page++;
		if(page>=datas.size()/row) {
			page=0;
		}
		
		if (dataSize < childSz) { // 先删除多余的VIEW
			for (int i = dataSize; i< childSz; i++) {
				this.removeViewAt(dataSize);
			}
		}

		int rightPx = 0;
		if (null == params) {
//			rightPx = mCtx.getResources().getDimensionPixelSize(R.dimen.dp_4);
		}
		int startIndex=0;
		// 若有需要 替换VIEW
		int min = Math.min(dataSize, childSz);
		if (min > 0) {
			for (; startIndex < min; startIndex++) {
				PushTable item = temp.get(startIndex);
				AnimFrameLayout frame = (AnimFrameLayout) this
						.getChildAt(startIndex);
//				View itemView = mInflater.inflate(R.layout.push_item, null);

				initItemView(frame.getChildAt(0), item);

//				if(row>=datas.size()) {
//					frame.addView(itemView,new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, 
//							FrameLayout.LayoutParams.MATCH_PARENT, Gravity.CENTER));
//				}else {
//					frame.replaceViews(itemView, startIndex);
//				}
			}
		}
		for (; startIndex < dataSize; startIndex++) {
			AnimFrameLayout frame = (AnimFrameLayout) mInflater.inflate(
					R.layout.item_base, null);

			PushTable item = datas.get(startIndex);
			View itemView = mInflater.inflate(R.layout.push_item, null);

			initItemView(itemView, item);

			frame.replaceView(itemView, startIndex);
			params = new LinearLayout.LayoutParams(0,
					LinearLayout.LayoutParams.MATCH_PARENT, 1);
			if (startIndex == 0) {
				params.leftMargin = 0;
				params.rightMargin = rightPx;
			} else if (startIndex == dataSize - 1) {
				params.leftMargin = rightPx;
				params.rightMargin = 0;
			} else {
				params.leftMargin = rightPx;
				params.rightMargin = rightPx;
			}
			this.addView(frame, params);
		}
		

	}

	/***
	 * 绑定指定的VIEW
	 * 
	 * @param v
	 * @param data
	 */
	private void initItemView(View v, PushTable data) {
		v.setTag(data);
		v.setOnClickListener(this);
		// 标题或名称
		TextView  txtName  = (TextView) v.findViewById(R.id.txt_title);
		txtName.setText(data.name);
//		txtName = (TextView) v.findViewById(R.id.txt_score);
		// txtName.setText(String.valueOf(data.score / 10f)); 
		String imgUrl = data.iconUrl;
		ImageView imgView = (ImageView) v.findViewById(R.id.img_left);
		RelativeLayout.LayoutParams lp = (android.widget.RelativeLayout.LayoutParams) imgView
				.getLayoutParams();
		lp.width = Utilities.sIconTextureHeight;
		lp.height = Utilities.sIconTextureWidth;
		
		String url = (String) imgView.getTag();
		if (null == url || !url.equals(imgUrl)) {
			imgView.setTag(imgUrl);

			if(/*!Utilities.isLocalTheme()*/true) {
				x.image().bind(imgView, imgUrl, leftImgOption);
			}else {
				ImageLoader.getInstance().displayImage(
						imgUrl,
						new ImageViewWrapAware(imgView,
								false));
			}
			
			
		}
	}

	/***
	 * 获取美食(团队)项数据
	 */
	public List<PushTable> getDatas() {
		return datas;
	}

	/**
	 * 加载图片
	 */
	public void doBindImg() {
		int childSz = this.getChildCount();
		for (int i = 0; i < childSz; i++) {
			AnimFrameLayout frame = (AnimFrameLayout) this.getChildAt(i);
			ImageView imgLeft = (ImageView) frame.getChildAt(0).findViewById(
					R.id.img_left);
			if (imgLeft != null && imgLeft.getTag() != null) {
				String url = (String) imgLeft.getTag();
				if(/*!Utilities.isLocalTheme()*/true) {
					x.image().bind(imgLeft, url, leftImgOption);
				}else {
					ImageLoader.getInstance().displayImage(
							url,
							new ImageViewWrapAware(imgLeft,
									false));
				}
			}
		}
	}

	public PushViewLinearLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		mCtx = context;
		mInflater = LayoutInflater.from(mCtx);
		// TODO Auto-generated constructor stub
	}

	public PushViewLinearLayout(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onClick(View v) {
		PushTable t = (PushTable) v.getTag();
		if (t.linkUrl.startsWith("http")){
			Intent it = new Intent(mCtx, WebViewActivity.class);
			it.putExtra(WebViewActivity.P_URL, t.linkUrl);
			mCtx.startActivity(it);
			it = null;
		}
	   /* indexOfChild = ((ViewGroup)v.getParent()).indexOfChild(v);
	   ViewParent p = v.getParent().getParent();*/
	   View frame = (View) v.getParent();
	   indexOfChild =this.indexOfChild(frame);
	   	    
		mProp.clear();
		mProp.setProperty("position", indexOfChild+"");
		StatService.trackCustomKVEvent(mCtx, "CardPush", mProp);
	}

}
