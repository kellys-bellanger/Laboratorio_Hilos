# Laboratorio_Hilos

Laboratorio guiado de concurrencia en Java y JavaFX: una oficina procesa solicitudes desde dos ventanillas que
actualizan un contador compartido, sin bloquear la interfaz y con cancelación cooperativa de los hilos.

**CNU0525 · Programación de Aplicaciones de Escritorio · Grupo 1** · Profesor: José Durán García

## Equipo

| Integrante | Rol | Aporte principal | Rama |
|---|---|---|---|
| Daniella Rocha | Líder | Estación 5 (integración JavaFX), integración de ramas, pruebas JUnit, documentación | `feature/daniella` |
| Kellys Bellanger | Integrante | Base del proyecto y Estación 1 (Thread y Runnable) | `feature/kellys` |
| Nora Obregón | Integrante | Estaciones 2 (estados de hilos) y 4 (detención) | `feature/nora` |
| Daris Mayorga | Integrante | Estación 3 (sincronización del contador) | `feature/daris` |

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
| `tareas` | Tareas que se ejecutan en hilos: `ProcesoVentanilla`, `VentanillaCancelable`, `MonitorEstados`, `ContadorCompartido`, `VentanillaOficina` y `SesionOficina` |
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

`PruebaConsola` acepta los incrementos por hilo como argumento (por defecto 100 000). Con `1000`, como dice el
enunciado, la condición de carrera aparece muy pocas veces.

Desde IntelliJ basta con abrir la clase y ejecutar su método `main`.

### Pruebas automáticas (JUnit 5)

```bash
./mvnw test
```

| Clase de prueba | Qué verifica |
|---|---|
| `ContadorCompartidoTest` | `synchronized` y `AtomicInteger` no pierden conteos con dos hilos (10 repeticiones cada uno) |
| `VentanillaOficinaTest` | Estados de la ventanilla y cancelación cooperativa: `detener()` interrumpe el `sleep()` y el hilo termina solo |
| `SesionOficinaTest` | Las dos ventanillas cuentan sin pérdidas, se detienen juntas y el aviso de fin llega sin hilos vivos |
