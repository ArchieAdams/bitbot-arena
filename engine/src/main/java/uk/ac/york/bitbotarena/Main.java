package uk.ac.york.bitbotarena;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class Main {
    private static final int BOARD_WIDTH = 10;
    private static final int BOARD_HEIGHT = 10;

    public static void main(String[] args) {
        MatchEngine matchEngine = new MatchEngine(BOARD_WIDTH,BOARD_HEIGHT,4);
        int turn=0;
        //LocalDateTime start = LocalDateTime.now();
//        LocalDateTime turnTime;
//        StringBuilder turnTimes = new StringBuilder();
        while (!matchEngine.isGameOver()) {
            //turnTime = LocalDateTime.now();
            turn++;
//            try {
//                Thread.sleep(0);
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
            matchEngine.executeTick();
            //System.out.print(String.format("\033[%dA", 14));
            //turnTimes.append("Turn ").append(turn).append(" time: ").append(ChronoUnit.MICROS.between(turnTime, LocalDateTime.now())).append(" μs\n");

            if (turn==100) {
                break;
            }
        }
//        System.out.println("Game Over in " + turn + " turns!");
//        LocalDateTime end = LocalDateTime.now();
//        System.out.println("Total Time: " + ChronoUnit.MILLIS.between(start,end) + " ms");
//        System.out.println(turnTimes);
//        System.out.println(matchEngine);
//        matchEngine.printFinalScoreboard();
    }

    private static BitBoard floodBoard = new BitBoard(BOARD_WIDTH+2,BOARD_HEIGHT+2);
    public static BitBoard floodFill(BitBoard claimed) {
        int floodBoardWidth = claimed.getWidth() + 2;
        int floodBoardHeight = claimed.getHeight() + 2;
        floodBoard.clearBoard();
        floodBoard.setRow(-1L, 0);
        floodBoard.setRow(-1L, floodBoardHeight - 1);
        floodBoard.setColumn(-1L, 0);
        floodBoard.setColumn(-1L, floodBoardWidth - 1);
        //System.out.println(floodBoard);
        BitBoard paddedClaim = padBoard(claimed);

        //System.out.println(paddedClaim);
        BitBoard notShiftedClaim = paddedClaim.copy();
        notShiftedClaim.not();

        BitBoard current = floodBoard.copy();
        BitBoard next = current.copy();

        while (true) {
            BitBoard north = current.copy();
            north.shiftNorth(1);
            BitBoard south = current.copy();
            south.shiftSouth(1);
            BitBoard east = current.copy();
            east.shiftEast(1);
            BitBoard west = current.copy();
            west.shiftWest(1);

            next.or(north);
            next.or(south);
            next.or(east);
            next.or(west);

            next.and(notShiftedClaim);
            //System.out.println(current);

            if (next.equals(current)) {
                break;
            }
            current = next.copy();
        }
        current.not();

        return shrinkBoard(current);
    }

    public static BitBoard shrinkBoard(BitBoard board) {
        int shrunkWidth = board.getWidth() - 2;
        int shrunkHeight = board.getHeight() - 2;
        if (shrunkWidth <= 0 || shrunkHeight <= 0) {
            return new BitBoard(0, 0);
        }
        BitBoard copiedBody = board.copy();
        BitBoard shrunkBoard = new BitBoard(shrunkWidth, shrunkHeight);
        copiedBody.shiftNorth(1);
        copiedBody.shiftWest(1);
        for (int i = 0; i < shrunkBoard.getHeight(); i++) {
            shrunkBoard.setRow(copiedBody.getRow(i) & shrunkBoard.getRowMask(), i);
        }
        return shrunkBoard;
    }

    public static BitBoard padBoard(BitBoard board) {
        int paddedWidth = board.getWidth() + 2;
        int paddedHeight = board.getHeight() + 2;
        if (paddedWidth > 64) {
            throw new IllegalArgumentException("Padded width cannot exceed 64");
        }
        BitBoard paddedBoard = new BitBoard(paddedWidth, paddedHeight);
        long mask = paddedBoard.getRowMask();

        for (int y = 0; y < board.getHeight(); y++) {
            long row = board.getRow(y);
            paddedBoard.setRow((row << 1) & mask, y + 1);
        }
        return paddedBoard;
    }
}
