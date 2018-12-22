/*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：
 *当前版本：
 *作	者：
 *完成日期：
 *修改记录：
 *修改日期：
 *版 本 号：
 *修 改 人：
 *修改内容：
 ...
 *修改记录：
 *修改日期：
 *版 本 号：
 *修 改 人：
 *修改内容：
 *********************************************/

package com.prize.appcenter.ui.widget;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.view.animation.Transformation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.prize.app.util.JLog;
import com.prize.appcenter.R;

/**
 * 类描述：点击展开textView的自定义控件（ 应用介绍）
 *
 * @author huanglingjun
 * @version 1.0
 */
public class ExpendTextView extends LinearLayout implements OnClickListener {
    // 默认显示多少行
    private static final int DEFAULT_MAX_LINE_COUNT = 5;

    private TextView descriptionView;
    private TextView app_introduce_id;
    private ImageView expandView;
    private int maxDescripLine = DEFAULT_MAX_LINE_COUNT;
    private boolean isExpand;
    public int maxLines;
    LinearLayout  description_layout;
    public ExpendTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        View view = inflate(context, R.layout.expend_view, this);

        descriptionView = (TextView) view.findViewById(R.id.description_view);
        expandView = (ImageView) view.findViewById(R.id.expand_view);
        app_introduce_id = (TextView) view.findViewById(R.id.app_introduce_id);
        descriptionView.setHeight(descriptionView.getLineHeight()
                * maxDescripLine);
        descriptionView.post(new Runnable() {

            @Override
            public void run() {
                expandView
                        .setVisibility(descriptionView.getLineCount() > maxDescripLine ? View.VISIBLE
                                : View.GONE);
                maxLines = descriptionView.getLineCount();
                if (JLog.isDebug) {
                    JLog.i("AppDetailParentFgm","ExpendTextView构造" +
                            "-descriptionView.getLineCount()="+descriptionView.getLineCount());
                }
            }
        });

		view.findViewById(R.id.description_layout).setOnClickListener(
				new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						isExpand = !isExpand;
						expendText(isExpand);
					}
				});

		expandView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				isExpand = !isExpand;
				expendText(isExpand);
			}
		});
	}

    public void expendText(boolean isExpand) {
        if (maxLines <= 5)
            return;
        descriptionView.clearAnimation();
        final int deltaValue;
        final int startValue = descriptionView.getHeight();
        int durationMillis = 350;
        if (isExpand) {
            deltaValue = descriptionView.getLineHeight()
                    * descriptionView.getLineCount() - startValue;
            RotateAnimation animation = new RotateAnimation(0, 180,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f);
            animation.setDuration(durationMillis);
            animation.setFillAfter(true);
            expandView.startAnimation(animation);
        } else {
            deltaValue = descriptionView.getLineHeight() * maxDescripLine
                    - startValue;
            RotateAnimation animation = new RotateAnimation(180, 0,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f);
            animation.setDuration(durationMillis);
            animation.setFillAfter(true);
            expandView.startAnimation(animation);
        }
        Animation animation = new Animation() {
            protected void applyTransformation(float interpolatedTime,
                                               Transformation t) {
                descriptionView.setHeight((int) (startValue + deltaValue
                        * interpolatedTime));
            }
        };
        animation.setDuration(durationMillis);
        descriptionView.startAnimation(animation);
    }

    public ExpendTextView(Context context) {
        this(context, null);
    }

	public final void setContentDesc(CharSequence title, String contentDesc) {
		if (!TextUtils.isEmpty(contentDesc)) {
			String contentOne = contentDesc.replace("\r\n\r\n", "\r\n");
			String contentTwo = contentOne.replace("\r\n\r\n", "\r\n");
			descriptionView.setText(contentTwo.trim());
		} else {
			descriptionView.setText(getResources().getString(
					R.string.none));
		}
	}

	public final void setRelayoutDesc(CharSequence title,
			CharSequence contentDesc, CharSequence update, CharSequence version) {
		descriptionView.setText(contentDesc);
	}

	@Override
	public void onClick(View v) {
		// expend();
	}
}
