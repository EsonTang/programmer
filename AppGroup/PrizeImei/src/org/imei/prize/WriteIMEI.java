package org.imei.prize;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;
//import com.android.internal.telephony.gemini.GeminiPhone;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.os.AsyncResult;
//import android.view.View.OnClickListener; 
import android.view.View;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class WriteIMEI extends Activity
{
  private static final String TAG = "WriteIMEI";
  private Handler mResponseHander;
  private Phone phone = null;
  private EditText mIMEIEdit, mIMEIEdit2, mIMEIEdit3;
  private Button mButton1,mButton2,mButton3;
  public void onCreate(Bundle paramBundle)
  {
    super.onCreate(paramBundle);
    setContentView(R.layout.main);
	initHandler();
	mIMEIEdit = (EditText) findViewById(R.id.dual_imei);
    mIMEIEdit.setText("");
    mIMEIEdit2 = (EditText) findViewById(R.id.sim1_imei);
    mIMEIEdit2.setText("");
	mIMEIEdit3 = (EditText) findViewById(R.id.sim2_imei);
    mIMEIEdit3.setText("");
	mButton1 = (Button) findViewById(R.id.button_write_dual);
    mButton1.setOnClickListener(new Button.OnClickListener()
	{
	   @Override
    	public void onClick(View v) {
        
            if (checkInvalid(0)) {
                String imei = mIMEIEdit.getText().toString();
                writeDualIMEI(imei)	;
            Log.d("WriteIMEI", "  writeDualIMEI");
            }
        
      }
	});
    mButton2 = (Button) findViewById(R.id.button_write_sim1);
    mButton2.setOnClickListener(new Button.OnClickListener()
	{
	   @Override
    	public void onClick(View v) {
        
            if (checkInvalid(1)) {
				String imei = mIMEIEdit2.getText().toString();
           		writeSim1IMEI(imei);
            Log.d("WriteIMEI", "  writeDualIMEI");
            }
        
      }
	});
	mButton3 = (Button) findViewById(R.id.button_write_sim2);
    mButton3.setOnClickListener(new Button.OnClickListener()
	{
	   @Override
    	public void onClick(View v) {
        
            if (checkInvalid(2)) {
				String imei = mIMEIEdit3.getText().toString();
           		writeSim2IMEI(imei);
            Log.d("WriteIMEI", "  writeDualIMEI");
            }
        
      }
	});
  }

 protected final void initHandler() {
        mResponseHander = new Handler() {
		public void handleMessage(Message paramMessage)
		  {
		    AsyncResult localAsyncResult = (AsyncResult)paramMessage.obj;
		    switch (paramMessage.what)
		    {

		    default:
		      Log.d("WriteIMEI", "  default");
		      break;
		    case 1:
		    case 2:
			   if (localAsyncResult.exception == null)
			      {
				  Log.d("WriteIMEI", "  handleMessage case 2");
				 
				AlertDialog.Builder localBuilder1 = new AlertDialog.Builder(WriteIMEI.this);
				localBuilder1.setTitle(R.string.write_success);
				localBuilder1.setMessage(R.string.success_content_sim1);
			    localBuilder1.setPositiveButton(R.string.ok,new DialogInterface.OnClickListener(){
						@Override
						public void onClick(final DialogInterface dialog, final int which) {
						  //finish();
						   showToast("success!");
						}

                   			 });
				localBuilder1.setCancelable(false);
				localBuilder1.create().show();
			      }else
			      {
				 
				AlertDialog.Builder localBuilder2 = new AlertDialog.Builder(WriteIMEI.this);
				localBuilder2.setTitle(R.string.write_fail);
				localBuilder2.setMessage(R.string.fail_content_sim1);
			    localBuilder2.setPositiveButton(R.string.ok,new DialogInterface.OnClickListener(){
						@Override
						public void onClick(final DialogInterface dialog, final int which) {
						  //finish();
						   showToast("failed!");
						}

                   			 });
				localBuilder2.setCancelable(false);
				localBuilder2.create().show();
			      }
				//mIMEIEdit.setText("");
			    //mIMEIEdit2.setText("");
			    //mIMEIEdit3.setText("");
			 break;
		    }
		     
		  }
        };
    }
	
   private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
  public void writeDualIMEI(String paramString)
  {
    String[] arrayOfString1 = new String[2];
    String str1 = "AT+EGMR=1,7,\"" + paramString + "\"";
    arrayOfString1[0] = str1;
    arrayOfString1[1] = "";
    String[] arrayOfString2 = new String[2];
    String str2 = "AT+EGMR=1,10,\"" + paramString + "\"";
    arrayOfString2[0] = str2;
    arrayOfString2[1] = "";
    Phone localPhone1 = PhoneFactory.getDefaultPhone();
    Phone localPhone2 = localPhone1;
    localPhone2.invokeOemRilRequestStrings(arrayOfString1, mResponseHander.obtainMessage(3));
    Phone localPhone3 = localPhone1;
    localPhone3.invokeOemRilRequestStrings(arrayOfString2, mResponseHander.obtainMessage(2));
  }
    public void writeSim1IMEI(String paramString)
  {
    String[] arrayOfString1 = new String[2];
    String str1 = "AT+EGMR=1,7,\"" + paramString + "\"";
    arrayOfString1[0] = str1;
    arrayOfString1[1] = "";
   
    Phone localPhone1 = PhoneFactory.getDefaultPhone();
    localPhone1.invokeOemRilRequestStrings(arrayOfString1, mResponseHander.obtainMessage(1));
  }
    public void writeSim2IMEI(String paramString)
  {
    String[] arrayOfString = new String[2];
    String str = "AT+EGMR=1,10,\"" + paramString + "\"";
    arrayOfString[0] = str;
    arrayOfString[1] = "";
    Phone localPhone2 = PhoneFactory.getDefaultPhone();
    localPhone2.invokeOemRilRequestStrings(arrayOfString, mResponseHander.obtainMessage(2));
  }
      private boolean checkInvalid(int i) {
        String imei = null;
        if (i == 0) {
            imei = mIMEIEdit.getText().toString();
        } else if (i == 1) {
            imei = mIMEIEdit2.getText().toString();
        }else if (i == 2) {
            imei = mIMEIEdit3.getText().toString();
		}
        if (imei == null || imei.equals("")) {
            showToast("empty input!");
            return false;
        }
        if (imei.trim().length() != 15) {
            showToast("must be 15 digits!");
            return false;
        }
        return true;
    }

}

