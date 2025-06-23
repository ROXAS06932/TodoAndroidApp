package com.websarva.wings.android.todoaprication.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;


import com.websarva.wings.android.todoaprication.model.Task;

import java.util.List;

//データ操作のインターフェースを作成
//⭐️このインターフェースを変更や機能追加した場合、実装先で実装処理を記述しなければならない！！⭐️
@Dao
public interface TaskDao {
    // データを保存するメソッドを追加
    @Insert
    void insertTask(Task task);

    //タスク一覧を取得するメソッドを追加
    @Query("SELECT * FROM tasks ORDER BY id DESC")
    List<Task> getAllTasks();

    //フィルターメソッドを追加
    //（SQLite は `true = 1`, `false = 0`）
    @Query("SELECT * FROM tasks WHERE isCompleted = 0 ORDER BY id DESC")
    List<Task> getIncompleteTask();


    //チェックボックスをタスクの完了状態と連携
    @Update
    void updateTask(Task task);

    @Delete
    void deleteTask(Task task);

}

//Kotlinとは違い、suspendキーワード不要！
//(ただし、非同期処理にはAsyncTaskやExecutorServiceが必要)
