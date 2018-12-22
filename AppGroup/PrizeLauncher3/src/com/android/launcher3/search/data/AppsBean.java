package com.android.launcher3.search.data;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ResolveInfo;

public class AppsBean {

	private ComponentName componentName;

	private Intent intent;

	public AppsBean(ResolveInfo info) {
		super();
		final String packageName = info.activityInfo.applicationInfo.packageName;

		this.componentName = new ComponentName(packageName,
				info.activityInfo.name);
		setActivity(componentName, Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
	}

	final void setActivity(ComponentName className, int launchFlags) {
		intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		intent.setComponent(className);
		intent.setFlags(launchFlags);
	}

	public Intent getIntent() {
		return intent;
	}

	@Override
	public String toString() {
		return "AppsBean [componentName=" + componentName + ", intent="
				+ intent + ", getClass()=" + getClass() + ", hashCode()="
				+ hashCode() + ", toString()=" + super.toString() + "]";
	}

}
