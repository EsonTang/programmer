/*******************************************
 * 版权所有©2015,深圳市铂睿智恒科技有限公司
 * <p/>
 * 内容摘要：
 * 当前版本：
 * 作	者：
 * 完成日期：
 * 修改记录：
 * 修改日期：
 * 版 本 号：
 * 修 改 人：
 * 修改内容：
 * ...
 * 修改记录：
 * 修改日期：
 * 版 本 号：
 * 修 改 人：
 * 修改内容：
 *********************************************/
package com.prize.appcenter.ui.actionBar;

import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.prize.app.constants.Constants;
import com.prize.appcenter.R;
import com.prize.cloud.activity.WebviewActivity;
import com.tencent.stat.StatService;

/**
 * Title:    积分系统的Actionbar
 * Desc:
 * Version:
 * Created by huangchangguo
 * on   2016/8/16  20:44
 * <p/>
 * Update Description: 更新描述
 * Updater:  更新者
 * Update Time:  更新时间
 */

public abstract class ActionBarPointsActivity extends ActionBarActivity {

    /**
     * 标题
     */
    private TextView title = null;
    protected View      divideLine;
    protected ImageView rule;

    @Override
    protected void initActionBar() {
        enableSlideLayout(false);
        findViewById(R.id.action_bar_points_rlyt).setVisibility(View.VISIBLE);
        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int id = v.getId();
                switch (id) {
                    // 返回
                    case R.id.actionbar_back_points_btn:
                        onBackPressed();
                        break;
                    //去积分规则界面
                    case R.id.actionbar_points_rules_iv:

                        Intent intent = new Intent(ActionBarPointsActivity.this,
                                WebviewActivity.class);
                            intent.putExtra(WebviewActivity.EXTRA_URL,
                                    Constants.POINT_RULE_URL);
                            intent.putExtra(WebviewActivity.EXTRA_TITLE,
                                    ActionBarPointsActivity.this.getString(R.string.points_rules));
                            startActivity(intent);

                        break;
                }
            }
        };
        // 增加点击返回的灵敏度
        TextView back = (TextView) findViewById(R.id.actionbar_back_points_btn);
        back.setOnClickListener(onClickListener);
        rule = (ImageView) findViewById(R.id.actionbar_points_rules_iv);
        rule.setOnClickListener(onClickListener);

        divideLine = findViewById(R.id.actionbar_points_divide_line);
        title = (TextView) findViewById(R.id.actionbar_points_title);
    }


    /**
     * 设置标题栏
     *
     * @param title String
     */
    public void setTitle(String title) {
        if (this.title != null) {
            this.title.setText(title);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        StatService.onResume(this);
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onPause() {
        super.onPause();
        StatService.onPause(this);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }
}