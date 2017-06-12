package com.example.bogdi.bogdan;

import android.Manifest;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

public class LoginActivity extends AppCompatActivity {

    private KeyStore keyStore;
    private static final String KEY_NAME = "Tap2Pay_Key";
    private Cipher cipher;
    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    static LoginActivity loginActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loginActivity = this;

        //set fullscreen activity without title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        preferences = getSharedPreferences("login", Context.MODE_PRIVATE);
        editor = preferences.edit();

        setContentView(R.layout.activity_login);

        //declare view elements
        final EditText etEmail = (EditText) findViewById(R.id.etLoginEmail);
        final EditText etPassword = (EditText) findViewById(R.id.etLoginPassword);
        final TextView tvSignUp = (TextView) findViewById(R.id.tvSignUp);

        final Button btLogin = (Button) findViewById(R.id.btLogin);
        etEmail.setText(preferences.getString("email", ""));

        //Sign in button handler
        btLogin.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                //get data from email and password field
                final String email = etEmail.getText().toString();
                final String password = etPassword.getText().toString();

                //check if is introduced an email valid
                if (!isEmailValid(email)) {
                    Toast.makeText(LoginActivity.this, "Please provide an valid email", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    //check if there was a session for this user after email address
                    if (preferences.getString("email", "").equals("")) {
                        //check if network is available
                        if (!isNetworkAvailable()) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                            builder.setMessage("Please activate your Internet connection")
                                    .setNegativeButton("Retry", null)
                                    .create()
                                    .show();
                            return;
                        }
                        if (password.length() != 0) {
                            //make login if there is no session
                            Response.Listener<String> responseListener = new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    try {
                                        JSONObject jsonObject = new JSONObject(response);
                                        boolean success = jsonObject.getBoolean("success");

                                        if (success) {
                                            editor.putString("email", email);
                                            editor.putBoolean("fingerprint", true);
                                            editor.putString("password", password);
                                            editor.putInt("user_id", jsonObject.getInt("user_id"));
                                            editor.putString("firstname", jsonObject.getString("firstname"));
                                            editor.putString("lastname", jsonObject.getString("lastname"));
                                            editor.apply();
                                            Intent intent = new Intent(LoginActivity.this, UserMainActivity.class);
                                            LoginActivity.this.startActivity(intent);
                                            finish();

                                        } else {
                                            AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                                            builder.setMessage("Sign in Failed")
                                                    .setNegativeButton("Retry", null)
                                                    .create()
                                                    .show();
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            };

                            LoginRequest loginRequest = new LoginRequest(email, password, responseListener);
                            RequestQueue queue = Volley.newRequestQueue(LoginActivity.this);
                            queue.add(loginRequest);
                        } else {
                            Toast.makeText(LoginActivity.this, "Please provide account password!", Toast.LENGTH_SHORT).show();
                        }
                    } else {

                        //checking session make autentication with fingerprint or password
                        if (email.equals(preferences.getString("email", "")) && preferences.getBoolean("fingerprint", false)) {

                            //fingerprint autentication
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                KeyguardManager keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
                                FingerprintManager fingerprintManager = (FingerprintManager) getSystemService(FINGERPRINT_SERVICE);

                                if (ActivityCompat.checkSelfPermission(LoginActivity.this, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
                                    return;
                                }

                                // password authentication for API level > M
                                if (!fingerprintManager.isHardwareDetected()) {
                                    Toast.makeText(LoginActivity.this, "Fingerprint authentication permission not enable,please provide your account password!", Toast.LENGTH_LONG).show();
                                    if (password.equals(preferences.getString("password", ""))) {
                                        Intent intent = new Intent(LoginActivity.this, UserMainActivity.class);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        Toast.makeText(LoginActivity.this, "Incorrect password", Toast.LENGTH_SHORT).show();
                                    }

                                    //continue with fingerprint authentication if device has this hardware component
                                } else {
                                    if (!fingerprintManager.hasEnrolledFingerprints())
                                        Toast.makeText(LoginActivity.this, "Register at least one fingerprint in Settings", Toast.LENGTH_SHORT).show();
                                    else {
                                        if (!keyguardManager.isKeyguardSecure())
                                            Toast.makeText(LoginActivity.this, "Lock screen security not enabled in Settings", Toast.LENGTH_SHORT).show();
                                        else
                                            Toast.makeText(LoginActivity.this, "Fingerprint authentication enable, please provide your fingerprint!", Toast.LENGTH_LONG).show();
                                        genKey();
                                        if (cipherInit()) {
                                            FingerprintManager.CryptoObject cryptoObject = new FingerprintManager.CryptoObject(cipher);
                                            FingerprintHandler helper = new FingerprintHandler(LoginActivity.this);
                                            helper.setDeleteVar(false);
                                            helper.startAuthentication(fingerprintManager, cryptoObject);
                                        }
                                    }
                                }
                            } else {

                                //password authentication for API level < M
                                if (password.equals(preferences.getString("password", ""))) {
                                    Intent intent = new Intent(LoginActivity.this, UserMainActivity.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Toast.makeText(LoginActivity.this, "Incorrect password", Toast.LENGTH_SHORT).show();
                                }
                            }
                        } else {
                            //authenticate into another account even if there is a active session and override session
                            Response.Listener<String> responseListener = new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    try {
                                        JSONObject jsonObject = new JSONObject(response);
                                        boolean success = jsonObject.getBoolean("success");

                                        if (success) {
                                            //store session info
                                            editor.putString("email", email);
                                            editor.putBoolean("fingerprint", true);
                                            editor.putString("password", password);
                                            editor.putString("firstname", jsonObject.getString("firstname"));
                                            editor.putString("lastname", jsonObject.getString("lastname"));
                                            editor.putInt("user_id", jsonObject.getInt("user_id"));
                                            editor.apply();
                                            Intent intent = new Intent(LoginActivity.this, UserMainActivity.class);
                                            LoginActivity.this.startActivity(intent);
                                            finish();

                                        } else {
                                            AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                                            builder.setMessage("Sign in Failed")
                                                    .setNegativeButton("Retry", null)
                                                    .create()
                                                    .show();
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            };

                            LoginRequest loginRequest = new LoginRequest(email, password, responseListener);
                            RequestQueue queue = Volley.newRequestQueue(LoginActivity.this);
                            queue.add(loginRequest);
                        }
                    }
                }
            }
        });


        //Sign Up text click handler
        tvSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    /**
     * method is used for checking valid email id format.
     *
     * @param email
     * @return boolean true for valid false for invalid
     */
    private static boolean isEmailValid(String email) {
        boolean isValid = false;

        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        CharSequence inputStr = email;

        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(inputStr);
        if (matcher.matches()) {
            isValid = true;
        }
        return isValid;
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

    //method that return an instance of this activity
    public static LoginActivity getInstance() {
        return loginActivity;
    }

    /**
     * method is used for checking network connection.
     *
     * @return boolean true for valid false for invalid
     */
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }
}
