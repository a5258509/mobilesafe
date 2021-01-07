package com.asao.mobilesafe.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.asao.mobilesafe.R;

/**
 * 抽取设置中心条目布局的自定义控件
 * */
public class SettingView extends RelativeLayout {
    private static final String NAMESPACE ="http://schemas.android.com/apk/res-auto";
    private TextView mTitle;
    private ImageView mIcon;
    //存储开关状态
    private Boolean mIsToggle;

    public SettingView(Context context) {
        this(context,null);
    }

    public SettingView(Context context, AttributeSet attrs) {
        this(context,attrs,-1);
    }

    public SettingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        //将包含有textview和imageView的布局文件添加到自定义控件中显示
        initView();

        //将自定义控件中的属性值获取出来,设置给自定义控件中的textview显示
        String title = attrs.getAttributeValue(NAMESPACE, "title");
        //将获取的属性值设置给textview展示
        mTitle.setText(title);
        boolean istoggle = attrs.getAttributeBooleanValue(NAMESPACE, "istoggle", true);
        mIcon.setVisibility(istoggle?View.VISIBLE:View.GONE);//设置图片的隐藏或显示
    }

    private void initView() {
        View view = View.inflate(getContext(), R.layout.textview_imageview, null);
        this.addView(view);//将view对象添加到自定义控件中
        //初始化控件
        mTitle = view.findViewById(R.id.setting_tv_title);
        mIcon = view.findViewById(R.id.setting_iv_icon);
    }

    /**
     * 提供给activity调用的方法,根据传递过来的开关状态,
     *设置自定义控件中的图片的显示类型
     */
    public void setToggleOn(boolean isToggle){
        mIsToggle=isToggle;
        if(isToggle){
            mIcon.setImageResource(R.mipmap.on);
        }else{
            mIcon.setImageResource(R.mipmap.off);
        }
    }

    public boolean istoggle(){
        return mIsToggle;
    }

    public void toggle(){
        //获取开关状态
        setToggleOn(!mIsToggle);

    }
}
