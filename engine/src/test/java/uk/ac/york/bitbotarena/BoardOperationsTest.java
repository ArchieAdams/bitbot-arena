package uk.ac.york.bitbotarena;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("BoardOperations Tests")
class BoardOperationsTest {

    @Nested
    @DisplayName("padBoard Tests")
    class PadBoardTests {

        @Test
        void padBoard_shouldIncreaseDimensionsByTwo() {
            BitBoard board = new BitBoard(4, 3);

            BitBoard padded = BoardOperations.padBoard(board);

            assertEquals(6, padded.getWidth(), "Width should increase by 2");
            assertEquals(5, padded.getHeight(), "Height should increase by 2");
        }

        @Test
        void padBoard_shouldShiftOriginalContentByOneInBothAxes() {
            BitBoard board = new BitBoard(4, 4);
            board.setBit(0, 0);
            board.setBit(3, 3);

            BitBoard padded = BoardOperations.padBoard(board);

            assertTrue(padded.getBit(1, 1), "Original (0,0) should appear at (1,1)");
            assertTrue(padded.getBit(4, 4), "Original (3,3) should appear at (4,4)");
            assertFalse(padded.getBit(0, 0), "Top-left border should remain empty");
        }

        @Test
        void padBoard_shouldThrowWhenPaddedWidthExceeds64() {
            BitBoard board = new BitBoard(63, 5);

            assertThrows(IllegalArgumentException.class, () -> BoardOperations.padBoard(board),
                    "Padded width > 64 should throw");
        }
    }

    @Nested
    @DisplayName("shrinkBoard Tests")
    class ShrinkBoardTests {

        @Test
        void shrinkBoard_shouldReduceDimensionsByTwo() {
            BitBoard board = new BitBoard(8, 6);

            BitBoard shrunk = BoardOperations.shrinkBoard(board);

            assertEquals(6, shrunk.getWidth(), "Width should decrease by 2");
            assertEquals(4, shrunk.getHeight(), "Height should decrease by 2");
        }

        @Test
        void shrinkBoard_shouldExtractBodyWithoutBorder() {
            BitBoard board = new BitBoard(6, 6);
            board.setBit(1, 1); // maps to (0,0) in shrunk
            board.setBit(4, 4); // maps to (3,3) in shrunk

            BitBoard shrunk = BoardOperations.shrinkBoard(board);

            assertTrue(shrunk.getBit(0, 0), "Inner (1,1) should map to (0,0)");
            assertTrue(shrunk.getBit(3, 3), "Inner (4,4) should map to (3,3)");
        }

        @ParameterizedTest(name = "shrink {0}x{1} should return empty board")
        @CsvSource({
                "2,2",
                "1,5",
                "5,1",
                "0,0"
        })
        void shrinkBoard_smallDimensionsShouldReturnEmptyBoard(int width, int height) {
            BitBoard board = new BitBoard(width, height);

            BitBoard shrunk = BoardOperations.shrinkBoard(board);

            assertEquals(0, shrunk.getWidth(), "Shrunk width should be 0");
            assertEquals(0, shrunk.getHeight(), "Shrunk height should be 0");
        }
    }

    @Nested
    @DisplayName("floodFill Tests")
    class FloodFillTests {

        @Test
        void floodFill_emptyBoardShouldRemainEmpty() {
            BitBoard claimed = new BitBoard(8, 8);

            BitBoard result = BoardOperations.floodFill(claimed);

            BitBoard empty = new BitBoard(8, 8);
            assertEquals(empty, result, "Empty board has no enclosed regions, so floodFill should return empty");
        }

        @Test
        void floodFill_fullBoardShouldRemainFull() {
            BitBoard claimed = new BitBoard(8, 8);
            for (int y = 0; y < 8; y++) {
                claimed.setRow((1L << 8) - 1L, y);
            }

            BitBoard result = BoardOperations.floodFill(claimed);

            assertEquals(claimed, result, "Flood fill should preserve already-full claimed board");
        }

        @Test
        void floodFill_closedRingShouldFillEnclosedArea() {
            BitBoard claimed = new BitBoard(8, 8);

            // Build a 4x4 ring from (2,2) to (5,5)
            for (int x = 2; x <= 5; x++) {
                claimed.setBit(x, 2);
                claimed.setBit(x, 5);
            }
            for (int y = 2; y <= 5; y++) {
                claimed.setBit(2, y);
                claimed.setBit(5, y);
            }

            BitBoard result = BoardOperations.floodFill(claimed);

            assertTrue(result.getBit(3, 3), "Interior of a closed ring should be filled");
            assertTrue(result.getBit(4, 4), "Interior should be filled");
            assertFalse(result.getBit(0, 0), "Outside region should remain unclaimed");
        }

        @Test
        void floodFill_shouldNotMutateInputBoard() {
            BitBoard claimed = new BitBoard(8, 8);
            claimed.setBit(1, 1);
            BitBoard originalCopy = claimed.copy();

            BoardOperations.floodFill(claimed);

            assertEquals(originalCopy, claimed, "floodFill should not mutate its input board");
        }

        @Test
        void floodFill_shouldHandleDifferentBoardSizesAcrossCalls() {
            BitBoard small = new BitBoard(8, 8);
            BitBoard big = new BitBoard(16, 16);

            assertDoesNotThrow(() -> BoardOperations.floodFill(small),
                    "First flood fill call should succeed");
            assertDoesNotThrow(() -> BoardOperations.floodFill(big),
                    "Second flood fill on different size should also succeed");
        }
    }
}