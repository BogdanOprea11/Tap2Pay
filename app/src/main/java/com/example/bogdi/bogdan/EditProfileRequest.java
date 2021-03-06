package com.example.bogdi.bogdan;


import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class EditProfileRequest extends StringRequest {
    private static final String REGISTER_REQUEST_URL = "https://tap2pay.000webhostapp.com/updateProfile.php";
    private Map<String, String> params;

    public EditProfileRequest(String email, String password, String firstName, String lastName, int user_id, Response.Listener<String> listener) {
        super(Request.Method.POST, REGISTER_REQUEST_URL, listener, null);

        params = new HashMap<>();
        params.put("email", email);
        params.put("password", password);
        params.put("firstname", firstName);
        params.put("lastname", lastName);
        params.put("user_id", user_id + "");
    }

    @Override
    public Map<String, String> getParams() {
        return params;
    }
}
