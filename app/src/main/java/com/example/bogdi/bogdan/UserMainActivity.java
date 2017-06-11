package com.example.bogdi.bogdan;

import android.*;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.AsyncTask;
import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.concurrent.ExecutionException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

public class UserMainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private Toolbar toolbar;
    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    SharedPreferences preferencesCards;
    SharedPreferences.Editor editorCards;
    static UserMainActivity userMainActivity;
    private int user_id;
    private KeyStore keyStore;
    private static final String KEY_NAME = "Tap2Pay_Key";
    private Cipher cipher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userMainActivity = this;

        //set fullscreen activity without title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        preferences = getSharedPreferences("login", Context.MODE_PRIVATE);
        editor = preferences.edit();
        user_id = preferences.getInt("user_id", 0);

        getCards();

        setContentView(R.layout.activity_user_main);
        Button btPay = (Button) findViewById(R.id.btPay1);
        btPay.setBackgroundResource(R.drawable.pay);
        btPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserMainActivity.this, PayActivity.class);
                startActivity(intent);
            }
        });

        toolbar = (Toolbar) findViewById(R.id.nav_action);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);


        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close);

        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            // method invoked only when the actionBar is not null.
            actionBar.setDisplayHomeAsUpEnabled(true);

        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);

        //handle the option pressed in navigation drawer
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                switch (id) {
                    case R.id.nav_addCard:
                        Intent intent = new Intent(UserMainActivity.this, AddCardActivity.class);
                        startActivity(intent);
                        drawerLayout.closeDrawers();
                        break;
                    case R.id.nav_cards:
                        Intent add_card = new Intent(UserMainActivity.this, CreditCardViewActivity.class);
                        startActivity(add_card);
                        drawerLayout.closeDrawers();
                        break;
                    case R.id.nav_recentActivity:
                        //Intent recentActivity = new Intent(UserMainActivity.this, DownloadActivity.class);
                        //startActivity(recentActivity);
                        drawerLayout.closeDrawers();
                        break;
                    case R.id.nav_logout:
                        AlertDialog.Builder logOutBuilder = new AlertDialog.Builder(UserMainActivity.this);
                        logOutBuilder.setMessage("Do you really want to Log Out?")
                                .setPositiveButton("Log Out", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        editor.clear();
                                        editor.apply();
                                        Intent logout = new Intent(UserMainActivity.this, LoginActivity.class);
                                        startActivity(logout);
                                        finish();
                                        drawerLayout.closeDrawers();
                                    }
                                })
                                .setNegativeButton("Cancel", null)
                                .create()
                                .show();
                        break;
                    case R.id.nav_editProfile:
                        Intent editProfile = new Intent(UserMainActivity.this, EditProfileActivity.class);
                        startActivity(editProfile);
                        drawerLayout.closeDrawers();
                        break;
                    case R.id.nav_deleteProfile:
                        AlertDialog.Builder deleteBuilder = new AlertDialog.Builder(UserMainActivity.this);
                        deleteBuilder.setMessage("Do you really want to delete your Account?")
                                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        authenticationFingerprint();
                                    }
                                })
                                .setNegativeButton("Cancel", null)
                                .create()
                                .show();
                        break;
                    default:
                        Toast.makeText(UserMainActivity.this, "Invalid option", Toast.LENGTH_LONG).show();
                        break;
                }
                return true;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (actionBarDrawerToggle.onOptionsItemSelected(item))
            return true;
        return super.onOptionsItemSelected(item);
    }

    //method that return an instance of this activity
    public static UserMainActivity getUserMainActivity() {
        return userMainActivity;
    }

    //method used for deleting user account
    public void deleteProfile() {
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

    //method that provide Fingerprint authentication
    public void authenticationFingerprint(){
        //fingerprint autentication
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            KeyguardManager keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
            FingerprintManager fingerprintManager = (FingerprintManager) getSystemService(FINGERPRINT_SERVICE);

            if (ActivityCompat.checkSelfPermission(UserMainActivity.this, android.Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            // delete account for API level > M if device does not provide hardware for fingerprint
            if (!fingerprintManager.isHardwareDetected()) {
                deleteProfile();
                //continue with fingerprint authentication if device has this hardware component
            } else {
                if (!fingerprintManager.hasEnrolledFingerprints())
                    Toast.makeText(UserMainActivity.this, "Register at least one fingerprint in Settings", Toast.LENGTH_SHORT).show();
                else {
                    if (!keyguardManager.isKeyguardSecure())
                        Toast.makeText(UserMainActivity.this, "Lock screen security not enabled in Settings", Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(UserMainActivity.this, "Please provide your fingerprint, to succeed deleting your account!", Toast.LENGTH_SHORT).show();
                    genKey();
                    if (cipherInit()) {
                        FingerprintManager.CryptoObject cryptoObject = new FingerprintManager.CryptoObject(cipher);
                        FingerprintHandler helper = new FingerprintHandler(UserMainActivity.this);
                        helper.setDeleteVar(true);
                        helper.startAuthentication(fingerprintManager, cryptoObject);
                    }
                }
            }
        }
    }

    //method for Fingerprint
    private boolean cipherInit() {

        try {
            cipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/" + KeyProperties.BLOCK_MODE_CBC + "/" + KeyProperties.ENCRYPTION_PADDING_PKCS7);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        }

        try {
            keyStore.load(null);
            SecretKey key = (SecretKey) keyStore.getKey(KEY_NAME, null);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return true;
        } catch (IOException e1) {

            e1.printStackTrace();
            return false;
        } catch (NoSuchAlgorithmException e1) {

            e1.printStackTrace();
            return false;
        } catch (CertificateException e1) {

            e1.printStackTrace();
            return false;
        } catch (UnrecoverableKeyException e1) {

            e1.printStackTrace();
            return false;
        } catch (KeyStoreException e1) {

            e1.printStackTrace();
            return false;
        } catch (InvalidKeyException e1) {

            e1.printStackTrace();
            return false;
        }

    }

    //method for Fingerprint
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void genKey() {
        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore");
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }

        KeyGenerator keyGenerator = null;

        try {
            keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        }

        try {
            keyStore.load(null);
            keyGenerator.init(new KeyGenParameterSpec.Builder(KEY_NAME, KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT).setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7).build()
            );
            keyGenerator.generateKey();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }


    }

    //method that interrogates DB and return all cards that user has inserted
    private void getCards() {
        preferences = getSharedPreferences("login", Context.MODE_PRIVATE);
        int user_id = preferences.getInt("user_id", 0);

        preferencesCards = getSharedPreferences("cards", Context.MODE_PRIVATE);
        editorCards = preferencesCards.edit();

        Response.Listener<String> responseListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONArray jsonarray = new JSONArray(response);
                    editorCards.putString("cardsString",jsonarray.toString());
                    editorCards.apply();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
        GetCardsRequest getCardsRequest = new GetCardsRequest(user_id, responseListener);
        RequestQueue queue = Volley.newRequestQueue(UserMainActivity.this);
        queue.add(getCardsRequest);
    }
}
