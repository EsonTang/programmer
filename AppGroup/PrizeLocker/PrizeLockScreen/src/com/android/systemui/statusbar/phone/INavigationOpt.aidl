package com.android.systemui.statusbar.phone;
/***
 * 开启或隐藏虚拟导航栏, 获取熌光灯是否开启变量
 *
 */
interface INavigationOpt {
    // 隐藏虚拟导航栏按键不可用
	boolean hideNavigation();
	// 展示虚拟导航栏
	boolean showNavigation();
	// 显示虚拟导航栏并使按键可用
	boolean showNavigationAll();
	// 虚拟导航栏存在性
	int getSysPropByKey(String key);
}