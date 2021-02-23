package com.example.campaign;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

public class otpActivity extends AppCompatActivity {
    private Button mVerifyCodeBtn;
    private EditText otpEdit;
    private FirebaseAuth firebaseAuth;
    private String OTP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);
        OTP = getIntent().getStringExtra("auth");
        InitializeControllers();

        mVerifyCodeBtn.setOnClickListener(v -> {
            String verification_code = otpEdit.getText().toString();
            if(!verification_code.isEmpty()){
                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(OTP , verification_code);
                signIn(credential);
            }else{
                Toast.makeText(otpActivity.this, "Please Enter OTP", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void InitializeControllers() {
        mVerifyCodeBtn=findViewById(R.id.Otp_vbutton);
        otpEdit=findViewById(R.id.editOtpNumber);
    }

    private void signIn(PhoneAuthCredential credential){
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                startActivity(new Intent(otpActivity.this , registrationActivity.class));
            }else{
                Toast.makeText(otpActivity.this, "Verification Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

}