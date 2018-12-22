package com.prize.runoldtest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Timer;
import java.util.TimerTask;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
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
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.SurfaceHolder.Callback;
import android.widget.RelativeLayout;
import android.widget.Toast;

@SuppressLint("HandlerLeak") 
public class VideoActivity extends Activity {
	private static final String TAG = "VideoActivity"; 
    private long video_time = 1;
    private SurfaceView videoSurfaceView;
    private RelativeLayout videoRelativeLayout;
    Context mContext;
    String dataPath;
    
    public Handler mHandler = new Handler() {
    	public void handleMessage(android.os.Message msg) {
    		Log.i("Ganxiayong", "--------msg = " + msg.what + " --------");
    		switch (msg.what) {
			case 1:
				videoStart();
				Log.i("Ganxiayong", "--------videoStart()--------");
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
			Message mMessage = new Message();
			mMessage.what = 1;
			mHandler.sendMessage(mMessage);
		}
	});
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        videoSurfaceView = (SurfaceView)findViewById(R.id.video_surface);
        videoRelativeLayout = (RelativeLayout) findViewById(R.id.video_rl);
//        videoRelativeLayout.setSystemUiVisibility(View.INVISIBLE);
        PowerManager pm = (PowerManager)getSystemService(POWER_SERVICE);
        WakeLock wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK| PowerManager.ACQUIRE_CAUSES_WAKEUP, TAG);
        wakeLock.acquire();
        mThread.start();
        Log.e("Ganxiayong", "--------mThread.start()--------");
    }
    
    @Override
    protected void onPause() {
    	// TODO Auto-generated method stub
    	super.onPause();
//    	mThread.destroy();
    }
    
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
    	// TODO Auto-generated method stub
    	super.onWindowFocusChanged(hasFocus);
    	if(hasFocus && Build.VERSION.SDK_INT >= 19){
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    |View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    |View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    |View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    |View.SYSTEM_UI_FLAG_FULLSCREEN
                    |View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            );
        }
    }
    
    private int getScreenWith(){
		return getWindowManager().getDefaultDisplay().getWidth(); 
	}
    
    /***************************video*****************************/
	public void toast(Object s) {
		if (s == null)
			return;
		Toast.makeText(this, s + "", Toast.LENGTH_SHORT).show();
	}

    private MediaPlayer mediaPlayer;
	private void videoStart(){
		videoSurfaceView.setVisibility(View.VISIBLE);
		videoSurfaceView.getHolder().addCallback(videoCallback);		
		videoSurfaceView.getHolder().setFixedSize(getScreenWith(), 480);
	}
	
	private Callback videoCallback = new Callback() {		
		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			Log.i(TAG, "videoCallback surfaceCreated");
			play(0);
		}
		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width,int height) {
			Log.i(TAG, "videoCallback surfaceChanged");
		}
		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			Log.i(TAG, "videoCallback surfaceDestroyed");
			if(mediaPlayer!=null){
				if(mediaPlayer.isPlaying()){
					mediaPlayer.stop();
				}
				mediaPlayer.release();
			}	
		}
	};
	private File copyVideo(){
		Log.i(TAG, "File file = null");
		File file = new File(getFilesDir(), "Beyond.mp4");
		if(!file.exists()){
			toast(getString(R.string.copy_video_notice));
			try {
				InputStream in = getAssets().open("Beyond.mp4");
				OutputStream out = new FileOutputStream(file);
				byte[] buffer = new byte[1024];
				int len = 0;
				while((len = in.read(buffer)) != -1){
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
		File videoFile=copyVideo();
		if(videoFile != null){
			dataPath = videoFile.getAbsolutePath().toString();
		}else{
			toast(getString(R.string.no_vieofile_notice));
			finish();
		}	
		File file = new File(dataPath);
		if (!file.exists()) {
			Toast.makeText(this, "video file not find", 0).show();
			return;
		}
		try {
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
	/***************************video*****************************/

    TimerTask task = new TimerTask(){
        public void run(){
            try {
                ManualTestActivity.FlagMc = true;
                Runtime.getRuntime().exec("input keyevent " + KeyEvent.KEYCODE_BACK);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    };
    
    Timer timer = new Timer();
    
    protected void onStart() {
        super.onStart();
        timer.schedule(task, video_time*300000);
    }
}
