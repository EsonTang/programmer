package com.prize.app.net.datasource.base;

import java.io.Serializable;

public class PushTimeBean {
	/**手机预装应用*/
	public String shieldPackages;
	/**手机更新白名单 add 2.5 longbaoxiu*/
	public String prealoads;
	public Settings settings;
	public static class Settings implements Serializable {
		/**push出现频率，小时为单位*/
		public int pushFrequency;
		/**请求push频率，小时为单位*/
		public int pushRequestFrequency;
		/**是否开启Push 开关*/
		public boolean pushSwitch;
		/**是否开启垃圾清理Push 开关*/
		public boolean garbageSwitch;
		/**是否是在Push时间段之内 开关*/
		public boolean validPushTime;
		/**垃圾清理检测时间间隔*/
		public int garbageCleanTime;
		/**垃圾大小超过该size弹push*/
		public int garbageCleanSize;
		/**内存占用超过该百分比弹push*/
		public float storageOcuppySize;

		@Override
		public String toString() {
			return "Settings{" +
					"pushFrequency=" + pushFrequency +
					", pushRequestFrequency=" + pushRequestFrequency +
					", pushSwitch=" + pushSwitch +
					", garbageSwitch=" + garbageSwitch +
					", validPushTime=" + validPushTime +
					", garbageCleanTime=" + garbageCleanTime +
					", garbageCleanSize=" + garbageCleanSize +
					", storageOcuppySize=" + storageOcuppySize +
					'}';
		}
	}
	
	@Override
	public String toString() {
		return "PushTimeBean [settings=" + settings + "]";
	}	
}
