package se233.finalcontra.view.GameStages;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import se233.finalcontra.controller.GameLoop;
import se233.finalcontra.controller.SoundController;
import se233.finalcontra.model.*;
import se233.finalcontra.model.Boss.Boss;
import se233.finalcontra.model.Boss.FirstStageBoss;
// import se233.finalcontra.model.Boss.FirstStageBoss; // คอมเมนต์ออก
import se233.finalcontra.view.Platform;
// import se233.finalcontra.model.FirstStageMinion; // คอมเมนต์ออก
// import se233.finalcontra.model.FirstStageTurret; // คอมเมนต์ออก

public class ThirdStage extends GameStage {

    public ThirdStage() {
        GameLoop.enemies.clear();
        GameLoop.bullets.clear();
        SoundController.getInstance().stopAllSounds();
        bossPhase = false;
        totalMinions = 0;

        drawScore();
        drawLives();

        // 1. ฉากหลัง (Background)
        ImageView background = new ImageView(ImageAssets.THIRD_STAGE_BOSS_IDLE);
        background.setFitWidth(WIDTH);
        background.setFitHeight(HEIGHT);

        platforms = new ArrayList<>();

        Platform bottomGround = new Platform(WIDTH, 0, 620, true);
        Platform midGround = new Platform(WIDTH, 0, 506, false);


        platforms.addAll(List.of(
                bottomGround,
                midGround

        ));


        // **คอมเมนต์ส่วนที่เกี่ยวข้องกับ Player, Boss, และ Minions ออกทั้งหมด**


        int spawnX = bottomGround.getxPos() + 20;
        int spawnY = bottomGround.getyPos() - 64;
        player = new Player(spawnX, spawnY, KeyCode.A, KeyCode.D, KeyCode.W, KeyCode.S);
        player.setCurrentStage(this);

        double xScale = WIDTH / ImageAssets.THIRD_STAGE_BOSS_IDLE.getWidth();
        double yScale = HEIGHT / ImageAssets.THIRD_STAGE_BOSS_IDLE.getHeight();

        int originalHitboxX = 379;
        int originalHitboxY = 148;
        int originalHitboxWidth = 169;
        int originalHitboxHeight = 153;

        int bossX = (int) Math.round(originalHitboxX * xScale);
        int bossY = (int) Math.round(originalHitboxY * yScale);
        int bossWidth = (int) Math.max(1, Math.round(originalHitboxWidth * xScale));
        int bossHeight = (int) Math.max(1, Math.round(originalHitboxHeight * yScale));





        FirstStageBoss stageThreeBoss = new FirstStageBoss(bossX, bossY, bossWidth, bossHeight);


        boss = stageThreeBoss;





        // 3. การเพิ่ม Element เข้าสู่ฉาก (Add to Scene)
        getChildren().addAll(background,
                bottomGround, midGround,
                player,boss,
                livesLabel, scoreLabel);

          player.respawn();
        logging();
    }

    // ... (ส่วน Methods ที่เหลือ ให้คงไว้ตามเดิม หรือปรับแก้ตามที่จำเป็น)

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