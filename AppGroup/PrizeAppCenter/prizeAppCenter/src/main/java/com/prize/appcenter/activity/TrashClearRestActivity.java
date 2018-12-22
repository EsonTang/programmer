
package com.prize.appcenter.activity;


import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.prize.appcenter.R;

/**
 * 垃圾清理一分钟内再次清理提示页面
 */
public class TrashClearRestActivity extends Activity {

    private ImageView backImg;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //沉浸式状态栏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        setContentView(R.layout.clear_trash_rest);
        backImg = (ImageView) findViewById(R.id.back_im);
        backImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }
}
