/*
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

package com.prize.appcenter.ui.widget;

import android.app.Activity;
import android.text.Html;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.prize.app.net.datasource.base.AppsItemBean;
import com.prize.app.util.CommonUtils;
import com.prize.app.util.MTAUtil;
import com.prize.appcenter.R;
import com.prize.appcenter.ui.util.UIUtils;

import java.util.ArrayList;

/**
 * Desc: 下载队列有任务时候 底部的推荐位
 * <p/>
 * Created by huangchangguo
 * Date:  2016/9/13 15:24
 */

public class DownloadfootertypeItem extends RelativeLayout {
    private static final int CONTAINER_ITEM_COUNT = 4;
    //    private TextView mTitle;
    private TextView download_footer_type_title;
    private TextView mAddDownloadedMore;
    private Activity mContext;
    private LinearLayout container;
    private LinearLayout mFooter;
    private ArrayList<AppsItemBean> datas = new ArrayList<AppsItemBean>();
    private ArrayList mItems;
    private boolean isAddFooter;
    private boolean isAddMore;

    public DownloadfootertypeItem(Activity context) {
        super(context);
        mContext = context;
        View view = inflate(context, R.layout.downloadqueen_footer_type_item, this);
        initView(view);
    }

    public DownloadfootertypeItem(Activity context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        View view = inflate(context, R.layout.downloadqueen_footer_type_item, this);
        initView(view);
    }

    private void initView(View view) {
//        setBackgroundResource(R.color.cmmn_bg_gray);
        mAddDownloadedMore = (TextView) view.findViewById(R.id.download_footer_add_more_tv);
        download_footer_type_title = (TextView) view.findViewById(R.id.download_footer_type_title);
        container = (LinearLayout) view.findViewById(R.id.download_footer_type_llyt_container);
        mFooter = (LinearLayout) view.findViewById(R.id.download_footer_type_all);
        setContainerView();

        mAddDownloadedMore.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //点击加载listview
                Linstener.AddMoreListener();
            }
        });
    }

    private OnclickLinstener Linstener;

    public interface OnclickLinstener {
        void AddMoreListener();
    }

    public void setAddMoreOnClickListener(OnclickLinstener linstener) {
        this.Linstener = linstener;
    }

    //设置下拉是否可见
    public void setAddMoreVisibility(boolean isVisible) {
        if (isVisible) {
            if (!isAddMore) {
                isAddMore = true;
                mAddDownloadedMore.setVisibility(VISIBLE);
            }
        } else {
            mAddDownloadedMore.setVisibility(GONE);
        }
    }

    //设置下拉是否可见
    public void setFooterVisibility(boolean isVisible) {

        if (isVisible) {
            if (!isAddFooter) {
                isAddFooter = true;
                mFooter.setVisibility(VISIBLE);
            }
        } else {
            mFooter.setVisibility(GONE);
        }
    }

    public void setData(ArrayList<AppsItemBean> itemsBeen, String requestAppName) {
        String title = "下载了 " + "<font color='#ff7d5a'>" + requestAppName + "</font>" + " 的人还会下这些";
        if (itemsBeen == null)
            return;
        datas = itemsBeen;
        download_footer_type_title.setText(Html.fromHtml(title));
        for (int i = 0; i < container.getChildCount(); i++) {
            DownloadFooterGridViewItem gridViewItem = (DownloadFooterGridViewItem) container
                    .getChildAt(i);
            if (i < itemsBeen.size()) {
                AppsItemBean itemBean = itemsBeen.get(i);
                gridViewItem.setData(itemBean);
            }
        }


    }

    //填充线性布局中4个item
    private void setContainerView() {
        container.removeAllViews();
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);
        params.weight = 1;
        mItems = new ArrayList<>();
        for (int i = 0; i < CONTAINER_ITEM_COUNT; i++) {
            DownloadFooterGridViewItem gridViewItem = new DownloadFooterGridViewItem(mContext);
            gridViewItem.setLayoutParams(params);
            //给view设置编号
            gridViewItem.setTag(i);
            gridViewItem.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (CommonUtils.isFastDoubleClick())
                        return;

                    int position = (int) v.getTag();

                    if (datas != null && position < datas.size() && datas.get(position) != null) {

                        AppsItemBean bean = datas.get(position);
                        //跳转到详情
                        UIUtils.gotoAppDetail(bean, bean.id, mContext);
                        MTAUtil.onClickDownLoadQueenHaveData(position + 1, bean.name);

                    }
                }
            });
            mItems.add(i, gridViewItem);
            container.addView(gridViewItem);
        }

    }


    public void unBindregisterCallback() {
        if (mItems != null && mItems.size() > 0) {
            for (int i = 0; i < mItems.size(); i++) {
                DownloadFooterGridViewItem item = (DownloadFooterGridViewItem) mItems.get(i);
                if (item != null)
                    item.unBindregisterCallback();
            }
        }
    }

    public void setDownlaodRefreshHandle() {
        if (mItems != null && mItems.size() > 0) {
            for (int i = 0; i < mItems.size(); i++) {
                DownloadFooterGridViewItem item = (DownloadFooterGridViewItem) mItems.get(i);
                if (item != null)
                    item.setDownlaodRefreshHandle();
            }
        }
    }


//    /**
//     * 去除卡片中已下载应用数据
//     *
//     * @return
//     */
//    public ArrayList<AppsItemBean> getNoInstalledAppList() {
//        if (datas.size() <= CONTAINER_ITEM_COUNT)
//            return datas;
//        ArrayList<AppsItemBean> appsItemBeans = new ArrayList<AppsItemBean>();
//        for (int i = 0; i < datas.size(); i++) {
//            AppsItemBean item = datas.get(i);
//            int state = AppManagerCenter.getGameAppState(item.packageName,
//                    String.valueOf(item.id), item.versionCode);
//            if (state != AppManagerCenter.APP_STATE_INSTALLED) {
//                appsItemBeans.add(item);
//                if (appsItemBeans.size() >= CONTAINER_ITEM_COUNT)
//                    break;
//            }
//        }
//        if (appsItemBeans.size() < CONTAINER_ITEM_COUNT) {
//            for (int i = 0; i < datas.size(); i++) {
//                AppsItemBean item = datas.get(i);
//                int state = AppManagerCenter.getGameAppState(item.packageName,
//                        String.valueOf(item.id), item.versionCode);
//                if (state == AppManagerCenter.APP_STATE_INSTALLED) {
//                    appsItemBeans.add(item);
//                    if (appsItemBeans.size() >= CONTAINER_ITEM_COUNT)
//                        break;
//                }
//            }
//        }
//        return appsItemBeans;
//    }
}
