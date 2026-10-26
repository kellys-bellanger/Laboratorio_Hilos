package ni.edu.uam.laboratorio_hilos.tareas;

import ni.edu.uam.laboratorio_hilos.modelo.EstadoVentanilla;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SesionOficinaTest {

    @Test
    void sesionCompletaAtiendeTodasLasSolicitudesSinPerdidas() throws InterruptedException {
        SesionOficina sesion = new SesionOficina(4);
        CountDownLatch terminada = new CountDownLatch(1);

        sesion.iniciar(terminada::countDown);

        assertTrue(terminada.await(5, TimeUnit.SECONDS), "La sesión debe avisar que terminó");
        assertEquals(8, sesion.getTotalEsperado());
        assertEquals(8, sesion.getTotal());
        assertFalse(sesion.hayHilosVivos());
        sesion.getVentanillas().forEach(v -> assertEquals(EstadoVentanilla.FINALIZADO, v.getEstado()));
    }

    @Test
    void detenerTerminaAmbasVentanillasCooperativamente() throws InterruptedException {
        SesionOficina sesion = new SesionOficina(100);
        CountDownLatch terminada = new CountDownLatch(1);
        sesion.iniciar(terminada::countDown);
        Thread.sleep(600);

        sesion.detener();

        assertTrue(terminada.await(2, TimeUnit.SECONDS), "Los hilos deben terminar tras detener()");
        assertFalse(sesion.hayHilosVivos());
        assertTrue(sesion.getTotal() < sesion.getTotalEsperado());
        sesion.getVentanillas().forEach(v -> assertEquals(EstadoVentanilla.DETENIDO, v.getEstado()));
    }

    @Test
    void elAvisoDeFinSoloLlegaCuandoNoQuedanHilosVivos() throws InterruptedException {
        SesionOficina sesion = new SesionOficina(3);
        CountDownLatch terminada = new CountDownLatch(1);
        AtomicBoolean habiaHilosVivos = new AtomicBoolean(true);

        sesion.iniciar(() -> {
            habiaHilosVivos.set(sesion.hayHilosVivos());
            terminada.countDown();
        });

        assertTrue(terminada.await(5, TimeUnit.SECONDS));
        assertFalse(habiaHilosVivos.get(), "Reinicio seguro: al avisar ya no debe quedar ningún hilo vivo");
    }

    @Test
    void elRegistroSeVaciaAlLeerlo() throws InterruptedException {
        SesionOficina sesion = new SesionOficina(2);
        CountDownLatch terminada = new CountDownLatch(1);
        sesion.iniciar(terminada::countDown);
        assertTrue(terminada.await(5, TimeUnit.SECONDS));

        assertFalse(sesion.vaciarRegistro().isEmpty());
        assertTrue(sesion.vaciarRegistro().isEmpty());
    }

    @Test
    void cadaVentanillaTieneSuPropioHiloConNombre() {
        SesionOficina sesion = new SesionOficina(1);

        assertEquals("Hilo-Ventanilla-1", sesion.getHilos().get(0).getName());
        assertEquals("Hilo-Ventanilla-2", sesion.getHilos().get(1).getName());
        sesion.getHilos().forEach(h -> assertEquals(Thread.State.NEW, h.getState()));
    }
}
