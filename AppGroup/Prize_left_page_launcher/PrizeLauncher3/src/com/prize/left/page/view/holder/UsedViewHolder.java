package com.prize.left.page.view.holder;

import java.util.List;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.launcher3.IconCache;
import com.android.launcher3.ImageUtils;
import com.android.launcher3.Launcher;
import com.android.launcher3.LauncherAppState;
import com.android.launcher3.R;
import com.android.launcher3.Utilities;
import com.lqsoft.LqServiceUpdater.LqService;
import com.prize.left.page.bean.AppBean;
import com.prize.left.page.bean.ContactPerson;
import com.prize.left.page.ui.PushViewLinearLayout;
import com.prize.left.page.util.CommonUtils;
import com.prize.left.page.util.DBUtils;
import com.tencent.stat.StatService;

/**
 * 最近使用 Card Holder
 */
public class UsedViewHolder extends RecyclerView.ViewHolder {
	/**卡片标题*/
	public TextView titleTxt;
	/**删除按钮*/
	public ImageView imgRefresh;
	/**菜单/更多 按钮*/
	public ImageView imgMenu;
	/**应用容器*/
	private LinearLayout appContents;
	/**联系人容器*/
	private LinearLayout contactContents;
	
	/**
	 * 推送信息容器
	 */
	public PushViewLinearLayout push;
	
	private TextView txtExpand;
	
	private LayoutInflater mInflater;
	
	private RecyclerView.Adapter<RecyclerView.ViewHolder> mRecycleAdapter;
	
	private Context mCtx;
	
	private LinearLayout.LayoutParams mParams = null;
	
	private Activity mAct = null;
	/**中间变量*/
	private Rect tmpRect = new Rect();
	/**动画的左右图片*/
	private ImageView mImgLeft, mImgRight;
	
	private View mContactOptLay = null;
	
	private boolean isTouchOk = false;
	/**点击了哪个联系人*/
	private ContactPerson mTmpContact = null;
	
	private Bitmap mBitLeft = null, mBitRight = null;
	/**统一动画时间*/
	private final int ANIM_DURATION = 600;
	/**是否可以点击*/
	private boolean canClick = false;
	/**是否为打开动画*/
	private boolean isOpenAnim = false;
	
	private View mDividerLine;
	
	private int whichPos = -1;
	
	private View infoView = null;
	
	private View frameView = null;
	/*private ImageOptions imgOption = new ImageOptions.Builder()
    	.setSize(100, 100)
    	.setRadius(4)
    	//.setPlaceholderScaleType(ImageView.ScaleType.MATRIX)
    	.setImageScaleType(ImageView.ScaleType.CENTER_INSIDE)
    	.build();*/
	
	public UsedViewHolder(View v) {
		super(v);
		mCtx = v.getContext();
		titleTxt = (TextView) v.findViewById(R.id.txt_title);
		imgRefresh = (ImageView) v.findViewById(R.id.img_refresh);
		imgMenu = (ImageView) v.findViewById(R.id.img_more);
		
		mDividerLine = v.findViewById(R.id.divider_line);
		
		contactContents = (LinearLayout) v.findViewById(R.id.contact_content);
		push = (PushViewLinearLayout) v.findViewById(R.id.content);
		push.setUsedView(v);
		
		frameView = v.findViewById(R.id.contact_frame);
		
		contactContents.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				
				int act = event.getAction() & MotionEvent.ACTION_MASK;
				if (act != MotionEvent.ACTION_UP)
					return true;
				if (isTouchOk)
					return false;
				isTouchOk = true;
				int x = (int)event.getX(0);
				int y = (int)event.getY(0);
				
				whichPos = pointWhich(x, y);
				if (whichPos == -1) {
					isTouchOk = false;
					return false;
				}
				
				layW = contactContents.getWidth();
				
				mContactOptLay.setX(tmpRect.width());
				mContactOptLay.setY(tmpRect.top);
				// 截图 赋值给控件
				cutImgAndTo();
				// 开始执行动画
				openAnim();
				
				isTouchOk = false;
				return true;
			}
		});
		
		appContents = (LinearLayout) v.findViewById(R.id.app_content);
		
		txtExpand = (TextView) v.findViewById(R.id.txt_expand);
		
		mImgLeft = (ImageView)v.findViewById(R.id.img_left);
		mImgLeft.setOnClickListener(mClick);
		
		mImgRight = (ImageView)v.findViewById(R.id.img_right);
		mImgRight.setOnClickListener(mClick);
		
		mContactOptLay = v.findViewById(R.id.contact_opt_lay);
		
		mContactOptLay.setOnClickListener(mClick);
		
		mContactOptLay.findViewById(R.id.img_phone).setOnClickListener(mClick);
		mContactOptLay.findViewById(R.id.img_mms).setOnClickListener(mClick);
		infoView = mContactOptLay.findViewById(R.id.img_info);
		infoView.setOnClickListener(mClick);
		
		mInflater = LayoutInflater.from(mCtx);
		mParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1);
	}
	/**屏宽*/
	private int layW = 0;
	/***
	 * 截图并赋值
	 */
	private void cutImgAndTo() {
		
		contactContents.setDrawingCacheEnabled(true);
		
		int h = tmpRect.height();
		Bitmap src = contactContents.getDrawingCache();
		
		if (mBitLeft != null)
			mBitLeft.recycle();
		
		mBitLeft = Bitmap.createBitmap(src, 0, 0, tmpRect.right, h);
		
		mImgLeft.setImageBitmap(mBitLeft);
		
		if (mBitRight != null)
			mBitRight.recycle();
		mBitRight = null;
		if (tmpRect.right < layW - 50) {
			mBitRight = Bitmap.createBitmap(src, tmpRect.right, 0, layW - tmpRect.right, h);
		}
		
		mImgRight.setImageBitmap(mBitRight);
		
		contactContents.setDrawingCacheEnabled(false);
		
		contactContents.setVisibility(View.GONE);
		
		mImgLeft.setVisibility(View.VISIBLE);
		mImgRight.setVisibility(View.VISIBLE);
		
		mContactOptLay.setVisibility(View.VISIBLE);
	}
	
	
	public void doPost() {
		push.doPost();
	}
	
	public void doRefresh() {
		push.doRefresh();
	}
	
	private static final String TRANSLATION_X = "translationX";
	/**缩放轴*/
	private static final String SCALE = "scaleY";
	
	private static final String ALPHA = "alpha";
	
	private AnimatorSet mAnimSet = null;
	/***
	 * 打开动画
	 */
	private void openAnim() {
		// ANIM_DURATION;
		isOpenAnim = true;
		if (mAnimSet != null)
			mAnimSet.end();
		mAnimSet = new AnimatorSet();
		ObjectAnimator a = null;
		if (tmpRect.left > 10) {
			a = ObjectAnimator.ofFloat(mImgLeft, TRANSLATION_X, 0, -tmpRect.left);
		}
		
		ObjectAnimator b = null;
		if (tmpRect.right < 700) {
			b = ObjectAnimator.ofFloat(mImgRight, TRANSLATION_X, 0, layW - tmpRect.left);
		}
		
		float fromScale = (3 - whichPos) * 0.25f;
		
		ObjectAnimator c = ObjectAnimator.ofFloat(mContactOptLay, SCALE, fromScale, 1f);
		
		ObjectAnimator d = ObjectAnimator.ofFloat(mContactOptLay, ALPHA, 0, 1f);
		d.addListener(mAnimListener);
		
		ObjectAnimator e = ObjectAnimator.ofFloat(mContactOptLay, TRANSLATION_X, 0, tmpRect.width());
		
		if (a == null)
			mAnimSet.playTogether(b, c, d);
		else if (b == null) {
			mAnimSet.playTogether(a, c, d, e);
		}
		else
			mAnimSet.playTogether(a, b, c, d, e);
		
		mAnimSet.setDuration(ANIM_DURATION);
		mAnimSet.setInterpolator(mInterpolater);
		// mAnimSet.setStartDelay(pos * ANIM_DURATION);
		mAnimSet.start();
	}
	
	private final TimeInterpolator mInterpolater = new AccelerateDecelerateInterpolator();
	
	/***
	 * 关闭动画
	 */
	private void closeAnim() {
		isOpenAnim = false;
		canClick = false;
		if (mAnimSet != null)
			mAnimSet.end();
		mAnimSet = new AnimatorSet();
		ObjectAnimator a = null;
		if (tmpRect.left > 10) {
			a = ObjectAnimator.ofFloat(mImgLeft, TRANSLATION_X, -tmpRect.left, 0);
		}
		
		ObjectAnimator b = null;
		if (tmpRect.right < 700) {
			b = ObjectAnimator.ofFloat(mImgRight, TRANSLATION_X, layW - tmpRect.left, 0);
		}
		
		float toScale = (3 - whichPos) * 0.25f;
		ObjectAnimator c = ObjectAnimator.ofFloat(mContactOptLay, SCALE, 1f, toScale);
		
		ObjectAnimator d = ObjectAnimator.ofFloat(mContactOptLay, ALPHA, 1f, 0);
		d.addListener(mAnimListener);
		
		ObjectAnimator e = ObjectAnimator.ofFloat(mContactOptLay, TRANSLATION_X, tmpRect.width(), 0);
		
		if (a == null)
			mAnimSet.playTogether(b, c, d);
		else if (b == null) {
			mAnimSet.playTogether(a, c, d, e);
		}
		else
			mAnimSet.playTogether(a, b, c, d, e);
		
		mAnimSet.setDuration(ANIM_DURATION);
		mAnimSet.setInterpolator(mInterpolater);
		mAnimSet.start();
	}
	
	private Animator.AnimatorListener mAnimListener = new Animator.AnimatorListener() {

		@Override
		public void onAnimationCancel(Animator animation) {
			
		}

		@Override
		public void onAnimationEnd(Animator animation) {
			// TODO Auto-generated method stub
			if (isOpenAnim) {
				canClick = true;
			}
			else {
				mImgLeft.setVisibility(View.GONE);
				mImgRight.setVisibility(View.GONE);
				mContactOptLay.setVisibility(View.INVISIBLE);
				contactContents.setVisibility(View.VISIBLE);
			}
		}

		@Override
		public void onAnimationRepeat(Animator animation) {
			
		}

		@Override
		public void onAnimationStart(Animator animation) {
			
		}
		
	};
	/***
	 * 点击在哪个子控件身上
	 * @return -1 none, else OK
	 */
	private int pointWhich(int x, int y) {
		mTmpContact = null;
		tmpRect.setEmpty();
		int sz = contactContents.getChildCount();
		if (sz < 1)
			return -1;
		int c = -1;
		for (int i=0; i<sz; i++) {
			View v = contactContents.getChildAt(i);
			v.getHitRect(tmpRect);
			if (tmpRect.contains(x, y)) {
				c = i;
				mTmpContact = (ContactPerson)v.getTag();
				break;
			}
		}
		if (mTmpContact != null && mTmpContact.contactId > -1) {
			infoView.setVisibility(View.VISIBLE);
		}
		else
			infoView.setVisibility(View.INVISIBLE);
		return c;
	}
	
	public void setAdapter(RecyclerView.Adapter<RecyclerView.ViewHolder> ad) {
		mRecycleAdapter = ad;
	}
	
	private View.OnClickListener mExClick = null;
	private ComponentName c=new ComponentName("com.android.contacts", "com.android.contacts.activities.peopleactivity");
	
	public void setExpandClick(View.OnClickListener clk) {
		mExClick = clk;
		if (txtExpand != null)
			txtExpand.setOnClickListener(mExClick);
	}
	
	
	public void disableViews(int status ) {
		switch (status) {
		case 0:
			mDividerLine.setVisibility(View.VISIBLE);
			appContents.setVisibility(View.VISIBLE);
			mDividerLine.setVisibility(View.VISIBLE);
			frameView.setVisibility(View.VISIBLE);
			push.setVisibility(View.GONE);
			break;
		case 1:

			mDividerLine.setVisibility(View.GONE);
			appContents.setVisibility(View.GONE);
			mDividerLine.setVisibility(View.VISIBLE);
			frameView.setVisibility(View.VISIBLE);
//			push.setVisibility(View.VISIBLE);
			break;
		case 2:

			mDividerLine.setVisibility(View.VISIBLE);
			appContents.setVisibility(View.VISIBLE);
			mDividerLine.setVisibility(View.GONE);
			frameView.setVisibility(View.GONE);
//			push.setVisibility(View.GONE);
			
			break;

		default:
			break;
		}
	}
	/***
	 * 设置最近应用数据
	 * @param ls
	 */
	public void setAppDatas(List<AppBean> ls) {
		
		/*if (mRecycleAdapter != null && pos != -1)
			mRecycleAdapter.notifyItemChanged(pos);*/
		
		disableViews(DBUtils.disablePushView());
		
		if (ls == null || ls.size() < 1) {
			mDividerLine.setVisibility(View.GONE);
			appContents.setVisibility(View.GONE);
			return;
		}
		
		//mDividerLine.setVisibility(View.VISIBLE);
//		appContents.setVisibility(View.VISIBLE);
		
		int cSize = appContents.getChildCount();
		
		int s = ls.size();
		
		if (cSize > s) {// 先删除多余的VIEW
			for (int i = s; i < cSize; i++) {
				appContents.removeViewAt(s);
			}
		}
		
		cSize = appContents.getChildCount();
		
		int z = Math.min(s, 4);
		
		for (int i=0; i<z; i++) {
			AppBean a = ls.get(i);
			if (i < cSize) {
				View v = appContents.getChildAt(i);
				v.setTag(a);
				v.setOnClickListener(mClick);
				initAppView(a, v);
			}
			else {
				View v = mInflater.inflate(R.layout.left_app_item, null);
				v.setTag(a);
				v.setOnClickListener(mClick);
				initAppView(a, v);
				appContents.addView(v, mParams);
			}
		}
	}
	
	/**
	 * 赋值于应用view
	 * @param a
	 * @param v
	 */
	private void initAppView(final AppBean a, final View v) {
		final ImageView img = (ImageView)v.findViewById(R.id.img_top);
		
		/*Thread t = new Thread(new Runnable() {

			@Override
			public void run() {

				final Bitmap result = IconCache.getThemeIcon(
						a.it.getComponent(), null, true, null, v.getContext());
				v.post(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub

						img.setImageBitmap(result);
					}
				});
			}
		});
		t.start();*/
		
		
		AsyncTask<Bitmap, Void, Bitmap> task = new AsyncTask<Bitmap, Void, Bitmap>() {

			@Override
			protected Bitmap doInBackground(Bitmap... arg0) {
				final Bitmap result = IconCache.getThemeIcon(
						a.it.getComponent(), null, true, null, v.getContext());
				return result;
			}

			@Override
			protected void onPostExecute(Bitmap result) {
				img.setImageBitmap(result);
				super.onPostExecute(result);
			}
			
			
		};
		task.execute();
		TextView txt = (TextView)v.findViewById(R.id.txt_title);
		txt.setText(a.title);
	}
	
	/***
	 * 设置最近联系人数据
	 * @param ls
	 */
	public void setContactDatas(List<ContactPerson> ls) {
		

		disableViews(DBUtils.disablePushView());
		
		if (ls == null || ls.size() < 1) {
			mDividerLine.setVisibility(View.GONE);
			frameView.setVisibility(View.GONE);
			return;
		}
	/*	mDividerLine.setVisibility(View.VISIBLE);
		frameView.setVisibility(View.VISIBLE);
		appContents.setVisibility(View.GONE);
		push.setVisibility(View.GONE);*/
		
		int cSize = contactContents.getChildCount();
		
		int s = ls.size();
		
		if (cSize > s) {// 先删除多余的VIEW
			for (int i = s; i < cSize; i++) {
				contactContents.removeViewAt(s);
			}
		}
		
		cSize = contactContents.getChildCount();
		
		int z = Math.min(s, 4);
		
		for (int i=0; i<z; i++) {
			ContactPerson a = ls.get(i);
			if (i < cSize) {
				View v = contactContents.getChildAt(i);
				v.setId(i);
				v.setTag(a);
				initContactView(a, v);
			}
			else {
				View v = mInflater.inflate(R.layout.left_app_item, null);
				v.setId(i);
				v.setTag(a);
				initContactView(a, v);
				contactContents.addView(v, mParams);
			}
		}
	}
	
	/**
	 * 赋值于联系人view
	 * @param a
	 * @param v
	 */
	private void initContactView(ContactPerson data, final View v) {
		final ImageView img = (ImageView)v.findViewById(R.id.img_top);
		if (data.headIco != null && !data.headIco.isRecycled()) {
			if(data.headIco!=null) {
				Bitmap headIco=data.headIco;
				if(Utilities.isLocalTheme()) {

					AsyncTask<Bitmap, Void, Bitmap> task = new AsyncTask<Bitmap, Void, Bitmap>() {

						@Override
						protected Bitmap doInBackground(Bitmap... arg0) {
							Bitmap result = IconCache.getLqIcon(null, arg0[0], true, null);
							return result;
						}

						@Override
						protected void onPostExecute(Bitmap result) {
							img.setImageBitmap(result);
							super.onPostExecute(result);
						}
						
						
					};
					task.execute(data.headIco);
					
					
				}
				else {
					img.setImageResource(R.drawable.ico_person_head);
				}
			}
		}else {
			if(Utilities.isLocalTheme()) {
				


				AsyncTask<Bitmap, Void, Bitmap> task = new AsyncTask<Bitmap, Void, Bitmap>() {

					@Override
					protected Bitmap doInBackground(Bitmap... arg0) {

						Bitmap result = IconCache.getThemeIcon(
								c,
								arg0[0], true, null, v.getContext());
						return result;
					}

					@Override
					protected void onPostExecute(Bitmap result) {
						img.setImageBitmap(result);
						super.onPostExecute(result);
					}
					
					
				};
				task.execute(data.headIco);

			}else{
				img.setImageBitmap(ImageUtils.drawableToBitmap(v.getContext().getDrawable(R.drawable.ico_person_head)));
			}
			
		}
		
		TextView txt = (TextView)v.findViewById(R.id.txt_title);
		String str = data.name;
		if (TextUtils.isEmpty(str))
			str = data.phoneNum;
		txt.setText(str);
	}
	
	private View.OnClickListener mClick = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
				case R.id.app_item:
//					if(!isPageMoving(mCtx)){
						StatService.trackCustomEvent(mCtx, "CardPush", "");
						AppBean b = (AppBean)v.getTag();
						mCtx.startActivity(b.it);
						if (mAct != null)
							mAct.overridePendingTransition(R.anim.left_in_from_right, 0);
//					}
					break;
				case R.id.contact_opt_lay: //操作区点击事件
				case R.id.img_left:
				case R.id.img_right:
					if (canClick)
						// 关闭动画
						closeAnim();
					break;
				case R.id.img_phone:
					StatService.trackCustomEvent(mCtx, "CardPush", "");
					if (canClick)
						CommonUtils.jumpToCallPhone(mAct, mTmpContact.phoneNum, R.anim.left_in_from_right);
					break;
				case R.id.img_mms:
					StatService.trackCustomEvent(mCtx, "CardPush", "");
					if (canClick)
						CommonUtils.jumpToSms(mAct, mTmpContact.phoneNum, R.anim.left_in_from_right);
					break;
				case R.id.img_info:
					StatService.trackCustomEvent(mCtx, "CardPush", "");
					if (canClick) {
						/*QuickContact.showQuickContact(mAct, v, ContentUris.withAppendedId(
								ContactsContract.Contacts.CONTENT_URI, 453),
			                    3, null);*/
						Intent it = new Intent();
						it.setAction(Intent.ACTION_VIEW);
						it.setData(ContentUris.withAppendedId(
								ContactsContract.Contacts.CONTENT_URI, mTmpContact.contactId));
						mCtx.startActivity(it);
						if (mAct != null)
							mAct.overridePendingTransition(R.anim.left_in_from_right, 0);
					}
					break;
			}
			
		}
	};
	/***
	 * 设置activity
	 * @param act
	 */
	public void setActivity(Activity act) {
		mAct = act;
	}
	/***
	 * 设置展开按钮的可见性
	 * @param isVisible
	 */
	public void setExpandVisible(boolean isVisible) {
		if (null == txtExpand)
			return ;
		if (isVisible)
			txtExpand.setVisibility(View.VISIBLE);
		else
			txtExpand.setVisibility(View.INVISIBLE);
	}
	/***
	 * 设置展开按钮的文本
	 * @param isVisible
	 */
	public void setExpandText(boolean isExpand) {
		if (null == txtExpand)
			return ;
		if (isExpand)
			txtExpand.setText(R.string.str_unexpand);
		else
			txtExpand.setText(R.string.str_expand);
	}
	
	public static boolean isPageMoving(Context ctx){
		if(ctx instanceof Launcher){
			ctx = (Launcher)ctx;
			return ((Launcher) ctx).getworkspace().isPageMoving();
		}
		return false;
	}
	
}
