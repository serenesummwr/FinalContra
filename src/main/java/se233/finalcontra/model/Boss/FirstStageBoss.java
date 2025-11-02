package se233.finalcontra.model.Boss;

import javafx.geometry.Bounds;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import se233.finalcontra.controller.GameLoop;
import se233.finalcontra.model.ImageAssets;
import se233.finalcontra.model.FirstStageTurret;

public class FirstStageBoss extends Boss {
    private static final int DEFAULT_MAX_HEALTH = 3000;
    private static final int DEFEAT_SCORE = 5000;

    private final Rectangle hitboxOutline;
    private final ImageView brokenGateImage;
    private boolean gateDisplayed;
    private boolean outlineVisible = true;
    private FirstStageTurret turretGuard;

    public FirstStageBoss(int xPos, int yPos, int width, int height) {
        super(xPos, yPos, width, height, DEFAULT_MAX_HEALTH);
        hitboxOutline = new Rectangle(width, height);
        hitboxOutline.setStroke(Color.RED);
        hitboxOutline.setStrokeWidth(3);
        hitboxOutline.setFill(Color.color(1, 0, 0, 0.15));
        hitboxOutline.setMouseTransparent(true);

        brokenGateImage = new ImageView(ImageAssets.BOSS1_GATE_BROKEN);
        brokenGateImage.setFitWidth(width);
        brokenGateImage.setFitHeight(height);
        brokenGateImage.setVisible(false);
        brokenGateImage.setMouseTransparent(true);

        getChildren().addAll(hitboxOutline, brokenGateImage);
        setState(BossState.ATTACKING);
    }

    @Override
    public void update() {
        if (isDefeated()) {
            displayBrokenGate();
        } else {
            setState(BossState.ATTACKING);
        }
    }

    public void applyDamage(int amount) {
        if (getCurrentState() == BossState.DEFEATED) {
            return;
        }
        if (turretGuard != null && turretGuard.isAlive()) {
            return;
        }
        takeDamage(amount);
        if (getHealth() <= 0) {
            setState(BossState.DEFEATED);
            displayBrokenGate();
        }
    }

    public Bounds getHitboxBounds() {
        return localToParent(hitboxOutline.getBoundsInParent());
    }

    public boolean isDefeated() {
        return getCurrentState() == BossState.DEFEATED;
    }

    private void displayBrokenGate() {
        if (gateDisplayed) {
            return;
        }
        gateDisplayed = true;
        GameLoop.addScore(DEFEAT_SCORE);
        outlineVisible = false;
        hitboxOutline.setVisible(false);
        brokenGateImage.setVisible(true);
        brokenGateImage.toFront();
    }

    public void toggleHitboxOutline() {
        outlineVisible = !outlineVisible;
        hitboxOutline.setVisible(outlineVisible);
        if (outlineVisible) {
            hitboxOutline.toFront();
        } else if (brokenGateImage.isVisible()) {
            brokenGateImage.toFront();
        }
        if (turretGuard != null) {
            turretGuard.resetOutlineVisibility(outlineVisible);
        }
    }

    public boolean isHitboxOutlineVisible() {
        return outlineVisible;
    }

    public void attachTurret(FirstStageTurret turret) {
        this.turretGuard = turret;
        if (turret != null) {
            turret.resetOutlineVisibility(outlineVisible);
        }
    }

    public boolean isTurretActive() {
        return turretGuard != null && turretGuard.isAlive();
    }
}
