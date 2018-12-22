/**
 * @author yangzh6
 *
 * Reference: <<IFAA 标准: REE 系统框架部分>>
 * Description: IFAAManager为抽象类，需要放到系统framework中，通过IFAAManager生成实例。
 */

package org.ifaa.android.manager;

import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.util.Log;
public abstract class IFAAManager {
    static
    {
        System.loadLibrary("ifaa_jni");
    }
	String TAG = "steven----------------";
	/** Internal Functions */
	private boolean isFpSupported(Context context) {
		if (null != context) {
			FingerprintManager fpManager = (FingerprintManager) context
					.getSystemService(Context.FINGERPRINT_SERVICE);
			if (null != fpManager) {
				return fpManager.isHardwareDetected();
			}
		}
		return false;
	}

	private boolean isIrisSupported(Context context) {
		if (null != context) {
			/**
			 * TODO: ...
			 */
			return false;
		}
		return false;
	}

	// APIs
	// ------------------------------------------------------------------------
	/**
	 * Description: 返回系统手机型号， model字段用于标记手机的型号，主要是指的一个系列。例如一个厂商的各个MODLE，
	 * 例如三星的S6有可能叫SAMSUNG-SM9200， SAMSUNG-SM9201等等，其实都是指的一个系列，
	 * 但是他们都是使用的同一个方案，例如TA和芯片和指纹芯片。所以对model的定义就是一个系列，他们对应了一个内置的厂商TA私钥。
	 */
	public String getDeviceModel() {
		
		String model_name = Build.MODEL;
		String realModel="";
		
		if(model_name.indexOf("Plus")==-1){
			realModel = model_name.replace(' ','-');
		}
		else{
			realModel = model_name.replaceFirst(" ", "-").replaceAll(" ", "");
		}
		Log.w(TAG,"test: " + realModel);
		
		return realModel;
	}

	/**
	 * * Description: 返回Manager接口版本，目前为1。
	 */
	public int getVersion() {
		return 1;
	}

	/**
	 * Description: 返回手机上支持的校验方式，
	 * 目前IFAF协议1.0版本指纹为0x01、虹膜为0x02，验证类型可以为多种不同方式组合，用‘或’操作符拼接。
	 */
	public int getSupportBIOTypes(Context context) {
		int types = 0;
		if (isFpSupported(context)) {
			types |= IFAAUtils.BIO_TYPE_FINGERPRINT;
		}
		if (isIrisSupported(context)) {
			types |= IFAAUtils.BIO_TYPE_IRIS;
		}
		return types;
	}

	/**
	 * Description: 启动系统的指纹管理应用界面，让用户进行指纹录入。
	 * 指纹录入是在系统的指纹管理应用中实现的，本函数的作用只是将指纹管理应用运行起来，直接进行页面跳转，方便用户录入。
	 *
	 * @param context
	 *            上下文环境
	 * @param authType
	 *            生物特征识别类型，指纹为1，虹膜为2
	 * @return COMMAND_OK(0): 成功启动指纹管理应用； COMMAND_FAIL(-1): 其他值，启动指纹管理应用失败。
	 */
	public int startBIOManager(Context context, int authType) {
		return IFAAUtils.COMMAND_FAIL;
	}

	/**
	 * Description: 通过ifaateeclient的so文件实现REE到TAA的通道。
	 *
	 * @param context
	 * @param param
	 *            用于传输到IFAA TA的数据buffer。
	 * @return 返回IFAA TA返回REE的数据buffer。
	 */
	public native byte[] processCmd(Context context, byte[] param);

}
