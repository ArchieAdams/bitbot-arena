package uk.ac.york.bitbotarena;

import uk.ac.york.bitbotarena.BotControllers.BotController;

public class BotEntity {
    private final BotState state;
    private final BotController controller;
    private int kills = 0;
    private final byte index;
    Movement previousMove = null;

    public BotEntity(BotState state, BotController controller, byte index) {
        this.state = state;
        this.controller = controller;
        this.index = index;
    }

    public BotEntity(int width, int height, int startX, int startY, BotController controller, byte index) {
        this.state = new BotState(width, height, startX, startY);
        this.controller = controller;
        this.index = index;
    }

    public BotState getState() {
        return state;
    }

    public void initController(MatchState state) {
        controller.init(state, index);
    }

    public void gameOver(byte winningBot, short[] scores) {
        controller.gameOver(winningBot, scores, index);
    }

    public boolean isDead() { return state.isDead(); }
    public boolean isClaiming() { return state.isClaiming(); }
    public BitBoard getCurrentPosition() { return state.getCurrentPosition(); }
    public BitBoard getClaimingBoard() { return state.getClaimingBoard(); }
    public BitBoard getClaimedBoard() { return state.getClaimedBoard(); }
    public void updateInvalidBoard(BitBoard invalidBoard) { state.updateInvalidBoard(invalidBoard); }
    public void kill() { state.kill(); }

    public void executeMove(MatchState matchState) {
        Movement move = controller.getMove(matchState, index);
        state.move(move);
        previousMove = move;
    }

    public void killedOtherBot() {
        kills++;
    }

    public int getKills() {
        return kills;
    }

    public byte getIndex() {
        return index;
    }

    public Movement getPreviousMove() {
        return previousMove;
    }

    // TODO test function
    public void setPreviousMove(Movement previousMove) {
        this.previousMove = previousMove;
    }
}