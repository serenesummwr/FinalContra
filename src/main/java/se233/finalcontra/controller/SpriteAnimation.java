package se233.finalcontra.controller;

import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import se233.finalcontra.model.SpriteDefinition;


public class SpriteAnimation extends ImageView {

    int count, columns, rows, offsetX, offsetY, width, height, curIndex, curColumnIndex =0 , curRowIndex = 0;
    Image currentImage;
    private long lastFrameTime = 0;
    private long frameDuration = 200_000_000;
    private SpriteDefinition currentDefinition;
    private int completedLoops = 0;

    public SpriteAnimation(Image image, int count, int columns, int rows, int offsetX, int offsetY, int width, int height) {
        setSheet(image, count, columns, rows, offsetX, offsetY, width, height);
    }

    public SpriteAnimation(SpriteDefinition definition) {
        this(definition.image(), definition.frameCount(), definition.columns(), definition.rows(), definition.offsetX(),
                definition.offsetY(), definition.frameWidth(), definition.frameHeight());
        this.currentDefinition = definition;
        this.frameDuration = definition.frameDurationNanos();
    }

    private void setSheet(Image image, int count, int columns, int rows, int offsetX, int offsetY, int width, int height) {
        this.setImage(image);
        this.currentImage = image;
        this.count = count;
        this.columns = columns;
        this.rows = rows;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.width = width;
        this.height = height;
        this.curIndex = 0;
        this.curColumnIndex = 0;
        this.curRowIndex = 0;
        this.setViewport(new Rectangle2D(offsetX, offsetY, width, height));
        this.lastFrameTime = System.nanoTime();
        this.completedLoops = 0;
    }
    public void tick() {
        long now = System.nanoTime();
        if (now - lastFrameTime >= frameDuration && count > 0) {
            int previousIndex = curIndex;
            curIndex = (curIndex + 1) % count;
            curColumnIndex = curIndex % columns;
            curRowIndex = curIndex / columns;
            if (curIndex == 0 && (previousIndex != 0 || count == 1)) {
                completedLoops++;
            }
            interpolate();
            lastFrameTime = now;
        }
    }
    
    public void changeSpriteSheet(Image image, int count, int columns, int rows) {
        if (this.currentImage != image || this.count != count || this.columns != columns || this.rows != rows) {
            setSheet(image, count, columns, rows, 0, 0, (int) image.getWidth() / columns, (int) image.getHeight() / rows);
            this.frameDuration = 200_000_000;
            this.currentDefinition = null;
            this.completedLoops = 0;
            interpolate();
        }
    }

    public void changeSpriteSheet(SpriteDefinition definition) {
        if (this.currentDefinition != definition) {
            setSheet(definition.image(), definition.frameCount(), definition.columns(), definition.rows(), definition.offsetX(),
                    definition.offsetY(), definition.frameWidth(), definition.frameHeight());
            this.frameDuration = definition.frameDurationNanos();
            this.currentDefinition = definition;
            this.completedLoops = 0;
            interpolate();
        }
    }
    
    protected void interpolate() {
        final int x = curColumnIndex * width + offsetX;
        final int y = curRowIndex * height + offsetY;
        this.setViewport(new Rectangle2D(x, y, width, height));
    }

    public SpriteDefinition getCurrentDefinition() {
        return currentDefinition;
    }

    public int getCurrentFrameIndex() {
        return curIndex;
    }

    public int getFrameCount() {
        return count;
    }

    public void setFrameIndex(int frameIndex) {
        if (count == 0) {
            return;
        }
        int clamped = Math.max(0, Math.min(frameIndex, count - 1));
        curIndex = clamped;
        curColumnIndex = curIndex % columns;
        curRowIndex = curIndex / columns;
        interpolate();
    }

    public boolean isAtLastFrame() {
        return count > 0 && curIndex == count - 1;
    }

    public void resetLoopCounter() {
        this.completedLoops = 0;
    }

    public int getCompletedLoops() {
        return completedLoops;
    }
    
}