package com.example.dbvideomarker.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.dbvideomarker.R;
import com.example.dbvideomarker.database.entitiy.SearchGroupList;
import com.example.dbvideomarker.database.entitiy.SearchItemList;

import java.util.ArrayList;

public class SearchAdapter extends BaseExpandableListAdapter {

    private Context context;
    private ArrayList<SearchGroupList> searchGroupLists;
    private ArrayList<SearchGroupList> searchGroupListArrayList;

    public SearchAdapter (Context context, ArrayList<SearchGroupList> arrayList) {
        this.context = context;
        this.searchGroupLists = new ArrayList<SearchGroupList>();
        this.searchGroupLists.addAll(arrayList);

        this.searchGroupListArrayList = new ArrayList<SearchGroupList>();
        this.searchGroupListArrayList.addAll(arrayList);
    }


    @Override
    public int getGroupCount() {
        return searchGroupListArrayList.size();
    }

    @Override
    public int getChildrenCount(int i) {
        return searchGroupListArrayList.get(i).getSearchItemLists().size();
    }

    @Override
    public Object getGroup(int i) {
        return searchGroupListArrayList.get(i);
    }

    @Override
    public Object getChild(int i, int i1) {
        return searchGroupListArrayList.get(i).getSearchItemLists().get(i1);
    }

    @Override
    public long getGroupId(int i) {
        return i;
    }

    @Override
    public long getChildId(int i, int i1) {
        return i1;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(int i, boolean b, View view, ViewGroup viewGroup) {
        SearchGroupList searchGroupList = searchGroupListArrayList.get(i);
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.search_group_view,viewGroup,false);
        }
        TextView groupTitle = (TextView) view.findViewById(R.id.category);
        TextView groupCount = (TextView) view.findViewById(R.id.count);
        groupTitle.setText(searchGroupList.getGroupTitle());
        groupCount.setText(searchGroupList.getGroupCount());
        return view;
    }

    @Override
    public View getChildView(int i, int i1, boolean b, View view, ViewGroup viewGroup) {
        SearchItemList searchItemList = searchGroupListArrayList.get(i).getSearchItemLists().get(i1);
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.home_item,viewGroup,false);
        }
        ImageView thumb = (ImageView) view.findViewById(R.id.thumb);
        TextView vDur = (TextView) view.findViewById(R.id.vDur);
        TextView vName = (TextView) view.findViewById(R.id.vName);
        TextView vId = (TextView) view.findViewById(R.id.vId);

        thumb.setImageResource(searchItemList.getThumb());
        vDur.setText(searchItemList.getvDur());
        vName.setText(searchItemList.getvName());
        vId.setText(searchItemList.getvId());
        return view;
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return true;
    }
}
