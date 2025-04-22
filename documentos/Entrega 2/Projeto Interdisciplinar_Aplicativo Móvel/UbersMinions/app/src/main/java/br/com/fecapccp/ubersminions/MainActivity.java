package br.com.fecapccp.ubersminions;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Botão "Entrar" → LoginActivity
        Button btnEntrar = findViewById(R.id.botaoEntrar);
        btnEntrar.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        // Botão "Cadastre-se" → CadastroActivity
        Button btnCadastrar = findViewById(R.id.botaoCadastrar);
        btnCadastrar.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CadastroActivity.class);
            startActivity(intent);
        });

        // Botão "Entrar Anonimamente" → Não faz nada
        Button btnPularLogin = findViewById(R.id.botaoPularLogin);
        btnPularLogin.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, TesteActivity.class);
            startActivity(intent);
        });
    }
}