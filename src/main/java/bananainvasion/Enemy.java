package bananainvasion;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class Enemy extends Rectangle {

    private final double speed;

    public Enemy(double x, double y, double speed) {
        super(35, 55);
        this.speed = speed;

        setLayoutX(x);
        setLayoutY(y);

        setFill(Color.CRIMSON);
        setStroke(Color.DARKRED);
        setArcWidth(20);
        setArcHeight(20);
        setRotate(20);
    }

    public void update() {
        setLayoutY(getLayoutY() + speed);
    }
}
