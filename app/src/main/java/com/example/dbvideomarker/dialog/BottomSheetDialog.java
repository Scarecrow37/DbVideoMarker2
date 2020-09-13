package com.example.dbvideomarker.dialog;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dbvideomarker.R;
import com.example.dbvideomarker.adapter.PlayListAdapter;
import com.example.dbvideomarker.database.entitiy.PlRel;
import com.example.dbvideomarker.listener.OnItemClickListener;
import com.example.dbvideomarker.database.entitiy.PlayList;
import com.example.dbvideomarker.ui.home.HomeFragment;
import com.example.dbvideomarker.ui.playlist.PlaylistViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationItemView;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

public class BottomSheetDialog extends BottomSheetDialogFragment implements OnItemClickListener {

    private PlaylistViewModel playlistViewModel;
    private int pid;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_bottom_sheet, container, false);
//
//        Context context = v.getContext();
//
//        RecyclerView recyclerView = v.findViewById(R.id.rv_playlist_bottom);
//        DividerItemDecoration dividerItemDecoration =
//                new DividerItemDecoration(recyclerView.getContext(),new LinearLayoutManager(getContext()).getOrientation());
//        recyclerView.addItemDecoration(dividerItemDecoration);
//
//        PlayListAdapter adapter = new PlayListAdapter(context, this);
//
//        // Get a new or existing ViewModel from the ViewModelProvider.
//        playlistViewModel = new ViewModelProvider(getActivity()).get(PlaylistViewModel.class);
//
//        // Add an observer on the LiveData returned by getAlphabetizedWords.
//        // The onChanged() method fires when the observed data changes and the activity is
//        // in the foreground.
//        playlistViewModel.findAllPlayList().observe(getViewLifecycleOwner(), new Observer<List<PlayList>>() {
//            @Override
//            public void onChanged(List<PlayList> playList) {
//                //Update the cached copy of the words in the adapter.
//                adapter.setPlayLists(playList);
//            }
//        });
//
//        recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
//        recyclerView.setAdapter(adapter);
//
//
        return v;
    }

    @Override
    public void clickItem(int id, String path) {

    }

    @Override
    public void clickLongItem(View v, int id, String path) {

    }
}
