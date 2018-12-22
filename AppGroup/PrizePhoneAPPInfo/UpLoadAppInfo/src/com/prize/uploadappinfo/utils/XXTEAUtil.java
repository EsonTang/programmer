package com.prize.uploadappinfo.utils;

import java.net.URLDecoder;
import java.net.URLEncoder;

/**
 **
 * 用XXTEA加密解密的工具类
 * 
 * @author longbaoxiu
 * @version V1.0
 */
public class XXTEAUtil {
	private final static String KEY = "sdfsdfi23eswrfj5d521dsf@@#!!%$@.1";
	private final static String DEFAULT_PARAMS_ENCODING = "UTF-8";

	/**
	 * @Description:[加密]
	 * @param params
	 *            需要加密的参数
	 * @return 加密过的参数
	 */
	public static String getParamsEncypt(String params) {
		String paramsEncrypt = null;
		try {
			String paramsEncode = URLEncoder.encode(params,
					DEFAULT_PARAMS_ENCODING);

			int[] paramsInt = XXTEA.toIntArray(paramsEncode.getBytes(), true);
			int[] keyInt = XXTEA.toIntArray(KEY.getBytes(), false);
			int[] paramsEncryptInt = XXTEA.encrypt(paramsInt, keyInt);

			byte[] paramsEncryptByte = XXTEA.toByteArray(paramsEncryptInt,
					false);
			paramsEncrypt = new String(Base64.encode(paramsEncryptByte));

			paramsEncrypt = URLEncoder.encode(paramsEncrypt,
					DEFAULT_PARAMS_ENCODING);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return paramsEncrypt;
	}

	/**
	 * @Description:[加密,两个加密都可以用]
	 * @param params
	 *            需要加密的参数
	 * @return 加密过的参数
	 */
	public static String getParamsEncypt2(String params) {
		String paramsEncrypt = null;
		try {
			String paramsEncode = URLEncoder.encode(params,
					DEFAULT_PARAMS_ENCODING);
			byte[] paramsByte = paramsEncode.getBytes();
			byte[] keyByte = KEY.getBytes();

			byte[] paramsEncryptByte = XXTEA.encrypt(paramsByte, keyByte);
			paramsEncrypt = new String(Base64.encode(paramsEncryptByte));

			paramsEncrypt = URLEncoder.encode(paramsEncrypt,
					DEFAULT_PARAMS_ENCODING);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return paramsEncrypt;
	}

	/**
	 * @Description:[解密]
	 * @param response
	 *            需要解密的参数
	 * @return 解密过的参数
	 */
	public static String getResponseDecrypt(String response) {
		String responseDecrypt = null;
		try {
			String responseDecode = URLDecoder.decode(response,
					DEFAULT_PARAMS_ENCODING);
			byte[] responseByte = responseDecode.getBytes();
			byte[] keyByte = KEY.getBytes();

			byte[] responseDecryptByte = XXTEA.decrypt(
					Base64.decode(responseByte), keyByte);

			responseDecrypt = new String(responseDecryptByte);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return responseDecrypt;
	}
}