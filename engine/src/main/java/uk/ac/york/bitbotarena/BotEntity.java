package uk.ac.york.bitbotarena;

import uk.ac.york.bitbotarena.BotControllers.BotController;

public class BotEntity {
    private final BotState state;
    private final BotController controller;
    public int kills = 0;

    public BotEntity(BotState state, BotController controller) {
        this.state = state;
        this.controller = controller;
    }

    public BotEntity(int width, int height, int startX, int startY, BotController controller) {
        this.state = new BotState(width, height, startX, startY);
        this.controller = controller;
    }



    public boolean isDead() { return state.isDead(); }
    public boolean isClaiming() { return state.isClaiming(); }
    public BitBoard getCurrentPosition() { return state.getCurrentPosition(); }
    public BitBoard getClaimingBoard() { return state.getClaimingBoard(); }
    public BitBoard getClaimedBoard() { return state.getClaimedBoard(); }
    public void updateInvalidBoard(BitBoard invalidBoard) { state.updateInvalidBoard(invalidBoard); }
    public void kill() { state.kill(); }

    public void executeMove() {
        Movement move = controller.getMove(state);
        state.move(move);
    }

    public void killedOtherBot() {
        kills++;
    }

    public int getKills() {
        return kills;
    }
}