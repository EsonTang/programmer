/*****************************************
*版权所有©2015,深圳市铂睿智恒科技有限公司
*
*文件名称：UsbSoundAndVibration
*内容摘要：控制USB连接时的音效和振动
*当前版本：1.0
*作	者：xiaxuefeng
*完成日期：2015-7-24
*修改记录：
*修改日期：
*版 本 号：
*修 改 人：
*修改内容：
...
*修改记录：
*修改日期：
*版 本 号：
*修 改 人：
*修改内容：
********************************************/
package com.android.systemui.usb;

import java.io.IOException;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.SystemClock;
import android.os.Vibrator;
import android.util.Log;
import android.os.SystemProperties;
import com.mediatek.common.prizeoption.PrizeOption;
import com.android.systemui.SystemUI;
import android.telephony.TelephonyManager;

public class UsbSoundAndVibration  extends SystemUI {
    private boolean usbHasDone = false;
	boolean shouldPlayBeep = false;
	boolean shouldVibrate = false;
	float BEEP_VOLUME = 1.0f;
	long VIBRATE_DURATION = 100;

    /* prize add solve the black background yueliu start */
	private static boolean isFirst = true;
	private static long WAIT_TIME = 60 * 1000;
	private Handler mHandler = new Handler();
    /* prize add solve the black background yueliu end */

    private BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        	SharedPreferences usbState = context
					.getSharedPreferences("usb_state", 0);
        	String mConnect = usbState.getString("usb_connect", "false");
            String action = intent.getAction();
            AudioManager audioService = (AudioManager) context
					.getSystemService(Context.AUDIO_SERVICE);
			if (audioService.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {
				shouldPlayBeep = true;
				shouldVibrate = false;
			} else if (audioService.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE) {
				shouldPlayBeep = false;
				shouldVibrate = true;
			}
			Vibrator vibrator = (Vibrator) context
					.getSystemService(Context.VIBRATOR_SERVICE);
			if (action.equals("android.hardware.usb.action.USB_STATE")) {
				if (intent.getExtras().getBoolean("connected")) {
					if (mConnect.equals("false")
							&& SystemProperties.get("persist.sys.usb_askagain",
									"false").equals("false")) {
						TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE); 
						if(tm.getCallState() == TelephonyManager.CALL_STATE_IDLE){
							usbState.edit().putString("usb_connect", "true").commit();
				
							usbHasDone = true;
							Intent intentUSB = new Intent();
							/*prize-usb-liuweiquan-20160615-start*/
							ComponentName cn = new ComponentName(
									"com.android.settings",
									"com.android.settings.deviceinfo.UsbModeChooserActivity");
							if(PrizeOption.PRIZE_USB_SETTINGS){
								cn = new ComponentName(
									"com.android.settings",
									"com.android.settings.UsbSettings");
							}
							/*prize-usb-liuweiquan-20160615-end*/
							intentUSB.setComponent(cn);
							intentUSB.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                            /* prize add solve the black background yueliu start */
                            //context.startActivity(intentUSB);
							if (isFirst && SystemClock.elapsedRealtime() < WAIT_TIME) {
                                mHandler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        context.startActivity(intentUSB);
                                    }
                                }, 10000);
                                isFirst = false;
                            } else {
                                context.startActivity(intentUSB);
                            }
                            /* prize add solve the black background yueliu end */
						}
					}
				} else {
					usbState.edit().putString("usb_connect", "false").commit();
				}	
			}		
            if (Intent.ACTION_BATTERY_CHANGED.equals(action)) {
                final int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS,
                        BatteryManager.BATTERY_STATUS_UNKNOWN);
				boolean mCharged = status == BatteryManager.BATTERY_STATUS_FULL;
                boolean mCharging = mCharged || status == BatteryManager.BATTERY_STATUS_CHARGING;
                if (mCharging) {
                	if (!usbHasDone) {
                		if (shouldVibrate && vibrator != null) {
                			vibrator.vibrate(VIBRATE_DURATION);
                		}
                		
                		MediaPlayer mediaPlayer = getMediaPlayer(context);
                		if (shouldPlayBeep && mediaPlayer != null) {
                			mediaPlayer.start();
                		}
						usbHasDone = true;
                	}
				} else {
					usbHasDone = false;
				}
            }
        } 
    };
	@Override
	public void start() {
		if (PrizeOption.PRIZE_USB_SETTINGS) {
			IntentFilter filter = new IntentFilter();
			filter.addAction(Intent.ACTION_BATTERY_CHANGED);
			filter.addAction("android.hardware.usb.action.USB_STATE");
        	mContext.registerReceiver(mUsbReceiver, filter);
        }
	}
	private MediaPlayer getMediaPlayer(Context context) {
		MediaPlayer mediaPlayer = new MediaPlayer();
		mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		mediaPlayer
				.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
					@Override
					public void onCompletion(MediaPlayer player) {
						player.seekTo(0);
                        /*PRIZE-add for bugid:50703-liufan-2018-03-05-start*/
                        if(player != null){
                            player.release();
                        }
                        /*PRIZE-add for bugid:50703-liufan-2018-03-05-end*/
					}
				});
		AssetFileDescriptor file = context.getResources().openRawResourceFd(
				com.android.systemui.R.raw.charger_connection);
		try {
			mediaPlayer.setDataSource(file.getFileDescriptor(),
					file.getStartOffset(), file.getLength());
			file.close();
			mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
			mediaPlayer.prepare();
		} catch (IOException ioe) {
			mediaPlayer = null;
		}
		return mediaPlayer;
	}
	
}
