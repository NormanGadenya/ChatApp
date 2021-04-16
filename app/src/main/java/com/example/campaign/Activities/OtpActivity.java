package com.example.campaign.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.campaign.R;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class OtpActivity extends AppCompatActivity {
    private Button mVerifyCodeBtn;
    private EditText otpEdit;
    private FirebaseAuth firebaseAuth;
    private String OTP,phoneNumber,verificationId;
    private ProgressBar progressBar;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallBacks;
    private PhoneAuthCredential credential;
    public static final String TAG="otpActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);
//        OTP = getIntent().getStringExtra("auth");
        otpEdit=findViewById(R.id.editOtpNumber);

        OTP=otpEdit.getText().toString();
        phoneNumber=getIntent().getStringExtra("phoneNumber");
        firebaseAuth = FirebaseAuth.getInstance();
        InitializeControllers();

        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(firebaseAuth)
                                .setPhoneNumber(phoneNumber)
                                .setTimeout(60L , TimeUnit.SECONDS)
                                .setActivity(OtpActivity.this)
                                .setCallbacks(mCallBacks)
                                .build();
        try{
            PhoneAuthProvider.verifyPhoneNumber(options);
        }catch(Exception e){
            Log.d(TAG,"ERROR"+ e.getMessage());
        }


        mVerifyCodeBtn.setOnClickListener(v -> {

            String verificationCode = otpEdit.getText().toString();
            progressBar.setVisibility(View.VISIBLE);
            if(!verificationCode.isEmpty() && verificationId!=null ){
                try{
                    verifyPhoneNumberWithCode(verificationId,verificationCode);
                    signIn(credential);
                }catch(Exception e){
                    Log.d(TAG,"ERROR"+ e.getMessage());
                }

            }else{
                Toast.makeText(OtpActivity.this, "Please Enter OTP", Toast.LENGTH_SHORT).show();
            }
        });


    }

    private void InitializeControllers() {
        mVerifyCodeBtn=findViewById(R.id.button);
        otpEdit=findViewById(R.id.editOtpNumber);
        progressBar=findViewById(R.id.progressBar1);

        mCallBacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                signIn(phoneAuthCredential);

            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                Toast.makeText(OtpActivity.this,e.getMessage(),Toast.LENGTH_LONG).show();
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
                verificationId=s;

            }
        };
    }

    private void signIn(PhoneAuthCredential credential){
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                progressBar.setVisibility(View.GONE);
                startActivity(new Intent(OtpActivity.this , RegistrationActivity.class));
            }else{
                progressBar.setVisibility(View.GONE);
                Toast.makeText(OtpActivity.this, "Verification Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void verifyPhoneNumberWithCode(String verificationId, String code) {
        // [START verify_with_code]
         credential = PhoneAuthProvider.getCredential(verificationId, code);
        // [END verify_with_code]
    }

}