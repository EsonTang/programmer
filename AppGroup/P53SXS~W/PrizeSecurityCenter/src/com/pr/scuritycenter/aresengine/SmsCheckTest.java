/**
 * 
 */
package com.pr.scuritycenter.aresengine;

import tmsdk.bg.module.aresengine.AresEngineManager;
import tmsdk.bg.module.aresengine.IntelliSmsChecker;
import tmsdk.common.module.aresengine.IntelliSmsCheckResult;
import tmsdk.common.module.aresengine.SmsEntity;
import android.util.Log;

public class SmsCheckTest {
	
	private static final String[] ARRAY_SMS = {
		"1", "13990379659", "神驹踏上平安道，天上人间皆欢笑。爆竹声里热情高，合家团圆乐淘陶。招财进宝福星照，万事如意生活好。只愿马年无烦恼，幸福快乐将你抱。张红祝你春节快乐",
		"2", "13990379659", "老大，给你拜个早年！祝你201 4说神马就是神马。红包什么的，别给太多，意思下就行了哈。http://m.alipay.com/J/iJ86Y9Mg",
		"3", "10690133051611", "3月6日10点您预约的Xplay3S全网首发，首批购买可享购机基金及惊喜，您的预定码是TDTB123987【vivo】",
		"4", "10690133051611","陈兴强这是之前的聚会照弄好了，大家的精彩瞬间。手机直接点击地址x安装激活查看is.gd/baidu2c"
	};
	
	public static void testSms(AresEngineManager nAresEnginManager) {
		SmsEntity sms = new SmsEntity();
		Log.i("demo", "智能拦截结果: ");
		IntelliSmsChecker smschecker = nAresEnginManager.getIntelligentSmsChecker();
		for(int i = 0; i < ARRAY_SMS.length; i+=3) {
			String id = ARRAY_SMS[i];
			sms.name = "";
			sms.phonenum = ARRAY_SMS[i+1];
			sms.body = ARRAY_SMS[i+2];
			
			// 智能拦截调用接口
			IntelliSmsCheckResult checkresult = smschecker.check(sms);
			Log.i("demo", id+": " + getContentType(checkresult.contentType()) + " , " + getSuggestions(checkresult.suggestion));
		}
	}
	
	private static final String[] ARRAY_ContentType1 = {//0-10
		"/** 未初始化 */",
		"/** 未知类型 */",
		"/** 正常类型 */",
		"/** 广告类型 */",
		"/** 诈骗类型 */",
		"/** 12590付费电话 */",
		"/** 色情类型 */",
		"/** 合法机构类型，如运营商、银行的短信等 */",
		"/** MO扣费类型 */",
		"/** MT扣费类型 */",
		"/** 恶意软件 */"
	};
	private static final String[] ARRAY_ContentType2 = {//40-49
		"/** 电话广告 */",
		"/** 电话诈骗 */",
		"/** 银行电话 */",
		"/** 信用卡电话推销 */",
		"/** 保险 */",
		"/** 房地产 */",
		"/** 培训电话 */",
		"/** 中小企业会议邀请电话 */",
		"/** 网络电话 */",
		"/** 联通的隐藏号码增值服务 */"
	};
	
	private static String getContentType(int pos) {
		if(0 <= pos && pos <= 10) {
			return ARRAY_ContentType1[pos];
		} else if(40 <= pos && pos <= 49 ) {
			return ARRAY_ContentType2[pos];
		}
		return "/** 其它 */";
	}
	
	private static final String[] ARRAY_Suggestions = {
		"/** 判断出错 */",
//		public static final int SUGGESTION_ERROR = -1;
		"/** 未知 */",
		"/** 通过 */",
//		public static final int SUGGESTION_PASS = 1;
		"/** 拦截 */",
//		public static final int SUGGESTION_INTERCEPT = 2;
		"/** 可疑 */",
//		public static final int SUGGESTION_DOUBT = 3;
		"/** 未知 */"
//		public static final int SUGGESTION_UNKNOWN = 4;
	};
	
	private static String getSuggestions(int pos) {
		pos++;
		if(0 <= pos && pos <= 5) {
			return ARRAY_Suggestions[pos];
		} 
		return "/** 其它 */";
	}	
}
