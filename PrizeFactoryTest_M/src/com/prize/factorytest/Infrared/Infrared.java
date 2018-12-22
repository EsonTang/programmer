package com.prize.factorytest.Infrared;

import com.prize.factorytest.R;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.hardware.ConsumerIrManager;
import com.prize.factorytest.PrizeFactoryTestListActivity;
import android.view.KeyEvent;

public class Infrared extends Activity {
	TextView mFreqsText;
    ConsumerIrManager mCIR;
	private Button buttonPass;
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK){
			return true;
		}
		return false;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		mCIR = (ConsumerIrManager) getSystemService(Context.CONSUMER_IR_SERVICE);
		setContentView(R.layout.infrared);

		findViewById(R.id.send_button).setOnClickListener(mSendClickListener);
		findViewById(R.id.get_freqs_button).setOnClickListener(mGetFreqsClickListener);
		mFreqsText = (TextView) findViewById(R.id.freqs_text);

		confirmButton();
		buttonPass.setEnabled(false);
	}
	View.OnClickListener mSendClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            if (!mCIR.hasIrEmitter()) {
                return;
            }
            int[] pattern = {1901, 4453, 625, 1614, 625, 1588, 625, 1614, 625, 442, 625, 442, 625,
                468, 625, 442, 625, 494, 572, 1614, 625, 1588, 625, 1614, 625, 494, 572, 442, 651,
                442, 625, 442, 625, 442, 625, 1614, 625, 1588, 651, 1588, 625, 442, 625, 494, 598,
                442, 625, 442, 625, 520, 572, 442, 625, 442, 625, 442, 651, 1588, 625, 1614, 625,
                1588, 625, 1614, 625, 1588, 625, 48958};
            mCIR.transmit(38400, pattern);
			buttonPass.setEnabled(true);
        }
    };

    View.OnClickListener mGetFreqsClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            StringBuilder b = new StringBuilder();

            if (!mCIR.hasIrEmitter()) {
                mFreqsText.setText("No IR Emitter found!");
                return;
            }
            ConsumerIrManager.CarrierFrequencyRange[] freqs = mCIR.getCarrierFrequencies();
            b.append("IR Carrier Frequencies:\n");
            for (ConsumerIrManager.CarrierFrequencyRange range : freqs) {
                b.append(String.format("    %d - %d\n", range.getMinFrequency(),
                            range.getMaxFrequency()));
            }
            mFreqsText.setText(b.toString());
        }
    };

	public void confirmButton() {
		buttonPass = (Button) findViewById(R.id.passButton);
		final Button buttonFail = (Button) findViewById(R.id.failButton);
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
}
