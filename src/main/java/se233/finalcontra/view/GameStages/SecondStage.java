package se233.finalcontra.view.GameStages;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import se233.finalcontra.view.Platform;
import se233.finalcontra.controller.GameLoop;
import se233.finalcontra.controller.SoundController;
import se233.finalcontra.model.Bullet;
import se233.finalcontra.model.Enemy;
import se233.finalcontra.model.ImageAssets;
import se233.finalcontra.model.Keys;
import se233.finalcontra.model.FirstStageMinion;
import se233.finalcontra.model.Player;
import se233.finalcontra.model.Boss.Boss;
import se233.finalcontra.model.Boss.JavaBoss;

public class SecondStage extends GameStage {

	public SecondStage() {
		GameLoop.enemies.clear();
		SoundController.getInstance().stopAllSounds();
		drawScore();
		drawLives();
		ImageView background = new ImageView(ImageAssets.SECOND_STAGE);
		background.setFitWidth(WIDTH);
		background.setFitHeight(HEIGHT);
		platforms = new ArrayList<Platform>();
		player = new Player(60, 244 ,KeyCode.A, KeyCode.D, KeyCode.W, KeyCode.S);
		player.respawn();
		Platform upperLeftCliff = new Platform(341, 0, 308, true);
		Platform midBridge = new Platform(310, 210, 463, false);
		Platform leftBunker = new Platform(86, 21, 566, true);
		Platform centralGround = new Platform(230, 530, 563, true);
		Platform rightGround = new Platform(370, 902, 563, true);
		platforms.addAll(List.of(
			upperLeftCliff,
			midBridge,
			leftBunker,
			centralGround,
			rightGround
		));
		GameLoop.enemies.clear();

		boss = new JavaBoss(1015, 60, 128, 256, this);

		bossPhase = false;
		totalMinions = 4;
		final int minionWidth = 64;
		final int minionHeight = 64;

		int bunkerSpawnX = leftBunker.getxPos() + Math.max(0, (leftBunker.getPaneWidth() - minionWidth) / 2);
		int bridgeSpawnX = midBridge.getxPos() + Math.max(0, (midBridge.getPaneWidth() - minionWidth) / 2);
		int centralSpawnX = centralGround.getxPos() + 50;
		int rightSpawnX = rightGround.getxPos() + rightGround.getPaneWidth() - minionWidth - 20;

		FirstStageMinion bunkerGuard = new FirstStageMinion(
			bunkerSpawnX,
			leftBunker.getyPos() - minionHeight
		);
		FirstStageMinion bridgeGuard = new FirstStageMinion(
			bridgeSpawnX,
			midBridge.getyPos() - minionHeight
		);
		FirstStageMinion centralGuard = new FirstStageMinion(
			centralSpawnX,
			centralGround.getyPos() - minionHeight
		);
		FirstStageMinion rightGuard = new FirstStageMinion(
			rightSpawnX,
			rightGround.getyPos() - minionHeight
		);
		
		GameLoop.enemies.addAll(List.of(bunkerGuard, bridgeGuard, centralGuard, rightGuard));

		getChildren().addAll(background, bunkerGuard, bridgeGuard, centralGuard, rightGuard
				, upperLeftCliff, midBridge, leftBunker, centralGround, rightGround, player, boss,
				livesLabel, scoreLabel);

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
	public Boss getBoss() { return this.boss; }
	@Override
	public List<Enemy> getEnemies() { return GameLoop.enemies; }
	
	@Override
	public List<Bullet> getBullets() { return GameLoop.bullets; }
}
