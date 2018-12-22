package com.prize.appcenter.ui.animation;

import java.math.RoundingMode;
import java.text.DecimalFormat;

 /**
   * Desc:
   *
   * Created by huangchangguo
   * Date:  2016/8/16 10:29
   */

public class Utils {
	/**
	 * RiseNumberTextView 数字增长动画使用到的工具类
	 */
	private static DecimalFormat dfs = null;

	public static DecimalFormat format(String pattern) {
		if (dfs == null) {
			dfs = new DecimalFormat();
		}
		dfs.setRoundingMode(RoundingMode.FLOOR);
		dfs.applyPattern(pattern);
		return dfs;
	}
}
