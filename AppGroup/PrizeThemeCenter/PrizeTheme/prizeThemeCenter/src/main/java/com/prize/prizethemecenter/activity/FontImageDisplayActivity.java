package com.prize.prizethemecenter.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.prize.prizethemecenter.R;
import com.prize.prizethemecenter.ui.utils.UILimageUtil;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * 字体大图显示类
 * Created by pengy on 2016/11/3.
 */
public class FontImageDisplayActivity extends FragmentActivity {

    @InjectView(R.id.display_IV)
    ImageView displayIV;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //隐藏标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // 隐藏状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.font_img_display_layout);
        ButterKnife.inject(this);
        String paths = null;
        if(getIntent()!=null){
            paths = getIntent().getStringExtra("paths");
        }
        ImageLoader.getInstance().displayImage(paths,displayIV, UILimageUtil.getFullScreenUILoptions(),null);
        displayIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }
}
