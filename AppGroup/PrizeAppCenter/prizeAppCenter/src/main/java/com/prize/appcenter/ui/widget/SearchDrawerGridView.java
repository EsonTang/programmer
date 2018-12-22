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
import android.widget.LinearLayout;
import android.widget.TextView;

import com.prize.app.net.datasource.base.AppsItemBean;
import com.prize.app.util.CommonUtils;
import com.prize.app.util.DisplayUtil;
import com.prize.app.util.MTAUtil;
import com.prize.appcenter.R;
import com.prize.appcenter.ui.util.UIUtils;

import java.util.List;


/**
 * 类描述：抽屉布局
 *
 * @version 1.0
 */
public class SearchDrawerGridView extends LinearLayout {
    private Context mContext;
    private List<AppsItemBean> mApps;
    private LinearLayout container;
    private static final int APP_COUNT = 3;
    private TextView titleView;
    private LayoutParams params;
    private LayoutParams params1;

    public SearchDrawerGridView(Activity context, String title) {
        super(context);
    }

    public SearchDrawerGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        setOrientation(VERTICAL);
        setGravity(Gravity.CENTER_HORIZONTAL);
        setBackgroundColor(Color.parseColor("#f5f5f5"));
        setDescendantFocusability(FOCUS_BLOCK_DESCENDANTS);
        View view = inflate(context, R.layout.search_drawer_layout, this);
        params = new LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT);
        params1 = new LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT);
        int margin = (int) DisplayUtil.forMatSpAndDp(TypedValue.COMPLEX_UNIT_DIP, 18.0f);
        params.setMargins(0,margin,0,margin);
        findViewById(view);
    }


    private void findViewById(View view) {
        titleView = (TextView) view.findViewById(R.id.title);
        container = (LinearLayout) view.findViewById(R.id.apps_container);
        container.setOrientation(LinearLayout.VERTICAL);
    }

    public LinearLayout getDrawerContainer() {
        return container;
    }

    public void setData(List<AppsItemBean> apps, String mTitle,int position) {
        if (apps == null)
            return;
        setlistener(position);
        if (TextUtils.isEmpty(mTitle)) {
            titleView.setText(getResources().getString(R.string.recommand));
        } else {
            titleView.setText(mTitle);
        }
        mApps = apps;
        for (int i = 0; i < container.getChildCount(); i++) {
            SearchDrawerItem gridViewItem = (SearchDrawerItem) container
                    .getChildAt(i);
            if (i < apps.size()) {
                AppsItemBean itemBean = apps.get(i);
                gridViewItem.setData(itemBean);
            }
        }
    }

    private void setlistener(final int listPosition) {
        container.removeAllViews();
        for (int i = 0; i < APP_COUNT; i++) {
            SearchDrawerItem gridViewItem = new SearchDrawerItem(mContext, listPosition);
            if (i == 1) {
                gridViewItem.setLayoutParams(params);
            } else {
                gridViewItem.setLayoutParams(params1);
            }
            gridViewItem.setTag(i);
            gridViewItem.setOnClickListener(new OnClickListener() {


                @Override
                public void onClick(View v) {
                    if (CommonUtils.isFastDoubleClick())
                        return;
                    int position = (int) v.getTag();
                    if (mApps !=null&&position<mApps.size()&&mApps.get(position) != null) {
                        AppsItemBean bean = mApps.get(position);
                        UIUtils.gotoAppDetail(bean, bean.id, (Activity) mContext);
                        MTAUtil.onDetailClick(mContext, bean.name,
                                bean.packageName);
                        MTAUtil.onSearchDrawerClick(listPosition);
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

        if (position < 0 || mApps == null || mApps.isEmpty() || position >= mApps.size() || position >= getCount()) {
            return null;
        }
        return mApps.get(position);

    }


    public View getChildDownLoadViewAt(int position) {
        SearchDrawerItem itemView = (SearchDrawerItem) container.getChildAt(position);
        return itemView.findViewById(R.id.game_download_btn);
    }

}
