/*******************************************
 * 版权所有©2015,深圳市铂睿智恒科技有限公司
 * <p/>
 * 内容摘要：首页gallery的广告bean
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

package com.prize.app.beans;

import com.prize.app.net.datasource.base.AppsItemBean;

import java.io.Serializable;

/**
 **
 * 首页gallery的广告bean
 *
 * @author longbaoxiu
 * @version V1.0
 */
public class HomeAdBean implements Serializable {

    /** 用一句话描述这个变量表示什么 */
    private static final long serialVersionUID = -4921593634744607844L;
    public String associateId;
    /** * 判断是专题（topic）还是单个app（app），再依此跳转 */
    public String adType;
    /** * 名称 */
    public String title;
    /** * 图片地址 */
    public String imageUrl;
    public String bigImageUrl;
    /** *跳转地址 */
    public String url;
    /** * 描述语句 */
    public String description;
    /** * 添加的事件 */
    public String addTime;
    /** * 广播展示开始时间 */
    public String startTime;
    /** * 广播展示结束时间 */
    public String endTime;
    public int status;
    public int seconds;
    public String id;
    public AppsItemBean app;

    @Override
    public String toString() {
        return "HomeAdBean [associateId=" + associateId + ", adType=" + adType
                + ", title=" + title + ", imageUrl=" + imageUrl
                + ", description=" + description + ", addTime=" + addTime
                + ", startTime=" + startTime + ", endTime=" + endTime
                + ", status=" + status + "]";
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof HomeAdBean){
            HomeAdBean st=(HomeAdBean) o;
            return (id.equals(st.id));
        }else{
            return super.equals(o);
        }
    }
}
