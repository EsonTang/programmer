package android.os;
import android.os.WakeupItem;

interface IWhiteListChangeListener
{
	void onPurebackgroundChange(boolean isHideChange,boolean isDefChange,boolean isnotkillChange);
	void onNotificationChange(boolean isHideChange,boolean isDefChange);
	void onFloatDefChange();
	void onAutoLaunchDefChange();
	void onNetForbadeListChange();
	void onWakeupListChange(in WakeupItem[] wakeuplist);
	void onProviderWakeupListChange(in WakeupItem[] wakeuplist);
	void onSleepNetListChange(in String[] sleepnetlist);
	void onBlockActivityListChange(in String[] blocklist);
	void onMsgWhiteListChange();
	void onInstallWhiteListChange();
}
