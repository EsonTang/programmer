package com.prize.prizethemecenter.ui.utils;

import com.iapppay.sdk.main.IAppPayOrderUtils;

public class PayConfig {
	
	/**
	 * appid		-- 应用appid
	 * privateKey   -- 应用私钥
	 * publicKey	-- 平台公钥(platKey)
	 * notifyurl	-- 商户服务端接收支付结果通知的地址
	 * */

	/**线上环境*/
	public static final String appid = "3009365433";
	public static final String privateKey ="MIICXQIBAAKBgQCypmK3TKCF2VPucI2603d6yjy7t+Hz1F8W1hNdQkm/nH1hfxDdlKWeRz0jirNC+8OZhagrk0i5nXehiRRPcKngGU8oVWwVNPq4BjwykBXnFsAtgyqlINZh0tLZzxy87MrjA4pIOHBjrnB+EkAwh7lb5rCX2MGdW8J7hBHBdfj3kwIDAQABAoGAM1fUWR/BppYuJAHfCcmKthR3Xe/cFWp8paCC0jOh5FrCQgn1u+jjrFiG/rb3DBI1ReL+0P80UHwsDJXL64TSnSUDudlSKpJWezB4lSjpZ1Hf6hkmg5RB9F71NDqgyAS6V3kUtDd7YwjTQMBaFLbaEJL21M3BeiEDLluWpienQxECQQDmheHqcXaS9IDHJFywj7QJNcyXBX85MpomTuwxDuQisOdxfiSCXCR2u3vzkDjyT64NMTLYD04VkzL9lt8VrLPrAkEAxmTf+fosLw0c/OeQC7eBzK18g0NaP+kkwEwM04YVfypbpQTgojMlwedblf9sZEubwjWirIhk9bsqCs8TKpPo+QJBAN6pVuuiqMuNS44O+1C3nUiAJJuC/b37uJVkChPgtE8l3IftNcqMwdPFiBZcqgSzHgU0Dk5PijagtdHoFpbS9/0CQG4YstbVh0mBHHIYyy5FYunD6NIrQV0wM63nRkLoKOvOBNeZ0lMQ0UY+7utZuLwb3U80VpcqheR8PkOLUNaTCHkCQQCmelM27oqgMFe7e4hDAHzryUxt23eZKKN17jdMVyPo64vZtimxAlDktSuYCGfUlu7LHwND55wNVGHCP/zgx2rh" ;
	public static final String publicKey="MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCvDRK6G1zIrMd8T9gfCpLsKYsjxvo8uZnigew2Sz5HkBOQtOG1f2KGaUZDi607qaumbZVYgE28WOek5s7XB0NKwoC18OVHNUE4tHpcLoJfkg+bmUMdsh2IjANfhoHFpSv2PD7s4jqLYUYQE/sGuSl3vQaJc5yXVEYc4OkvTQJ/3wIDAQAB";
	public static final String notifyurl = "http://192.168.0.140:8094/monizhuang/api?type=100";



	/** 获取收银台参数 */
	public static String getTransdata(String appuserid, String cpprivateinfo, int waresid, float price, String cporderid, String mName) {
		//调用 IAppPayOrderUtils getTransdata() 获取支付参数
		IAppPayOrderUtils orderUtils = new IAppPayOrderUtils();
		orderUtils.setAppid(PayConfig.appid);
		orderUtils.setWaresid(waresid);//传入您商户后台创建的商品编号
		orderUtils.setCporderid(cporderid);  //商户生成的订单号， 需要保证系统唯一
		orderUtils.setAppuserid(appuserid);   //	用户在商户应用的唯一标识
		orderUtils.setPrice(price);//单位 元
		orderUtils.setWaresname(mName);//开放价格名称(用户可自定义，如果不传以后台配置为准)
		orderUtils.setCpprivateinfo(cpprivateinfo);
//        orderUtils.setNotifyurl(PayConfig.notifyurl);
		return orderUtils.getTransdata(PayConfig.privateKey);
	}

}
