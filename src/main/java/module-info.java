module main.visuile {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;

    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;

    opens main.visuile to javafx.fxml;
    exports main.visuile;
}