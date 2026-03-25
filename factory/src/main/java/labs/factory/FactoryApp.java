package labs.factory;

import atlantafx.base.theme.PrimerDark;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class FactoryApp extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());

        FXMLLoader fxmlLoader = new FXMLLoader(FactoryApp.class.getResource("main-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 800, 600);
        AppController controller = fxmlLoader.getController();
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        stage.setTitle("Fucktory");
        stage.setScene(scene);
        stage.setOnCloseRequest(e -> controller.stopFactory());
        stage.show();
    }

    public static void main(String[] args) {
        Application.launch(FactoryApp.class, args);
    }
}
