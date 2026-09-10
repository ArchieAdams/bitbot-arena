package uk.ac.york.bitbotarena;

import uk.ac.york.bitbotarena.BotControllers.BotController;

public class BotTestHelper {
    public static BotEntity initBot(byte index, int x, int y) {
        BotController dummyController = new BotController() {
            @Override
            public Movement getMove(MatchState matchState, byte botIndex) {
                return Movement.NORTH;
            }

            @Override
            public void init(MatchState matchState, byte botIndex) {
            }

            @Override
            public void gameOver(byte winningBot, short[] scores, byte botIndex) {
            }
        };
        BotEntity bot = new BotEntity(32, 32, x, y, dummyController, index);
        bot.getClaimedBoard().clearBoard();
        bot.getClaimingBoard().clearBoard();
        return bot;
    }

    public static BotEntity[] initBots() {
        BotEntity[] bots = new BotEntity[4];
        int[] x = {2, 32 - 3, 2, 32 - 3};
        int[] y = {2, 2, 32 - 3, 32 - 3};

        for (byte i = 0; i < 4; i++) {
            bots[i] = initBot(i, x[i], y[i]);
        }
        return bots;
    }
}
