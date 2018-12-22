/*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：
 *当前版本：1.0.0
 *作	者：yiyi
 *完成日期：2015年7月23日
 *修改记录：
 *修改日期：
 *版 本 号：
 *修 改 人：
 *修改内容：
 ...
 *修改记录：
 *修改日期：
 *版 本 号：
 *修 改 人：
 *修改内容：
*********************************************/
package com.prize.cloud.widgets;

import com.prize.cloud.R;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class SelectSexDialog extends AlertDialog implements android.view.View.OnClickListener{
	private TextView popu_cancle;
	private TextView poTakes_photo;
	private TextView popu_photos;
	
	private static final int CANCEL = 0;
	private static final int TAKE_PHOTO = 1;
	private static final int PHOTOS = 2;
	
	private Context context;
	public SelectSexDialog(Context context, int theme) {
		super(context, theme);
		this.context = context;
	}

	@Override  
	protected void onCreate(Bundle savedInstanceState) {  
		 super.onCreate(savedInstanceState);  
		 setContentView(R.layout.popupwindow);  
		popu_cancle = (TextView) this.findViewById(R.id.popu_cancle);
		poTakes_photo = (TextView) this.findViewById(R.id.take_photos_id);
		popu_photos = (TextView) this.findViewById(R.id.photos_id);
		
		popu_photos.setText(context.getString(R.string.woman));
		poTakes_photo.setText(context.getText(R.string.man));
		
		popu_cancle.setOnClickListener(this);
		poTakes_photo.setOnClickListener(this);
		popu_photos.setOnClickListener(this);
	} 
	
	@Override
	public boolean isShowing() {
		// TODO Auto-generated method stub
		return super.isShowing();
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch (id) {
			case R.id.popu_cancle:
				shSex.onClick(CANCEL);
				break;
				
			case R.id.take_photos_id:
				shSex.onClick(TAKE_PHOTO);
				break;
				
			case R.id.photos_id:
				shSex.onClick(PHOTOS); 
				break;

			default:
				break;
		}
		
	}
	
   public static interface SelectSex{
	   void onClick(int which);
   }
   
   public SelectSex shSex;
   
	public void setShSex(SelectSex shSex) {
		this.shSex = shSex;
	}
  
}
