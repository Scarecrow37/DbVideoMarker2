package com.example.dbvideomarker.database.entitiy;

import androidx.lifecycle.ViewModel;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(tableName = "mark", foreignKeys = {
        @ForeignKey(entity = Video.class, parentColumns = "contentId", childColumns = "vid")
})
public class Mark {

    @PrimaryKey(autoGenerate = true)
    public int mid;

    @ColumnInfo(name = "vid")
    public int vid;

    @ColumnInfo(name = "mMemo")
    public String mMemo;

    @ColumnInfo(name = "mStart")
    public Long mStart;

    @ColumnInfo(name = "mAdded")
    public Long mAdded;

    @ColumnInfo(name = "mPath")
    public String mpath;

    public Long getmAdded() {
        return mAdded;
    }

    public void setmAdded(Long mAdded) {
        this.mAdded = mAdded;
    }

    public int getmid() { return mid; }

    public void setmid(int mid) { this.mid = mid; }

    public int getvid() { return vid; }

    public void setvid(int vid) { this.vid = vid; }

    public String getmMemo() { return mMemo; }

    public void setmMemo(String mMemo) { this.mMemo = mMemo; }

    public Long getmStart() {
        return mStart;
    }

    public void setmStart(Long mStart) {
        this.mStart = mStart;
    }

    public String getMpath() {
        return mpath;
    }

    public void setMpath(String mpath) {
        this.mpath = mpath;
    }
}
