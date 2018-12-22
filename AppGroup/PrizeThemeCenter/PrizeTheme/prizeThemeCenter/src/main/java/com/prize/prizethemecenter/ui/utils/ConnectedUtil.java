package com.prize.prizethemecenter.ui.utils;

import android.content.Intent;

import com.prize.prizethemecenter.MainApplication;
import com.prize.prizethemecenter.service.PrizeThemeCenterService;

/**
 * Created by pengy on 2016/12/9.
 */
public class ConnectedUtil {

    public static void pauseAllDownload() {
        Intent intentManager = new Intent(MainApplication.curContext,
                PrizeThemeCenterService.class);
        intentManager.putExtra(PrizeThemeCenterService.ACTION,
                PrizeThemeCenterService.ACT_PAUSEALL_TASK);
        intentManager.putExtra(PrizeThemeCenterService.OPT_TYPE, 2);
        MainApplication.getInstance().startService(intentManager);

    }

    public static void continueAllDownload() {
        Intent intentManager = new Intent(MainApplication.curContext,
                PrizeThemeCenterService.class);
        intentManager.putExtra(PrizeThemeCenterService.ACTION,
                PrizeThemeCenterService.ACT_CONTINUE_DOWNLOAD);
        intentManager.putExtra(PrizeThemeCenterService.OPT_TYPE, 2);
        MainApplication.getInstance().startService(intentManager);
    }

}
