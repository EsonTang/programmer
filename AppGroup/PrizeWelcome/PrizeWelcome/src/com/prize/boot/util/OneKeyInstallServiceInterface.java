package com.prize.boot.util;

import com.prize.app.net.datasource.base.AppsItemBean;
import com.prize.boot.WelcomeApplication;
import com.prize.boot.util.OneKeyInstallData.DataEntity.AppsEntity;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;

public class OneKeyInstallServiceInterface {

	private static void startDownloadService(AppsEntity game, boolean isBackground) {
		Intent intentManager = new Intent();
		intentManager.setComponent(
				new ComponentName("com.prize.appcenter", "com.prize.appcenter.service.PrizeAppCenterService"));
		intentManager.putExtra("action", 4);
		intentManager.putExtra("optType", 2);
		intentManager.putExtra("isbackground", isBackground);
		Bundle bundle = new Bundle();
		bundle.putParcelable("bean", bean2Bean(game));
		intentManager.putExtras(bundle);
		WelcomeApplication.getInstance().startService(intentManager);
	}

	private static AppsItemBean bean2Bean(AppsEntity from) {
		AppsItemBean to = new AppsItemBean();
		to.appTypeId = from.getAppTypeId();
		to.versionCode = from.getVersionCode();
		to.name = from.getName();
		to.categoryName = from.getCategoryName();
		to.packageName = from.getPackageName();
		to.rating = String.valueOf(from.getRating());
		to.versionName = from.getVersionName();
		to.iconUrl = from.getIconUrl();
		to.apkSize = String.valueOf(from.getApkSize());
		to.apkSizeFormat = from.getApkSizeFormat();
		to.boxLabel = String.valueOf(from.getBoxLabel());
		to.downloadTimes = String.valueOf(from.getDownloadTimes());
		to.downloadUrl = from.getDownloadUrl();
		to.updateTime = from.getUpdateTime();
		to.updateInfo = from.getUpdateInfo();
		to.largeIcon = from.getLargeIcon();
		to.giftCount = from.getGiftCount();
		to.downloadTimesFormat = from.getDownloadTimesFormat();
		to.apkMd5 = from.getApkMd5();
		to.id = String.valueOf(from.getId());
		to.isAd = from.getIsAd();
		to.bannerUrl = String.valueOf(from.getBannerUrl());
		to.brief = from.getBrief();
		to.tag = from.getTag();
		to.subTitle = String.valueOf(from.getSubTitle());
		return to;
	}
	
	public static void startBackgoundDownload(AppsEntity app) {
		startDownloadService(app, false);
	}
}
