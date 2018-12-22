package com.android.settings;

import android.app.Activity;
import android.os.Bundle;

public class PrizeLuckyMoneyDialog extends Activity{  

  protected void onCreate(Bundle savedInstanceState) {  
      super.onCreate(savedInstanceState);  
      	setContentView(R.layout.prize_lucky_money_solution_dialog);   
      	getWindow().setBackgroundDrawableResource(android.R.color.transparent);
  }
}
