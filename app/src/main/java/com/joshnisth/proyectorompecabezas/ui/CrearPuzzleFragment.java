package com.joshnisth.proyectorompecabezas.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.joshnisth.proyectorompecabezas.MainActivity;
import com.joshnisth.proyectorompecabezas.R;
import com.joshnisth.proyectorompecabezas.data.models.Rompecabezas;
import com.joshnisth.proyectorompecabezas.data.repositories.PuntuacionRepository;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class CrearPuzzleFragment extends Fragment {

    private ImageView imagenSeleccionada;
    private RadioGroup radioGroupTamaño;
    private EditText etNombrePuzzle;
    private Uri imagenUri;
    private Bitmap imagenProcesada;
    private final int ANCHO_IMAGEN = 300;
    private final int ALTO_IMAGEN = 300;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_crear_puzzle, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        imagenSeleccionada = view.findViewById(R.id.imagenSeleccionada);
        radioGroupTamaño = view.findViewById(R.id.radioGroupTamaño);
        etNombrePuzzle = view.findViewById(R.id.etNombrePuzzle);
        Button btnTomarFoto = view.findViewById(R.id.btnTomarFoto);
        Button btnElegirGaleria = view.findViewById(R.id.btnElegirGaleria);
        Button btnConfirmar = view.findViewById(R.id.btnConfirmar);

        btnTomarFoto.setOnClickListener(v -> abrirCamara());
        btnElegirGaleria.setOnClickListener(v -> abrirGaleria());
        btnConfirmar.setOnClickListener(v -> confirmarPuzzle());
        Button btnVolver = view.findViewById(R.id.btnRegresarMenu);
        btnVolver.setOnClickListener(v -> {
            ((MainActivity) requireActivity()).cargarFragment(new MenuFragment());
        });
    }

    private final ActivityResultLauncher<Intent> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Bitmap foto = (Bitmap) result.getData().getExtras().get("data");
                    if (foto != null) {
                        imagenProcesada = redimensionarImagen(foto, ANCHO_IMAGEN, ALTO_IMAGEN);
                        imagenSeleccionada.setImageBitmap(imagenProcesada);
                        imagenUri = guardarImagenTemporal(imagenProcesada);
                    }
                }
            });

    private final ActivityResultLauncher<Intent> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    imagenUri = result.getData().getData();
                    try {
                        InputStream imageStream = requireActivity().getContentResolver().openInputStream(imagenUri);
                        Bitmap bitmapOriginal = BitmapFactory.decodeStream(imageStream);
                        imagenProcesada = redimensionarImagen(bitmapOriginal, ANCHO_IMAGEN, ALTO_IMAGEN);
                        imagenSeleccionada.setImageBitmap(imagenProcesada);
                        imagenUri = guardarImagenTemporal(imagenProcesada);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });

    private void abrirCamara() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraLauncher.launch(intent);
    }

    private void abrirGaleria() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        galleryLauncher.launch(intent);
    }

    private Bitmap redimensionarImagen(Bitmap bitmap, int nuevoAncho, int nuevoAlto) {
        return Bitmap.createScaledBitmap(bitmap, nuevoAncho, nuevoAlto, true);
    }

    private Uri guardarImagenTemporal(Bitmap bitmap) {
        try {
            // Generar un nombre único usando la hora actual
            String uniqueName = "imagen_puzzle_" + System.currentTimeMillis() + ".png";

            File archivoTemporal = new File(requireContext().getCacheDir(), uniqueName);
            FileOutputStream fos = new FileOutputStream(archivoTemporal);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
            return Uri.fromFile(archivoTemporal);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    private void confirmarPuzzle() {
        if (imagenUri == null) {
            Toast.makeText(requireContext(), "Selecciona una imagen", Toast.LENGTH_SHORT).show();
            return;
        }

        int tamañoSeleccionado = (radioGroupTamaño.getCheckedRadioButtonId() == R.id.radio3x3) ? 3 : 4;
        String nombrePuzzle = etNombrePuzzle.getText().toString().trim();

        if (nombrePuzzle.isEmpty()) {
            Toast.makeText(requireContext(), "Ingresa un nombre para el puzzle", Toast.LENGTH_SHORT).show();
            return;
        }

        // === Insertar en BD ===
        PuntuacionRepository repo = new PuntuacionRepository(requireContext());
        Rompecabezas nuevoRompecabezas = new Rompecabezas(nombrePuzzle, imagenUri.toString());
        repo.insertarRompecabezas(nuevoRompecabezas);

        // Ir a armar puzzle
        ArmarPuzzleFragment fragment = ArmarPuzzleFragment.newInstance(imagenUri.toString(), tamañoSeleccionado, nombrePuzzle);
        ((MainActivity) requireActivity()).cargarFragment(fragment);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        imagenUri = null;
        imagenSeleccionada.setImageDrawable(null);
        etNombrePuzzle.setText("");
        radioGroupTamaño.check(R.id.radio3x3);
    }
}
