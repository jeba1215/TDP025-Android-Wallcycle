package com.example.jesper.WallCycle;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import java.io.Console;
import java.io.File;
import java.util.ArrayList;

import com.amazon.insights.Event;
import com.amazon.insights.InsightsCallback;
import com.amazon.insights.Variation;
import com.amazon.insights.VariationSet;
import com.squareup.picasso.Picasso;

public class MyActivity extends Activity {
    SharedPreferences settings;
    Editor editor;
    ImageAdapter myImageAdapter;
    private GridView gridview;
    ImageView pic;
    int screen_width, screen_height;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
        directorySetup();

        gridview = (GridView) findViewById(R.id.gridview);
        pic = (ImageView) findViewById(R.id.picture);
        myImageAdapter = new ImageAdapter(this);
        gridview.setAdapter(myImageAdapter);
        pic.setVisibility(View.GONE);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        screen_width = size.x;
        screen_height = size.y;

        preferences();

        final ActionBar actionBar = getActionBar();
        BitmapDrawable background = new BitmapDrawable (BitmapFactory.decodeResource(getResources(), R.drawable.header));
        background.setTileModeX(android.graphics.Shader.TileMode.REPEAT);
        actionBar.setBackgroundDrawable(background);

        String targetPath = Environment.getExternalStorageDirectory() + "/WallCycle/";

        Log.v("trace", targetPath);

        for (File file : new File(targetPath).listFiles()){
		     myImageAdapter.add(file.getAbsolutePath());
        }

        myAsyncTaskLoadFiles = new AsyncTaskLoadFiles(myImageAdapter);
        myAsyncTaskLoadFiles.execute();


        pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pic.setVisibility(View.GONE);
                //adapterRefresh();
            }
        });
    }

    public void preferences() {
        settings = getSharedPreferences("settings", MODE_PRIVATE);
        editor = settings.edit();
        if (!settings.contains("KEY_FIRST_RUN")) {
            editor.putInt("Timer", 60000);
            editor.putInt("TimeDisplayValue", 60);
            editor.putString("SelectedDropdownTime", "Seconds");
            editor.putString("KEY_FIRST_RUN", "");
            editor.apply();
            Log.d("trace", "First run!");

            pic.setVisibility(View.VISIBLE);
        }
    }

    public void adapterRefresh(){
        //Load images from wallpaper folder and reload them to the ImageSwitcher
        Log.v("trace", "reloading ImageSwitcher");

        //Cancel the previous running task, if exist.
        myAsyncTaskLoadFiles.cancel(true);

        //new another ImageAdapter, to prevent the adapter have
        //mixed files
        myImageAdapter = new ImageAdapter(MyActivity.this);
        gridview.setAdapter(myImageAdapter);
        myAsyncTaskLoadFiles = new AsyncTaskLoadFiles(myImageAdapter);
        myAsyncTaskLoadFiles.execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        new MenuInflater(this).inflate(R.menu.my, menu);
        return (super.onCreateOptionsMenu(menu));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, Options.class);
            startActivity(intent);
            return true;
        }
        if (id == R.id.action_refresh) {
            adapterRefresh();
        }
        if (id == R.id.action_helpButton){
            pic.setVisibility(View.VISIBLE);
        }
        return super.onOptionsItemSelected(item);
    }

    public void directorySetup() {
        String path = Environment.getExternalStorageDirectory() + "/WallCycle";
        File dir = new File(path);
        if (!dir.exists()) {
            if( dir.mkdirs() ){
                Log.v("trace", "created folder");
                MediaScannerConnection.scanFile(this, new String[]{dir.toString()}, null,
                        new MediaScannerConnection.OnScanCompletedListener() {
                            public void onScanCompleted(String path, Uri uri) {
                                Log.v("trace", "Scanned " + path + ":");
                                Log.v("trace", "-> uri=" + uri);
                            }
                        });
            }
            else
                Log.v("trace", "folder couldn't be created");
        }else
            Log.v("trace", "folder " + dir.getAbsolutePath() + " exists");
    }


    //--------------------------------------------------------------------------------------------


    AsyncTaskLoadFiles myAsyncTaskLoadFiles;

    public class AsyncTaskLoadFiles extends AsyncTask<Void, String, Void> {

        File targetDirector;
        ImageAdapter myTaskAdapter;

        public AsyncTaskLoadFiles(ImageAdapter adapter) {
            myTaskAdapter = adapter;
        }

        @Override
        protected void onPreExecute() {
            String ExternalStorageDirectoryPath = Environment
                    .getExternalStorageDirectory().getAbsolutePath();

            String targetPath = ExternalStorageDirectoryPath + "/WallCycle/";
            targetDirector = new File(targetPath);
            myTaskAdapter.clear();

            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {

            File[] files = targetDirector.listFiles();
            for (File file : files) {
                publishProgress(file.getAbsolutePath());
                if (isCancelled()) break;
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            myTaskAdapter.add(values[0]);
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(Void result) {
            myTaskAdapter.notifyDataSetChanged();
            super.onPostExecute(result);
        }

    }

    public class ImageAdapter extends BaseAdapter {

        private Context mContext;
        ArrayList<String> itemList = new ArrayList<String>();

        public ImageAdapter(Context c) {
            mContext = c;
        }

        void add(String path) {
            itemList.add(path);
        }

        void clear() {
            itemList.clear();
        }

        @Override
        public int getCount() {
            return itemList.size();
        }

        @Override
        public Object getItem(int position) {
            return itemList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ImageView imageView;
            if (convertView == null) {
                // if it's not recycled, initialize some attributes
                imageView = new ImageView(mContext);
                imageView.setLayoutParams(new GridView.LayoutParams(screen_width / 3, screen_height / 3));
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setPadding(8, 8, 8, 8);
            } else {
                imageView = (ImageView) convertView;
            }

            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.v("trace", "clicked image");

                    Log.v("trace", itemList.get(position));
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.parse("file://" + itemList.get(position)), "image/*");
                    startActivity(intent);
                }
            });

            Picasso.with(this.mContext).load(new File(itemList.get(position))).resize(screen_width / 3, screen_height / 2).centerCrop().into(imageView);
            //Log.v("trace", " " + test.width);
            imageView.setBackgroundColor(Color.rgb(255, 255, 255));
            return imageView;
        }
    }
}