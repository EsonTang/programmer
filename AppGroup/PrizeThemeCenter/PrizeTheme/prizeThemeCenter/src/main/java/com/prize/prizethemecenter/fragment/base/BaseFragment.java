package com.prize.prizethemecenter.fragment.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

/**
 * Created by Administrator on 2016/9/8.
 */
public abstract class BaseFragment extends Fragment{

    private LinearLayout mFrameLayout;
    private View view;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final FragmentActivity activity = getActivity();
        mFrameLayout = new LinearLayout(activity);
        view = onCreateContentView(inflater, mFrameLayout);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }
    public abstract View onCreateContentView(LayoutInflater inflater,ViewGroup container);


}
