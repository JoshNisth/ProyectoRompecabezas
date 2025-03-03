package com.joshnisth.proyectorompecabezas;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class CrearPuzzleActivity extends AppCompatActivity {

    private static final int REQUEST_CAMERA = 1;
    private static final int REQUEST_GALLERY = 2;
    private static final int ANCHO_IMAGEN = 300;
    private static final int ALTO_IMAGEN = 300;

    private ImageView imagenSeleccionada;
    private EditText etNombrePuzzle;
    private Uri imagenUri; // Almacena la URI de la imagen seleccionada
    private Bitmap imagenProcesada; // Bitmap de la imagen redimensionada

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
                if (foto != null) {
                    // Redimensionar la imagen capturada
                    imagenProcesada = redimensionarImagen(foto, ANCHO_IMAGEN, ALTO_IMAGEN);
                    imagenSeleccionada.setImageBitmap(imagenProcesada);

                    // Guardar la imagen redimensionada como un archivo temporal
                    imagenUri = guardarImagenTemporal(imagenProcesada);
                }
            } else if (requestCode == REQUEST_GALLERY) {
                // Obtener imagen de la galería
                imagenUri = data.getData();
                try {
                    InputStream imageStream = getContentResolver().openInputStream(imagenUri);
                    Bitmap bitmapOriginal = BitmapFactory.decodeStream(imageStream);

                    // Redimensionar la imagen seleccionada
                    imagenProcesada = redimensionarImagen(bitmapOriginal, ANCHO_IMAGEN, ALTO_IMAGEN);
                    imagenSeleccionada.setImageBitmap(imagenProcesada);

                    // Guardar la imagen redimensionada como un archivo temporal
                    imagenUri = guardarImagenTemporal(imagenProcesada);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // Método para redimensionar la imagen
    private Bitmap redimensionarImagen(Bitmap bitmap, int nuevoAncho, int nuevoAlto) {
        return Bitmap.createScaledBitmap(bitmap, nuevoAncho, nuevoAlto, true);
    }

    // Método para guardar la imagen redimensionada en un archivo temporal y obtener su URI
    private Uri guardarImagenTemporal(Bitmap bitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "imagen_puzzle", null);
        return Uri.parse(path);
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
