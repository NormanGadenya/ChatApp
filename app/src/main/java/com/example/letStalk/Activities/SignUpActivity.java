package com.example.letStalk.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.broooapps.otpedittext2.OtpEditText;
import com.example.campaign.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignUpActivity extends AppCompatActivity {
    private FirebaseAuth Auth;
    private OtpEditText phoneNumberEdit;

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
        phoneNumberEdit.setOnCompleteListener(value -> {

        });
        progressBar.setVisibility(View.GONE);
        Auth = FirebaseAuth.getInstance();
        sendOTPBtn.setOnClickListener(v -> {
            String phone="+256"+Integer.parseInt(phoneNumberEdit.getText().toString());
            progressBar.setVisibility(View.VISIBLE);

            if (phone.length() != 0){
                Intent otpIntent = new Intent(SignUpActivity.this , OtpActivity.class);
                otpIntent.putExtra("phoneNumber",phone);
                startActivity(otpIntent);

            }else{
                Toast.makeText(SignUpActivity.this,"please enter valid phone Number",Toast.LENGTH_LONG).show();
            }
        });

        phoneNumberEdit.setOnKeyListener((v, keyCode, event) -> {
            // If the event is a key-down event on the "enter" button
            if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                    (keyCode == KeyEvent.KEYCODE_ENTER)) {
                String phone="+256"+Integer.parseInt(phoneNumberEdit.getOtpValue());
                Log.d("phoneNumber",phone);
                progressBar.setVisibility(View.VISIBLE);

                if (!phone.isEmpty()){
                    Intent otpIntent = new Intent(SignUpActivity.this , OtpActivity.class);
                    otpIntent.putExtra("phoneNumber",phone);
                    startActivity(otpIntent);
                    progressBar.setVisibility(View.GONE);

                }else{
                    Toast.makeText(SignUpActivity.this,"please enter valid phone Number",Toast.LENGTH_LONG).show();
                }
                return true;
            }
            return false;
        });

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
        Intent mainIntent = new Intent(SignUpActivity.this , RegistrationActivity.class);
        startActivity(mainIntent);
        finish();
    }

}