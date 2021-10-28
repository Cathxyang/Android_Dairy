package com.example.dairy;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class DairyList extends AppCompatActivity {

    private ArrayList<Integer> IdData = new ArrayList<>();
    private ArrayList<String> TitleData = new ArrayList<>();
    private DairyDatabaseHelper dbHelper;
    private SQLiteDatabase db;
    ListView listView;


    //创建开始
    @SuppressLint({"Recycle", "Range"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dairy_list);

        //编写数据库，将数据库实例化
        dbHelper = new DairyDatabaseHelper(this, null, null, 1);
        db = dbHelper.getWritableDatabase();

        //Button写日记
        Button addData = (Button) findViewById(R.id.add);
        addData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //跳转到写日记页面
                Intent intent = new Intent(DairyList.this, Dairy.class);
                startActivity(intent);
            }
        });
    }


    //上一个活动返回处
    @SuppressLint("Range")
    @Override
    protected void onStart() {
        super.onStart();
        IdData = new ArrayList<>();
        TitleData = new ArrayList<>();

        //查询id和title两列分别存入
        Cursor cursor = db.query("dairy", new String[]{"id", "title"}, null, null, null, null, null);
        while (cursor.moveToNext()) {
            IdData.add(cursor.getInt(cursor.getColumnIndex("id")));
            TitleData.add(cursor.getString(cursor.getColumnIndex("title")));
        }

        //List View内容
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                DairyList.this, android.R.layout.simple_list_item_1, TitleData);
        listView = (ListView) findViewById(R.id.list_view);
        listView.setAdapter(adapter);

        //增改当前日记
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //跳转到当前条目的日记
                Intent intent = new Intent(DairyList.this, Dairy.class);
                //将当前id传给下一个活动
                intent.putExtra("extra_data", IdData.get(position));
                startActivity(intent);
            }
        });

        //删除当前日记
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                //移除数据库中数据
                db.execSQL("delete from dairy where id = ?", new String[]{String.valueOf(IdData.get(position))});
                //移除ArrayList中数据
                IdData.remove(IdData.get(position));
                TitleData.remove(String.valueOf(TitleData.get(position)));
                //重新加载当前adapter
                listView.setAdapter(adapter);
                return true;
            }
        });
    }
}