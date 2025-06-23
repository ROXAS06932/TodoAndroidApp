package com.websarva.wings.android.todoaprication;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.websarva.wings.android.todoaprication.adapter.TaskAdapter;
import com.websarva.wings.android.todoaprication.data.AppDatabase;
import com.websarva.wings.android.todoaprication.data.TaskDao;
import com.websarva.wings.android.todoaprication.repository.TaskRepository;
import com.websarva.wings.android.todoaprication.model.Task;
import com.websarva.wings.android.todoaprication.repository.TaskRepository;
import com.websarva.wings.android.todoaprication.utils.DatabaseProvider;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.lang.reflect.Type;

public class MainActivity extends AppCompatActivity {
    private List<Task> taskList = new ArrayList<>(); //クラスのフィールドとして宣言
        //RecyclerViewのセットアップ(空のリストで初期化)
        //この書き方は「クロージャ」の中から外の変数を書き換える」javaのテクニック
    private TaskAdapter[] adapter = new TaskAdapter[1]; //配列で参照を保持
//    private RecyclerView.Adapter adapter;

    private boolean isInitialSetup = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        System.out.println("出力テスト");
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);


        //Roomデータベースのセットアップ
//        AppDatabase db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "task_database").build();
        AppDatabase db = DatabaseProvider.getDatabase(getApplicationContext());
        TaskDao taskDao = db.taskDao();
        TaskRepository taskRepository = new TaskRepository(taskDao);

        //　各データを取得↓↓

        //タスク入力欄・ボタン取得・リサイクラビュー・フィルター
        EditText etTask = findViewById(R.id.etTask);
        Button btnAdd = findViewById(R.id.btnAdd);
        RecyclerView rvTasks = findViewById(R.id.rvTasks);
        //標準の <Switch> を使うよりも、SwitchCompat か MaterialSwitchを使用する
        SwitchCompat switchFilter = findViewById(R.id.switchFilter);

        adapter[0] = new TaskAdapter(taskList, taskRepository); //adapterを初期化
        rvTasks.setAdapter(adapter[0]);
        rvTasks.setLayoutManager(new LinearLayoutManager(this));

        //データベースから読み込む時
        taskRepository.getAllTasks(tasks -> {
            Log.d("DEBUG", "取得したタスク数: " + tasks.size());
            for(Task t : tasks){
                Log.d("DEBUG", "タスク： " + t.title);
            }
            runOnUiThread(() -> {
                taskList.clear();
                taskList.addAll(tasks);
                adapter[0].notifyDataSetChanged(); // ← RecyclerView のデータに変更があったとき、再度全描画する
//                rvTasks.setLayoutManager(new LinearLayoutManager(this));
            });
        });

        //入力され、ボタンが押されたらタスクをリストに追加
        btnAdd.setOnClickListener(v -> {
            Log.d("DEBUG", "ボタンが押された");
            String taskName = etTask.getText().toString();
            if(!taskName.isEmpty()){
                //Room用の「Task」オブジェクトを作成
                Task newTask = new Task(taskName, "", false); //空の説明 & 完了状態はfalse

                //タスクをデータベースに保存
                taskRepository.insertTask(newTask);

//                //リアルタイムで追加させる
//                adapter[0].addTask(newTask);

                if (switchFilter.isChecked()) {
                    taskRepository.getIncompleteTasks(tasks -> {
                        taskList.clear();
                        taskList.addAll(tasks);
                        runOnUiThread(() -> adapter[0].notifyDataSetChanged());
                    });
                } else {
                    taskRepository.getAllTasks(tasks -> {
                        taskList.clear();
                        taskList.addAll(tasks);
                        runOnUiThread(() -> adapter[0].notifyDataSetChanged());
                    });
                }

//                //RecyclerViewを更新
//                if(adapter[0] != null){
//                    taskList.add(newTask);
//                    adapter[0].notifyDataSetChanged();
//                }

                //ユーザーに通知
                Toast.makeText(this, "タスクを追加しましした！", Toast.LENGTH_SHORT).show();

                //入力欄をクリア
                etTask.setText("");
            }
        });

        //フィルタースイッチの挙動を実装

        //スイッチの状態を手動でセットしてから、リスタを付ける
        switchFilter.setChecked(false);

        switchFilter.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Log.d("DEBUG", isChecked ? "フィルターon" : "フィルターoff");
            if(isChecked){
                Log.d("DEBUG", "🔔 getIncompleteTasks() を呼び出した！");
                taskRepository.getIncompleteTasks(tasks -> {
                    taskList.clear();
                    taskList.addAll(tasks);
                    runOnUiThread(() -> adapter[0].notifyDataSetChanged()); // ← RecyclerView のデータに変更があったとき、再度全描画する
                });
            }else{
                taskRepository.getAllTasks(tasks -> {
                    taskList.clear();
                    taskList.addAll(tasks);
                    runOnUiThread(() -> adapter[0].notifyDataSetChanged()); // ← RecyclerView のデータに変更があったとき、再度全描画する
                    Log.d("DEBUG", "取得された未完了タスク数: " + tasks.size()); //2
                });
            }
        });

        //タスクをスワイプ時に削除する処理
        ItemTouchHelper.SimpleCallback  simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT){
            //スワイプ時に削除アイコンを表示させる
            final ColorDrawable background = new ColorDrawable(Color.RED);

            @Override
            public void onChildDraw(@NonNull Canvas canvas, @NonNull RecyclerView recyclerView,
                                    @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY,
                                    int actionState, boolean isCurrentlyActive){

                Context context = viewHolder.itemView.getContext();
                Drawable icon = ContextCompat.getDrawable( context, R.drawable.ic_delete);
                View itemView = viewHolder.itemView;
                int backgroundCornerOffset = 20;

                //左右スワイプで背景描画
                if(dX > 0){ //→スワイプ
                    background.setBounds(itemView.getLeft(), itemView.getTop(),
                                        itemView.getLeft() + ((int) dX) + backgroundCornerOffset,
                                        itemView.getBottom());
                }else if(dX < 0){ //　←スワイプ
                    background.setBounds(itemView.getRight() + ((int) dX) - backgroundCornerOffset,
                                        itemView.getTop(), itemView.getRight(), itemView.getBottom());
                }else{
                    background.setBounds(0, 0, 0, 0);
                }

                background.draw(canvas);

                //アイコンの位置計算
                int iconMargin = (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
                int iconTop = itemView.getTop() + iconMargin;
                int iconBottom = iconTop + icon.getIntrinsicHeight();

                if(dX > 0){ // →スワイプ
                    int iconLeft = itemView.getLeft() + iconMargin;
                    int iconRight = iconLeft + icon.getIntrinsicWidth();
                }else if(dX < 0){ // ←スワイプ
                    int iconRight = itemView.getRight() - iconMargin;
                    int iconLeft = iconRight - icon.getIntrinsicWidth();
                    icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                }

                icon.draw(canvas);

                super.onChildDraw(canvas, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false; // ドラッグ移動は使わない
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction){
                int position = viewHolder.getAdapterPosition();
                Task taskToDelete = taskList.get(position);

                taskRepository.deleteTask(taskToDelete); //DBから削除
                taskList.remove(position);
                adapter[0].notifyItemRemoved(position);

                Toast.makeText(MainActivity.this, "タスクを削除しました", Toast.LENGTH_SHORT).show();
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(rvTasks);


//        RecyclerView recyclerView = findViewById(R.id.recycler_view);

//        recyclerView.setAdapter(adapter[0]);
//        recyclerView.setLayoutManager(new LinearLayoutManager(this));

//        recyclerView.setAdapter(adapter[0]);
//        recyclerView.setLayoutManager(new LinearLayoutManager(this));

//        Button addTaskButton = findViewById(R.id.btnAdd);
//        addTaskButton.setOnClickListener(v -> {
//            Task newTask = new Task("新しいタスク", "説明", false);
//            taskRepository.insertTask(newTask); //タスク保存
//            Toast.makeText(this, "タスクを追加しました！", Toast.LENGTH_SHORT).show();
//        });
    }

    //タスクを保存するメソッド
//    private void saveTasks(){
//        SharedPreferences prefs = getSharedPreferences("TodoPrefs", MODE_PRIVATE);
//        SharedPreferences.Editor editor = prefs.edit();
//
//        //SharedPreferencesを使って、リストを保存&読み込み
//        Gson gson = new Gson(); //JSON変換用
//        String json = gson.toJson(taskList); //List<Task>をJSONに変換
//
//        editor.putString("tasks", json); //Setで保存
//        //SharedPreferencesの変更を即座に適用するメソッド
//        editor.apply(); //データを非同期で保存
//    }

    //タスクを読み込むメソッド
//    private List<Task> loadTasks(){
//        SharedPreferences prefs = getSharedPreferences("TodoPrefs", MODE_PRIVATE);
//        String json = prefs.getString("tasks", null);
//
//        if(json != null){
//            Gson gson = new Gson();
//            Type listType = new TypeToken<ArrayList<Task>>(){}.getType();
//            return gson.fromJson(json, listType);
//        }
//        return new ArrayList<>(); //初回起動時は空のリスト
//    }
//
//
//    //ViewHolderの定義
//    class TodoViewHolder extends RecyclerView.ViewHolder{
//        TextView tvTaskName;
//        CheckBox cbComplete;
//
//        public TodoViewHolder(View itemView){
//            super(itemView);
//            tvTaskName = itemView.findViewById(R.id.tvTaskName);
//            cbComplete = itemView.findViewById(R.id.cbComplete);
//        }
//    }

    //RecyclerView.Adapterの作成
//    class TodoAdapter extends RecyclerView.Adapter<TodoViewHolder>{
//        private List<Task> taskList;
//
//        //コンストラクタ
//        public TodoAdapter(List<Task> taskList){
//            this.taskList = taskList;
//        }
//
//        @Override
//        public TodoViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
//            //XMLレイアウトをインフレーとしてViewを作成
//            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_task, parent, false);
//            return new TodoViewHolder(view);
//        }
//
//        @Override
//        public void onBindViewHolder(TodoViewHolder holder, int position){
//
//            Task task = taskList.get(position);
//            //タスク名をセット
//            holder.tvTaskName.setText(task.title);
//            //チェック状態をセット
//            holder.cbComplete.setChecked(task.isCompleted);
//
//            //チェックボックスのクリックイベント
//            holder.cbComplete.setOnCheckedChangeListener((buttonView, isChecked) -> {
//
//                //チェックをつけてもタスクが削除されず、状態が保持される処理
//                task.isCompleted = isChecked; //チェック状態を更新
////                saveTasks(); //状態を保持
//                taskRepository.updateTask(task);
//            });
//        }
//
//        @Override
//        public int getItemCount(){
//            return taskList.size();
//        }
//    }
}
