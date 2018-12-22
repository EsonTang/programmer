
package com.prize.appcenter.activity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ExpandableListView;
import android.widget.ImageView;

import com.prize.appcenter.R;
import com.prize.appcenter.ui.adapter.TrashClearListAdapter;
import com.prize.appcenter.ui.adapter.TrashClearListAdapter.OnClickCallback;
import com.prize.qihoo.cleandroid.sdk.TrashClearSDKHelper;
import com.qihoo360.mobilesafe.opti.i.trashclear.TrashClearCategory;
import com.qihoo360.mobilesafe.opti.i.trashclear.TrashInfo;

import java.util.List;

/**
 * 垃圾清理界面
 */
public class SafeTrashClearActivity extends Activity implements OnClickCallback {

    private ExpandableListView mExpandableListView;

    private Context mContext;

    private ImageView mBackImageView;

    private TrashClearSDKHelper mTrashClearHelper;

    private TrashClearListAdapter mAdapter;
    
    private List<TrashClearCategory> trashList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //沉浸式状态栏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        setContentView(R.layout.safe_clear_trash);
        mContext = this;

        mBackImageView = (ImageView) findViewById(R.id.back_im);
        mBackImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        mExpandableListView = (ExpandableListView) findViewById(R.id.list);
        
        mTrashClearHelper = TrashClearSDKHelper.getInstance(getApplicationContext());
        
        /*Intent intent = getIntent();
        if(intent != null){
        	TrashClearCategory[] data = (TrashClearCategory[]) intent.getExtra("trashList");
        	trashList = Arrays.asList(data);
        }*/
        trashList = mTrashClearHelper.getSafeTrashClearCategoryList();
        refreshView();
    }

    private void refreshView() {
        if (mAdapter == null) {
            mAdapter = new TrashClearListAdapter(mContext, trashList);
            /*for (TrashClearCategory category : trashList) {
                category.isSelectedAll = false;
                mTrashClearHelper.onTrashClearCategorySelectedChanged(category);
            }*/
            mExpandableListView.setAdapter(mAdapter);
            mAdapter.setOnClickCallback(this);
        } else {
            trashList = mTrashClearHelper.getSafeTrashClearCategoryList();
            mAdapter.refresh(trashList);
        }
    }

    @Override
    public void onTrashInfoSelectedChanged(TrashInfo trashInfo) {
        mTrashClearHelper.onTrashInfoSelectedChanged(trashInfo);
        refreshView();
    }

    @Override
    public void onCategorySelectedChanged(TrashClearCategory trashClearCategory) {
        mTrashClearHelper.onTrashClearCategorySelectedChanged(trashClearCategory);
        refreshView();
    }
}
