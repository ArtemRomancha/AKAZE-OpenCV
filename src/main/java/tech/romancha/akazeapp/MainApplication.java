package tech.romancha.akazeapp;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApplication extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/mainWindow.fxml"));
        Parent root = loader.load();
        MainWindowController controller = loader.getController();
        primaryStage.setOnCloseRequest(e -> controller.setClosed());
        primaryStage.setTitle("A-KAZE Application");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }
}
