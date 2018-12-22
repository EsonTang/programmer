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

package com.prize.music.helpers;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;

import com.prize.app.beans.ClientInfo;
import com.prize.music.MainApplication;
import com.prize.music.service.PrizeMusicService;
import com.prize.onlinemusibean.SongDetailInfo;

/**
 * 单个下载（包含暂停 ）
 **
 * @author longbaoxiu
 * @version V1.0
 */
public class DownLoadUtils {
	
	/**
	 * 
	 * 单个下载
	 * @param songDetailInfo 
	 * @return void 
	 */
	public static void downloadMusic(SongDetailInfo songDetailInfo) {
		int netType = ClientInfo.networkType;
		if ((netType == ClientInfo.MOBILE_3G)
				|| (netType == ClientInfo.MOBILE_2G)) {
		}
		startDownloadService(songDetailInfo);
	}

	/**
	 * 批量下载
	 * 
	 * @param songDetailInfos
	 * @return void
	 * @see
	 */
	public static void downloadMultMusic(ArrayList<SongDetailInfo> songDetailInfos) {
		if (songDetailInfos == null || songDetailInfos.size() <= 0)
			return;
		startDownloadBatchService(songDetailInfos);
	}

	protected static void startDownloadBatchService(ArrayList<SongDetailInfo> songDetailInfo) {
		Intent intentManager = new Intent(MainApplication.curContext,
				PrizeMusicService.class);
		Bundle bundle = new Bundle();
		bundle.putParcelableArrayList("list", songDetailInfo);
		bundle.putInt(PrizeMusicService.ACTION, PrizeMusicService.ACT_DOWNLOAD);
		intentManager.putExtras(bundle);
		MainApplication.curContext.startService(intentManager);
	}
	protected static void startDownloadService(SongDetailInfo songDetailInfo) {
		Intent intentManager = new Intent(MainApplication.curContext,
				PrizeMusicService.class);
		Bundle bundle = new Bundle();
		bundle.putParcelable("bean", songDetailInfo);
		bundle.putInt(PrizeMusicService.ACTION, PrizeMusicService.ACT_DOWNLOAD);
		intentManager.putExtras(bundle);
		MainApplication.curContext.startService(intentManager);
	}
}
