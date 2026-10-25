package ni.edu.uam.laboratorio_hilos.tareas;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class SesionOficina {
    private static final int CANTIDAD_VENTANILLAS = 2;

    private final ContadorCompartido contador = new ContadorCompartido();
    // Los hilos dejan aquí sus mensajes y la interfaz los lee en bloque
    private final Queue<String> registro = new ConcurrentLinkedQueue<>();
    private final List<VentanillaOficina> ventanillas = new ArrayList<>();
    private final List<Thread> hilos = new ArrayList<>();
    private final int solicitudesPorVentanilla;

    public SesionOficina(int solicitudesPorVentanilla) {
        this.solicitudesPorVentanilla = solicitudesPorVentanilla;
        for (int i = 1; i <= CANTIDAD_VENTANILLAS; i++) {
            VentanillaOficina ventanilla = new VentanillaOficina("Ventanilla " + i, contador, solicitudesPorVentanilla, registro::add);
            ventanillas.add(ventanilla);
            hilos.add(new Thread(ventanilla, "Hilo-Ventanilla-" + i));
        }
    }

    // alTerminar se ejecuta en un hilo supervisor cuando ambas ventanillas terminaron
    public void iniciar(Runnable alTerminar) {
        hilos.forEach(Thread::start);

        Thread supervisor = new Thread(() -> {
            try {
                for (Thread hilo : hilos) {
                    hilo.join(); // join() fuera del hilo de JavaFX
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
            alTerminar.run();
        }, "Hilo-Supervisor");
        supervisor.setDaemon(true);
        supervisor.start();
    }

    public void detener() {
        ventanillas.forEach(VentanillaOficina::detener);
    }

    public boolean hayHilosVivos() {
        return hilos.stream().anyMatch(Thread::isAlive);
    }

    public int getTotal() {
        return contador.getTotalSincronizado();
    }

    public int getTotalEsperado() {
        return solicitudesPorVentanilla * CANTIDAD_VENTANILLAS;
    }

    public List<VentanillaOficina> getVentanillas() {
        return List.copyOf(ventanillas);
    }

    public List<Thread> getHilos() {
        return List.copyOf(hilos);
    }

    public List<String> vaciarRegistro() {
        List<String> mensajes = new ArrayList<>();
        String mensaje;
        while ((mensaje = registro.poll()) != null) {
            mensajes.add(mensaje);
        }
        return mensajes;
    }
}
