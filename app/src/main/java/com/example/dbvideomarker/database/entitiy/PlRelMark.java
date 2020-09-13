package com.example.dbvideomarker.database.entitiy;

import androidx.room.ColumnInfo;

public class PlRelMark {

    @ColumnInfo(name = "playlist_id")
    public int pm_pid;

    @ColumnInfo(name = "mark_id")
    public int pm_mid;

    @ColumnInfo(name = "mark_vid")
    public int pm_vid;

    @ColumnInfo(name = "mark_mmemo")
    public String pm_mmemo;

    @ColumnInfo(name = "mark_mstart")
    public Long pm_mstart;

    @ColumnInfo(name = "mark_mpath")
    public String pm_mpath;

    public int getPm_pid() {
        return pm_pid;
    }

    public void setPm_pid(int pm_pid) {
        this.pm_pid = pm_pid;
    }

    public int getPm_mid() {
        return pm_mid;
    }

    public void setPm_mid(int pm_mid) {
        this.pm_mid = pm_mid;
    }

    public int getPm_vid() {
        return pm_vid;
    }

    public void setPm_vid(int pm_vid) {
        this.pm_vid = pm_vid;
    }

    public String getPm_mmemo() {
        return pm_mmemo;
    }

    public void setPm_mmemo(String pm_mmemo) {
        this.pm_mmemo = pm_mmemo;
    }

    public Long getPm_mstart() {
        return pm_mstart;
    }

    public void setPm_mstart(Long pm_mstart) {
        this.pm_mstart = pm_mstart;
    }

    public String getPm_mpath() {
        return pm_mpath;
    }

    public void setPm_mpath(String pm_mpath) {
        this.pm_mpath = pm_mpath;
    }
}
