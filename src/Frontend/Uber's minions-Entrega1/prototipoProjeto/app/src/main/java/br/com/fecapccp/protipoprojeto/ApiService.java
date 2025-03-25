package br.com.fecapccp.protipoprojeto;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {
    @POST("users")
    Call<ApiResponse> cadastrarUsuario(@Body Usuario usuario);

    @POST("login")
    Call<LoginResponse> loginUsuario(@Body Usuario usuario);

    class ApiResponse {
        private boolean success;
        private String message;

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
    }

    class LoginResponse {
        private boolean success;
        private String message;
        private User user;

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public User getUser() { return user; }

        public static class User {
            private String email;
            public String getEmail() { return email; }
        }
    }
}