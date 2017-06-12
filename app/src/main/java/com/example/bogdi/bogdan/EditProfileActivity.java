package com.example.bogdi.bogdan;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

public class EditProfileActivity extends AppCompatActivity {
    SharedPreferences preferences;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //set fullscreen activity without title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        preferences = getSharedPreferences("login", Context.MODE_PRIVATE);
        editor = preferences.edit();
        final int user_id = preferences.getInt("user_id", 0);

        setContentView(R.layout.activity_edit_profile);

        final EditText etEmail = (EditText) findViewById(R.id.etChangeEmail);
        final EditText etFirstName = (EditText) findViewById(R.id.etChangeFirstName);
        final EditText etLastName = (EditText) findViewById(R.id.etChangeLastName);
        final EditText etOldPassword = (EditText) findViewById(R.id.etOldPassword);
        final EditText etPassword = (EditText) findViewById(R.id.etChangePassword);
        final EditText etRepeatPassword = (EditText) findViewById(R.id.etChangeRepeatPassword);

        final Button btRegister = (Button) findViewById(R.id.btSubmit);

        //fill fields with current information
        etEmail.setText(preferences.getString("email", ""));
        etFirstName.setText(preferences.getString("firstname", ""));
        etLastName.setText(preferences.getString("lastname", ""));

        btRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isNetworkAvailable()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(EditProfileActivity.this);
                    builder.setMessage("Please activate your Internet connection")
                            .setNegativeButton("Retry", null)
                            .create()
                            .show();
                    return;
                }

                final String email = etEmail.getText().toString();
                final String firstName = etFirstName.getText().toString();
                final String lastName = etLastName.getText().toString();
                final String oldPassword = etOldPassword.getText().toString();
                final String password = etPassword.getText().toString();
                final String repeatPassword = etRepeatPassword.getText().toString();
                boolean allInformation = false;
                boolean partialInformation = false;

                if (email.length() == 0) {
                    Toast.makeText(EditProfileActivity.this, "Please provide an email", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    if (!isEmailValid(email)) {
                        Toast.makeText(EditProfileActivity.this, "Please provide an email format", Toast.LENGTH_SHORT).show();
                        return;
                    } else {
                        if (firstName.length() == 0) {
                            Toast.makeText(EditProfileActivity.this, "Please provide your First Name", Toast.LENGTH_SHORT).show();
                            return;
                        } else {
                            if (lastName.length() == 0) {
                                Toast.makeText(EditProfileActivity.this, "Please provide your Last Name", Toast.LENGTH_SHORT).show();
                                return;
                            } else {
                                partialInformation = true;
                            }
                        }
                    }
                }

                if (partialInformation && (oldPassword.length() != 0 || password.length() != 0 || repeatPassword.length() != 0)) {
                    if (!oldPassword.equals(preferences.getString("password", ""))) {
                        Toast.makeText(EditProfileActivity.this, "Your old password doesn't match", Toast.LENGTH_SHORT).show();
                        return;
                    } else {
                        if (password.length() < 6) {
                            Toast.makeText(EditProfileActivity.this, "Please provide a password that contains minimum 6 characters.", Toast.LENGTH_LONG).show();
                            return;
                        } else {
                            if (!isValidPassword(password)) {
                                Toast.makeText(EditProfileActivity.this, "Please provide a password that contains: at least 1 Alphabet and 1 Number.", Toast.LENGTH_LONG).show();
                                return;
                            } else {
                                if (!repeatPassword.equals(password)) {
                                    Toast.makeText(EditProfileActivity.this, "Please provide same password as above", Toast.LENGTH_SHORT).show();
                                    return;
                                } else {
                                    allInformation = true;
                                }
                            }
                        }
                    }
                }

                if (partialInformation && !allInformation) {
                    Toast.makeText(EditProfileActivity.this, "Success", Toast.LENGTH_SHORT).show();
                    Response.Listener<String> responseListener = new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONObject jsonResponse = new JSONObject(response);
                                boolean success = jsonResponse.getBoolean("success");

                                if (success) {
                                    editor.putString("email", email);
                                    editor.putString("firstname", firstName);
                                    editor.putString("lastname", lastName);
                                    editor.apply();
                                    UserMainActivity.getUserMainActivity().finish();
                                    Intent intent = new Intent(EditProfileActivity.this, UserMainActivity.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(EditProfileActivity.this);
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

                    EditProfileRequest editProfileRequest = new EditProfileRequest(email, preferences.getString("password", ""), firstName, lastName, user_id, responseListener);
                    RequestQueue queue = Volley.newRequestQueue(EditProfileActivity.this);
                    queue.add(editProfileRequest);
                }

                if (partialInformation && allInformation) {
                    Toast.makeText(EditProfileActivity.this, "Success", Toast.LENGTH_SHORT).show();
                    Response.Listener<String> responseListener = new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONObject jsonResponse = new JSONObject(response);
                                boolean success = jsonResponse.getBoolean("success");

                                if (success) {
                                    editor.putString("email", email);
                                    editor.putString("firstname", firstName);
                                    editor.putString("lastname", lastName);
                                    editor.putString("password", password);
                                    editor.apply();
                                    UserMainActivity.getUserMainActivity().finish();
                                    Intent intent = new Intent(EditProfileActivity.this, UserMainActivity.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(EditProfileActivity.this);
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

                    EditProfileRequest editProfileRequest = new EditProfileRequest(email, password, firstName, lastName, user_id, responseListener);
                    RequestQueue queue = Volley.newRequestQueue(EditProfileActivity.this);
                    queue.add(editProfileRequest);
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
