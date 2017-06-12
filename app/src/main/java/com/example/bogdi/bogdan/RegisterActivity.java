package com.example.bogdi.bogdan;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {
    SharedPreferences preferences;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //set fullscreen activity without title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_register);

        final EditText etEmail = (EditText) findViewById(R.id.etEmail);
        final EditText etFirstName = (EditText) findViewById(R.id.etFirstName);
        final EditText etLastName = (EditText) findViewById(R.id.etLastName);
        final EditText etPassword = (EditText) findViewById(R.id.etPassword);
        final EditText etRepeatPassword = (EditText) findViewById(R.id.etRepeatPassword);

        final Button btRegister = (Button) findViewById(R.id.btRegister);

        btRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isNetworkAvailable()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                    builder.setMessage("Please activate your Internet connection")
                            .setNegativeButton("Retry", null)
                            .create()
                            .show();
                    return;
                }

                final String email = etEmail.getText().toString();
                final String firstName = etFirstName.getText().toString();
                final String lastName = etLastName.getText().toString();
                final String password = etPassword.getText().toString();
                final String repeatPassword = etRepeatPassword.getText().toString();
                boolean allInformation = false;

                if (email.length() == 0) {
                    Toast.makeText(RegisterActivity.this, "Please provide an email", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    if (!isEmailValid(email)) {
                        Toast.makeText(RegisterActivity.this, "Please provide an email format", Toast.LENGTH_SHORT).show();
                        return;
                    } else {
                        if (firstName.length() == 0) {
                            Toast.makeText(RegisterActivity.this, "Please provide your First Name", Toast.LENGTH_SHORT).show();
                            return;
                        } else {
                            if (lastName.length() == 0) {
                                Toast.makeText(RegisterActivity.this, "Please provide your Last Name", Toast.LENGTH_SHORT).show();
                                return;
                            } else {
                                if (password.length() == 0) {
                                    Toast.makeText(RegisterActivity.this, "Please provide a password", Toast.LENGTH_SHORT).show();
                                    return;
                                } else {
                                    if (password.length() < 6) {
                                        Toast.makeText(RegisterActivity.this, "Please provide a password that contains minimum 6 characters.", Toast.LENGTH_LONG).show();
                                        return;
                                    } else {
                                        if (!isValidPassword(password)) {
                                            Toast.makeText(RegisterActivity.this, "Please provide a password that contains: at least 1 Alphabet and 1 Number.", Toast.LENGTH_LONG).show();
                                            return;
                                        } else {
                                            if (!repeatPassword.equals(password)) {
                                                Toast.makeText(RegisterActivity.this, "Please provide same password as above", Toast.LENGTH_SHORT).show();
                                                return;
                                            } else {
                                                allInformation = true;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (allInformation) {
                    Toast.makeText(RegisterActivity.this, "Success", Toast.LENGTH_SHORT).show();
                    Response.Listener<String> responseListener = new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONObject jsonResponse = new JSONObject(response);
                                boolean success = jsonResponse.getBoolean("success");

                                if (success) {
                                    LoginActivity.getInstance().finish();
                                    createSession(email, password);
                                    Intent intent = new Intent(RegisterActivity.this, UserMainActivity.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                                    builder.setMessage("Sign up Failed")
                                            .setNegativeButton("Retry", null)
                                            .create()
                                            .show();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    };

                    RegisterRequest registerRequest = new RegisterRequest(email, password, firstName, lastName, responseListener);
                    RequestQueue queue = Volley.newRequestQueue(RegisterActivity.this);
                    queue.add(registerRequest);
                }
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

    /**
     * method is used for checking valid password format.
     *
     * @param password
     * @return boolean true for valid false for invalid
     */
    private static boolean isValidPassword(String password) {
        boolean isValid = false;

        CharSequence inputStr = password;
        String PASSWORD_PATTERN = "((?=.*\\d)(?=.*[a-z])(?=.*[A-Z]).{6,20})";

        Pattern pattern = Pattern.compile(PASSWORD_PATTERN, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(inputStr);

        if (matcher.matches()) {
            isValid = true;
        }
        return isValid;
    }

    /**
     * method is used for creating a login session.
     *
     * @params email1, password1
     */
    private void createSession(String email1, String password1) {
        final String email = email1;
        final String password = password1;
        preferences = getSharedPreferences("login", Context.MODE_PRIVATE);
        editor = preferences.edit();
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
                        editor.putString("firstname", jsonObject.getString("firstname"));
                        editor.putString("lastname", jsonObject.getString("lastname"));
                        editor.putInt("user_id", jsonObject.getInt("user_id"));
                        editor.apply();

                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
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

        RequestQueue queue = Volley.newRequestQueue(RegisterActivity.this);
        queue.add(loginRequest);
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
