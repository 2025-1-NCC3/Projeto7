package br.fecap.pi.ubersaferroutes;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

import br.fecap.pi.ubersaferroutes.R;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CadastroActivity extends AppCompatActivity {
    EditText emailInput, passwordInput;
    Button btnCadastrar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);

        emailInput = findViewById(R.id.editTextEmail);
        passwordInput = findViewById(R.id.editTextSenha);
        btnCadastrar = findViewById(R.id.botaoCadastrar_Cadastro);

        btnCadastrar.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show();
                return;
            }

            UserActivity user = new UserActivity(email, password);
            ApiService apiService = ApiClient.getRetrofit().create(ApiService.class);

            Call<ApiResponse> call = apiService.register(user);
            call.enqueue(new Callback<ApiResponse>() {
                @Override
                public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        String message = response.body().getMessage();
                        Toast.makeText(CadastroActivity.this, message, Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        try {
                            String errorBody = response.errorBody().string();
                            Log.e("API_ERROR", "Código: " + response.code() + " - Erro: " + errorBody);
                            Toast.makeText(CadastroActivity.this, "Erro detalhado: " + errorBody, Toast.LENGTH_LONG).show();
                        } catch (IOException e) {
                            e.printStackTrace();
                            Toast.makeText(CadastroActivity.this, "Erro ao ler mensagem de erro", Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse> call, Throwable t) {
                    Toast.makeText(CadastroActivity.this, "Erro: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}

