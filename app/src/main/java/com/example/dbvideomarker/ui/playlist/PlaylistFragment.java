package com.example.dbvideomarker.ui.playlist;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dbvideomarker.R;
import com.example.dbvideomarker.activity.PlayListEditActivity;
import com.example.dbvideomarker.activity.SearchActivity;
import com.example.dbvideomarker.adapter.PlayListAdapter;
import com.example.dbvideomarker.listener.OnItemClickListener;
import com.example.dbvideomarker.database.entitiy.PlayList;

import java.util.List;

public class PlaylistFragment extends Fragment implements OnItemClickListener {

    private PlaylistViewModel playlistViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rv = inflater.inflate(R.layout.fragment_playlist, container, false);
        Context context = rv.getContext();

        RecyclerView recyclerView = rv.findViewById(R.id.rv_Playlist);
        DividerItemDecoration dividerItemDecoration =
                new DividerItemDecoration(recyclerView.getContext(),new LinearLayoutManager(getContext()).getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);

        PlayListAdapter adapter = new PlayListAdapter(context, this);


        playlistViewModel = new ViewModelProvider(getActivity()).get(PlaylistViewModel.class);
        playlistViewModel.findAllPlayList().observe(getViewLifecycleOwner(), new Observer<List<PlayList>>() {
            @Override
            public void onChanged(List<PlayList> playList) {
                adapter.setPlayLists(playList);
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(adapter);

        rv.findViewById(R.id.btn_addPlayList).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText et = rv.findViewById(R.id.et_PlayListName);
                if (et.getText().toString().trim().length() != 0) {
                    String name = et.getText().toString().trim();
                    PlayList playList = new PlayList();
                    playList.setpName(name);

                    playlistViewModel.insertPlayList(playList);
                }

            }
        });
        return rv;
    }

    @Override
    public void clickLongItem(View v, int id) {
        PopupMenu popupMenu = new PopupMenu(getContext(),v);
        MenuInflater inflater = popupMenu.getMenuInflater();
        Menu menu = popupMenu.getMenu();
        inflater.inflate(R.menu.menu_popup_video, menu);

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.popup_edit:
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        EditText et = new EditText(getActivity());
                        builder.setView(et);
                        builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (et.getText().toString().trim().length() != 0) {
                                    PlayList playList = new PlayList();
                                    playList.setpName(et.getText().toString());
                                    playList.setPid(id);
                                    playlistViewModel.update(playList);
                                }
                            }
                        });
                        AlertDialog dialog = builder.create();
                        dialog.show();
                        break;
                    case(R.id.popup_delete):
                        playlistViewModel.deleteWithPlayList(id);
                        playlistViewModel.deletePlayList(id);
                        break;
                }
                return false;
            }
        });
        popupMenu.show();
    }

    @Override
    public void clickItem(int pid) {
        Intent intent = new Intent(getContext(), PlayListEditActivity.class);
        intent.putExtra("재생목록 번호", pid);
        getContext().startActivity(intent);
    }

    @Override
    public void clickMark(int id, long start) {}

    @Override
    public void clickLongMark(View v, int id) {}
}