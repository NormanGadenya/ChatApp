package com.neuralBit.letsTalk.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
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
import com.firebase.ui.auth.ui.phone.CountryListSpinner;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.hbb20.CountryCodePicker;

import java.util.Objects;

public class SignUpActivity extends AppCompatActivity {
    private FirebaseAuth Auth;
    private OtpEditText phoneNumberEdit;
    private ProgressBar progressBar;
    public static final String TAG = "SignUpActivity";
    private CountryCodePicker countryCodePicker;
    private String countryCode;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        Button sendOTPBtn;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        sharedPreferences=getSharedPreferences("countryCode",MODE_PRIVATE);
        SharedPreferences.Editor editor=sharedPreferences.edit();
        progressBar=findViewById(R.id.progressBar1);
        sendOTPBtn=findViewById(R.id.button);
        phoneNumberEdit=findViewById(R.id.editTextPhone);
        countryCodePicker=findViewById(R.id.countryCode);
        countryCodePicker.setAutoDetectedCountry(true);
        phoneNumberEdit.setOnCompleteListener(value -> {
        });
        countryCodePicker.setOnCountryChangeListener(new CountryCodePicker.OnCountryChangeListener() {
            @Override
            public void onCountrySelected() {
                countryCode="+" +countryCodePicker.getSelectedCountryCodeAsInt();
            }
        });


        progressBar.setVisibility(View.GONE);
        Auth = FirebaseAuth.getInstance();
        sendOTPBtn.setOnClickListener(v -> {
            if(countryCode==null){
                countryCode="+"+countryCodePicker.getDefaultCountryCode();
            }
            String phone=countryCode+Integer.parseInt(Objects.requireNonNull(phoneNumberEdit.getText()).toString());
            progressBar.setVisibility(View.VISIBLE);

            if (phone.length() != 4){
                editor.putString("CountryCode",countryCode);
                editor.apply();
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
                if(countryCode==null){
                    countryCode="+"+countryCodePicker.getDefaultCountryCode();
                }
                String phone=countryCode+Integer.parseInt(Objects.requireNonNull(phoneNumberEdit.getOtpValue()));
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
        progressBar.setVisibility(View.GONE);
        phoneNumberEdit.setText(null);
        if (user !=null){
            sendToChats();
        }
    }


    private void sendToChats(){
        SharedPreferences sharedPreferences = getSharedPreferences("Settings", MODE_PRIVATE);
        boolean fingerprint = sharedPreferences.getBoolean("setFingerprint",false);
        Class<?> gotoclass ;
        if(!fingerprint){
            gotoclass = MainActivity.class;
        }else{
            gotoclass = FingerprintActivity.class;
        }
        Intent mainIntent = new Intent(SignUpActivity.this ,gotoclass );
        startActivity(mainIntent);
        finish();
    }

}