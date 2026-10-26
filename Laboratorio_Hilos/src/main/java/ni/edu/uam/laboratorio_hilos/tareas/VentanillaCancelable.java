package ni.edu.uam.laboratorio_hilos.tareas;

import ni.edu.uam.laboratorio_hilos.modelo.EstadoVentanilla;

public class VentanillaCancelable implements Runnable {
    private volatile boolean activo = true;
    private volatile EstadoVentanilla estado = EstadoVentanilla.EN_ESPERA;
    private Thread hiloAsociado;

    public void setHiloAsociado(Thread hilo) {
        this.hiloAsociado = hilo;
    }

    public EstadoVentanilla getEstado() {
        return estado;
    }

    public void detener() {
        this.activo = false;
        this.estado = EstadoVentanilla.DETENIENDO;
        if (hiloAsociado != null) {
            hiloAsociado.interrupt(); // Interrumpe bloqueos de sleep() o wait()
        }
    }

    @Override
    public void run() {
        estado = EstadoVentanilla.EJECUTANDO;
        System.out.println("[" + Thread.currentThread().getName() + "] Iniciando trabajo.");

        try {
            while (activo && !Thread.currentThread().isInterrupted()) {
                System.out.println("[" + Thread.currentThread().getName() + "] Atendiendo cliente...");

                // Genera el estado TIMED_WAITING mientras duerme
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            System.out.println("[" + Thread.currentThread().getName() + "] Capturada InterruptedException durante sleep().");
            // Se restaura la bandera de interrupción por buenas prácticas
            Thread.currentThread().interrupt();
        } finally {
            if (!activo) {
                estado = EstadoVentanilla.DETENIDO;
                System.out.println("[" + Thread.currentThread().getName() + "] Finalizó en estado DETENIDO.");
            } else {
                estado = EstadoVentanilla.FINALIZADO;
                System.out.println("[" + Thread.currentThread().getName() + "] Finalizó en estado FINALIZADO.");
            }
        }
    }
}