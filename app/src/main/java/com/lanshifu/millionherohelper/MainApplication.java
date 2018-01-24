package com.lanshifu.millionherohelper;

import android.content.Context;
import android.util.Log;

import com.lanshifu.baselibrary.BaseApplication;

import org.litepal.LitePal;

/**
 * Created by lanxiaobin on 2018/1/4.
 */

public class MainApplication extends BaseApplication {

    private static final String TAG = "MainApplication-lxb";

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        long start = System.currentTimeMillis();
        Log.d(TAG, "attachBaseContext: use time:" + (System.currentTimeMillis() - start));
        LitePal.initialize(base);
    }
}
