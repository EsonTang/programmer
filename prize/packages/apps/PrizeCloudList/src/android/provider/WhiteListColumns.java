package android.provider;

import android.net.Uri;

public class WhiteListColumns {
	public static final String AUTHORITY = "com.android.provider.prizeblacklist";
	public static class BaseColumns
	{
		public static final String _ID = "id";
		public static final String PKGNAME = "pkgname";
		public static final String ENABLE = "enable";
		public static final String ISSERVERCONFIG = "isserver";
	}
	public static class Purebackground extends BaseColumns
	{
		public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/purebackground");
		public static final String TABLE = "purebackground";
	}
	public static class Notification extends BaseColumns
	{
		public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/notification");
		public static final String TABLE = "notification";
	}
	public static class FloatWindow extends BaseColumns
	{
		public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/floatwindow");
		public static final String TABLE = "floatwindow";
	}
	public static class AutoLaunch extends BaseColumns
	{
		public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/autolaunch");
		public static final String TABLE = "autolaunch";
	}
	public static class NetForbade extends BaseColumns
	{
		public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/netforbade");
		public static final String TABLE = "netforbade";
	}
	public static class RelateWakeup extends BaseColumns
	{
		public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/releatewakeup");
		public static final String TABLE = "releatewakeup";
		
		public static final String CLASS = "class";
		public static final String ACTION = "action";
		public static final String CALLERPKG = "caller";	
		
		//State that indicates we disallow to launch/send service/broadcast be launching/sending
	    public static final int STATE_DISALLOW = 0;    
	    //State that indicates we allow to launch/send service/broadcast be launching/sending
	    public static final int STATE_ALLOW = 1;
	    //State that indicates we allow to launch/send service/broadcast be launching/sending if its process has already running.    
	    public static final int STATE_MAY_DISALLOW = 2;
	    //State that indicates we can't find the component to detemine whether this service/broadcast can start/send.
	    public static final int STATE_NOT_FOUND_COMPONENT = 3;
	}
    public static class ProviderWakeup extends BaseColumns
	{
		public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/providerwakeup");
		public static final String TABLE = "providerwakeup";
		
		public static final String CLASS = "class";
		public static final String CALLERPKG = "caller";	
		
		// State that indicates start request(activity/broadcast/service/provider) can be granted.
        public static final int STATE_GRANTED = 0;
        // State that indicates start request(activity/broadcast/service/provider) should be denied.
        public static final int STATE_DENIED = 1; 
        // State that indicates if the host process has already been running, then start request can 
        // be granted. If not, start request will be denied.
        public static final int STATE_DENIED_IF_NOT_RUNNING = 2;
	}
	public static class SleepNetWhite extends BaseColumns
	{
		public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/sleepnet");
		public static final String TABLE = "sleepnet";
	}
	public static class BlockActivity extends BaseColumns
	{
		public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/blockactivity");
		public static final String TABLE = "blockactivity";
		
		public static final String CLASS = "class";
	}
	public static class MsgWhite extends BaseColumns
	{
		public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/msgwhite");
		public static final String TABLE = "msgwhite";
	}
	public static class InstallWhite extends BaseColumns
	{
		public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/installwhite");
		public static final String TABLE = "installwhite";
	}
	/**
	*@hide
	*/
       public static class DozeWhiteList extends BaseColumns
	{
		public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/dozewhite");
		public static final String TABLE = "dozewhite";
	}
	public static final int SERVER_CONFIG = 1;
	public static final int LOCAL_CONFIG = 0;
	
	public static final int DISABLE = 0;
	public static final int ENABLE = 1;
	public static final int HIDE = 2;//purebackground,notification
	public static final int DEFENABLE = 3;//purebackground,notification,autolaunch
	public static final int NOTKILL = 4;//purebackground
}
