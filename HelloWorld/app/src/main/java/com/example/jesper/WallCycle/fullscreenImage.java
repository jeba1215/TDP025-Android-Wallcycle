package com.example.jesper.WallCycle;

import android.app.Activity;
import android.app.WallpaperManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import java.io.IOException;


public class fullscreenImage extends Activity {
    String img = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen_image);

        Bundle b = getIntent().getExtras();
        img = b.getString("img");

        ImageView imageview = (ImageView) findViewById(R.id.imageView);
        Drawable db = Drawable.createFromPath(img);
        imageview.setImageDrawable(db);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.fullscreen_image, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }else if(id == R.id.action_setWallpaper){
            Handler handler=new Handler();

            final Runnable r = new Runnable()
            {
                public void run()
                {
                    try {
                        WallpaperManager wpm = WallpaperManager.getInstance(fullscreenImage.this);
                        Bitmap bmp = BitmapFactory.decodeFile(img);
                        wpm.setBitmap(bmp);
                    }catch(IOException e){
                        e.printStackTrace();
                    }
                }
            };

            handler.postDelayed(r, 1000);

        }else if(id == R.id.action_Remove){
            //TODO implement function
        }
        return super.onOptionsItemSelected(item);
    }
}
