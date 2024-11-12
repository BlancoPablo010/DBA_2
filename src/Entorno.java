/**
 * La clase Entorno representa el entorno inmediato que rodea al agente.
 * Esta clase permite al agente "ver" las celdas contiguas, incluidas las diagonales,
 * y actualizar su percepción sobre el mundo.
 */
public class Entorno {
    private Mapa mapa;
    private int[] posicionAgente;

    /**
     * Constructor que inicializa el entorno con el mapa y la posición inicial del agente.
     *
     * @param mapa El mapa del mundo en el que se mueve el agente.
     * @param posicionInicial Posición inicial del agente en el mapa como un arreglo [fila, columna].
     */
    public Entorno(Mapa mapa, int[] posicionInicial) {
        this.mapa = mapa;
        this.posicionAgente = posicionInicial;
    }

    /**
     * Actualiza la posición del agente en el entorno.
     *
     * @param nuevaPosicion La nueva posición del agente como un arreglo [fila, columna].
     */
    public void actualizarPosicion(int[] nuevaPosicion) {
        this.posicionAgente = nuevaPosicion;
    }

    /**
     * Método que permite al agente ver las celdas contiguas (arriba, abajo, izquierda, derecha y diagonales)
     * y determina si son accesibles. Las celdas fuera de los límites o con obstáculos se consideran inaccesibles.
     *
     * @return Un arreglo de booleanos indicando si cada celda contigua es accesible o no en el orden:
     *         [arriba, abajo, izquierda, derecha, arriba-izquierda, arriba-derecha, abajo-izquierda, abajo-derecha].
     */
    public boolean[] see() {
        int fila = posicionAgente[0];
        int columna = posicionAgente[1];

        // Array que guarda la accesibilidad de las celdas contiguas, incluyendo diagonales
        boolean[] percepciones = new boolean[8];

        // Verificar la celda arriba
        percepciones[0] = mapa.esCeldaAccesible(fila - 1, columna);

        // Verificar la celda abajo
        percepciones[1] = mapa.esCeldaAccesible(fila + 1, columna);

        // Verificar la celda a la izquierda
        percepciones[2] = mapa.esCeldaAccesible(fila, columna - 1);

        // Verificar la celda a la derecha
        percepciones[3] = mapa.esCeldaAccesible(fila, columna + 1);

        // Verificar la celda arriba-izquierda
        percepciones[4] = mapa.esCeldaAccesible(fila - 1, columna - 1);

        // Verificar la celda arriba-derecha
        percepciones[5] = mapa.esCeldaAccesible(fila - 1, columna + 1);

        // Verificar la celda abajo-izquierda
        percepciones[6] = mapa.esCeldaAccesible(fila + 1, columna - 1);

        // Verificar la celda abajo-derecha
        percepciones[7] = mapa.esCeldaAccesible(fila + 1, columna + 1);

        return percepciones;
    }

    /**
     * Obtiene la posición actual del agente en el entorno.
     *
     * @return La posición actual del agente como un arreglo [fila, columna].
     */
    public int[] getPosicionAgente() {
        return posicionAgente;
    }
}
