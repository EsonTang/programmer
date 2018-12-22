/*****************************************
*版权所有©2015,深圳市铂睿智恒科技有限公司
*
*内容摘要：SystemUI版本控制
*当前版本：V1.0
*作  者：liufan
*完成日期：2015-6-16
*修改记录：
*修改日期：
*版 本 号：
*修 改 人：
*修改内容：
********************************************/
package com.android.systemui.statusbar.phone;
import com.mediatek.common.prizeoption.PrizeOption;
import android.provider.Settings;
import android.content.Context;
import android.util.Log;
import android.app.KeyguardManager;
/**
* 枚举：SystemUI版本控制
* @author liufan
* @version V1.0
*/
public enum VersionControl {
    
    COLOR_BG_VER,//1、纯颜色版本
    BLUR_BG_VER;//2、通知栏和锁屏改为磨砂背景的版本

    public static final VersionControl CUR_VERSION = PrizeOption.PRIZE_SYSTEMUI_BLUR_BG ? VersionControl.BLUR_BG_VER : VersionControl.COLOR_BG_VER;//定义当前版本
    public static final boolean isDismissScrimView = PrizeOption.PRIZE_SYSTEMUI_SCRIM_BG;
    public static boolean isAllowDropDown = true;//Lockscreen is allow drop down

    public static boolean isAllowDropDown(Context context){
        KeyguardManager keyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        isAllowDropDown = Settings.System.getInt(context.getContentResolver(),
            Settings.System.PRIZE_ALLOW_DROPDOWN_NOTIFICATIONBAR_SWITCH, 0) == 1 ? true : false;
        boolean isUseHaoKan = Settings.System.getInt(context.getContentResolver(),
                Settings.System.PRIZE_MAGAZINE_KGWALLPAPER_SWITCH, 0) == 1 ? true : false;
        if(PrizeOption.PRIZE_SYSTEMUI_HAOKAN_SCREENVIEW && isUseHaoKan){
            return isAllowDropDown && NotificationPanelView.IS_ShowNotification_WhenShowHaoKan && !keyguardManager.isKeyguardSecure();
        } else {
            return isAllowDropDown && !keyguardManager.isKeyguardSecure();
        }
        //return isAllowDropDown && NotificationPanelView.HaokanShow && !keyguardManager.isKeyguardSecure(); //prize modify by xiarui 2017-12-18 Bug#45733
    }

    public static final String PRIZE_LOCK_STATE = "prize_lock_state";
    //it's not allowed to drop down screen when the new LockScreen appeared.
    public static boolean isNewLockscreenAllowDropDown(Context ctx){
        boolean result = Settings.System.getInt(ctx.getContentResolver(), PRIZE_LOCK_STATE, 0) == 0;
        return  result ;//1：lock, 0：unlock
    }
}
