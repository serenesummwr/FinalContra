package se233.finalcontra;

import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.Duration;
import se233.finalcontra.controller.DrawingLoop;
import se233.finalcontra.controller.GameLoop;
import se233.finalcontra.controller.SoundController;
import se233.finalcontra.controller.CheatManager;
import se233.finalcontra.model.Boss.FirstStageBoss;
import se233.finalcontra.view.GameStages.ThirdStage;
import se233.finalcontra.view.MainMenu;
import se233.finalcontra.view.GameStages.GameStage;
import se233.finalcontra.view.GameStages.SecondStage;
import se233.finalcontra.view.GameStages.FirstStage;

public class Launcher extends Application {
	public static Stage primaryStage;
	private static MainMenu menu = null;
	private static Scene menuScene = null;
	private static Scene currentScene = null;
	private static GameStage currentStage = null;
	public static Integer currentStageIndex = null;
	private static Thread currentDrawingThread = null;
	private static Thread currentGameThread = null;
	private static GameLoop currentGameLoop = null;
	private static DrawingLoop currentDrawingLoop = null;
	private static Rectangle fadeOverlay = null;
	
    @Override
    public void start(Stage stage) {
    	menu = new MainMenu();
    	menuScene = new Scene(menu, GameStage.WIDTH, GameStage.HEIGHT);
    	currentScene = menuScene;
    	stage.setScene(menuScene);
    	stage.setTitle("Contra");
    	stage.show();
    	stage.setResizable(false);
    	primaryStage = stage;
    }
    
    public static void changeStage(int index) {
    	Platform.runLater(() -> {
    		SoundController.getInstance().stopAllSounds();
    		fadeOverlay = new Rectangle(0, 0, GameStage.WIDTH, GameStage.HEIGHT);
            fadeOverlay.setFill(Color.BLACK);
            fadeOverlay.setOpacity(0);

            if (currentScene.getRoot() instanceof Pane) {
                ((Pane) currentScene.getRoot()).getChildren().add(fadeOverlay);
            }
            FadeTransition fadeOut = new FadeTransition(Duration.millis(500), fadeOverlay);
            fadeOut.setFromValue(0);
            fadeOut.setToValue(1);
            
            fadeOut.setOnFinished(event -> {
            	((Pane)currentScene.getRoot()).getChildren().remove(fadeOverlay);
        		if (GameLoop.isPaused) GameLoop.pause(); // unpause the game
        		if (currentGameLoop != null) {
        			currentGameLoop.stop();
        			currentGameThread = null;
        		}
        		if (currentDrawingLoop != null) {
        			currentDrawingLoop.stop();
        			currentDrawingThread = null;
        		}
        		
        		currentStageIndex = index;
			GameStage gameStage = switch (index) {
	    			case 0 -> new FirstStage();
	    			case 1 -> new SecondStage();
					case 2 -> new ThirdStage();
				default -> throw new IllegalArgumentException("Unexpected value: " + index);
			};
        		currentScene = new Scene(gameStage, GameStage.WIDTH, GameStage.HEIGHT);
        		currentScene.setOnKeyPressed(e -> {
        			if (e.getCode() == KeyCode.ESCAPE) {
        				GameLoop.pause();
        			}
        			if (e.getCode() == KeyCode.H) {
        				if (gameStage.getBoss() instanceof FirstStageBoss stageFourBoss) {
        					stageFourBoss.toggleHitboxOutline();
        				}
        				return;
        			}
        			if (e.getCode() == KeyCode.O) {
						gameStage.setPlatformOutlineVisible(!gameStage.isPlatformOutlineVisible());
        			}
        			if (!gameStage.getKeys().isPressed(e.getCode())) {
        				gameStage.getKeys().add(e.getCode());
        				gameStage.getKeys().addPressed(e.getCode());
        			} else {
        				gameStage.getKeys().add(e.getCode());
        			}
    				if (e.getCode() == gameStage.getPlayer().getCheatKey()) {
    					CheatManager.getInstance().toggleCheats();
    				}
        		});
        		currentScene.setOnKeyReleased(e -> {
        			gameStage.getKeys().remove(e.getCode());

        		});
        		currentGameLoop = new GameLoop(gameStage);
        		currentDrawingLoop = new DrawingLoop(gameStage);
        		
        		currentGameThread = new Thread(currentGameLoop, "GameLoopThread");
        		currentDrawingThread = new Thread(currentDrawingLoop, "DrawingLoopThread");


        		primaryStage.setScene(currentScene);
        		currentStage = gameStage;
        		
        		currentGameThread.start();
        		currentDrawingThread.start();

				// Play theme music for every stage
				SoundController.getInstance().playMinecraftTheme();
        		
        		Rectangle fadeInOverlay = new Rectangle(0, 0, GameStage.WIDTH, GameStage.HEIGHT);
                fadeInOverlay.setFill(Color.BLACK);
                fadeInOverlay.setOpacity(0);

                if (gameStage instanceof Pane) {
                    ((Pane) currentScene.getRoot()).getChildren().add(fadeInOverlay);
                }
                FadeTransition fadeIn = new FadeTransition(Duration.millis(500), fadeInOverlay);
                fadeIn.setFromValue(1);
                fadeIn.setToValue(0);
                fadeIn.setOnFinished(e -> {
                    if (gameStage instanceof Pane) {
                        ((Pane) currentScene.getRoot()).getChildren().remove(fadeInOverlay);
                    }
                });
                fadeIn.play();
            });
            fadeOut.play();
    	});
    }
    
    public static void exitToMenu() {
    	Platform.runLater(() -> {
    		fadeOverlay = new Rectangle(0, 0, GameStage.WIDTH, GameStage.HEIGHT);
            fadeOverlay.setFill(Color.BLACK);
            fadeOverlay.setOpacity(0);
            
            if (currentScene != null && currentScene.getRoot() instanceof Pane) {
                ((Pane) currentScene.getRoot()).getChildren().add(fadeOverlay);
            }
            FadeTransition fadeOut = new FadeTransition(Duration.millis(500), fadeOverlay);
            fadeOut.setFromValue(0);
            fadeOut.setToValue(1);

            fadeOut.setOnFinished(e -> {

        		SoundController.getInstance().stopAllSounds();
                if (GameLoop.isPaused) GameLoop.pause(); // Unpause the game
            	if (currentGameLoop != null) {
            		currentGameLoop.stop();
            		currentGameThread = null;
            	}
            	if (currentDrawingLoop != null) {
            		currentDrawingLoop.stop();
            		currentDrawingThread = null;
            	}
            	
            	currentStage = null;
                primaryStage.setScene(menuScene);
                currentScene = menuScene;
                
        		Rectangle fadeInOverlay = new Rectangle(0, 0, GameStage.WIDTH, GameStage.HEIGHT);
                fadeInOverlay.setFill(Color.BLACK);
                fadeInOverlay.setOpacity(0);

                if (currentScene.getRoot() instanceof Pane) {
                    ((Pane) currentScene.getRoot()).getChildren().add(fadeInOverlay);
                }
                FadeTransition fadeIn = new FadeTransition(Duration.millis(500), fadeInOverlay);
                fadeIn.setFromValue(1);
                fadeIn.setToValue(0);
                fadeIn.setOnFinished(event -> {
                    if (currentScene.getRoot() instanceof Pane) {
                        ((Pane) currentScene.getRoot()).getChildren().remove(fadeInOverlay);
                    }
                });
                fadeIn.play();
            });
        	fadeOut.play();
    	});
    }
    
    public static GameStage getCurrentStage() {
    	return currentStage;
    }

    public static void main(String[] args) {
    	launch(args);
    }
}
