package com.prize.lockscreen.service;

import java.util.List;

import android.content.ComponentName;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaMetadata;
import android.media.session.MediaController;
import android.media.session.MediaSessionManager;
import android.media.session.PlaybackState;

import com.prize.lockscreen.bean.NoticeBean;
import com.prize.lockscreen.bean.NoticeInfo;
import com.prize.lockscreen.utils.LogUtil;
/***
 * 音乐通知辅助类
 * @author fanjunchen
 *
 */
public class MusicHelper {

	private MediaSessionManager mMediaSessionManager;
    private MediaController mMediaController = null;
    private String mMediaNotificationKey = null;
    private MediaMetadata mMediaMetadata;
    
    private ComponentName mCn;
    
    private List<MediaController> aList;
    
    /**消息标识, 延迟获取音乐状态*/
    private final int MSG_DELAY_FIND = 1;
    /**延迟处理时间 **/
    private final int DELAY_TIME = 400;
    
    private Context mCtx;
    
    private static MusicHelper instance = null;
    
    public static MusicHelper getInstance() {
    	return instance;
    }
    /**若是在其他场合这个会有用*/
    private MediaController.Callback mMediaListener
            = new MediaController.Callback() {
        @Override
        public void onPlaybackStateChanged(PlaybackState state) {
            super.onPlaybackStateChanged(state);
            
            LogUtil.i("MusicHelper", "fanjunchen===>" + state.toString());
        }

        @Override
        public void onMetadataChanged(MediaMetadata metadata) {
            super.onMetadataChanged(metadata);
            // 得到播放的歌曲信息 需要更新到UI上面去
            mMediaMetadata = metadata;
            // mMediaMetadata.getBitmap(MediaMetadata.METADATA_KEY_ART);
            
            String title = mMediaMetadata.getString(MediaMetadata.METADATA_KEY_TITLE);
            
            LogUtil.i("MusicHelper", "fanjunchen===>" + title);
        }
    };
    
    public MusicHelper(Context ctx) {
    	mCtx = ctx;
    	if (null == mMediaSessionManager)
	    	mMediaSessionManager
	        	= (MediaSessionManager) ctx.getSystemService(Context.MEDIA_SESSION_SERVICE);
    	if (null == mCn)
    		mCn = new ComponentName("com.prize.prizelockscreen", "com.prize.lockscreen.service.LockScreenNotificationListenerService");
    	instance = this;
    }
    /***
     * 找正在播放的媒体会话
     */
    private NoticeInfo getMusicNotification() {
    	
    	MediaController c = null;
    	
    	if (null == aList)
    		return null;
    	int sz = aList.size();
    	for (int i=0; i< sz; i++) {
    		MediaController aController = aList.get(i);
    		if (aController == null) continue;
            final PlaybackState state = aController.getPlaybackState();
            if (state == null) continue;
            int j = state.getState();
            switch (j) {
                case PlaybackState.STATE_STOPPED:
                case PlaybackState.STATE_ERROR:
                    continue;
                default:
                	LogUtil.i("MusicHelper", "fanjunchen===>state=" + j);
                	c = aController;
                	break;
            }
            LogUtil.i("MusicHelper", "fanjunchen===>sz====" + sz + ";===i==" + i);
    	}
    	
    	if (!sameSessions(mMediaController, c)) {
//    		if (mMediaController != null) {
//                mMediaController.unregisterCallback(mMediaListener);
//            }
            mMediaController = c;
            
            //mMediaController.registerCallback(mMediaListener);
            try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
            
            PlaybackState state = mMediaController.getPlaybackState();
            LogUtil.i("MusicHelper", "fanjunchen===>state=" + state);
            
            NoticeInfo ninfo = new NoticeInfo();
            ninfo.type = NoticeInfo.MUSIC;
            mMediaMetadata = mMediaController.getMetadata();
            ninfo.packageName = mMediaController.getPackageName();
            ninfo.id = 0;
            
            NoticeBean nb = new NoticeBean();
            
            // 专集图
            Bitmap bitmap = mMediaMetadata.getBitmap(MediaMetadata.METADATA_KEY_ART);
            if (bitmap != null && mCtx != null) {
            	BitmapDrawable d = new BitmapDrawable(mCtx.getResources(), bitmap);
            	nb.appIcon = d;
            }
            // 歌名
            String title = mMediaMetadata.getString(MediaMetadata.METADATA_KEY_TITLE);
            nb.title = title;
            // 演唱者
            nb.text = mMediaMetadata.getString(MediaMetadata.METADATA_KEY_ARTIST);
            // PendingIntent
            nb.contentIntent = mMediaController.getSessionActivity();
            nb.status = state.getState();
            
            ninfo.setNoticeBean(nb);
            
            LogUtil.i("MusicHelper", "fanjunchen===>" + title);
            // add notification
            //KeyEvent key = new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE);
            //mMediaController.dispatchMediaButtonEvent(key);
            
            return ninfo;
    	}
    	return null;
    } 
    /**
     * 播放/暂停
     */
    public void playOrPause() {
    	if (mMediaController != null) {
//	    	KeyEvent key = new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE);
//	        mMediaController.dispatchMediaButtonEvent(key);
	        MediaController.TransportControls a = mMediaController.getTransportControls();
	        if (a != null && mMediaController.getPlaybackState().getState() == PlaybackState.STATE_PLAYING)
	        	a.pause();
	        else if (a != null) {
	        	a.play();
	        }
    	}
    }
    /**
     * 上一曲
     */
    public void previous() {
    	if (mMediaController != null) {
//	    	KeyEvent key = new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PREVIOUS);
//	        mMediaController.dispatchMediaButtonEvent(key);
    		MediaController.TransportControls a = mMediaController.getTransportControls();
	        if (a != null)
	        	a.skipToPrevious();
    	}
    }
    /**
     * 下一曲
     */
    public void next() {
    	if (mMediaController != null) {
//	    	KeyEvent key = new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_NEXT);
//	        mMediaController.dispatchMediaButtonEvent(key);
    		MediaController.TransportControls a = mMediaController.getTransportControls();
	        if (a != null)
	        	a.skipToNext();
    	}
    }
    /***
     * 获取音乐通知
     */
    public NoticeInfo findMediaMusic() {
    	System.out.println("fanjunchen");
    	aList = mMediaSessionManager.getActiveSessions(mCn);
    	return getMusicNotification();
//    	mHandle.removeMessages(MSG_DELAY_FIND);
//    	Message msg = mHandle.obtainMessage(MSG_DELAY_FIND);
//    	mHandle.sendMessageDelayed(msg, DELAY_TIME);
    }
    /***
     * 判断是否为同一个会话,若是同属于更新即可
     * @param a
     * @param b
     * @return
     */
    private boolean sameSessions(MediaController a, MediaController b) {
        if (a == b) return true;
        if (a == null) return false;
        return false;
    }
    
    /*private final Handler mHandle = new Handler() {
    	@Override
    	public void handleMessage(Message msg) {
    		switch (msg.what) {
	    		case MSG_DELAY_FIND:
	    			getMusicNotification();
	    			break;
    		}
    	}
    };*/
}
