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
import android.graphics.Color;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.prize.app.constants.Constants;
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
 * @author 聂礼刚
 * @version 1.0
 */
public class DrawerGridView extends LinearLayout {
    private Activity mContext;
    private String mTitle;
    private List<AppsItemBean> mApps;
    private LinearLayout container;
    private static final int APP_COUNT = 3;
    private boolean isGame = false;
    private boolean isHome = false;
    private LayoutParams params;
    private LayoutParams params1;
    public DrawerGridView(Activity context, String title, boolean isGame, boolean isHome) {
        super(context);
        mContext = context;
        mTitle = title;
        this.isGame = isGame;
        this.isHome = isHome;
        setOrientation(VERTICAL);
        setGravity(Gravity.CENTER_HORIZONTAL);
        setBackgroundColor(Color.parseColor("#f5f5f5"));
        setDescendantFocusability(FOCUS_BLOCK_DESCENDANTS);
        View view = inflate(context, R.layout.drawer_layout, this);
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
        TextView titleView = (TextView) view.findViewById(R.id.title);
        if (TextUtils.isEmpty(mTitle)) {
            titleView.setText(getResources().getString(R.string.recommand));
        } else {
            titleView.setText(mTitle);
        }
        container = (LinearLayout) view.findViewById(R.id.apps_container);

        setlistener();
    }

    public void setData(List<AppsItemBean> apps, int position) {
        if (apps == null)
            return;
        mApps = apps;
        for (int i = 0; i < container.getChildCount(); i++) {
            AppGridViewItem gridViewItem = (AppGridViewItem) container
                    .getChildAt(i);
            if (i < apps.size()) {
                AppsItemBean itemBean;
                if (isHome) {
                    itemBean = CommonUtils.formatAppPageInfo(apps.get(i), Constants.HOME_GUI,Constants.DRAWER, position);
                } else {
                    if (isGame) {
                        itemBean = apps.get(i);
                    } else {
                        itemBean = CommonUtils.formatAppPageInfo(apps.get(i), Constants.APP_GUI, Constants.DRAWER, position);
                    }
                }
                gridViewItem.setData(itemBean);
            }
        }
    }

    private void setlistener() {
        for (int i = 0; i < APP_COUNT; i++) {
            AppGridViewItem gridViewItem = new AppGridViewItem(mContext, R.layout.search_drawer_itemlayout, i, isGame, isHome);
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
                    if (mApps.get(position) != null) {
                        AppsItemBean bean = mApps.get(position);
                        UIUtils.gotoAppDetail(bean, bean.id, mContext);
                        MTAUtil.onDetailClick(mContext, bean.name,
                                bean.packageName);
                        if (isHome) {
                            MTAUtil.homeDrawerSubViewClick(bean.name);
                        } else {
                            if (isGame) {
                                MTAUtil.gameDrawerSubViewClick(bean.name);
                            } else {
                                MTAUtil.appDrawerSubViewClick(bean.name);
                            }
                        }
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
        AppGridViewItem itemView = (AppGridViewItem) container.getChildAt(position);
        return itemView.findViewById(R.id.game_download_btn);
    }

}
