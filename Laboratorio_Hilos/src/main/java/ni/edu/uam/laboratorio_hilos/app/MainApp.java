package ni.edu.uam.laboratorio_hilos.app;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import ni.edu.uam.laboratorio_hilos.modelo.EstadoVentanilla;
import ni.edu.uam.laboratorio_hilos.tareas.SesionOficina;
import ni.edu.uam.laboratorio_hilos.tareas.VentanillaOficina;

import java.util.List;

public class MainApp extends Application {
    private static final int SOLICITUDES_POR_VENTANILLA = 20;

    private final Button botonIniciar = new Button("Iniciar");
    private final Button botonDetener = new Button("Detener");
    private final Label etiquetaEstado = new Label(EstadoVentanilla.EN_ESPERA.getEtiqueta());
    private final Label etiquetaContador = new Label("0");
    private final Label etiquetaVentanilla1 = new Label("Ventanilla 1: -");
    private final Label etiquetaVentanilla2 = new Label("Ventanilla 2: -");
    private final TextArea areaRegistro = new TextArea();

    // Refresco agrupado: en vez de un Platform.runLater() por mensaje, la vista se actualiza cada 100 ms
    private final Timeline refresco = new Timeline(new KeyFrame(Duration.millis(100), e -> refrescarVista()));

    private SesionOficina sesion;

    @Override
    public void start(Stage primaryStage) {
        botonDetener.setDisable(true);
        botonIniciar.setOnAction(e -> iniciar());
        botonDetener.setOnAction(e -> detener());

        areaRegistro.setEditable(false);
        areaRegistro.setWrapText(true);

        HBox botones = new HBox(10, botonIniciar, botonDetener);
        VBox panelEstado = new VBox(6,
                new HBox(6, new Label("Estado:"), etiquetaEstado),
                new HBox(6, new Label("Solicitudes completadas:"), etiquetaContador),
                etiquetaVentanilla1,
                etiquetaVentanilla2);

        VBox superior = new VBox(12, botones, panelEstado);
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(15));
        root.setTop(superior);
        root.setCenter(areaRegistro);
        BorderPane.setMargin(areaRegistro, new Insets(12, 0, 0, 0));

        refresco.setCycleCount(Animation.INDEFINITE);
        refresco.play();

        // Al cerrar se solicita la detención cooperativa en lugar de cortar los hilos
        primaryStage.setOnCloseRequest(e -> {
            if (sesion != null) {
                sesion.detener();
            }
        });

        primaryStage.setTitle("Oficina - Concurrencia en JavaFX");
        primaryStage.setScene(new Scene(root, 560, 480));
        primaryStage.show();
    }

    private void iniciar() {
        // Solo se permite una nueva sesión cuando los hilos de la anterior terminaron
        if (sesion != null && sesion.hayHilosVivos()) {
            return;
        }
        sesion = new SesionOficina(SOLICITUDES_POR_VENTANILLA);
        areaRegistro.clear();
        etiquetaContador.setText("0");
        etiquetaEstado.setText(EstadoVentanilla.EJECUTANDO.getEtiqueta());
        botonIniciar.setDisable(true);
        botonDetener.setDisable(false);

        SesionOficina sesionActual = sesion;
        sesionActual.iniciar(() -> Platform.runLater(() -> sesionTerminada(sesionActual)));
    }

    private void detener() {
        botonDetener.setDisable(true);
        etiquetaEstado.setText(EstadoVentanilla.DETENIENDO.getEtiqueta());
        sesion.detener();
    }

    private void sesionTerminada(SesionOficina sesionTerminada) {
        refrescarVista();
        boolean completa = sesionTerminada.getTotal() == sesionTerminada.getTotalEsperado();
        etiquetaEstado.setText(completa
                ? EstadoVentanilla.FINALIZADO.getEtiqueta()
                : EstadoVentanilla.DETENIDO.getEtiqueta());
        areaRegistro.appendText("--- Sesión terminada: " + sesionTerminada.getTotal() + " de "
                + sesionTerminada.getTotalEsperado() + " solicitudes. Hilos vivos: "
                + (sesionTerminada.hayHilosVivos() ? "sí" : "no") + " ---\n");
        botonDetener.setDisable(true);
        botonIniciar.setDisable(false);
    }

    private void refrescarVista() {
        if (sesion == null) {
            return;
        }
        List<String> mensajes = sesion.vaciarRegistro();
        if (!mensajes.isEmpty()) {
            areaRegistro.appendText(String.join("\n", mensajes) + "\n");
        }
        etiquetaContador.setText(sesion.getTotal() + " / " + sesion.getTotalEsperado());

        List<VentanillaOficina> ventanillas = sesion.getVentanillas();
        List<Thread> hilos = sesion.getHilos();
        etiquetaVentanilla1.setText(describir(ventanillas.get(0), hilos.get(0)));
        etiquetaVentanilla2.setText(describir(ventanillas.get(1), hilos.get(1)));
    }

    private String describir(VentanillaOficina ventanilla, Thread hilo) {
        // getState() es solo una instantánea del momento de la consulta
        return ventanilla.getNombre() + ": " + ventanilla.getEstado().getEtiqueta()
                + "  |  " + hilo.getName() + ": " + hilo.getState();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
