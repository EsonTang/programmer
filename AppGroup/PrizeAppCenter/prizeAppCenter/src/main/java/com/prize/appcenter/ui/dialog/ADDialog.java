/*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：
 *当前版本：
 *作	者：
 *完成日期：
 *修改记录：
 *修改日期：
 *版 本 号：
 *修 改 人：
 *修改内容：
 ...
 *修改记录：
 *修改日期：
 *版 本 号：
 *修 改 人：
 *修改内容：
 *********************************************/

package com.prize.appcenter.ui.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.prize.app.beans.ClientInfo;
import com.prize.app.beans.HomeAdBean;
import com.prize.app.constants.Constants;
import com.prize.app.util.CommonUtils;
import com.prize.app.util.DisplayUtil;
import com.prize.app.util.JLog;
import com.prize.app.util.MTAUtil;
import com.prize.app.util.PrizeStatUtil;
import com.prize.appcenter.R;
import com.prize.appcenter.activity.MainActivity;
import com.prize.appcenter.ui.util.UILimageUtil;
import com.prize.statistics.model.ExposureBean;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static com.prize.appcenter.R.id.cancel_Iv;

/**
 * *
 * 首页插屏广告
 *
 * @author longbaoxiu
 * @version V1.0
 */
public class ADDialog extends AlertDialog implements OnClickListener {
    private ImageView content_Iv;
    public static final int CANCEL = 0;
    public static final int SURE = 1;

    public ADDialog(Context context, int theme) {
        super(context, theme);
        View modifyPasswordDialog = getLayoutInflater().inflate(R.layout.ad_dialog, null);
        setView(modifyPasswordDialog, 0, 0, 0, 0);
//        RelativeLayout  mRelativeLayout= (RelativeLayout) modifyPasswordDialog.findViewById(R.id.mRelativeLayout);
//        mRelativeLayout.setBackgroundColor(Color.parseColor("#00ffffff"));
        ImageView cancel_Iv = (ImageView) modifyPasswordDialog.findViewById(R.id.cancel_Iv);
        content_Iv = (ImageView) modifyPasswordDialog.findViewById(R.id.content_Iv);
        cancel_Iv.setOnClickListener(this);
        content_Iv.setOnClickListener(this);
    }

    private String ADID = null;

    public void setHomeBean(final HomeAdBean homeBean) {

        this.ADID = homeBean.title + "_" + homeBean.id;

        if (JLog.isDebug) {
            JLog.i("ADDialog", "setImageUrl.bean=" + homeBean.imageUrl + "--mID=" + this.ADID);
        }
        ImageLoader.getInstance().displayImage(homeBean.imageUrl, content_Iv, UILimageUtil.getADNoLoadingoptions(), new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                if (loadedImage != null) {
                    RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    int cacelViewHight = (int) DisplayUtil.forMatSpAndDp(TypedValue.COMPLEX_UNIT_DIP, 18.0f);
                    int imagWith = loadedImage.getWidth();
                    int imagHigh = loadedImage.getHeight();
                    if (imagWith <= 0 || imagHigh <= 0) return;
                    //以488 630宽度为基准 图片显示区域就是这俩种
                    if (imagWith <= 550) {
                        params1.width = (int) (ClientInfo.getInstance().screenWidth * 0.678);
                    }
                    if (imagWith > 550) {
                        params1.width = (int) (ClientInfo.getInstance().screenWidth * 0.875);
                    }
                    float a = (float) params1.width / imagWith;
                    BigDecimal b = new BigDecimal(a);
                    double f1 = b.setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue();
                    params1.height = (int) (imagHigh * f1);
//                    if (JLog.isDebug) {
//                        JLog.i("ADDialog", "params1.width=" + params1.width + "--imagWith=" + imagWith + "--imagHigh=" + imagHigh + "--params1.height=" + params1.height + "--f1=" + f1 + "--a=" + a);
//                    }
                    if (imagHigh <= 550) {
                        cacelViewHight = (int) DisplayUtil.forMatSpAndDp(TypedValue.COMPLEX_UNIT_DIP, 43.0f);
                    }
                    params1.addRule(RelativeLayout.BELOW, R.id.cancel_Iv);
                    params1.addRule(RelativeLayout.CENTER_HORIZONTAL);
                    params1.setMargins(0, cacelViewHight, 0, 0);
                    content_Iv.setLayoutParams(params1);
//                    ((ImageView) view).setImageBitmap(loadedImage);
                    Window win = getWindow();
                    if (MainActivity.thisActivity == null || MainActivity.thisActivity.isHaveDialogShow)
                        return;
                    try {
                        show();
                    } catch (WindowManager.BadTokenException e) {
                        e.printStackTrace();
                        return;
                    }
                    MainActivity.thisActivity.isHaveDialogShow = true;
                    if (win == null || win.getDecorView() == null) return;
                    win.getDecorView().setPadding(0, 0, 0, 0);
                    WindowManager.LayoutParams lp = win.getAttributes();
                    lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                    if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.N){
                        lp.height = ClientInfo.getInstance().screenHeight;
                    }else{
                        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
                    }
                    win.setAttributes(lp);
                    MTAUtil.onHomeADShow(ADID);
                    if (TextUtils.isEmpty(CommonUtils.getNewTid())) return;
                    if (Constants.BROADCAST_AD_TYPES[6].equals(homeBean.adType) || Constants.BROADCAST_AD_TYPES[7].equals(homeBean.adType)) {
                        if (homeBean.app != null && !TextUtils.isEmpty(homeBean.app.packageName)) {
                            ExposureBean bean = CommonUtils.formNewPagerExposure(homeBean.app, Constants.HOME_GUI, "interstitial");
                            List<ExposureBean> temp = new ArrayList<>();
                            temp.add(bean);
                            PrizeStatUtil.startNewUploadExposure(temp);
                            temp.clear();
                        }
                    }
                }
            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {


            }
        });
    }

    @Override
    public void onClick(View v) {
        if (mOnButtonClic == null)
            return;
        dismiss();
        switch (v.getId()) {
            case cancel_Iv:
                mOnButtonClic.onClick(CANCEL);
                break;
            case R.id.content_Iv:
                mOnButtonClic.onClick(SURE);
                break;
        }
    }

    /**
     * 点击button后的回调
     *
     * @author longbaoxiu
     * @version V1.0
     */
    public static interface OnButtonClic {
        void onClick(int which);
    }

    public OnButtonClic mOnButtonClic;

    public void setmOnButtonClic(OnButtonClic mOnButtonClic) {
        this.mOnButtonClic = mOnButtonClic;
    }

}
