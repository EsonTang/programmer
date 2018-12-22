/*
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
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;

import com.prize.app.constants.Constants;
import com.prize.app.net.datasource.base.AppsItemBean;
import com.prize.app.util.CommonUtils;
import com.prize.app.util.DisplayUtil;
import com.prize.app.util.MTAUtil;
import com.prize.appcenter.R;
import com.prize.appcenter.ui.util.UIUtils;

import java.util.List;

/**
 * 类描述：单机顶部三个item布局
 *
 * @author longbaoixu
 * @version 1.0
 */
public class TopThreeGridView extends LinearLayout {
    private List<AppsItemBean> mApps;
    private LinearLayout container;
    private static final int APP_COUNT = 3;

    public TopThreeGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        setOrientation(VERTICAL);
        setGravity(Gravity.CENTER);
        setDescendantFocusability(FOCUS_BLOCK_DESCENDANTS);
        View view = inflate(context, R.layout.top_tree_layout, this);
        findViewById(view);
    }

    private void findViewById(View view) {
        container = (LinearLayout) view.findViewById(R.id.container);
        setlistener();
    }

    public void setData(List<AppsItemBean> apps) {
        if (apps == null)
            return;
        mApps = apps;
        for (int i = 0; i < container.getChildCount(); i++) {
            TopThreeViewItem gridViewItem = (TopThreeViewItem) container
                    .getChildAt(i);
            if (i < apps.size()) {
                AppsItemBean itemBean = apps.get(i);
                itemBean = CommonUtils.formatAppPageInfo(itemBean, Constants.HOME_GUI, Constants.LIST, i + 1);
                gridViewItem.setData(itemBean);
            }
        }
    }

    private void setlistener() {
        container.setGravity(Gravity.CENTER_HORIZONTAL);
        LayoutParams params = new LayoutParams((int) DisplayUtil.forMatSpAndDp(TypedValue.COMPLEX_UNIT_DIP,90), (int) DisplayUtil.forMatSpAndDp(TypedValue.COMPLEX_UNIT_DIP,156));
       int margin= (int) DisplayUtil.forMatSpAndDp(TypedValue.COMPLEX_UNIT_DIP,9);
       int topMargin= (int) DisplayUtil.forMatSpAndDp(TypedValue.COMPLEX_UNIT_DIP,5 );
        for (int i = 0; i < APP_COUNT; i++) {
            TopThreeViewItem gridViewItem = new TopThreeViewItem(mContext, R.layout.top_three_item_view, i);
            if (i == 1) {
                params.leftMargin = margin;
                params.rightMargin = margin;
                params.topMargin=topMargin;
            }
            gridViewItem.setLayoutParams(params);
            gridViewItem.setTag(i);
            gridViewItem.setOnClickListener(new OnClickListener() {


                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    if (CommonUtils.isFastDoubleClick())
                        return;
                    int position = (int) v.getTag();
                    if (mApps != null && position < mApps.size() && mApps.get(position) != null) {
                        AppsItemBean bean = mApps.get(position);
                        UIUtils.gotoAppDetail(bean, bean.id, (Activity) mContext);
                        MTAUtil.onSingleGamePosition(position+1);
                    }
                }
            });
            container.addView(gridViewItem);
        }
    }

    public int getCount() {
        return APP_COUNT;
    }

    public AppsItemBean getItem(int position) {
        if (position < getCount()) {
            return mApps.get(position);
        } else {
            return null;
        }
    }


    /**
     * 刷新下载app的下载状态
     * @param packageName 应用包名
     */
    public void notifyState(String packageName) {
        if (mApps == null) return;
        AppsItemBean bean;
        for (int i = 0; i < APP_COUNT; i++) {
            if (i > mApps.size()) return;
            bean = mApps.get(i);
            if (bean.packageName.equals(packageName)) {
                TopThreeViewItem itemView = (TopThreeViewItem) container.getChildAt(i);
                View view = itemView.findViewById(R.id.game_download_btn);
                if (view != null) {
                    view.invalidate();
                }
            }
        }
    }

}
