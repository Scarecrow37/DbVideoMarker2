package com.example.dbvideomarker.adapter;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.example.dbvideomarker.R;
import com.example.dbvideomarker.listener.OnItemClickListener;
import com.example.dbvideomarker.listener.OnItemSelectedListener;
import com.example.dbvideomarker.adapter.util.MyItemView;
import com.example.dbvideomarker.adapter.util.ViewCase;
import com.example.dbvideomarker.adapter.viewholder.VideoViewHolderNormal;
import com.example.dbvideomarker.adapter.viewholder.VideoViewHolderSelect;
import com.example.dbvideomarker.database.entitiy.Video;
import com.example.dbvideomarker.util.MediaStoreLoader;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class VideoAdapter extends RecyclerView.Adapter<MyItemView> {

    private List<Video> videoList; //cached copy of words
    private LayoutInflater mInflater;
    private ViewCase sel_type;
    private SparseBooleanArray mSelectedItems = new SparseBooleanArray(0);
    private SparseBooleanArray mSelectedItemIds = new SparseBooleanArray(0);
    private OnItemSelectedListener onItemSelectedListener;
    private OnItemClickListener onItemClickListener;
    private RequestManager mRequestManager;
    private MediaStoreLoader loader;

    public VideoAdapter(Context context, ViewCase sel_type, OnItemSelectedListener onItemSelectedListener, OnItemClickListener onItemClickListener) {
        mInflater = LayoutInflater.from(context);
        mRequestManager = Glide.with(context);
        this.sel_type = sel_type;
        this.onItemSelectedListener = onItemSelectedListener;
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public MyItemView onCreateViewHolder(ViewGroup parent, int viewType) {
        if (sel_type == ViewCase.NORMAL) {
            View view = mInflater.from(parent.getContext()).inflate(R.layout.item_video, parent, false);
            return new VideoViewHolderNormal(view);
        } else if (sel_type == ViewCase.SELECT) {
            View view = mInflater.from(parent.getContext()).inflate(R.layout.item_video_select, parent, false);
            return new VideoViewHolderSelect(view);
        }
        return null;
    }


    @Override
    public void onBindViewHolder(@NonNull final MyItemView holder, int position) {
        loader = new MediaStoreLoader();
        if (holder instanceof VideoViewHolderNormal) {
            VideoViewHolderNormal viewHolderNormal = (VideoViewHolderNormal) holder;
            if (videoList != null) {
                Video current = videoList.get(position);
                viewHolderNormal.vId.setText(String.valueOf(current.getContentId()));
                viewHolderNormal.vName.setText(String.valueOf(current.getVname()));
                viewHolderNormal.vDur.setText(loader.getReadableDuration(current.getVdur()));
                //viewHolderNormal.vThumb.setImage
                mRequestManager.asBitmap().load(Uri.fromFile(new File(current.getVpath()))).into(viewHolderNormal.vThumb);
                //viewHolderNormal.vThumb.setImageBitmap(loader.getThumbnail(current.vpath, current.getVdur()/3));
                //TODO: 특정 시간을 추출하는게 가능한가
                viewHolderNormal.view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        onItemClickListener.clickItem(current.getContentId(), current.getVpath());
                    }
                });
                viewHolderNormal.view.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        onItemClickListener.clickLongItem(v, current.getContentId(), current.getVpath());
                        return false;
                    }
                });
            } else {
                viewHolderNormal.vName.setText("No Data");
            }
        } else if (holder instanceof VideoViewHolderSelect) {
            //선택모드

            VideoViewHolderSelect viewHolderSelect = (VideoViewHolderSelect) holder;
            if (videoList != null) {
                Video current = videoList.get(position);
                viewHolderSelect.selectedId.setText(String.valueOf(current.getContentId()));
                viewHolderSelect.selectedName.setText(String.valueOf(current.getVname()));
                viewHolderSelect.selectedDur.setText(String.valueOf(loader.getReadableDuration(current.getVdur())));
                mRequestManager.asBitmap().load(Uri.fromFile(new File(current.getVpath()))).into(viewHolderSelect.selectedThumb);

                if (mSelectedItems.get(position, false)) {
                    viewHolderSelect.view.setBackgroundColor(Color.GRAY);
                } else {
                    viewHolderSelect.view.setBackgroundColor(Color.WHITE);
                }
                viewHolderSelect.view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
//                        toggleItemSelected(position);
                        if (mSelectedItems.get(position, false) == true) {
                            //GRAY
                            mSelectedItems.delete(position);
                            mSelectedItemIds.delete(current.getContentId());
                            notifyItemChanged(position);
                        } else {
                            //WHITE
                            mSelectedItems.put(position, true);
                            mSelectedItemIds.put(current.getContentId(), true);
                            notifyItemChanged(position);
                        }
                        Log.d("test", "parsed"+ mSelectedItemIds.size());

                        onItemSelectedListener.onItemSelected(view, mSelectedItemIds);
                    }
                });
            }
        }
    }

    public void setVideos(List<Video> videos) {
        videoList = videos;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if (videoList != null) {
            return videoList.size();
        } else return 0;
    }
}
