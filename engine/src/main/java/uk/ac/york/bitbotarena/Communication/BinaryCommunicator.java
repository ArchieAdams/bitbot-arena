package uk.ac.york.bitbotarena.Communication;

import uk.ac.york.bitbotarena.BotEntity;
import uk.ac.york.bitbotarena.Movement;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;

// This will be limited to 4 players.
public class BinaryCommunicator implements BotCommunicator {

    private static final byte MAGIC_NUMBER = (byte) 0xBB;
    private static final byte START_FRAME = (byte) 0b00000000;
    private static final byte[] GAME_STATE_FRAME = {(byte) 0b01000000, (byte) 0b10000000};
    private static final byte END_FRAME = (byte) 0b11000000;


    private final DataOutputStream os;
    private final DataInputStream is;
    private final byte[] buffer = new byte[516];
    private short currentByteIndex = 0;

    public BinaryCommunicator(Process process) {
        this.os = new DataOutputStream(process.getOutputStream());
        this.is = new DataInputStream(process.getInputStream());
    }

    @Override
    public void sendState(int tick, byte index, BotEntity[] bots) throws IOException {
        writeGameStateHeader(tick, index, bots);

        writeMapToBuffer(bots);

        os.write(buffer);
        os.flush();
        clearBuffer();
    }

    private void writeGameStateHeader(int tick, byte index, BotEntity[] bots) {
        buffer[0] = MAGIC_NUMBER;
        buffer[1] = (byte) (GAME_STATE_FRAME[tick % 2] | index);

        for (BotEntity bot : bots) {
            buffer[2] |= (byte) (bot.getPreviousMove() == null ? 0 : moveEncoder(bot.getPreviousMove()) << (2 * bot.getIndex()));
            if (bot.isDead()) {
                continue;
            }
            byte baseShift = 2;
            buffer[1] |= (byte) (1 << (baseShift + bot.getIndex()));
        }

        for (int i = 0; i < 3; i++) {
            xorForParity(buffer[i]);
        }

        currentByteIndex = 3;
    }

    private void writeMapToBuffer(BotEntity[] bots) {
        int height = 32;
        int width = 32;

        byte mapBuffer = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int bitIndex = y * width + x;
                boolean bigSideOfByte = bitIndex % 2 == 0;

                for (BotEntity bot : bots) {
                    boolean hasClaimedBit = bot.getClaimedBoard().getBit(x, y);
                    boolean isClaimingBit = bot.getClaimingBoard().getBit(x, y);

                    if (hasClaimedBit && isClaimingBit) {
                        throw new IllegalStateException("A cell cannot be both claimed and claiming at the same time!");
                    }

                    //0 - 12
                    //1 - 34
                    //2 - 56
                    //3 - 78

                    if (hasClaimedBit || isClaimingBit) {
                        byte baseBotValue = (byte) (bot.getIndex() * 2);
                        byte adjustedValue = (byte) (baseBotValue + (hasClaimedBit ? 1 : 2));

                        mapBuffer |= (byte) (adjustedValue << (bigSideOfByte ? 4 : 0));
                        mapBuffer |= (byte) (adjustedValue << (bigSideOfByte ? 4 : 0));
                        break;
                    }
                }
                if (!bigSideOfByte) {
                    writeToBuffer(mapBuffer);
                    xorForParity(mapBuffer);
                    mapBuffer = 0;
                }
            }
        }
    }


    private void clearBuffer() {
        Arrays.fill(buffer, (byte) 0);
    }

    private void writeToBuffer(byte data) {
        buffer[currentByteIndex++] = data;
    }

    private void xorForParity(byte data) {
        buffer[515] ^= data;
    }

    private byte moveEncoder(Movement movement) {
        return switch (movement) {
            case NORTH -> 0;
            case EAST -> 1;
            case SOUTH -> 2;
            case WEST -> 3;
        };
    }

    private void writeGameStartToBuffer(byte botIndex, BotEntity[] bots, int[] startingX, int[] startingY, byte tournamentPhase, short[] botELOs, int environmentSeed) {
        writeToBuffer(MAGIC_NUMBER);
        writeToBuffer((byte) (START_FRAME | botIndex));
        writeToBuffer((byte) bots[0].getCurrentPosition().getWidth());
        writeToBuffer((byte) bots[0].getCurrentPosition().getHeight());
        for (int i = 0; i < bots.length; i++) {
            writeToBuffer((byte) startingX[i]);
            writeToBuffer((byte) startingY[i]);
        }
        writeToBuffer(tournamentPhase);
        for (short botELO : botELOs) {
            byte low = (byte) (botELO & 0xFF);
            byte high = (byte) ((botELO >> 8) & 0xFF);
            writeToBuffer(high);
            writeToBuffer(low);
        }

        byte[] byteSeed = intToBytes(environmentSeed);
        writeToBuffer(byteSeed[0]);
        writeToBuffer(byteSeed[1]);
        writeToBuffer(byteSeed[2]);
        writeToBuffer(byteSeed[3]);
    }

    private void writeGameEndToBuffer(byte botIndex, byte winningBot, short[] scores) {
        writeToBuffer(MAGIC_NUMBER);
        writeToBuffer((byte) (END_FRAME | botIndex));
        writeToBuffer(winningBot); // 0-3 for winner, 4 for draw
        for (short score : scores) {
            byte low = (byte) (score & 0xFF);
            byte high = (byte) ((score >> 8) & 0xFF);
            writeToBuffer(high);
            writeToBuffer(low);
        }
    }

    public byte[] intToBytes(int value) {
        return new byte[]{
                (byte) ((value >>> 24) & 0xFF),
                (byte) ((value >>> 16) & 0xFF),
                (byte) ((value >>> 8) & 0xFF),
                (byte) (value & 0xFF)
        };
    }

    @Override
    public Movement readMove() throws IOException {
        return Movement.values()[is.readByte()];
    }
}