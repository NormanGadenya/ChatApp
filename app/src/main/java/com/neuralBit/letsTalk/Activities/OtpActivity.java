package com.neuralBit.letsTalk.Activities;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.broooapps.otpedittext2.OtpEditText;
import com.example.campaign.R;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;


public class OtpActivity extends AppCompatActivity {
    private Button mVerifyCodeBtn,resendCodeBtn;
    private OtpEditText otpEdit;
    private FirebaseAuth firebaseAuth;
    private String phoneNumber,verificationId;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallBacks;
    private PhoneAuthCredential credential;
    public static final String TAG="OTPActivity";
    private static final String FORMAT = "%02d:%02d";
    private TextView countDownTimer;
    private CountDownTimer countDT;
    private ProgressBar progressBar;
    private String code;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);
        InitializeControllers();
        requestCode();
        otpEdit.setOnCompleteListener(value -> {
            String verificationCode = otpEdit.getText().toString();
            verifyOtp(verificationCode);
        });

        resendCodeBtn.setOnClickListener(I->{
           requestCode();
           resendCodeBtn.setVisibility(GONE);
           mVerifyCodeBtn.setVisibility(VISIBLE);
        });

        mVerifyCodeBtn.setOnClickListener(v -> {
            String verificationCode = otpEdit.getText().toString();
            verifyOtp(verificationCode);
        });


    }
    private void countDownTimer(){
         countDT=new CountDownTimer(120000, 1000) {

            public void onTick(long millisUntilFinished) {
                if(millisUntilFinished<60000 && code==null){
                    resendCodeBtn.setVisibility(VISIBLE);
                }
                countDownTimer.setText(""+String.format(FORMAT,
                        TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) - TimeUnit.HOURS.toMinutes(
                                TimeUnit.MILLISECONDS.toHours(millisUntilFinished)),
                        TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) - TimeUnit.MINUTES.toSeconds(
                                TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished))));

            }

            public void onFinish() {
                resendCodeBtn.setVisibility(VISIBLE);
                mVerifyCodeBtn.setVisibility(GONE);
            }
        };
        countDT.start();

    }

    public void requestCode(){
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(firebaseAuth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L , TimeUnit.SECONDS)
                .setActivity(OtpActivity.this)
                .setCallbacks(mCallBacks)
                .build();
        try{
            PhoneAuthProvider.verifyPhoneNumber(options);
            countDownTimer.setVisibility(VISIBLE);
            countDownTimer();

        }catch(Exception e){
            Log.e(TAG, "requestCode: ",e.fillInStackTrace() );
        }
    }
    private void verifyOtp(String verificationCode){
        if (!verificationCode.isEmpty()){
            if(verificationId!=null){
                try{
                    progressBar.setVisibility(VISIBLE);
                    verifyPhoneNumberWithCode(verificationId,verificationCode);
                    signIn(credential);
                }catch(Exception e){
                    progressBar.setVisibility(GONE);
                    Log.e(TAG, "verifyOtp: ",e.fillInStackTrace() );
                }
            }
        }else{
            Toast.makeText(OtpActivity.this, "Please Enter OTP", Toast.LENGTH_SHORT).show();
        }

    }

    private void InitializeControllers() {
        resendCodeBtn=findViewById(R.id.resendButton);
        mVerifyCodeBtn=findViewById(R.id.verifyButton);
        otpEdit=findViewById(R.id.editOtpNumber);
        progressBar=findViewById(R.id.progressBarOTP);
        countDownTimer=findViewById(R.id.timer);
        mCallBacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                code = phoneAuthCredential.getSmsCode();
                countDownTimer.setVisibility(GONE);
                otpEdit.setText(code);
                progressBar.setVisibility(GONE);
                signIn(phoneAuthCredential);

            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                countDT.cancel();
                Toast.makeText(getApplicationContext(),e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                countDownTimer.setVisibility(GONE);
                resendCodeBtn.setVisibility(VISIBLE);
                mVerifyCodeBtn.setVisibility(GONE);

            }

            @Override
            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
                verificationId=s;
                countDownTimer.setVisibility(GONE);

            }
        };
        otpEdit=findViewById(R.id.editOtpNumber);
        phoneNumber=getIntent().getStringExtra("phoneNumber");
        firebaseAuth = FirebaseAuth.getInstance();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        countDT.cancel();
    }

    private void signIn(PhoneAuthCredential credential){
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                countDT.cancel();
                Intent i = new Intent(OtpActivity.this, RegistrationActivity.class);
                startActivity(i);
                finish();
            }else{
                progressBar.setVisibility(GONE);
            }
        });

    }

    private void verifyPhoneNumberWithCode(String verificationId, String code) {

         credential = PhoneAuthProvider.getCredential(verificationId, code);
    }

}