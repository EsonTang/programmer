package com.prize.prizenavigation.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.facebook.drawee.view.SimpleDraweeView;
import com.prize.prizenavigation.NavigationApplication;
import com.prize.prizenavigation.R;
import com.prize.prizenavigation.base.BaseFragment;
import com.prize.prizenavigation.bean.NaviDatas;
import com.prize.prizenavigation.manager.NaviDatasManager;
import com.prize.prizenavigation.utils.IConstants;
import com.prize.prizenavigation.utils.UILimageUtil;

import fm.jiecao.jcvideoplayer_lib.JCVideoPlayer;
import fm.jiecao.jcvideoplayer_lib.JCVideoPlayerStandard;

/**
 * 详情fragment
 * Created by liukun on 2017/3/2.
 */
public class NaviFragment extends BaseFragment implements View.OnClickListener, NaviDatasManager.setUpDownCallback {

    private Activity mActivity;

    private View rootView;
    /**
     * 数据源
     */
    private NaviDatas.ListBean fraDatas;
    /**
     * 大图
     */
    private SimpleDraweeView img;
    /**
     * 视频窗口
     */
    private JCVideoPlayerStandard jcVideoPlayerStandard;
    /**
     * 小图
     */
    private SimpleDraweeView imgtitle;
    private TextView textView;
    private TextView textViewtitle;
//    private TextView textViewpos;

    private RadioGroup radioGroup;
    private RadioButton radioPrasie;
    private RadioButton radioBlame;

    private int total;

    public NaviFragment() {

    }

    public static NaviFragment getInstance(NaviDatas.ListBean dataBeans, int total) {
        NaviFragment naviFragment = new NaviFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(IConstants.NAVIFRAGMENT_DATAS_FLAG, dataBeans);
        bundle.putInt(IConstants.NAVIFRAGMENT_DATAS_TOTAL_FLAG, total);
        if (dataBeans != null)
            naviFragment.setFragmentId(dataBeans.getPagenum());
        naviFragment.setArguments(bundle);
        return naviFragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.mActivity = activity;
    }

    @Override
    protected View initViews(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (rootView != null) {
            return rootView;
        } else {
            rootView = inflater.inflate(R.layout.fragment_navi_content, container, false);
            img = (SimpleDraweeView) rootView.findViewById(R.id.vp_img);
            jcVideoPlayerStandard = (JCVideoPlayerStandard) rootView.findViewById(R.id.videoplayer);
//          gifView = (GifView) rootView.findViewById(R.id.vp_img);
            imgtitle = (SimpleDraweeView) rootView.findViewById(R.id.vp_img_titile);
            textView = (TextView) rootView.findViewById(R.id.vp_center_tv);
            textViewtitle = (TextView) rootView.findViewById(R.id.vp_center_title_tv);
//        textViewpos = (TextView) rootView.findViewById(R.id.vp_center_pos);
            radioGroup = (RadioGroup) rootView.findViewById(R.id.fra_comment_rg);
            radioPrasie = (RadioButton) rootView.findViewById(R.id.fra_praise_rb);
            radioBlame = (RadioButton) rootView.findViewById(R.id.fra_blame_rb);
            img.setOnClickListener(this);
            radioPrasie.setOnClickListener(this);
            radioBlame.setOnClickListener(this);
            return rootView;
        }
    }

    @Override
    protected void initData() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    if (fraDatas != null && isAdded()) {
                        if (fraDatas.getIs_video().equals("0")) {
                            jcVideoPlayerStandard.setVisibility(View.INVISIBLE);
                            img.setVisibility(View.VISIBLE);
                            // 加载图片
                            UILimageUtil.displayImg(fraDatas.getLarge_icon_url(), img);
                        } else {
                            jcVideoPlayerStandard.setVisibility(View.VISIBLE);
                            img.setVisibility(View.INVISIBLE);
                            //加载视频 http://gslb.miaopai.com/stream/ed5HCfnhovu3tyIQAiv60Q__.mp4
                            jcVideoPlayerStandard.setUp(fraDatas.getVideo_url()
                                    , JCVideoPlayerStandard.SCREEN_LAYOUT_NORMAL, "");
                            Glide.with(NavigationApplication.getContext()).load(fraDatas.getLarge_icon_url()).into(jcVideoPlayerStandard.thumbImageView);
                        }
                        UILimageUtil.displayImg(fraDatas.getSmall_icon_url(), imgtitle);
                        textView.setText(fraDatas.getContent());
                        textViewtitle.setText(fraDatas.getTitle());
                        int updown = fraDatas.getUpdown();
                        if (updown == 1) {
                            radioPrasie.setChecked(true);
//                        radioBlame.setEnabled(false);
                        } else if (updown == 0) {
                            radioBlame.setChecked(true);
//                        radioPrasie.setEnabled(false);
                        }
//                    StringBuilder sbPos = new StringBuilder();
//                    sbPos.append(fraDatas.getPagenum()).append("/").append(total);
//                    textViewpos.setText(sbPos);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }, IConstants.FRAGMENT_DELAYMILLIS);
    }

    @Override
    public void initVariables(Bundle bundle) {
        fraDatas = (NaviDatas.ListBean) bundle.getSerializable(IConstants.NAVIFRAGMENT_DATAS_FLAG);
        total = bundle.getInt(IConstants.NAVIFRAGMENT_DATAS_TOTAL_FLAG);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fra_praise_rb:
//                ToastUtils.showToast("点击了赞" + fragmentId);
                NaviDatasManager.getInstance().setUpDown(fraDatas.getId(), 1, this);
                fraDatas.setUpdown(1);//bug51586
                break;
            case R.id.fra_blame_rb:
//                ToastUtils.showToast("点击了踩" + fragmentId);
                NaviDatasManager.getInstance().setUpDown(fraDatas.getId(), 0, this);
                fraDatas.setUpdown(0);//bug51586
                break;
            case R.id.vp_img:
//                //若图片加载失败 点击重载数据
//                if (img.getDrawable()==null)
//                refreshData(fraDatas);
                break;
        }
    }

    /**
     * 刷新fragment数据
     *
     * @param listBean
     */
    public void refreshData(NaviDatas.ListBean listBean) {
        if (listBean != null) {
            fraDatas = listBean;

            //如果被回收的Fragment会重新从Bundle里获取数据,所以也要更新一下
            Bundle args = getArguments();
            if (args != null) {
                args.putSerializable(IConstants.NAVIFRAGMENT_DATAS_FLAG, fraDatas);
            }

//            if (textView != null) {
//                textView.setVisibility(View.GONE);
//            }
//            if (progressBar != null) {
//                progressBar.setVisibility(View.VISIBLE);
//            }

            if (isFragmentVisible()) {
                initData();
            } else {
                setForceLoad(true);
            }
        }
    }

    /**
     * 点赞回调
     *
     * @param isUpDown 是否提交成功
     * @param msg      返回信息
     * @param type     赞1 踩0
     */
    @Override
    public void onUpDown(Boolean isUpDown, String msg, int type) {
       /* if (!isUpDown) {
            ToastUtils.showOneToast(msg);
        } else {
            if (type == 1) {
                //点赞成功
                radioPrasie.setChecked(true);
//                radioBlame.setEnabled(false);
            } else {
                //点踩成功
                radioBlame.setChecked(true);
//                radioPrasie.setEnabled(false);
            }
        }*/
    }

    @Override
    public void onPause() {
        super.onPause();
        JCVideoPlayer.releaseAllVideos();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        BitmapDrawable drawable = (BitmapDrawable) img.getDrawable();
//        if(drawable!=null) {
//            Bitmap bitmap = drawable.getBitmap();
//            boolean b = !bitmap.isRecycled();
//            if (b)
//            bitmap.recycle();
//        }
//        unbindDrawables(rootView.findViewById(R.id.vp_content)); // <---This should be the ID of this fragments (ScreenSlidePageFragment) layout
    }

//    private void unbindDrawables(View view)
//    {
//        if (view.getBackground() != null)
//        {
//            view.getBackground().setCallback(null);
//        }
//        if (view instanceof ViewGroup && !(view instanceof AdapterView))
//        {
//            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++)
//            {
//                unbindDrawables(((ViewGroup) view).getChildAt(i));
//            }
//            ((ViewGroup) view).removeAllViews();
//        }
//    }


}
