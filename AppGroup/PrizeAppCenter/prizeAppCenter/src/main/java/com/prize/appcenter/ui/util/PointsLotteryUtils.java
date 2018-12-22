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
package com.prize.appcenter.ui.util;

import com.prize.app.constants.Constants;
import com.prize.app.net.datasource.base.AppsItemBean;
import com.prize.app.util.CommonUtils;
import com.prize.app.util.JLog;
import com.prize.app.util.Verification;
import com.prize.custmerxutils.XExtends;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;

import java.util.HashSet;

/**
 * Title:    积分商城-抽奖-兑换弹提示框的逻辑
 * Desc:    在积分商城和抽奖-兑换详情界面用到
 * Version:
 * Created by huangchangguo
 * on   2016/8/22  13:56
 * <p>
 * Update Description: 更新描述
 * Updater:  更新者
 * Update Time:  更新时间
 */

public class PointsLotteryUtils {
    private static final String TAG = "PointsLotteryUtils";
    private static HashSet<String> gettedPkg = new HashSet<String>();
    private static HashSet<String> gettingdPkg = new HashSet<String>();

    public static void add2HashSet(String pkg, boolean isGetting) {
        if (isGetting) {
            gettingdPkg.add(pkg);
        } else {
            gettedPkg.add(pkg);

        }
    }

    public static void remoceFromHashSet(String pkg, boolean isGetting) {
        if (isGetting) {
            gettingdPkg.remove(pkg);
        } else {
            gettedPkg.remove(pkg);

        }
    }

    public static boolean isInHashSet(String pkg, boolean isGetting) {
        if (isGetting)
            return gettingdPkg.contains(pkg);
        return gettedPkg.contains(pkg);
    }

    public static void clearHashSet() {
        gettedPkg.clear();
        gettingdPkg.clear();
    }


//    private static RequestParams mParams;

    public interface ResultLinstener {

        void getresult(String data,AppsItemBean bean);
    }

//    /***
//     * 抽奖
//     *
//     * @param context
//     * @param userId
//     * @param goodsId
//     * @param resultLinstener
//     */
//    public static void RequstPointsLottery(Context context, String userId, int goodsId, final ResultLinstener resultLinstener) {
//        final ProDialog proDialog = new ProDialog(context,
//                ProgressDialog.THEME_HOLO_LIGHT, "加载中，请稍后...");
//
//        if (mParams == null) {
//            mParams = new RequestParams(Constants.GIS_URL + "/point/doexchange");
//        }
//        mParams.addBodyParameter("accountId", String.valueOf(userId));
//        mParams.addBodyParameter("goodsId", String.valueOf(goodsId));
//        String sign = Verification.getInstance().getSign(mParams.getBodyParams());
//        mParams.addBodyParameter("sign", sign);
//
//        XExtends.http().post(mParams, new Callback.CommonCallback<String>() {
//            @Override
//            public void onSuccess(String result) {
//                if (proDialog != null && proDialog.isShowing()) {
//                    proDialog.dismiss();
//                }
//                try {
//                    String data = new JSONObject(result).getString("data");
//                    resultLinstener.getresult(data,null);
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                    resultLinstener.getresult(null,null);
//                }
//
//            }
//
//            @Override
//            public void onError(Throwable ex, boolean isOnCallback) {
//                if (proDialog != null && proDialog.isShowing()) {
//                    proDialog.dismiss();
//                }
//                resultLinstener.getresult(null,null);
//
//            }
//
//            @Override
//            public void onCancelled(CancelledException cex) {
//            }
//
//            @Override
//            public void onFinished() {
//            }
//        });
//
//
//    }

    /***
     * 领取积分
     *
     * @param bean            AppsItemBean
     * @param resultLinstener ResultLinstener
     */
    public static void requstGetPoints(final AppsItemBean bean, final ResultLinstener resultLinstener) {
        RequestParams mParams = new RequestParams(Constants.GIS_URL + "/point/acheive");
        mParams.addBodyParameter("accountId", CommonUtils.queryUserId());
        mParams.addBodyParameter("appId", bean.id);
        mParams.addBodyParameter("packageName", bean.packageName);
        mParams.addBodyParameter("points", bean.points + "");
        String sign = Verification.getInstance().getSign(mParams.getBodyParams());
        mParams.addBodyParameter("sign", sign);

        XExtends.http().post(mParams, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                JLog.i(TAG, "requstGetPoints-result=" + result);
                if (resultLinstener != null) {
                    resultLinstener.getresult(result,bean);
                }

            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                if (resultLinstener != null) {
                    resultLinstener.getresult(null,bean);

                }

            }

            @Override
            public void onCancelled(CancelledException cex) {
            }

            @Override
            public void onFinished() {
            }
        });


    }
}