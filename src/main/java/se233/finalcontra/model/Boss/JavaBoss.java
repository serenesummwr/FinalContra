package se233.finalcontra.model.Boss;

import javafx.scene.image.Image;
import se233.finalcontra.Launcher;
import se233.finalcontra.controller.GameLoop;
import se233.finalcontra.controller.SoundController;
import se233.finalcontra.model.Enemy;
import se233.finalcontra.model.ImageAssets;
import se233.finalcontra.model.Enums.EnemyType;
import se233.finalcontra.view.GameStages.GameStage;


public class JavaBoss extends Boss{

    private static final double SIZE_MULTIPLIER = 1.5; // Increase boss size by 50%
    private final int maxEnemies = 3;
    private int enemyTimer = 0;
    private GameStage gameStage;
    private final Enemy Head;
    private int spawnAnimationTimer = 0;

    public JavaBoss(int xPos, int yPos , int Height, int Width, GameStage gameStage) {
        super(xPos, yPos, (int) Math.round(Width * SIZE_MULTIPLIER), (int) Math.round(Height * SIZE_MULTIPLIER), 20000);
    	getWeakPoints().clear();
        this.setTranslateX(xPos);
        this.setTranslateY(yPos);
        final int scaledW = (int) Math.round(Width * SIZE_MULTIPLIER);
        final int scaledH = (int) Math.round(Height * SIZE_MULTIPLIER);
        Head = new Enemy(xPos, yPos, 0, scaledW, scaledH, scaledW, scaledH, 1, 1, 1, ImageAssets.JAVA_IDLE, this.getMaxHealth(), EnemyType.JAVAHEAD);
        Head.getSprite().setFitHeight(scaledH);
        Head.getSprite().setFitWidth(scaledW);
        this.gameStage = gameStage;

        getWeakPoints().add(Head);
        GameLoop.enemies.add(Head);
        javafx.application.Platform.runLater(() -> {
            this.getChildren().addAll(Head);
        });
    }

    @Override
    protected void handleAttackingState() {
        if (Head.isAlive()) {
            spawnEnemy();
            if (getSpawnAnimationTimer() > 0) {
                updateSpawnAnimation();
                Head.getSprite().changeSpriteSheet(new Image(Launcher.class.getResourceAsStream("assets/Boss/Boss2/JAVA_IDLE.png")), 1, 1, 1);
            }  else {
                Head.getSprite().changeSpriteSheet(new Image(Launcher.class.getResourceAsStream("assets/Boss/Boss2/JAVA_IDLE.png")), 1, 1, 1);
            }
        }

    }

    private void spawnEnemy() {

        if (enemyTimer > 0) {
            enemyTimer--;
            return;
        }
        int aliveCount = 0;
        for (Enemy enemy : GameLoop.enemies) {
            if (enemy.isAlive() && enemy.getType() == EnemyType.FLYING) {
                aliveCount++;
            }
        }

        if (aliveCount < maxEnemies) {
            int spawnX = -50;
            int spawnY = +100;


            Enemy enemy = new Enemy(spawnX, spawnY, 3, 64, 64, 64, 64, 4, 4, 1, ImageAssets.JAVA_SKILL, 50, EnemyType.FLYING);
            // NOTE: Get children's global position do not touch!!!!
            //System.out.print("Enemy Bound: " + getLocalToParentTransform());
            GameLoop.enemies.add(enemy);
            SoundController.getInstance().playJavaAttackSound();
            javafx.application.Platform.runLater(() -> {
                this.getChildren().add(enemy);
            });

            enemyTimer = 80;
            spawnAnimationTimer = 20;

            if (aliveCount == maxEnemies) {
                enemyTimer = 500;
            }
        }
    }

    public int getSpawnAnimationTimer() {
        return spawnAnimationTimer;
    }

    public void updateSpawnAnimation() {
        if (spawnAnimationTimer > 0) {
            spawnAnimationTimer--;
        }
    }
}
