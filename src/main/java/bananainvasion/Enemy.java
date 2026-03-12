package bananainvasion;

import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;

import java.util.Objects;

public class Enemy extends Rectangle {

    private final double speed;

    public Enemy(double x, double y, double speed) {
        super(120, 120);
        this.speed = speed;

        setLayoutX(x);
        setLayoutY(y);

        try {
            Image bananaImage = new Image(
                    Objects.requireNonNull(getClass().getResourceAsStream("/images/banana.png"))
            );
            setFill(new ImagePattern(bananaImage));
        } catch (Exception e) {
            setFill(Color.CRIMSON);
            System.out.println("Banana image not found: " + e.getMessage());
        }

        setArcWidth(20);
        setArcHeight(20);
    }

    public void update() {
        setLayoutY(getLayoutY() + speed);
    }
}