package com.asao.mobilesafe;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.asao.mobilesafe.bean.BlackNumberInfo;
import com.asao.mobilesafe.db.dao.BlackNumberDao;

import java.util.List;

public class BlackNumberActivity extends AppCompatActivity {

    //添加请求码
    private static final int REQUESTADDCODE = 100;
    //更新请求码
    private static final int REQUESTUPDATECODE = 101;
    private ImageView mEmpty;
    private ListView mListView;
    private LinearLayout mLLLoading;
    private BlackNumberDao blackNumberDao;
    private List<BlackNumberInfo> infoList;
    private Myadapter myadapter;
    //查询个数
    private final int MAXNUM=20;
    //起始查询位置
    private int startIndex=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_black_number);
        blackNumberDao = new BlackNumberDao(this);

        initView();
    }

    private void initView() {
        mEmpty = findViewById(R.id.blacknumber_iv_empty);
        mListView = findViewById(R.id.blacknumber_lv_listview);
        mLLLoading = findViewById(R.id.blacknumber_ll_loading);



        initData();

        //设置条目点击事件
        mListView.setOnItemClickListener((parent, view, position, id) -> {
           //跳转到更新界面
            Intent intent =new Intent(BlackNumberActivity.this,BlackNumberUpdateActivity.class);
            //传递被点击的数据给更新界面
            intent.putExtra("number",infoList.get(position).blacknumber);
            intent.putExtra("mode",infoList.get(position).mode);
            //需要将更新哪个条目的索引告诉更新界面
            intent.putExtra("position",position);
            //startActivity(intent);
            startActivityForResult(intent,REQUESTUPDATECODE);


        });

        //监听listview的滚动状态,当停止滚动并且当前界面的最后一条数据是listview的最后一条数据，加载下一波数据
        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            //当listview的滚动状态改变的时候调用的方法
            //scrollState : listview的滚动状态
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                //SCROLL_STATE_IDLE;//空闲的状态，停止滚动的状态
                //SCROLL_STATE_TOUCH_SCROLL;//触摸滚动的状态
                //SCROLL_STATE_FLING;//快速滚动的状态（惯性滚动状态）
                //判断listview的状态是否是滚动状态
                if(scrollState==SCROLL_STATE_IDLE){
                    //判断当前界面的最后一条数据是listview的最后一条数据
                    int lastVisiblePosition = mListView.getLastVisiblePosition();//获取当前界面显示的最后一条数据(眼睛能看到的最后一条)，返回的是条目的索引
                    if(lastVisiblePosition==infoList.size()-1){
                        startIndex+=MAXNUM;
                        //再次加载数据
                         initData();
                    }
                }
            }

            //listview滚动的时候调用的方法
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });


    }

    //获取数据展示数据
    private void initData() {
        //因为是查询数据库操作，属于耗时操作，需要隐藏/显示进度条
        //加载数据之前，显示进度条
        mLLLoading.setVisibility(View.VISIBLE);
        new Thread(){
            @Override
            public void run() {
                super.run();
                //infoList = blackNumberDao.queryAll();
                //加载分页数据的时候，会将上次加载的数据覆盖
                if(infoList==null){
                    infoList = blackNumberDao.queryPartAll(MAXNUM,startIndex);
                }else{
                    //将一个集合跟另一个集合合并，谁调用的addALL方法最终得到那个集合
                    //A[1,2,3]
                    //B[4,5,6]  A.addAll(B)    A[1,2,3,4,5,6]
                    infoList.addAll(blackNumberDao.queryPartAll(MAXNUM,startIndex));
                }


                //查询数据库完成,展示数据
                runOnUiThread(() -> {
                    if(myadapter==null){
                        myadapter = new Myadapter();
                        mListView.setAdapter(myadapter);//给listview设置adapter,listview会自动显示第一页的数据，重复执行该方法，相当于给listview重复设置新的adapter
                    }else{
                        myadapter.notifyDataSetChanged();//在原有的基础上刷新数据
                    }
                    //如果listview没有数据,就显示image,如果listview有数据,就隐藏imageview
                    mListView.setEmptyView(mEmpty);
                    //在加载完数据,隐藏进度条
                    mLLLoading.setVisibility(View.GONE);
                });
            }
        }.start();
    }

    private class Myadapter extends BaseAdapter {


        @Override
        public int getCount() {
            return infoList.size();
        }

        @Override
        public Object getItem(int position) {
            return infoList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if(convertView==null){
               convertView = View.inflate(getApplicationContext(), R.layout.blacknumber_listview_item, null);
               viewHolder=new ViewHolder();
               viewHolder.mNumber=convertView.findViewById(R.id.item_tv_number);
               viewHolder.mMode=convertView.findViewById(R.id.item_tv_mode);
               viewHolder.mDelete= convertView.findViewById(R.id.item_iv_delete);
                convertView.setTag(viewHolder);
            }else{
                viewHolder = (ViewHolder) convertView.getTag();
            }
            //获取数据,展示数据
            BlackNumberInfo blackNumberInfo = infoList.get(position);
            viewHolder.mNumber.setText(blackNumberInfo.blacknumber);
            //因为数据库中的拦截类型是0,1,2
            switch (blackNumberInfo.mode){
                case 0:
                    //电话拦截
                    viewHolder.mMode.setText("电话拦截");
                    break;
                case 1:
                    //短信拦截
                    viewHolder.mMode.setText("短信拦截");
                    break;
                case 2:
                    //全部拦截
                    viewHolder.mMode.setText("全部拦截");
                    break;
            }

            //设置删除按钮的点击事件
            viewHolder.mDelete.setOnClickListener(v -> {
                //弹出删除黑名单对话框
                AlertDialog.Builder builder=new AlertDialog.Builder(BlackNumberActivity.this);
                builder.setMessage("是否删除黑名单号码"+blackNumberInfo.blacknumber);
                builder.setPositiveButton("确定", (dialog, which) -> {
                    //删除黑名单号码
                    //1.从数据库删除
                    boolean delete = blackNumberDao.delete(blackNumberInfo.blacknumber);
                    if(delete){
                        //2.从界面删除
                        infoList.remove(blackNumberInfo);//从集合中将每一个blackNumberInfo的bean类删除
                        myadapter.notifyDataSetChanged();
                    }else{
                        Toast.makeText(getApplicationContext(),"系统繁忙,稍候再试",Toast.LENGTH_SHORT).show();
                    }
                    //隐藏对话框
                    dialog.dismiss();

                });
                builder.setNegativeButton("取消",null);//如果只是隐藏对话框,可以直接设置为null,仅限于隐藏对话框,如果有别的操作就不行
                builder.show();
            });
            return convertView;
        }
    }


    static class ViewHolder{
        TextView mNumber,mMode;
        ImageView mDelete;
    }







    //添加按钮点击事件
    public void enteradd(View view) {
        Intent intent=new Intent(this,BlackNumberAddActivity.class);
        //startActivity(intent);
        startActivityForResult(intent,REQUESTADDCODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //接收数据
        //因为添加和更新界面都会传递数据回来,所以要判断数据是谁传递来的
        if(requestCode==REQUESTADDCODE){
            //接收添加界面传递的数据
            if(resultCode==RESULT_OK){
                if(data!=null){
                    //获取数据
                    String number = data.getStringExtra("number");
                    int mode = data.getIntExtra("mode", -1);

                    //因为listview中的数据是通过list列表显示的,而list列表中的每个元素又是一个java bean
                    // 所以先将数据存放到bean类
                    BlackNumberInfo blackNumberInfo=new BlackNumberInfo(number,mode);
                    //再将bean类添加到集合
                    //infoList.add(blackNumberInfo);
                    infoList.add(0,blackNumberInfo);//将数据添加到集合的哪个位置,添0表示放到集合的第0个位置,即最上面
                    //更新界面,将新的数据刷新出来
                    myadapter.notifyDataSetChanged();//调用此方法,会重新调用adapter的getcount和getview方法,刷新界面
                }
            }
        }else if(requestCode==REQUESTUPDATECODE){
            //接收更新界面传递的数据
            if(resultCode==RESULT_OK){
                if(data!=null){
                    int updatemode = data.getIntExtra("mode", -1);
                    //将更新的拦截类型设置给更新的条目,重新刷新界面
                    int position = data.getIntExtra("position", -1);
                    //根据infoList集合中的position索引找到要更新的具体元素,再通过.mode的方式找到要
                    //更新的bean中的mode属性,最后将更新后的mode值updatemode赋给它即可
                    infoList.get(position).mode=updatemode;
                    //更新界面,将新的数据刷新出来
                    myadapter.notifyDataSetChanged();
                }
            }
        }
    }











}