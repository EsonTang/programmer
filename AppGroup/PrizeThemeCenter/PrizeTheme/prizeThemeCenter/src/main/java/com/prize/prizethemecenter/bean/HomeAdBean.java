/*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：首页gallery的广告bean
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

package com.prize.prizethemecenter.bean;

import java.io.Serializable;

/**
 * gallery的广告bean
 * @author pengy
 * @version V1.0
 */
public class HomeAdBean implements Serializable {
	
	private static final long serialVersionUID = -4921593634744607844L;
    public String id;
    public int position;
	/** * 名称 */
    public String name;
	/** * 图片地址 */
	public String imageUrl;
	public String bigImageUrl;
	/** * 判断是专题（topic）还是单个app（app），再依此跳转 */
	public String ad_type;
	public String correlation_id;
	public String  carousel_time;
	public int ad_status;
	public int status;
	public int sort;
	/** * 添加的事件 */
	public String addtime;


}
