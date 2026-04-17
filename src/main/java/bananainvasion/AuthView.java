package bananainvasion;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class AuthView {

    private final Stage stage;
    private final UserService userService = new UserService();

    public AuthView(Stage stage) {
        this.stage = stage;
    }

    public Scene createChoiceScene() {
        Label title = new Label("Banana Invasion");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold;");

        Label subtitle = new Label("Choose how you want to continue");
        subtitle.setStyle("-fx-font-size: 16px;");

        Button loginButton = new Button("Login");
        Button registerButton = new Button("Register");
        Button guestButton = new Button("Continue as Guest");

        loginButton.setPrefWidth(220);
        registerButton.setPrefWidth(220);
        guestButton.setPrefWidth(220);

        loginButton.setOnAction(e -> switchScene(createLoginScene()));
        registerButton.setOnAction(e -> switchScene(createRegisterScene()));
        guestButton.setOnAction(e -> openGame(null, true));

        VBox root = new VBox(15, title, subtitle, loginButton, registerButton, guestButton);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #dbeafe, #bfdbfe);");

        return new Scene(root, 500, 400);
    }

    public Scene createLoginScene() {
        Label title = new Label("Login");
        title.setStyle("-fx-font-size: 26px; -fx-font-weight: bold;");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.setMaxWidth(250);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setMaxWidth(250);

        Label messageLabel = new Label();
        messageLabel.setStyle("-fx-text-fill: red; -fx-font-size: 14px;");

        Button loginButton = new Button("Login");
        Button backButton = new Button("Back");

        loginButton.setPrefWidth(200);
        backButton.setPrefWidth(200);

        loginButton.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText().trim();

            if (username.isEmpty() || password.isEmpty()) {
                messageLabel.setText("Enter username and password.");
                return;
            }

            boolean success = userService.loginUser(username, password);

            if (success) {
                switchScene(createOTPScene(username, false));
            } else {
                messageLabel.setText("Invalid login or account not verified.");
            }
        });

        backButton.setOnAction(e -> switchScene(createChoiceScene()));

        VBox root = new VBox(12, title, usernameField, passwordField, loginButton, backButton, messageLabel);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #dbeafe, #bfdbfe);");

        return new Scene(root, 500, 400);
    }

    public Scene createRegisterScene() {
        Label title = new Label("Register");
        title.setStyle("-fx-font-size: 26px; -fx-font-weight: bold;");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.setMaxWidth(250);

        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        emailField.setMaxWidth(250);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setMaxWidth(250);

        Label messageLabel = new Label();
        messageLabel.setStyle("-fx-text-fill: red; -fx-font-size: 14px;");

        Button registerButton = new Button("Register");
        Button backButton = new Button("Back");

        registerButton.setPrefWidth(200);
        backButton.setPrefWidth(200);

        registerButton.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String email = emailField.getText().trim();
            String password = passwordField.getText().trim();

            if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                messageLabel.setText("Fill in username, email, and password.");
                return;
            }

            if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
                messageLabel.setText("Enter a valid email address.");
                return;
            }

            String result = userService.registerUser(username, email, password);

            if ("SUCCESS".equals(result)) {
                // After register, go straight to verification page
                switchScene(createOTPScene(username, true));
            } else {
                messageLabel.setText(result);
            }
        });

        backButton.setOnAction(e -> switchScene(createChoiceScene()));

        VBox root = new VBox(12, title, usernameField, emailField, passwordField, registerButton, backButton, messageLabel);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #dbeafe, #bfdbfe);");

        return new Scene(root, 500, 450);
    }

    // fromRegistration = true  -> verify account, then go to login
    // fromRegistration = false -> verify login, then go to game
    public Scene createOTPScene(String username, boolean fromRegistration) {
        Label title = new Label(fromRegistration ? "Verify Registration" : "Two-Step Verification");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        Label info = new Label("Check your email and enter the 6-digit code");
        info.setStyle("-fx-font-size: 14px;");

        TextField codeField = new TextField();
        codeField.setPromptText("Enter 6-digit code");
        codeField.setMaxWidth(250);

        Label msg = new Label();
        msg.setStyle("-fx-text-fill: red; -fx-font-size: 14px;");

        Label timerLabel = new Label();
        timerLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #2563eb;");

        Button verifyBtn = new Button("Verify");
        Button backBtn = new Button("Back");

        verifyBtn.setPrefWidth(200);
        backBtn.setPrefWidth(200);

        // 5 minutes countdown
        final int[] timeLeft = {300};
        final javafx.animation.Timeline[] timeline = new javafx.animation.Timeline[1];

        // show initial timer text immediately
        timerLabel.setText("Code expires in: 5:00");

        timeline[0] = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(javafx.util.Duration.seconds(1), e -> {
                    timeLeft[0]--;

                    int minutes = timeLeft[0] / 60;
                    int seconds = timeLeft[0] % 60;
                    timerLabel.setText("Code expires in: " + minutes + ":" + String.format("%02d", seconds));

                    if (timeLeft[0] <= 0) {
                        timeline[0].stop();
                        msg.setText("Code expired!");
                        codeField.setDisable(true);
                        verifyBtn.setDisable(true);

                        // Delete account if registration was not completed
                        if (fromRegistration) {
                            userService.deleteUnverifiedUser(username);
                        }
                    }
                })
        );

        timeline[0].setCycleCount(300);
        timeline[0].play();

        verifyBtn.setOnAction(e -> {
            boolean ok = userService.verifyOTP(username, codeField.getText().trim());

            if (ok) {
                timeline[0].stop();

                if (fromRegistration) {
                    userService.markUserVerified(username);
                    switchScene(createLoginScene());
                } else {
                    userService.clearOTP(username);
                    openGame(username, false);
                }
            } else {
                msg.setText("Invalid or expired code");
            }
        });

        backBtn.setOnAction(e -> {
            timeline[0].stop();

            if (fromRegistration) {
                userService.deleteUnverifiedUser(username);
                switchScene(createRegisterScene());
            } else {
                switchScene(createLoginScene());
            }
        });

        VBox root = new VBox(12, title, info, timerLabel, codeField, verifyBtn, backBtn, msg);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #dbeafe, #bfdbfe);");

        return new Scene(root, 500, 400);
    }

    private void switchScene(Scene scene) {
        stage.setScene(scene);
        stage.sizeToScene();
        Platform.runLater(stage::centerOnScreen);
    }

    private void openGame(String username, boolean guestMode) {
        GameView gameView = new GameView(stage, username, guestMode);
        Scene gameScene = new Scene(gameView.getRoot(), 1000, 700);

        stage.setTitle("Banana Invasion");
        stage.setScene(gameScene);
        stage.sizeToScene();
        Platform.runLater(stage::centerOnScreen);

        gameView.startGame();
    }
}