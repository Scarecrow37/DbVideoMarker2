package com.example.dbvideomarker.player;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.bumptech.glide.RequestManager;
import com.example.dbvideomarker.R;
import com.example.dbvideomarker.adapter.MarkAdapter;
import com.example.dbvideomarker.adapter.util.ViewCase;
import com.example.dbvideomarker.database.entitiy.Mark;
import com.example.dbvideomarker.dialog.BottomSheetDialog;
import com.example.dbvideomarker.listener.OnItemClickListener;
import com.example.dbvideomarker.listener.OnItemSelectedListener;
import com.example.dbvideomarker.listener.OnMarkClickListener;
import com.example.dbvideomarker.player.media.SimpleMediaSource;
import com.example.dbvideomarker.player.ui.ExoVideoPlaybackControlView;
import com.example.dbvideomarker.player.ui.ExoVideoView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;


import java.util.List;

import static com.example.dbvideomarker.player.orientation.OnOrientationChangedListener.SENSOR_LANDSCAPE;
import static com.example.dbvideomarker.player.orientation.OnOrientationChangedListener.SENSOR_PORTRAIT;

public class PlayerActivity extends AppCompatActivity implements OnItemClickListener, OnMarkClickListener, OnItemSelectedListener {


    private ExoVideoView videoView;
    private View wrapper;

    private PlayerViewModel playerViewModel;
    private RequestManager mGlideRequestManager;

    public static int CONTENT_ID;
    private long CONTENT_START;
    private String CONTENT_PATH;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_video_view);
        wrapper = findViewById(R.id.wrapper);

        FloatingActionButton fab = findViewById(R.id.fab_add_mark);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addMark(videoView.getCurrentPosition());
            }
        });

        Intent intent = getIntent();
        CONTENT_ID = intent.getExtras().getInt("ContentID");
        CONTENT_PATH = intent.getExtras().getString("Path");
        CONTENT_START = intent.getExtras().getLong("Start");

        videoView.seekTo(CONTENT_START);


        initVideoView(String.valueOf(CONTENT_ID));
        Log.d("TAG", "start =" + CONTENT_START + "//" + CONTENT_ID);
        initBottomView();
    }


    private void initBottomView() {
        RecyclerView recyclerView = findViewById(R.id.rv_getMark);
        MarkAdapter markAdapter = new MarkAdapter(this, ViewCase.NORMAL, this, this, mGlideRequestManager);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setAdapter(markAdapter);

        playerViewModel = new ViewModelProvider(this).get(PlayerViewModel.class);
        playerViewModel.getMarkByVideoId(CONTENT_ID).observe(this, new Observer<List<Mark>>() {
            @Override
            public void onChanged(List<Mark> marks) {
                markAdapter.setMarks(marks);
            }
        });

        Button button = findViewById(R.id.bottom_sheet);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BottomSheetDialog playerBottomSheetDialog = new BottomSheetDialog();
                playerBottomSheetDialog.show(getSupportFragmentManager(), "bottomSheetDialog");
            }
        });
    }

    public void addMark(long position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        EditText mMemo = new EditText(this);
        builder.setView(mMemo);
        builder.setTitle("북마크 추가");
        builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (mMemo.getText().toString().trim().length() != 0) {
                    Mark mark = new Mark();
                    mark.setvid(CONTENT_ID);
                    mark.setmMemo(mMemo.getText().toString());
                    mark.setmStart(position);
                    mark.setmAdded(System.currentTimeMillis());
                    mark.setMpath(CONTENT_PATH);

                    playerViewModel.insertMark(mark);
                    videoView.resume();
                }
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void seekToOnDoubleTap() {
        //getWidthOfScreen();
        final GestureDetector gestureDetector = new GestureDetector(this,
                new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onDoubleTap(MotionEvent e) {
                        Long cur_pos = videoView.getCurrentPosition();
                        Log.d("TAG", "onDoubleTap():  " + videoView.getCurrentPosition());
                        addMark(cur_pos);
                        videoView.resume();
                        return true;
                    }
                });
        videoView.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));
    }

    private void initVideoView(String id) {
        videoView = findViewById(R.id.videoView);
        videoView.setPortrait(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT);
        videoView.setBackListener((view, isPortrait) -> {
            if (isPortrait) {
                finish();
            }
            return false;
        });

        videoView.setOrientationListener(orientation -> {
            if (orientation == SENSOR_PORTRAIT) {
                changeToPortrait();
            } else if (orientation == SENSOR_LANDSCAPE) {
                changeToLandscape();
            }
        });
        videoView.addMarkToDoubleTap();

        //SimpleMediaSource mediaSource = new SimpleMediaSource("http://vfx.mtime.cn/Video/2019/03/12/mp4/190312083533415853.mp4"); //동영상 URI
        Uri uri = Uri.withAppendedPath(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id);
        SimpleMediaSource mediaSource = new SimpleMediaSource(uri);
        mediaSource.setDisplayName("Apple HLS"); // 동영상 제목 설정

        videoView.setControllerDisplayMode(ExoVideoPlaybackControlView.CONTROLLER_MODE_ALL);
        videoView.play(mediaSource, false);
        seekToOnDoubleTap();
    }


    private void changeToPortrait() {

        // WindowManager operation is not necessary
        WindowManager.LayoutParams attr = getWindow().getAttributes();
//        attr.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
        Window window = getWindow();
        window.setAttributes(attr);
        window.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);


        wrapper.setVisibility(View.VISIBLE);
    }


    private void changeToLandscape() {

        // WindowManager operation is not necessary

        WindowManager.LayoutParams lp = getWindow().getAttributes();
//        lp.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
        Window window = getWindow();
        window.setAttributes(lp);
        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        wrapper.setVisibility(View.GONE);
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (Build.VERSION.SDK_INT > 23) {
            videoView.resume();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if ((Build.VERSION.SDK_INT <= 23)) {
            videoView.resume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (Build.VERSION.SDK_INT <= 23) {
            videoView.pause();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (Build.VERSION.SDK_INT > 23) {
            videoView.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        videoView.releasePlayer();
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return videoView.onKeyDown(keyCode, event);
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void clickItem(int id, String path) {
    }

    @Override
    public void clickLongItem(View v, int id, String path) {
    }

    @Override
    public void clickMark(int id, long start, String path) {
        videoView.seekTo(start);
    }

    @Override
    public void clickLongMark(View v, int id, String path) {
    }

    @Override
    public void onItemSelected(View v, SparseBooleanArray sparseBooleanArray) {
    }
}

