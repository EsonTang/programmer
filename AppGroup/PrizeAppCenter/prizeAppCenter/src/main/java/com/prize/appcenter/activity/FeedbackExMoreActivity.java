package com.prize.appcenter.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.PaintDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.prize.app.beans.ClientInfo;
import com.prize.app.constants.Constants;
import com.prize.app.util.JLog;
import com.prize.app.util.Verification;
import com.prize.app.util.WindowMangerUtils;
import com.prize.appcenter.R;
import com.prize.appcenter.fragment.PromptDialogFragment;
import com.prize.appcenter.ui.actionBar.ActionBarNoTabActivity;
import com.prize.appcenter.ui.adapter.popListviewAdapter;
import com.prize.appcenter.ui.util.ToastUtils;
import com.prize.appcenter.ui.util.UIUtils;
import com.prize.appcenter.ui.widget.CornerImageView;
import com.prize.appcenter.ui.widget.PrizeCommButton;
import com.prize.custmerxutils.XExtends;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Desc: 意见反馈-提交反馈
 * <p/>
 * Created by huangchangguo
 * Date:  2016/9/7 11:32
 */

public class FeedbackExMoreActivity extends ActionBarNoTabActivity {
    /**
     * 消息编辑框
     */
    private EditText contentET;
    private EditText contact_et;
    private Button add_Btn;
    private int PICTURE = 100;
    private String picturePath;
    private ArrayList<File> files = new ArrayList<File>();
    private ArrayList<String> picturePaths = new ArrayList<String>();
    private LinearLayout tableLayout;
    private LinearLayout select_Llyt;
    private PromptDialogFragment df;
    private ProgressDialog dialog;
    private boolean flag = true;
    private PrizeCommButton feedback_send_btn;
    private boolean isRequesting = false;
    private TextView select_class_Tv;
    //private TextView           sub_title_Tv;
    private PopupWindow popupWindow;
    private popListviewAdapter mExpandableListAdapter;
    private String[] groupList;
    private boolean isFirst = false;
    private TextView mTipsNoMsg;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_feedbackex_more);
        WindowMangerUtils.changeStatus(getWindow());
        setTitle(R.string.feedback_title);

        findViewById();
        initData();
        setListener();
        init();

    }

    private void initData() {

//        String selectsub = "下载";
        groupList = getResources().getStringArray(R.array.feed_title_array);
//        int len = groupList.length;
//        if (TextUtils.isEmpty(selectsub)) {
//            return;
//        }
//        for (int i = 0; i < len; i++) {
//            if (selectsub.equals(groupList[i])) {
//                enterPosition = i;
//                break;
//            }
//
//        }

    }

    private void init() {
        mExpandableListAdapter = new popListviewAdapter(groupList, this);
    }

    private void setListener() {
        feedback_send_btn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                for (int i = 0; i < picturePaths.size(); i++) {
                    files.add(new File(picturePaths.get(i)));
                }
                sendFeedback();
            }
        });

        add_Btn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                Intent picture = new Intent(
                        Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                picture.setType("image/*");
                startActivityForResult(picture, PICTURE);

            }
        });

        select_Llyt.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                getPopupWindow(v);
            }
        });

    }

    private void findViewById() {
        select_class_Tv = (TextView) findViewById(R.id.select_class_Tv);
        mTipsNoMsg = (TextView) findViewById(R.id.feedback_more_nomsg);
        // sub_title_Tv = (TextView) findViewById(R.id.sub_title_Tv);
        contentET = (EditText) findViewById(R.id.fedbck_content_et);
        contact_et = (EditText) findViewById(R.id.contact_et);
        add_Btn = (Button) findViewById(R.id.add_Btn);
        tableLayout = (LinearLayout) findViewById(R.id.tableLayout);
        select_Llyt = (LinearLayout) findViewById(R.id.select_Llyt);
        InputFilter emojiFilter = UIUtils.getEmojiFilter();
        contentET.setFilters(new InputFilter[]{emojiFilter});
        feedback_send_btn = (PrizeCommButton) findViewById(R.id.feedback_send_btn);
        feedback_send_btn.enabelDefaultPress(true);
    }

    private void sendFeedback() {

        String fbType = select_class_Tv.getText().toString();
        //问题描述
        String content = contentET.getText().toString();
        //联系方式
        String contactInfo = contact_et.getText().toString();
        // String fbType1 = sub_title_Tv.getText().toString();
        //String fbType = fbType0 + ":" + fbType1;

        if (TextUtils.isEmpty(fbType)) {
            mTipsNoMsg.setVisibility(View.VISIBLE);
            //ToastUtils.showToast(R.string.pl_select_class);
            mTipsNoMsg.setText(R.string.pl_select_class);
            return;
        } else {
            mTipsNoMsg.setVisibility(View.GONE);
        }

        if (TextUtils.isEmpty(content.trim())) {
            mTipsNoMsg.setVisibility(View.VISIBLE);
            mTipsNoMsg.setText(R.string.feedback_msg_empty);
            //ToastUtils.showToast(R.string.feedback_msg_empty);
            contentET.setText("");
            return;
        } else {

            mTipsNoMsg.setVisibility(View.GONE);
        }

        if (ClientInfo.networkType == ClientInfo.NONET) {
            ToastUtils.showToast(R.string.nonet_connect);
            return;
        }
        if (isRequesting) {
            ToastUtils.showToast(R.string.questing);
            return;
        }
        flag = false;
        String url = Constants.GIS_URL + "/feedback/upfeedback";
        RequestParams params = new RequestParams(url);
        params.setMultipart(true);
        params.addBodyParameter("content", content);
        params.addBodyParameter("feedbackType", fbType);
        if (!TextUtils.isEmpty(contactInfo)) {
            params.addBodyParameter("contactInfo", contactInfo);
        }

        // MD5加密校验key
        Map<String, String> signKey = new HashMap<String, String>();
        signKey.put("content", String.valueOf(content));
        signKey.put("feedbackType", String.valueOf(fbType));
        signKey.put("contactInfo", String.valueOf(contactInfo));
        String sign = Verification.getInstance().getSign(signKey);
        params.addBodyParameter("sign", sign);
        params.setConnectTimeout(30 * 1000);
        for (int i = 0; i < files.size(); i++) {
            params.addBodyParameter("file" + (i + 1), files.get(i), null);
        }
        XExtends.http().post(params, new Callback.ProgressCallback<String>() {

            @Override
            public void onSuccess(String result) {
                if (dialog != null && dialog.isShowing()
                        && !FeedbackExMoreActivity.this.isFinishing()) {
                    dialog.dismiss();
                }
                try {
                    JSONObject obi = new JSONObject(result);
                    int code = obi.getInt("code");
                    if (code == 0) {
                        ToastUtils.showToast(R.string.send_fedbck_tip);
                        finish();
                    } else {
                        String msg = obi.getString("msg");
                        ToastUtils.showToast(msg);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    isRequesting = false;

                }

                isRequesting = false;
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                JLog.i("FeedbackExMoreActivity", "onError-ex=" + ex.getMessage());
                if (dialog != null && dialog.isShowing()
                        && !FeedbackExMoreActivity.this.isFinishing()) {
                    dialog.dismiss();
                }
                isRequesting = false;
                ToastUtils.showToast(ex.getMessage());

            }

            @Override
            public void onCancelled(CancelledException cex) {

            }

            @Override
            public void onFinished() {
            }

            @Override
            public void onWaiting() {

            }

            @Override
            public void onStarted() {
                isRequesting = true;


                dialog = new ProgressDialog(FeedbackExMoreActivity.this);
                dialog.setMessage(getString(R.string.committing));
                dialog.setCancelable(true);
                if (!FeedbackExMoreActivity.this.isFinishing()) {
                    dialog.show();
                }
                //防止闪屏
//                try {
//                    Thread.currentThread().sleep(500);//阻断2秒
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }

            }

            @Override
            public void onLoading(long total, long current,
                                  boolean isDownloading) {

            }

        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onBack(int what, int arg1, int arg2, Object obj) {

    }

    @Override
    public String getActivityName() {
        return "FeedbackExActivity";
    }

    @Override
    protected void initActionBar() {
        findViewById(R.id.action_bar_feedback).setVisibility(View.INVISIBLE);
        super.initActionBar();
    }

    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LayoutParams.WRAP_CONTENT);

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) {
            return;
        }
        if (data.toString().contains("video")) {
            ToastUtils.showToast("注意：请不要选择视频文件");
            return;
        }
        if (resultCode == Activity.RESULT_OK && null != data) {
            lp.weight = 1;
            lp.rightMargin = 12;
            String bitMap = getBitmap(data);
            if (bitMap != null) {
                picturePaths.add(bitMap);
                JuageAddBtn();
                final View view = LayoutInflater.from(this).inflate(
                        R.layout.item_feedback, null);
                view.setTag(bitMap);
                view.setLayoutParams(lp);
                CornerImageView game_iv = (CornerImageView) view
                        .findViewById(R.id.game_iv);
                game_iv.setScaleType(ScaleType.CENTER_CROP);
                Bitmap bm = decodeSampledBitmapFromResource(bitMap, 100, 100);
                game_iv.setImageBitmap(bm);
                ImageView delete_iv = (ImageView) view
                        .findViewById(R.id.delete_iv);
                delete_iv.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        String path = (String) view.getTag();
                        for (int i = 0; i < picturePaths.size(); i++) {
                            if (picturePaths.get(i).equals(path)) {
                                picturePaths.remove(i);
                                break;
                            }
                        }
                        tableLayout.removeView(view);
                        JuageAddBtn();
                    }
                });
                tableLayout.addView(view);
                // }

            }
        }
    }

    private void JuageAddBtn() {
        if (picturePaths.size() >= 3) {
            add_Btn.setVisibility(View.GONE);
        } else {
            add_Btn.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 调用系统相册的操作,在onActivityResult中调用
     *
     * @param data onActivityResult中的Intent
     */
    public String getBitmap(Intent data) {
        Uri selectedImage = data.getData();
        if (selectedImage.toString().contains("content://")) {
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            if (cursor == null)
                return "";
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            picturePath = cursor.getString(columnIndex);
            cursor.close();
            return picturePath;
        } else {
            String file = selectedImage.toString();
            String files[] = file.split("//");
            picturePath = files[1];
            return picturePath;
        }
    }

    @Override
    public void onBackPressed() {
        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.dismiss();
            return;
        }
        if (flag
                && (picturePaths.size() > 0 || contentET.getText().toString()
                .trim().length() > 0)) {
            if (df == null || !df.isAdded()) {
                df = PromptDialogFragment.newInstance(getString(R.string.caution),
                        getString(R.string.caution_content),
                        getString(R.string.alert_button_yes), null,
                        mDeletePromptListener);

            }
            if (df != null && !df.isAdded()) {
                df.show(getSupportFragmentManager(), "sureDialog");
            }
        } else {
            super.onBackPressed();
        }
    }

    /**
     * 编辑后退出提示框
     */
    private OnClickListener mDeletePromptListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            df.dismissAllowingStateLoss();
            FeedbackExMoreActivity.this.finish();
        }
    };

    /**
     * 计算大小，防止加载过大图片 造成内存溢出
     *
     * @param pathName  路径
     * @param reqWidth  需要的宽度
     * @param reqHeight 需要的高度
     * @return Bitmap Bitmap
     */
    private Bitmap decodeSampledBitmapFromResource(String pathName,
                                                   int reqWidth, int reqHeight) {
        // 第一次解析将inJustDecodeBounds设置为true，来获取图片大小
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(pathName, options);
        options.inSampleSize = calculateInSampleSize(options, reqWidth,
                reqHeight);
        // 使用获取到的inSampleSize值再次解析图片
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(pathName, options);
    }

    /**
     * @param options   BitmapFactory.Options
     * @param reqWidth  需要的宽度
     * @param reqHeight 需要的高度
     * @return int 缩放比例
     */
    private int calculateInSampleSize(BitmapFactory.Options options,
                                      int reqWidth, int reqHeight) {
        // 源图片的高度和宽度
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            // 计算出实际宽高和目标宽高的比率
            final int heightRatio = Math.round((float) height
                    / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            // 选择宽和高中最小的比率作为inSampleSize的值，这样可以保证最终图片的宽和高
            // 一定都会大于等于目标的宽和高。
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        return inSampleSize;
    }

    /***
     * 获取PopupWindow实例
     *
     * @param v
     */
    private void getPopupWindow(View v) {
        if (null != popupWindow) {
            if (popupWindow.isShowing()) {
                popupWindow.dismiss();
            } else {
                popupWindow.showAsDropDown(v);
            }
        } else {
            initPopuptWindow();
            popupWindow.showAsDropDown(v);
        }
    }

    /**
     * 创建PopupWindow
     */
    @SuppressLint("ClickableViewAccessibility")
    protected void initPopuptWindow() {

        // 获取自定义布局文件activity_popupwindow_left.xml的视图
        View popupWindowView = getLayoutInflater().inflate(
                R.layout.popupwindow_layout, null, false);
        ListView mListView = (ListView) popupWindowView
                .findViewById(R.id.poplistView);
        // mListView.set
        mListView.setAdapter(mExpandableListAdapter);
        // 创建PopupWindow实例,200,LayoutParams.MATCH_PARENT分别是宽度和高度
        popupWindow = new PopupWindow(popupWindowView,
                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, true);
        // 设置动画效果
        // 点击其他地方消失
        // popupWindow.setAnimationStyle(R.style.mypopwindow_anim_style);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setBackgroundDrawable(new PaintDrawable());
        popupWindowView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (popupWindow != null && popupWindow.isShowing()) {
                    popupWindow.dismiss();
                    popupWindow = null;
                }
                return true;
            }
        });
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String itemText = FeedbackExMoreActivity.this.groupList[position];
                select_class_Tv.setText(itemText);

                if (null != popupWindow && popupWindow.isShowing()) {
                    popupWindow.dismiss();
                }
            }
        });
    }

    public void onWindowFocusChanged(boolean hasFocus) {
        if (hasFocus && isFirst && select_Llyt != null) {
            getPopupWindow(select_Llyt);
            isFirst = false;
        }
    }
}
