package com.joshnisth.proyectorompecabezas.utils;

import android.graphics.Bitmap;
import java.util.Random;

public class PuzzleLogic {

    public static final int PUZZLE_SIZE = 300; // Tamaño fijo para subBitmaps

    // Dividir una imagen escalada en subBitmaps, ninguno es null
    public static Bitmap[] dividirImagen(Bitmap bmpPuzzle, int tamaño) {
        int n = tamaño * tamaño;
        Bitmap[] subBitmaps = new Bitmap[n];

        int anchoPieza = bmpPuzzle.getWidth() / tamaño;
        int altoPieza  = bmpPuzzle.getHeight() / tamaño;

        for (int fila = 0; fila < tamaño; fila++) {
            for (int col = 0; col < tamaño; col++) {
                int index = fila * tamaño + col;
                subBitmaps[index] = Bitmap.createBitmap(
                        bmpPuzzle,
                        col * anchoPieza,
                        fila * altoPieza,
                        anchoPieza,
                        altoPieza
                );
            }
        }
        return subBitmaps;
    }

    // Generar un estado final con 0..(n-1) y un hueco aleatorio
    public static int[][] generarEstadoFinal(int tamaño) {
        int n = tamaño * tamaño;
        int[][] meta = new int[tamaño][tamaño];

        // Llenar en orden
        for (int i = 0; i < n; i++) {
            meta[i / tamaño][i % tamaño] = i;
        }

        // Escoger hueco aleatorio
        Random rand = new Random();
        int filaBlanca = rand.nextInt(tamaño);
        int colBlanca  = rand.nextInt(tamaño);
        meta[filaBlanca][colBlanca] = -1;

        return meta;
    }

    // Copiar meta[][] a posiciones[][]
    public static int[][] copiarEstadoFinal(int[][] meta) {
        int filas = meta.length;
        int cols  = meta[0].length;
        int[][] posiciones = new int[filas][cols];
        for (int f = 0; f < filas; f++) {
            for (int c = 0; c < cols; c++) {
                posiciones[f][c] = meta[f][c];
            }
        }
        return posiciones;
    }

    // Mezclar el puzzle con movimientos aleatorios del hueco
    public static int[][] mezclarPuzzle(int[][] posiciones) {
        // Buscar dónde está -1
        int filas = posiciones.length;
        int cols  = posiciones[0].length;
        int filaBlanca = -1;
        int colBlanca  = -1;

        for (int f = 0; f < filas; f++) {
            for (int c = 0; c < cols; c++) {
                if (posiciones[f][c] == -1) {
                    filaBlanca = f;
                    colBlanca  = c;
                    break;
                }
            }
            if (filaBlanca != -1) break;
        }

        Random rand = new Random();
        for (int i = 0; i < 100; i++) {
            int[][] dirs = {{-1,0},{1,0},{0,-1},{0,1}};
            int[] d = dirs[rand.nextInt(4)];
            int nf = filaBlanca + d[0];
            int nc = colBlanca + d[1];

            if (nf>=0 && nf<filas && nc>=0 && nc<cols) {
                int temp = posiciones[nf][nc];
                posiciones[nf][nc] = -1;
                posiciones[filaBlanca][colBlanca] = temp;

                filaBlanca = nf;
                colBlanca  = nc;
            }
        }
        return posiciones;
    }

    // Verificar si posiciones == meta
    public static boolean estaResuelto(int[][] posiciones, int[][] meta) {
        int filas = posiciones.length;
        int cols  = posiciones[0].length;
        for (int f = 0; f < filas; f++) {
            for (int c = 0; c < cols; c++) {
                if (posiciones[f][c] != meta[f][c]) {
                    return false;
                }
            }
        }
        return true;
    }
}
