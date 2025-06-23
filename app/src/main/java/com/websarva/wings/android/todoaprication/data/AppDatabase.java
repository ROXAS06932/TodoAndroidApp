package com.websarva.wings.android.todoaprication.data;

import androidx.room.RoomDatabase;
import androidx.room.Database;

import com.websarva.wings.android.todoaprication.model.Task;
import com.websarva.wings.android.todoaprication.data.TaskDao;

//versionを　1 -> 2 に変更することでスムーズにデータの変更を適用できる
@Database(entities = {Task.class}, version = 2, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract TaskDao taskDao();
}
