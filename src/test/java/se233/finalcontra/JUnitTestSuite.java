package se233.finalcontra;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

import javafx.application.Platform;

@Suite
@SelectClasses({PlayerTest.class, GameLoopTest.class})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class JUnitTestSuite {
	@BeforeAll
	public static void initJfxRuntime() { Platform.runLater(() -> {});}
}
