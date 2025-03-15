package com.joshnisth.proyectorompecabezas.utils;

import java.util.*;

public class PuzzleSolver {

    /**
     * Resuelve el puzzle usando A*.
     * @param inicial estado inicial NxN
     * @param meta estado final NxN
     * @param n tamaño del puzzle (3 o 4)
     * @return lista de estados hasta la meta o null si no hay solución
     */
    public List<int[][]> solve(int[][] inicial, int[][] meta, int n) {
        PriorityQueue<Node> open = new PriorityQueue<>();
        HashSet<String> closed = new HashSet<>();

        Node start = new Node(inicial, 0, null, n, meta);
        open.add(start);

        while (!open.isEmpty()) {
            Node current = open.poll();

            // ¿Llegamos a la meta?
            if (estaIgual(current.state, meta)) {
                return reconstruirCamino(current);
            }

            closed.add(matrixToString(current.state));

            // Generar vecinos (posibles movimientos de la ficha blanca)
            int[] posBlanco = encontrarBlanco(current.state, n);
            int filaB = posBlanco[0];
            int colB = posBlanco[1];

            int[][] direcciones = {{-1,0},{1,0},{0,-1},{0,1}};
            for (int[] d : direcciones) {
                int nuevaFila = filaB + d[0];
                int nuevaCol  = colB + d[1];

                if (nuevaFila>=0 && nuevaFila<n && nuevaCol>=0 && nuevaCol<n) {
                    // Generar un nuevo estado intercambiando la ficha
                    int[][] nuevoEstado = copiarMatriz(current.state, n);
                    nuevoEstado[filaB][colB] = nuevoEstado[nuevaFila][nuevaCol];
                    nuevoEstado[nuevaFila][nuevaCol] = -1;

                    String key = matrixToString(nuevoEstado);
                    if (!closed.contains(key)) {
                        Node vecino = new Node(nuevoEstado, current.g + 1, current, n, meta);
                        open.add(vecino);
                    }
                }
            }
        }
        return null; // No se encontró solución
    }

    // Reconstruir el camino desde el nodo final hasta el inicial
    private List<int[][]> reconstruirCamino(Node goal) {
        List<int[][]> path = new ArrayList<>();
        Node current = goal;
        while (current != null) {
            path.add(0, current.state);
            current = current.parent;
        }
        return path;
    }

    // Verifica si dos matrices NxN son iguales
    private boolean estaIgual(int[][] a, int[][] b) {
        if (a.length != b.length) return false;
        for (int i=0; i<a.length; i++){
            if (!Arrays.equals(a[i], b[i])) return false;
        }
        return true;
    }

    // Encontrar posición del -1 (ficha blanca)
    private int[] encontrarBlanco(int[][] matriz, int n) {
        for (int i=0; i<n; i++){
            for (int j=0; j<n; j++){
                if (matriz[i][j] == -1) {
                    return new int[]{i,j};
                }
            }
        }
        return null;
    }

    // Copiar matriz NxN
    private int[][] copiarMatriz(int[][] original, int n) {
        int[][] copia = new int[n][n];
        for (int i=0; i<n; i++){
            copia[i] = original[i].clone();
        }
        return copia;
    }

    // Convertir matriz a String (para closed set)
    private String matrixToString(int[][] mat) {
        return Arrays.deepToString(mat);
    }

    // Clase interna Node para la búsqueda A*
    private static class Node implements Comparable<Node> {
        int[][] state;
        int g;        // costo desde inicio
        int h;        // heurística
        Node parent;
        int n;        // tamaño NxN
        int[][] meta; // meta NxN

        Node(int[][] state, int g, Node parent, int n, int[][] meta) {
            this.state = state;
            this.g = g;
            this.parent = parent;
            this.n = n;
            this.meta = meta;
            this.h = calcularHeuristica(state, meta, n);
        }

        int f() {
            return g + h;
        }

        @Override
        public int compareTo(Node o) {
            return Integer.compare(this.f(), o.f());
        }

        // Heurística Manhattan
        private static int calcularHeuristica(int[][] actual, int[][] meta, int n) {
            int distancia = 0;
            // meta[x][y] = valor. Debemos ubicar valor en la pos final.
            // O, más fácil: hallamos la pos de 'val' en meta y comparamos
            // con pos de 'val' en actual
            for (int i=0; i<n; i++){
                for (int j=0; j<n; j++){
                    int val = actual[i][j];
                    if (val == -1) continue;

                    // dónde está val en meta?
                    int metaFila = val / n; // En puzzle normal, val/n = fila
                    int metaCol  = val % n;
                    if (meta[metaFila][metaCol] == -1) {
                        // Si la meta dice que val no está en [metaFila, metaCol]?
                        // Depende de tu final. Asumimos el puzzle final es val= i*(n)+j
                    }
                    // Distancia manhattan
                    distancia += Math.abs(i - metaFila) + Math.abs(j - metaCol);
                }
            }
            return distancia;
        }
    }
}
