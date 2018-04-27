package com.bly01854.idlerunner.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.bly01854.idlerunner.entity.User;

/**
 * Created by bly01854 on 4/17/2018.
 */

@Dao
public interface UserDao {

    @Update
    void updateUser(User user);

    /*@Query("INSERT INTO user(id, steps) SELECT 1, 0 WHERE NOT EXISTS(SELECT 1 FROM user WHERE id = 1)")
    void createUser();  Inserts not supported in Room yet*/

    @Query("SELECT * FROM user WHERE id = 1")
    User getUser();


    @Insert(onConflict = OnConflictStrategy.ABORT)
    void addUser(User user);

    @Delete
    void deleteUser(User user);
}
