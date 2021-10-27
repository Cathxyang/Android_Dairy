package com.example.dairy;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

public class Dairy extends AppCompatActivity {

    private int id;
    private String date;
    private DairyDatabaseHelper dbHelper;
    private SQLiteDatabase db;

    @SuppressLint("Range")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dairy);
        Intent intent = getIntent();
        id = intent.getIntExtra("extra_data", -1);

        EditText title = (EditText) findViewById(R.id.title);
        EditText text = (EditText) findViewById(R.id.text);
        TextView user = (TextView) findViewById(R.id.username);
        TextView time = (TextView) findViewById(R.id.time);
        Button update = (Button) findViewById(R.id.update);
        Calendar calendar = Calendar.getInstance();
        date = calendar.get(Calendar.YEAR) + "/" +
                (calendar.get(Calendar.MONTH) + 1) + "/" +
                calendar.get(Calendar.DATE);

        dbHelper = new DairyDatabaseHelper(this, null, null, 1);
        db = dbHelper.getWritableDatabase();

        //如果不等于-1，则说明数据库有数据，进行更改，否则进行添加。
        if (id != -1) {
            Cursor cursor = db.rawQuery("select title,username,content,time from dairy where id = ?", new String[] {String.valueOf(id)});
            while (cursor.moveToNext()) {
                title.setText(cursor.getString(cursor.getColumnIndex("title")));
                user.setText(cursor.getString(cursor.getColumnIndex("username")));
                text.setText(cursor.getString(cursor.getColumnIndex("content")));
                time.setText(cursor.getString(cursor.getColumnIndex("time")));
            }
            update.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //获取当前时长
                    date = calendar.get(Calendar.YEAR) + "年" +
                            (calendar.get(Calendar.MONTH) + 1) + "月" +
                            calendar.get(Calendar.DATE) + "日" + " " +
                            (calendar.get(Calendar.HOUR) + 8)+ ":" +
                            calendar.get((Calendar.MINUTE));

                    //将当前修改存入数据库
                    ContentValues values = new ContentValues();
                    values.put("title", title.getText().toString());
                    values.put("content", text.getText().toString());
                    values.put("time",date);
                    db.update ("dairy", values, "id = ?", new String[] {String.valueOf(id)});

                    //跳转出当前页面
                    finish();
                }
            });
        } else {
            update.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //获取当前时长
                    date = calendar.get(Calendar.YEAR) + "年" +
                            (calendar.get(Calendar.MONTH) + 1) + "月" +
                            calendar.get(Calendar.DATE) + "日" + " " +
                            (calendar.get(Calendar.HOUR) + 8)+ ":" +
                            calendar.get((Calendar.MINUTE));

                    //将当前添加存入数据库
                    ContentValues values = new ContentValues();
                    values.put("title", title.getText().toString());
                    values.put("content", text.getText().toString());
                    values.put("time",date);
                    db.insert("dairy", null, values);

                    //跳转出当前页面
                    finish();
                }
            });
        }

    }
}
