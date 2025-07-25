package com.websarva.wings.android.todoaprication.repository;

import android.util.Log;

import com.websarva.wings.android.todoaprication.data.TaskDao;
import com.websarva.wings.android.todoaprication.model.Task;

import java.util.List;

//taskDaoインターフェースを使用しているので　taskDaoを変更した場合はここで実装しなければならない
public class TaskRepository {
    private final TaskDao taskDao;

    //コンストラクタ
    public TaskRepository(TaskDao taskDao){
        this.taskDao = taskDao;
    }

    public void insertTask(Task task){
        //非同期でデータを保存(AndroidのMainThreadをブロックしないため)
        new Thread(() -> taskDao.insertTask(task)).start(); //非同期処理
    }


    public void getAllTasks(Callback<List<Task>> callback){
        new Thread(() ->{
            System.out.println("Repository: getAllTasks()　に入った");
            List<Task> tasks = taskDao.getAllTasks(); //Roomからデータ取得
            System.out.println("Repository: タスク数 = " + tasks.size());
            callback.onResult(tasks); //結果をコールバックで渡す
        }).start();
    }

    //更新処理を追加
    public void updateTask(Task task){
        new Thread(() -> taskDao.updateTask(task)).start();
    }

    public void deleteTask(Task task){
        new Thread(() -> taskDao.deleteTask(task)).start();
    }

    public void getIncompleteTasks(Callback<List<Task>> callback){
        new Thread(() -> {
            List<Task> tasks = taskDao.getIncompleteTask();
            callback.onResult(tasks);
        }).start();
    }
}
