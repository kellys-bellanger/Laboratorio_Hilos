package ni.edu.uam.laboratorio_hilos.tareas;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ContadorCompartidoTest {
    private static final int INCREMENTOS_POR_HILO = 100_000;

    private static void ejecutarEnDosHilos(Runnable incremento) throws InterruptedException {
        Runnable tarea = () -> {
            for (int i = 0; i < INCREMENTOS_POR_HILO; i++) {
                incremento.run();
            }
        };
        Thread h1 = new Thread(tarea, "Hilo-Prueba-1");
        Thread h2 = new Thread(tarea, "Hilo-Prueba-2");
        h1.start();
        h2.start();
        h1.join();
        h2.join();
    }

    @RepeatedTest(10)
    void incrementoSincronizadoNoPierdeConteos() throws InterruptedException {
        ContadorCompartido contador = new ContadorCompartido();

        ejecutarEnDosHilos(contador::incrementarSincronizado);

        assertEquals(2 * INCREMENTOS_POR_HILO, contador.getTotalSincronizado());
    }

    @RepeatedTest(10)
    void incrementoAtomicoNoPierdeConteos() throws InterruptedException {
        ContadorCompartido contador = new ContadorCompartido();

        ejecutarEnDosHilos(contador::incrementarAtomico);

        assertEquals(2 * INCREMENTOS_POR_HILO, contador.getTotalAtomico());
    }

    @Test
    void incrementoInseguroFuncionaConUnSoloHilo() {
        ContadorCompartido contador = new ContadorCompartido();

        for (int i = 0; i < 1000; i++) {
            contador.incrementarInseguro();
        }

        assertEquals(1000, contador.getTotalInseguro());
    }

    @Test
    void reiniciarDejaAmbosContadoresEnCero() {
        ContadorCompartido contador = new ContadorCompartido();
        contador.incrementarSincronizado();
        contador.incrementarAtomico();

        contador.reiniciar();

        assertEquals(0, contador.getTotalSincronizado());
        assertEquals(0, contador.getTotalAtomico());
    }
}
