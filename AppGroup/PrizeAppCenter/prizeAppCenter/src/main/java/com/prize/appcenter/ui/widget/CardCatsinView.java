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
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.prize.app.net.datasource.base.PrizeAppsCardData;
import com.prize.app.util.MTAUtil;
import com.prize.appcenter.R;
import com.prize.appcenter.activity.CategoryAppGameListActivity;
import com.prize.appcenter.ui.util.UILimageUtil;

import static com.prize.appcenter.activity.CategoryAppGameListActivity.parentID;
import static com.prize.appcenter.activity.CategoryAppGameListActivity.subtypeName;
import static com.prize.appcenter.activity.CategoryAppGameListActivity.typeName;

/**
 * 类描述：首页card（应用分类气泡）
 *
 * @author huanglingjun
 * @version 1.0
 */
public class CardCatsinView extends RelativeLayout {
    private Activity mContext;
    private ImageView catsin_bgImg;
    private RelativeLayout container_Rlyt;
    private OnClickListener mOnClickListener ;
    private PrizeAppsCardData.FocusBean mFocusBean;
    public CardCatsinView(Activity context, final String fromPage, final boolean isGame) {
        super(context);
        mContext = context;
        View view= inflate(context, R.layout.card_catsinview, this);
        findViewById(view);
        mOnClickListener= new OnClickListener() {
            @Override
            public void onClick(View v) {
                PrizeAppsCardData.CatFocusBean bean= (PrizeAppsCardData.CatFocusBean) v.getTag();
                gotoCategoryAppGameListActivity(bean);

                if(!TextUtils.isEmpty(fromPage)) {
                    MTAUtil.onHomePageFocusClick(mFocusBean.positon);
                }else {
                    if(isGame){
                        MTAUtil.onGamePageFocusClick(mFocusBean.positon);
                    }else {
                        MTAUtil.onAppPageFocusClick(mFocusBean.positon);
                    }
                }
            }
        };
    }


    private void findViewById(View view) {
        catsin_bgImg= (ImageView) view.findViewById(R.id.catsin_bgImg);
        container_Rlyt= (RelativeLayout) view.findViewById(R.id.container_Rlyt);
    }

    public void setData(PrizeAppsCardData.FocusBean item) {
        mFocusBean = item;
        catsin_bgImg.setDrawingCacheEnabled(true);
        ImageLoader.getInstance().displayImage(item.imageUrl,catsin_bgImg, UILimageUtil.getUILoptions(R.drawable.bg_ad));
        int size =container_Rlyt.getChildCount();
        int dataSize=item.catFocusList.size();
        for(int i=0;i<size;i++){
            if(i>=dataSize)
                return;
            TextView view= (TextView) container_Rlyt.getChildAt(i);
            view.setText(item.catFocusList.get(i).catName);
            view.setOnClickListener(mOnClickListener);
            view.setTag(item.catFocusList.get(i));
        }
    }

    private void gotoCategoryAppGameListActivity(PrizeAppsCardData.CatFocusBean  bean){
        Intent intent = new Intent(mContext,
                CategoryAppGameListActivity.class);
        intent.putExtra(parentID, bean.cId);
        intent.putExtra(typeName, bean.pCatName);
        intent.putExtra(subtypeName, bean.catName);
        intent.putExtra(CategoryAppGameListActivity.tags,bean.tags);
        intent.putExtra(CategoryAppGameListActivity.selectPos,bean.cIdpos);
        intent.putExtra(CategoryAppGameListActivity.isGameKey, (bean.catTypeId)==2);
        mContext.startActivity(intent);
        if ((bean.catTypeId)==2) {
            MTAUtil.onClickGameCategoryTAB(bean.pCatName);
        } else {
            MTAUtil.onClickAppCategoryTAB(bean.pCatName);
        }
        MTAUtil.onclicGameAppCardCatsinView((bean.catTypeId)==2,bean.catName);
    }



}
