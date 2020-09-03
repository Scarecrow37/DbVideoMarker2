package com.example.dbvideomarker.database.entitiy;

import android.widget.ImageView;

public class SearchItemList {
    private int thumb;
    private String vDur;
    private String vName;
    private String vId;

    public SearchItemList(int thumb, String vDur, String vName, String vId) {
        this.thumb = thumb;
        this.vDur = vDur;
        this.vName = vName;
        this.vId = vId;
    }

    public void setThumb(int thumb) {
        this.thumb = thumb;
    }

    public void setvDur(String vDur) {
        this.vDur = vDur;
    }

    public void setvName(String vName) {
        this.vName = vName;
    }

    public void setvId(String vId) {
        this.vId = vId;
    }

    public int getThumb() {
        return this.thumb;
    }

    public String getvDur() {
        return this.vDur;
    }

    public String getvName() {
        return this.vName;
    }

    public String getvId() {
        return this.vId;
    }

}
