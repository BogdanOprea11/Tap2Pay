package com.example.bogdi.bogdan;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class DeleteCardRequest extends StringRequest {
    private static final String REGISTER_REQUEST_URL = "https://tap2pay.000webhostapp.com/deleteCard.php";
    private Map<String, String> params;

    public DeleteCardRequest(int user_id, String CARD_NUMBER, Response.Listener<String> listener) {
        super(Request.Method.POST, REGISTER_REQUEST_URL, listener, null);

        params = new HashMap<>();
        params.put("user_id", user_id + "");
        params.put("CARD_NUMBER", CARD_NUMBER);
    }

    @Override
    public Map<String, String> getParams() {
        return params;
    }
}
