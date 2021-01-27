package com.asao.mobilesafe;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.asao.mobilesafe.bean.BlackNumberInfo;
import com.asao.mobilesafe.db.AppLockOpenHelper;
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
public class AppLockTest {
    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("com.asao.mobilesafe", appContext.getPackageName());
    }

    @Test
    public void testCreateDB(){
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        AppLockOpenHelper appLockOpenHelper=new AppLockOpenHelper(appContext);//不会创建数据库
        appLockOpenHelper.getWritableDatabase();//才会创建数据库


    }


}
