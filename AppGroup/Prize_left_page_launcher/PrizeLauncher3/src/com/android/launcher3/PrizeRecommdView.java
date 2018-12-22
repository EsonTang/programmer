package com.android.launcher3;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.TextView;

import com.android.download.DownLoadService;
import com.android.gallery3d.util.LogUtils;
import com.android.launcher3.view.DownLoadlDialog;
import com.android.launcher3.view.UnInstallDialog;
import com.prize.app.net.datasource.base.AppsItemBean;
import com.prize.left.page.model.IResponse;
import com.prize.left.page.model.RecommdModel;
import com.prize.left.page.response.RecommdResponse;
import com.prize.left.page.util.ClientInfo;
import com.prize.left.page.util.ToastUtils;

public class PrizeRecommdView extends PrizeScrollView implements
		IResponse<RecommdResponse> {
	private com.android.launcher3.CellLayout.LayoutParams lp;
	AppsItemBean tag;

	private View view;
	boolean finish = true;

	private boolean isRunning = false;
	private DownLoadlDialog applyDialog;
	@Override
	public void onClick(View v) {
		
		if (ClientInfo.networkType == ClientInfo.NONET) {
			ToastUtils.showToast(mContext, R.string.net_error);
			return;
		}
		if (v.getId() == R.id.recommd_refresh) {

			RecommdModel.getInstance(mContext).doPostRefresh();
			return;
		}
		if(ClientInfo.getInstance(mContext).networkType!=ClientInfo.WIFI) {
			downloadIfNeed(v);
		}else {
			download(v);
		}
	}

	
	public void download(View v) {

		if (isRunning)
			return;
		isRunning = true;

		try {
			final DownLoadService downLoadService = LauncherAppState.getInstance().getModel()
					.getDownLoadService();
			final Folder fi = mLauncher.getworkspace().getOpenFolder();
			CellLayout layout = fi.getContent();
			fi.setupContentForNumItems(fi.getItemCount() + 1);

			View child = layout.getShortcutsAndWidgets().getChildAt(0);
			ShortcutInfo info = (ShortcutInfo) child.getTag();
			view = v;
			finish = true;
			tag = (AppsItemBean) v.getTag();
			tag.conatiner = (int) info.container;
			mDatas.remove(tag);
			fi.getViewTreeObserver().addOnGlobalLayoutListener(
					new OnGlobalLayoutListener() {

						@Override
						public void onGlobalLayout() {
							if (finish == true) {
								finish = false;
								mLauncher.getworkspace().getOpenFolder()
										.scrollToEnd();
								postDelayed(new Runnable() {

									@Override
									public void run() {

										view.setVisibility(View.GONE);
										push((RecomdIcon) view, new Runnable() {

											@Override
											public void run() {
												try {
													startDownLoad(fi,
															downLoadService);
												} catch (Exception e) {
													// TODO: handle exception
												}
											}
										});
									}
								}, 200);
							}
						}
					});

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void startDownLoad(Folder fi,DownLoadService downLoadService) {

		isRunning = false;
		int f[] = new int[2];
		int t[] = new int[2];
		int top = (int) fi
				.getTranslationY();
		fi.refreshContentHeight(
				fi.getItemCount() + 1,
				f, t);
		downLoadService
				.startDownLoadTask(tag);

	
		
	}
	
	
	
	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
	}


	public void downloadIfNeed(final View view) {

		if (applyDialog == null)
			applyDialog = new DownLoadlDialog(mContext, R.style.add_dialog);

		applyDialog.getWindow().setGravity(Gravity.BOTTOM);
		applyDialog.getWindow().setWindowAnimations(R.style.mystyle); // 添加动画
		applyDialog
				.setOnItemClick(new com.android.launcher3.view.DownLoadlDialog.OnUninstallClick() {

					@Override
					public void onClick(boolean which) {
						if (which) {
							download(view);
							applyDialog.dismiss();
						} else {
							applyDialog.dismiss();
						}

					}

				});
		applyDialog.show();
	}
	
	public void pushTofolderContent() {

	}

	public void setupLp(CellLayout v) {

		int[] cell = new int[2];
		v.getLastNewPosition(cell);
		lp = new CellLayout.LayoutParams(cell[0], cell[1], 1, 1);
		v.getShortcutsAndWidgets().setupLp(lp);
	}

	public void push(RecomdIcon v, Runnable r) {
		Rect mTempRect = new Rect();
		CellLayout layout = mLauncher.getworkspace().getOpenFolder()
				.getContent();
		mDragLayer.getDescendantRectRelativeToSelf(v, mTempRect);// 这个就是读取folderIcon
		Rect layoutLp = new Rect();
		mDragLayer.getDescendantRectRelativeToSelf(layout, layoutLp);// 这个就是读取folderIcon
		Drawable d = v.getCompoundDrawables()[1];
		View child = layout.getShortcutsAndWidgets().getChildAt(0);
		int left = child.getWidth() / 2 - Utilities.sIconTextureHeight / 2;
		int top = child.getPaddingTop();
		setupLp(layout);

		int finalX = lp.x + layoutLp.left + left;
		int finalY = lp.y + layoutLp.top + top;

		int srcX = mTempRect.left;
		int srcY = mTempRect.top;
		RecommdDragView dragView = new RecommdDragView(v.getContext(), srcX,
				srcY, finalX, finalY, d, layout, r);
		dragView.show();
		
	}

	public void setPckageNames(String pckageNames) {
		this.packageNames = pckageNames;
	}

	String packageNames;

	public void open() {
		if (!Launcher.isf) {
			setupLayout();
		}
		RecommdModel.getInstance(mContext).setIResponse(this);
		RecommdModel.getInstance(mContext).mPagedIndex = 1;
		RecommdModel.getInstance(mContext).doGet(packageNames);
		this.show();
	}

	private Launcher mLauncher;
	private DragLayer mDragLayer;
	private List<AppsItemBean> mDatas;

	@Override
	protected void onDataReady(int width, int height) {
		// TODO Auto-generated method stub
		super.onDataReady(width, height);

	}

	public PrizeRecommdView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mLauncher = (Launcher) context;
		mDragLayer = mLauncher.getDragLayer();
	}

	public PrizeRecommdView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mLauncher = (Launcher) context;
		mDragLayer = mLauncher.getDragLayer();
	}

	@Override
	protected boolean applyInfo(Object t, View icon) {

		return true;
	}

	@Override
	public void syncPages() {

		for (int i = 0; i < mPages; i++) {

			if (i >= getChildCount()) {
				PrizeCellLayout page = new PrizeCellLayout(getContext());
				PagedView.LayoutParams params = new PagedView.LayoutParams(
						LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
				addView(page, params);
				/*page.setScaleX(0.88f);
				page.setScaleY(0.88f);*/
			}
		}
		this.setScaleX(0.88f);
		this.setScaleY(0.88f);

	}

	@Override
	protected View syncGetLayout(ViewGroup layout, Object t) {
		View icon = mInflate.inflate(R.layout.recommd_icon, layout, false);// 创建一个icon项
		applyIconInfo(t, icon);
		icon.setOnClickListener(mLauncher);
		return icon;
	}

	static PrizeRecommdView fromXml(Context context) {
		return (PrizeRecommdView) LayoutInflater.from(context).inflate(
				R.layout.recommdp, null);
	}

	public void close() {

		DragLayer parent = (DragLayer) getParent();
		if (parent != null) {
			parent.removeView(this);
		}
	}

	public void setupLayout() {
		DragLayer parent = (DragLayer) ((Activity) mContext)
				.findViewById(R.id.drag_layer);
		if (this.getParent() == null) {
			parent.addView(this, new DragLayer.LayoutParams(mLauncher
					.getHotseat().getWidth(), mLauncher.getHotseat()
					.getHeight()));
		} else {
			LogUtils.i("zhouerlong", "Opening recommdView (" + this
					+ ") which already has a parent (" + this.getParent()
					+ ").");
		}
		DragLayer.LayoutParams lp = (DragLayer.LayoutParams) getLayoutParams();

		Rect rect = new Rect();
		float scale = parent.getDescendantRectRelativeToSelf(
				mLauncher.getHotseat(), rect);// 这个就是读取folderIcon
		int x = rect.left;
		int y = rect.top;
		lp.x = x;
		lp.y = y;
		lp.topMargin = y;
		lp.width = (int) (rect.width() * scale);
		lp.height = (int) (rect.height() * scale);

	}

	public void filter(List<AppsItemBean> datas) {
		Iterator<AppsItemBean> it = datas.iterator();
		String is = "com.sohu.inputmethod.sogou;com.android.music;com.baidu.input;com.iflytek.inputmethod";
		
		List<String> inputs = Arrays.asList(is.split(";"));
		while (it.hasNext()) {
			AppsItemBean t = it.next();

			boolean isExits = LauncherModel.shortcutExistsWithPkg(mContext,
					t.packageName);
			if (isExits) {
				it.remove();
			}
			if(inputs.contains(t.packageName)) {
				it.remove();
			}

		}
		for (AppsItemBean app : datas) {
			boolean isExits = LauncherModel.shortcutExistsWithPkg(mContext,
					app.packageName);
			if (isExits) {
				datas.remove(app);
			}
		}
	}
	
	public void nextPage() {

		RecommdModel.getInstance(mContext).doPostRefresh();
	}

	public void refresh() {
		try {
			filter(mDatas);
		} catch (Exception e) {
			// TODO: handle exception
		}

		this.removeAllViews();

		if (mDatas == null || mDatas.size() <= 0) {

			RecommdLinearLayout p = (RecommdLinearLayout) this.getParent();
			View empty = p.findViewById(R.id.emptiy);
			empty.setVisibility(View.VISIBLE);
			nextPage();
			
		}else {
			RecommdLinearLayout p = (RecommdLinearLayout) this.getParent();
			View empty = p.findViewById(R.id.emptiy);
			empty.setVisibility(View.GONE);
		}
		setDatas(null);
		setDatas(mDatas);
		resetDataIsReady();
		updatePageCounts();
		this.requestLayout();
	}

	@Override
	public void onResponse(RecommdResponse resp) {

		try {
			mDatas = resp.data.apps;
			if (resp.data.apps.size() > 0) {
				RecommdLinearLayout p = (RecommdLinearLayout) this.getParent();
				p.setVisibility(View.VISIBLE);
			}
			refresh();

			RecommdLinearLayout p = (RecommdLinearLayout) getParent();
			p.end();
		} catch (Exception e) {
			e.printStackTrace();
			RecommdLinearLayout p = (RecommdLinearLayout) this.getParent();
			p.close();
		}
	}

}
