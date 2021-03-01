package com.example.campaign.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.campaign.R;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;




import java.util.concurrent.TimeUnit;

public class signUpActivity extends AppCompatActivity {
    private FirebaseAuth Auth;
    private EditText phoneNumberEdit;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallBacks;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        Button sendOTPBtn;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        progressBar=findViewById(R.id.progressBar1);
        sendOTPBtn=findViewById(R.id.button);
        phoneNumberEdit=findViewById(R.id.editTextPhone);

        Auth = FirebaseAuth.getInstance();
        sendOTPBtn.setOnClickListener(v -> {
            String phone="+256"+Integer.parseInt(phoneNumberEdit.getText().toString());
            Log.d("phoneNumber",phone);
            progressBar.setVisibility(View.VISIBLE);

            if (!phone.isEmpty()){
                PhoneAuthOptions options = PhoneAuthOptions.newBuilder(Auth)
                        .setPhoneNumber(phone)
                        .setTimeout(60L , TimeUnit.SECONDS)
                        .setActivity(signUpActivity.this)
                        .setCallbacks(mCallBacks)
                        .build();
                PhoneAuthProvider.verifyPhoneNumber(options);

            }else{
                Toast.makeText(signUpActivity.this,"please enter valid phone Number",Toast.LENGTH_LONG).show();
            }
        });

        phoneNumberEdit.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    String phone="+256"+Integer.parseInt(phoneNumberEdit.getText().toString());
                    Log.d("phoneNumber",phone);
                    progressBar.setVisibility(View.VISIBLE);

                    if (!phone.isEmpty()){
                        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(Auth)
                                .setPhoneNumber(phone)
                                .setTimeout(60L , TimeUnit.SECONDS)
                                .setActivity(signUpActivity.this)
                                .setCallbacks(mCallBacks)
                                .build();
                        PhoneAuthProvider.verifyPhoneNumber(options);

                    }else{
                        Toast.makeText(signUpActivity.this,"please enter valid phone Number",Toast.LENGTH_LONG).show();
                    }
                    return true;
                }
                return false;
            }
        });

        mCallBacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                signIn(phoneAuthCredential);

            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                Toast.makeText(signUpActivity.this,e.getMessage(),Toast.LENGTH_LONG).show();
                progressBar.setVisibility(View.GONE);
            }
            
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = Auth.getCurrentUser();
        if (user !=null){
            sendToChats();
        }
    }
    private void sendToChats(){
        Intent mainIntent = new Intent(signUpActivity.this , MainActivity.class);
        startActivity(mainIntent);
        finish();
    }

    private void signIn(PhoneAuthCredential credential){
        Auth.signInWithCredential(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                Intent registration = new Intent(signUpActivity.this , registrationActivity.class);
                startActivity(registration);
                progressBar.setVisibility(View.GONE);
                finish();
            }else{
                Intent otpIntent = new Intent(signUpActivity.this , otpActivity.class);
                startActivity(otpIntent);
                Toast.makeText(signUpActivity.this,task.getException().getMessage(),Toast.LENGTH_LONG).show();
                progressBar.setVisibility(View.GONE);

            }
        });
    }
}