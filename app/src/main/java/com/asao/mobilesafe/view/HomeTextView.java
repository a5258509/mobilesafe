package com.asao.mobilesafe.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * 自定义textview
 * */
public class HomeTextView extends androidx.appcompat.widget.AppCompatTextView {
    //在代码中使用的时候调用
    public HomeTextView(@NonNull Context context){
        this(context,null);

    }
    //在布局文件中使用的时候调用
    //布局文件中的控件最终会通过反射的形式转化为代码
    //控件的所有属性都会保存到AttributeSet属性集中
    public HomeTextView(@NonNull Context context, @Nullable AttributeSet attrs){
        this(context,attrs,-1);

    }
    //在控件的内部让两个参数的构造调用
    public HomeTextView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    //设置天生自带焦点
    public boolean isFocused() {
        return true;
    }
}
