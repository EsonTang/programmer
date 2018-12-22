package com.prize.runoldtest;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class RunAllTestActivity extends Activity {
    private ListView listview_runall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_run_all_test);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(RunAllTestActivity.this, android.R.layout.simple_list_item_1);
        listview_runall = (ListView)findViewById(R.id.run_all_list);
        listview_runall.setAdapter(adapter);

        adapter.add("4 Hours");
        adapter.add("12 Hours");

        listview_runall.setOnItemClickListener(new AdapterView.OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position,
                                    long arg3) {
                // TODO Auto-generated method stub
                if (position == 0)
                {
                    Log.i("main", "pos = " + position + " Run All 4HourTest");
                    Intent intent = new Intent(RunAllTestActivity.this, RunAll4HourActivity.class);
                    startActivity(intent);
                }
                if (position == 1)
                {
                    Log.i("main", "pos = " + position + "Run All 12Hour Test");
                    Intent intent = new Intent(RunAllTestActivity.this, RunAll12HourActivity.class);
                    startActivity(intent);
                }
            }
        });
    }
}
