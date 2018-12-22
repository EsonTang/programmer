package com.prize.prizethemecenter.fragment;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.telecom.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.prize.app.util.JLog;
import com.prize.prizethemecenter.MainApplication;
import com.prize.prizethemecenter.R;
import com.prize.prizethemecenter.activity.SingleThemeDetailActivity;
import com.prize.prizethemecenter.activity.ThemeCommentActivity;
import com.prize.prizethemecenter.activity.ThemeDetailImgActivity;
import com.prize.prizethemecenter.activity.similarityActivity;
import com.prize.prizethemecenter.bean.SearchSimilartyData;
import com.prize.prizethemecenter.bean.SingleThemeItemBean;
import com.prize.prizethemecenter.bean.SingleThemeItemBean.ItemsBean.TypesBean;
import com.prize.prizethemecenter.bean.ThemeItemBean;
import com.prize.prizethemecenter.fragment.base.BaseFragment;
import com.prize.prizethemecenter.manage.ThreadPoolManager;
import com.prize.prizethemecenter.request.SimilarityThemeRequest;
import com.prize.prizethemecenter.response.SearchSimilartyResponse;
import com.prize.prizethemecenter.ui.adapter.SimilartyThemeListAdapter;
import com.prize.prizethemecenter.ui.adapter.SingleThemeGridAdapter;
import com.prize.prizethemecenter.ui.adapter.ThemeCommentAdapter;
import com.prize.prizethemecenter.ui.utils.CommonUtils;
import com.prize.prizethemecenter.ui.utils.DBUtils;
import com.prize.prizethemecenter.ui.utils.MTAUtil;
import com.prize.prizethemecenter.ui.utils.UILimageUtil;
import com.prize.prizethemecenter.ui.utils.UIUtils;
import com.prize.prizethemecenter.ui.widget.CornerImageView;
import com.prize.prizethemecenter.ui.widget.ImgForHorizontalScrollview;
import com.prize.prizethemecenter.ui.widget.ListViewForScrollView;
import com.prize.prizethemecenter.ui.widget.NotifyingScrollView;
import com.prize.prizethemecenter.ui.widget.view.ExpendTextView;
import com.prize.prizethemecenter.ui.widget.view.LinearLayoutForDetail;
import com.prize.prizethemecenter.ui.widget.view.ScollerGridView;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.x;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by Administrator on 2016/9/18.
 */
public class SingleThemeDetailFragment extends BaseFragment implements AdapterView.OnItemClickListener, View.OnClickListener {

    @InjectView(R.id.theme_name)
    TextView themeName;
    @InjectView(R.id.download_count)
    TextView downloadCount;
    @InjectView(R.id.detail_size)
    TextView detailSize;
    @InjectView(R.id.head_view)
    LinearLayout headView;
    @InjectView(R.id.parentScrollView_id)
    NotifyingScrollView parentScrollViewId;
    @InjectView(R.id.title)
    TextView title;
    @InjectView(R.id.child_id)
    LinearLayoutForDetail childId;
    @InjectView(R.id.horizotal_id)
    ImgForHorizontalScrollview horizotalId;
    @InjectView(R.id.select_point)
    View selectPoint;
    @InjectView(R.id.ll_guide_point_group)
    LinearLayout llGuidePointGroup;
    @InjectView(R.id.expendTextView_one)
    ExpendTextView expandView;
    @InjectView(R.id.type)
    ScollerGridView typeItems;
    @InjectView(R.id.sign)
    TextView sign;
    @InjectView(R.id.comment_counts)
    TextView commentCounts;
    @InjectView(R.id.detail_comment_lv_id)
    ListViewForScrollView detailCommentLvId;
    @InjectView(R.id.comment_content_more)
    RelativeLayout commentContentMore;
    @InjectView(R.id.comment_end)
    View commentEnd;
    @InjectView(R.id.gv_theme)
    GridView gvTheme;
    @InjectView(R.id.iv_more_theme)
    ImageView ivMoreTheme;


    private Bundle mBundle;
    private View view;
    private SingleThemeDetailActivity mActivity;
    private SingleThemeItemBean appData;
    private SingleThemeItemBean.ItemsBean mItemData;
    private static SingleThemeDetailFragment fragment;
    private int windowHeight;
    private static String TAG = "bian";
    private ImageLoader imageLoader;
    private int windowWith;
    private View indicator;
    private SingleThemeGridAdapter themeGridAdapter;
    private ThemeCommentAdapter commentAdapter;
    private ThemeItemBean mItemBean;
    private String themeId;
    private SimilartyThemeListAdapter similartyThemeListAdapter;
    private float mScale;


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity != null) {
            mActivity = (SingleThemeDetailActivity) activity;
        }
    }

    @Override
    public View onCreateContentView(LayoutInflater inflater, ViewGroup container) {

        mBundle = getArguments();
        if (mBundle != null) {
            mItemBean = mBundle.getParcelable("ThemeItemBean");
            themeId = mBundle.getString("themeID", null);
        }
        Log.d(TAG, "onCreateContentView " + mItemBean);
        if (view == null) {
            view = inflater.inflate(R.layout.fragment_detail_parent, container, false);
            view.setVisibility(View.VISIBLE);
        }

        return view;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    public void initData(SingleThemeItemBean.ItemsBean themeItemData) {
        if (mActivity == null) {
            return;
        }
        windowHeight = getActivity().getWindow().getDecorView().getHeight();
        windowWith = getActivity().getWindow().getDecorView().getWidth();
        themeGridAdapter = new SingleThemeGridAdapter(MainApplication.curContext);
        commentAdapter = new ThemeCommentAdapter(MainApplication.curContext);
        similartyThemeListAdapter = new SimilartyThemeListAdapter(mActivity, false);
        commentAdapter.setIsDetai(true);
        init(typeItems);
        typeItems.setAdapter(themeGridAdapter);
        detailCommentLvId.setAdapter(commentAdapter);
        typeItems.setOnItemClickListener(this);
        commentContentMore.setOnClickListener(this);
        ivMoreTheme.setOnClickListener(this);

        mItemData = themeItemData;
        initHeadData(headView);
        initHorizontalScrollView(view);
        horizotalId.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View view, int i, int i1, int i2, int i3) {
                int selectdePaged = (int) ((218*mScale + i2) / ((mActivity.getResources().getInteger(R.integer.image_wight)
                        + mActivity.getResources().getInteger(R.integer.leftMargin))*mScale/2));
//                selectdePaged = (int) (selectdePaged * mScale) / 2;
                int basicWidth = llGuidePointGroup.getChildAt(1).getLeft() - llGuidePointGroup.getChildAt(0).getLeft();
                int leftMargin = basicWidth * (selectdePaged);
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)
                        selectPoint.getLayoutParams();
                params.leftMargin = leftMargin;
                selectPoint.setLayoutParams(params);
            }
        });
    }

    private void init(GridView typeItems) {
        typeItems.setNumColumns(4);
        typeItems.setColumnWidth(13);
        typeItems.setSelector(new ColorDrawable(Color.TRANSPARENT));
    }


    private void initHeadData(LinearLayout headView) {
        if (mItemData != null) {
            themeName.setText(mItemData.getName());
            downloadCount.setText("已下载" + mItemData.getDownload_count() + "次");
            detailSize.setText(mItemData.getSize());
            title.setText(mItemData.getTitle());
            expandView.setContentDesc(mActivity.getString(R.string.theme_introduce), mItemData.getIntro());
            themeGridAdapter.setData(mItemData.getTypes());
            final String tag = initTag(mItemData.getTypes());
            ThreadPoolManager.getDownloadPool().execute(new Runnable() {
                @Override
                public void run() {
                    initSimilaryTheme(tag);
                }
            });

            if (mItemData.getComments().getItem() != null) {
                commentCounts.setText("(" + mItemData.getComments().getItem().size() + ")");
                commentAdapter.setData(mItemData.getComments().getItem());
            } else {
                commentCounts.setText("（暂无评论）");
            }

        }
    }

    private void initSimilaryTheme(String tag) {
        SimilarityThemeRequest request = new SimilarityThemeRequest();
        request.tag = tag;
        x.http().post(request, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    JSONObject obj = new JSONObject(result);
                    if (obj.getInt("code") == 00000) {
                        SearchSimilartyResponse response = CommonUtils.getObject(result, SearchSimilartyResponse.class);
                        List<SearchSimilartyData.TagBean> tags = response.data.getTag();
                        for (int i = 0; i < tags.size(); i++) {
                            SearchSimilartyData.TagBean tagBean= tags.get(i);
                            if (tagBean.getId().equals(themeId))
                                tags.remove(i);
                        }
                        similartyThemeListAdapter.setData(tags);
                        gvTheme.setAdapter(similartyThemeListAdapter);
                        gvTheme.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                if (CommonUtils.isFastDoubleClick()) return;
                                if (similartyThemeListAdapter.getItem(position) != null) {
                                    //跳转到搜索页
                                    SearchSimilartyData.TagBean bean = similartyThemeListAdapter.getItem(position);
                                    if (bean.getAd_pictrue() != null) {
                                        UIUtils.gotoThemeDetail(bean.getId(), bean.getAd_pictrue());
                                    }
                                    MTAUtil.onClickTagTheme(bean.getName());
                                }
                            }
                        });
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {

            }

            @Override
            public void onCancelled(CancelledException cex) {

            }

            @Override
            public void onFinished() {

            }
        });
    }

    private String initTag(List<TypesBean> types) {
        StringBuffer sb = new StringBuffer();
        if (types != null) {
            for (TypesBean type : types) {
                sb.append(type.getName()).append(",");
            }
            return sb.toString().substring(0,sb.toString().length()-1);
        }
        return null;
    }


    private ArrayList<ImageView> imgs = new ArrayList<ImageView>();

    /**
     * 方法描述：初始化图片展示区
     *
     * @param view
     * @return void
     * @see /类名/完整类名/完整类名#方法名
     */
    private void initHorizontalScrollView(View view) {
        if (mItemData == null || mItemData.getScreenshot() == null) return;
        final List<String> paths = mItemData.getScreenshot();
        imageLoader = ImageLoader.getInstance();

        for (int i = 0; i < paths.size(); i++) {
            final CornerImageView image = (CornerImageView) LayoutInflater.from(mActivity).inflate(R.layout.imageview, null);
            int w = 0;
            int h = 0;
            int leftMargin = 0;
            if (windowHeight >= 1000 * mScale) {
                w = (int) (221 * mScale);
                h = (int) (391 * mScale);
                leftMargin = (int) (8 * mScale);
            } else {
                w = (int) (mActivity.getResources().getInteger(R.integer.image_wight) * mScale) / 2;
                h = (int) (mActivity.getResources().getInteger(R.integer.image_height) * mScale) / 2;
                leftMargin = (int) (mActivity.getResources().getInteger(R.integer.leftMargin) * mScale);
            }
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(w, h);
            params.weight = 1;
            if (i != 0 ) {
                params.leftMargin = leftMargin;
                if (i == paths.size() - 1) {
                    params.rightMargin = (int) (15 * mScale);
                }
            } else {
                params.leftMargin = (int) (15 * mScale);
            }
            image.setLayoutParams(params);
            image.setDrawingCacheEnabled(true);
            image.setTag(i);
            image.requestLayout();
            image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mActivity, ThemeDetailImgActivity.class);
                    intent.putStringArrayListExtra("paths", (ArrayList<String>) paths);
                    intent.putExtra("index", (Integer) v.getTag());
                    mActivity.startActivity(intent);
                    mActivity.overridePendingTransition(R.anim.scale_in,
                            R.anim.scale_out);
                }
            });
            imgs.add(image);
            childId.addView(image);
        }

        for (int i = 0; i < paths.size(); i++) {
            CornerImageView imageView = (CornerImageView) childId.getChildAt(i);
            imageLoader.displayImage(paths.get(i), imageView, UILimageUtil.getSingleThemeDpLoptions(), null);
            indicator = new View(mActivity);
            indicator.setBackgroundResource(R.drawable.point_normal);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams((int) (4 * mScale), (int) (4 * mScale));
            if (i != 0) {
                params.leftMargin = (int) (4 * mScale);
            }
            llGuidePointGroup.addView(indicator, params);
        }
        childId.invalidate();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // TODO: inflate a fragment view
        View rootView = super.onCreateView(inflater, container, savedInstanceState);
        ButterKnife.inject(this, rootView);
        mScale = getResources().getDisplayMetrics().density;
        return rootView;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (CommonUtils.isFastDoubleClick()) return;
        if (themeGridAdapter.getItem(position) != null) {
            //跳转到搜索页
            TypesBean TypesBean = (TypesBean) themeGridAdapter.getItem(position);
            JLog.d(TAG, "onItemClick " + TypesBean.getName() + "::" + TypesBean.getId());
            Intent intent = new Intent(MainApplication.curContext,
                    similarityActivity.class);
            intent.putExtra("name", TypesBean.getName());
            startActivity(intent);
            MTAUtil.onClickTagTheme(TypesBean.getName());
        }
    }
    Intent intent = null;

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
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.comment_content_more:
                intent = new Intent(MainApplication.curContext, ThemeCommentActivity.class);
                Bundle bundle = new Bundle();
//                bundle.putParcelable("detailData", mItemData.getComments());
                if (themeId != null) {
                    bundle.putInt("themeID", Integer.parseInt(themeId));
                    boolean download = DBUtils.isDownload(themeId);
                    bundle.putBoolean("isDownload", download);
                    if (mItemData != null) {
                       /* mItemData.setComments(null);
                        mItemData.setScreenshot(null);
                        mItemData.setScreenshot(null);
                        mItemData.setTypes(null);*/

                        bundle.putParcelable("itemBean", mItemData);
                    }
                }
                bundle.putBoolean("isFont", false);
                intent.putExtras(bundle);
                break;
            case R.id.iv_more_theme:
                intent = new Intent(MainApplication.curContext, similarityActivity.class);
                if(mItemData !=null ) {
                    Bundle bundleSim = new Bundle();
                    bundleSim.putString("name",initTag(mItemData.getTypes()));
                    bundleSim.putString("id",themeId);
                    intent.putExtras(bundleSim);
                }
                break;
        }
//add by zhouerlong  comment
        this.startActivityForResult(intent,Activity.RESULT_FIRST_USER);
//add by zhouerlong  comment

        ((Activity) MainApplication.curContext).overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }
}
