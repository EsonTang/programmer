package com.android.contacts;

import java.util.Hashtable;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import com.mediatek.contacts.util.Log;

import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.app.ActionBar;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;

import com.android.contacts.util.ImageViewDrawableSetter;
import com.android.contacts.widget.QuickContactImageView;
import com.mediatek.contacts.ExtensionManager;
import android.os.Bundle;
import com.android.contacts.quickcontact.QuickContactActivity;
import android.widget.QuickContactBadge;
import com.android.contacts.common.model.Contact;
import com.android.contacts.prize.PrizeQuickMarkDataManager;
public class QuickMarkActivity extends Activity {
	private final String TAG = "QuickMarkActivity";
	private int QR_WIDTH = 200, QR_HEIGHT = 200;
	private ImageView myImageView;
	private ImageView contactTitleImage;
	private TextView nameView;
	private  QuickContactImageView mPhotoView;
	private  ImageViewDrawableSetter mPhotoSetter;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.quick_mark_activity);
		ActionBar actionBar = getActionBar();
		if(actionBar != null) {
			actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        	actionBar.setDisplayShowCustomEnabled(false);
        	actionBar.setDisplayHomeAsUpEnabled(true);
        	actionBar.setDisplayShowTitleEnabled(true);
        	actionBar.setDisplayUseLogoEnabled(false);
        	actionBar.setTitle(R.string.scan_QR);
		}
		myImageView = (ImageView) findViewById(R.id.contactTwoDeminsionCode);
		Intent intent = getIntent();
		String number=intent.getStringExtra("number");
		String name = intent.getStringExtra("name");
		String displayName = intent.getStringExtra("display_name");
		nameView = (TextView) findViewById(R.id.contactNameTextView);
 		nameView.setText(displayName);
		createQRImage(name+number);
		
		/*prize-add huangpengfei-2016-8-16-start*/
		
		Contact contact = PrizeQuickMarkDataManager.getContact();
		if(contact != null){
			PrizeQuickMarkDataManager.clearData();
			Uri photoUri = contact.getLookupUri();
			Log.d(TAG, "contact photoUri=="+photoUri.toString());
			mPhotoView = (QuickContactImageView) findViewById(R.id.contactTitleImage);
			
			mPhotoSetter = new ImageViewDrawableSetter();
			mPhotoView.setIsBusiness(contact.isDisplayNameFromOrganization());
	        mPhotoSetter.setupContactPhoto(contact, mPhotoView);
	        ExtensionManager.getInstance().getRcsExtension()
	        .updateContactPhotoFromRcsServer(photoUri, mPhotoView, this);
		}
        /*prize-add huangpengfei-2016-8-16-end*/
		
		
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
            case android.R.id.home: 
				finish();
			return true;
		}
		return false;
	}

	public void createQRImage(String url)
	{
		try
		{

			if (url == null || "".equals(url) || url.length() < 1)
			{
				return;
			}
			Hashtable<EncodeHintType, String> hints = new Hashtable<EncodeHintType, String>();
			hints.put(EncodeHintType.CHARACTER_SET, "utf-8");

			BitMatrix bitMatrix = new QRCodeWriter().encode(url, BarcodeFormat.QR_CODE, QR_WIDTH, QR_HEIGHT, hints);
			int[] pixels = new int[QR_WIDTH * QR_HEIGHT];

			for (int y = 0; y < QR_HEIGHT; y++)
			{
				for (int x = 0; x < QR_WIDTH; x++)
				{
					if (bitMatrix.get(x, y))
					{
						pixels[y * QR_WIDTH + x] = 0xff000000;
					}
					else
					{
						pixels[y * QR_WIDTH + x] = 0xfff5f5f5;
					}
				}
			}

			Bitmap bitmap = Bitmap.createBitmap(QR_WIDTH, QR_HEIGHT, Bitmap.Config.ARGB_8888);
			bitmap.setPixels(pixels, 0, QR_WIDTH, 0, 0, QR_WIDTH, QR_HEIGHT);

			myImageView.setImageBitmap(bitmap);
		}
		catch (WriterException e)
		{
			e.printStackTrace();
		}
	}
}
