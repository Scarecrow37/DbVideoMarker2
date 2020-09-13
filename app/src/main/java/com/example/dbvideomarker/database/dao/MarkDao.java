package com.example.dbvideomarker.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.dbvideomarker.database.entitiy.Mark;

import java.util.List;

import static androidx.room.OnConflictStrategy.IGNORE;

@Dao
public interface MarkDao {

    @Query("SELECT * FROM Mark ORDER BY CASE WHEN :sort = 0 THEN mMemo END ASC, " +
                                        "CASE WHEN :sort = 1 THEN mAdded END ASC, " +
                                        "CASE WHEN :sort = 2 THEN mAdded END DESC")
    LiveData<List<Mark>> findAllMark(int sort);

    @Query("SELECT * FROM Mark ORDER BY mmemo")
    LiveData<List<Mark>> findAllMark();

    @Query("SELECT * FROM Mark WHERE mMemo LIKE + '%' + :mMemo + '%' ORDER BY mMemo")
    LiveData<List<Mark>> searchMark(String mMemo);

    @Query("SELECT * FROM mark WHERE vid = :id")
    LiveData<List<Mark>> getMarkByVideoId(int id);

    @Query("SELECT * FROM Mark WHERE mid = :mid")
    Mark findMark(int mid);

    @Insert
    long insertMark(Mark mark);

    @Query("UPDATE mark SET mMemo =:name WHERE mid = :id ")
    int updateMark(int id, String name);

    @Query("DELETE FROM Mark WHERE mid = :mid")
    int deleteMark(int mid);

    @Query("DELETE FROM Mark WHERE vid =:id")
    int deleteVideoWithMark(int id);
}
