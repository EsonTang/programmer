package com.android.lpserver;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.android.lpserver.util.StatusBarUtils;

import java.util.Locale;
/**
 * prize create by chenjiahua 20161106
 * */

public class MoreQuestionActivity extends AppCompatActivity {
    private static final String TAG = "MoreQuestionActivity";
    private LinearLayout question1;
    private LinearLayout question2;
    private LinearLayout question3;
    private LinearLayout question4;
    private LinearLayout question5;
    private LinearLayout answer1;
    private LinearLayout answer2;
    private LinearLayout answer3;
    private LinearLayout answer4;
    private LinearLayout answer5;
    private ImageView rotate1;
    private ImageView rotate2;
    private ImageView rotate3;
    private ImageView rotate4;
    private ImageView rotate5;
    private int rotateDegree = 180;
    private String language;
    private ImageButton backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //prize modify by zhaojian v8.0 2017912 start
        //setNotificationStatus();
        StatusBarUtils.setStatusBar(getWindow(),this);
        //prize modify by zhaojian v8.0 2017912 end
        setContentView(R.layout.activity_more_question);

        backButton = (ImageButton) findViewById(R.id.back);
        backButton.setOnClickListener(clickBack);

        question1 = (LinearLayout) findViewById(R.id.question1);
        question2 = (LinearLayout) findViewById(R.id.question2);
        question3 = (LinearLayout) findViewById(R.id.question3);
        question4 = (LinearLayout) findViewById(R.id.question4);
//        question5 = (LinearLayout) findViewById(R.id.question5);

        answer1 = (LinearLayout) findViewById(R.id.answer1);
        answer2 = (LinearLayout) findViewById(R.id.answer2);
        answer3 = (LinearLayout) findViewById(R.id.answer3);
        answer4 = (LinearLayout) findViewById(R.id.answer4);
//        answer5 = (LinearLayout) findViewById(R.id.answer5);

        rotate1 = (ImageView) findViewById(R.id.rotate1);
        rotate2 = (ImageView) findViewById(R.id.rotate2);
        rotate3 = (ImageView) findViewById(R.id.rotate3);
        rotate4 = (ImageView) findViewById(R.id.rotate4);
//        rotate5 = (ImageView) findViewById(R.id.rotate5);

        question1.setOnClickListener(onClick);
        question2.setOnClickListener(onClick);
        question3.setOnClickListener(onClick);
        question4.setOnClickListener(onClick);
//        question5.setOnClickListener(onClick);

        Locale locale = getResources().getConfiguration().locale;
        language = locale.getLanguage();
    }

    private void setNotificationStatus() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    //| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//            window.setStatusBarColor(Color.TRANSPARENT);
            window.setStatusBarColor(getResources().getColor(R.color.prize_actionbar_bg_color_v8));      // prize modify zhaojian 8.0 2017803
        }

//        WindowManager.LayoutParams lp= getWindow().getAttributes();
//        lp.statusBarInverse = StatusBarManager.STATUS_BAR_INVERSE_GRAY;
//        getWindow().setAttributes(lp);
    }

    View.OnClickListener clickBack = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            finish();
        }
    };

    View.OnClickListener onClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.question1:
                    Log.d(TAG, "1: " + answer1.getHeight() + " 2. " + answer2.getHeight() + " 3. " + answer3.getHeight() + " 4. " + answer4.getHeight() /*+ " 5. " + answer5.getHeight()*/);
                    showBehavior(answer1, answer2, answer3, answer4, answer5);
                    rotateBehavior(rotate1, rotate2, rotate3, rotate4, rotate5);
                    break;
                case R.id.question2:
                    showBehavior(answer2, answer1, answer3, answer4, answer5);
                    rotateBehavior(rotate2, rotate1, rotate3, rotate4, rotate5);
                    break;
                case R.id.question3:
                    showBehavior(answer3, answer1, answer2, answer4, answer5);
                    rotateBehavior(rotate3, rotate1, rotate2, rotate4, rotate5);
                    break;
                case R.id.question4:
                    showBehavior(answer4, answer1, answer2, answer3, answer5);
                    rotateBehavior(rotate4, rotate1, rotate2, rotate3, rotate5);
                    break;
//                case R.id.question5:
//                    showBehavior(answer5, answer1, answer2, answer3, answer4);
//                    rotateBehavior(rotate5, rotate1, rotate2, rotate3, rotate4);
//                    break;
            }
        }
    };

    private void showBehavior(final View v1, final View v2, final View v3, final View v4, final View v5) {
        //First fold other answers and then expand the v1
        if (v2.getVisibility() == View.VISIBLE) {
            dismiss(v2, getHeight(v2));
        }
        if (v3.getVisibility() == View.VISIBLE) {
            dismiss(v3, getHeight(v3));
        }
        if (v4.getVisibility() == View.VISIBLE) {
            dismiss(v4, getHeight(v4));
        }
//        if (v5.getVisibility() == View.VISIBLE) {
//            dismiss(v5, getHeight(v5));
//        }
        if (v1.getVisibility() == View.VISIBLE) {
            dismiss(v1, getHeight(v1));
        } else {
            show(v1, getHeight(v1));
        }
    }

    private void rotateBehavior(final View v1, final View v2, final View v3, final View v4, final View v5) {
        doViewRotationAnim(v1);
        if (v2.getRotation() == rotateDegree) {
            doViewRotationAnim(v2);
        }
        if (v3.getRotation() == rotateDegree) {
            doViewRotationAnim(v3);
        }
        if (v4.getRotation() == rotateDegree) {
            doViewRotationAnim(v4);
        }
//        if (v5.getRotation() == rotateDegree) {
//            doViewRotationAnim(v5);
//        }
    }

    private int getDisplay(){
        DisplayMetrics metrics=new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int widthPixels=metrics.widthPixels;
        int heightPixels=metrics.heightPixels;
        return heightPixels*widthPixels;
    }

    private int getHeight(final View v) {
        switch (v.getId()) {
            case R.id.answer1:
                return this.getResources().getDimensionPixelOffset(R.dimen.answer1_height);
            case R.id.answer2:
                return (int)getResources().getDimensionPixelOffset(R.dimen.answer2_height);
            case R.id.answer3:
                return (int)getResources().getDimensionPixelOffset(R.dimen.answer3_height);
            case R.id.answer4:
                return (int)getResources().getDimensionPixelOffset(R.dimen.answer4_height);
//            case R.id.answer5:
//                if (language.contains("en")) {
//                    return 402;
//                } else {
//                    if(metric == 1920*1080){
//                        return 451;
//                    }else{
//                        return 300;
//                    }
//                }
        }
        return 0;
    }

    //Expand animation
    public void show(final View v, int height) { //The second parameter is the height of the expansion.
        v.setVisibility(View.VISIBLE);
        ValueAnimator animator = ValueAnimator.ofInt(0, height);
        animator.setDuration(500);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (Integer) animation.getAnimatedValue();
                v.getLayoutParams().height = value;
                v.setLayoutParams(v.getLayoutParams());
            }
        });
        animator.start();
    }

    //Folding animation
    public void dismiss(final View v, int height) {
        ValueAnimator animator = ValueAnimator.ofInt(height, 0);
        animator.setDuration(500);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (Integer) animation.getAnimatedValue();
                if (value == 0) {
                    v.setVisibility(View.GONE);
                }
                v.getLayoutParams().height = value;
                v.setLayoutParams(v.getLayoutParams());
            }
        });
        animator.start();
    }

    private View linkedItemAndRotate(final View view) {
        switch (view.getId()) {
            case R.id.rotate1:
                return question1;
            case R.id.rotate2:
                return question2;
            case R.id.rotate3:
                return question3;
            case R.id.rotate4:
                return question4;
//            case R.id.rotate5:
//                return question5;
        }
        return null;
    }

    //Rotating animation
    private void doViewRotationAnim(final View view) {
        int degree = 0;
        if (view.getRotation() == 0) {
            degree = rotateDegree;
        }
        if (view.getRotation() == rotateDegree) {
            degree = -rotateDegree;
        }
        ObjectAnimator anim = ObjectAnimator.ofFloat(view, "rotation", view.getRotation(), view.getRotation() + degree);
        degree = 0;
        anim.setDuration(500);
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                //Do not listen to the animation after the start, to prevent the formation of the position due to the animation is not completed
                linkedItemAndRotate(view).setClickable(false);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                linkedItemAndRotate(view).setClickable(true);
            }
        });
        anim.start();
    }
}
