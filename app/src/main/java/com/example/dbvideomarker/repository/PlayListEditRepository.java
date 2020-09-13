package com.example.dbvideomarker.repository;

import android.app.Application;
import android.os.AsyncTask;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.example.dbvideomarker.database.AppDatabase;
import com.example.dbvideomarker.database.dao.PlRelDao;
import com.example.dbvideomarker.database.entitiy.PlRel;
import com.example.dbvideomarker.database.entitiy.PlRelMark;
import com.example.dbvideomarker.database.entitiy.PlRelVideo;

import java.util.List;

public class PlayListEditRepository {
    private static final String TAG = PlayListEditRepository.class.getSimpleName();

    private PlRelDao plRelDao;
    private LiveData<List<PlRel>> allPlRel;

    public PlayListEditRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        plRelDao = db.plRelDao();
        allPlRel = plRelDao.findAllPlayListRelation();
    }

    public LiveData<List<PlRel>> getAllPlRel() {
        return allPlRel;
    }

    public LiveData<List<PlRelVideo>> findVideoInPlayList(int pid) {
        return plRelDao.findVideoInPlayList(pid);
    }

    public LiveData<List<PlRelMark>> findMarkInPlayList(int pid) {
        return plRelDao.findMarkInPlayList(pid);
    }

//    public LiveData<Integer> getMarkRowCount(int pid) {
//        return plRelDao.getMarkRowCount(pid);
//    }

//    public LiveData<Integer> getVideoCount(int pid) {
//        return plRelDao.getVideoRowCount(pid);
//    }

//    public LiveData<List<PlRel>> videoOverlapCheck(int pid) {
//        return plRelDao.videoOverlapCheck(pid);
//    }

    public void insertPlRelation(PlRel plRel) {
        new AsyncTask<PlRel, Void, Long>() {
            @Override
            protected Long doInBackground(PlRel... plRels) {
                if(plRelDao == null)
                    return -1L;
                return plRelDao.insertPlRel(plRels[0]);
            }

            @Override
            protected void onPostExecute(Long aLong) {
                super.onPostExecute(aLong);
                Log.d(TAG, "insert : " + aLong);
            }
        }.execute(plRel);
    }

    public void deleteVideoInPlaylist(int vid) {
        new AsyncTask<Integer, Void, Integer>() {
            @Override
            protected Integer doInBackground(Integer... integers) {
                if (plRelDao == null)
                    return -1;
                return plRelDao.deleteVideoInPlaylist(integers[0]);
            }

            @Override
            protected void onPostExecute(Integer integer) {
                super.onPostExecute(integer);
                Log.d(TAG, "delete video in playlist : " + integer);
            }
        }.execute(vid);
    }

    public void deleteMarkInPlaylist(int mid) {
        new AsyncTask<Integer, Void, Integer>() {
            @Override
            protected Integer doInBackground(Integer... integers) {
                if (plRelDao == null)
                    return -1;
                return plRelDao.deleteMarkInPlaylist(integers[0]);
            }

            @Override
            protected void onPostExecute(Integer integer) {
                super.onPostExecute(integer);
                Log.d(TAG, "delete mark in playlist : " + integer);
            }
        }.execute(mid);
    }

    public void deleteWithPlayList(int pid) {
        new AsyncTask<Integer, Void, Integer>() {
            @Override
            protected Integer doInBackground(Integer... integers) {
                if(plRelDao == null)
                    return -1;
                return plRelDao.deleteWithPlayList(integers[0]);
            }

            @Override
            protected void onPostExecute(Integer integer) {
                super.onPostExecute(integer);
                Log.d(TAG, "deleteWithPlayList : " + integer);
            }
        }.execute(pid);
    }
}
