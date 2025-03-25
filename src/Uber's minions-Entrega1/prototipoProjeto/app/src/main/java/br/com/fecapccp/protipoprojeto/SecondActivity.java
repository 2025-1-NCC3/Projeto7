package br.com.fecapccp.protipoprojeto;

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

public class SecondActivity extends AppCompatActivity {

    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://jd9t3z-3000.csb.app/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(ApiService.class);

        EditText editTextEmail = findViewById(R.id.editTextEmail);
        EditText editTextSenha = findViewById(R.id.editTextSenha);
        Button buttonCadastrar = findViewById(R.id.button1);

        buttonCadastrar.setOnClickListener(v -> {
            String email = editTextEmail.getText().toString().trim();
            String senha = editTextSenha.getText().toString().trim();

            if (email.isEmpty() || senha.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show();
                return;
            }

            cadastrarUsuario(email, senha);
        });
    }

    private void cadastrarUsuario(String email, String senha) {
        Usuario usuario = new Usuario(email, senha);
        Log.d("CADASTRO", "Enviando: " + email);

        apiService.cadastrarUsuario(usuario).enqueue(new Callback<ApiService.ApiResponse>() {
            @Override
            public void onResponse(Call<ApiService.ApiResponse> call,
                                   Response<ApiService.ApiResponse> response) {

                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(SecondActivity.this,
                            response.body().getMessage(),
                            Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(SecondActivity.this,
                            "Erro no cadastro: " + response.code(),
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiService.ApiResponse> call, Throwable t) {
                Toast.makeText(SecondActivity.this,
                        "Falha na conexão: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
                Log.e("CADASTRO", "Erro:", t);
            }
        });
    }
}