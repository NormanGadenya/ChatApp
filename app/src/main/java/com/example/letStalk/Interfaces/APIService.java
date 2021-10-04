package com.example.letStalk.Interfaces;

import com.example.letStalk.Notifications.MyResponse;
import com.example.letStalk.Notifications.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAAYbC3jHI:APA91bFCwc_4xNvBWrStAlWfnbNaBkZnmXHSeecOvwVaAYHW6EYAwLP6guLnJk-f33KnSk2L190jaCogNBq3JyPfpCFN-1Zy2plrWV9Vz2V5_TKGGuxWFQZVktXTpj5VrZmmsNrxIkTP"

            }
    )

    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);
}
