package com.example.dbvideomarker.util;

import android.util.Log;

import java.io.File;

public class FileUtil {

    public void fileDelete(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            file.delete();
            Log.d("FileUtil", "File " + file.getName() + " deleted");
        } else {
            Log.d("FileUtil", "No File " + file.getName());
        }
    }


    public void fileRename(String filePath, String befFile, String aftFile) {
        File beforeFileName;
        File afterFileName;

        beforeFileName = new File(filePath, befFile);
        afterFileName = new File(filePath, aftFile);

        beforeFileName.renameTo(afterFileName);
    }

}


