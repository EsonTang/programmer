package com.prize.runoldtest.video;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Timer;
import java.util.TimerTask;

import com.prize.runoldtest.R;
import com.prize.runoldtest.camera.CameraTestActivity;
import com.prize.runoldtest.sleeprsm.SlpRsmActivity;
import com.prize.runoldtest.util.Const;
import com.prize.runoldtest.util.DataUtil;
import com.prize.runoldtest.util.LogToFile;
import com.prize.runoldtest.util.LogUtil;
import com.prize.runoldtest.util.OldTestResult;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.view.SurfaceHolder.Callback;
import android.widget.Toast;

@SuppressLint("HandlerLeak")
public class VideoActivity extends Activity {
	private long video_time;
	private SurfaceView videoSurfaceView;
	Context mContext;
	String dataPath;
	private String TAG = "RuninVedioTest";
	private PowerManager.WakeLock wakeLock = null;
	Message mMessage = new Message();
	TimerTask task = null;
	Timer timer = null;
	boolean isRunning = false;
	public Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 1:
				videoStart();
				break;
			default:
				break;
			}
		};
	};
	Thread mThread = new Thread(new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub

			mMessage.what = 1;
			mHandler.sendMessage(mMessage);
		}
	});

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_video);
		DataUtil.addDestoryActivity(VideoActivity.this, "VideoActivity");
		videoSurfaceView = (SurfaceView) findViewById(R.id.video_surface);
		PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
		wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, LogUtil.TAG);
		wakeLock.acquire();
		LogToFile.writeToFile(LogToFile.VERBOSE, TAG, "VideoTest begin.." + "\n");

		DataUtil.addDestoryActivity(VideoActivity.this, "VideoActivity");

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
				| WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		if (!isRunning) {
			mHandler.removeMessages(1);
			mMessage.what = 1;
			mHandler.sendMessage(mMessage);
			// mThread.start();
		}
		Intent intent = getIntent();
		video_time = intent.getIntExtra(Const.EXTRA_MESSAGE, 0);
		video_time = video_time * 60 * 1000;
		LogToFile.writeToFile(LogToFile.VERBOSE, TAG, "onstart VideoTest begin..video_time:" + video_time + "\n");
		task = new TimerTask() {
			public void run() {
				LogUtil.e("KEYCODE_BACKFlagCam");
				DataUtil.FlagCamera = true;
				OldTestResult.CameraTestresult = true;
				LogToFile.writeToFile(LogToFile.VERBOSE, TAG, "VideoActivity  finish.." + "\n");
				Intent intent = new Intent();
				// 把返回数据存入Intent
				intent.putExtra("result", "VideoTest:PASS");
				// 设置返回数据
				VideoActivity.this.setResult(RESULT_OK, intent);
				VideoActivity.this.finish();
			}
		};
		timer = new Timer();
		timer.schedule(task, video_time);
		LogUtil.e("zwl VideoActivity onCreate() isRunning = " + isRunning);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
		LogToFile.writeToFile(LogToFile.VERBOSE, TAG, "VideoTest onConfigurationChanged.." + "\n");
	}

	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		LogToFile.writeToFile(LogToFile.VERBOSE, TAG, "VideoActivity onWindowFocusChanged." + "\n");
		if (!DataUtil.FlagCamera && hasFocus && Build.VERSION.SDK_INT >= 19) {
			View decorView = getWindow().getDecorView();
			decorView.setSystemUiVisibility(
					View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
							| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
							| View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
		}
	}

	private int getScreenWith() {
		return getWindowManager().getDefaultDisplay().getWidth();
	}

	/*************************** video *****************************/
	public void toast(Object s) {
		if (s == null)
			return;
		Toast.makeText(this, s + "", Toast.LENGTH_SHORT).show();
	}

	private MediaPlayer mediaPlayer;

	private void videoStart() {
		videoSurfaceView.setVisibility(View.VISIBLE);
		videoSurfaceView.getHolder().addCallback(videoCallback);
		videoSurfaceView.getHolder().setFixedSize(getScreenWith(), 480);
		isRunning = true;
	}

	private Callback videoCallback = new Callback() {
		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			LogUtil.e("videoCallback surfaceCreated");
			play(0);
		}

		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
			LogUtil.e("videoCallback surfaceChanged");
		}

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			LogUtil.e("videoCallback surfaceDestroyed");
			if (mediaPlayer != null) {
				if (mediaPlayer.isPlaying()) {
					mediaPlayer.stop();
				}
				mediaPlayer.release();
			}
		}
	};

	private File copyVideo() {
		LogUtil.e("File file = null");
		File file = new File(getFilesDir(), "video.mp4");
		if (!file.exists()) {
			toast(getString(R.string.copy_video_notice));
			try {
				InputStream in = getAssets().open("video.mp4");
				OutputStream out = new FileOutputStream(file);
				byte[] buffer = new byte[1024];
				int len = 0;
				while ((len = in.read(buffer)) != -1) {
					out.write(buffer, 0, len);
				}
				out.close();
				in.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return file;
	}

	protected void play(final int msec) {
		File videoFile = copyVideo();
		if (videoFile.exists()) {
			LogToFile.writeToFile(LogToFile.VERBOSE, TAG, "videoFile::" + videoFile + "\n");
			dataPath = videoFile.getAbsolutePath().toString();
		} else {
			LogToFile.writeToFile(LogToFile.VERBOSE, TAG, "videoFile is null.." + "\n");
			toast(getString(R.string.no_vieofile_notice));
			DataUtil.FlagCamera = true;
			new Handler().postDelayed(new Runnable() {
				public void run() {
					VideoActivity.this.finish();
				}
			}, 2000);

		}
		File file = new File(dataPath);
		if (!file.exists()) {
			LogToFile.writeToFile(LogToFile.VERBOSE, TAG, "videoFile is not exist.." + "\n");
			Toast.makeText(this, "video file not find", 5).show();
			DataUtil.FlagCamera = true;
			new Handler().postDelayed(new Runnable() {
				public void run() {
					VideoActivity.this.finish();
				}
			}, 2000);

			return;
		}
		try {
			LogToFile.writeToFile(LogToFile.VERBOSE, TAG, "mediaPlayer is playing.." + "\n");
			mediaPlayer = new MediaPlayer();
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			mediaPlayer.setDataSource(file.getAbsolutePath());
			mediaPlayer.setLooping(true);
			mediaPlayer.setDisplay(videoSurfaceView.getHolder());
			mediaPlayer.prepareAsync();
			mediaPlayer.setOnPreparedListener(new OnPreparedListener() {
				@Override
				public void onPrepared(MediaPlayer mp) {
					mediaPlayer.start();
					mediaPlayer.seekTo(msec);
				}
			});
			mediaPlayer.setOnCompletionListener(new OnCompletionListener() {
				@Override
				public void onCompletion(MediaPlayer mp) {
				}
			});
			mediaPlayer.setOnErrorListener(new OnErrorListener() {
				@Override
				public boolean onError(MediaPlayer mp, int what, int extra) {
					play(0);
					return false;
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*************************** video *****************************/

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		LogToFile.writeToFile(LogToFile.VERBOSE, TAG, "VideoActivity  onDestroy.." + "\n");
		super.onDestroy();
		/*
		 * if(task!=null){ task.cancel(); } if(timer!=null){ timer.cancel(); }
		 * if(mThread!=null){ mThread.destroy(); }
		 */

	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		/*
		 * Intent intent = new Intent(); //把返回数据存入Intent
		 * intent.putExtra("result", "VideoTest:FAIL"); //设置返回数据
		 * VideoActivity.this.setResult(RESULT_OK, intent);
		 */
		if (wakeLock.isHeld()) {
			wakeLock.release();
		}

		DataUtil.finishBackPressActivity();
	}

}
