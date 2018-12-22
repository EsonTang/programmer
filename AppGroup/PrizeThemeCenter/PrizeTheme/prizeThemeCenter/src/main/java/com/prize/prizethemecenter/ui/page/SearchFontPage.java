package com.prize.prizethemecenter.ui.page;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.prize.prizethemecenter.MainApplication;
import com.prize.prizethemecenter.R;
import com.prize.prizethemecenter.activity.SearchActivity;
import com.prize.prizethemecenter.bean.AutoTipsData;
import com.prize.prizethemecenter.bean.SearchOriginData;
import com.prize.prizethemecenter.bean.SearchResultData.ResultData;
import com.prize.prizethemecenter.bean.table.SearchHistoryTable;
import com.prize.prizethemecenter.request.SearchResultRequest;
import com.prize.prizethemecenter.response.SearchResultResponse;
import com.prize.prizethemecenter.ui.adapter.AutoTipsAdapter;
import com.prize.prizethemecenter.ui.adapter.OriginAdapter;
import com.prize.prizethemecenter.ui.adapter.SearchFontAdapter;
import com.prize.prizethemecenter.ui.adapter.SearchHistoryAdapter;
import com.prize.prizethemecenter.ui.utils.ChangeWatchedManager;
import com.prize.prizethemecenter.ui.utils.CommonUtils;
import com.prize.prizethemecenter.ui.utils.HomeCacheUtils;
import com.prize.prizethemecenter.ui.utils.MTAUtil;
import com.prize.prizethemecenter.ui.utils.SearchRequest;
import com.prize.prizethemecenter.ui.utils.SearchResult.Watcher;
import com.prize.prizethemecenter.ui.utils.SearchResult.WatcherChange;
import com.prize.prizethemecenter.ui.utils.ToastUtils;
import com.prize.prizethemecenter.ui.utils.UIUtils;
import com.prize.prizethemecenter.ui.utils.WatchedManager;
import com.prize.prizethemecenter.ui.widget.GridViewWithHeaderAndFooter;
import com.prize.prizethemecenter.ui.widget.ScrollGridView;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.db.sqlite.WhereBuilder;
import org.xutils.ex.DbException;
import org.xutils.x;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/9/8.
 */
public class SearchFontPage extends BasePage implements Watcher ,WatcherChange{

    private SearchActivity activity;
    /**搜索历史记录*/
    private ListView search_history_lv;
    /**搜索自动提示*/
    private ListView search_tips_lv;
    /**搜索origin*/
    private ScrollGridView hotWords_GV;
    /**搜索结果*/
    private GridViewWithHeaderAndFooter search_font_result_gv;

    private SearchHistoryAdapter historyAdapter;
    private AutoTipsAdapter autoTipsAdapter;
    private OriginAdapter originAdapter;
    private SearchFontAdapter searchFontAdapter;

    private static SearchResultRequest searchResultRequest;
    private static SearchResultResponse searchResultResponse;
    private static Callback.Cancelable mHandler;

    private TextView search_result;
    private ImageView search_no_result;

    /** 清空历史 */
    private TextView clear_history;

    private String keyTips;

    /**判断是否显示搜索结果*/
    private boolean hasResult = false;

    private View view;

    /**是否失败过*/
    private boolean isFailEver = false;

    public SearchFontPage(SearchActivity activity) {
        super(activity);
        this.activity = (SearchActivity) activity;
        WatchedManager.registObserver(this);
        ChangeWatchedManager.registObserver(this);
    }

    @Override
    public View onCreateView() {
        LayoutInflater inflater = LayoutInflater.from(activity);
        if(view==null){
            view = inflater.inflate(R.layout.search_theme_layout,null);
        }
        View headView = inflater.inflate(R.layout.search_history_head_layout,null);
        View resulthead = inflater.inflate(R.layout.search_result_head_layout,null);
        search_result = (TextView) resulthead.findViewById(R.id.search_result);
        search_no_result = (ImageView)resulthead.findViewById(R.id.search_no_result);

        search_history_lv = (ListView) view.findViewById(R.id.search_history_lv);
        search_history_lv.addHeaderView(headView);
        historyAdapter = new SearchHistoryAdapter(activity);
        search_history_lv.setAdapter(historyAdapter);

        clear_history = (TextView)headView.findViewById(R.id.clear_history);

        search_tips_lv = (ListView) view.findViewById(R.id.search_tips_lv);
        autoTipsAdapter = new AutoTipsAdapter(activity);
        search_tips_lv.setAdapter(autoTipsAdapter);

        hotWords_GV = (ScrollGridView) view.findViewById(R.id.hotWords_GV);
        originAdapter = new OriginAdapter(activity);
        hotWords_GV.setAdapter(originAdapter);

        search_font_result_gv = (GridViewWithHeaderAndFooter) view.findViewById(R.id.search_font_result_gv);
        search_font_result_gv.addHeaderView(resulthead);
        searchFontAdapter = new SearchFontAdapter(activity);
        searchFontAdapter.setIsActivity(true);
        searchFontAdapter.setDownlaodRefreshHandle();
        search_font_result_gv.setAdapter(searchFontAdapter);

        setListener();
        return view;
    }

    private void setListener() {
        clear_history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    WhereBuilder builder = WhereBuilder.b();
                    builder.and("type","==","font");
                    MainApplication.getDbManager().delete(SearchHistoryTable.class,builder);
                } catch (DbException e) {
                    e.printStackTrace();
                }
                search_history_lv.setVisibility(View.INVISIBLE);
                historyAdapter.clearAll();
            }
        });
        /**history onclick**/
        search_history_lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (historyAdapter.getItem(position-1) != null) {
                    SearchHistoryTable bean = historyAdapter.getItem(position-1);
                    if (!TextUtils.isEmpty(bean.word)) {
                        activity.searchView.setTextForEditText(bean.word);
                    }
                }
            }
        });

        /**original search onclick**/
        hotWords_GV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (originAdapter.getItem(position) != null) {
                    SearchOriginData.OriginData bean = originAdapter.getItem(position);
                    if (!TextUtils.isEmpty(bean.name)) {
                        activity.searchView.setTextForEditText(bean.name);
                        MTAUtil.onSearchPageFontKey(originAdapter.getItem(position).name);
                    }
                }
            }
        });

        /**tips search onclick**/
        search_tips_lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (autoTipsAdapter.getItem(position) != null) {
                    AutoTipsData.AutoTip bean = autoTipsAdapter.getItem(position);
                    if (!TextUtils.isEmpty(bean.suggestion)) {
                        activity.searchView.setTextForEditText(bean.suggestion);
                    }
                }
            }
        });

        /**result search onclick**/
        search_font_result_gv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (searchFontAdapter.getItem(position) != null) {
                    ResultData bean = searchFontAdapter.getItem(position);
                    UIUtils.gotoFontDetail(bean.id,searchFontAdapter.getItem(position).ad_pictrue,false);
                }
            }
        });
    }

    private String lastKeyTips = null;
    @Override
    public void loadData() {
        if(!TextUtils.isEmpty(keyTips)){
            lastKeyTips = keyTips;
            search_history_lv.setVisibility(View.GONE);
            search_tips_lv.setVisibility(View.GONE);
            hotWords_GV.setVisibility(View.GONE);
            search_font_result_gv.setVisibility(View.VISIBLE);
            if(!keyTips.equals(lastKeyTips) || isFailEver){
                searchFontAdapter.clearAll();
                requestResultData(keyTips);
            }
        }else {
            if(hasResult){
                search_history_lv.setVisibility(View.GONE);
                search_tips_lv.setVisibility(View.GONE);
                hotWords_GV.setVisibility(View.GONE);
                search_font_result_gv.setVisibility(View.VISIBLE);
            }else {
                SearchRequest.loadOriginData("2",originAdapter);
                search_history_lv.setVisibility(View.GONE);
                search_tips_lv.setVisibility(View.GONE);
                hotWords_GV.setVisibility(View.VISIBLE);
                search_font_result_gv.setVisibility(View.GONE);
            }
        }
    }

    public void setEditHint(String hint){
        updateNotify(hint);
    }

    @Override
    public void updateNotify(String keyWord) {
        if(!TextUtils.isEmpty(keyWord)){
            search_history_lv.setVisibility(View.GONE);
            search_tips_lv.setVisibility(View.GONE);
            hotWords_GV.setVisibility(View.GONE);
            search_font_result_gv.setVisibility(View.VISIBLE);
            searchFontAdapter.clearAll();
            requestResultData(keyWord);
        }
    }

    public void requestResultData(String keyWord) {
        searchResultRequest = new SearchResultRequest();
        searchResultRequest.query = keyWord;
        searchResultRequest.type = "2";
        mHandler = x.http().post(searchResultRequest, new Callback.CommonCallback<String>() {

            @Override
            public void onSuccess(String result) {
                try {
                    JSONObject obj = new JSONObject(result);
                    if(obj.getInt("code")==00000){
                        hasResult = true;
                        isFailEver = false;
                        searchResultResponse = CommonUtils.getObject(result,
                                SearchResultResponse.class);
                        if(searchResultResponse==null){
                            search_result.setVisibility(View.GONE);
                            search_no_result.setVisibility(View.VISIBLE);
                        }else{
                            ArrayList<ResultData> data = searchResultResponse.data.tag;
                            searchFontAdapter.setData(data);
                            search_result.setVisibility(View.VISIBLE);
                            search_no_result.setVisibility(View.GONE);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                hideWaiting();
                if (null != searchFontAdapter && searchFontAdapter.getCount() == 0) {
                    loadingFailed(new ReloadFunction() {

                        @Override
                        public void reload() {
                            isFailEver = true;
                            loadData();
                        }
                    });
                } else {
                    ToastUtils.showToast(R.string.net_error);
                }
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
    public void updateTips(String tips) {
        if(!TextUtils.isEmpty(tips)){
            search_history_lv.setVisibility(View.GONE);
            hotWords_GV.setVisibility(View.GONE);
            search_tips_lv.setVisibility(View.VISIBLE);
            search_font_result_gv.setVisibility(View.GONE);
            autoTipsAdapter.clearAll();
            SearchRequest.loadAutoTips("2",tips,autoTipsAdapter);
        }else{
            search_history_lv.setVisibility(View.GONE);
            search_tips_lv.setVisibility(View.GONE);
            if(hasResult){
                hotWords_GV.setVisibility(View.GONE);
                search_font_result_gv.setVisibility(View.VISIBLE);
            }else{
                hotWords_GV.setVisibility(View.VISIBLE);
                search_font_result_gv.setVisibility(View.GONE);
            }
        }
        keyTips = tips;
    }

    @Override
    public void showHistory() {
        hotWords_GV.setVisibility(View.GONE);
        search_tips_lv.setVisibility(View.GONE);
        search_font_result_gv.setVisibility(View.GONE);
        historyAdapter.clearAll();
        try {
             List<SearchHistoryTable> list =  MainApplication.getDbManager().selector(SearchHistoryTable.class).
                     where("type","==","font").orderBy("timestamp",true).findAll();
             if(list!=null && list.size()>0){
                 search_history_lv.setVisibility(View.VISIBLE);
                 historyAdapter.addData(list);
             }
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void addToHistory(String text) {
        if(!TextUtils.isEmpty(text)) {
            HomeCacheUtils.addToHistory(text, "font");
        }
    }

    @Override
    public void onActivityCreated() {
    }

    @Override
    public String getPageName() {
        return "SearchThemePage";
    }

    @Override
    public void onDestroy() {
        if(mHandler!=null){
            mHandler .cancel();
        }
        if(searchFontAdapter!=null){
            searchFontAdapter.setIsActivity(false);
            searchFontAdapter.removeDownLoadHandler();
        }
        WatchedManager.unregistObserver(this);
        ChangeWatchedManager.unregistObserver(this);
    }
}
