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

package com.prize.appcenter.activity;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.prize.appcenter.R;
import com.prize.appcenter.ui.util.ImageUtil;
import com.prize.appcenter.ui.util.UILimageUtil;
import com.viewpagerindicator.CirclePageIndicator;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * 类描述：展示详情大图activity
 *
 * @author huanglinglun
 * @version 版本
 */
public class AppDetailImgActivity extends FragmentActivity {
    private CirclePageIndicator mCirclePageIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_img);
        ViewPager mViewPager = (ViewPager) findViewById(R.id.viewpager_id);
        mCirclePageIndicator = (CirclePageIndicator) findViewById(R.id.circlePageIndicator);
        MpagerAdapter mAdapter = new MpagerAdapter();
        String[] paths = getIntent().getStringArrayExtra("paths");
        WeakReference<AppDetailImgActivity> reference = new WeakReference<>(this);
        Activity activity = reference.get();
        if (activity == null) {
            finish();
            return;
        }
        int index = getIntent().getIntExtra("index", 0);
        ArrayList<View> datas = new ArrayList<>();
        if (paths != null && paths.length >= 0) {
            LayoutParams param = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            for (int j = 0; j < paths.length; j++) {
                if (activity == null)
                    return;
                final ImageView mImageView = new ImageView(activity);
                mImageView.setLayoutParams(param);
                ImageLoader.getInstance().displayImage(paths[j], mImageView,
                        UILimageUtil.getFullScreenUILoptions(), new MyImageLoadingListener());
                datas.add(mImageView);
            }
        }
        mAdapter.setData(datas);
        mViewPager.setAdapter(mAdapter);
        mCirclePageIndicator.setViewPager(mViewPager, index);
        mViewPager.setCurrentItem(index);
        mViewPager.setOnPageChangeListener(new OnPageChangeListener() {

            @Override
            public void onPageSelected(int arg0) {
                mCirclePageIndicator.setCurrentItem(arg0);
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
            }
        });
    }

    private static class MyImageLoadingListener implements ImageLoadingListener {

        @Override
        public void onLoadingStarted(String imageUri, View view) {

        }

        @Override
        public void onLoadingFailed(String imageUri, View view, FailReason failReason) {

        }

        @Override
        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
            if (loadedImage == null) {
                return;
            }
            int width = loadedImage.getWidth();
            int height = loadedImage.getHeight();
            if (loadedImage != null && view != null
                    && width > height) {
                Bitmap bitmap = ImageUtil.adjustPhotoRotation(loadedImage, 90);
                if (bitmap != null) {
                    ((ImageView) view).setImageBitmap(bitmap);
                }
            }
        }

        @Override
        public void onLoadingCancelled(String imageUri, View view) {

        }
    }

    private class MpagerAdapter extends PagerAdapter {

        ArrayList<View> mViews;

        MpagerAdapter() {
            mViews = new ArrayList<>();
        }

        public void setData(ArrayList<View> datas) {
            if (datas != null && datas.size() >= 0) {
                mViews.addAll(datas);
            }
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View view = mViews.get(position);
            view.setOnClickListener(new OnClickListener() {

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
        public int getCount() {

            return mViews.size();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }
}
