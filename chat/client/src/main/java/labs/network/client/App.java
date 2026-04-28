package labs.network.client;

import atlantafx.base.theme.PrimerDark;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class App extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("/labs.network.client/main-view.fxml"));
        Parent root = fxmlLoader.load();
        AppController controller = fxmlLoader.getController();

        Scene scene = new Scene(root, 980, 640);
        scene.getStylesheets().add(getClass().getResource("/labs.network.client/style.css").toExternalForm());
        stage.setTitle("Chat Client");
        stage.setScene(scene);
        stage.setOnCloseRequest(e -> controller.disconnect());
        stage.show();
    }

    public static void main(String[] args) {
        Application.launch(App.class, args);
    }
}
