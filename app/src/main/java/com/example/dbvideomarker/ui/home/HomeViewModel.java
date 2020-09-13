package com.example.dbvideomarker.ui.home;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.dbvideomarker.database.entitiy.Video;
import com.example.dbvideomarker.repository.MarkRepository;
import com.example.dbvideomarker.repository.VideoRepository;

import java.util.List;


public class HomeViewModel extends AndroidViewModel {

    private MarkRepository markRepository;
    private VideoRepository videoRepository;
    private LiveData<List<Video>> allVideo;

    public HomeViewModel(@NonNull Application application) {
        super(application);
        markRepository = new MarkRepository(application);
        videoRepository = new VideoRepository(application);
        allVideo = videoRepository.getAllVideo();
    }
    public LiveData<List<Video>> getAllVideo() {
        return allVideo;
    }

    public LiveData<List<Video>> getAllVideo(int sort) {
        return videoRepository.getAllVideo(sort);
    }

//    public LiveData<List<Video>> selectVideo(int pid) {
//        return videoRepository.selectVideo(pid);
//    }

    public void insertVideo(Video video) {
        videoRepository.insertVideo(video);
    }

    public void deleteVideo(int id) {
        videoRepository.deleteVideo(id);
    }

    public void deleteVideoWithMark(int id) {
        markRepository.deleteVideoWithMark(id);
    }

    public void updateVideo(int id, String name) {
        videoRepository.updateVideo(id, name);
    }
}