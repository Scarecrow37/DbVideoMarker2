package com.example.dbvideomarker.database.entitiy;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.google.android.exoplayer2.video.spherical.Projection;

@Entity(tableName = "video")
public class Video {

    @PrimaryKey
    @ColumnInfo(name = "contentId")
    public int contentId;

    @ColumnInfo(name = "vname")
    public String vname;

    @ColumnInfo(name = "vdur")
    public long vdur;

    @ColumnInfo(name = "vpath")
    public String vpath;

    @ColumnInfo(name = "vadded")
    public String vadded;

  
    public int getContentId() {
        return contentId;
    }

    public void setContentId(int contentId) {
        this.contentId = contentId;
    }

    public String getVname() {
        return vname;
    }

    public void setVname(String vname) {
        this.vname = vname;
    }

    public long getVdur() {
        return vdur;
    }

    public void setVdur(long vdur) {
        this.vdur = vdur;
    }

    public String getVpath() {
        return vpath;
    }

    public void setVpath(String vpath) {
        this.vpath = vpath;
    }

    public String getVadded() {
        return vadded;
    }

    public void setVadded(String vadded) {
        this.vadded = vadded;
    }
}

