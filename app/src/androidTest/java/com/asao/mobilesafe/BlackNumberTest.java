package com.asao.mobilesafe;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.asao.mobilesafe.bean.BlackNumberInfo;
import com.asao.mobilesafe.db.dao.BlackNumberDao;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.Random;

import static org.junit.Assert.assertEquals;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class BlackNumberTest {
    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("com.asao.mobilesafe", appContext.getPackageName());
    }

    @Test
    public void testAdd(){
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        BlackNumberDao blackNumberDao = new BlackNumberDao(appContext);
        //blackNumberDao.add("110", 2);
        Random random = new Random();
        //random.nextInt(3);//表示生成0-2的数据，不包含尾数
        for (int i = 0; i < 200; i++) {
            blackNumberDao.add("156123152"+i, random.nextInt(3));
        }
    }

    @Test
    public void testQueryALL(){
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        BlackNumberDao blackNumberDao = new BlackNumberDao(appContext);
        List<BlackNumberInfo> queryAll = blackNumberDao.queryAll();

        for (BlackNumberInfo blackNumberInfo : queryAll) {
            System.out.println(blackNumberInfo.toString());
        }
    }











}