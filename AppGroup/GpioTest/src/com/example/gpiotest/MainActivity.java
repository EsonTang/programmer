package com.example.gpiotest;

import java.io.IOException;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity {

	private Button MD_LED;	
	private Button lED1;
	private Button ID_CARD;

	private int m_count1 = 0;
	private int m_count2 = 0;
	private int m_count3 = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		MD_LED = (Button)findViewById(R.id.MD_LED);
		lED1 = (Button)findViewById(R.id.lED1);
		ID_CARD = (Button)findViewById(R.id.ID_CARD);

		MD_LED.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				int cmd = 1;
				if(m_count1++>0)
				{
					m_count1 = 0;
					cmd = 0;
				}
				try {
					String[] cmdMode = new String[]{"/system/bin/sh","-c","echo" + " " + cmd + " > /proc/driver/md_led_on"};
					Runtime.getRuntime().exec(cmdMode);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});

		ID_CARD.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				
				int cmd = 1;
				if(m_count2++>0)
				{
					m_count2 = 0;
					cmd = 0;
				}
				try {
					String[] cmdMode = new String[]{"/system/bin/sh","-c","echo" + " " + cmd + " > /proc/driver/ID_card_led_on"};
					Runtime.getRuntime().exec(cmdMode);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});

		lED1.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				
				int cmd = 1;
				if(m_count3++>0)
				{
					m_count3 = 0;
					cmd = 0;
				}
				try {
					String[] cmdMode = new String[]{"/system/bin/sh","-c","echo" + " " + cmd + " > /proc/driver/led1_on"};
					Runtime.getRuntime().exec(cmdMode);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
	}
}
