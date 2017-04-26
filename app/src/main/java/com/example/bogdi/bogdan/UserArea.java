package com.example.bogdi.bogdan;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

public class UserArea extends AppCompatActivity {
    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    static UserArea userArea;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userArea = this;

        //set fullscreen activity without title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        preferences = getSharedPreferences("login", Context.MODE_PRIVATE);
        editor = preferences.edit();

        setContentView(R.layout.activity_user_area);

        Button btAddCard = (Button) findViewById(R.id.btAddCard);
        Button btRemoveCard = (Button) findViewById(R.id.btRemoveCard);
        Button btRecentActivity = (Button) findViewById(R.id.btRecentActivity);
        Button btPay = (Button) findViewById(R.id.btPay);

        btAddCard.setBackgroundResource(R.drawable.add_card);
        btRecentActivity.setBackgroundResource(R.drawable.recent_activity);
        btRemoveCard.setBackgroundResource(R.drawable.remove_card);
        btPay.setBackgroundResource(R.drawable.pay);


        btAddCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserArea.this, AddCardActivity.class);
                startActivity(intent);
            }
        });

        btRemoveCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.clear();
                editor.apply();
                Intent intent = new Intent(UserArea.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        btPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserArea.this, CardFormActivity.class);
                startActivity(intent);
            }
        });

        btRecentActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(UserArea.this,UserMainActivity.class);
                startActivity(intent);
            }
        });
    }

    public static UserArea getInstance() {
        return userArea;
    }
}
