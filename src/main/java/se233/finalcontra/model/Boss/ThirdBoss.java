package se233.finalcontra.model.Boss;

import javafx.geometry.Bounds;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import se233.finalcontra.model.ImageAssets;
import se233.finalcontra.model.FirstStageTurret;

public class ThirdBoss extends Boss {
    private static final int DEFAULT_MAX_HEALTH = 3000;

    private final Rectangle hitboxOutline;
    private final ImageView ThirdBoss_Die;
    private boolean bossDisplayed;
    private boolean outlineVisible = true;

    public ThirdBoss(int xPos, int yPos, int width, int height) {
        super(xPos, yPos, width, height, DEFAULT_MAX_HEALTH);
        hitboxOutline = new Rectangle(width, height);
        hitboxOutline.setStroke(Color.RED);
        hitboxOutline.setStrokeWidth(3);
        hitboxOutline.setFill(Color.color(1, 0, 0, 0.15));
        hitboxOutline.setMouseTransparent(true);

        ThirdBoss_Die = new ImageView(ImageAssets.THIRD_STAGE_BOSS_DIE);
        ThirdBoss_Die.setFitWidth(width);
        ThirdBoss_Die.setFitHeight(height);
        ThirdBoss_Die.setVisible(false);
        ThirdBoss_Die.setMouseTransparent(true);

        getChildren().addAll(hitboxOutline, ThirdBoss_Die);
        setState(BossState.ATTACKING);
    }

    @Override
    public void update() {
        if (isDefeated()) {
            DisplayBossDie();
        } else {
            setState(BossState.ATTACKING);
        }
    }


    public Bounds getHitboxBounds() {
        return localToParent(hitboxOutline.getBoundsInParent());
    }

    public boolean isDefeated() {
        return getCurrentState() == BossState.DEFEATED;
    }

    private void DisplayBossDie() {
        if (bossDisplayed) {
            return;
        }
        bossDisplayed = true;
        outlineVisible = false;
        hitboxOutline.setVisible(false);
        ThirdBoss_Die.setVisible(true);
        ThirdBoss_Die.toFront();
    }

    public void toggleHitboxOutline() {
        outlineVisible = !outlineVisible;
        hitboxOutline.setVisible(outlineVisible);
        if (outlineVisible) {
            hitboxOutline.toFront();
        } else if (ThirdBoss_Die.isVisible()) {
            ThirdBoss_Die.toFront();
        }

    }

    public boolean isHitboxOutlineVisible() {
        return outlineVisible;
    }


}
