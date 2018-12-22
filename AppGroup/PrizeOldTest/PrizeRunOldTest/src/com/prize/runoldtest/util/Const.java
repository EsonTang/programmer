package com.prize.runoldtest.util;

public class Const {
	
	public static final int SET_ZOOM_OUT = 0;
	public static final int SET_ZOOM_IN = 1;
	// camera cmd order
	public static final String CMD_CAMERA_FRONT_OPEN = "11";
	public static final String CMD_CAMERA_FRONT_CLOSE = "10";
	// public static final String CMD_CAMERA_FRONT_FOCUS = 1;
	public static final String CMD_CAMERA_FRONT_SHUTTER = "0";
	public static final String CMD_CAMERA_BACK_OPEN = "01";
	public static final String CMD_CAMERA_BACK_CLOSE = "00";
	public static final String CMD_CAMERA_BACK_FOCUS = "03";
	public static final String CMD_CAMERA_BACK_SHUTTER = "a";

	// audio cmd order
	public static final String CDM_AUDIO_MIC = "12"; // 主麦
	public static final String CMD_AUDIO_PLAYER = "23";// 扬声器
	public static final String CMD_AUDIO_RECEIVER = "21";// 听筒
	public static final String CMD_AUDIO_HEADSET = "13";// 耳机
	public static final String CMD_AUDIO_FINISH = "00";// 耳机

	public static final String RESULT_FAIL = "0";
	public static final String RESULT_SUCCUSS = "2";

	public static final String EXTRA_MESSAGE = "extra_message";
	public static final String SHARE_PREF_REBOOT_SELECTE = "reboot_selected";
	public static final String SHARE_PREF_REBOOT_CURTIMES = "reboot_currenttimes";
	public static final String SHARE_PREF_REBOOT_TOTALTIMES = "reboot_totaltimes";
	public static final String SHARE_PREF_DDR_TEST = "ddr_test";
	public static final String SHARE_PREF_DDR_TEST_CIRCLES = "ddr_test_circles";

	public static final int TIMER_FONT_SIZE = 70;
	public static final int ALT_STATE_FONT_SIZE = 40;
	
	public static final int TITLE_FONT_SIZE = 30;
	public static final int SUBTITILE_FONT_SIZE = 25;
	public static final int ITEM_FONT_SIZE = 20;
	public static final int BRIGHTNESS_IN_TESTINT = 100;
	public static final int BRIGHTNESS_IN_CHARGING = 10;
	
	public static final String PACKAGE_DATA_DIR			= "/data/data/com.tools.alt/";
	public static final String SYSTEM_TOOLS_LOG_FILE	= PACKAGE_DATA_DIR + "system_tools.log";
	public static final String PACKAGE_FILES_DIR		= PACKAGE_DATA_DIR + "files/";
	public static final String TIME_POINT_LOG_FILE		= PACKAGE_FILES_DIR + "timepoint.log";
	public static final String CHART_LOG_PATH			= PACKAGE_FILES_DIR + "Chartlog.txt";
	public static final String TEMP_RESULT_PATH			= PACKAGE_FILES_DIR + "temp_result.txt";
	public static final String SYSTEM_ETC_DIR 			= "/system/etc/";
	public static final String DEFAULT_CONFIG_PATH		= SYSTEM_ETC_DIR + "tools_config.xml";
	public static final String MAPPING_TABLE_PATH		= SYSTEM_ETC_DIR + "config_mapping.txt";
	public static final String MTK_RFCOMMAND_PATH		= SYSTEM_ETC_DIR + "runit_mtk.xml";
//	public static final String CONFIG_PATH_1			= SYSTEM_ETC_DIR + "tools_config.xml_1";
//	public static final String[] CONFIG_PATH_ARY		= {SYSTEM_ETC_DIR + "tools_config.xml"
//														 , SYSTEM_ETC_DIR + "tools_config_1.xml"};
	public static final int MAX_CONFIG_OPTION			= 9;
	public static final String DEFAULT_RFCOMMAND_PATH	= SYSTEM_ETC_DIR + "runit.xml";
	public static final String DEVICE_MODEL_PATH		= "/proc/devmodel";
	public static final String BASE_BAND_PATH 			= "/proc/baseband";
	public static final String BAND_INFO_PATH 			= "/proc/bandinfo";
	public static final String PRODUCT_ID_PATH			= "/proc/productid";
	
	public static final String USB_TYPE_PATH = "/sys/class/power_supply/usb/type";
	public static final String USB_TYPE_DISCONNECTED = "Unknown";
	public static final String USB_TYPE_PATH_MTK = "/sys/class/power_supply/battery/status";
	public static final String USB_TYPE_DISCONNECTED_MTK = "Not charging";
	
	public static final String TP_FW_PATH = "/proc/AllHWList/Touch";
	public static final String TP_FW_PATH_MTK = "/sys/android_touch/ftmgetversion";
	
	public static final String CAMERA_FW_PATH = "/sys/kernel/isp_control/platform_version";
	
	//mtk
	public static final String MTK_ALT_DIR = "/storage/sdcard0/ALT/";
//	public static final String MTK_RF_LOG_PATH = MTK_ALT_DIR + "mtk_rf.log";
	public static final String MTK_RF_LOG_FILE = "mtk_rf.log";
	
	public static final int CHART_LOG_INTERVAL = 5;
	public static final int CPU_USAGE_INTERVAL = CHART_LOG_INTERVAL;
	
	public static final String SECRET_CODE = "secretCode";
	public static final String TARGET_CYCLE = "targetCycle";
	public static final String IS_LAST_CONFIG = "isLastConfig";
	public static final String IS_AUTO_RUN = "isAutoRun";
	public static final String CONFIG_IDX = "configIdx";
	public static final int ALT_MAINACTIVITY = 0;
	public static final int ALT_SECRETCODE_786 = 786;
	public static final int ALT_SECRETCODE_7861 = 7861;
	public static final int ALT_SECRETCODE_7862 = 7862;
	public static final int ALT_SECRETCODE_7869 = 7869;
//	public static final String SYSPROPERTY_SECRET_CODE			= "persist.sys.alt_secret_code";
	public static final String SYSPROPERTY_CONFIG_IDX			= "persist.sys.alt_config_idx";
	public static final String SYSPROPERTY_ALT_THIS_CYCLE		= "persist.sys.alt_this_cycle";
	public static final String SYSPROPERTY_ALT_TARGET_CYCLE		= "persist.sys.alt_target_cycle";
	public static final String SYSPROPERTY_ALT_IS_LASTCONFIG	= "persist.sys.alt_is_last";
	
	public enum TIME_POINT_TYPE {START, END}
	public static final String COMPLETE = "Complete";
	public static final String CLOSED = "Closed";//for chartlog
	public static final String LOG_STRING_START = "START";
	public static final String LOG_STRING_END = "END";
	public static final String PASS = "PASS";
	public static final String FAIL = "FAIL";
	
//	public static final String LOG_TEST_ITEM = "Test Item";
//	public static final String LOG_TEST_SPEC = "TEST_SPEC";
//	public static final String LOG_ITEM_NAME = "ITEM_NAME";
//	public static final String LOG_LOOP_RESULT = "LOOP_RESULT";
//	public static final String LOG_TEST_RESULT = "TEST_RESULT";
	
	public static final String REBOOT_COUNT = "reboot_count";

//	public static final int MESSAGE_CHECK_CHARGING = 0;
	public static final int MESSAGE_TYPE_READY = 0;
	public static final int MESSAGE_TYPE_TEST_UPDATE = 1;
	public static final int MESSAGE_TYPE_TEST_FINISH = 2;
	public static final int MESSAGE_TYPE_MAIN_STAGE_COMPLETE = 3;
	public static final int MESSAGE_TYPE_LAST_STAGE_COMPLETE = 4;
	public static final int MESSAGE_TYPE_UPDATE_REMAINING_TIME = 5;
//	public static final int MESSAGE_TYPE_LCM_COMPLETE = 12;
	public static final int MESSAGE_TYPE_BATTERY_CAPACITY = 12;
	public static final int BATTERY_CAPACITY_START = 0;
	public static final int BATTERY_CAPACITY_END = 1;
//	public static final int MESSAGE_TYPE_END_BATTERY_CAPACITY = 13;
//	public static final int MESSAGE_TYPE_BATTERY_CAPACITY_COMPLETE = 14;
//	public static final int MESSAGE_TYPE_ENABLE_COMPLETE_BUTTON = 15;
	public static final int MESSAGE_TYPE_CPU_MAXFREQ = 21;
	public static final int MESSAGE_TYPE_BACKGROUND_COLOR = 22;
	public static final int MESSAGE_TYPE_PLAY_VIDEO = 23;
	public static final int MESSAGE_TYPE_STOP_VIDEO = 24;
	public static final int MESSAGE_TYPE_START_PREVIEW = 25;
	public static final int MESSAGE_TYPE_STOP_PREVIEW = 26;
	public static final int MESSAGE_TYPE_START_PREVIEW_2 = 27;
	public static final int MESSAGE_TYPE_STOP_PREVIEW_2 = 28;
	public static final int MESSAGE_TYPE_TAKE_PIC = 29;
//	public static final int MESSAGE_TYPE_USB_CONNECTED = 30;
	public static final int MESSAGE_TYPE_RELEASE_CAMERA = 31;
	public static final int MESSAGE_TYPE_DISABLE_BLOCK_KEY = 40;
	public static final int MESSAGE_TYPE_MTK_RF_START = 50;
	public static final int MESSAGE_TYPE_MTK_RF_STOP = 51;
	public static final int MESSAGE_TYPE_MTK_FLIGHT_MODE = 52;
	public static final int MESSAGE_TYPE_MTK_AT_START = 53;
	public static final int MESSAGE_TYPE_MTK_AT_PAUSE = 54;
	public static final int MESSAGE_TYPE_MTK_REBOOT = 55;
	
	//for startActivityForResult
	public static final int ACTIVITY_FLAG_CPU_MAXFREQ = 1;
	public static final int ACTIVITY_FLAG_SCENARIO = 2;
	public static final int ACTIVITY_FLAG_MTK_RF = 3;
	
	public static final String LOOP = "loop";
	public static final String TOPBOX_NAME = "topbox";
	public static final String NAME = "name";
	public static final String PRIORITY = "priority";
	public static final String CYCLE = "cycle";
	public static final String PERIOD = "period";
	public static final String INTERVAL = "interval";
	public static final String S_INTERVAL = "s_interval";
	public static final String R_INTERVAL = "r_interval";
	public static final String ESTIMATE_TIME = "estimatetime";
	
	public static final String VALUE = "value";
	public static final String ENABLE = "enable";
	public static final String DISABLE = "disable";
	public static final String TYPE = "type";
	public static final String TRUE = "true";
	public static final String FALSE = "false";
	public static final String THREAD = "thread";
	public static final String PATH = "Path";
	public static final String BASE = "base";
	public static final String VERSION = "version";
	public static final String RD_MODE = "rd_mode";
	public static final String MTK_PLATFORM = "mtk_platform";
	
	public static final int DEFAULT_CPUMATH2_THREAD_COUNT = 4;
	public static final int DEFAULT_CPUMATH2_PERIOD = 300;
	
	public static final int DEFAULT_PRIORITY = 0;
	public static final int DEFAULT_CYCLE = 1;
	public static final int DEFAULT_PERIOD = 0;
	public static final int MAX_PRIORITY = 99;
//	public static final int PRIORITY_LCM = MAX_PRIORITY + 1;
	
	public static final long WAIT_INTERVAL_MS = 100;
	public static final long SENSOR_UPDATE_INTERVAL = 1;
	public static final long WIFI_UPDATE_INTERVAL = 1;
	public static final long BT_UPDATE_INTERVAL = 1;
	public static final long CPU_INFO_UPDATE_INTERVAL_MS = 500;
	public static final long BATTERY_CAPACITY_CHECK_INTERVAL = 5;

	public static final String TAG_RECORD_PATH = "RecordPath";
	public static final String TAG_PIC_PATH = "PicPath";
	
	//speaker, receiver, mainmic, vicemic
	public static final String TAG_SPEAKER = "Speaker";
	public static final String TAG_RECEIVER = "Receiver";
	public static final String TAG_MAINMIC = "MainMic";
	public static final String TAG_VICEMIC = "ViceMic";
	public static final String VOLUME_MODE = "volumeMode";
	public static final String VOLUME_MODE_MUTE = "mute";
	public static final String VOLUME_MODE_MAX = "max";
//	public static final String RECORD = "Record";
	public static final String MAX_RECORD_TIME = "MaxRecordTime";
//	public static final String RECORDER_TYPE_MIC = "mainmic";
//	public static final String RECORDER_TYPE_VICEMIC = "vicemic";
	public static final int DEFAULT_MAX_RECORD_TIME = 30;
	//backlight
	public static final String TAG_BACKLIGHT_MIN = "BacklightMin";
	public static final String TAG_BACKLIGHT_PATH = "BacklightPath";
	//charging mode, delay
	public static final String TAG_SET_CHARGING_MODE = "SetChargingMode";
	public static final String TAG_DELAY = "Delay";
	//alt setting
	public static final String TAG_ALT_SETTING = "alt_setting";
	//sensor
	public static final String TAG_GYRO = "Gyroscope";
	public static final String TAG_ACC = "Accelerometer";
	public static final String TAG_COMP = "Compass";
	public static final String TAG_LIGHT = "LightSensor";
	public static final String TAG_CAP = "CapSensor";
	//vibrator
	public static final String TAG_VIBR = "Vibrator";
	//wifi, bt
	public static final String TAG_WIFI = "Wifi";
	public static final String TAG_BT = "Bluetooth";
	//reboot
	public static final String TAG_REBOOT = "Reboot";
	public static final String REBOOT_CNT = "RebootCount";
	public static final int DEFAULT_REBOOT_ESTIMATE_TIME = 120;
	public static final int REBOOT_STATUS_NONE = 0;//useless?
	public static final int REBOOT_STATUS_ING = 1;
	public static final int RUNIN_TEST_SUCCESS = 1;
	public static final int RUNIN_TEST_FAIL = 0;
	public static final int REBOOT_STATUS_FINISH = 2;
	//video
	public static final String TAG_VIDEO = "Video";
	public static final String TAG_VIDEO_PATH = "VideoPath";
	//camera
	public static final String TAG_MAIN_CAMERA_VIDEO = "MainCameraVideo";
	public static final String TAG_MAIN_CAMERA_PICTURE = "MainCameraPic";
	public static final String TAG_FRONT_CAMERA_VIDEO = "FrontCameraVideo";
	public static final String TAG_FRONT_CAMERA_PICTURE = "FrontCameraPic";
	public static final int VIDEO_SIZE_X = 320;
	public static final int VIDEO_SIZE_Y = 240;
	public static final int VIDEO_FRAME_RATE = 15;
	public static final Long MAX_RECORD_SIZE = 2000000L;
	//cpumath
	public static final String TAG_CPUMATH = "CPUMath";
	public static final String TAG_CPU_MAXFREQ = "CPUMaxFreq";
	//change background
	public static final String TAG_CHANGE_BACKGROUND = "ChangeBackground";
	public static final int DEFAULT_LCM_INTERVAL = 5;
	//cpu info
	public static final int SUPPORT_CPU_NUMBER = 16;
	public static final int SUPPORT_TMP_PATH_NUMBER = 8;
	public static final String TAG_CPU_INFO = "CPUInfo";
	public static final String TAG_CPU_MAX_FREQ_PATH = "CPUMaxFreqPath";
	public static final String TAG_CPU_MIN_FREQ_PATH = "CPUMinFreqPath";
	public static final String TAG_CPU_FREQ_PATH = "CPU#ScaleFreqPath";
	public static final String TAG_CPU_TMP_PATH = "CPUTmpPath";
	public static final String TAG_CPU_HOTSPOT_PATH = "CPUHotspotPath";
//	public static final String TAG_CPU_FREQ_PATH_0 = "CPU0ScaleFreqPath";
//	public static final String TAG_CPU_FREQ_PATH_1 = "CPU1ScaleFreqPath";
//	public static final String TAG_CPU_FREQ_PATH_2 = "CPU2ScaleFreqPath";
//	public static final String TAG_CPU_FREQ_PATH_3 = "CPU3ScaleFreqPath";
	//battery info
	public static final String TAG_BATTERY_INFO = "BatteryInfo";
	public static final String TAG_BATTERY_CAPACITY_PATH = "BatteryCapacityPath";
	public static final String TAG_BATTERY_TMP_PATH = "BatteryTmpPath";
	public static final String TAG_BATTERY_THRESHOLD = "BatteryThreshold";
	public static final String START = "start";
	public static final String END = "end";
//	public static final String TAG_BATTERY_CAPACITY_THRESHOLD = "BatteryCapacityThreshold";
	public static final String TAG_BATTERY_AFTER_TEST = "BatteryAfterTest";
	public static final String TAG_UPPER_BOUND = "upperbound";
	public static final String TAG_LOWER_BOUND = "lowerbound";
	//current monitor
	public static final String TAG_CURRENT_MONITOR = "CurrentMonitor";
	public static final String TAG_CURRENT_PATH = "CurrentPath";
	public static final String CHARGING_BASE = "chargingBase";
	public static final String TAG_CHARGING_MODE_PATH = "ChargingModePath";
	public static final String TAG_CHARGING_ENABLE = "ChargingEnable";
	public static final String TAG_CHARGING_DISABLE = "ChargingDisable";
	public static final int DEFAULT_CURRENT_MONITOR_INTERVAL = 5;
	public static final String TAG_CURRENT_THRESHOLD = "CurrentThreshold";
	//suspend/resume
	public static final String TAG_SUSPEND_RESUME = "SuspendResume";
	public static final int SUSPEND_INTERVAL_MIN = 120;
	public static final int RESUME_INTERVAL_MIN = 1;
	//maxpower
	public static final String TAG_RFMAXPOWER = "RFMaxPower";
	public static final String TAG_MTK_PLATFORM = "MTKPlatform";
	public static final String TAG_QXDM_WORKAROUND = "QXDMWorkAround";
	public static final String TAG_BACKGROUND_COLOR = "BackgroundColor";
	public static final String SYSPROPERTY_USB_CONFIG = "sys.usb.config";
	//TP
	public static final String TAG_TP = "TP";
	public static final String TAG_TP_SELFTEST = "TPSelfTest";
	public static final String TAG_TP_PATH_WRITE = "TPPathWrite";
	public static final String TAG_TP_PATH_READ = "TPPathRead";
	public static final String SYSPROPERTY_TP_CNT = "persist.sys.alt_tp_cnt";
	public static final int DEFAULT_TP_INTERVAL = 60;
	public static final int DEFAULT_TP_SELFTEST_INTERVAL = 60;
	//charging
	public static final String TAG_CHARGING = "Charging";
//	public static final String SYSPROPERTY_TP_IN_TESTING = "persist.sys.runin_tp_testing";
	//LED
	public static final String TAG_LED = "LED";
	
	//system property
	public static final String SYSPROPERTY_TESTING_STATUS = "persist.sys.alt_testing_st";
	/* SYSPROPERTY_TESTING_STATUS
	 * ex."1:2_3:4_3:4_3:4_3:4"
	 * 1: thisCycle of topBox, must be DEFAULT_CYCLE=1
	 * 2: thisPriority of topBox
	 * 3: thisCycle of non-top box
	 * 4: thisPriority of non-top box
	 * 
	 * last "3:4" => the outerBox of reboot, and the priority of reboot is 3
	 */
																	 //1234567890123456789012345678901
	public static final String SYSPROPERTY_ALT_START				= "persist.sys.alt_start";
	public static final String SYSPROPERTY_REBOOT_TIME				= "persist.sys.alt_reboot_time";
	public static final String SYSPROPERTY_REBOOT_TARGET_COUNT		= "persist.sys.run_reboot_tcnt";
	public static final String SYSPROPERTY_REBOOT_COUNT				= "persist.sys.alt_reboot_cnt";
	public static final String SYSPROPERTY_REBOOT_STATUS			= "persist.sys.run_reboot_st";
	public static final String SYSPROPERTY_REBOOT_START_TIME		= "persist.sys.alt_reboot_stime";
	public static final String SYSPROPERTY_REMAINING_TIME			= "persist.sys.alt_remain_time";
	public static final String SYSPROPERTY_ALT_TEST_CNT				= "persist.sys.alt_test_cnt";
	public static final String SYSPROPERTY_RUN_TEST_REBOOT				= "persist.sys.run_test_reboot";
	public static final String SYSPROPERTY_RUN_TEST_CPU				= "persist.sys.run_test_cpu";
	public static final String SYSPROPERTY_RUN_TEST_MEM				= "persist.sys.run_test_mem";
	public static final String SYSPROPERTY_RUN_TEST_EMMC				= "persist.sys.run_test_emmc";
	public static final String SYSPROPERTY_RUN_TEST_CAM				= "persist.sys.run_test_cam";
	public static final String SYSPROPERTY_RUN_TEST_LCD				= "persist.sys.run_test_lcd";
	public static final String SYSPROPERTY_RUN_TEST_SR				= "persist.sys.run_test_sr";
	public static final String SYSPROPERTY_RUN_TEST_VIDEO				= "persist.sys.run_test_video";
	public static final String SYSPROPERTY_RUN_TEST_SR_TIMES				= "persist.sys.run_test_sr_times";
	
}
