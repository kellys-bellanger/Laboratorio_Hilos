package ni.edu.uam.laboratorio_hilos.tareas;

public class MonitorEstados implements Runnable {
    private final Thread hiloObjetivo;
    private final long intervaloMs;
    private volatile boolean monitoreando = true;

    public MonitorEstados(Thread hiloObjetivo, long intervaloMs) {
        this.hiloObjetivo = hiloObjetivo;
        this.intervaloMs = intervaloMs;
    }

    public void detenerMonitoreo() {
        this.monitoreando = false;
    }

    @Override
    public void run() {
        while (monitoreando && hiloObjetivo.getState() != Thread.State.TERMINATED) {
            System.out.println("[MONITOR] Estado de '" + hiloObjetivo.getName() + "': " + hiloObjetivo.getState());
            try {
                Thread.sleep(intervaloMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        System.out.println("MONITOR: Estado final de '" + hiloObjetivo.getName() + "': " + hiloObjetivo.getState());
    }
}