package ni.edu.uam.laboratorio_hilos.modelo;

public enum EstadoVentanilla {
    EN_ESPERA("En espera"),
    EJECUTANDO("Ejecutando"),
    DETENIENDO("Deteniendo"),
    DETENIDO("Detenido"),
    FINALIZADO("Finalizado");

    private final String etiqueta;

    EstadoVentanilla(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
}}
