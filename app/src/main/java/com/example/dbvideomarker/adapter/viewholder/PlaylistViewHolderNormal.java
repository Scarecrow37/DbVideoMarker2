package com.example.dbvideomarker.adapter.viewholder;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dbvideomarker.R;
import com.example.dbvideomarker.adapter.util.MyItemView;
import com.example.dbvideomarker.database.entitiy.Media;

public class PlaylistViewHolderNormal extends MyItemView {
        public View view;
        public TextView pId;
        public TextView pName;

        public PlaylistViewHolderNormal(View view) {
            super(view);
            this.view = view;
            pId = view.findViewById(R.id.pId);
            pName = view.findViewById(R.id.pName);
        }

}
