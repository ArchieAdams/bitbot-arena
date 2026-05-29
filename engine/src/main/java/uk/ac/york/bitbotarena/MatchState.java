package uk.ac.york.bitbotarena;

public class MatchState {
    int width;
    int height;
    private int tick;
    private final BotEntity[] bots;

    public MatchState(int width, int height, BotEntity[] bots) {
        this.width = width;
        this.height = height;
        this.tick = 0;
        this.bots = bots;
    }

    public void incrementTick() {
        this.tick++;
    }

    public BotEntity[] getBots() {
        return bots;
    }

    public BotEntity getBot(byte index) {
        return bots[index];
    }

    public int getTick() {
        return tick;
    }
}
