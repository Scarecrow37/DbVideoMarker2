package com.example.dbvideomarker.database.entitiy;

import java.util.ArrayList;

public class SearchGroupList {
    private String groupTitle;
    private String groupCount;
    private ArrayList<SearchItemList> searchItemLists;

    public SearchGroupList(String groupTitle, String groupCount, ArrayList<SearchItemList> searchItemLists) {
        this.groupTitle = groupTitle;
        this.groupCount = groupCount;
        this.searchItemLists = searchItemLists;
    }

    public void setGroupTitle(String groupTitle) {
        this.groupTitle = groupTitle;
    }

    public void setGroupCount(String groupCount) {
        this.groupCount = groupCount;
    }

    public void setSearchItemLists(ArrayList<SearchItemList> searchItemLists) {
        this.searchItemLists = searchItemLists;
    }

    public String getGroupTitle() {
        return this.groupTitle;
    }

    public String getGroupCount() {
        return this.groupCount;
    }

    public ArrayList<SearchItemList> getSearchItemLists() {
        return this.searchItemLists;
    }
}

