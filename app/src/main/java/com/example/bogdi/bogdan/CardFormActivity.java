package com.example.bogdi.bogdan;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.craftman.cardform.Card;
import com.craftman.cardform.CardForm;
import com.craftman.cardform.OnPayBtnClickListner;

import org.w3c.dom.Text;

public class CardFormActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_form);

        CardForm cardForm= (CardForm) findViewById(R.id.cardform);
        TextView txtDes= (TextView) findViewById(R.id.payment_amount);
        Button btnPay =(Button) findViewById(R.id.btn_pay);

        txtDes.setText("$5000");
        btnPay.setText(String.format("Payer %s",txtDes.getText()));

        cardForm.setPayBtnClickListner(new OnPayBtnClickListner(){

            @Override
            public void onClick(Card card){
                Toast.makeText(CardFormActivity.this,""+card.getName(),Toast.LENGTH_SHORT).show();
            }
        });
    }
}
