package br.com.fecapccp.protipoprojeto;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {
    @POST("users")
    Call<String> cadastrarUsuario(@Body Usuario usuario);

    @POST("login")
    Call<String> loginUsuario(@Body Usuario usuario);
}