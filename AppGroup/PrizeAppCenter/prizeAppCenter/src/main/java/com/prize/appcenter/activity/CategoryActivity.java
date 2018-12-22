/*
 * 版权所有©2015,深圳市铂睿智恒科技有限公司
 * <p/>
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

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;

import com.google.gson.Gson;
import com.prize.app.beans.TopicItemBean;
import com.prize.app.constants.Constants;
import com.prize.app.net.datasource.base.Categories;
import com.prize.app.net.datasource.base.CategoryContent;
import com.prize.app.util.MTAUtil;
import com.prize.app.util.WindowMangerUtils;
import com.prize.appcenter.R;
import com.prize.appcenter.bean.HomeCategoryData;
import com.prize.appcenter.receiver.PrizeXutilStringCallBack;
import com.prize.appcenter.ui.actionBar.ActionBarTabActivity;
import com.prize.appcenter.ui.adapter.AppsCategoryAdapter;
import com.prize.appcenter.ui.adapter.HotCategoryAdapter;
import com.prize.appcenter.ui.widget.ScrollLineGridView;
import com.prize.custmerxutils.XExtends;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;

import java.util.ArrayList;

/**
 * *
 * 分类
 *
 * @author 聂礼刚
 * @version V1.0
 */
public class CategoryActivity extends ActionBarTabActivity {
    private HotCategoryAdapter mHotCategoryAdapter;
    private AppsCategoryAdapter mAppsCategoryAdapter;
    private AppsCategoryAdapter mGamesCategoryAdapter;
    protected final String TAG = "SingleGameActivity";
    private Callback.Cancelable mCancelable;
    private HomeCategoryData data;
    private ScrollLineGridView mHotCategoryGridView;
    private ScrollLineGridView mAppsListView;
    private ScrollLineGridView mGamesListView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setNeedAddWaitingView(true);
        setContentView(R.layout.activity_home_category_layout);
        //Overdraw 的处理移除不必要的background
        getWindow().setBackgroundDrawable(null);
        WindowMangerUtils.changeStatus(getWindow());
        String title = getIntent().getStringExtra("title");
        if (!TextUtils.isEmpty(title)) {
            setTopicTitle(title);
        }
        findViewById();
        requestData();
    }


    private class AppGameListener implements AdapterView.OnItemClickListener {
        private ArrayList<Categories> data;
        private boolean isGame = false;

        public AppGameListener(ArrayList<Categories> data, boolean isGame) {
            this.data = data;
            this.isGame = isGame;
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view,
        int position, long id) {

            String parentID = data.get(position).id;
            String typeName = data.get(position).typeName;
            ArrayList<CategoryContent> tags = data.get(position).tags;

            if (typeName == null) {
                return;
            }

            Intent intent = new Intent(CategoryActivity.this,
                    CategoryAppGameListActivity.class);

            intent.putExtra(CategoryAppGameListActivity.parentID, parentID);
            intent.putExtra(CategoryAppGameListActivity.typeName, typeName);
            intent.putExtra(CategoryAppGameListActivity.tags, tags);
            intent.putExtra(CategoryAppGameListActivity.isGameKey, isGame);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            if (isGame) {
                MTAUtil.onClickGameCategoryHome(typeName);
            } else {
                MTAUtil.onClickAppCategoryHome(typeName);
            }

        }
    }

    private void findViewById() {

        mHotCategoryGridView = (ScrollLineGridView) findViewById(R.id.hot_cats_gv);
        mAppsListView = (ScrollLineGridView) findViewById(R.id.apps_list);
        mGamesListView = (ScrollLineGridView) findViewById(R.id.games_list);
    }

    @Override
    public String getActivityName() {
        return "CategoryActivity";
    }

    @Override
    public void onBack(int what, int arg1, int arg2, Object obj) {

    }

    private void requestData() {
        showWaiting();
        RequestParams params = new RequestParams(Constants.GIS_URL + "/category/homecatlist");
        mCancelable = XExtends.http().get(params, new PrizeXutilStringCallBack<String>() {
            @Override
            public void onSuccess(String result) {//SingGameResData
                hideWaiting();
                try {
                    JSONObject o = new JSONObject(result);
                    int code = o.getInt("code");
                    if (0 == code) {
                        String res = o.getString("data");
                        data = new Gson().fromJson(res, HomeCategoryData.class);

                        if(data != null) {
                            if (data.hot_topic != null && data.hot_topic.size() > 0) {
                                mHotCategoryAdapter = new HotCategoryAdapter(data.hot_topic, CategoryActivity.this);
                                mHotCategoryGridView.setAdapter(mHotCategoryAdapter);
                                mHotCategoryGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                                        TopicItemBean bean = new TopicItemBean();
                                        bean.title = data.hot_topic.get(position).title;
                                        bean.id = data.hot_topic.get(position).cid;
                                        Bundle b = new Bundle();
                                        b.putSerializable("bean", bean);
                                        Intent intent = new Intent(CategoryActivity.this,
                                                TopicDetailActivity.class);
                                        intent.putExtras(b);
                                        startActivity(intent);
                                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

                                        MTAUtil.onClickHomeCategoryTopic(bean.title);
                                    }
                                });
                            }

                            if (data.app_cat != null && data.app_cat.size() > 0) {
                                mAppsCategoryAdapter = new AppsCategoryAdapter(data.app_cat, CategoryActivity.this);
                                mAppsListView.setAdapter(mAppsCategoryAdapter);
                                mAppsListView.setOnItemClickListener(new AppGameListener(data.app_cat, false));
                            }

                            if (data.game_cat != null && data.game_cat.size() > 0) {
                                mGamesCategoryAdapter = new AppsCategoryAdapter(data.game_cat, CategoryActivity.this);
                                mGamesListView.setAdapter(mGamesCategoryAdapter);
                                mGamesListView.setOnItemClickListener(new AppGameListener(data.game_cat, true));
                            }
                        }

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    loadingFailed(new ReloadFunction() {

                        @Override
                        public void reload() {
                            requestData();
                        }

                    });
                }
            }


            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                hideWaiting();
                loadingFailed(new ReloadFunction() {

                    @Override
                    public void reload() {
                        requestData();
                    }

                });
            }

        });

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mCancelable != null) {
            mCancelable.cancel();
        }
    }
}
