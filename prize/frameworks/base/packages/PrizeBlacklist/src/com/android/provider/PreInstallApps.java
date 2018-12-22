package com.android.provider;

public class PreInstallApps {
	/*
	public static final String[] preInstallApps = {
		"com.prize.prizelockscreen"
	};
	
	public static final int FLOATINGDEFUALT = 1;
	*/

	////////////////////////////////////////////////////
	//floatwindow
	public static final String[] floatwindow_defenablelist = {
        "com.example.longshotscreen",
        "com.android.gallery3d",
        "com.android.floatwindow",
        "com.prize.music",
        "com.prize.smart",
	"com.android.systemui",
		"com.goodix.fpsetting",
        "com.android.calculator2",
		"com.tencent.mm",
		"com.tencent.mobileqq",
		"com.prize.prizeappoutad",
		"com.prize.appcenter",
		"cn.com.htjc.pay",	
        "com.prize.barragewindow",
		
		//third app
		"com.jjapp.quicktouch.inland",
		"com.andreader.prein",//miguyuedu
		"com.kdx.game.z_play_3d.config", // add for konka 3D 
		"com.cmbchina.ccd.pluto.cmbActivity",//zhangshangshenghuo

		//music
		"com.kugou.android",
		"com.tencent.qqmusic",
		"com.tencent.karaoke",
		"cmccwm.mobilemusic",
		"com.ting.mp3.android",
		"cn.kuwo.player",
		"com.changba",
		"com.gwsoft.imusic.controller",
		"fm.xiami.main",
		"com.yinyuetai.ui",
		"com.netease.cloudmusic",
		"com.sonyericsson.trackid",
		"com.melodis.midomiMusicIdentifier.freemium",
		"com.android.mediacenter",
		"com.sds.android.ttpod",
		"com.sonyericsson.music",
		"com.gionee.ringtone",
		"com.voicedragon.musicclient",
		"com.meile.mobile.scene",

		"fm.qingting.qtradio",
		"cn.kuwo.tingshu",
		"com.tencent.radio",
		"com.yibasan.lizhifm",
		"air.GSMobile",
		"com.itings.myradio",
		"com.imusic.iting",
		"com.audio.tingting",
		"com.pianke.client",
		"com.xinli.fm",
		"com.shinyv.cnr",
		"com.ifeng.fhdt",
		"com.douban.radio",
		"org.ajmd",
		
		//system preInstall apps
		"com.mediatek.mtklogger",//version 11 add
		"com.android.calendar",
		"com.android.contacts",
		"com.android.fileexplorer",
		"com.android.dialer",
		"com.android.phone",// version 11 add
		"com.android.certinstaller",
		"com.android.email",
		"com.android.speechrecorder",
		"com.prize.factorytest",
		"com.android.compass",
		"com.android.exchange",
		"com.android.soundrecorder",
		"com.android.mms",
		"com.android.launcher",
		"com.android.settings",
		"com.example.framerecorder",
		"com.android.provision",
		"com.android.contacts.common",
		"com.android.quicksearchbox",
		"com.android.packageinstaller",
		"com.prize.flash",
		"com.mediatek.fmradio",
		"com.android.cellbroadcastreceiver",
		"com.android.deskclock",
		"com.android.terminal",
		"com.android.spare_parts",
		"org.imei.prize",
		"com.android.nfc",
		"com.android.camera2",
		"com.android.purebackground",
		"com.android.notepad",
		"com.prize.htmlviewer",
		"com.android.bluetooth",
		"com.android.music",
		"com.android.htmlviewer",
		"com.android.protips",
		"com.android.stk",
		"com.prize.lockscreen",
		"com.android.prizefloatwindow",
		"com.android.launcher3",
		"com.android.onetimeinitializer",
		"com.android.musicfx",
		"com.android.superpowersave",
		"com.android.managedprovisioning",
		"com.android.gallery",
		"com.prize.prizehwinfo",
		"com.android.voicedialer",
		"com.android.keychain",
		"com.android.apps.tag",
		"com.android.camera",
		"com.android.basicsmsreceiver",
		"com.android.fmradio",
		"com.android.dreams.phototable",
		"com.android.dreams.basic",
		"com.android.dreams.web",
		"com.android.wallpaper.holospiral",
		"com.android.galaxy4",
		"com.android.wallpaper.livepicker",
		"com.android.musicvis",
		"com.android.phasebeam",
		"com.android.noisefield",
		"com.android.wallpaper",
		"com.android.magicsmoke",
		"com.mediatek.providers.drm",
		"com.mediatek.mage.plant.p2",
		"com.opera.max.loader",
		"com.mediatek.voiceunlock",
		"com.mediatek.app.mtv",
		"com.mediatek.engineermodecmas",
		"com.mediatek.ygps",
		"com.mediatek.StkSelection",
		"com.mediatek.mediatekdm",
		"com.mediatek.gba",
		"com.mediatek.nfc.dta",
		"com.mediatek.hotknot.verifier",
		"com.mediatek.regionalphone",
		"com.mediatek.miravision.ui",
		"com.mediatek.multiwindow.service",
		"com.mediatek.deviceregister",
		"com.mediatek.thermalmanager",
		"com.mediatek.wifi.hotspot.em",
		"com.mediatek.hetcomm",
		"com.mediatek.mtksartestprogram",
		"com.mediatek.dataprotection",
		"com.mediatek.cta",
		"com.mtk.offlinek",
		"com.dsi.ant.server",
		"com.mediatek.systemupdate",
		"com.mediatek.matv" ,
		"com.mediatek.apst.target",
		"com.mediatek.FMTransmitter",
		"com.mediatek.batterywarning",
		"com.mediatek.floatmenu",
		"com.mediatek.voicecommand",
		"com.mediatek.ppl",
		"com.hesine.nmsg",
		"com.mediatek.engineermode",
		"com.mediatek.smsreg",
		"com.mediatek.nlpservice",
		"com.mediatek.schpwronoff",
		"com.mediatek.datatransfer",
		"com.mediatek.smartmotion",
		"com.mediatek.hotknotbeam",
		"com.mediatek.security",
		"com.mediatek.calendarimporter",
		"com.android.simmelock",
		"com.mediatek.dongle",
		"com.orangelabs.rcs",
		"com.mediatek.rcs",
		"com.mediatek.systemupdate.sysoper",
		"com.mediatek.omacp",
		"com.mediatek.hotknot.common.ui",
		"com.mediatek.filemanager",
		"com.android.email.policy",
		"com.mediatek.atci.service",
		"com.mediatek.sensorhub.settings",
		"com.mtk.telephony",
		"com.mediatek.connectivity",
		"com.mediatek.cellbroadcastreceiver",
		"com.mediatek.connectivity.EpdgTestApp",
		"com.mediatek.blemanager",
		"com.mediatek.voiceextension",
		"com.android.backupconfirm",
		"com.android.badservicesysserver",
		"com.android.captiveportallogin",
		"com.android.documentsui",
		"com.android.defcontainer",
		"com.android.fakeoemfeatures",
		"com.android.location.fused",
		"com.android.inputdevices",
		"com.android.keyguard",
		"com.android.printspooler",
		"com.android.systemui",
		"com.android.vpndialogs",
		"com.android.wallpapercropper",
		"com.android.smspush"
	};

	public static final int FLOATINGDEFUALT = 1;
	
	////////////////////////////////////////////////////////////////////
	//enable==DEFENABLE && isserver = 1 (serverconfig)
	//default turn on ,value=1
	public static final String[] prize_switchon_list = {
	    "persist.sys.bitmap.pd",//bitmap preparetodraw
       };
	//purebackground
	public static final int DISABLE = 0;
	public static final int ENABLE = 1;
	public static final int HIDE = 2;//purebackground,notification
	public static final int DEFENABLE = 3;//purebackground,notification,autolaunch
	public static final int NOTKILL = 4;//purebackground
	//hidelist will be not show in purebackgound ui, and will not be killed
	public static final String[] purebackground_hidelist = {
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
		"com.android.prizefloatwindow",		
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
	//default will be select in purebackground, and will not be killed when selected
	public static final String[] purebackground_defenablelist = {		
		//tools
		"com.sdu.didi.psnger",
		"com.tencent.mobileqq",
		"com.tencent.mm",
		"cn.com.fetion",
		"com.alibaba.android.rimet",
		//sport
		"com.boohee.one",
		"com.codoon.gps",
		"com.hnjc.dl",
		"com.rjfittime.app",
		"co.runner.app",
		"com.bamboo.ibike",
		"com.lipian.gcwds",

		"com.android.providers.downloads.ui",

		//launcher
		"wallpaper",
		"theme",								
	};
	//delay kill in disable background run
	public static final String[] purebackground_notkilllist = {
	       //shopping
	       "com.taobao.taobao",        //taobao
	       "com.tmall.wireless",	    //tianmao
		"com.jingdong.app.mall",  //jingdong
		"com.suning.mobile.ebuy",//suningyigou
		"com.achievo.vipshop",	   //weipinghui
		"cn.amazon.mShop.android",//yamaxun
		
		"com.eg.android.AlipayGphone",//zhifubao
			
		
		//reading
             "com.qq.reader", //qqyuedu
             "com.iyd.reader.ReadingJoy", //aiyuedu
             "com.chaozh.iReader",  //zhangyue
             "com.andreader.prein",
             "com.ophone.reader.ui", //miguyuedu
             
		//browser
		"com.qihoo.browser",//360browser
		"sogou.mobile.explorer",//sogou
		"com.tencent.mtt", //qq
		"com.UCMobile",   //uc
		"com.android.browser",
		
	};
	////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////
	//notification
	public static final String[] notification_hidelist = {
		//android core app
		"com.android.dialer",
		"com.android.contacts",
		"com.android.mms",
		"com.android.phone",
		"com.android.server.telecom",
		"com.android.settings",
		"com.android.calendar",
		"com.android.deskclock",
		"com.android.email",
		"com.android.gallery3d",
		"com.android.music",	
		"com.android.settings",
		"com.android.soundrecorder",
		"com.android.stk",
		"com.android.utk",
		"com.android.calculator2",
		"com.android.systemui",
		"com.android.exchange",
		"com.android.bluetooth",		
		"com.android.galaxy4",
		"com.android.browser",
		"com.android.notepad",	
		"com.android.shell",
		"com.android.providers.downloads.ui",
		"com.android.packageinstaller",		
		"com.android.launcher3",
		"com.android.wallpaper",
		
		//android other app
		"com.android.fileexplorer",		
		"com.android.development",		
		"com.android.calllogbackup",		
		"com.android.documentsui",		
		"com.android.externalstorage",
		"com.android.htmlviewer",		
		"com.android.defcontainer",
		"com.android.phasebeam",
		"com.android.sharedstoragebackup",
		"com.android.printspooler",
		"com.android.dreams.basic",
		"com.android.webview",
		"com.android.inputdevices",
		"com.android.onetimeinitializer",
		"com.android.keychain",
		"com.android.proxyhandler",
		"com.android.inputmethod.latin",
		"com.android.managedprovisioning",
		"com.android.dreams.phototable",		
		"com.android.noisefield",		
		"com.android.HorCali",		
		"com.android.vpndialogs",
		"com.android.captiveportallogin",
		"com.android.backupconfirm",
		"com.android.statementservice",		
		"com.android.providers.downloads",
			
		//meditek
		"com.mediatek",		
		"com.goodix.fpsetting",		
		"com.mtk.telephony",
		
		//prize
		"com.prize.flash",
		"com.prize.music",		
		"com.prize.factorytest",
		"com.prize.appcenter",
		"com.prize.rootcheck",
		
	};
	public static final String[] notification_defenablelist = {
		"com.tencent.mm",
		"com.tencen1.mm",
		"com.tencent.mobileqq",
		"com.tencent.wework",
		"com.koobee.koobeecenter02",
		"com.android.fmradio",
		"com.pr.scuritycenter",
		"com.prize.weather",
		"com.adups.fota",
		"com.prize.compass",
		"com.prize.prizethemecenter",
		"com.prize.lockscreen",
		"com.prize.debug.tool",
		"com.prize.htmlviewer",


		//music
		"com.kugou.android",
		"com.tencent.qqmusic",
		"com.tencent.karaoke",
		"cmccwm.mobilemusic",
		"fm.xiami.main",
		"cn.kuwo.player",	
		"com.ting.mp3.android",
		"com.changba",
		//"com.nodemusic",
		//"com.ss.android.ugc.aweme",		
		"com.gwsoft.imusic.controller",
		"com.yinyuetai.ui",
		//"com.edog",
		"com.netease.cloudmusic",
		"com.sonyericsson.trackid",
		"com.melodis.midomiMusicIdentifier.freemium",
		"com.android.mediacenter",
		"com.sds.android.ttpod",
		"com.sonyericsson.music",
		"com.gionee.ringtone",
		"com.voicedragon.musicclient",
		//"cn.sspace.tingshuo.android.mobile",
		"com.meile.mobile.scene",
		//"com.codingcaveman.Solo",
		//"com.tbig.playerpro",

		"fm.qingting.qtradio",
		"cn.kuwo.tingshu",
		"com.tencent.radio",
		"com.yibasan.lizhifm",
		"air.GSMobile",
		"com.itings.myradio",
		"com.imusic.iting",
		"com.audio.tingting",
		"com.pianke.client",
		"com.xinli.fm",
		"com.shinyv.cnr",
		"com.ifeng.fhdt",
		"com.douban.radio",
		"org.ajmd",
		
		//e-mail
		"com.tencent.androidqqmail",
		"com.netease.mobimail",
		"com.netease.mail",
		"cn.cj.pe",
		"com.corp21cn.mail189",
		"com.kingsoft.email",
		
		"com.immomo.momo",
		"com.sina.weibo",
		"com.qzone",
		"com.alibaba.mobileim",
		"com.alibaba.android.rimet",
		
		//bank
		"com.eg.android.AlipayGphone",//zhifubao
		"cmb.pb",
		"com.chinamworld.main",
		"com.chinamworld.bocmbci",
		"com.icbc",
		"com.android.bankabc",
		"com.cgbchina.xpt",
		"cn.com.shbank.mper",
		"com.czbank.mbank",
		
		
	};
	/////////////////
	//autolaunch
	
	public static final String[] autolaunch_defenablelist = {
		"com.tencent.mm",
		"com.tencen1.mm",
		"com.tencent.mobileqq",
		"com.baidu.input",
		"com.prize.prizeappoutad",
		"com.prize.appcenter.service",
		"com.android.lpserver",		
		"com.android.launcher3",
		"com.prize.appcenter",
		"com.prize.globaldata",
		
		"com.immomo.momo",
		"cn.com.fetion",
		"com.alibaba.mobileim",
		"com.sina.weibo",
		"com.alibaba.android.rimet",
		
		//e-mail
		"com.asiainfo.android",
	};
	
	/////////////////////////////
	//netforbade
	public static final String[] netforbade_alwaysforbadelist = {
		"com.gangyun.beautysnap",
		"com.vlife.koobee.wallpaper",
		"com.ibimuyu.lockscreen",
	};
	
	////////////////////////////////////////
	//releatewakeup
	public static class ReleateWakeupItem
	{
		//State that indicates we disallow to launch/send service/broadcast be launching/sending
	    public static final int STATE_DISALLOW = 0;    
	    //State that indicates we allow to launch/send service/broadcast be launching/sending
	    public static final int STATE_ALLOW = 1;
	    //State that indicates we allow to launch/send service/broadcast be launching/sending if its process has already running.    
	    public static final int STATE_MAY_DISALLOW = 2;
	    //State that indicates we can't find the component to detemine whether this service/broadcast can start/send.
	    public static final int STATE_NOT_FOUND_COMPONENT = 3;
	    public static final int STATE_MATCH_START = 10;
           //state that match class head and may disallow
           //for example,com.tencent.android.tpush.service match com.tencent.android.tpush.servicexxx
           public static final int STATE_MATCH_CLASS_MAY_DISALLOW = STATE_MATCH_START+STATE_MAY_DISALLOW;
           //state that match class head and  disallow
           //for example,com.tencent.android.tpush.service match com.tencent.android.tpush.servicexxx
           public static final int STATE_MATCH_CLASS_DISALLOW = STATE_MATCH_START+STATE_DISALLOW;
           
		public String targetpkg;
		public String action;
		public String classname;
		public String callerpkg;
		public int    state;
		public ReleateWakeupItem(String _targetpkg,String _classname,String _action,String _callpkg,int _state)
		{
			targetpkg = _targetpkg;			
			action = _action;
			classname = _classname;
			callerpkg = _callpkg;
			state = _state;
		}
	}
	public static final ReleateWakeupItem[] releatewakeup_deflist ={
		new ReleateWakeupItem(null,"com.baidu.sapi2.share.ShareService","baidu.intent.action.account.SHARE_SERVICE",null,ReleateWakeupItem.STATE_DISALLOW),
		new ReleateWakeupItem(null,"com.ali.money.shield.service.TransmitService","com.ali.money.shield.service.action.init",null,ReleateWakeupItem.STATE_DISALLOW),
		new ReleateWakeupItem(null,"com.alipay.pushsdk.push.AppInfoRecvIntentService","org.rome.sdk.IPP_CALL",null,ReleateWakeupItem.STATE_DISALLOW),
		new ReleateWakeupItem(null,"com.amap.api.service.AMapService",null,null,ReleateWakeupItem.STATE_DISALLOW),
		new ReleateWakeupItem(null,"com.igexin.sdk.PushService",null,null,ReleateWakeupItem.STATE_DISALLOW),
		new ReleateWakeupItem(null,"com.taobao.accs.ChannelService","org.agoo.android.intent.action.PING_V4",null,ReleateWakeupItem.STATE_DISALLOW),
		new ReleateWakeupItem(null,"com.taobao.accs.ChannelService","com.taobao.accs.intent.action.SERVICE",null,ReleateWakeupItem.STATE_DISALLOW),
		new ReleateWakeupItem(null,"com.taobao.agoo.PushService",null,null,ReleateWakeupItem.STATE_DISALLOW),
		new ReleateWakeupItem(null,"com.taobao.agoo.PushService","org.agoo.android.intent.action.PING_V4",null,ReleateWakeupItem.STATE_DISALLOW),
		new ReleateWakeupItem(null,"com.taobao.agoo.TaobaoMessageIntentReceiverService",null,null,ReleateWakeupItem.STATE_DISALLOW),
		new ReleateWakeupItem(null,"com.taobao.android.sso.internal.PidGetterService",null,null,ReleateWakeupItem.STATE_DISALLOW),
		new ReleateWakeupItem(null,"com.tmall.wireless.TaobaoIntentService","org.agoo.android.intent.action.RECEIVE",null,ReleateWakeupItem.STATE_DISALLOW),
		new ReleateWakeupItem(null,"com.uc.base.push.PushFriendBridge","com.UCMobile.intent.action.FRIEND",null,ReleateWakeupItem.STATE_DISALLOW),
		new ReleateWakeupItem(null,"com.youku.pushsdk.service.PushService", "com.youku.pushsdk.pushservice.FRIEND",null,ReleateWakeupItem.STATE_DISALLOW),
		new ReleateWakeupItem(null,"com.umeng.message.UmengMessageIntentReceiverService",null,null,ReleateWakeupItem.STATE_DISALLOW),
		new ReleateWakeupItem(null,"org.rome.android.ipp.binder.IppService",null,null,ReleateWakeupItem.STATE_DISALLOW),
		new ReleateWakeupItem(null,"com.UCMobile.TaobaoIntentService", "org.agoo.android.intent.action.RECEIVE",null,ReleateWakeupItem.STATE_DISALLOW),
		new ReleateWakeupItem(null,"com.ucweb.message.UcwebMessageIntentReceiverService", "org.android.agoo.client.MessageReceiverService",null,ReleateWakeupItem.STATE_DISALLOW),
		new ReleateWakeupItem(null,"qrom.component.push.core.TCMService",null,null,ReleateWakeupItem.STATE_DISALLOW),
		new ReleateWakeupItem(null,"com.tencent.qqmail.utilities.qmnetwork.service.QMWakeUpService",null,null,ReleateWakeupItem.STATE_DISALLOW),
		new ReleateWakeupItem(null,"com.tencent.gallerymanager.service.PhotoGalleryService", "com.tencent.gallerymanager.service.PhotoGalleryService",null,ReleateWakeupItem.STATE_DISALLOW),
		new ReleateWakeupItem(null,"com.tencent.mtt.sdk.BrowserSdkService", "com.tencent.mtt.ACTION_ACTIVE_PUSH",null,ReleateWakeupItem.STATE_DISALLOW),
		new ReleateWakeupItem(null,"com.tencent.mtt.sdk.BrowserSdkService", "com.tencent.mtt.ACTION_INSTALL_X5",null,ReleateWakeupItem.STATE_DISALLOW),
		
		new ReleateWakeupItem(null,"com.tencent.qqpim.service.share.QQPimShareService",null,null,ReleateWakeupItem.STATE_MAY_DISALLOW),		
		new ReleateWakeupItem(null,"com.tencent.news.account.SyncService", "android.content.SyncAdapter",null,ReleateWakeupItem.STATE_MAY_DISALLOW),		
		new ReleateWakeupItem(null,"com.qihoo.appstore.zhushouhelper.ZhushouHelperService", "com.qihoo.appstore.zhushouhelper.IZhushouHelperService",null,ReleateWakeupItem.STATE_MAY_DISALLOW),
		
		new ReleateWakeupItem(null,null, "com.qihoo.appstore.QihooAlliance",null,ReleateWakeupItem.STATE_DISALLOW),
		
		new ReleateWakeupItem(null,"com.qihoo.core.CoreService", "com.qihoo.appstore.ACTION_DAEMON_CORE_SERVICE",null,ReleateWakeupItem.STATE_MAY_DISALLOW),
		
		new ReleateWakeupItem(null,"com.qihoo.express.mini.service.DaemonCoreService", "android.intent.action.VIEW",null,ReleateWakeupItem.STATE_DISALLOW),
		new ReleateWakeupItem(null,null, "com.qihoo.browser.QihooAlliance",null,ReleateWakeupItem.STATE_DISALLOW),
		new ReleateWakeupItem(null,"com.qihoo360.accounts.sso.svc.AccountService", "com.qihoo360.accounts.action.START_SERVICE",null,ReleateWakeupItem.STATE_DISALLOW),
		new ReleateWakeupItem(null,null, "com.qihoo.gameunion.QihooAlliance",null,ReleateWakeupItem.STATE_DISALLOW),
		new ReleateWakeupItem(null,null, "com.qihoo.video.QihooAlliance",null,ReleateWakeupItem.STATE_DISALLOW),
		new ReleateWakeupItem(null,null, "com.qihoo360.mobilesafe.QihooAlliance",null,ReleateWakeupItem.STATE_DISALLOW),
		new ReleateWakeupItem(null,null, "com.qihoo.cleandroid_cn.QihooAlliance",null,ReleateWakeupItem.STATE_DISALLOW),
		new ReleateWakeupItem(null,"com.xiaomi.mipush.sdk.PushMessageHandler",null,null,ReleateWakeupItem.STATE_DISALLOW),
		new ReleateWakeupItem(null,"com.qihoo.browser.pushmanager.PushBrowserService",null,null,ReleateWakeupItem.STATE_DISALLOW),
		new ReleateWakeupItem(null,"com.letv.android.client.push.LetvPushService",null,null,ReleateWakeupItem.STATE_DISALLOW),
		new ReleateWakeupItem(null,"com.tencent.assistant.sdk.SDKSupportService",null,null,ReleateWakeupItem.STATE_MAY_DISALLOW),
		/*-prize-add by lihuangyuan,for jiguang push block-2018-03-14-start*/
		//jiguang push block
		//new ReleateWakeupItem(null,"cn.jpush.android.service.PushService",null,null,ReleateWakeupItem.STATE_DISALLOW),
		//new ReleateWakeupItem(null,"cn.jpush.android.service.DaemonService",null,null,ReleateWakeupItem.STATE_DISALLOW),
		//new ReleateWakeupItem(null,"cn.jpush.android.service.DownloadService",null,null,ReleateWakeupItem.STATE_DISALLOW),
		new ReleateWakeupItem(null,"cn.jpush.android.service",null,null,ReleateWakeupItem.STATE_MATCH_CLASS_DISALLOW),
		/*-prize-add by lihuangyuan,for jiguang push block-2018-03-14-end*/
                     

            //tencent push
            //new ReleateWakeupItem(null,"com.tencent.android.tpush.service.XGPushService",null,null,ReleateWakeupItem.STATE_MAY_DISALLOW),
            //new ReleateWakeupItem(null,"com.tencent.android.tpush.rpc.XGRemoteService",null,null,ReleateWakeupItem.STATE_MAY_DISALLOW),
            //add by lihuangyuan,2018-05-11
            //new ReleateWakeupItem(null,"com.tencent.android.tpush.service.XGPushServiceV3",null,null,ReleateWakeupItem.STATE_MAY_DISALLOW),
            new ReleateWakeupItem(null,"com.tencent.android.tpush",null,null,ReleateWakeupItem.STATE_MATCH_CLASS_MAY_DISALLOW),

            //add by lihuangyuan,2018-04-10
            //huawei
            new ReleateWakeupItem(null,"com.huawei.android.pushagent.PushService",null,null,ReleateWakeupItem.STATE_DISALLOW),

            //alibaba            
            //new ReleateWakeupItem(null,"com.alibaba.sdk.android.push.PushIntentService",null,null,ReleateWakeupItem.STATE_DISALLOW),
            //new ReleateWakeupItem(null,"com.alibaba.sdk.android.push.CloudPushIntentService",null,null,ReleateWakeupItem.STATE_DISALLOW),
            //new ReleateWakeupItem(null,"com.alibaba.sdk.android.push.AliyunPushIntentService",null,null,ReleateWakeupItem.STATE_DISALLOW),
            //new ReleateWakeupItem(null,"com.alibaba.sdk.android.push.ChannelService",null,null,ReleateWakeupItem.STATE_DISALLOW),
            //new ReleateWakeupItem(null,"com.alibaba.sdk.android.push.MsgService",null,null,ReleateWakeupItem.STATE_DISALLOW),
            new ReleateWakeupItem(null,"com.alibaba.sdk.android.push",null,null,ReleateWakeupItem.STATE_MATCH_CLASS_DISALLOW),
            
            //xiaomi
            //new ReleateWakeupItem(null,"com.xiaomi.push.service.XMJobService",null,null,ReleateWakeupItem.STATE_DISALLOW),
            //new ReleateWakeupItem(null,"com.xiaomi.push.service.XMPushService",null,null,ReleateWakeupItem.STATE_DISALLOW),
            //new ReleateWakeupItem(null,"com.xiaomi.mipush.sdk.MessageHandleService",null,null,ReleateWakeupItem.STATE_DISALLOW),
            new ReleateWakeupItem(null,"com.xiaomi.push.service",null,null,ReleateWakeupItem.STATE_MATCH_CLASS_DISALLOW),
            new ReleateWakeupItem(null,"com.xiaomi.mipush",null,null,ReleateWakeupItem.STATE_MATCH_CLASS_DISALLOW),
            
            //meizu
            new ReleateWakeupItem(null,"com.meizu.cloud.pushsdk.NotificationService",null,null,ReleateWakeupItem.STATE_DISALLOW),
            //baidu
            new ReleateWakeupItem(null,"com.baidu.android.pushservice.CommandService",null,null,ReleateWakeupItem.STATE_DISALLOW),
            new ReleateWakeupItem(null,"com.baidu.android.pushservice.PushService",null,null,ReleateWakeupItem.STATE_DISALLOW),
            //irong
            //new ReleateWakeupItem(null,"io.rong.push.core.PushRegistrationService",null,null,ReleateWakeupItem.STATE_DISALLOW),
            //new ReleateWakeupItem(null,"io.rong.push.PushService",null,null,ReleateWakeupItem.STATE_DISALLOW),
            //new ReleateWakeupItem(null,"io.rong.push.core.MessageHandleService",null,null,ReleateWakeupItem.STATE_DISALLOW),
            new ReleateWakeupItem(null,"io.rong.push",null,null,ReleateWakeupItem.STATE_MATCH_CLASS_DISALLOW),
            
            //sogou
            //new ReleateWakeupItem(null,"com.sogou.udp.push.PushService",null,null,ReleateWakeupItem.STATE_DISALLOW),
            //new ReleateWakeupItem(null,"com.sogou.udp.push.SGPushMessageService",null,null,ReleateWakeupItem.STATE_DISALLOW),
            new ReleateWakeupItem(null,"com.sogou.udp.push",null,null,ReleateWakeupItem.STATE_MATCH_CLASS_DISALLOW),
            
            //ss
            //new ReleateWakeupItem(null,"com.ss.android.push.daemon.PushService",null,null,ReleateWakeupItem.STATE_DISALLOW),
            //new ReleateWakeupItem(null,"com.ss.android.push.DefaultService",null,null,ReleateWakeupItem.STATE_DISALLOW),
            new ReleateWakeupItem(null,"com.ss.android.message.PushJobService",null,null,ReleateWakeupItem.STATE_DISALLOW),
            new ReleateWakeupItem(null,"com.ss.android.push",null,null,ReleateWakeupItem.STATE_MATCH_CLASS_DISALLOW),

            //vivo
            new ReleateWakeupItem(null,"com.vivo.push.sdk.service.CommandService",null,null,ReleateWakeupItem.STATE_DISALLOW),
            
            new ReleateWakeupItem(null,"com.ixigua.feature.fantasy.feature.push.LocalPushService",null,null,ReleateWakeupItem.STATE_DISALLOW),            
            new ReleateWakeupItem(null,"com.igexin.sdk.PushService",null,null,ReleateWakeupItem.STATE_DISALLOW),
            new ReleateWakeupItem(null,"com.igexin.sdk.PushServiceUser",null,null,ReleateWakeupItem.STATE_DISALLOW),
            new ReleateWakeupItem(null,"com.coloros.mcssdk.PushService",null,null,ReleateWakeupItem.STATE_DISALLOW),             
            //end

            //match class
            //new ReleateWakeupItem(null,"com.example.test",null,null,ReleateWakeupItem.STATE_MATCH_CLASS_MAY_DISALLOW),
            //new ReleateWakeupItem(null,"com.example.test",null,null,ReleateWakeupItem.STATE_MATCH_CLASS_DISALLOW),
		
	};
	//////////////////////////////////////////
	//sleepnetwhite
	public static final String[] sleepnetwhite_deflist = {
	       //music
		"music",
		"Music",
		//music
		"com.kugou.android",
		"com.tencent.qqmusic",
		"com.tencent.karaoke",
		"cmccwm.mobilemusic",
		"com.ting.mp3.android",
		"cn.kuwo.player",
		"com.changba",
		"com.gwsoft.imusic.controller",
		"fm.xiami.main",
		"com.yinyuetai.ui",
		"com.netease.cloudmusic",
		"com.sonyericsson.trackid",
		"com.melodis.midomiMusicIdentifier.freemium",
		"com.android.mediacenter",
		"com.sds.android.ttpod",
		"com.sonyericsson.music",
		"com.gionee.ringtone",
		"com.voicedragon.musicclient",
		"com.meile.mobile.scene",	

		"fm.qingting.qtradio",
		"cn.kuwo.tingshu",
		"com.tencent.radio",
		"com.yibasan.lizhifm",
		"air.GSMobile",
		"com.itings.myradio",
		"com.imusic.iting",
		"com.audio.tingting",
		"com.pianke.client",
		"com.xinli.fm",
		"com.shinyv.cnr",
		"com.ifeng.fhdt",
		"com.douban.radio",
		"org.ajmd",		
		"com.duomi.android",
		"com.ximalaya.ting.android",
		
		"navi",
		"map",
		"Map",
		"com.tencent.mobileqq",
		"com.tencent.mm",
		"cn.com.fetion",
		"com.tianqiwhite",
		"com.sdu.didi.psnger",
		"bubei.tingshu",
	};
	///////////////////////////////////////
	//blockactivity
	public static final String[] blockactivity_deflist = {
		"com.tencent.mobileqq.activity.QQLSActivity",
		"com.tencent.news.push.alive.offactivity.OffActivity",
		"com.qihoo.browser.pushmanager.LockScreenTipsActivity",
		"com.tencent.news.push.alive.offactivity.HollowActivity",
		"com.ss.android.message.sswo.SswoActivity",
		"com.myzaker.ZAKER_Phone.view.push.weakup.DaemonActivity",
		"com.qihoo.util.CommonActivity",
		"com.tencent.mtt.base.notification.LockScreenTipsActivity",// QQBrowser push activity. see bug-33304
		"cn.etouch.ecalendar.remind.AlarmRemindActivity",//zhonghua wannianli
		"com.uc.base.push.dex.lockscreen.PushLockScreenActivity",////UC brower
		"com.vlocker.settings.DismissActivity", //add by xiarui ,for bug-46750
		"com.alibaba.sdk.android.push.keeplive.PushExtActivity", //add by xiarui, for bug-52306
		"com.lightsky.video.push.PushNotificationMessageActivity", //for bug-53725
		
		"com.ss.android.lockscreen.activity.lock.LockScreenActivity",
 		"com.lightsky.video.lockscreen.LockScreenActivity",
 		"com.coohua.xinwenzhuan.wakeup.SinglePixelActivity",
 		"cn.etouch.ecalendar.remind.DailyRemindActivity",
 		"com.sohu.push.alive.one_pixel.OnePixelActivity",
 		"com.cleanmaster.daemon.onePxForLive.OnepxActivity",
 		"com.qihoo.video.pullalive.PushActivity",
 		"com.ss.android.lockscreen_wrapper.NoViewLockScreenActivity",
 		"com.tencent.server.task.plugin.KeyguardNotifyActivity",
 		// 360 appstore chargingscreen
 		"com.qihoo.appstore.lockscreen.ChargeScreenProxyActivity",
 		"com.qihoo.appstore.loader.a.ActivityN1SINTS0",
 		"com.tencent.reading.push.alive.offactivity.HollowActivity",
	};
	///////////////////////////////////
	//msgwhite
	public static final String[] msgwhite_deflist = {
		"com.john.whitesendmessage",
		"com.android.mms",
        "com.prize.appcenter",
        "com.android.prize",
        "com.android.soundrecorder",
        "com.prize.prizethemecenter",
        "com.prize.music",
        "com.baidu.duer.phone",//add by yangming,for xiaoku send mms
		"com.mediatek.deviceregister",
 		"com.mediatek.selfregister",
	};
	/////////////////////////////////
	//installwhite
	public static final String[] installwhite_deflist = {
		"com.android.packageinstaller",
        "com.android.launcher3",
        "com.prize.prizethemecenter",
        "com.prize.prizeappoutad",
        "com.prize.appcenter",
        "android.uid.system:1000",
        "com.prize.gamecenter",
	};

	//dozewhite
	public static final String[] dozewhite_deflist = {
	     "com.tencent.mm",
            "com.tencent.mobileqq",       
            "com.prize.appcenter",    
            "com.prize.cloudlist",
	};
    /////////////////////////////////////////////////////
    //releatewakeup
    public static class ProviderWakeupItem
    {
        // State that indicates start request(activity/broadcast/service/provider) can be granted.
        public static final int STATE_GRANTED = 0;
        // State that indicates start request(activity/broadcast/service/provider) should be denied.
        public static final int STATE_DENIED = 1; 
        // State that indicates if the host process has already been running, then start request can 
        // be granted. If not, start request will be denied.
        public static final int STATE_DENIED_IF_NOT_RUNNING = 2;
        
        public static final int STATE_MATCH_CLASS_START = 10;
        //state that match class head and defined
        public static final int STATE_MATCH_CLASS_DENIED = STATE_MATCH_CLASS_START+STATE_DENIED;
        //state that match class head and defined if not running
        public static final int STATE_MATCH_CLASS_DENIED_IF_NOT_RUNNING = STATE_DENIED_IF_NOT_RUNNING+STATE_DENIED;

        public String targetpkg;
        public String classname;
        public String callerpkg;
        public int    state;
        public ProviderWakeupItem(String _targetpkg,String _classname,String _callpkg,int _state)
        {
            targetpkg = _targetpkg;
            classname = _classname;
            callerpkg = _callpkg;
            state = _state;
        }
    }
    public static final ProviderWakeupItem[] providerwakeup_deflist =
    {
        //tencent
        //new ProviderWakeupItem(null,"com.tencent.android.tpush.XGPushProvider",null,ProviderWakeupItem.STATE_DENIED_IF_NOT_RUNNING),
        new ProviderWakeupItem(null,"com.tencent.mid.api.MidProvider",null,ProviderWakeupItem.STATE_DENIED_IF_NOT_RUNNING),
        //add by lihuangyuan 2018-06-11
        //new ProviderWakeupItem(null,"com.tencent.android.tpush.SettingsContentProvider",null,ProviderWakeupItem.STATE_DENIED_IF_NOT_RUNNING),
        new ProviderWakeupItem(null,"com.tencent.android.tpush",null,ProviderWakeupItem.STATE_MATCH_CLASS_DENIED_IF_NOT_RUNNING),
        
        //add by lihuangyuan,2018-04-10
        new ProviderWakeupItem(null,"com.ss.android.pushmanager.setting.PushMultiProcessSharedProvider",null,ProviderWakeupItem.STATE_DENIED_IF_NOT_RUNNING),
        new ProviderWakeupItem(null,"com.baidu.android.pushservice.PushInfoProvider",null,ProviderWakeupItem.STATE_DENIED_IF_NOT_RUNNING),
        //jiguang push
        //new ProviderWakeupItem(null,"cn.jpush.android.service.DownloadProvider",null,ProviderWakeupItem.STATE_DENIED_IF_NOT_RUNNING),
        //new ProviderWakeupItem(null,"cn.jpush.android.service.DataProvider",null,ProviderWakeupItem.STATE_DENIED_IF_NOT_RUNNING),
        new ProviderWakeupItem(null,"cn.jpush.android.service",null,ProviderWakeupItem.STATE_MATCH_CLASS_DENIED_IF_NOT_RUNNING),
        
        new ProviderWakeupItem(null, "com.ss.android.common.util.MultiProcessSharedProvider",null,ProviderWakeupItem.STATE_DENIED_IF_NOT_RUNNING),
        new ProviderWakeupItem(null,"com.huawei.hms.update.provider.UpdateProvider",null,ProviderWakeupItem.STATE_DENIED_IF_NOT_RUNNING),
        new ProviderWakeupItem(null,"com.igexin.download.DownloadProvider",null,ProviderWakeupItem.STATE_DENIED_IF_NOT_RUNNING),
        new ProviderWakeupItem(null,"com.umeng.message.provider.MessageProvider",null,ProviderWakeupItem.STATE_DENIED_IF_NOT_RUNNING),
        new ProviderWakeupItem(null,"com.tencent.bugly.beta.utils.BuglyFileProvider",null,ProviderWakeupItem.STATE_DENIED_IF_NOT_RUNNING),
        //end
    };
}
