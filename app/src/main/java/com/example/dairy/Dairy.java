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

        //照相
        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //创建File对象，用于存储拍照后的照片
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

                //启动相机
                Intent intentCamera = new Intent("android.media.action.IMAGE_CAPTURE");
                intentCamera.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(intentCamera, TAKE_PHOTO);
            }
        });

        //从相册选择
        album.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent("android.intent.action.GET_CONTENT");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
                startActivityForResult(intent, CHOOSE_PHOTO);
            }
        });

        //如果不等于-1，则说明数据库有数据，进行更改，否则进行添加。
        if (pageID != -1) {

            //读取作者
            SharedPreferences prefGet = getSharedPreferences("data", MODE_PRIVATE);
            String name = prefGet.getString("name", "佚名").equals("") ?
                    "佚名" : prefGet.getString("name", "佚名");
            user.setText(name);

            //读取除了作者和图片以外的信息
            Cursor cursor = db.rawQuery("select title,content,image,time from dairy where id = ?",
                    new String[]{String.valueOf(pageID)});
            while (cursor.moveToNext()) {
                title.setText(cursor.getString(cursor.getColumnIndex("title")));
                text.setText(cursor.getString(cursor.getColumnIndex("content")));
                image.setImageBitmap(load(cursor.getInt(cursor.getColumnIndex("image"))));
                time.setText(cursor.getString(cursor.getColumnIndex("time")));
            }

            //读取图片


            //提交修改
            update.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //获取当前时长
                    date = calendar.get(Calendar.YEAR) + "年" +
                            calendar.get(Calendar.MONTH) + "月" +
                            calendar.get(Calendar.DATE) + "日" + " " +
                            calendar.get(Calendar.HOUR_OF_DAY) + ":" +
                            calendar.get((Calendar.MINUTE));

                    //将当前修改title、content、image和time存入数据库
                    ContentValues values = new ContentValues();
                    values.put("title", title.getText().toString());
                    values.put("content", text.getText().toString());
                    values.put("image", imageID);
                    values.put("time", date);
                    db.update("dairy", values, "id = ?",
                            new String[]{String.valueOf(pageID)});

                    //修改作者
                    SharedPreferences.Editor editor = getSharedPreferences("data",
                            MODE_PRIVATE).edit();
                    editor.putString("name", user.getText().toString());
                    editor.apply();

                    //跳转出当前页面
                    finish();
                }
            });
        } else {
            update.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    //获取当前时间
                    date = calendar.get(Calendar.YEAR) + "年" +
                            calendar.get(Calendar.MONTH) + "月" +
                            calendar.get(Calendar.DATE) + "日" + " " +
                            calendar.get(Calendar.HOUR_OF_DAY) + ":" +
                            calendar.get((Calendar.MINUTE));

                    //将当前添加title、content、image和time存入数据库
                    ContentValues values = new ContentValues();
                    values.put("title", title.getText().toString());
                    values.put("content", text.getText().toString());
                    values.put("image", imageID);
                    values.put("time", date);
                    db.insert("dairy", null, values);

                    //修改作者
                    SharedPreferences.Editor editor = getSharedPreferences("data",
                            MODE_PRIVATE).edit();
                    editor.putString("name", user.getText().toString());
                    editor.apply();

                    //

                    //跳转出当前页面
                    finish();
                }
            });
        }

    }

    //显示图片
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            //照相
            case TAKE_PHOTO:
                if (resultCode == RESULT_OK) {
                    try {
                        //将拍摄的照片显示出来
                        Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().
                                openInputStream(imageUri));
                        image.setImageBitmap(bitmap);
                        save(bitmap);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                break;
            //选照片
            case CHOOSE_PHOTO:
                if (resultCode == RESULT_OK) {
                    //查找照片路径
                    handleImageOnKitKat(data);
                }
                break;
            default:
                break;
        }
    }

    //处理图片
    @TargetApi(19)
    private void handleImageOnKitKat(Intent data) {
        String imagePath = null;
        Uri uri = data.getData();
        if (DocumentsContract.isDocumentUri(this, uri)) {
            //如果是document类型的Uri，则通过document id处理
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
            //如果是content类型的Uri，则通过普通方式处理
            imagePath = getImagePath(uri, null);
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            //如果是file类型的Uri，则直接获取图片路径
            imagePath = uri.getPath();
        }
        displayImage(imagePath);

    }

    //得到图片路径
    @SuppressLint("Range")
    private String getImagePath(Uri uri, String selection) {
        String path = null;
        //通过Uri和selection来获取真实的图片路径
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

    //得到图片
    private void displayImage(String imagePath) {
        if (imagePath != null) {
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            image.setImageBitmap(bitmap);
            save(bitmap);
        } else {
            Toast.makeText(this, "failed to get image", Toast.LENGTH_SHORT).show();
        }
    }

    //存入文件
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

    //从文件读出
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
