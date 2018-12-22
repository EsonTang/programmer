package com.prize.appcenter.ui.util;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.prize.app.net.datasource.base.AppsItemBean;

import java.util.LinkedList;
import java.util.Random;

public class AnimationUtil {

    private static boolean flag = true;

    /**
     * @param activity 所属Activity
     * @param bean     要下载的app
     * @param srcView  要执行动画的View
     * @param decView  目的地动画
     */
    public static void startAnimationToTop(final Activity activity,
                                           final AppsItemBean bean, final ImageView srcView, final View decView) {

        /** 获取屏幕的绝对位置 */
        int[] srcLocation = new int[2];
        srcView.getLocationOnScreen(srcLocation);
        int srcX = (int) srcLocation[0];
        int srcY = (int) srcLocation[1];

        int[] decLocation = new int[2];
        decView.getLocationOnScreen(decLocation);
        int decX = decLocation[0] + decView.getWidth() / 2;
        int decY = decLocation[1] + decView.getHeight() / 2;

        int offsetX = decX - srcView.getWidth() / 2;
        int offsetY = decY - srcView.getHeight() / 2;

        final int detalX = offsetX - srcX;
        final int detalY = offsetY - srcY;

        // JLog.i("hu", decX+"------"+"decY-----"+decY);
        final ViewGroup rootView = (ViewGroup) activity.getWindow()
                .getDecorView();
        final View view = addViewToAnimLayout(activity, srcLocation, srcView,
                rootView);

        AnimatorSet bouncer = new AnimatorSet();

        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 0.8f,
                0.0f).setDuration(800);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 0.8f,
                0.0f).setDuration(800);
        // ObjectAnimator translationUp = ObjectAnimator.ofFloat(view, "Y",
        // view.getTop(), view.getTop() + 50).setDuration(5000);

        ObjectAnimator flyX = ObjectAnimator
                .ofFloat(view, "scaleX", 1.0f, 0.8f).setDuration(2000);
        ObjectAnimator flyY = ObjectAnimator
                .ofFloat(view, "scaleY", 1.0f, 0.8f).setDuration(2000);

        final ValueAnimator valueAnimator = ObjectAnimator.ofFloat(0f, 1f);
        valueAnimator.setDuration(2000);

        //		TimeInterpolator value = AnimationUtils.loadInterpolator(activity,
        //				com.android.internal.R.interpolator.decelerate_cubic);
        //		valueAnimator.setInterpolator(value);

        valueAnimator.addUpdateListener(new AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator value) {
                float progress = (float) value.getAnimatedValue();
                int translationY = (int) (progress * progress * detalY);
                int translationX = (int) (progress * detalX);
                view.setTranslationX(translationX);
                view.setTranslationY(translationY);
                // JLog.i("translationX---",
                // translationX+""+"translationY-----"+translationY);
            }
        });

        scaleX.addListener(new AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animation) {
                view.setLayerType(View.LAYER_TYPE_HARDWARE, null);
                flag = false;
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (view != null) {
                    view.setLayerType(View.LAYER_TYPE_NONE, null);
                    if (rootView.indexOfChild(view) != -1) {
                        rootView.removeView(view);
                    }
                }
                flag = true;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }
        });
        bouncer.play(valueAnimator).before(scaleX);
        bouncer.play(valueAnimator).with(flyX);
        bouncer.play(valueAnimator).with(flyY);
        bouncer.play(scaleX).with(scaleY);
        // bouncer.play(translationUp);
        bouncer.start();
    }

    /**
     * @param activity 动画运行的层 这里是frameLayout
     * @param location 要运行动画的View
     * @param srcView  要执行的view
     * @param rootView 到达的位置
     * @return view
     * @deprecated 将要执行动画的view 添加到动画层
     */
    private static View addViewToAnimLayout(Activity activity, int[] location,
                                            ImageView srcView, ViewGroup rootView) {

        int x = location[0];
        int y = location[1];

        // 创建一个图片
        final ImageView view = new ImageView(activity);
        view.setImageDrawable(srcView.getDrawable());

        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                srcView.getWidth(), srcView.getHeight());
        lp.leftMargin = x;
        lp.topMargin = y;

        rootView.addView(view, lp);
        view.bringToFront();
        return view;
    }

    public static boolean isFlag() {
        return flag;
    }




      /**
        * Desc: 个人中心积分展示的动画
        *       2.0积分系统
        *   (数字增长动画，暂时不用)
        * Created by huangchangguo
        * Date:  2016/8/15 20:43
        */

    //每秒刷新多少次
    private static final int COUNTPERS = 100;
    public static void startnumAnim(TextView textV, float num) {
        startnumAnim(textV, num, 800);
    }

    public static void startnumAnim(TextView textV, float num, long time) {
        if (num == 0) {
            textV.setText(NumberFormat(num, 0));
            return;
        }

        Float[] nums = splitnum(num, (int) ((time / 1000f) * COUNTPERS));

        Counter counter = new Counter(textV, nums, time);

        textV.removeCallbacks(counter);
        textV.post(counter);
    }

    private static Float[] splitnum(float num, int count) {
        Random random = new Random();
        float numtemp = num;
        float sum = 0;
        LinkedList<Float> nums = new LinkedList<Float>();
        nums.add(0f);
        while (true) {
            float nextFloat = NumberFormatFloat(
                    (random.nextFloat() * num * 2f) / (float) count,
                    2);
            System.out.println("next:" + nextFloat);
            if (numtemp - nextFloat >= 0) {
                sum = NumberFormatFloat(sum + nextFloat, 2);
                nums.add(sum);
                numtemp -= nextFloat;
            } else {
                nums.add(num);
                return nums.toArray(new Float[0]);
            }
        }
    }

    static class Counter implements Runnable {

        private final TextView view;
        private       Float[]  nums;
        private       long     pertime;

        private int i = 0;

        Counter(TextView view, Float[] nums, long time) {
            this.view = view;
            this.nums = nums;
            this.pertime = time / nums.length;
        }

        @Override
        public void run() {
            if (i > nums.length - 1) {
                view.removeCallbacks(Counter.this);
                return;
            }
            view.setText(NumberFormat(nums[i++], 2));
            view.removeCallbacks(Counter.this);
            view.postDelayed(Counter.this, pertime);
        }
    }

    //保留两位小数
    private static String NumberFormat(float f, int m) {
        return String.format("%." + m + "f", f);
    }
    private static float NumberFormatFloat(float f, int m) {
        String strfloat = NumberFormat(f, m);
        return Float.parseFloat(strfloat);
    }
}
