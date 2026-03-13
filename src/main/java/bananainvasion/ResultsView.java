package bananainvasion;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.List;

public class ResultsView {

    private final Stage stage;
    private final String currentUsername;
    private final boolean guestMode;
    private final int currentScore;
    private final UserService userService = new UserService();

    public ResultsView(Stage stage, String currentUsername, boolean guestMode, int currentScore) {
        this.stage = stage;
        this.currentUsername = currentUsername;
        this.guestMode = guestMode;
        this.currentScore = currentScore;
    }

    public Scene createScene() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(25));
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #dbeafe, #bfdbfe);");

        VBox centerBox = new VBox(15);
        centerBox.setAlignment(Pos.TOP_CENTER);

        Label title = new Label("Game Results");
        title.setStyle("-fx-font-size: 30px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");

        Label scoreLabel = new Label("Your Score: " + currentScore);
        scoreLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

        Label boardTitle = new Label("Top 10 Leaderboard");
        boardTitle.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");

        VBox leaderboardBox = new VBox(8);
        leaderboardBox.setAlignment(Pos.CENTER);
        leaderboardBox.setPadding(new Insets(10));
        leaderboardBox.setStyle("-fx-background-color: rgba(255,255,255,0.6); -fx-background-radius: 12;");

        List<ScoreEntry> leaderboard = userService.getLeaderboard();

        if (leaderboard.isEmpty()) {
            Label empty = new Label("No scores yet.");
            empty.setStyle("-fx-font-size: 16px; -fx-text-fill: #334155;");
            leaderboardBox.getChildren().add(empty);
        } else {
            for (int i = 0; i < leaderboard.size(); i++) {
                ScoreEntry entry = leaderboard.get(i);

                HBox row = new HBox(30);
                row.setAlignment(Pos.CENTER);
                row.setPrefWidth(420);

                Label rankLabel = new Label((i + 1) + ".");
                rankLabel.setPrefWidth(40);
                rankLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

                Label userLabel = new Label(entry.getUsername());
                userLabel.setPrefWidth(180);
                userLabel.setStyle("-fx-font-size: 16px;");

                Label scoreValueLabel = new Label(String.valueOf(entry.getHighScore()));
                scoreValueLabel.setPrefWidth(100);
                scoreValueLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

                row.getChildren().addAll(rankLabel, userLabel, scoreValueLabel);
                leaderboardBox.getChildren().add(row);
            }
        }

        centerBox.getChildren().addAll(title, scoreLabel, boardTitle, leaderboardBox);
        root.setCenter(centerBox);

        HBox bottomBox = new HBox();
        bottomBox.setPadding(new Insets(20, 0, 0, 0));
        bottomBox.setAlignment(Pos.BOTTOM_RIGHT);

        if (guestMode) {
            Button registerButton = new Button("Register to Play Again");
            registerButton.setPrefWidth(220);
            registerButton.setPrefHeight(45);
            registerButton.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: bold;");
            registerButton.setOnAction(e -> {
                AuthView authView = new AuthView(stage);
                stage.setScene(authView.createRegisterScene());
            });
            bottomBox.getChildren().add(registerButton);
        } else {
            Button playAgainButton = new Button("Play Again");
            playAgainButton.setPrefWidth(160);
            playAgainButton.setPrefHeight(45);
            playAgainButton.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: bold;");
            playAgainButton.setOnAction(e -> {
                GameView gameView = new GameView(stage, currentUsername, false);
                Scene gameScene = new Scene(gameView.getRoot(), 1000, 700);
                stage.setTitle("Banana Invasion");
                stage.setScene(gameScene);
                gameView.startGame();
            });
            bottomBox.getChildren().add(playAgainButton);
        }

        root.setBottom(bottomBox);

        return new Scene(root, 800, 650);
    }
}