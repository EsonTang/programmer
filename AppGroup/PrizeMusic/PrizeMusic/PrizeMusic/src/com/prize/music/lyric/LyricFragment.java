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

import java.io.File;
import java.util.List;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
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

import com.prize.music.Constants;
import com.prize.music.activities.AudioPlayerActivity;
import com.prize.music.base.BaseFragment;
import com.prize.music.helpers.utils.LogUtils;
import com.prize.music.helpers.utils.MusicUtils;
import com.prize.music.lyric.LyricLoadHelper.LyricListener;
import com.prize.music.R;

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
	private static LyricLoadHelper mLyricLoadHelper;
	private LyricDownloadManager mLyricDownloadManager;
	private String albumName;
	private AudioPlayerActivity activity;
	private TextView lyric_save_detail;
	private LinearLayout lyric_Llyt;;
	private String endWith = ".mp3";
	private String displayName;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		activity = (AudioPlayerActivity) getActivity();
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
		lyric_Llyt = (LinearLayout) layoutView.findViewById(R.id.lyric_Llyt);
		lyric_save_detail.setText(getString(R.string.save_lyric_to)
				+ Constants.LYRIC_SAVE_PATH);

	}

	@Override
	protected void setListener() {
		lyric_Llyt.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
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
		mLrcListView.setEmptyView(lyric_Llyt);
		mLrcListView.startAnimation(AnimationUtils.loadAnimation(getActivity(),
				android.R.anim.fade_in));

		mLyricLoadHelper = new LyricLoadHelper();
		mLyricLoadHelper.setLyricListener(mLyricListener);
		mLyricDownloadManager = new LyricDownloadManager(getActivity());
		// albumName = MusicUtils.mService.getAlbumName();
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
		albumName = MusicUtils.getTrackName();
		String artist = MusicUtils.getArtistName();
		long audioId = MusicUtils.getCurrentAudioId();
		displayName = MusicUtils.getAudioDisPlayName(getActivity(), audioId);
		if (!TextUtils.isEmpty(displayName) && displayName.endsWith(endWith)) {
			int index = displayName.lastIndexOf(endWith);
			displayName = displayName.substring(0, index);
		}
		loadLyricByHand(displayName, albumName, artist);
	}

	private LyricListener mLyricListener = new LyricListener() {

		@Override
		public void onLyricLoaded(List<LyricSentence> lyricSentences, int index) {
			if (lyricSentences != null && lyricSentences.size() > 0) {
				mLyricAdapter.setLyric(lyricSentences);
				mLyricAdapter.setCurrentSentenceIndex(index);
				mLyricAdapter.notifyDataSetChanged();
			} else {
				if (mLyricAdapter != null) {
					mLyricAdapter.setLyric(null);
					mLyricAdapter.notifyDataSetChanged();

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

		String folderPath = Constants.LYRIC_SAVE_PATH;
		File savefolder = new File(folderPath);
		if (!savefolder.exists()) {
			savefolder.mkdirs();
		}

		String lyricFilePath = Constants.LYRIC_SAVE_PATH + File.separator
				+ displayName + ".lrc";
		String lyricFilePathto = Constants.LYRIC_SAVE_PATH + File.separator
				+ musicName + ".lrc";
		File lyricfile = new File(lyricFilePath);
		File lyricfileTo = new File(lyricFilePathto);

		if (lyricfile.exists()) {
			// 本地有歌词，直接读取
			mLyricLoadHelper.loadLyric(lyricFilePath);
			return;
		} else if (lyricfileTo.exists()) {
			// 本地有歌词，直接读取
			mLyricLoadHelper.loadLyric(lyricFilePathto);
			return;
		} else {
			if (mLyricAdapter != null) {
				mLyricAdapter.setLyric(null);
				mLyricAdapter.notifyDataSetChanged();

			}
			// mIsLyricDownloading = true;
			// // 尝试网络获取歌词
			// new LyricDownloadAsyncTask().execute(musicName, artist);

		}
	}

	/**
	 * 
	 * 类描述：异步加载下载歌词
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
		};
	};

	/**
	 * @Description: 刷新播放进度条
	 * @param: @param curTime 当前播放的时间点
	 * @param: @param totalTime 总时长
	 * @return: void
	 * @see
	 */
	public void refreshUI(int curTime, int totalTime) {
		// 开始搜索
		// loadLyricByHand(MusicUtils.getAlbumName(),
		// MusicUtils.getArtistName());
		int tempCurTime = curTime;
		mLyricLoadHelper.notifyTime(tempCurTime);
	}

}
