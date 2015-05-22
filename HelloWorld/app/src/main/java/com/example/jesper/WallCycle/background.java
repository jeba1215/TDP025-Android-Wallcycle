package com.example.jesper.WallCycle;

import android.app.Service;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class background extends Service {

    static localImageHandler imageHandler = new localImageHandler();
    static String path = "";
    static Handler handler = new Handler();
    static SharedPreferences settings;
    static Editor editor;
    static PowerManager mgr;
    static PowerManager.WakeLock wakeLock;

    public Runnable runnable = new Runnable(){
        @Override
        public void run(){
            if(!wakeLock.isHeld())
                wakeLock.acquire();
            int i = imageHandler.getNextIndex();
            if( i != -1){
                Log.v("trace", "getting index for wallpaper: "+i+" which returns "+ imageHandler.get(i));
                setWallpaper(imageHandler.get(i));
            }
            handler.postDelayed(this, settings.getInt("Timer", 0));
            if(wakeLock.isHeld())
                wakeLock.release();
        }
    };

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onCreate() {
        path = Environment.getExternalStorageDirectory().getAbsolutePath()+"/WallCycle/";
        mgr = (PowerManager)this.getSystemService(Context.POWER_SERVICE);
        wakeLock = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyWakeLock");
        settings = this.getSharedPreferences("settings", Context.MODE_PRIVATE);
        editor = settings.edit();

        Log.v("trace", "onCreate");
    }

    @Override
    public void onStart(Intent intent, int startId) {
        Log.v("trace", "onStart");
        handler.postDelayed(runnable, 0);
    }

    @Override
    public void onDestroy() {
        Log.v("trace", "onDestroy");
        handler.removeCallbacks(runnable);
    }

    public background(){}


    public void setWallpaper(String path) {
        WallpaperManager wpm = WallpaperManager.getInstance(this.getBaseContext());
        File f = new File(path);
        Log.v("trace", "settings wallpaper to " + path);
        try {
            InputStream ins = new FileInputStream(f);
            wpm.setStream(ins);
            ins.close();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }
}
