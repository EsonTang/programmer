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

package com.prize.app.beans;

import com.prize.app.net.datasource.base.AppsItemBean;

import java.io.Serializable;

/**
 **
 * 
 * @author nieligang
 * @version V1.0
 */
public class HottestBean implements Serializable {

	private static final long serialVersionUID = 1820078980793267246L;
	public String title;
	public String description;
	public String images;
	public String appId;
	public AppsItemBean app;
}
