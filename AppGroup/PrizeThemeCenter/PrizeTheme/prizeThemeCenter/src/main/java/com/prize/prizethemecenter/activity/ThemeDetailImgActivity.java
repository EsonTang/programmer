package com.prize.prizethemecenter.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.telecom.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.prize.prizethemecenter.R;
import com.prize.prizethemecenter.ui.utils.UILimageUtil;
import com.prize.prizethemecenter.ui.widget.FlowIndicator;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class ThemeDetailImgActivity extends FragmentActivity {

    @InjectView(R.id.viewpager_id)
    ViewPager viewpagerId;
    @InjectView(R.id.flowIndicator)
    FlowIndicator flowIndicator;
    private ImageLoader imageLoader;
    private ArrayList<ImageView> imgs;
    private MpagerAdapter mAdapter;

    private static String TAG = "bian";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_img);
        ButterKnife.inject(this);
        mAdapter = new MpagerAdapter();
        imgs = new ArrayList<>();
        ArrayList<String> paths = getIntent().getStringArrayListExtra("paths");
        int index = getIntent().getIntExtra("index", 0);
        Log.d(TAG, "onCreate "+paths.size());
        ArrayList<View> datas = new ArrayList<>();
        imageLoader = ImageLoader.getInstance();
        imageLoader.setRotate(true);

        if(paths != null && paths.size()>=0){
            for (int j = 0; j < paths.size(); j++){
                ImageView imageView = new ImageView(this);
                imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                imageView.setLayoutParams(new LayoutParams(
                        LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
                imageLoader.displayImage(paths.get(j), imageView, UILimageUtil.getFullScreenUILoptions(), null);
                datas.add(imageView);
                imgs.add(imageView);
            }
            flowIndicator.setCount(paths.size());
        }

        mAdapter.setData(datas);
        viewpagerId.setAdapter(mAdapter);

        viewpagerId.setCurrentItem(index);
        flowIndicator.setSeletion(index);
        viewpagerId.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                flowIndicator.setSeletion(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private class MpagerAdapter extends PagerAdapter {
        protected ArrayList<View> mViews;

        public MpagerAdapter() {
            mViews = new ArrayList<>();
        }

        public void setData(ArrayList<View> datas) {
            if (datas != null && datas.size() >= 0) {
                mViews.addAll(datas);
            }
        }

        @Override
        public int getCount() {
            return mViews.size();
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View view = mViews.get(position);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(mViews.get(position));
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    @Override
    protected void onDestroy() {
        if(imageLoader != null){
            for (ImageView view : imgs){
                imageLoader.cancelDisplayTask(view);
            }
        }
        super.onDestroy();
    }
}
