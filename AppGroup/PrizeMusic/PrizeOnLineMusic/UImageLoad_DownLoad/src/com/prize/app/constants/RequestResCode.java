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

package com.prize.app.constants;

/**
 * 
 **
 * 请求返回状态
 * 
 * @author longbaoxiu
 * @version V1.0
 */
public class RequestResCode {
	public static final int REQUEST_OK = 0;
	public static final int REQUEST_FAILE = REQUEST_OK + 1;
	public static final int REQUEST_EXCEPTION = REQUEST_FAILE + 1;
	public static final String POST = "post"; // 歌曲收藏操作,收藏
	public static final String CANCEL = "cancel"; // 歌曲取消收藏，取消
	public static final String url_1 = Constants.GIS_URL + "/collection/song"; // 收藏单首歌曲
	public static final String url_2 = Constants.GIS_URL + "/collection/songs"; // 批量收藏歌曲
}
