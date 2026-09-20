package com.example.mobile_app_orlen;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface EmailApiService {
    @POST("f/mqpappnw")
    Call<Void> sendEmail(@Body EmailRequest emailRequest);
}
