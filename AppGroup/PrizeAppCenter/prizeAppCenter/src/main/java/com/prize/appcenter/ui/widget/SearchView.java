/*
 * 版权所有©2015,深圳市铂睿智恒科技有限公司
 * <p>
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

package com.prize.appcenter.ui.widget;

import android.content.Context;
import android.os.Handler;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.prize.app.constants.Constants;
import com.prize.app.database.dao.SearchHistoryDao;
import com.prize.app.download.IUIDownLoadListenerImp;
import com.prize.app.download.IUIDownLoadListenerImp.IUIDownLoadCallBack;
import com.prize.app.net.datasource.base.AppsItemBean;
import com.prize.app.net.datasource.base.PrizeMatchKeyTypeData;
import com.prize.app.util.CommonUtils;
import com.prize.app.util.GsonParseUtils;
import com.prize.app.util.JLog;
import com.prize.app.util.MTAUtil;
import com.prize.appcenter.R;
import com.prize.appcenter.activity.RootActivity;
import com.prize.appcenter.activity.SearchActivity;
import com.prize.appcenter.receiver.PrizeXutilStringCallBack;
import com.prize.appcenter.ui.adapter.MatchSearchRecordAdapter;
import com.prize.appcenter.ui.datamgr.DataManagerCallBack;
import com.prize.appcenter.ui.util.AIDLUtils;
import com.prize.appcenter.ui.util.ToastUtils;
import com.prize.appcenter.ui.util.UIUtils;
import com.prize.custmerxutils.XExtends;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;

import java.util.ArrayList;

/**
 * *
 * 带有搜索联想的搜索框
 *
 * @author longbaoxiu
 * @version V1.0
 */
public class SearchView extends LinearLayout implements OnClickListener,
        DataManagerCallBack {
    private final String TAG = "SearchView";
    /**
     * 输入框
     */
    private EditText etInput;

    /**
     * 删除键
     */
    private ImageView ivDelete;

//    /**
//     * 返回按钮
//     */
//    private ImageView btnBack;
//    /**
//     * 搜索按钮
//     */
//    private ImageView search_btn;

    /**
     * 上下文对象
     */
    private RootActivity mContext;

    /**
     * 弹出列表
     */
    private ListView lvTips;
    /*** 头布局的搜索记录listView ****/
    private ListViewForScrollView mListView;
    private String[] items;
    /**
     * 搜索回调接口
     */
    private SearchViewListener mListener;

    private Callback.Cancelable mCancelable;
    private MatchAdapter adapter;
    private View headerView;
    private AppsItemBean bean = new AppsItemBean();
    private IUIDownLoadListenerImp listener = null;
    private boolean isActivity = true; // 默认true
    /*** 当前关键字 */
    private String currentText = "";
    private MatchSearchRecordAdapter mSearchRecordAdapter;
    private ArrayList<String> list = new ArrayList<String>();
    private SearchHeaderView mSearchHeaderView;
    private Handler mHandler = new Handler();

    public void setHint(String hint) {
        if (!TextUtils.isEmpty(hint) && etInput != null) {
            etInput.setHint(hint);
        }
    }

    public void setSearchViewListener(SearchViewListener listener) {
        mListener = listener;
    }

    public SearchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = (RootActivity) context;
        setOrientation(VERTICAL);
        LayoutInflater.from(context).inflate(R.layout.search_layout, this);
        headerView = LayoutInflater.from(context).inflate(
                R.layout.search_match_header_layout, null);
        adapter = new MatchAdapter(items);
        initHeadView();
        initViews();
        lvTips.addHeaderView(headerView);
        lvTips.setAdapter(adapter);
        listener = IUIDownLoadListenerImp.getInstance();
        listener.setmCallBack(new IUIDownLoadCallBack() {

            @Override
            public void callBack(final String pkgName, int state, boolean isNewDownload) {
                if (mHandler == null) {
                    return;
                }
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (isActivity && mSearchHeaderView != null) {
                            mSearchHeaderView.refreshState(pkgName);
                        }
                    }
                });

            }
        });

    }

    public void setIsActivity(boolean state) {
        isActivity = state;
    }
//	public void removemCallBack() {
//		if(listener !=null){
//			listener.setmCallBack(null);
//			listener=null;
//		}
//	}

    @Override
    protected void onDetachedFromWindow() {
        if (listener != null) {
            listener.setmCallBack(null);
            listener = null;
        }
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }
        super.onDetachedFromWindow();
    }


    private void initHeadView() {
        mListView = (ListViewForScrollView) headerView
                .findViewById(R.id.search_history_listView);
        mSearchHeaderView = (SearchHeaderView) headerView.findViewById(R.id.mSearchHeaderView);
        mSearchRecordAdapter = new MatchSearchRecordAdapter(mContext, list);
        mListView.setAdapter(mSearchRecordAdapter);

    }

    private void initViews() {
        etInput = (EditText) findViewById(R.id.search_et_input);
        InputFilter emojiFilter = UIUtils.getEmojiFilter();
        etInput.setFilters(new InputFilter[]{emojiFilter, new InputFilter.LengthFilter(20)});
        ivDelete = (ImageView) findViewById(R.id.search_iv_delete);
        ImageView btnBack = (ImageView) findViewById(R.id.search_btn_back);
        ImageView search_btn = (ImageView) findViewById(R.id.search_btn);
        lvTips = (ListView) findViewById(R.id.search_lv_tips);
        lvTips.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view,
                                    int postion, long l) {
                if (postion != 0) {
                    String text = lvTips.getAdapter().getItem(postion)
                            .toString();
                    // hint list view gone and result list view show
                    lvTips.setVisibility(View.GONE);
                    setTextForEditText(text);
                    notifyStartSearching(text);

                } else {
                    notifyStartSearching(null);
                }
            }
        });

        mListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                String text = mListView.getAdapter().getItem(position)
                        .toString();
                lvTips.setVisibility(View.GONE);
                setTextForEditText(text);
                notifyStartSearching(text);
            }
        });

        ivDelete.setOnClickListener(this);
        btnBack.setOnClickListener(this);
        search_btn.setOnClickListener(this);

        etInput.addTextChangedListener(new EditChangedListener());
        etInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId,
                                          KeyEvent keyEvent) {
                if (etInput.hasFocus()
                        && actionId == EditorInfo.IME_ACTION_SEARCH) {
                    lvTips.setVisibility(GONE);
                    notifyStartSearching(etInput.getText().toString());
                    /*2.0版本增加键盘上的搜索统计***/
                    if (!TextUtils.isEmpty(etInput.getText().toString())) {
                        MTAUtil.onSearch(etInput.getText().toString());
                    } else {
                        CharSequence hint = etInput.getHint();
                        if (!TextUtils.isEmpty(hint)) {
//                            MTAUtil.onSearch(hint.toString());
                            MTAUtil.onSearchOriginal(hint.toString());
                        }
                    }
                    // 隐藏软键盘
                    InputMethodManager imm = (InputMethodManager) mContext
                            .getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null && imm.isActive()) {// 若返回true，则表示输入法打开
                        // imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
                        // imm.showSoftInput(view,InputMethodManager.SHOW_FORCED);//强制显示键盘
                        imm.hideSoftInputFromWindow(etInput.getWindowToken(), 0); // 强制隐藏键盘

                    }
                }
                return true;
            }
        });
    }

    /**
     * 通知监听者 进行搜索操作
     *
     * @param text String
     */
    private void notifyStartSearching(String text) {
        Editable query = etInput.getText();
        String key = query.toString().trim();
        if (!TextUtils.isEmpty(key)) {
            processSearchAction(text);
        } else if (!TextUtils.isEmpty(etInput.getHint())) {
            processSearchAction(etInput.getHint().toString());
        } else {
            ToastUtils.showToast(R.string.search_keyword_empty);
        }

    }

    private class EditChangedListener implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i2,
                                      int i3) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            String content = editable.toString();
            if (JLog.isDebug) {
                JLog.i(TAG,"afterTextChanged-content="+content
                        +"-currentText="+currentText+"-etInput.hasFocus()="+etInput.hasFocus());
            }
            if (content.trim().length() > 0 && etInput.hasFocus()
                    && !currentText.equals(content)) {
                ivDelete.setVisibility(VISIBLE);
//                requestMatchData(content);
//                list = SearchHistoryDao.getSearchHistoryMatchList(content);
//                mSearchRecordAdapter.setList(list);
            } else {
                ivDelete.setVisibility(GONE);
                lvTips.setVisibility(GONE);
//                if(mContext!=null){
//                    ((SearchActivity) mContext).hideFragment();
//                }
            }
            if (etInput.hasFocus() && !currentText.equals(content)) {
                requestMatchData(content);
                list = SearchHistoryDao.getSearchHistoryMatchList(content);
                mSearchRecordAdapter.setList(list);
                currentText = content;
            }
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before,
                                  int count) {

        }
    }

    /**
     * 响应搜索动作
     *
     * @param text 关键字
     */
    private void processSearchAction(String text) {
        if (mCancelable != null) {
            mCancelable.cancel();
        }
        if (mListener != null) {
            if (bean != null) {
                mListener.onSearch(text, bean.id);
            } else {
                mListener.onSearch(text, null);
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.search_iv_delete:
                etInput.setText("");
                ivDelete.setVisibility(GONE);
                ((SearchActivity) mContext).hideFragment();
                // 隐藏软键盘
                InputMethodManager immo = (InputMethodManager) mContext
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                if (immo != null) {// 若返回true，则表示输入法打开
                    immo.showSoftInput(etInput, InputMethodManager.SHOW_FORCED);//强制显示键盘
                }
                break;
            case R.id.search_btn_back:
                // 隐藏软键盘
                InputMethodManager immp = (InputMethodManager) mContext
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                if (immp != null) {// 若返回true，则表示输入法打开
                    immp.hideSoftInputFromWindow(view.getWindowToken(), 0); // 强制隐藏键盘

                }
                mContext.finish();
                break;
            case R.id.search_btn:
                lvTips.setVisibility(GONE);
                notifyStartSearching(etInput.getText().toString());
                if (!TextUtils.isEmpty(etInput.getText().toString())) {
                    MTAUtil.onSearch(etInput.getText().toString());
                } else {
                    CharSequence hint = etInput.getHint();
                    if (!TextUtils.isEmpty(hint)) {
                        MTAUtil.onSearchOriginal(hint.toString());
                    }
                }
                // 隐藏软键盘
                InputMethodManager imm = (InputMethodManager) mContext
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {// 若返回true，则表示输入法打开
                    // imm.showSoftInput(view,InputMethodManager.SHOW_FORCED);//强制显示键盘
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0); // 强制隐藏键盘

                }
                break;
        }
    }

    /**
     * search view回调方法
     */
    public interface SearchViewListener {
        /**
         * @param text 关键字
         * @param id   第一行app的id
         */
        void onSearch(String text, String id);
    }

    @Override
    public void onBack(int what, int arg1, int arg2, Object obj) {

    }

    private void initHeadData(ArrayList<AppsItemBean> games) {
        mSearchHeaderView.setData(games);
    }

    private class MatchAdapter extends BaseAdapter {
        String[] matherItems;

        MatchAdapter(String[] matherItems) {
            super();
            this.matherItems = matherItems;
        }

        @Override
        public int getCount() {
            if (matherItems == null) {
                return 0;
            }
            return matherItems.length;
        }

        public void setData(String[] items) {
            if (items != null) {
                matherItems = items;
            }
            notifyDataSetChanged();
        }

        @Override
        public String getItem(int position) {

            return matherItems[position];
        }

        @Override
        public long getItemId(int position) {

            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = LayoutInflater.from(mContext).inflate(
                        R.layout.item_search_match, null);
                viewHolder.textView = (TextView) convertView
                        .findViewById(R.id.textView);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            viewHolder.textView.setText(matherItems[position]);
            return convertView;
        }

    }

    static class ViewHolder {
        TextView textView;
    }

    /**
     * @param text 点击热门索引或搜索记录传入的关键字
     */
    public void setTextForEditText(String text) {
        if (etInput != null) {
            //适配之前的版本超过20个字的情况
            text = CommonUtils.getMaxLenStr(text);
            this.currentText = text;
            etInput.setText(text);
            InputFilter emojiFilter = UIUtils.getEmojiFilter();
            etInput.setFilters(new InputFilter[]{emojiFilter, new InputFilter.LengthFilter(20)});
            try {
                etInput.setSelection(text.length());

            } catch (IndexOutOfBoundsException e) {
                e.printStackTrace();

            }
            ivDelete.setVisibility(VISIBLE);
            // 隐藏软键盘
            InputMethodManager imm = (InputMethodManager) mContext
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(etInput.getWindowToken(), 0); // 强制隐藏键盘

            }
        }
    }

    /**
     * 取消 下载监听, Activity OnDestroy 时调用
     */
    public void removeDownLoadHandler() {
        AIDLUtils.unregisterCallback(listener);
    }

    /**
     * 设置刷新handler,Activity OnResume 时调用
     */
    public void setDownlaodRefreshHandle() {
        AIDLUtils.registerCallback(listener);
    }

    private void requestMatchData(final String key) {
        if (mCancelable != null) {
            mCancelable.cancel();
        }
        if (TextUtils.isEmpty(key)) {
            if (lvTips != null) {
                lvTips.setVisibility(View.GONE);
            }
            return;
        }
        RequestParams params = new RequestParams(Constants.GIS_URL + "/search/match");
        params.addBodyParameter("query", key);
        params.addBodyParameter("pageIndex", String.valueOf(1));
        params.addBodyParameter("pageSize", "20");
        mCancelable = XExtends.http().post(params, new PrizeXutilStringCallBack<String>() {
                    @Override
                    public void onSuccess(String result) {
                        try {
                            JSONObject o = new JSONObject(result);
                            int code = o.getInt("code");
                            if (0 == code) {
                                PrizeMatchKeyTypeData data = GsonParseUtils.parseSingleBean(o.getString("data"), PrizeMatchKeyTypeData.class);
                                if (data != null) {
                                    bean = data.app;
                                    if (data.app == null && data.ads == null) {
                                        items = null;
                                        bean = null;
                                        lvTips.setVisibility(View.GONE);
                                        return;
                                    }
                                    if (data.items != null) {
                                        if (JLog.isDebug) {
                                            JLog.i(TAG, "requestMatchData-ivDelete.setVisibility(VISIBLE)-key=" + key);
                                        }
                                        initHeadData(CommonUtils.filterSearchMatch(data.ads, bean));
                                        items = data.items;
                                        lvTips.setVisibility(VISIBLE);
                                        adapter.setData(items);
                                    }
                                } else {
                                    lvTips.setVisibility(View.GONE);
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            JLog.i(TAG, "requestMatchData-onSuccess=" + e.getMessage());
                        }


                    }

                    @Override
                    public void onError(Throwable ex, boolean isOnCallback) {
                        if (lvTips != null) {
                            lvTips.setVisibility(View.GONE);
                        }
                        JLog.i(TAG, "requestMatchData-onError=" + ex.getMessage());
                    }
                }
        );

    }
}
