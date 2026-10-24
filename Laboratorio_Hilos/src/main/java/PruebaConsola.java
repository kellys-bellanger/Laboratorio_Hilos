public class PruebaConsola {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=================================================");
        System.out.println("   ESTACIÓN 3: REGISTRO DE PRUEBAS (10 RUNS)");
        System.out.println("=================================================\n");

        ContadorCompartido contador = new ContadorCompartido();
        System.out.println("--- PRUEBA 1: SIN PROTECCIÓN (Esperado: < 2000) ---");
        for (int i = 1; i <= 10; i++) {
            contador.reiniciar();

            Thread h1 = new Thread(() -> {
                for (int j = 0; j < 1000; j++) contador.incrementarInseguro();
            });
            Thread h2 = new Thread(() -> {
                for (int j = 0; j < 1000; j++) contador.incrementarInseguro();
            });

            h1.start();
            h2.start();

            h1.join();
            h2.join();

            int total = contador.getTotalInseguro();
            System.out.printf("Run %2d: Total = %d %s%n", i, total, (total < 2000 ? "<- ¡Condición de Carrera!" : ""));
        }

        System.out.println("\n------------");

