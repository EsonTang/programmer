package com.prize.factorytest;

import android.app.Activity;    
import android.media.MediaPlayer;    
import android.media.MediaPlayer.OnCompletionListener;    
import android.media.MediaPlayer.OnErrorListener;    
import android.media.MediaPlayer.OnPreparedListener;     
import android.os.Bundle;    
import android.os.Environment;    
import android.os.Message;
import android.util.Log;    
import android.view.Display;    
import android.view.SurfaceHolder;    
import android.view.SurfaceView;    
import android.widget.LinearLayout;       
import android.widget.FrameLayout;     
import android.hardware.Camera.Parameters; 
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera;
import android.graphics.PixelFormat;
import android.widget.Toast;
import android.view.MotionEvent;
import android.view.View;
import android.content.Context;
import android.hardware.Camera.Size;
import android.util.DisplayMetrics;
import android.content.Intent;
import android.media.AudioManager;
import android.view.WindowManager;
import android.os.Handler;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.Vibrator;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.SystemClock;
import android.widget.TextView;
import android.os.CountDownTimer;
import android.view.SurfaceHolder.Callback;
import android.widget.MediaController;
import android.widget.Button;

import com.prize.factorytest.CameraBack.CameraBack;
import com.prize.factorytest.util.AgingTestTimer;
import com.prize.factorytest.util.SupportedSizesReflect;
import com.prize.factorytest.util.AgingTestItems;

import java.io.IOException;    
import java.util.HashMap;
import java.util.List;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class AgingTestImplActivity extends Activity{   
	private static final String TAG = "AgingTestImplActivity";
	public static final int SWITCH_TO_NEXT_ITEM = 0;
	public static final int TICK_REFRESH = 1;
	public static final int FOR_TEST = 2;
	
	public static final int NONE_PARAMETER=-1;
	public static final int VEDIO_PARAMETER=0;
	public static final int CAMERA_PARAMETER=1;
	
	private static final String KEY_VIDEO_SPEAKER_DURATION="video_speaker_duration";
	private static final String KEY_VIDEO_RECEIVER_DURATION="video_receiver_duration";
	private static final String KEY_VIBRATE_DURATION="vibrate_duration";
	private static final String KEY_MIC_LOOP_DURATION="mic_loop_duration";
	private static final String KEY_FRONT_CAMERA_DURATION="front_camera_duration";
	private static final String KEY_BACK_CAMERA_DURATION="back_camera_duration";
	
	private static final String KEY_REBOOT_RESULT = "reboot_result";
	private static final String KEY_SLEEP_RESULT = "sleep_result";
	private static final String KEY_FAIL_RESULT = "fail";
	private static final String KEY_PASS_RESULT = "pass";
	
    private SurfaceView videoSurfaceView;    
	private SurfaceView cameraSurfaceView;    
    private SurfaceHolder camera_holder;  	   
    private Camera mCamera = null;        
	private Camera.Parameters cameraParameters;
			
	private int cameraPosition = 1;
	private boolean isCameraBack=true;
	
	private AudioManager mAudioManager;
	
	private Handler mHandler = new Handler();
	private final long VIBRATOR_ON_TIME = 1000;
	private final long VIBRATOR_OFF_TIME = 500;
	Vibrator mVibrator = null;
	PowerManager powerManager = null;
	WakeLock wakeLock = null;
	long[] pattern = { VIBRATOR_OFF_TIME, VIBRATOR_ON_TIME };
	
	boolean flag = false;
	private AudioRecord m_record;
	private AudioTrack m_track;
	private int bufferSize;
	private byte[] buffer;
	
	private FactoryTestApplication app;
	private AgingTestItems gAgingTestItems;
	private AgingTestTimer mAgingTestTimer;
	
	private long videoSpeakerStart=0;
	private long videoSpeakerElapseTime =0;
	private long videoReceiverStart=0;
	private long videoReceiverElapseTime =0;
	private long vibrateStart=0;
	private long vibrateElapseTime =0;
	private long micLoopStart=0;
	private long micElapseTime =0;
	private long frontCameraStart=0;
	private long frontCameraElapseTime =0;
	private long backCameraStart=0;
	private long backCameraElapseTime =0;
	
	private long gDuration =0;
	private TextView elapsetime_tv;
	
	Context mContext;
	String dataPath;
	
	private Object mFaceDetectionSync = new Object();
    public void onCreate(Bundle savedInstanceState){    
        super.onCreate(savedInstanceState);            
        setContentView(R.layout.agingtest_impl);   
        keepScreenOn();		
		initViews();  
		mContext=this;		
		
		app = (FactoryTestApplication) getApplication();		
		gAgingTestItems=getAgingTestItems();
		gDuration=getDuration();	
		startMain();
    }
    private void initViews(){
		cameraSurfaceView = (SurfaceView)findViewById(R.id.camera_surface);                
        videoSurfaceView = (SurfaceView)findViewById(R.id.video_surface);   
		elapsetime_tv = (TextView)findViewById(R.id.elapsetimeshow_tv);   
	}
	private AgingTestItems getAgingTestItems(){
		Intent intent = getIntent();  
		AgingTestItems mAgingTestItems = (AgingTestItems)intent.getSerializableExtra("AGINGTEST_ITEMS");  
		return mAgingTestItems;
	}
	private long getDuration(){
		Intent intent = getIntent();  
		return intent.getLongExtra("AGINGTEST_DURATION",0);
	}
	private void keepScreenOn(){
		powerManager = (PowerManager) this
					.getSystemService(Context.POWER_SERVICE);
		wakeLock = this.powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK,
					"My Lock");
		wakeLock.acquire();
	}
	private void startMain() {
		// TODO Auto-generated method stub
		if(gAgingTestItems!=null){
			mAgingTestTimer = new AgingTestTimer(gDuration,mTickCallBack,mTimeUpCallBack);
			pickOneEnter(gAgingTestItems.getNextTestItem(-1),NONE_PARAMETER);
		}
	}
	private void pickOneEnter(int id,int parameter){
		Log.d(TAG,"pickOneEnter id="+id+",parameter="+parameter);
		final int testId = id;
		switch(testId){
			case AgingTestItems.VIDEO_SPEAKER:
				videoSpeakerStart=SystemClock.elapsedRealtime();
				setAudio2Speaker();				
				videoSurfaceView.setVisibility(View.VISIBLE);
				videoStart();								
				mAgingTestTimer.start(testId,videoSpeakerStart);
				break;
			case AgingTestItems.VIDEO_RECEIVER:				
				setAudio2Receiver();
				videoReceiverStart=SystemClock.elapsedRealtime();
				if(parameter!=VEDIO_PARAMETER){					
					videoSurfaceView.setVisibility(View.VISIBLE);
					videoStart();	
				}						
				mAgingTestTimer.start(testId,videoReceiverStart);
				break;
			case AgingTestItems.VIBRATE:				
				vibrateStart=SystemClock.elapsedRealtime();
				mVibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
				mHandler.postDelayed(mRunnable, 0);
				mAgingTestTimer.start(testId,vibrateStart);
				break;
			case AgingTestItems.MIC_LOOP:						
				startMicLoop();
				micLoopStart=SystemClock.elapsedRealtime();				
				mAgingTestTimer.start(testId,micLoopStart);
				break;
			case AgingTestItems.FRONT_CAMERA:				
				frontCameraStart=SystemClock.elapsedRealtime();
				cameraSurfaceView.setVisibility(View.VISIBLE);
				isCameraBack=false;
				cameraPosition = 0;
				camera_holder = cameraSurfaceView.getHolder();
				camera_holder.addCallback(cameraCallback);
				camera_holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
				mAgingTestTimer.start(testId,frontCameraStart);
				break;
			case AgingTestItems.BACK_CAMERA:				
				backCameraStart=SystemClock.elapsedRealtime();
				if(parameter==CAMERA_PARAMETER){
					switchCamera();
				}else{
					cameraSurfaceView.setVisibility(View.VISIBLE);
					isCameraBack=true;
					cameraPosition = 1;
					camera_holder = cameraSurfaceView.getHolder();
					camera_holder.addCallback(cameraCallback);
					camera_holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
				}				
				mAgingTestTimer.start(testId,backCameraStart);
				break;
			default:		
				if(mAgingTestTimer!=null){
					mAgingTestTimer.cancel();
					mAgingTestTimer=null;
				}
				//add liup aging recovery backup
				String duration_tv_bak = getLastDuration();
				String reboot_tv_bak = getText(KEY_REBOOT_RESULT);
				String sleep_tv_bak = getText(KEY_SLEEP_RESULT);
				String aging_result_text = duration_tv_bak + "-" + reboot_tv_bak + "-" + sleep_tv_bak;
				writeFile(aging_result_text);
				//add liup aging recovery backup
				
				app.getSharePref().putValue("reboot_selected", "0");
				app.getSharePref().putValue("sleep_selected", "0");
				
				
				finish();
				break;
		}					
	}
	
	private String getText(String key){
		String result;
		String r=new String();
		if(key.equals(KEY_REBOOT_RESULT)){
			result =app.getSharePref().getValue(KEY_REBOOT_RESULT);
			if(result.equals(KEY_PASS_RESULT)){
				r=getString(R.string.serial_agingtest_reboot)+":pass";
			}else if(result.equals(KEY_FAIL_RESULT)){
				r=getString(R.string.serial_agingtest_reboot)+":fail";
			}else{
				r=getString(R.string.serial_agingtest_reboot)+":untest";
			}
		}
		if(key.equals(KEY_SLEEP_RESULT)){
			result =app.getSharePref().getValue(KEY_SLEEP_RESULT);
			if(result.equals(KEY_PASS_RESULT)){
				r=getString(R.string.serial_agingtest_sleep)+":pass";
			}else if(result.equals(KEY_FAIL_RESULT)){
				r=getString(R.string.serial_agingtest_sleep)+":fail";
			}else{
				r=getString(R.string.serial_agingtest_sleep)+":untest";
			}
		}
		return r;
	}
	
	private void writeFile(String data) {
		try {
			FileOutputStream fout = new FileOutputStream("/data/prize_backup/prize_factory_data");
			byte[] bytes = data.getBytes();
			fout.write(bytes);
			fout.flush();				
			fout.close();
			Log.e("liup","writeFile succcess");
		} catch (Exception e) {
		}
	}
	
	private String getLastDuration(){
		StringBuilder duration = new StringBuilder();
		duration.append(getString(R.string.last_duration_detail)+"\n");
		duration.append(AgingTestActivity.agingtest_items[0]+":"+app.getSharePref().getValue("video_speaker_duration")+"\n");
		duration.append(AgingTestActivity.agingtest_items[1]+":"+app.getSharePref().getValue("video_receiver_duration")+"\n");
		duration.append(AgingTestActivity.agingtest_items[2]+":"+app.getSharePref().getValue("vibrate_duration")+"\n");
		duration.append(AgingTestActivity.agingtest_items[3]+":"+app.getSharePref().getValue("mic_loop_duration")+"\n");
		duration.append(AgingTestActivity.agingtest_items[4]+":"+app.getSharePref().getValue("front_camera_duration")+"\n");
		duration.append(AgingTestActivity.agingtest_items[5]+":"+app.getSharePref().getValue("back_camera_duration")+"\n");
		return duration.toString();
	}
	
	private void exitCurrentPickOneEnter(int current,boolean destroy){	
		Log.d(TAG,"exitCurrentPickOneEnter current="+current+",destroy="+destroy);
		final int currentId=current;
		final int nextId;
		if(destroy){
			nextId=-1;
		}else{
			nextId=gAgingTestItems.getNextTestItem(currentId);
		}		
		switch(currentId){
			case AgingTestItems.VIDEO_SPEAKER:
				videoSpeakerElapseTime = SystemClock.elapsedRealtime()-videoSpeakerStart;
				calculaterDuration(KEY_VIDEO_SPEAKER_DURATION,videoSpeakerElapseTime);
				if(nextId==AgingTestItems.VIDEO_RECEIVER){					
					pickOneEnter(nextId,VEDIO_PARAMETER);
				}else{										
					videoSurfaceView.setVisibility(View.GONE);
					setAudio2Speaker();
					pickOneEnter(nextId,NONE_PARAMETER);
				}			
				break;
			case AgingTestItems.VIDEO_RECEIVER:
				videoReceiverElapseTime = SystemClock.elapsedRealtime()-videoReceiverStart;		
				calculaterDuration(KEY_VIDEO_RECEIVER_DURATION,videoReceiverElapseTime);
				videoSurfaceView.setVisibility(View.GONE);
				setAudio2Speaker();
				pickOneEnter(nextId,NONE_PARAMETER);
				break;
			case AgingTestItems.VIBRATE:	
				vibrateElapseTime= SystemClock.elapsedRealtime()-vibrateStart;	
				calculaterDuration(KEY_VIBRATE_DURATION,vibrateElapseTime);
				if(mVibrator!=null&&mHandler!=null){
					mHandler.removeCallbacks(mRunnable);
					mVibrator.cancel();
				}
				pickOneEnter(nextId,NONE_PARAMETER);
				break;
			case AgingTestItems.MIC_LOOP:
				micElapseTime= SystemClock.elapsedRealtime()-micLoopStart;
				calculaterDuration(KEY_MIC_LOOP_DURATION,micElapseTime);
				flag = false;				
				setAudio2Speaker();
				pickOneEnter(nextId,NONE_PARAMETER);
				break;
			case AgingTestItems.FRONT_CAMERA:
				frontCameraElapseTime = SystemClock.elapsedRealtime()-frontCameraStart;
				calculaterDuration(KEY_FRONT_CAMERA_DURATION,frontCameraElapseTime);
				if(nextId==AgingTestItems.BACK_CAMERA){
					pickOneEnter(nextId,CAMERA_PARAMETER);
				}else{
					cameraSurfaceView.setVisibility(View.GONE);
					pickOneEnter(nextId,NONE_PARAMETER);
				}				
				break;
			case AgingTestItems.BACK_CAMERA:	
				backCameraElapseTime= SystemClock.elapsedRealtime()-backCameraStart;
				calculaterDuration(KEY_BACK_CAMERA_DURATION,backCameraElapseTime);
				cameraSurfaceView.setVisibility(View.GONE);
				pickOneEnter(nextId,NONE_PARAMETER);
				break;
			default:
				break;
		}
	}

	AgingTestTimer.TickCallBack mTickCallBack = new AgingTestTimer.TickCallBack(){
		@Override
		public void showElapseTime(int current,long startTime) {
			// TODO Auto-generated method stub
			String s=AgingTestActivity.agingtest_items[current]+":"+elapse2String(SystemClock.elapsedRealtime()-startTime);
			Message msg = handler.obtainMessage(TICK_REFRESH);
			msg.obj=s;
			handler.sendMessage(msg);		
		}
	};
	AgingTestTimer.TimeUpCallBack mTimeUpCallBack = new AgingTestTimer.TimeUpCallBack(){
		@Override
		public void goToNextItem(int current) {
			// TODO Auto-generated method stub
			Message msg = handler.obtainMessage(SWITCH_TO_NEXT_ITEM);
			msg.arg1 = current;
			handler.sendMessage(msg);			
		}
	};
	private Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub 
			switch(msg.what){
				case SWITCH_TO_NEXT_ITEM:
					Log.d(TAG,"SWITCH_TO_NEXT_ITEM current : "+msg.arg1);
					exitCurrentPickOneEnter(msg.arg1,false);
					break;
				case TICK_REFRESH:
					elapsetime_tv.setText((String)msg.obj);
					break;
				case FOR_TEST:
					break;
				default:
					break;
			}
			super.handleMessage(msg);
		}
	};
		
	/***************************video*****************************/
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
		File file = new File(mContext.getFilesDir(), "video.mp4");
		if(!file.exists()){
			toast(getString(R.string.copy_video_notice));
			try {
				InputStream in = mContext.getAssets().open("video.mp4");
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
	/***************************audio*****************************/
	private void setAudio2Speaker(){
		mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		mAudioManager.setMode(AudioManager.MODE_NORMAL);
	}
	private void setAudio2Receiver(){
		mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
				mAudioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM) - 3, 0);
		mAudioManager.setSpeakerphoneOn(false);
		/*
		setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
		mAudioManager.setMode(AudioManager.MODE_IN_CALL);
		*/
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		//mAudioManager.setMode(AudioManager.MODE_NORMAL);
		mAudioManager.setMode(AudioManager.MODE_IN_CALL);
		//mAudioManager.setParameter("SetPrizeReceiverVolume","0");
	}
	/***************************audio*****************************/
	/***************************vibrate*****************************/
	private Runnable mRunnable = new Runnable() {
		public void run() {
			mHandler.removeCallbacks(mRunnable);
			mVibrator.vibrate(pattern, 0);
		}
	};
	/***************************vibrate*****************************/
	/***************************micloop*****************************/
	private void startMicLoop(){
		mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		mAudioManager.setSpeakerphoneOn(false);
		setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
		mAudioManager.setMode(AudioManager.MODE_IN_CALL);
		new Thread(){
			public void run() {
				flag = true;				
				startMicLoopServer();
			}
		}.start();
	}
	private void startMicLoopServer() {
		int SAMPLE_RATE = 8000;
		int BUF_SIZE = 1024;
		bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE,
				AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
		bufferSize = Math.max(bufferSize, AudioTrack.getMinBufferSize(
				SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO,
				AudioFormat.ENCODING_PCM_16BIT));
		bufferSize = Math.max(bufferSize, BUF_SIZE);

		buffer = new byte[bufferSize];

		m_record = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE,
				AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT,
				bufferSize);

		m_track = new AudioTrack(AudioManager.STREAM_VOICE_CALL, SAMPLE_RATE,
				AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT,
				bufferSize, AudioTrack.MODE_STREAM);

		m_track.setPlaybackRate(SAMPLE_RATE);
		m_record.startRecording();
		m_track.play();

		while (flag) {
			int readSize = m_record.read(buffer, 0, bufferSize);
			if (readSize > 0) {
				m_track.write(buffer, 0, readSize);
			}
		}
		m_track.stop();
        m_record.stop();
	}		
	/***************************micloop*****************************/
	/***************************camera*****************************/
	private SurfaceHolder.Callback	cameraCallback = new SurfaceHolder.Callback(){			    
		@Override    
		public void surfaceCreated(SurfaceHolder holder) {  
			Log.v(TAG, "cameraCallback surfaceCreated");
			openCamera();           
		}    
		@Override    
		public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {       
			Log.v(TAG, "cameraCallback surfaceChanged called arg2="+arg2+" arg3="+arg3);    
			setCameraParameters(arg2,arg3);
		}
		@Override    
		public void surfaceDestroyed(SurfaceHolder holder) {                
			Log.v(TAG, "cameraCallback surfaceDestroyed called");   
			if (mCamera != null) {
				try {
					holder.removeCallback(this);
					mCamera.setPreviewCallback(null);
					mCamera.stopPreview();
					mCamera.release();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	};
        
	private void openCamera(){
		try {
			if(isCameraBack){
				mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);				
			}else{
				mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);				
			}
		} catch (Exception exception) {
			toast(getString(R.string.cameraback_fail_open));
			mCamera = null;
		}
		if (mCamera == null) {
			finish();
		} else {
			try {
				mCamera.setPreviewDisplay(camera_holder);
			} catch (IOException exception) {
				mCamera.release();
				mCamera = null;
				finish();
			}
		}		
	}	
	private void setCameraParameters(int w,int h){
		if (mCamera != null) {
			try {
				synchronized (mFaceDetectionSync) {
					cameraParameters = mCamera.getParameters();					
					Size pictureSize=null;
					Size previewSize=null;
					List<Size> supportedPictureSizes
					= SupportedSizesReflect.getSupportedPictureSizes(cameraParameters);
					List<Size> supportedPreviewSizes
					= SupportedSizesReflect.getSupportedPreviewSizes(cameraParameters);
					if ( supportedPictureSizes != null &&
						supportedPreviewSizes != null &&
						supportedPictureSizes.size() > 0 &&
						supportedPreviewSizes.size() > 0) {
						//2.x
						pictureSize = supportedPictureSizes.get(0);
						int maxSize = 1280;
						if(maxSize > 0){
							for(Size size : supportedPictureSizes){
								if(maxSize >= Math.max(size.width,size.height)){
									pictureSize = size;
									break;
								}
							}
						}
						WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
						Display display = windowManager.getDefaultDisplay();
						DisplayMetrics displayMetrics = new DisplayMetrics();
						display.getMetrics(displayMetrics);

						previewSize = getOptimalPreviewSize(
											supportedPreviewSizes,
											display.getWidth(),
											display.getHeight()); 
						cameraParameters.setPictureSize(pictureSize.width, pictureSize.height);
						Log.e(TAG, "setCameraParameters:pictureSize.width="+pictureSize.width+" pictureSize.height="+pictureSize.height);
						cameraParameters.setPreviewSize(previewSize.width, previewSize.height);								
						Log.e(TAG, "setCameraParameters:previewSize.width="+previewSize.width+" previewSize.height="+previewSize.height);
					}
					cameraParameters.setPictureFormat(PixelFormat.JPEG);
					cameraParameters.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
					if (cameraParameters.getZSDMode().equals(CameraBack.ZSD_MODE_ON)) {
						cameraParameters.setZSDMode(CameraBack.ZSD_MODE_OFF);					
					}
					mCamera.setParameters(cameraParameters);
					mCamera.setDisplayOrientation(90);
					mCamera.startPreview();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}	
	private int getScreenWith(){
		return getWindowManager().getDefaultDisplay().getWidth(); 
	}
	private int getScreenHeight(){
		return getWindowManager().getDefaultDisplay().getHeight(); 
	}
	private Size getOptimalPreviewSize(List<Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) w / h;
        if (sizes == null) return null;
        Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;
        int targetHeight = h;
        // Try to find an size match aspect ratio and size
        for (Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }
        // Cannot find the one match the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }
	private void switchCamera(){
		if(isCameraBack){			
			isCameraBack=false;
		}else{			
			isCameraBack=true;
		}
		int cameraCount = 0;
		CameraInfo cameraInfo = new CameraInfo();
	    cameraCount = Camera.getNumberOfCameras();
		for(int i = 0; i < cameraCount; i++) {	                	
			Camera.getCameraInfo(i, cameraInfo);
			if(cameraPosition == 1) {
				if(cameraInfo.facing  == Camera.CameraInfo.CAMERA_FACING_FRONT) {
					if (mCamera != null) {
						try {
							//camera_holder.removeCallback(cameraCallback);
							mCamera.setPreviewCallback(null);
							mCamera.stopPreview();
							mCamera.release();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					mCamera = null;
					mCamera = Camera.open(i);
					try {
						synchronized (mFaceDetectionSync) {
							setCameraParameters(getScreenWith(),getScreenHeight());
							mCamera.setPreviewDisplay(camera_holder);
							mCamera.startPreview();
						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					cameraPosition = 0;
					break;
				}
			} else {				
				if(cameraInfo.facing  == Camera.CameraInfo.CAMERA_FACING_BACK) {
					if (mCamera != null) {
						try {
							//camera_holder.removeCallback(cameraCallback);
							mCamera.setPreviewCallback(null);
							mCamera.stopPreview();
							mCamera.release();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					mCamera = null;
					mCamera = Camera.open(i);
					try {
						synchronized (mFaceDetectionSync) {
							setCameraParameters(getScreenWith(),getScreenHeight());
							mCamera.setPreviewDisplay(camera_holder);
							mCamera.startPreview();
						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					cameraPosition = 1;
					break;
				}
			}
		}
	}
	/***************************camera*****************************/
	public void toast(Object s) {
		if (s == null)
			return;
		Toast.makeText(this, s + "", Toast.LENGTH_SHORT).show();
	}
	private String elapse2String(long elapseRealtime){
		long tmp=elapseRealtime/1000;
		long hours=tmp/3600;
		long minutes=(tmp-hours*3600)/60;
		long seconds=tmp-hours*3600-minutes*60;		
		String ret=String.format("%02d:%02d:%02d",hours,minutes,seconds);  
		return ret;
	}
	private void calculaterDuration(String key,long value){
		Log.d(TAG,"calculaterDuration "+key+","+value);
		app.getSharePref().putValue(key, elapse2String(value));
	}	
	private void calculaterDuration(int id){
		Log.d(TAG,"calculaterDuration id = "+id);
		switch(id){
			case AgingTestItems.VIDEO_SPEAKER:
				app.getSharePref().putValue(KEY_VIDEO_SPEAKER_DURATION, elapse2String(videoSpeakerElapseTime));
				break;
			case AgingTestItems.VIDEO_RECEIVER:
				app.getSharePref().putValue(KEY_VIDEO_RECEIVER_DURATION, elapse2String(videoReceiverElapseTime));
				break;
			case AgingTestItems.VIBRATE:
				app.getSharePref().putValue(KEY_VIBRATE_DURATION, elapse2String(vibrateElapseTime));
				break;
			case AgingTestItems.MIC_LOOP:
				app.getSharePref().putValue(KEY_MIC_LOOP_DURATION, elapse2String(micElapseTime));
				break;
			case AgingTestItems.FRONT_CAMERA:
				app.getSharePref().putValue(KEY_FRONT_CAMERA_DURATION, elapse2String(frontCameraElapseTime));
				break;
			case AgingTestItems.BACK_CAMERA:
				app.getSharePref().putValue(KEY_BACK_CAMERA_DURATION, elapse2String(backCameraElapseTime));
				break;
		}
	}	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		Log.v(TAG, "onBackPressed"); 
		//add liup aging recovery backup
		String duration_tv_bak = getLastDuration();
		String reboot_tv_bak = getText(KEY_REBOOT_RESULT);
		String sleep_tv_bak = getText(KEY_SLEEP_RESULT);
		String aging_result_text = duration_tv_bak + "-" + reboot_tv_bak + "-" + sleep_tv_bak;
		writeFile(aging_result_text);
		//add liup aging recovery backup
		
		app.getSharePref().putValue("reboot_selected", "0");
		app.getSharePref().putValue("sleep_selected", "0");
				
		if(mAgingTestTimer!=null){
			exitCurrentPickOneEnter(mAgingTestTimer.getCurrent(),true);
		}

		super.onBackPressed();
	}
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub	
		Log.v(TAG, "onPause");  
		//calculaterDuration(mAgingTestTimer.getCurrent());
		super.onPause();
	}
	@Override    
	protected void onDestroy() {        
		// TODO Auto-generated method stub   
		Log.v(TAG, "onDestroy");  
		//cancel keep screen on
		if(wakeLock!=null){
			wakeLock.release();
		}				
		super.onDestroy();   
    }
}