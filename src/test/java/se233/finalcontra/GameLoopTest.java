package se233.finalcontra;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import se233.finalcontra.controller.GameLoop;

public class GameLoopTest {

	@Test
	public void addScore100_scoreIncreases100() {
		int previousScore = GameLoop.getScore();
		GameLoop.addScore(100);
		assertEquals(previousScore + 100, GameLoop.getScore(), "Score should increase by 100");
	}
}
