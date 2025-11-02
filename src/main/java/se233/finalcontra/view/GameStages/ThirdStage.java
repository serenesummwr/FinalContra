package se233.finalcontra.view.GameStages;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import se233.finalcontra.controller.GameLoop;
import se233.finalcontra.controller.SoundController;
import se233.finalcontra.Launcher;
import se233.finalcontra.model.Boss.Boss;
import se233.finalcontra.model.Boss.ThirdBoss;
import se233.finalcontra.model.Bullet;
import se233.finalcontra.model.Enemy;
import se233.finalcontra.model.ImageAssets;
import se233.finalcontra.model.Keys;
import se233.finalcontra.model.Player;
import se233.finalcontra.view.Platform;

public class ThirdStage extends GameStage {

    private final ImageView background;
    private boolean bossDefeated;

    public ThirdStage() {
        GameLoop.enemies.clear();
        GameLoop.bullets.clear();
        SoundController.getInstance().stopAllSounds();
        bossPhase = false;
        totalMinions = 0;

        drawScore();
        drawLives();
        setStyle("-fx-background-color: black;");

        background = new ImageView(ImageAssets.THIRD_STAGE_BOSS_IDLE);
        background.setPreserveRatio(true);
        background.setFitHeight(HEIGHT);

        double backgroundScale = HEIGHT / ImageAssets.THIRD_STAGE_BOSS_IDLE.getHeight();
        double scaledBackgroundWidth = ImageAssets.THIRD_STAGE_BOSS_IDLE.getWidth() * backgroundScale;
        double backgroundOffsetX = (WIDTH - scaledBackgroundWidth) / 2.0;
        background.setLayoutX(backgroundOffsetX);

        platforms = new ArrayList<>();

        Platform bottomGround = new Platform(WIDTH, 0, 620, true);
        Platform midGround = new Platform(WIDTH, 0, 506, false);

        platforms.addAll(List.of(bottomGround, midGround));

        int spawnX = bottomGround.getxPos() + 20;
        int spawnY = bottomGround.getyPos() - 64;
        player = new Player(spawnX, spawnY, KeyCode.A, KeyCode.D, KeyCode.W, KeyCode.S);
        player.setCurrentStage(this);

        double xScale = backgroundScale;
        double yScale = backgroundScale;

        int originalHitboxX = 379;
        int originalHitboxY = 148;
        int originalHitboxWidth = 169;
        int originalHitboxHeight = 153;

        int bossX = (int) Math.round(backgroundOffsetX + originalHitboxX * xScale);
        int bossY = (int) Math.round(originalHitboxY * yScale);
        int bossWidth = (int) Math.max(1, Math.round(originalHitboxWidth * xScale));
        int bossHeight = (int) Math.max(1, Math.round(originalHitboxHeight * yScale));

        ThirdBoss stageThreeBoss = new ThirdBoss(bossX, bossY, bossWidth, bossHeight, this, this::handleBossDefeated);
        boss = stageThreeBoss;

        getChildren().addAll(background,
                bottomGround, midGround,
                player, boss,
                livesLabel, scoreLabel);

        player.respawn();
        logging();
    }

    private void handleBossDefeated() {
        if (bossDefeated) {
            return;
        }
        bossDefeated = true;
        background.setImage(ImageAssets.THIRD_STAGE_BOSS_DIE);
        SoundController.getInstance().playWinSound();

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("CONGRATULATION!");
        alert.setHeaderText("You cleared the game!");
        alert.setContentText("Thank you for playing Contra.");
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK || response == ButtonType.CLOSE) {
                Launcher.exitToMenu();
            }
        });
    }

    @Override
    public Keys getKeys() {
        return this.keys;
    }

    @Override
    public Player getPlayer() {
        return this.player;
    }

    @Override
    public List<Platform> getPlatforms() {
        return this.platforms;
    }

    @Override
    public void logging() {
        logger.info("Player spawned at X:{} Y:{}", player.getxPos(), player.getyPos());
        for (Platform platform : platforms) {
            logger.info("Platform spawned at X:{} Y:{} Width:{}", platform.getxPos(), platform.getyPos(), platform.getPaneWidth());
        }
    }

    @Override
    public Boss getBoss() {
        return this.boss;
    }

    @Override
    public List<Enemy> getEnemies() {
        return GameLoop.enemies;
    }

    @Override
    public List<Bullet> getBullets() {
        return GameLoop.bullets;
    }
}
