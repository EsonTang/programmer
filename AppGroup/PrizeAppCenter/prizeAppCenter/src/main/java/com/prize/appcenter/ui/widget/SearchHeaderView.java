/*******************************************
 * 版权所有©2015,深圳市铂睿智恒科技有限公司
 * <p>
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

package com.prize.appcenter.ui.widget;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;

import com.prize.app.net.datasource.base.AppsItemBean;
import com.prize.app.util.CommonUtils;
import com.prize.app.util.DisplayUtil;
import com.prize.app.util.MTAUtil;
import com.prize.appcenter.R;
import com.prize.appcenter.ui.util.UIUtils;

import java.util.ArrayList;


/**
 * 类描述：及时搜索头部
 *
 * @version 1.0
 */
public class SearchHeaderView extends LinearLayout {
    private Context mContext;
    private LinearLayout container;
    private LayoutParams params1;

    /*** 头布局的搜索记录listView ****/
    public SearchHeaderView(Context context) {
        super(context);
        mContext = context;
        setOrientation(VERTICAL);
        setGravity(Gravity.CENTER_HORIZONTAL);
        setBackgroundColor(Color.parseColor("#f5f5f5"));
        setDescendantFocusability(FOCUS_BLOCK_DESCENDANTS);
        View view = inflate(context, R.layout.search_match_header_lvew_contain, this);
        params1 = new LayoutParams(LayoutParams.MATCH_PARENT,
                (int) DisplayUtil.forMatSpAndDp(TypedValue.COMPLEX_UNIT_DIP, 60));
        findViewById(view);
    }

    public SearchHeaderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        setOrientation(VERTICAL);
        setGravity(Gravity.CENTER_HORIZONTAL);
        setBackgroundColor(Color.parseColor("#f5f5f5"));
        setDescendantFocusability(FOCUS_BLOCK_DESCENDANTS);
        View view = inflate(context, R.layout.search_match_header_lvew_contain, this);
        params1 = new LayoutParams(LayoutParams.MATCH_PARENT,
                (int) DisplayUtil.forMatSpAndDp(TypedValue.COMPLEX_UNIT_DIP, 60));
        findViewById(view);
    }


    private void findViewById(View view) {
        container = (LinearLayout) view.findViewById(R.id.apps_container);
        container.setOrientation(LinearLayout.VERTICAL);
    }


    public void refreshState(String packageName) {
        if (this.games != null && this.games.size() > 0) {
            AppsItemBean bean;
            for (int i = 0; i < games.size(); i++) {
                bean = games.get(i);
                if (bean!=null&& !TextUtils.isEmpty(bean.packageName)&&bean.packageName.equals(packageName)) {
                    View subView = container.getChildAt(i).findViewById(R.id.game_download_btn);
                    if (subView != null) {
                        subView.invalidate();
                    }
                }
            }
        }
    }

    private ArrayList<AppsItemBean> games;

    public void setData(ArrayList<AppsItemBean> games) {
        this.games = games;
        container.removeAllViews();
        SearchMatchViewItem gridViewItem;
        for (int i = 0; i < games.size(); i++) {
            gridViewItem = new SearchMatchViewItem(mContext);
            gridViewItem.setLayoutParams(params1);
            gridViewItem.setData(games.get(i));
            gridViewItem.setTag(games.get(i));
            gridViewItem.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (CommonUtils.isFastDoubleClick())
                        return;
                    AppsItemBean bean = (AppsItemBean) v.getTag();
                    if(bean==null)return;
                    if(bean.isAdvertise){
                        MTAUtil.onMatchSearchClick(bean.name);
                    }
                    // 隐藏软键盘
                    InputMethodManager immp = (InputMethodManager) mContext
                            .getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (immp != null) {
                        immp.hideSoftInputFromWindow(v.getWindowToken(), 0); // 强制隐藏键盘
                    }
                    UIUtils.gotoAppDetail(bean, bean.id, (Activity) mContext);
                }
            });
            container.addView(gridViewItem);
        }
    }
}
