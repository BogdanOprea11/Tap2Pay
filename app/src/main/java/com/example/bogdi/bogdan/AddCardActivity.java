package com.example.bogdi.bogdan;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AddCardActivity extends AppCompatActivity {
    //declare shared preferences
    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    //declare static variable for context
    static Context context;
    String cardType;
    //CreditCard creditCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //initialize context
        context = this;

        //set fullscreen activity without title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        //initialize preferences;
        preferences = getSharedPreferences("login", Context.MODE_PRIVATE);
        editor = preferences.edit();

        final int user_id = preferences.getInt("user_id", 0);

        setContentView(R.layout.activity_add_card);

        final EditText etCardNumber= (EditText) findViewById(R.id.et_cardNumber);
        final EditText etExpirationDate = (EditText) findViewById(R.id.et_cardExpDate);
        final EditText etCVC = (EditText) findViewById(R.id.etCVC);
        final EditText etCardName = (EditText) findViewById(R.id.et_cardName);

        final Button btSubmitCard = (Button) findViewById(R.id.btSubmitCard);

        btSubmitCard.setOnClickListener(new View.OnClickListener() {
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
                final String card_name = etCardName.getText().toString();
                boolean allInformation = false;

                if (!isValidCardNumber(cardNumber)) {
                    Toast.makeText(AddCardActivity.this, "Please provide 16 digits number for Card Number", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    String auxiliar="";
                    for (int i = 0; i < cardNumber.length(); i=i+4) {
                        auxiliar+=cardNumber.charAt(i)+ "";
                        auxiliar+=cardNumber.charAt(i+1)+ "";
                        auxiliar+=cardNumber.charAt(i+2)+ "";
                        auxiliar+=cardNumber.charAt(i+3)+ "";
                        auxiliar+="     ";
                    }
                    etCardNumber.setText(auxiliar);
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
                    //creditCard=new CreditCard(Integer.parseInt(cardNumber),card_name,expirationDate,Integer.parseInt(cvc));
                    Response.Listener<String> responseListener = new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONObject jsonResponse = new JSONObject(response);
                                boolean success = jsonResponse.getBoolean("success");

                                if (success) {
                                    UserMainActivity.getUserMainActivity().finish();
                                    saveFrameLayout((LinearLayout) findViewById(R.id.linearLayoutCardView), cardNumber);
                                    Toast.makeText(AddCardActivity.this, cardType, Toast.LENGTH_SHORT).show();
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

                    AddCardRequest addCardRequest = new AddCardRequest(cardNumber, expirationDate, cvc, user_id, card_name, responseListener);
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
    private boolean isValidCardNumber(String cardNumber) {
        final String PATTERN_VISA = "^4[0-9]{12}(?:[0-9]{3})?$";

        final String PATTERN_MASTER_CARD = "^(?:5[1-5][0-9]{2}|222[1-9]|22[3-9][0-9]|2[3-6][0-9]{2}|27[01][0-9]|2720)[0-9]{12}$";

        final String PATTERN_AMERICAN_EXPRESS = "^3[47][0-9]{13}$";

        final String PATTERN_DISCOVER = "^6(?:011|5[0-9]{2})[0-9]{12}$";

        if (Pattern.compile(PATTERN_VISA).matcher(cardNumber).matches()) {
            this.cardType = "VISA";
            return true;
        } else if (Pattern.compile(PATTERN_MASTER_CARD).matcher(cardNumber).matches()) {
            this.cardType = "MASTERCARD";
            return true;
        } else if (Pattern.compile(PATTERN_AMERICAN_EXPRESS).matcher(cardNumber).matches()) {
            this.cardType = "AMERICAN_EXPRESS";
            return true;
        } else if (Pattern.compile(PATTERN_DISCOVER).matcher(cardNumber).matches()) {
            this.cardType = "DISCOVER";
            return true;
        } else
            return false;
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

    private static File getOutputMediaFile(String card_number) {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory()
                + "/Android/data/"
                + context.getPackageName()
                + "/Files");

        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmm").format(new Date());
        File mediaFile;
        //String mImageName = "MI_" + timeStamp + ".jpg";
        String mImageName = card_number + ".jpg";
        mediaFile = new File(mediaStorageDir.getPath() + File.separator + mImageName);
        return mediaFile;
    }

    public static void saveFrameLayout(LinearLayout linearLayout, String card_number) {
        linearLayout.setDrawingCacheEnabled(true);
        linearLayout.buildDrawingCache(true);
        Bitmap cache = Bitmap.createBitmap(linearLayout.getDrawingCache());
        linearLayout.setDrawingCacheEnabled(false);

        try {
            File pictureFile = getOutputMediaFile(card_number);
            FileOutputStream fos = new FileOutputStream(pictureFile);
            cache.compress(Bitmap.CompressFormat.PNG, 90, fos);
            fos.close();
        } catch (Exception e) {
            // TODO: handle exception
        } finally {
            linearLayout.destroyDrawingCache();
        }
    }

}
