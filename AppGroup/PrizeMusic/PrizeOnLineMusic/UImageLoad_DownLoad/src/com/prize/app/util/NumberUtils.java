package com.prize.app.util;

import java.text.DecimalFormat;

public class NumberUtils {
	public static final long defLongValue = 0;
	public static final int defIntegerValue = 0;
	public static final short defShortValue = 0;
	public static final byte defByteValue = 0;

	/**
	 * 取Long 值
	 * 
	 * @param netValue
	 * @return
	 */
	public static long getLongValue(Long netValue) {
		return netValue == null ? defLongValue : netValue;
	}

	public static long getLongValue(Long netValue, long defValue) {
		return netValue == null ? defValue : netValue;
	}

	public static long getLongValue(String netValue) {
		try {
			return Long.valueOf(netValue);
		} catch (Exception e) {
			return 0;
		}
	}

	/**
	 * 去 Integer 值
	 * 
	 * @param netValue
	 * @return
	 */
	public static int getIntegerValue(Integer netValue) {
		return netValue == null ? defIntegerValue : netValue;
	}

	public static int getIntegerValue(Integer netValue, int defValue) {
		return netValue == null ? defValue : netValue;
	}

	/**
	 * 取Short 值
	 * 
	 * @param netValue
	 * @return
	 */
	public static short getShortrValue(Short netValue) {
		return netValue == null ? defShortValue : netValue;
	}

	public static short getShortrValue(Short netValue, short defValue) {
		return netValue == null ? defValue : netValue;
	}

	/**
	 * 取Byte 值
	 * 
	 * @param netValue
	 * @return
	 */
	public static byte getByteValue(Byte netValue) {
		return netValue == null ? defByteValue : netValue;
	}

	public static byte getByteValue(Byte netValue, byte defValue) {
		return netValue == null ? defValue : netValue;
	}

	/**
	 * 转换一个以分计数的值为0.00格式的字符串
	 * 
	 * @param fenValue
	 * @return
	 */
	public static String convertFenToYuan(int fenValue) {
		String yuan = fenValue / 100 + "." + (fenValue / 10) % 10
				+ (fenValue % 10);
		return yuan;
	}

	/**
	 * 0~1000 显示为 1千+ <br/>
	 * 1001～9999 显示为 N千+；N为千位实际数值<br/>
	 * 10000～ 显示为下载N万+；N为万位实际数值<br/>
	 * 
	 * 
	 * @param gameDownloadCount
	 * @return
	 */
	public static String getGameDownloadNick(String downloadCount) {
		long gameDownloadCount = 0;
		try {
			gameDownloadCount = Long.valueOf(downloadCount);
		} catch (Exception e) {
		}
		if (gameDownloadCount <= 1000) {
			return "1千+";
		}
		if (gameDownloadCount < 10000) {
			return (gameDownloadCount / 1000) + "千+";
		} else {
			return (gameDownloadCount / 10000) + "万+";
		}
	}

	/**
	 * 转化 Kb 到 Mb
	 * 
	 * @param size
	 * @return
	 */
	public static String convertKbToMb(String size) {
		String result = null;
		try {
			float f = Float.valueOf(size) / 1024;
			DecimalFormat fnum = new DecimalFormat("##0.00");
			StringBuilder sb = new StringBuilder();
			sb.append(fnum.format(f));
			sb.append("M");
			result = sb.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
}
