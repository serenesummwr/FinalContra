package se233.finalcontra.model;

import javafx.scene.image.Image;
import se233.finalcontra.Launcher;

public class ImageAssets {
	private static final int PLAYER_FRAME_WIDTH = 64;
	private static final int PLAYER_FRAME_HEIGHT = 64;

	private static final SpriteDefinition BILL_FACE_DOWN_SIDE = SpriteSheetFactory
			.loadBillSprite("assets/Player/bill_FaceDownSide.png", PLAYER_FRAME_WIDTH, PLAYER_FRAME_HEIGHT,
					150_000_000L);
	private static final SpriteDefinition BILL_RUN = SpriteSheetFactory
			.loadBillSprite("assets/Player/bill_Run.png", PLAYER_FRAME_WIDTH, PLAYER_FRAME_HEIGHT, 90_000_000L);
	private static final SpriteDefinition BILL_SHOOT = SpriteSheetFactory
			.loadBillSprite("assets/Player/bill_Shoot.png", PLAYER_FRAME_WIDTH, PLAYER_FRAME_HEIGHT, 110_000_000L);
	private static final SpriteDefinition BILL_PRONE = SpriteSheetFactory
			.loadBillSprite("assets/Player/bill_Prone.png", PLAYER_FRAME_WIDTH, PLAYER_FRAME_HEIGHT, 250_000_000L);
	private static final SpriteDefinition BILL_JUMP = SpriteSheetFactory
			.loadBillSprite("assets/Player/bill_Jump.png", PLAYER_FRAME_WIDTH, PLAYER_FRAME_HEIGHT, 140_000_000L);
	private static final SpriteDefinition BILL_FACE_UP_SIDE = SpriteSheetFactory
			.loadBillSprite("assets/Player/bill_FaceUpSide.png", PLAYER_FRAME_WIDTH, PLAYER_FRAME_HEIGHT, 140_000_000L);
	private static final SpriteDefinition BILL_FACE_UP = SpriteSheetFactory
			.loadBillSprite("assets/Player/bill_FaceUp.png", PLAYER_FRAME_WIDTH, PLAYER_FRAME_HEIGHT, 220_000_000L);
	private static final SpriteDefinition BILL_DIE = SpriteSheetFactory
			.loadBillSprite("assets/Player/bill_Die.png", PLAYER_FRAME_WIDTH, PLAYER_FRAME_HEIGHT, 120_000_000L);

	public static final SpriteDefinition PLAYER_IDLE = BILL_FACE_DOWN_SIDE;
	public static final SpriteDefinition PLAYER_FACE_DOWN_SIDE = BILL_FACE_DOWN_SIDE.withFrameDuration(120_000_000L);
	public static final SpriteDefinition PLAYER_CHARGING = BILL_RUN.withFrameDuration(70_000_000L);
	public static final SpriteDefinition PLAYER_RUN = BILL_RUN;
	public static final SpriteDefinition PLAYER_WALK_SHOOT = BILL_SHOOT;
	public static final SpriteDefinition PLAYER_SHOOTING = BILL_SHOOT.withFrameDuration(90_000_000L);
	public static final SpriteDefinition PLAYER_PRONE = BILL_PRONE;
	public static final SpriteDefinition PLAYER_JUMP = BILL_JUMP;
	public static final SpriteDefinition PLAYER_SHOOT_UP_SIDE = BILL_FACE_UP_SIDE;
	public static final SpriteDefinition PLAYER_SHOOT_UP = BILL_FACE_UP;
	public static final SpriteDefinition PLAYER_DIE = BILL_DIE;

	public static final Image STAGE4_MINION = new Image(
			Launcher.class.getResourceAsStream("assets/Enemy/Minion_Minion.png"));
	public static final Image JAVA_SKILL = new Image(Launcher.class.getResourceAsStream("assets/Enemy/Java_Skill.png"));

	public static final Image JAVA_IDLE = new Image(
			Launcher.class.getResourceAsStream("assets/Boss/Boss2/JAVA_IDLE.png"));
	public static final Image DESTROYED_JAVA = new Image(
			Launcher.class.getResourceAsStream("assets/Boss/Boss2/JAVA_DEAD.png"));

	public static final Image BOSS1_GATE_BROKEN = new Image(
			Launcher.class.getResourceAsStream("assets/Boss/Boss1/gate_broken.png"));
	public static final Image BOSS1_TURRET_SHOOT = new Image(
			Launcher.class.getResourceAsStream("assets/Boss/Boss1/turret_shoot.png"));
	public static final Image BOSS1_TURRET_BROKEN = new Image(
			Launcher.class.getResourceAsStream("assets/Boss/Boss1/turret_broken.png"));

	public static final Image EXPLOSION_IMG = new Image(
			Launcher.class.getResourceAsStream("assets/Effects/explosionSprite.png"));

	// Bullet sprites
	public static final Image BULLET_IMAGE = new Image(
			Launcher.class.getResourceAsStream("assets/Item/Entities/Bullet.png"));
	public static final Image LASER_IMAGE = new Image(
			Launcher.class.getResourceAsStream("assets/Item/Entities/Laser.png"));
	public static final Image CANNONBALL_IMAGE = new Image(
			Launcher.class.getResourceAsStream("assets/Item/Entities/Cannonball.png"));

	public static final Image MAIN_MENU = new Image(
			Launcher.class.getResourceAsStream("assets/Backgrounds/MainMenu.png"));
	public static final Image SECOND_STAGE = new Image(
			Launcher.class.getResourceAsStream("assets/Backgrounds/Stage2.png"));
	public static final Image FIRST_STAGE = new Image(
			Launcher.class.getResourceAsStream("assets/Backgrounds/Stage1.png"));
	public static final Image THIRD_STAGE_BOSS_IDLE = new Image(
			Launcher.class.getResourceAsStream("assets/Backgrounds/Stage3bidle.png"));
	public static final Image THIRD_STAGE_BOSS_DIE = new Image(
			Launcher.class.getResourceAsStream("assets/Boss/Boss3/Stage3bossdie.png"));
}
