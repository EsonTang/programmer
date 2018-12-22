package com.prize.smartcleaner;

/**
 * Created by xiarui on 2018/1/8.
 */

public class Constant {


    public static final String DEFAULT_LOCATION_APP = "map,navi";

    //hidelist will be not show in purebackgound ui, and will not be killed
    public static final String[] whiteList = {
            //third market
            "com.baidu.appsearch",
            "com.qihoo.appstore",
            "com.sogou.androidtool",
            "com.wandoujia.phoenix2",
            "com.pp.assistant",
            "com.hiapk.marketpho",
            "com.tencent.android.qqdownloader",
            "com.ekesoo.font",
            "com.nd.assistance",
            "com.oem91.market",
            "com.dragon.android.pandaspace",

            //input method
            "input",
            /*
            "com.baidu.input",
            "com.sohu.inputmethod.sogou",
            "com.iflytek.inputmethod",
            */
            "com.tencent.qqpinyin",

            //map
            "map",
            /*
            contain map will be hide
            "com.autonavi.minimap",
            "com.sogou.map.android.maps",
            */
            "com.baidu.BaiduMap",
            "navi",
            /*
            contain navi will be hide
            "com.autonavi.xmgd.navigator",
            */


            //music
            "music",
            /*
            contains music will be hide
            "com.tencent.qqmusic",
            */
            "com.kugou.android",
            "cn.kuwo.player",
            "com.ting.mp3.android",
            "com.duomi.android",
            "com.tencent.karaoke",

            //FM
            "fm.qingting.qtradio",
            "com.douban.radio",
            "com.ximalaya.ting.android",
            "com.sds.android.ttpod",
            "bubei.tingshu",
            "fm.xiami.main",
            "com.android.fmradio",


            //android app
            "com.android.",
            /*"com.android.launcher",
            "com.android.deskclock",
            "com.android.stk",
            "com.android.mms",
            "com.android.dialer",
            "com.android.documentsui",
            "com.android.soundrecorder",
            "com.android.contacts",
            "com.android.settings",
            "com.android.email",
            "com.android.purebackground",*/


            //mtk
            "com.mediatek",

            //prize app
            "com.koobee.koobeecenter",
            "com.pr.scuritycenter",
            "com.android.floatwindow",
            "com.android.lpserver",
            "com.prize",
            /*
            contain "com.prize" will be hide
            "com.prize.prizeappoutad",
            "com.prize.rootcheck",
            "com.prize.tts",
            "com.prize.appcenter",
            "com.prize.luckymonkeyhelper",
            */

            //other
            "com.goodix.fpsetting",

    };
}
