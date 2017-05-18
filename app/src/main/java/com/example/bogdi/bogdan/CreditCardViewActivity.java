package com.example.bogdi.bogdan;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.util.ArrayList;

public class CreditCardViewActivity extends AppCompatActivity {
    static Context context;
    String path;
    static File dir;
    private int numberOfCards;

    LinearLayout linearLayout;

    //array list of Image View which will serve as a list items
    ArrayList<String> imageNameList = new ArrayList<>();

    // array of supported extensions (use a List if you prefer)
    static final String EXTENSIONS = ".jpg";    // and other formats you need

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;

        //set fullscreen activity without title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_credit_card);

        linearLayout = (LinearLayout) findViewById(R.id.linearLayoutCardView);

        //get resources from application folder
        path = Environment.getExternalStorageDirectory() + "/Android/data/" + context.getPackageName() + "/Files/";
        dir = new File(path);

        loadImageFromStorage(dir);

        //set listener for item(card) clicked
        for(int i=0;i<numberOfCards;i++) {
            final int j=i;
            linearLayout.getChildAt(i).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(CreditCardViewActivity.this, imageNameList.get(j), Toast.LENGTH_SHORT).show();
                }
            });
        }

        linearLayout.removeViewAt(1);
    }

    //method that load images from storage and show them in views
    private void loadImageFromStorage(File directory) {
        try {
            File[] files = directory.listFiles();

            if (files != null) {
                numberOfCards = files.length;
                for (int i = 0; i < files.length; i++) {
                    if (files[i].getName().endsWith(EXTENSIONS)) {
                        //show cards in view
                        Bitmap bitmap = BitmapFactory.decodeStream(new FileInputStream(files[i]));
                        final ImageView imageView = new ImageView(this);
                        imageView.setImageBitmap(bitmap);
                        linearLayout.addView(imageView);

                        //create list of cards
                        String name = files[i].getName();
                        int pos = name.lastIndexOf(".");
                        if (pos > 0) {
                            name = name.substring(0, pos);
                        }
                        imageNameList.add(name);
                    }
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
