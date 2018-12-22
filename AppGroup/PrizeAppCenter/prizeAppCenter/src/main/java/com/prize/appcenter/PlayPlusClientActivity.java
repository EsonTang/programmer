package com.prize.appcenter;

import android.os.Bundle;

import com.prize.app.beans.ClientInfo;
import com.prize.appcenter.activity.FrontCoverActivity;
import com.tencent.android.tpush.XGPushManager;
import com.tencent.stat.MtaSDkException;
import com.tencent.stat.StatConfig;
import com.tencent.stat.StatService;

public class PlayPlusClientActivity extends FrontCoverActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!isTaskRoot()) {
            finish();
            return;
        }
        StatConfig.setAutoExceptionCaught(true);
        StatService.trackCustomEvent(this, "onCreate", "");
        initTengXunAccount();
        setPushTag();
    }

    /**
     * 设置信鸽标签
     */
    private void setPushTag() {
        XGPushManager.setTag(this, ClientInfo.getInstance().channel);
        XGPushManager.setTag(this, ClientInfo.getInstance().model);
    }

    /**
     * 初始化MTA统计平台
     */
    private void initTengXunAccount() {
        // androidManifest.xml指定本activity最先启动
        // 因此，MTA的初始化工作需要在本onCreate中进行
        // 在startStatService之前调用StatConfig配置类接口，使得MTA配置及时生效
        // initMTAConfig(true);
        String appkey = "AYB4VFH8I87H";
        // 初始化并启动MTA
        // 第三方SDK必须按以下代码初始化MTA，其中appkey为规定的格式或MTA分配的代码。
        // 其它普通的app可自行选择是否调用
        try {
            // 第三个参数必须为：com.tencent.stat.common.StatConstants.VERSION
            StatService.startStatService(this.getApplicationContext(), appkey,
                    com.tencent.stat.common.StatConstants.VERSION);
        } catch (MtaSDkException e) {
            // MTA初始化失败
        }
    }
}
