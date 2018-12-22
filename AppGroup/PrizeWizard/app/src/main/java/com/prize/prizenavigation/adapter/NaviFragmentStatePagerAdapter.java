package com.prize.prizenavigation.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.ViewGroup;

import com.prize.prizenavigation.bean.NaviDatas;
import com.prize.prizenavigation.fragment.NaviFragment;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 详情fragment适配器
 * Created by liukun on 2017/3/5.
 */
public class NaviFragmentStatePagerAdapter<T extends Serializable> extends FragmentStatePagerAdapter {

    private List<NaviFragment> mFragmentList = new ArrayList<NaviFragment>();

    private FragmentManager fragmentManager;

    public NaviFragmentStatePagerAdapter(FragmentManager fm) {
        super(fm);
        this.fragmentManager = fm;
    }

    /**
     * 初始化适配器数据源
     *
     * @param list
     */
    public void init(List<NaviDatas.ListBean> list) {
        if (list!=null&&list.size()>0) {
            int total=list.size();
            for (NaviDatas.ListBean info : list) {
                mFragmentList.add(NaviFragment.getInstance(info,total));
            }
            notifyDataSetChanged();
        }
    }

    /**
     * 根据title刷新对应的fragment
     *
     * @param list
     */
    public void refreshAllFragment(List<NaviDatas.ListBean> list) {
        if (list!=null &&list.size()>0) {
            for (NaviDatas.ListBean info : list) {
                for (NaviFragment fragment : mFragmentList) {
                    //最好使用唯一标示来判定是否刷了正确的Fragment 比如id
                    int fragmentId = fragment.getFragmentId();
                    if (fragmentId != 0 && fragmentId == info.getPagenum()) {
                        fragment.refreshData(info);
                    }
                }

            }
            notifyDataSetChanged();
        }
    }

    @Override
    public Fragment getItem(int position) {
        if (mFragmentList != null && position < mFragmentList.size())
            return mFragmentList.get(position);
        else
            return null;
//        return NaviFragment.getInstance((TestBeans.DataBean.ListBean) mPagerList.get(position));
    }

    @Override
    public int getCount() {
        return mFragmentList.size();
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        return super.instantiateItem(container, position);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        super.destroyItem(container, position, object);
    }

    //    @Override
//    public Object instantiateItem(ViewGroup container, int position) {
//        Fragment fragment = (Fragment) super.instantiateItem(container, position);
//        this.fragmentManager.beginTransaction().show(fragment). commitAllowingStateLoss();
//        return fragment;
//    }
//
//    @Override
//    public void destroyItem(ViewGroup container, int position, Object object) {
////        super.destroyItem(container, position, object);
//        Fragment fragment = mFragmentList.get(position);
//        this.fragmentManager.beginTransaction().hide(fragment).commit();
//        //回收图片，释放内存
////        ViewPager vpContainer = (ViewPager)container;
////        View view = vpContainer.getChildAt(position);
////        if(view!=null){
////            NotRecycledImageView imageView = (NotRecycledImageView)view.findViewById(R.id.vp_img);
////            releaseImageViewResouce(imageView);
////        }
//    }
//    /**
//     * 释放图片资源
//     * @param imageView
//     */
//    public void releaseImageViewResouce(ImageView imageView) {
//        if (imageView == null) return;
//        Drawable drawable = imageView.getDrawable();
//        if (drawable != null && drawable instanceof GlideBitmapDrawable) {
//            GlideBitmapDrawable bitmapDrawable = (GlideBitmapDrawable) drawable;
//            Bitmap bitmap = bitmapDrawable.getBitmap();
//            boolean b = !bitmap.isRecycled();
//            if (bitmap != null && !bitmap.isRecycled()) {
//                bitmap.recycle();
//                bitmap=null;
//            }
//        }
//        System.gc();
//    }
}
