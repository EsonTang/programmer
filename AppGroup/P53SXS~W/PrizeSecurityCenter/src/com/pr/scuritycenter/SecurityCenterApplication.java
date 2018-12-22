package com.pr.scuritycenter;

import java.util.HashMap;
import java.util.Map;

import tmsdk.common.IDualPhoneInfoFetcher;
import tmsdk.common.ITMSApplicaionConfig;
import tmsdk.common.TMSDKContext;
import android.app.Application;
import android.content.Context;
import android.telephony.TelephonyManager;

import com.android.internal.telephony.PhoneConstants;

public class SecurityCenterApplication extends Application {

	private Context mContext;
	private String sim1imsi;
	private String sim2imsi;
	private TelephonyManager mTelephonyMgr;

	public SecurityCenterApplication() {
		super();
	}

	public SecurityCenterApplication(Context mContext) {
		this.mContext = mContext;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		/***
		 * TMSDKContext.setDualPhoneInfoFetcher()方法为流量校准支持双卡情况时设置，其它情况不需要调用该函数。
		 * 该函数中需要返回第一卡槽和第二卡槽imsi的读取内容。
		 * 
		 * 实现此方法时。一定在TMSDKContext.init前调用
		 */
		mTelephonyMgr = (TelephonyManager) getApplicationContext()
				.getSystemService(Context.TELEPHONY_SERVICE);
		if (mTelephonyMgr != null) {
			sim1imsi = mTelephonyMgr.getSubscriberId(PhoneConstants.SIM_ID_1);
			sim2imsi = mTelephonyMgr.getSubscriberId(PhoneConstants.SIM_ID_2);
		}

		TMSDKContext.setDualPhoneInfoFetcher(new IDualPhoneInfoFetcher() {
			@Override
			public String getIMSI(int simIndex) {
				String imsi = "";
				if (simIndex == IDualPhoneInfoFetcher.FIRST_SIM_INDEX) {
					imsi = (sim1imsi == null) ? null : sim1imsi;
				} else if (simIndex == IDualPhoneInfoFetcher.SECOND_SIM_INDEX) {
					imsi = (sim2imsi == null) ? null : sim2imsi;
				}
				return imsi;
			}
		});

		/**
		 * setAutoConnectionSwitch（）影响渠道号上报 项是否运行。请不要一直设置为false，影响激活量和活跃量统计
		 */
		boolean nFlag = true;
		TMSDKContext.setAutoConnectionSwitch(nFlag);

		// TMSDK初始化
		TMSDKContext.init(this, SecurityCenterService.class,
				new ITMSApplicaionConfig() {

					@Override
					public HashMap<String, String> config(
							Map<String, String> src) {

						HashMap<String, String> ret = new HashMap<String, String>(
								src);

						// 如厂商有自己服务器（如国外）中转需求，需配置自己服务器域名
						// http 服务器
						ret.put(TMSDKContext.CON_HOST_URL,
								"http://pmir.sec.miui.com");
						// tcp 服务器
						ret.put(TMSDKContext.TCP_SERVER_ADDRESS,
								"mazu.sec.miui.com");
						ret.put(TMSDKContext.USE_IP_LIST, "false");
						return ret;
					}
				});

	}

	public String getSim1imsi() {
		return sim1imsi;
	}

	public void setSim1imsi(String sim1imsi) {
		this.sim1imsi = sim1imsi;
	}

	public String getSim2imsi() {
		return sim2imsi;
	}

	public void setSim2imsi(String sim2imsi) {
		this.sim2imsi = sim2imsi;
	}
}
