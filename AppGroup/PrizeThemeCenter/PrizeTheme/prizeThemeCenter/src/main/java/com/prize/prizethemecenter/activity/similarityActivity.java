package com.prize.prizethemecenter.activity;

import android.os.Bundle;
import android.telecom.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.LinearLayout;

import com.prize.prizethemecenter.R;
import com.prize.prizethemecenter.bean.SearchSimilartyData;
import com.prize.prizethemecenter.request.SimilarityThemeRequest;
import com.prize.prizethemecenter.response.SearchSimilartyResponse;
import com.prize.prizethemecenter.ui.actionbar.ActionBarNoTabActivity;
import com.prize.prizethemecenter.ui.adapter.SimilartyThemeListAdapter;
import com.prize.prizethemecenter.ui.utils.CommonUtils;
import com.prize.prizethemecenter.ui.utils.UIUtils;
import com.prize.prizethemecenter.ui.utils.WindowMangerUtils;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.x;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class similarityActivity extends ActionBarNoTabActivity implements AdapterView.OnItemClickListener {


    @InjectView(R.id.gv_theme)
    GridView gvTheme;
    @InjectView(R.id.container_wait)
    FrameLayout containerWait;
    @InjectView(R.id.container_reload)
    FrameLayout containerReload;
    private SearchSimilartyResponse response;
    private SimilarityThemeRequest request;
    private String tag;
    private String id;
    private JSONObject obj;

    private SimilartyThemeListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        WindowMangerUtils.initStateBar(window, this);
        setContentView(R.layout.activity_similarity);
        ButterKnife.inject(this);
        WindowMangerUtils.changeStatus(window);
        if (getIntent() != null) {
            tag = getIntent().getStringExtra("name");
            id = getIntent().getStringExtra("id");
        }
        adapter = new SimilartyThemeListAdapter(this, false);
        if (tag.contains(",")) {
            setTitle(this.getResources().getString(R.string.similary_theme_label));
        } else {
            setTitle(tag);
        }
        initLoadVIew();
        initData();
    }

    private void initLoadVIew() {
        View waiting_view = LayoutInflater.from(this).inflate(R.layout.waiting_view, null);
        View reload_layout = LayoutInflater.from(this).inflate(R.layout.reload_layout, null);
        LinearLayout loadingView = (LinearLayout) waiting_view.findViewById(R.id.loading_Llyt_id);
        LinearLayout reloadView = (LinearLayout) reload_layout.findViewById(R.id.reload_Llyt);
        loadingView.setGravity(Gravity.CENTER);
        reloadView.setGravity(Gravity.CENTER);
        containerWait.addView(waiting_view, FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        containerReload.addView(reload_layout, FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        gvTheme.setNumColumns(3);
        gvTheme.setOnItemClickListener(this);
    }

    private void initData() {
        if (response == null) {
            containerWait.setVisibility(View.VISIBLE);
            showWaiting();
            loadData();
        } else {
            containerWait.setVisibility(View.GONE);
            hideWaiting();
            gvTheme.setVisibility(View.VISIBLE);
        }
    }

    private void loadData() {
        request = new SimilarityThemeRequest();
        request.tag = tag;
        x.http().post(request, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    obj = new JSONObject(result);
                    if (obj.getInt("code") == 00000) {
                        containerWait.setVisibility(View.GONE);
                        hideWaiting();
                        response = CommonUtils.getObject(result, SearchSimilartyResponse.class);
                        List<SearchSimilartyData.TagBean> tags = response.data.getTag();
                        for (int i = 0; i < tags.size(); i++) {
                            SearchSimilartyData.TagBean tagBean = tags.get(i);
                            if (tagBean.getId().equals(id))
                                tags.remove(i);
                        }
                        adapter.setData(tags);
                        gvTheme.setAdapter(adapter);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {

            }

            @Override
            public void onCancelled(CancelledException cex) {

            }

            @Override
            public void onFinished() {

            }
        });
    }

    @Override
    public String getActivityName() {
        return null;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        SearchSimilartyData.TagBean item = adapter.getItem(position);
        Log.d("bian", "onItemClick " + item.getId());
        if (item.getId() != null&&item.getAd_pictrue()!=null) UIUtils.gotoThemeDetail(item.getId(), item.getAd_pictrue());
    }
}
