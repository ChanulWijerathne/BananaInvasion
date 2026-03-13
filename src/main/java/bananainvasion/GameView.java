package bananainvasion;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.Base64;
import java.util.Objects;

public class GameView {

    private static final int WIDTH = 1000;
    private static final int HEIGHT = 700;
    private static final int ISLAND_Y = 590;

    private final Stage stage;
    private final String currentUsername;
    private final boolean guestMode;
    private final UserService userService = new UserService();

    private final StackPane root = new StackPane();
    private final Pane gamePane = new Pane();

    private final Rectangle island = new Rectangle(WIDTH, 110);
    private final Rectangle player = new Rectangle(150, 180);

    private final Label titleLabel = new Label("Banana Invasion");
    private final Label scoreLabel = new Label("Score: 0");

    private final List<Enemy> enemies = new ArrayList<>();
    private final List<Rectangle> bullets = new ArrayList<>();
    private final Set<KeyCode> pressedKeys = new HashSet<>();

    private final Random random = new Random();

    private AnimationTimer gameLoop;
    private Timeline enemySpawner;
    private Timeline reviveTimer;

    private boolean running = false;
    private boolean reviveUsed = false;
    private int score = 0;

    public GameView(Stage stage, String currentUsername, boolean guestMode) {
        this.stage = stage;
        this.currentUsername = currentUsername;
        this.guestMode = guestMode;

        buildUI();
        setupInput();
        setupLoop();
    }

    public Parent getRoot() {
        return root;
    }

    public void startGame() {
        score = 0;
        reviveUsed = false;
        scoreLabel.setText("Score: 0");
        clearObjects();

        player.setLayoutX(WIDTH / 2.0 - 75);
        player.setLayoutY(ISLAND_Y - 150);

        startMainLoop();
    }

    private void buildUI() {
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #87ceeb, #dbeafe);");

        gamePane.setPrefSize(WIDTH, HEIGHT);

        try {
            Image islandImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/island.png")));
            island.setFill(new ImagePattern(islandImage));
        } catch (Exception e) {
            island.setFill(Color.DARKSEAGREEN);
        }
        island.setLayoutY(ISLAND_Y);

        try {
            Image playerImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/player.png")));
            player.setFill(new ImagePattern(playerImage));
        } catch (Exception e) {
            player.setFill(Color.DODGERBLUE);
            System.out.println("Player image not found: " + e.getMessage());
        }

        player.setArcWidth(10);
        player.setArcHeight(10);
        player.setLayoutX(WIDTH / 2.0 - 75);
        player.setLayoutY(ISLAND_Y - 180);

        titleLabel.setStyle("-fx-font-size: 30px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");
        scoreLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");

        VBox hud = new VBox(6, titleLabel, scoreLabel);
        hud.setAlignment(Pos.TOP_LEFT);
        hud.setLayoutX(20);
        hud.setLayoutY(18);

        gamePane.getChildren().addAll(island, player, hud);
        root.getChildren().add(gamePane);
    }

    private void setupInput() {
        root.setFocusTraversable(true);

        root.setOnKeyPressed(e -> {
            pressedKeys.add(e.getCode());

            if (e.getCode() == KeyCode.SPACE && running) {
                shoot();
            }
        });

        root.setOnKeyReleased(e -> pressedKeys.remove(e.getCode()));
    }

    private void setupLoop() {
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                updatePlayer();
                updateBullets();
                updateEnemies();
                checkBulletEnemyCollisions();
            }
        };
    }

    private void startMainLoop() {
        running = true;
        root.requestFocus();

        gameLoop.start();

        enemySpawner = new Timeline(new KeyFrame(Duration.seconds(1.8), e -> spawnEnemy()));
        enemySpawner.setCycleCount(Timeline.INDEFINITE);
        enemySpawner.play();
    }

    private void stopMainLoop() {
        running = false;

        if (gameLoop != null) {
            gameLoop.stop();
        }

        if (enemySpawner != null) {
            enemySpawner.stop();
        }
    }

    private void clearObjects() {
        gamePane.getChildren().removeAll(enemies);
        gamePane.getChildren().removeAll(bullets);
        enemies.clear();
        bullets.clear();
    }

    private void updatePlayer() {
        double moveSpeed = 6;

        if (pressedKeys.contains(KeyCode.LEFT) || pressedKeys.contains(KeyCode.A)) {
            player.setLayoutX(Math.max(0, player.getLayoutX() - moveSpeed));
        }

        if (pressedKeys.contains(KeyCode.RIGHT) || pressedKeys.contains(KeyCode.D)) {
            player.setLayoutX(Math.min(WIDTH - player.getWidth(), player.getLayoutX() + moveSpeed));
        }
    }

    private void shoot() {
        Rectangle bullet = new Rectangle(6, 16, Color.GOLD);
        bullet.setArcWidth(4);
        bullet.setArcHeight(4);
        bullet.setLayoutX(player.getLayoutX() + (player.getWidth() / 2.0) - 5);
        bullet.setLayoutY(player.getLayoutY() - 10);

        bullets.add(bullet);
        gamePane.getChildren().add(bullet);
    }

    private void spawnEnemy() {
        if (enemies.size() >= 3) {
            return;
        }

        double x = 60 + random.nextInt(WIDTH - 200);
        double speed = 1.2 + random.nextDouble() * 0.5;

        Enemy enemy = new Enemy(x, 30, speed);
        enemies.add(enemy);
        gamePane.getChildren().add(enemy);
    }

    private void updateBullets() {
        Iterator<Rectangle> iterator = bullets.iterator();

        while (iterator.hasNext()) {
            Rectangle bullet = iterator.next();
            bullet.setLayoutY(bullet.getLayoutY() - 9);

            if (bullet.getLayoutY() < -30) {
                gamePane.getChildren().remove(bullet);
                iterator.remove();
            }
        }
    }

    private void updateEnemies() {
        Iterator<Enemy> iterator = enemies.iterator();

        while (iterator.hasNext()) {
            Enemy enemy = iterator.next();
            enemy.update();

            if (enemy.getLayoutY() + enemy.getHeight() >= ISLAND_Y) {
                gamePane.getChildren().remove(enemy);
                iterator.remove();
                onEnemyReachedIsland();
                return;
            }
        }
    }

    private void checkBulletEnemyCollisions() {
        List<Enemy> enemiesToRemove = new ArrayList<>();
        List<Rectangle> bulletsToRemove = new ArrayList<>();

        for (Rectangle bullet : bullets) {
            for (Enemy enemy : enemies) {
                if (bullet.getBoundsInParent().intersects(enemy.getBoundsInParent())) {
                    enemiesToRemove.add(enemy);
                    bulletsToRemove.add(bullet);
                    score += 10;
                    scoreLabel.setText("Score: " + score);
                }
            }
        }

        gamePane.getChildren().removeAll(enemiesToRemove);
        gamePane.getChildren().removeAll(bulletsToRemove);
        enemies.removeAll(enemiesToRemove);
        bullets.removeAll(bulletsToRemove);
    }

    private void onEnemyReachedIsland() {
        stopMainLoop();

        if (!reviveUsed) {
            reviveUsed = true;
            showReviveOverlay();
        } else {
            endGame();
        }
    }

    private void showReviveOverlay() {
        VBox overlay = new VBox(15);
        overlay.setAlignment(Pos.CENTER);
        overlay.setPrefSize(720, 540);
        overlay.setMaxSize(720, 540);
        overlay.setStyle("-fx-background-color: rgba(15, 23, 42, 0.95); -fx-background-radius: 16; -fx-padding: 25;");

        Label title = new Label("Revive Challenge");
        title.setStyle("-fx-font-size: 30px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label text = new Label("Solve the Banana puzzle in 15 seconds to continue");
        text.setStyle("-fx-font-size: 16px; -fx-text-fill: #cbd5e1;");

        Label timerLabel = new Label("15");
        timerLabel.setStyle("-fx-font-size: 34px; -fx-font-weight: bold; -fx-text-fill: #fbbf24;");

        ImageView puzzleImageView = new ImageView();
        puzzleImageView.setFitWidth(420);
        puzzleImageView.setFitHeight(240);
        puzzleImageView.setPreserveRatio(true);

        HBox row1 = new HBox(15);
        HBox row2 = new HBox(15);
        row1.setAlignment(Pos.CENTER);
        row2.setAlignment(Pos.CENTER);

        try {
            PuzzleData puzzle = fetchPuzzle();
            int correctAnswer = puzzle.solution();
            puzzleImageView.setImage(puzzle.image());

            List<Integer> options = generateOptions(correctAnswer);

            for (int i = 0; i < 4; i++) {
                int optionValue = options.get(i);

                Button btn = new Button(String.valueOf(optionValue));
                btn.setPrefWidth(120);
                btn.setPrefHeight(45);
                btn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");

                btn.setOnAction(e -> {
                    if (reviveTimer != null) {
                        reviveTimer.stop();
                    }

                    root.getChildren().remove(overlay);

                    if (optionValue == correctAnswer) {
                        clearObjects();
                        startMainLoop();
                    } else {
                        endGame();
                    }
                });

                if (i < 2) {
                    row1.getChildren().add(btn);
                } else {
                    row2.getChildren().add(btn);
                }
            }

            overlay.getChildren().addAll(title, text, timerLabel, puzzleImageView, row1, row2);
            root.getChildren().add(overlay);

            final int[] timeLeft = {15};

            reviveTimer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
                timeLeft[0]--;
                timerLabel.setText(String.valueOf(timeLeft[0]));

                if (timeLeft[0] <= 5) {
                    timerLabel.setTextFill(Color.RED);
                }

                if (timeLeft[0] <= 0) {
                    reviveTimer.stop();
                    root.getChildren().remove(overlay);
                    endGame();
                }
            }));

            reviveTimer.setCycleCount(15);
            reviveTimer.play();

        } catch (Exception e) {
            root.getChildren().remove(overlay);
            endGame();
        }
    }

    private void endGame() {
        running = false;

        if (gameLoop != null) {
            gameLoop.stop();
        }
        if (enemySpawner != null) {
            enemySpawner.stop();
        }
        if (reviveTimer != null) {
            reviveTimer.stop();
        }

        if (!guestMode && currentUsername != null) {
            userService.updateHighScore(currentUsername, score);
        }

        ResultsView resultsView = new ResultsView(stage, currentUsername, guestMode, score);
        Scene resultsScene = resultsView.createScene();
        stage.setScene(resultsScene);
        stage.centerOnScreen();
    }

    private List<Integer> generateOptions(int correct) {
        Set<Integer> values = new HashSet<>();
        values.add(correct);

        while (values.size() < 4) {
            int offset = 1 + random.nextInt(9);
            int candidate = correct + (random.nextBoolean() ? offset : -offset);
            if (candidate < 0) {
                candidate = correct + offset;
            }
            values.add(candidate);
        }

        List<Integer> options = new ArrayList<>(values);
        Collections.shuffle(options);
        return options;
    }

    private PuzzleData fetchPuzzle() throws Exception {
        String apiUrl = "https://marcconrad.com/uob/banana/api.php?out=json&base64=yes";

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("User-Agent", "BananaInvasion")
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(response.body());

        String base64Image = rootNode.get("question").asText();
        int solution = rootNode.get("solution").asInt();

        byte[] imageBytes = Base64.getDecoder().decode(base64Image);
        Image image = new Image(new ByteArrayInputStream(imageBytes));

        return new PuzzleData(image, solution);
    }

    private record PuzzleData(Image image, int solution) {
    }
}