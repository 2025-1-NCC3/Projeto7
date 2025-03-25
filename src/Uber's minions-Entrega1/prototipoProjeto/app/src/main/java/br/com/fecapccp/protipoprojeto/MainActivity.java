package br.com.fecapccp.protipoprojeto;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button entrarButton = findViewById(R.id.button);
        entrarButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, FourthActivity.class);
            startActivity(intent);
        });

        Button cadastrarButton = findViewById(R.id.button2);
        cadastrarButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SecondActivity.class);
            startActivity(intent);
        });

        TextView senhaEsquecidaText = findViewById(R.id.textView3);
        senhaEsquecidaText.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ThirdActivity.class);
            startActivity(intent);
        });
    }
}
