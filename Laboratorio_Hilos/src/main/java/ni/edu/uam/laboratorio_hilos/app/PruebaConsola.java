package ni.edu.uam.laboratorio_hilos.app;

import ni.edu.uam.laboratorio_hilos.tareas.ContadorCompartido;

import java.util.function.IntSupplier;

public class PruebaConsola {
    private static final int CORRIDAS = 10;

    // Uso: PruebaConsola [incrementos por hilo]. El enunciado pide 1000; por defecto se usan 100000
    // porque con 1000 el primer hilo suele terminar antes de que arranque el segundo y la carrera no se ve.
    public static void main(String[] args) throws InterruptedException {
        int iteraciones = args.length > 0 ? Integer.parseInt(args[0]) : 100_000;
        int esperado = 2 * iteraciones;

        System.out.println("=================================================");
        System.out.println("   ESTACIÓN 3: REGISTRO DE PRUEBAS (" + CORRIDAS + " RUNS)");
        System.out.println("   " + iteraciones + " incrementos por hilo, 2 hilos");
        System.out.println("=================================================\n");

        ContadorCompartido contador = new ContadorCompartido();

        System.out.println("--- PRUEBA 1: SIN PROTECCIÓN (Esperado: " + esperado + ") ---");
        ejecutarCorridas(contador, iteraciones, contador::incrementarInseguro, contador::getTotalInseguro, esperado);

        System.out.println("\n--- PRUEBA 2: CON SYNCHRONIZED (Esperado: " + esperado + ") ---");
        ejecutarCorridas(contador, iteraciones, contador::incrementarSincronizado, contador::getTotalSincronizado, esperado);

        System.out.println("\n--- PRUEBA 3: CON ATOMICINTEGER (Esperado: " + esperado + ") ---");
        ejecutarCorridas(contador, iteraciones, contador::incrementarAtomico, contador::getTotalAtomico, esperado);
    }

    private static void ejecutarCorridas(ContadorCompartido contador, int iteraciones, Runnable incremento,
                                         IntSupplier total, int esperado) throws InterruptedException {
        int conPerdidas = 0;
        for (int i = 1; i <= CORRIDAS; i++) {
            contador.reiniciar();

            Runnable tarea = () -> {
                for (int j = 0; j < iteraciones; j++) incremento.run();
            };
            Thread h1 = new Thread(tarea, "Hilo-Contador-1");
            Thread h2 = new Thread(tarea, "Hilo-Contador-2");

            h1.start();
            h2.start();

            // join() se usa aquí porque esta prueba corre en consola, nunca en el hilo de JavaFX
            h1.join();
            h2.join();

            int resultado = total.getAsInt();
            if (resultado < esperado) {
                conPerdidas++;
            }
            System.out.printf("Run %2d: Total = %d %s%n", i, resultado,
                    resultado < esperado ? "<- ¡Condición de Carrera! (" + (esperado - resultado) + " perdidos)" : "");
        }
        System.out.println("Corridas con pérdidas: " + conPerdidas + " de " + CORRIDAS);
    }
}
