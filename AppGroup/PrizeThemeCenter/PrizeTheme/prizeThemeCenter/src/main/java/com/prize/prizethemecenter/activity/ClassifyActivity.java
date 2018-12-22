package com.prize.prizethemecenter.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.SimpleAdapter;

import com.prize.prizethemecenter.R;
import com.prize.prizethemecenter.ui.actionbar.ActionBarNoTabActivity;
import com.prize.prizethemecenter.ui.utils.MTAUtil;
import com.prize.prizethemecenter.ui.utils.UIUtils;
import com.prize.prizethemecenter.ui.utils.WindowMangerUtils;
import com.prize.prizethemecenter.ui.widget.view.ScollerGridView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 主題分类
 * Created by pengy on 2016/9/6.
 */
public class ClassifyActivity extends ActionBarNoTabActivity{

    private static final String TAG = "ClassifyActivity";
    private ScollerGridView classify_gv;

    private int[] imageData = { R.drawable.theme_top,
            R.drawable.theme_shouhui, R.drawable.theme_dongman,
            R.drawable.theme_wenyi,R.drawable.theme_yiwu,
            R.drawable.theme_jianyue, R.drawable.theme_xuanku,
            R.drawable.theme_aiqing};

    private String[] names = {"精品","手绘","动漫","文艺","拟物","简约","炫酷","爱情"};


    private int[] wallpaperImageData = { R.drawable.icon_wallpaper_01,
            R.drawable.icon_wallpaper_02, R.drawable.icon_wallpaper_03,
            R.drawable.icon_wallpaper_04, R.drawable.icon_wallpaper_05,
            R.drawable.icon_wallpaper_08, R.drawable.icon_wallpaper_09,
            R.drawable.icon_wallpaper_10};

    private String[] wallpaperNames = {"风景","美女","爱情","萌宠","名车","影视","其他","节日"};
    private String page;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowMangerUtils.initStateBar(getWindow(), this);
        setContentView(R.layout.classify_layout);
        WindowMangerUtils.changeStatus(getWindow());

        page = getIntent().getStringExtra("page");
        if(page.equals("theme")){
            MTAUtil.onClickClassify("主题");
        }else{
            MTAUtil.onClickClassify("壁纸");
        }
        setTitle(R.string.recommend_classify);
        classify_gv = (ScollerGridView) findViewById(R.id.classify_gv);
        View someView = this.findViewById(R.id.classify_gv);
        View root = someView.getRootView();
        root.setBackgroundColor(getResources().getColor(android.R.color.white));
        classify_gv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if("wallpaper".equals(page)){
                    UIUtils.onClickClassifyItem(String.valueOf((position+21)),wallpaperNames[position],ClassifyActivity.this,page);
                }else {
                    UIUtils.onClickClassifyItem(String.valueOf((position+1)),names[position],ClassifyActivity.this,page);
                }
            }
        });

        List<Map<String, Object>> listems = new ArrayList<>();
        for (int i = 0; i < imageData.length; i++) {
            Map<String, Object> listem = new HashMap<>();
            listem.put("head", imageData[i]);
            listems.add(listem);
        }
        SimpleAdapter simpleAdapter = new SimpleAdapter(this,listems,R.layout.classify_item_layout,new String[] {"head"},
                new int[] {R.id.classify_img});

        List<Map<String, Object>> lists = new ArrayList<>();
        for (int i = 0; i < wallpaperImageData.length; i++) {
            Map<String, Object> list = new HashMap<>();
            list.put("head", wallpaperImageData[i]);
            lists.add(list);
        }
        SimpleAdapter wallpaperSimpleAdapter = new SimpleAdapter(this,lists,R.layout.classify_item_layout,new String[] {"head"},
                new int[] {R.id.classify_img});
        if("wallpaper".equals(page)){
            classify_gv.setAdapter(wallpaperSimpleAdapter);
        }else {
            classify_gv.setAdapter(simpleAdapter);
        }
    }

    @Override
        public String getActivityName() {
            return null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
