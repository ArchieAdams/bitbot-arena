package uk.ac.york.bitbotarena;

import static uk.ac.york.bitbotarena.Main.floodFill;

public class BotState {

    BitBoard claimedBoard;
    BitBoard claimingBoard;
    BitBoard currentPosition;
    BitBoard invalidBoard;
    boolean claiming;
    boolean dead;
    int height;
    int width;

    public BotState(int width, int height, int startX, int startY) {
        this.claimingBoard = new BitBoard(width, height);
        this.currentPosition = new BitBoard(width, height);
        this.currentPosition.setBit(startX, startY);
        this.claimedBoard = this.currentPosition.copy();
        this.claiming = false;
        this.dead = false;
        this.height = height;
        this.width = width;
    }

    public void updateInvalidBoard(BitBoard invalidBoard) {
        this.invalidBoard = invalidBoard;
    }

    public void move(Movement movement) {
        if (dead) return;
        if (validMove(movement)) {
            BitBoard nextPosition = currentPosition.shiftOutput(movement, 1);
            if (nextPosition.orOutput(claimedBoard).equals(claimedBoard)) {
                //Inside claim
                if (claiming) {
                    claimedBoard.or(claimingBoard);
                    claimingBoard.clearBoard();
                    claimedBoard = floodFill(claimedBoard);
                    claimedBoard.and(invalidBoard.notOutput());
                    claiming = false;
                }
            } else {
                //Outside claim
                if (!claiming) {
                    claiming = true;
                }
                claimingBoard.or(nextPosition);
            }
            this.currentPosition = nextPosition;
        } else {
            this.kill();
        }
    }

    public String getVisualGrid() {
        StringBuilder sb = new StringBuilder();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (currentPosition.getBit(x, y)) sb.append("P ");
                else if (claimingBoard.getBit(x, y)) sb.append("+ ");
                else if (claimedBoard.getBit(x, y)) sb.append("# ");
                else sb.append(". ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    public boolean validMove(Movement movement) {
        BitBoard newPosition = currentPosition.shiftOutput(movement, 1);
        boolean isNotCuttingHeadOff = !newPosition.intersects(claimingBoard);
        boolean isOnBoard = !newPosition.isEmpty();
        boolean isNotInvalid = invalidBoard == null || !newPosition.intersects(invalidBoard);
        return isNotCuttingHeadOff && isOnBoard && isNotInvalid;
    }

    public boolean isDead() {
        return dead;
    }

    public void kill() {
        dead = true;
        claiming = false;
        currentPosition.clearBoard();
        claimingBoard.clearBoard();
    }

    public boolean isClaiming() {
        return claiming;
    }

    public BitBoard getCurrentPosition() {
        return currentPosition;
    }

    public BitBoard getClaimedBoard() {
        return claimedBoard;
    }

    public BitBoard getClaimingBoard() {
        return claimingBoard;
    }

    public BitBoard getInvalidBoard() {
        return invalidBoard;
    }
}
