package uk.ac.york.bitbotarena;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.ac.york.bitbotarena.BotControllers.BotController;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("BotEntity Tests")
class BotEntityTest {

    private static BotController fixedMoveController(Movement movement) {
        return new BotController() {
            @Override
            public Movement getMove(MatchState matchState, byte botIndex) {
                return movement;
            }

            @Override
            public void init(MatchState matchState, byte botIndex) {
            }

            @Override
            public void gameOver(byte winningBot, short[] scores, byte botIndex) {
            }
        };
    }

    @Nested
    @DisplayName("Constructor and Getter Tests")
    class ConstructorAndGetterTests {

        @Test
        void constructor_shouldExposeIndexAndInitialState() {
            BotEntity bot = new BotEntity(32, 32, 4, 5, fixedMoveController(Movement.NORTH), (byte) 2);

            assertEquals(2, bot.getIndex(), "Index should match constructor input");
            assertFalse(bot.isDead(), "New bot should not be dead");
            assertFalse(bot.isClaiming(), "New bot should not be claiming");
            assertTrue(bot.getCurrentPosition().getBit(4, 5), "Current position should be at start");
            assertTrue(bot.getClaimedBoard().getBit(4, 5), "Claimed board should include start tile");
            assertNull(bot.getPreviousMove(), "Previous move should start as null");
        }

        @Test
        void stateBasedConstructor_shouldUseProvidedState() {
            BotState state = new BotState(32, 32, 1, 1);
            BotEntity bot = new BotEntity(state, fixedMoveController(Movement.EAST), (byte) 1);

            assertTrue(bot.getCurrentPosition().getBit(1, 1), "Bot should reflect provided state");
            assertEquals(1, bot.getIndex(), "Index should match constructor input");
        }
    }

    @Nested
    @DisplayName("Move Execution Tests")
    class MoveExecutionTests {

        @Test
        void executeMove_shouldApplyControllerMoveAndStorePreviousMove() {
            BotEntity bot = new BotEntity(32, 32, 10, 10, fixedMoveController(Movement.EAST), (byte) 0);

            MatchState ms = new MatchState(32, 32, new BotEntity[]{bot});
            bot.executeMove(ms);

            assertTrue(bot.getCurrentPosition().getBit(11, 10), "Bot should move east");
            assertEquals(Movement.EAST, bot.getPreviousMove(), "Previous move should be updated");
        }

        @Test
        void executeMove_invalidMoveShouldKillBotAndStillStorePreviousMove() {
            BotEntity bot = new BotEntity(32, 32, 0, 0, fixedMoveController(Movement.NORTH), (byte) 0);

            MatchState ms = new MatchState(32, 32, new BotEntity[]{bot});
            bot.executeMove(ms);

            assertTrue(bot.isDead(), "Invalid move should kill the bot");
            assertEquals(Movement.NORTH, bot.getPreviousMove(), "Previous move should still be recorded");
        }

        @Test
        void setPreviousMove_shouldOverridePreviousMoveForTests() {
            BotEntity bot = new BotEntity(32, 32, 5, 5, fixedMoveController(Movement.SOUTH), (byte) 0);

            bot.setPreviousMove(Movement.WEST);

            assertEquals(Movement.WEST, bot.getPreviousMove(), "setPreviousMove should set explicit test value");
        }
    }

    @Nested
    @DisplayName("Kill and Score Tests")
    class KillAndScoreTests {

        @Test
        void kill_shouldDelegateToState() {
            BotEntity bot = new BotEntity(32, 32, 6, 6, fixedMoveController(Movement.NORTH), (byte) 0);

            bot.kill();

            assertTrue(bot.isDead(), "kill() should mark bot as dead");
            assertEquals(0L, bot.getCurrentPosition().getRow(6), "Current position row should be cleared on kill");
        }

        @Test
        void killedOtherBot_shouldIncrementKills() {
            BotEntity bot = new BotEntity(32, 32, 6, 6, fixedMoveController(Movement.NORTH), (byte) 0);

            assertEquals(0, bot.getKills(), "Kills should start at 0");
            bot.killedOtherBot();
            bot.killedOtherBot();

            assertEquals(2, bot.getKills(), "Kills should increment per call");
        }
    }

    @Nested
    @DisplayName("Invalid Board Delegation Tests")
    class InvalidBoardDelegationTests {

        @Test
        void updateInvalidBoard_shouldAffectMovementValidity() {
            BotEntity bot = new BotEntity(32, 32, 5, 5, fixedMoveController(Movement.NORTH), (byte) 0);
            BitBoard invalid = new BitBoard(32, 32);
            invalid.setBit(5, 4);

            bot.updateInvalidBoard(invalid);
            MatchState ms = new MatchState(32, 32, new BotEntity[]{bot});
            bot.executeMove(ms);

            assertTrue(bot.isDead(), "Moving into invalid board should kill bot");
            assertEquals(Movement.NORTH, bot.getPreviousMove(), "Previous move should be recorded");
        }
    }
}