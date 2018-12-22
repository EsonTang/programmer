package com.prize.cloudlist;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Date;
import java.util.Calendar;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

import android.content.Context;
import android.text.TextUtils;
import android.util.Xml;
import android.os.SystemProperties;
public class CloudUtils
{
	public static final String TAG = "whitelist";
	public static final boolean IS_DEBUG = true;

	public static String getImei() {
		String imei1=null,imei2=null,imei=null;
		try {			
			imei1 = SystemProperties.get("gsm.mtk.imei1", "");
			imei2 = SystemProperties.get("gsm.mtk.imei2", "");
			
			if (!TextUtils.isEmpty(imei1)) {
				imei = imei1;
			} else if (!TextUtils.isEmpty(imei2)) {
				imei = imei2;
			} else {
				imei = null;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return imei;
	}
	public static boolean sync(FileOutputStream stream)
	{
		try
		{
			if (stream != null)
			{
				stream.getFD().sync();
			}
			return true;
		}
		catch (IOException e)
		{
		}
		return false;
	}

	public static String getCheckRootDataFilePath(Context context)
	{
		return getCloudListDir() + "/" + "checkupdate";
	}
	public static String getCloudListDir()
	{
		return "/data/system/cloudlist";
	}
	public static String getScreenOffKillDataFilePath(Context context)
	{
		return getCloudListDir() + "/" + "screenofkill.xml";
	}
	public static String getSmartKillDataFilePath(Context context)
	{
		return getCloudListDir() + "/" + "smartkill.xml";
	}
	public static boolean copyFile(File src,File dest)
	{
		boolean ret = false;
		long len = src.length();
		FileOutputStream os =null;
		FileInputStream is = null;
		try
		{
			is = new FileInputStream(src);
			os = new FileOutputStream(dest);
			byte[] buffer = new byte[(int)len];
			is.read(buffer);
			os.write(buffer);
			os.flush();
			sync(os);
			
			is.close();
			is = null;
			os.close();
			os = null;	
			
			ret = true;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		if(is != null)
		{
			try
			{
				is.close();
			}
			catch(IOException e)
			{
				
			}
		}
		if(os != null)
		{
			try
			{
				os.close();
			}
			catch(IOException e)
			{
				
			}
		}
		return ret;
	}
	public static class ReleateWakeupItem
	{
		public String targetpkg;
		public String action;
		public String classname;
		public String callerpkg;
		public int state;

		public ReleateWakeupItem(String _targetpkg, String _classname, String _action, String _callpkg, int _state)
		{
			targetpkg = _targetpkg;
			action = _action;
			classname = _classname;
			callerpkg = _callpkg;
			state = _state;
		}

		public String toString()
		{
			String str = "targetpkg:" + targetpkg + ",action:" + action + ",class:" + classname + ",caller:" + callerpkg + ",state:" + state;
			return str;
		}
	}
	public static class ProviderWakeupItem
	{
		public String targetpkg;		
		public String classname;
		public String callerpkg;
		public int state;

		public ProviderWakeupItem(String _targetpkg, String _classname, String _callpkg, int _state)
		{
			targetpkg = _targetpkg;			
			classname = _classname;
			callerpkg = _callpkg;
			state = _state;
		}

		public String toString()
		{
			String str = "targetpkg:" + targetpkg +  ",class:" + classname + ",caller:" + callerpkg + ",state:" + state;
			return str;
		}
	}
	// //////////////////////////////////////////////////////////////////

	// ///////////////////////////////////////////////////////////////////////
	public static final String TAG_UPDATE_CLOUD_LIST = "cloudlist";
	public static final String TAG_DOWNLOAD_WHITELIST_TIME = "whithlisttime";
	public static final String TAG_WHITELIST_VERSION = "whithlistversion";
	public static final String ATTR_YEAR = "year";
	public static final String ATTR_MONTH = "month";
	public static final String ATTR_DAY = "day";
	public static final String ATTR_HOUR = "hour";
	public static final String ATTR_MIN = "min";
	public static final String ATTR_SECOND = "second";

	public static final String ATTR_PUREBKGROUND_VERSION = "purever";
	public static final String ATTR_NOTIFICATION_VERSION = "notificationver";
	public static final String ATTR_FLOATWINDOW_VERSION = "floatwindowver";
	public static final String ATTR_AUTOLAUNCH_VERSION = "autolaunchver";
	public static final String ATTR_NETFORBADE_VERSION = "netforbadever";
	public static final String ATTR_WAKEUP_VERSION = "wakeupver";
	public static final String ATTR_SLEEPNET_VERSION = "sleepnetver";
	public static final String ATTR_BLOCKACTIVITY_VERSION = "blockacver";
	public static final String ATTR_MSG_WHITELIST_VERSION = "msgver";
	public static final String ATTR_INSTALL_WHITELIST_VERSION = "installver";
	public static final String ATTR_DOZE_WHITELIST_VERSION = "dozever";
	public static final String ATTR_SCREENOFFKILL_WHITELIST_VERSION = "screenoffkillver";
	public static final String ATTR_SMARTKILL_WHITELIST_VERSION = "smartkillver";
	public static final String ATTR_PROVIDER_WAKEUP_VERSION = "providerwakeup";

	public static class Datatime
	{
		public int year;
		public int month;
		public int dayofmonth;
		public int hourofday;
		public int min;
		public int second;
	}

	public static class CloudUpdateData
	{
		public String purebkgroundver;
		public String notificationver;
		public String floatwindowver;
		public String autolaunchver;
		public String netforbadever;
		public String wakeupver;
		public String sleepnetver;
		public String blockactivityver;
		public String msgwhitelistversion;
		public String installwhitelistversion;
		public String dozewhitelistversion;
		public String screenoffkillversion;
		public String smartkillversion;
		public String providerwakeupversion;

		public Datatime lastdownloadwhitelisttime;

		public CloudUpdateData()
		{
			purebkgroundver = "";
			notificationver = "";
			floatwindowver = "";
			autolaunchver = "";
			netforbadever = "";
			wakeupver = "";
			sleepnetver = "";
			blockactivityver = "";
			msgwhitelistversion = "";
			installwhitelistversion = "";
			dozewhitelistversion = "";
			screenoffkillversion = "";
			smartkillversion = "";
			providerwakeupversion = "";

			lastdownloadwhitelisttime = new Datatime();
		}
	};

	public boolean writeCloudUpdateDataFile(CloudUpdateData cloudupdatedata, String savepath)
	{
		boolean ret = false;
		File file = new File(savepath);
		FileOutputStream fstr = null;
		BufferedOutputStream str = null;
		try
		{
			fstr = new FileOutputStream(file);
			str = new BufferedOutputStream(fstr);

			final XmlSerializer serializer = new FastXmlSerializer();
			serializer.setOutput(str, "utf-8");

			serializer.startDocument(null, true);
			serializer.startTag(null, TAG_UPDATE_CLOUD_LIST);

			serializer.startTag(null, TAG_WHITELIST_VERSION);
			serializer.attribute(null, ATTR_PUREBKGROUND_VERSION, cloudupdatedata.purebkgroundver);
			serializer.attribute(null, ATTR_NOTIFICATION_VERSION, cloudupdatedata.notificationver);
			serializer.attribute(null, ATTR_FLOATWINDOW_VERSION, cloudupdatedata.floatwindowver);
			serializer.attribute(null, ATTR_AUTOLAUNCH_VERSION, cloudupdatedata.autolaunchver);
			serializer.attribute(null, ATTR_NETFORBADE_VERSION, cloudupdatedata.netforbadever);
			serializer.attribute(null, ATTR_WAKEUP_VERSION, cloudupdatedata.wakeupver);
			serializer.attribute(null, ATTR_SLEEPNET_VERSION, cloudupdatedata.sleepnetver);
			serializer.attribute(null, ATTR_BLOCKACTIVITY_VERSION, cloudupdatedata.blockactivityver);
			serializer.attribute(null, ATTR_MSG_WHITELIST_VERSION, cloudupdatedata.msgwhitelistversion);
			serializer.attribute(null, ATTR_INSTALL_WHITELIST_VERSION, cloudupdatedata.installwhitelistversion);
			serializer.attribute(null, ATTR_DOZE_WHITELIST_VERSION, cloudupdatedata.dozewhitelistversion);
			serializer.attribute(null, ATTR_SCREENOFFKILL_WHITELIST_VERSION, cloudupdatedata.screenoffkillversion);
			serializer.attribute(null, ATTR_SMARTKILL_WHITELIST_VERSION, cloudupdatedata.smartkillversion);
			serializer.attribute(null, ATTR_PROVIDER_WAKEUP_VERSION, cloudupdatedata.providerwakeupversion);
			serializer.endTag(null, TAG_WHITELIST_VERSION);

			serializer.startTag(null, TAG_DOWNLOAD_WHITELIST_TIME);
			serializer.attribute(null, ATTR_YEAR, "" + cloudupdatedata.lastdownloadwhitelisttime.year);
			serializer.attribute(null, ATTR_MONTH, "" + cloudupdatedata.lastdownloadwhitelisttime.month);
			serializer.attribute(null, ATTR_DAY, "" + cloudupdatedata.lastdownloadwhitelisttime.dayofmonth);
			serializer.attribute(null, ATTR_HOUR, "" + cloudupdatedata.lastdownloadwhitelisttime.hourofday);
			serializer.attribute(null, ATTR_MIN, "" + cloudupdatedata.lastdownloadwhitelisttime.min);
			serializer.attribute(null, ATTR_SECOND, "" + cloudupdatedata.lastdownloadwhitelisttime.second);
			serializer.endTag(null, TAG_DOWNLOAD_WHITELIST_TIME);

			serializer.endTag(null, TAG_UPDATE_CLOUD_LIST);
			serializer.endDocument();

			str.flush();
			sync(fstr);
			str.close();
			str = null;
			ret = true;
		}
		catch (Exception e)
		{
			ret = false;
		}
		finally
		{
			try
			{
				if (str != null)
				{
					str.close();
				}
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		return ret;
	}

	public CloudUpdateData readCloudUpdateDataFile(String filepath)
	{
		CloudUpdateData checkrootdata = new CloudUpdateData();
		FileInputStream str = null;
		try
		{
			str = new FileInputStream(filepath);
			final XmlPullParser parser = Xml.newPullParser();
			parser.setInput(str, null);

			int type;
			while ((type = parser.next()) != XmlPullParser.START_TAG && type != XmlPullParser.END_DOCUMENT)
			{
				;
			}

			// checkrootdata
			if (type != XmlPullParser.START_TAG)
			{
				str.close();
				return checkrootdata;
			}

			int outerDepth = parser.getDepth();
			while ((type = parser.next()) != XmlPullParser.END_DOCUMENT && (type != XmlPullParser.END_TAG || parser.getDepth() > outerDepth))
			{
				if (type == XmlPullParser.END_TAG || type == XmlPullParser.TEXT)
				{
					continue;
				}
				String tagName = parser.getName();
				if (tagName.equals(TAG_WHITELIST_VERSION))
				{
					checkrootdata.purebkgroundver = parser.getAttributeValue(null, ATTR_PUREBKGROUND_VERSION);
					checkrootdata.notificationver = parser.getAttributeValue(null, ATTR_NOTIFICATION_VERSION);
					checkrootdata.floatwindowver = parser.getAttributeValue(null, ATTR_FLOATWINDOW_VERSION);
					checkrootdata.autolaunchver = parser.getAttributeValue(null, ATTR_AUTOLAUNCH_VERSION);
					checkrootdata.netforbadever = parser.getAttributeValue(null, ATTR_NETFORBADE_VERSION);
					checkrootdata.wakeupver = parser.getAttributeValue(null, ATTR_WAKEUP_VERSION);
					checkrootdata.sleepnetver = parser.getAttributeValue(null, ATTR_SLEEPNET_VERSION);
					checkrootdata.blockactivityver = parser.getAttributeValue(null, ATTR_BLOCKACTIVITY_VERSION);
					checkrootdata.msgwhitelistversion = parser.getAttributeValue(null, ATTR_MSG_WHITELIST_VERSION);
					checkrootdata.installwhitelistversion = parser.getAttributeValue(null, ATTR_INSTALL_WHITELIST_VERSION);
					checkrootdata.dozewhitelistversion = parser.getAttributeValue(null, ATTR_DOZE_WHITELIST_VERSION);
					checkrootdata.screenoffkillversion = parser.getAttributeValue(null, ATTR_SCREENOFFKILL_WHITELIST_VERSION);
					checkrootdata.smartkillversion = parser.getAttributeValue(null, ATTR_SMARTKILL_WHITELIST_VERSION);
					checkrootdata.providerwakeupversion = parser.getAttributeValue(null, ATTR_PROVIDER_WAKEUP_VERSION);
				}
				else if (tagName.equals(TAG_DOWNLOAD_WHITELIST_TIME))
				{
					String strtemp = parser.getAttributeValue(null, ATTR_YEAR);
					try
					{
						checkrootdata.lastdownloadwhitelisttime.year = Integer.parseInt(strtemp);
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}

					strtemp = parser.getAttributeValue(null, ATTR_MONTH);
					try
					{
						checkrootdata.lastdownloadwhitelisttime.month = Integer.parseInt(strtemp);
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}

					strtemp = parser.getAttributeValue(null, ATTR_DAY);
					try
					{
						checkrootdata.lastdownloadwhitelisttime.dayofmonth = Integer.parseInt(strtemp);
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}

					strtemp = parser.getAttributeValue(null, ATTR_HOUR);
					try
					{
						checkrootdata.lastdownloadwhitelisttime.hourofday = Integer.parseInt(strtemp);
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}

					strtemp = parser.getAttributeValue(null, ATTR_MIN);
					try
					{
						checkrootdata.lastdownloadwhitelisttime.min = Integer.parseInt(strtemp);
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}

					strtemp = parser.getAttributeValue(null, ATTR_SECOND);
					try
					{
						checkrootdata.lastdownloadwhitelisttime.second = Integer.parseInt(strtemp);
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}
			}

		}
		catch (Exception e)
		{
		}
		finally
		{
			try
			{
				if (str != null)
				{
					str.close();
				}
			}
			catch (IOException e)
			{

			}
		}

		return checkrootdata;
	}

	// /////////////////////////////////////////////////////////////////////////
	public static class UpgradeUpdateData
	{
		public static final String STATUS_CHECKVER = "checkver";
		public static final String STATUS_DOWNLOAD = "download";
		public static final String STATUS_INSTALL = "install";

		public int svrVerNum;// server version
		public String downloadurl;// download url
		public String localpath;// local path
		// checkver->download->install->checkver
		// if download try download everyday
		// if install try install everyday
		// nothing check ver everyday
		public String status;// "download","install","checkver"

		public Datatime checkversiontime;
		public Datatime lastdownloadtime;
		public Datatime lastinstalltime;

		public UpgradeUpdateData()
		{
			svrVerNum = 0;
			status = STATUS_CHECKVER;
			downloadurl = "";
			localpath = "";
			checkversiontime = new Datatime();
			lastdownloadtime = new Datatime();
			lastinstalltime = new Datatime();
		}
	};

	public boolean writeUpgradeUpdateDataFile(UpgradeUpdateData upgradeupdatedata, String savepath)
	{
		boolean ret = false;
		File file = new File(savepath);
		FileOutputStream fstr = null;
		BufferedOutputStream str = null;
		try
		{
			fstr = new FileOutputStream(file);
			str = new BufferedOutputStream(fstr);

			final XmlSerializer serializer = new FastXmlSerializer();
			serializer.setOutput(str, "utf-8");

			serializer.startDocument(null, true);
			serializer.startTag(null, "upgrade");

			serializer.startTag(null, "upgradeupdatever");
			serializer.attribute(null, "version", "" + upgradeupdatedata.svrVerNum);
			serializer.attribute(null, "downloadurl", upgradeupdatedata.downloadurl);
			serializer.attribute(null, "localpath", upgradeupdatedata.localpath);
			serializer.endTag(null, "upgradeupdatever");

			serializer.startTag(null, "checkversiontime");
			serializer.attribute(null, ATTR_YEAR, "" + upgradeupdatedata.checkversiontime.year);
			serializer.attribute(null, ATTR_MONTH, "" + upgradeupdatedata.checkversiontime.month);
			serializer.attribute(null, ATTR_DAY, "" + upgradeupdatedata.checkversiontime.dayofmonth);
			serializer.attribute(null, ATTR_HOUR, "" + upgradeupdatedata.checkversiontime.hourofday);
			serializer.attribute(null, ATTR_MIN, "" + upgradeupdatedata.checkversiontime.min);
			serializer.attribute(null, ATTR_SECOND, "" + upgradeupdatedata.checkversiontime.second);
			serializer.endTag(null, "checkversiontime");

			serializer.startTag(null, "downloadtime");
			serializer.attribute(null, ATTR_YEAR, "" + upgradeupdatedata.lastdownloadtime.year);
			serializer.attribute(null, ATTR_MONTH, "" + upgradeupdatedata.lastdownloadtime.month);
			serializer.attribute(null, ATTR_DAY, "" + upgradeupdatedata.lastdownloadtime.dayofmonth);
			serializer.attribute(null, ATTR_HOUR, "" + upgradeupdatedata.lastdownloadtime.hourofday);
			serializer.attribute(null, ATTR_MIN, "" + upgradeupdatedata.lastdownloadtime.min);
			serializer.attribute(null, ATTR_SECOND, "" + upgradeupdatedata.lastdownloadtime.second);
			serializer.endTag(null, "downloadtime");

			serializer.startTag(null, "installtime");
			serializer.attribute(null, ATTR_YEAR, "" + upgradeupdatedata.lastinstalltime.year);
			serializer.attribute(null, ATTR_MONTH, "" + upgradeupdatedata.lastinstalltime.month);
			serializer.attribute(null, ATTR_DAY, "" + upgradeupdatedata.lastinstalltime.dayofmonth);
			serializer.attribute(null, ATTR_HOUR, "" + upgradeupdatedata.lastinstalltime.hourofday);
			serializer.attribute(null, ATTR_MIN, "" + upgradeupdatedata.lastinstalltime.min);
			serializer.attribute(null, ATTR_SECOND, "" + upgradeupdatedata.lastinstalltime.second);
			serializer.endTag(null, "installtime");

			serializer.endTag(null, "upgrade");
			serializer.endDocument();

			str.flush();
			sync(fstr);
			str.close();
			str = null;
			ret = true;
		}
		catch (Exception e)
		{
			ret = false;
		}
		finally
		{
			try
			{
				if (str != null)
				{
					str.close();
				}
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		return ret;
	}

	public void readDatatimeFromXmlParser(XmlPullParser parser, Datatime datatime)
	{
		String strtemp = parser.getAttributeValue(null, ATTR_YEAR);
		try
		{
			datatime.year = Integer.parseInt(strtemp);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		strtemp = parser.getAttributeValue(null, ATTR_MONTH);
		try
		{
			datatime.month = Integer.parseInt(strtemp);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		strtemp = parser.getAttributeValue(null, ATTR_DAY);
		try
		{
			datatime.dayofmonth = Integer.parseInt(strtemp);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		strtemp = parser.getAttributeValue(null, ATTR_HOUR);
		try
		{
			datatime.hourofday = Integer.parseInt(strtemp);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		strtemp = parser.getAttributeValue(null, ATTR_MIN);
		try
		{
			datatime.min = Integer.parseInt(strtemp);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		strtemp = parser.getAttributeValue(null, ATTR_SECOND);
		try
		{
			datatime.second = Integer.parseInt(strtemp);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public UpgradeUpdateData readUpgradeUpdateDataFile(String filepath)
	{
		UpgradeUpdateData upgradeupdatedata = new UpgradeUpdateData();
		FileInputStream str = null;
		try
		{
			str = new FileInputStream(filepath);
			final XmlPullParser parser = Xml.newPullParser();
			parser.setInput(str, null);

			int type;
			while ((type = parser.next()) != XmlPullParser.START_TAG && type != XmlPullParser.END_DOCUMENT)
			{
				;
			}

			// checkrootdata
			if (type != XmlPullParser.START_TAG)
			{
				str.close();
				return upgradeupdatedata;
			}

			int outerDepth = parser.getDepth();
			while ((type = parser.next()) != XmlPullParser.END_DOCUMENT && (type != XmlPullParser.END_TAG || parser.getDepth() > outerDepth))
			{
				if (type == XmlPullParser.END_TAG || type == XmlPullParser.TEXT)
				{
					continue;
				}
				String tagName = parser.getName();
				if (tagName.equals("upgradeupdatever"))
				{
					String vernum = parser.getAttributeValue(null, "version");
					upgradeupdatedata.svrVerNum = Integer.parseInt(vernum);
					upgradeupdatedata.downloadurl = parser.getAttributeValue(null, "downloadurl");
					upgradeupdatedata.localpath = parser.getAttributeValue(null, "localpath");
				}
				else if (tagName.equals("checkversiontime"))
				{
					readDatatimeFromXmlParser(parser, upgradeupdatedata.checkversiontime);
				}
				else if (tagName.equals("downloadtime"))
				{
					readDatatimeFromXmlParser(parser, upgradeupdatedata.lastdownloadtime);
				}
				else if (tagName.equals("installtime"))
				{
					readDatatimeFromXmlParser(parser, upgradeupdatedata.lastinstalltime);
				}
			}

		}
		catch (Exception e)
		{
		}
		finally
		{
			try
			{
				if (str != null)
				{
					str.close();
				}
			}
			catch (IOException e)
			{

			}
		}

		return upgradeupdatedata;
	}

	// /////////////////////////////////////////////////////////////////////////////////
	public static String getCurtime()
	{
		Calendar c = Calendar.getInstance();
		String time = "" + String.format("%04d", c.get(Calendar.YEAR));
		time += "-" + String.format("%02d", c.get(Calendar.MONTH) + 1);
		time += "-" + String.format("%02d", c.get(Calendar.DAY_OF_MONTH));
		time += " " + String.format("%02d", c.get(Calendar.HOUR_OF_DAY));
		time += ":" + String.format("%02d", c.get(Calendar.MINUTE));
		time += ":" + String.format("%02d", c.get(Calendar.SECOND));
		return time;
	}

	public static long getUTCTime()
	{
		Calendar c = Calendar.getInstance();
		return Date.UTC(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH), c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), c.get(Calendar.SECOND));
	}

	/**
	 * 
	 * @param path
	 * @return 无权限的文件返回null
	 */
	public static String getProperty(String property)
	{
		String propertytext = null;
		String command = "getprop " + property;
		Runtime r = Runtime.getRuntime();
		Process p;
		try
		{
			p = r.exec(command);
			BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String inline;
			StringBuffer strbuf = new StringBuffer();
			while ((inline = br.readLine()) != null)
			{
				strbuf.append(inline);
			}
			br.close();
			p.waitFor();
			if (strbuf.length() > 0)
			{
				propertytext = strbuf.toString();
			}
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (InterruptedException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return propertytext;
	}
}
