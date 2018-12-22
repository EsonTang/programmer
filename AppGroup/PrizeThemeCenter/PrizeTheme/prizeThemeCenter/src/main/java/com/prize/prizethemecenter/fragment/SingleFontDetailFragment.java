package com.prize.prizethemecenter.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.prize.app.util.JLog;
import com.prize.prizethemecenter.MainApplication;
import com.prize.prizethemecenter.R;
import com.prize.prizethemecenter.activity.FontDetailActivity;
import com.prize.prizethemecenter.activity.FontImageDisplayActivity;
import com.prize.prizethemecenter.activity.ThemeCommentActivity;
import com.prize.prizethemecenter.bean.SingleThemeItemBean;
import com.prize.prizethemecenter.fragment.base.BaseFragment;
import com.prize.prizethemecenter.ui.adapter.ThemeCommentAdapter;
import com.prize.prizethemecenter.ui.utils.DBUtils;
import com.prize.prizethemecenter.ui.widget.ListViewForScrollView;
import com.prize.prizethemecenter.ui.widget.view.ExpendTextView;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * 字体详情Fragment
 * Created by pengy on 2016/11/1.
 */
public class SingleFontDetailFragment extends BaseFragment {

    private static final String TAG ="pengy" ;
    @InjectView(R.id.font_title)
    TextView fontTitle;
    @InjectView(R.id.font_download_count)
    TextView fontDownloadCount;
    @InjectView(R.id.font_size)
    TextView fontSize;
    @InjectView(R.id.font_pic_IV)
    ImageView fontPicIV;
    @InjectView(R.id.expendTextView_one)
    ExpendTextView expendTextViewOne;
    @InjectView(R.id.comment_counts)
    TextView commentCounts;
    @InjectView(R.id.comment_content_more)
    RelativeLayout commentContentMore;
    @InjectView(R.id.detail_comment_lv_id)
    ListViewForScrollView detailCommentLvId;

    private View view;
    private FontDetailActivity mCtx;
    //图片显示路径
    private String paths;
    //显示两条评论的适配器
    private ThemeCommentAdapter commentAdapter;

    private SingleThemeItemBean.ItemsBean mItemData;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if(activity!=null){
            mCtx = (FontDetailActivity) activity;
        }
    }

    @Override
    public View onCreateContentView(LayoutInflater inflater, ViewGroup container) {
        if (view == null) {
            view = inflater.inflate(R.layout.fragment_font_detail_layout, null);
            view.setVisibility(View.VISIBLE);
        }
        return view;
    }

    public void setFontData(SingleThemeItemBean.ItemsBean bean) {

        commentAdapter = new ThemeCommentAdapter(MainApplication.curContext);
        commentAdapter.setIsDetai(true);
        detailCommentLvId.setAdapter(commentAdapter);
        mItemData = bean;
        initData();
        setListener(bean);
//        Typeface tf = Typeface.createFromAsset(mCtx.getAssets(),
//                "fonts/xxx.ttf");
//        tf.getStyle();
    }



//add by zhouerlong  comment
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(commentCounts==null) {
            commentCounts = (TextView) this.getActivity().findViewById(R.id.comment_counts);
        }

        if(data!=null) {
            String count = (String) data.getExtra("count");
            commentCounts.setText("(" + count + ")");
        }

    }
//add by zhouerlong  comment

    private void initData() {
        if(mItemData!=null){
            fontTitle.setText(mItemData.getName());
            fontSize.setText(mItemData.getSize());
            if(mItemData.getDownload_count()!=null){
                String downloadCount = getResources().getString(R.string.download_times);
                downloadCount = String.format(downloadCount, Integer.parseInt(mItemData.getDownload_count()));
                fontDownloadCount.setText(downloadCount);
            }
            paths = mItemData.getScreenshot().get(0);
            ImageLoader.getInstance().displayImage(paths,fontPicIV, null,null);
            expendTextViewOne.setContentDesc(getResources().getString(R.string.resource_intro), mItemData.getIntro());

            if (mItemData.getComments().getItem() != null) {
                commentCounts.setText("(" + mItemData.getComments().getItem().size() + ")");
                JLog.i("hu","fontCount==="+mItemData.getComments().getItem().size());
                commentAdapter.setData(mItemData.getComments().getItem());
            } else {
                commentCounts.setText(getResources().getString(R.string.no_comment));
            }
        }
    }

    private void setListener(final SingleThemeItemBean.ItemsBean bean) {
        fontPicIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mCtx, FontImageDisplayActivity.class);
                intent.putExtra("paths", paths);
                mCtx.startActivity(intent);
                mCtx.overridePendingTransition(R.anim.scale_in,
                        R.anim.scale_out);
            }
        });

        commentContentMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mCtx, ThemeCommentActivity.class);
                Bundle bundle = new Bundle();
                bundle.putParcelable("detailData",bean.getComments());
                if(bean.getId()!=null){
                    bundle.putInt("fontID",Integer.parseInt(bean.getId()));
                    boolean download = DBUtils.isDownload(bean.getId());
                    bundle.putBoolean("isDownload",download);
                    bundle.putParcelable("itemBean",bean);
                }
                bundle.putBoolean("isFont",true);
                intent.putExtras(bundle);
//add by zhouerlong  comment
                startActivityForResult(intent,Activity.RESULT_FIRST_USER);
//add by zhouerlong  comment
                mCtx.overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
            }
        });

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // TODO: inflate a fragment view
        View rootView = super.onCreateView(inflater, container, savedInstanceState);
        ButterKnife.inject(this, rootView);
        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }
}
