package com.prize.contacts.common.util;

import java.util.List;
import android.net.Uri;
import android.telecom.PhoneAccount;
import android.telecom.PhoneAccountHandle;
import android.content.Context;
import android.content.Intent;
import android.telecom.TelecomManager;
import android.telecom.VideoProfile;
import android.telephony.PhoneNumberUtils;

public class PrizeVideoCallHelper {
	private static PrizeVideoCallHelper prizeVideoCallHelper = null;
	private Context mContext;
	private TelecomManager telecommMgr;

	private PrizeVideoCallHelper(Context context) {
		mContext = context;
		telecommMgr = (TelecomManager) mContext
				.getSystemService(Context.TELECOM_SERVICE);
	}

	public static PrizeVideoCallHelper getInstance(Context context) {
		if (prizeVideoCallHelper == null) {
			prizeVideoCallHelper = new PrizeVideoCallHelper(context);
		}
		return prizeVideoCallHelper;
	}

	private Uri getCallUri(String number) {
		if (PhoneNumberUtils.isUriNumber(number)) {
			return Uri.fromParts(PhoneAccount.SCHEME_SIP, number, null);
		}
		return Uri.fromParts(PhoneAccount.SCHEME_TEL, number, null);
	}

	public boolean canStartVideoCall() {
		if (telecommMgr == null) {
			return false;
		}

		List<PhoneAccountHandle> accountHandles = telecommMgr
				.getCallCapablePhoneAccounts();
		for (PhoneAccountHandle accountHandle : accountHandles) {
			PhoneAccount account = telecommMgr.getPhoneAccount(accountHandle);
			if (account != null
					&& account
							.hasCapabilities(PhoneAccount.CAPABILITY_VIDEO_CALLING)) {
				return true;
			}
		}
		return false;
	}

	public void placeOutgoingVideoCall(String phoneNumber) {
		if (telecommMgr == null) {
			return;
		}
		final Intent intent = new Intent(Intent.ACTION_CALL,
				getCallUri(phoneNumber));
		intent.putExtra(TelecomManager.EXTRA_START_CALL_WITH_VIDEO_STATE,
				VideoProfile.STATE_BIDIRECTIONAL);
		telecommMgr.placeCall(intent.getData(), intent.getExtras());

	}

}
