package com.calorieminer.minerapp.CustomClass;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.pnikosis.materialishprogress.ProgressWheel;
import com.calorieminer.minerapp.R;

public class CustomDialog extends Dialog{

    private TextView tv;
    private ProgressWheel pw;
    public CustomDialog(Context context)
    {
        super(context);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        init(context);
    }

    private void init(Context context)
    {
        View view = LayoutInflater.from(context).inflate(R.layout.custom_dialog,null,false);
        tv = (TextView) view.findViewById(R.id.tv_message);
        tv.setTypeface(Typeface.createFromAsset(context.getAssets(), "Roboto-Regular.ttf"));
        pw = (ProgressWheel) view.findViewById(R.id.pw);

        this.setCancelable(false);
        this.setCanceledOnTouchOutside(false);
        this.setContentView(view);
    }

    public void setProgressColor(int color)
    {
        pw.setBarColor(color);
    }
    public void setText(String text)
    {
        tv.setText(text);
    }
}
