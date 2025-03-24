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

public class SecondActivity extends AppCompatActivity {

    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_second);

        // Configurar Retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://localhost:3000/") // Certifique-se de que está correto
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(ApiService.class);

        EditText editTextEmail = findViewById(R.id.editTextEmail);
        EditText editTextSenha = findViewById(R.id.editTextSenha);
        Button buttonCadastrar = findViewById(R.id.button1);

        buttonCadastrar.setOnClickListener(v -> {
            String email = editTextEmail.getText().toString();
            String senha = editTextSenha.getText().toString();

            Usuario usuario = new Usuario(email, senha);

            apiService.cadastrarUsuario(usuario).enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    if (response.isSuccessful()) {
                        String mensagem = response.body(); // Lê a mensagem do servidor
                        Toast.makeText(SecondActivity.this, mensagem, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(SecondActivity.this, "Erro ao cadastrar!", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    Toast.makeText(SecondActivity.this, "Falha na conexão!", Toast.LENGTH_SHORT).show();
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