import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * La clase Mapa representa un mundo bidimensional en forma de matriz.
 * Cada celda del mapa puede ser accesible (valor 0) o inaccesible (valor -1).
 * La clase permite cargar el mapa desde un archivo de texto y verificar
 * si una celda específica es accesible.
 */
public class Mapa {
    private int[][] matriz;
    private int filas;
    private int columnas;

    /**
     * Constructor que inicializa el mapa cargándolo desde un archivo de texto.
     * El archivo debe tener las dimensiones del mapa en las dos primeras líneas,
     * seguidas de las filas con los valores de cada celda separados por tabulaciones.
     *
     * @param rutaArchivo La ruta del archivo de texto que contiene el mapa.
     * @throws IOException Si ocurre un error durante la lectura del archivo.
     */
    public Mapa(String rutaArchivo) throws IOException {
        cargarMapa(rutaArchivo);
    }

    /**
     * Método privado que carga el mapa desde el archivo especificado.
     * La primera línea del archivo indica el número de filas del mapa,
     * la segunda línea el número de columnas, y las líneas restantes
     * representan la matriz de celdas (0 para celdas accesibles, -1 para obstáculos).
     *
     * @param rutaArchivo La ruta del archivo de texto que contiene el mapa.
     * @throws IOException Si ocurre un error durante la lectura del archivo.
     */
    private void cargarMapa(String rutaArchivo) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(rutaArchivo))) {
            filas = Integer.parseInt(br.readLine().trim()); // Número de filas
            columnas = Integer.parseInt(br.readLine().trim()); // Número de columnas
            matriz = new int[filas][columnas]; // Inicializar la matriz con las dimensiones

            // Cargar los valores de cada celda en la matriz
            for (int i = 0; i < filas; i++) {
                String[] valores = br.readLine().trim().split("\t");
                for (int j = 0; j < columnas; j++) {
                    matriz[i][j] = Integer.parseInt(valores[j]);
                }
            }
        }
    }

    /**
     * Verifica si una celda específica del mapa es accesible.
     * Una celda es accesible si está dentro de los límites de la matriz y tiene un valor de 0.
     *
     * @param fila Índice de la fila de la celda.
     * @param columna Índice de la columna de la celda.
     * @return true si la celda es accesible, false si es inaccesible o está fuera de los límites.
     */
    public boolean esCeldaAccesible(int fila, int columna) {
        return (fila >= 0 && fila < filas && columna >= 0 && columna < columnas) && (matriz[fila][columna] == 0);
    }

    /**
     * Devuelve el número de filas del mapa.
     *
     * @return El número de filas.
     */
    public int getFilas() {
        return filas;
    }

    /**
     * Devuelve el número de columnas del mapa.
     *
     * @return El número de columnas.
     */
    public int getColumnas() {
        return columnas;
    }
}
