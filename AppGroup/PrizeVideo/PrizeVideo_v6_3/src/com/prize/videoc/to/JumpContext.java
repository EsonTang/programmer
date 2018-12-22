package com.prize.videoc.to;

import android.content.Context;

public class JumpContext {
	private ITarget mTarget;

	public void setTarget(ITarget target) {
		mTarget = target;
	}

	public void doJump(Context ctx) {
		mTarget.jumpTo(ctx);
	}

}
