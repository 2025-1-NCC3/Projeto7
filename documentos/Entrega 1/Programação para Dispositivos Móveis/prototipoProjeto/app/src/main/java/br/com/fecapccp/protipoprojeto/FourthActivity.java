package br.com.fecapccp.protipoprojeto;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class FourthActivity extends AppCompatActivity {

    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fourth);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://jd9t3z-3000.csb.app/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(ApiService.class);

        EditText editTextEmail = findViewById(R.id.editTextEmail);
        EditText editTextSenha = findViewById(R.id.editTextSenha);
        Button buttonEntrar = findViewById(R.id.button1);

        buttonEntrar.setOnClickListener(v -> {
            String email = editTextEmail.getText().toString().trim();
            String senha = editTextSenha.getText().toString().trim();

            if (email.isEmpty() || senha.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show();
                return;
            }

            fazerLogin(email, senha);
        });
    }

    private void fazerLogin(String email, String senha) {
        Usuario usuario = new Usuario(email, senha);
        Log.d("LOGIN", "Enviando: " + email);

        apiService.loginUsuario(usuario).enqueue(new Callback<ApiService.LoginResponse>() {
            @Override
            public void onResponse(Call<ApiService.LoginResponse> call,
                                   Response<ApiService.LoginResponse> response) {

                if (response.isSuccessful() && response.body() != null) {
                    ApiService.LoginResponse loginResponse = response.body();

                    if (loginResponse.isSuccess()) {
                        Toast.makeText(FourthActivity.this,
                                loginResponse.getMessage(),
                                Toast.LENGTH_SHORT).show();

                        startActivity(new Intent(FourthActivity.this, HomeActivity.class));
                        finish();
                    } else {
                        Toast.makeText(FourthActivity.this,
                                loginResponse.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(FourthActivity.this,
                            "Erro no servidor: " + response.code(),
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiService.LoginResponse> call, Throwable t) {
                Toast.makeText(FourthActivity.this,
                        "Falha na conexão: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
                Log.e("LOGIN", "Erro detalhado:", t);
            }
        });
    }
}