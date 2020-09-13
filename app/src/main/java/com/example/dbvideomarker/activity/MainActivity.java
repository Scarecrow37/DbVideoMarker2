package com.example.dbvideomarker.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;
import android.widget.Toast;

import com.example.dbvideomarker.R;
import com.example.dbvideomarker.activity.setting.SettingActivity;
import com.example.dbvideomarker.adapter.ViewPagerAdapter;
import com.example.dbvideomarker.database.entitiy.PlRel;
import com.example.dbvideomarker.ui.mark.MarkFragment;
import com.example.dbvideomarker.ui.home.HomeFragment;
import com.example.dbvideomarker.ui.playlist.PlaylistFragment;
import com.google.android.material.tabs.TabLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager viewPager;
    MarkFragment markFragment;
    HomeFragment homeFragment;
    PlaylistFragment playlistFragment;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar); //툴바를 액션바(앱바)로 사용할 수 있는 메소드

        viewPager = (ViewPager) findViewById(R.id.view_pager);
        viewPager.setOffscreenPageLimit(3);
//        setupViewPager(viewPager);

        tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setSelectedTabIndicatorColor(Color.parseColor("#D5355F"));
        tabLayout.setTabTextColors(Color.parseColor("#73D5355F"), Color.parseColor("#D5355F"));

//        BottomNavigationView navView = findViewById(R.id.nav_view);
//        // Passing each menu ID as a set of Ids because each
//        // menu should be considered as top level destinations.
//        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
//                R.id.navigation_home,
//                R.id.navigation_dashboard,
//                R.id.navigation_notifications).build();
//
//        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
//        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
//        NavigationUI.setupWithNavController(navView, navController);

        setupViewPager(viewPager);
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        markFragment = new MarkFragment();
        homeFragment = new HomeFragment();
        playlistFragment = new PlaylistFragment();
        adapter.addFragment(homeFragment, "VIDEO");
        adapter.addFragment(markFragment, "BOOKMARK");
        adapter.addFragment(playlistFragment, "MY");
        viewPager.setAdapter(adapter);
    }

    public void disableToolbar() {
        toolbar.setVisibility(View.GONE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_toolbar, menu);
//        SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();

//        searchView.setMaxWidth(Integer.MAX_VALUE);
//        /*getSupportActionBar().setDisplayHomeAsUpEnabled(true);*/
//
//        searchView.setQueryHint("검색어를 입력하세요");
//        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//            @Override
//            public boolean onQueryTextSubmit(String query) {
//                Toast.makeText(getApplicationContext(), "검색을 완료했습니다.", Toast.LENGTH_SHORT).show();
//                return false;
//            }
//
//            @Override
//            public boolean onQueryTextChange(String newText) {
//                Toast.makeText(getApplicationContext(), "입력중입니다.", Toast.LENGTH_SHORT).show();
//                return false;
//            }
//        });
        return true;
    }

    private long backKeyPressedTime = 0;
    private Toast toast;

    @Override
    public void onBackPressed() {
        //super.onBackPressed();

        if (System.currentTimeMillis() > backKeyPressedTime + 2500) {
            backKeyPressedTime = System.currentTimeMillis();
            toast = Toast.makeText(this, "뒤로 가기 버튼을 한 번 더 누르면 종료됩니다.", Toast.LENGTH_LONG);
            toast.show();
            return;
        }

        if (System.currentTimeMillis() <= backKeyPressedTime + 2500) {
            finish();
            toast.cancel();
        }
    }
}

