package com.joshnisth.proyectorompecabezas;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

public class CrearPuzzleActivity extends AppCompatActivity {

    private static final int REQUEST_CAMERA = 1;
    private static final int REQUEST_GALLERY = 2;

    private ImageView imagenSeleccionada;
    private EditText etNombrePuzzle;
    private Uri imagenUri; // Almacena la URI de la imagen seleccionada

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_puzzle);

        imagenSeleccionada = findViewById(R.id.imagenSeleccionada);
        etNombrePuzzle = findViewById(R.id.etNombrePuzzle);
        Button btnTomarFoto = findViewById(R.id.btnTomarFoto);
        Button btnElegirGaleria = findViewById(R.id.btnElegirGaleria);
        Button btnConfirmar = findViewById(R.id.btnConfirmar);

        // Botón para tomar foto
        btnTomarFoto.setOnClickListener(v -> abrirCamara());

        // Botón para elegir imagen de la galería
        btnElegirGaleria.setOnClickListener(v -> abrirGaleria());

        // Botón para confirmar
        btnConfirmar.setOnClickListener(v -> confirmarPuzzle());
    }

    // Método para abrir la cámara
    private void abrirCamara() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_CAMERA);
    }

    // Método para abrir la galería
    private void abrirGaleria() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_GALLERY);
    }

    // Manejar el resultado de la cámara o galería
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && data != null) {
            if (requestCode == REQUEST_CAMERA) {
                // Obtener imagen capturada
                Bitmap foto = (Bitmap) data.getExtras().get("data");
                imagenSeleccionada.setImageBitmap(foto);
                // Guardar temporalmente
                imagenUri = data.getData();
            } else if (requestCode == REQUEST_GALLERY) {
                // Obtener imagen de la galería
                imagenUri = data.getData();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imagenUri);
                    imagenSeleccionada.setImageBitmap(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // Método para confirmar y pasar a la siguiente pantalla
    private void confirmarPuzzle() {
        String nombrePuzzle = etNombrePuzzle.getText().toString().trim();

        if (imagenUri == null) {
            Toast.makeText(this, "Selecciona una imagen", Toast.LENGTH_SHORT).show();
            return;
        }

        if (nombrePuzzle.isEmpty()) {
            Toast.makeText(this, "Ingresa un nombre para el puzzle", Toast.LENGTH_SHORT).show();
            return;
        }

        // Enviar datos a la siguiente actividad
        Intent intent = new Intent(this, ArmarPuzzleActivity.class);
        intent.putExtra("imagenUri", imagenUri.toString());
        intent.putExtra("nombrePuzzle", nombrePuzzle);
        startActivity(intent);
    }
}
