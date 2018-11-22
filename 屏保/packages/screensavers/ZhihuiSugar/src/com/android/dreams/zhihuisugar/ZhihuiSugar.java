package com.android.dreams.zhihuisugar;


import android.service.dreams.DreamService;
import android.view.View;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.ViewSwitcher;

public class ZhihuiSugar extends DreamService {
    private ImageSwitcher IS_Older;
    int[] images = {R.drawable.sugar1, R.drawable.sugar1, R.drawable.sugar2, R.drawable.sugar3, R.drawable.sugar4, R.drawable.sugar5, R.drawable.sugar6, R.drawable.sugar7
            , R.drawable.sugar8, R.drawable.sugar9, R.drawable.sugar10, R.drawable.sugar11, R.drawable.sugar12, R.drawable.sugar3, R.drawable.sugar14, R.drawable.sugar15, R.drawable.sugar16};
    private int currentIndex = 0;

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();

        setContentView(R.layout.main);

        setFullscreen(true);
        IS_Older = (ImageSwitcher) findViewById(R.id.IS_Older);

        IS_Older.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                return new ImageView(ZhihuiSugar.this);
            }
        });


        IS_Older.postDelayed(new Runnable() {
            int i = 0;

            @Override
            public void run() {
                IS_Older.setImageResource(images[currentIndex]);
                if (currentIndex == (images.length - 1))
                    currentIndex = 0;
                else
                    currentIndex++;
                IS_Older.postDelayed(this, 500);
            }
        }, 1000);
    }

}

