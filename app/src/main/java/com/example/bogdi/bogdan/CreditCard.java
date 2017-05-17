package com.example.bogdi.bogdan;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class CreditCard extends AppCompatActivity {
    static Context context;
    String path;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;

        //set fullscreen activity without title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_credit_card);
        path = Environment.getExternalStorageDirectory() + "/Android/data/" + context.getPackageName() + "/Files/";
        loadImageFromStorage(path, (ImageView) findViewById(R.id.cardImg1));
        loadImageFromStorage(path, (ImageView) findViewById(R.id.cardImg2));
        loadImageFromStorage(path, (ImageView) findViewById(R.id.cardImg3));
    }

    private void loadImageFromStorage(String path, ImageView imageView) {
        try {
            File file = new File(path, "oprea.jpg");
            Bitmap bitmap = BitmapFactory.decodeStream(new FileInputStream(file));
            imageView.setImageBitmap(bitmap);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }


}
