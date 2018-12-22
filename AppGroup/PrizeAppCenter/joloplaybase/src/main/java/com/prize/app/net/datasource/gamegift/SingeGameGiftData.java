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

package com.prize.app.net.datasource.gamegift;

import java.util.ArrayList;

import com.prize.app.beans.GameListGiftBean;
import com.prize.app.beans.GiftPkgItemBean;
import com.prize.app.net.AbstractNetData;
import com.prize.app.net.datasource.base.AppsItemBean;

/**
 * 
 ** 单个游戏的所有礼包数据
 * 
 * @author longbaoxiu
 * @version V1.0
 */
public class SingeGameGiftData extends AbstractNetData {
	public AppsItemBean app;
	public ArrayList<GiftPkgItemBean> gifts = new ArrayList<GiftPkgItemBean>();
	public int pageCount;
	public int pageIndex;
	public int pageSize;
	public int pageItemCount;
	public ArrayList<GameListGiftBean> appGifts = new ArrayList<GameListGiftBean>();
}
