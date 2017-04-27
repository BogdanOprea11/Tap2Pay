package com.example.bogdi.bogdan;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
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

public class AddCardActivity extends AppCompatActivity {
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

        setContentView(R.layout.activity_add_card);

        final EditText etCardNumber = (EditText) findViewById(R.id.etCardNumber);
        final EditText etExpirationDate = (EditText) findViewById(R.id.etExpirationDate);
        final EditText etCVC = (EditText) findViewById(R.id.etCVC);

        final Button btRegisterCard = (Button) findViewById(R.id.btRegisterCard);

        btRegisterCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isNetworkAvailable()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(AddCardActivity.this);
                    builder.setMessage("Please activate your Internet connection")
                            .setNegativeButton("Retry", null)
                            .create()
                            .show();
                    return;
                }

                final String cardNumber = etCardNumber.getText().toString();
                final String expirationDate = etExpirationDate.getText().toString();
                final String cvc = etCVC.getText().toString();
                boolean allInformation = false;

                if (!isValidCardNumber(cardNumber)) {
                    Toast.makeText(AddCardActivity.this, "Please provide 16 digits number for Card Number", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    if (!isValidCardExpDate(expirationDate)) {
                        Toast.makeText(AddCardActivity.this, "Please provide MM/YY card expiration date", Toast.LENGTH_SHORT).show();
                        return;
                    } else {
                        if (!isValidCardCVC(cvc)) {
                            Toast.makeText(AddCardActivity.this, "Please provide 3 digits number for CVC", Toast.LENGTH_SHORT).show();
                            return;
                        } else {
                            allInformation = true;
                        }
                    }
                }

                if (allInformation) {
                    Response.Listener<String> responseListener = new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONObject jsonResponse = new JSONObject(response);
                                boolean success = jsonResponse.getBoolean("success");

                                if (success) {
                                    UserMainActivity.getUserMainActivity().finish();
                                    Intent intent = new Intent(AddCardActivity.this, UserMainActivity.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(AddCardActivity.this);
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

                    AddCardRequest addCardRequest = new AddCardRequest(cardNumber, expirationDate, cvc, user_id, responseListener);
                    RequestQueue queue = Volley.newRequestQueue(AddCardActivity.this);
                    queue.add(addCardRequest);
                }
            }
        });

    }


    /**
     * method is used for checking valid password format.
     *
     * @param cardNumber
     * @return boolean true for valid false for invalid
     */
    private static boolean isValidCardNumber(String cardNumber) {
        boolean isValid = false;

        CharSequence inputStr = cardNumber;
        String CARD_NUMBER_PATTERN = "^\\d{16}$";

        Pattern pattern = Pattern.compile(CARD_NUMBER_PATTERN, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(inputStr);

        if (matcher.matches()) {
            isValid = true;
        }
        return isValid;
    }

    /**
     * method is used for checking valid password format.
     *
     * @param cardExpDate
     * @return boolean true for valid false for invalid
     */
    private static boolean isValidCardExpDate(String cardExpDate) {
        boolean isValid = false;

        CharSequence inputStr = cardExpDate;
        String CARD_EXP_DATE_PATTERN = "^\\d{2}/\\d{2}$";

        Pattern pattern = Pattern.compile(CARD_EXP_DATE_PATTERN, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(inputStr);

        if (matcher.matches()) {
            isValid = true;
        }
        return isValid;
    }

    /**
     * method is used for checking valid password format.
     *
     * @param cardCVC
     * @return boolean true for valid false for invalid
     */
    private static boolean isValidCardCVC(String cardCVC) {
        boolean isValid = false;

        CharSequence inputStr = cardCVC;
        String CARD_CVC_PATTERN = "^\\d{3}$";

        Pattern pattern = Pattern.compile(CARD_CVC_PATTERN, Pattern.CASE_INSENSITIVE);
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
