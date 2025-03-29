package com.HZFinger_FpStdSample;

import android.app.Application;
import android.util.Log;
import android.widget.Toast;

public class MyApplication extends Application {
    private static final String TAG = "MyApplication";

    @Override
    public void onCreate() {
        super.onCreate();
        
        // 设置全局异常处理
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable ex) {
                Log.e(TAG, "应用发生崩溃: " + ex.getMessage(), ex);
                // 这里可以将崩溃信息写入文件或发送到服务器
            }
        });
    }
}