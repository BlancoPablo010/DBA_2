import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import javax.swing.*;
import java.util.*;

public class AgenteMovil extends Agent {
    private int[] posicionAgente;        // Posición actual del agente
    private int[] posicionObjetivo;      // Posición del objetivo
    private Mapa mapa;
    private Interfaz interfaz;           // Referencia a la GUI
    private double direccionAgente = 0;  // Dirección actual del agente en grados

    private List<int[]> camino;          // Camino calculado por A*
    private int energiaConsumida = 0;    // Energía consumida por el agente

    protected void setup() {
        // Obtener los argumentos pasados al agente (mapa y GUI)
        Object[] args = getArguments();
        if (args != null && args.length == 2) {
            mapa = (Mapa) args[0];
            interfaz = (Interfaz) args[1];
        } else {
            System.out.println("No se proporcionaron argumentos al agente.");
            doDelete();
            return;
        }

        // Generar posiciones aleatorias para el agente y el objetivo
        posicionAgente = generarPosicionAleatoria(); // new int[]{0, 0};
        posicionObjetivo = generarPosicionAleatoria(); // new int[]{6,7};

        // Asegurarse de que el agente y el objetivo no estén en la misma posición
        while (posicionAgente[0] == posicionObjetivo[0] && posicionAgente[1] == posicionObjetivo[1]) {
            posicionObjetivo = generarPosicionAleatoria();
        }

        // Actualizar las posiciones en la GUI
        interfaz.setPosicionAgente(posicionAgente);
        interfaz.setPosicionObjetivo(posicionObjetivo);
        interfaz.setDireccionAgente(0); // Inicializar la dirección a 0 grados (mirando a la derecha)
        interfaz.repaintMapa();

        // Calcular el camino usando A*
        camino = buscarCamino();

        if (camino == null) {
            JOptionPane.showMessageDialog(null, "¡No se encontró un camino al objetivo!");
            doDelete(); // Terminar el agente
            return;
        }

        // Añadir un comportamiento que mueve al agente cada 0.5 segundos
        addBehaviour(new TickerBehaviour(this, 500) {
            private int indicePaso = 1; // Comenzamos desde 1 porque la posición 0 es la posición actual

            protected void onTick() {
                if (indicePaso < camino.size()) {
                    moverAgente(camino.get(indicePaso));
                    indicePaso++;
                } else {
                    // El agente ha llegado al objetivo
                    SwingUtilities.invokeLater(() -> {
                        interfaz.setPosicionAgente(posicionAgente);
                        interfaz.setPosicionObjetivo(null); // Eliminar el objetivo de la interfaz
                        interfaz.repaintMapa();
                        JOptionPane.showMessageDialog(null, "¡El agente ha alcanzado el objetivo!");
                    });
                    doDelete(); // Terminar el agente
                }
            }
        });
    }

    // Función para generar una posición aleatoria accesible en el mapa
    private int[] generarPosicionAleatoria() {
        Random rand = new Random();
        int filas = mapa.getFilas();
        int columnas = mapa.getColumnas();
        int[] posicion;
        do {
            posicion = new int[]{rand.nextInt(filas), rand.nextInt(columnas)};
        } while (!mapa.esCeldaAccesible(posicion[0], posicion[1]));
        return posicion;
    }

    // Método para mover al agente a una nueva posición
    private void moverAgente(int[] nuevaPosicion) {
        int[] posicionAnterior = posicionAgente.clone();
        posicionAgente = nuevaPosicion;

        // Calcular la dirección del movimiento
        int deltaY = posicionAgente[0] - posicionAnterior[0]; // Fila
        int deltaX = posicionAgente[1] - posicionAnterior[1]; // Columna
        direccionAgente = Math.toDegrees(Math.atan2(deltaY, deltaX));

        // Ajustar el ángulo para que vaya de 0 a 360 grados
        if (direccionAgente < 0) {
            direccionAgente += 360;
        }

        // Incrementar la energía consumida en 1
        energiaConsumida += 1;

        // Actualizar la GUI en el hilo de despacho de eventos
        SwingUtilities.invokeLater(() -> {
            interfaz.setPosicionAgente(posicionAgente);
            interfaz.setDireccionAgente(direccionAgente);
            interfaz.actualizarEnergia(energiaConsumida);
            interfaz.repaintMapa();
        });
    }

    // Implementación del algoritmo A*
    private List<int[]> buscarCamino() {
        PriorityQueue<Nodo> abiertos = new PriorityQueue<>();
        Set<Nodo> cerrados = new HashSet<>();

        Nodo inicio = new Nodo(posicionAgente[0], posicionAgente[1], null, 0, calcularHeuristica(posicionAgente));
        abiertos.add(inicio);

        while (!abiertos.isEmpty()) {
            Nodo actual = abiertos.poll();

            if (actual.getFila() == posicionObjetivo[0] && actual.getColumna() == posicionObjetivo[1]) {
                // Se encontró el camino al objetivo
                return reconstruirCamino(actual);
            }

            cerrados.add(actual);

            for (int[] vecino : obtenerVecinos(actual)) {
                if (!mapa.esCeldaAccesible(vecino[0], vecino[1])) {
                    continue;
                }

                Nodo nodoVecino = new Nodo(vecino[0], vecino[1], actual, actual.getCostoG() + 1, calcularHeuristica(vecino));

                if (cerrados.contains(nodoVecino)) {
                    continue;
                }

                Optional<Nodo> nodoAbierto = abiertos.stream().filter(n -> n.equals(nodoVecino)).findFirst();

                if (nodoAbierto.isPresent()) {
                    if (nodoVecino.getCostoG() < nodoAbierto.get().getCostoG()) {
                        abiertos.remove(nodoAbierto.get());
                        abiertos.add(nodoVecino);
                    }
                } else {
                    abiertos.add(nodoVecino);
                }
            }
        }

        // No se encontró un camino al objetivo
        return null;
    }

    // Método para reconstruir el camino desde el nodo objetivo hasta el inicio
    private List<int[]> reconstruirCamino(Nodo nodoObjetivo) {
        List<int[]> camino = new ArrayList<>();
        Nodo actual = nodoObjetivo;

        while (actual != null) {
            camino.add(0, new int[]{actual.getFila(), actual.getColumna()});
            actual = actual.getPadre();
        }

        return camino;
    }

    // Obtener los vecinos accesibles de un nodo
    private List<int[]> obtenerVecinos(Nodo nodo) {
        int fila = nodo.getFila();
        int columna = nodo.getColumna();
        List<int[]> vecinos = new ArrayList<>();

        int[][] direcciones = {
                {-1, 0},  // Arriba
                {1, 0},   // Abajo
                {0, -1},  // Izquierda
                {0, 1},   // Derecha
                {-1, -1}, // Arriba-Izquierda
                {-1, 1},  // Arriba-Derecha
                {1, -1},  // Abajo-Izquierda
                {1, 1}    // Abajo-Derecha
        };

        for (int[] dir : direcciones) {
            int nuevaFila = fila + dir[0];
            int nuevaColumna = columna + dir[1];

            if (mapa.esCeldaAccesible(nuevaFila, nuevaColumna)) {
                vecinos.add(new int[]{nuevaFila, nuevaColumna});
            }
        }

        return vecinos;
    }

    // Calcular el costo heurístico (usamos la distancia Manhattan)
    private double calcularHeuristica(int[] posicion) {
        int dx = Math.abs(posicion[0] - posicionObjetivo[0]);
        int dy = Math.abs(posicion[1] - posicionObjetivo[1]);
        return dx + dy;
    }
}
