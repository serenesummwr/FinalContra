package se233.finalcontra.model;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class Turret extends Rectangle {
	int xPos, yPos, width, height;
	
	public Turret(int xPos, int yPos, int width, int height) {
		this.setTranslateX(xPos);
		this.setTranslateY(yPos);
		this.setWidth(width);
		this.setHeight(height);
		this.setFill(Color.RED);
	}
}