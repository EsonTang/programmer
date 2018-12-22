package com.prize.lockscreen.utils;

import android.content.Context;
import android.os.Vibrator;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.CycleInterpolator;
import android.view.animation.TranslateAnimation;
/***
 * 动画工具类
 * @author fanjunchen
 *
 */
public class AnimationUtil {

	/***
	 * 抖动动画
	 * @param counts 抖动次数
	 * @return
	 */
	public static Animation shakeAnimation(int counts) {
    	Animation translateAnimation = new TranslateAnimation(0, 15, 0, 0);
    	//设置一个循环加速器，使用传入的次数就会出现摆动的效果。
    	translateAnimation.setInterpolator(new CycleInterpolator(counts));
    	translateAnimation.setDuration(500);
    	return translateAnimation;
    }
	
	/***
	 * 抖动动画
	 * @param counts 抖动次数
	 * @param l 动画监听器
	 * @return
	 */
	public static Animation shakeAnimation(int counts, AnimationListener l) {
    	Animation shakeAnimation = shakeAnimation(counts);
    	
    	shakeAnimation.setAnimationListener(l);
    	
    	return shakeAnimation;
    }
	/**
	 * 震动
	 * @param ctx
	 */
	public static void virbate(Context ctx) {
		Vibrator vibrator = (Vibrator) ctx
				.getSystemService(Context.VIBRATOR_SERVICE);
		vibrator.vibrate(200);
	}
}
