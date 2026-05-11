package uk.ac.york.bitbotarena;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.function.BiConsumer;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
@DisplayName("BitBoard Engine Core")
class BitBoardTest {

    @Nested
    @DisplayName("ConstructorTests")
    class ConstructorTests {

        @Test
        void constructor_shouldSetCorrectDimensions() {
            int expectedWidth = 32;
            int expectedHeight = 34;

            BitBoard board = new BitBoard(expectedWidth, expectedHeight);

            assertEquals(expectedWidth, board.getWidth(), "Width did not match constructor input");
            assertEquals(expectedHeight, board.getHeight(), "Height did not match constructor input");
        }

        @Test
        void constructor_shouldThrowOnInvalidWidth() {
            assertThrows(IllegalArgumentException.class, () -> new BitBoard(65, 32));
        }
    }

    @Nested
    @DisplayName("Bit Manipulation Tests")
    class BitManipulationTests {
        @ParameterizedTest(name = "Testing coordinate x={0}, y={1}")
        @CsvSource({
                "0, 0",
                "15, 15",
                "31, 31",
                "0, 31",
                "31, 0"
        })
        void setBit_and_getBit_shouldWorkForEdgesAndMiddle(int x, int y) {
            BitBoard board = new BitBoard(32, 32);

            boolean startBit = board.getBit(x, y);
            board.setBit(x, y);
            boolean endBit = board.getBit(x, y);

            assertFalse(startBit, "Bit should initially be false at (" + x + "," + y + ")");
            assertTrue(endBit, "Bit should be true after setting at (" + x + "," + y + ")");
        }

        @ParameterizedTest(name = "Testing coordinate x={0}, y={1}")
        @CsvSource({
                "-1, 0",
                "0, -1",
                "32, 0",
                "0, 32",
        })
        void setBit_shouldThrowOnInvalidCoordinates(int x, int y) {
            BitBoard board = new BitBoard(32, 32);
            assertThrows(IndexOutOfBoundsException.class, () -> board.setBit(x, y));
        }

        @ParameterizedTest(name = "Testing coordinate x={0}, y={1}")
        @CsvSource({
                "-1, 0",
                "0, -1",
                "32, 0",
                "0, 32",
        })
        void getBit_shouldThrowOnInvalidCoordinates(int x, int y) {
            BitBoard board = new BitBoard(32, 32);
            assertThrows(IndexOutOfBoundsException.class, () -> board.getBit(x, y));
        }
    }

    @Nested
    @DisplayName("Row Manipulation Tests")
    class RowManipulationTests {
        @ParameterizedTest(name = "Testing value {0} and row {1}")
        @CsvSource({
                "1, 0",
                "2147483648, 15",         // 1L << 31
                "4294967295, 31"          // (1L << 32) - 1L
        })
        void setRow_and_getRow_shouldWorkCorrectly(long value, int y) {
            BitBoard board = new BitBoard(32, 32);

            long startRow = board.getRow(y);
            board.setRow(value, y);
            long endRow = board.getRow(y);

            assertEquals(0L, startRow, "Row should initially be 0 at row " + y + " but was " + startRow);
            assertEquals(value, endRow, "Row should be " + value + " after setting row " + y + " but was " + endRow);
        }

        @ParameterizedTest(name = "Testing value {0} and row {1}")
        @CsvSource({
                "4294967296, 0",          // 1L << 32
                "4294967296, 15",         // 1L << 32
                "4294967296, 31"          // 1L << 32
        })
        void setRow_shouldMaskOffHigherNumbers(long value, int y) {
            BitBoard board = new BitBoard(32, 32);

            board.setRow(value, y);
            assertEquals(0L, board.getRow(y), "Row should have higher bits masked off at row " + y);
        }

        @Test
        void getRowMask_with64length() {
            BitBoard board = new BitBoard(64, 32);
            assertEquals(-1L, board.getRowMask(), "Row mask should be -1");
        }
    }

    @Nested
    @DisplayName("Clearing Tests")
    class ClearingTests {
        @ParameterizedTest(name = "Testing coordinate x={0}, y={1}")
        @CsvSource({
                "0,0",
                "15,15",
                "31,31",
                "31,0",
                "0,31"
        })
        void clearBit_shouldWorkCorrectly(int x, int y) {
            BitBoard board = new BitBoard(32, 32);
            board.setBit(x, y);
            boolean startBit = board.getBit(x, y);
            board.clearBit(x, y);
            boolean endBit = board.getBit(x, y);

            assertTrue(startBit, "Bit should be true after setting at (" + x + "," + y + ")");
            assertFalse(endBit, "Bit should be false after clearing at (" + x + "," + y + ")");
        }

        @ParameterizedTest(name = "Testing coordinate x={0}, y={1}")
        @CsvSource({
                "-1, 0",
                "0, -1",
                "32, 0",
                "0, 32",
        })
        void clearBit_shouldThrowOnInvalidCoordinates(int x, int y) {
            BitBoard board = new BitBoard(32, 32);
            assertThrows(IndexOutOfBoundsException.class, () -> board.clearBit(x, y));
        }


        @ParameterizedTest(name = "Testing row {0}")
        @CsvSource({
                "0",
                "15",
                "31",
        })
        void clearBoard_shouldWorkCorrectly(int y) {
            BitBoard board = new BitBoard(32, 32);
            board.setRow(-1L, y);
            assertEquals(board.getRowMask(), board.getRow(y), "Row " + y + " should be full of bits");
            board.clearBoard();
            assertEquals(0L, board.getRow(y), "Row " + y + " should be cleared");
        }
    }

    @Nested
    @DisplayName("Equality and Copy Tests")
    class EqualityAndCopyTests {
        @ParameterizedTest(name = "{3}")
        @MethodSource("provideEqualsScenarios")
        void equals_shouldWorkCorrectly(BitBoard board1, Object board2, boolean expectedResult, String testName) {
            boolean actualResult = board1.equals(board2);
            assertEquals(expectedResult, actualResult, testName + " failed.");
        }

        private static Stream<Arguments> provideEqualsScenarios() {
            BitBoard boardA = new BitBoard(32, 32);
            boardA.setBit(5, 5);
            boardA.setBit(10, 10);

            BitBoard boardB = new BitBoard(32, 32);
            boardB.setBit(5, 5);
            boardB.setBit(10, 10);

            BitBoard boardC = new BitBoard(32, 32);

            BitBoard boardD = new BitBoard(31, 32);
            BitBoard boardE = new BitBoard(32, 31);

            return Stream.of(
                    Arguments.of(boardA, boardA, true, "Identity: Board should equal itself"),
                    Arguments.of(boardA, boardB, true, "Equality: Board should equal identical board"),
                    Arguments.of(boardA, boardC, false, "Inequality: Boards should not be equal"),
                    Arguments.of(boardA, null, false, "Null Check: Board should not equal null"),
                    Arguments.of(boardA, new Object(), false, "Null Check: Board should not equal object of different type"),
                    Arguments.of(boardA, boardD, false, "Dimension Mismatch: Boards of different widths should not be equal"),
                    Arguments.of(boardA, boardE, false, "Dimension Mismatch: Boards of different heights should not be equal")
            );
        }

        @Test
        void copy_shouldCreateDeepCopy() {
            BitBoard original = new BitBoard(32, 32);
            original.setBit(5, 5);
            original.setBit(10, 10);

            BitBoard copiedBoard = original.copy();
            assertEquals(original, copiedBoard, "The copied board should have the exact same state as the original");
            assertNotSame(original, copiedBoard, "The copied board should be a completely separate object in memory");
            copiedBoard.setBit(15, 15);

            assertNotEquals(original, copiedBoard, "Mutating the copy should NOT alter the original board");
            assertFalse(original.getBit(15, 15), "The original board should not have the newly set bit");
        }
    }

    @Nested
    @DisplayName("To String Tests")
    class ToStringTests{
        @Test
        void toString_shouldVisuallyRepresentBoard() {
            BitBoard board = new BitBoard(4, 4);

            String emptyVisual = board.toString();
            assertTrue(emptyVisual.contains("0 0 0 0 \n"), "Empty board should just be zeroes");

            board.setBit(0, 0);
            String topLeftVisual = board.toString();

            assertTrue(topLeftVisual.startsWith("1 0 0 0 \n"), "Bit at (0,0) should print in the top-left corner");

            board.setBit(3, 3);
            String bottomRightVisual = board.toString();

            assertTrue(bottomRightVisual.endsWith("0 0 0 1 \n"), "Bit at (3,3) should print in the bottom-right corner");
        }
    }

    @Nested
    @DisplayName("Operation Tests")
    class OperationTests {
        private static BitBoard createEmptyBoard() {
            return new BitBoard(32, 32);
        }

        private static BitBoard createFullBoard() {
            BitBoard b = new BitBoard(32, 32);
            for (int i = 0; i < 32; i++) {
                b.setRow(-1L, i);
            }
            return b;
        }

        private static BitBoard createLeftHalfBoard() {
            BitBoard b = new BitBoard(32, 32);
            for (int i = 0; i < 16; i++) {
                b.setRow(-1L, i);
            }
            return b;
        }

        private static BitBoard createRightHalfBoard() {
            BitBoard b = new BitBoard(32, 32);
            for (int i = 16; i < 32; i++) {
                b.setRow(-1L, i);
            }
            return b;
        }

        private static BitBoard createDiagonalBoard() {
            BitBoard b = new BitBoard(32, 32);
            for (int i = 0; i < 32; i++) {
                b.setBit(i, i);
            }
            return b;
        }

        @ParameterizedTest(name = "{3}")
        @MethodSource("provideAndScenarios")
        void and_shouldWorkCorrectly(BitBoard b1, BitBoard b2, BitBoard expected, String testName) {
            b1.and(b2);
            assertEquals(expected, b1, testName);
        }

        private static Stream<Arguments> provideAndScenarios() {
            return Stream.of(
                    Arguments.of(createFullBoard(), createFullBoard(), createFullBoard(), "Full AND Full = Full"),
                    Arguments.of(createFullBoard(), createEmptyBoard(), createEmptyBoard(), "Full AND Empty = Empty"),
                    Arguments.of(createEmptyBoard(), createEmptyBoard(), createEmptyBoard(), "Empty AND Empty = Empty"),
                    Arguments.of(createLeftHalfBoard(), createRightHalfBoard(), createEmptyBoard(), "Left Half AND Right Half = Empty"),
                    Arguments.of(createFullBoard(), createDiagonalBoard(), createDiagonalBoard(), "Full AND Diagonal = Diagonal")
            );
        }

        @ParameterizedTest(name = "Testing board sizes w1={0}, h1={1} and w2={2}, h2={3}")
        @CsvSource({
                "32, 32, 31, 31",
                "31, 31, 32, 32",
                "32, 32, 31, 32",
                "32, 32, 32, 31"
        })
        void and_shouldThrowOnBoardSizeMismatch(int w1, int h1, int w2, int h2) {
            BitBoard b1 = new BitBoard(w1, h1);
            BitBoard b2 = new BitBoard(w2, h2);
            assertThrows(IllegalArgumentException.class, () -> b1.and(b2));
        }

        @ParameterizedTest(name = "{3}")
        @MethodSource("provideOrScenarios")
        void or_shouldWorkCorrectly(BitBoard b1, BitBoard b2, BitBoard expected, String testName) {
            b1.or(b2);
            assertEquals(expected, b1, testName);
        }

        private static Stream<Arguments> provideOrScenarios() {
            return Stream.of(
                    Arguments.of(createFullBoard(), createFullBoard(), createFullBoard(), "Full OR Full = Full"),
                    Arguments.of(createFullBoard(), createEmptyBoard(), createFullBoard(), "Full OR Empty = Full"),
                    Arguments.of(createEmptyBoard(), createEmptyBoard(), createEmptyBoard(), "Empty OR Empty = Empty"),
                    Arguments.of(createLeftHalfBoard(), createRightHalfBoard(), createFullBoard(), "Left Half OR Right Half = Full"),
                    Arguments.of(createFullBoard(), createDiagonalBoard(), createFullBoard(), "Full OR Diagonal = Full")
            );
        }

        @ParameterizedTest(name = "Testing board sizes w1={0}, h1={1} and w2={2}, h2={3}")
        @CsvSource({
                "32, 32, 31, 31",
                "31, 31, 32, 32",
                "32, 32, 31, 32",
                "32, 32, 32, 31"
        })
        void or_shouldThrowOnBoardSizeMismatch(int w1, int h1, int w2, int h2) {
            BitBoard b1 = new BitBoard(w1, h1);
            BitBoard b2 = new BitBoard(w2, h2);
            assertThrows(IllegalArgumentException.class, () -> b1.or(b2));
        }

        @ParameterizedTest(name = "{3}")
        @MethodSource("provideXorScenarios")
        void xor_shouldWorkCorrectly(BitBoard b1, BitBoard b2, BitBoard expected, String testName) {
            b1.xor(b2);
            assertEquals(expected, b1, testName);
        }

        private static Stream<Arguments> provideXorScenarios() {
            return Stream.of(
                    Arguments.of(createFullBoard(), createFullBoard(), createEmptyBoard(), "Full XOR Full = Empty"),
                    Arguments.of(createFullBoard(), createEmptyBoard(), createFullBoard(), "Full XOR Empty = Full"),
                    Arguments.of(createEmptyBoard(), createEmptyBoard(), createEmptyBoard(), "Empty XOR Empty = Empty"),
                    Arguments.of(createLeftHalfBoard(), createRightHalfBoard(), createFullBoard(), "Left Half XOR Right Half = Full")
            );
        }

        @ParameterizedTest(name = "Testing board sizes w1={0}, h1={1} and w2={2}, h2={3}")
        @CsvSource({
                "32, 32, 31, 31",
                "31, 31, 32, 32",
                "32, 32, 31, 32",
                "32, 32, 32, 31"
        })
        void xor_shouldThrowOnBoardSizeMismatch(int w1, int h1, int w2, int h2) {
            BitBoard b1 = new BitBoard(w1, h1);
            BitBoard b2 = new BitBoard(w2, h2);
            assertThrows(IllegalArgumentException.class, () -> b1.xor(b2));
        }

        @ParameterizedTest(name = "{3}")
        @MethodSource("provideNotScenarios")
        void not_shouldWorkCorrectly(BitBoard b1, BitBoard expected, String testName) {
            b1.not();
            assertEquals(expected, b1, testName);
        }

        private static Stream<Arguments> provideNotScenarios() {
            return Stream.of(
                    Arguments.of(createFullBoard(), createEmptyBoard(), "!Full = Empty"),
                    Arguments.of(createEmptyBoard(), createFullBoard(), "!Empty = Full"),
                    Arguments.of(createLeftHalfBoard(), createRightHalfBoard(), "!Left Half = Right Half ")
            );
        }

        @Test
        void not_shouldNotFlipInvisiblePaddingBits() {
            BitBoard board = new BitBoard(32, 32);
            board.not();

            long expectedRow = (1L << 32) - 1L;

            assertEquals(expectedRow, board.getRow(0), "not() leaked and flipped bits outside the 32-width boundary!");
        }
    }

    @Nested
    @DisplayName("Shift Operation Tests")
    class ShiftOperationTests {

        @ParameterizedTest(name = "[{6}] Start({0},{1}), Shift:{2} -> End({3},{4})")
        @MethodSource("provideStandardShifts")
        void shift_shouldMoveBitsCorrectly(int startX, int startY, int shiftAmt, int expectedX, int expectedY, BiConsumer<BitBoard, Integer> shiftOperation, String directionName) {
            BitBoard board = new BitBoard(32, 32);
            board.setBit(startX, startY);

            shiftOperation.accept(board, shiftAmt);

            assertTrue(board.getBit(expectedX, expectedY), "Bit should be at (" + expectedX + ", " + expectedY + ") after shifting " + directionName);
            assertFalse(board.getBit(startX, startY), "Start position should be empty after shifting " + directionName);
        }

        private static Stream<Arguments> provideStandardShifts() {
            return Stream.of(
                    Arguments.of(15, 15, 5, 20, 15, (BiConsumer<BitBoard, Integer>) BitBoard::shiftEast, "East"),
                    Arguments.of(15, 15, 5, 10, 15, (BiConsumer<BitBoard, Integer>) BitBoard::shiftWest, "West"),
                    Arguments.of(15, 15, 5, 15, 10, (BiConsumer<BitBoard, Integer>) BitBoard::shiftNorth, "North"),
                    Arguments.of(15, 15, 5, 15, 20, (BiConsumer<BitBoard, Integer>) BitBoard::shiftSouth, "South")
            );
        }

        @ParameterizedTest(name = "[{3}] Pushing bit off edge at ({0},{1})")
        @MethodSource("provideAnnihilationEdges")
        void shift_shouldAnnihilateBitsOffEdge(int edgeX, int edgeY, BiConsumer<BitBoard, Integer> shiftOperation, String directionName) {
            BitBoard board = new BitBoard(32, 32);
            board.setBit(edgeX, edgeY);

            shiftOperation.accept(board, 1);

            assertFalse(board.getBit(edgeX, edgeY), "Bit should be destroyed after shifting off edge " + directionName);

            BitBoard emptyBoard = new BitBoard(32, 32);
            assertEquals(emptyBoard, board, "Board should be completely empty after annihilation");
        }

        private static Stream<Arguments> provideAnnihilationEdges() {
            return Stream.of(
                    Arguments.of(31, 15, (BiConsumer<BitBoard, Integer>) BitBoard::shiftEast, "East"),
                    Arguments.of(0, 15, (BiConsumer<BitBoard, Integer>) BitBoard::shiftWest, "West"),
                    Arguments.of(15, 0, (BiConsumer<BitBoard, Integer>) BitBoard::shiftNorth, "North"),
                    Arguments.of(15, 31, (BiConsumer<BitBoard, Integer>) BitBoard::shiftSouth, "South")
            );
        }

        @Test
        void shiftEast_shouldNotLeaveGhostBits() {
            BitBoard board = new BitBoard(32, 32);
            board.setBit(31, 15);

            board.shiftEast(1);
            board.shiftWest(1);

            assertFalse(board.getBit(31, 15), "Bit should have been destroyed off the East edge, but a ghost returned!");
            assertEquals(0L, board.getRow(15), "Row 15 should be completely empty");
        }

        @Test
        void shiftWest_shouldNotPullGhostBits() {
            BitBoard board = new BitBoard(32, 32);
            board.setBit(0, 15);

            board.shiftWest(1);
            board.shiftEast(1);

            assertFalse(board.getBit(0, 15), "Bit should have been destroyed off the West edge, but a ghost returned!");
            assertEquals(0L, board.getRow(15), "Row 15 should be completely empty");
        }

        @Test
        void shiftNorth_shouldNotLeaveDuplicateRowGhosts() {
            BitBoard board = new BitBoard(32, 32);
            long fullRow = (1L << 32) - 1L;
            board.setRow(fullRow, 0);

            board.shiftNorth(1);
            board.shiftSouth(1);

            assertEquals(0L, board.getRow(0), "Row 0 should be fully empty after shifting back and forth");
        }


        @Test
        void shiftSouth_shouldNotLeaveDuplicateRowGhosts() {
            BitBoard board = new BitBoard(32, 32);
            long fullRow = (1L << 32) - 1L;
            board.setRow(fullRow, 31);

            board.shiftSouth(1);
            board.shiftNorth(1);

            assertEquals(0L, board.getRow(31), "Row 31 should be fully empty after shifting back and forth");
        }
    }
}