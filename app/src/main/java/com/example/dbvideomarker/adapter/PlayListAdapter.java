package com.example.dbvideomarker.adapter;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.dbvideomarker.R;
import com.example.dbvideomarker.adapter.util.MyItemView;
import com.example.dbvideomarker.adapter.util.ViewCase;
import com.example.dbvideomarker.adapter.viewholder.PlaylistViewHolderNormal;
import com.example.dbvideomarker.adapter.viewholder.PlaylistViewHolderSelect;
import com.example.dbvideomarker.adapter.viewholder.VideoViewHolderNormal;
import com.example.dbvideomarker.adapter.viewholder.VideoViewHolderSelect;
import com.example.dbvideomarker.listener.OnItemClickListener;
import com.example.dbvideomarker.database.entitiy.PlayList;
import com.example.dbvideomarker.listener.OnItemSelectedListener;

import java.util.List;

public class PlayListAdapter extends RecyclerView.Adapter<MyItemView> {

    private SparseBooleanArray mSelectedItems = new SparseBooleanArray(0);
    private SparseBooleanArray mSelectedItemIds = new SparseBooleanArray(0);
    private OnItemSelectedListener onItemSelectedListener;
    private OnItemClickListener onItemClickListener;
    private List<PlayList> playListList;
    private LayoutInflater mInflater;
    private ViewCase sel_type;

    public PlayListAdapter(Context context, ViewCase sel_type, OnItemClickListener onItemClickListener, OnItemSelectedListener onItemSelectedListener) {
        mInflater = LayoutInflater.from(context);
        this.sel_type = sel_type;
        this.onItemSelectedListener = onItemSelectedListener;
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public MyItemView onCreateViewHolder(ViewGroup parent, int viewType) {
        if (sel_type == ViewCase.NORMAL) {
            View view = mInflater.from(parent.getContext()).inflate(R.layout.item_playlist, parent, false);
            return new PlaylistViewHolderNormal(view);
        } else if (sel_type == ViewCase.SELECT) {
            View view = mInflater.from(parent.getContext()).inflate(R.layout.item_playlist, parent, false);
            return new PlaylistViewHolderSelect(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(final MyItemView holder, int position) {
        if(holder instanceof PlaylistViewHolderNormal) {
            PlaylistViewHolderNormal playlistViewHolderNormal = (PlaylistViewHolderNormal) holder;
            if (playListList != null) {
                PlayList current = playListList.get(position);
                playlistViewHolderNormal.pId.setText(String.valueOf(current.getPid()));
                playlistViewHolderNormal.pName.setText(current.getpName());

                playlistViewHolderNormal.view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int pid = current.getPid();
                        onItemClickListener.clickItem(pid, "");
                    }
                });
                playlistViewHolderNormal.view.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        int pid = current.getPid();
                        onItemClickListener.clickLongItem(view, pid, "");
                        return false;
                    }
                });


            }
        } else if(holder instanceof PlaylistViewHolderSelect) {
            PlaylistViewHolderSelect playlistViewHolderSelect = (PlaylistViewHolderSelect) holder;
            if (playListList != null) {
                PlayList current = playListList.get(position);
                playlistViewHolderSelect._pId.setText(String.valueOf(current.getPid()));
                playlistViewHolderSelect._pName.setText(current.getpName());

                if(mSelectedItems.get(position, false)) {
                    playlistViewHolderSelect._view.setBackgroundColor(Color.GRAY);
                } else {
                    playlistViewHolderSelect._view.setBackgroundColor(Color.WHITE);
                }

                playlistViewHolderSelect._view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (mSelectedItems.get(position, false) == true) {
                            //GRAY
                            mSelectedItems.delete(position);
                            mSelectedItemIds.delete(current.getPid());
                            notifyItemChanged(position);
                        } else {
                            //WHITE
                            mSelectedItems.put(position, true);
                            mSelectedItemIds.put(current.getPid(), true);
                            notifyItemChanged(position);
                        }
                        Log.d("test", "parsed"+ mSelectedItemIds.size());

                        onItemSelectedListener.onItemSelected(view, mSelectedItemIds);
                    }
                });


            }
        }

    }

    public void setPlayLists(List<PlayList> playList) {
        playListList = playList;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if (playListList != null)
            return playListList.size();
        else return 0;
    }
}
