package uk.ac.york.bitbotarena;

public class Main {
    public static void main(String[] args) {
        MatchEngine matchEngine = new MatchEngine(10,10,6);
        while (!matchEngine.isGameOver()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            matchEngine.executeTick();
            System.out.print(String.format("\033[%dA", 14));
        }
    }
    public static BitBoard floodFill(BitBoard claimed) {
        int floodBoardWidth = claimed.getWidth() + 2;
        int floodBoardHeight = claimed.getHeight() + 2;
        BitBoard floodBoard = new BitBoard(floodBoardWidth, floodBoardHeight);
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
