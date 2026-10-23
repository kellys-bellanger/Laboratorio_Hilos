package ni.edu.uam.laboratorio_hilos.tareas;

public class ProcesoVentanilla implements Runnable {
    private final String nombre;

    public ProcesoVentanilla(String nombre) {
        this.nombre = nombre;
    }

    @Override
    public void run() {
        for (int i = 1; i <= 5; i++) {
            if (Thread.currentThread().isInterrupted()) {
                System.out.println("[" + nombre + "] Proceso interrumpido.");
                return;
            }
            System.out.println("[" + Thread.currentThread().getName() + "] - " + nombre + " procesa solicitud " + i);
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                System.out.println("[" + nombre + "] Interrumpido durante el descanso (sleep).");
                Thread.currentThread().interrupt();
                return;
            }
        }
    }
}