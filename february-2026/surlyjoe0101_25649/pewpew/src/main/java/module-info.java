
module pewpew.main {
  requires javafx.controls;
  requires javafx.fxml;
  requires org.jspecify;

  requires java.desktop;
  requires javafx.base;
  requires javafx.media;
  requires java.rmi;
  requires java.compiler;

  exports tj.things.pewpew;

  opens tj.things.pewpew to javafx.graphics,javafx.fxml;
}
