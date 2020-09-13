package com.example.dbvideomarker.adapter.viewholder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.dbvideomarker.R;
import com.example.dbvideomarker.adapter.util.MyItemView;


public class MarkViewHolderNormal extends MyItemView {
    public View view;
    public ImageView mthumb;
    public TextView mid;
    public TextView mMemo;
    public TextView mStart;

    public MarkViewHolderNormal(@NonNull View view) {
        super(view);
        this.view = view;
        mthumb = view.findViewById(R.id.mthumb);
        mid = view.findViewById(R.id.mid);
        mMemo = view.findViewById(R.id.mMemo);
        mStart = view.findViewById(R.id.mstart);
    }
}
