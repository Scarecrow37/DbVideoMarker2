package com.example.dbvideomarker.ui.mark;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.dbvideomarker.database.entitiy.Mark;
import com.example.dbvideomarker.database.entitiy.PlRelMark;
import com.example.dbvideomarker.repository.MarkRepository;
import com.example.dbvideomarker.repository.PlayListEditRepository;

import java.util.List;

public class MarkViewModel extends AndroidViewModel {

    private MarkRepository markRepository;
    private PlayListEditRepository playListEditRepository;
    private LiveData<List<Mark>> allMark;

    public MarkViewModel(@NonNull Application application) {
        super(application);
        markRepository = new MarkRepository(application);
        allMark = markRepository.getAllMark();
    }

    public LiveData<List<Mark>> getAllMark(int sort) {
        return markRepository.getAllMark(sort);
    }

    public LiveData<List<Mark>> getAllMark() {
        return allMark;
    }

    public void deleteMark(int id) {
        markRepository.deleteMark(id);
    }

    public void updateMark(int id, String name) {
        markRepository.updateMark(id, name);
    }
}