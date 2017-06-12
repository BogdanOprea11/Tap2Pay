package com.example.bogdi.bogdan;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.Charset;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

public class PayActivity extends Activity
        implements NfcAdapter.OnNdefPushCompleteCallback,
        NfcAdapter.CreateNdefMessageCallback {

    //The array list to hold messages
    private ArrayList<String> messagesToSend = new ArrayList<>();
    private ArrayList<String> jsonObjects = new ArrayList<>();

    private int position;

    SharedPreferences preferences;
    SharedPreferences preferencesCards;
    private NfcAdapter nfcAdapter;
    Context context;
    static PayActivity payActivity;

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    RecyclerViewAdapter recyclerViewAdapter;

    private KeyStore keyStore;
    private static final String KEY_NAME = "Tap2Pay_Key";
    private Cipher cipher;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //set fullscreen activity without title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_pay);
        context = this;
        payActivity=this;

        recyclerView=(RecyclerView) findViewById(R.id.recyclerView);

        layoutManager =new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        recyclerViewAdapter=new RecyclerViewAdapter(this,jsonObjects);
        recyclerView.setAdapter(recyclerViewAdapter);
        addCards();

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if (nfcAdapter != null) {
            //check if NFC is enabled continue and if not show a dialog that prompt you a message to enable NFC
            if (nfcAdapter.isEnabled()) {
                //This will refer back to createNdefMessage for what it will send
                nfcAdapter.setNdefPushMessageCallback(this, this);

                //This will be called if the message is sent successfully
                nfcAdapter.setOnNdefPushCompleteCallback(this, this);
            } else {
                alertNFCDissabled();
                return;
            }
        } else {
            Toast.makeText(this, "NFC is not Available", Toast.LENGTH_SHORT).show();
        }

        if (!isNetworkAvailable()) {
            alertNetworkDissabled();
            return;
        }
    }

    //Save our array lists of messages, if the user navigates away
    @Override
    public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putStringArrayList("messageToSend", messagesToSend);
    }

    //Load our array lists of messages when user navigates back
    @Override
    public void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        messagesToSend = savedInstanceState.getStringArrayList("messageToSend");
    }

    //This will be called when another NFC capable device is detected
    @Override
    public NdefMessage createNdefMessage(NfcEvent event) {
        if (messagesToSend.size() == 0) {
            return null;
        }
        NdefRecord[] recordsToAttach = createRecords();
        return new NdefMessage(recordsToAttach);
    }

    //This method is called when the system detects that our message was successfully sent
    @Override
    public void onNdefPushComplete(NfcEvent event) {
        messagesToSend.clear();
    }

    @Override
    public void onResume() {
        super.onResume();
        handleNfcIntent(getIntent());
    }

    @Override
    public void onNewIntent(Intent intent) {
        handleNfcIntent(intent);
    }

    //method that create records for Ndef Message
    public NdefRecord[] createRecords() {
        NdefRecord[] records = new NdefRecord[messagesToSend.size()];
        for (int i = 0; i < messagesToSend.size(); i++) {
            byte[] payload = messagesToSend.get(i).getBytes(Charset.forName("UTF-8"));

            NdefRecord record = NdefRecord.createMime("text/plain", payload);
            records[i] = record;
        }
        return records;
    }

    //method used to handle the NFC intent received
    private void handleNfcIntent(Intent NfcIntent) {
    }

    /**
     * method that gets cards from database
     *
     * @return every card data and save it in jsonObjects
     */
    public void addCards() {
        preferencesCards = getSharedPreferences("cards", Context.MODE_PRIVATE);

        try {
            JSONArray jsonarray = new JSONArray(preferencesCards.getString("cardsString", ""));
            for (int i = 0; i < jsonarray.length(); i++) {
                JSONObject jsonobject = jsonarray.getJSONObject(i);
                JSONObject json = new JSONObject();
                try {
                    json.put("CARD_NUMBER", jsonobject.getString("CARD_NUMBER"));
                    json.put("EXP_DATE", jsonobject.getString("EXP_DATE"));
                    json.put("CVC", jsonobject.getString("CVC"));
                    json.put("card_name", jsonobject.getString("card_name"));
                    json.put("Sign", jsonobject.getString("Sign"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                jsonObjects.add(json.toString());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void chooseCards(int position,boolean delete){
        if(delete){
            delete();
        }else {
            messagesToSend.add(jsonObjects.get(position));
        }
    }

    public void delete(){
        preferences = getSharedPreferences("login", Context.MODE_PRIVATE);
        int user_id = preferences.getInt("user_id", 0);
        Toast.makeText(context,"Am intrat in delete!",Toast.LENGTH_SHORT).show();

//        Response.Listener<String> responseListener = new Response.Listener<String>() {
//            @Override
//            public void onResponse(String response) {
//                try {
//                    JSONObject jsonResponse = new JSONObject(response);
//                    boolean success = jsonResponse.getBoolean("success");
//
//                    if (success) {
//                        Toast.makeText(context,"Card deleted!",Toast.LENGTH_SHORT).show();
//                        UserMainActivity.getUserMainActivity().getCards();
//                        recreate();
//                    } else {
//                        AlertDialog.Builder builder = new AlertDialog.Builder(PayActivity.this);
//                        builder.setMessage("Delete card failed")
//                                .setNegativeButton("Retry", null)
//                                .create()
//                                .show();
//                    }
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//        };
//
//        DeleteCardRequest deleteCardRequest = new DeleteCardRequest(user_id,getCardNumber(jsonObjects.get(position)), responseListener);
//        RequestQueue queue = Volley.newRequestQueue(PayActivity.this);
//        queue.add(deleteCardRequest);
    }

    public String getCardNumber(String data){
        try {
            JSONObject jsonObject=new JSONObject(data);
            return jsonObject.get("CARD_NUMBER").toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "";
    }

    public void setPosition(int position) {
        this.position = position;
    }

    /**
     * method that create a dialog that prompt a message to enable NFC.
     *
     * @return alert dialiog
     */
    public void alertNFCDissabled() {
        AlertDialog.Builder builder = new AlertDialog.Builder(PayActivity.this);
        builder.setMessage("NFC is not enabled!")
                .setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        recreate();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        UserMainActivity.getUserMainActivity().finish();
                        Intent intent = new Intent(PayActivity.this, UserMainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                })
                .create()
                .show();
    }

    /**
     * method that create a dialog that prompt a message to enable NFC.
     *
     * @return alert dialiog
     */
    public void alertNetworkDissabled() {
        AlertDialog.Builder builder = new AlertDialog.Builder(PayActivity.this);
        builder.setMessage("Internet is not enabled!")
                .setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        recreate();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        UserMainActivity.getUserMainActivity().finish();
                        Intent intent = new Intent(PayActivity.this, UserMainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                })
                .create()
                .show();
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

    public static PayActivity getpayActivity(){
        return payActivity;
    }

    //method that provide Fingerprint authentication
    public void authenticationFingerprint() {
        //fingerprint autentication
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            KeyguardManager keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
            FingerprintManager fingerprintManager = (FingerprintManager) getSystemService(FINGERPRINT_SERVICE);

            if (ActivityCompat.checkSelfPermission(PayActivity.this, android.Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            // delete account for API level > M if device does not provide hardware for fingerprint
            if (!fingerprintManager.isHardwareDetected()) {
                delete();
                //continue with fingerprint authentication if device has this hardware component
            } else {
                if (!fingerprintManager.hasEnrolledFingerprints())
                    Toast.makeText(PayActivity.this, "Register at least one fingerprint in Settings", Toast.LENGTH_SHORT).show();
                else {
                    if (!keyguardManager.isKeyguardSecure())
                        Toast.makeText(PayActivity.this, "Lock screen security not enabled in Settings", Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(PayActivity.this, "Please provide your fingerprint, to succeed deleting your card!", Toast.LENGTH_SHORT).show();
                    genKey();
                    if (cipherInit()) {
                        FingerprintManager.CryptoObject cryptoObject = new FingerprintManager.CryptoObject(cipher);
                        FingerprintHandler helper = new FingerprintHandler(PayActivity.this);
                        helper.setDeleteVar(false);
                        helper.setDeleteCard(true);
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
}