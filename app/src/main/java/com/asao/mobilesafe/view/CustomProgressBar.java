package com.asao.mobilesafe.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.asao.mobilesafe.R;

public class CustomProgressBar extends RelativeLayout {


    private TextView mText;
    private ProgressBar mPB;
    private TextView mLeft;
    private TextView mRight;

    public CustomProgressBar(Context context) {
        //super(context);
        this(context,null);
    }

    public CustomProgressBar(Context context, AttributeSet attrs) {
        this(context,attrs,-1);
    }

    public CustomProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        initView();
    }

    //将进度条布局添加到自定义控件中
    private void initView() {
        View view = View.inflate(getContext(), R.layout.customprogress, null);
        this.addView(view);//将view对象添加到自定义控件中

        mText = view.findViewById(R.id.customprgress_tv_text);
        mPB = view.findViewById(R.id.customprgress_pb_progress);
        mLeft = view.findViewById(R.id.customprgress_tv_left);
        mRight = view.findViewById(R.id.customprgress_tv_right);
    }

    //提供给activity使用的,用来设置Text控件的值
    //给初始化出来的自定义控件设置数据
    public void setText(String text){
        mText.setText(text);
    }

    public void setLeft(String text){
        mLeft.setText(text);
    }

    public void setRight(String text){
        mRight.setText(text);
    }

    public void setProgress(int progress){
        mPB.setProgress(progress);
    }
}
