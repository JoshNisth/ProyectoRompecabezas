package com.joshnisth.proyectorompecabezas.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.joshnisth.proyectorompecabezas.R;
import com.joshnisth.proyectorompecabezas.data.models.Jugador;
import java.util.List;

public class RankingAdapter extends ArrayAdapter<Jugador> {

    public RankingAdapter(Context context, List<Jugador> jugadores) {
        super(context, 0, jugadores);
    }

    // Formatea segundos a mm:ss
    private String formatearTiempo(int segundosTotales) {
        int minutos = segundosTotales / 60;
        int segundos = segundosTotales % 60;
        return String.format("%02d:%02d", minutos, segundos);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Reutilizar view si existe
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_ranking, parent, false);
        }

        // Obtener jugador actual
        Jugador jugador = getItem(position);

        // Referenciar vistas
        TextView tvPosicion = convertView.findViewById(R.id.tvPosicion);
        TextView tvNombre   = convertView.findViewById(R.id.tvNombre);
        TextView tvTiempo   = convertView.findViewById(R.id.tvTiempo);

        // Llenar data
        tvPosicion.setText(String.valueOf(position + 1));
        tvNombre.setText(jugador.getNombre());
        tvTiempo.setText(formatearTiempo(jugador.getTiempo()));

        return convertView;
    }
}
