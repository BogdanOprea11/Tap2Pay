package com.example.bogdi.bogdan;


import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.CancellationSignal;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;
import android.Manifest;

@RequiresApi(api = Build.VERSION_CODES.M)
public class FingerprintHandler extends FingerprintManager.AuthenticationCallback {

    private Context context;
    private boolean deleteVar;
    private boolean deleteCard;

    protected FingerprintHandler(Context context) {
        this.context = context;
    }

    protected void startAuthentication(FingerprintManager fingerprintManager, FingerprintManager.CryptoObject cryptoObject) {
        CancellationSignal cenCancellationSignal = new CancellationSignal();
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED)
            return;
        fingerprintManager.authenticate(cryptoObject, cenCancellationSignal, 0, this, null);

    }

    @Override
    public void onAuthenticationFailed() {
        super.onAuthenticationFailed();
        Toast.makeText(context, "Fingerprint Authentication failed!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
        super.onAuthenticationSucceeded(result);
        if (!deleteVar && !deleteCard) {
            context.startActivity(new Intent(context, UserMainActivity.class));
            LoginActivity.getInstance().finish();
        } else {
            if (deleteVar) {
                UserMainActivity.getUserMainActivity().deleteProfile();
            }
            if (deleteCard) {
                PayActivity.getpayActivity().delete();
            }
        }
    }

    public void setDeleteVar(boolean deleteVar) {
        this.deleteVar = deleteVar;
    }

    public void setDeleteCard(boolean deleteCard) {
        this.deleteCard = deleteCard;
    }
}