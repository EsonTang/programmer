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
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.prize.app.beans.TopicItemBean;
import com.prize.app.net.datasource.base.AppsItemBean;
import com.prize.app.net.datasource.home.CarParentBean;
import com.prize.app.util.CommonUtils;
import com.prize.app.util.MTAUtil;
import com.prize.appcenter.R;
import com.prize.appcenter.activity.TopicDetailActivity;
import com.prize.appcenter.ui.util.UILimageUtil;
import com.prize.appcenter.ui.util.UIUtils;

/**
 * 类描述：首页card(专题类型)
 *
 * @author huanglingjun
 * @version 1.0
 */
public class CardGridView extends LinearLayout implements OnClickListener {
    private Activity mContext;
    private ImageView mTopicIcon;
    private CarParentBean cardBean;
    private LinearLayout container;
    private static final int CARD_APP_COUNT = 4;
    private String fromPage;
    private boolean isGame = false;
    private int marginleft = 0;

    public CardGridView(Activity context, String fromPage, boolean isGame) {
        super(context);
        mContext = context;
        marginleft = (int) mContext.getResources().getDimension(R.dimen.card_topicmagin_22dp);
        this.fromPage = fromPage;
        this.isGame = isGame;
        setOrientation(VERTICAL);
        setGravity(Gravity.CENTER_VERTICAL);
        setDescendantFocusability(FOCUS_BLOCK_DESCENDANTS);
        View view = inflate(context, R.layout.card_gridlist, this);
        findViewById(view);
    }


    private void findViewById(View view) {
        mTopicIcon = (ImageView) view.findViewById(R.id.card_topic_icon_id);
        container = (LinearLayout) view.findViewById(R.id.container);

        mTopicIcon.setOnClickListener(this);
        mTopicIcon.setDrawingCacheEnabled(true);
        setlistener();
    }

    public void setData(CarParentBean bean, int position) {
        if (bean == null)
            return;
        cardBean = bean;
        ImageLoader.getInstance().displayImage(bean.focus.imageUrl,mTopicIcon,UILimageUtil .getUILoptions(R.drawable.topic_icon_background), null);
        for (int i = 0; i < container.getChildCount(); i++) {
            GridViewItem gridViewItem = (GridViewItem) container
                    .getChildAt(i);
            if (i < cardBean.focus.apps.size()) {
                AppsItemBean itemBean = cardBean.focus.apps.get(i);
//                if (!TextUtils.isEmpty(fromPage)) {
//                    itemBean = CommonUtils.formatAppPageInfo(itemBean, Constants.HOME_GUI, Constants.FOCUS, position);
//                } else {
//                    if (isGame) {
//                        itemBean = CommonUtils.formatAppPageInfo(itemBean, Constants.GAME_GUI, Constants.FOCUS, position);
//                    } else {
//                        itemBean = CommonUtils.formatAppPageInfo(itemBean, Constants.APP_GUI, Constants.FOCUS, position);
//                    }
//                }

                gridViewItem.setData(itemBean);
            }
        }
    }


    private void setlistener() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.weight = 1;
        for (int i = 0; i < CARD_APP_COUNT; i++) {
            GridViewItem gridViewItem = new GridViewItem(mContext);
            if (i != 3) {
                params.rightMargin = marginleft;
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
                    if (position < cardBean.focus.apps.size() && cardBean.focus.apps.get(position) != null) {
                        AppsItemBean bean = cardBean.focus.apps.get(position);
                        UIUtils.gotoAppDetail(bean, bean.id, mContext);
                        MTAUtil.onDetailClick(mContext, bean.name, bean.packageName);

                        if (!TextUtils.isEmpty(fromPage)) {
                            MTAUtil.onclicHomeFocusTopic(cardBean.focus.title);
                            MTAUtil.onHomePageFocusClick(cardBean.focus.positon);
                        } else {
                            if (isGame) {
                                MTAUtil.onclicGameFocusTopic(cardBean.focus.title);
                                MTAUtil.onGamePageFocusClick(cardBean.focus.positon);
                            } else {
                                MTAUtil.onclicAppFocusTopic(cardBean.focus.title);
                                MTAUtil.onAppPageFocusClick(cardBean.focus.positon);
                            }
                        }
                    }
                }
            });
            container.addView(gridViewItem);
        }
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.card_topic_icon_id:
                goToTopicActivity();
                break;
            default:
                break;
        }
    }

    private void goToTopicActivity() {
        Intent intent = new Intent(mContext, TopicDetailActivity.class);
        Bundle b = new Bundle();
        b.putSerializable("bean", getTopicBean());
        intent.putExtras(b);
        mContext.startActivity(intent);
        if (!TextUtils.isEmpty(fromPage)) {
            MTAUtil.onclicHomeFocusTopic(cardBean.focus.title);
            MTAUtil.onHomePageFocusClick(cardBean.focus.positon);
        } else {
            if (isGame) {
                MTAUtil.onclicGameFocusTopic(cardBean.focus.title);
                MTAUtil.onGamePageFocusClick(cardBean.focus.positon);
            } else {
                MTAUtil.onclicAppFocusTopic(cardBean.focus.title);
                MTAUtil.onAppPageFocusClick(cardBean.focus.positon);
            }
        }
    }

    private TopicItemBean getTopicBean() {
        TopicItemBean topicItemBean = new TopicItemBean();
        if (cardBean.focus != null) {
            topicItemBean.bigImageUrl = cardBean.focus.imageUrl;
            topicItemBean.id = cardBean.focus.cid;
            topicItemBean.imageUrl = cardBean.focus.imageUrl;
            topicItemBean.createTime = cardBean.focus.updateTime;
            topicItemBean.title = cardBean.focus.title;
        }
        return topicItemBean;
    }

    public int getCount() {
        return CARD_APP_COUNT;
    }

    public AppsItemBean getItem(int position) {
        if (cardBean.focus.apps.get(position) != null) {
            return cardBean.focus.apps.get(position);
        }
        return null;
    }

    public View getChildDownLoadViewAt(int position) {
        GridViewItem itemView = (GridViewItem) container.getChildAt(position);
        return itemView.findViewById(R.id.progressButton_id);
    }

}
