package com.example.campaign.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.campaign.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

public class otpActivity extends AppCompatActivity {
    private Button mVerifyCodeBtn;
    private EditText otpEdit;
    private FirebaseAuth firebaseAuth;
    private String OTP;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);
        OTP = getIntent().getStringExtra("auth");

        InitializeControllers();

        mVerifyCodeBtn.setOnClickListener(v -> {
            String verification_code = otpEdit.getText().toString();
            progressBar.setVisibility(View.VISIBLE);
            if(!verification_code.isEmpty() && OTP!=null){
                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(OTP , verification_code);
                signIn(credential);
            }else{
                Toast.makeText(otpActivity.this, "Please Enter OTP", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void InitializeControllers() {
        mVerifyCodeBtn=findViewById(R.id.button);
        otpEdit=findViewById(R.id.editOtpNumber);
        progressBar=findViewById(R.id.progressBar1);
    }

    private void signIn(PhoneAuthCredential credential){
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                progressBar.setVisibility(View.GONE);
                startActivity(new Intent(otpActivity.this , registrationActivity.class));
            }else{
                progressBar.setVisibility(View.GONE);
                Toast.makeText(otpActivity.this, "Verification Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

}