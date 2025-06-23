package com.websarva.wings.android.todoaprication.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.websarva.wings.android.todoaprication.R;
import com.websarva.wings.android.todoaprication.model.Task;
import com.websarva.wings.android.todoaprication.repository.TaskRepository;

import java.util.List;

//RecyclerView用のアダプターを作成
public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {
    private List<Task> taskList;
    private TaskRepository taskRepository; //TaskRepositoryを追加

    //コンストラクタ
    public TaskAdapter(List<Task> taskList, TaskRepository taskRepository){
        this.taskList = taskList;
        this.taskRepository = taskRepository; //TaskRepositoryを受け取る
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.task_item, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position){
        Task task = taskList.get(position);

        //タスク名を表示
        holder.title.setText(task.title);
        holder.description.setText(task.description);

//        //チェック状態を表示
//        holder.checkbox.setChecked(task.isCompleted);
//
//        //チェックボックスの状態が表示されたら、データを更新
//        holder.checkbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
//            task.isCompleted = isChecked; // ✅ 状態更新
//            taskRepository.updateTask(task); // ✅ データベースに保存
//        });

        //delete処理の記述↓↓
        //表示制御 & 削除処理を記述
        //リスナを一旦解除してから状態をセット
        holder.checkboxCompleted.setOnCheckedChangeListener(null);
        holder.checkboxCompleted.setChecked(task.isCompleted);
        holder.btnDelete.setVisibility(task.isCompleted ? View.VISIBLE : View.GONE);

        // その後に再度リスナーをセット！
        holder.checkboxCompleted.setOnCheckedChangeListener((buttonView, isChecked) -> {
            task.isCompleted = isChecked;
            holder.btnDelete.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            taskRepository.updateTask(task); //チェック状態をDBに保存！次回起動時に保持される！
        });

        holder.btnDelete.setOnClickListener(v -> {
            taskRepository.deleteTask(task); // RepositoryにdeleteTask()がある前提
            taskList.remove(holder.getAdapterPosition());
            notifyItemRemoved(holder.getAdapterPosition());
        });
        //deleteここまで↑↑

        //編集ダイアログを表示して、タイトル編集を反映する処理
        //descriptionも一緒に編集できるようにする
        holder.itemView.setOnClickListener(v -> { //タイトルタップで編集画面表示
            Context context = v.getContext(); // Contextが必要になる
            //カスタムビューを作成(dialog_edit_task.xmlを使うとキレイ)
            View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edit_task, null);

            EditText etTitle = dialogView.findViewById(R.id.etEditTitle);
            EditText etDescription = dialogView.findViewById(R.id.etEditDescription);

            etTitle.setText(task.title);
            etDescription.setText(task.description);

//            EditText input = new EditText(context);
//            input.setText(task.title); //現在のタイトルをダイアログにセット

            new AlertDialog.Builder(context)
                    .setTitle("タスクを編集")
//                    .setView(input)
                    .setView(dialogView)
                    .setPositiveButton("保存", (dialog, which) -> {
//                        String updatedTitle = input.getText().toString().trim();
                        String updatedTitle = etTitle.getText().toString().trim();
                        String updatedDescription = etDescription.getText().toString().trim();

                        if(!updatedTitle.isEmpty()){
                            task.title = updatedTitle; //Taskデータの書き換え
                            task.description = updatedDescription;
                            taskRepository.updateTask(task); //Roomに変更を反映
                            notifyItemChanged(holder.getAdapterPosition()); //変更した部分のみRecyclerViewを更新
                        }
                    }).setNegativeButton("キャンセル", null).show();
        });

    }

    @Override
    public int getItemCount(){
        return taskList.size();
    }

    public void addTask(Task task){
        taskList.add(task);
        notifyItemInserted(taskList.size() - 1);
    }


    static class TaskViewHolder extends RecyclerView.ViewHolder{
        TextView title, description;
        Button btnDelete;
        CheckBox checkboxCompleted;
        TaskViewHolder(View itemView){
            super(itemView);
            title = itemView.findViewById(R.id.task_title);
            description = itemView.findViewById(R.id.task_description);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            checkboxCompleted = itemView.findViewById(R.id.checkboxCompleted);
        }
    }
}
