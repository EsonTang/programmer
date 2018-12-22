package com.android.lpserver;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.android.lpserver.bean.Recorder;
import com.android.lpserver.db.HBHelper;
import com.android.lpserver.job.WechatAccessbilityJob;

import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class HBRecordorActivity extends AppCompatActivity {
    private static final String TAG = "HBRecordorActivity";
    private TextView sumText;
    private TextView countText;
    private TextView secondText;
    private ListView listView;
    private LinearLayout clearView;
    private double aveTime;
    private int successAmount;
    private double sum;
    private ImageView clearImgView;
    private TextView clearTextView;
    private List<Recorder> list;
    private HBRecordorAdapter adapter;
    private Cursor cursor1;
    private Cursor cursor2;
    private SQLiteDatabase readableDatabase;
    private LinearLayout clearViewParent;
    private double totalTime;
    private String getMoney;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setNotificationStatus();
        setContentView(R.layout.activity_hbrecordor);

        readableDatabase = new HBHelper(this,"RED_BAG.db",null,3).getReadableDatabase();  //   /data/user/0/com.comfort.helper/databases/RED_BAG.db
        initViews();

        //Set adapter
        list = new ArrayList();
        setAdapter();


        cursor1 = readableDatabase.query("red_bag1", new String[]{"sender", "get_money","date"}, null, null, null, null, null);
        while (cursor1.moveToNext()){
            String sender = cursor1.getString(cursor1.getColumnIndex("sender"));
            getMoney = cursor1.getString(cursor1.getColumnIndex("get_money"));
            String date = cursor1.getString(cursor1.getColumnIndex("date"));
            if(getMoney != null){
                list.add(new Recorder(sender, getMoney,date));
            }
        }

        cursor2 = readableDatabase.query("red_bag2", new String[]{"success_amount","ave_time","sum","total_time"}, null, null, null, null, null);
        while (cursor2.moveToNext()){
            successAmount = Integer.parseInt(cursor2.getString(cursor2.getColumnIndex("success_amount")));
            aveTime = Double.parseDouble(cursor2.getString(cursor2.getColumnIndex("ave_time")));
            sum = Double.parseDouble(cursor2.getString(cursor2.getColumnIndex("sum")));
            totalTime = Double.parseDouble(cursor2.getString(cursor2.getColumnIndex("total_time")));
            aveTime = totalTime / successAmount;
            DecimalFormat decimalFormat = new DecimalFormat(".#");
            aveTime = Double.parseDouble(decimalFormat.format(aveTime)) ;
            if(aveTime <= 0.5){
                aveTime = 0.5;
            }
        }

        Log.d(TAG,"list="+list.toString());
        if(list.size() != 0){
            clearImgView.setBackgroundResource(R.drawable.delete);
            clearView.setClickable(true);
            clearTextView.setTextColor(getResources().getColor(R.color.text_color));

            //Clear red packets
            clearView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG,"Click the clear button");
                    readableDatabase.delete("red_bag1",null,null);
                    readableDatabase.delete("red_bag2",null,null);

                    list.clear();
                    setAdapter();

                    cursor2 = readableDatabase.query("red_bag2", null, null, null, null, null, null);
                    if(!cursor2.moveToNext()){
                        sumText.setText(0.00+"");
                        countText.setText(0+"");
                        secondText.setText(0.0+"");
                    }

                    //Send a broadcast to clear the data
                    Intent intent = new Intent(WechatAccessbilityJob.action);
                    intent.putExtra("isClear",true);
                    sendBroadcast(intent);

                    //Clear the data and empty the button
                    clearImgView.setBackgroundResource(R.drawable.delete_not);
                    clearView.setClickable(false);
                    clearView.setFocusable(false);
                    clearTextView.setTextColor(getResources().getColor(R.color.clear_gray));
                }
            });
        }else{
            clearImgView.setBackgroundResource(R.drawable.delete_not);
            clearView.setClickable(false);
            clearView.setFocusable(false);
            clearTextView.setTextColor(getResources().getColor(R.color.clear_gray));
        }


        //Set a red envelope to record the information
        initData();

        //When the virtual buttons are displayed or hidden, the button position is changed.
        moveClearView();
    }

    private void moveClearView() {
        final LinearLayout activityRootView = (LinearLayout) findViewById(R.id.recorder_linearLayout);
        activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int heightDiff = activityRootView.getRootView().getHeight()- activityRootView.getHeight();
                // TODO: 2016/12/13
            }
        });
    }

    private void setNotificationStatus() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    //| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }

        WindowManager.LayoutParams lp= getWindow().getAttributes();
//        lp.statusBarInverse = StatusBarManager.STATUS_BAR_INVERSE_WHITE;
//        getWindow().setAttributes(lp);
        try {
            Class statusBarManagerClazz = Class.forName("android.app.StatusBarManager");
            Field whiteField = statusBarManagerClazz.getDeclaredField("STATUS_BAR_INVERSE_WHITE");
            Object white = whiteField.get(statusBarManagerClazz);
            Class windowManagerLpClazz = lp.getClass();
            Field statusBarInverseField = windowManagerLpClazz.getDeclaredField("statusBarInverse");
            statusBarInverseField.set(lp,white);
            getWindow().setAttributes(lp);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setAdapter() {
        adapter = new HBRecordorAdapter();
        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    private void initData() {
        sumText.setText(sum+"");
        countText.setText(successAmount+"");
        secondText.setText(aveTime+"");
    }

    private void initViews() {
        sumText = (TextView) findViewById(R.id.sum_textView);
        countText = (TextView) findViewById(R.id.count_textView);
        secondText = (TextView) findViewById(R.id.second_textView);
        listView = (ListView) findViewById(R.id.hb_listView);
        clearView = (LinearLayout) findViewById(R.id.clearView);
        clearImgView = (ImageView) findViewById(R.id.clear_imageView);
        clearTextView = (TextView) findViewById(R.id.clear_textView);
        clearViewParent = (LinearLayout)findViewById(R.id.clearView_parent);
        findViewById(R.id.recorder_back_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private class HBRecordorAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if(convertView == null){
                convertView = LayoutInflater.from(HBRecordorActivity.this).inflate(R.layout.hb_recorder_item,null);
                viewHolder = new ViewHolder();
                viewHolder.personView = (TextView) convertView.findViewById(R.id.person_view);
                viewHolder.dateView = (TextView) convertView.findViewById(R.id.getBag_time_view);
                viewHolder.amountView = (TextView) convertView.findViewById(R.id.amount_view);

                convertView.setTag(viewHolder);
            }else{
                viewHolder = (ViewHolder) convertView.getTag();
            }

            viewHolder.personView.setText(list.get(position).getSender());
            viewHolder.dateView.setText(list.get(position).getDate());
            viewHolder.amountView.setText(list.get(position).getGetMoney());
            return convertView;
        }

        class ViewHolder{
            TextView personView;
            TextView dateView;
            TextView amountView;
        }
    }
}
