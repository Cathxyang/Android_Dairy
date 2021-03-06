package com.example.dairy;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

public class Dairy extends AppCompatActivity {

    public static final int TAKE_PHOTO = 1;
    public static final int CHOOSE_PHOTO = 2;

    private int pageID;
    private int imageID;
    private String date;
    private DairyDatabaseHelper dbHelper;
    private SQLiteDatabase db;
    private Uri imageUri;
    private ImageView image;

    @SuppressLint({"Range", "ResourceType"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dairy);
        Intent intent = getIntent();
        pageID = intent.getIntExtra("extra_data", -1);

        EditText title = (EditText) findViewById(R.id.title);
        EditText user = (EditText) findViewById(R.id.username);
        EditText text = (EditText) findViewById(R.id.text);
        TextView time = (TextView) findViewById(R.id.time);
        Button update = (Button) findViewById(R.id.update);
        Button camera = (Button) findViewById(R.id.take_photo);
        Button album = (Button) findViewById(R.id.from_album);
        image = (ImageView) findViewById(R.id.picture);
        Calendar calendar = Calendar.getInstance();

        dbHelper = new DairyDatabaseHelper(this, null, null, 1);
        db = dbHelper.getWritableDatabase();

        //??????
        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //??????File???????????????????????????????????????
                File outputImage = new File(getExternalCacheDir(), "output_image.jpg");
                try {
                    if (outputImage.exists()) {
                        outputImage.delete();
                    }
                    outputImage.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (Build.VERSION.SDK_INT >= 24) {
                    imageUri = FileProvider.getUriForFile(Dairy.this,
                            "com.example.cameraalbumtest.fileprovider", outputImage);
                } else {
                    imageUri = Uri.fromFile(outputImage);
                }

                //????????????
                Intent intentCamera = new Intent("android.media.action.IMAGE_CAPTURE");
                intentCamera.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(intentCamera, TAKE_PHOTO);
            }
        });

        //???????????????
        album.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent("android.intent.action.GET_CONTENT");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
                startActivityForResult(intent, CHOOSE_PHOTO);
            }
        });

        //???????????????-1?????????????????????????????????????????????????????????????????????
        if (pageID != -1) {

            //????????????
            SharedPreferences prefGet = getSharedPreferences("data", MODE_PRIVATE);
            String name = prefGet.getString("name", "??????").equals("") ?
                    "??????" : prefGet.getString("name", "??????");
            user.setText(name);

            //?????????????????????????????????
            Cursor cursor = db.rawQuery("select title,content,image,time from dairy where id = ?",
                    new String[]{String.valueOf(pageID)});
            while (cursor.moveToNext()) {
                title.setText(cursor.getString(cursor.getColumnIndex("title")));
                text.setText(cursor.getString(cursor.getColumnIndex("content")));
                image.setImageBitmap(load(cursor.getInt(cursor.getColumnIndex("image"))));
                time.setText(cursor.getString(cursor.getColumnIndex("time")));
            }

            //????????????
            update.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //??????????????????
                    date = calendar.get(Calendar.YEAR) + "???" +
                            (calendar.get(Calendar.MONTH) + 1)+ "???" +
                            calendar.get(Calendar.DATE) + "???" + " " +
                            calendar.get(Calendar.HOUR_OF_DAY) + ":" +
                            calendar.get((Calendar.MINUTE));

                    //???????????????title???content???image???time???????????????
                    ContentValues values = new ContentValues();
                    values.put("title", title.getText().toString());
                    values.put("content", text.getText().toString());
                    values.put("image", imageID);
                    values.put("time", date);
                    db.update("dairy", values, "id = ?",
                            new String[]{String.valueOf(pageID)});

                    //????????????
                    SharedPreferences.Editor editor = getSharedPreferences("data",
                            MODE_PRIVATE).edit();
                    editor.putString("name", user.getText().toString());
                    editor.apply();

                    //?????????????????????
                    finish();
                }
            });
        } else {
            update.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    //??????????????????
                    date = calendar.get(Calendar.YEAR) + "???" +
                            (calendar.get(Calendar.MONTH) + 1) + "???" +
                            calendar.get(Calendar.DATE) + "???" + " " +
                            calendar.get(Calendar.HOUR_OF_DAY) + ":" +
                            calendar.get((Calendar.MINUTE));

                    //???????????????title???content???image???time???????????????
                    ContentValues values = new ContentValues();
                    values.put("title", title.getText().toString());
                    values.put("content", text.getText().toString());
                    values.put("image", imageID);
                    values.put("time", date);
                    db.insert("dairy", null, values);

                    //????????????
                    SharedPreferences.Editor editor = getSharedPreferences("data",
                            MODE_PRIVATE).edit();
                    editor.putString("name", user.getText().toString());
                    editor.apply();

                    //

                    //?????????????????????
                    finish();
                }
            });
        }

    }

    //????????????
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            //??????
            case TAKE_PHOTO:
                if (resultCode == RESULT_OK) {
                    try {
                        //??????????????????????????????
                        Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().
                                openInputStream(imageUri));
                        image.setImageBitmap(bitmap);
                        save(bitmap);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                break;
            //?????????
            case CHOOSE_PHOTO:
                if (resultCode == RESULT_OK) {
                    //??????????????????
                    handleImageOnKitKat(data);
                }
                break;
            default:
                break;
        }
    }

    //????????????
    @TargetApi(19)
    private void handleImageOnKitKat(Intent data) {
        String imagePath = null;
        Uri uri = data.getData();
        if (DocumentsContract.isDocumentUri(this, uri)) {
            //?????????document?????????Uri????????????document id??????
            String docId = DocumentsContract.getDocumentId(uri);
            if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
                String id = docId.split(":")[1];
                String selection = MediaStore.Images.Media._ID + "=" + id;
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
            } else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content:" +
                        "//downloads/public_downloads"), Long.parseLong(docId));
                imagePath = getImagePath(contentUri, null);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            //?????????content?????????Uri??????????????????????????????
            imagePath = getImagePath(uri, null);
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            //?????????file?????????Uri??????????????????????????????
            imagePath = uri.getPath();
        }
        displayImage(imagePath);

    }

    //??????????????????
    @SuppressLint("Range")
    private String getImagePath(Uri uri, String selection) {
        String path = null;
        //??????Uri???selection??????????????????????????????
        Cursor cursor = getContentResolver().query(uri, null, selection,
                null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }

    //????????????
    private void displayImage(String imagePath) {
        if (imagePath != null) {
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            image.setImageBitmap(bitmap);
            save(bitmap);
        } else {
            Toast.makeText(this, "failed to get image", Toast.LENGTH_SHORT).show();
        }
    }

    //????????????
    public void save(Bitmap bitmap) {
        FileOutputStream out = null;
        imageID = bitmap.getGenerationId();
        try {
            out = openFileOutput(String.valueOf(imageID), Context.MODE_PRIVATE);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //???????????????
    public Bitmap load(int id) {
        FileInputStream in = null;
        Bitmap bitmap = null;
        try {
            in = openFileInput(String.valueOf(id));
            bitmap = BitmapFactory.decodeStream(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }
}
