package se233.finalcontra.view.GameStages;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import se233.finalcontra.controller.GameLoop;
import se233.finalcontra.view.PauseMenu;
import se233.finalcontra.view.Platform;
import se233.finalcontra.model.Bullet;
import se233.finalcontra.model.Enemy;
import se233.finalcontra.model.Keys;
import se233.finalcontra.model.Player;
import se233.finalcontra.model.Boss.Boss;

public abstract class GameStage extends Pane {
	protected Logger logger = LogManager.getLogger(GameStage.class);
	protected List<Platform> platforms;
	protected Label scoreLabel;
	protected Label livesLabel;
	protected PauseMenu pauseMenu;
	public static final int WIDTH = 1280;
	public static final int HEIGHT = 720;
	public static int totalMinions;
	public static boolean bossPhase = false;
	private boolean platformOutlineVisible = false;
	
	protected Image backgroundIMG;
	protected Player player;
	protected Keys keys;
	protected Boss boss;
	
	public GameStage() {
		this.keys = new Keys();
	}
	
	public void drawScore() {
		scoreLabel = new Label();
		scoreLabel.setStyle("-fx-font-weight: bold;"
				+ "-fx-font-size: 2em;");
		scoreLabel.setTextFill(Color.WHITE);
		scoreLabel.setLayoutX(WIDTH - 250);
		scoreLabel.setLayoutY(40);
		updateScoreLabel();
	}
	
	public void drawLives() {
		livesLabel = new Label("Lives: 3");
		livesLabel.setStyle("-fx-font-weight: bold;"
				+ "-fx-font-size: 2em;");
		livesLabel.setTextFill(Color.WHITE);
		livesLabel.setLayoutX(25);
		livesLabel.setLayoutY(40);
	}

	public abstract Keys getKeys();
	public abstract Player getPlayer();
	public abstract List<Platform> getPlatforms();
	
	public abstract void logging();
	public abstract Boss getBoss();
	public Label getScoreLabel() { return scoreLabel;}
	public Label getLivesLabel() { return livesLabel;}
	public abstract List<Enemy> getEnemies();
	public abstract List<Bullet> getBullets();

	protected void updateScoreLabel() {
		if (scoreLabel == null) {
			return;
		}
		scoreLabel.setText("Score: " + String.format("%06d", Math.max(0, GameLoop.getScore())));
	}
	
	public void setPlatformOutlineVisible(boolean visible) {
		platformOutlineVisible = visible;
		if (platforms == null) {
			return;
		}
		for (Platform platform : platforms) {
			platform.setOutlineVisible(visible);
		}
	}
	
	public boolean isPlatformOutlineVisible() {
		return platformOutlineVisible;
	}
}
