/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.content.pm;


import java.io.IOException;

class PrizeSplitScreenApk {
    final static String splitApk[]={
         //"com.baidu.video",
         //"com.letv.android.client",
         "com.tencent.qqlive",//0821 -- tencent video
         "com.qiyi.video",//0821 -- aiqiyi  video
         
         //"com.andreader.prein",
         "com.iyd.reader.ReadingJoy",
         //"com.qq.reader",//0821
         
         "com.tencent.news",
         "com.sohu.newsclient",
         "com.netease.newsreader.activity",
//         "com.sina.news",  //2017-9-19 enter split screen crash, so not enter split screen 
         //"com.ifeng.news2",  fenghuaxinwen
         //"com.ss.android.article.news",   jinritoutiao
         //"com.baidu.news",    baidu xinwen
         
    	//com.tencent.mobileqq 692
        "com.tencent.mobileqq",
        "com.tencent.mm",
        //com.tencent.qzone 4
        "com.qzone",
        "com.immomo.momo",
        "com.tencent.WBlog",
        
        "sogou.mobile.explorer",
        "com.ijinshan.browser_fast",
        //"com.UCMobile", //UC browser  2018/5/28 bug 59668  69034 69037
        
        //"com.taobao.taobao",
        "com.mogujie",
        "com.husor.beibei",
        "com.tmall.wireless",
        //"com.jingdong.app.mall",
        "com.sankuai.meituan",
        //"com.nuomi",  dazhongdianping
        
        "com.huajiao",
        //"air.tv.douyu.android",
        "com.panda.videoliveplatform",
        "com.meelive.ingkee",
        "com.duowan.mobile",
        
    };
    
    static boolean apkCanSplitScreen(String packageName){
    	for( String apk:splitApk){
    		if( apk.equals(packageName) ){
    			return true;
    		}
    	}
    	
    	return false;
    }
    
    
    //com.youku.phone   can in split screen, but we don't let it in split.
    final static String notSplitApk[]={
         "com.youku.phone",
         "com.sohu.sohuvideo",
         "com.dianping.v1",
         "com.baidu.video",
         "com.koobeemobile.club",
         "com.UCMobile", //UC browser  2018/5/28 bug 59668  69034 69037
         "com.mt.mtxx.mtxx",
         "com.quark.browser",
        };    
    static boolean apkNotSplitScreen(String packageName){
    	for( String apk:notSplitApk){
    		if( apk.equals(packageName) ){
    			return true;
    		}
    	}
    	return false;
    }

    //system app
    final static String notSplitSysApk[]={
        "com.android.phone",//com.android.phone/.EmergencyDialer
         "com.adups.fota", //upgrade 
         "com.assistant.icontrol", //yao kong jing ling
         "com.prize.inforstream",
        };
    static boolean sysApkNotSplitScreen(String packageName){
    	for( String apk:notSplitSysApk){
    		if( apk.equals(packageName) ){
    			return true;
    		}
    	}
    	return false;
    }

}
