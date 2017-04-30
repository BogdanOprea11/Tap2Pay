package com.example.bogdi.bogdan;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.vinaygaba.creditcardview.CreditCardView;

public class CreditCard extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_credit_card);

        CreditCardView creditCardView= new CreditCardView(this);
    }
}
