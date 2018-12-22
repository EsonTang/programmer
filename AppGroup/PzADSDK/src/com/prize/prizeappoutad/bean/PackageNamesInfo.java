package com.prize.prizeappoutad.bean;

import java.io.Serializable;
import java.util.ArrayList;

public class PackageNamesInfo implements Serializable {

	private static final long serialVersionUID = 1L;
	// 返回的展示广告的包名
	public ArrayList<String> packageName;

	// 是否需要更新
	public boolean needUpdate;
	// 广告类型,a 开屏,b 插屏,c banner
	public String adtype;
	// 广告来源
	public String adSource;

	// 更新包信息
	public AppSelfUpdateBean app;

}
