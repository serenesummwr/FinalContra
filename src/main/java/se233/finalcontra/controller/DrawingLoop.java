package se233.finalcontra.controller;

import java.util.List;

import se233.finalcontra.model.Enums.BulletOwner;
import se233.finalcontra.model.Enums.EnemyType;
import se233.finalcontra.model.Enums.ShootingDirection;
import se233.finalcontra.view.GameStages.GameStage;
import se233.finalcontra.view.GameStages.SecondStage;
import se233.finalcontra.view.GameStages.FirstStage;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;

import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Alert.AlertType;
import se233.finalcontra.Launcher;
import se233.finalcontra.model.*;
import se233.finalcontra.model.Boss.FirstStageBoss;
import se233.finalcontra.model.Boss.JavaBoss;

public class DrawingLoop implements Runnable {	
	public static List<Effect> effects = new ArrayList<>();
	private GameStage gameStage;
	private int frameRate;
	private float interval;
	private boolean running;
	boolean isWin;
	
	public DrawingLoop(GameStage gameStage) {
		this.isWin = false;
		this.gameStage = gameStage;
		frameRate = 60;
		interval = 1000f / frameRate;
		running = true;
	}
	
	public void checkAllCollisions(Player player) {
		player.checkHighestJump();
		player.checkStageBoundaryCollision();
		player.checkPlatformCollision(gameStage.getPlatforms());
		player.updateTimer();
		player.resetHitBoxHeight();
		// Kill player if they fall out of bounds (off platforms)
		player.handleOutOfBounds(gameStage);
		checkPlayerEnemyCollision();
		for (Enemy enemy: GameLoop.enemies) {
			if (enemy.getType() == EnemyType.MINION) {
				((MinionEnemy) enemy).updateAI(player, gameStage);
				((MinionEnemy) enemy).repaint();
				((MinionEnemy) enemy).checkReachHighest();
				((MinionEnemy) enemy).checkPlatformCollision(gameStage.getPlatforms());
				((MinionEnemy) enemy).checkReachGameWall();
				((MinionEnemy) enemy).handleOutOfBounds(gameStage);
			}
		}
	}

	public void paint(Player player) {
		player.repaint();
	}

	private void paintBullet(List<Bullet> bullets, ShootingDirection direction) {

		Iterator<Bullet> iterator = bullets.iterator();
		while (iterator.hasNext()) {
			Bullet bullet = iterator.next();
			bullet.move();
			bullet.tick(); // Update bullet animation
			
			boolean shouldRemove = false;

			if (bullet.isOutOfBounds(GameStage.WIDTH, GameStage.HEIGHT )) {
				shouldRemove = true;
			}
			
			// Enemies collision with bullet
			for (Enemy enemy : gameStage.getEnemies()) {
				if (!enemy.isAlive() || bullet.getOwner() != BulletOwner.PLAYER) {
					continue;
				}

				boolean hitDirect = enemy.getBoundsInParent().intersects(bullet.getBoundsInParent());
				boolean hitBossChild = !hitDirect
						&& gameStage.getBoss().localToParent(enemy.getBoundsInParent()).intersects(bullet.getBoundsInParent());

				if (!hitDirect && !hitBossChild) {
					continue;
				}

				if (hitDirect && enemy.getType() == EnemyType.BOSS4_TURRET) {
					Effect explosion = new Effect(ImageAssets.EXPLOSION_IMG, 7, 7, 1,
							bullet.getxPos() - 64, bullet.getyPos() - 128, 256, 256);
					effects.add(explosion);
					Platform.runLater(() -> gameStage.getChildren().add(explosion));
				}

				if (hitBossChild) {
					Effect explosion = new Effect(ImageAssets.EXPLOSION_IMG, 7, 7, 1,
							bullet.getxPos() - 64, bullet.getyPos() - 128, 256, 256);
					effects.add(explosion);
					Platform.runLater(() -> gameStage.getChildren().add(explosion));
				}

				enemy.takeDamage(bullet.getDamage(), gameStage.getBoss());

				if (bullet.consumeHit()) {
					shouldRemove = true;
					break;
				}
			}

			if (!shouldRemove
					&& bullet.getOwner() == BulletOwner.PLAYER
					&& gameStage instanceof FirstStage
					&& gameStage.getBoss() instanceof FirstStageBoss) {
				FirstStageBoss stageFourBoss = (FirstStageBoss) gameStage.getBoss();
				if (!stageFourBoss.isDefeated()
						&& stageFourBoss.getHitboxBounds().intersects(bullet.getBoundsInParent())) {
					Effect explosion = new Effect(ImageAssets.EXPLOSION_IMG, 7, 7, 1,
							bullet.getxPos() - 64, bullet.getyPos() - 128, 256, 256);
					effects.add(explosion);
					Platform.runLater(() -> gameStage.getChildren().add(explosion));
					stageFourBoss.applyDamage(bullet.getDamage());
					if (bullet.consumeHit()) {
						shouldRemove = true;
					}
				}
			}
			
			// Player collisions with bullet
			
			Bounds playerBounds = gameStage.getPlayer().localToParent(gameStage.getPlayer().getHitBox().getBoundsInParent());
			if (playerBounds.intersects(bullet.getBoundsInParent())
					&& bullet.getOwner() != BulletOwner.PLAYER
					&& !gameStage.getPlayer().isDying()) {
                if (!CheatManager.getInstance().areCheatsActive()) {
                    if (Player.spawnProtectionTimer <= 0) {
                        gameStage.getPlayer().die();
                        shouldRemove = true;
                    }
                }
			}
			
			// Remove bullet
			try {
				if (shouldRemove) {
				iterator.remove();
				Platform.runLater(() -> gameStage.getChildren().remove(bullet));
				}
			} catch (ConcurrentModificationException e) {
				System.err.println("The bullet deletion is duplicated: " + e.getMessage());
			}
			Platform.runLater(this::updateScore);
			Platform.runLater(this::updateLives);
		}
	}

	private void checkPlayerEnemyCollision() {
		if (gameStage.getPlayer().isDying()) {
			return;
		}

		Bounds playerBounds = gameStage.getPlayer().localToParent(
				gameStage.getPlayer().getHitBox().getBoundsInParent()
		);

		List<Enemy> enemiesCopy = new ArrayList<>(GameLoop.enemies);

		for (Enemy enemy : enemiesCopy) {
			if (enemy.isAlive() && enemy.getType() == EnemyType.FLYING) {
				Bounds enemyBounds = gameStage.getBoss().localToParent(enemy.getBoundsInParent());

				if (enemyBounds.intersects(playerBounds)) {
					if (!CheatManager.getInstance().areCheatsActive()) {
						gameStage.getPlayer().die();
					}

					clearAllEnemies();

					Platform.runLater(this::updateLives);
				}
			}
			
			// Player Collided with MINION enemy
			if (enemy.isAlive() && (enemy.getType() == EnemyType.MINION)) {
				Bounds enemyBounds = enemy.localToParent(((MinionEnemy) enemy).getBoundsInLocal());
				if (enemyBounds.intersects(playerBounds) && Player.spawnProtectionTimer <= 0) {
					if (!CheatManager.getInstance().areCheatsActive()) {
						gameStage.getPlayer().die();
						
						Platform.runLater(this::updateLives);
					}
				}
			}

		}
	}
	
	private void paintEffects(List<Effect> effects) {
        Iterator<Effect> iterator = effects.iterator();
        while (iterator.hasNext()) {
            Effect effect = iterator.next();
            effect.tick();
            if (effect.isFinished()) {
                iterator.remove();
                Platform.runLater(() -> gameStage.getChildren().remove(effect));
            }
        }
    }
	private void clearAllEnemies() {
		List<Enemy> enemiesToRemove = new ArrayList<>();

		for (Enemy enemy : GameLoop.enemies) {
			if (enemy.getType() == EnemyType.FLYING) {
				// Add explosion effect for each removed enemy
				Bounds enemyBounds = gameStage.getBoss().localToParent(enemy.getBoundsInParent());
				Effect explosion = new Effect(
						ImageAssets.EXPLOSION_IMG, 7, 7, 1,
						(int) enemyBounds.getCenterX() - 64,
						(int) enemyBounds.getCenterY() - 64,
						256, 256
				);
				effects.add(explosion);
				Platform.runLater(() -> {
					gameStage.getChildren().add(explosion);
				});
				enemiesToRemove.add(enemy);
			}
		}

		// Remove all marked enemies
		for (Enemy enemy : enemiesToRemove) {
			GameLoop.enemies.remove(enemy);
			Platform.runLater(() -> {
				gameStage.getBoss().getChildren().remove(enemy);
			});
		}
	}

	private void updateScore() {
		gameStage.getScoreLabel().setText("Score: " + String.format("%06d", GameLoop.getScore()));
	}
	
	private void updateLives() {
		gameStage.getLivesLabel().setText("Lives: " + gameStage.getPlayer().getLives());
	}

	// Update boss in each stage
	private void updateBoss() {
		if (gameStage instanceof FirstStage) {
			if (!GameStage.bossPhase) {
				GameStage.bossPhase = true;
			}
			if (gameStage.getBoss() instanceof FirstStageBoss stageFourBoss) {
				if (!stageFourBoss.isDefeated()) {
					stageFourBoss.update();
				}
				if (stageFourBoss.isDefeated() && !isWin) {
					isWin = true;
					Platform.runLater(() -> {
						SoundController.getInstance().playWinSound();
						if (Launcher.currentStageIndex != null && Launcher.currentStageIndex == 0) {
							Alert alert = new Alert(AlertType.CONFIRMATION);
							alert.setTitle("CONGRATULATION!");
							alert.setHeaderText("You Win!");
							alert.setContentText("Continue to the next stage?");
							alert.showAndWait();
							if (alert.getResult() == ButtonType.OK) {
								Launcher.changeStage(1);
							} else {
								Launcher.exitToMenu();
							}
						} else {
							Alert alert = new Alert(AlertType.INFORMATION);
							alert.setTitle("CONGRATULATION!");
							alert.setHeaderText("Stage 4 Cleared!");
							alert.setContentText("Returning to the main menu.");
							alert.showAndWait();
							Launcher.exitToMenu();
						}
					});
				}
			}
			return;
		} else if (gameStage instanceof SecondStage) {
			gameStage.getPlayer().isCollided(gameStage, 250);
			if (GameStage.totalMinions <= 0 && !GameStage.bossPhase) {
				GameStage.bossPhase = true;
			}
			if (GameStage.bossPhase) {
				if (gameStage.getBoss() instanceof JavaBoss javaBoss && !javaBoss.isSpawned()) {
					javaBoss.spawn();
				}
				if (gameStage.getBoss() != null && gameStage.getBoss().isAlive()) {
					gameStage.getBoss().update();
				}
			}
			if (gameStage.getBoss().getWeakPoints().isEmpty() && !isWin && !gameStage.getPlayer().isDying()) {
				isWin = true;
				Platform.runLater(() -> {
					SoundController.getInstance().playWinSound();
					Alert alert = new Alert(AlertType.CONFIRMATION);
					alert.setTitle("CONGRATULATION!");
					alert.setHeaderText("You Win!");
					alert.setContentText("Continue to the next stage?");
					alert.showAndWait();
					if (alert.getResult() == ButtonType.OK) {
						Launcher.changeStage(2);
					} else {
						Launcher.exitToMenu();
					}
				});
			}
		} 
	}
	
	private void updateEnemies() {
		if (!GameLoop.isPaused) {
			Iterator<Enemy> iterator = GameLoop.enemies.iterator();
			while (iterator.hasNext()) {
				Enemy enemy = iterator.next();
				if (enemy.isAlive()) {
					enemy.updateWithPlayer(gameStage.getPlayer(), gameStage);
				} else if (!(enemy.getType() == EnemyType.TURRET) && !(enemy.getType() == EnemyType.BOSS4_TURRET) && !(enemy.getType() == EnemyType.WALL)
					&& !(enemy.getType() == EnemyType.JAVAHEAD) ) {
					// Kill remove enemy from the stage
					iterator.remove();
					javafx.application.Platform.runLater(() -> {
						gameStage.getBoss().getChildren().remove(enemy);
					});
					if (enemy.getType() == EnemyType.MINION) {
						Platform.runLater(() -> gameStage.getChildren().remove(enemy));
					}
				} 
			}
		}
	}
	
	public void stop() {
		running = false;
	}
	


    @Override
    public void run() {
        while (running) {
            float startTime = System.currentTimeMillis();
            Platform.runLater(() -> {
            	if (GameLoop.isPaused || !running) {
                    return;
                }
                checkAllCollisions(gameStage.getPlayer());
                paint(gameStage.getPlayer());
                paintBullet(GameLoop.bullets, GameLoop.shootingDir);
                updateEnemies();
                updateBoss();
                paintEffects(DrawingLoop.effects);
            });
            float elapsedTime = System.currentTimeMillis() - startTime;
            if (elapsedTime < interval) {
            	try {
            		Thread.sleep((long) (interval - elapsedTime));
            	} catch (InterruptedException e) {
            		e.printStackTrace();
            	}
            } else {
            	try {
            		Thread.sleep((long) (interval - (interval % elapsedTime)));
            	} catch (InterruptedException e) {
            		e.printStackTrace();
            	}
            }
        }
    }
}
