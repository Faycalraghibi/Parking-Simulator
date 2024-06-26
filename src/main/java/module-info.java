module com.example.parkingsimulator {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;
    requires com.almasb.fxgl.all;
    requires java.logging;

    opens com.example.parkingsimulator to javafx.fxml;
    exports com.example.parkingsimulator;
}