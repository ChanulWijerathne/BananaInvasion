package bananainvasion;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;
import java.util.Objects;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

public class GameView {

    private static final int WIDTH = 1000;
    private static final int HEIGHT = 700;
    private static final int ISLAND_Y = 590;

    private final StackPane root = new StackPane();
    private final Pane gamePane = new Pane();

    private final Rectangle island = new Rectangle(WIDTH, 110);
    private final javafx.scene.shape.Rectangle player = new javafx.scene.shape.Rectangle(150, 180);

    private final Label titleLabel = new Label("Banana Invasion");
    private final Label scoreLabel = new Label("Score: 0");

    private final List<Enemy> enemies = new ArrayList<>();
    private final List<Rectangle> bullets = new ArrayList<>();
    private final Set<KeyCode> pressedKeys = new HashSet<>();

    private final Random random = new Random();

    private AnimationTimer gameLoop;
    private Timeline enemySpawner;
    private boolean running = false;
    private boolean reviveUsed = false;
    private int score = 0;

    public GameView() {
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

        player.setLayoutX(WIDTH / 2.0 - 45);
        player.setLayoutY(ISLAND_Y - 115);

        startMainLoop();
    }

    private void buildUI() {
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #87ceeb, #dbeafe);");

        gamePane.setPrefSize(WIDTH, HEIGHT);

        island.setFill(Color.DARKSEAGREEN);
        island.setLayoutY(ISLAND_Y);


        try {
            Image playerImage = new Image(
                    Objects.requireNonNull(getClass().getResourceAsStream("/images/player.png"))
            );
            player.setFill(new ImagePattern(playerImage));
        } catch (Exception e) {
            player.setFill(Color.DODGERBLUE);
            System.out.println("Player image not found: " + e.getMessage());
        }

        player.setArcWidth(10);
        player.setArcHeight(10);
        player.setLayoutX(WIDTH / 2.0 - 45);
        player.setLayoutY(ISLAND_Y - 115);

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
        gameLoop.stop();
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
        bullet.setLayoutX(player.getLayoutX() + (player.getWidth() / 2.0) - 3);
        bullet.setLayoutY(player.getLayoutY() - 10);

        bullets.add(bullet);
        gamePane.getChildren().add(bullet);
    }

    private void spawnEnemy() {

        if (enemies.size() >= 3) {
            return;
        }

        double x = 60 + random.nextInt(WIDTH - 200);
        double speed = 1.4 + random.nextDouble() * 0.7;

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
            showGameOverOverlay();
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
                puzzleImageView.setImage(puzzle.image());

                List<Integer> options = generateOptions(puzzle.solution());

                final Timeline[] reviveTimerHolder = new Timeline[1];
                final boolean[] answered = {false};

                for (int i = 0; i < 4; i++) {
                    int optionValue = options.get(i);

                    Button btn = new Button(String.valueOf(optionValue));
                    btn.setPrefWidth(120);
                    btn.setPrefHeight(45);
                    btn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");

                    btn.setOnAction(e -> {
                        if (answered[0]) return;
                        answered[0] = true;

                        if (reviveTimerHolder[0] != null) {
                            reviveTimerHolder[0].stop();
                        }

                        root.getChildren().remove(overlay);

                        if (optionValue == puzzle.solution()) {
                            clearObjects();
                            startMainLoop();
                        } else {
                            showGameOverOverlay();
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

                Timeline reviveTimer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
                    if (answered[0]) return;

                    timeLeft[0]--;
                    timerLabel.setText(String.valueOf(timeLeft[0]));

                    if (timeLeft[0] <= 5) {
                        timerLabel.setTextFill(Color.RED);
                    }

                    if (timeLeft[0] <= 0) {
                        answered[0] = true;
                        root.getChildren().remove(overlay);
                        showGameOverOverlay();
                    }
                }));

                reviveTimer.setCycleCount(15);
                reviveTimerHolder[0] = reviveTimer;
                reviveTimer.play();

            } catch (Exception e) {
                root.getChildren().remove(overlay);
                showGameOverOverlay();
            }
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

    private void showGameOverOverlay() {
        VBox overlay = new VBox(18);
        overlay.setAlignment(Pos.CENTER);
        overlay.setPrefSize(500, 300);
        overlay.setMaxSize(500, 300);
        overlay.setStyle("-fx-background-color: rgba(15, 23, 42, 0.96); -fx-background-radius: 16; -fx-padding: 25;");

        Label title = new Label("Game Over");
        title.setStyle("-fx-font-size: 34px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label finalScore = new Label("Final Score: " + score);
        finalScore.setStyle("-fx-font-size: 20px; -fx-text-fill: #cbd5e1;");

        Button playAgain = new Button("Play Again");
        playAgain.setPrefWidth(160);
        playAgain.setPrefHeight(45);
        playAgain.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");

        playAgain.setOnAction(e -> {
            root.getChildren().remove(overlay);
            startGame();
        });

        overlay.getChildren().addAll(title, finalScore, playAgain);
        root.getChildren().add(overlay);
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