package bananainvasion;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) {
        AuthView authView = new AuthView(stage);
        Scene scene = authView.createChoiceScene();

        stage.setTitle("Banana Invasion");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.sizeToScene();
        stage.show();

        // Center after JavaFX finishes drawing the window
        Platform.runLater(stage::centerOnScreen);
    }

    public static void main(String[] args) {
        launch(args);
    }
}