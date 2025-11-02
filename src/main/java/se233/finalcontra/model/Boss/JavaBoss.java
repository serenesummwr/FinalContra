package se233.finalcontra.model.Boss;

import java.util.Random;

import javafx.application.Platform;
import se233.finalcontra.controller.GameLoop;
import se233.finalcontra.controller.SoundController;
import se233.finalcontra.model.Bullet;
import se233.finalcontra.model.Enemy;
import se233.finalcontra.model.Enums.BulletOwner;
import se233.finalcontra.model.Enums.BulletType;
import se233.finalcontra.model.Enums.EnemyType;
import se233.finalcontra.model.ImageAssets;
import se233.finalcontra.model.Player;
import se233.finalcontra.model.Vector2D;
import se233.finalcontra.view.GameStages.GameStage;

public class JavaBoss extends Boss {

    private static final double SIZE_MULTIPLIER = 1.5;
    private static final int BASE_MAX_ENEMIES = 3;
    private static final int ENRAGE_TRIGGER_FRAMES = 900;
    private static final int BASE_SUMMON_DELAY = 60;
    private static final double BASE_MINION_SPEED = 3.2;
    private static final double ENRAGED_MINION_SPEED = 4.4;
    private static final double VOLLEY_SPREAD = 0.24;
    private static final double ENRAGED_VOLLEY_SPREAD = 0.32;
    private static final double VOLLEY_SPEED = 7.2;
    private static final double ENRAGED_VOLLEY_SPEED = 8.6;
    private static final int ORB_BASE_COUNT = 8;
    private static final int ORB_ENRAGED_COUNT = 12;
    private static final double ORB_SPEED = 5.8;
    private static final double ORB_ENRAGED_SPEED = 7.4;

    private final GameStage gameStage;
    private final Enemy head;
    private final Random random = new Random();

    private boolean spawned = false;
    private boolean enraged = false;
    private int framesAlive = 0;
    private int enemyTimer = 0;
    private int spawnAnimationTimer = 0;

    private AttackPattern activePattern = null;
    private int patternCooldown = 120;
    private int volleysRemaining = 0;
    private int volleyDelayTimer = 0;
    private int swarmBurstsRemaining = 0;
    private int swarmDelayTimer = 0;
    private int orbWavesRemaining = 0;
    private int orbDelayTimer = 0;
    private double orbAngleAccumulator = 0.0;

    public JavaBoss(int xPos, int yPos, int height, int width, GameStage gameStage) {
        super(xPos, yPos, (int) Math.round(width * SIZE_MULTIPLIER), (int) Math.round(height * SIZE_MULTIPLIER), 20000);
    	getWeakPoints().clear();
        this.setTranslateX(xPos);
        this.setTranslateY(yPos);
        final int scaledW = (int) Math.round(width * SIZE_MULTIPLIER);
        final int scaledH = (int) Math.round(height * SIZE_MULTIPLIER);
        head = new Enemy(xPos, yPos, 0, scaledW, scaledH, scaledW, scaledH, 1, 1, 1, ImageAssets.JAVA_IDLE, this.getMaxHealth(), EnemyType.JAVAHEAD);
        head.getSprite().setFitHeight(scaledH);
        head.getSprite().setFitWidth(scaledW);
        this.gameStage = gameStage;

        getWeakPoints().add(head);
    }

    @Override
    public void update() {
        if (GameLoop.isPaused || !spawned || getWeakPoints().isEmpty()) {
            return;
        }

        if (!head.isAlive()) {
            setState(BossState.DEFEATED);
            return;
        }

        framesAlive++;
        if (!enraged && framesAlive >= ENRAGE_TRIGGER_FRAMES) {
            enraged = true;
            patternCooldown = 45;
        }

        maintainSummons();
        updateAttackPattern();
        animateHead();
        updateWeakPointsPosition();
    }

    private void maintainSummons() {
        if (enemyTimer > 0) {
            enemyTimer--;
        }
        if (!spawned) {
            return;
        }

        int cap = enraged ? BASE_MAX_ENEMIES + 1 : BASE_MAX_ENEMIES;
        if (countActiveFlyingMinions() < cap && enemyTimer <= 0) {
            spawnMinion(enraged, false);
        }
    }

    private void updateAttackPattern() {
        if (activePattern == null) {
            if (patternCooldown > 0) {
                patternCooldown--;
            } else {
                selectNextPattern();
            }
            return;
        }

        switch (activePattern) {
            case SUMMON_SWARM -> executeSummonSwarm();
            case FOCUSED_VOLLEY -> executeFocusedVolley();
            case ORBITAL_STRIKE -> executeOrbitalStrike();
        }
    }

    private void selectNextPattern() {
        AttackPattern[] options = enraged
                ? new AttackPattern[] { AttackPattern.SUMMON_SWARM, AttackPattern.FOCUSED_VOLLEY, AttackPattern.ORBITAL_STRIKE }
                : new AttackPattern[] { AttackPattern.SUMMON_SWARM, AttackPattern.FOCUSED_VOLLEY };
        activePattern = options[random.nextInt(options.length)];
        switch (activePattern) {
            case SUMMON_SWARM -> {
                swarmBurstsRemaining = enraged ? 4 : 3;
                swarmDelayTimer = 10;
            }
            case FOCUSED_VOLLEY -> {
                volleysRemaining = enraged ? 4 : 3;
                volleyDelayTimer = 18;
            }
            case ORBITAL_STRIKE -> {
                orbWavesRemaining = 3;
                orbDelayTimer = 0;
                orbAngleAccumulator = random.nextDouble() * Math.PI * 2;
            }
        }
    }

    private void executeSummonSwarm() {
        if (swarmDelayTimer > 0) {
            swarmDelayTimer--;
            return;
        }

        spawnMinion(true, true);
        if (enraged) {
            spawnMinion(true, true);
        }
        swarmBurstsRemaining--;
        swarmDelayTimer = enraged ? 25 : 32;
        if (swarmBurstsRemaining <= 0) {
            endCurrentPattern();
        }
    }

    private void executeFocusedVolley() {
        if (volleyDelayTimer > 0) {
            volleyDelayTimer--;
            return;
        }

        fireVolley();
        volleysRemaining--;
        volleyDelayTimer = enraged ? 22 : 28;
        if (volleysRemaining <= 0) {
            endCurrentPattern();
        }
    }

    private void executeOrbitalStrike() {
        if (orbDelayTimer > 0) {
            orbDelayTimer--;
            return;
        }

        fireOrbitalWave();
        orbWavesRemaining--;
        orbDelayTimer = 18;
        orbAngleAccumulator += Math.PI / 6;
        if (orbWavesRemaining <= 0) {
            endCurrentPattern();
        }
    }

    private void fireVolley() {
        Player player = gameStage.getPlayer();
        Vector2D origin = getHeadCenterOnStage();
        Vector2D direction = aimAtPlayer(origin, player);
        int projectiles = enraged ? 5 : 4;
        double spread = enraged ? ENRAGED_VOLLEY_SPREAD : VOLLEY_SPREAD;
        double baseSpeed = enraged ? ENRAGED_VOLLEY_SPEED : VOLLEY_SPEED;

        double centerIndex = (projectiles - 1) / 2.0;
        for (int i = 0; i < projectiles; i++) {
            double offset = (i - centerIndex) * spread;
            Vector2D rotated = rotate(direction, offset);
            spawnJavaOrb(origin, rotated, baseSpeed);
        }
        SoundController.getInstance().playJavaAttackSound();
    }

    private void fireOrbitalWave() {
        Vector2D origin = getHeadCenterOnStage();
        int projectileCount = enraged ? ORB_ENRAGED_COUNT : ORB_BASE_COUNT;
        double speed = enraged ? ORB_ENRAGED_SPEED : ORB_SPEED;
        for (int i = 0; i < projectileCount; i++) {
            double angle = orbAngleAccumulator + (2 * Math.PI / projectileCount) * i;
            Vector2D direction = new Vector2D(Math.cos(angle), Math.sin(angle));
            spawnJavaOrb(origin, direction, speed);
        }
        SoundController.getInstance().playLaserSound();
    }

    private void spawnMinion(boolean aggressive, boolean forced) {
        int cap = enraged ? BASE_MAX_ENEMIES + 1 : BASE_MAX_ENEMIES;
        if (!forced && countActiveFlyingMinions() >= cap) {
            return;
        }

        double speed = aggressive ? ENRAGED_MINION_SPEED : BASE_MINION_SPEED;
        int health = aggressive ? 70 : 50;
        int spawnX = -50;
        int spawnY = 60 + random.nextInt(140);

        Enemy enemy = new Enemy(spawnX, spawnY, speed, 64, 64, 64, 64, 4, 4, 1, ImageAssets.JAVA_SKILL, health, EnemyType.FLYING);
        GameLoop.enemies.add(enemy);
        SoundController.getInstance().playJavaAttackSound();
        Platform.runLater(() -> getChildren().add(enemy));

        enemyTimer = forced ? BASE_SUMMON_DELAY / 2 : BASE_SUMMON_DELAY;
        spawnAnimationTimer = 20;
    }

    private int countActiveFlyingMinions() {
        int aliveCount = 0;
        for (Enemy enemy : GameLoop.enemies) {
            if (enemy.isAlive() && enemy.getType() == EnemyType.FLYING) {
                aliveCount++;
            }
        }
        return aliveCount;
    }

    private void spawnJavaOrb(Vector2D origin, Vector2D direction, double speed) {
        Vector2D normalized = normalize(direction);
        Bullet bullet = new Bullet(origin, normalized, speed, BulletOwner.ENEMY, BulletType.JAVA_ORB);
        GameLoop.bullets.add(bullet);
        Platform.runLater(() -> {
            if (!gameStage.getChildren().contains(bullet)) {
                gameStage.getChildren().add(bullet);
            }
        });
    }

    private void animateHead() {
        if (spawnAnimationTimer > 0) {
            spawnAnimationTimer--;
        }
        head.getSprite().changeSpriteSheet(ImageAssets.JAVA_IDLE, 1, 1, 1);
    }

    public void spawn() {
        if (spawned) {
            return;
        }
        spawned = true;
        framesAlive = 0;
        enemyTimer = BASE_SUMMON_DELAY;
        patternCooldown = 120;
        activePattern = null;
        enraged = false;
        if (!GameLoop.enemies.contains(head)) {
            GameLoop.enemies.add(head);
        }
        head.setTranslateX(0);
        head.setTranslateY(0);
        head.setVisible(true);
        Platform.runLater(() -> {
            if (!getChildren().contains(head)) {
                getChildren().add(head);
            }
        });
        setState(BossState.ATTACKING);
    }

    public boolean isSpawned() {
        return spawned;
    }

    private Vector2D aimAtPlayer(Vector2D origin, Player player) {
        if (player == null) {
            return new Vector2D(-1, 0);
        }
        Vector2D playerCenter = new Vector2D(
                player.getxPos() + Player.width / 2.0,
                player.getyPos() + Player.height / 2.0
        );
        return normalize(playerCenter.subtract(origin));
    }

    private Vector2D getHeadCenterOnStage() {
        double x = getTranslateX() + head.getTranslateX() + head.getW() / 2.0;
        double y = getTranslateY() + head.getTranslateY() + head.getH() / 2.0;
        return new Vector2D(x, y);
    }

    private Vector2D rotate(Vector2D vector, double radians) {
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);
        return new Vector2D(
                vector.getX() * cos - vector.getY() * sin,
                vector.getX() * sin + vector.getY() * cos
        );
    }

    private Vector2D normalize(Vector2D vector) {
        Vector2D normalized = vector != null ? vector.normalize() : new Vector2D(0, 0);
        if (normalized.getLength() == 0) {
            return new Vector2D(-1, 0);
        }
        return normalized;
    }

    private void endCurrentPattern() {
        activePattern = null;
        volleyDelayTimer = 0;
        volleysRemaining = 0;
        swarmDelayTimer = 0;
        swarmBurstsRemaining = 0;
        orbDelayTimer = 0;
        orbWavesRemaining = 0;
        patternCooldown = enraged ? 60 + random.nextInt(50) : 90 + random.nextInt(60);
    }

    private enum AttackPattern {
        SUMMON_SWARM,
        FOCUSED_VOLLEY,
        ORBITAL_STRIKE
    }
}
