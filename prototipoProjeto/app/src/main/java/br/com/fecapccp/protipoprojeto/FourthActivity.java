package br.com.fecapccp.protipoprojeto;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_fourth);

        // Configurar Retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://localhost:3000/") // Certifique-se de que está correto
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(ApiService.class);

        EditText editTextEmail = findViewById(R.id.editTextEmail);
        EditText editTextSenha = findViewById(R.id.editTextSenha);
        Button buttonEntrar = findViewById(R.id.button1);

        buttonEntrar.setOnClickListener(v -> {
            String email = editTextEmail.getText().toString();
            String senha = editTextSenha.getText().toString();

            Usuario usuario = new Usuario(email, senha);

            apiService.loginUsuario(usuario).enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    if (response.isSuccessful()) {
                        String mensagem = response.body(); // Lê a mensagem do servidor
                        Toast.makeText(FourthActivity.this, mensagem, Toast.LENGTH_SHORT).show();
                    } else {
                        // Se a resposta não for bem-sucedida, exibe uma mensagem de erro
                        Toast.makeText(FourthActivity.this, "Credenciais inválidas!", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    // Em caso de falha na conexão
                    Toast.makeText(FourthActivity.this, "Falha na conexão!", Toast.LENGTH_SHORT).show();
                }
            });
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}