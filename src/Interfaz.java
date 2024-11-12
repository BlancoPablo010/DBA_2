import jade.core.Runtime;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;

import javax.sound.sampled.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.IOException;

public class Interfaz extends JFrame {
    private static final String MAPAS_PATH = "maps";
    private static final String IMAGEN_FONDO = "img/menu.png"; // Ruta de la imagen de fondo
    private static final String MAPA_POR_DEFECTO = MAPAS_PATH + "/mapWithoutObstacle.txt"; // Ruta del mapa por defecto
    private static final String MUSICA_FONDO = "sound/pacman_beginning.wav"; // Ruta de la música de fondo
    private Mapa mapaSeleccionado;
    private int[] posicionAgente;  // Posición actual del agente
    private int[] posicionObjetivo; // Posición del objetivo
    private double direccionAgente = 0; // Dirección actual del agente en grados
    private int energiaConsumida = 0; // Energía consumida por el agente

    private JPanel panelMenu;
    private JPanel panelMapa;
    private Image imagenFondo;
    private Image imgObjetivo;
    private Image imgAgente;

    // Referencias al agente y al contenedor de JADE
    private AgentController agentController;
    private AgentContainer mainContainer;
    private Runtime jadeRuntime;

    // Control de sonido
    private Clip clip;
    private FloatControl volumeControl;
    private boolean isMuted = false;
    private JButton btnMute;

    // Etiqueta para mostrar la energía consumida
    private JLabel lblEnergia;

    public Interfaz() {
        setTitle("Interfaz Principal");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(850, 850); // Tamaño ajustado para incluir el botón sin superposición
        setLocationRelativeTo(null);
        setLayout(new CardLayout());

        // Cargar las imágenes para el fondo, el agente y el objetivo
        imagenFondo = new ImageIcon(IMAGEN_FONDO).getImage();
        imgObjetivo = new ImageIcon("img/objetivo.png").getImage();
        imgAgente = new ImageIcon("img/agente.png").getImage();

        // Inicializar los paneles
        inicializarPanelMenu();
        inicializarPanelMapa();

        // Agregar los paneles a la ventana
        add(panelMenu, "Menu");
        add(panelMapa, "Mapa");

        // Reproducir la música de fondo
        reproducirMusicaFondo();

        // Mostrar el menú inicial
        ((CardLayout) getContentPane().getLayout()).show(getContentPane(), "Menu");
    }

    /**
     * Método para reproducir música de fondo en bucle.
     */
    private void reproducirMusicaFondo() {
        try {
            File archivoMusica = new File(MUSICA_FONDO);
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(archivoMusica);
            clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            // Obtener el control de volumen
            if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                volumeControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            } else {
                volumeControl = null;
            }
            clip.loop(Clip.LOOP_CONTINUOUSLY); // Reproducir en bucle
            clip.start(); // Comenzar la reproducción
        } catch (UnsupportedAudioFileException | LineUnavailableException | IOException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar la música de fondo: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Inicializa el panel del menú con los botones de inicio, selección de mapa y salir.
     */
    private void inicializarPanelMenu() {
        panelMenu = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Dibujar la imagen de fondo
                g.drawImage(imagenFondo, 0, 0, getWidth(), getHeight(), this);
            }
        };
        panelMenu.setLayout(new BorderLayout());

        // Panel central para los botones principales
        JPanel panelCentral = new JPanel(new GridBagLayout());
        panelCentral.setOpaque(false);

        // Panel de botones
        JPanel panelBotones = new JPanel();
        panelBotones.setLayout(new GridLayout(3, 1, 10, 10));
        panelBotones.setOpaque(false); // Hacer el panel de botones transparente

        // Botones estilizados
        JButton btnIniciar = crearBotonEstilizado("Iniciar");
        JButton btnElegirMapa = crearBotonEstilizado("Elegir Mapa");
        JButton btnSalir = crearBotonEstilizado("Salir");

        btnIniciar.addActionListener(e -> iniciarMapaSeleccionado());
        btnElegirMapa.addActionListener(e -> mostrarBotonesMapas());
        btnSalir.addActionListener(e -> System.exit(0));

        panelBotones.add(btnIniciar);
        panelBotones.add(btnElegirMapa);
        panelBotones.add(btnSalir);

        // Añadir el panel de botones centrado
        panelCentral.add(panelBotones, new GridBagConstraints());

        // Botón de mute/unmute en la esquina inferior izquierda
        btnMute = new JButton();
        actualizarIconoMute();
        btnMute.setContentAreaFilled(false);
        btnMute.setBorderPainted(false);
        btnMute.setFocusPainted(false);
        btnMute.addActionListener(e -> toggleMute());
        btnMute.setBackground(new Color(70, 130, 180)); // Fondo azulado
        btnMute.setOpaque(true);

        // Panel inferior para el botón mute
        JPanel panelInferior = new JPanel(new BorderLayout());
        panelInferior.setOpaque(false);
        panelInferior.add(btnMute, BorderLayout.WEST);

        // Añadir paneles al panelMenu
        panelMenu.add(panelCentral, BorderLayout.CENTER);
        panelMenu.add(panelInferior, BorderLayout.SOUTH);
    }

    /**
     * Método para togglear el mute
     */
    private void toggleMute() {
        isMuted = !isMuted;
        if (volumeControl != null) {
            if (isMuted) {
                volumeControl.setValue(volumeControl.getMinimum()); // Mute (mínimo volumen)
            } else {
                volumeControl.setValue(0.0f);   // Unmute (restaurar volumen)
            }
        }
        actualizarIconoMute();
    }

    /**
     * Método para actualizar el icono del botón mute
     */
    private void actualizarIconoMute() {
        String iconPath = isMuted ? "img/mute.png" : "img/unmute.png";
        ImageIcon icon = new ImageIcon(iconPath);
        // Escalar la imagen
        Image scaledImage = icon.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH);
        icon = new ImageIcon(scaledImage);
        btnMute.setIcon(icon);
    }

    /**
     * Inicializa el panel del mapa donde se visualizará el entorno del agente y el objetivo.
     */
    private void inicializarPanelMapa() {
        panelMapa = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (mapaSeleccionado != null) {
                    int anchoPanel = getWidth();
                    int altoPanel = getHeight() - 50; // Ajuste para evitar superposición con el botón
                    int filas = mapaSeleccionado.getFilas();
                    int columnas = mapaSeleccionado.getColumnas();

                    // Calcular el tamaño de cada celda en función del tamaño de la pantalla
                    int tamanoCelda = Math.min(anchoPanel / columnas, altoPanel / filas);
                    int margenX = (anchoPanel - (columnas * tamanoCelda)) / 2;
                    int margenY = (altoPanel - (filas * tamanoCelda)) / 2;

                    // Dibujar cada celda del mapa
                    for (int i = 0; i < filas; i++) {
                        for (int j = 0; j < columnas; j++) {
                            int x = margenX + j * tamanoCelda;
                            int y = margenY + i * tamanoCelda;

                            // Dibujar obstáculos, celdas libres, agente y objetivo
                            if (mapaSeleccionado.esCeldaAccesible(i, j)) {
                                g.setColor(Color.BLACK); // Celdas libres en negro
                                g.fillRect(x, y, tamanoCelda, tamanoCelda);
                            } else {
                                g.setColor(Color.BLUE); // Obstáculos en azul
                                g.fillRect(x, y, tamanoCelda, tamanoCelda);
                            }
                            g.setColor(Color.GRAY);
                            g.drawRect(x, y, tamanoCelda, tamanoCelda); // Bordes de celdas
                        }
                    }

                    // Dibujar la posición del agente usando la imagen rotada
                    if (posicionAgente != null) {
                        Graphics2D g2d = (Graphics2D) g.create();

                        // Calcular la posición donde se dibujará el agente
                        int agenteX = margenX + posicionAgente[1] * tamanoCelda;
                        int agenteY = margenY + posicionAgente[0] * tamanoCelda;

                        // Crear una transformación para rotar la imagen
                        AffineTransform at = new AffineTransform();
                        at.translate(agenteX + tamanoCelda / 2, agenteY + tamanoCelda / 2);
                        at.rotate(Math.toRadians(direccionAgente));
                        at.translate(-tamanoCelda / 2, -tamanoCelda / 2);
                        at.scale((double) tamanoCelda / imgAgente.getWidth(null), (double) tamanoCelda / imgAgente.getHeight(null));

                        // Dibujar la imagen rotada
                        g2d.drawImage(imgAgente, at, this);
                        g2d.dispose();
                    }

                    // Dibujar la posición objetivo usando la imagen (si aún existe)
                    if (posicionObjetivo != null) {
                        g.drawImage(imgObjetivo, margenX + posicionObjetivo[1] * tamanoCelda, margenY + posicionObjetivo[0] * tamanoCelda, tamanoCelda, tamanoCelda, this);
                    }
                }
            }
        };

        panelMapa.setBackground(new Color(200, 200, 200)); // Fondo claro para el mapa

        // Botón para volver al menú principal
        JButton btnVolver = crearBotonEstilizado("Volver al Menú Principal");
        btnVolver.addActionListener(e -> {
            // Terminar el agente y el contenedor de JADE
            terminarAgenteYContenedor();
            mostrarMenu();
        });

        // Etiqueta para mostrar la energía consumida
        lblEnergia = new JLabel("Energía: 0");
        lblEnergia.setForeground(Color.WHITE);
        lblEnergia.setFont(new Font("Arial", Font.BOLD, 16));

        JPanel panelInferior = new JPanel();
        panelInferior.setBackground(new Color(50, 50, 50));
        panelInferior.add(lblEnergia);
        panelInferior.add(btnVolver);

        panelMapa.setLayout(new BorderLayout());
        panelMapa.add(panelInferior, BorderLayout.SOUTH);
    }

    /**
     * Muestra el panel del menú.
     */
    private void mostrarMenu() {
        ((CardLayout) getContentPane().getLayout()).show(getContentPane(), "Menu");
    }

    /**
     * Muestra el panel del mapa.
     */
    private void mostrarMapa() {
        ((CardLayout) getContentPane().getLayout()).show(getContentPane(), "Mapa");
        panelMapa.repaint();
    }

    /**
     * Inicia el mapa seleccionado o carga el mapa por defecto si no hay selección.
     */
    private void iniciarMapaSeleccionado() {
        try {
            mapaSeleccionado = (mapaSeleccionado != null) ? mapaSeleccionado : new Mapa(MAPA_POR_DEFECTO);
            mostrarMapa();
            launchAgent(); // Lanzar el agente
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar el mapa: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Muestra botones para seleccionar un mapa.
     */
    private void mostrarBotonesMapas() {
        File directorio = new File(MAPAS_PATH);
        String[] mapas = directorio.list((dir, name) -> name.endsWith(".txt"));

        if (mapas == null || mapas.length == 0) {
            JOptionPane.showMessageDialog(this, "No se encontraron mapas en el directorio 'maps'.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JPanel panelMapas = new JPanel();
        panelMapas.setLayout(new GridLayout(mapas.length, 1, 10, 10));
        panelMapas.setBackground(new Color(50, 50, 50));
        panelMapas.setBorder(new EmptyBorder(20, 20, 20, 20));

        for (String mapaNombre : mapas) {
            JButton botonMapa = crearBotonEstilizado(mapaNombre);
            botonMapa.addActionListener(e -> seleccionarMapa(mapaNombre));
            panelMapas.add(botonMapa);
        }

        JOptionPane.showMessageDialog(this, panelMapas, "Selecciona un Mapa", JOptionPane.PLAIN_MESSAGE);
    }

    private void seleccionarMapa(String mapaNombre) {
        try {
            mapaSeleccionado = new Mapa(MAPAS_PATH + "/" + mapaNombre);
            JOptionPane.showMessageDialog(this, "Mapa seleccionado: " + mapaNombre, "Mapa Cargado", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar el mapa: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JButton crearBotonEstilizado(String texto) {
        JButton boton = new JButton(texto);
        boton.setBackground(new Color(70, 130, 180));
        boton.setForeground(Color.WHITE);
        boton.setFocusPainted(false);
        boton.setFont(new Font("Arial", Font.BOLD, 16));
        boton.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(30, 30, 30), 2), new EmptyBorder(10, 20, 10, 20)));
        return boton;
    }

    // Métodos para actualizar posiciones y repintar el mapa
    public void setPosicionAgente(int[] posicionAgente) {
        this.posicionAgente = posicionAgente;
    }

    public void setPosicionObjetivo(int[] posicionObjetivo) {
        this.posicionObjetivo = posicionObjetivo;
    }

    public void repaintMapa() {
        panelMapa.repaint();
    }

    // Nuevo método para actualizar la dirección del agente
    public void setDireccionAgente(double direccionAgente) {
        this.direccionAgente = direccionAgente;
    }

    // Método para actualizar la energía consumida
    public void actualizarEnergia(int energia) {
        this.energiaConsumida = energia;
        lblEnergia.setText("Energía: " + energiaConsumida);
    }

    // Método para lanzar el agente
    private void launchAgent() {
        try {
            if (jadeRuntime == null) {
                // Inicializar el entorno de JADE solo si no está ya inicializado
                jadeRuntime = Runtime.instance();
                jadeRuntime.setCloseVM(false); // Evitar que cierre la JVM
            }

            // Crear un perfil para el contenedor principal
            Profile profile = new ProfileImpl(null, 1200, null);
            mainContainer = jadeRuntime.createMainContainer(profile);

            // Pasar el mapa y la interfaz como argumentos al agente
            Object[] agentArgs = new Object[]{mapaSeleccionado, this};

            // Crear y lanzar el agente
            agentController = mainContainer.createNewAgent("AgenteMovil", "AgenteMovil", agentArgs);
            agentController.start();

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al lanzar el agente: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Método para terminar el agente y el contenedor de JADE
    private void terminarAgenteYContenedor() {
        try {
            if (agentController != null) {
                try {
                    agentController.kill();
                } catch (jade.wrapper.StaleProxyException e) {
                    // El agente ya ha sido eliminado, no es necesario hacer nada
                }
                agentController = null;
            }
            if (mainContainer != null) {
                mainContainer.kill();
                mainContainer = null;
            }
            // No cerramos el runtime de JADE
            // if (jadeRuntime != null) {
            //     jadeRuntime.shutDown();
            //     jadeRuntime = null;
            // }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
