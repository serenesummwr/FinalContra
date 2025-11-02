package se233.finalcontra.view;

import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import se233.finalcontra.Launcher;
import se233.finalcontra.controller.SoundController;
import se233.finalcontra.model.ImageAssets;
import se233.finalcontra.util.ResourceUtils;
import se233.finalcontra.view.GameStages.GameStage;
import se233.finalcontra.view.tools.HitboxCropTool;

public class MainMenu extends AnchorPane {
	private boolean isSelectingLevel = false;
	private HitboxCropTool cropTool;
	private Button selectStageButton;
	private VBox levelList;
	private Pane levelPane;
	
	public MainMenu() {
		VBox buttonBox = new VBox(20);
		buttonBox.setAlignment(Pos.CENTER);
		ImageView background = new ImageView(ImageAssets.MAIN_MENU);
		background.setFitHeight(GameStage.HEIGHT);
		background.setFitWidth(GameStage.WIDTH);
		Button startButton = drawStartButton();
		selectStageButton = drawStageButton();
		Button cropToolButton = drawCropToolButton();
		Button exitButton = drawExitButton();
		levelList = drawLevelListPane();
		levelPane = new Pane(levelList);
		String style = ResourceUtils.requireResource("styles/style.css").toExternalForm();
		this.getStylesheets().add(style);
		buttonBox.getChildren().addAll(startButton, selectStageButton, cropToolButton, exitButton);
		widthProperty().addListener((obs, oldVal, newVal) -> centerMenu(buttonBox));
		heightProperty().addListener((obs, oldVal, newVal) -> centerMenu(buttonBox));
		buttonBox.widthProperty().addListener((obs, oldVal, newVal) -> centerMenu(buttonBox));
		buttonBox.heightProperty().addListener((obs, oldVal, newVal) -> centerMenu(buttonBox));
		selectStageButton.layoutBoundsProperty().addListener((obs, oldVal, newVal) -> positionLevelPane(buttonBox));
		levelList.heightProperty().addListener((obs, oldVal, newVal) -> positionLevelPane(buttonBox));
		levelList.widthProperty().addListener((obs, oldVal, newVal) -> positionLevelPane(buttonBox));
		levelPane.setPickOnBounds(false);
		positionLevelPane(buttonBox);
		centerMenu(buttonBox);
		levelList.setAlignment(Pos.CENTER);
		levelList.setPrefWidth(200);
		showLevelList(false);
		startButton.setOnAction(e -> {
			Launcher.changeStage(0);
		});
		selectStageButton.setOnAction(e -> {
			showLevelList(!isSelectingLevel);
		});
		cropToolButton.setOnAction(e -> {
			if (cropTool == null) {
				cropTool = new HitboxCropTool();
				if (Launcher.primaryStage != null) {
					cropTool.initOwner(Launcher.primaryStage);
				}
			}
			if (!cropTool.isShowing()) {
				cropTool.show();
			}
			cropTool.toFront();
		});
		exitButton.setOnAction(e -> {
			Alert alert = new Alert(AlertType.CONFIRMATION);
			alert.setTitle("Are you sure?");
			alert.setHeaderText("Exit?");
			alert.setContentText("Are you sure you want to exit?");
			alert.showAndWait();
			if (alert.getResult() == ButtonType.OK) {
				Launcher.primaryStage.close();
			}
		});
		getChildren().addAll(background, buttonBox, levelPane);
		SoundController.getInstance().playStartTheme();
	}
	
	public Button drawStartButton() {
		Button startButton = new Button("Start");
		startButton.setPrefSize(150, 50);
		return startButton;
	}
	
	public Button drawStageButton() {
		Button selectStageButton = new Button("Select Level");
		selectStageButton.setPrefSize(200, 50);
		return selectStageButton;
	}

	public Button drawCropToolButton() {
		Button cropButton = new Button("Hitbox Tool");
		cropButton.setPrefSize(200, 50);
		return cropButton;
	}
	
	public Button drawExitButton() {
		Button exitButton = new Button("Quit");
		exitButton.setPrefSize(150, 50);
		return exitButton;
	}
	
	public VBox drawLevelListPane() {
		Button stage1 = new Button("Level 1");
		Button stage2 = new Button("Level 2");
		Button stage3 = new Button("Level 3");

		VBox stageList = new VBox(5);
		stageList.setAlignment(Pos.CENTER);
		stageList.getStyleClass().add("stage-selector");
		stage1.setId("stageButton");
		stage2.setId("stageButton");
		stage1.setOnAction(event -> {
			Launcher.changeStage(0);
			showLevelList(false);
		});
		stage2.setOnAction(event -> {
			Launcher.changeStage(1);
			showLevelList(false);
		});
		stage3.setOnAction(event -> {
			Launcher.changeStage(2);
			showLevelList(false);
		});
		
		stageList.getChildren().addAll(stage1, stage2, stage3);
		return stageList;
	}

	private void centerMenu(VBox buttonBox) {
		// Shift a bit to the left so it doesn't feel right-aligned
		double xBias = -80; // pixels to nudge left
		double centeredX = (getWidth() - buttonBox.getWidth()) / 2.0 + xBias;
		double centeredY = (getHeight() * 0.45) - (buttonBox.getHeight() / 2.0);
		if (!Double.isNaN(centeredX)) {
			buttonBox.setLayoutX(Math.max(centeredX, 16));
		}
		if (!Double.isNaN(centeredY)) {
			buttonBox.setLayoutY(Math.max(centeredY, 0));
		}
		positionLevelPane(buttonBox);
	}

	private void showLevelList(boolean show) {
		isSelectingLevel = show;
		if (levelList != null) {
			levelList.setVisible(show);
			levelList.setManaged(show);
			if (show) {
				levelList.requestFocus();
			}
		}
		if (levelPane != null) {
			levelPane.setVisible(show);
			levelPane.setManaged(show);
			levelPane.setMouseTransparent(!show);
		}
		if (selectStageButton != null) {
			selectStageButton.setText("Select Level");
		}
		if (show && selectStageButton != null && selectStageButton.getParent() instanceof VBox parentBox) {
			positionLevelPane(parentBox);
		}
	}

	private void positionLevelPane(VBox buttonBox) {
		if (levelPane == null || levelList == null || selectStageButton == null || buttonBox == null) {
			return;
		}
		if (selectStageButton.getParent() == null) {
			return;
		}
		double listWidth = levelList.getWidth() > 0 ? levelList.getWidth() : levelList.prefWidth(-1);
		double targetX = buttonBox.getLayoutX() + buttonBox.getWidth() + 40;
		double maxX = GameStage.WIDTH - listWidth - 40;
		targetX = Math.max(0, Math.min(targetX, maxX));
		double selectButtonCenterY = selectStageButton.getBoundsInParent().getMinY()
				+ selectStageButton.getBoundsInParent().getHeight() / 2.0;
		double targetY = buttonBox.getLayoutY() + selectButtonCenterY - (levelList.getHeight() / 2.0);
		double maxY = getHeight() - levelList.getHeight() - 20;
		targetY = Math.max(0, Double.isFinite(maxY) ? Math.min(targetY, Math.max(maxY, 0)) : targetY);
		if (!Double.isNaN(targetX)) {
			levelPane.setLayoutX(targetX);
		}
		if (!Double.isNaN(targetY)) {
			levelPane.setLayoutY(Math.max(targetY, 0));
		}
	}
}
