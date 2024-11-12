public class Nodo implements Comparable<Nodo> {
    private int fila;
    private int columna;
    private Nodo padre;
    private double costoG; // Costo desde el inicio hasta este nodo
    private double costoH; // Heurística (estimación del costo desde este nodo hasta el objetivo)
    private double costoF; // costoF = costoG + costoH

    public Nodo(int fila, int columna, Nodo padre, double costoG, double costoH) {
        this.fila = fila;
        this.columna = columna;
        this.padre = padre;
        this.costoG = costoG;
        this.costoH = costoH;
        this.costoF = costoG + costoH;
    }

    public int getFila() {
        return fila;
    }

    public int getColumna() {
        return columna;
    }

    public Nodo getPadre() {
        return padre;
    }

    public double getCostoG() {
        return costoG;
    }

    public double getCostoH() {
        return costoH;
    }

    public double getCostoF() {
        return costoF;
    }

    @Override
    public int compareTo(Nodo otro) {
        return Double.compare(this.costoF, otro.costoF);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Nodo) {
            Nodo otro = (Nodo) obj;
            return this.fila == otro.fila && this.columna == otro.columna;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return fila * 31 + columna;
    }
}
