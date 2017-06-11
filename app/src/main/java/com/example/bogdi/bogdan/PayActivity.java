package com.example.bogdi.bogdan;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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

    private ArrayList<String> jsonObjects= new ArrayList<>();

    SharedPreferences preferencesCards;

    //The array list to hold messages
    private ArrayList<String> messagesToSend = new ArrayList<>();
    //private ArrayList<String> messagesReceived = new ArrayList<>();

    //edit text for inserting message
    private EditText etMessage;

    //text views to show message to send and received
    //private TextView tvMessageReceived;
    private TextView tvMessageToSend;

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
        context=this;

        addCards();
        //get layout items
        etMessage = (EditText) findViewById(R.id.txtBoxAddMessage);
        //tvMessageReceived = (TextView) findViewById(R.id.txtMessagesReceived);
        tvMessageToSend = (TextView) findViewById(R.id.txtMessageToSend);

        Button button = (Button) findViewById(R.id.buttonAddMessage);

        button.setText("Send");

        updateTextViews();

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
    }

    //Save our array lists of messages, if the user navigates away
    @Override
    public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putStringArrayList("messageToSend", messagesToSend);
        //savedInstanceState.putStringArrayList("messageReceived", messagesReceived);
    }

    //Load our array lists of messages when user navigates back
    @Override
    public void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        //messagesReceived = savedInstanceState.getStringArrayList("messageReceived");
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
        updateTextViews();
        handleNfcIntent(getIntent());
    }

    @Override
    public void onNewIntent(Intent intent) {
        handleNfcIntent(intent);
    }

    //method used to get text from Edit Text and add to messageToSend array
    public void addMessage(View view) {
        String newMessage = etMessage.getText().toString();
        messagesToSend.add(newMessage);

        etMessage.setText(null);
        updateTextViews();
        Toast.makeText(this, "Message added to send", Toast.LENGTH_SHORT).show();
    }

    //method used for update text views
    private void updateTextViews() {
        //update text view for message that want to send
        tvMessageToSend.setText("Message to Send:\n");
        if (messagesToSend.size() > 0) {
            for (int i = 0; i < messagesToSend.size(); i++) {
                tvMessageToSend.append(messagesToSend.get(i));
                tvMessageToSend.append("\n");
            }
        }

        //update text view for message that was received
//        tvMessageReceived.setText("Message Received: \n");
//        if (messagesReceived.size() > 0) {
//            for (int i = 0; i < messagesReceived.size(); i++) {
//                tvMessageReceived.append(messagesReceived.get(i));
//                tvMessageReceived.append("\n");
//            }
//        }
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
//        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(NfcIntent.getAction())) {
//            Parcelable[] receivedArray = NfcIntent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
//
//            //if something is received we will proceed our computations
//            if (receivedArray != null) {
//                //clear existed messages
//                messagesReceived.clear();
//                NdefMessage receivedMessage = (NdefMessage) receivedArray[0];
//                NdefRecord[] attachedRecords = receivedMessage.getRecords();
//
//                //handle message received
//                for (NdefRecord record : attachedRecords) {
//                    String string = new String(record.getPayload());
//                    messagesReceived.add(string);
//                }
//
//                updateTextViews();
//            } else {
//                Toast.makeText(this, "Received a blank message", Toast.LENGTH_SHORT).show();
//            }
//        }
    }

    private void addCards(){
        preferencesCards = getSharedPreferences("cards", Context.MODE_PRIVATE);

        try {
            JSONArray jsonarray = new JSONArray(preferencesCards.getString("cardsString",""));
            for (int i = 0; i < jsonarray.length(); i++) {
                JSONObject jsonobject = jsonarray.getJSONObject(i);
                JSONObject json = new JSONObject();
                try {
                    json.put("CARD_NUMBER", jsonobject.getString("CARD_NUMBER"));
                    json.put("EXP_DATE", jsonobject.getString("EXP_DATE"));
                    json.put("CVC", jsonobject.getString("CVC"));
                    json.put("card_name", jsonobject.getString("card_name"));
                    json.put("Sign",jsonobject.getString("Sign"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                jsonObjects.add(json.toString());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }


//        for(int i=0;i<jsonObjects.size();i++){
//            Toast.makeText(PayActivity.this,jsonObjects.get(i),Toast.LENGTH_LONG).show();
//        }
        messagesToSend.add(jsonObjects.get(0));
    }

    //method that create a dialog that prompt a message to enable NFC
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


}