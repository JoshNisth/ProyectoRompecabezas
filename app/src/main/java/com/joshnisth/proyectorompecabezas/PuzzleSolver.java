package com.joshnisth.proyectorompecabezas;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;

// Clase separada para resolver el puzzle usando A*
public class PuzzleSolver {

    // Método público que recibe el estado inicial y devuelve la secuencia de movimientos (estados)
    public List<int[]> solve(int[] estadoInicial) {
        int[] meta = {0, 1, 2, 3, 4, 5, 6, 7, -1};

        PriorityQueue<Node> open = new PriorityQueue<>();
        HashSet<String> closed = new HashSet<>();

        Node start = new Node(estadoInicial, 0, null);
        open.add(start);

        while (!open.isEmpty()) {
            Node current = open.poll();

            if (Arrays.equals(current.state, meta)) {
                List<int[]> path = new ArrayList<>();
                while (current != null) {
                    path.add(0, current.state); // Inserta al inicio para invertir el camino
                    current = current.parent;
                }
                return path;
            }
            closed.add(Arrays.toString(current.state));

            int blankIndex = 0;
            for (int i = 0; i < 9; i++) {
                if (current.state[i] == -1) {
                    blankIndex = i;
                    break;
                }
            }
            int blankRow = blankIndex / 3;
            int blankCol = blankIndex % 3;
            int[][] direcciones = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
            for (int[] dir : direcciones) {
                int newRow = blankRow + dir[0];
                int newCol = blankCol + dir[1];
                if (newRow >= 0 && newRow < 3 && newCol >= 0 && newCol < 3) {
                    int newIndex = newRow * 3 + newCol;
                    int[] nuevoEstado = current.state.clone();
                    nuevoEstado[blankIndex] = nuevoEstado[newIndex];
                    nuevoEstado[newIndex] = -1;
                    if (closed.contains(Arrays.toString(nuevoEstado))) continue;
                    Node vecino = new Node(nuevoEstado, current.g + 1, current);
                    open.add(vecino);
                }
            }
        }
        return null;
    }

    // Clase interna Node para la búsqueda A*
    private static class Node implements Comparable<Node> {
        int[] state;
        int g; // Costo desde el inicio
        int h; // Heurística
        Node parent;

        Node(int[] state, int g, Node parent) {
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

    // Heurística: suma de las distancias Manhattan
    private static int calcularHeuristica(int[] state) {
        int distancia = 0;
        for (int i = 0; i < 9; i++) {
            int value = state[i];
            if (value == -1) continue;
            int goalRow = value / 3;
            int goalCol = value % 3;
            int currentRow = i / 3;
            int currentCol = i % 3;
            distancia += Math.abs(currentRow - goalRow) + Math.abs(currentCol - goalCol);
        }
        return distancia;
    }
}

