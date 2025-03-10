package com.joshnisth.proyectorompecabezas;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;

// Clase separada para resolver el puzzle usando A*
public class PuzzleSolver {

    // Método público que recibe el estado inicial y devuelve la secuencia de movimientos (estados)
    public List<int[][]> solve(int[][] posiciones) {
        int[][] meta = {
                {0, 1, 2},
                {3, 4, 5},
                {6, 7, -1}
        };

        PriorityQueue<Node> open = new PriorityQueue<>();
        HashSet<String> closed = new HashSet<>();

        Node start = new Node(posiciones, 0, null);
        open.add(start);

        while (!open.isEmpty()) {
            Node current = open.poll();

            if (Arrays.deepEquals(current.state, meta)) {
                List<int[][]> path = new ArrayList<>();
                while (current != null) {
                    path.add(0, current.state);
                    current = current.parent;
                }
                return path;
            }
            closed.add(Arrays.deepToString(current.state));

            int[] blankPos = encontrarBlanco(current.state);
            int blankRow = blankPos[0];
            int blankCol = blankPos[1];

            int[][] direcciones = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
            for (int[] dir : direcciones) {
                int newRow = blankRow + dir[0];
                int newCol = blankCol + dir[1];

                if (newRow >= 0 && newRow < 3 && newCol >= 0 && newCol < 3) {
                    int[][] nuevoEstado = copiarMatriz(current.state);
                    nuevoEstado[blankRow][blankCol] = nuevoEstado[newRow][newCol];
                    nuevoEstado[newRow][newCol] = -1;

                    if (closed.contains(Arrays.deepToString(nuevoEstado))) continue;
                    Node vecino = new Node(nuevoEstado, current.g + 1, current);
                    open.add(vecino);
                }
            }
        }
        return null;
    }

    // Método para encontrar la posición de la ficha vacía (-1)
    private int[] encontrarBlanco(int[][] matriz) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (matriz[i][j] == -1) {
                    return new int[]{i, j};
                }
            }
        }
        return null;
    }

    // Copiar matriz para evitar mutaciones
    private int[][] copiarMatriz(int[][] matriz) {
        int[][] nuevaMatriz = new int[3][3];
        for (int i = 0; i < 3; i++) {
            nuevaMatriz[i] = matriz[i].clone();
        }
        return nuevaMatriz;
    }

    // Clase interna Node para la búsqueda A*
    private static class Node implements Comparable<Node> {
        int[][] state;
        int g;
        int h;
        Node parent;

        Node(int[][] state, int g, Node parent) {
            this.state = state;
            this.g = g;
            this.h = calcularHeuristica(state);
            this.parent = parent;
        }

        int f() {
            return g + h;
        }

        @Override
        public int compareTo(Node o) {
            return Integer.compare(this.f(), o.f());
        }
    }

    // Heurística Manhattan
    private static int calcularHeuristica(int[][] state) {
        int distancia = 0;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                int value = state[i][j];
                if (value == -1) continue;
                int goalRow = value / 3;
                int goalCol = value % 3;
                distancia += Math.abs(i - goalRow) + Math.abs(j - goalCol);
            }
        }
        return distancia;
    } }


