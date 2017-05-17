package com.example.bogdi.bogdan;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class UserMainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private Toolbar toolbar;
    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    static UserMainActivity userMainActivity;
    private int user_id;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userMainActivity=this;

        //set fullscreen activity without title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        preferences = getSharedPreferences("login", Context.MODE_PRIVATE);
        editor = preferences.edit();
        user_id=preferences.getInt("user_id",0);

        setContentView(R.layout.activity_user_main);
        Button btPay = (Button) findViewById(R.id.btPay1);
        btPay.setBackgroundResource(R.drawable.pay);
        btPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(UserMainActivity.this,PayActivity.class);
                startActivity(intent);
            }
        });

        toolbar =(Toolbar)findViewById(R.id.nav_action);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);


        drawerLayout=(DrawerLayout)findViewById(R.id.drawerLayout);
        actionBarDrawerToggle=new ActionBarDrawerToggle(this,drawerLayout,R.string.open,R.string.close);

        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            // method invoked only when the actionBar is not null.
            actionBar.setDisplayHomeAsUpEnabled(true);

        NavigationView navigationView=(NavigationView)findViewById(R.id.navigation_view);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                switch (id)
                {
                    case R.id.nav_addCard:
                        Intent intent = new Intent(UserMainActivity.this, AddCardActivity.class);
                        startActivity(intent);
                        drawerLayout.closeDrawers();
                        break;
                    case R.id.nav_cards:
                        Intent add_card=new Intent(UserMainActivity.this,CreditCard.class);
                        startActivity(add_card);
                        drawerLayout.closeDrawers();
                        break;
                    case R.id.nav_recentActivity:
//                        Intent recentActivity = new Intent(UserMainActivity.this, AddCardActivity.class);
//                        startActivity(recentActivity);
                        drawerLayout.closeDrawers();
                        break;
                    case R.id.nav_logout:
                        editor.clear();
                        editor.apply();
                        Intent logout = new Intent(UserMainActivity.this, LoginActivity.class);
                        startActivity(logout);
                        finish();
                        drawerLayout.closeDrawers();
                        break;
                    case R.id.nav_editProfile:
                        Intent editProfile=new Intent(UserMainActivity.this,EditProfileActivity.class);
                        startActivity(editProfile);
                        drawerLayout.closeDrawers();
                        break;
                    case R.id.nav_deleteProfile:
                        deleteProfile();
                        break;
                    default:
                        Toast.makeText(UserMainActivity.this, "Invalid option",Toast.LENGTH_SHORT).show();
                        break;
                }
                return true;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(actionBarDrawerToggle.onOptionsItemSelected(item))
            return true;
        return super.onOptionsItemSelected(item);
    }

    public static UserMainActivity getUserMainActivity()
    {
        return userMainActivity;
    }

    public void deleteProfile()
    {
        Response.Listener<String> responseListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    boolean success = jsonResponse.getBoolean("success");

                    if (success) {
                        editor.clear();
                        editor.apply();
                        Intent intent = new Intent(UserMainActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(UserMainActivity.this);
                        builder.setMessage("Delete Profile failed")
                                .setNegativeButton("Retry", null)
                                .create()
                                .show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };

        DeleteProfileRequest deleteProfileRequest = new DeleteProfileRequest(user_id, responseListener);
        RequestQueue queue = Volley.newRequestQueue(UserMainActivity.this);
        queue.add(deleteProfileRequest);
    }
}
