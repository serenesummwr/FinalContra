package se233.finalcontra.model.Boss;

import javafx.scene.layout.Pane;
import se233.finalcontra.controller.GameLoop;
import se233.finalcontra.model.Enemy;

import java.util.ArrayList;
import java.util.List;

public class Boss extends Pane {

    private int xPos, yPos;
    private int health;
    private final int maxHealth;
    private boolean isAlive;
    private BossState currentState;
    protected int shootTimer = 10;
    protected int idleTimer = 5;
    protected int dieTimer = 20;
    
    private boolean isBossSpawned;
    private int width, height;
    private List<Enemy> weakPoints;

    public Boss(int xPos, int yPos, int width, int height, int maxHealth) {
    	setTranslateX(xPos);
    	setTranslateY(yPos);
    	this.setHeight(height);
    	this.setWidth(width);
        this.xPos = xPos;
        this.yPos = yPos;
        this.width = width;
        this.height = height;
        this.maxHealth = maxHealth;
        this.health = maxHealth;
        this.isAlive = true;
        this.currentState = BossState.IDLE;
        this.weakPoints = new ArrayList<>();
    }

    public void update() {
        if (!isAlive || GameLoop.isPaused) {
            return;
        }
        switch (currentState) {
            case IDLE:       handleIdleState();       break;
            case ATTACKING:  handleAttackingState();  break;
            case DEFEATED:   handleDefeatedState();   break;
        }
        updateWeakPointsPosition();
    }


    protected void handleIdleState() {
        if (idleTimer > 0) {
            idleTimer--;
            return;
        } else {
            setState(BossState.ATTACKING);
            idleTimer = 5;
        }
    }

    protected void handleAttackingState() {
        if (shootTimer > 0) {
            shootTimer--;
            return;
        } else {
            setState(BossState.IDLE);
            shootTimer = 10;
        }
    }

    protected void handleDefeatedState() {
        if (dieTimer > 0) {
            idleTimer--;
            return;
        } else {
            setState(BossState.DEFEATED);
            dieTimer = 20;
        }
    }

    public void takeDamage(int amount) {
        if (currentState == BossState.ATTACKING) {
            this.health -= amount;
            if (this.health <= 0) {
                this.health = 0;
                setState(BossState.DEFEATED);
            }
        }
    }

    public void updateWeakPointsPosition() {
        // This method can be overridden by stationary bosses
        if (!weakPoints.isEmpty()) {
            // Update positions relative to the Pane's origin (0,0)
            weakPoints.get(0).setTranslateX(0);
            weakPoints.get(0).setTranslateY(0);
        }
    }

    protected void setState(BossState newState) {
        this.currentState = newState;
    }

    // Getters
    public int getXPos() { return xPos; }
    public int getYPos() { return yPos; }
    public int getHealth() { return health; }
    public int getMaxHealth() { return maxHealth; }
    public int getheight() { return height; }
    public int getwidth() { return width; }
    public BossState getCurrentState() { return currentState; }
    public boolean isDefeated() { return isDefeated(); }
    public boolean isAlive() { return isAlive; }
    public List<Enemy> getWeakPoints() { return weakPoints; }
}