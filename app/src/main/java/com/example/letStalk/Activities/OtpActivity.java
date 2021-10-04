package com.example.letStalk.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.broooapps.otpedittext2.OtpEditText;
import com.example.campaign.R;
import com.virgilsecurity.common.callback.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;
import com.virgilsecurity.android.common.callback.OnGetTokenCallback;
import com.virgilsecurity.android.ethree.interaction.EThree;
import com.virgilsecurity.common.callback.OnResultListener;

import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;



public class OtpActivity extends AppCompatActivity {
    private Button mVerifyCodeBtn,resendCodeBtn;
    private OtpEditText otpEdit;
    private FirebaseAuth firebaseAuth;
    private String phoneNumber,verificationId;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallBacks;
    private PhoneAuthCredential credential;
    private Handler handler=new Handler();
    private Runnable runnable;
    public static final String TAG="OTPActivity";
    private static final String FORMAT = "%02d:%02d";
    private TextView countDownTimer;
    private CountDownTimer countDT;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);
        otpEdit=findViewById(R.id.editOtpNumber);

        phoneNumber=getIntent().getStringExtra("phoneNumber");
        firebaseAuth = FirebaseAuth.getInstance();
        InitializeControllers();
        requestCode();
        otpEdit.setOnCompleteListener(value -> {
            String verificationCode = otpEdit.getText().toString();
            verifyOtp(verificationCode);
        });

        resendCodeBtn.setOnClickListener(I->{
           requestCode();
           resendCodeBtn.setVisibility(View.GONE);
           mVerifyCodeBtn.setVisibility(View.VISIBLE);
        });

        mVerifyCodeBtn.setOnClickListener(v -> {

            String verificationCode = otpEdit.getText().toString();
            verifyOtp(verificationCode);
        });


    }
    private void countDownTimer(){
         countDT=new CountDownTimer(120000, 1000) { // adjust the milli seconds here

            public void onTick(long millisUntilFinished) {

                countDownTimer.setText(""+String.format(FORMAT,
                        TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) - TimeUnit.HOURS.toMinutes(
                                TimeUnit.MILLISECONDS.toHours(millisUntilFinished)),
                        TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) - TimeUnit.MINUTES.toSeconds(
                                TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished))));
                Log.d(TAG, "onTick: ");
            }

            public void onFinish() {
                resendCodeBtn.setVisibility(View.VISIBLE);
                mVerifyCodeBtn.setVisibility(View.GONE);
            }
        };
        countDT.start();
        handler.post(runnable);

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
            countDownTimer.setVisibility(View.VISIBLE);
            countDownTimer();

        }catch(Exception e){
            Log.e(TAG, "requestCode: ",e.fillInStackTrace() );
        }
    }
    private void verifyOtp(String verificationCode){
        if(!verificationCode.isEmpty() && verificationId!=null ){
            try{
                verifyPhoneNumberWithCode(verificationId,verificationCode);
                signIn(credential);
            }catch(Exception e){
                Log.e(TAG, "verifyOtp: ",e.fillInStackTrace() );
            }

        }else{
            Toast.makeText(OtpActivity.this, "Please Enter OTP", Toast.LENGTH_SHORT).show();
        }
    }

    private void InitializeControllers() {
        resendCodeBtn=findViewById(R.id.resendButton);
        mVerifyCodeBtn=findViewById(R.id.verifyButton);
        otpEdit=findViewById(R.id.editOtpNumber);
        countDownTimer=findViewById(R.id.timer);
        mCallBacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                final String code = phoneAuthCredential.getSmsCode();
                handler.removeCallbacks(runnable);
                countDownTimer.setVisibility(View.GONE);
                otpEdit.setText(code);
                signIn(phoneAuthCredential);

            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
//                Toast.makeText(OtpActivity.this,e.getMessage(),Toast.LENGTH_LONG).show();
                handler.removeCallbacks(runnable);
                countDownTimer.setVisibility(View.GONE);
                resendCodeBtn.setVisibility(View.VISIBLE);
                mVerifyCodeBtn.setVisibility(View.GONE);

            }

            @Override
            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
                verificationId=s;
                countDownTimer.setVisibility(View.GONE);

            }
        };
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        countDT.cancel();
    }

    private void signIn(PhoneAuthCredential credential){
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                countDT.cancel();
                Intent i = new Intent(OtpActivity.this,RegistrationActivity.class);
                startActivity(i);
                finish();
            }
        });

    }



    private void verifyPhoneNumberWithCode(String verificationId, String code) {
        // [START verify_with_code]
         credential = PhoneAuthProvider.getCredential(verificationId, code);
        // [END verify_with_code]
    }

}