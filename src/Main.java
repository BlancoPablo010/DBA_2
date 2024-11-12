import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // Inicia la interfaz en el hilo de eventos de Swing
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                // Crea una nueva instancia de la clase Interfaz
                Interfaz interfaz = new Interfaz();
                interfaz.setVisible(true); // Mostrar la ventana
            }
        });
    }
}