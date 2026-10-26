package ni.edu.uam.laboratorio_hilos.tareas;

import ni.edu.uam.laboratorio_hilos.modelo.EstadoVentanilla;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VentanillaOficinaTest {
    private final ContadorCompartido contador = new ContadorCompartido();
    private final List<String> registro = new CopyOnWriteArrayList<>();

    private static void esperarEstado(VentanillaOficina ventanilla, EstadoVentanilla esperado) throws InterruptedException {
        long limite = System.currentTimeMillis() + 2000;
        while (ventanilla.getEstado() != esperado && System.currentTimeMillis() < limite) {
            Thread.sleep(10);
        }
        assertEquals(esperado, ventanilla.getEstado());
    }

    @Test
    void empiezaEnEspera() {
        VentanillaOficina ventanilla = new VentanillaOficina("Ventanilla A", contador, 3, registro::add);

        assertEquals(EstadoVentanilla.EN_ESPERA, ventanilla.getEstado());
    }

    @Test
    void terminaFinalizadaAlAtenderTodasLasSolicitudes() throws InterruptedException {
        VentanillaOficina ventanilla = new VentanillaOficina("Ventanilla A", contador, 3, registro::add);
        Thread hilo = new Thread(ventanilla, "Hilo-Prueba");

        hilo.start();
        hilo.join(3000);

        assertFalse(hilo.isAlive());
        assertEquals(EstadoVentanilla.FINALIZADO, ventanilla.getEstado());
        assertEquals(3, contador.getTotalSincronizado());
    }

    @Test
    void detenerInterrumpeElSleepYTerminaDetenida() throws InterruptedException {
        VentanillaOficina ventanilla = new VentanillaOficina("Ventanilla A", contador, 100, registro::add);
        Thread hilo = new Thread(ventanilla, "Hilo-Prueba");
        hilo.start();
        esperarEstado(ventanilla, EstadoVentanilla.EJECUTANDO);

        long inicio = System.currentTimeMillis();
        ventanilla.detener();
        hilo.join(2000);
        long demora = System.currentTimeMillis() - inicio;

        assertFalse(hilo.isAlive(), "El hilo debe terminar por sí mismo tras detener()");
        assertTrue(demora < 250, "interrupt() debe despertar el sleep() sin esperar los 250 ms");
        assertEquals(EstadoVentanilla.DETENIDO, ventanilla.getEstado());
        assertTrue(contador.getTotalSincronizado() < 100);
        assertTrue(registro.stream().anyMatch(m -> m.contains("interrumpida durante sleep()")));
    }

    @Test
    void detenerAntesDeIniciarNoAtiendeSolicitudes() throws InterruptedException {
        VentanillaOficina ventanilla = new VentanillaOficina("Ventanilla A", contador, 5, registro::add);
        ventanilla.detener();
        Thread hilo = new Thread(ventanilla, "Hilo-Prueba");

        hilo.start();
        hilo.join(2000);

        assertFalse(hilo.isAlive());
        assertEquals(EstadoVentanilla.DETENIDO, ventanilla.getEstado());
        assertEquals(0, contador.getTotalSincronizado());
    }

    @Test
    void detenerUnaVentanillaFinalizadaNoCambiaSuEstado() throws InterruptedException {
        VentanillaOficina ventanilla = new VentanillaOficina("Ventanilla A", contador, 1, registro::add);
        Thread hilo = new Thread(ventanilla, "Hilo-Prueba");
        hilo.start();
        hilo.join(2000);

        ventanilla.detener();

        assertEquals(EstadoVentanilla.FINALIZADO, ventanilla.getEstado());
    }
}
