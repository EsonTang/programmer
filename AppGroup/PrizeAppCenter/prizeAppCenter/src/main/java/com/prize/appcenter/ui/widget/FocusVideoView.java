/*******************************************
 * 版权所有©2015,深圳市铂睿智恒科技有限公司
 * <p>
 * 内容摘要：
 * 当前版本：
 * 作	者：
 * 完成日期：
 * 修改记录：
 * 修改日期：
 * 版 本 号：
 * 修 改 人：
 * 修改内容：
 * ...
 * 修改记录：
 * 修改日期：
 * 版 本 号：
 * 修 改 人：
 * 修改内容：
 *********************************************/

package com.prize.appcenter.ui.widget;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.prize.app.BaseApplication;
import com.prize.app.beans.ClientInfo;
import com.prize.app.download.AppManagerCenter;
import com.prize.app.net.datasource.base.AppsItemBean;
import com.prize.app.net.datasource.home.CarParentBean;
import com.prize.app.util.MTAUtil;
import com.prize.appcenter.R;
import com.prize.appcenter.activity.AppDetailActivity;
import com.prize.appcenter.activity.MainActivity;
import com.prize.appcenter.ui.dialog.DownDialog;
import com.prize.appcenter.ui.dialog.VideoDialog;
import com.prize.appcenter.ui.util.AIDLUtils;
import com.prize.appcenter.ui.util.ToastUtils;
import com.prize.appcenter.ui.util.UILimageUtil;
import com.prize.appcenter.ui.util.UIUtils;
import com.prize.appcenter.ui.widget.progressbutton.AnimDownloadProgressButton;

import fm.jiecao.jcvideoplayer_lib.JCVideoPlayer;
import fm.jiecao.jcvideoplayer_lib.JCVideoPlayerStandard;

import static com.prize.appcenter.R.id.game_download_btn;

/**
 * 类描述：首页video(视频类型)
 *
 * @author 聂礼刚
 * @version 1.0
 */
public class FocusVideoView extends RelativeLayout implements OnClickListener {
    private Activity mContext;
    private CarParentBean cardBean;
    private DownDialog mDownDialog;
    private VideoDialog mVideoDialog;
    private String fromPage;
    private boolean isGame = false;

    // 游戏图标
    private ImageView gameIcon;
    // 游戏名称
    private TextView gameName;
    // 游戏大小
    private TextView gameSize;
    // 游戏下载量
    private TextView downLoadCount;
    // 下载按钮
    private AnimDownloadProgressButton downloadBtn;

    private RelativeLayout app_Rl;
    private RelativeLayout game_detail_Rlyt;
    private RelativeLayout game_download_Rlyt;

    public FocusVideoView(Activity context, String fromPage, boolean isGame) {
        super(context);
        mContext = context;
        this.fromPage = fromPage;
        this.isGame = isGame;
        View view = inflate(context, R.layout.focus_vedio_layout, this);
        findViewById(view);
    }


    private void findViewById(View view) {
        gameIcon = (ImageView) view.findViewById(R.id.game_iv);
        gameName = (TextView) view.findViewById(R.id.game_name_tv);
        gameSize = (TextView) view.findViewById(R.id.game_size_tv);
        downLoadCount = (TextView) view.findViewById(R.id.download_count_tv);
        downloadBtn = (AnimDownloadProgressButton) findViewById(game_download_btn);
        game_detail_Rlyt = (RelativeLayout) view.findViewById(R.id.item_rlyt);
        game_download_Rlyt = (RelativeLayout) view.findViewById(R.id.game_download_Rlyt);
        app_Rl = (RelativeLayout) view.findViewById(R.id.app_Rl);
    }

    public void setData(final CarParentBean bean) {
        if (bean == null)
            return;

        cardBean = bean;

        if(bean.focus.app == null) {
            app_Rl.setVisibility(GONE);
            return;
        }else {
            app_Rl.setVisibility(VISIBLE);
        }

        if (!TextUtils.isEmpty(bean.focus.app.largeIcon)) {
            ImageLoader.getInstance().displayImage(bean.focus.app.largeIcon,
                    gameIcon, UILimageUtil.getHottestAppLoptions(), null);
        } else {

            ImageLoader.getInstance()
                    .displayImage(bean.focus.app.iconUrl,
                            gameIcon,
                            UILimageUtil.getHottestAppLoptions(), null);
        }
        gameName.setText(bean.focus.app.name);

        gameSize.setText(bean.focus.app.apkSizeFormat);
        if (null != bean.focus.app.downloadTimesFormat) {
            downLoadCount.setVisibility(View.VISIBLE);
            String user = bean.focus.app.downloadTimesFormat.replace("次", "人");
            downLoadCount.setText(mContext.getString(
                    R.string.person_use, user));
        } else {
            downLoadCount.setVisibility(View.GONE);
        }

        downloadBtn.setGameInfo(bean.focus.app);
        downloadBtn.enabelDefaultPress(true);
        game_detail_Rlyt.setOnClickListener(this);
        downloadBtn.setOnClickListener(this);
        game_download_Rlyt.setOnClickListener(this);

        //添加视频
        final JCVideoPlayerStandard player = (JCVideoPlayerStandard) findViewById(R.id.video);
        player.setUp(bean.focus.value, JCVideoPlayer.SCREEN_LAYOUT_LIST, "");
        Glide.with(mContext).load(bean.focus.imageUrl).into(player.thumbImageView);
        player.setOnStartListener(new JCVideoPlayer.OnStartListener() {
            @Override
            public void onStartNoWifi() {
                mVideoDialog = new VideoDialog(mContext, R.style.add_dialog);
                mVideoDialog.show();
                mVideoDialog.setmOnButtonClic(new VideoDialog.OnButtonClic() {

                    @Override
                    public void onClick(int which) {
                        dismissDialog();
                        switch (which) {
                            case 0:
                                break;
                            case 1:
                                player.startVideo();

                                if(!TextUtils.isEmpty(fromPage)) {
                                    MTAUtil.onVideoPlayClick("首页");
                                    MTAUtil.onHomePageFocusClick(cardBean.focus.positon);
                                }else {
                                    if(isGame){
                                        MTAUtil.onVideoPlayClick("游戏页");
                                        MTAUtil.onGamePageFocusClick(cardBean.focus.positon);
                                    }else {
                                        MTAUtil.onVideoPlayClick("应用页");
                                        MTAUtil.onAppPageFocusClick(cardBean.focus.positon);
                                    }
                                }
                                break;
                        }
                    }
                });
            }

            @Override
            public void onStart() {
                if(!TextUtils.isEmpty(fromPage)) {
                    MTAUtil.onVideoPlayClick("首页");
                    MTAUtil.onHomePageFocusClick(cardBean.focus.positon);
                }else {
                    if(isGame){
                        MTAUtil.onVideoPlayClick("游戏页");
                        MTAUtil.onGamePageFocusClick(cardBean.focus.positon);
                    }else {
                        MTAUtil.onVideoPlayClick("应用页");
                        MTAUtil.onAppPageFocusClick(cardBean.focus.positon);
                    }
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.item_rlyt:
                goToAppDetailActivity();
                if(!TextUtils.isEmpty(fromPage)) {
                    MTAUtil.onVideoAppClick("首页");
                    MTAUtil.onHomePageFocusClick(cardBean.focus.positon);
                }else {
                    if(isGame){
                        MTAUtil.onVideoAppClick("游戏页");
                        MTAUtil.onGamePageFocusClick(cardBean.focus.positon);
                    }else {
                        MTAUtil.onVideoAppClick("应用页");
                        MTAUtil.onAppPageFocusClick(cardBean.focus.positon);
                    }
                }
                break;
            case R.id.game_download_btn:
                onDownLoadBtnClick();
                break;
            case R.id.game_download_Rlyt:
                downloadBtn.performClick();
                break;
            default:
                break;
        }
    }

    private void goToAppDetailActivity() {
        if (cardBean != null && cardBean.focus != null && cardBean.focus.app != null) {
            // 跳转到详细界面
            // UIUtils.gotoAppDetail(item.id);

            Intent intent = new Intent(mContext,
                    AppDetailActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("appid", cardBean.focus.app.id);
            intent.putExtra("bundle", bundle);
            mContext.startActivity(intent);
            mContext.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        }
    }

    private void onDownLoadBtnClick(){
        final AppsItemBean itemBean = cardBean.focus.app;
        final int state = AIDLUtils.getGameAppState(
                itemBean.packageName, itemBean.id,
                itemBean.versionCode);
        switch (state) {
            case AppManagerCenter.APP_STATE_UNEXIST:
            case AppManagerCenter.APP_STATE_UPDATE:
            case AppManagerCenter.APP_STATE_DOWNLOAD_PAUSE:
                if (ClientInfo.getAPNType(BaseApplication.curContext) == ClientInfo.NONET) {
                    ToastUtils.showToast(R.string.nonet_connect);
                    return;
                }
        }
        if (BaseApplication.isDownloadWIFIOnly()
                && ClientInfo.getAPNType(BaseApplication.curContext) != ClientInfo.WIFI) {
            switch (state) {
                case AppManagerCenter.APP_STATE_UNEXIST:
                case AppManagerCenter.APP_STATE_UPDATE:
                case AppManagerCenter.APP_STATE_DOWNLOAD_PAUSE:
                    mDownDialog = new DownDialog(mContext, R.style.add_dialog);
                    mDownDialog.show();
                    mDownDialog.setmOnButtonClic(new DownDialog.OnButtonClic() {

                        @Override
                        public void onClick(int which) {
                            dismissDialog();
                            switch (which) {
                                case 0:
                                    break;
                                case 1:
                                    startAnimation(state);
                                    UIUtils.downloadApp(itemBean);
                                    break;
                            }
                        }
                    });
                    break;
                default:
                    downloadBtn.onClick();
                    break;
            }

        } else {
            downloadBtn.onClick();
        }
        if (ClientInfo.networkType == ClientInfo.WIFI) {
            startAnimation(state);
        }
    }

    private void dismissDialog() {
        if (mDownDialog != null && mDownDialog.isShowing()) {
            mDownDialog.dismiss();
            mDownDialog = null;
        }

        if (mVideoDialog != null && mVideoDialog.isShowing()) {
            mVideoDialog.dismiss();
            mVideoDialog = null;
        }
    }

    public void startAnimation(int state) {
        if (state == AppManagerCenter.APP_STATE_UNEXIST
                || state == AppManagerCenter.APP_STATE_UPDATE) {
            if (mContext instanceof MainActivity) {
                ((MainActivity) mContext).startAnimation(gameIcon);
            }
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        JCVideoPlayer.releaseAllVideos();
    }

    public AppsItemBean getAppBean(){
        return cardBean.focus.app;
    }

    public View getDownLoadView(){
        return downloadBtn;
    }

}
