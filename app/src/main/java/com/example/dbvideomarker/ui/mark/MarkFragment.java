package com.example.dbvideomarker.ui.mark;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.example.dbvideomarker.R;
import com.example.dbvideomarker.adapter.MarkAdapter;
import com.example.dbvideomarker.adapter.util.ViewCase;
import com.example.dbvideomarker.listener.OnItemClickListener;
import com.example.dbvideomarker.listener.OnItemSelectedListener;
import com.example.dbvideomarker.database.entitiy.Mark;
import com.example.dbvideomarker.player.PlayerActivity;

import java.util.List;

public class MarkFragment extends Fragment implements OnItemClickListener, OnItemSelectedListener {

    private MarkViewModel markViewModel;
    private RequestManager mGlideRequestManager;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View rootv = inflater.inflate(R.layout.fragment_mark, container, false);

        Context context = rootv.getContext();
        mGlideRequestManager = Glide.with(context);

        RecyclerView recyclerView = rootv.findViewById(R.id.rv_Mark);
        MarkAdapter adapter = new MarkAdapter(context, ViewCase.NORMAL, this, this, mGlideRequestManager);

        markViewModel = new ViewModelProvider(getActivity()).get(MarkViewModel.class);
        markViewModel.getAllMark().observe(getActivity(), new Observer<List<Mark>>() {
            @Override
            public void onChanged(List<Mark> marks) {
                adapter.setMarks(marks);
            }
        });

        recyclerView.setLayoutManager(new GridLayoutManager(context, 2));
        recyclerView.setAdapter(adapter);

        Button buttonSearch = rootv.findViewById(R.id.btn_Search);
        buttonSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent intentSearch = new Intent(getContext(), SearchActivity.class);
                //getContext().startActivity(intentSearch);
            }
        });

        return rootv;
    }

    @Override
    public void clickLongItem(View v, int id) {}

    @Override
    public void clickItem(int id) {}


    @Override
    public void clickMark(int id, long start) {
        Intent playerIntent = new Intent(getContext(), PlayerActivity.class);
        playerIntent.putExtra("ContentID", id);
        playerIntent.putExtra("Start", start);
        getContext().startActivity(playerIntent);
    }

    @Override
    public void clickLongMark(View v, int id) {
        PopupMenu popupMenu = new PopupMenu(getContext(), v);
        MenuInflater inflater = popupMenu.getMenuInflater();
        Menu menu = popupMenu.getMenu();
        inflater.inflate(R.menu.menu_popup_mark, menu);

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.popup_edit:
                        /*AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        EditText et = new EditText(getActivity());
                        builder.setView(et);
                        builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (et.getText().toString().trim().length() != 0) {
                                    Mark mark = new Mark();
                                    mark.setmMemo(et.getText().toString());
                                    mark.set
                                    mark.setmid(id);

                                    DashboardViewModel.update(playList);
                                }
                            }
                        });
                        AlertDialog dialog = builder.create();
                        dialog.show();*/
                        //TODO: update구문 특정 column만 변경할 수 있도록 수정필요
                        break;
                    case(R.id.popup_delete):
                        markViewModel.deleteMark(id);
                        break;
                }
                return false;
            }
        });
        popupMenu.show();
    }

    @Override
    public void onItemSelected(View v, SparseBooleanArray sparseBooleanArray) {}
}