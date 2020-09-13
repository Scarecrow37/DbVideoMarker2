package com.example.dbvideomarker.listener;

import android.view.View;

public interface OnMarkClickListener {
    void clickMark(int id, long start, String path);

    void clickLongMark(View v, int id, String path);
}
