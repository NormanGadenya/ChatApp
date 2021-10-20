package com.neuralBit.letsTalk.Services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslator;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions;
import com.neuralBit.letsTalk.Common.Tools;

public class downloadLanguage extends Service {
    private FirebaseTranslator Translator;
    private Context context;
    private ResultReceiver myResultReceiver;
    private final Bundle bundle = new Bundle();
    private Tools tools;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        tools = new Tools();
        String otherUserLang = intent.getStringExtra("otherUserLang");
        String prefLang = intent.getStringExtra("preferredLang");
        myResultReceiver =  intent.getParcelableExtra("receiver");
        FirebaseTranslatorOptions options =
                new FirebaseTranslatorOptions.Builder()
                        .setSourceLanguage(tools.convertLangName(otherUserLang))
                        .setTargetLanguage(tools.convertLangName(prefLang))
                        .build();
        Translator = FirebaseNaturalLanguage.getInstance().getTranslator(options);

        downloadModal(otherUserLang);
        return START_NOT_STICKY;

    }

    private void downloadModal(String otherUserLang) {
        FirebaseModelDownloadConditions conditions = new FirebaseModelDownloadConditions.Builder().requireWifi().build();


        // below line is use to download our modal.
        Translator.downloadModelIfNeeded(conditions).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) { ;
                // this method is called when modal is downloaded successfully.
                bundle.putBoolean("otherUserLang",true);
                switch(otherUserLang){
                    case "English": myResultReceiver.send(100,bundle);

                    case "Spanish" : myResultReceiver.send(200,bundle);

                    case "German" : myResultReceiver.send(300,bundle);

                    case "Swahili" : myResultReceiver.send(400,bundle);

                    case "French" : myResultReceiver.send(500,bundle);

                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void setContext(Context context) {
        this.context = context;
    }
}
