package br.com.fecapccp.protipoprojeto;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Configurar a navegação para a tela de login
        Button entrarButton = findViewById(R.id.button);
        entrarButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, FourthActivity.class);
            startActivity(intent);
        });

        // Configurar a navegação para a tela de cadastro
        Button cadastrarButton = findViewById(R.id.button2);
        cadastrarButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SecondActivity.class);
            startActivity(intent);
        });

        // Configurar a navegação para a tela de "Esqueci minha senha"
        TextView senhaEsquecidaText = findViewById(R.id.textView3);
        senhaEsquecidaText.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ThirdActivity.class);
            startActivity(intent);
        });

        // Configurar a aplicação dos "Insets" no layout
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}