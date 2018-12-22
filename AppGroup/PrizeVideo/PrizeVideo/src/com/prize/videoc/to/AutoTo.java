package com.prize.videoc.to;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class AutoTo implements ITarget {

	private String videoPath;

	public AutoTo(String path) {
		videoPath = path;
	}

	@Override
	public void jumpTo(Context ctx) {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		Uri uri = Uri.parse(videoPath);
		intent.setDataAndType(uri, "video/*");
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		ctx.startActivity(intent);

	}
}
