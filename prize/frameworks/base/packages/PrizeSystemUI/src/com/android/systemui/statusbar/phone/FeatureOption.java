package com.android.systemui.statusbar.phone;
/*****************************************
*版权所有©2015,深圳市铂睿智恒科技有限公司
*
*内容摘要：读取通知栏快速设置排序的宏定义开关
*当前版本：V1.0
*作  者：liufan
*完成日期：2015-4-8
*修改记录：
*修改日期：
*版 本 号：
*修 改 人：
*修改内容：
********************************************/
import android.os.SystemProperties;
/**
* 类描述：通知栏快速设置排序的宏定义开关实体类
* @author liufan
* @version V1.0
*/
public class FeatureOption {
    /** 控制通知栏快速设置排序的开关，true是打开，false是关闭 */
    public static final boolean PRIZE_QS_SORT = true;//getValue("ro.prize_qs_sort");
    

    /**
     * 方法描述：读取配置的宏开关的值，配置值1对应true，其他对应false
     * @param String key
     * @return boolean
     */
    //private static boolean getValue(String key) {
    //    return SystemProperties.get(key).equals("1");
    //}
}
