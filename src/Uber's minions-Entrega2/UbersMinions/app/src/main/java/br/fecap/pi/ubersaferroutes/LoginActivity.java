package br.fecap.pi.ubersaferroutes;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

import br.fecap.pi.ubersaferroutes.R;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    EditText emailInput, passwordInput;
    Button btnEntrar;
    TextView textEsqueciSenha;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailInput = findViewById(R.id.editTextEmail);
        passwordInput = findViewById(R.id.editTextSenha);
        btnEntrar = findViewById(R.id.botaoEntrar_Login);

        btnEntrar.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            UserActivity user = new UserActivity(email, password);
            ApiService apiService = ApiClient.getRetrofit().create(ApiService.class);

            Call<ApiResponse> call = apiService.login(user);
            call.enqueue(new Callback<ApiResponse>() {
                @Override
                public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        String message = response.body().getMessage();
                        Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(LoginActivity.this, TesteActivity.class);
                        startActivity(intent);

                    } else {
                        // Adicione esta parte para ver o erro detalhado
                        try {
                            String errorBody = response.errorBody().string();
                            Toast.makeText(LoginActivity.this, "Erro: " + response.code() + " - " + errorBody, Toast.LENGTH_LONG).show();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse> call, Throwable t) {  // <-- corrigido aqui
                    Toast.makeText(LoginActivity.this, "Erro: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}

