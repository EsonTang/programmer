package com.android.launcher3;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.android.gallery3d.util.LogUtils;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.util.ArrayMap;
import android.util.Log;
import android.view.View;

public class LauncherUnlock {

	public static LauncherUnlock mInstace = null;

	Collection<Animator> mObjs = new ArrayList<Animator>();
	public static int DELAY = 1000;
	
	public static int REPEAT = 8;

	AnimatorSet mSet = new AnimatorSet();

	private int mCountY;
	private int mCountX;

	public static LauncherUnlock getInstace() {
		if (null == mInstace) {
			mInstace = new LauncherUnlock();
			DELAY =Utilities.getUnlockDuration();
		}
		return mInstace;
	}

	public Collection<Animator> getObjs() {
		return mObjs;
	}

	public String getTopActivityName(Context context) {
		String topActivityName = null;
		ActivityManager activityManager = (ActivityManager) (context
				.getSystemService(android.content.Context.ACTIVITY_SERVICE));
		List<RunningTaskInfo> runningTaskInfos = activityManager
				.getRunningTasks(1);
		if (runningTaskInfos != null) {
			ComponentName f = runningTaskInfos.get(0).topActivity;
			String topActivityClassName = f.getClassName();
			topActivityName = topActivityClassName;
		}
		return topActivityName;
	}

	public void unlock() {
		try {
			if (mUnlockReady) {
				mSet = new AnimatorSet();
				if(mSet!=null&&mObjs.size()>0) {
				mSet.playTogether(mObjs);
				mSet.start();
				mObjs.clear();
				mSet=null;
				}
			}else {
				reset();
			}
			mUnlockReady = true;
		} catch (Exception e) {
		}
	}

	boolean mUnlockReady = false;

	Handler mHandler = new Handler();

	private CellLayout c;

	private CellLayout hosetCell;

	private Launcher mLauncher;
	
	private void reset() {
		if(mObjs.size()>0) {
		ShortcutAndWidgetContainer shot = c.getShortcutsAndWidgets();
		for (int i = 0; i < shot.getChildCount(); i++) {
			View view = shot.getChildAt(i);
			view.setTranslationY(0);
		}

		shot = hosetCell.getShortcutsAndWidgets();
		for (int i = 0; i < shot.getChildCount(); i++) {
			View view = shot.getChildAt(i);
			view.setTranslationY(0);
		}
		View page = mLauncher.getPageIndicators();
		page.setTranslationY(0);
		}
		mObjs.clear();

	}

	public void onPause(CellLayout c, CellLayout hosetCell, Launcher launcher,
			int time,boolean open) {
		if (c == null || hosetCell == null) {
			return;
		}
		mUnlockReady = false;
		this.c = c;
		c.enableHardwareLayer(true);
		this.hosetCell = hosetCell;
		this.mLauncher = launcher;
		String name = getTopActivityName(launcher);
		if (name != null && name.contains(launcher.getPackageName())) {
//			mHandler.postDelayed(new TimeOut(), 0);

			if (!mUnlockReady) {
				mUnlockReady = true;
				onPause(c, hosetCell, mLauncher,open);
			}
		}
	}

	public void onPause(CellLayout c, CellLayout hosetCell, Launcher launcher,boolean open) {
		if (c == null || hosetCell == null) {
			return;
		}
		mObjs.clear();
		mCountY = c.getCountY();
		mCountX = c.getCountX();
		ShortcutAndWidgetContainer shot = c.getShortcutsAndWidgets();
		for (int i = 0; i < shot.getChildCount(); i++) {
			View view = shot.getChildAt(i);
			createAnimation(view, view.getHeight() * REPEAT, getDelay(view, false), open);
		}
		

		LogUtils.i("zhouerlong", "ACTION_SCREEN_ON::::::OK CellLayout");

		shot = hosetCell.getShortcutsAndWidgets();
		for (int i = 0; i < shot.getChildCount(); i++) {
			View view = shot.getChildAt(i);
			createAnimation(view, view.getHeight() * REPEAT, getDelay(view, true),open);
		}

		LogUtils.i("zhouerlong", "ACTION_SCREEN_ON::::::OK--hotseat");
		View page = launcher.getPageIndicators();
		View v = shot.getChildAt(0);
		if(v!=null)
		createAnimation(page, v.getHeight() * REPEAT,
				getDelay(shot.getChildAt(0), true),open);
		

		LogUtils.i("zhouerlong", "ACTION_SCREEN_ON::::::OK--pageIndicator");

	}

	public ValueAnimator createAnimation(final View view, int startPos,
			int duration,boolean open) {
		PropertyValuesHolder holder;
		if(open) {
			view.setTranslationY(0);
			 holder = PropertyValuesHolder.ofFloat("translationY", 0,startPos);
		}else {
			view.setTranslationY(startPos);
			 holder = PropertyValuesHolder.ofFloat("translationY", startPos,0);
		}
		ValueAnimator bounceAnim = LauncherAnimUtils.ofPropertyValuesHolder(view,
				/*PropertyValuesHolder.ofFloat("alpha", 1f),*/
				holder);
		bounceAnim.setDuration(duration);
		bounceAnim.setInterpolator(new DecelerateInterpolator());
		bounceAnim.addListener(new AnimatorListener() {

			@Override
			public void onAnimationStart(Animator arg0) {
				view.setLayerType(View.LAYER_TYPE_HARDWARE, null);

			}

			@Override
			public void onAnimationRepeat(Animator arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onAnimationEnd(Animator arg0) {
				view.setLayerType(View.LAYER_TYPE_NONE, null);

			}

			@Override
			public void onAnimationCancel(Animator arg0) {
				view.setLayerType(View.LAYER_TYPE_NONE, null);

			}
		});
		mObjs.add(bounceAnim);
//		mObjs.add(alpha);
		return bounceAnim;

	}
	
	
	/*public ObjectAnimator createAnimation(final View view, int startPos,
			int duration) {

		ObjectAnimator tra = ObjectAnimator.ofFloat(view, "translationY",
				startPos, 0);
		ObjectAnimator alpha = ObjectAnimator.ofFloat(view, "alpha", 0, 1f);
		tra.setDuration(duration);
		alpha.setDuration(duration);
		tra.setInterpolator(new DecelerateInterpolator());
		alpha.setInterpolator(new DecelerateInterpolator());
		tra.addListener(new AnimatorListener() {

			@Override
			public void onAnimationStart(Animator arg0) {
				view.setLayerType(View.LAYER_TYPE_HARDWARE, null);

			}

			@Override
			public void onAnimationRepeat(Animator arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onAnimationEnd(Animator arg0) {
				view.setLayerType(View.LAYER_TYPE_NONE, null);

			}

			@Override
			public void onAnimationCancel(Animator arg0) {
				view.setLayerType(View.LAYER_TYPE_NONE, null);

			}
		});
		mObjs.add(tra);
//		mObjs.add(alpha);
		return tra;

	}*/

	public void play() {
		mSet.playTogether(mObjs);
	}

	public int getDelay(View v, boolean ishotseat) {
		int result = 0;
		ItemInfo info = (ItemInfo) v.getTag();
		if (info == null) {
			return -1;
		}

		float progress = 0f;
	/*	if (!ishotseat) {
			progress = 0.05f * (Utilities.random(mCountX*mCountY));
		} else {
			progress = 0.05f * (Utilities.random(mCountX*mCountY));
		}*/
		

		if (!ishotseat) {
			progress = 0.05f * (info.cellY * mCountX + info.cellX);
		} else {
			progress = 0.05f * (mCountY * mCountX +info.cellX);
		}
		float pow = (float) (1.0f - (1.0f - progress) * (1.0f - progress));
		result = (int) ((-progress + 2) * DELAY);

		return result;
	}

}
