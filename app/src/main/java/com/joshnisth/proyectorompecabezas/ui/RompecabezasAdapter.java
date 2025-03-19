package com.joshnisth.proyectorompecabezas.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.joshnisth.proyectorompecabezas.R;
import com.joshnisth.proyectorompecabezas.data.models.Rompecabezas;
import java.io.File;
import java.util.List;

public class RompecabezasAdapter extends ArrayAdapter<Rompecabezas> {

    public RompecabezasAdapter(Context context, List<Rompecabezas> rompecabezasList) {
        super(context, 0, rompecabezasList);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Reutilizar la vista si existe
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_rompecabezas, parent, false);
        }

        // Obtener elemento actual
        Rompecabezas item = getItem(position);

        // Referenciar vistas
        ImageView imgPuzzle = convertView.findViewById(R.id.imgPuzzle);
        TextView tvNombre = convertView.findViewById(R.id.tvNombreRompecabezas);

        // Setear nombre
        tvNombre.setText(item.getNombre());

        // Cargar imagen desde la ruta
        // Suponiendo que la ruta es un 'file://...' o 'content://...'
        // o si fue guardada con 'Uri.fromFile(...)'
        Uri uriImagen = Uri.parse(item.getRutaImagen());

        // Forma sencilla (puede que necesites decodificar con BitmapFactory si no es content://)
        // Ejemplo 1: setImageURI si la imagen está en un content / file Uri
        imgPuzzle.setImageURI(uriImagen);

        // Si no mostrara imagen, podrías decodificar manualmente:
        // File f = new File(uriImagen.getPath());
        // if (f.exists()) {
        //     Bitmap bmp = BitmapFactory.decodeFile(f.getAbsolutePath());
        //     imgPuzzle.setImageBitmap(bmp);
        // }

        return convertView;
    }
}
