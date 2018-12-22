package com.prize.appcenter.fragment;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.prize.app.beans.ClientInfo;
import com.prize.app.net.datasource.base.AppsItemBean;
import com.prize.app.util.JLog;
import com.prize.appcenter.R;
import com.prize.appcenter.ui.adapter.OneKeyInsallAdapter;
import com.prize.appcenter.ui.adapter.OneKeyInsallAdapter.CheckCountCallBack;
import com.prize.appcenter.ui.util.DisplayUtil;
import com.prize.appcenter.ui.util.UILimageUtil;
import com.prize.statistics.model.ExposureBean;

import java.util.ArrayList;
import java.util.List;

/**
 * 一键装机Fragment
 */
public class OneKeyFragment extends Fragment {

    private TextView mChooseText;
    private OneKeyInsallAdapter onKeyInsallAdapter;
    private int checkedCount;
    private int currentPage;
    public String title;
    public String iconUrl;
    public boolean isLastPage = false;


    public void setCheckedCount(int checkedCount) {
        this.checkedCount = checkedCount;
        setChooseText();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (onKeyInsallAdapter != null) {//// FIX:longbaoxiu 2016/12/8  友盟上出错

            Bundle bundle = new Bundle();
            bundle.putParcelableArrayList("checked_apps", onKeyInsallAdapter.getChecks());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        Context mContext = getActivity();
        View view = inflater.inflate(R.layout.fragment_onekey_install, container, false);
        mChooseText = (TextView) view.findViewById(R.id.choose_text);


        Bundle bundle = this.getArguments();
        String bg_color = "#" + bundle.getString("bg_color");
        int bgColor;
        try {
            bgColor = Color.parseColor(bg_color.trim());
        }catch (Exception e){
            JLog.i("OneKeyFragment", "e=" + e);
            bgColor=Color.parseColor("#42cf78");//防止后台配置出错时，默认浅绿色
        }
        title = bundle.getString("title");
        iconUrl = bundle.getString("icon_url");
        ArrayList<AppsItemBean> mDatas = bundle.getParcelableArrayList("data");
        int[] pageIndext = bundle.getIntArray("page_flag");
        currentPage = bundle.getInt("current_page");

        if (savedInstanceState != null) {
            ArrayList<AppsItemBean> checkedApps = savedInstanceState.getParcelableArrayList("checked_apps");
            if (checkedApps != null) {
                mDatas.addAll(checkedApps);
            }
        }

        ArrayList<AppsItemBean> mCheckedDatas = new ArrayList<AppsItemBean>();
        for (AppsItemBean bean : mDatas) {
            if (bean.isCheck) {
                mCheckedDatas.add(bean);
            }
        }

        ImageView dot1View = (ImageView) view.findViewById(R.id.dot1);
        ImageView dot2View = (ImageView) view.findViewById(R.id.dot2);
        ImageView dot3View = (ImageView) view.findViewById(R.id.dot3);
        ImageView dot4View = (ImageView) view.findViewById(R.id.dot4);
        ImageView dot5View = (ImageView) view.findViewById(R.id.dot5);

        ArrayList<ImageView> imageViews = new ArrayList<ImageView>();
        imageViews.add(dot1View);
        imageViews.add(dot2View);
        imageViews.add(dot3View);
        imageViews.add(dot4View);
        imageViews.add(dot5View);

        int removeCount = 0;
        for (int i = 0; i < pageIndext.length; i++) {
            if (pageIndext[i] == 0) {
                imageViews.get(i - removeCount).setVisibility(View.GONE);
                imageViews.remove(i - removeCount);
                removeCount++;
            }
        }

        if (imageViews.size() == 1) {
            imageViews.get(0).setVisibility(View.GONE);
        } else {
            GradientDrawable solidDotShape = new GradientDrawable();//实心圆点
            GradientDrawable hollowDotShape = new GradientDrawable();//空心圆点
            solidDotShape.setShape(GradientDrawable.OVAL);
            hollowDotShape.setShape(GradientDrawable.OVAL);
            //设置颜色
            //设置大小
            int size= (int) com.prize.app.util.DisplayUtil.forMatSpAndDp(TypedValue.COMPLEX_UNIT_DIP,6);
            int width= (int) com.prize.app.util.DisplayUtil.forMatSpAndDp(TypedValue.COMPLEX_UNIT_DIP,1);
            solidDotShape.setSize(size, size);
            solidDotShape.setColor(bgColor);//设置内部填充色

            hollowDotShape.setSize(size, size);
            hollowDotShape.setColor(Color.WHITE);//设置内部填充色
            hollowDotShape.setStroke(width,bgColor);//设置边框填充色
            for (int i = 0; i < imageViews.size(); i++) {
                if (i == currentPage) {
                    imageViews.get(i).setImageDrawable(solidDotShape);
                } else {
                    imageViews.get(i).setImageDrawable(hollowDotShape);
                }
            }
        }

        isLastPage = (currentPage == (imageViews.size() - 1));

        final float scale = mContext.getResources().getDisplayMetrics().density;

        int Radius = DisplayUtil.dip2px(scale, 20);
        float[] outerRadii = {Radius, Radius, Radius, Radius, Radius, Radius, Radius, Radius};
        ShapeDrawable nextButton_shapeDrawable = new ShapeDrawable();
        nextButton_shapeDrawable.setShape(new RoundRectShape(outerRadii, null, null));
        //nextButton_shapeDrawable.setPadding(pading_h, pading_v, pading_h, pading_v);

        nextButton_shapeDrawable.setIntrinsicWidth(DisplayUtil.dip2px(scale, 194));
        nextButton_shapeDrawable.setIntrinsicHeight(Radius);

        nextButton_shapeDrawable.getPaint().setColor(bgColor);


        RelativeLayout topLayout = (RelativeLayout) view.findViewById(R.id.top_view_ll);
        topLayout.setBackgroundColor(bgColor);
        topLayout.setId(R.id.top_view_ll);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, (int) (ClientInfo.getInstance().screenHeight * 0.101), 0, 0);
        params.addRule(RelativeLayout.CENTER_HORIZONTAL, R.id.top_view_ll);
        ImageView titleImg = (ImageView) view.findViewById(R.id.app_summary_img);
        if (!TextUtils.isEmpty(iconUrl)) {
            ImageLoader.getInstance().displayImage(iconUrl,
                    titleImg, UILimageUtil.getNoLoadLoptions(), null);
        }
        titleImg.setLayoutParams(params);
        RelativeLayout.LayoutParams pa = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        pa.setMargins(0, 0, 0, (int) (ClientInfo.getInstance().screenHeight * 0.0429));
        pa.addRule(RelativeLayout.CENTER_HORIZONTAL, R.id.top_view_ll);
        pa.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, R.id.top_view_ll);
        TextView titleTxt = (TextView) view.findViewById(R.id.app_summary_txt);
        titleTxt.setLayoutParams(pa);
        titleTxt.setText(title);

        //installAll = (TextView) view.findViewById(R.id.install_all);
        TextView next = (TextView) view.findViewById(R.id.next);
        next.setBackground(nextButton_shapeDrawable);
        next.setGravity(Gravity.CENTER);
        if (isLastPage) {
            next.setText(R.string.choose_done);
        } else {
            next.setText(R.string.next);
        }
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSelectInterface.onNextClick(isLastPage);
            }
        });

        mSelectInterface.onItemCheck(mCheckedDatas, currentPage);

        onKeyInsallAdapter = new OneKeyInsallAdapter(getActivity(), mDatas, currentPage);

        GridView dialog_gridview = (GridView) view.findViewById(R.id.dialog_gridview);
        dialog_gridview.setAdapter(onKeyInsallAdapter);

        onKeyInsallAdapter.setCheckCountCallBack(new CheckCountCallBack() {
            boolean isZeroCount = false;

            @Override
            public void countCallBack(ArrayList<AppsItemBean> checks) {
                int checkCount = checks.size();
                mSelectInterface.onItemCheck(checks, currentPage);
                if (checkCount == 0) {
                    isZeroCount = true;
                    //installAll.setEnabled(false);
                } else if (checkCount > 0 && isZeroCount) {
                    //installAll.setEnabled(true);
                    isZeroCount = false;
                }
            }
        });

        return view;

    }

    private void setChooseText() {
        if (isLastPage) {
            if (ClientInfo.networkType == ClientInfo.WIFI) {
                mChooseText.setText(this.getResources()
                        .getString(R.string.dialog_connect_wify, checkedCount));
            } else {
                mChooseText.setText(this.getResources().getString(
                        R.string.choose_text, checkedCount));
            }
        } else {
            mChooseText.setText(this.getResources()
                    .getString(R.string.choose_text,
                            checkedCount));
        }
    }

    public List<ExposureBean> getmExposureBeans() {
       if(onKeyInsallAdapter!=null){
          return onKeyInsallAdapter.getmExposureBeans();
       }
       return null;
    }

    // /**
    // * 数据流量下载对话框
    // */
    // private View.OnClickListener mDeletePromptListener = new
    // View.OnClickListener() {
    //
    // @Override
    // public void onClick(View v) {
    // oneKeyDown();
    // fadeToMainActivity();
    // }
    // };
    //

    // private ArrayList<AppsItemBean> filter(ArrayList<AppsItemBean> list) {
    // return filterInstallted(list);
    // }

    // /**
    // * 过滤 已安装的apk
    // */
    // public ArrayList<AppsItemBean> filterInstallted(
    // ArrayList<AppsItemBean> allApps) {
    // if (allApps == null)
    // return null;
    //
    // ArrayList<AppsItemBean> updateApps = new ArrayList<AppsItemBean>();
    // int size = allApps.size();
    // for (int i = 0; i < size; i++) {
    // AppsItemBean app = allApps.get(i);
    // if (app == null)
    // continue;
    //
    // int state = AppManagerCenter.getGameAppState(app.packageName,
    // String.valueOf(app.id), app.versionCode);
    // JLog.i("0000", "state="+state+"---app.packageName="+app.packageName);
    // if (state == AppManagerCenter.APP_STATE_INSTALLED
    // || AppManagerCenter.APP_STATE_UPDATE == state) {
    // continue;
    // }
    // updateApps.add(app);
    //
    // }
    //
    // return updateApps;
    // }

    private NextClickInterface mSelectInterface;

    public interface NextClickInterface {
        void onNextClick(boolean isLastPage);

        void onItemCheck(ArrayList<AppsItemBean> checks, int currentPage);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mSelectInterface = (NextClickInterface) activity;
        } catch (Exception e) {
            throw new ClassCastException(activity.toString() + "must implement OnArticleSelectedListener");
        }
    }

}
