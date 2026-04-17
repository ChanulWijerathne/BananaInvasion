package bananainvasion;

import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;

import java.util.Objects;

public class Enemy extends Rectangle {

    // Speed controls how fast the banana falls
    private final double speed;

    public Enemy(double x, double y, double speed) {
        super(120, 120);
        this.speed = speed;

        // Set starting position of the enemy
        setLayoutX(x);
        setLayoutY(y);

        try {
            // Load banana image and use it as the enemy sprite
            Image bananaImage = new Image(
                    Objects.requireNonNull(getClass().getResourceAsStream("/images/banana.png"))
            );
            setFill(new ImagePattern(bananaImage));
        } catch (Exception e) {
            // Fallback color if image cannot be loaded
            setFill(Color.CRIMSON);
            System.out.println("Banana image not found: " + e.getMessage());
        }

        setArcWidth(20);
        setArcHeight(20);
    }

    // Called in the game loop to move the enemy downward
    public void update() {
        setLayoutY(getLayoutY() + speed);
    }
}