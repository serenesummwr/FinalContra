module se233.finalcontra {
    requires javafx.controls;
    requires javafx.fxml;
	requires javafx.graphics;
	requires org.apache.logging.log4j;
	requires javafx.base;
	requires javafx.media;

    opens se233.finalcontra to javafx.fxml;
    exports se233.finalcontra;
}