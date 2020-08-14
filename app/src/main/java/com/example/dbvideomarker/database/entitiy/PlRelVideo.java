package com.example.dbvideomarker.database.entitiy;

import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;

import com.google.android.material.circularreveal.CircularRevealHelper;

public class PlRelVideo{

    @ColumnInfo(name="playlist_id")
    private int pv_pid;

    @ColumnInfo(name="video_id")
    private int pv_vid;

    @ColumnInfo(name="video_name")
    private String pv_vname;

    public int getPv_pid() {
        return pv_pid;
    }

    public void setPv_pid(int pv_pid) {
        this.pv_pid = pv_pid;
    }

    public int getPv_vid() {
        return pv_vid;
    }

    public void setPv_vid(int pv_vid) {
        this.pv_vid = pv_vid;
    }

    public String getPv_vname() {
        return pv_vname;
    }

    public void setPv_vname(String pv_vname) {
        this.pv_vname = pv_vname;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if(obj instanceof PlRelVideo) {
            PlRelVideo plRelVideo = (PlRelVideo) obj;
            return (pv_vid == plRelVideo.pv_vid);
        }
        return false;
    }


}


