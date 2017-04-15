package com.example.bogdi.bogdan;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class AddCardRequest extends StringRequest{
    private static final String REGISTER_REQUEST_URL="https://tap2pay.000webhostapp.com/addcard.php";
    private Map<String, String> params;

    public AddCardRequest(String cardNumber, String expirationDate, String cvc, int user_id, Response.Listener<String> listener){
        super(Request.Method.POST, REGISTER_REQUEST_URL, listener,null);

        params =new HashMap<>();
        params.put("CARD_NUMBER",cardNumber);
        params.put("EXP_DATE", expirationDate);
        params.put("CVC",cvc);
        params.put("user_id",user_id+"");
    }

    @Override
    public Map<String, String> getParams() {
        return params;
    }
}
