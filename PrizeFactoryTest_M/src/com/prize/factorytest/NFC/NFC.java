package com.prize.factorytest.NFC;

import com.prize.factorytest.R;
import com.prize.factorytest.PrizeFactoryTestListActivity;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.widget.Toast;
import android.view.View;
import android.widget.Button;
import android.view.KeyEvent;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.LayoutInflater;
import android.nfc.NfcAdapter;
import android.nfc.Tag;

import android.util.Log;

public class NFC extends Activity {
	String TAG = "NFC";
	PowerManager powerManager = null;
	WakeLock wakeLock = null;
	
	private boolean mNfcIsEnable;
    private boolean mNfcSupport;
	private NfcAdapter mNfcAdapter;
    private PendingIntent mPendingIntent;
    private IntentFilter[] mIntentFilters;
    private String[][] mTechLists;
	
	private Button buttonPass;
	private Button buttonFail;
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK){
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.nfc);
		powerManager = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
		wakeLock = this.powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK,"My Lock");
		confirmButton();
		
		mPendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, this.getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        mIntentFilters = new IntentFilter[]{
                new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED),
                new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED),
                new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED),
        };
        mTechLists = new String[][]{
                new String[]{android.nfc.tech.NfcA.class.getName()},
                new String[]{android.nfc.tech.NfcB.class.getName()},
                new String[]{android.nfc.tech.NfcF.class.getName()},
                new String[]{android.nfc.tech.NfcV.class.getName()},
                new String[]{android.nfc.tech.Ndef.class.getName()},
                new String[]{android.nfc.tech.MifareClassic.class.getName()},
                new String[]{android.nfc.tech.NfcBarcode.class.getName()},
                new String[]{android.nfc.tech.MifareUltralight.class.getName()},
        };
		
		Log.i("pss","onCreate");
		
	}
	

	@Override
	protected void onPause() {
		Log.i("pss","onPause");
		wakeLock.release();
		if (mNfcAdapter != null) {
            mNfcAdapter.disableForegroundDispatch(this);
        }
		super.onPause();
	}

	@Override
	protected void onResume() {
		Log.i("pss","onResume");
		initNfc();
		wakeLock.acquire();
		super.onResume();
	}

	public void confirmButton() {
		buttonPass = (Button) findViewById(R.id.passButton);
		buttonPass.setEnabled(false);
		buttonFail = (Button) findViewById(R.id.failButton);
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
	
	@Override
    public void onNewIntent(Intent intent) {
        judgeNfcTag(intent);
    }

    private void judgeNfcTag(Intent intent) {
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (tag != null) {
            buttonPass.setEnabled(true);
        }
    }
	
	private void initNfc() {
		Log.i("pss","initNfc");
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        mNfcSupport = mNfcAdapter != null;
        if (mNfcSupport) {
            mNfcIsEnable = mNfcAdapter.isEnabled();
            if (mNfcIsEnable) {
                mNfcAdapter.enableForegroundDispatch(this, mPendingIntent, mIntentFilters, mTechLists);
            }else{
				Toast.makeText(getApplicationContext(), "CAN'T OPEN NFC",Toast.LENGTH_SHORT).show();
			}
        }else{
			Toast.makeText(getApplicationContext(), "CAN'T SUPPORT NFC",Toast.LENGTH_SHORT).show();
		}
    }
}
