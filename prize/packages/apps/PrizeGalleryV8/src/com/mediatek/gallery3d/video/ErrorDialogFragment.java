package com.mediatek.gallery3d.video;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.os.Bundle;
import android.view.KeyEvent;
import android.content.Intent;
import android.app.Activity;
import android.content.ComponentName;
import com.android.gallery3d.app.MovieActivity;

/** M: use DialogFragment to show Dialog */
public class ErrorDialogFragment extends DialogFragment {
    private static final String TAG = "Gallery2/VideoPlayer/SR/ErrorDialogFragment";
    private static final String KEY_MESSAGE = "message";
    private static Activity mContext;

    /**
     * M: create a instance of ErrorDialogFragment
     *
     * @param titleID
     *            the resource id of title string
     * @param messageID
     *            the resource id of message string
     * @return the instance of ErrorDialogFragment
     */
    public static ErrorDialogFragment newInstance(Activity context, int messageID) {
        mContext = context;
        ErrorDialogFragment frag = new ErrorDialogFragment();
        Bundle args = new Bundle(1);
        args.putInt(KEY_MESSAGE, messageID);
        frag.setArguments(args);
        return frag;
    }

    @Override
    /**
     * M: create a dialog
     */
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = getArguments();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setPositiveButton(android.R.string.ok,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        getActivity().finish();
						if(((MovieActivity)mContext).isFromVideo()){
							Intent intent = new Intent();  
							intent.setComponent(new ComponentName("com.prize.videoc",  
											   "com.prize.videoc.LocalActivity"));	
							intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
							startActivity(intent); 
						}
                    }
        }).setCancelable(false);
        if (args.getInt(KEY_MESSAGE) > 0) {
            builder.setMessage(getString(args.getInt(KEY_MESSAGE)));
        }
        Dialog res = builder.create();
        res.setCanceledOnTouchOutside(false);
        res.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface arg0, int keyCode,
                    KeyEvent keyEvent) {
                if (keyCode == KeyEvent.KEYCODE_BACK
                        || keyCode == KeyEvent.KEYCODE_SEARCH
                        || keyCode == KeyEvent.KEYCODE_MENU) {
                    return true;
                }
                return false;
            }
        });
        return res;
    }
}
