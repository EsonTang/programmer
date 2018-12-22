/*
 * Copyright (C) 2013 AChep@xda <artemchep@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.prize.appcenter.ui.util;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.prize.app.util.WindowMangerUtils;
import com.prize.appcenter.R;

/**
 * 类描述：改变actionbar工具类
 * 
 * @author huanglingjun
 * @version 版本
 */
public class TopicFadingActionBarHelper {

	private static final String TAG = "TopicFadingActionBarHelper";

	private int mAlpha = 0;
	private Drawable mDrawable;
	private boolean isAlphaLocked;

	private final View mActionBar;
	private final Window mWindow;
	private final Context mContext;

	public TopicFadingActionBarHelper(final Context context,
			final Window window, final View actionBar) {
		mActionBar = actionBar;
		mWindow = window;
		mContext = context;
	}

	public TopicFadingActionBarHelper(final Context context,
			final Window window, final View actionBar, final Drawable drawable) {
		mActionBar = actionBar;
		mWindow = window;
		mContext = context;
		setActionBarBackgroundDrawable(drawable);
	}

	public void setActionBarBackgroundDrawable(Drawable drawable) {
		setActionBarBackgroundDrawable(drawable, true);
	}

	public void setActionBarBackgroundDrawable(Drawable drawable, boolean mutate) {
		mDrawable = mutate ? drawable.mutate() : drawable;
		mActionBar.setBackgroundDrawable(mDrawable);
		if (mAlpha == 255) {
			// if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
			// mAlpha = mDrawable.getAlpha();
		} else {
			setActionBarAlpha(mAlpha);
		}
	}

	/**
	 * An {@link android.app.ActionBar} background drawable.
	 * 
	 * @see #setActionBarBackgroundDrawable(android.graphics.drawable.Drawable)
	 * @see #setActionBarAlpha(int)
	 */
	public Drawable getActionBarBackgroundDrawable() {
		return mDrawable;
	}

	/**
	 * Please use this method for global changes only! This is helpful when you
	 * need to provide something like Navigation drawer: lock ActionBar and set
	 * {@link android.graphics.drawable.Drawable#setAlpha(int)} to
	 * {@link #getActionBarBackgroundDrawable()} directly.
	 * 
	 * @param alpha
	 *            a value from 0 to 255
	 * @see #getActionBarBackgroundDrawable()
	 * @see #getActionBarAlpha()
	 */
	public void setActionBarAlpha(int alpha) {
		if (mDrawable == null) {
			return;
		}
		if (alpha >= 255) {
			mDrawable.setAlpha(255);
		} else if (alpha <= 0) {
			mDrawable.setAlpha(0);
		} else {
			mDrawable.setAlpha(alpha);
		}
		if (mActionBar != null) {
			TextView title = (TextView) mActionBar
					.findViewById(R.id.app_title_Tv);
			if (alpha >= 250) {
				mActionBar.findViewById(R.id.action_bar_back_topic)
						.setBackgroundResource(
								R.drawable.action_bar_back_selector);
				mActionBar.findViewById(R.id.action_bar_search_topic)
						.setBackgroundResource(
								R.drawable.action_bar_search_selector);
				mActionBar.findViewById(R.id.action_go_downQueen_topic)
						.setBackgroundResource(
								R.drawable.btn_enter_downqueue_selector);
				title.setVisibility(View.VISIBLE);
				WindowMangerUtils.changeStatus(mWindow);
			} else {
				mActionBar.findViewById(R.id.action_bar_back_topic)
						.setBackgroundResource(
								R.drawable.detail_white_back_selector);
				mActionBar.findViewById(R.id.action_bar_search_topic)
						.setBackgroundResource(
								R.drawable.btn_search_person_sl);
				mActionBar.findViewById(R.id.action_go_downQueen_topic)
						.setBackgroundResource(
								R.drawable.detail_white_down_selector);
				title.setVisibility(View.GONE);
				WindowMangerUtils.changeStatus(mWindow);
			}
		}
		mAlpha = alpha;
	}

	public int getActionBarAlpha() {
		return mAlpha;
	}

	/**
	 * When ActionBar's alpha is locked {@link #setActionBarAlpha(int)} won't
	 * change drawable\'s alpha (but will change {@link #getActionBarAlpha()}
	 * level)
	 * 
	 * @param lock
	 */
	public void setActionBarAlphaLocked(boolean lock) {

		// Update alpha level on unlock
		if (isAlphaLocked != (isAlphaLocked = lock) && !isAlphaLocked) {

			setActionBarAlpha(mAlpha);
		}
	}

	public boolean isActionBarAlphaLocked() {
		return isAlphaLocked;
	}
}
