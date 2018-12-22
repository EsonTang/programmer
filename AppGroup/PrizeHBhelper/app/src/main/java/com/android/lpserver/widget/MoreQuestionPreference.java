package com.android.lpserver.widget;

import android.content.Context;
import android.content.Intent;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.lpserver.MoreQuestionActivity;
import com.android.lpserver.R;

/**
 * Created by prize on 2016/11/16.
 */
public class MoreQuestionPreference extends Preference {

    private TextView moreQuestionView;

    public MoreQuestionPreference(Context context) {
        super(context);
    }

    public MoreQuestionPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MoreQuestionPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected View onCreateView(ViewGroup parent) {
        super.onCreateView(parent);
        LayoutInflater inflater=(LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.preferencereminder, null);
        moreQuestionView = (TextView)layout.findViewById(R.id.text5);
        moreQuestionView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getContext().startActivity(new Intent(getContext(),MoreQuestionActivity.class));
            }
        });
        return layout;
    }
}
