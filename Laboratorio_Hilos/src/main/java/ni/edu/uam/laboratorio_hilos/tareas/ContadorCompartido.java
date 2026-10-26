package ni.edu.uam.laboratorio_hilos.tareas;

import java.util.concurrent.atomic.AtomicInteger;

public class ContadorCompartido {

    private int totalPrimitivo = 0;
    private final AtomicInteger totalAtomico = new AtomicInteger(0);
    public void incrementarInseguro() {
        totalPrimitivo++;
    }

    public int getTotalInseguro() {
        return totalPrimitivo;
    }

    public synchronized void incrementarSincronizado() {
        totalPrimitivo++;
    }

    public synchronized int getTotalSincronizado() {
        return totalPrimitivo;
    }

    public void incrementarAtomico() {
        totalAtomico.incrementAndGet();
    }

    public int getTotalAtomico() {
        return totalAtomico.get();
    }
    public synchronized void reiniciar() {
        this.totalPrimitivo = 0;
        this.totalAtomico.set(0);
    }
}
