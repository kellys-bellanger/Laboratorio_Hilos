module ni.edu.uam.laboratorio_hilos {
    requires javafx.controls;
    requires javafx.fxml;


    opens ni.edu.uam.laboratorio_hilos to javafx.fxml;
    exports ni.edu.uam.laboratorio_hilos;
}