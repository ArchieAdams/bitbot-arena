import java.io.DataInputStream;
import java.io.IOException;

public class FrameReader {
    private final DataInputStream in;
    private final byte[] buffer = new byte[516];
    private static final int[] DX = {0, 1, 0, -1};
    private static final int[] DY = {-1, 0, 1, 0};

    public FrameReader(DataInputStream in) {
        this.in = in;
    }

    /**
     * Reads the next 516 bytes and unpacks them into the reusable view object.
     *
     * @return the frame type.
     */
    public FrameType readNextFrame(GameStateView view) throws IOException {
        in.readFully(buffer);

        checkParity();
        checkMagicNumber();

        int header = buffer[1] & 0xFF;
        int frameType = header & 0xC0;

        return switch (frameType) {
            case 0x00 -> {
                extractGameStart(view);
                yield FrameType.GameStart;
            }
            case 0x40, 0x80 -> {
                if (view.grid == null) {
                    throw new IllegalStateException("Received game state frame before receiving game start frame.");
                }
                extractGameState(view);
                yield FrameType.GameState;
            }
            case 0xC0 -> {
                extractGameEnd(view);
                yield FrameType.GameOver;
            }
            default -> throw new IOException("Unknown frame type received: " + String.format("%02X", header));
        };

    }

    private void extractGameStart(GameStateView view) {
        view.tick = 0;
        extractedIndex(view);
        byte currentIndex = 2;
        view.width = buffer[currentIndex++];
        view.height = buffer[currentIndex++];
        view.createGrid();

        for (int i = 0; i < 4; i++) {
            view.bots[i].x = buffer[currentIndex++];
            view.bots[i].y = buffer[currentIndex++];
        }
        view.tournamentPhase = buffer[currentIndex++];

        for (int i = 0; i < 4; i++) {
            view.bots[i].ELO = readShort(currentIndex);
            currentIndex += 2;
        }

        view.environmentSeed = readInt(currentIndex);
    }

    private void extractGameState(GameStateView view) {
        view.tick++;

        byte aliveMask = 0x04;
        byte moveMask = 0x03;
        int moves = buffer[2] & 0xFF;
        int header = buffer[1] & 0xFF;
        for (int i = 0; i < 4; i++) {
            view.bots[i].isDead = (header & aliveMask) == 0;
            view.bots[i].x += DX[moves & moveMask];
            view.bots[i].y += DY[moves & moveMask];
            aliveMask <<= 1;
            moves >>>= 2;
        }

        // Unpack metadata
        extractedIndex(view);

        // Unpack the 32x32 Grid
        int currentByte = 3;
        for (int y = 0; y < 32; y++) {
            for (int x = 0; x < 32; x += 2) {
                byte data = buffer[currentByte++];

                // Unpack upper 4 bits (Left pixel) and lower 4 bits (Right pixel)
                view.grid[y][x] = (byte) ((data >> 4) & 0x0F);
                view.grid[y][x + 1] = (byte) (data & 0x0F);
            }
        }
    }

    private void extractGameEnd(GameStateView view) {
        view.tick = 0;
        extractedIndex(view);
        byte winner = (byte) ((buffer[1] & 0x1C) >> 2);
        short[] scores = new short[4];
        for (int i = 0; i < 4; i++) {
            scores[i] = readShort(2 + i * 2);
        }
        view.gameOver(winner, scores);
    }

    private void extractedIndex(GameStateView view) {
        byte index = (byte) (buffer[1] & 0x03);
        if (view.myIndex == -1) {
            view.myIndex = index;
        } else if (index != view.myIndex) {
            throw new IllegalStateException("Received frame for bot index " + index + " but expected " + view.myIndex);
        }
    }

    private void checkMagicNumber() throws IOException {
        if (buffer[0] != (byte) 0xBB) {
            throw new IOException("Invalid frame header: " + String.format("%02X", buffer[0]));
        }
    }

    private void checkParity() throws IOException {
        byte parity = 0;
        for (byte b : buffer) {
            parity ^= b;
        }
        if (parity != 0) {
            throw new IOException("Parity check failed for received frame.");
        }
    }

    private short readShort(int startIndex) {
        short result = 0;
        result |= (short) ((buffer[startIndex] & 0xFF) << 8);
        result |= (short) (buffer[startIndex + 1] & 0xFF);
        return result;
    }

    private int readInt(int startIndex) {
        int result = 0;
        result |= (buffer[startIndex] & 0xFF) << 24;
        result |= (buffer[startIndex + 1] & 0xFF) << 16;
        result |= (buffer[startIndex + 2] & 0xFF) << 8;
        result |= (buffer[startIndex + 3] & 0xFF);
        return result;
    }

}