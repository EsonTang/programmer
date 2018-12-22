
package com.prize.appcenter.activity;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.prize.app.BaseApplication;
import com.prize.app.constants.Constants;
import com.prize.app.threads.SingleThreadScannExecutor;
import com.prize.app.util.JLog;
import com.prize.app.util.PreferencesUtils;
import com.prize.app.util.WindowMangerUtils;
import com.prize.appcenter.R;
import com.prize.appcenter.ui.adapter.ScanListAdapter;
import com.prize.appcenter.ui.dialog.ClearTrashCheckDialog;
import com.prize.appcenter.ui.util.UIUtils;
import com.prize.appcenter.ui.widget.CategoryHolder;
import com.prize.appcenter.ui.widget.NewScrollView;
import com.prize.appcenter.ui.widget.ScrollListView;
import com.prize.appcenter.ui.widget.TrashInfoHolder;
import com.prize.appcenter.ui.widget.TrashInfoHolder.OnClickCallback;
import com.prize.qihoo.cleandroid.sdk.BaseOptiTask;
import com.prize.qihoo.cleandroid.sdk.ResultSummaryInfo;
import com.prize.qihoo.cleandroid.sdk.TrashClearSDKHelper;
import com.prize.qihoo.cleandroid.sdk.TrashClearUtils;
import com.prize.qihoo.lib.atv.model.TreeNode;
import com.prize.qihoo.lib.atv.view.AndroidTreeView;
import com.qihoo.cleandroid.sdk.utils.ClearModuleUtils;
import com.qihoo.cleandroid.sdk.utils.ClearSDKException;
import com.qihoo360.mobilesafe.opti.env.clear.ClearOptionEnv;
import com.qihoo360.mobilesafe.opti.env.clear.TrashClearEnv;
import com.qihoo360.mobilesafe.opti.i.trashclear.TrashClearCategory;
import com.qihoo360.mobilesafe.opti.i.trashclear.TrashInfo;

import org.xutils.common.util.FileUtil;

import java.lang.ref.WeakReference;
import java.util.ConcurrentModificationException;
import java.util.List;

/**
 * 垃圾清理界面
 */
public class TrashClearActivity extends Activity implements OnClickListener, OnClickCallback {

    private static final String TAG = "TrashClearActivity";

    private static final int MSG_UPDATE_SCAN_PATH = 1;

    //private ScrollExpandableListView mExpandableListView;

    private LinearLayout mTreeNodeContainer;

    private LinearLayout mTitleBar;


    private RelativeLayout mTopRlView;

    private TextView mScanPathView;

    private TextView mTotalSizeVeiw;

    private long mTotalSize;

    private TextView mSizeUnitView;

    private TextView mTitleTextView;

    private TextView mSafeSizeView;

    private long safeSelectedSize = 0, safeTotalSize = 0;

    private ImageView safeCheckedView;

    private LinearLayout mWeixinLlView;

    private LinearLayout mQQLlView;

    private RelativeLayout mWeixinRlView;

    private ImageView mWeixinArrowView;

    private View mWeixinDividerView;

    private View mQQDividerView;

    private RelativeLayout mQQRlView;

    private ImageView mQQArrowView;

    private TextView mWeixinResultView;

    private TextView mQQResultView;

    private RelativeLayout mBottomRlView;

    private Button mClearBtn;

    private ProgressDialog mClearDialog;

    private Context mContext;

    private TrashClearSDKHelper mTrashClearHelper;

    public List<String> mScanPathList;

    //勾选提示框
    private ClearTrashCheckDialog mDialog;

    private int mScanPathSize;

    private long checkedLength;

    private long clearLength;

    public static final String SCAN_TYPE = "type";

    public static final int TYPE_SCAN_ALL = 0;

    public static final int TYPE_SCAN_ONEKEY = 1;

    private int mScanType = TYPE_SCAN_ALL;
    private String from = null;
    private final Handler mHandler = new MyHander(this);


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //沉浸式状态栏
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        if (!BaseApplication.isThird) {
            WindowMangerUtils.initStateBar(getWindow(), this);
        }
        setContentView(R.layout.clear_trash_main);
        mContext = getApplicationContext();
        mScanPathView = (TextView) findViewById(R.id.scan_path);
        mTotalSizeVeiw = (TextView) findViewById(R.id.scan_result_total_size);
        mTitleTextView = (TextView) findViewById(R.id.trash_title_tv);
        mSizeUnitView = (TextView) findViewById(R.id.size_unit);
        RelativeLayout mSafeRlView = (RelativeLayout) findViewById(R.id.safe_rl_view);
        mSafeRlView.setOnClickListener(this);
        mSafeSizeView = (TextView) findViewById(R.id.safe_size);
        safeCheckedView = (ImageView) findViewById(R.id.check_view);
        safeCheckedView.setOnClickListener(this);
        mWeixinLlView = (LinearLayout) findViewById(R.id.weixin_ll);
        mQQLlView = (LinearLayout) findViewById(R.id.qq_ll);
        mWeixinRlView = (RelativeLayout) findViewById(R.id.weixin_rl);
        mWeixinRlView.setOnClickListener(this);
        mQQRlView = (RelativeLayout) findViewById(R.id.qq_rl);
        mQQRlView.setOnClickListener(this);
        mWeixinResultView = (TextView) findViewById(R.id.weixin_size);
        mQQResultView = (TextView) findViewById(R.id.qq_size);
        mWeixinArrowView = (ImageView) findViewById(R.id.weixin_arrow_iv);
        mWeixinDividerView = findViewById(R.id.divider_d7d7d7_wx);
        mQQDividerView = findViewById(R.id.divider_d7d7d7_qq);
        mQQArrowView = (ImageView) findViewById(R.id.qq_arrow_iv);
        //mExpandableListView = (ScrollExpandableListView) findViewById(R.id.se_list);
        mTreeNodeContainer = (LinearLayout) findViewById(R.id.tree_container);
        mTopRlView = (RelativeLayout) findViewById(R.id.top_bg);
        mBottomRlView = (RelativeLayout) findViewById(R.id.bottom_id);
        ImageView mBackImageView = (ImageView) findViewById(R.id.back_im);
        mBackImageView.setOnClickListener(this);
        mClearBtn = (Button) findViewById(R.id.btn_clear);
        mClearBtn.setOnClickListener(this);

        mTitleBar = (LinearLayout) findViewById(R.id.title_bar);
        NewScrollView mScrollView = (NewScrollView) findViewById(R.id.scroll_view_id);
        mScrollView.setOnScrollChangedListener(new NewScrollView.ScrollChangeListener() {

            @Override
            public void onScroll(int scrollY) {
                float alpha = (float) (255.0 / 490.0 * scrollY);
                Drawable titleBarDrawable = getDrawable(R.drawable.trash_clear_title_bg);
                if (alpha >= 255) {
                    alpha = 255;
                } else if (alpha <= 0) {
                    alpha = 0;
                }
                if(titleBarDrawable!=null){
                    titleBarDrawable.setAlpha((int) alpha);
                }
                mTitleBar.setBackground(titleBarDrawable);
            }
        });
        if (getIntent() != null) {
            from = getIntent().getStringExtra(Constants.FROM);
        }
        ScrollListView mScanList = (ScrollListView) findViewById(R.id.scan_list);
        ScanListAdapter adapter = new ScanListAdapter(mContext);
        mScanList.setAdapter(adapter);

        mScanType = getIntent().getIntExtra(SCAN_TYPE, 0);

        mTrashClearHelper = TrashClearSDKHelper.getInstance(getApplicationContext());
        mTrashClearHelper.setType(TrashClearEnv.TYPE_ALL_ITEMS, null);

        mTrashClearHelper.setCallback(mScanCallback, mClearCallback);
        mScanPathList = TrashClearUtils.getScanList(mContext);
        if (mScanPathList != null) {
            mScanPathSize = mScanPathList.size();
        }
        //扫描会触发亮屏以及网络变化
        SingleThreadScannExecutor.getInstance().execute(
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        if (mScanType == TYPE_SCAN_ONEKEY) {
                            try {
                                // 缓存专项扫描设置,1:扫描;0:不扫描.不设置时，默认值为1
                                ClearModuleUtils.getClearModulel(mContext).setOption(ClearOptionEnv.SCAN_FILE_CACHE, "0");
                                // 系统盘垃圾扫描设置,1:扫描;0:不扫描.不设置时，默认值为1
                                ClearModuleUtils.getClearModulel(mContext).setOption(ClearOptionEnv.SCAN_SYSTEM_TRASH, "0");
                            } catch (ClearSDKException e) {
                                e.printStackTrace();
                            }
                            // 只扫描一键清理
                            mTrashClearHelper.setType(TrashClearEnv.TYPE_ONEKEY_CLEAR_ITEMS, null);
                        }

                        mTrashClearHelper.scan();
                    }
                })
        );


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_clear:
                if (checkedLength == 0) {
                    Toast.makeText(mContext, R.string.clear_sdk_please_selected_trash, Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!mTrashClearHelper.isScanFinish()) {
                    Toast.makeText(mContext, R.string.clear_sdk_scanning_wait, Toast.LENGTH_SHORT).show();
                    return;
                }
                SingleThreadScannExecutor.getInstance().execute(new Thread(new Runnable() {

                    @Override
                    public void run() {
                        clearLength = checkedLength;
                        mTrashClearHelper.clear();
                    }
                }));
                PreferencesUtils.putLong(mContext, Constants.KEY_TRASH_CLEAR_TIME, System.currentTimeMillis());
                break;

            case R.id.safe_rl_view:
                if (safeTotalSize <= 0) {
                    break;
                }
                Intent intent = new Intent(TrashClearActivity.this, SafeTrashClearActivity.class);
                startActivity(intent);
                break;

            case R.id.check_view:
                List<TrashClearCategory> categoryList = mTrashClearHelper.getSafeTrashClearCategoryList();
                if (safeTotalSize == safeSelectedSize) {
                    for (TrashClearCategory category : categoryList) {
                        mTrashClearHelper.trashClearCategoryDeSelectedAll(category);
                    }
                } else {
                    for (TrashClearCategory category : categoryList) {
                        mTrashClearHelper.trashClearCategorySelectedAll(category);
                    }
                }
                refreshSafeClear();
                break;

            case R.id.weixin_rl:
                mWeixinArrowView.setImageResource(mWeixinLlView.getVisibility() == View.VISIBLE ? R.drawable.node_not_expanded : R.drawable.node_expanded);
                mWeixinDividerView.setVisibility(mWeixinLlView.getVisibility() == View.VISIBLE ? View.VISIBLE : View.GONE);
                mWeixinLlView.setVisibility(mWeixinLlView.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
                break;

            case R.id.qq_rl:
                mQQArrowView.setImageResource(mQQLlView.getVisibility() == View.VISIBLE ? R.drawable.node_not_expanded : R.drawable.node_expanded);
                mQQDividerView.setVisibility(mQQLlView.getVisibility() == View.VISIBLE ? View.VISIBLE : View.GONE);
                mQQLlView.setVisibility(mQQLlView.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
                break;

            case R.id.back_im:
                onBackPressed();
                break;
            default:
                break;
        }
    }

    private void refreshView() {

        /*List<TrashClearCategory> list = mTrashClearHelper.getTrashClearCategoryList();
        if (mAdapter == null) {
            mAdapter = new TrashClearListAdapter(mContext, list);
            //mExpandableListView.setAdapter(mAdapter);
            mAdapter.setOnClickCallback(this);
        } else {
            mAdapter.refresh(list);
        }*/
        if (!mTrashClearHelper.isScanFinish()) {
            return;
        }


        ResultSummaryInfo resultSummaryInfo = mTrashClearHelper.getResultInfo();
        checkedLength = resultSummaryInfo.selectedSize;

        mTotalSize = resultSummaryInfo.size;
        mTotalSizeVeiw.setText(TrashClearUtils.getHumanReadableSize(mTotalSize));
        mSizeUnitView.setText(TrashClearUtils.getHumanReadableSizeUnit(mTotalSize));

        /*安全清理项刷新*/
        safeSelectedSize = 0;
        safeTotalSize = 0;
        List<TrashClearCategory> safeList = mTrashClearHelper.getSafeTrashClearCategoryList();
        for (TrashClearCategory category : safeList) {
            safeSelectedSize += category.selectedSize;
            safeTotalSize += category.size;
        }


        if (safeTotalSize <= 0) {
            safeCheckedView.setVisibility(View.GONE);
            mSafeSizeView.setText(R.string.clear_sdk_not_found);
        } else {
            safeCheckedView.setVisibility(View.VISIBLE);

            String safeSize = TrashClearUtils.getHumanReadableSizeMore(safeTotalSize);
            mSafeSizeView.setText(safeSize);

            if (safeTotalSize == safeSelectedSize) {
                safeCheckedView.setImageResource(R.drawable.node_selected);
            } else if (safeSelectedSize > 0) {
                safeCheckedView.setImageResource(R.drawable.node_half_selected);
            } else {
                safeCheckedView.setImageResource(R.drawable.node_not_selected);
            }
        }

        mClearBtn.setText(getString(R.string.clear_sdk_clear_trash, resultSummaryInfo.selectedSize == 0 ? "" :
                TrashClearUtils.getHumanReadableSizeMore(resultSummaryInfo.selectedSize)));
    }

    private void refreshSafeClear() {
        safeSelectedSize = 0;
        safeTotalSize = 0;
        List<TrashClearCategory> safeList = mTrashClearHelper.getSafeTrashClearCategoryList();
        for (TrashClearCategory category : safeList) {
            safeSelectedSize += category.selectedSize;
            safeTotalSize += category.size;
        }
        String safeSize = TrashClearUtils.getHumanReadableSizeMore(safeTotalSize);
        mSafeSizeView.setText(safeSize);

        ResultSummaryInfo resultSummaryInfo = mTrashClearHelper.getResultInfo();
        checkedLength = resultSummaryInfo.selectedSize;
        mClearBtn.setText(getString(R.string.clear_sdk_clear_trash, resultSummaryInfo.selectedSize == 0 ? "" :
                TrashClearUtils.getHumanReadableSizeMore(resultSummaryInfo.selectedSize)));

        if (safeTotalSize == safeSelectedSize) {
            safeCheckedView.setImageResource(R.drawable.node_selected);
        } else if (safeSelectedSize > 0) {
            safeCheckedView.setImageResource(R.drawable.node_half_selected);
        } else {
            safeCheckedView.setImageResource(R.drawable.node_not_selected);
        }
    }

    private void addTreeNode() {
        TreeNode root = TreeNode.root();
        List<TrashClearCategory> list = mTrashClearHelper.getTrashClearCategoryList();
        AndroidTreeView tView = new AndroidTreeView(mContext, root);
        boolean isNeedCheckTip;
        for (TrashClearCategory trashClearCategory : list) {
            TreeNode groupNode = new TreeNode(trashClearCategory).setViewHolder(new CategoryHolder(mContext));
            List<TrashInfo> trashInfoList = trashClearCategory.trashInfoList;
            isNeedCheckTip = trashClearCategory.type == TrashClearEnv.CATE_BIGFILE;
            try {
                for (TrashInfo trashInfo : trashInfoList) {
                    TrashInfoHolder trashInfoHolder = new TrashInfoHolder(TrashClearActivity.this, 0, isNeedCheckTip);
                    TreeNode childNode = new TreeNode(trashInfo).setViewHolder(trashInfoHolder);
                    trashInfoHolder.setOnClickCallback(this);

                    if (trashClearCategory.type == TrashClearEnv.CATE_BIGFILE) {
                        List<TrashInfo> subList = trashInfo.bundle.getParcelableArrayList(TrashClearEnv.subList);
                        if (subList != null && subList.size() > 0) {
                            for (TrashInfo trashInfo2 : subList) {
                                TrashInfoHolder subHolder = new TrashInfoHolder(TrashClearActivity.this, 1, isNeedCheckTip);
                                TreeNode subChildNode = new TreeNode(trashInfo2).setViewHolder(subHolder);
                                subHolder.setOnClickCallback(this);
                                childNode.addChild(subChildNode);
                            }
                        }
                    } else {
                        trashInfo.isSelected = true;
                        mTrashClearHelper.onTrashInfoSelectedChanged(trashInfo);
                    }
                    groupNode.addChild(childNode);
                }
            } catch (ConcurrentModificationException e) {
                e.printStackTrace();
                finish();
            }

            root.addChild(groupNode);
        }
        mTreeNodeContainer.removeAllViews();
        mTreeNodeContainer.addView(tView.getView());
    }

    private void refreshTreeNode(TreeNode node) {
        TrashInfo trashInfo = (TrashInfo) node.getValue();

        TrashInfoHolder holder = (TrashInfoHolder) node.getViewHolder();

        if (holder.mLevel == 0) {
            ImageView checkedView = (ImageView) holder.getView().findViewById(R.id.checked_view);
            checkedView.setImageResource(trashInfo.isSelected ? R.drawable.node_selected : R.drawable.node_not_selected);

            List<TreeNode> subNodeList = node.getChildren();
            for (TreeNode treeNode : subNodeList) {
                TrashInfo subTrashInfo = (TrashInfo) treeNode.getValue();
                TrashInfoHolder subChildHolder = (TrashInfoHolder) treeNode.getViewHolder();
                ImageView subCheckedView = (ImageView) subChildHolder.getView().findViewById(R.id.checked_view);
                subCheckedView.setImageResource(subTrashInfo.isSelected ? R.drawable.node_selected : R.drawable.node_not_selected);
            }

        } else if (holder.mLevel == 1) {
            ImageView subCheckedView = (ImageView) holder.getView().findViewById(R.id.checked_view);
            subCheckedView.setImageResource(trashInfo.isSelected ? R.drawable.node_selected : R.drawable.node_not_selected);


            TrashInfoHolder subChildHolder = (TrashInfoHolder) node.getParent().getViewHolder();
            TrashInfo childTrashInfo = (TrashInfo) node.getParent().getValue();
            ImageView checkedView = (ImageView) subChildHolder.getView().findViewById(R.id.checked_view);
            checkedView.setImageResource(childTrashInfo.isSelected ? R.drawable.node_selected : R.drawable.node_not_selected);
        }

        ResultSummaryInfo resultSummaryInfo = mTrashClearHelper.getResultInfo();
        checkedLength = resultSummaryInfo.selectedSize;
        mClearBtn.setText(getString(R.string.clear_sdk_clear_trash, resultSummaryInfo.selectedSize == 0 ? "" :
                TrashClearUtils.getHumanReadableSizeMore(resultSummaryInfo.selectedSize)));

    }

    private void addWeixinQQSubView() {
        LayoutInflater mInflater = LayoutInflater.from(mContext);
        TrashInfo weixiInfo = mTrashClearHelper.getWixinTrashInfo();
        TrashInfo qqInfo = mTrashClearHelper.getQQTrashInfo();

        if (weixiInfo != null) {
            TrashClearUtils.onTrashInfoSelectedChanged(weixiInfo, false);//取消所有取消所有勾选
            mTrashClearHelper.refreshSummaryInfo(mTrashClearHelper.getResultInfo());

            List<TrashInfo> subList = weixiInfo.bundle.getParcelableArrayList(TrashClearEnv.subList);
            if (subList == null || subList.size() <= 0) { //微信垃圾为空则不显示微信清理项
                mWeixinRlView.setVisibility(View.GONE);
                mWeixinDividerView.setVisibility(View.GONE);
                mWeixinLlView.removeAllViews();
            } else {
                mWeixinLlView.removeAllViews();

                for (TrashInfo info : subList) {
                    View subView = mInflater.inflate(R.layout.clear_trash_clear_list_item_level2, mWeixinLlView, false);
                    setTrashInfoView(info, subView);
                    mWeixinLlView.addView(subView);
                }
                mWeixinResultView.setText(TrashClearUtils.getHumanReadableSizeMore(weixiInfo.size));
            }
        } else {
            mWeixinRlView.setVisibility(View.GONE);
            mWeixinDividerView.setVisibility(View.GONE);
            mWeixinLlView.removeAllViews();
        }

        if (qqInfo != null) {
            TrashClearUtils.onTrashInfoSelectedChanged(qqInfo, false);//取消所有勾选
            mTrashClearHelper.refreshSummaryInfo(mTrashClearHelper.getResultInfo());

            List<TrashInfo> subList = qqInfo.bundle.getParcelableArrayList(TrashClearEnv.subList);
            if (subList == null || subList.size() <= 0) { //QQ垃圾为空则不显示微信清理项
                mQQRlView.setVisibility(View.GONE);
                mQQDividerView.setVisibility(View.GONE);
                mQQLlView.removeAllViews();
            } else {
                mQQLlView.removeAllViews();

                for (TrashInfo info : subList) {
                    View subView = mInflater.inflate(R.layout.clear_trash_clear_list_item_level2, mQQLlView, false);
                    setTrashInfoView(info, subView);
                    mQQLlView.addView(subView);
                }
                mQQResultView.setText(TrashClearUtils.getHumanReadableSizeMore(qqInfo.size));
            }
        } else {
            mQQRlView.setVisibility(View.GONE);
            mQQDividerView.setVisibility(View.GONE);
            mQQLlView.removeAllViews();
        }
    }

    /**
     * 刷新清理按钮上的所要清理的垃圾大小
     */
    private void refreshWeixinQQ() {
        ResultSummaryInfo resultSummaryInfo = mTrashClearHelper.getResultInfo();
        checkedLength = resultSummaryInfo.selectedSize;
        mClearBtn.setText(getString(R.string.clear_sdk_clear_trash, resultSummaryInfo.selectedSize == 0 ? "" :
                TrashClearUtils.getHumanReadableSizeMore(resultSummaryInfo.selectedSize)));
    }

    private void setTrashInfoView(final TrashInfo trashInfo, View itemView) {

        ImageView icon = (ImageView) itemView.findViewById(R.id.running_app_icon);
        TextView leftTopText = (TextView) itemView.findViewById(R.id.left_top_text);
        TextView leftBottomText = (TextView) itemView.findViewById(R.id.left_bottom_text);
        TextView rightText = (TextView) itemView.findViewById(R.id.right_text);
        final ImageView selectedView = (ImageView) itemView.findViewById(R.id.checked_view);
        selectedView.setVisibility(View.VISIBLE);
        selectedView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (trashInfo.isSelected) {
                    TrashClearUtils.onTrashInfoSelectedChanged(trashInfo, false);
                    mTrashClearHelper.refreshSummaryInfo(mTrashClearHelper.getResultInfo());
                    refreshWeixinQQ();
                    // 设置勾选状态
                    if (trashInfo.isInWhiteList) {
                        selectedView.setImageResource(R.drawable.common_whitelist_lock);
                    } else {
                        selectedView.setImageResource(R.drawable.node_not_selected);
                    }
                } else {
                    mDialog = new ClearTrashCheckDialog(TrashClearActivity.this, R.style.add_dialog);
                    mDialog.show();
                    mDialog.setmOnButtonClick(new ClearTrashCheckDialog.OnButtonClick() {
                        @Override
                        public void onClick(int which) {
                            dismissDialog();
                            switch (which) {
                                case 0:
                                    break;
                                case 1:
                                    TrashClearUtils.onTrashInfoSelectedChanged(trashInfo, true);
                                    mTrashClearHelper.refreshSummaryInfo(mTrashClearHelper.getResultInfo());
                                    refreshWeixinQQ();
                                    // 设置勾选状态
                                    if (trashInfo.isInWhiteList) {
                                        selectedView.setImageResource(R.drawable.common_whitelist_lock);
                                    } else {
                                        selectedView.setImageResource(R.drawable.node_selected);
                                    }
                                    break;
                            }
                        }
                    });
                }
            }
        });

        View middleView = itemView.findViewById(R.id.midle_view);
        middleView.setTag(trashInfo);
        middleView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                selectedView.performClick();
            }
        });

        leftBottomText.setVisibility(View.GONE);

        Drawable mDefaultActivityIcon = mContext.getPackageManager().getDefaultActivityIcon();

        if (TextUtils.isEmpty(trashInfo.packageName)) {
            int appTypeFromDB = trashInfo.bundle.getInt(TrashClearEnv.dbType);
            if (appTypeFromDB == TrashClearEnv.DB_APPTYPE_COMMON) {// 显示通用目录的图标
                icon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.icon_trash_common));
            } else {
                icon.setImageDrawable(mDefaultActivityIcon);
            }
            icon.setImageDrawable(mDefaultActivityIcon);
        } else {
            icon.setImageDrawable(TrashClearUtils.getApplicationIcon(trashInfo.packageName, mContext.getPackageManager()));
        }
        icon.setVisibility(View.VISIBLE);

        if (TrashClearEnv.CATE_BIGFILE == trashInfo.type && TrashClearEnv.BIGFILE_OTHER.equals(trashInfo.desc)) {
            leftTopText.setText(R.string.clear_sdk_bigfile_other);
        } else {
            leftTopText.setText(trashInfo.desc);
        }

        rightText.setText(TrashClearUtils.getHumanReadableSizeMore(trashInfo.size));

        // 设置勾选状态
        if (trashInfo.isInWhiteList) {
            selectedView.setImageResource(R.drawable.common_whitelist_lock);
        } else {
            selectedView.setImageResource(trashInfo.isSelected ? R.drawable.node_selected : R.drawable.node_not_selected);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshView();
    }

    private final BaseOptiTask.Callback mScanCallback = new BaseOptiTask.Callback() {

        private boolean isFirstUpdate;

        @Override
        public void onStart() {
            if (JLog.isDebug) {
                Log.d(TAG, "onStart");
            }
            mHandler.sendEmptyMessage(MSG_UPDATE_SCAN_PATH);
        }

        @Override
        public void onProgressUpdate(int progress, int max) {
            if (JLog.isDebug) {
                Log.d(TAG, "onProgressUpdate : " + progress + " " + max);
            }
        }

        @Override
        public void onDataUpdate(final long totalSize, final long totalCheckedSize, TrashInfo trashInfo) {

            if (JLog.isDebug) {
                Log.d(TAG, "onDataUpdate" + TrashClearUtils.getHumanReadableSizeMore(totalSize) + " 可清理：" + TrashClearUtils.getHumanReadableSizeMore(totalCheckedSize));
            }
            mTotalSize = totalSize;
            // 简单处理一下，让数据变化更流畅
            if (!isFirstUpdate) {
                isFirstUpdate = true;
                mHandler.post(dataUpdateRunnable);
            }
        }

        private final Runnable dataUpdateRunnable = new Runnable() {

            @Override
            public void run() {

                mTotalSizeVeiw.setText(TrashClearUtils.getHumanReadableSize(mTotalSize));
                mSizeUnitView.setText(TrashClearUtils.getHumanReadableSizeUnit(mTotalSize));
                mHandler.postDelayed(dataUpdateRunnable, 200);
            }
        };

        @Override
        public void onFinish(boolean isCanceled) {
            if (JLog.isDebug) {
                Log.d(TAG, "onFinish isCanceled:" + isCanceled);
            }
            mHandler.removeMessages(MSG_UPDATE_SCAN_PATH);
            mHandler.removeCallbacks(dataUpdateRunnable);

            mScanPathView.post(new Runnable() {

                @Override
                public void run() {
                    mBottomRlView.setVisibility(View.VISIBLE);
                    mTopRlView.setBackground(getDrawable(R.drawable.trash_result_bg));
                    mTitleTextView.setText(R.string.clear_sdk_trash_clear);
                    mScanPathView.setText(R.string.clear_sdk_scan_done);
                    //mExpandableListView.setVisibility(View.VISIBLE);

                    findViewById(R.id.scan_layout).setVisibility(View.INVISIBLE);
                    findViewById(R.id.result_layout).setVisibility(View.VISIBLE);

                    /*List<TrashClearCategory> categoryList = mTrashClearHelper.getSafeTrashClearCategoryList();
                    if(categoryList != null && categoryList.size() > 0) {
                        for (TrashClearCategory category : categoryList) {
                            if(category.type == TrashClearEnv.CATE_APK) {
                                mTrashClearHelper.trashClearCategoryDeSelectedAll(category);
                                mTrashClearHelper.trashClearCategorySelectedAll(category);
                            }
                        }
                    }*/

                    addWeixinQQSubView();
                    addTreeNode();
                    refreshView();
                    if (safeTotalSize <= 0) {
                        return;
                    }

                    if (safeTotalSize > 0 && safeTotalSize == safeSelectedSize) {
                        safeCheckedView.performClick();
                    }
                    safeCheckedView.performClick();
                }
            });
        }
    };

    private void showClearDialog() {
        if (this.isFinishing())
            return;
        if (mClearDialog == null) {
            mClearDialog = new ProgressDialog(this);
            mClearDialog.setTitle(R.string.clear_sdk_prompt);
            mClearDialog.setMessage(getString(R.string.clear_sdk_clearing_wait));
            mClearDialog.setIndeterminate(true);
            mClearDialog.setCancelable(true);
        }
        if (this.isFinishing())
            return;
        mClearDialog.show();
    }

    private final BaseOptiTask.Callback mClearCallback = new BaseOptiTask.Callback() {

        @Override
        public void onStart() {
            if (JLog.isDebug) {
                Log.d(TAG, "onStart");
            }
            mScanPathView.post(new Runnable() {

                @Override
                public void run() {
                    showClearDialog();
                }
            });
        }

        @Override
        public void onProgressUpdate(int progress, int max) {
            if (JLog.isDebug) {
                Log.d(TAG, "onProgressUpdate : " + progress + " " + max);
            }
        }

        @Override
        public void onFinish(boolean isCanceled) {
            if (JLog.isDebug) {
                Log.d(TAG, "onFinish isCanceled:" + isCanceled);
            }
            mScanPathView.post(new Runnable() {

                @Override
                public void run() {

                    addWeixinQQSubView();
                    addTreeNode();
                    refreshView();
                    if (mClearDialog != null && mClearDialog.isShowing()) {
                        mClearDialog.dismiss();
                    }

                    Intent intent = new Intent(TrashClearActivity.this, TrashClearDoneActivity.class);
                    intent.putExtra("clear_size", TrashClearUtils.getHumanReadableSizeMore(clearLength));
                    float savePercent = (float) clearLength / (float) FileUtil.getPhoneTotalSize(TrashClearActivity.this) * 100;
                    savePercent = (float) (Math.round(savePercent * 100)) / 100;
                    if (savePercent <= 0) {
                        savePercent = 0.01f;
                    }
                    intent.putExtra("save_space", savePercent);
                    startActivity(intent);
                }
            });
        }

        @Override
        public void onDataUpdate(final long length, final long checkedLength, TrashInfo trashInfo) {
            if (JLog.isDebug) {
                Log.d(TAG, "onDataUpdate");
            }
        }
    };

    @Override
    public void onTrashInfoSelectedChanged(final View view, final TrashInfo trashInfo, final TreeNode node, boolean isNeedCheckTip) {
        if (!isNeedCheckTip) {
            mTrashClearHelper.onTrashInfoSelectedChanged(trashInfo);
            ((ImageView) view).setImageResource(trashInfo.isSelected ? R.drawable.node_selected : R.drawable.node_not_selected);
            refreshTreeNode(node);
        } else {
            mDialog = new ClearTrashCheckDialog(TrashClearActivity.this, R.style.add_dialog);
            mDialog.show();
            mDialog.setmOnButtonClick(new ClearTrashCheckDialog.OnButtonClick() {
                @Override
                public void onClick(int which) {
                    dismissDialog();
                    switch (which) {
                        case 0:
                            break;
                        case 1:
                            ((ImageView) view).setImageResource(R.drawable.node_selected);
                            mTrashClearHelper.onTrashInfoSelectedChanged(trashInfo);
                            refreshTreeNode(node);
                            break;
                    }
                }
            });
        }
    }

    @Override
    public void onTrashInfoItemClick(TrashInfo trashInfo) {
        showTrashInfoItemDialog(trashInfo);
    }

    private void showTrashInfoItemDialog(final TrashInfo trashInfo) {
        Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(trashInfo.desc);
        View view = View.inflate(mContext, R.layout.clear_item_detail_view, null);
        builder.setView(view);
        builder.setCancelable(true);
        AlertDialog dialog = builder.create();

        TextView sizeView = (TextView) view.findViewById(R.id.tv_size);
        TextView pathTextView = (TextView) view.findViewById(R.id.tv_path);
        TextView pathLabelTextView = (TextView) view.findViewById(R.id.tv_path_label);
        View pathView = view.findViewById(R.id.ll_path);
        CheckBox isWhiteListTextView = (CheckBox) view.findViewById(R.id.cb_is_whitelist);
        View whitelistView = view.findViewById(R.id.ll_whitelist);
        TextView whitelistLabelTextView = (TextView) view.findViewById(R.id.tv_is_whitelist_desc);
        sizeView.setText(TrashClearUtils.getHumanReadableSizeMore(trashInfo.size));
        pathTextView.setText(trashInfo.path);

        whitelistView.setVisibility(View.GONE);

        switch (trashInfo.type) {
            case TrashClearEnv.CATE_APP_SD_CACHE:
            case TrashClearEnv.CATE_FILE_CACHE:
            case TrashClearEnv.CATE_ADPLUGIN:
            case TrashClearEnv.CATE_UNINSTALLED:
            case TrashClearEnv.CATE_APK:
                whitelistView.setVisibility(View.VISIBLE);
                isWhiteListTextView.setChecked(trashInfo.isInWhiteList);
                isWhiteListTextView.setOnCheckedChangeListener(new OnCheckedChangeListener() {

                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        mTrashClearHelper.onWhiteListStatusChanged(trashInfo);
                        refreshView();
                    }
                });

                switch (trashInfo.type) {
                    case TrashClearEnv.CATE_APP_SD_CACHE:
                        whitelistLabelTextView.setText(R.string.clear_sdk_prompt_no_clear_cache);
                        break;
                    case TrashClearEnv.CATE_UNINSTALLED:
                        whitelistLabelTextView.setText(R.string.clear_sdk_prompt_no_clear_uninstalled);
                        break;
                    case TrashClearEnv.CATE_APK:
                        whitelistLabelTextView.setText(R.string.clear_sdk_prompt_no_clear_apk);
                        break;
                    default:
                        break;
                }

                break;
            case TrashClearEnv.CATE_APP_SYSTEM_CACHE:

                pathView.setVisibility(View.GONE);
                sizeView.setText(TrashClearUtils.getHumanReadableSizeMore(trashInfo.size));
                break;
            case TrashClearEnv.CATE_SYSTEM_LOG:
            case TrashClearEnv.CATE_SYSTEM_LOSTDIR:
            case TrashClearEnv.CATE_SYSTEM_TEMP:
            case TrashClearEnv.CATE_SYSTEM_BAK:
            case TrashClearEnv.CATE_SYSTEM_THUMBNAIL:
            case TrashClearEnv.CATE_SYSTEM_TRASH:

                pathView.setVisibility(View.GONE);
                sizeView.setText(TrashClearUtils.getHumanReadableSizeMore(trashInfo.size));
                break;
            case TrashClearEnv.CATE_SYSTEM_EMPTYDIR:
                sizeView.setText(TrashClearUtils.getHumanReadableSizeMore(trashInfo.size));
                pathLabelTextView.setText(R.string.clear_sdk_number);
                pathTextView.setText(String.valueOf(trashInfo.count));

                break;

            default:
                break;
        }
        dialog.show();

    }

    private void dismissDialog() {
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        /*PRIZE START add byg longbaoxiu   防止内存泄漏*/
        if (mTrashClearHelper != null) {
            mTrashClearHelper.cancelScan();
            mTrashClearHelper.setCallback(null, null);
            mTrashClearHelper.onDestroy();
        }
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
        SingleThreadScannExecutor.getInstance().cancleAllTask();
        /*PRIZE END add byg longbaoxiu   防止内存泄漏*/

    }

    @Override
    public void onBackPressed() {
        try {
            super.onBackPressed();
        } catch (IllegalStateException ignored) {
            // There's no way to avoid getting this if saveInstanceState has already been called.
        }
        if (!TextUtils.isEmpty(from) && "push".equals(from)) {
            try {
                UIUtils.gotoActivity(MainActivity.class, this);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    private static class MyHander extends Handler {
        private WeakReference<TrashClearActivity> mActivities;

        MyHander(TrashClearActivity mActivity) {
            this.mActivities = new WeakReference<TrashClearActivity>(mActivity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (mActivities == null) return;
            final TrashClearActivity activity = mActivities.get();
            if (activity != null) {
                //执行业务逻辑
                switch (msg.what) {
                    case MSG_UPDATE_SCAN_PATH:
                        if (activity.mTrashClearHelper.isScanFinish()) {
                            return;
                        }

                        int scanIndex = msg.arg1;
                        if (scanIndex >= activity.mScanPathSize) {
                            scanIndex = 0;
                        }
                        activity.mScanPathView.setText(activity.getString(R.string.clear_sdk_scann_item, activity.mScanPathList.get(scanIndex)));
                        Message m = Message.obtain();
                        msg.what=MSG_UPDATE_SCAN_PATH;
//                        activity.mHandler.obtainMessage(MSG_UPDATE_SCAN_PATH);
                        m.arg1 = ++scanIndex;
                        sendMessageDelayed(m, 300);
                        break;

                    default:
                        break;
                }

            }
        }
    }
}
