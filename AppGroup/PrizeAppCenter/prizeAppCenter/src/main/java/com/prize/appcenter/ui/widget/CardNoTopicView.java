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

import android.graphics.Rect;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.prize.app.net.datasource.base.AppsItemBean;
import com.prize.app.net.datasource.home.CarParentBean;
import com.prize.app.util.CommonUtils;
import com.prize.app.util.DisplayUtil;
import com.prize.app.util.MTAUtil;
import com.prize.appcenter.R;
import com.prize.appcenter.activity.RootActivity;
import com.prize.appcenter.ui.adapter.NotopicRcycleAdapter;
import com.prize.appcenter.ui.util.UIUtils;

/**
 * 类描述：无背景图专题类型
 *
 * @author 龙宝修
 * @version 1.0
 */
public class CardNoTopicView extends LinearLayout {
    private RootActivity mContext;
    private CarParentBean cardBean;
    private String fromPage;
    private boolean isGame = false;
    private TextView title;
    private NotopicRcycleAdapter myAdapter;
    private LinearLayoutManager mLinearLayoutManager;

    public CardNoTopicView(RootActivity context, String fromPage, boolean isGame) {
        super(context);
        mContext = context;
        this.fromPage = fromPage;
        this.isGame = isGame;
        setOrientation(VERTICAL);
        setGravity(Gravity.CENTER_VERTICAL);
        setDescendantFocusability(FOCUS_BLOCK_DESCENDANTS);
        View view = inflate(context, R.layout.card_notopic_recycleview, this);
        findViewById(view);
    }


    private void findViewById(View view) {
        title = (TextView) view.findViewById(R.id.title);
        RecyclerView mRecyclerView = (RecyclerView) view.findViewById(R.id.mRecyclerView);
        // 提高性能
        mRecyclerView.setHasFixedSize(true);
        mLinearLayoutManager = new LinearLayoutManager(mContext);
        mLinearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mRecyclerView.addItemDecoration(new SpaceItemDecoration((int) DisplayUtil.forMatSpAndDp(TypedValue.COMPLEX_UNIT_DIP, 27.0f)));
        myAdapter = new NotopicRcycleAdapter(mContext);
        mRecyclerView.setAdapter(myAdapter);
        myAdapter.setOnItemClickListener(new NotopicRcycleAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (CommonUtils.isFastDoubleClick())
                    return;
                if (position < cardBean.focus.apps.size() && cardBean.focus.apps.get(position) != null) {
                    AppsItemBean bean = cardBean.focus.apps.get(position);
                    UIUtils.gotoAppDetail(bean, bean.id, mContext);
                    MTAUtil.onDetailClick(mContext, bean.name, bean.packageName);
                    if (!TextUtils.isEmpty(CardNoTopicView.this.fromPage)) {
                        MTAUtil.onclicHomeFocusNoPicTopic(cardBean.focus.title);
                        MTAUtil.onHomePageFocusClick(cardBean.focus.positon);
                    } else {
                        if (CardNoTopicView.this.isGame) {
                            MTAUtil.onclicGameFocusNoPicTopic(cardBean.focus.title);
                            MTAUtil.onGamePageFocusClick(cardBean.focus.positon);
                        } else {
                            MTAUtil.onclicAppFocusNoPicTopic(cardBean.focus.title);
                            MTAUtil.onAppPageFocusClick(cardBean.focus.positon);
                        }
                    }
                }
            }
        });
    }

    public void setData(CarParentBean bean, int position) {
        if (bean == null || bean.focus == null || bean.focus.apps == null)
            return;
        cardBean = bean;
        title.setText(bean.focus.title);
        myAdapter.updateData(cardBean.focus.apps);
    }

    public int getCount() {
        if (cardBean == null || cardBean.focus == null || cardBean.focus.apps == null) {
            return 0;
        }
        return cardBean.focus.apps.size();
    }

    public AppsItemBean getItem(int position) {
        if (cardBean.focus.apps.get(position) != null) {
            return cardBean.focus.apps.get(position);
        }
        return null;
    }

    public void updatState(String packageName) {
        if (TextUtils.isEmpty(packageName)) return;
        int lastItemPosition = mLinearLayoutManager.findLastVisibleItemPosition();
        //获取第一个可见view的位置
        int visiblePosition = mLinearLayoutManager.findFirstVisibleItemPosition();
        AppsItemBean bean;
        for (int i = visiblePosition; i <= lastItemPosition; i++) {
            if (i >= getCount()) return;
            bean = cardBean.focus.apps.get(i);
            if(bean==null)continue;
            if (!TextUtils.isEmpty(packageName)&&packageName.equals(bean.packageName)) {
                View parentView = mLinearLayoutManager.getChildAt(i - visiblePosition);
                View subView = parentView.findViewById(R.id.progressButton_id);
                if (subView != null) {
                    subView.invalidate();
                }
            }
        }
    }


    public class SpaceItemDecoration extends RecyclerView.ItemDecoration {

        private int space;
        private int leftSpace;

        public SpaceItemDecoration(int space) {
            this.space = space;
            this.leftSpace = (int) DisplayUtil.forMatSpAndDp(TypedValue.COMPLEX_UNIT_DIP, 13.0f);
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            outRect.right = space;
            if (parent.getChildPosition(view) == 0) {
                outRect.left = leftSpace;
            }
        }
    }
}
