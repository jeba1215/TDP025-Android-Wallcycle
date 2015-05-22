package com.example.jesper.WallCycle;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class localImageHandler {
    private List<String> imagePaths = new ArrayList<String>();
    private String path = Environment.getExternalStorageDirectory().getAbsolutePath()+"/WallCycle/";
    private int index = 0;

    public localImageHandler(){
        Log.v("trace", "in localImageHandler");
        reload();
    }

    public String get(int i){
        return imagePaths.get(i);
    }

    public void reload(){
        for(File f : new File(path).listFiles()){
            Log.v("trace", "local image: "+f.getName());
            imagePaths.add(f.getAbsolutePath());
        }
    }

    public int getNextIndex(){
        ++index;
        reload();
        if( imagePaths.size() > 0)
            return index = index%imagePaths.size();
        else
            return -1;
    }

    public boolean dirStatus(){
        return imagePaths.size() > 0;
    }
}
