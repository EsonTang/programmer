/*****************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：歌词秀
 *当前版本：V1.0
 *作  者：longbaoxiu
 *完成日期：2015-7-21
 *修改记录：
 *修改日期：
 *版 本 号：
 *修 改 人：
 *修改内容：
 ********************************************/
package com.prize.music.lyric;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.prize.app.beans.LyricSentence;
import com.prize.app.constants.Constants;
import com.prize.app.constants.RequestMethods;
import com.prize.app.lyric.LyricDownloadManager;
import com.prize.app.lyric.LyricLoadHelper;
import com.prize.app.lyric.LyricLoadHelper.LyricListener;
import com.prize.app.util.JLog;
import com.prize.app.util.SDKUtil;
import com.prize.app.util.ToastUtils;
import com.prize.music.R;
import com.prize.music.activities.AudioPlayerActivity;
import com.prize.music.base.BaseFragment;
import com.prize.music.database.DatabaseConstant;
import com.prize.music.database.MusicInfo;
import com.prize.music.helpers.utils.LogUtils;
import com.prize.music.helpers.utils.MusicUtils;
import com.prize.music.online.util.PrizeParesNetData;
import com.prize.onlinemusibean.SongDetailInfo;
import com.xiami.sdk.XiamiSDK;

/**
 * 歌词秀
 *
 * @author longbaoxiu
 * @version v1.0
 */
public class LyricFragment extends BaseFragment {
	private final String TAG = "LyricFragment";
	/** 歌词是否正在下载 */
	private boolean mIsLyricDownloading;
	private View layoutView;
	private ListView mLrcListView;
	private LyricAdapter mLyricAdapter;
	// private TextView mLrcEmptyView;
	private  LyricLoadHelper mLyricLoadHelper;
	private LyricDownloadManager mLyricDownloadManager;
	private String songName;
	private AudioPlayerActivity activity;
	private TextView lyric_save_detail;
	private TextView lyric_empty;
	private LinearLayout lyric_Llyt;;
	private String endWith = ".mp3";
	private String displayName;
	XiamiSDK mXiamiSDK;
	
	private boolean isLyricOnClick = false;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		activity = (AudioPlayerActivity) getActivity();
		mXiamiSDK = new XiamiSDK(activity.getApplicationContext(), SDKUtil.KEY, SDKUtil.SECRET);
		if (layoutView == null) {
			layoutView = inflater.inflate(R.layout.fragment_lyric_layout, null);
			findViewById();
			init();
			setListener();
		}
		// 缓存的rootView需要判断是否已经被加过parent，
		// 如果有parent需要从parent删除，要不然会发生这个rootview已经有parent的错误。
		ViewGroup parent = (ViewGroup) layoutView.getParent();
		if (parent != null) {
			parent.removeView(layoutView);
		}
		return layoutView;
	}

	@Override
	protected void findViewById() {
		mLrcListView = (ListView) layoutView.findViewById(R.id.lyricshow);
		mLrcListView
				.setPadding(mLrcListView.getPaddingLeft(), 160,
						mLrcListView.getPaddingRight(),
						mLrcListView.getPaddingBottom());
		lyric_save_detail = (TextView) layoutView
				.findViewById(R.id.lyric_save_detail);
		lyric_empty = (TextView) layoutView
				.findViewById(R.id.lyric_empty);
		lyric_Llyt = (LinearLayout) layoutView.findViewById(R.id.lyric_Llyt);
		lyric_save_detail.setText(getString(R.string.save_lyric_to)
				+ Constants.LYRIC_SAVE_PATH);

	}

	@Override
	protected void setListener() {
		lyric_Llyt.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				isLyricOnClick = true;
				MusicInfo music_info = MusicUtils.getCurrentMusicInfo();
				if(music_info !=null&&!TextUtils.isEmpty(music_info.source_type)&&music_info.source_type.equals(DatabaseConstant.LOCAL_TYPE)){
					Intent intent = new Intent();
					File file = new File(Constants.LYRIC_SAVE_PATH);
					intent.setAction(android.content.Intent.ACTION_GET_CONTENT);
					ComponentName component = new ComponentName(
							"com.android.fileexplorer",
							"com.android.fileexplorer.FileExplorerTabActivity");
					intent.setComponent(component);
					intent.setDataAndType(Uri.fromFile(file), "file/*");
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	
					if (getActivity().getPackageManager()
							.resolveActivity(intent, 0) != null) {
						startActivity(intent);
					}
				} else {
					updatLyricInfo();
				}
			}
		});

	}

	/**
	 * @Description:显示搜索下载歌词对话框
	 * @return: void
	 * @see
	 */
	protected void displayLrcDialog() {
		View view = View.inflate(getActivity(), R.layout.lrc_dialog, null);
		view.setMinimumWidth(500);
		final Dialog dialog = new Dialog(getActivity(), R.style.lrc_dialog);

		final Button okBtn = (Button) view.findViewById(R.id.ok_btn);
		final Button cancleBtn = (Button) view.findViewById(R.id.cancel_btn);
		final EditText artistEt = (EditText) view.findViewById(R.id.artist_tv);
		final EditText musicEt = (EditText) view.findViewById(R.id.music_tv);

		artistEt.setText(MusicUtils.getArtistName());
		musicEt.setText(MusicUtils.getTrackName());
		OnClickListener btnListener = new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (v == okBtn) {
					String artist = artistEt.getText().toString().trim();
					String music = musicEt.getText().toString().trim();
					if (TextUtils.isEmpty(artist) || TextUtils.isEmpty(music)) {
						Toast.makeText(getActivity(), "歌手和歌曲不能为空",
								Toast.LENGTH_SHORT).show();
					} else {
						// 开始搜索
						loadLyricByHand(displayName, music, artist);
						dialog.dismiss();
					}
				} else if (v == cancleBtn) {
					dialog.dismiss();
				}
			}
		};
		okBtn.setOnClickListener(btnListener);
		cancleBtn.setOnClickListener(btnListener);
		dialog.setContentView(view);
		dialog.show();
	}

	@Override
	protected void init() {
		// 歌词秀设置---------------------------------------------------------------

		// 防止第一次进入时 不播放歌曲
		File savefolder = new File(Constants.LYRIC_SAVE_PATH);
		if (!savefolder.exists()) {
			savefolder.mkdirs();
		}

		mLyricAdapter = new LyricAdapter(getActivity());
		View v = new View(getActivity());
		LayoutParams params = new LayoutParams(
				android.widget.AbsListView.LayoutParams.MATCH_PARENT, 200);
		v.setLayoutParams(params);
		mLrcListView.addHeaderView(v);
		mLrcListView.setAdapter(mLyricAdapter);
		lyric_empty.setText(R.string.loading_lyric);
		mLrcListView.setEmptyView(lyric_Llyt);
		mLrcListView.startAnimation(AnimationUtils.loadAnimation(getActivity(),
				android.R.anim.fade_in));

		mLyricLoadHelper = new LyricLoadHelper();
		mLyricLoadHelper.setLyricListener(mLyricListener);
		mLyricDownloadManager = new LyricDownloadManager(getActivity());
		// songName = MusicUtils.mService.getsongName();
		
		if (MusicUtils.isPlaying()) {
			updatLyricInfo();
		}
		
	}

	/**
	 * @Description: 时时更新歌词 专辑 歌曲名称的信息
	 * @return: void
	 * @see
	 */
	public void updatLyricInfo() {
 		songName = MusicUtils.getTrackName();
		String artist = MusicUtils.getArtistName();
		long audioId = MusicUtils.getCurrentAudioId();
		displayName = MusicUtils.getAudioDisPlayName(getActivity(), audioId);
		if (!TextUtils.isEmpty(displayName) && displayName.endsWith(endWith)) {
			int index = displayName.lastIndexOf(endWith);
			displayName = displayName.substring(0, index);
		}
		loadLyricByHand(displayName, songName, artist);
	}

	private LyricListener mLyricListener = new com.prize.app.lyric.LyricLoadHelper.LyricListener() {

		@Override
		public void onLyricLoaded(List<LyricSentence> lyricSentences, int index) {
			if(JLog.isDebug){
				for(LyricSentence lyricSentence :lyricSentences){
					JLog.i(TAG, "onLyricLoaded="+lyricSentence.toString());
				}
				
			}
			if (lyricSentences != null && lyricSentences.size() > 0) {
				lyric_empty.setText(R.string.nolyric_pl_download);
				mLyricAdapter.setLyric(lyricSentences);
				mLyricAdapter.setCurrentSentenceIndex(index);
				mLyricAdapter.notifyDataSetChanged();
			} else {
				if (mLyricAdapter != null) {
					mLyricAdapter.setLyric(null);
					mLyricAdapter.notifyDataSetChanged();
					
					lyric_empty.setText(R.string.nolyric);
				}
			}
			
		}

		@Override
		public void onLyricSentenceChanged(int indexOfCurSentence) {
			mLyricAdapter.setCurrentSentenceIndex(indexOfCurSentence);
			mLyricAdapter.notifyDataSetChanged();
			mLrcListView.smoothScrollToPositionFromTop(indexOfCurSentence,
					mLrcListView.getHeight() / 3, 500);
			
		}

	};

	
	private void getSongDetailInfo(final long song_id){
		new Thread(new Runnable() {			
			@Override
			public void run() {
				HashMap<String, Object> params = new HashMap<String, Object>();
		        params.clear();
                params.put("song_id", song_id);
                params.put("quality", "l");
                params.put("lyric_type", 2);
				try {
					String result = mXiamiSDK.xiamiSDKRequest(RequestMethods.SONG_DETAIL, params);
					LogUtils.d(TAG,"getSongDetail() result = " + result);
					SongDetailInfo song_info = PrizeParesNetData.parseSongDetailInfo(result);
					if(song_info != null){
						Message msg = new Message();
						msg.what = PARSE_NET_RESULT_SUCESSS;
						Bundle bundle = new Bundle();
						bundle.putParcelable("bean", song_info);
						msg.setData(bundle);
						mHandler.sendMessage(msg);						
					} else {
						ToastUtils.showToast(R.string.net_error);
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					String error = e.toString();					
				}
			}
		}).start();

	}
	private static final int PARSE_NET_RESULT_SUCESSS = 0;
	
	@SuppressLint("HandlerLeak")
	Handler mHandler = new Handler(){
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch(msg.what){
			case PARSE_NET_RESULT_SUCESSS:
				SongDetailInfo song_info = msg.getData().getParcelable("bean");
				JLog.i(TAG, "song_info.lyric="+song_info.lyric);
				if(song_info==null||TextUtils.isEmpty(song_info.lyric)){
					mLyricLoadHelper.loadLyric(null);
				}else{
					new DownloadLyricByUrlAsyncTask().execute(song_info.lyric,song_info.song_name,song_info.singers);
				}
				break;
			}
		}
	};
	
	
	private void getOnLineLyric(){
		JLog.i(TAG,"getOnLineLyric");
		if(MusicUtils.getCurrentMusicInfo()==null){
			JLog.i(TAG,"MusicUtils.getCurrentMusicInfo()==null");
			return;
		}
		JLog.i(TAG,"MusicUtils.getCurrentMusicInfo()="+MusicUtils.getCurrentMusicInfo());
		if(MusicUtils.getCurrentMusicInfo().source_type.equals(DatabaseConstant.ONLIEN_TYPE)){  //直接用xiami的歌词地址
			try {
//				String url = MusicUtils.getCurrentSongDetailInfo().getLyric();
//				if(url != null){
//					new DownloadLyricByUrlAsyncTask().execute(url);
//				} else {
					getSongDetailInfo(MusicUtils.getCurrentMusicInfo().songId);
//				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else{
			JLog.i(TAG,"lyric_empty="+lyric_empty);
			if(lyric_empty==null)
				return;
			lyric_empty.setText(R.string.nolyric);
		}
	}
	
	/**
	 * 加载歌词
	 * 
	 * @param displayName
	 *            音乐显示（displayName）的名称
	 * @param musicName
	 *            音乐实际名称
	 * @param artist
	 *            歌手
	 * @return void
	 * @see
	 */
	private void loadLyricByHand(String displayName, String musicName,
			String artist) {
		// 取得歌曲同目录下的歌词文件绝对路径
        if(mLyricLoadHelper==null)
        	return;
		String folderPath = Constants.LYRIC_SAVE_PATH;
		File savefolder = new File(folderPath);
		if (!savefolder.exists()) {
			savefolder.mkdirs();
		}
		
		File lyricfile = null;
		String lyricFilePath = null;
		if(!TextUtils.isEmpty(displayName)){
			lyricFilePath = Constants.LYRIC_SAVE_PATH + File.separator
					+ LyricLoadHelper.getFileName(displayName, artist);
//			lyricFilePath = Constants.LYRIC_SAVE_PATH + File.separator
//					+ displayName + ".lrc";
			lyricfile = new File(lyricFilePath);
		}
		String lyricFilePathto = Constants.LYRIC_SAVE_PATH + File.separator
				+ LyricLoadHelper.getFileName(musicName, artist);
//		String lyricFilePathto = Constants.LYRIC_SAVE_PATH + File.separator
//				+ musicName + ".lrc";
		
		File lyricfileTo = new File(lyricFilePathto);
		if (lyricfileTo.exists()) {
			JLog.i(TAG,"exists-lyricFilePathto="+ lyricFilePathto);
				// 本地有歌词，直接读取
			mLyricLoadHelper.loadLyric(lyricFilePathto);				
			return;
		} else if (lyricfile !=null && lyricfile.exists()) {
				// 本地有歌词，直接读取
			mLyricLoadHelper.loadLyric(lyricFilePath);
			return;
		} else {
			JLog.i(TAG,"loadLyricByHand=歌词不存在"+ lyricFilePathto);
			if (mLyricAdapter != null) {
				mLyricAdapter.setLyric(null);
				mLyricAdapter.notifyDataSetChanged();	
			}
				// mIsLyricDownloading = true;
				// // 尝试网络获取歌词
				// new LyricDownloadAsyncTask().execute(musicName, artist);	
			getOnLineLyric();
		}
	}

	/**
	 * @see 根据歌词的url 下载歌词并保存为文件
	 * @author lixing
	 *
	 */
	class DownloadLyricByUrlAsyncTask extends AsyncTask<String, Void, String>{
		@Override
		protected String doInBackground(String... arg0) {
			String path = downLoadXiamiLyricAndSaveFile(arg0[0],arg0[1],arg0[2]);
			return path;
		}
		
		@Override
		protected void onPostExecute(String result) {
			LogUtils.i(TAG, "onPostExecute网络获取歌词完毕，歌词保存路径:" + result);
			// 读取保存到本地的歌曲
			if(result != null)
				mLyricLoadHelper.loadLyric(result);
		};		
	}
	
	
	/**
	 * 
	 * 下载歌词并保存到本地 
	 * @param url 歌词下载的地址
	 * @return String 保存路径
	 */
	private String downLoadXiamiLyricAndSaveFile(String url,String songName,String singer){
		BufferedReader br = null;
		StringBuilder content = null;
		String temp = null;
		LogUtils.i(TAG, "歌词的真实下载地址:" + url);
		URL mUrl = null;
		try {
			mUrl = new URL(url);
		} catch (MalformedURLException e2) {
			e2.printStackTrace();
		}
		
		// 获取歌词文本，存在字符串类中
		try {
			// 建立网络连接
			br = new BufferedReader(new InputStreamReader(mUrl.openStream(),LyricDownloadManager.UTF_8));
			if (br != null) {
				content = new StringBuilder();
				// 逐行获取歌词文本
				while ((temp = br.readLine()) != null) {
					content.append(temp);
				}
				br.close();
			}
		} catch (Exception e1) {
			e1.printStackTrace();
			LogUtils.i(TAG, "歌词获取失败");
			if(isLyricOnClick){
				ToastUtils.showToast("抱歉，暂无歌词");
			}
		}
//		String mSongName = "";
//		String mSingers = "";
		try {
//			mSongName = URLDecoder.decode(MusicUtils.mService.getCurrentSongDetailInfo().getSongName(), LyricDownloadManager.UTF_8);
//			mSingers = URLDecoder.decode(MusicUtils.mService.getCurrentSongDetailInfo().getSingers(), LyricDownloadManager.UTF_8);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (content != null) {
			// 检查保存的目录是否已经创建
			String folderPath = Constants.LYRIC_SAVE_PATH;
			File savefolder = new File(folderPath);
			if (!savefolder.exists()) {
				savefolder.mkdirs();
			}
			String savePath = folderPath +File.separator+ LyricLoadHelper.getFileName(songName, singer);
//			String savePath = folderPath + File.separator + mSongName + ".lrc";
			LogUtils.i(TAG, "歌词保存路径:" + savePath + "---songName=" + songName+"--singer="+singer);
			LyricDownloadManager.saveLyric(content.toString(), savePath);
			return savePath;
		}
		return null;
	}
//	/**
//	 * 
//	 * 下载歌词并保存到本地 
//	 * @param url 歌词下载的地址
//	 * @return String 保存路径
//	 */
//	private String downLoadXiamiLyricAndSaveFile(String url){
//		BufferedReader br = null;
//		StringBuilder content = null;
//		String temp = null;
//		LogUtils.i(TAG, "歌词的真实下载地址:" + url);
//		URL mUrl = null;
//		try {
//			mUrl = new URL(url);
//		} catch (MalformedURLException e2) {
//			e2.printStackTrace();
//		}
//
//		// 获取歌词文本，存在字符串类中
//		try {
//			// 建立网络连接
//			br = new BufferedReader(new InputStreamReader(mUrl.openStream(),LyricDownloadManager.UTF_8));
//			if (br != null) {
//				content = new StringBuilder();
//				// 逐行获取歌词文本
//				while ((temp = br.readLine()) != null) {
//					content.append(temp);
//				}
//				br.close();
//			}
//		} catch (Exception e1) {
//			e1.printStackTrace();
//			LogUtils.i(TAG, "歌词获取失败");
//			if(isLyricOnClick){
//				ToastUtils.showToast("抱歉，暂无歌词");
//			}
//		}
//		String mSongName = "";
//		String mSingers = "";
//		try {
//			mSongName = URLDecoder.decode(MusicUtils.mService.getCurrentSongDetailInfo().getSongName(), LyricDownloadManager.UTF_8);
//			mSingers = URLDecoder.decode(MusicUtils.mService.getCurrentSongDetailInfo().getSingers(), LyricDownloadManager.UTF_8);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		if (content != null) {
//			// 检查保存的目录是否已经创建
//			String folderPath = Constants.LYRIC_SAVE_PATH;
//			File savefolder = new File(folderPath);
//			if (!savefolder.exists()) {
//				savefolder.mkdirs();
//			}
//			String savePath = folderPath + File.separator + mSongName + ".lrc";
//			LogUtils.i(TAG, "歌词保存路径:" + savePath + "---oldMusicName=" + mSongName);
//			LyricDownloadManager.saveLyric(content.toString(), savePath);
//			return savePath;
//		}
//		return null;
//	}
	
	/**
	 * 
	 * 类描述：异步加载下载歌词,根据歌名查询百度的ID，下载百度的歌词
	 *
	 * @author longbaoxiu
	 * @version v1.0
	 */
	class LyricDownloadAsyncTask extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... params) {
			// 从网络获取歌词，然后保存到本地
			String lyricFilePath = mLyricDownloadManager.searchLyricFromWeb(
					params[0], params[1], displayName);
			LogUtils.i(TAG, "doInBackgroundlyricFilePath=" + lyricFilePath);
			// 返回本地歌词路径
			mIsLyricDownloading = false;
			return lyricFilePath;
		}

		@Override
		protected void onPostExecute(String result) {
			LogUtils.i(TAG, "onPostExecute网络获取歌词完毕，歌词保存路径:" + result);
			// 读取保存到本地的歌曲
			mLyricLoadHelper.loadLyric(result);
		}
	}
	

	/**
	 * @Description: 刷新播放进度条
	 * @param: @param curTime 当前播放的时间点
	 * @param: @param totalTime 总时长
	 * @return: void
	 * @see
	 */
	public void refreshUI(int curTime, int totalTime) {
		// 开始搜索
		// loadLyricByHand(MusicUtils.getsongName(),
		// MusicUtils.getArtistName());
		int tempCurTime = curTime;
		if(mLyricLoadHelper != null){   /* PRIZE nieligang add for bug14233 20160506 */
		    mLyricLoadHelper.notifyTime(tempCurTime);
		}
	}
@Override
public void onDestroy() {
	mLyricLoadHelper.setLyricListener(null);
	super.onDestroy();
}
}
