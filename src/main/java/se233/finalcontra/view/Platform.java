package se233.finalcontra.view;

import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import se233.finalcontra.util.ResourceUtils;

public class Platform extends Rectangle {
	private static final int PLATFORM_HEIGHT = 5;
	private static final Color OUTLINE_COLOR = Color.web("#f8e71c");
	
	private int width;
	private int xPos;
	private int yPos;
	private Image platformImg;
	private boolean isGround;
	
	public Platform(int width, int xPos, int yPos, boolean isGround) {
		setTranslateX(xPos);
		setTranslateY(yPos);
		this.width = width;
		this.xPos = xPos;
		this.yPos = yPos;	
		this.platformImg = ResourceUtils.loadImage("assets/Platform.png");
		this.isGround = isGround;
		setWidth(width);
		setHeight(PLATFORM_HEIGHT);
		setFill(new ImagePattern(platformImg, 0, 0, width, PLATFORM_HEIGHT, false));
		setStroke(Color.TRANSPARENT);
		setStrokeWidth(0);
		setVisible(false);
	}
	
	public int getPaneWidth() { return this.width; }
	public int getxPos() { return this.xPos; }
	public int getyPos() { return this.yPos; }
	public boolean getIsGround() { return this.isGround; }
	
	public void setOutlineVisible(boolean visible) {
		if (visible) {
			setVisible(true);
			setStroke(OUTLINE_COLOR);
			setStrokeWidth(5);
			setEffect(createOutlineGlow());
			setViewOrder(-100);
		} else {
			setVisible(false);
			setStroke(Color.TRANSPARENT);
			setStrokeWidth(0);
			setEffect(null);
			setViewOrder(0);
		}
	}
	
	private static DropShadow createOutlineGlow() {
		DropShadow glow = new DropShadow(5, OUTLINE_COLOR);
		glow.setSpread(0.7);
		return glow;
	}
}
