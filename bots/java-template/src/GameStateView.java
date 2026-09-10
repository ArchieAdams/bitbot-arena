public class GameStateView {
    public int tick = Integer.MIN_VALUE;
    public byte myIndex = -1;
    public byte[][] grid; // 0 = empty, 1 = Bot 0 trail, etc.
    public final BotProxy[] bots = new BotProxy[4];
    public byte width;
    public byte height;
    public byte tournamentPhase;
    public int environmentSeed;


    public static class BotProxy {
        public int x, y;
        public boolean isDead;
        public short ELO;
    }

    public GameStateView() {
        for (int i = 0; i < 4; i++) bots[i] = new BotProxy();
    }

    public void createGrid() {
        if (width == 0 || height == 0) return;
        grid = new byte[width][height];
    }

    public void gameOver(byte winner, short[] scores) {
        // Feel free to use however you want
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Tick: ").append(tick).append("\n");
        sb.append("My Index: ").append(myIndex).append("\n");
        sb.append("Tournament Phase: ").append(tournamentPhase).append("\n");
        sb.append("Environment Seed: ").append(environmentSeed).append("\n");
        for (int i = 0; i < bots.length; i++) {
            BotProxy bot = bots[i];
            sb.append("Bot ").append(i).append(": (").append(bot.x).append(", ").append(bot.y).append(") ")
                    .append(bot.isDead ? "DEAD" : "ALIVE").append(" ELO: ").append(bot.ELO).append("\n");
        }
        for (byte[] bytes : grid) {
            for (byte aByte : bytes) {
                sb.append(aByte).append(" ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}