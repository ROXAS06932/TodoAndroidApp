package com.websarva.wings.android.todoaprication.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

//Entityの定義
@Entity(tableName = "tasks")
public class Task {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String title;
    public String description;
    public boolean isCompleted; //チェック状態を管理する変数を追加

    public Task(String title, String description, boolean isCompleted){
        this.title = title;
        this.description = description;
        this.isCompleted = isCompleted;
    }
}
