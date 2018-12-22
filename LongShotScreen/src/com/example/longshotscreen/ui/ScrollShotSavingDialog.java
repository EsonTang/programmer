package com.example.longshotscreen.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.longshotscreen.R;

public class ScrollShotSavingDialog extends Activity implements View.OnClickListener
{
  public static ScrollShotSavingDialog instance;

  public void onClick(View view)
  {
    switch (view.getId())
    {
    case R.id.longshot_saving_ok:
    	finish();
    	break;
    }
  }

  protected void onCreate(Bundle paramBundle)
  {
    super.onCreate(paramBundle);
    setContentView(R.layout.scrolshot_saving_dialog);
    instance = this;
    ((Button)findViewById(R.id.longshot_saving_ok)).setOnClickListener(this);
  }
}
