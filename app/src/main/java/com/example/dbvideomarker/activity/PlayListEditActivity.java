package com.example.dbvideomarker.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dbvideomarker.R;
import com.example.dbvideomarker.adapter.PlayListEditAdapter;
import com.example.dbvideomarker.adapter.listener.OnItemClickListener;
import com.example.dbvideomarker.adapter.util.Callback;
import com.example.dbvideomarker.adapter.util.ViewCase;
import com.example.dbvideomarker.database.dao.PlayListDao;
import com.example.dbvideomarker.database.entitiy.PlRel;
import com.example.dbvideomarker.database.entitiy.PlRelVideo;
import com.example.dbvideomarker.database.entitiy.PlayList;
import com.example.dbvideomarker.ui.notifications.NotificationsViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class PlayListEditActivity extends AppCompatActivity implements OnItemClickListener, PlayListEditAdapter.OnStartDragListener {

    private PlayListEditViewModel playListEditViewModel;
    TextView PlayListName, PlayListId;
    public int SELECT_REQUEST_CODE = 1001;
    private List<PlRelVideo> resultList = new ArrayList<>();
    private int pid;
    ItemTouchHelper itemTouchHelper;
    PlayListEditAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actvity_playlistedit);

        Intent intent = getIntent();

        pid = intent.getIntExtra("재생목록 번호", -1);
//        int pid = Integer.parseInt(pidToString);
//        String pname = intent.getStringExtra("재생목록 이름");

        TextView playListName = (TextView) findViewById(R.id.playListName);
        TextView playListId = (TextView) findViewById(R.id.playListId);
        TextView playListCount = (TextView) findViewById(R.id.playListCount);

        // Get a new or existing ViewModel from the ViewModelProvider.
        playListEditViewModel = new ViewModelProvider(this).get(PlayListEditViewModel.class);

        playListEditViewModel.getPlayList(pid).observe(this, new Observer<PlayList>() {
            @Override
            public void onChanged(PlayList playList) {
                String pname = playList.getpName();
                playListName.setText(pname);
            }
        });


        playListId.setText(""+pid); //setText 에서 int형 파라미터는 리소스 id 값이지 그냥 int값이 아님. String 형태로 바꿔서 출력해야함 + setText는 charsequance 자료형임

        RecyclerView recyclerView = findViewById(R.id.rv_PlaylistEdit);
        adapter = new PlayListEditAdapter(this, this, this);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),new LinearLayoutManager(this).getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);
        Callback callback = new Callback(adapter);
        itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(recyclerView);






        // Add an observer on the LiveData returned by getAlphabetizedWords.
        // The onChanged() method fires when the observed data changes and the activity is
        // in the foreground.
        playListEditViewModel.findVideoInPlayList(pid).observe(this, new Observer<List<PlRelVideo>>() {
            @Override
            public void onChanged(List<PlRelVideo> plRels) {
                //Update the cached copy of the words in the adapter.
                resultList = getStringArrayList(""+pid);

                if(resultList == null) {
                    setStringArrayList(""+pid, plRels);
                    adapter.setPlRels(plRels);
                } else {
                    if(resultList.size() < plRels.size()) {
                        for(int i = 0; i < plRels.size(); i++) {
                            if (!resultList.contains(plRels.get(i))) {
                                resultList.add(plRels.get(i));
                                setStringArrayList(""+pid, resultList);
                                adapter.setPlRels(resultList);
                            }
                        }
                    } else if(resultList.size() > plRels.size()){
                        for(int i = 0; i < resultList.size(); i++) {
                            if (!plRels.contains(resultList.get(i))) {
                                resultList.remove(i);
                                setStringArrayList(""+pid, resultList);
                                adapter.setPlRels(resultList);
                            }
                        }
                    } else {
                        setStringArrayList(""+pid, resultList);
                        adapter.setPlRels(resultList);
                    }
                }

            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(adapter);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent1 = new Intent(PlayListEditActivity.this, SelectActivity.class);
                intent1.putExtra("추가할 재생목록 번호", pid);
                startActivityForResult(intent1, SELECT_REQUEST_CODE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == SELECT_REQUEST_CODE && resultCode == RESULT_OK) {
            ArrayList<Integer> selectedVidList = data.getIntegerArrayListExtra("vidlist");

            /*
            //선택했던 데이터를 제거해도 데이터를 전달하는 ArrayList에서는 제거되지 않으므로, ArrayList의 중복을 제거하는 Logic
            //중복을 제거하는 코드를 Adapter 에 추가하였으므로 주석처리
            resultList = new ArrayList<Integer>();
            for(int i = 0; i < selectedVidList.size(); i++) {
                if(!resultList.contains(selectedVidList.get(i))) {
                    resultList.add(selectedVidList.get(i));
                }
            }*/

            for(int i=0; i<selectedVidList.size(); i++) {
                PlRel plRel = new PlRel();
                plRel.setPid(pid);
                plRel.setVid(selectedVidList.get(i));
                playListEditViewModel.insertPlRelation(plRel);
            }
        }
    }

    @Override
    public void clickLongItem(View v, int id) {
        PopupMenu popupMenu = new PopupMenu(this, v);
        MenuInflater inflater = popupMenu.getMenuInflater();
        Menu menu = popupMenu.getMenu();
        inflater.inflate(R.menu.menu_popup, menu);

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case(R.id.popup_delete):
                        playListEditViewModel.deletePlRel(id);
                        break;
                }
                return false;
            }
        });
        popupMenu.show();

    }

    @Override
    public void clickItem(int pid) {

    }

    @Override
    public void onStartDrag(PlayListEditAdapter.PLEViewHolder holder) {
        itemTouchHelper.startDrag(holder);
    }

    @Override
    protected void onPause() {
        super.onPause();
        setStringArrayList(""+pid, adapter.plRelList);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        setStringArrayList(""+pid, adapter.plRelList);
    }

    public void setStringArrayList(String key, List<PlRelVideo> valueList) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = pref.edit();
        //Gson build gradle에 추가해야함
        Gson gson = new Gson();
        String json = gson.toJson(valueList);
        editor.putString(key, json);
        editor.commit();
    }

    public List<PlRelVideo> getStringArrayList(String key) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        Gson gson = new Gson();
        String json = pref.getString(key, null);
        Type type = new TypeToken<List<PlRelVideo>>() {
        }.getType();
        List<PlRelVideo> items = gson.fromJson(json, type);
        return items;
    }
}
