package com.example.dbvideomarker.repository;

import android.app.Application;
import android.os.AsyncTask;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.example.dbvideomarker.database.AppDatabase;
import com.example.dbvideomarker.database.dao.VideoDao;
import com.example.dbvideomarker.database.entitiy.Media;
import com.example.dbvideomarker.database.entitiy.PlRelVideo;
import com.example.dbvideomarker.database.entitiy.PlayList;
import com.example.dbvideomarker.database.entitiy.Video;

import java.util.List;

public class VideoRepository {

    private String TAG = VideoRepository.class.getSimpleName();

    private VideoDao videoDao;
    private LiveData<List<Video>> allVideo;

    public VideoRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        videoDao = db.videoDao();
        allVideo = videoDao.findAllVideo();
    }

    public LiveData<List<Video>> getAllVideo() { return allVideo; }

    public LiveData<List<Video>> getAllVideo(int sort) {
        return videoDao.findAllVideo(sort);
    }


//    public LiveData<List<Video>> selectVideo(int pid) {
//        return videoDao.selectVideo(pid);
//    }

//    public LiveData<List<Video>> getSearchVideo(String vName) {
//        return videoDao.searchVideo(vName);
//    }

    public void insertVideo(Video video) {
        new AsyncTask<Video, Void, Long>() {
            @Override
            protected Long doInBackground(Video... videos) {
                if(videoDao == null)
                    return -1L;
                return videoDao.insertVideo(videos[0]);
            }

            @Override
            protected void onPostExecute(Long aLong) {
                super.onPostExecute(aLong);
                Log.d(TAG, "insert : " + aLong);
            }
        }.execute(video);
    }

    public void deleteVideo(int id) {
        new AsyncTask<Integer, Void, Integer>() {
            @Override
            protected Integer doInBackground(Integer... integers) {
                if(videoDao == null)
                    return -1;
                return videoDao.deleteVideo(integers[0]);
            }

            @Override
            protected void onPostExecute(Integer integer) {
                super.onPostExecute(integer);
                Log.d(TAG, "deleteVideo : " + integer);
            }
        }.execute(id);
    }

    public void updateVideo(int id, String name) {
        new AsyncTask<Integer, Void, Integer>() {
            @Override
            protected Integer doInBackground(Integer... integers) {
                if (videoDao == null)
                    return -1;
                return videoDao.updateVideo(integers[0], name);
            }

            @Override
            protected void onPostExecute(Integer integer) {
                super.onPostExecute(integer);
                Log.d(TAG, "update : " + integer);
            }
        }.execute(id);
    }
}
