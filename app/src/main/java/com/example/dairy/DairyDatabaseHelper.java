package com.example.dairy;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import androidx.annotation.Nullable;

public class DairyDatabaseHelper extends SQLiteOpenHelper {

    public static final String CREATE_DAIRY = "create table dairy ("
            + "id integer primary key autoincrement,"
            + "title text,"
            + "content text,"
            + "image integer,"
            + "time text)";
//    public static final String CREATE_DAIRY = "drop table dairy";

    private Context mContext;

    public DairyDatabaseHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, "dairy.db", factory, version);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_DAIRY);
        Toast.makeText(mContext, "Create succeeded", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
