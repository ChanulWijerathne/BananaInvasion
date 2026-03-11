package bananainvasion;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) {
        GameView gameView = new GameView();

        Scene scene = new Scene(gameView.getRoot(), 1000, 700);
        stage.setTitle("Banana Invasion");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();

        gameView.startGame();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
