package uk.ac.york.bitbotarena;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("BotState Tests")
class BotStateTest {

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        void constructor_shouldInitializeStartingState() {
            BotState state = new BotState(32, 32, 2, 3);

            assertFalse(state.isDead(), "New bot should not be dead");
            assertFalse(state.isClaiming(), "New bot should not be claiming");

            assertTrue(state.getCurrentPosition().getBit(2, 3), "Current position should start at the provided coordinates");
            assertTrue(state.getClaimedBoard().getBit(2, 3), "Claimed board should include the starting position");
            assertFalse(state.getClaimingBoard().getBit(2, 3), "Claiming board should start empty");
        }
    }

    @Nested
    @DisplayName("Valid Move Tests")
    class ValidMoveTests {

        @ParameterizedTest(name = "Movement {0} from start should be valid")
        @ValueSource(strings = {"NORTH", "SOUTH", "EAST", "WEST"})
        void validMove_shouldAllowInBoundsMoves(String movementName) {
            BotState state = new BotState(32, 32, 15, 15);
            Movement movement = Movement.valueOf(movementName);

            assertTrue(state.validMove(movement), "Expected move to be valid from the middle of the board");
        }

        @Test
        void validMove_shouldRejectOffBoardMove() {
            BotState state = new BotState(32, 32, 0, 0);

            assertFalse(state.validMove(Movement.NORTH), "Moving north off the board should be invalid");
            assertFalse(state.validMove(Movement.WEST), "Moving west off the board should be invalid");
        }

        @Test
        void validMove_shouldRejectClaimingBoardCollision() {
            BotState state = new BotState(32, 32, 5, 5);
            state.getClaimingBoard().setBit(5, 4);

            assertFalse(state.validMove(Movement.NORTH), "Move should be invalid if it intersects claiming board");
        }

        @Test
        void validMove_shouldRejectInvalidBoardCollision() {
            BotState state = new BotState(32, 32, 5, 5);
            BitBoard invalid = new BitBoard(32, 32);
            invalid.setBit(5, 4);
            state.updateInvalidBoard(invalid);

            assertFalse(state.validMove(Movement.NORTH), "Move should be invalid if it intersects invalid board");
        }
    }

    @Nested
    @DisplayName("Move Tests")
    class MoveTests {

        @Test
        void move_shouldUpdateCurrentPosition() {
            BotState state = new BotState(32, 32, 10, 10);

            state.move(Movement.EAST);

            assertTrue(state.getCurrentPosition().getBit(11, 10), "Current position should move east");
            assertFalse(state.getCurrentPosition().getBit(10, 10), "Old position should be cleared");
        }

        @Test
        void move_shouldStartClaimingWhenLeavingClaimedArea() {
            BotState state = new BotState(32, 32, 10, 10);

            state.move(Movement.EAST);

            assertTrue(state.isClaiming(), "Bot should enter claiming mode after leaving claimed area");
            assertTrue(state.getClaimingBoard().getBit(11, 10), "New position should be added to claiming board");
        }

        @Test
        void move_shouldFinalizeClaimWhenReturningInside() {
            BotState state = new BotState(32, 32, 10, 10);
            state.updateInvalidBoard(new BitBoard(32, 32));
            state.move(Movement.EAST); // leave claimed area, start claiming
            state.move(Movement.WEST); // return inside

            assertFalse(state.isClaiming(), "Bot should stop claiming after returning inside");
            assertTrue(state.getClaimedBoard().getBit(11, 10), "Claimed board should include claimed territory");
            assertTrue(state.getClaimedBoard().getBit(10, 10), "Starting tile should still be claimed");
            assertTrue(state.getCurrentPosition().getBit(10, 10), "Current position should be back on the start tile");
            assertEquals(0L, state.getClaimingBoard().getRow(10), "Claiming board should be cleared after claim finalization");
        }

        @Test
        void move_shouldKillBotOnInvalidMove() {
            BotState state = new BotState(32, 32, 0, 0);

            state.move(Movement.NORTH);

            assertTrue(state.isDead(), "Bot should die when moving off the board");
            assertFalse(state.getCurrentPosition().getBit(0, 0), "Current position should be cleared on death");
            assertEquals(0L, state.getClaimingBoard().getRow(0), "Claiming board should be cleared on death");
        }

        @Test
        void move_shouldIgnoreMovesAfterDeath() {
            BotState state = new BotState(32, 32, 0, 0);
            state.kill();

            state.move(Movement.EAST);

            assertTrue(state.isDead(), "Bot should remain dead");
            assertEquals(0L, state.getCurrentPosition().getRow(0), "Dead bot should not move");
        }
    }

    @Nested
    @DisplayName("Kill Tests")
    class KillTests {

        @Test
        void kill_shouldClearState() {
            BotState state = new BotState(32, 32, 4, 4);
            state.getClaimingBoard().setBit(5, 4);

            state.kill();

            assertTrue(state.isDead(), "Bot should be dead after kill()");
            assertFalse(state.isClaiming(), "Bot should not be claiming after kill()");
            assertEquals(0L, state.getCurrentPosition().getRow(4), "Current position should be cleared");
            assertEquals(0L, state.getClaimingBoard().getRow(4), "Claiming board should be cleared");
        }
    }

    @Nested
    @DisplayName("Invalid Board")
    class InvalidBoardTests {

        @Test
        void updateInvalidBoard_shouldAffectValidMoves() {
            BotState state = new BotState(32, 32, 5, 5);
            BitBoard invalid = new BitBoard(32, 32);
            invalid.setBit(5, 4);
            state.updateInvalidBoard(invalid);

            assertFalse(state.validMove(Movement.NORTH), "Move should be invalid if it intersects updated invalid board");
        }

        @Test
        void invalidBoard_shouldHoldValues() {
            BotState state = new BotState(32, 32, 5, 5);
            BitBoard invalid = new BitBoard(32, 32);
            invalid.setBit(5, 4);
            state.updateInvalidBoard(invalid);
            assertEquals(invalid, state.getInvalidBoard(), "getInvalidBoard should return the board that was set with updateInvalidBoard");
        }
    }

    @Nested
    @DisplayName("Visual Grid Tests")
    class VisualGridTests {

        @Test
        void getVisualGrid_shouldRenderCorrectSymbols() {
            BotState state = new BotState(4, 4, 1, 1);
            state.getClaimingBoard().setBit(2, 1);
            state.getClaimedBoard().setBit(0, 0);

            String grid = state.getVisualGrid();

            assertTrue(grid.contains("P "), "Visual grid should contain the player symbol");
            assertTrue(grid.contains("+ "), "Visual grid should contain the claiming symbol");
            assertTrue(grid.contains("# "), "Visual grid should contain the claimed symbol");
            assertTrue(grid.contains(". "), "Visual grid should contain empty cells");
        }
    }
}