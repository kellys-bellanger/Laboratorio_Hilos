# Laboratorio_Hilos

Laboratorio guiado de concurrencia en Java y JavaFX: una oficina procesa solicitudes desde dos ventanillas que
actualizan un contador compartido, sin bloquear la interfaz y con cancelación cooperativa de los hilos.

## Requisitos

- JDK 21 (probado con Temurin 21)
- Maven (se incluye el wrapper `mvnw` / `mvnw.cmd`, no hace falta instalarlo)
- IntelliJ IDEA (opcional)

## Estructura

El proyecto Maven está en la carpeta `Laboratorio_Hilos/`. El código está en
`src/main/java/ni/edu/uam/laboratorio_hilos/`:

| Paquete | Contenido |
|---|---|
| `app` | Aplicación JavaFX (`MainApp`) y programas de prueba por consola de cada estación |
| `tareas` | Tareas que se ejecutan en hilos: `ProcesoVentanilla`, `VentanillaCancelable`, `MonitorEstados`, `ContadorCompartido` |
| `modelo` | `EstadoVentanilla`: En espera, Ejecutando, Deteniendo, Detenido y Finalizado |

## Cómo ejecutar

Todos los comandos se ejecutan dentro de la carpeta `Laboratorio_Hilos/`.

### Aplicación JavaFX

```bash
./mvnw clean javafx:run
```

En Windows (PowerShell) usar `.\mvnw.cmd clean javafx:run`. Desde IntelliJ también se puede ejecutar la clase
`Launcher`.

La ventana (Estación 5) tiene los botones **Iniciar** y **Detener**, el estado de la sesión, el contador de
solicitudes completadas, el estado de cada ventanilla con su `Thread.State` y un área de registro. Las dos
ventanillas trabajan en hilos propios (`SesionOficina` y `VentanillaOficina`); la vista se refresca cada 100 ms en
el hilo de JavaFX y **Iniciar** solo se vuelve a habilitar cuando ambos hilos de la sesión anterior terminaron.

### Pruebas por consola

Compilar primero con `./mvnw compile` y luego ejecutar la prueba de cada estación:

| Estación | Clase | Qué demuestra |
|---|---|---|
| 1. Thread y Runnable | `app.PruebaEstacion1` | Diferencia entre llamar a `run()` y a `start()` |
| 2 y 4. Estados y detención | `app.PruebaParte2` | Estados NEW, RUNNABLE, TIMED_WAITING y TERMINATED; detención con `volatile` + `interrupt()` |
| 3. Sincronización | `app.PruebaConsola` | Condición de carrera y su corrección con `synchronized` y `AtomicInteger` |

```bash
java -cp target/classes ni.edu.uam.laboratorio_hilos.app.PruebaConsola
```

Desde IntelliJ basta con abrir la clase y ejecutar su método `main`.
