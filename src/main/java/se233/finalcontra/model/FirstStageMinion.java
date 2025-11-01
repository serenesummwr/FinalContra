package se233.finalcontra.model;

import se233.finalcontra.model.Enums.EnemyType;

/**
 * Stage four minion that reuses the minion enemy AI but swaps in the minion sprite sheet.
 */
public class FirstStageMinion extends MinionEnemy {
    private static final double DEFAULT_SPEED = 1.0;
    private static final int FRAME_WIDTH = 64;
    private static final int FRAME_HEIGHT = 64;
    private static final int SPRITE_COUNT = 2;
    private static final int SPRITE_COLUMNS = 2;
    private static final int SPRITE_ROWS = 1;
    private static final int DEFAULT_HEALTH = 500;

    public FirstStageMinion(int xPos, int yPos) {
        super(
                xPos,
                yPos,
                DEFAULT_SPEED,
                FRAME_WIDTH,
                FRAME_HEIGHT,
                SPRITE_COUNT,
                SPRITE_COLUMNS,
                SPRITE_ROWS,
                ImageAssets.STAGE4_MINION,
                DEFAULT_HEALTH,
                EnemyType.MINION
        );
    }
}
