package se233.finalcontra.model;

import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import se233.finalcontra.controller.SpriteAnimation;

public class Effect extends Pane {
    private SpriteAnimation sprite;
    private int currentFrame = 0;
    private int totalFrames;

    public Effect(Image spriteSheet, int count, int columns, int rows, double x, double y, int displayWidth, int displayHeight) {
    	// total frame in spritesheet
        this.totalFrames = count;

        int width = (int) spriteSheet.getWidth() / columns;
        int height = (int) spriteSheet.getHeight() / rows;

        this.sprite = new SpriteAnimation(spriteSheet, 7, 7, 1, 0, 0, width, height);

        setTranslateX(x);
        setTranslateY(y);
        sprite.setFitWidth(displayWidth);
        sprite.setFitHeight(displayHeight);
        getChildren().add(this.sprite);
    }

    public void tick() {
        this.sprite.tick();
        this.currentFrame++;
    }

    public boolean isFinished() {
        return this.currentFrame >= this.totalFrames;
    }
}