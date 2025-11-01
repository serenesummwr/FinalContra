package se233.finalcontra.view.tools;

import java.io.File;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToolBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 * Utility window that lets designers drag to crop an image and copy hitbox coordinates.
 */
public class HitboxCropTool extends Stage {
	private final ImageView imageView = new ImageView();
	private final Rectangle selection = new Rectangle();
	private final Pane imageLayer = new Pane();
	private final Label coordinatesLabel = new Label("Load an image and drag to select an area.");
	private final Label imageInfoLabel = new Label();
	private double pressX;
	private double pressY;
	private boolean movingSelection;
	private double moveOffsetX;
	private double moveOffsetY;

	public HitboxCropTool() {
		setTitle("Hitbox Crop Tool");

		Button loadImageButton = new Button("Load Image");
		Button clearSelectionButton = new Button("Clear Selection");
		ToolBar toolBar = new ToolBar(loadImageButton, clearSelectionButton);

		imageView.setSmooth(true);
		imageView.setManaged(false);

		selection.setManaged(false);
		selection.setStroke(Color.RED);
		selection.setStrokeWidth(1.5);
		selection.setFill(Color.color(1, 0, 0, 0.25));
		selection.setVisible(false);
		selection.setMouseTransparent(true);

		imageLayer.getChildren().addAll(imageView, selection);

		ScrollPane scrollPane = new ScrollPane(imageLayer);
		scrollPane.setPannable(true);
		scrollPane.setFitToWidth(true);
		scrollPane.setFitToHeight(true);

		VBox infoBox = new VBox(6, coordinatesLabel, imageInfoLabel);
		infoBox.setPadding(new Insets(8));

		BorderPane root = new BorderPane();
		root.setTop(toolBar);
		root.setCenter(scrollPane);
		root.setBottom(infoBox);
		Scene scene = new Scene(root, 900, 700);
		setScene(scene);

		loadImageButton.setOnAction(event -> openImage());
		clearSelectionButton.setOnAction(event -> {
			clearSelection();
			if (imageView.getImage() != null) {
				coordinatesLabel.setText("Selection cleared. Drag to select a new area.");
			} else {
				coordinatesLabel.setText("Load an image and drag to select an area.");
			}
		});

		imageView.addEventHandler(MouseEvent.MOUSE_PRESSED, this::handleMousePressed);
		imageView.addEventHandler(MouseEvent.MOUSE_DRAGGED, this::handleMouseDragged);
		imageView.addEventHandler(MouseEvent.MOUSE_RELEASED, this::handleMouseReleased);
	}

	private void openImage() {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Open Stage Image");
		fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
		File initialDir = new File(System.getProperty("user.dir"), "src/main/resources/se233/finalcontra/assets");
		if (initialDir.exists() && initialDir.isDirectory()) {
			fileChooser.setInitialDirectory(initialDir);
		}
		File file = fileChooser.showOpenDialog(this);
		if (file == null) {
			return;
		}
		Image image = new Image(file.toURI().toString());
		imageView.setImage(image);
		imageLayer.setPrefSize(image.getWidth(), image.getHeight());
		imageLayer.setMinSize(image.getWidth(), image.getHeight());
		imageLayer.setMaxSize(image.getWidth(), image.getHeight());
		coordinatesLabel.setText("Drag across the image to measure a hitbox.");
		imageInfoLabel.setText(String.format("Loaded: %s (%.0f x %.0f)", file.getName(), image.getWidth(), image.getHeight()));
		clearSelection();
	}

	private void handleMousePressed(MouseEvent event) {
		if (imageView.getImage() == null) {
			return;
		}
		double imageX = clamp(event.getX(), 0, imageView.getImage().getWidth());
		double imageY = clamp(event.getY(), 0, imageView.getImage().getHeight());
		boolean hasSelection = selection.isVisible() && selection.getWidth() > 0 && selection.getHeight() > 0;
		if (hasSelection && selection.contains(imageX, imageY)) {
			movingSelection = true;
			moveOffsetX = imageX - selection.getX();
			moveOffsetY = imageY - selection.getY();
		} else {
			movingSelection = false;
			pressX = imageX;
			pressY = imageY;
			updateSelection(pressX, pressY);
			selection.setVisible(true);
		}
	}

	private void handleMouseDragged(MouseEvent event) {
		if (imageView.getImage() == null) {
			return;
		}
		double currentX = clamp(event.getX(), 0, imageView.getImage().getWidth());
		double currentY = clamp(event.getY(), 0, imageView.getImage().getHeight());
		if (movingSelection) {
			moveSelection(currentX - moveOffsetX, currentY - moveOffsetY);
		} else {
			updateSelection(currentX, currentY);
		}
	}

	private void handleMouseReleased(MouseEvent event) {
		if (imageView.getImage() == null) {
			return;
		}
		double currentX = clamp(event.getX(), 0, imageView.getImage().getWidth());
		double currentY = clamp(event.getY(), 0, imageView.getImage().getHeight());
		if (movingSelection) {
			moveSelection(currentX - moveOffsetX, currentY - moveOffsetY);
		} else {
			updateSelection(currentX, currentY);
		}
		movingSelection = false;
	}

	private void updateSelection(double currentX, double currentY) {
		double x = Math.min(pressX, currentX);
		double y = Math.min(pressY, currentY);
		double width = Math.abs(currentX - pressX);
		double height = Math.abs(currentY - pressY);

		selection.setX(x);
		selection.setY(y);
		selection.setWidth(width);
		selection.setHeight(height);
		updateCoordinatesLabel();
	}

	private void moveSelection(double targetX, double targetY) {
		Image image = imageView.getImage();
		if (image == null) {
			return;
		}
		double maxX = Math.max(0, image.getWidth() - selection.getWidth());
		double maxY = Math.max(0, image.getHeight() - selection.getHeight());
		double newX = clamp(targetX, 0, maxX);
		double newY = clamp(targetY, 0, maxY);
		selection.setX(newX);
		selection.setY(newY);
		updateCoordinatesLabel();
	}

	private void updateCoordinatesLabel() {
		if (!selection.isVisible()) {
			return;
		}

		int displayX = (int) Math.round(selection.getX());
		int displayY = (int) Math.round(selection.getY());
		int displayWidth = (int) Math.round(selection.getWidth());
		int displayHeight = (int) Math.round(selection.getHeight());
		coordinatesLabel.setText(String.format("x: %d, y: %d, width: %d, height: %d", displayX, displayY, displayWidth, displayHeight));
	}

	private void clearSelection() {
		selection.setVisible(false);
		selection.setX(0);
		selection.setY(0);
		selection.setWidth(0);
		selection.setHeight(0);
	}

	private double clamp(double value, double min, double max) {
		if (value < min) {
			return min;
		}
		if (value > max) {
			return max;
		}
		return value;
	}
}
