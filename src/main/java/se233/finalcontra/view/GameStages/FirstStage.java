package se233.finalcontra.view.GameStages;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import se233.finalcontra.controller.GameLoop;
import se233.finalcontra.controller.SoundController;
import se233.finalcontra.model.Boss.Boss;
import se233.finalcontra.model.Bullet;
import se233.finalcontra.model.Enemy;
import se233.finalcontra.model.ImageAssets;
import se233.finalcontra.model.Keys;
import se233.finalcontra.model.Player;
import se233.finalcontra.model.Boss.FirstStageBoss;
import se233.finalcontra.view.Platform;
import se233.finalcontra.model.FirstStageMinion;
import se233.finalcontra.model.FirstStageTurret;

public class FirstStage extends GameStage {

    public FirstStage() {
        GameLoop.enemies.clear();
        GameLoop.bullets.clear();
        SoundController.getInstance().stopAllSounds();
        bossPhase = false;
        totalMinions = 0;

        drawScore();
        drawLives();

        ImageView background = new ImageView(ImageAssets.FIRST_STAGE);
        background.setFitWidth(WIDTH);
        background.setFitHeight(HEIGHT);

        platforms = new ArrayList<>();

        Platform farLeftPlatform = new Platform(170, 3, 543, false);
        Platform midBridgePlatform = new Platform(340, 428, 338, false);
        Platform leftUpperPlatform = new Platform(167, 258, 440, false);
        Platform rightUpperPlatform = new Platform(85, 769, 440, false);
        Platform centerPlatePlatform = new Platform(255, 514, 492, false);
        Platform rightSmallPlatform = new Platform(85, 854, 540, false);
        Platform groundPlatform = new Platform(638, 428, 646, true);

        platforms.addAll(List.of(
            farLeftPlatform,
            midBridgePlatform,
            leftUpperPlatform,
            rightUpperPlatform,
            centerPlatePlatform,
            rightSmallPlatform,
            groundPlatform
        ));

        int spawnX = farLeftPlatform.getxPos() + 20;
        int spawnY = farLeftPlatform.getyPos() - 64;
        player = new Player(spawnX, spawnY, KeyCode.A, KeyCode.D, KeyCode.W, KeyCode.S);

        double xScale = WIDTH / ImageAssets.FIRST_STAGE.getWidth();
        double yScale = HEIGHT / ImageAssets.FIRST_STAGE.getHeight();

        int originalHitboxX = 2225;
        int originalHitboxY = 815;
        int originalHitboxWidth = 308;
        int originalHitboxHeight = 387;

        int bossX = (int) Math.round(originalHitboxX * xScale);
        int bossY = (int) Math.round(originalHitboxY * yScale);
        int bossWidth = (int) Math.max(1, Math.round(originalHitboxWidth * xScale));
        int bossHeight = (int) Math.max(1, Math.round(originalHitboxHeight * yScale));

        int turretOriginalX = 2226;
        int turretOriginalY = 720;
        int turretOriginalWidth = 276;
        int turretOriginalHeight = 60;
        int turretBrokenOriginalWidth = 274;
        int turretBrokenOriginalHeight = 54;

        int turretX = (int) Math.round(turretOriginalX * xScale);
        int turretY = (int) Math.round(turretOriginalY * yScale);
        int turretWidth = (int) Math.max(1, Math.round(turretOriginalWidth * xScale));
        int turretHeight = (int) Math.max(1, Math.round(turretOriginalHeight * yScale));
        int turretBrokenWidth = (int) Math.max(1, Math.round(turretBrokenOriginalWidth * xScale));
        int turretBrokenHeight = (int) Math.max(1, Math.round(turretBrokenOriginalHeight * yScale));

        FirstStageBoss stageFourBoss = new FirstStageBoss(bossX, bossY, bossWidth, bossHeight);
        FirstStageTurret turret = new FirstStageTurret(turretX, turretY, turretWidth, turretHeight, turretBrokenWidth, turretBrokenHeight);
        stageFourBoss.attachTurret(turret);

        boss = stageFourBoss;

        FirstStageMinion leftMinion = new FirstStageMinion(
            farLeftPlatform.getxPos() + 40,
            farLeftPlatform.getyPos() - 64
        );
        FirstStageMinion centerMinion = new FirstStageMinion(
            centerPlatePlatform.getxPos() + 90,
            centerPlatePlatform.getyPos() - 64
        );
        FirstStageMinion rightMinion = new FirstStageMinion(
            rightSmallPlatform.getxPos() + 10,
            rightSmallPlatform.getyPos() - 64
        );

        List<FirstStageMinion> minions = List.of(leftMinion, centerMinion, rightMinion);
        GameLoop.enemies.addAll(minions);
        GameLoop.enemies.add(turret);
        totalMinions = minions.size();

        getChildren().addAll(background,
            farLeftPlatform, midBridgePlatform, leftUpperPlatform, rightUpperPlatform,
            centerPlatePlatform, rightSmallPlatform, groundPlatform,
            boss, leftMinion, centerMinion, rightMinion, turret, player,
            livesLabel, scoreLabel);

        player.respawn();
        logging();
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
