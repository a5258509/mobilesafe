package com.asao.mobilesafe;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.asao.mobilesafe.bean.ContactInfo;
import com.asao.mobilesafe.engine.ContactsEngine;

import java.util.List;

public class ContactActivity extends AppCompatActivity {

    private ListView mContacts;
    private List<ContactInfo> contactsInfos;
    private ProgressBar mLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);
        
        initView();
    }

    private void initView() {
        mContacts =findViewById(R.id.contact_lv_contacts);
        mLoading = findViewById(R.id.contact_pb_loading);


        intData();
        //listview,即条目的点击事件
        mContacts.setOnItemClickListener((parent, view, position, id) -> {
            //将选中的条目对应的号码传递给setup3界面显示,同时还要移除界面
            //设置结果的方法,将结果返回给调用的界面
            Intent data=new Intent();
            data.putExtra("number",contactsInfos.get(position).number);
            setResult(RESULT_OK, data);//参数1:结果码 参数2:传递数据的意图
            //移除界面
            finish();
        });
    }
    /**
     * 获取数据展示数据
     *
     */
    private void intData() {

        //查询数据库是耗时操作,所以要放到子线程中去执行
        //在加载数据之前,显示进度条
        mLoading.setVisibility(View.VISIBLE);
        new Thread(){
            @Override
            public void run() {
                super.run();
                contactsInfos = ContactsEngine.getContactsInfo(ContactActivity.this);

                //当数据查询完成,展示数据
                //展示数据属于更新UI操作,必须放在主线程,所以用runOnUiThread的方法,
                runOnUiThread(() -> {
                    //设置listview的adapter
                    mContacts.setAdapter(new Myadapter());
                    //在加载完数据,隐藏进度条
                    mLoading.setVisibility(View.GONE);
                });

            }
        }.start();


    }

    private class Myadapter extends BaseAdapter{



        @Override
        public int getCount() {
            return contactsInfos.size();
        }

        @Override
        public Object getItem(int position) {
            return contactsInfos.get(position);
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
                view = View.inflate(getApplicationContext(), R.layout.contact_listview_item, null);
                //1.创建盒子
                viewHolder=new ViewHolder();
                //2.将findviewbyid放到盒子中
                viewHolder.mIcon=view.findViewById(R.id.item_iv_icon);
                viewHolder.mName=view.findViewById(R.id.item_tv_name);
                viewHolder.mNumber=view.findViewById(R.id.item_tv_number);
                //3.将盒子与view对象进行绑定,一起复用
                view.setTag(viewHolder);//将viewHolder与view对象绑定
            }else{
                view=convertView;
                //4.复用缓存,拿到缓存对象之后,就可以将和复用view对象绑定的viewHolder获取出来
                 viewHolder = (ViewHolder) view.getTag();//获取和view对象绑定的数据
            }


            //获取数据展示数据
            ContactInfo contactInfo = contactsInfos.get(position);
            //5.使用盒子中保存的findViewById好的控件操作
            viewHolder.mName.setText(contactInfo.name);
            viewHolder.mNumber.setText(contactInfo.number);
            //显示头像,获取头像
            Bitmap contactPhoto = ContactsEngine.getContactPhoto(getApplicationContext(), contactInfo.id);
            if(contactPhoto!=null){
                viewHolder.mIcon.setImageBitmap(contactPhoto);
            }else{
                Drawable drawable = getResources().getDrawable(R.mipmap.ic_contact);
                BitmapDrawable bd = (BitmapDrawable) drawable;
                Bitmap bmp=bd.getBitmap();
                viewHolder.mIcon.setImageBitmap(bmp);
            }

            return view;
        }
    }

    //1.创建盒子
    static class ViewHolder{
        ImageView mIcon;
        TextView  mName;
        TextView mNumber;



    }






}