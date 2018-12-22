package com.prize.runoldtest.emmc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import com.prize.runoldtest.R;
import com.prize.runoldtest.util.Const;
import com.prize.runoldtest.util.DataUtil;
import com.prize.runoldtest.util.LogUtil;
import com.prize.runoldtest.util.SystemProperties;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.PowerManager;
import android.os.StatFs;
import android.text.format.Formatter;
import android.util.Log;

@SuppressLint({ "SdCardPath", "HandlerLeak", "Wakelock" })
@SuppressWarnings("deprecation")
public class StableEmmcActivity extends Activity {
	private long test_counts;
	private int create_count_max;
	private int delete_count_max;
	public static boolean createFile;
	private String sdPath;
	private String mAvailableMemoryTotal;
	private String mAvailableMemoryIng;
	private PowerManager.WakeLock wakeLock = null;
	private Thread t;

	public enum FileUnit {
		KB, MB, GB
	}

	private boolean mStartTest = false;
	private final static int STARTTEST = 1;

	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case STARTTEST:
				Log.e("StableEmmcActivity", "-----test_counts(handle) = "
						+ test_counts + " -------");
				if (test_counts-- > 1) {
					mHandler.post(mRunnable);
				} else {
					mHandler.removeCallbacks(mRunnable);
					DataUtil.resetFlag();
					StableEmmcActivity.this.finish();
				}
				break;
			default:
				break;
			}
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LogUtil.e("onCreateStableEmmcActivity");
		setContentView(R.layout.activity_stable_emmc);
		DataUtil.addDestoryActivity(StableEmmcActivity.this,
				"StableEmmcActivity");
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK
				| PowerManager.ACQUIRE_CAUSES_WAKEUP, "StableEmmcActivity");
		if (wakeLock == null || !wakeLock.isHeld()) {
			wakeLock.acquire();
		}
		Intent intent = getIntent();
		test_counts = intent.getIntExtra(Const.EXTRA_MESSAGE, 0);
		LogUtil.e("StableEmmcActivity-----test_counts = " + test_counts);
		sdPath = Environment.getExternalStorageDirectory().getAbsolutePath();
		mAvailableMemoryTotal = Formatter.formatFileSize(
				StableEmmcActivity.this, getAvailSpace(sdPath));
		create_count_max = (int) ((Double.valueOf(mAvailableMemoryTotal
				.substring(0, 4)) * 1024) / (double) 1800);
		delete_count_max = (int) ((Double.valueOf(mAvailableMemoryTotal
				.substring(0, 4)) * 1024) / (double) 1800);
		LogUtil.e("----------onCreate's create_count_max = " + create_count_max
				+ "-----------");
		mStartTest = true;
		mHandler.postDelayed(mRunnable, 500);
	}

	Runnable mRunnable = new Runnable() {
		@Override
		public void run() {
			if (mStartTest) {
				closeThread();
				onstart();
				Log.e("StableEmmcActivity", "-----------mStartTest= "
						+ mStartTest + "---------");
			}
		}
	};

	protected void onstart() {
		t = new Thread(new Runnable() {
			@Override
			public void run() {
				create_count_max = delete_count_max;
				while (create_count_max-- != 0) {
					mAvailableMemoryIng = Formatter.formatFileSize(
							StableEmmcActivity.this, getAvailSpace(sdPath));
					LogUtil.e("sdAvaliSpace_using("
							+ create_count_max
							+ ")= "
							+ Double.valueOf(mAvailableMemoryIng
									.substring(0, 4)));
					LogUtil.e("write file id = " + create_count_max);
					try {
						int fileSize1 = 100;
						int fileSize2 = 200;
						int fileSize3 = 500;
						int fileSize4 = 1000;

						String filePath_MB1 = "/sdcard/eMMCTest_MB1_"
								+ create_count_max + ".txt";
						createFile = createFile(filePath_MB1, fileSize1,
								FileUnit.MB);

						String filePath_MB2 = "/sdcard/eMMCTest_MB2_"
								+ create_count_max + ".txt";
						createFile = createFile(filePath_MB2, fileSize2,
								FileUnit.MB);

						String filePath_MB3 = "/sdcard/eMMCTest_MB3_"
								+ create_count_max + ".txt";
						createFile = createFile(filePath_MB3, fileSize3,
								FileUnit.MB);

						String filePath_MB4 = "/sdcard/eMMCTest_MB4_"
								+ create_count_max + ".txt";
						createFile = createFile(filePath_MB4, fileSize4,
								FileUnit.MB);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						LogUtil.e("StableEmmcActivity---------runOnUiThread------------");
						if (!createFile) {
							SystemProperties.set(
									Const.SYSPROPERTY_RUN_TEST_EMMC,
									Const.RUNIN_TEST_FAIL + "");
						}
						LogUtil.e("StableEmmcActivity---------runOnUiThread_test_time = "
								+ delete_count_max);
						for (int i = 0; i <= delete_count_max; i++) {
							delFile("/sdcard/eMMCTest_MB1_" + i + ".txt");
							delFile("/sdcard/eMMCTest_MB2_" + i + ".txt");
							delFile("/sdcard/eMMCTest_MB3_" + i + ".txt");
							delFile("/sdcard/eMMCTest_MB4_" + i + ".txt");
						}
					}
				});
				mHandler.sendEmptyMessage(STARTTEST);
			}
		});
		t.start();
	}

	private synchronized void closeThread() {
		try {
			notify();
			if (t != null && t.isAlive()) {
				t.interrupt();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void onPause() {
		super.onPause();
		LogUtil.e("onPauseStableEmmcActivity");
		mStartTest = false;
		closeThread();
		mHandler.removeCallbacks(mRunnable);
	}

	@Override
	protected void onStop() {
		super.onStop();
		LogUtil.e("onStopStableEmmcActivity");
		DataUtil.FlagALL_stable = true;
		for (int i = 0; i <= delete_count_max; i++) {
			delFile("/sdcard/eMMCTest_MB1_" + i + ".txt");
			delFile("/sdcard/eMMCTest_MB2_" + i + ".txt");
			delFile("/sdcard/eMMCTest_MB3_" + i + ".txt");
			delFile("/sdcard/eMMCTest_MB4_" + i + ".txt");
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		LogUtil.e("onDestroyStableEmmcActivity");
		if (wakeLock != null && wakeLock.isHeld()) {
			wakeLock.release();
			wakeLock = null;
		}
	}

	public static void delFile(String fileName) {
		File file = new File(fileName);
		if (file.isFile()) {
			LogUtil.e("StableEmmcActivity-----deleteFile-----------------");
			file.delete();
		}
		file.exists();
	}

	private long getAvailSpace(String path) {
		// 获取可用内存大小
		StatFs statfs = new StatFs(path);
		// 获取可用区块的个数
		long count = statfs.getAvailableBlocks();
		// 获取区块大小
		long size = statfs.getBlockSize();
		// 可用空间总大小
		return count * size;
	}

	public boolean createFile(String targetFile, long fileLength, FileUnit unit) {
		LogUtil.e("StableEmmcActivity-----createFile()-----------------");
		long KBSIZE = 1024;
		long MBSIZE1 = 1024 * 1024;
		long MBSIZE10 = 1024 * 1024 * 10;
		switch (unit) {
		case KB:
			fileLength = fileLength * 1024;
			break;
		case MB:
			fileLength = fileLength * 1024 * 1024;
			break;
		case GB:
			fileLength = fileLength * 1024 * 1024 * 1024;
			break;

		default:
			break;
		}
		FileOutputStream fos = null;
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
}
