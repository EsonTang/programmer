package com.prize.factorytest.RAM;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import com.prize.factorytest.R;
import com.prize.factorytest.PrizeFactoryTestListActivity;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.view.KeyEvent;

public class RAM extends Activity {
    private static DecimalFormat fileIntegerFormat = new DecimalFormat("#0");
    private static DecimalFormat fileDecimalFormat = new DecimalFormat("#0.#");
    
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK){
			return true;
		}
		return false;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ram);
		TextView totalRam = (TextView) findViewById(R.id.ram_total);
		TextView freeRam = (TextView) findViewById(R.id.ram_free);
		totalRam.setText(getString(R.string.total_memory) + formatFileSize(getTotalMemorySize(this),true));
		freeRam.setText(getString(R.string.available_memory) + formatFileSize(getAvailableMemory(this),true));

		confirmButton();
	}

	public void confirmButton() {
		final Button buttonFail = (Button) findViewById(R.id.failButton);
		final Button buttonPass = (Button) findViewById(R.id.passButton);
		buttonPass.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (PrizeFactoryTestListActivity.toStartAutoTest == true) {
					PrizeFactoryTestListActivity.itempos++;
				}
				setResult(RESULT_OK);
				finish();
			}
		});
		buttonFail.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (PrizeFactoryTestListActivity.toStartAutoTest == true) {
					PrizeFactoryTestListActivity.itempos++;
				}
				setResult(RESULT_CANCELED);
				finish();

			}

		});
	}

    public static long getTotalMemorySize(Context context) {
        String dir = "/proc/meminfo";
        try {
            FileReader fr = new FileReader(dir);
            BufferedReader br = new BufferedReader(fr, 2048);
            String memoryLine = br.readLine();
            String subMemoryLine = memoryLine.substring(memoryLine.indexOf("MemTotal:"));
            br.close();
            return Integer.parseInt(subMemoryLine.replaceAll("\\D+", "")) * 1024l;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static long getAvailableMemory(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(memoryInfo);
        return memoryInfo.availMem;
    }

    public static String formatFileSize(long size, boolean isInteger) {
        DecimalFormat df = isInteger ? fileIntegerFormat : fileDecimalFormat;
        String fileSizeString = "0M";
        if (size < 1024 && size > 0) {
            fileSizeString = df.format((double) size) + "B";
        } else if (size < 1024 * 1024) {
            fileSizeString = df.format((double) size / 1024) + "K";
        } else{
            fileSizeString = df.format((double) size / (1024 * 1024)) + "M";
        }
        return fileSizeString;
    }
	
}