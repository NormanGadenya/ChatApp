package com.example.letStalk.Activities;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.biometrics.BiometricPrompt;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.widget.Toast;

import com.example.campaign.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class FingerprintActivity extends AppCompatActivity {
    private BiometricPrompt.AuthenticationCallback authenticationCallback;
    private CancellationSignal cancellationSignal;
    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fingerprint);

        if (checkBiometricSupport()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                authenticationCallback = new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationError(int errorCode, CharSequence errString) {
                        super.onAuthenticationError(errorCode, errString);
                        Toast.makeText(getApplicationContext(),errString,Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                        super.onAuthenticationSucceeded(result);
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        if (user != null) {
                            Intent mainIntent = new Intent(FingerprintActivity.this, MainActivity.class);
                            startActivity(mainIntent);
                            finish();
                        }else{
                            finishAndRemoveTask();
                        }


                    }
                };


            }

            BiometricPrompt biometricPrompt = new BiometricPrompt.Builder(this)
                    .setTitle("Let's talk")
                    .setSubtitle("Authentication is required")
                    .setDescription("Fingerprint Authentication")
                    .setNegativeButton("Cancel", getMainExecutor(), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    }).build();
            biometricPrompt.authenticate(getCancellationSignal(), getMainExecutor(), authenticationCallback);
        }

    }
    private CancellationSignal getCancellationSignal(){
        cancellationSignal=new CancellationSignal();
        cancellationSignal.setOnCancelListener(new CancellationSignal.OnCancelListener() {
            @Override
            public void onCancel() {
                makeToast("Authentication cancelled");
            }
        });
        return cancellationSignal;
    }

    private void makeToast(String text){
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
    }

    private Boolean checkBiometricSupport(){

        FingerprintManager fingerprintManager = (FingerprintManager) this.getSystemService(Context.FINGERPRINT_SERVICE);
        if (!fingerprintManager.isHardwareDetected()) {
            return false;
        } else if (!fingerprintManager.hasEnrolledFingerprints()) {
            return false;
        } else {
            // Everything is ready for fingerprint authentication
            return true;
        }

    }
}