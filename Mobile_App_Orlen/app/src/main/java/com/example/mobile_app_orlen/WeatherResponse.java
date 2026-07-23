package com.example.mobile_app_orlen;

import com.google.gson.annotations.SerializedName;

public class WeatherResponse {
    @SerializedName("main")
    public Main main;
    
    @SerializedName("wind")
    public Wind wind;

    public static class Main {
        @SerializedName("temp")
        public float temp;
        
        @SerializedName("humidity")
        public int humidity;
    }

    public static class Wind {
        @SerializedName("speed")
        public float speed;

        @SerializedName("deg")
        public int deg;
    }
}
