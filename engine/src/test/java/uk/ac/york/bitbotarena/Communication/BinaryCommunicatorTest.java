package uk.ac.york.bitbotarena.Communication;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import uk.ac.york.bitbotarena.BotEntity;
import uk.ac.york.bitbotarena.Movement;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.ac.york.bitbotarena.BotTestHelper.initBots;

@DisplayName("Binary Communicator Tests")
class BinaryCommunicatorTest {

    @Nested
    @DisplayName("sendState Tests")
    class SendState {
        private ByteArrayOutputStream capturedOutput;
        private BinaryCommunicator communicator;
        private BotEntity[] bots;

        @BeforeEach
        void setUp() {
            capturedOutput = new ByteArrayOutputStream();

            Process mockProcess = mock(Process.class);
            when(mockProcess.getOutputStream()).thenReturn(capturedOutput);

            communicator = new BinaryCommunicator(mockProcess);
            bots = initBots();
        }

        @Test
        void sendState_packetLength() throws IOException {
            communicator.sendState(0, (byte) 2, bots);

            byte[] results = capturedOutput.toByteArray();
            assertEquals(516, results.length);
        }

        @Test
        void sendState_magicNumber() throws IOException {
            communicator.sendState(0, (byte) 2, bots);

            byte[] results = capturedOutput.toByteArray();
            assertEquals((byte) 0xBB, results[0]);
        }

        @ParameterizedTest(name = "Tick {0}")
        @ValueSource(bytes = {0, 1, 2, 3})
        void sendState_ticksShouldAlternate(byte tick) throws IOException {
            communicator.sendState(tick, (byte) 0, bots);

            byte[] results = capturedOutput.toByteArray();
            assertEquals((byte) (tick % 2 == 0 ? 0b01000000 : 0b10000000), (byte) (results[1] & 0b11000000));
        }

        @ParameterizedTest(name = "Bot index {0}")
        @ValueSource(bytes = {0, 1, 2, 3})
        void sendState_botIndexCheck(byte index) throws IOException {
            communicator.sendState(0, index, bots);

            byte[] results = capturedOutput.toByteArray();
            assertEquals(index, results[1] & 0b11);
        }

        @ParameterizedTest(name = "Bot alive {0}")
        @ValueSource(bytes = {0, 1, 2, 3})
        void sendState_botAlive(byte index) throws IOException {
            communicator.sendState(0, index, bots);

            byte[] results = capturedOutput.toByteArray();
            byte deathMask = (byte) (1 << (2 + index));
            assertEquals(0, (~results[1] & deathMask), "Expected alive bit to be set for bot " + index);
        }

        @ParameterizedTest(name = "Bot killed {0}")
        @ValueSource(bytes = {0, 1, 2, 3})
        void sendState_botDead(byte index) throws IOException {
            bots[index].kill();
            communicator.sendState(0, index, bots);

            byte[] results = capturedOutput.toByteArray();
            byte deathMask = (byte) (1 << (2 + index));
            assertEquals(0, (results[1] & deathMask), "Expected alive bit to not be set for bot " + index);
        }

        @ParameterizedTest(name = "Testing move {0} for bot {1}")
        @CsvSource({
                "NORTH, 0, 0x00",
                "SOUTH, 1, 0x08",
                "EAST, 2, 0x10",
                "WEST, 3, 0xC0"
        })
        void sendState_packsPreviousMovesProperly(Movement move, byte index, String expectedHex) throws IOException {
            bots[index].setPreviousMove(move);

            communicator.sendState(0, index, bots);
            byte[] results = capturedOutput.toByteArray();

            byte expected = (byte) Integer.decode(expectedHex).intValue();
            assertEquals(expected, results[2], "Expected movement byte to be perfectly packed for bot " + index);
        }

        @Test
        void sendState_invalidBoard() {
            bots[0].getClaimedBoard().setBit(0, 0);
            bots[0].getClaimingBoard().setBit(0, 0);
            assertThrows(IllegalStateException.class, () -> communicator.sendState(0, (byte) 2, bots),
                    "Expected an exception to be thrown if a cell is both claimed and claiming");
        }

        @ParameterizedTest(name = "Testing coordinate x={0}, y={1}, for bot {2}")
        @CsvSource({
                "0, 0, 0, 0x10",
                "0, 0, 1, 0x30",
                "0, 0, 2, 0x50",
                "0, 0, 3, 0x70",
                "15, 15, 0, 0x1",
                "15, 15, 1, 0x3",
                "15, 15, 2, 0x5",
                "15, 15, 3, 0x7",
                "31, 31, 0, 0x1",
                "31, 31, 1, 0x3",
                "31, 31, 2, 0x5",
                "31, 31, 3, 0x7",
        })
        void sendState_claimedBoard(int x, int y, byte index, String expectedHex) throws IOException {
            bots[index].getClaimedBoard().setBit(x, y);
            communicator.sendState(0, index, bots);

            byte[] results = capturedOutput.toByteArray();
            int bufferIndex = (y * 32 + x) / 2 + 3;
            byte expected = (byte) Integer.decode(expectedHex).intValue();
            assertEquals(expected, (results[bufferIndex]), "Expected (" + x + "," + y + ") cell of bot " + index + " to be set in the buffer");
        }

        @ParameterizedTest(name = "Testing coordinate x={0}, y={1}, for bot {2}")
        @CsvSource({
                "0, 0, 0, 0x20",
                "0, 0, 1, 0x40",
                "0, 0, 2, 0x60",
                "0, 0, 3, 0x80",
                "15, 15, 0, 0x2",
                "15, 15, 1, 0x4",
                "15, 15, 2, 0x6",
                "15, 15, 3, 0x8",
                "31, 31, 0, 0x2",
                "31, 31, 1, 0x4",
                "31, 31, 2, 0x6",
                "31, 31, 3, 0x8",
        })
        void sendState_claimingBoard(int x, int y, byte index, String expectedHex) throws IOException {
            bots[index].getClaimingBoard().setBit(x, y);
            communicator.sendState(0, index, bots);

            byte[] results = capturedOutput.toByteArray();
            int bufferIndex = (y * 32 + x) / 2 + 3;
            byte expected = (byte) Integer.decode(expectedHex).intValue();
            assertEquals(expected, (results[bufferIndex]), "Expected (" + x + "," + y + ") cell of bot " + index + " to be set in the buffer");
        }

        @ParameterizedTest(name = "Testing coordinate x={0} (MUST BE EVEN), y={1}, for bot {2} and bot {3}")
        @CsvSource({
                "0, 0, 0, 1, 0x23",
                "0, 0, 1, 0, 0x41",
                "30, 31, 2, 3, 0x67"
        })
        void sendState_mergedNibbles(int evenX, int y, byte index1, byte index2, String expectedHex) throws IOException {
            bots[index1].getClaimingBoard().setBit(evenX, y);
            bots[index2].getClaimedBoard().setBit(evenX + 1, y);

            communicator.sendState(0, index1, bots);

            byte[] results = capturedOutput.toByteArray();
            int bufferIndex = (y * 32 + evenX) / 2 + 3;
            byte expected = (byte) Integer.decode(expectedHex).intValue();

            assertEquals(expected, results[bufferIndex],
                    "Expected merged nibbles for bots " + index1 + " and " + index2 + " at buffer index " + bufferIndex);
        }

        @Test
        void sendState_calculatedParityIsCorrect() throws IOException {
            bots[0].getClaimedBoard().setBit(0, 0);
            bots[1].getClaimingBoard().setBit(5, 5);
            bots[2].setPreviousMove(Movement.EAST);
            communicator.sendState(42, (byte) 1, bots);
            byte[] results = capturedOutput.toByteArray();

            byte parityCheck = 0;
            for (int i = 0; i < 516; i++) {
                parityCheck ^= results[i];
            }

            assertEquals(0, parityCheck, "The total XOR sum of the entire packet must equal 0");
        }

    }
}
