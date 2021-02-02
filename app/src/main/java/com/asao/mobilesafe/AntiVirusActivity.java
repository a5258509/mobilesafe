package com.asao.mobilesafe;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.asao.mobilesafe.bean.Appinfo;
import com.asao.mobilesafe.db.dao.AntivirusDao;
import com.asao.mobilesafe.engine.AppEngine;
import com.github.lzyzsd.circleprogress.ArcProgress;

import java.util.ArrayList;
import java.util.List;

public class AntiVirusActivity extends AppCompatActivity implements View.OnClickListener {

    private MyAsyncTask myAsyncTask;
    /**保存数据的集合**/
    private List<Appinfo> list = new ArrayList<Appinfo>();
    /**保存病毒的集合**/
    private List<Appinfo> antiviruslist = new ArrayList<Appinfo>();
    private ListView mListView;
    private Myadapter myadapter;
    private ArcProgress mPb;
    private TextView mPackageName;
    private int maxProgress;
    private LinearLayout mLLAgainScan;
    private LinearLayout mLLScan;
    private TextView mText;
    private Button mAgain;
    private LinearLayout mLLImage;
    private int width;
    private ImageView mRight;
    private ImageView mLeft;
    private MyReceiver myReceiver;
    private List<Appinfo> allAppInfos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anti_virus);
        setRemoveReceiver();
        initView();
        initData();

    }

    private class MyReceiver extends BroadcastReceiver {

        @SuppressLint("SetTextI18n")
        @Override
        public void onReceive(Context context, Intent intent) {
            //接受卸载的应用的包名
            String dataString = intent.getDataString();//获取广播接受者接受的数据

            //接受的包名信息中会包含package:
            //遍历list集合，查看卸载的应用程序的包名是否在集合中，如果在，从集合中删除
            //遍历集合的时候不能删除集合中的数据,所以不能遍历list集合,而又因为list集合中的数据就是从allAppInfos中获取的，
            // 当加载完成的时候，list集合中的数据是和allAppInfos中数据是一样,下标也是一样,所以直接遍历allappinfos集合去删除list中的数据
            for (int i = 0; i < allAppInfos.size(); i++) {
                if (dataString.contains(allAppInfos.get(i).packageName)) {
                    list.remove(allAppInfos.get(i));
                    myadapter.notifyDataSetChanged();
                    antiviruslist.remove(allAppInfos.get(i));
                    if (antiviruslist.size() > 0) {
                        mText.setText("发现"+antiviruslist.size()+"个病毒");
                    }else{
                        mText.setText("手机非常安全");
                    }
                }
            }
        }

    }

    /**
     * 注册监听卸载的广播接受者
     *
     * 2016-10-26 上午11:47:56
     */
    private void setRemoveReceiver() {
        myReceiver = new MyReceiver();
        IntentFilter filter = new IntentFilter();
        //ACTION_PACKAGE_ADDED : 安装广播事件
        //ACTION_PACKAGE_REMOVED : 卸载的广播事件
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        //设置接受卸载的应用的包名
        filter.addDataScheme("package");
        registerReceiver(myReceiver, filter);
    }

    private void initView() {
        mListView = findViewById(R.id.anti_lv_listview);

        mPb = findViewById(R.id.anti_acp_pb);
        mPackageName = findViewById(R.id.anti_tv_packageName);

        mLLAgainScan = findViewById(R.id.anti_ll_againscan);
        mLLScan = findViewById(R.id.anti_ll_scan);
        mText = findViewById(R.id.anti_tv_text);

        mAgain = findViewById(R.id.anti_btn_again);
        mAgain.setOnClickListener(this);

        mLLImage = findViewById(R.id.anti_ll_image);

        mLeft = findViewById(R.id.anti_iv_left);
        mRight = findViewById(R.id.anti_iv_right);

    }

    private void initData() {
        myAsyncTask = new MyAsyncTask();
        myAsyncTask.execute();

    }



    private class MyAsyncTask extends AsyncTask<Void,Appinfo,Void>{


        @Override
        protected void onPreExecute() {
            if (myAsyncTask.isCancelled()) {
                return;
            }

            list.clear();//保证每次保存都是最新的数据
            antiviruslist.clear();

            //加载数据之前先给listview设置adapter，方便后面进行更新界面操作
            myadapter = new Myadapter();
            mListView.setAdapter(myadapter);

            //重新扫描，进度条布局显示，重新扫描布局隐藏
            mLLAgainScan.setVisibility(View.GONE);
            mLLImage.setVisibility(View.GONE);
            mLLScan.setVisibility(View.VISIBLE);

            super.onPreExecute();
        }


        @Override
        protected Void doInBackground(Void... voids) {
            //1.加载系统中所有的应用app
            allAppInfos = AppEngine.getAllAppInfos(getApplicationContext());

            maxProgress = allAppInfos.size();


            for(Appinfo appinfo: allAppInfos){

                if (myAsyncTask.isCancelled()) {
                    return null;
                }

                //8.根据应用的签名信息,查询数据库判断应用是否是病毒
                boolean isantivirus = AntivirusDao.isAntivirus(getApplicationContext(), appinfo.md5);
                //将是否是病毒的标示，保存到bean类
                appinfo.isAntiVirus=isantivirus;

                //2.将应用程序的信息，传递给onProgressUpdate方法进行显示
                publishProgress(appinfo);
                SystemClock.sleep(150);

            }
            return null;
        }

        @SuppressLint("SetTextI18n")
        @Override
        protected void onPostExecute(Void aVoid) {

            if (myAsyncTask.isCancelled()) {
                return ;
            }

            //6.加载完成，自动滚动到listview的第一个条目
            mListView.smoothScrollToPosition(0);

            //10.扫描完成，隐藏进度条布局，显示重新扫描的布局，显示病毒个数信息
            mLLScan.setVisibility(View.GONE);
            mLLAgainScan.setVisibility(View.VISIBLE);
            mLLImage.setVisibility(View.VISIBLE);
            //设置显示病毒的个数的信息
            if (antiviruslist.size() > 0) {
                mText.setText("发现"+antiviruslist.size()+"个病毒");
            }else{
                mText.setText("手机非常安全");
            }

            //11.将进度条的布局绘制设置成图片
            mLLScan.setDrawingCacheEnabled(true);//是否可以绘制控件的图片，true:可以
            mLLScan.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);//设置绘制的图片的质量
            Bitmap drawingCache = mLLScan.getDrawingCache();//绘制图片，并返回使用bitmap保存的图片
            //12.拆分进度条布局的图片，放到相应的imageview进行操作
            Bitmap leftBitmap = getLeftBitmap(drawingCache);
            Bitmap rihtBitmap = getRihtBitmap(drawingCache);
            //把图片设置给imageview显示
            mLeft.setImageBitmap(leftBitmap);
            mRight.setImageBitmap(rihtBitmap);

            //13.开始动画
            //渐变+平移效果
            startAnimaition();
            super.onPostExecute(aVoid);
        }

        @Override
        protected void onProgressUpdate(Appinfo... values) {

            if (myAsyncTask.isCancelled()) {
                return ;
            }

            //3.接收doInBackground传递过来的数据
            Appinfo appinfo=values[0];
            //4.显示数据
            //9.如果是病毒，添加list集合第一个位置
            if (appinfo.isAntiVirus) {
                list.add(0,appinfo);
                //保存到病毒的集合中，方便重新扫描界面显示
                antiviruslist.add(appinfo);
            }else{
                list.add(appinfo);
            }
            myadapter.notifyDataSetChanged();
            //5.每加载一条数据,自动滚动到listview的最后一个条目
            mListView.smoothScrollToPosition(list.size());

            //7.每加载一个条目，显示一个进度，显示加载的应用程序的包名
            //加载条目占用总条目数的比例
            int progress = (int) (list.size() * 100f / maxProgress + 0.5f);
            mPb.setProgress(progress);//设置当前进度
            mPb.setText(String.valueOf(progress));//不加这一行,进度条数字不会变化
            mPackageName.setText(appinfo.packageName);
            super.onProgressUpdate(values);
        }

    }

    /**
     * 执行动画的操作
     */
    public void startAnimaition() {
        //左右图片：平移+渐变    重新扫描的布局：渐变
        //mLeft.setTranslationX(translationX)
        //mLeft.setAlpha(alpha)
        //target : 执行动画的控件
        //propertyName : 执行动画的名称
        //values : 动画执行的值
        ObjectAnimator animator1 = ObjectAnimator.ofFloat(mLeft, "translationX", 0,-width);
        ObjectAnimator animator2 = ObjectAnimator.ofFloat(mLeft, "alpha", 1.0f,0.0f);

        ObjectAnimator animator3 = ObjectAnimator.ofFloat(mRight, "translationX", 0,width);
        ObjectAnimator animator4 = ObjectAnimator.ofFloat(mRight, "alpha", 1.0f,0.0f);

        ObjectAnimator animator5 = ObjectAnimator.ofFloat(mLLAgainScan, "alpha", 0.0f,1.0f);

        //组合动画
        AnimatorSet animatorSet = new AnimatorSet();
        //设置动画一起执行
        animatorSet.playTogether(animator1,animator2,animator3,animator4,animator5);
        animatorSet.setDuration(1000);
        //执行动画
        animatorSet.start();

    }


    //拆分进度条左边的图片
    private Bitmap getLeftBitmap(Bitmap drawingCache) {
        //1.设置新的图片的宽高
        width = drawingCache.getWidth()/2;
        int height=drawingCache.getHeight();
        //2.创建保存新的图片的载体
        //config : 图片的配置信息，因为是按照原图片进行绘制的，所以配置信息必须和原图一致
        Bitmap createBitmap = Bitmap.createBitmap(width, height, drawingCache.getConfig());
        //3.绘制图片
        //bitmap : 绘制的图片保存的地方
        Canvas canvas=new Canvas(createBitmap);
        Matrix matrix=new Matrix();
        Paint paint=new Paint();
        //bitmap : 根据哪张图片绘制，原图
        //matrix : 矩阵
        //paint : 画笔
        canvas.drawBitmap(drawingCache,matrix,paint);
        return createBitmap;
    }



     //拆分进度条图片右边的图片
    public Bitmap getRihtBitmap(Bitmap drawingCache) {
        //1.设置新的图片的宽高
        int width = drawingCache.getWidth() / 2;
        int height = drawingCache.getHeight();
        //2.创建保存新的图片的载体
        //config : 图片的配置信息，因为是按照原图片进行绘制的，所以配置信息必须和原图一致
        Bitmap createBitmap = Bitmap.createBitmap(width, height, drawingCache.getConfig());
        //3.绘制图片
        //bitmap : 绘制的图片保存的地方
        Canvas canvas = new Canvas(createBitmap);
        Matrix matrix = new Matrix();
        //参数：画布x和y轴平移的距离
        matrix.postTranslate(-width, 0);
        Paint paint = new Paint();
        //bitmap : 根据哪张图片绘制，原图
        //matrix : 矩阵
        //paint : 画笔
        canvas.drawBitmap(drawingCache, matrix, paint);

        return createBitmap;
    }


    private class Myadapter extends BaseAdapter{



        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            ViewHolder viewHolder;
            if(convertView==null){
                view = View.inflate(getApplicationContext(), R.layout.antivirus_listview_item, null);
                viewHolder=new ViewHolder();
                viewHolder.mIcon=view.findViewById(R.id.item_iv_icon);
                viewHolder.mName=view.findViewById(R.id.item_tv_name);
                viewHolder.mIsAntVirus=view.findViewById(R.id.item_tv_isAntivirus);
                viewHolder.mClear=view.findViewById(R.id.item_iv_clear);
                view.setTag(viewHolder);
            }else{
                view=convertView;
                viewHolder= (ViewHolder) view.getTag();
            }
            Appinfo appinfo = list.get(position);
            viewHolder.mIcon.setImageDrawable(appinfo.icon);
            viewHolder.mName.setText(appinfo.name);

            //根据bean类中的病毒标示，设置显示不同的结果
            if (appinfo.isAntiVirus) {
                //病毒
                viewHolder.mIsAntVirus.setText("病毒");
                viewHolder.mIsAntVirus.setTextColor(Color.RED);
                viewHolder.mClear.setVisibility(View.VISIBLE);
            }else{
                //安全
                viewHolder.mIsAntVirus.setText("安全");
                viewHolder.mIsAntVirus.setTextColor(Color.GREEN);
                viewHolder.mClear.setVisibility(View.GONE);
            }

            //卸载操作
            viewHolder.mClear.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    //跳转到系统的卸载界面
                    Intent intent = new Intent();
                    intent.setAction("android.intent.action.DELETE");
                    intent.addCategory("android.intent.category.DEFAULT");
                    intent.setData(Uri.parse("package:"+appinfo.packageName));
                    startActivity(intent);
                }
            });

            return view;
        }
    }

    static class ViewHolder{
        TextView mName,mIsAntVirus;
        ImageView mIcon,mClear;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.anti_btn_again:
                //重新扫描
                //关门的动画
                backAnimation();
                break;
        }
    }

    /**
     * 关门的动画
     *
     * 2016-10-26 上午11:35:10
     */
    private void backAnimation() {
        //平移
        ObjectAnimator animator1 = ObjectAnimator.ofFloat(mLeft, "translationX", -width,0);
        ObjectAnimator animator2 = ObjectAnimator.ofFloat(mLeft, "alpha", 0.0f,1.0f);

        ObjectAnimator animator3 = ObjectAnimator.ofFloat(mRight, "translationX", width,0);
        ObjectAnimator animator4 = ObjectAnimator.ofFloat(mRight, "alpha", 0.0f,1.0f);

        ObjectAnimator animator5 = ObjectAnimator.ofFloat(mLLAgainScan, "alpha", 1.0f,0.0f);

        //组合动画
        AnimatorSet animatorSet = new AnimatorSet();
        //设置动画一起执行
        animatorSet.playTogether(animator1,animator2,animator3,animator4,animator5);
        animatorSet.setDuration(1000);
        //动画执行完，才能重新进行扫描操作
        animatorSet.addListener(new Animator.AnimatorListener() {
            //动画开始调用
            @Override
            public void onAnimationStart(Animator animation) {

            }
            //动画重复调用
            @Override
            public void onAnimationRepeat(Animator animation) {

            }
            //动画结束调用
            @Override
            public void onAnimationEnd(Animator animation) {
                initData();
            }
            //动画取消调用
            @Override
            public void onAnimationCancel(Animator animation) {

            }
        });
        //执行动画
        animatorSet.start();
        //animatorSet.end();//动画停止
        //animatorSet.cancel();//取消动画
    }

    @Override
    protected void onDestroy() {
        myAsyncTask.cancel(true);
        unregisterReceiver(myReceiver);
        super.onDestroy();
    }

}