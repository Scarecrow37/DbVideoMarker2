package com.example.dbvideomarker.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ExpandableListView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.dbvideomarker.R;
import com.example.dbvideomarker.adapter.SearchAdapter;
import com.example.dbvideomarker.database.entitiy.SearchGroupList;
import com.example.dbvideomarker.database.entitiy.SearchItemList;

import java.util.ArrayList;

public class SearchActivity extends AppCompatActivity {

    private ExpandableListView expandableListView;
    private EditText editText;
    private SearchAdapter searchAdapter;
    private ArrayList<SearchGroupList> searchGroupLists;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_search);

        editText = (EditText) findViewById(R.id.editText);

        expandableListView = (ExpandableListView) findViewById(R.id.expandableListView);

        searchGroupLists = new ArrayList<>();
        searchGroupLists = Data(searchGroupLists);
        searchAdapter = new SearchAdapter(this, searchGroupLists);
        expandableListView.setAdapter(searchAdapter);

        expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView expandableListView, View view, int i, int i1, long l) {
                return false;
            }
        });
        expandableListView.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {
            @Override
            public void onGroupCollapse(int i) {

            }
        });
        expandableListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            @Override
            public void onGroupExpand(int i) {

            }
        });
    }


    private ArrayList<SearchGroupList> Data(ArrayList<SearchGroupList> list) {
        SearchGroupList videoList = new SearchGroupList("영상 제목", "(1)",null);
        ArrayList<SearchItemList> videoLists = new ArrayList<>();
        videoLists.add(new SearchItemList(R.drawable.ic_baseline_search_24,"30:28","은밀한 사생활","GJW-903"));
        videoList.setSearchItemLists(videoLists);
        list.add(videoList);

        SearchGroupList markList = new SearchGroupList("북마크 메모","(1)",null);
        ArrayList<SearchItemList> markLists = new ArrayList<>();
        markLists.add(new SearchItemList(R.drawable.ic_baseline_search_24,"30:28","은밀한 사생활","GJW-903"));
        markList.setSearchItemLists(markLists);
        list.add(markList);

        return list;
    }


    /*
     */

}