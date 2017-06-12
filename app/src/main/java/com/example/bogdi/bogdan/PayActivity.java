package com.example.bogdi.bogdan;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
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

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class PayActivity extends Activity
        implements NfcAdapter.OnNdefPushCompleteCallback,
        NfcAdapter.CreateNdefMessageCallback {

    //The array list to hold messages
    private ArrayList<String> messagesToSend = new ArrayList<>();
    private ArrayList<String> jsonObjects = new ArrayList<>();

    SharedPreferences preferencesCards;
    private NfcAdapter nfcAdapter;
    Context context;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //set fullscreen activity without title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_pay);
        context = this;

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
    private void addCards() {
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

        messagesToSend.add(jsonObjects.get(0));
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
}