package com.joshnisth.proyectorompecabezas;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Configuración del botón personalizado
        ImageButton btnPersonalizado = findViewById(R.id.btnPersonalizado);
        btnPersonalizado.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CrearPuzzleActivity.class);
            startActivity(intent);
        });
    }
}