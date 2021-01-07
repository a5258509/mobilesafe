package com.asao.mobilesafe;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;
/**
 * setup界面的父类
 *
 *2016-10-10  下午3:08:56
 */
public abstract class BaseActivity extends Activity {

    private GestureDetector detector;

    //因为每个界面都有上一步和下一步的按钮的点击事件，所有相同操作，抽取到父类
    //本质：抽取按钮点击事件的

    //按钮的点击事件

    /**
     * 上一步的点击事件
     *
     * @param view 2016-10-10 上午11:17:27
     */
    public void pre(View view) {
        doPre();
    }

    /**
     * 下一步点击事件
     *
     * @param view 2016-10-10 上午11:17:44
     */
    public void next(View view) {
        doNext();
    }

    //按钮点击事件的具体实现

    /**
     * 上一步的具体操作
     * <p>
     * 2016-10-10 下午3:12:55
     */
    private void doPre() {
		/*Intent intent = new Intent(this,SetUp1Activity.class);
		startActivity(intent);*/
        if (pre_activity()) {
            return;
        }
        finish();
        overridePendingTransition(R.anim.anim_setup_pre_enter, R.anim.anim_setup_pre_exit);
    }

    /**
     * 下一步的具体操作
     * <p>
     * 2016-10-10 下午3:12:55
     */
    private void doNext() {
		/*Intent intent = new Intent(this,SetUp3Activity.class);
		startActivity(intent);*/
        if (next_activity()) {
            return;
        }
        finish();
        overridePendingTransition(R.anim.anim_setup_next_enter, R.anim.anim_setup_next_exit);
    }

    //但是父类不知道相应activity应该跳转到那个界面，处理方案：父类可以设置抽象方法，子类实现父类的抽象方法，根据自己的特性进行相应的实现,模板模式
    /**
     * 上一步具体操作的抽象方法，由子类实现，子类根据自己的特性进行跳转操作实现
     * 由子类返回boolean值，true:不能执行跳转操作，false:可以执行跳转操作
     * 2016-10-10 下午3:18:12
     */
    public abstract boolean pre_activity();
    /**
     * 下一步具体操作的抽象方法，由子类实现，子类根据自己的特性进行跳转操作实现
     *
     * 2016-10-10 下午3:18:12
     */
    public abstract boolean next_activity();


    /**
     * 实体返回键的响应操作
     *
     *2016-10-10  下午3:45:28
     */
    @Override
    public void onBackPressed() {

        //点击返回键执行上一步操作
        doPre();

        super.onBackPressed();//没效果，可以删除
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //1.获取手势识别器
        detector = new GestureDetector(this, new MyOnGestureDetectorListener());
    }
    /**手势识别器监听**/
    private class MyOnGestureDetectorListener extends SimpleOnGestureListener{
        //滑动事件
        //e1 : 按下事件，保存有按下的坐标
        //e2 : 抬起的事件，保存有抬起的坐标
        //velocity : 滑动的速率，速度
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                               float velocityY) {

            //获取按下和抬起的坐标
            int downX = (int) e1.getRawX();
            int upX = (int) e2.getRawX();

            //获取按下和抬起的y坐标
            int downY = (int) e1.getRawY();
            int upY = (int) e2.getRawY();

            //屏蔽斜滑操作
            if (Math.abs(downY - upY) > 50) {
                Toast.makeText(getApplicationContext(), "不要乱滑哟!", Toast.LENGTH_SHORT).show();
                return true;
            }

            //下一步
            if (downX - upX > 100) {
                doNext();
            }
            //上一步
            if (upX - downX > 100) {
                doPre();
            }

            //true if the event is consumed, else false
            //如果想要执行事件，返回true,不想执行事件，返回false
            return true;
        }

    }

    //2.需要让手势识别器和界面的触摸事件发送关系
    //界面的触摸事件
    //MotionEvent : 触摸事件
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        detector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }







}
