package com.prize.runoldtest.emmc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import com.prize.runoldtest.R;
import com.prize.runoldtest.sleeprsm.SlpRsmActivity;
import com.prize.runoldtest.test3d.Test3DActivity;
import com.prize.runoldtest.util.Const;
import com.prize.runoldtest.util.DataUtil;
import com.prize.runoldtest.util.LogToFile;
import com.prize.runoldtest.util.LogUtil;
import com.prize.runoldtest.util.OldTestResult;
import com.prize.runoldtest.util.SystemProperties;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.PowerManager;
import android.os.StatFs;
import android.text.format.Formatter;
import android.view.WindowManager;

@SuppressLint("SdCardPath")
@SuppressWarnings("deprecation")
public class EmmcActivity extends Activity {
	private long emmc_time;
	public static boolean createFile;
	private PowerManager.WakeLock wakeLock = null;
	private String TAG = "RuninEmmcTest";

	public enum FileUnit {
		KB, MB, GB
	}

	private Thread mthread = null;
	FileOutputStream fos = null;
	private long bsecircles = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_emmc);
		DataUtil.addDestoryActivity(EmmcActivity.this, "EmmcActivity");
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wakeLock = pm.newWakeLock(
				PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE,
				"My TAG");
		wakeLock.acquire();
		LogToFile.writeToFile(LogToFile.VERBOSE, TAG, "EmmcTest begin..." + "\n");
		LogUtil.e("EmmcActivity OnCreate()");
		Intent intent = getIntent();
		emmc_time = intent.getIntExtra(Const.EXTRA_MESSAGE, 0);
		IntentFilter storyfilter = new IntentFilter();
		storyfilter.addAction(Intent.ACTION_DEVICE_STORAGE_LOW);
		registerReceiver(StoryReceiver, storyfilter);
	}

	private BroadcastReceiver StoryReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context arg0, Intent arg1) {
			// TODO Auto-generated method stub
			LogToFile.writeToFile(LogToFile.VERBOSE, TAG, "StoryReceiver Story is out " + "\n");
			delFile("/sdcard/eMMCTest_KB.txt");
			delFile("/sdcard/eMMCTest_MB.txt");
			delFile("/sdcard/eMMCTest_GB.txt");
		}

	};

	protected void onStart() {
		super.onStart();
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
				| WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		int mbaseTestCircle = getDeviceStorageCircle();
		/*
		 * if(mbaseTestCircle!=0){ emmc_time=emmc_time*mbaseTestCircle; }
		 */

		if (mbaseTestCircle == 1) {
			bsecircles = 20;
		} else if (mbaseTestCircle == 2) {
			bsecircles = 45;
		} else if (mbaseTestCircle == 4) {
			bsecircles = 90;
		}
		LogToFile.writeToFile(LogToFile.VERBOSE, TAG, "EmmcTest onStart ..emmc_time:" + emmc_time + "\n");
		if (mthread != null && mthread.isAlive()) {
			LogUtil.e("start: thread is alive");
		} else {
			mthread = new Thread(new Runnable() {
				// while (emmc_time-- != 0){
				@Override
				public void run() {

					for (long i = emmc_time; i > 0; i--) {

						/*
						 * } while (emmc_time-- != 0) {
						 */
						LogToFile.writeToFile(LogToFile.VERBOSE, TAG, "EmmcTest begin emmc_time !=0..." + "\n");

						for (long j = bsecircles; j > 0; j--) {

							LogUtil.e("write file id = " + emmc_time);
							try {
								LogToFile.writeToFile(LogToFile.VERBOSE, TAG, "EmmcTest begin run..." + "\n");
								String filePath_KB = "/sdcard/eMMCTest_KB.txt";
								int fileSizekB = 100;// 100kb

								createFile = createFile(filePath_KB, fileSizekB, FileUnit.KB);

								int fileSizeMb = 100;// 100mb
								String filePath_MB = "/sdcard/eMMCTest_MB.txt";
								createFile = createFile(filePath_MB, fileSizeMb, FileUnit.MB);
								int fileSizeGb = 1;// 1gb
								String filePath_GB = "/sdcard/eMMCTest_GB.txt";
								createFile = createFile(filePath_GB, fileSizeGb, FileUnit.GB);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}

						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								LogToFile.writeToFile(LogToFile.VERBOSE, TAG, "runOnUiThread delete file..." + "\n");
								DataUtil.FlagSr = true;
								if (!createFile) {
									SystemProperties.set(Const.SYSPROPERTY_RUN_TEST_EMMC, Const.RUNIN_TEST_FAIL + "");
								}
								delFile("/sdcard/eMMCTest_KB.txt");
								delFile("/sdcard/eMMCTest_MB.txt");
								delFile("/sdcard/eMMCTest_GB.txt");

							}
						});
					}

					delFile("/sdcard/eMMCTest_KB.txt");
					delFile("/sdcard/eMMCTest_MB.txt");
					delFile("/sdcard/eMMCTest_GB.txt");
					LogToFile.writeToFile(LogToFile.VERBOSE, TAG, "EmmcTest finish..." + "\n");
					OldTestResult.EmmcTestresult = true;
					Intent intent = new Intent();
					// 把返回数据存入Intent
					intent.putExtra("result", "EMMCTest:PASS");
					// 设置返回数据
					EmmcActivity.this.setResult(RESULT_OK, intent);

					EmmcActivity.this.finish();
				}
			});
			mthread.start();
		}
	}

	private int getDeviceStorageCircle() {
		File path2 = Environment.getExternalStorageDirectory();
		StatFs stat = new StatFs(path2.getPath());
		long blockSize = stat.getBlockSize();
		long totalBlocks = stat.getBlockCount();
		long devicestorage = 0;
		long totalsize = blockSize * totalBlocks;
		// long basesize32=32*1024*1024*1024;
		// long basesize64=64*1024*1024*1024;
		int mbasecircle = 0;
		LogToFile.writeToFile(LogToFile.VERBOSE, TAG, "totalsize...:" + totalsize + "\n");
		long msize = 1024 * 1024 * 1024;
		if ((1.0 * totalsize / msize) > 0) {
			devicestorage = 32;
			mbasecircle = 1;
		}
		if ((1.0 * totalsize / msize) > 32) {
			devicestorage = 64;
			mbasecircle = 2;
		}
		if ((1.0 * totalsize / msize) > 64) {
			devicestorage = 128;
			mbasecircle = 4;
		}
		LogToFile.writeToFile(LogToFile.VERBOSE, TAG, "getDeviceStorage..." + devicestorage + "\n");
		LogToFile.writeToFile(LogToFile.VERBOSE, TAG, "mbasecircle..." + mbasecircle + "\n");
		return mbasecircle;
		// String str2=Formatter.formatFileSize(MainActivity.this, blockSize2 *
		// totalBlocks2);

	}

	public void onPause() {
		super.onPause();
		//EmmcActivity.this.finish();
	}

	public static void delFile(String fileName) {
		File file = new File(fileName);
		if (file.isFile()) {
			file.delete();
		}
		file.exists();
	}

	public boolean createFile(String targetFile, long fileLength, FileUnit unit) {
		long KBSIZE = 1024;
		long MBSIZE1 = 1024 * 1024;
		long MBSIZE10 = 1024 * 1024 * 10;
		switch (unit) {
		case KB:
			fileLength = fileLength * 1024;
			LogToFile.writeToFile(LogToFile.VERBOSE, TAG, "case KB fileLength:" + fileLength + "\n");
			break;
		case MB:
			fileLength = fileLength * 1024 * 1024;
			LogToFile.writeToFile(LogToFile.VERBOSE, TAG, "case MB fileLength:" + fileLength + "\n");
			break;
		case GB:
			fileLength = fileLength * 1024 * 1024 * 1024;
			LogToFile.writeToFile(LogToFile.VERBOSE, TAG, "case GB fileLength:" + fileLength + "\n");
			break;

		default:
			break;
		}

		File file = new File(targetFile);
		try {

			if (!file.exists()) {
				file.createNewFile();
			}

			long batchSize = 0;
			batchSize = fileLength;
			if (fileLength > KBSIZE) {
				batchSize = KBSIZE;
			}
			if (fileLength > MBSIZE1) {
				batchSize = MBSIZE1;
			}
			if (fileLength > MBSIZE10) {
				batchSize = MBSIZE10;
			}
			long count = fileLength / batchSize;
			long last = fileLength % batchSize;

			fos = new FileOutputStream(file);
			FileChannel fileChannel = fos.getChannel();
			LogToFile.writeToFile(LogToFile.VERBOSE, TAG, "EmmcActivity count:" + count + "\n");
			LogToFile.writeToFile(LogToFile.VERBOSE, TAG, "EmmcActivity batchSize:" + batchSize + "\n");
			for (int i = 0; i < count; i++) {
				ByteBuffer buffer = ByteBuffer.allocate((int) batchSize);
				fileChannel.write(buffer);

			}
			if (last != 0) {
				ByteBuffer buffer = ByteBuffer.allocate((int) last);
				fileChannel.write(buffer);
			}
			fos.close();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (fos != null) {
					fos.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (wakeLock.isHeld()) {
			wakeLock.release();
		}
		unregisterReceiver(StoryReceiver);
		if (wakeLock.isHeld()) {
			wakeLock.release();
		}
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		/*
		 * Intent intent = new Intent(); //把返回数据存入Intent
		 * intent.putExtra("result", "EMMCTest:FAIL"); //设置返回数据
		 * EmmcActivity.this.setResult(RESULT_OK, intent);
		 */
		if (fos != null) {
			try {
				fos.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (mthread != null) {
			mthread.interrupt();
			mthread = null;
		}

		if (createFile) {
			delFile("/sdcard/eMMCTest_KB.txt");
			delFile("/sdcard/eMMCTest_MB.txt");
			delFile("/sdcard/eMMCTest_GB.txt");
		}

		DataUtil.finishBackPressActivity();
		finish();
	}

}
