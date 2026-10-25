package ni.edu.uam.laboratorio_hilos.app;

import ni.edu.uam.laboratorio_hilos.tareas.ProcesoVentanilla;

public class PruebaEstacion1 {

    public static void main(String[] args) {
        System.out.println("=== PRUEBA 1: Invocación directa a run() ===");
        ProcesoVentanilla v1 = new ProcesoVentanilla("Ventanilla 1");
        ProcesoVentanilla v2 = new ProcesoVentanilla("Ventanilla 2");

        Thread t1Directo = new Thread(v1, "Hilo-Ventanilla-1");
        Thread t2Directo = new Thread(v2, "Hilo-Ventanilla-2");

        // Llamada directa a run() -> Se ejecuta secuencialmente en el hilo principal (main)
        t1Directo.run();
        t2Directo.run();

        System.out.println("\n=== PRUEBA 2: Invocación con start() ===");
        Thread t1Background = new Thread(v1, "Hilo-Ventanilla-1");
        Thread t2Background = new Thread(v2, "Hilo-Ventanilla-2");

        // start() solicita al S.O. crear un nuevo hilo de ejecución
        t1Background.start();
        t2Background.start();

        System.out.println("[" + Thread.currentThread().getName() + "] El método main terminó de invocar start().");
    }
}
