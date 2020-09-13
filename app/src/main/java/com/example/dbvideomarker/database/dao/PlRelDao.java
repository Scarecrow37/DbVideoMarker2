package com.example.dbvideomarker.database.dao;

import android.database.sqlite.SQLiteConstraintException;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Entity;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.dbvideomarker.database.entitiy.PlRel;
import com.example.dbvideomarker.database.entitiy.PlRelMark;
import com.example.dbvideomarker.database.entitiy.PlRelVideo;

import java.util.List;

import static androidx.room.OnConflictStrategy.IGNORE;

@Dao
public interface PlRelDao {

    @Query("SELECT * FROM plrel ORDER BY plrel_id")
    LiveData<List<PlRel>> findAllPlayListRelation();

    @Query("SELECT DISTINCT video.contentId as video_id, video.vname as video_name , video.vdur as video_dur , video.vpath as video_path, " +
            "plrel.plrel_pid as playlist_id FROM plrel " +
            "INNER JOIN video ON video.contentId = plrel.plrel_vid " +
            //"INNER JOIN playlist ON playlist.pid = plrel.plrel_pid " +
            "WHERE plrel_pid = :pid")
    LiveData<List<PlRelVideo>> findVideoInPlayList(int pid);


    @Query("SELECT DISTINCT mark.mid as mark_id, mark.vid as mark_vid, mark.mMemo as mark_mmemo, mark.mStart as mark_mstart, " +
            "plrel.plrel_pid as playlist_id FROM plrel " +
            "INNER JOIN mark ON mark.mid = plrel.plrel_mid " +
            //"INNER JOIN playlist ON playlist.pid = plrel.plrel_pid " +
            "WHERE plrel_pid = :pid")
    LiveData<List<PlRelMark>> findMarkInPlayList(int pid);

//    @Query("SELECT COUNT(plrel_mid) FROM plrel WHERE plrel_pid =:pid ")
//    LiveData<Integer> getMarkRowCount(int pid);

//    @Query("SELECT COUNT(plrel_vid) FROM plrel WHERE plrel_pid =:pid ")
//    LiveData<Integer> getVideoRowCount(int pid);


    @Insert(onConflict = IGNORE)
    long insertPlRel(PlRel plRel);

    @Update
    int updatePlRel(PlRel plRel);

    @Query("DELETE FROM plrel WHERE plrel_vid = :vid")
    int deleteVideoInPlaylist(int vid);

    @Query("DELETE FROM plrel WHERE plrel_mid = :mid" )
    int deleteMarkInPlaylist(int mid);

    @Query("DELETE FROM plrel WHERE plrel_pid = :pid")
    int deleteWithPlayList(int pid);

    @Query("SELECT * FROM Plrel WHERE plrel_pid != :pid")
    LiveData<List<PlRel>> videoOverlapCheck(int pid);

//    @Query("SELECT video.vid as video_id, video.vname as video_name, plrel_pid as playlist_id FROM plrel " +
//            "INNER JOIN video ON video.vid = plrel.plrel_vid " +
//            "WHERE plrel_pid != :pid")
//    LiveData<List<PlRelVideo>> getVideoOverlap(int pid);

}


