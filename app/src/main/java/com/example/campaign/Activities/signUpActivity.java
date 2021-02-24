package com.example.campaign.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
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
    private Button sendOTPBtn;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallBacks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        sendOTPBtn=findViewById(R.id.button);
        phoneNumberEdit=findViewById(R.id.editTextPhone);
        Auth = FirebaseAuth.getInstance();
        sendOTPBtn.setOnClickListener(v -> {
            String phoneNumber = phoneNumberEdit.getText().toString();

            if (!phoneNumber.isEmpty()){
                PhoneAuthOptions options = PhoneAuthOptions.newBuilder(Auth)
                        .setPhoneNumber(phoneNumber)
                        .setTimeout(60L , TimeUnit.SECONDS)
                        .setActivity(signUpActivity.this)
                        .setCallbacks(mCallBacks)
                        .build();
                PhoneAuthProvider.verifyPhoneNumber(options);

            }else{
                Toast.makeText(signUpActivity.this,"please enter valid phone Number",Toast.LENGTH_LONG).show();
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
            }

//            @Override
//            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
//                super.onCodeSent(s, forceResendingToken);
//
//                //sometime the code is not detected automatically
//                //so user has to manually enter the code
//
//                new Handler().postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        Intent otpIntent = new Intent(signUpActivity.this , otpActivity.class);
//                        otpIntent.putExtra("auth" , s);
//                        startActivity(otpIntent);
//                    }
//                }, 10000);
//
//            }
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
                finish();
            }else{
                Intent otpIntent = new Intent(signUpActivity.this , otpActivity.class);
                startActivity(otpIntent);
                Toast.makeText(signUpActivity.this,task.getException().getMessage(),Toast.LENGTH_LONG).show();

            }
        });
    }
}