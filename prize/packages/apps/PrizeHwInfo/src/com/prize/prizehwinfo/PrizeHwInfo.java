package com.prize.prizehwinfo;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class PrizeHwInfo extends Activity {
	private static final String[] ARGS_HW;

	static {
		String[] arrayOfString = new String[2];
		arrayOfString[0] = "cat";
		arrayOfString[1] = "sys/class/hw_info/hw_info_data/hw_info_read";
		ARGS_HW = arrayOfString;
	}

	public String getResultHwinfo() {
		ShellExecute localShellExecute = new ShellExecute();
		try {
			String[] arrayOfString = ARGS_HW;
			String str = localShellExecute.execute(arrayOfString, "/");
			return str;
		} catch (Exception localException) {
			while (true)
				localException.printStackTrace();
		}
	}

	protected void onCreate(Bundle paramBundle) {
		super.onCreate(paramBundle);
		setContentView(R.layout.main);
		TextView localTextView = (TextView) findViewById(R.id.showresult);
		String str1 = getResultHwinfo();
		localTextView.setText(str1);
	}

	class ShellExecute {
		private ShellExecute() {
		}

		public String execute(String[] paramArrayOfString, String paramString)
				throws IOException {
			String str1 = " ";
			try {
				ProcessBuilder localProcessBuilder = new ProcessBuilder(
						paramArrayOfString);
				if (paramString != null) {
					File localFile = new File(paramString);
					localProcessBuilder.directory(localFile);
				}
				localProcessBuilder.redirectErrorStream(true);
				InputStream localInputStream = localProcessBuilder.start()
						.getInputStream();
				byte[] arrayOfByte = new byte[1024];
				while (localInputStream.read(arrayOfByte) != -1) {
					String str2 = new String(arrayOfByte);
					str1 = str2;
				}
				localInputStream.close();
				return str1;
			} catch (Exception localException) {
				while (true)
					localException.printStackTrace();
			}
		}
	}
}
