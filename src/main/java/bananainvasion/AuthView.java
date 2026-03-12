package bananainvasion;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
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

        loginButton.setOnAction(e -> stage.setScene(createLoginScene()));
        registerButton.setOnAction(e -> stage.setScene(createRegisterScene()));
        guestButton.setOnAction(e -> openGame());

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

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");

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
                openGame();
            } else {
                messageLabel.setText("Invalid username or password.");
            }
        });

        backButton.setOnAction(e -> stage.setScene(createChoiceScene()));

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

        TextField emailField = new TextField();
        emailField.setPromptText("Email");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");

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

            boolean success = userService.registerUser(username, email, password);

            if (success) {
                stage.setScene(createLoginScene());
            } else {
                messageLabel.setText("Registration failed. Username or email may already exist.");
            }
        });

        backButton.setOnAction(e -> stage.setScene(createChoiceScene()));

        VBox root = new VBox(12, title, usernameField, emailField, passwordField, registerButton, backButton, messageLabel);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #dbeafe, #bfdbfe);");

        return new Scene(root, 500, 450);
    }

    private void openGame() {
        GameView gameView = new GameView();
        Scene gameScene = new Scene(gameView.getRoot(), 1000, 700);
        stage.setTitle("Banana Invasion");
        stage.setScene(gameScene);
        gameView.startGame();
    }
}