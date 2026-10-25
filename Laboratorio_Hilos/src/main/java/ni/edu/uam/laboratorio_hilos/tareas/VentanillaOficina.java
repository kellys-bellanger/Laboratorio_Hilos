package ni.edu.uam.laboratorio_hilos.tareas;

import ni.edu.uam.laboratorio_hilos.modelo.EstadoVentanilla;

import java.util.function.Consumer;

public class VentanillaOficina implements Runnable {
    private final String nombre;
    private final ContadorCompartido contador;
    private final int solicitudes;
    private final Consumer<String> registro;

    private volatile boolean activo = true;
    private volatile EstadoVentanilla estado = EstadoVentanilla.EN_ESPERA;
    private volatile Thread hiloAsociado;

    public VentanillaOficina(String nombre, ContadorCompartido contador, int solicitudes, Consumer<String> registro) {
        this.nombre = nombre;
        this.contador = contador;
        this.solicitudes = solicitudes;
        this.registro = registro;
    }

    public String getNombre() {
        return nombre;
    }

    public EstadoVentanilla getEstado() {
        return estado;
    }

    // Cancelación cooperativa: se baja la bandera y se interrumpe un posible sleep()
    public synchronized void detener() {
        if (estado == EstadoVentanilla.DETENIDO || estado == EstadoVentanilla.FINALIZADO) {
            return;
        }
        activo = false;
        estado = EstadoVentanilla.DETENIENDO;
        Thread hilo = hiloAsociado;
        if (hilo != null) {
            hilo.interrupt();
        }
    }

    @Override
    public void run() {
        hiloAsociado = Thread.currentThread();
        synchronized (this) {
            if (activo) {
                estado = EstadoVentanilla.EJECUTANDO;
            }
        }
        registro.accept("[" + Thread.currentThread().getName() + "] " + nombre + " inicia la atención.");

        int atendidas = 0;
        try {
            while (activo && atendidas < solicitudes && !Thread.currentThread().isInterrupted()) {
                // Simula el tiempo de atención de una solicitud (TIMED_WAITING)
                Thread.sleep(250);
                contador.incrementarSincronizado();
                atendidas++;
                registro.accept("[" + Thread.currentThread().getName() + "] " + nombre + " atendió la solicitud " + atendidas);
            }
        } catch (InterruptedException e) {
            registro.accept("[" + Thread.currentThread().getName() + "] " + nombre + " fue interrumpida durante sleep().");
            Thread.currentThread().interrupt();
        } finally {
            synchronized (this) {
                estado = atendidas == solicitudes ? EstadoVentanilla.FINALIZADO : EstadoVentanilla.DETENIDO;
            }
            registro.accept("[" + Thread.currentThread().getName() + "] " + nombre + " terminó en estado "
                    + estado.getEtiqueta() + " (" + atendidas + " de " + solicitudes + " solicitudes).");
        }
    }
}
