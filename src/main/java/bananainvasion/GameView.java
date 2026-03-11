package bananainvasion;

import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.util.*;

public class GameView {

    private static final int WIDTH = 1000;
    private static final int HEIGHT = 700;
    private static final int ISLAND_Y = 590;

    private final StackPane root = new StackPane();
    private final Pane gamePane = new Pane();

    private final Rectangle island = new Rectangle(WIDTH, 110);
    private final Rectangle player = new Rectangle(55, 55);

    private final Label titleLabel = new Label("Banana Invasion");
    private final Label subtitleLabel = new Label("Guest Mode - Defend the island from invading bananas");
    private final Label scoreLabel = new Label("Score: 0");
    private final Label infoLabel = new Label("Move: A / D or ← / →   Shoot: SPACE");

    private final List<Enemy> enemies = new ArrayList<>();
    private final List<Rectangle> bullets = new ArrayList<>();
    private final Set<KeyCode> pressedKeys = new HashSet<>();

    private final Random random = new Random();

    private AnimationTimer gameLoop;
    private Timeline enemySpawner;

    private boolean running = false;
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
        scoreLabel.setText("Score: 0");
        clearObjects();
        player.setLayoutX(WIDTH / 2.0 - 27);
        player.setLayoutY(ISLAND_Y - 70);
        startMainLoop();
    }

    private void buildUI() {
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #87ceeb, #dbeafe);");

        gamePane.setPrefSize(WIDTH, HEIGHT);

        island.setFill(Color.DARKSEAGREEN);
        island.setLayoutY(ISLAND_Y);

        player.setFill(Color.DODGERBLUE);
        player.setArcWidth(10);
        player.setArcHeight(10);
        player.setLayoutX(WIDTH / 2.0 - 27);
        player.setLayoutY(ISLAND_Y - 70);

        titleLabel.setStyle("-fx-font-size: 30px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");
        subtitleLabel.setStyle("-fx-font-size: 15px; -fx-text-fill: #1e293b;");
        scoreLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");
        infoLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #1e293b;");

        VBox hud = new VBox(6, titleLabel, subtitleLabel, scoreLabel, infoLabel);
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

        enemySpawner = new Timeline(new KeyFrame(Duration.seconds(1.2), e -> spawnEnemy()));
        enemySpawner.setCycleCount(Timeline.INDEFINITE);
        enemySpawner.play();
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
        double x = 50 + random.nextInt(WIDTH - 100);
        double speed = 2 + random.nextDouble() * 1.5;

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
                showGameOverOverlay();
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

    private void showGameOverOverlay() {
        running = false;
        gameLoop.stop();
        if (enemySpawner != null) enemySpawner.stop();

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
}
