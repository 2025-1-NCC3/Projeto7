package br.fecap.pi.ubersaferroutes;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {
    @POST("register")
    Call<ApiResponse> register(@Body UserActivity user);

    @POST("login")
    Call<ApiResponse> login(@Body UserActivity user);
}

