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
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.prize.app.net.datasource.base.PrizeAppsCardData;
import com.prize.app.util.MTAUtil;
import com.prize.appcenter.R;
import com.prize.appcenter.ui.util.UILimageUtil;
import com.prize.appcenter.ui.util.UIUtils;

/**
 * 类描述：首页card（应用雷达）
 *
 * @author longbaoxiu
 * @version 1.0
 */
public class CardAppsinView extends RelativeLayout {
    private Activity mContext;
    private ImageView appsin_bgImg;
    private RelativeLayout container_Rlyt;
    private OnClickListener mOnClickListener;
    private  PrizeAppsCardData.FocusBean item;
    public CardAppsinView(Activity context, final String fromPage, final boolean isGame) {
        super(context);
        mContext = context;
        View view = inflate(context, R.layout.card_appsinview, this);
        findViewById(view);
        mOnClickListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = (int) v.getTag();
                if(item==null||item.apps==null||position>=item.apps.size())
                    return;
                UIUtils.gotoAppDetail(item.apps.get(position), item.apps.get(position).id, mContext);
                MTAUtil.onclicappsin(item.title,position+1);
                if(!TextUtils.isEmpty(fromPage)) {
                    MTAUtil.onHomePageFocusClick(item.positon);
                }else {
                    if(isGame){
                        MTAUtil.onGamePageFocusClick(item.positon);
                    }else {
                        MTAUtil.onAppPageFocusClick(item.positon);
                    }
                }
            }
        };
    }


    private void findViewById(View view) {
        appsin_bgImg = (ImageView) view.findViewById(R.id.appsin_bgImg);
        container_Rlyt = (RelativeLayout) view.findViewById(R.id.container_Rlyt);
    }

    public void setData(PrizeAppsCardData.FocusBean item) {
        this.item=item;
        ImageLoader.getInstance().displayImage(item.imageUrl, appsin_bgImg,UILimageUtil.getUILoptions(R.drawable.bg_ad));
        int size = container_Rlyt.getChildCount();
        int dataSize = item.apps.size();
        for (int i = 0; i < size; i++) {
            if (i >= dataSize) {
                return;
            }
            SubCardAppsinView view = (SubCardAppsinView) container_Rlyt.getChildAt(i);
            view.setTag(i);
            view.setOnClickListener(mOnClickListener);
            view.setData(item.apps.get(i));
        }
    }

}
