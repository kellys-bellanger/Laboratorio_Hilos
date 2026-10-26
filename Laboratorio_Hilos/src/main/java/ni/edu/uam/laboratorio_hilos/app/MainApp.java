package ni.edu.uam.laboratorio_hilos.app;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import ni.edu.uam.laboratorio_hilos.modelo.EstadoVentanilla;
import ni.edu.uam.laboratorio_hilos.tareas.SesionOficina;
import ni.edu.uam.laboratorio_hilos.tareas.VentanillaOficina;

import java.util.List;

public class MainApp extends Application {
    private static final int SOLICITUDES_POR_VENTANILLA = 20;
    private static final int TOTAL_ESPERADO = SOLICITUDES_POR_VENTANILLA * 2;

    private final Button botonIniciar = new Button("Iniciar");
    private final Button botonDetener = new Button("Detener");
    private final Label etiquetaEstado = new Label();
    private final Label etiquetaContador = new Label("0");
    private final Label etiquetaTotal = new Label("de " + TOTAL_ESPERADO + " solicitudes");
    private final ProgressBar barraProgreso = new ProgressBar(0);
    private final TarjetaVentanilla tarjetaVentanilla1 = new TarjetaVentanilla("Ventanilla 1");
    private final TarjetaVentanilla tarjetaVentanilla2 = new TarjetaVentanilla("Ventanilla 2");
    private final TextArea areaRegistro = new TextArea();

    // Refresco agrupado: en vez de un Platform.runLater() por mensaje, la vista se actualiza cada 100 ms
    private final Timeline refresco = new Timeline(new KeyFrame(Duration.millis(100), e -> refrescarVista()));

    private SesionOficina sesion;

    @Override
    public void start(Stage primaryStage) {
        botonIniciar.getStyleClass().addAll("boton", "boton-iniciar");
        botonDetener.getStyleClass().addAll("boton", "boton-detener");
        botonDetener.setDisable(true);
        botonIniciar.setOnAction(e -> iniciar());
        botonDetener.setOnAction(e -> detener());

        etiquetaEstado.getStyleClass().add("insignia");
        aplicarEstado(etiquetaEstado, EstadoVentanilla.EN_ESPERA);

        areaRegistro.setEditable(false);
        areaRegistro.setWrapText(true);
        areaRegistro.getStyleClass().add("registro");
        VBox.setVgrow(areaRegistro, Priority.ALWAYS);

        Label tituloRegistro = new Label("Registro de actividad");
        tituloRegistro.getStyleClass().add("seccion-titulo");

        VBox contenido = new VBox(
                new HBox(10, botonIniciar, botonDetener),
                crearTarjetas(),
                tituloRegistro,
                areaRegistro);
        contenido.getStyleClass().add("contenido");
        VBox.setVgrow(contenido, Priority.ALWAYS);

        VBox root = new VBox(crearEncabezado(), contenido);

        refresco.setCycleCount(Animation.INDEFINITE);
        refresco.play();

        // Al cerrar se solicita la detención cooperativa en lugar de cortar los hilos
        primaryStage.setOnCloseRequest(e -> {
            if (sesion != null) {
                sesion.detener();
            }
        });

        Scene scene = new Scene(root, 820, 620);
        scene.getStylesheets().add(getClass().getResource("estilos.css").toExternalForm());

        primaryStage.setTitle("Oficina - Concurrencia en JavaFX");
        primaryStage.setMinWidth(700);
        primaryStage.setMinHeight(520);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private HBox crearEncabezado() {
        Label titulo = new Label("Oficina de solicitudes");
        titulo.getStyleClass().add("titulo");
        Label subtitulo = new Label("Laboratorio de concurrencia en Java y JavaFX");
        subtitulo.getStyleClass().add("subtitulo");

        Region espacio = new Region();
        HBox.setHgrow(espacio, Priority.ALWAYS);

        HBox encabezado = new HBox(new VBox(2, titulo, subtitulo), espacio, etiquetaEstado);
        encabezado.getStyleClass().add("encabezado");
        return encabezado;
    }

    private HBox crearTarjetas() {
        Label tituloContador = new Label("Solicitudes completadas");
        tituloContador.getStyleClass().add("tarjeta-titulo");
        etiquetaContador.getStyleClass().add("contador-valor");
        etiquetaTotal.getStyleClass().add("contador-total");
        barraProgreso.setMaxWidth(Double.MAX_VALUE);

        HBox valor = new HBox(8, etiquetaContador, etiquetaTotal);
        valor.setAlignment(Pos.BASELINE_LEFT);

        VBox tarjetaContador = new VBox(tituloContador, valor, barraProgreso);
        tarjetaContador.getStyleClass().add("tarjeta");

        HBox tarjetas = new HBox(14, tarjetaContador, tarjetaVentanilla1, tarjetaVentanilla2);
        for (var tarjeta : tarjetas.getChildren()) {
            HBox.setHgrow(tarjeta, Priority.ALWAYS);
            ((Region) tarjeta).setMaxWidth(Double.MAX_VALUE);
            ((Region) tarjeta).setPrefWidth(0);
        }
        return tarjetas;
    }

    private void iniciar() {
        // Solo se permite una nueva sesión cuando los hilos de la anterior terminaron
        if (sesion != null && sesion.hayHilosVivos()) {
            return;
        }
        sesion = new SesionOficina(SOLICITUDES_POR_VENTANILLA);
        areaRegistro.clear();
        aplicarEstado(etiquetaEstado, EstadoVentanilla.EJECUTANDO);
        botonIniciar.setDisable(true);
        botonDetener.setDisable(false);

        SesionOficina sesionActual = sesion;
        sesionActual.iniciar(() -> Platform.runLater(() -> sesionTerminada(sesionActual)));
    }

    private void detener() {
        botonDetener.setDisable(true);
        aplicarEstado(etiquetaEstado, EstadoVentanilla.DETENIENDO);
        sesion.detener();
    }

    private void sesionTerminada(SesionOficina sesionTerminada) {
        refrescarVista();
        boolean completa = sesionTerminada.getTotal() == sesionTerminada.getTotalEsperado();
        aplicarEstado(etiquetaEstado, completa ? EstadoVentanilla.FINALIZADO : EstadoVentanilla.DETENIDO);
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
        int total = sesion.getTotal();
        etiquetaContador.setText(String.valueOf(total));
        barraProgreso.setProgress((double) total / sesion.getTotalEsperado());

        List<VentanillaOficina> ventanillas = sesion.getVentanillas();
        List<Thread> hilos = sesion.getHilos();
        tarjetaVentanilla1.actualizar(ventanillas.get(0), hilos.get(0));
        tarjetaVentanilla2.actualizar(ventanillas.get(1), hilos.get(1));
    }

    private static void aplicarEstado(Label insignia, EstadoVentanilla estado) {
        insignia.getStyleClass().removeIf(clase -> clase.startsWith("estado-"));
        insignia.getStyleClass().add("estado-" + estado.name().toLowerCase().replace('_', '-'));
        insignia.setText(estado.getEtiqueta());
    }

    private static class TarjetaVentanilla extends VBox {
        private final Label insigniaEstado = new Label();
        private final Label detalleHilo = new Label("Hilo: -");

        TarjetaVentanilla(String nombre) {
            Label titulo = new Label(nombre);
            titulo.getStyleClass().add("tarjeta-titulo");
            insigniaEstado.getStyleClass().add("insignia");
            detalleHilo.getStyleClass().add("detalle-hilo");
            aplicarEstado(insigniaEstado, EstadoVentanilla.EN_ESPERA);

            getChildren().addAll(titulo, insigniaEstado, detalleHilo);
            getStyleClass().add("tarjeta");
        }

        void actualizar(VentanillaOficina ventanilla, Thread hilo) {
            aplicarEstado(insigniaEstado, ventanilla.getEstado());
            // getState() es solo una instantánea del momento de la consulta
            detalleHilo.setText(hilo.getName() + "\n" + hilo.getState());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
