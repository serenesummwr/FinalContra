package se233.finalcontra;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.ArrayList;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import se233.finalcontra.controller.GameLoop;
import se233.finalcontra.model.Player;
import se233.finalcontra.model.Enums.ShootingDirection;
import se233.finalcontra.view.GameStages.FirstStage;
import se233.finalcontra.view.GameStages.GameStage;

public class PlayerTest  {
	Player player;
	@BeforeAll
	public static void initJfxRuntime() {
	    javafx.application.Platform.startup(() -> {});
	}
	
	@BeforeEach
	public void setUp() throws NoSuchFieldException{
		player = new Player(0, 0, KeyCode.A, KeyCode.D, KeyCode.W, KeyCode.S);
	}
	
	@Test
	public void respawn_playerPositionIs0and0() {
		player.respawn();
		assertEquals(0, player.getxPos(), "Start X should be 0");
		assertEquals(0, player.getyPos(), "Start Y shoud be 0");
	}
	
	@Test
	public void moveRightOnce_xPosIncreaseBy1() throws Exception{
		ReflectionHelper.setField(player, "xPos", 0);
		player.moveRight();
		player.moveX();
		assertEquals(1, player.getxPos(), "xPos should be 1");
	}
	@Test
	public void moveLeft_xPosDecreaseByXVelocity() throws Exception {
		ReflectionHelper.setField(player, "xPos", 0);
		player.moveLeft();
		player.moveX();
		assertEquals(-1, player.getxPos(), "xPos should be -1");
	}
	@Test 
	public void respawn_xPosShouldBe0_moveToLeftBorder_thenCheckWallCollision() throws Exception {
		ReflectionHelper.setField(player, "xPos", 0);
		player.respawn();
		player.moveLeft();
		player.moveX();
		player.checkStageBoundaryCollision();
		assertEquals(0, player.getxPos(), "xPos should be 0");
	}
	@Test
	public void respawn_xPosShouldBe1280_moveToRightBorder_thenCheckWallCollision() throws Exception {
		player.respawn();
		int width = (int) ReflectionHelper.getField(player, "width");
		ReflectionHelper.setField(player, "xPos", GameStage.WIDTH - width);
		player.moveRight();
		player.moveX();
		player.checkStageBoundaryCollision();
		assertEquals(GameStage.WIDTH-player.width, player.getxPos(), "xPos should be 1216");
	}
	
	@Test
	public void givePlayer2Lives_playerDieOnce_thenLivesWillDecreaseBy1() throws Exception {
		ReflectionHelper.setField(player, "lives", 2);
		player.die();
		int lives = (int) ReflectionHelper.getField(player, "lives");
		assertEquals(1, lives, "Player lives should decrease by 1");
	}
	
	@Test
	public void playerAtCoordinate0_0_moveRight_theCoordinateIncreaseByXVelocity() throws Exception {
		ReflectionHelper.setField(player, "xPos", 0);
		ReflectionHelper.setField(player, "xVelocity", 5);
		double xVelocity = (double) ReflectionHelper.getField(player, "xVelocity");
		player.moveRight();
		player.moveX();
		int currentxPos = player.getxPos();
		assertEquals(xVelocity, currentxPos, "xPos moved based on xVelocity.");
	}
	
	@Test
	public void playerAtCoordinate0_0_moveLeft_theCoordinateDecreaseByXVelocity() throws Exception {
		ReflectionHelper.setField(player, "xPos", 0);
		ReflectionHelper.setField(player, "xVelocity", 5);
		double xVelocity = (double) ReflectionHelper.getField(player, "xVelocity");
		player.moveLeft();
		player.moveX();
		int currentxPos = player.getxPos();
		assertEquals(-xVelocity, currentxPos, "xPos moved based on yVelocity.");
	}
	
	@Test
	public void playerAtCoordinate0_10_jump_theCoordinateYDecreaseByYVelocity() throws Exception {
		ReflectionHelper.setField(player, "yPos", 0);
		ReflectionHelper.setField(player, "yVelocity", 0);
		double yVelocity = (double) ReflectionHelper.getField(player, "yVelocity");
		player.jump();
		player.moveY();
		int currentyPos = player.getyPos();
		assertEquals(yVelocity, currentyPos, "Player position is increase by yVelocity of 10");
	}
	
	@Test
	public void disableKey_thenCheckTheKey_isNull() {
		player.disableKeys();
		assertNull(player.getDownKey(), "Down key is null.");
		assertNull(player.getUpKey(), "Up key is null.");
		assertNull(player.getLeftKey(), "Left key is null.");
		assertNull(player.getRightKey(), "Right key is null.");
		assertNull(player.getShootKey(), "Shoot key is null.");
		assertNull(player.getJumpKey(), "Jump key is null.");
	}
	
	@Test
	public void disableKeyOnce_andEnableKeyAgain_thenCheckTheKey_isDefined() {
		player.disableKeys();
		player.enableKeys();
		assertNotNull(player.getDownKey(), "Down key is not null.");
		assertNotNull(player.getUpKey(), "Up key is not null.");
		assertNotNull(player.getLeftKey(), "Left key is not null.");
		assertNotNull(player.getRightKey(), "Right key is not null.");
		assertNotNull(player.getShootKey(), "Shoot key is not null.");
		assertNotNull(player.getJumpKey(), "Jump key is not null.");
	}
	
	@Test
	public void player_onPlatform_startDropdown_thenDropDownTimer_shouldBeSet() throws Exception {
		ReflectionHelper.setField(player, "canDropDown", true);
		ReflectionHelper.setField(player, "dropDownTimer", 0);
		ReflectionHelper.setField(player, "isOnPlatform", true);
		
	    ReflectionHelper.invokeMethod(player, "dropDown", new Class<?>[]{});
		int dropDownTimer = (int) ReflectionHelper.getField(player, "dropDownTimer");
		assertEquals(12, dropDownTimer, "Dropdown Timer should be set to 12");
	}
	
	@Test
	public void playerWith1Bullet_shoots2Times_thenReloadTimerIsSet_AndBulletPerClipShouldBe3() throws Exception {
	    GameLoop.bullets = new ArrayList<>(); 
	    GameStage mockGameStage = Mockito.mock(FirstStage.class);
	    ObservableList<Node> nodeList = FXCollections.observableArrayList();
	    when(mockGameStage.getChildren()).thenReturn(nodeList);
	    
	    ReflectionHelper.setField(player, "reloadTimer", 0);
	    ReflectionHelper.setField(player, "bulletPerClip", 1);
	    ReflectionHelper.setField(player, "lastShotTime", 0);

	    ReflectionHelper.invokeMethod(player, "shoot", new Class<?>[]{GameStage.class, ShootingDirection.class}, mockGameStage, ShootingDirection.RIGHT);
	    ReflectionHelper.setField(player, "lastShotTime", 0);
	    ReflectionHelper.invokeMethod(player, "shoot", new Class<?>[]{GameStage.class, ShootingDirection.class}, mockGameStage, ShootingDirection.RIGHT);
	    int bulletPerClip = (int) ReflectionHelper.getField(player, "bulletPerClip");
	    int reloadTimer = (int) ReflectionHelper.getField(player, "reloadTimer");
	    assertEquals(30, reloadTimer, "The reload timer should start at 30");
	    assertEquals(3, bulletPerClip,"The clip should be refilled to 3");
	}
	
	@Test
	public void playerProne_isProningShouldBe_true() throws Exception {
		ReflectionHelper.setField(player, "isProning", false);
		player.prone();
		boolean isProning = (boolean) ReflectionHelper.getField(player, "isProning");
		assertTrue(isProning, "isProning should be true.");
	}
	
	@Test
	public void playerIs_atHighestJump_thenStartFalling() throws Exception {
		ReflectionHelper.setField(player, "isJumping", true);
		ReflectionHelper.setField(player, "yVelocity", -1);
		player.checkHighestJump();

		boolean isFalling = (boolean) ReflectionHelper.getField(player, "isFalling");
		boolean isJumping = (boolean) ReflectionHelper.getField(player, "isJumping");
		double yVelocity = (double) ReflectionHelper.getField(player, "yVelocity");
		assertTrue(isFalling, "Player should start falling.");
		assertFalse(isJumping, "Player should not continue jumping.");
		assertEquals(0, yVelocity, "yVelocity should be 0.");
	}
}