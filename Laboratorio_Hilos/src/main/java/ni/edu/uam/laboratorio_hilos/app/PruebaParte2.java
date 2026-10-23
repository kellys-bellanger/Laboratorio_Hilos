package ni.edu.uam.laboratorio_hilos.app;


import ni.edu.uam.laboratorio_hilos.tareas.MonitorEstados;
import ni.edu.uam.laboratorio_hilos.tareas.VentanillaCancelable;

public class PruebaParte2 {
    public static void main(String[] args) throws InterruptedException {
        VentanillaCancelable tareaVentanilla = new VentanillaCancelable();
        Thread hiloVentanilla = new Thread(tareaVentanilla, "Hilo-Ventanilla-1");
        tareaVentanilla.setHiloAsociado(hiloVentanilla);


        System.out.println("[EVIDENCIA] Estado justo al instanciar (NEW): " + hiloVentanilla.getState());


        MonitorEstados monitor = new MonitorEstados(hiloVentanilla, 300);
        Thread hiloMonitor = new Thread(monitor, "Hilo-Monitor");
        hiloMonitor.start();


        hiloVentanilla.start();


        Thread.sleep(2500);

        
        System.out.println("\n--- Solicitando detención de la ventanilla ---");
        tareaVentanilla.detener();


        hiloVentanilla.join();
        hiloMonitor.join();


        System.out.println("[EVIDENCIA] Estado tras terminar (TERMINATED): " + hiloVentanilla.getState());
        System.out.println("Estado final en modelo Enum: " + tareaVentanilla.getEstado());
    }
}
