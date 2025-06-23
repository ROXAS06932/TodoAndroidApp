package com.websarva.wings.android.todoaprication.utils;

import android.content.Context;

import androidx.room.Room;

import com.websarva.wings.android.todoaprication.data.AppDatabase;

//データベースのインスタンス化
public class DatabaseProvider {
    private static AppDatabase instance;

    public static AppDatabase getDatabase(Context context){
        if(instance == null){
            instance = Room.databaseBuilder(context, AppDatabase.class, "task_database")
                    .allowMainThreadQueries() //必要なら削除して非同期処理に対応！
                    .build();
        }
        return instance;
    }
}
